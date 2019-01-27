/*
 *  Copyright (C) 2017. Timo Homburg
 *  This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 3 of the License, or
 *   (at your option) any later version.
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *   You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
 *
 */

package com.github.situx.postagger.main.gui.ime.jquery;

// required by InputMethod

import com.github.situx.postagger.main.gui.ime.JPagedChooser;
import com.github.situx.postagger.main.gui.ime.descriptor.GenericInputMethodDescriptor;
import com.github.situx.postagger.util.Tuple;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodHighlight;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodContext;
import java.text.AttributedString;
import java.util.*;
import java.util.List;

// required by implementation

public class GenericInputMethodOld implements InputMethod {
    private static Locale[] SUPPORTED_LOCALES = {
            GenericInputMethodDescriptor.AKKADIAN,
            //GenericInputMethodDescriptor.HITTITE,
            //GenericInputMethodDescriptor.SUMERIAN,
            Locale.ENGLISH
    };
    private static Window statusWindow;
    // alternatives are rendered in the alternatives window
    // through the below component.
    private JPagedChooser<InputTerm<String>,String> alternativesChooser;
    private List<Tuple<InputTerm<String>,String>> alternatives;
    private Integer termBufferIndex=0;
    private Boolean wordTerminated=true;
    private Integer method=0;
    private Window alternativesWindow;
    /**Buffers uncommitted but converted terms.*/
    private ConvertedTermBuffer<String> convertedTermBuffer;
    private java.util.Map<Integer, ConvertedTermBuffer<String>> builder;
    private InputMethodContext context;
    private GenericInputMethodControl controlobject;
    private InputMethodContext inputMethodContext;
    private JList list;
    private Integer local = 0;
    private Locale locale = GenericInputMethodDescriptor.AKKADIAN;
    private StringBuffer rawInput = new StringBuffer();
    // raw input is rendered through this label
    // in the raw window when showing raw below-the-spot
    private JLabel rawLabel;
    // window for rendering raw input if showing
    // raw below-the-spot
    private Window rawWindow;
    private Integer words = 5;

    public GenericInputMethodOld() {
        this.locale = GenericInputMethodDescriptor.AKKADIAN;
        this.local = 0;
        this.builder = new TreeMap<>();
        this.controlobject = new GenericInputMethodControl();
        /*builder.put(0, new ConvertedTermBuffer<String>(new TreeBuilder(this.getClass().getClassLoader().getResourceAsStream("ime/akkadian.xml"),GenericInputMethodDescriptor.AKKADIAN)));
        builder.put(1, new ConvertedTermBuffer<String>(new TreeBuilder(this.getClass().getClassLoader().getResourceAsStream("ime/hittite.xml"),GenericInputMethodDescriptor.HITTITE)));
        builder.put(2, new ConvertedTermBuffer<String>(new TreeBuilder(this.getClass().getClassLoader().getResourceAsStream("ime/sumerian.xml"),GenericInputMethodDescriptor.SUMERIAN)));
        */this.convertedTermBuffer=builder.get(0);
    }

    public void activate() {
        // Activates the input method for immediate input processing.
        // do nothing
    }

    private void appendRawText(char ch) {
        rawInput.append(ch);
        sendRawText();
    }

    private void clearRaw() {
        this.updateRaw("");
    }

    private void closeAlternatives() {
        this.alternatives = null;
        this.hideAlternativesWindow();
    }

    private void commit(String text) {
        sendConvertedText(text);
        rawInput.setLength(0);
    }

    private void commitRawText() {
        String converted = convert(rawInput);
        this.rawInput.setLength(0);
        converted = converted.substring(converted.indexOf('.') + 1);
        if (converted.split("[A-Za-z0-9]").length > 0)
            converted = converted.split("[A-Za-z0-9]")[0];
        commit(converted);
    }

    /*String[] completedWords() {
        return ((TreeBuilder)builder.get(local).getTermSource()).queryToArray(rawInput.toString(), words);
    } */

    private String convert(StringBuffer rawText) {
        if (rawText.length() == 0) {
            return "";
        }

        InputMethodHighlight highlight;
        String convertedText = rawText.toString();

        return convertedText;
    }

    public void deactivate(boolean isTemporary) {
        // Deactivates the input method.
    }

    public void dispatchEvent(AWTEvent event) {
        if (this.getControlObject().isEnabled()) {
            int eventID = event.getID();

            switch (eventID) {
                case KeyEvent.KEY_PRESSED:
                    KeyEvent e = (KeyEvent) event;
                    handleCharacter(e);
                    break;
                case KeyEvent.KEY_TYPED:
                    this.dispatchKeyTypedEvent((KeyEvent) event);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    this.dispatchMouseReleasedEvent((MouseEvent) event);
                    break;
            }
        }

        // Also handle selection by mouse here
    }

    /**
     * Handle key pressed events.
     * We use key pressed events to capture those key
     * hits that don't produce key pressed events (arrow keys,
     * in particular).
     *
     * @param keyPressedEvent
     */
    private void dispatchKeyPressedEvent(KeyEvent keyPressedEvent) {
        int keyCode = keyPressedEvent.getKeyCode();

        // if the character isn't accepted then we make some
        // kind of indication or something at the end.
        boolean accepted = true;

        // if the character is consumed, then we consume
        // the KeyEvent, otherwise it'll get passed
        // through to the text component.
        boolean consume = true;

        if (keyPressedEvent.getModifiers() != 0) {
            // doesn't interpret key events
            // with modifiers (alt, ctrl, meta).
            // don't consume the event, pass it through.

            consume = false;

        } else if (this.hasRawInput()) {
            // when we have raw input the only key events we care
            // about also produce key typed events.  so we
            // the event and handle the typed event instead.

        } else if (this.isShowingAlternatives()) {
            // if we're showing candidates, then the arrow keys
            // are used to select and page on the term chooser.

            boolean verticalPaging = this.getControlObject().getChooserOrientation();

            if ((!verticalPaging && KeyEvent.VK_UP == keyCode) ||
                    (verticalPaging && KeyEvent.VK_LEFT == keyCode) ||
                    KeyEvent.VK_PAGE_UP == keyCode) {
                // in horizontal mode and up pressed or
                // in vertical mode and left pressed or
                // page up pressed:

                // page the chooser to the previous page
                accepted = this.alternativesChooser.previousPage();

            } else if ((!verticalPaging && KeyEvent.VK_DOWN == keyCode) ||
                    (verticalPaging && KeyEvent.VK_RIGHT == keyCode) ||
                    KeyEvent.VK_PAGE_DOWN == keyCode) {
                // in horizontal mode and down pressed or
                // in vertical mode and right pressed or
                // page down pressed:

                // page the chooser to the next page
                accepted = this.alternativesChooser.nextPage();

            } else if ((!verticalPaging && KeyEvent.VK_LEFT == keyCode) ||
                    (verticalPaging && KeyEvent.VK_UP == keyCode)) {
                // in horizontal mode and left pressed or
                // in vertical mode and up pressed:

                // choose the previous item
                accepted = this.alternativesChooser.previousItem();

            } else if ((!verticalPaging && KeyEvent.VK_RIGHT == keyCode) ||
                    (verticalPaging && KeyEvent.VK_DOWN == keyCode)) {
                // in horizontal mode and right pressed or
                // in vertical mode and down pressed:

                // chose the next item
                accepted = this.alternativesChooser.nextItem();
            }

        } else if (this.hasUncommittedText()) {
            // no raw text, if it has uncommitted text then
            // left and right arrow keys control the cursor selection,
            // while up and down open the chooser to select alternatives
            // for the current selection

            if (KeyEvent.VK_LEFT == keyCode) {
                // left arrow key

                // move the buffer selection to the previous term/item
                if (this.convertedTermBuffer.previous()) {
                    //this.alternatives=alts;
                    this.updateUncommittedText();
                } else {
                    accepted = false;
                }

            } else if (KeyEvent.VK_RIGHT == keyCode) {
                // right arrow key

                // move the buffer selection to the next term/item
                if (this.convertedTermBuffer.next()) {
                    this.updateUncommittedText();
                } else {
                    accepted = false;
                }

            } else if (KeyEvent.VK_UP == keyCode || KeyEvent.VK_DOWN == keyCode ||
                    KeyEvent.VK_PAGE_UP == keyCode || KeyEvent.VK_PAGE_DOWN == keyCode) {
                // up/down/page up/page down
                // see if there are any altenratives to the current selection.
                // if so, open the chooser with those alternatives.

                if (null != alternatives && !alternatives.isEmpty()) {
                    this.updateAlternatives(alternatives, keyPressedEvent);

                } else {
                    // no alternatives
                    accepted = false;
                }
            }

        } else {
            // couldn't interpret the input, pass it through
            // to the text component.

            consume = false;
        }

        if (consume) {
            keyPressedEvent.consume();
        }

        if (!accepted) {
            // key input rejected.
            this.indicateRejectedInput();
        }
    }

    /**
     * @param keyTypedEvent
     */
    private void dispatchKeyTypedEvent(KeyEvent keyTypedEvent) {
        char keyChar = keyTypedEvent.getKeyChar();

        boolean accepted = true;
        boolean consume = true;

        int keyModifiers = keyTypedEvent.getModifiers();
        if (keyModifiers != 0 && keyModifiers != InputEvent.SHIFT_MASK) {
            // doesn't interpret key events with modifiers other than shift.
            // with modifiers (alt, ctrl, meta).
            // don't consume the event, pass it through.
            consume = false;

        } else if (this.hasRawInput()) {
            // there is currently some raw input.
            // we check if it's a control character and if
            // so apply it, otherwise try to add the character
            // to the raw input
            switch (keyChar) {
                case KeyEvent.VK_ENTER:
                    this.commitRawText();
                    break;
                case KeyEvent.VK_BACK_SPACE:
                    this.updateRaw(this.rawInput.substring(0, this.rawInput.length() - 1));
                    break;
                case KeyEvent.VK_ESCAPE:
                case KeyEvent.VK_DELETE:
                    this.clearRaw();
                    break;
                case KeyEvent.VK_SPACE:
                    this.commit(this.rawInput.toString());
                    break;
                default:
                    if (Character.isLetter(keyChar) || Character.isDigit(keyChar)) {
                        //this.rawInput.append(keyEvent.getKeyChar());
                        appendRawText(keyChar);
                        if (alternativesWindow != null) {
                            alternativesWindow.setVisible(false);
                            alternativesWindow = null;
                        }
                        invokeLookupWindow();
                        // ch handled
                    }

            }
        } else if (this.isShowingAlternatives()) {
            // we are showing alternatives.
            // interpret input in that context.

            if (KeyEvent.VK_0 <= keyChar && KeyEvent.VK_9 >= keyChar) {
                // interpret numeric input as selecting a particular
                // alternative from the displayed options.

                int selectIndex = Integer.parseInt(Character.toString(keyChar));

                // items are indexed from 0, but displayed from 1.
                // so if they hit 0 they get item 10, otherwise minus 1
                selectIndex = selectIndex == 0 ? 10 : selectIndex;

                if (selectIndex <= this.alternativesChooser.getPageSize()) {
                    // the select index is within the page size, so we
                    // select that opion, and close the candidates window

                    InputTerm<InputTerm<String>> selectedTerm = this.alternativesChooser.getValue(selectIndex);
                    this.selectAlternative(selectedTerm.getInputKeys().get(0));

                } else {
                    // selected index is out of range
                    accepted = false;
                }

            } else if (KeyEvent.VK_ENTER == keyChar) {
                // enter key, pick the currently selected chooser value if any

                /*InputTerm<V> selectedTerm = this.alternativesChooser.getSelectedValue();
                if (null != selectedTerm) {
                    this.selectAlternative(selectedTerm);

                } else {
                    // no value currently selected
                    accepted = false;
                } */

            } else if (KeyEvent.VK_ESCAPE == keyChar || KeyEvent.VK_BACK_SPACE == keyChar || KeyEvent.VK_DELETE == keyChar) {
                // close the candidates window on escape or backspace
                this.closeAlternatives();

            } else if (this.handleCharacterInput(keyChar, keyTypedEvent)) {

                // if the character is accepted as further input,
                // then its applied and the alernatives are discarded.
                this.closeAlternatives();

            } /*else if (this.convertedTermBuffer.getTermSource().shouldPassThrough(keyChar)) {
                // couldn't enter the input as part of the term.
                // commit the uncommitted text and pass through
                // the input to the text component.

                this.closeAlternatives();
                this.commitText();
                consume = false;

            }*/ else {
                accepted = false;
            }

        } else if (this.hasUncommittedText()) {
            // uncommitted terms in the buffer
            switch (keyChar) {
                case KeyEvent.VK_ENTER:
                    this.commitRawText();
                    break;
                case KeyEvent.VK_SPACE:
                    this.commit(this.rawInput.toString());
                    break;
                case KeyEvent.VK_DELETE:
                case KeyEvent.VK_BACK_SPACE:
                    this.updateRaw(this.rawInput.substring(0, this.rawInput.length() - 1));
                    break;

            }
            /*if(KeyEvent.VK_SPACE == keyChar) {
                // on space or enter, commit any uncommitted terms in the buffer

                this.commitText();

            } else if(KeyEvent.VK_BACK_SPACE == keyChar || KeyEvent.VK_DELETE == keyChar) {
                // on backspace or delete, delete the selected term/item from the buffer

                if(this.termBuffer.truncateAnticipated() || this.termBuffer.delete()) {
                    // if deletion was successful, we need to update the text to reflect the deletion
                    this.updateUncommittedText();
                } else {
                    // nothing to delete, apparently
                    accepted = false;
                }

            }*/
            if (this.handleCharacterInput(keyChar, keyTypedEvent)) {
                // the character was accepted as input.

            }/* else if (this.convertedTermBuffer.getTermSource().shouldPassThrough(keyChar)) {
                // if the character input be passed through
                // to the text component, then we first
                // commit the uncommitted text, then we
                // mark it so the key event isn't consumed.

                this.commitText();
                consume = false;

            }*/ else {
                // couldn't do anything with the input char
                accepted = false;
            }

        } else if (this.handleCharacterInput(keyChar, keyTypedEvent)) {
            // the character was accepted as input.

        } /*else if (this.convertedTermBuffer.getTermSource().shouldPassThrough(keyChar)) {
            // if the character input be passed through
            // then we don't consume the key event
            consume = false;
        }*/

        if (consume) {
            keyTypedEvent.consume();
        }

        if (!accepted) {
            // input was rejected, say so
            this.indicateRejectedInput();
        }
    }

    /**
     * Replace the currently selected term with the given alternative.
     * @param term the term
     */
    void selectAlternative(String term) {
        this.rawInput.replace(0,this.rawInput.length(),term);
        // update the uncommitted and hide
        // the alternatives window
        this.updateUncommittedText();
        this.closeAlternatives();
    }


    /**
     * Handle a character of raw input.
     * Depending on the current state, the character
     * might be interpreted in different ways:
     * appended to the end of the current raw input,
     * part of a new term, etc.
     *
     * @param c the character
     * @return true if the character is accepted, false otherwise
     */
    private boolean handleCharacterInput(char c, InputEvent event) {
        boolean accepted = false;

        //String inputString = this.convertedTermBuffer.getTermSource().convertRawCharacter(c);
        //String proposedInput = this.rawInput + inputString;

        /*if(this.convertedTermBuffer.getTermSource().isPartialInputKey(proposedInput)) {
            // if appending the character to the raw input
            // is the initial character sequence of a input key,
            // we set the appended string as the new raw

            this.updateRaw(proposedInput);
            accepted = true;

        } else if(this.insertIntoBuffer(proposedInput, true, event)) {
            // if the buffer accepted the input, then we clear
            // the raw String since the input is now contained
            // within the buffer.

            this.clearRaw();
            accepted = true;

        } else if(this.convertedTermBuffer.getTermSource().isPartialInputKey(inputString) && this.insertIntoBuffer(this.rawInput.toString(), false, event)) {
            // the buffer accepted the existing raw input, and the new
            // input was added as the start of a new raw string.
            // don't show the alternatives because we're showing more raw input.

            this.updateRaw(inputString);
            accepted = true;

        } else {
            // last option is that we try splitting the existing raw input
            // between left and right sections, adding on the new input.
            // i.e. if ABC is the raw input, see if we can insert
            // CD is a partial key and we can insert AB, or
            // BCD is a partial and we can insert A, etc.

            for(int i = this.rawInput.length() - 1; i >= 0; i--) {
                // try splitting successively father back
                // into the existing raw input.

                String left = this.rawInput.substring(0, i);
                String right = this.rawInput.substring(i) + inputString;

                if(this.convertedTermBuffer.getTermSource().isPartialInputKey(right) && this.insertIntoBuffer(left, false, event)) {
                    // successful if the split off right part is a partial key,
                    // and the buffer accepts the left input.
                    // don't show alternatives because we still have raw

                    this.updateRaw(right);
                    accepted = true;
                    break;
                }
            }
        } */

        return accepted;
    }

    /**
     * Attempt to insert the raw input into the term buffer.
     * The buffer will determine if the raw input is accepted
     * or not.
     *
     * @param raw
     * @param showAlternatives show alternatives of a successful insert if any
     * @return true if the raw input was accepted into the buffer, false otherwise
     */
    private boolean insertIntoBuffer(String raw, boolean showAlternatives, InputEvent event) {
        this.appendRawText(raw.charAt(0));
        return true;
    }

    /**
     * Handle mouse release event.
     * We may need to reposition the current
     * selection within the buffer.
     *
     * @param mouseReleasedEvent
     */
    private void dispatchMouseReleasedEvent(MouseEvent mouseReleasedEvent) {

        Component sourceComponent = mouseReleasedEvent.getComponent();
        Component sourceParent = sourceComponent.getParent();

        // let the chooser handle its own mouse events,
        // so don't consume those events generated by the
        // chooser (the chooser is the parent of the JList
        // from which the events are generated).
        if (sourceParent != this.alternativesChooser) {

            // source is not the chooser, must be the input window.
            Point sourceLocation = sourceComponent.getLocationOnScreen();

            int x = (int) sourceLocation.getX() + mouseReleasedEvent.getX();
            int y = (int) sourceLocation.getY() + mouseReleasedEvent.getY();

            // figure out where in the text cursor should
            // be and update the text to include the new selection
            // based on that index if necessary.
            TextHitInfo hit = this.context.getLocationOffset(x, y);
            if (null != hit) {
                if (this.builder.get(method).setInsertionIndex(hit.getInsertionIndex())) {
                    this.updateUncommittedText();
                }

            } else {
                // if the click was outside the area of the current
                // commit any uncommited text (if any) and close
                // any windows that may be up.
                this.commitText();

                this.closeAlternatives();
                this.hideRawWindow();

            }

            // we've taken care of the click.
            // consume the event so the underlying
            // component doesn't handle it again.
            mouseReleasedEvent.consume();
        }
    }

    /**
     * Commit any currently converted but uncommitted text.
     */
    private void commitText() {
        this.commitRawText();

    }

    public void dispose() {
        // Disposes of the input method and releases the resources used by it.
        System.out.println("English completion di-XXX");
    }

    public void endComposition() {
        // Ends any input composition that may currently be going on in this context.
        String convertedText = convert(rawInput);
        if (convertedText != null) {
            commit(convertedText);
        }
    }

    public GenericInputMethodControl getControlObject() {
        return this.controlobject;
    }

    public Locale getLocale() {
        // Returns the current input locale.
        return locale;
    }

    private AttributedString getUncommittedAttributedString() {

        AttributedString highlightedString = null;
        if (!this.getControlObject().isUsingRawWindow() && this.hasRawInput()) {
            // we're inserting some text inline.
            // the raw text is composed within the buffer
            // before being converted.

            highlightedString = this.convertedTermBuffer.toAttributedStringWithRawInsert(this.rawInput.toString());

        } else if (this.hasUncommittedText()) {
            // generate the highlighted text of the buffer.
            // any selected contents of the buffer is highlighted.

            highlightedString = this.convertedTermBuffer.toAttributedString();
        }

        return highlightedString;
    }

    /**
     * Attempts to handle a typed character.
     *
     * @return whether the character was handled
     * <p/>
     * States that need to be looked at:
     * - user is typing characters for composition
     * - user has asked for alternative completions
     * - user is asking for text to be committed
     */
    private boolean handleCharacter(KeyEvent keyEvent) {
        System.out.println("handling char \"" + keyEvent.getKeyChar() + "\"");
        System.out.println("raw is " + rawInput);
        if (alternativesWindow != null) {
            if (handleLookupWindow(keyEvent)) {
                return true;
            }
        }
        if (keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE && this.rawInput.length() > 0) {
            this.rawInput = new StringBuffer(this.rawInput.toString().substring(0, this.rawInput.length() - 1));
            if (alternativesWindow != null) {
                alternativesWindow.setVisible(false);
                alternativesWindow = null;
            }
            this.sendRawText();
            return true;
        }
        if (Character.isLetter(keyEvent.getKeyChar()) || Character.isDigit(keyEvent.getKeyChar())) {
            //this.rawInput.append(keyEvent.getKeyChar());
            appendRawText(keyEvent.getKeyChar());
            if (alternativesWindow != null) {
                alternativesWindow.setVisible(false);
                alternativesWindow = null;
            }
            invokeLookupWindow();
            // ch handled
            return true;
        }
        //this.sendRawText();
        // ch not handled
        return false;
    }

    private Boolean handleLookupWindow(KeyEvent keyEvent) {
        System.out.println("Keycode: " + keyEvent.getKeyCode() + "==" + KeyEvent.VK_DOWN);
        if (keyEvent.getKeyChar() == ' ') {
            System.out.println("Committing lookup window");
            String selection = (String) list.getSelectedValue();
            rawInput.setLength(0);
            rawInput.append(selection);
            commitRawText();
            wordTerminated = false;
            alternativesWindow.setVisible(false);
            alternativesWindow = null;
            return true;
        } else if (keyEvent.getKeyChar() == '\n') {
            this.commitRawText();
            this.wordTerminated = false;
            alternativesWindow.setVisible(false);
            alternativesWindow = null;
            return true;
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_DOWN && list.getSelectedIndex() < list.getModel().getSize()) {
            list.setSelectedIndex(list.getSelectedIndex() + 1);
            return true;
        } else if (keyEvent.getKeyCode() == KeyEvent.VK_UP && list.getSelectedIndex() > 0) {
            list.setSelectedIndex(list.getSelectedIndex() - 1);
            return true;
        } else if (Character.isDigit(keyEvent.getKeyChar()) && keyEvent.getKeyChar() != '0') {
            String converted;
            Boolean finishword = true;
            for (int i = 0; i < list.getModel().getSize() && finishword; i++) {
                converted = list.getModel().getElementAt(i).toString().substring(list.getModel().getElementAt(i).toString().indexOf('.') + 1);
                System.out.println("Converted: " + converted + " keyEvent.getChar()" + keyEvent.getKeyChar());
                System.out.println("Regex: " + converted.split("[A-Za-z0-9]").length);
                if (converted.split("[A-Za-z0-9]").length > 1) {
                    converted = converted.split("[A-Za-z0-9]")[1];
                    System.out.println("Converted: " + converted + " keyEvent.getChar()" + keyEvent.getKeyChar());
                    if (converted.charAt(0) == keyEvent.getKeyChar()) {
                        finishword = false;
                        i = list.getModel().getSize();
                    }
                }
            }
            if (finishword) {
                this.rawInput.setLength(0);
                this.rawInput.append(list.getModel().getElementAt(Character.getNumericValue(keyEvent.getKeyChar()) - 1).toString());
                this.commitRawText();
                this.wordTerminated = false;
                alternativesWindow.setVisible(false);
                alternativesWindow = null;
                return true;
            }

        }
        return false;
    }

    /**
     * @return true if there is raw, unconverted, input
     */
    private boolean hasRawInput() {
        return this.rawInput.length() > 0;
    }

    public boolean hasUncommittedText() {
        return this.rawInput.length() > 0;
    }

    private void hideAlternativesWindow() {
        if (null != this.alternativesWindow) {
            this.alternativesWindow.setVisible(false);
            this.alternativesWindow.dispose();

            this.alternativesWindow = null;
            this.alternativesChooser = null;
        }
    }

    private void hideRawWindow() {
        if (null != this.rawWindow) {
            this.rawWindow.setVisible(false);
            this.rawWindow.dispose();

            this.rawWindow = null;
            this.rawLabel = null;
        }
    }

    public void hideWindows() {
        this.alternativesWindow.setVisible(false);
        hideRawWindow();
        this.hideAlternativesWindow();
    }

    /**
     * Indicate to the user that there input was rejected.
     */
    protected void indicateRejectedInput() {
        Toolkit.getDefaultToolkit().beep();
    }

    private void invokeLookupWindow() {
        // stub
        System.out.println("invoked lookup window");
        alternativesWindow = inputMethodContext.createInputMethodWindow("Lookup Window", true);

        // make a list of possible completions
    /*
	String[] data = new String[] {
	    rawInput.toString(),
	    rawInput.toString()+rawInput.toString(),
	    rawInput.toString()+rawInput.toString()+rawInput.toString()
	};
	*/
        /*String[] data = completedWords();
        System.out.println("Completed Words: " + data);
        list = new JList(data);
        list.setSelectedIndex(0);

        alternativesWindow.setLayout(new BorderLayout());
        alternativesWindow.setAlwaysOnTop(false);
        alternativesWindow.setType(Window.Type.UTILITY);
        alternativesWindow.add(list, BorderLayout.CENTER);
        alternativesWindow.setSize(200, 300);
        alternativesWindow.setVisible(true);     */
    }

    public boolean isCompositionEnabled() {
        //  Determines whether this input method is enabled.
        // always enabled
        return true;
    }

    public void setCompositionEnabled(boolean enable) {
        // Enables or disables this input method for composition, depending on the value of the parameter enable.
        // not supported yet
        System.out.println("English completion sce-XXX");
        throw new UnsupportedOperationException();
    }

    public boolean isShowingAlternatives() {
        return this.alternativesWindow.isShowing();
    }

    /**
     * @see java.awt.im.spi.InputMethod#notifyClientWindowChange(java.awt.Rectangle)
     */
    public void notifyClientWindowChange(Rectangle bounds) {
        if (null != bounds) {
            if (this.hasRawInput()) {
                this.showRawWindow();
            } else if (this.isShowingAlternatives()) {
                this.updateAlternatives(this.alternatives, null);
            }
        } else {
            this.hideRawWindow();
            this.hideAlternativesWindow();
        }
    }

    /**
     * Helper method positions the alternatives window below the spot.
     *
     * @param window the window to position
     */
    private void positionWindowBelowTheSpot(final Window window, InputEvent event) {
        if (null != window) {
            // would like to be able to use the TextHitInfo to position
            // the window to take account of the raw input and the selectiion,
            // but leading doesn't seem to work... so just always use 0 and
            // we'll account for this manually using FontMetrics in a moment.
            Rectangle caretRect = GenericInputMethodOld.this.context.getTextLocation(TextHitInfo.leading(0));

            // think I stole this code the following couple of lines from somewhere,
            // but don't remember where, not sure if I can explain now...
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension windowSize = window.getSize();

            final int SPACING = 2;

            Point windowLocation = new Point();
            if (caretRect.x + windowSize.width > screenSize.width) {
                windowLocation.x = screenSize.width - windowSize.width;
            } else {
                windowLocation.x = caretRect.x;
            }

            if (caretRect.y + caretRect.height + SPACING + windowSize.height > screenSize.height) {
                windowLocation.y = caretRect.y - SPACING - windowSize.height;
            } else {
                windowLocation.y = caretRect.y + caretRect.height + SPACING;
            }

            if (null != event && event.getSource() instanceof Component) {
                // assuming the event was generated from an AWT/Swing
                // component, we'll use it's FontMetrics
                Component eventSource = (Component) event.getSource();
                Font componentFont = eventSource.getFont();

                // what we're doing is a little funky, so a lot of null
                // checks to make sure we don't blow up on unexpected
                // components.  if we can't do it, we just won't make
                // the adjustmet.
                if (null != componentFont) {
                    FontMetrics fontMetrics = eventSource.getFontMetrics(componentFont);
                    if (null != fontMetrics) {
                        // we'll use the FontMetrics to calculate how far
                        // we need to adjust our window position
                        // to account for the current input state.

                        String leadingString = "";
                        if (!this.getControlObject().isUsingRawWindow()) {
                            // if we're not using the raw window, then
                            // raw input is rendered inline, and we
                            // need to account for it when calculating
                            // where to put the window
                            leadingString += this.rawInput;
                        }
                        String selection = this.convertedTermBuffer.getCurrentSelection();
                        if (null != selection) {
                            // if there's a selection in the buffer, then we
                            // account for that when positioning the window also.
                            leadingString += selection;
                        }

                        // adjust the window position to account
                        // for selected/raw text.
                        int width = fontMetrics.stringWidth(leadingString);
                        windowLocation.x -= width;
                    }
                }
            }

            window.setLocation(windowLocation);

            window.pack();
            window.setVisible(true);
        }
    }

    public void reconvert() {
        // Starts the reconversion operation.
        // not supported yet
        System.out.println("English completion re-XXX");
        throw new UnsupportedOperationException();
    }

    public void removeNotify() {
        // Notifies the input method that a client component has been removed from its containment hierarchy, or that input method support has been disabled for the component.
        // not supported yet
        System.out.println("English completion rn-XXX");
        throw new UnsupportedOperationException();
    }

    private void sendConvertedText(String convertedText) {
        InputMethodHighlight highlight;

        highlight = InputMethodHighlight.SELECTED_CONVERTED_TEXT_HIGHLIGHT;
        AttributedString as = new AttributedString(convertedText);
        as.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, highlight);

        inputMethodContext.dispatchInputMethodEvent(
                InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                as.getIterator(),
                convertedText.length(),
                TextHitInfo.leading(convertedText.length()),
                null);

    }

    private void sendRawText() {
        String text = rawInput.toString();
        System.out.println("send raw text: " + text);
        InputMethodHighlight highlight;
        AttributedString as = new AttributedString(text);

        highlight = InputMethodHighlight.SELECTED_RAW_TEXT_HIGHLIGHT;
        as.addAttribute(TextAttribute.INPUT_METHOD_HIGHLIGHT, highlight);

        inputMethodContext.dispatchInputMethodEvent(
                InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                as.getIterator(),
                0,
                null,
                null);

    }

    public void setCharacterSubsets(Character.Subset[] subsets) {
        // Sets the subsets of the Unicode character set that this input method is allowed to input.
        // not supported yet
        System.out.println("English completion sc-XXX");
        return;
        // throw new UnsupportedOperationException();
    }

    public void setInputMethodContext(InputMethodContext context) {
        // Sets the input method context, which is used to dispatch input method events to the client component and to request information from the client component.

        inputMethodContext = context;
        System.out.println("Input context set " + context);


        /*if (statusWindow == null) {
            statusWindow = context.createInputMethodWindow("Simp. Chinese Pinyin", false);
            Label label = new Label("Pig Latin locale");
            label.setBackground(Color.white);
            statusWindow.add(label);
            // updateStatusWindow(locale);
            label.setSize(200, 50);
            statusWindow.pack();
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            statusWindow.setLocation(d.width - statusWindow.getWidth(),
                                     d.height - statusWindow.getHeight());
	    } */

    }

    public boolean setLocale(Locale locale) {
        // Attempts to set the input locale.

        for (int i = 0; i < SUPPORTED_LOCALES.length; i++) {
            if (locale.equals(SUPPORTED_LOCALES[i])) {
                this.locale = locale;
                this.local = i;
		/*
		if (statusWindow != null) {
                    updateStatusWindow(locale);
                }
		*/
                System.out.println("Locale set");
                return true;
            }
        }
        System.out.println("Locale not set");
        return false;
    }

    private void showAlternatives(InputEvent event) {
        // if currently displaying any alternatives, hide them
        this.hideAlternativesWindow();

        boolean horizontal = !this.getControlObject().getChooserOrientation();
        boolean showIndices = true;
        int pageSize = 10;

        // generate a new chooser
        //this.alternativesChooser = new JPagedChooser<InputTerm<String>,String>(this.alternatives, horizontal, showIndices, pageSize);
        Font windowFont = this.getControlObject().getFont();
        if(null != windowFont) {
            // configure with the appropriate Font, if specified
            this.alternativesChooser.setFont(windowFont);
        }

        // next item once so the first is selected
        this.alternativesChooser.nextItem();

        // black border around the chooser looks nice
        this.alternativesChooser.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // when an item is selected, through the chooser, apply it here
        /*this.alternativesChooser.addSelectionListener(new JPagedChooser.SelectionListener<InputTerm<String>>() {
            public void handleSelection(InputTerm<String> selection) {
                if(null != selection) {
                    GenericInputMethodOld.this.selectAlternative(selection);
                }
            }
        });*/

        // create the window, add the chooser to it
        this.alternativesWindow = this.context.createInputMethodWindow("", true);
        //this.alternativesWindow = this.context.createInputMethodJFrame("", true);
        this.alternativesWindow.add(this.alternativesChooser);

        // can't focus on the alternatives window,
        // don't want it ever handling input itself
        this.alternativesWindow.setFocusableWindowState(false);

        // position the window corectly and show it.
        this.positionWindowBelowTheSpot(this.alternativesWindow, event);
    }

    /**
     * Replace the currently selected term with the given alternative.
     * @param term the term
     */
    void selectAlternative(InputTerm<String> term) {
        this.convertedTermBuffer.replaceSelectionWithTerm(term);

        // update the uncommitted and hide
        // the alternatives window
        this.updateUncommittedText();
        this.closeAlternatives();
    }

    private void showRawWindow() {
        this.hideRawWindow();

        this.rawLabel = new JLabel();
        this.rawLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.rawLabel.setText(this.rawInput.toString());

        Font windowFont = this.getControlObject().getFont();
        if (null != windowFont) {
            // configure with the below the spot window if specified
            this.rawLabel.setFont(windowFont);
        }

        this.rawWindow = this.context.createInputMethodWindow("", true);
        this.rawWindow.add(this.rawLabel);
        this.rawWindow.setFocusableWindowState(false);

        this.positionWindowBelowTheSpot(this.rawWindow, null);
    }

    private void updateAlternatives(List<Tuple<InputTerm<String>,String>> alternatives, InputEvent event) {
        this.alternatives = alternatives;
        this.showAlternatives(event);
    }

    private void updateRaw(String raw) {
        this.rawInput = new StringBuffer(raw);

        if (this.getControlObject().isUsingRawWindow()) {
            if (this.rawInput.length() == 0) {
                this.hideRawWindow();
            } else {
                this.showRawWindow();
            }
        } else {
            this.updateUncommittedText();
        }
    }

    /**
     * @return true if currently showing alternatives
     */
    /*private boolean isShowingAlternatives() {
        return null != this.alternatives;
    }

    /**
     * @return true if there is converted but uncommitted text
     */
    /*private boolean hasUncommittedText() {
        return !this.termBuffer.isEmpty();
    }*/
    private void updateUncommittedText() {
        AttributedString highlightedString = this.getUncommittedAttributedString();

        int insertionIndex = this.convertedTermBuffer.getInsertionIndex();
        if (!this.getControlObject().isUsingRawWindow() && this.hasRawInput()) {
            insertionIndex += this.rawInput.length();
        }

        this.context.dispatchInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                null != highlightedString ? highlightedString.getIterator() : null,
                0,
                TextHitInfo.leading(insertionIndex),
                null);
    }

    /*private void updateRaw(String raw) {
        this.rawInput = new StringBuffer(raw);
        if(this.getControlObject().isUsingRawWindow()) {
            if(this.rawInput.length() == 0) {
                this.hideRawWindow();
            } else {
                this.showRawWindow();;
            }
        } else {
            this.updateUncommittedText();
        }
    }*/

    /*private void showRawWindow() {
        this.hideRawWindow();

        this.rawLabel = new JLabel();
        this.rawLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.rawLabel.setText(this.rawInput.toString());

        Font windowFont = this.getControlObject().getFont();
        if(null != windowFont) {
            // configure with the below the spot window if specified
            this.rawLabel.setFont(windowFont);
        }

        this.rawWindow = this.context.createInputMethodWindow("", true);
        this.rawWindow.add(this.rawLabel);
        this.rawWindow.setFocusableWindowState(false);

        this.positionWindowBelowTheSpot(this.rawWindow, null);
    }

    private void hideRawWindow() {
        if(null != this.rawWindow) {
            this.rawWindow.setVisible(false);
            this.rawWindow.dispose();

            this.rawWindow = null;
            this.rawLabel = null;
        }
    }  */

    /**
     * Helper method positions the alternatives window below the spot.
     * @param window the window to position
     */
   /* private void positionWindowBelowTheSpot(final Window window, InputEvent event) {
        if(null != window) {
            // would like to be able to use the TextHitInfo to position
            // the window to take account of the raw input and the selectiion,
            // but leading doesn't seem to work... so just always use 0 and
            // we'll account for this manually using FontMetrics in a moment.
            Rectangle caretRect = GenericInputMethod.this.context.getTextLocation(TextHitInfo.leading(0));

            // think I stole this code the following couple of lines from somewhere,
            // but don't remember where, not sure if I can explain now...
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension windowSize = window.getSize();

            final int SPACING = 2;

            Point windowLocation = new Point();
            if (caretRect.x + windowSize.width > screenSize.width) {
                windowLocation.x = screenSize.width - windowSize.width;
            } else {
                windowLocation.x = caretRect.x;
            }

            if (caretRect.y + caretRect.height + SPACING + windowSize.height > screenSize.height) {
                windowLocation.y = caretRect.y - SPACING - windowSize.height;
            } else {
                windowLocation.y = caretRect.y + caretRect.height + SPACING;
            }

            if(null != event && event.getSource() instanceof Component) {
                // assuming the event was generated from an AWT/Swing
                // component, we'll use it's FontMetrics
                Component eventSource = (Component)event.getSource();
                Font componentFont = eventSource.getFont();

                // what we're doing is a little funky, so a lot of null
                // checks to make sure we don't blow up on unexpected
                // components.  if we can't do it, we just won't make
                // the adjustmet.
                if(null != componentFont) {
                    FontMetrics fontMetrics = eventSource.getFontMetrics(componentFont);
                    if(null != fontMetrics) {
                        // we'll use the FontMetrics to calculate how far
                        // we need to adjust our window position
                        // to account for the current input state.

                        String leadingString = "";
                        if(!this.getControlObject().isUsingRawWindow()) {
                            // if we're not using the raw window, then
                            // raw input is rendered inline, and we
                            // need to account for it when calculating
                            // where to put the window
                            leadingString += this.rawInput.toString();
                        }
                        String selection = this.termBuffer.getCurrentSelection();
                        if(null != selection) {
                            // if there's a selection in the buffer, then we
                            // account for that when positioning the window also.
                            leadingString += selection;
                        }

                        // adjust the window position to account
                        // for selected/raw text.
                        int width = fontMetrics.stringWidth(leadingString);
                        windowLocation.x -= width;
                    }
                }
            }

            window.setLocation(windowLocation);

            window.pack();
            window.setVisible(true);
        }
    }
     */

}

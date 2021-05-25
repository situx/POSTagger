/* Copyright (c) 2007 Jordan Kiang
 * jordan-at-kiang.org
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.situx.postagger.main.gui.ime.descriptor;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.font.TextHitInfo;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodContext;
import java.lang.Character.Subset;
import java.text.AttributedString;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.main.gui.ime.jquery.ConvertedTermBuffer;
import com.github.situx.postagger.main.gui.ime.jquery.InputTerm;
import com.github.situx.postagger.main.gui.ime.jquery.InputTermSource;
import com.github.situx.postagger.main.gui.ime.JPagedChooser;


/**
 * A two-stage InputMethod that first buffers "raw" input until
 * the raw input amounts to an a particular key as determined
 * by a pluggable InputTermSource.  When the raw input is accepted
 * as a key, it can then be converted into uncommitted input.
 * The uncommitted input is added if possible as 
 *
 * @param <V> the parameterized input key type
 *
 * @author Jordan Kiang
 */
public class GenericInputMethod<V> implements InputMethod {

    private InputMethodContext context;

    // the source is the dictionary and governs what
    // input is and isn't legal given current state
    private InputTermSource<V> termSource;

    // the buffer contains the converted but uncommitted
    // text (but not any unconverted raw input).
    private ConvertedTermBuffer<V> termBuffer;

    // window for rendering raw input if showing
    // raw below-the-spot
    //private Window rawWindow;

    // raw input is rendered through this label
    // in the raw window when showing raw below-the-spot
    private JLabel rawLabel;

    // the raw input.  not much point in using
    // a StringBuilder since we're appending one character
    // and evaluating it to a String anyway with each key
    private String rawInput = "";

    // window for showing alternatives to the current term
    private Window alternativesWindow;

    // alternatives are rendered in the alternatives window
    // through the below component.
    private JPagedChooser<V,String> alternativesChooser;

    private List<Tuple<InputTerm<V>,String>> alternatives;

    private boolean activated;

    /**
     * A new TermInputMethod backed by the given source.
     * @param source
     */
    public GenericInputMethod(InputTermSource<V> source) {
        this.termSource = source;
        this.termBuffer = new ConvertedTermBuffer<V>(source);
        this.activated=true;
    }

    /**
     * @return true if there is raw, unconverted, input
     */
    private boolean hasRawInput() {
        return this.rawInput.length() > 0;
    }

    /**
     * @return true if currently showing alternatives
     */
    private boolean isShowingAlternatives() {
        return null != this.alternatives;
    }

    /**
     * @return true if there is converted but uncommitted text
     */
    private boolean hasUncommittedText() {
        return !this.termBuffer.isEmpty();
    }

    /**
     * Indicate to the user that there input was rejected.
     */
    protected void indicateRejectedInput() {
        Toolkit.getDefaultToolkit().beep();
    }

    private void showRawWindow() {
        this.hideAlternativesWindow();
        this.showAlternatives(null);
        /*this.hideRawWindow();

        this.rawLabel = new JLabel();
        this.rawLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.rawLabel.setText(this.rawInput);

        Font windowFont = this.getControlObject().getFont();
        if(null != windowFont) {
            // configure with the below the spot window if specified
            this.rawLabel.setFont(windowFont);
        }

        this.rawWindow = this.context.createInputMethodWindow("", true);
        this.rawWindow.add(this.rawLabel);
        this.rawWindow.setFocusableWindowState(false);

        this.positionWindowBelowTheSpot(this.rawWindow, null);*/
    }

    private void hideRawWindow() {
        this.hideAlternativesWindow();
        /*if(null != this.rawWindow) {
            this.rawWindow.setVisible(false);
            this.rawWindow.dispose();

            this.rawWindow = null;
            this.rawLabel = null;
        } */
    }

    private void closeAlternatives() {
        this.alternatives = null;
        this.hideAlternativesWindow();
    }

    private void updateAlternatives(List<Tuple<InputTerm<V>,String>> alternatives, InputEvent event) {
        this.alternatives = alternatives;
        this.showAlternatives(event);
    }

    private void showAlternatives(InputEvent event) {
        // if currently displaying any alternatives, hide them
        this.hideAlternativesWindow();

        boolean horizontal = !this.getControlObject().getChooserOrientation();
        boolean showIndices = true;
        int pageSize = 10;
        Font windowFont = this.getControlObject().getFont();
        //this.alternativesWindow = this.context.createInputMethodJFrame("", true);
        this.rawLabel = new JLabel();
        this.rawLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.rawLabel.setText(this.rawInput);
        if(null != windowFont) {
            // configure with the below the spot window if specified
            this.rawLabel.setFont(windowFont);
        }

        this.alternativesWindow = this.context.createInputMethodWindow("", true);
        this.alternativesWindow.add(this.rawLabel, BorderLayout.NORTH);

        // can't focus on the alternatives window,
        // don't want it ever handling input itself
        this.alternativesWindow.setFocusableWindowState(false);
        if(alternatives!=null) {
            System.out.println("Alternatives: "+this.alternatives);
            // generate a new chooser
            this.alternativesChooser = new JPagedChooser<V,String>(this.alternatives, horizontal, showIndices, pageSize);

            if (null != windowFont) {
                // configure with the appropriate Font, if specified
                this.alternativesChooser.setFont(windowFont);
            }

            // next item once so the first is selected
            this.alternativesChooser.nextItem();

            // black border around the chooser looks nice
            this.alternativesChooser.setBorder(BorderFactory.createLineBorder(Color.BLACK));

            // when an item is selected, through the chooser, apply it here
            this.alternativesChooser.addSelectionListener(new JPagedChooser.SelectionListener<V>() {

                @Override
                public void handleSelection(final InputTerm<V> selection) {
                    if (null != selection) {
                        GenericInputMethod.this.commitText(selection.toString());
                        GenericInputMethod.this.clearRaw();
                        GenericInputMethod.this.alternatives=new LinkedList<>();
                        GenericInputMethod.this.hideWindows();
                        //GenericInputMethod.this.selectAlternative(selection);
                    }
                }
            });
            this.alternativesWindow.add(this.alternativesChooser, BorderLayout.SOUTH);
        }



        // position the window corectly and show it.
        this.positionWindowBelowTheSpot(this.alternativesWindow, event);
    }

    private void hideAlternativesWindow() {
        if(null != this.alternativesWindow) {
            this.alternativesWindow.setVisible(false);
            this.alternativesWindow.dispose();
            this.alternativesWindow = null;
            this.alternativesChooser = null;
        }
    }


    /**
     * Helper method positions the alternatives window below the spot.
     * @param window the window to position
     */
    private void positionWindowBelowTheSpot(final Window window, InputEvent event) {
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
                            leadingString += this.rawInput;
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

    private boolean convertRaw(InputEvent event) {
        if(this.insertIntoBuffer(this.rawInput, true, event)) {
            this.clearRaw();

            return true;
        }

        return false;
    }

    private void clearRaw() {
        this.updateRaw("");
    }

    private void updateRaw(String raw) {
        this.rawInput = raw;

        if(this.getControlObject().isUsingRawWindow()) {
            if(this.rawInput.length() == 0) {
                this.hideRawWindow();
            } else {
                this.showRawWindow();;
            }
        } else {
            this.updateUncommittedText();
        }
    }

    /**
     * Replace the currently selected term with the given alternative.
     * @param term the term
     */
    void selectAlternative(InputTerm<V> term) {
        this.termBuffer.replaceSelectionWithTerm(term);

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

        String inputString = this.termSource.convertRawCharacter(c);
        String proposedInput = this.rawInput + inputString;

        if(this.termSource.isPartialInputKey(proposedInput)) {
            // if appending the character to the raw input
            // is the initial character sequence of a input key,
            // we set the appended string as the new raw

            this.updateRaw(proposedInput);
            accepted = true;
            List<V> list=new LinkedList<>();
            list.add(this.termSource.getInputKey(proposedInput));
            List<Tuple<InputTerm<V>,String>> alternatives = this.termBuffer.getAlternativesToSelection2(list);
            if(null != alternatives && !alternatives.isEmpty()) {
                this.updateAlternatives(alternatives, event);

            } else {
                // no alternatives
                accepted = false;
            }

        } else if(this.insertIntoBuffer(proposedInput, true, event)) {
            // if the buffer accepted the input, then we clear
            // the raw String since the input is now contained
            // within the buffer.

            this.clearRaw();
            accepted = true;

        } else if(this.termSource.isPartialInputKey(inputString) && this.insertIntoBuffer(this.rawInput, false, event)) {
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

                if(this.termSource.isPartialInputKey(right) && this.insertIntoBuffer(left, false, event)) {
                    // successful if the split off right part is a partial key,
                    // and the buffer accepts the left input.
                    // don't show alternatives because we still have raw

                    this.updateRaw(right);
                    accepted = true;
                    break;
                }
            }
        }

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
        V key = this.termSource.getInputKey(raw);
        if(null != key) {
            // if non null
            List<Tuple<InputTerm<V>,String>> terms = this.termBuffer.insertKey(key);
            if(null != terms) {
                // if the input was accepted, update
                // the uncommitted text so it's re-rendered
                // and show the alternatives
                this.updateUncommittedText();

                if(showAlternatives && terms.size() > 1) {
                    // if there were multiple options,
                    // show the alternatives window
                    this.updateAlternatives(terms, event);
                }

                return true;
            }
        }

        // the raw String didn't amount to an inputKey,
        // or the key couldn't be inserted into the buffer
        return false;
    }

    /**
     * Commit any currently converted but uncommitted text.
     */
    private void commitText() {
        String uncommitted = this.termBuffer.toString();
        AttributedString commitString = new AttributedString(uncommitted);
        this.termBuffer.clear();

        this.context.dispatchInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                commitString.getIterator(),
                uncommitted.length(),
                TextHitInfo.leading(this.termBuffer.getInsertionIndex()), null);


    }

    /**
     * Commit any currently converted but uncommitted text.
     */
    private void commitText(String toCommit) {
        String uncommitted = toCommit;
        AttributedString commitString = new AttributedString(toCommit);
        this.termBuffer.clear();

        this.context.dispatchInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                commitString.getIterator(),
                uncommitted.length(),
                TextHitInfo.leading(this.termBuffer.getInsertionIndex()), null);


    }

    /**
     * Commit any currently converted but uncommitted text.
     */
    private void commitRawText() { String uncommitted = this.rawInput.toString();
        AttributedString commitString = new AttributedString(uncommitted);
        this.termBuffer.clear();

        this.context.dispatchInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                commitString.getIterator(),
                uncommitted.length(),
                TextHitInfo.leading(this.termBuffer.getInsertionIndex()), null);


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
        if(keyPressedEvent.getKeyCode()==KeyEvent.VK_SHIFT){
            /*
            if(activated){
                this.activated=false;
                this.clearRaw();
                this.alternatives = new LinkedList<>();
                this.hideWindows();
                StyledDocument doc = (StyledDocument)POSTagMain.resultarea.getDocument();
                SimpleAttributeSet aSet = new SimpleAttributeSet();
                StyleConstants.setFontFamily(aSet, new JLabel().getFont().getFamily());
                StyleConstants.setFontSize(aSet, 20);
                doc.setParagraphAttributes(0, 0, aSet, false);
                //POSTagMain.resultarea.setFont(new JLabel().getFont());
            }else{
                this.activated=true;
                StyledDocument doc = (StyledDocument)POSTagMain.resultarea.getDocument();
                SimpleAttributeSet aSet = new SimpleAttributeSet();
                try {
                    StyleConstants.setFontFamily(aSet, POSTagMain.getFont("cunei.ttf",20).getFamily());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                StyleConstants.setFontSize(aSet, 20);
                doc.setParagraphAttributes(0, 0, aSet, false);
                try {
                    POSTagMain.resultarea.setFont(POSTagMain.getFont("cunei.ttf",20));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            */
        }
        if(activated) {
            if (keyPressedEvent.getModifiers() != 0) {
                // doesn't interpret key events
                // with modifiers (alt, ctrl, meta).
                // don't consume the event, pass it through.

                consume = false;

            } else if (this.hasRawInput() && this.isShowingAlternatives()) {
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
                    if (this.termBuffer.previous()) {
                        this.updateUncommittedText();
                    } else {
                        accepted = false;
                    }

                } else if (KeyEvent.VK_RIGHT == keyCode) {
                    // right arrow key

                    // move the buffer selection to the next term/item
                    if (this.termBuffer.next()) {
                        this.updateUncommittedText();
                    } else {
                        accepted = false;
                    }

                } else if (KeyEvent.VK_UP == keyCode || KeyEvent.VK_DOWN == keyCode ||
                        KeyEvent.VK_PAGE_UP == keyCode || KeyEvent.VK_PAGE_DOWN == keyCode) {
                    // up/down/page up/page down
                    // see if there are any altenratives to the current selection.
                    // if so, open the chooser with those alternatives.

                    List<Tuple<InputTerm<V>, String>> alternatives = this.termBuffer.getAlternativesToSelection();
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
    }

    /**
     *
     * @param keyTypedEvent
     */
    private void dispatchKeyTypedEvent(KeyEvent keyTypedEvent) {
        char keyChar = keyTypedEvent.getKeyChar();

        boolean accepted = true;
        boolean consume = true;
        if(keyTypedEvent.getKeyCode()==KeyEvent.VK_SHIFT){
            /*
            if(activated){
                this.activated=false;
                this.clearRaw();
                this.alternatives = new LinkedList<>();
                this.hideWindows();
                StyledDocument doc = (StyledDocument)POSTagMain.resultarea.getDocument();
                SimpleAttributeSet aSet = new SimpleAttributeSet();
                StyleConstants.setFontFamily(aSet, new JLabel().getFont().getFamily());
                StyleConstants.setFontSize(aSet, 20);
                doc.setParagraphAttributes(0, 0, aSet, false);
                //POSTagMain.resultarea.setFont(new JLabel().getFont());
            }else{
                this.activated=true;
                try {
                    POSTagMain.resultarea.setFont(POSTagMain.getFont("cunei.ttf",20));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } */
        }
        if(activated) {
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

                if (KeyEvent.VK_SPACE == keyChar) {
                    InputTerm<V> selectedTerm = this.alternativesChooser.getSelectedValue();
                    System.out.println("SelectedTerm: " + selectedTerm);
                    this.selectAlternative(selectedTerm);
                    this.commitText(selectedTerm.toString().trim());
                    this.clearRaw();
                    this.alternatives = new LinkedList<>();
                    this.hideWindows();
                    // on a space or enter hit, we try to convert the
                    // raw input by adding it to the buffer
                /*accepted = this.convertRaw(keyTypedEvent);
                if(!this.termSource.isPartialInputKey(this.rawInput+accepted)){
                    this.commitText();
                    this.clearRaw();
                    this.alternatives=new LinkedList<>();
                    this.hideWindows();
                } */
                } else if (KeyEvent.VK_ENTER == keyChar) {
                    this.commitRawText();
                    this.clearRaw();
                    this.hideWindows();
                } else if (KeyEvent.VK_ESCAPE == keyChar || KeyEvent.VK_DELETE == keyChar) {
                    // escape or delete clears out the raw text
                    this.clearRaw();
                    this.hideWindows();

                } else if (KeyEvent.VK_BACK_SPACE == keyChar) {
                    // if there is some raw input, backspace deletes one
                    // character from the raw input
                    this.updateRaw(this.rawInput.substring(0, this.rawInput.length() - 1));
                    List<V> list = new LinkedList<>();
                    list.add(this.termSource.getInputKey(this.rawInput));
                    List<Tuple<InputTerm<V>, String>> alternatives = this.termBuffer.getAlternativesToSelection2(list);
                    if (null != alternatives && !alternatives.isEmpty()) {
                        this.updateAlternatives(alternatives, keyTypedEvent);
                    } else {
                        // no alternatives
                        accepted = false;
                    }

                } else if (KeyEvent.VK_0 <= keyChar && KeyEvent.VK_9 >= keyChar) {
                    if(this.alternativesChooser.getNextNumbers().contains(Integer.parseInt(Character.toString(keyChar)))){
                        accepted = this.handleCharacterInput(keyChar, keyTypedEvent);
                    }else {

                        if (this.termSource.isPartialInputKey(this.rawInput + keyChar)) {
                            this.handleCharacterInput(keyChar, keyTypedEvent);
                        }
                        System.out.println("Got Key: " + keyChar);
                        // interpret numeric input as selecting a particular
                        // alternative from the displayed options.
                        int selectIndex = Integer.parseInt(Character.toString(keyChar));

                        // items are indexed from 0, but displayed from 1.
                        // so if they hit 0 they get item 10, otherwise minus 1
                        selectIndex = selectIndex == 0 ? 10 : selectIndex;
                        System.out.println("Selected Index: " + selectIndex);
                        //if(selectIndex <= this.alternativesChooser.getPageSize()) {
                        // the select index is within the page size, so we
                        // select that opion, and close the candidates window

                        InputTerm<V> selectedTerm = this.alternativesChooser.getValue(selectIndex);
                        System.out.println("SelectedTerm: " + selectedTerm);
                        this.selectAlternative(selectedTerm);
                        this.commitText(selectedTerm.toString().trim());
                        this.clearRaw();
                        this.alternatives = new LinkedList<>();
                        this.hideWindows();
                    }
                    //} else {
                    // selected index is out of range
                    accepted = false;
                    //}

                } else {
                    // try to send the character as new input
                    accepted = this.handleCharacterInput(keyChar, keyTypedEvent);
                }

            } else if (this.isShowingAlternatives()) {
                // we are showing alternatives.
                // interpret input in that context.

                if (this.handleCharacterInput(keyChar, keyTypedEvent)) {

                    // if the character is accepted as further input,
                    // then its applied and the alernatives are discarded.
                    this.closeAlternatives();

                } else if (this.termSource.shouldPassThrough(keyChar)) {
                    // couldn't enter the input as part of the term.
                    // commit the uncommitted text and pass through
                    // the input to the text component.

                    this.closeAlternatives();
                    this.commitText();
                    consume = false;

                } else {
                    accepted = false;
                }

            } else if (this.hasUncommittedText()) {
                // uncommitted terms in the buffer

                if (KeyEvent.VK_SPACE == keyChar || KeyEvent.VK_ENTER == keyChar) {
                    // on space or enter, commit any uncommitted terms in the buffer

                    this.commitText();

                } else if (KeyEvent.VK_BACK_SPACE == keyChar || KeyEvent.VK_DELETE == keyChar) {
                    // on backspace or delete, delete the selected term/item from the buffer

                    if (this.termBuffer.truncateAnticipated() || this.termBuffer.delete()) {
                        // if deletion was successful, we need to update the text to reflect the deletion
                        this.updateUncommittedText();
                    } else {
                        // nothing to delete, apparently
                        accepted = false;
                    }

                } else if (this.handleCharacterInput(keyChar, keyTypedEvent)) {
                    // the character was accepted as input.

                } else if (this.termSource.shouldPassThrough(keyChar)) {
                    // if the character input be passed through
                    // to the text component, then we first
                    // commit the uncommitted text, then we
                    // mark it so the key event isn't consumed.

                    this.commitText();
                    consume = false;

                } else {
                    // couldn't do anything with the input char
                    accepted = false;
                }

            } else if (this.handleCharacterInput(keyChar, keyTypedEvent)) {
                // the character was accepted as input.

            } else if (this.termSource.shouldPassThrough(keyChar)) {
                // if the character input be passed through
                // then we don't consume the key event
                consume = false;
            }

            if (consume) {
                keyTypedEvent.consume();
            }

            if (!accepted) {
                // input was rejected, say so
                this.indicateRejectedInput();
            }
        }
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
        if(activated) {

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
                    if (this.termBuffer.setInsertionIndex(hit.getInsertionIndex())) {
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
    }

    /**
     * @see java.awt.im.spi.InputMethod#dispatchEvent(java.awt.AWTEvent)
     */
    public void dispatchEvent(AWTEvent event) {
        if(this.getControlObject().isEnabled()) {
            int eventID = event.getID();

            switch(eventID) {
                case KeyEvent.KEY_PRESSED:
                    this.dispatchKeyPressedEvent((KeyEvent)event);
                    break;
                case KeyEvent.KEY_TYPED:
                    this.dispatchKeyTypedEvent((KeyEvent)event);
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    this.dispatchMouseReleasedEvent((MouseEvent)event);
                    break;

            }
        }
    }


    private void updateUncommittedText() {
        AttributedString highlightedString = this.getUncommittedAttributedString();

        int insertionIndex = this.termBuffer.getInsertionIndex();
        if(!this.getControlObject().isUsingRawWindow() && this.hasRawInput()) {
            insertionIndex += this.rawInput.length();
        }

        this.context.dispatchInputMethodEvent(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                null != highlightedString ? highlightedString.getIterator() : null,
                0,
                TextHitInfo.leading(insertionIndex),
                null);
    }

    private AttributedString getUncommittedAttributedString() {

        AttributedString highlightedString = null;
        if(!this.getControlObject().isUsingRawWindow() && this.hasRawInput()) {
            // we're inserting some text inline.
            // the raw text is composed within the buffer
            // before being converted.

            highlightedString = this.termBuffer.toAttributedStringWithRawInsert(this.rawInput);

        } else if(this.hasUncommittedText()) {
            // generate the highlighted text of the buffer.
            // any selected contents of the buffer is highlighted.

            highlightedString = this.termBuffer.toAttributedString();
        }

        return highlightedString;
    }

    /**
     * @see java.awt.im.spi.InputMethod#activate()
     */
    public void activate() {
        this.activated=true;
    }

    /**
     * @see java.awt.im.spi.InputMethod#deactivate(boolean)
     */
    public void deactivate(boolean isTemporary) {
        this.activated=false;
    }

    /**
     * @see java.awt.im.spi.InputMethod#dispose()
     */
    public void dispose() {
        // noop
    }

    /**
     * @see java.awt.im.spi.InputMethod#endComposition()
     */
    public void endComposition() {
        this.commitText();
    }

    /**
     * @see java.awt.im.spi.InputMethod#getControlObject()
     */
    public Control getControlObject() {
        return this.termSource.getControlObject();
    }

    /**
     * @see java.awt.im.spi.InputMethod#hideWindows()
     */
    public void hideWindows() {
        this.hideRawWindow();
        this.hideAlternativesWindow();
    }

    /**
     * @see java.awt.im.spi.InputMethod#setLocale(java.util.Locale)
     */
    public boolean setLocale(Locale locale) {
        return this.termSource.setLocale(locale);
    }

    /**
     * @see java.awt.im.spi.InputMethod#getLocale()
     */
    public Locale getLocale() {
        return this.termSource.getLocale();
    }

    /**
     * @see java.awt.im.spi.InputMethod#isCompositionEnabled()
     */
    public boolean isCompositionEnabled() {
        return true;
    }

    /**
     * @see java.awt.im.spi.InputMethod#setCompositionEnabled(boolean)
     */
    public void setCompositionEnabled(boolean enable) {
        throw new UnsupportedOperationException();
    }

    /**
     * @see java.awt.im.spi.InputMethod#notifyClientWindowChange(java.awt.Rectangle)
     */
    public void notifyClientWindowChange(Rectangle bounds) {
        if(null != bounds) {
            if(this.hasRawInput()) {
                this.showRawWindow();
            } else if(this.isShowingAlternatives()) {
                this.updateAlternatives(this.alternatives, null);
            }
        } else {
            this.hideRawWindow();
            this.hideAlternativesWindow();
        }
    }

    /**
     * @see java.awt.im.spi.InputMethod#reconvert()
     */
    public void reconvert() {
    }

    /**
     * @see java.awt.im.spi.InputMethod#removeNotify()
     */
    public void removeNotify() {
    }

    /**
     * @see java.awt.im.spi.InputMethod#setCharacterSubsets(java.lang.Character.Subset[])
     */
    public void setCharacterSubsets(Subset[] subsets) {
    }

    /**
     * @see java.awt.im.spi.InputMethod#setInputMethodContext(java.awt.im.spi.InputMethodContext)
     */
    public void setInputMethodContext(InputMethodContext context) {
        this.context = context;
    }

    /**
     * @return get the source of terms
     */
    protected InputTermSource<V> getTermSource() {
        return this.termSource;
    }

    /**
     * Defines control behaviors for the InputMethod.
     * InputTermMethod implementations can return
     * a custom implementation that has add additional
     * controls.
     */
    static public interface Control {

        /**
         * Obtain whether the method should use a window
         * below-the-spot for raw input (this window is
         * separate from the Java Input Method Framework's
         * below-the-spot handling of converted characters).
         * If false, raw input will be composed inline
         * with converted but uncommitted text.
         *
         * @return true if using a raw window
         */
        public boolean isUsingRawWindow();

        /**
         * Set whether the method should use a separate
         * window for raw input, or whether it should be
         * composed inline with other converted but
         * uncommitted text.
         *
         * @param usingRawWindow
         */
        public void setUsingRawWindow(boolean usingRawWindow);

        /**
         * Get the orientation of the alternatives paging
         * chooser that appears below-the-spot when choosing
         * an alternative.
         *
         * @return chooser orientation, true for vertical, false for horizontal
         */
        public boolean getChooserOrientation();

        /**
         * Set the orientation of the alternatives paging
         * chooser that appears below-the-spot when choosing
         * an aternative.
         *
         * @param chooserOrientation true for vertical, false for horizontal
         */
        public void setChooserOrientation(boolean chooserOrientation);

        /**
         * Get the font that this InputMethod is using in the components
         * it displays in its below-the-spot windows (raw mode and
         * alternates chooser).
         * @return the font
         */
        public Font getFont();

        /**
         * Set the font that this InputMethod is using in the components
         * it displays in its below-the-spot windows (raw mode and
         * alternates chooser).
         * @param font the font
         */
        public void setFont(Font font);

        /**
         * @return true if the InputMethod is enabled, false if input passes through
         */
        public boolean isEnabled();

        /**
         * @param enabled true if the InputMethod is enabled, false if input passes through
         */
        public void setEnabled(boolean enabled);
    }
}

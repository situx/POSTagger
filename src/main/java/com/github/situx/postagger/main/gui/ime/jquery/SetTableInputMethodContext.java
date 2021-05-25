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

package com.github.situx.postagger.main.gui.ime.jquery;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.font.TextHitInfo;
import java.awt.im.InputContext;
import java.awt.im.InputMethodRequests;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodContext;
import java.awt.im.spi.InputMethodDescriptor;
import java.lang.Character.Subset;
import java.text.AttributedCharacterIterator;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.Locale;

import javax.swing.*;


/**
 * An InputContext/InputMethodContext that can be loaded with an
 * InputMethodDescriptor in order to configure it with an
 * InputMethod.  Instances of these can then be returned
 * from text components' overridden getInputContext method
 * in order to hard-wire the InputMethod that those components use.
 *
 * A bit of a kludge, but it allows programmatic determination
 * of a InputMethod for a text component.  The Java input method
 * framework is really only supposed to allow user-set methods
 * and not programmatic selection, which is sometimes kind of
 * annoying, as when you want to bundle an InputMethod with
 * a program and/or have a particular text component use a
 * different method.  This is an attempt to get around that.
 *
 * @author Jordan Kiang
 */
public class SetTableInputMethodContext extends InputContext implements InputMethodContext, ComponentListener, WindowListener  {

    private Component clientComponent;
    private InputMethodRequests inputMethodRequests;

    private InputMethod inputMethod;

    private Window rootWindow;
    private boolean clientWindowNotificationEnabled = true;
    private Rectangle lastWindowBounds;

    /**
     * Build a new instance for the given component, configuring it
     * with an InputMethod as determined by the given InputMethodDescriptor.
     *
     * @param clientComponent
     * @param inputMethodDescriptor
     */
    public SetTableInputMethodContext(Component clientComponent, InputMethodDescriptor inputMethodDescriptor) {
        if(null == clientComponent) {
            throw new NullPointerException("clientComponent cannot be null!");
        } else if(null == inputMethodDescriptor) {
            throw new NullPointerException("inputMethodDescriptor cannot be null!");
        }

        this.clientComponent = clientComponent;
        this.inputMethodRequests = clientComponent.getInputMethodRequests();

        try {
            this.inputMethod = inputMethodDescriptor.createInputMethod();
            this.inputMethod.setInputMethodContext(this);

        } catch(Exception e) {
            // throw it up as a Runtime, just to get around having
            // to declare the Exception
            throw new RuntimeException("Unable to create InputMethod!", e);
        }
    }

    /**
     * @see java.awt.im.InputContext#selectInputMethod(java.util.Locale)
     */
    @Override
    public boolean selectInputMethod(Locale locale) {
        return this.inputMethod.setLocale(locale);
    }

    /**
     * @see java.awt.im.InputContext#getLocale()
     */
    @Override
    public Locale getLocale() {
        return this.inputMethod.getLocale();
    }

    /**
     * @see java.awt.im.InputContext#setCharacterSubsets(java.lang.Character.Subset[])
     */
    @Override
    public void setCharacterSubsets(Subset[] subsets) {
        this.inputMethod.setCharacterSubsets(subsets);
    }

    /**
     * @see java.awt.im.InputContext#setCompositionEnabled(boolean)
     */
    @Override
    public void setCompositionEnabled(boolean enable) {
        this.inputMethod.setCompositionEnabled(enable);
    }

    /**
     * @see java.awt.im.InputContext#isCompositionEnabled()
     */
    @Override
    public boolean isCompositionEnabled() {
        return this.inputMethod.isCompositionEnabled();
    }

    /**
     * @see java.awt.im.InputContext#reconvert()
     */
    @Override
    public void reconvert() {
        this.inputMethod.reconvert();
    }

    /**
     * @see java.awt.im.InputContext#dispatchEvent(java.awt.AWTEvent)
     */
    @Override
    public void dispatchEvent(AWTEvent event) {
        this.inputMethod.dispatchEvent(event);
    }

    /**
     * @see java.awt.im.InputContext#removeNotify(java.awt.Component)
     */
    @Override
    public void removeNotify(Component client) {
        // noop
    }

    /**
     * @see java.awt.im.InputContext#endComposition()
     */
    @Override
    public void endComposition() {
        this.inputMethod.endComposition();
    }

    /**
     * @see java.awt.im.InputContext#dispose()
     */
    @Override
    public void dispose() {
        if(null != this.rootWindow) {
            // de-register this instance as a listener of the Window
            this.rootWindow.removeComponentListener(this);
            this.rootWindow.removeWindowListener(this);
        }
    }

    /**
     * @see java.awt.im.InputContext#getInputMethodControlObject()
     */
    @Override
    public Object getInputMethodControlObject() {
        return this.inputMethod.getControlObject();
    }

    ////////////////////////////////////////////////////////////////////
    // Methods from interface java.awt.im.spi.InputMethodContext

    /**
     * @see java.awt.im.spi.InputMethodContext#dispatchInputMethodEvent(int, java.text.AttributedCharacterIterator, int, java.awt.font.TextHitInfo, java.awt.font.TextHitInfo)
     */
    public void dispatchInputMethodEvent(int id,
                                         AttributedCharacterIterator text, int committedCharacterCount,
                                         TextHitInfo caret, TextHitInfo visiblePosition) {

        InputMethodEvent event = new InputMethodEvent(this.clientComponent, id, text, committedCharacterCount, caret, visiblePosition);
        this.clientComponent.dispatchEvent(event);
    }

    /**
     * @see java.awt.im.spi.InputMethodContext#createInputMethodWindow(java.lang.String, boolean)
     */
    public Window createInputMethodWindow(String title, boolean attachToInputContext) {
        Window rootWindow = this.getRootWindow();
        SetTableInputMethodContextWindow window = new SetTableInputMethodContextWindow(rootWindow);

        if(attachToInputContext) {
            // if the new window shares the context,
            // then we set this as the context of the new window
            window.setInputContext(this);
        }

        return window;
    }

    /**
     * @see java.awt.im.spi.InputMethodContext#createInputMethodJFrame(java.lang.String, boolean)
     */
    public JFrame createInputMethodJFrame(String title, boolean attachToInputContext) {
        SetTableInputContextJFrame frame = new SetTableInputContextJFrame(title);

        if(attachToInputContext) {
            // if the new window shares the context,
            // then we set this as the context of the new frame
            frame.setInputContext(this);
        }

        return frame;
    }

    /**
     * @see java.awt.im.spi.InputMethodContext#enableClientWindowNotification(java.awt.im.spi.InputMethod, boolean)
     */
    public void enableClientWindowNotification(InputMethod inputMethod, boolean enable) {
        this.clientWindowNotificationEnabled = enable;
        if(enable) {
            this.notifyClientWindowChanged(true);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Methods from interface java.awt.im.InputMethodRequests

    /**
     * @see java.awt.im.InputMethodRequests#getTextLocation(java.awt.font.TextHitInfo)
     */
    public Rectangle getTextLocation(TextHitInfo offset) {
        return this.inputMethodRequests.getTextLocation(offset);
    }

    /**
     * @see java.awt.im.InputMethodRequests#getLocationOffset(int, int)
     */
    public TextHitInfo getLocationOffset(int x, int y) {
        return this.inputMethodRequests.getLocationOffset(x, y);
    }

    /**
     * @see java.awt.im.InputMethodRequests#getInsertPositionOffset()
     */
    public int getInsertPositionOffset() {
        return this.inputMethodRequests.getInsertPositionOffset();
    }

    /**
     * @see java.awt.im.InputMethodRequests#getCommittedText(int, int, java.text.AttributedCharacterIterator.Attribute[])
     */
    public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, Attribute[] attributes) {
        return this.inputMethodRequests.getCommittedText(beginIndex, endIndex, attributes);
    }

    /**
     * @see java.awt.im.InputMethodRequests#getCommittedTextLength()
     */
    public int getCommittedTextLength() {
        return this.inputMethodRequests.getCommittedTextLength();
    }

    /**
     * @see java.awt.im.InputMethodRequests#cancelLatestCommittedText(java.text.AttributedCharacterIterator.Attribute[])
     */
    public AttributedCharacterIterator cancelLatestCommittedText(Attribute[] attributes) {
        return this.inputMethodRequests.cancelLatestCommittedText(attributes);
    }

    /**
     * @see java.awt.im.InputMethodRequests#getSelectedText(java.text.AttributedCharacterIterator.Attribute[])
     */
    public AttributedCharacterIterator getSelectedText(Attribute[] attributes) {
        return this.inputMethodRequests.getSelectedText(attributes);
    }

    /////////////////////////////////////////
    // Methods from Window/ComponentListener
    // use to notify the InputMethod of WindowEvents.

    /**
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    public void componentHidden(ComponentEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    public void componentMoved(ComponentEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    public void componentResized(ComponentEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    public void componentShown(ComponentEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * @see java.awt.event.WindowListener#windowActivated(java.awt.event.WindowEvent)
     */
    public void windowActivated(WindowEvent e) {
        // on window activation we need to notify
        // so that the child windows will get shown
        // even if the bounds didn't change
        this.notifyClientWindowChanged(false);
    }

    /**
     * @see java.awt.event.WindowListener#windowClosed(java.awt.event.WindowEvent)
     */
    public void windowClosed(WindowEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
     */
    public void windowClosing(WindowEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * @see java.awt.event.WindowListener#windowDeactivated(java.awt.event.WindowEvent)
     */
    public void windowDeactivated(WindowEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * @see java.awt.event.WindowListener#windowDeiconified(java.awt.event.WindowEvent)
     */
    public void windowDeiconified(WindowEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * @see java.awt.event.WindowListener#windowIconified(java.awt.event.WindowEvent)
     */
    public void windowIconified(WindowEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * @see java.awt.event.WindowListener#windowOpened(java.awt.event.WindowEvent)
     */
    public void windowOpened(WindowEvent e) {
        this.notifyClientWindowChanged(true);
    }

    /**
     * Notify the InputMethod of WindowEvents if the Window bounds changes.
     */
    private void notifyClientWindowChanged(boolean onlyIfBoundsChanged) {
        if(this.clientWindowNotificationEnabled) {
            // only do anything if notificatin enabled

            Window rootWindow = this.getRootWindow();
            if(null == rootWindow || !rootWindow.isVisible() || ((Frame)rootWindow).getState() == 1) {
                // window is not visible
                this.inputMethod.notifyClientWindowChange(null);

            } else {
                Rectangle windowBounds = rootWindow.getBounds();

                // notify the InputMethod if the window
                if(!onlyIfBoundsChanged || !windowBounds.equals(this.lastWindowBounds)) {
                    this.inputMethod.notifyClientWindowChange(windowBounds);
                    this.lastWindowBounds = windowBounds;
                }
            }
        }
    }

    /**
     * Obtain the root parent Window of the client component.
     * Use this as the owner Window when creating new
     * Windows for the InputMethod.
     *
     * @return root Window
     */
    private Window getRootWindow() {
        // we can't cache the window immediately on instantation,
        // since we get the window from the client component, and
        // the client component may not have been added to a window
        // yet by the time this instance is created.  so we lazily
        // cache the root window at a later point when it's available.

        if(null == this.rootWindow) {
            // traverse up the component hierarchy till we find
            // the window.
            this.rootWindow = com.github.situx.postagger.main.gui.ime.util.SwingUtilities.getRootWindow(this.clientComponent);
            if(null == this.rootWindow) {
                throw new IllegalStateException("associated component is Window-less!");
            }

            // register this as a listener of the window.
            // need to be able to notify client window
            // of parent window events.
            this.rootWindow.addWindowListener(this);
            this.rootWindow.addComponentListener(this);
        }

        return this.rootWindow;
    }
}
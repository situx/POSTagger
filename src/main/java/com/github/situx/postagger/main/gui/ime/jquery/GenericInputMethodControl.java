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

import com.github.situx.postagger.main.gui.ime.descriptor.GenericInputMethod;

import java.awt.Font;

/**
 * An base implementation of TermInputMethod.Control
 * that supports the base control options:
 * raw window usage (raw below the spot or inline)
 * chooser orientation (vertical or horizontal)
 * 
 * @author Jordan Kiang
 */
public class GenericInputMethodControl implements GenericInputMethod.Control {
	
	private boolean enabled = true;
	private boolean usingRawWindow = true;
	private boolean chooserOrientation = true;
	private Font font;


	public boolean isUsingRawWindow() {
		return this.usingRawWindow;
	}

	public void setUsingRawWindow(boolean usingRawWindow) {
		this.usingRawWindow = usingRawWindow;
	}
	

	public boolean getChooserOrientation() {
		return this.chooserOrientation;
	}
	

	public void setChooserOrientation(boolean vertical) {
		this.chooserOrientation = vertical;
	}


	public Font getFont() {
		return this.font;
	}
	

	public void setFont(Font font) {
		this.font = font;
	}


	public boolean isEnabled() {
		return this.enabled;
	}


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}

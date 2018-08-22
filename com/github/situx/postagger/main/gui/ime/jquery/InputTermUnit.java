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

package com.github.situx.postagger.main.gui.ime.jquery;/* Copyright (c) 2007 Jordan Kiang
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


/**
 * An InputTermUnit is a subunit of an InputTerm.
 * Taking Chinese as an example, an InputTermUnit
 * represents a character of unit.  The inputKey
 * is the raw input used to select a character,
 * while the displayString is the character.
 * 
 * @param <V> the parameterized value type
 * 
 * @author Jordan Kiang
 */
public class InputTermUnit<V> {

	private V inputKey;
	private String displayString;
	
	/**
	 * Generate a new InputTermUnit.
	 * 
	 * @param inputKey the raw input
	 * @param displayString (the uncommitted text)
	 */
	public InputTermUnit(V inputKey, String displayString) {
		if(null == inputKey) {
			throw new NullPointerException("inputKey cannot be null!");
		} else if(null == displayString) {
			throw new NullPointerException("displayString cannot be null!");
		}
		
		this.inputKey = inputKey;
		this.displayString = displayString;
	}
	
	/**
	 * @return the input key (the raw input)
	 */
	public V getInputKey() {
		return this.inputKey;
	}
	
	/**
	 * @return the display string (the uncommitted text)
	 */
	@Override
	public String toString() {
		return this.displayString;
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof InputTermUnit) {
			InputTermUnit<?> that = (InputTermUnit<?>)o;
			
			// key and display String should match
			return this.inputKey.equals(that.inputKey) &&
					this.displayString.equals(that.toString());
			
		}
		
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		// eh... good enough
		return this.inputKey.hashCode() + this.displayString.hashCode();
	}
}

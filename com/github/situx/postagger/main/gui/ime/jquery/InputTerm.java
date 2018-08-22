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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * For use when manipulating uncommitted text in a TermInputMethod.
 *
 * An InputTerm is a sequence of InputTermUnits, which together
 * constitute a "term".  The uncommitted text of a TermInputMethod
 * consists of one or more InputTerms.  The uncommitted text
 * can be manipulated at the term level, or at the level of the
 * individual units.  If a unit within a term is changed, the
 * term itself will be broken up into sub terms.  Taking Chinese
 * as an example, an InputTerm amounts to a word, and an InputTermUnit
 * amounts to a character. 
 *
 * Taking Chinese as an example, an InputTerm is a word, and an
 * InputTermUnit is a character.
 * 
 * @param <V> the unit type
 * 
 * @author Jordan Kiang
 */
public class InputTerm<V> {

	private List<InputTermUnit<V>> units;
	private String anticipatedSuffix;
	
	/**
	 * Generate an InputTerm from the given units and optionally anticipated text.
	 * 
	 * @param units the units that are generated from input
	 * @param anticipatedSuffix additional text that was anticipated, but wasn't directly generated from input
	 */
	public InputTerm(List<InputTermUnit<V>> units, String anticipatedSuffix) {
		if(null == units) {
			throw new NullPointerException("units cannot be null!");
		}
	
		// make a defensive copy of the InputTerm units,
		// and make it unmodifiable through the term.
		this.units = Collections.unmodifiableList(new ArrayList<InputTermUnit<V>>(units));
	
		this.anticipatedSuffix = null != anticipatedSuffix ? anticipatedSuffix : "";
	}
	
	/**
	 * The InputTermUnits of the term.s
	 * @return the units that compose the term
	 */
	public List<InputTermUnit<V>> getUnits() {
		return this.units;
	}
	
	/**
	 * @return the anticipated suffix
	 */
	public String getAnticipatedSuffix() {
		return this.anticipatedSuffix;
	}
	
	/**
	 * Generate a new InputTerm from a susbsequence of the InputTermUnits
	 * that compose this term.
	 * 
	 * @param fromIndex inclusive
	 * @param toIndex exclusive
	 * @return new InputTerm formed from the given range
	 */
	public InputTerm<V> subTerm(int fromIndex, int toIndex) {
		List<InputTermUnit<V>> subUnits = this.units.subList(fromIndex, toIndex);
		return new InputTerm<V>(subUnits, null);
	}
	
	/**
	 * @return the InputKeys that were used to generate this term
	 */
	public List<V> getInputKeys() {
		List<V> inputKeys = new ArrayList<V>(this.units.size());
		for(InputTermUnit<V> unit : this.units) {
			inputKeys.add(unit.getInputKey());
		}
		
		return inputKeys;
	}
	
	/**
	 * A String of only the parts of this term that
	 * were generated directly from input keys (i.e.
	 * excluding any anticipated text on the end).
	 * 
	 * @return String representation of the matched text of the term
	 */
	public String toMatchedString() {
		// concantenate the unit display Strings together
		// to form the display String for the whole InputTerm.
		StringBuilder displayBuf = new StringBuilder();
		for(InputTermUnit<V> unit : this.units) {
			displayBuf.append(unit.toString());
		}

		return displayBuf.toString();
	}
	
	/**
	 * A String of all the text of this term, including
	 * text from both matched units and any anticipated
	 * text appended on the end.
	 * 
	 * @return String representation of the term including anticipated text
	 */
	public String toAnticipatedString() {
		return this.toMatchedString() + this.anticipatedSuffix;
	}
	
	/**
	 * Just return the anticipated String.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.toAnticipatedString();
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if(o instanceof InputTerm) {
			InputTerm that = (InputTerm)o;
			
			List<InputTermUnit<V>> thisUnits = this.units;
			List<InputTermUnit> thatUnits = that.getUnits(); 
		
			if(thisUnits.size() == thatUnits.size()) {
				for(int i = 0; i < thisUnits.size(); i++) {
					InputTermUnit<V> thisUnit = thisUnits.get(i);
					InputTermUnit thatUnit = thatUnits.get(i);
					
					if(!thisUnit.equals(thatUnit)) {
						return false;
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hashCode = 0;
		for(InputTermUnit<V> unit : this.units) {
			hashCode += unit.hashCode();
		}
		
		return hashCode;
	}
}

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
import com.github.situx.postagger.util.Tuple;

import java.awt.im.spi.InputMethod;
import java.util.List;
import java.util.Locale;


/**
 * A pluggable source of data for an TermInputMethod.
 *
 * @param <V> the parameterized input key type
 * 
 * @author Jordan Kiang
 */
public interface InputTermSource<V> {
	
	/**
	 * Takes a raw input String and obtains an InputKey for it.
	 * 
	 * @param raw the raw input
	 * @return an InputKey, null if the raw String doesn't compose to a key
	 */
    V getInputKey(String raw);
    
    /**
     * Determines whether the raw input is a partial prefix
     * of an InputKey.  Used to check if sequential additional
     * characters form legal input leading to an InputKey.
     * i.e. if "foo" forms an InputKey, "f" and "fo" and "foo"
     * are all legal and will return true.
     * 
     * @param raw a raw input String
     * @return true if the input is legal as an initial substring of an InputKey
     */
    boolean isPartialInputKey(String raw);

    /**
	 * Obtains candidate InputTerms that match the given InputKeys.
	 * 
	 * @param inputKeys
	 * @return candidate InputTerms
	 */
    List<Tuple<InputTerm<V>,String>> lookupTerms(List<V> inputKeys);

	/**
	 * Converts the raw character to a String.
	 * Might just convert it straight, or change the case, etc..
	 * 
	 * @param characterInput
	 * @return a String for the given character input
	 */
	String convertRawCharacter(char characterInput);
	
	/**
	 * Whether a character of input be passed through
	 * to the text component if it couldn't otherwise
	 * be consumed by the InputMethod
	 * @param characterInput
	 * @return true if it should pass through, false otherwise
	 */
	boolean shouldPassThrough(char characterInput);
   
	/**
	 * Set the Locale.
	 * @param locale
	 * @return whether the Locale was supported
	 * @see InputMethod#setLocale(Locale)
	 */
	boolean setLocale(Locale locale);
	
	/**
	 * @return the Locale
	 * @see InputMethod#getLocale()
	 */
	Locale getLocale();
	
	/**
	 * @return a Control Object for the InputMethod
	 */
	GenericInputMethod.Control getControlObject();
}

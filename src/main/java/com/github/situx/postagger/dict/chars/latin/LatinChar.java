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

package com.github.situx.postagger.dict.chars.latin;

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.util.enums.methods.CharTypes;

/**
 * Created by timo on 17.06.14.
 * Represents a latin character.
 */
public abstract class LatinChar extends LangChar {
    /**Indicates if this character is an adjective.*/
    private Boolean isAdjective;
    /**Indicates if this character is a noun.*/
    private Boolean isNoun;
    /**Indicates if this character is a verb.*/
    private Boolean isVerb;
    /**Indicates if this character is a word.*/
    private Boolean isWord;
    /**The lemma of the character.*/
    private String lemma;
    /**The relative occurance of this character.*/
    private Double relativeOccurance;

    /**
     * Constructor for this class.
     * @param character the character/word
     */
    public LatinChar(final String character) {
        super(character);this.charlength= CharTypes.LATIN.getChar_length();
    }

    /**
     * Indicates if this character/word is an adjective.
     * @return true if it is false otherwise
     */
    public Boolean getIsAdjective() {
        return isAdjective;
    }

    /**
     * Sets if the character/word is an adjective.
     * @param isAdjective adjective indicator
     */
    public void setIsAdjective(final Boolean isAdjective) {
        this.isAdjective = isAdjective;
    }

    /**
     * Indicates if this character/word is a noun.
     * @return true if it is false otherwise
     */
    public Boolean getIsNoun() {
        return isNoun;
    }

    /**
     * Sets if this character/word is a noun.
     * @param isNoun noun indicator
     */
    public void setIsNoun(final Boolean isNoun) {
        this.isNoun = isNoun;
    }

    /**
     * Indicate if this character/word is a verb.
     * @return true if it is false otherwise
     */
    public Boolean getIsVerb() {
        return isVerb;
    }

    /**
     * Sets if this character/word is a verb.
     * @param isVerb verb indicator
     */
    public void setIsVerb(final Boolean isVerb) {
        this.isVerb = isVerb;
    }

    /**
     * Indicates if this character is a word.
     * @return true if it is false otherwise
     */
    public Boolean getIsWord() {
        return isWord;
    }

    /**
     * Sets if this character/word is a word.
     * @param isWord word indicator
     */
    public void setIsWord(final Boolean isWord) {
        this.isWord = isWord;
    }

    @Override
    public Double getRelativeOccurance() {
        return null;
    }

    @Override
    public void setRelativeOccurance(final Double occurance) {

    }

    @Override
    public void setRelativeOccuranceFromDict(final Double relocc){
        this.relativeOccurance=relocc;
    }
}

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

package com.github.situx.postagger.dict.chars;

import com.github.situx.postagger.dict.utils.Transliteration;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents a character that is positionable i.e. has middle/single/end/beginning occurances.
 */
public abstract class PositionableChar extends LangChar{

    /**Indicates if this character occurs at the beginning of a word.*/
    protected Boolean beginningCharacter;
    /**The transliterations of this char/word.*/
    protected Map<Transliteration,Integer> begintransliterations;
    /**Indicates if this character occurs at the end of a word.*/
    protected Boolean endingCharacter;
    protected Map<Transliteration,Integer> endtransliterations;
    /**Indicates if this character occurs in the middle of a word.*/
    protected Boolean middleCharacter;
    /**Amounts of the middle begin, single and end occurances of this character.*/
    protected Double middleoccurance, beginoccurance, endoccurance, singleoccurance;
    /**Map of middle transliterations.*/
    protected Map<Transliteration,Integer> middletransliterations;
    /**
     * The positions on which this char has been seen.
     */
    protected Set<Integer> positions;
    /**Indicates if this characters also exists as a single word.*/
    protected Boolean singleCharacter;
    protected Map<Transliteration,Integer> singletransliterations;

    /**
     * Constructor for this class.
     * @param character the character
     */
    public PositionableChar(final String character) {
        super(character);
        this.singletransliterations = new TreeMap<>();
        this.endtransliterations = new TreeMap<>();
        this.middletransliterations = new TreeMap<>();
        this.begintransliterations = new TreeMap<>();
        this.positions = new TreeSet<>();
        this.beginoccurance = 0.;
        this.middleoccurance = 0.;
        this.endoccurance = 0.;
        this.singleoccurance = 0.;
        this.beginningCharacter = false;
        this.endingCharacter = false;
        this.middleCharacter = false;
        this.singleCharacter = false;
    }

    /**
     * Adds a beginning occurance of this character.
     */
    public void addBeginOccurance() {
        this.beginoccurance++;
        this.occurances++;
        this.positions.add(0);
    }

    /**
     * Adds an end occurance of this character.
     *
     * @param position the position of this end occurance
     */
    public void addEndOccurance(final Integer position) {
        this.endoccurance++;
        this.occurances++;
        this.positions.add(position);
    }

    /**
     * Adds a middle occurance of this character.
     *
     * @param position the position of this middle occurance
     */
    public void addMiddleOccurance(final Integer position) {
        this.middleoccurance++;
        this.occurances++;
        this.positions.add(position);
    }

    /**
     * Adds a position to the set of positions of this character.
     * @param position the position to be added
     */
    public void addPosition(final Integer position) {
        this.positions.add(position);
    }

    /**Adds a single occurance of this character.*/
    public void addSingleOccurance() {
        this.singleoccurance++;
        this.occurances++;
        this.positions.add(0);
    }

    /**
     * Gets the amount of beginning occurances of this character.
     *
     * @return the occurance as double
     */
    public Double getBeginOccurance() {
        return this.beginoccurance;
    }

    /**
     * Sets the amount of beginning occurances for this character.
     *
     * @param occuranceAmount the amount of occurances to set
     */
    public void setBeginOccurance(final Double occuranceAmount) {
        this.beginoccurance = occuranceAmount;
    }

    /**
     * Gets the amount of end occurances of this character.
     * @return the amount of end occurances
     */
    public Double getEndOccurance() {
        return this.endoccurance;
    }

    /**
     * Sets the amount of end occurances for this character.
     *
     * @param occuranceAmount the amount of occurances to set
     */
    public void setEndOccurance(final Double occuranceAmount) {
        this.endoccurance = occuranceAmount;
    }

    /**
     * Gets the first beginning transliteration of this character.
     *
     * @return the transliteration as transliteration object
     */
    public Transliteration getFirstBeginningTransliteration() {
        for (Transliteration transliteration : this.transliterations.keySet()) {
            if (transliteration.isBeginTransliteration()) {
                return transliteration;
            }
        }
        return this.transliterations.keySet().isEmpty()?new Transliteration("",""):this.transliterations.keySet().iterator().next();
    }

    /**
     * Gets the first end transliteration of this character.
     *
     * @return the transliteration as transliteration object
     */
    public Transliteration getFirstEndTransliteration() {
        for (Transliteration transliteration : this.transliterations.keySet()) {
            if (transliteration.isEndTransliteration()) {
                return transliteration;
            }
        }
        return this.transliterations.keySet().iterator().next();
    }

    /**
     * Gets the first middle transliteration of this character.
     *
     * @return the transliteration as transliteration object
     */
    public Transliteration getFirstMiddleTransliteration() {
        for (Transliteration transliteration : this.transliterations.keySet()) {
            if (transliteration.isMiddleTransliteration()) {
                return transliteration;
            }
        }
        return this.transliterations.keySet().isEmpty()?new Transliteration("",""):this.transliterations.keySet().iterator().next();
    }

    /**
     * Gets the first single transliteration of this character.
     *
     * @return the transliteration as transliteration object
     */
    public Transliteration getFirstSingleTransliteration() {
        for (Transliteration transliteration : this.transliterations.keySet()) {
            if (transliteration.isSingleTransliteration()) {
                return transliteration;
            }
        }
        if(this.transliterations.isEmpty()){
            return new Transliteration(" "," ");
        }
        return this.transliterations.keySet().iterator().next();
    }

    /**
     * Gets the amount of middle occurances of this character.
     *
     * @return the amount as Double
     */
    public Double getMiddleoccurance() {
        return this.middleoccurance;
    }

    /**
     * Gets the most probable begin transliteration of this character.
     *
     * @param position the position of the single character
     * @return the transliteration as transliteration object
     */
    public Transliteration getMostProbableBeginTransliteration(final Integer position) {
        Integer maxprob = 0;
        Transliteration result = this.transliterations.keySet().iterator().next();
        for (Transliteration transliteration : this.transliterations.keySet()) {
            if (!transliteration.getTransliteration().matches(".*[A-Z]+.*") && transliteration.isBeginTransliteration() && transliteration.getOccuranceAtPosition(position) != null
                    && transliteration.getOccuranceAtPosition(position).getOccuranceAmount() > maxprob) {
                maxprob = transliteration.getOccuranceAtPosition(position).getOccuranceAmount();
                result = transliteration;

            }
        }
        return result;
    }

    /**
     * Gets the most probable end transliteration of this character.
     *
     * @param position the position of the single character
     * @return the transliteration as transliteration object
     */
    public Transliteration getMostProbableEndTransliteration(final Integer position) {
        Integer maxprob = 0;
        Transliteration result = this.transliterations.keySet().iterator().next();
        for (Transliteration transliteration : this.transliterations.keySet()) {
            if (!transliteration.getTransliteration().matches(".*[A-Z]+.*") && transliteration.isEndTransliteration() && transliteration.getOccuranceAtPosition(position) != null
                    && transliteration.getOccuranceAtPosition(position).getOccuranceAmount() > maxprob) {
                maxprob = transliteration.getOccuranceAtPosition(position).getOccuranceAmount();
                result = transliteration;

            }
        }
        return result;
    }

    /**
     * Gets the most probable middle transliteration of this character.
     *
     * @param position the position of the single character
     * @return the transliteration as transliteration object
     */
    public Transliteration getMostProbableMiddleTransliteration(final Integer position) {
        Integer maxprob = 0;
        Transliteration result = this.transliterations.keySet().iterator().next();
        for (Transliteration transliteration : this.transliterations.keySet()) {
            if (!transliteration.getTransliteration().matches(".*[A-Z]+.*") && transliteration.isMiddleTransliteration() && transliteration.getOccuranceAtPosition(position) != null && transliteration.getOccuranceAtPosition(position).getOccuranceAmount() > maxprob) {
                maxprob = transliteration.getOccuranceAtPosition(position).getOccuranceAmount();
                result = transliteration;

            }
        }
        return result;
    }

    /**
     * Gets the most probable single transliteration of this character.
     *
     * @param position the position of the single character
     * @return the transliteration as transliteration object
     */
    public Transliteration getMostProbableSingleTransliteration(final Integer position) {
        Integer maxprob = 0;
        Transliteration result = this.transliterations.keySet().iterator().next();
        for (Transliteration transliteration : this.transliterations.keySet()) {
            if (!transliteration.getTransliteration().matches(".*[A-Z]+.*") && transliteration.isSingleTransliteration() && transliteration.getOccuranceAtPosition(position) != null && transliteration.getOccuranceAtPosition(position).getOccuranceAmount() > maxprob) {
                maxprob = transliteration.getOccuranceAtPosition(position).getOccuranceAmount();
                result = transliteration;

            }
        }
        return result;
    }

    /**
     * Gets the amount of single occurances for this character.
     * @return the amount of single occurances as Double
     */
    public Double getSingleOccurance() {
        return singleoccurance;
    }

    /**
     * Sets the amount of single occurances for this character.
     * @param singleOccurance the amount of single occurances as Double
     */
    public void setSingleOccurance(final Double singleOccurance) {
        this.singleoccurance = singleOccurance;
    }

    /**Indicates if this char is a beginning char.
     *
     * @return true if it is false otherwise
     */
    public Boolean isBeginningCharacter() {
        return beginningCharacter;
    }

    /**Indicates if this char is an ending char.
     *
     * @return true if it is false otherwise
     */
    public Boolean isEndingCharacter() {
        return endingCharacter;
    }

    /**Indicates if this char is a middle char.
     *
     * @return true if it is false otherwise
     */
    public Boolean isMiddleCharacter() {
        return middleCharacter;
    }

    /**Indicates if this char is a single char.
     *
     * @return true if it is false otherwise
     */
    public Boolean isSingleCharacter() {
        return singleCharacter;
    }

    /**
     * Sets if this chracter is a beginning character or not.
     * @param beginningCharacter single character indicator as boolean
     */
    public void setBeginningCharacter(final Boolean beginningCharacter) {
        this.beginningCharacter = beginningCharacter;
    }

    /**
     * Sets this character as an ending character.
     *
     * @param endingCharacter indicates if it should be set as an ending character
     */
    public void setEndingCharacter(final Boolean endingCharacter) {
        this.endingCharacter = endingCharacter;
    }

    /**
     * Sets if this chracter is a middle character or not.
     * @param middleCharacter middle character indicator as boolean
     */
    public void setMiddleCharacter(final Boolean middleCharacter) {
        this.middleCharacter = middleCharacter;
    }

    /**
     * Sets the amount of middle occurances for this character.
     *
     * @param occuranceAmount the amount of occurances to set
     */
    public void setMiddleOccurance(final Double occuranceAmount) {
        this.middleoccurance = occuranceAmount;
    }

    /**
     * Sets if this chracter is a single character or not.
     * @param singleCharacter single character indicator as boolean
     */
    public void setSingleCharacter(final Boolean singleCharacter) {
        this.singleCharacter = singleCharacter;
    }

}

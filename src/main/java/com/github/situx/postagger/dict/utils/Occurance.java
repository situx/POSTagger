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

package com.github.situx.postagger.dict.utils;

/**
 * Created with IntelliJ IDEA.
 * User: Timo Homburg
 * Date: 03.12.13
 * Time: 17:42
 * Class for modelling an occurance instance containing a relative and abolute word position as well as its amount.
 */
public class Occurance implements Comparable<Occurance> {
    /**
     * The absolute position within the word.
     */
    private Integer aboluteWordPosition;
    /**
     * The Amount of this occurance at its specified relative and absolute position.
     */
    private Integer occuranceAmount;
    /**
     * 0 for begin, 1 for middle, 2 for end, 3 for single
     */
    private Integer relativeWordPosition;

    /**Constructor for this class.*/
    public Occurance() {
        this.aboluteWordPosition = -1;
        this.relativeWordPosition = -1;
        this.occuranceAmount = 0;
    }

    /**
     * Constructor for this class.
     * @param relativeWordPosition the relative word position
     * @param aboluteWordPosition  the absolute word position
     */
    public Occurance(final Integer relativeWordPosition, final Integer aboluteWordPosition) {
        this.relativeWordPosition = relativeWordPosition;
        this.aboluteWordPosition = aboluteWordPosition;
        this.occuranceAmount = 0;
    }

    /**Adds a transliteration occuranceAmount and increases the counter.*/
    public void addTransliterationOccurance() {
        this.occuranceAmount++;
    }

    @Override
    public int compareTo(final Occurance o) {
            return this.aboluteWordPosition.compareTo(o.aboluteWordPosition) +
                    this.relativeWordPosition.compareTo(o.relativeWordPosition) +
                    this.occuranceAmount.compareTo(o.occuranceAmount);
    }

    /**
     * Gets the absoluteWordPosition of this occurance.
     * @return the absolute word position as Integer
     */
    public Integer getAboluteWordPosition() {
        return aboluteWordPosition;
    }

    /**
     * Sets the absolute word position.
     * @param aboluteWordPosition the absolute word position as Integer
     */
    public void setAboluteWordPosition(final Integer aboluteWordPosition) {
        this.aboluteWordPosition = aboluteWordPosition;
    }

    /**
     * Gets the amount of occurances.
     * @return the occurance amount as Integer
     */
    public Integer getOccuranceAmount() {
        return occuranceAmount;
    }

    /**
     * Sets the amount of occurances by reading it from a file.
     *
     * @param occuranceAmount the occuraneAmount to set
     */
    public void setOccuranceAmount(final Integer occuranceAmount) {
        this.occuranceAmount = occuranceAmount;
    }

    /**
     * Returns the absolute word position.
     * @return the relative word position as Integer
     */
    public Integer getRelativeWordPosition() {
        return this.relativeWordPosition;
    }

    /**
     *  Sets the relativeWordPosition of this occurance.
     * @param relativeWordPosition the relative word position as Integer
     */
    public void setRelativeWordPosition(final Integer relativeWordPosition) {
        this.relativeWordPosition = relativeWordPosition;
    }

    @Override
    public String toString() {
        return this.occuranceAmount.toString();
    }
}

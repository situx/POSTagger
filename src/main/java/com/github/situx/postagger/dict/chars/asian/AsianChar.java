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

package com.github.situx.postagger.dict.chars.asian;

import com.github.situx.postagger.dict.chars.PositionableChar;
import com.github.situx.postagger.util.enums.methods.CharTypes;

/**
 * Created by timo on 17.06.14.
 * Abstract class to represent an Asian character.
 */
public abstract class AsianChar extends PositionableChar {

    /**The relative occurance of this char/word in the corpusimport.*/
    protected Double relativeoccurance;

    /**
     * Constructor for this class
     * @param character the character to add
     */
    public AsianChar(final String character) {
        super(character);
        this.charlength= CharTypes.ASIANCHAR.getChar_length();
    }

    @Override
    public Integer getCharlength() {
        return this.charlength;
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
        this.relativeoccurance=relocc;
    }

    @Override
    public String toXML(final String startelement,Boolean statistics) {
        return super.toXML(startelement,statistics);
    }
}

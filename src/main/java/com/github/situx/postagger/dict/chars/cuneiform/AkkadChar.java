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

package com.github.situx.postagger.dict.chars.cuneiform;

import com.github.situx.postagger.util.enums.methods.CharTypes;

/**
 * Represents an akkadian character/word.
 */
public class AkkadChar extends CuneiChar {

    /**
     * Constructor for this class.
     * @param character the character to use
     */
    public AkkadChar(final String character){
        super(character);
        this.charlength= CharTypes.AKKADIAN.getChar_length();
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof AkkadChar) {
            AkkadChar akkad = (AkkadChar) o;
            if (this.determinative.equals(akkad.determinative) && this.logograph.equals(akkad.logograph)
                    && this.phonogram.equals(akkad.phonogram) && this.character.equals(akkad.character)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void setStem(final String stem) {
        this.stem=stem;
    }

    @Override
    public String toString() {
        return this.character;
    }

    @Override
    public String toXML(String startelement,Boolean statistics) {
        return super.toXML(startelement,statistics);
    }
}

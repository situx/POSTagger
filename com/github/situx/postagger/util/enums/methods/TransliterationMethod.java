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

package com.github.situx.postagger.util.enums.methods;

/**
 * Enum for transliteration methods.
 */
public enum TransliterationMethod implements MethodEnum {
    FIRST("First Transliteration","first"),
    PROB("MaxProb Transliteration","maxprob"),
    RANDOM("Random Transliteration","random");
    String label,shortlabel;

    private TransliterationMethod(){


    }

    private TransliterationMethod(String label,String shortlabel){
         this.label=label;
         this.shortlabel=shortlabel;
    }

    public String getLabel() {
        return label;
    }

    public String getShortlabel() {
        return shortlabel;
    }

    @Override
    public String getShortname() {
        return null;
    }

    @Override
    public String toString() {
        return this.label;
    }
}

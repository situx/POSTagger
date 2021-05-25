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

package com.github.situx.postagger.util.enums.pos;

/**
 * Created by timo on 6/7/15.
 */
public enum WordCase {
    NONE(""),
    ABLATIVE("Ablative"),
    ABSOLUTIVE("Absolutive"),
    ACCUSATIVE("Accusative"),
    AKKUSATIVE("Akkusative"),
    ALLATIVE("Allative"),
    ANIMATE("Animate"),
    AFFIRMATIVE("Affirmative"),
    COMITATIVE("Comitative"),
    DATIVE("Dative"),
    DIRECTIVE("Directive"),
    DUAL("Dual"),
    EQUATIVE("Equative"),
    ERGATIVE("Ergative"),
    EMPHASIZE("Emphasize"),
    FEMALE("Female"),
    GENITIVE("Genitive"),
    INSTRUMENTAL("Instrumental"),
    INANIMATE("Inanimate"),
    ISPART("Is Particle"),
    LOCATIVE("Locative"),
    NEGATIVE("Negative"),
    NOMINATIVE("Nominative"),
    OBLIQUE("Oblique"),
    OPTATIVE("Optative"), //Wish that can be fulfilled
    PLURAL("Plural"),
    POSITIVE("Positive"), //Ist wirklich so
    QUOTATIVE("Quotative"),
    REFLEXIVE("Reflexive"),
    STATIVE("Stative"),
    SINGULAR("Singular"),
    TERMINATIVE("Terminative"),
    VENTIVE("Ventive"),
    VOLUNTIVE("Voluntive"),
    VETITIVE("Vetitive");

    private String name;

    private WordCase(String name){
        this.name=name;
    }

    @Override
    public String toString() {
        return this.name;
    }

}

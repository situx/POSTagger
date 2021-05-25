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
 * Langauage Case Enum.
 */
public enum Case {

    NOMINATIVE_SINGULAR("Nominative Singular"),
    NOMINATIVE_SINGULAR_FEMALE("Nominative Singular Female"),
    NOMINATIVE_DUAL("Nominative Dual"),
    NOMINATIVE_DUAL_FEMALE("Nominative Dual Female"),
    NOMINATIVE_PLURAL("Nominative Plural"),
    NOMINATIVE_PLURAL_FEMALE("Nominative Plural Female"),
    GENITIVE_SINGULAR("Genitive Singular"),
    GENITIVE_SINGULAR_FEMALE("Genitive Singular Female"),
    GENITIVE_DUAL("Genitive Dual"),
    GENITIVE_DUAL_FEMALE("Genitive Dual Female"),
    GENITIVE_PLURAL("Genitive Plural"),
    GENITIVE_PLURAL_FEMALE("Genitive Plural Female"),
    ACCUSATIVE_SINGULAR("Accusative Singular"),
    ACCUSATIVE_SINGULAR_FEMALE("Accusative Singular Female"),
    ACCUSATIVE_DUAL("Accusative Dual"),
    ACCUSATIVE_DUAL_FEMALE("Accusative Dual Female"),
    ACCUSATIVE_PLURAL("Accusative Plural"),
    ACCUSATIVE_PLURAL_FEMALE("Accusative Plural Female"),
    OBLIQUE_SINGULAR("Oblique Singular"),
    OBLIQUE_SINGULAR_FEMALE("Oblique Singular Female"),
    OBLIQUE_DUAL("Oblique Dual"),
    OBLIQUE_DUAL_FEMALE("Oblique Dual Female"),
    OBLIQUE_PLURAL("Oblique Plural"),
    OBLIQUE_PLURAL_FEMALE("Oblique Dual Female");

    private String name;

    private Case(String name){
        this.name=name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}

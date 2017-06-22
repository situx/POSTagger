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
 * Word Enum.
 */
public enum POSTags {


    AGENT("AGENT","http://purl.org/olia/olia.owl#Agent"),
    ADJECTIVE("ADJ","http://purl.org/olia/olia.owl#Adjective"),
    ADVERB("ADV","http://purl.org/olia/olia.owl#Adverb"),
    DETERMINATIVE("DET","http://purl.org/olia/olia.owl#Determiner"),
    CONJUNCTION("CONJ","http://purl.org/olia/olia.owl#Conjunction"),
    INDEFINITEPRONOUN("INDPRO","http://purl.org/olia/olia.owl#IndefinitePronoun"),
    DEMONSTRATIVEPRONOUN("DEMPRO","http://purl.org/olia/olia.owl#DemonstrativePronoun"),
    MODALPREFIX("MOD","http://purl.org/olia/olia.owl#Modal"),
    NOUN("NN","http://purl.org/olia/olia.owl#Noun"),
    NOUNORADJ("NA","http://purl.org/olia/olia.owl#NounOrAdjective"),
    NAMEDENTITY("NE","http://purl.org/olia/olia.owl#NamedEntity"),
    NUMBER("CARD","http://purl.org/olia/olia.owl#Cardinal"),
    PARTICLE("PART","http://purl.org/olia/olia.owl#Particle"),
    RELATIVEPRONOUN("RELPRO","http://purl.org/olia/olia.owl#RelativePronoun"),
    POSSESSIVE("POSS","http://purl.org/olia/olia.owl#Possessive"),
    PRECATIVE("PRE","http://purl.org/olia/olia.owl#Precative"),
    POSTPOSITION("POSTPOS","http://purl.org/olia/olia.owl#Postposition"),
    PRONOUN("PRO","http://purl.org/olia/olia.owl#Pronoun"),
    UNKNOWN("UNKNOWN","http://purl.org/olia/olia.owl#Unknown"),
    VERB("VV","http://purl.org/olia/olia.owl#Verb"),
    SUBJECT("SUBJECT","http://purl.org/olia/olia.owl#Subject"),
    OBJECT("OBJ","http://purl.org/olia/olia.owl#Object"),
    PREPOSITION("PREP","http://purl.org/olia/olia.owl#Preposition");

    private String tagdesc;

    private String uri;

    public String getUri() {
        return uri;
    }

    private POSTags(String tagdesc, String uri){
        this.tagdesc=tagdesc;
        this.uri=uri;

    }

    @Override
    public String toString() {
        return this.tagdesc;
    }
}

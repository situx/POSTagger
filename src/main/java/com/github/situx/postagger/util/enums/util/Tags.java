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

package com.github.situx.postagger.util.enums.util;

import com.github.situx.postagger.util.enums.methods.MethodEnum;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 03.12.13
 * Time: 23:19
 * To change this template use File | Settings | File Templates.
 */
public enum Tags implements MethodEnum {
    ABSBD("absboundary"),
    ABSOCC("absoluteOccurance"),
    AKK("akk"),
    AKKADIAN("Akkadian"),
    BEGIN("begin"),
    DETERMINATIVE("determinative"),
    END("end"),
    ESCAPECHARACTERS("escapeCharacters"),
    HIT("hit"),
    HITTITE("Hittite"),
    ISWORD("isWord"),
    LANGUAGE("language"),
    LOGO("logograph"),
    MAPENTRIES("mapentries"),
    MIDDLE("middle"),
    PHONO("phonogram"),
    POSITION("position"),
    RELOCC("relativeOccurance"),
    REVERSE("_reverse"),
    SIGNNAME("signName"),
    SINGLE("single"),
    SUMERIAN("Sumerian"),
    SUM("sux"),
    UTF8("UTF-8"),
    UTF16("UTF-16"),
    XMLVERSION("1.1"),
    CUNEIFORM("Cuneiform"),
    LOCALE("locale"),
    TRANSCRIPTION("transcription"),
    MEANING("meaning"),
    PERSONNUMBERCASE("personnumbercase"),
    STEM("stem"), DATA("data"), DEFAULT("default"), NGRAMS("ngrams");
    public static final java.lang.String NGRAM = "ngram";
    public static final String LENGTH = "length";
    public static final String SUMEROGRAM = "sumerogram";
    public static final String S = "s";
    public static final String FILE = "file";
    public static final java.lang.String TEXT = "text";
    public static final String RULE = "rule";
    public static final String ABZL = "aBZL";
    public static final String HBZL = "HethZL";
    public static final String SLHA = "LHA";
    public static final String CONCEPT = "concept";
    public static final String PATTERN = "pattern";
    public static final String START = "start";
    public static final String MAINLOCATION = "mainlocation";
    public static final String URI = "uri";
    public static final String NAME = "name";
    public static final String REF = "ref";
    public static final String REPRESENTATION = "representation";
    public static final String MOOD = "mood";
    public static final String TENSE = "tense";
    public static final String WORDCASE = "wordcase";
    public static final String NUMBER = "number";
    public static final String PERSON = "person";
    public static final String VOICE = "voice";
    public static final String ANIMACY = "animacy";
    public static final String TRANSPREFIX = "transprefix";
    public static final String TRANSSUFFIX = "transsuffix";
    String test="53 607 894 612";
    public static final String C = "c";
    public static final String DICTENTRY="dictentry";
    public static final String F = "f";
    public static final String FOLLOWING="following";
    public static final String LEMMA="lemma";
    public static final String LOGOGRAM="logogram";
    public static final String MAPENTRY="mapentry";
    public static final String SIGN="sign";
    public static final java.lang.String TRANSLATION = "translation";
    public static final String TRANSLITERATION="transliteration";
    public static final String VAL="val";
    public static final String W = "w";
    public static final java.lang.String WORDLIST = "wordlist";
    public static final java.lang.String POSTAG = "postag";
    public static final String POSCASE = "poscase";
    public static final String PRECEDING = "preceding";
    public static final String OLDAKKADIAN = "oldakk";
    public static final String NEWAKKADIAN ="newakk";
    public static final String CHINESE="cn";
    public static final java.lang.String WORD = "word";
    public static final String FREQ = "freq";
    public static final String CHARS = "chars";
    public static final String TRANSLIT = "translit";
    public static final java.lang.String ENTRY = "entry";
    public static final String KEY = "key";
    public static final java.lang.String NODE = "node";
    public static final java.lang.String CANDIDATES = "candidate";
    public static final String RIGHTACCVAR = "rightAccessorVariety";
    public static final String LEFTACCVAR ="leftAccessorVariety";
    public static final String NUMBEROFWORDS = "numberOfWords";
    public static final String NUMBEROFCHARS = "numberOfChars";
    public static final String NUMBEROFWORDTRANSLITS = "numberOfWordTranslits";
    public static final String NUMBEROFCHARTRANSLITS = "numberOfCharTranslits";
    public static final String DICTENTRIES="dictentries";
    public static final java.lang.String STOPCHAR = "stopchar";
    public static final java.lang.String STOPCHARS = "stopchars";
    public static final String MEZL="MesZL";
    public static final String GOTTSTEIN="got";
    public static final String EPOCH="epoch";
    public static final String DIALECT="dialect";
    public static final String ID="id";
    public static final String UTF8CODEPOINT="utf8codepoint";
    public static final String UTF8NAME="utf8name";
    public static final String ISNUMBERCHAR = "isNumberChar";
    public static final String AVGWORDLENGTH = "avgwordlength";
    private final String value;

    private Tags(String value){
        this.value=value;
    }

    @Override
    public String getShortname() {
        return null;
    }

    public final String getValue(){
        return this.value;
    }

    @Override
    public final String toString() {
        return this.value;
    }
}

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

package com.github.situx.postagger.dict.dicthandler.latin;

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.latin.EngChar;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by timo on 17.06.14.
 */
public class EngDictHandler extends LatinDictHandler {
    /**The dictionary.*/
    protected Map<String,EngChar> dictionary;
    /**The list of characters.*/
    protected Map<String,EngChar> dictmap;

    /**
     * Constructor for this class.
     * @param stopchars the list of stopchars to consider
     */
    public EngDictHandler(List<String> stopchars){
        super(stopchars, CharTypes.ENGLISH,new POSTagger(new TreeMap<String, Color>(),CharTypes.ENGLISH));
    }


    @Override
    public void addFollowingWord(final String word, final String following) {

    }

    @Override
    public void addWordFromDictImport(final LangChar word, final CharTypes charType) {

    }

    @Override
    public String reformatToASCIITranscription(final String transcription) {
        return null;
    }

    @Override
    public String reformatToUnicodeTranscription(final String transcription) {
        return null;
    }

    @Override
    public void importMappingFromXML(final String filepath) throws ParserConfigurationException, SAXException, IOException {

    }

    public LangChar matchChar(final String word,CharTypes chartype){
        if(chartype==CharTypes.TRANSLITCHAR){
            return this.translitToChar(word);
        }else{
            return this.matchChar(word);
        }
    }

    /**
     * Translates a transliteration char to its cuneiform dependant.
     * @param translit the transliteration
     * @return The cuneiform character as String
     */
    public LangChar translitToChar(final String translit){
        return this.matchChar(this.translitToCharMap.get(translit));
    }
}

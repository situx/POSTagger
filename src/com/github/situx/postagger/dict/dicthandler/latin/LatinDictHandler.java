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

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.latin.LatinChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.pos.POSTags;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by timo on 17.06.14.
 */
public abstract class LatinDictHandler extends DictHandling {
    /**The dictionary.*/
    protected Map<String,LatinChar> dictionary;
    /**The signlist to use.*/
    protected Map<String,LatinChar> dictmap;

    /**
     * Constructor for this class.
     * @param stopchars the stopchars
     * @param chartype the chartype
     * @param posTagger the postagger
     */
    public LatinDictHandler(final List<String> stopchars, final CharTypes chartype, final POSTagger posTagger){
        super(stopchars,chartype,posTagger);
    }

    @Override
    public void addChar(final LangChar character) {
        LatinChar cnChar=(LatinChar)character;
        if(!this.dictmap.containsKey(character.getCharacter())){
            this.dictmap.put(cnChar.getCharacter(),cnChar);
        }
        for(Transliteration trans:cnChar.getTransliterationSet()){
            if(!this.translitToCharMap.containsKey(trans.getTransliteration())){
                this.translitToCharMap.put(trans.getTransliteration(),cnChar.getCharacter());
            }
        }
    }

    @Override
    public void addFollowingWord(final String word, final String following, final String preceding) {

    }

    @Override
    public void addTranscriptNonCunei(final String transcription, final LangChar word) {

    }

    @Override
    public void addWord(final LangChar word2,final CharTypes charType) {
        LatinChar word=(LatinChar)word2;
        this.dictionary.put(word.getCharacter(),word);
    }

    @Override
    public void calculateRelativeCharOccurances(final Double charsInCorpus) {

    }

    @Override
    public void calculateRelativeWordOccurances(final Double wordsInCorpus) {

    }

    @Override
    public void calculateRightLeftAccessorVariety() {
        for(LatinChar curchar:this.dictmap.values()){
            curchar.calculateLeftAccessorVariety();
            curchar.calculateRightAccessorVariety();
        }
        for(LatinChar curchar:this.dictionary.values()){
            curchar.calculateLeftAccessorVariety();
            curchar.calculateRightAccessorVariety();
        }
    }

    @Override
    public void exportToXML(final String dictpath, final String reversedictpath, final String mappath,final String ngrampath,Boolean statistics) throws XMLStreamException, IOException {

    }

    @Override
    public Set<String> getCandidatesForChar(final String charactersequence) {
        return null;
    }

    public Map<String, ? extends LangChar> getDictMap() {
        return dictmap;
    }

    @Override
    public String getDictTranslation(final LangChar word, final TranslationMethod translationMethod, final Locale locale) {
        return super.getDictTranslation(word, translationMethod, locale);
    }

    @Override
    public String getDictTransliteration(final LangChar tempword, final TransliterationMethod transliterationMethod) {
        return null;
    }

    public Map<String, ? extends LangChar> getDictionary() {
        return dictionary;
    }

    @Override
    public Map<Double, Set<String>> getFreqCandidatesForChar(final String charactersequence) {
        return null;
    }

    @Override
    public String getNoDictTranslation(final String word, final TranslationMethod translationMethod, final Locale locale) {
        return null;
    }

    @Override
    public String getNoDictTransliteration(final String word, final TransliterationMethod transliterationMethod) {
        return null;
    }

    @Override
    public void importDictFromXML(final String filepath) throws ParserConfigurationException, SAXException, IOException {

    }

    @Override
    public void importNGramsFromXML(final String s) throws ParserConfigurationException, SAXException, IOException {

    }

    @Override
    public void importReverseDictFromXML(final String filepath) throws ParserConfigurationException, SAXException, IOException {

    }

    @Override
    public boolean isFollowingWord(final LangChar word, final String following) {
        return false;
    }

    @Override
    public LangChar matchChar(final String translit) {
        return this.matchChar(translit);
    }

    @Override
    public LangChar matchReverseWord(final String word) {
        return null;
    }

    @Override
    public LangChar matchWord(final String word) {
        return this.matchWord(word);
    }

    @Override
    public LangChar matchWordByTranscription(final String word,final Boolean noncunei) {
        if(this.transcriptToWordDict.containsKey(word)){
            return this.dictionary.get(transcriptToWordDict.get(word));
        }
        return null;
    }

    @Override
    public LangChar matchWordByTransliteration(final String word) {
        return null;
    }

    @Override
    public LangChar matchWordByPOSandTransliteration(final String word,final POSTags postag) {
        return null;
    }

    @Override
    public void parseDictFile(final File file) throws IOException, ParserConfigurationException, SAXException {

    }

}

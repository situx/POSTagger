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

package com.github.situx.postagger.dict.corpusimport.latin;

import com.github.situx.postagger.dict.chars.latin.EngChar;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TestMethod;
import com.github.situx.postagger.dict.corpusimport.CorpusHandlerAPI;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

/**
 * Created by timo on 24.06.14.
 * English Corpus Handler.
 */
public class EngCorpusHandler extends CorpusHandlerAPI {
    /**
     * Constructor for this class.
     * @param stopchars  stopchars to consider
     */
    public EngCorpusHandler(List<String> stopchars){
        super(stopchars);
    }

    @Override
    public void addTranslations(final String file, final TestMethod testMethod1) throws ParserConfigurationException, SAXException, IOException {

    }

    @Override
    public String cleanWordString(String word, final boolean reformat,boolean lowercase) {
        EngChar engchar=new EngChar(word);
        if(word.contains("to ")){
            engchar.setIsVerb(true);
            word=word.replace("to ","");
        }
        word.replace(")","");
        return word;
    }

    @Override
    public String corpusToReformatted(final String text) {
        return null;
    }

    @Override
    public DictHandling dictImport(final String corpus, final TestMethod testMethod, final CharTypes sourcelang, final Boolean map, final Boolean dict, final Boolean reverse, final Boolean ngram) throws IOException, SAXException, ParserConfigurationException {
        return null;
    }

    @Override
    public void enrichExistingCorpus(final String filepath, final DictHandling dicthandler) throws IOException {

    }

    @Override
    public DictHandling generateCorpusDictionaryFromFile(final List<String> filepath, final String signpath, final String filename,final boolean wholecorpus, final boolean corpusstr,final TestMethod testMethod) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        return null;
    }

    @Override
    public POSTagger getPOSTagger(Boolean newPosTagger) {
        return null;
    }

    @Override
    public DictHandling getUtilDictHandler() {
        return null;
    }

    @Override
    public void textPercentageSplit(final Double perc, final Double startline,final Boolean random, final String corpusfile, final CharTypes charTypes) {

    }

    @Override
    public String transliterationToText(final String text, final Integer duplicator, final DictHandling dicthandler,final Boolean countmatches,final Boolean segmented) {
        return null;
    }
}

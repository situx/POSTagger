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

package com.github.situx.postagger.dict.corpusimport.cuneiform;

import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.dicthandler.cuneiform.EgyptDictHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TestMethod;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by timo on 22.03.17 .
 */
public class EgyptCorpusHandler extends CuneiCorpusHandler {
    /**
     * Constructor for this class.
     *
     * @param stopchars the stopchars to consider
     */
    public EgyptCorpusHandler(List<String> stopchars) {
        super(stopchars);
    }

    @Override
    public void addTranslations(String file, TestMethod testMethod1) throws ParserConfigurationException, SAXException, IOException {

    }

    @Override
    public String corpusToReformatted(String text) {
        return null;
    }

    @Override
    public DictHandling dictImport(String corpus, TestMethod testMethod, CharTypes sourcelang, Boolean map, Boolean dict, Boolean reverse, Boolean ngram) throws IOException, SAXException, ParserConfigurationException {
        return null;
    }

    @Override
    public void enrichExistingCorpus(String filepath, DictHandling dicthandler) throws IOException {

    }

    @Override
    public DictHandling generateCorpusDictionaryFromFile(List<String> filepath, String signpath, String filename, boolean wholecorpus, boolean corpusstr, TestMethod testMethod) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        return null;
    }

    @Override
    public POSTagger getPOSTagger(Boolean newPosTagger) {
        return null;
    }

    @Override
    public DictHandling getUtilDictHandler() {
        if(this.utilDictHandler==null){
            this.utilDictHandler=new EgyptDictHandler(new LinkedList<>());
            try {
                this.utilDictHandler.importMappingFromXML("dict/egy_map.xml");
                this.utilDictHandler.importDictFromXML("dict/egy_dict.xml");
                System.out.println("GetUtilDictHandler");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        return this.utilDictHandler;
    }

    @Override
    public void textPercentageSplit(Double perc, Double startline, Boolean random, String corpusfile, CharTypes charTypes) throws IOException {

    }

    @Override
    public String transliterationToText(String text, Integer duplicator, DictHandling dicthandler, Boolean countmisses, Boolean segmented) {
        return null;
    }
}

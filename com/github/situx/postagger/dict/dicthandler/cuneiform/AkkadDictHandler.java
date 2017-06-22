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

package com.github.situx.postagger.dict.dicthandler.cuneiform;

import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.importhandler.cuneiform.AkkadianImportHandler;
import com.github.situx.postagger.dict.importhandler.cuneiform.CuneiSignImportHandler;
import com.github.situx.postagger.dict.pos.cuneiform.AkkadPOSTagger;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.github.situx.postagger.util.enums.util.Files;
import com.github.situx.postagger.util.enums.util.Options;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class AkkadDictHandler extends CuneiDictHandler {

    /**
     * Dictionary handler for the Akkadian language.
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XMLStreamException
     */
	public AkkadDictHandler(List<String> stopchars) {
        super(stopchars, CharTypes.AKKADIAN,new AkkadPOSTagger());
        this.chartype=CharTypes.AKKADIAN;
    }

    /**
     * Dictionary handler for the Akkadian language.
     * @param dict the dictionary file to parse
     * @param mapping the mapping file to parse
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XMLStreamException
     */
    public AkkadDictHandler(final String dict,final String mapping) throws IOException, SAXException, ParserConfigurationException, XMLStreamException {
        this(CharTypes.AKKADIAN.getStopchars());
        this.importMappingFromXML(mapping);
        this.importDictFromXML(dict);
    }

    /**
     * Test import method.
     * @param args
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XMLStreamException
     */
    public static void main(final String[] args) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        AkkadDictHandler akkad=new AkkadDictHandler(Files.DICTDIR.toString()+ Tags.AKKADIAN.toString()+ Files.DICTSUFFIX.toString(), Files.DICTDIR.toString()+Tags.AKKADIAN.toString()+ Files.MAPSUFFIX.toString());
        //System.out.println(akkad.translitToCharMap);
        //System.out.println(akkad.translitToWordDict);
        //System.out.println(akkad.dictionary);
        //System.out.println(akkad.getCandidatesForChar("𒀭"));
        //System.out.println(akkad.matchChar("𒀭"));
        System.out.println("Size: "+akkad.dictmap.size());
        System.out.println("Size: "+akkad.dictionary.size());
    }

    public AkkadPOSTagger getPosTagger() {
        return (AkkadPOSTagger)postagger;
    }

    @Override
    public void importDictFromXML(final String filepath) throws ParserConfigurationException, SAXException, IOException {
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        System.out.println("AkkadDictHandler with creation of newWordAkkadian!!!!!!!!!!!!!!!!!!!!!");
        SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        AkkadianImportHandler imp=new AkkadianImportHandler(Options.FILLDICTIONARY,this,this.dictionary,this.translitToWordDict,this.transcriptToWordDict,this.logographmap,CharTypes.AKKADIAN);
        parser.parse(filepath,imp);
        parser=SAXParserFactory.newInstance().newSAXParser();
        imp=new AkkadianImportHandler(Options.FILLDICTIONARY,this,this.dictionary,this.translitToWordDict,this.transcriptToWordDict,this.logographmap,CharTypes.AKKADIAN);
        parser.parse("dict/akk_dict.xml",imp);
        System.out.println("AmountWordsInCorpus: "+imp.amountOfWordsInCorpus+" - Length: "+imp.lengthOfWordsInCorpus);
        /*imp=new AkkadianImportHandler(Options.FILLDICTIONARY,this,this.dictionary,this.translitToWordDict,this.transcriptToWordDict,this.logographmap,CharTypes.AKKADIAN);
        parser.parse("newwordakkadian2.xml",imp);
        System.out.println("AmountWordsInCorpus: "+imp.amountOfWordsInCorpus+" - Length: "+imp.lengthOfWordsInCorpus);
        this.amountOfWordsInCorpus=imp.amountOfWordsInCorpus;
        this.lengthOfWordsInCorpus=imp.lengthOfWordsInCorpus;
        *///System.out.println("============================================================");
        /*for(CuneiChar word:this.dictionary.values()){
            ((AkkadPOSTagger)this.postagger).checkForNounAdj(word);
        } */
       // System.out.println("============================================================");
        //this.calculateRelativeWordOccurances(this.amountOfWordsInCorpus);
        //this.calculateRelativeCharOccurances(this.amountOfWordsInCorpus);
    }

    @Override
    public void importMappingFromXML(final String filepath) throws ParserConfigurationException, SAXException, IOException {
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        AkkadianImportHandler imp=new AkkadianImportHandler(Options.FILLMAP,this,this.dictmap,this.translitToCharMap,this.transcriptToWordDict,this.logographmap,CharTypes.AKKADIAN);
        parser.parse(in,imp);
        parser.reset();

    }

    @Override
    public void importReverseDictFromXML(final String filepath) throws ParserConfigurationException, SAXException, IOException {
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        AkkadianImportHandler imp=new AkkadianImportHandler(Options.REVERSEDICT,this,this.reversedictionary,this.reverseTranslitToWordDict,this.reverseTranscriptToWordDict,this.logographmap,CharTypes.AKKADIAN);
        parser.parse(in,imp);
        parser.reset();
    }

    @Override
    public void parseDictFile(final File file) throws IOException, ParserConfigurationException, SAXException {
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(file);
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        CuneiSignImportHandler imp=new CuneiSignImportHandler(this.dictmap,this.dictionary,this.translitToCharMap,this.translitToWordDict,this.transcriptToWordDict, CharTypes.AKKADIAN);
        parser.parse(file, imp);

    }

    @Override
    public String translitWordToCunei(final CuneiChar word) {
        return null;
    }


}

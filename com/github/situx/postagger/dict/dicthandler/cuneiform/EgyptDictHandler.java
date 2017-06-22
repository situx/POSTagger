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

import com.github.situx.postagger.dict.importhandler.cuneiform.AkkadianImportHandler;
import com.github.situx.postagger.dict.importhandler.cuneiform.CuneiSignImportHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.util.enums.util.Options;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by timo on 22.03.17 .
 */
public class EgyptDictHandler extends CuneiDictHandler {
    /**
     * Constructor for this abstract class.
     *
     * @param stopchars
     */
    public EgyptDictHandler(List<String> stopchars){
        super(stopchars, CharTypes.EGYPTIANCHAR,new POSTagger(new TreeMap<>(),CharTypes.EGYPTIANCHAR));
    }

    @Override
    public void importDictFromXML(String filepath) throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        parser.parse(filepath,new AkkadianImportHandler(Options.FILLDICTIONARY,this,this.dictionary,this.translitToWordDict,this.transcriptToWordDict,this.logographmap, CharTypes.EGYPTIANCHAR));
    }

    @Override
    public void importMappingFromXML(String filepath) throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        parser.parse(filepath,new AkkadianImportHandler(Options.FILLMAP,this,this.dictmap,this.translitToCharMap,this.transcriptToWordDict,this.logographmap,CharTypes.EGYPTIANCHAR));
    }

    @Override
    public void importReverseDictFromXML(final String filepath) throws ParserConfigurationException, SAXException, IOException {
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        AkkadianImportHandler imp=new AkkadianImportHandler(Options.REVERSEDICT,this,this.reversedictionary,this.reverseTranslitToWordDict,this.reverseTranscriptToWordDict,this.logographmap,CharTypes.EGYPTIANCHAR);
        parser.parse(in,imp);
        parser.reset();
    }

    @Override
    public void parseDictFile(File file) throws IOException, ParserConfigurationException, SAXException {
        SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(file);
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF16.toString());
        parser.parse(file, new CuneiSignImportHandler(this.dictmap,this.dictionary,this.translitToCharMap,this.translitToWordDict,this.transcriptToWordDict, CharTypes.EGYPTIANCHAR));
    }

    @Override
    public String translitWordToCunei(final CuneiChar word) {
        return null;
    }
}
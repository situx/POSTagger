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

package com.github.situx.postagger.dict.importhandler.cuneiform;

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.util.enums.util.Tags;
import org.xml.sax.Attributes;
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
 * Created by timo on 10/29/14.
 */
public class SumDictImportHandler extends CuneiImportHandler {

    private DictHandling dicthandler;
    private Locale origlocale,destlocale;
    private String origvalue,destvalue;
    private LangChar tempchar;
    private Map<String,List<String>>  result;

    private static SumDictImportHandler instance;

    public SumDictImportHandler(){
        this.result=new TreeMap<>();

    }

    public Map<String, List<String>> getResult() {
        return result;
    }

    private void parseIt() throws ParserConfigurationException, SAXException, IOException {
        SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File("dict/sum.xml"));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        //ImportHandler imp=new ImportHandler(Options.FILLDICTIONARY,this.dictionary,this.translitToWordDict, CharTypes.AKKADIAN);
        parser.parse(in,this);
    }

    public void setResult(final Map<String, List<String>> result) {
        this.result = result;
    }

    public static SumDictImportHandler getInstance() throws IOException, SAXException, ParserConfigurationException {
            if(instance==null){
                instance=new SumDictImportHandler();
                instance.parseIt();
            }
            return instance;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        switch (qName){
            case Tags.TRANSLATION: this.origvalue=attributes.getValue("origvalue");
                String origatf=this.reformatToASCIITranscription(origvalue);
                //System.out.println("Meaning: "+tempchar.getMeaning());
                //System.out.println("Destvalue: "+attributes.getValue("destvalue"));
                this.destvalue=this.reformatToASCIITranscription(attributes.getValue("destvalue"));
                if(!this.destvalue.isEmpty()){
                    if(!this.result.containsKey(origatf.toUpperCase())){
                        this.result.put(origatf.toUpperCase(),new LinkedList<String>());
                    }
                    this.result.get(origatf.toUpperCase()).add(destvalue);

                }

                break;
        }

    }
}

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
import com.github.situx.postagger.dict.corpusimport.cuneiform.AkkadCorpusHandler;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TestMethod;
import com.github.situx.postagger.util.enums.util.Tags;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.util.enums.util.Files;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by timo on 23.06.14.
 */
public class TranslationImportHandler extends CuneiImportHandler {

    private DictHandling dicthandler;
    private Locale origlocale,destlocale;
    private String origvalue,destvalue;
    private LangChar tempchar;

    public TranslationImportHandler(final DictHandling dicthandler){
         this.dicthandler=dicthandler;
    }

    public static void main(String[] args) throws ParserConfigurationException, XMLStreamException, SAXException, IOException {
        AkkadCorpusHandler corpushandler=new AkkadCorpusHandler(CharTypes.AKKADIAN.getStopchars());
        DictHandling dictHandler=corpushandler.generateTestTrainSets("corpusimport.txt","",0.,0., TestMethod.FOREIGNTEXT,CharTypes.AKKADIAN);
        dictHandler.parseDictFile(new File(Files.AKKADXML.toString()));
        TranslationImportHandler importHandler=new TranslationImportHandler(dictHandler);
        SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File("newwords.xml"));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        //ImportHandler imp=new ImportHandler(Options.FILLDICTIONARY,this.dictionary,this.translitToWordDict, CharTypes.AKKADIAN);
        parser.parse(in,importHandler);
        /*dictHandler.exportToXML(Files.DICTDIR.toString()+ Tags.AKKADIAN.toString()+Files.DICTSUFFIX.toString(),
                Files.DICTDIR.toString()+ Tags.AKKADIAN.toString()+Tags.REVERSE+Files.DICTSUFFIX.toString(),
                Files.DICTDIR.toString()+Tags.AKKADIAN.toString()+Files.MAPSUFFIX.toString());*/
    }

    public void prepareTranslations(String translationString,Locale locale){
        String[] translations=translationString.split(",|;|:|\\/");
        for(String trans:translations){
            trans=trans.replaceAll("to ","").replaceAll("the","").replaceAll("a ","")
                    .replaceAll("\\(?\\)","").replaceAll("\\(?","").replaceAll("[0-9]\\)","")
                    .replaceAll("\\?","").replaceAll("[0-9]", "")
                    .replaceAll("\\[","").replaceAll("]","").replaceAll("\\...","");
            trans=trans.trim();
            if(!trans.isEmpty()) {
                this.tempchar.addTranslation(trans, locale);
                //System.out.println("Add Translation: " + trans);
            }
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        switch (qName){
            case Tags.TRANSLATION: this.origvalue=attributes.getValue("origvalue");
                                   String origatf=this.reformatToASCIITranscription(origvalue);
                                    //System.out.println("Origvalue: "+origvalue);
                                    //System.out.println("Origvalue Reformatted: "+this.reformatToATF(this.origvalue));
                                    //System.out.println("Origvalue in Cunei: "+dicthandler.translitToChar(this.transcriptToTranslit(this.reformatToATF(this.origvalue),dicthandler)));
                                    this.tempchar=dicthandler.matchWordByTranscription(origatf,false);
                                    if(tempchar==null){
                                        this.tempchar=this.dicthandler.translitToChar(TranscriptionMethods.transcriptToTranslit(origatf, dicthandler));
                                        this.tempchar.addTransliteration(new Transliteration(origatf, TranscriptionMethods.translitTotranscript(origatf)));
                                        //System.out.println("Add Word: "+tempchar);
                                        //System.out.println()
                                        //dicthandler.addWord(this.tempchar);
                                    }else{
                                        System.out.println("Already existing word: "+tempchar.getTransliterationSet().iterator().next());
                                    }
                                    this.destvalue=this.reformatToASCIITranscription(attributes.getValue("destvalue"));
                                    this.destlocale=new Locale(attributes.getValue("destlocale"));
                                    this.prepareTranslations(destvalue,destlocale);
                                    break;
        }

    }


}

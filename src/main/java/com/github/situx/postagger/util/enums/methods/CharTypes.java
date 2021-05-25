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

package com.github.situx.postagger.util.enums.methods;

import com.github.situx.postagger.dict.corpusimport.cuneiform.*;
import com.github.situx.postagger.dict.corpusimport.CorpusHandlerAPI;
import com.github.situx.postagger.dict.corpusimport.asian.CNCorpusHandler;
import com.github.situx.postagger.dict.corpusimport.asian.JapaneseCorpusHandler;
import com.github.situx.postagger.dict.corpusimport.cuneiform.*;
import com.github.situx.postagger.dict.corpusimport.latin.EngCorpusHandler;
import com.github.situx.postagger.dict.corpusimport.latin.GerCorpusHandler;
import com.github.situx.postagger.util.enums.pos.PersonNumberCases;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Enum containing CharTypes of the given languages.
 */
public enum CharTypes implements MethodEnum {
    /**Akkadian Char.*/
    AKKADIAN("Akkadian Char","akk",2,"-", Arrays.asList(new String[]{System.lineSeparator()}),Arrays.asList(new String[]{"-"}),new Integer[]{0X12000,0X12399},"[A-z0-9, -]+","\uD808\uDC2D",new AkkadCorpusHandler(Arrays.asList(new String[]{System.lineSeparator()})),"akkadian"),
    /**Chinese Char.*/
    CHINESE("Chinese Char", Locale.CHINESE.getLanguage(),1,"-", Arrays.asList(new String[]{"。","，","？","！"}),Arrays.asList(new String[]{"-"}),new Integer[0],".*","你好吗",new CNCorpusHandler(Arrays.asList(new String[]{"。","，","？","！"})),"chinese"),
    /**Elamite Char.*/
    ELAMITE("Elamite Char","ela",2,"-", Arrays.asList(new String[]{System.lineSeparator()}),Arrays.asList(new String[]{"-"}),new Integer[0],".*","",new ElamiteCorpusHandler(new LinkedList<>()),"elamite"),
    /**Cuneiform Char.*/
    CUNEICHAR("Cuneiform Char",Locale.ENGLISH.toString(),2,"-", Arrays.asList(new String[]{System.lineSeparator()}),Arrays.asList(new String[]{"-"}),new Integer[0],".*","",null,"cuneiform"),
    /**German Char.*/
    ENGLISH("English Char", Locale.ENGLISH.toString(),1,".", Arrays.asList(new String[]{" ",".",",","!","?",";",":","\\(","\\)","\\[","\\]","\\{","\\}",System.lineSeparator()}),Arrays.asList(new String[]{"-"}),new Integer[0],".*","",new EngCorpusHandler(Arrays.asList(new String[]{" ",".",",","!","?",";",":","\\(","\\)","\\[","\\]","\\{","\\}",System.lineSeparator()})),"english"),
    /**Hittite Char.*/
    HITTITE("Hittite Char","hit",2,"-", Arrays.asList(new String[]{System.lineSeparator()}),Arrays.asList(new String[]{"-"}),new Integer[]{0x12000,0x12399},"[A-z0-9, -]+","\uD808\uDC2D",new HittiteCorpusHandler(Arrays.asList(new String[]{System.lineSeparator()})),"hittite"),
    /**German Char.*/
    GERMAN("German Char",Locale.GERMAN.toString(),1,".", Arrays.asList(new String[]{" ",".",",","!","?",";",":","\\(","\\)","\\[","\\]","\\{","\\}",System.lineSeparator()}),Arrays.asList(new String[]{"-"}),new Integer[0],".*","Franz jagt im komplett verwahrlosten Taxi durch Bayern",new GerCorpusHandler(Arrays.asList(new String[]{" ",".",",","!","?",";",":","\\(","\\)","\\[","\\]","\\{","\\}",System.lineSeparator()})),"german"),
    /**Japanese Char.*/
    JAPANESE("Japanese Char",Locale.JAPANESE.toString(),1,"(?<=\\\\p{Nd})", Arrays.asList(new String[]{"。","，","？","！"}),Arrays.asList(new String[]{"-"}),new Integer[0],".*","ありがとございます",new JapaneseCorpusHandler(Arrays.asList(new String[]{"。","，","？","！"})),"japanese"),
    /**Language Char.*/
    LANGCHAR("Language Char",Locale.ENGLISH.toString(),1,".", Arrays.asList(new String[0]),Arrays.asList(new String[]{"-"}),new Integer[0],".*","",null,"lang"),
    /**Latin Char.*/
    LATIN("Latin Char",Locale.ENGLISH.toString(),1,".", Arrays.asList(new String[]{}),Arrays.asList(new String[]{"-"}),new Integer[0],".*","ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz",null,"latin"),
    /**Luwian Cuneiform Char.*/
    LUWIANCN("Luwian Cuneiform Char","luwcn",2,"-", Arrays.asList(new String[]{System.lineSeparator()}),Arrays.asList(new String[]{"-"}),new Integer[0],".*","",new LuwianCuneiCorpusHandler(new LinkedList<>()),"luwiancn"),
    /**Sumerian Char.*/
    SUMERIAN("Sumerian Char","sux",2,"-", Arrays.asList(new String[]{System.lineSeparator()}),Arrays.asList(new String[]{"-"}),new Integer[]{0x12000,0x12399},"[A-z0-9, -]+","\uD808\uDC2D",new SumerianCorpusHandler(Arrays.asList(new String[]{System.lineSeparator()})),"sumerian"),
    TRANSLITCHAR("Transliteration","tra",1,"-", Arrays.asList(System.lineSeparator()),Arrays.asList(new String[]{"-"}),new Integer[0],".*","",null,"translit"),
    ASIANCHAR("Asian Char",Locale.CHINESE.toString(),1,"(?<=\\p{Nd})", Arrays.asList("。","，","？","！"),Arrays.asList(new String[]{"-"}),new Integer[0],".*","",null,"asian"),   EGYPTIANCHAR("Egyptian Char","egy",1,"(?<=\\p{Nd})", Arrays.asList("。","，","？","！"),Arrays.asList("-",":","*"),new Integer[0],".*","",new EgyptCorpusHandler(Arrays.asList(System.lineSeparator())),"egyptian");

    public String getPreviewString() {
        return previewString;
    }


    private final String previewString;

    private final List<String> stopchars;

    private final List<String> separators;

    private Boolean initialized=false;
    private Integer char_length;
    private Integer[] unicode_ranges;
    private String legalTranslitCharsRegex;
    private CorpusHandlerAPI dicthandler;
    /**String value (name of the method).*/
    private String  locale,splitcriterion,smallstr;
    /**String value (name of the method).*/
    private String value;

    private Map<PersonNumberCases,String> personNumberCasesStringMap;

    public String getLegalTranslitCharsRegex() {
        return legalTranslitCharsRegex;
    }

    public void setLegalTranslitCharsRegex(final String legalTranslitCharsRegex) {
        this.legalTranslitCharsRegex = legalTranslitCharsRegex;
    }

    /**
     * Checks if a given character is in the unicode range of the given language.
     * @param codepoint the codepoint to check for
     * @return true if it is in the unicode range, false if it is not
     */
    public Boolean charIsInUnicodeRange(Integer codepoint){
        for(int i=0;i<unicode_ranges.length-1;i+=2){
            if(codepoint<=unicode_ranges[i+1] && codepoint>=unicode_ranges[i]){
                return true;
            }
        }
        return false;
    }

    public Boolean isFontDisplayable(Font font){
        for(int i=0;i<unicode_ranges.length-1;i+=2){
            if(font.canDisplay(unicode_ranges[i+1]) || font.canDisplay(unicode_ranges[i]))
                System.out.println("IsFontDisp: "+font.getName()+" "+unicode_ranges[i+1]+" "+unicode_ranges[i]+" "+font.canDisplay(unicode_ranges[i+1])+" "+font.canDisplay(unicode_ranges[i]));
            if(!font.canDisplay(unicode_ranges[i+1]) && !font.canDisplay(unicode_ranges[i])){
                return false;
            }
        }
        return true;
    }

    public String getSmallstr() {
        return smallstr;
    }

    /**Constructor using a description parameter.*/
    private CharTypes(String value,String locale,Integer char_length,String splitcriterion,final List<String> stopchars,final List<String> separators,final Integer[] unicode_ranges,final String legalTranslitChars,final String previewString,final CorpusHandlerAPI dicthandler,String smallstr){
        this.value=value;
        this.locale=locale;
        this.unicode_ranges=unicode_ranges;
        this.char_length=char_length;
        this.splitcriterion=splitcriterion;
        this.stopchars=stopchars;
        this.separators=separators;
        this.previewString=previewString;
        this.legalTranslitCharsRegex =legalTranslitChars;
        this.dicthandler=dicthandler;
        this.smallstr=smallstr;

    }

   /* private void initialize(CharTypes charTypes) {
        if(charTypes==this && !this.initialized){
            switch (charTypes){
                case AKKADIAN:
                    this.dicthandler=;
                    break;
                case HITTITE:
                    this.dicthandler=new HittiteCorpusHandler(this.stopchars);
                    break;
                case SUMERIAN:
                    this.dicthandler=new SumerianCorpusHandler(this.stopchars);
                    break;
                case ASIANCHAR:
                    this.dicthandler=new CNCorpusHandler(this.stopchars);
                    break;
                case CHINESE:
                    this.dicthandler=new CNCorpusHandler(this.stopchars);
                    break;
                case GERMAN:
                    this.dicthandler=new GerCorpusHandler(this.stopchars);
                    break;
                case ENGLISH:
                    this.dicthandler=new EngCorpusHandler(this.stopchars);
                    break;
            }

        }
        this.initialized=true;
    } */

    public Integer[] getUnicode_ranges() {
        return unicode_ranges;
    }

    public void setUnicode_ranges(final Integer[] unicode_ranges) {
        this.unicode_ranges = unicode_ranges;
    }

    public Integer getChar_length() {
        return char_length;
    }

    public CorpusHandlerAPI getCorpusHandlerAPI() {
        //System.out.println(this.getShortname()+" - ");
        //System.out.println("Is Initialized? "+this.initialized.toString());

        //if(!this.initialized)
        //    this.initialize(this);
        return dicthandler;
    }

    public String getLocale() {
        return locale;
    }

    @Override
    public String getShortname() {
        return null;
    }

    public String getSplitcriterion() {
        return splitcriterion;
    }

    public List<String> getStopchars() {
        return stopchars;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

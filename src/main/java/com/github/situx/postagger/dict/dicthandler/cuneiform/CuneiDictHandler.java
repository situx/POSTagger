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

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.PositionableChar;
import com.github.situx.postagger.dict.chars.cuneiform.AkkadChar;
import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.chars.cuneiform.HittiteChar;
import com.github.situx.postagger.dict.chars.cuneiform.SumerianChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.utils.StopChar;
import com.github.situx.postagger.main.gui.ime.jquery.tree.builder.CuneiPaintTreeBuilder;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.pos.POSTags;
import com.github.situx.postagger.util.enums.util.Tags;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 26.10.13
 * Time: 14:36
 * DictHandler fo cuneiform dictionaries.
 */
public abstract class CuneiDictHandler extends DictHandling {
    public Map<String,CuneiChar> dictionary;
    protected Map<String,CuneiChar> dictmap;
    protected Map<String,CuneiChar> logographmap;
    protected Map<String,CuneiChar> reversedictionary;
    /**Map for words with transcript without cuneiform match so far.*/
    protected Map<String,CuneiChar> transcriptToNonCunei;
    /**Saves the last processed word or character.*/
    CuneiChar lastword=null,lastlastword=null;
    /**
     * Constructor for this abstract class.
     */
    public CuneiDictHandler(List<String> stopchars,CharTypes  charType,POSTagger posTagger){
        super(stopchars,charType,posTagger);
        this.dictionary=new TreeMap<>();
        this.reversedictionary=new TreeMap<>();
        this.dictmap=new TreeMap<>();
        this.logographmap=new TreeMap<>(Collections.reverseOrder());
        this.transcriptToNonCunei=new TreeMap<>();
        this.amountOfWordsInCorpus=0.;
        this.paintTree=new CuneiPaintTreeBuilder(this);
    }

    @Override
    public void addChar(final LangChar character) {
        CuneiChar cnChar=(CuneiChar)character;
        if(!this.dictmap.containsKey(character.getCharacter())){
            this.dictmap.put(cnChar.getCharacter(),cnChar);
        }
        cnChar.getTransliterationSet().stream().filter(trans -> !this.translitToCharMap.containsKey(trans.getTransliteration())).forEach(trans -> {
            this.translitToCharMap.put(trans.getTransliteration(), cnChar.getCharacter());
        });
    }

    /**
     * Adds a word or char following the current word or char.
     * @param word the current word or char
     * @param following the following word or char
     */
    public void addFollowingChar(final String word,final String following){
        if(!" ".equals(word) && this.dictmap.get(word)!=null)
            this.dictmap.get(word).addFollowingWord(following);
    }

    /**
     * Adds a word or char following the current word or char.
     * @param word the current word or char
     * @param following the following word or char
     */
    public void addFollowingWord(final String word,final String following){
        if(!" ".equals(word) && this.dictionary.get(word)!=null)
            this.dictionary.get(word).addFollowingWord(following);
    }

    /**
     * Adds a word or char following the current word or char.
     * @param word the current word or char
     * @param following the following word or char
     */
    public void addFollowingWord(final String word,final String following,final String preceding){
        if(!" ".equals(word) && this.dictionary.get(word)!=null)
            this.dictionary.get(word).addFollowingWord(following,preceding);
    }

    @Override
    public void addTranscriptNonCunei(final String transcription, final LangChar word) {
        this.transcriptToNonCunei.put(transcription,(CuneiChar)word);
    }

    @Override
    public void addWordFromDictImport(final LangChar word, final CharTypes charType) {
        this.amountOfWordsInCorpus++;
        this.lengthOfWordsInCorpus+=word.length();
        this.amountOfWordTranslitsInCorpus++;
        if(((CuneiChar)word).getLogograph())
            this.logographmap.put(word.getCharacter(),(CuneiChar)word);
        if(this.dictionary.containsKey(word.getCharacter())) {
            this.dictionary.get(word.getCharacter()).addOccurance();
        }else{
            this.dictionary.put(word.getCharacter(),(CuneiChar)word);
        }
    }

    @Override
    public void addWord(final LangChar word,CharTypes charType){
        CuneiChar word2=(CuneiChar)word;
        if(((CuneiChar)word).getLogograph())
            this.logographmap.put(word.getCharacter(),(CuneiChar)word);
        final Integer charlength=word2.getCharlength();
        if(word2.getPaintInformation()!=null){
            this.paintTree.addWordComboToTree(word2.getPaintInformation(),word2.getCharacter(),word2.getOccurances().intValue(),word2.getMeaning(),word2.getTranslations().keySet().iterator().next(),((word2.getPostags()!=null && !word2.getPostags().isEmpty())?word2.getPostags().toString():""),((word2.getConceptURI()!=null && !word2.getConceptURI().isEmpty())?word2.getConceptURI():""), StringUtils.countMatches("a",word2.getPaintInformation())+StringUtils.countMatches("b",word2.getPaintInformation())+StringUtils.countMatches("c",word2.getPaintInformation())+StringUtils.countMatches("d",word2.getPaintInformation()));
        }
        /*AkkadChar reverseword=new AkkadChar(new StringBuffer(word.getCharacter()).reverse().toString());
        reverseword.setDeterminative(word2.getDeterminative());
        reverseword.setLogograph(word2.getLogograph());
        reverseword.setPhonogram(word2.getPhonogram());
        reverseword.setSumerogram(word2.getSumerogram());
        reverseword.setIsNumberChar(word2.getIsNumberChar());
        reverseword.setCharacter(new StringBuffer(word2.getCharacter()).reverse().toString());

        for(Transliteration trans:word2.getTransliterationSet()){
            String reversed=this.reverseTransliteration(trans.getTransliteration(), CharTypes.CUNEICHAR.getSplitcriterion());
            reverseword.addTransliteration(new Transliteration(reversed, TranscriptionMethods.translitTotranscript(reversed)));
            this.reverseTranslitToWordDict.put(reversed,reverseword.getCharacter());
            this.reverseTranscriptToWordDict.put(TranscriptionMethods.translitTotranscript(reversed),reverseword.getCharacter());
        }
        this.reversedictionary.put(reverseword.getCharacter(),reverseword);
        */
        CuneiChar templangchar=null,oldtemplangchar=null;
        CuneiChar tempchar;
        String[] tempcharsplit;
        this.amountOfWordsInCorpus++;
        this.lengthOfWordsInCorpus+=word2.length();
        this.amountOfWordTranslitsInCorpus++;
        if(this.dictionary.containsKey(word.getCharacter())){
            this.dictionary.get(word.getCharacter()).addOccurance();
            //System.out.println("Occurance of "+word2.getCharacter()+": "+this.dictionary.get(word2.getCharacter()).getOccurances());
            String cuneiword=word.getCharacter();
            if(cuneiword.length()>charlength){
                tempchar= (CuneiChar)translitToChar(cuneiword.substring(0, charlength));
                if(tempchar!=null){
                    templangchar=tempchar;
                    templangchar.addBeginOccurance();
                    if(this.lastword!=null) {
                        if(lastlastword!=null){
                            this.lastword.addFollowingWord(templangchar.getCharacter(),lastlastword.getCharacter(), true);
                        }else{
                            this.lastword.addFollowingWord(templangchar.getCharacter(), true);
                        }
                        templangchar.addPrecedingWord(this.lastword.getCharacter());
                    }
                    this.continuationFollowsBoundary++;
                }
                lastlastword=lastword;
                this.lastword=templangchar;
                for(int i=charlength;i<cuneiword.length()-charlength;i+=charlength){

                    tempchar= (CuneiChar)translitToChar(cuneiword.substring(i, i + charlength));

                    if(tempchar!=null){
                        templangchar=tempchar;
                        templangchar.addMiddleOccurance(i/charlength);
                        if(this.lastword!=null) {
                            if(lastlastword!=null){
                                this.lastword.addFollowingWord(templangchar.getCharacter(),lastlastword.getCharacter(), true);
                            }else{
                                this.lastword.addFollowingWord(templangchar.getCharacter(), true);
                            }
                            templangchar.addPrecedingWord(this.lastword.getCharacter());
                        }
                        this.continuationFollowsContinuation++;
                    }
                    lastlastword=lastword;
                    this.lastword=templangchar;
                }
                tempchar= (CuneiChar)translitToChar(cuneiword.substring(cuneiword.length() - charlength));

                if(tempchar!=null){
                    templangchar=tempchar;
                    templangchar.addEndOccurance(templangchar.length()-charlength);
                    if(this.lastword!=null) {
                        if(lastlastword!=null){
                            this.lastword.addFollowingWord(templangchar.getCharacter(),lastlastword.getCharacter(), true);
                        }else{
                            this.lastword.addFollowingWord(templangchar.getCharacter(), true);
                        }
                        templangchar.addPrecedingWord(this.lastword.getCharacter());
                    }
                    lastlastword=lastword;
                    this.lastword=templangchar;
                    this.boundariesFollowContinuations++;
                }
            }else if(cuneiword.length()==charlength){
                tempchar= (CuneiChar)translitToChar(cuneiword.substring(0, charlength));

                if(tempchar!=null){
                    templangchar=tempchar;
                    templangchar.addSingleOccurance();
                    if(this.lastword!=null) {
                        if(lastlastword!=null){
                            this.lastword.addFollowingWord(templangchar.getCharacter(),lastlastword.getCharacter(), true);
                        }else{
                            this.lastword.addFollowingWord(templangchar.getCharacter(), true);
                        }
                        templangchar.addPrecedingWord(this.lastword.getCharacter());
                    }
                    this.boundariesFollowBoundaries++;
                }
            }
        }else{
            this.dictionary.put(word.getCharacter(), word2);
            //this.dictionary.put(reverseword.getCharacter(),reverseword);
            //System.out.println("ADD WORD: " + word2.getCharacter());
            //System.out.println("ADD REVERSEWORD: " + reverseword);
            for(Transliteration trans:word2.getTransliterationSet()){
                this.translitToWordDict.put(trans.getTransliteration(), word2.getCharacter());
                this.transcriptToWordDict.put(TranscriptionMethods.translitTotranscript(trans.getTransliteration()), word2.getCharacter());
            }

            tempcharsplit=word.getTransliterationSet().iterator().next().toString().split("-");
            tempchar=(CuneiChar)word;
            if(tempchar.length().equals(charlength)){
                if(!this.dictmap.containsKey(tempchar.getCharacter())){
                    this.addChar(word2);
                }
                tempchar.setSingleCharacter(true);
                for(Transliteration translit:word2.getTransliterationSet()){
                    if(tempchar.getTransliterationSet().contains(translit)){
                        CuneiChar testchar=this.dictmap.get(tempchar.getCharacter());
                        for(Transliteration trans:testchar.getTransliterationSet()){
                            if(trans.toString().equals(translit.getTransliterationString())){
                               trans.setSingleTransliteration(true,0);
                               this.dictmap.get(tempchar.getCharacter()).addSingleOccurance();
                               this.boundariesFollowBoundaries++;
                            }
                        }
                        testchar.addTransliteration(translit);
                    }
                }
            }else if(tempchar.length()>charlength){
                if(!this.dictmap.containsKey(tempchar.getCharacter().substring(0,charlength)) || this.dictmap.get(tempchar.getCharacter().substring(0,charlength)).getTransliterationSet().isEmpty()){
                    CuneiChar addchar=this.createCorrectCharType(tempchar.getCharacter().substring(0,charlength),charType);
                    String[] split=word2.getTransliterationSet().iterator().next().getTransliteration().split(CharTypes.AKKADIAN.getSplitcriterion());

                    /*System.out.print("Split: ");
                    ArffHandler.arrayToStr(split);*/
                    if(split.length>1)
                        addchar.addTransliteration(new Transliteration(split[0],split[0]));
                    addchar.setIsNumberChar(word2.getIsNumberChar());
                    this.addChar(addchar);
                    templangchar=addchar;
                }else{
                    templangchar=this.dictmap.get(tempchar.getCharacter().substring(0,charlength));
                }
                //templangchar=(AkkadChar)this.dictmap.get(tempchar.getCharacter().substring(0,charlength));
                if(templangchar!=null){
                    templangchar.setBeginningCharacter(true);
                    for(Transliteration translit:templangchar.getTransliterationSet()){
                        if(templangchar.getTransliterationSet().contains(translit)){
                            for(Transliteration trans:templangchar.getTransliterationSet()){
                                if(trans.toString().equals(translit.getTransliterationString())){
                                    trans.setBeginTransliteration(true,0);
                                    templangchar.addBeginOccurance();
                                    if(lastword!=null){
                                        if(lastlastword!=null){
                                            this.lastword.addFollowingWord(templangchar.getCharacter(),lastlastword.getCharacter(), false);
                                        }else{
                                            this.lastword.addFollowingWord(templangchar.getCharacter(), false);
                                        }
                                        templangchar.addPrecedingWord(this.lastword.getCharacter());
                                    }
                                    this.boundariesFollowContinuations++;
                                }
                            }
                            templangchar.addTransliteration(translit);
                            lastlastword=lastword;
                            this.lastword=templangchar;
                        }
                    }

                }
                for(int i=charlength;i<tempchar.length()-charlength;i+=charlength){
                    if(!this.dictmap.containsKey(tempchar.getCharacter().substring(i,i+charlength)) || this.dictmap.get(tempchar.getCharacter().substring(i,i+charlength)).getTransliterationSet().isEmpty()){
                        CuneiChar addchar=this.createCorrectCharType(tempchar.getCharacter().substring(i,i+charlength),charType);
                        String[] split=word2.getTransliterationSet().iterator().next().getTransliteration().split(CharTypes.AKKADIAN.getSplitcriterion());
                        /*System.out.print("Split: ");
                        ArffHandler.arrayToStr(split);*/
                        if(split.length>1)
                            addchar.addTransliteration(new Transliteration(split[(i/charlength)-1],split[(i/charlength)-1]));
                        this.addChar(addchar);
                        //System.out.println("Add: "+tempchar.substring(i,i+charlength));

                    }
                    templangchar=this.dictmap.get(tempchar.getCharacter().substring(i,i+charlength));
                    if(templangchar!=null){
                        templangchar.setMiddleCharacter(true);
                        for(Transliteration translit:templangchar.getTransliterationSet()){
                            if(templangchar.getTransliterationSet().contains(translit)){
                                for(Transliteration trans:templangchar.getTransliterationSet()){
                                    if(trans.toString().equals(translit.getTransliterationString())){
                                        trans.setMiddleTransliteration(true,i);
                                        this.continuationFollowsContinuation++;
                                        if(lastword!=null) {
                                            if(lastlastword!=null){
                                                this.lastword.addFollowingWord(templangchar.getCharacter(),lastlastword.getCharacter(), false);
                                            }else{
                                                this.lastword.addFollowingWord(templangchar.getCharacter(), false);
                                            }
                                            templangchar.addPrecedingWord(this.lastword.getCharacter());
                                        }
                                    }
                                }
                                templangchar.addTransliteration(translit);
                                lastlastword=lastword;
                                this.lastword=templangchar;
                            }
                        }
                    }

                }
                if(!this.dictmap.containsKey(tempchar.getCharacter().substring(tempchar.length()-charlength)) || this.dictmap.get(tempchar.getCharacter().substring(tempchar.length()-charlength)).getTransliterationSet().isEmpty()){
                    CuneiChar addchar=this.createCorrectCharType(tempchar.getCharacter().substring(tempchar.length()-charlength),charType);
                    String[] split=word2.getTransliterationSet().iterator().next().getTransliteration().split(CharTypes.AKKADIAN.getSplitcriterion());
                    /*System.out.print("Split: ");
                    ArffHandler.arrayToStr(split);*/
                    if(split.length>1)
                        addchar.addTransliteration(new Transliteration(split[split.length-1],split[split.length-1]));
                    this.addChar(addchar);
                }
                templangchar=this.dictmap.get(tempchar.getCharacter().substring(tempchar.length()-charlength));
                if(templangchar!=null){
                    templangchar.setEndingCharacter(true);
                    for(Transliteration translit:templangchar.getTransliterationSet()){
                        if(templangchar.getTransliterationSet().contains(translit)){
                            for(Transliteration trans:templangchar.getTransliterationSet()){
                                if(trans.toString().equals(translit.getTransliterationString())){
                                    trans.setEndTransliteration(true,tempchar.length()-charlength);
                                    this.boundariesFollowContinuations++;
                                    if(lastword!=null) {
                                        if(lastlastword!=null){
                                            this.lastword.addFollowingWord(templangchar.getCharacter(),lastlastword.getCharacter(), true);
                                        }else{
                                            this.lastword.addFollowingWord(templangchar.getCharacter(), true);
                                        }
                                        templangchar.addPrecedingWord(this.lastword.getCharacter());
                                    }
                                }
                            }
                            templangchar.addTransliteration(translit);
                            lastlastword=lastword;
                            this.lastword=templangchar;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void calculateRelativeCharOccurances(final Double charsInCorpus){
        for(CuneiChar cunei:this.dictmap.values()){
            cunei.setRelativeOccurance(charsInCorpus);
            cunei.getTransliterationSet().forEach(transliteration -> transliteration.setRelativeOccurance(CuneiDictHandler.this.amountOfCharTranslitsInCorpus));
        }
    }

    @Override
    public void calculateRelativeWordOccurances(final Double wordsInCorpus) {
        this.dictionary.values().forEach(cunei ->  {
            cunei.setRelativeOccurance(wordsInCorpus);
            cunei.getTransliterationSet().forEach(transliteration -> transliteration.setRelativeOccurance(CuneiDictHandler.this.amountOfWordTranslitsInCorpus));
    });
    }

    @Override
    public void calculateRightLeftAccessorVariety() {
        this.dictmap.values().forEach(curchar -> {
            curchar.calculateLeftAccessorVariety();
            curchar.calculateRightAccessorVariety();
        });
        this.dictionary.values().forEach(curchar -> {
            curchar.calculateLeftAccessorVariety();
            curchar.calculateRightAccessorVariety();
        });
        this.reversedictionary.values().forEach(curchar -> {
            curchar.calculateLeftAccessorVariety();
            curchar.calculateRightAccessorVariety();
        });
        this.stopchars.values().forEach(curchar -> {
            curchar.calculateLeftPunctuationVariety(CharTypes.CUNEICHAR);
            curchar.calculateRightPunctuationVariety(CharTypes.CUNEICHAR);
        });
    }

    protected Boolean checkForNumberChar(final String cuneiform){
        String currentchar="";
        for(int i=0;i<cuneiform.length()-chartype.getChar_length();i+=chartype.getChar_length()){
            //System.out.println("Currentchar: "+currentchar);
           // System.out.println("Currentchar.equals(substring)?: "+currentchar+" - "+cuneiform.substring(i,i+chartype.getChar_length())+" - "+currentchar.equals(cuneiform.substring(i,i+chartype.getChar_length())));
            if(currentchar.isEmpty()){
                currentchar=cuneiform.substring(i,i+chartype.getChar_length());
                //System.out.println("Currentchar in dict?: "+this.dictmap.get(currentchar)+" IsNumberChar? "+this.dictmap.get(currentchar).getIsNumberChar());
                if((this.dictmap.get(currentchar)==null || !this.dictmap.get(currentchar).getIsNumberChar()) && (this.dictionary.get(currentchar)==null || !this.dictionary.get(currentchar).getIsNumberChar())){
                    return false;
                }
            }else if(!currentchar.equals(cuneiform.substring(i,i+chartype.getChar_length()))){
                return false;
            }
        }
        return true;
    }

    public CuneiChar createCorrectCharType(final String character,CharTypes chartype){
        CuneiChar result;
        switch (chartype){
            case AKKADIAN:result=new AkkadChar(character);break;
            case HITTITE: result=new HittiteChar(character);break;
            case SUMERIAN:result=new SumerianChar(character);break;
            default: result=new AkkadChar(character);break;
        }
        return result;
    }

    @Override
    public void exportToXML(final String dictpath,final String reversedictpath, final String mappath,final String ngrampath,Boolean withStatistics)throws XMLStreamException, IOException {
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        output.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        OutputStreamWriter outwriter=new OutputStreamWriter(new FileOutputStream(dictpath), Tags.UTF8.toString());
        XMLStreamWriter writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(outwriter));
        writer.writeStartDocument(Tags.UTF8.toString(),Tags.XMLVERSION.toString());
        //writer.writeCharacters("\n");
        writer.writeStartElement(Tags.DICTENTRIES);
        writer.writeAttribute(Tags.NUMBEROFWORDS,this.amountOfWordsInCorpus.toString());
        writer.writeAttribute(Tags.NUMBEROFCHARS,this.amountOfCharTranslitsInCorpus.toString());
        writer.writeAttribute(Tags.NUMBEROFWORDTRANSLITS,this.amountOfWordTranslitsInCorpus.toString());
        writer.writeAttribute(Tags.NUMBEROFCHARTRANSLITS,this.amountOfCharTranslitsInCorpus.toString());
        writer.writeAttribute(Tags.AVGWORDLENGTH,this.averageWordLength.toString());
        writer.writeCharacters(System.lineSeparator());
        for(CuneiChar akkadchar:this.dictionary.values()){
            writer.writeCharacters(akkadchar.toXML(Tags.DICTENTRY,withStatistics)+System.lineSeparator());
        }
        writer.writeStartElement(Tags.STOPCHARS);
        for(StopChar stopchar:this.stopchars.values()){
            writer.writeCharacters(stopchar.toXML());
        }
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        OutputStreamWriter outwriter3=new OutputStreamWriter(new FileOutputStream(reversedictpath), Tags.UTF8.toString());
        XMLStreamWriter writer3 = new IndentingXMLStreamWriter(output.createXMLStreamWriter(outwriter3));
        writer3.writeStartDocument(Tags.UTF8.toString(), Tags.XMLVERSION.toString());
        writer3.writeStartElement(Tags.DICTENTRIES);
        System.out.println("Words: "+this.amountOfWordsInCorpus.toString());
        writer3.writeAttribute(Tags.NUMBEROFWORDS, this.amountOfWordsInCorpus.toString());
        writer3.writeAttribute(Tags.NUMBEROFCHARS,this.amountOfCharTranslitsInCorpus.toString());
        writer3.writeAttribute(Tags.NUMBEROFWORDTRANSLITS,this.amountOfWordTranslitsInCorpus.toString());
        writer3.writeAttribute(Tags.NUMBEROFCHARTRANSLITS,this.amountOfCharTranslitsInCorpus.toString());
        writer3.writeAttribute(Tags.AVGWORDLENGTH,this.averageWordLength.toString());
        writer3.writeCharacters(System.lineSeparator());
        for(CuneiChar akkadchar:this.reversedictionary.values()){
            writer3.writeCharacters(akkadchar.toXML(Tags.DICTENTRY,withStatistics)+System.lineSeparator());
        }
        writer3.writeStartElement(Tags.STOPCHARS);
        for(StopChar stopchar:this.stopchars.values()){
            writer3.writeCharacters(stopchar.toXML());
        }
        writer3.writeEndElement();
        writer3.writeEndElement();
        writer3.writeEndDocument();
        writer3.close();
        XMLOutputFactory output2 = XMLOutputFactory.newInstance();
        output2.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        OutputStreamWriter outwriter2=new OutputStreamWriter(new FileOutputStream(mappath), Tags.UTF8.toString());
        XMLStreamWriter writer2 = new IndentingXMLStreamWriter(output.createXMLStreamWriter(outwriter2));
        writer2.writeStartDocument(Tags.UTF8.toString(),Tags.XMLVERSION.toString());
        writer2.writeCharacters(System.lineSeparator());
        writer2.writeStartElement(Tags.MAPENTRIES.toString());
        writer2.writeAttribute(Tags.NUMBEROFWORDS,this.amountOfWordsInCorpus.toString());
        writer2.writeAttribute(Tags.NUMBEROFCHARS,this.amountOfCharTranslitsInCorpus.toString());
        writer2.writeAttribute(Tags.NUMBEROFWORDTRANSLITS,this.amountOfWordTranslitsInCorpus.toString());
        writer2.writeAttribute(Tags.NUMBEROFCHARTRANSLITS,this.amountOfCharTranslitsInCorpus.toString());
        writer2.writeCharacters(System.lineSeparator());
        for(CuneiChar akkadchar:this.dictmap.values()){
            writer2.writeCharacters(akkadchar.toXML(Tags.MAPENTRY,withStatistics)+System.lineSeparator());
        }
        writer2.writeStartElement(Tags.STOPCHARS);
        for(StopChar stopchar:this.stopchars.values()){
            writer2.writeCharacters(stopchar.toXML());
        }
        writer2.writeEndElement();
        writer2.writeEndElement();
        writer2.writeEndDocument();
        writer2.close();
        XMLOutputFactory output4 = XMLOutputFactory.newInstance();
        output4.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        OutputStreamWriter outwriter4=new OutputStreamWriter(new FileOutputStream(ngrampath), Tags.UTF8.toString());
        XMLStreamWriter writer4 = new IndentingXMLStreamWriter(output.createXMLStreamWriter(outwriter4));
        writer4.writeStartDocument(Tags.UTF8.toString(),Tags.XMLVERSION.toString());
        writer4.writeCharacters(System.lineSeparator());
        writer4.writeStartElement(Tags.NGRAMS.toString());
        writer4.writeCharacters(System.lineSeparator());
        writer4.writeCharacters(this.ngams.toXML());
        writer4.writeEndElement();
        writer4.writeEndDocument();
        writer4.close();
    }

    @Override
    public List<String> getAlternativeWritings() {
        return null;
    }

    /**
     * Gets the boundary-follows-boundary score.
     * @return the score as double
     */
    @Override
    public Double getBoundariesFollowBoundaries(){
        return this.boundariesFollowBoundaries;
    }

    /**
     * Gets the boundary-follows-continuation score.
     * @return the score as double
     */
    @Override
    public Double getBoundariesFollowContinuations(){
        return this.boundariesFollowContinuations;
    }

    @Override
    public Set<String> getCandidatesForChar(final String character){
        return this.dictionary.keySet().stream().filter(key -> key.startsWith(character)).collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Gets the continuation-follows-boundary score.
     * @return the score as double
     */
    public Double getContinuationFollowsBoundary(){
        return this.continuationFollowsBoundary;
    }

    /**
     * Gets the continuation-follows-continuation score.
     * @return the score as double
     */
    public Double getContinuationFollowsContinuation(){
        return this.continuationFollowsContinuation;
    }

    public Map<String, ? extends LangChar> getDictMap() {
        return dictmap;
    }

    /**
     * Matches a given string with the dictionary
     * @param word2 the word to match
     * @param transliterationMethod the transliteration method to use
     * @return
     */
    @Override
    public String getDictTransliteration(final LangChar word2,final TransliterationMethod transliterationMethod){
        CuneiChar word=(CuneiChar)word2;
        if(word==null){
            return null;
        }
        switch (transliterationMethod){
            case FIRST:return word.getTransliterationSet().isEmpty()?"":word.getTransliterationSet().iterator().next().getTransliteration();
            case PROB: return word.getMostProbableWordTransliteration().getTransliteration();
            case RANDOM:return word.getRandomWordTransliteration().getTransliteration();
            default: return null;
        }
}

    public Map<String, ? extends LangChar> getDictionary() {
        return dictionary;
    }

    @Override
    public Map<Double, Set<String>> getFreqCandidatesForChar(final String character) {
        Map<Double,Set<String>> candidates=new TreeMap<>();
        this.dictionary.keySet().stream().filter(key -> key.startsWith(character)).forEach(key -> {
            if (candidates.get(this.dictionary.get(key).getRelativeOccurance()) == null) {
                candidates.put(this.dictionary.get(key).getRelativeOccurance(), new TreeSet<>());
            }
            candidates.get(this.dictionary.get(key).getRelativeOccurance()).add(key);
        });
        return candidates;
    }

    public String reformatToASCIITranscription(final String transcription) {
        String result=transcription;
        int i=0,length=0;
        if(transcription.isEmpty()){
            return "";
        }
        result=transcription.replace("!", "").replace("#","").replaceAll("\\*","");
        result=result.replace("š","sz").replace("Š","SZ").replace("ṣ","s,").replace("ş","s,").replace("Ṣ","S,")
                .replace("ḫ","h").replace("Ḫ","H").replace("ĝ","g").replace("ṭ","t,").replace("ţ","t,").replace("Ṭ","T,");
        result=result.replace("â","a").replace("ā","a").replace("á","a2").replace("à","a3")
                .replace("ê","e").replace("ē","e").replace("é","e2").replace("è","e3")
                .replace("î","i").replace("ī","i").replace("í","i2").replace("ì","i3")
                .replace("û","u").replace("ū", "u").replace("ú","u2").replace("ù","u3");
        result=result.replace("₀", "0").replace("₁","1").replace("₂","2").replace("₃","3")
                .replace("₄","4").replace("₅","5").replace("₆","6").replace("₇","7").replace("₈","8").replace("₉","9");
        length=result.length();
        while(!(length<2) && Character.isDigit(result.toCharArray()[length-1])){
            length-=1;
        }
        /*for(i=0;i<length;i++){
            if(Character.isDigit(result.charAt(i))){
                result.replace(""+result.charAt(i),"");
                result+=result.charAt(i);
            }
        }*/
        return result;
        //return result.toLowerCase();
    }

    public static String reformatToASCIITranscription2(final String transcription) {
        String result=transcription;
        int i=0,length=0;
        if(transcription.isEmpty()){
            return "";
        }
        result=transcription.replace("!", "").replace(".","-").replace("#","").replaceAll("\\*","");
        result=result.replace("š","sz").replace("Š","SZ").replace("ṣ","s,").replace("Ṣ","S,")
                .replace("ḫ","h").replace("Ḫ","H").replace("ĝ","g").replace("ṭ","t,").replace("Ṭ","T,");
        result=result.replace("â","a").replace("ā","a").replace("ê","e").replace("ē","e").replace("î","i").replace("ī","i").replace("û","u").replace("ū", "u");
        result=replaceDiacriticInTranscription("á","a","2",result);
        result=replaceDiacriticInTranscription("à","a","3",result);
        result=replaceDiacriticInTranscription("Á","A","2",result);
        result=replaceDiacriticInTranscription("À","A","3",result);
        result=replaceDiacriticInTranscription("é","e","2",result);
        result=replaceDiacriticInTranscription("è","e","3",result);
        result=replaceDiacriticInTranscription("É","E","2",result);
        result=replaceDiacriticInTranscription("È","E","3",result);
        result=replaceDiacriticInTranscription("í","i","2",result);
        result=replaceDiacriticInTranscription("ì","i","3",result);
        result=replaceDiacriticInTranscription("Í","I","2",result);
        result=replaceDiacriticInTranscription("Ì","I","3",result);
        result=replaceDiacriticInTranscription("ú","u","2",result);
        result=replaceDiacriticInTranscription("ù","u","3",result);
        result=replaceDiacriticInTranscription("Ú","U","2",result);
        result=replaceDiacriticInTranscription("Ù","U","3",result);
        result=result.replace("₀", "0").replace("₁","1").replace("₂","2").replace("₃","3")
                .replace("₄","4").replace("₅","5").replace("₆","6").replace("₇","7").replace("₈","8").replace("₉","9").replace("`","");
        length=result.length();
        while(!(length<2) && Character.isDigit(result.toCharArray()[length-1])){
            length-=1;
        }
        for(i=0;i<length;i++){
            if(Character.isDigit(result.charAt(i))){
                result.replace(""+result.charAt(i),"");
                //result+=result.charAt(i);
            }
        }
        return result;
    }

    private static String replaceDiacriticInTranscription(final String diacritic,final String replaceChar,final String replacementNumber,final String transcription){
        //System.out.println("In: "+transcription);
        if(!transcription.contains(diacritic)){
            //System.out.println("Out: "+transcription);
            return transcription;
        }
        Integer diacritindex=transcription.indexOf(diacritic);
        Integer newNumPos=transcription.indexOf('-',diacritindex);
        if(newNumPos==-1 || diacritindex==transcription.length()-1){
            //System.out.println("Out: "+transcription.replace(diacritic,replaceChar)+replacementNumber);
            return transcription.replace(diacritic,replaceChar)+replacementNumber;
        }else{
            //System.out.println("Out: "+transcription.replace(diacritic,replaceChar).substring(0,newNumPos)+replacementNumber+transcription.replace(diacritic,replaceChar).substring(newNumPos));
            return transcription.replace(diacritic,replaceChar).substring(0,newNumPos)+replacementNumber+transcription.replace(diacritic,replaceChar).substring(newNumPos);
        }

    }

    public String reformatToUnicodeTranscription(final String transcription) {
        //System.out.println("ReformatToUnicode: "+transcription);
        String result=transcription;
        int i=0,length=0;
        result=transcription.replace("!","").replace("#","");
        result=result.replace("sz","š").replace("SZ","Š").replace("s,","ṣ").replace("S,","Ṣ").
                replace("h","ḫ").replace("H","Ḫ").replace("ĝ","g").replace("t,", "ṭ").replace("T,", "Ṭ");
        result=result.replace("a:","ā").replace("a2","á").replace("a3","à")
                .replace("e:","ē").replace("e2","é").replace("e3","è")
                .replace("i:", "ī").replace("i2,", "í").replace("i3", "ì")
                .replace("u:", "ū").replace("u2", "ú").replace("u3,","ù");
        result=result.replace("0", "₀").replace("1", "₁").replace("2", "₂").replace("3", "₃")
                .replace("4", "₄").replace("5", "₅").replace("6", "₆").replace("7", "₇").replace("8", "₈").replace("9","₉");
        length=result.length();
        while(!result.isEmpty() && Character.isDigit(result.toCharArray()[length-1])){
            length-=1;
        }
        for(i=0;i<length;i++){
            if(Character.isDigit(result.charAt(i))){
                result.replace(""+result.charAt(i),"");
                result+=result.charAt(i);
            }
        }
        //System.out.println("Reformatted: "+result.toLowerCase());
        return result;
        //return result.toLowerCase();
    }

    @Override
    public String getNoDictTranslation(final String word, final TranslationMethod translationMethod, final Locale locale) {
        return word;
    }

    public Map<String,CuneiChar> getLogographs(){
        return this.logographmap;
    }

    /**
     * Generates a transliteration that cannot be found in the dictionary by using probabilistic models.
     * @param word  the word to match
     * @param transliterationMethod the transliteration method to choose
     * @return the transliteration as String
     */
    @Override
    public String getNoDictTransliteration(final String word,final TransliterationMethod transliterationMethod){
          int charlength=this.chartype.getChar_length();
          System.out.println("NoDict Word: "+word);
          StringBuilder result=new StringBuilder();
          PositionableChar curchar;
          result.append("[");
          if(word.length()<charlength){
              return " ";
          }else if(word.length()==charlength){
              if(dictmap.containsKey(word) && !dictmap.get(word).getTransliterationSet().isEmpty()) {
                  switch (transliterationMethod) {
                      case PROB:
                          return "[" + this.dictmap.get(word).getMostProbableBeginTransliteration(0).getTransliteration() + "]";
                      case RANDOM:
                      case FIRST:
                      default:
                          return "[" + this.dictmap.get(word).getFirstSingleTransliteration().getTransliteration() + "]";
                  }
              }else{
                  return "[]";
              }
          }
          switch(transliterationMethod){
              case PROB:
                  for(int i=0;i<word.length()-1;i+=charlength){
                      curchar= this.dictmap.get(word.substring(i,i+charlength));
                      if(i==0 && curchar!=null){
                          result.append(curchar.getMostProbableBeginTransliteration(i));
                          result.append("-");
                      }else if(i<word.length()-charlength && curchar!=null){
                          result.append(curchar.getMostProbableMiddleTransliteration(i));
                          result.append("-");
                      }else if(i==word.length()-charlength && curchar!=null){
                          result.append(curchar.getMostProbableEndTransliteration(i));
                          result.append("] ");
                      }
                  }
                  break;
              case RANDOM:for(int i=0;i<word.length()-1;i+=charlength){
                  curchar= this.dictmap.get(word.substring(i,i+charlength));
                  if(i==0 && curchar!=null){
                      result.append(curchar.getRandomWordTransliteration());
                      result.append("-");
                  }else if(i<word.length()-charlength && curchar!=null){
                      result.append(curchar.getRandomWordTransliteration());
                      result.append("-");
                  }else if(i==word.length()-charlength && curchar!=null){
                      result.append(curchar.getRandomWordTransliteration());
                      result.append("] ");
                  }
              }break;
              case FIRST:
              default:for(int i=0;i<word.length()-1;i+=charlength){
                  curchar= this.dictmap.get(word.substring(i, i + charlength));
                  if(i==0 && curchar!=null){
                      result.append(curchar.getFirstBeginningTransliteration());
                      result.append("-");
                  }else if(i<word.length()-charlength && curchar!=null){
                      result.append(curchar.getFirstMiddleTransliteration());
                      result.append("-");
                  }else if(i==word.length()-charlength && curchar!=null){
                      result.append(curchar.getFirstEndTransliteration());
                      result.append("] ");
                  }
              }
          }
          if(result.toString().equals("[")){
             return "";
          }
          return result.toString();
    }

    /**
     * Gets reverse candidates for the given char.
     * @param character the character to investigate
     * @return the set of candidates
     */
    public Set<String> getReverseCandidatesForChar(final String character){
        //System.out.println(this.reversedictionary.toString());
        return this.reversedictionary.keySet().stream().filter(key -> key.endsWith(character)).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public abstract void importMappingFromXML(final String filepath)throws ParserConfigurationException, SAXException, IOException;

    @Override
    public void importNGramsFromXML(final String s) throws ParserConfigurationException, SAXException, IOException {

    }

    @Override
    public boolean isFollowingWord(final LangChar word, final String following){
        return this.dictmap.get(word.getCharacter()).getFollowingWords().containsKey(following);
    }

    @Override
    public LangChar matchChar(final String cuneiform){
        if(this.dictmap!=null && cuneiform!=null && this.dictmap.containsKey(cuneiform)){
            return this.dictmap.get(cuneiform);
        }
        return null;
    }
    @Override
    public LangChar matchChar(final String word,CharTypes chartype){
        if(chartype==CharTypes.TRANSLITCHAR){
            return this.translitToChar(word);
        }else{
            return this.matchChar(word);
        }
    }

    @Override
    public LangChar matchReverseWord(final String word){
        if(this.reversedictionary.get(word)!=null){
            return this.reversedictionary.get(word);
        }
        return null;
    }

    @Override
    public LangChar matchWord(final String cuneiform){
        if(this.dictionary!=null && cuneiform!=null && this.dictionary.get(cuneiform)!=null){
            return this.dictionary.get(cuneiform);
        }
        //System.out.println("MatchWordNumberChar: "+cuneiform+" - "+checkForNumberChar(cuneiform));
        if(cuneiform!=null && (cuneiform.length()/chartype.getChar_length()>1) && checkForNumberChar(cuneiform)){
            AkkadChar akkad=new AkkadChar(cuneiform);
            akkad.addTransliteration(new Transliteration((akkad.length()/chartype.getChar_length())+"("+dictmap.get(cuneiform.substring(0,chartype.getChar_length())).getTransliterationSet().iterator().next().getTransliteration()+")",(akkad.length()/chartype.getChar_length())+"("+cuneiform+")"));
            this.dictionary.put(cuneiform,akkad);
            this.translitToWordDict.put((akkad.length()/chartype.getChar_length())+"("+cuneiform+")",cuneiform);
            return new AkkadChar(cuneiform);
        }
        return null;
    }
    @Override
    public LangChar matchWordByTranscription(final String word,Boolean noncunei){
        if(this.transcriptToWordDict.containsKey(word)){
            return this.dictionary.get(this.transcriptToWordDict.get(word));
        }
        if(noncunei && this.transcriptToNonCunei.containsKey(word)){
            return this.transcriptToNonCunei.get(word);
        }
        return null;
    }
    @Override
    public LangChar matchWordByTransliteration(String word){
        if(this.translitToWordDict!=null && word!=null && this.translitToWordDict.containsKey(word)){
            return this.dictionary.get(this.translitToWordDict.get(word));
        }
        if(word!=null) {
            word = this.reformatToUnicodeTranscription(word);
            if (this.translitToWordDict.containsKey(word)) {
                return this.dictionary.get(this.translitToWordDict.get(word));
            }
        }
        return null;
    }

    @Override
    public LangChar matchWordByPOSandTransliteration(String word,POSTags postag){
        if(this.postagMap.containsKey(postag) && this.postagMap.get(postag).containsKey(word)){
            return this.postagMap.get(postag).get(word);
        }
        word=this.reformatToUnicodeTranscription(word);
        if(this.postagMap.containsKey(postag) && this.postagMap.get(postag).containsKey(word)){
            return this.postagMap.get(postag).get(word);
        }
        if(this.translitToWordDict.containsKey(word)){
            return this.dictionary.get(this.translitToWordDict.get(word));
        }
        return null;
    }

    public void morfessorExport(final String filepath) throws IOException {
        System.out.println("Morfessor Export: "+filepath);
        BufferedWriter morfessorwriter=new BufferedWriter(new FileWriter(new File(filepath)));
        for(String key:this.dictionary.keySet()){
            morfessorwriter.write(this.dictionary.get(key).getOccurances().intValue()+" "+this.dictionary.get(key).getCharacter()+"\n");
        }
        morfessorwriter.close();
    }

    /**Resets the last processed word when beginning a new line.*/
    public void newLine(){
        this.lastword=null;
    }

    /**
     * Translates a transliteration char to its cuneiform dependant.
     * @param translit the transliteration
     * @return The cuneiform character as String
     */
    @Override
    public LangChar translitToChar(String translit){
        if(!this.translitToCharMap.containsKey(translit)){
            translit=this.reformatToUnicodeTranscription(translit);
            if(this.translitToCharMap.containsKey(translit)){
                return this.dictmap.get(this.translitToCharMap.get(translit));
            }
            return null;
        }
        return this.dictmap.get(this.translitToCharMap.get(translit));
        /*System.out.println("Translit: "+translit+" "+this.translitToCharMap.toString());
        System.out.println("Dictmap: "+this.dictmap.toString());
        return this.dictmap.containsKey(this.translitToCharMap.get(translit))?:null;*/
    }

    public abstract String translitWordToCunei(CuneiChar word);

}

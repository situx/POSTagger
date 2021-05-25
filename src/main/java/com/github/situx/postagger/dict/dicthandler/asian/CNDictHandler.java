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

package com.github.situx.postagger.dict.dicthandler.asian;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.PositionableChar;
import com.github.situx.postagger.dict.chars.asian.CNChar;
import com.github.situx.postagger.dict.importhandler.asian.CNImportHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.utils.StopChar;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.util.Options;
import com.github.situx.postagger.util.enums.util.Tags;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.*;

public class CNDictHandler extends AsianDictHandler {
    /**The dictionary to use.*/
    protected Map<String,CNChar> dictionary;
    /**The signlist.*/
    protected Map<String,CNChar> dictmap;
    /**The reversdictionary.*/
    protected Map<String,CNChar> reversedictionary;
    /**The lastword and lastlastword to remember.*/
    private CNChar lastword,lastlastword;

    /**
     * Constructor for this class.
     * @param stopchars the list of stopchars to use
     */
	public CNDictHandler(List<String> stopchars){
        super(stopchars,CharTypes.CHINESE,new POSTagger(new TreeMap<String, Color>(),CharTypes.CHINESE));
		this.dictionary=new TreeMap<>();
        this.dictmap=new TreeMap<>();
        this.reversedictionary=new TreeMap<>();
		//this.parseDictFile(new File(filepath));
		
	}

    @Override
    public void addChar(final LangChar character){
        CNChar cnChar=(CNChar)character;
         /*if(cnChar.getTransliterationSet().isEmpty() || cnChar.getCharacter().equals("亮")){
             System.out.println("Add Char: "+cnChar.toString()+" "+cnChar.getTransliterationSet().toString());
         }*/
         if(!this.dictmap.containsKey(character.getCharacter())){
             this.dictmap.put(cnChar.getCharacter(),cnChar);
;
         }
        for(Transliteration trans:cnChar.getTransliterationSet()){
            if(!this.translitToCharMap.containsKey(trans.getTransliteration())){
                this.translitToCharMap.put(trans.getTransliteration(),cnChar.getCharacter());
                //System.out.println("Add Char Translit: "+trans.toString());
            }
;
        }
    }

    /**
     * Adds a following word to the current word.
     * @param word the current word
     * @param following the following word
     */
    public void addFollowingWord(final String word, final String following) {
        this.dictionary.get(word).addFollowingWord(following);
    }

    @Override
    public void addFollowingWord(final String word, final String following, final String preceding) {

    }

    @Override
    public void addStopWord(final StopChar stopChar){
        if(stopChar!=null && !this.stopchars.containsKey(stopChar.getStopchar())){
            this.stopchars.put(stopChar.getStopchar(),stopChar);
        }
    }

    @Override
    public void addTranscriptNonCunei(final String transcription, final LangChar word) {

    }

    @Override
    public void addWord(final LangChar word,final CharTypes charType){
        CNChar word2=(CNChar)word;
        final int charlength= word2.getCharlength();
        CNChar reverseword=new CNChar(word.getCharacter());
        reverseword.setCharacter(new StringBuffer(word2.getCharacter()).reverse().toString());
        for(Transliteration trans:word2.getTransliterationSet()){
            reverseword.addTransliteration(new Transliteration(this.reverseTransliteration(trans.getTransliteration(),CharTypes.CHINESE.getSplitcriterion()), TranscriptionMethods.translitTotranscript(this.reverseTransliteration(trans.getTransliteration(), CharTypes.CHINESE.getSplitcriterion()))));
        }
        CNChar templangchar=null,oldtemplangchar=null;
        CNChar tempchar;
        String[] tempcharsplit;
        this.amountOfWordsInCorpus++;
        this.lengthOfWordsInCorpus+=word2.length();
        this.amountOfWordTranslitsInCorpus++;
        //if(lastword!=null)
            //System.out.println("Lastword: "+this.lastword);
        if(this.dictionary.containsKey(word.getCharacter())){
            this.dictionary.get(word.getCharacter()).addOccurance();
            //System.out.println("Occurance of "+word2.getCharacter()+": "+this.dictionary.get(word2.getCharacter()).getOccurances());
            String cuneiword=word.getCharacter();
            if(lastword!=null){
                lastword.addFollowingWord(word.getCharacter(),true);
            }
            this.lastword=word2;
            if(cuneiword.length()>charlength){
                tempchar= (CNChar)translitToChar(cuneiword.substring(0, charlength));
                if(tempchar!=null){
                    templangchar=this.dictmap.get(tempchar.getCharacter());
                    templangchar.addBeginOccurance();
                    if(lastword!=null) {
                        if (lastlastword != null) {
                            this.lastword.addFollowingWord(templangchar.getCharacter(), lastlastword.getCharacter(), true);
                        } else {
                            this.lastword.addFollowingWord(templangchar.getCharacter(), true);
                        }
                        templangchar.addPrecedingWord(this.lastword.getCharacter());
                    }
                    this.continuationFollowsBoundary++;
                }
                for(int i=charlength;i<cuneiword.length()-charlength;i+=charlength){

                    tempchar= (CNChar)translitToChar(cuneiword.substring(i, i + charlength));

                    if(tempchar!=null){
                        templangchar=this.dictmap.get(tempchar.getCharacter());
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
                tempchar= (CNChar)translitToChar(cuneiword.substring(cuneiword.length() - charlength));

                if(tempchar!=null){
                    templangchar=this.dictmap.get(tempchar.getCharacter());
                    templangchar.addEndOccurance(templangchar.length()-charlength);
                    if(this.lastword!=null){
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
                tempchar= (CNChar)translitToChar(cuneiword.substring(0, charlength));

                if(tempchar!=null){
                    templangchar=this.dictmap.get(tempchar.getCharacter());
                    templangchar.addSingleOccurance();
                    if(this.lastword!=null){
                        if(lastlastword!=null){
                            this.lastword.addFollowingWord(templangchar.getCharacter(),lastlastword.getCharacter(), true);
                        }else{
                            this.lastword.addFollowingWord(templangchar.getCharacter(), true);
                        }
                        templangchar.addPrecedingWord(this.lastword.getCharacter());
                    }
                    this.boundariesFollowBoundaries++;
                }
                this.dictionary.put(word2.getCharacter(), word2);
            }
        }else{
            this.dictionary.put(word.getCharacter(), word2);
            this.reversedictionary.put(reverseword.getCharacter(),reverseword);
            if(lastword!=null){
                if(lastlastword!=null){
                    this.lastword.addFollowingWord(word2.getCharacter(),lastlastword.getCharacter(), true);
                }else{
                    this.lastword.addFollowingWord(word2.getCharacter(), true);
                }
                word2.addPrecedingWord(this.lastword.getCharacter());
            }
            lastword=word2;
            //System.out.println("ADD WORD: " + word2);
            //System.out.println("ADD REVERSEWORD: " + reverseword);
            for(Transliteration trans:word2.getTransliterationSet()){
                this.translitToWordDict.put(trans.getTransliteration(), word2.getCharacter());
                this.transcriptToWordDict.put(TranscriptionMethods.translitTotranscript(trans.getTransliteration()), word2.getCharacter());
            }
            tempchar=(CNChar)word;
            //System.out.println("Word.getCharacter(): "+tempchar.toString()+" "+tempchar.length()+" "+charlength+" "+word2.getTransliterationSet().iterator().next().getTransliteration().toString());
            if(tempchar.length()==charlength){
                if(!this.dictmap.containsKey(tempchar.getCharacter())){
                    this.addChar(word2);
                }
                tempchar.setSingleCharacter(true);
                for(Transliteration translit:word2.getTransliterationSet()){
                    if(tempchar.getTransliterationSet().contains(translit)){
                        CNChar testchar=tempchar;
                        for(Transliteration trans:testchar.getTransliterationSet()){
                            if(trans.toString().equals(translit.getTransliterationString())){
                                trans.setSingleTransliteration(true,0);
                                tempchar.addSingleOccurance();
                                this.boundariesFollowBoundaries++;
                            }
                        }
                        testchar.addTransliteration(translit);
                    }
                }
            }else if(tempchar.length()>charlength){
                if(!word2.getTransliterationSet().isEmpty() && (!this.dictmap.containsKey(tempchar.getCharacter().substring(0,charlength)) || this.dictmap.get(tempchar.getCharacter().substring(0,charlength)).getTransliterationSet().isEmpty())){
                    CNChar addchar=new CNChar(tempchar.getCharacter().substring(0,charlength));
                    String[] split=word2.getTransliterationSet().iterator().next().getTransliteration().split(CharTypes.CHINESE.getSplitcriterion());
                    //System.out.print("Split: ");
                    //ArffHandler.arrayToStr(split);
                    if(split.length>1)
                        addchar.addTransliteration(new Transliteration(split[0],split[0]));
                    this.addChar(addchar);
                    templangchar=addchar;
                }else{
                    templangchar=this.dictmap.get(tempchar.getCharacter().substring(0,charlength));
                }
                //System.out.println("Beginning Templangchar: "+templangchar.toString()+" "+templangchar.getTransliterationSet().toString());
                if(templangchar!=null){
                    templangchar.setBeginningCharacter(true);
                    for(Transliteration translit:templangchar.getTransliterationSet()){
                        //System.out.println("Beginning Current Translit: "+translit);
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
                            //System.out.println("Set Beginning Lastchar: "+templangchar.toString());
                            lastlastword=lastword;
                            this.lastword=templangchar;
                        }
                    }
                }
                for(int i=charlength;i<tempchar.length()-charlength;i+=charlength){
                    if(!word2.getTransliterationSet().isEmpty() && (!this.dictmap.containsKey(tempchar.getCharacter().substring(i,i+charlength)) || this.dictmap.get(tempchar.getCharacter().substring(i,i+charlength)).getTransliterationSet().isEmpty())){
                        CNChar addchar=new CNChar(tempchar.getCharacter().substring(i,i+charlength));
                        String[] split=word2.getTransliterationSet().iterator().next().getTransliteration().split(CharTypes.CHINESE.getSplitcriterion());
                        //System.out.print("Split: ");
                        //ArffHandler.arrayToStr(split);
                        if(split.length>1 && (i/charlength)<split.length)
                            addchar.addTransliteration(new Transliteration(split[i/charlength],split[i/charlength]));
                        this.addChar(addchar);
                        //System.out.println("Add: "+tempchar.substring(i,i+charlength));

                    }
                    //System.out.println("Get: "+this.dictmap.get(tempchar.substring(i,i+charlength)));
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
                                            if (lastlastword != null) {
                                                this.lastword.addFollowingWord(templangchar.getCharacter(), lastlastword.getCharacter(), false);
                                            } else {
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
                if(!word2.getTransliterationSet().isEmpty() && (!this.dictmap.containsKey(tempchar.getCharacter().substring(tempchar.length()-charlength)) || this.dictmap.get(tempchar.getCharacter().substring(tempchar.length()-charlength)).getTransliterationSet().isEmpty())){
                    CNChar addchar=new CNChar(tempchar.getCharacter().substring(tempchar.length()-charlength));
                    String[] split=word2.getTransliterationSet().iterator().next().getTransliteration().split(CharTypes.CHINESE.getSplitcriterion());
                    //System.out.print("Split: ");
                    //ArffHandler.arrayToStr(split);
                    if(split.length>1 && ((tempchar.length()-1)/charlength)<split.length)
                        addchar.addTransliteration(new Transliteration(split[(tempchar.length()-1)/charlength],split[(tempchar.length()-1)/charlength]));
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
                                    System.out.println(templangchar.toString());
                                    if(lastword!=null){
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
    public void addWordFromDictImport(final LangChar word, final CharTypes charType) {

    }

    @Override
    public void calculateRelativeCharOccurances(final Double charsInCorpus) {
        for(CNChar cunei:this.dictmap.values()){
            cunei.setRelativeOccurance(charsInCorpus);
            for(Transliteration translit:cunei.getTransliterationSet()){
                translit.setRelativeOccurance(this.amountOfCharTranslitsInCorpus);
            }
        }
    }

    @Override
    public void calculateRelativeWordOccurances(final Double wordsInCorpus) {
        for(CNChar cunei:this.dictionary.values()){
            cunei.setRelativeOccurance(wordsInCorpus);
            for(Transliteration translit:cunei.getTransliterationSet()){
                translit.setRelativeOccurance(this.amountOfWordTranslitsInCorpus);
            }
        }
    }

    @Override
    public void calculateRightLeftAccessorVariety() {
        for(CNChar curchar:this.dictmap.values()){
            curchar.calculateLeftAccessorVariety();
            curchar.calculateRightAccessorVariety();
        }
        for(CNChar curchar:this.dictionary.values()){
            curchar.calculateLeftAccessorVariety();
            curchar.calculateRightAccessorVariety();
        }
        for(CNChar curchar:this.reversedictionary.values()){
            curchar.calculateLeftAccessorVariety();
            curchar.calculateRightAccessorVariety();
        }
        for(StopChar curchar:this.stopchars.values()){
            curchar.calculateLeftPunctuationVariety(CharTypes.CHINESE);
            curchar.calculateRightPunctuationVariety(CharTypes.CHINESE);
        }
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
    public void exportToXML(final String dictpath,final String reversedictpath, final String mappath,final String ngrampath,Boolean statistics) throws XMLStreamException, IOException {
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        output.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        OutputStreamWriter outwriter=new OutputStreamWriter(new FileOutputStream(dictpath), Tags.UTF8.toString());
        XMLStreamWriter writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(outwriter));
        writer.writeStartDocument(Tags.UTF8.toString(),Tags.XMLVERSION.toString());
        //writer.writeCharacters("\n");
        writer.writeStartElement(Tags.DICTENTRIES.toString());
        writer.writeAttribute(Tags.NUMBEROFWORDS,this.amountOfWordsInCorpus.toString());
        writer.writeAttribute(Tags.NUMBEROFCHARS,this.amountOfCharTranslitsInCorpus.toString());
        writer.writeAttribute(Tags.NUMBEROFWORDTRANSLITS,this.amountOfWordTranslitsInCorpus.toString());
        writer.writeAttribute(Tags.NUMBEROFCHARTRANSLITS,this.amountOfCharTranslitsInCorpus.toString());
        writer.writeAttribute(Tags.AVGWORDLENGTH,this.averageWordLength.toString());
        writer.writeCharacters("\n");
        for(CNChar akkadchar:this.dictionary.values()){
            writer.writeCharacters(akkadchar.toXML(Tags.DICTENTRY,statistics)+System.lineSeparator());
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
        writer3.writeStartDocument(Tags.UTF8.toString(),Tags.XMLVERSION.toString());
        writer3.writeStartElement(Tags.DICTENTRIES.toString());
        writer3.writeAttribute(Tags.NUMBEROFWORDS,this.amountOfWordsInCorpus.toString());
        writer3.writeAttribute(Tags.NUMBEROFCHARS,this.amountOfCharTranslitsInCorpus.toString());
        writer3.writeAttribute(Tags.NUMBEROFWORDTRANSLITS,this.amountOfWordTranslitsInCorpus.toString());
        writer3.writeAttribute(Tags.NUMBEROFCHARTRANSLITS,this.amountOfCharTranslitsInCorpus.toString());
        writer3.writeAttribute(Tags.AVGWORDLENGTH,this.averageWordLength.toString());
        writer3.writeCharacters(System.lineSeparator());
        for(CNChar akkadchar:this.reversedictionary.values()){
            writer3.writeCharacters(akkadchar.toXML(Tags.DICTENTRY,statistics)+System.lineSeparator());
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
        for(CNChar akkadchar:this.dictmap.values()){
            writer2.writeCharacters(akkadchar.toXML(Tags.MAPENTRY,statistics)+System.lineSeparator());
        }
        writer2.writeStartElement(Tags.STOPCHARS);
        for(StopChar stopchar:this.stopchars.values()){
            writer2.writeCharacters(stopchar.toXML()+System.lineSeparator());
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
    public Double getAmountOfWordsInCorpus() {
        return this.amountOfWordsInCorpus;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<String> getCandidatesForChar(final String character){
        Set<String> candidates=new TreeSet<String>();
        for(String key:this.dictionary.keySet()){
            if(key.startsWith(character)){
                candidates.add(key);
            }
        }
        return candidates;
    }

    @Override
    public String getDictTransliteration(final LangChar tempword, final TransliterationMethod transliterationMethod) {
        /*if(tempchar==null){
            return null;
        }*/
        System.out.println("GetDictTransliteration yeah");
        switch (transliterationMethod){
            case FIRST:return tempword.getTransliterationSet().iterator().next().getTransliteration();
            case PROB: return tempword.getMostProbableWordTransliteration().getTransliteration();
            case RANDOM:return tempword.getRandomWordTransliteration().getTransliteration();
            default: return "";
        }
    }

    @Override
    public String getDictTransliteration(final PositionableChar tempchar, final TransliterationMethod transliterationMethod) {
        /*if(tempchar==null){
            return null;
        }*/
        System.out.println("GetDictTransliteration yeah");
        switch (transliterationMethod){
            case FIRST:return tempchar.getTransliterationSet().iterator().next().getTransliteration();
            case PROB: return tempchar.getMostProbableWordTransliteration().getTransliteration();
            case RANDOM:return tempchar.getRandomWordTransliteration().getTransliteration();
            default: return "";
        }
    }

    @Override
    public Map<Double, Set<String>> getFreqCandidatesForChar(final String charactersequence) {
        Map<Double,Set<String>> candidates=new TreeMap<>();
        for(String key:this.dictionary.keySet()){
            if(key.startsWith(charactersequence)){
                if(candidates.get(this.dictionary.get(key).getRelativeOccurance())==null){
                    candidates.put(this.dictionary.get(key).getRelativeOccurance(),new TreeSet<String>());
                }
                candidates.get(this.dictionary.get(key).getRelativeOccurance()).add(key);
            }
        }
        return candidates;
    }

    @Override
    public String getNoDictTranslation(final String word, final TranslationMethod translationMethod, final Locale locale) {
        return word;
    }

    @Override
    public String getNoDictTransliteration(final String word, final TransliterationMethod transliterationMethod) {
        System.out.println("GetNoDictTransliteration yeah");
        String curchar,result="[";
        if(word.length()<chartype.getChar_length()){
            return " ";
        }else if(word.length()==chartype.getChar_length()){
            switch(transliterationMethod){
                case PROB: return "["+this.dictmap.get(word).getMostProbableBeginTransliteration(0).getTransliteration()+"]";
                case RANDOM:
                case FIRST:
                default:   if(this.dictmap.get(word)!=null)
                                return "["+this.dictmap.get(word).getFirstSingleTransliteration().getTransliteration()+"]";
                           else
                                return "[]";
            }
        }
        switch(transliterationMethod){
            case PROB:
                for(int i=0;i<word.length();i+=chartype.getChar_length()){
                    curchar=word.substring(i,i+chartype.getChar_length());
                    if(i==0 && this.dictmap.get(curchar)!=null){
                        result+=this.dictmap.get(curchar).getMostProbableBeginTransliteration(i)+"-";
                    }else if(i<word.length()-chartype.getChar_length() && this.dictmap.get(curchar)!=null){
                        result+=this.dictmap.get(curchar).getMostProbableMiddleTransliteration(i)+"-";
                    }else if(i==word.length()-chartype.getChar_length() && this.dictmap.get(curchar)!=null){
                        result+=this.dictmap.get(curchar).getMostProbableEndTransliteration(i)+"] ";
                    }
                }
                break;
            case RANDOM:for(int i=0;i<word.length();i+=chartype.getChar_length()){
                curchar=word.substring(i,i+chartype.getChar_length());
                if(i==0 && this.dictmap.get(curchar)!=null){
                    result+=this.dictmap.get(curchar).getRandomWordTransliteration()+"-";
                }else if(i<word.length()-chartype.getChar_length() && this.dictmap.get(curchar)!=null){
                    result+=this.dictmap.get(curchar).getRandomWordTransliteration()+"-";
                }else if(i==word.length()-chartype.getChar_length() && this.dictmap.get(curchar)!=null){
                    result+=this.dictmap.get(curchar).getRandomWordTransliteration()+"] ";
                }
            }break;
            case FIRST:
            default:for(int i=0;i<word.length();i+=chartype.getChar_length()){
                curchar=word.substring(i,i+chartype.getChar_length());
                if(i==0 && this.dictmap.get(curchar)!=null){
                    result+=this.dictmap.get(curchar).getFirstBeginningTransliteration()+"-";
                }else if(i<word.length()-chartype.getChar_length() && this.dictmap.get(curchar)!=null){
                    result+=this.dictmap.get(curchar).getFirstMiddleTransliteration()+"-";
                }else if(i==word.length()-chartype.getChar_length() && this.dictmap.get(curchar)!=null){
                    result+=this.dictmap.get(curchar).getFirstEndTransliteration()+"] ";
                }
            }
        }
        return result;
    }

    @Override
    public void importDictFromXML(final String filepath) throws ParserConfigurationException, SAXException, IOException {
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        SAXParser parser=SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        CNImportHandler imp=new CNImportHandler(Options.FILLDICTIONARY,this,this.dictionary,this.translitToWordDict,this.transcriptToWordDict,CharTypes.CHINESE);
        parser.parse(filepath,imp);
        System.out.println("AmountWordsInCorpus: "+imp.amountOfWordsInCorpus+" - Length: "+imp.lengthOfWordsInCorpus);
        this.amountOfWordsInCorpus=imp.amountOfWordsInCorpus;
        this.lengthOfWordsInCorpus=imp.lengthOfWordsInCorpus;
    }

    @Override
    public void importMappingFromXML(final String filepath) throws ParserConfigurationException, SAXException, IOException {
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        CNImportHandler imp=new CNImportHandler(Options.FILLMAP,this,this.dictmap,this.translitToCharMap,this.transcriptToWordDict,CharTypes.CHINESE);
        parser.parse(in,imp);
        parser.reset();
    }

    @Override
    public LangChar matchChar(final String translit) {
        return this.dictmap.get(translit);
    }

    public LangChar matchChar(final String word,CharTypes chartype){
        if(chartype==CharTypes.TRANSLITCHAR){
            return this.translitToChar(word);
        }else{
            return this.matchChar(word);
        }
    }

    @Override
    public LangChar matchReverseWord(final String word) {
        return this.reversedictionary.get(word);
    }

    @Override
	public LangChar matchWord(final String word) {
		return this.dictionary.get(word);
	}

    @Override
    public LangChar matchWordByTranscription(final String word,Boolean noncunei) {
        if(this.transcriptToWordDict.containsKey(word) && transcriptToWordDict.get(word)!=null && this.dictionary.containsKey(transcriptToWordDict.get(word))){
            return this.dictionary.get(transcriptToWordDict.get(word));
        }
        return null;
    }

    @Override
    public LangChar matchWordByTransliteration(final String word) {
        if(this.translitToWordDict.containsKey(word)){
            return this.dictionary.get(this.translitToWordDict.get(word));
        }
        return null;
    }

    public void parseDictFile(final File file) throws IOException{
		final BufferedReader reader=new BufferedReader(new FileReader(file));
		String line,entry;
		String[] splitit;
		while((line=reader.readLine())!=null){
            if(!line.contains("#")) {
			    entry=line.substring(line.indexOf(" "),line.indexOf("["));
                //System.out.println(i+++": "+entry);
			    this.dictionary.put(entry,new CNChar(entry));
			    splitit=line.split("/");
			    for(int i=1;i<splitit.length;i++){
                    this.dictionary.get(entry).addTranslation(splitit[i], Locale.ENGLISH);
			    }
                this.dictionary.get(entry).addTransliteration(new Transliteration(splitit[0].substring(splitit[0].indexOf("[") + 1, splitit[0].lastIndexOf("]")),splitit[0].substring(splitit[0].indexOf("[") + 1, splitit[0].lastIndexOf("]"))));
                //System.out.println(dictionary.get(entry));
            }
		}
		reader.close();
	}

    /**
     * Translates a transliteration char to its cuneiform dependant.
     * @param translit the transliteration
     * @return The cuneiform character as String
     */
    public LangChar translitToChar(final String translit){
        if(!this.translitToCharMap.containsKey(translit)){
            return null;
        }
        return this.dictmap.get(this.translitToCharMap.get(translit));
    }

}

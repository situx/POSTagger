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

package com.github.situx.postagger.dict.dicthandler;

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.corpusimport.util.NGramStat;
import com.github.situx.postagger.dict.importhandler.NGramImportHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.utils.MorphPattern;
import com.github.situx.postagger.dict.utils.POSTag;
import com.github.situx.postagger.main.gui.ime.jquery.tree.builder.PaintTreeBuilder;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;
import com.github.situx.postagger.util.enums.util.ExportMethods;
import com.github.situx.postagger.util.enums.util.Files;
import com.github.situx.postagger.util.enums.util.Tags;
import com.github.situx.postagger.dict.utils.StopChar;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.pos.POSTags;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;

/**
 * @author Timo Homburg
 * Abstract class for defining a dictionary or wordmap handler.
 */
public abstract class DictHandling {
    /**Amount of characer in corpus.*/
    private final Double amountOfCharsInCorpus;
    /**Amount of characters transliterations in corpus.*/
    protected Double amountOfCharTranslitsInCorpus=0.;
    /**Amount of word transliterations in corpus.*/
    protected Double amountOfWordTranslitsInCorpus=0.;
    /**Amount of words in corpusimport with duplicates.*/
    protected Double amountOfWordsInCorpus=0.;
    /**The average word length as double.*/
    protected Double averageWordLength=0.;
    /**Counts boundaries that followed boundary characters.*/
    protected Double boundariesFollowBoundaries;
    /**Counts boundaries that followed continuation characters.*/
    protected Double boundariesFollowContinuations;
    protected CharTypes chartype;
    /**Counts continuations that followed boundary characters.*/
    protected Double continuationFollowsBoundary;
    /**Counts continurations that followed continuation characters.*/
    protected Double continuationFollowsContinuation;
    /**Length of all the words in the corpusimport.*/
    protected Double lengthOfWordsInCorpus;
    /**The ngrams of this dicthandler.*/
    protected NGramStat ngams;
    /**The postagger being used by this dicthandler.*/
    protected POSTagger postagger;
    /**Maps from transcriptions to words of the corresponding language.*/
    protected Map<String,String> reverseTranscriptToWordDict;
    /**Map from transliterations to Chars of the corresponding language.*/
    protected Map<String,String> reverseTranslitToCharMap;
    /**Maps from transliterations to words of the corresponding language.*/
    protected Map<String,String> reverseTranslitToWordDict;
    /**The list of stopchars.*/
    protected Map<String,StopChar> stopchars;
    /**Maps from transcriptions to words of the corresponding language.*/
    protected Map<String,String> transcriptToWordDict;
    /**Map from transliterations to Chars of the corresponding language.*/
    protected Map<String,String> translitToCharMap;
    /**Maps from transliterations to words of the corresponding language.*/
    protected Map<String,String> translitToWordDict;
    protected Map<POSTags,Map<String,LangChar>> postagMap;

    protected Map<String,Set<String>> paintInfoToCharStrings;
    private Double avgWordLength;

    protected PaintTreeBuilder paintTree;

    public Map<String,Set<MorphPattern>> morphpattern;

    public PaintTreeBuilder getPaintTree() {
        return paintTree;
    }

    public void setPaintTree(final PaintTreeBuilder paintTree) {
        this.paintTree = paintTree;
    }

    public Map<String, Set<String>> getPaintInfoToCharStrings() {
        return paintInfoToCharStrings;
    }

    /**Constructor for DictHandling.*/
    public DictHandling(List<String> stopchars,CharTypes chartype,POSTagger postagger) {
        this.translitToCharMap =new TreeMap<>();
        this.translitToWordDict =new TreeMap<>();
        this.morphpattern=new TreeMap<>();
        this.paintInfoToCharStrings=new TreeMap<>();
        this.postagMap=new TreeMap<>();
        this.transcriptToWordDict=new TreeMap<>();
        this.reverseTranscriptToWordDict =new TreeMap<>();
        this.reverseTranslitToCharMap =new TreeMap<>();
        this.reverseTranslitToWordDict=new TreeMap<>();
        this.stopchars=new TreeMap<>();
        if(stopchars!=null) {
            for (String str : stopchars) {
                StopChar stopchar = new StopChar();
                stopchar.setStopchar(str);
                this.stopchars.put(str, stopchar);
            }
        }
        this.averageWordLength=0.;
        this.amountOfWordsInCorpus=0.;
        this.lengthOfWordsInCorpus=0.;
        this.amountOfWordTranslitsInCorpus=0.;
        this.amountOfCharTranslitsInCorpus=0.;
        this.amountOfCharsInCorpus=0.;
        this.continuationFollowsBoundary=0.;
        this.continuationFollowsContinuation=0.;
        this.boundariesFollowContinuations=0.;
        this.boundariesFollowBoundaries=0.;
        this.chartype=chartype;
        this.postagger=postagger;
        this.ngams=new NGramStat();
    }

    /**
     * Adds a word to the dictionary/the character map.
     * @param word the word to add
     */
    public abstract void addChar(final LangChar word);

    public void addPOSTagForWord(POSTag postag, String translit, LangChar langchar){
        if(!this.postagMap.containsKey(postag.getPostag())){
            this.postagMap.put(postag.getPostag(),new TreeMap<>());
        }
        this.postagMap.get(postag.getPostag()).put(translit,langchar);
    }

    /**
     * Adds a following word to the current word.
     * @param word the current word
     * @param following the following word
     */
    public abstract void addFollowingWord(final String word,final String following);
    /**
     * Adds a following word to the current word.
     * @param word the current word
     * @param following the following word
     * @param preceding the preceding word
     */
    public abstract void addFollowingWord(final String word,final String following,final String preceding);

    /**
     * Adds a stopword to the list of stopwords.
     * @param stopChar the stopchar to add
     */
    public void addStopWord(final StopChar stopChar){
        if(!this.stopchars.containsKey(stopChar.getStopchar())){
            this.stopchars.put(stopChar.getStopchar(),stopChar);
        }
        stopChar.setAbsoluteOccurance(stopChar.getAbsoluteOccurance()+1);
    }

    public abstract void addTranscriptNonCunei(String transcription,LangChar word);

    /**
     * Adds a word to the dictionary/the character map.
     * @param word the word to add
     */
    public abstract void addWord(final LangChar word,final CharTypes charType);

    /**
     * Adds a word to the dictionary/the character map.
     * @param word the word to add
     */
    public abstract void addWordFromDictImport(final LangChar word,final CharTypes charType);

    /**
     * Calculates the average word length of this dicthandler.
     */
    public void calculateAvgWordLength() {
        this.averageWordLength=Math.round((this.lengthOfWordsInCorpus/this.amountOfWordsInCorpus)*100.00)/100.00>1?Math.round((this.lengthOfWordsInCorpus/this.amountOfWordsInCorpus)*100.00)/100.00:2;
    }

    /**
     * Calculates the occurances of a word/char relative to the corpusimport size.
     * @param charsInCorpus the amoutn of chars in the given corpusimport
     */
    public abstract void calculateRelativeCharOccurances(final Double charsInCorpus);

    /**
     * Calculates the occurances of a word/char relative to the corpusimport size.
     * @param wordsInCorpus the amount of word in the given corpusimport
     */
    public abstract void calculateRelativeWordOccurances(final Double wordsInCorpus);
    /**Calculates the left and right accessor variety.*/
    public abstract void calculateRightLeftAccessorVariety();

    public abstract String reformatToASCIITranscription(String transcription);

    public abstract String reformatToUnicodeTranscription(String transcription);

    /**
     * Exports the dictionary and the character map to XML.
     * @param dictpath path of the dictionary file
     * @param mappath  path of the character map file
     * @throws XMLStreamException
     * @throws IOException
     */
    public abstract void exportToXML(final String dictpath,final String reversedictpath, final String mappath,final String ngrampath,Boolean statistics)throws XMLStreamException, IOException;

    /**
     * Gets the list of alternative writings in this dicthandler.
     * @return the list
     */
    public List<String> getAlternativeWritings(){
          return null;
    }

    /**Returns the amount of words given in the corpusimport.
     * @return the amount as double
     */
    public Double getAmountOfWordsInCorpus(){
        return this.amountOfWordsInCorpus;
    }

    /**
     * Sets the amount of words in this corpus.
     * @param amountOfWordsInCorpus the amount as Double
     */
    public void setAmountOfWordsInCorpus(final Double amountOfWordsInCorpus) {
        this.amountOfWordsInCorpus = amountOfWordsInCorpus;
    }

    /**
     * Returns the average word length of the given corpusimport.
     * @return the average word length as int
     */
    public Double getAvgWordLength(){
        return this.avgWordLength;
    }

    /**
     * Sets the average word length of this dicthandler.
     * @param avgWordLength the average word length to set
     */
    public void setAvgWordLength(final Double avgWordLength) {
        this.avgWordLength = avgWordLength;
    }

    /**
     * Gets the occurances of boundaries following boundaries.
     * @return  the occurance as Double
     */
    public Double getBoundariesFollowBoundaries() {
        return boundariesFollowBoundaries;
    }
    /**
     * Gets the occurances of boundaries following continuations.
     * @return  the occurance as Double
     */
    public Double getBoundariesFollowContinuations() {
        return boundariesFollowContinuations;
    }

    /**
     * Gets a set of possible transliterations for a character or sequence of characters.
     * @param charactersequence the sequence of characters to match
     * @return The set of possible transliterations
     */
    public abstract Set<String> getCandidatesForChar(final String charactersequence);

    /**
     * Gets the chartype.
     * @return the chartype
     */
    public CharTypes getChartype() {
        return chartype;
    }
    /**
     * Gets the occurances of continuations following boundaries.
     * @return  the occurance as Double
     */
    public Double getContinuationFollowsBoundary() {
        return continuationFollowsBoundary;
    }
    /**
     * Gets the occurances of continuations following continuations.
     * @return  the occurance as Double
     */
    public Double getContinuationFollowsContinuation() {
        return continuationFollowsContinuation;
    }

    /**
     * Gets the dictmap.
     * @return the dictmap
     */
    public abstract Map<String,? extends LangChar> getDictMap();

    /**
     * Gets the dictionary translation for a given word.
     * @param word the word
     * @param translationMethod the translationmethod
     * @param locale the locale
     * @return the translation as String
     */
    public String getDictTranslation(final LangChar word, final TranslationMethod translationMethod, final Locale locale){
        if(word==null){
            return null;
        }
        switch (translationMethod){
            case LEMMA:
            case LEMMAFIRST: return word.getFirstTranslation(locale);
            case LEMMAPROB:  return word.getMaxProbTranslation(locale);
            case LEMMARANDOM: return null;
            default:  return null;
        }
    }

    public abstract String getDictTransliteration(final LangChar tempword, final TransliterationMethod transliterationMethod);

    public abstract Map<String,? extends LangChar> getDictionary();

    /**
     * Gets a set of possible transliterations for a character or sequence of characters.
     * @param charactersequence the sequence of characters to match
     * @return The set of possible transliterations including their frequency
     */
    public abstract Map<Double,Set<String>> getFreqCandidatesForChar(final String charactersequence);

    public NGramStat getNGramStats() {
        return ngams;
    }

    public abstract String getNoDictTranslation(final String word, final TranslationMethod translationMethod, final Locale locale);

    /**Matches a given String which cannot be found in the dictionary.
     * @param word  the word to match
     * @param transliterationMethod indicates which TransliterationMethod to choose
     * @return the transliteration as string
     */
    public abstract String getNoDictTransliteration(final String word, final TransliterationMethod transliterationMethod);

    public POSTagger getPosTagger(){
        return this.postagger;
    }

    public Map<String,StopChar> getStopchars() {
        return stopchars;
    }

    public Map<String, String> getTranscriptToWordDict() {
        return transcriptToWordDict;
    }

    public Map<String, String> getTranslitToCharMap() {
        return translitToCharMap;
    }

    public Map<String, String> getTranslitToWordDict() {
        return translitToWordDict;
    }

    /**
     * Imports a given dictionary from XML.
     * @param filepath the path to the dictionary
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public abstract void importDictFromXML(final String filepath)throws ParserConfigurationException, SAXException, IOException;

    public abstract void importMappingFromXML(final String filepath)throws ParserConfigurationException, SAXException, IOException;

    public void importNGramsFromXML(final String filepath) throws IOException, SAXException, ParserConfigurationException {
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        NGramImportHandler imp=new NGramImportHandler(this.ngams,this.chartype);
        parser.parse(in,imp);
        parser.reset();
    }

    /**
     * Imports a given dictionary from XML.
     * @param filepath the path to the dictionary
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public abstract void importReverseDictFromXML(final String filepath)throws ParserConfigurationException, SAXException, IOException;

    public void incLengthOfWordsInCorpus(Integer length){
          this.lengthOfWordsInCorpus+=length;
    }

    /**
     * Checks if a given word/char is a following word..
     * @return true if it is a following word, false otherwise
     */
    public abstract boolean isFollowingWord(final LangChar word,final String following);

    /**Matches a given char with the map of chars.
     * @param translit the char to match
     * @return the LangChar which matches the given transliteration
     */
    public abstract LangChar matchChar(final String translit);

    public abstract LangChar matchChar(final String word,CharTypes chartype);

    public Double matchNGramOccurance(String word){
        return this.ngams.getNGramOccurance(word);
    }

    public abstract LangChar matchReverseWord(String word);

    public StopChar matchStopChar(String stopchar){
        if(this.stopchars.containsKey(stopchar)){
            return this.stopchars.get(stopchar);
        }
        return null;
    }

    /**Matches a given String with the dictionary.
     * @param word  the word to match
     * @return the LangChar which matches the given word
     */
    public abstract LangChar matchWord(final String word);

    public abstract LangChar matchWordByTranscription(String word,Boolean noncunei);

    public abstract LangChar matchWordByTransliteration(String word);

    public abstract LangChar matchWordByPOSandTransliteration(String word,POSTags postag);

    /**
     * Parses the file containing a non-standardized dictionary.
     * @param file the dictionary file
     * @throws IOException on IO error
     * @throws ParserConfigurationException on Parsing error
     * @throws SAXException on SAXException
     */
    public abstract void parseDictFile(final File file) throws IOException, ParserConfigurationException, SAXException;

    public String reverseTransliteration(final String translit,final String splitcriterion){
        StringBuilder result=new StringBuilder();
        if(!translit.contains("-")){
            return translit;
        }
        //first create a list from String array
        List<String> list = Arrays.asList(translit.split(splitcriterion));

        //next, reverse the list using Collections.reverse method
        Collections.reverse(list);
        for(String str:list){
            result.append(str);
            result.append("-");
        }
        return result.substring(0,result.length()-1);
    }

    public void setCharType(final CharTypes charType) {
        this.chartype = charType;
    }


    public String toIME(final ExportMethods export, Map<String,? extends LangChar> dictmap, Map<String,? extends LangChar> dictionary, String header, String exportfile) throws IOException {
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File(exportfile+"_"+export.methodname.toLowerCase()+export.fileformat)));
        BufferedReader footerreader;
        String temp;
        writer.write(header);
        if(export==ExportMethods.GOTTSTEINJSON){
            writer.write("{"+System.lineSeparator());
            for(Iterator<String> iterout=this.paintInfoToCharStrings.keySet().iterator();iterout.hasNext();){
                String strokes= iterout.next();
                writer.write("\""+strokes+"\":[");
                for(Iterator<String> iter=this.paintInfoToCharStrings.get(strokes).iterator();iter.hasNext();){
                    String chara=iter.next();
                    if(chara.isEmpty()){
                        continue;
                    }
                    if(this.getDictMap().containsKey(chara))
                        writer.write("[\""+chara+" ("+this.getDictMap().get(chara).getCharName()+")\",\"<html>"+this.getDictMap().get(chara).getCharInformation("<br>",true,false)+"</html>\"]");
                    if(iter.hasNext()){
                       writer.write(",");
                    }
                }
                if(iterout.hasNext()){
                    writer.write("],"+System.lineSeparator());
                }else{
                    writer.write("]"+System.lineSeparator());
                }

            }
            writer.write("}");
        }else if(export==ExportMethods.CUNEILISTJSON){
            writer.write("{"+System.lineSeparator());
            for(Iterator<String> iterout=this.translitToCharMap.keySet().iterator();iterout.hasNext();){
                String translit= iterout.next();
                writer.write("\""+translit+"\":\""+this.translitToCharMap.get(translit)+"\"");
                String asciitrans=this.reformatToASCIITranscription(translit);
                if(!translit.equals(asciitrans)){
                    writer.write(","+System.lineSeparator());
                    writer.write("\""+asciitrans+"\":\""+this.translitToCharMap.get(translit)+"\"");
                }
                if(iterout.hasNext()){
                    writer.write(","+System.lineSeparator());
                }else{
                    writer.write(""+System.lineSeparator());
                }
            }
            writer.write("}");
        }else if(export==ExportMethods.IBUS){

        File file=new File(Files.IME_DIR + export.methodname.toLowerCase() + File.separator + export.methodname.toLowerCase() + Files.HEADER.toString() + export.fileformat);
        if(file.exists()){
            footerreader = new BufferedReader(new FileReader(file));
            while ((temp = footerreader.readLine()) != null) {
                writer.write(temp + System.lineSeparator());
            }
            footerreader.close();
            for(LangChar ch:dictmap.values()){
                writer.write(ch.toIME(export));
            }
            for(LangChar ch:dictionary.values()){
                writer.write(ch.toIME(export));
            }
        }
        } else{
            writer.write("<?xml version=\"1.0\"?><data>");
            for(LangChar ch:dictmap.values()){
                writer.write(ch.toIME(export));
            }
            for(LangChar ch:dictionary.values()){
                writer.write(ch.toIME(export));
            }
        }
        File file=new File(Files.IME_DIR + export.methodname.toLowerCase() + File.separator + export.methodname.toLowerCase() + Files.FOOTER.toString() + export.fileformat);
        if(file.exists()){
            footerreader = new BufferedReader(new FileReader(file));
            while ((temp = footerreader.readLine()) != null) {
                writer.write(temp + System.lineSeparator());
            }
            footerreader.close();
        }
        writer.close();
        return Files.IME_DIR+exportfile+"_"+export.methodname.toLowerCase()+export.fileformat;
    }

    /**
     * Translates a transliteration char to its cuneiform dependant.
     * @param translit the transliteration
     * @return The cuneiform character as String
     */
    public abstract LangChar translitToChar(final String translit);



}

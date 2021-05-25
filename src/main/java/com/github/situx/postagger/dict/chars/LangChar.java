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

package com.github.situx.postagger.dict.chars;

import com.github.situx.postagger.dict.utils.*;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.dict.importhandler.cuneiform.CuneiImportHandler;
import com.github.situx.postagger.dict.utils.*;
import com.github.situx.postagger.util.enums.util.ExportMethods;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Interface for modelling a class for chars/words of a language.
 */
public abstract class LangChar implements Comparable<LangChar>{
    /**The character as String.*/
    protected String character;
    /**The length of the character as Integer.*/
    protected Integer charlength;

    public String getRefString() {
        return refString;
    }

    public void setRefString(String refString) {
        this.refString = refString;
    }

    protected String refString;
    /**
     * Saves words or chars which occured after this word/char with a corresponding frequency
     */
    protected Map<String, Following> followingChars;
    /**Left and right accessor variety.*/
    protected Double leftaccessorvariety,rightaccessorvariety;
    /**The meaning of this character.*/
    protected String meaning;
    /**The occurances of this character.*/
    protected Double occurances;
    /**A set of postags associated with this character.*/
    protected Set<POSTag> postags;

    protected Set<Dialect> dialects;

    protected Set<Epoch> epochs;
    /**Map of preceding chars associated with this character.*/
    protected Map<String,Preceding> precedingChars;
    /**Map of transliterations to translations and their occurances.*/
    protected Map<String,Map<Translation,Integer>> translations;
    /**Map of transliterations to occurances.*/
    protected Map<Transliteration,Double> transliterations;

    protected String paintInformation;

    protected String charName;

    public void setConceptURI(String conceptURI) {
        this.conceptURI = conceptURI;
    }

    private String conceptURI;

    public Boolean getIsWord() {
        return isWord;
    }

    public void setIsWord(final Boolean isWord) {
        this.isWord = isWord;
    }

    protected Boolean isWord;

    public TreeSet<String> logographs;

    public Set<POSTag> getPostags() {
        return postags;
    }

    public void setPostags(final Set<POSTag> postags) {
        this.postags = postags;
    }

    public Map<String, Preceding> getPrecedingChars() {
        return precedingChars;
    }

    public void setPrecedingChars(final Map<String, Preceding> precedingChars) {
        this.precedingChars = precedingChars;
    }

    public String getCharName() {

        return charName;
    }

    public String getCharInformation(final String lineSeparator,final Boolean translitList,final Boolean html){
        StringBuilder result=new StringBuilder();
        if(translitList){
            if(html){
                result.append("<font face=\"Akkadian\">");
                result.append(this.toString());
                result.append("</font>");
            }else{
                result.append(this.toString());
            }
            result.append((this.charName!=null && !this.charName.isEmpty()?(" ("+this.charName+") "+lineSeparator):""));
            if(!this.transliterations.keySet().isEmpty()) {
                result.append(this.transliterations.keySet().toString());
                result.append(lineSeparator);
            }

        } else{
            result.append((this.charName!=null && !this.charName.isEmpty()?(this.charName+lineSeparator):("")));
        }
        if(!this.isWord){
            result.append("Codepoint: U+");
            result.append(Integer.toHexString(this.toString().codePointAt(0)).toUpperCase());
            result.append(lineSeparator);
            if(this.paintInformation!=null){
                result.append("PaintInfo: ");
                result.append(this.paintInformation);
            }
        } else {
            result.append("Occurance: ");
            result.append(this.occurances);
            result.append(lineSeparator);
            result.append("POSTags: ");
            result.append(this.postags);
            result.append(lineSeparator);
        }
        if(!this.meaning.isEmpty()){
            result.append(lineSeparator);
            result.append("Meaning: ").append(this.meaning);
        }
        return result.toString();
    }

    public void setCharName(final String charName) {
        this.charName = charName;
    }

    public String getPaintInformation() {
        return paintInformation;
    }

    public void setPaintInformation(final String paintInformation) {
        this.paintInformation = paintInformation;
    }

    public String getConceptURI() {
        return conceptURI;
    }

    public Set<Dialect> getDialects() {
        return dialects;
    }

    public void addDialect(Dialect dialect) {
        this.dialects.add(dialect);
    }

    public Set<Epoch> getEpochs() {
        return epochs;
    }

    public void addEpoch(Epoch epoch) {
        this.epochs.add(epoch);
    }

    /**
     * Constructor for this class.
     * @param character the character to set
     */
    public LangChar(String character){
        this.character=character;
        this.translations=new TreeMap<>();
        this.transliterations=new TreeMap<>();
        this.followingChars=new TreeMap<>();
        this.precedingChars=new TreeMap<>();
        this.dialects=new TreeSet<>();
        this.epochs=new TreeSet<>();
        this.logographs=new TreeSet<String>();
        this.postags=new TreeSet<>();
        this.occurances=1.;
        this.isWord=false;
        this.meaning="";
        this.conceptURI="";

        this.leftaccessorvariety=0.;
        this.rightaccessorvariety=0.;
        this.occurances=0.;
        this.charlength= CharTypes.LANGCHAR.getChar_length();

    }

    /**
     * Adds a word/char following this char to its description and/or increases the probability
     * @param following the following word to add
     */
    public void addFollowingWord(final String following,final String preceding) {
        this.addFollowingWord(following,preceding, false,false);
    }

    /**
     * Adds a word/char following this char to its description and/or increases the probability
     * @param following the following word to add
     */
    public void addFollowingWord(final String following) {
        this.addFollowingWord(following,"", false,true);
    }

    /**
     * Adds a word/char following this char to its description and/or increases the probability
     * @param following the following word to add
     */
    public void addFollowingWord(final String following,final Boolean boundary) {
        this.addFollowingWord(following,"", false,boundary);
    }

    /**
     * Adds a word/char following this char to its description and/or increases the probability
     * @param following the following word to add
     */
    public void addFollowingWord(final String following,final String preceding,final Boolean boundary) {
        this.addFollowingWord(following, preceding, false, boundary);
    }

    /**
     * Adds a word/char following this char to its description and/or increases the probability
     * @param following the following word to add
     */
    public void addFollowingWord(final Following following) {
        this.followingChars.put(following.getFollowingstr(), following);
    }

    /**
     * Adds a following word to this word.
     * @param following the following word to add
     * @param preceding the preceding word to add
     * @param followingboundary indicates if there is a boundary between this character and the following word
     * @param precedingboundary indicates if there is a boundary between this character and the preceding word
     */
    public void addFollowingWord(final String following,final String preceding, final boolean followingboundary,final boolean precedingboundary) {
        if (!followingboundary) {
            if (!this.followingChars.containsKey(following)) {
                this.followingChars.put(following, new Following(new Tuple<Double, Double>(1., 0.),following,preceding,false,precedingboundary));
            } else {
                this.followingChars.get(following).incFollowing(followingboundary);
                this.followingChars.get(following).incPreceding(preceding,precedingboundary);
                //this.followingChars.put(following, new Tuple<Double, Double>(this.followingChars.get(following).getOne() + 1., this.followingChars.get(following).getTwo()));
            }
        } else {
            if (!this.followingChars.containsKey(following)) {
                this.followingChars.put(following, new Following(new Tuple<Double, Double>(0., 1.),following,preceding,false,precedingboundary));
            } else {
                this.followingChars.get(following).incFollowing(followingboundary);
                this.followingChars.get(following).incPreceding(preceding,precedingboundary);
                //this.followingChars.put(following, new Tuple<Double, Double>(this.followingChars.get(following).getOne(), this.followingChars.get(following).getTwo() + 1));
            }
        }
    }

    /**
     * Adds a word/char following this char to its description and/or increases the probability
     * @param following the following word to add
     * @param frequency a given frequency
     */
    public void addFollowingWord(final String following,final String preceding, final Tuple<Double,Double> frequency){
        Following following1=new Following(frequency,following,preceding,false,false);
        this.followingChars.put(following, following1);
    }

    /**Gets the occurances of this word/char in the corpusimport.*/
    public void addOccurance(){
        this.occurances++;
    }

    /**
     * Adds a postag to the set of postags.
     * @param posTag  the postag to add
     */
    public void addPOSTag(final POSTag posTag){
        this.postags.add(posTag);
    }

    /**
     * Adds a word/char following this char to its description and/or increases the probability
     * @param preceding the following word to add
     */
    public void addPrecedingWord(final Preceding preceding) {
        this.precedingChars.put(preceding.getFollowingstr(), preceding);
    }

    /**
     * Adds a word/char following this char to its description and/or increases the probability
     * @param preceding the following word to add
     */
    public void addPrecedingWord(final String preceding) {
        if(!this.precedingChars.containsKey(preceding)){
            Preceding preceding1=new Preceding();
            preceding1.setFollowingstr(preceding);
            preceding1.setFollowing(1.0,0.0);
            this.precedingChars.put(preceding,preceding1);
        }else{
            this.precedingChars.get(preceding).incFollowing(false);
        }
    }

    /**
     * Adds a word/char following this char to its description and/or increases the probability
     * @param preceding the following word to add
     */
    public void addPrecedingWord(final String preceding,final Boolean boundary) {
        if(!this.precedingChars.containsKey(preceding)){
            Preceding preceding1=new Preceding();
            preceding1.setFollowingstr(preceding);
            if(boundary){
                preceding1.setFollowing(0.0,1.0);
            }else{
                preceding1.setFollowing(1.0,0.0);
            }
            this.precedingChars.put(preceding,preceding1);
        }else{
            this.precedingChars.get(preceding).incFollowing(boundary);
        }
    }

    /**Adds an element to the set of translations.
     * @param translation the translation to add
     * @param locale the locale of the language of the translation
     * Example: Wörterbuch DE_de*/
    public Translation addTranslation(final String translation,final Locale locale){
        Translation translation1=new Translation(translation,locale);
        if(!this.translations.containsKey(locale.toString())){
           this.translations.put(locale.toString(),new TreeMap<>());
        }
        if(!this.translations.get(locale.toString()).containsKey(translation1)){
            this.translations.get(locale.toString()).put(translation1, 0);
        }
        this.translations.get(locale.toString()).put(translation1,this.translations.get(locale.toString()).get(translation1)+1);
        return translation1;
    }

    /**Adds a transliteration to the Set of transliterations.
     * @param transliteration the transliteration to add*/
    public void addTransliteration(final Transliteration transliteration){
        if(!transliterations.keySet().contains(transliteration)){
            this.transliterations.put(transliteration, 0.);
        }
        this.transliterations.put(transliteration,this.transliterations.get(transliteration)+1);
        transliteration.setAbsoluteOccurance(this.transliterations.get(transliteration));
    }

    /**
     * Calculates the left accessor variety for this character.
     */
    public void calculateLeftAccessorVariety(){
        Double leftaccessor=0.;
        Set<String> foundchars=new TreeSet<>();
        for(String preceding:this.precedingChars.keySet()){
            if(preceding.length()>=this.charlength && !foundchars.contains(preceding.substring(0, this.charlength))){
                foundchars.add(preceding.substring(0,this.charlength));
                leftaccessor++;
            }
        }
        this.leftaccessorvariety=leftaccessor;
    }

    /**
     * Calculates the right accessor variety for this character.
     */
    public void calculateRightAccessorVariety(){
        Double rightaccessor=0.;
        Set<String> foundchars=new TreeSet<>();
        for(String following:this.followingChars.keySet()){
             if(following.length()>=this.charlength && !foundchars.contains(following.substring(0,this.charlength))){
                 foundchars.add(following.substring(0,this.charlength));
                 rightaccessor++;
             }
        }
        this.rightaccessorvariety=rightaccessor;
    }

    @Override
    public int compareTo(final LangChar langChar) {
        int comp=this.length().compareTo(langChar.length());
        if(comp==0){
            return ((Integer)this.getOccurances().intValue()).compareTo(langChar.getOccurances().intValue())*-1;
        }
        return comp*-1;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof LangChar && this.character.equals(((LangChar) obj).character);
    }

    /**Gets the given character.
     * @return the character as String*/
    public String getCharacter(){
        return this.character;
    }

    /**Sets the character.
     * @param character the character to set
     */
    public void setCharacter(final String character){
        this.character = character;
    }

    /**
     * Gets the length of this characte/word.
     * @return the length as Integer
     */
    public Integer getCharlength() {
        return charlength;
    }

    /**
     * Gets the first translation of this character.
     * @param locale the locale of the language of the translation Example: Wörterbuch DE_de
     * @return the translation as String
     */
    public String getFirstTranslation(Locale locale){
        if(!this.translations.containsKey(locale.toString())){
             return "";
        }
        if(this.translations.get(locale.toString()).isEmpty()){
            return "";
        }
        return this.translations.get(locale.toString()).keySet().iterator().next().getTranslation();
    }

    /**Gets the words following the current character with corresponding probabilities.
     * @return a Map of String to Tuple which represents the relative and absolute occurances
     */
    public Map<String, Following> getFollowingWords(){
        return this.followingChars;
    }

    /**
     * Gets the left accessor variety for this character.
     * @return the left accessor variety as Double.
     */
    public Double getLeftaccessorvariety() {
        return leftaccessorvariety;
    }

    /**
     * Sets the left accessor variety for this character.
     * @param leftaccessorvariety the left accessor variety as Double.
     */
    public void setLeftaccessorvariety(final Double leftaccessorvariety) {
        this.leftaccessorvariety = leftaccessorvariety;
    }

    /**
     * Gets the maximum probability translation for this character/Word.
     * @param locale the locale of the language of the translation Example: Wörterbuch DE_de
     * @return the translation as String
     */
    public String getMaxProbTranslation(Locale locale){
        Integer maxprob=0;
        Translation result=this.translations.get(locale.toString()).keySet().iterator().next();
        for(Translation trans:this.translations.get(locale.toString()).keySet()){
            if(trans.getOccurance()>maxprob){
                maxprob=trans.getOccurance();
                result=trans;
            }
        }
        return result.getTranslation();
    }

    /**
     * Gets the meaing of this character.
     * @return  the meaning as String
     */
    public String getMeaning() {
        return meaning;
    }

    /**
     * Sets the meaning of this character.
     * @param meaning the meaning as String
     */
    public void setMeaning(final String meaning) {
        this.meaning = meaning;
    }

    /**
     * Gets the most probable word transliteration of this word.
     *
     * @return the transliteration as transliteration object
     */
    public Transliteration getMostProbableWordTransliteration() {
        Double maxprob = 0.;
        Transliteration result = this.transliterations.keySet().iterator().next();
        for (Transliteration transliteration : this.transliterations.keySet()) {
            if (transliteration.getAbsoluteOccurance() > maxprob) {
                maxprob = transliteration.getAbsoluteOccurance();
                result = transliteration;

            }
        }
        return result;
    }

    /**
     * Gets the number of chars of this character/word.
     * @return the number as Integer
     */
    public Integer getNumberOfChars(){
         return this.character.length()/this.charlength;
    }

    /**Gets the occurances of this word/char in the corpusimport.
     *
     * @return the occurances as Double
     */
    public Double getOccurances(){
        return this.occurances;
    }

    /**
     * Gets a random transliteration for this word.
     *
     * @return the transliteration as transliteration object
     */
    public Transliteration getRandomWordTransliteration() {
        Integer item,i=0;
        Random random=new Random(System.currentTimeMillis());
        item=random.nextInt(this.transliterations.size());
        for(Transliteration transliteration:this.transliterations.keySet()){
            if(item.equals(i++)){
                return transliteration;
            }
        }
        return this.transliterations.keySet().iterator().next();
    }

    /**Gets the relative occurance of this word/char in the corpusimport.
     *
     * @return the relative occurance as Double
     */
    public abstract Double getRelativeOccurance();

    /**Gets the occurances of this word/char in the corpusimport.
     * @param occurance the relative occurance to set
     */
    public abstract void setRelativeOccurance(final Double occurance);

    /**
     * Gets the right accessor variety of this character.
     * @return the right accessor variety as Double
     */
    public Double getRightaccessorvariety() {
        return rightaccessorvariety;
    }

    /**
     * Sets the right accessor variety of this character.
     * @param rightaccessorvariety The right accessor variety as Double
     */
    public void setRightaccessorvariety(final Double rightaccessorvariety) {
        this.rightaccessorvariety = rightaccessorvariety;
    }

    /**Gets the set of translations.
     *
     * @param locale the locale of the language of the translation Example: Wörterbuch DE_de
     * @return  the translation as String
     */
    public Map<Translation,Integer> getTranslationSet(Locale locale){
        if(this.translations.containsKey(locale.toString())){
            return this.translations.get(locale.toString());
        }
        return null;
    }

    public Map<String,Map<Translation,Integer>> getTranslations() {
        return this.translations;
    }

    /**Gets the set of transliterations.
     *
     * @return the set of transliterations
     */
    public Set<Transliteration> getTransliterationSet(){
        return this.transliterations.keySet();
    }

    /**Gets the map of transliterations.
     *
     * @return  the map of transliterations
     */
    public Map<Transliteration,Double> getTransliterations(){
        return this.transliterations;
    }

    /**Gets the length of this char/word.
     *
     * @return the length as Integer
     */
    public Integer length(){
        return this.character.length();
    }

    /**Sets the occurances of this word/char in the corpusimport.
     * @param occurance the occurances to set
     */
    public void setAbsOccurance(final Double occurance){
        this.occurances =occurance;
    }

    /**Gets the occurances of this word/char in the corpusimport.
     * @param occurance the relative occurance to set
     */
    public abstract void setRelativeOccuranceFromDict(final Double occurance);

    /**
     * Converts this char to a IME representation.
     * @param export the export method to choose
     * @return the representation as String
     */
    public String toIME(final ExportMethods export){
        String result="";
        StringWriter strwriter;
        StringBuilder buffer;
        XMLOutputFactory output3;
        XMLStreamWriter writer;
        Set<String> collect=new HashSet<>();
        CuneiImportHandler importH=new CuneiImportHandler();
        switch (export){
            case ANKI:
                buffer=new StringBuilder();
                for(Transliteration transliteration:this.transliterations.keySet()){
                    if(!collect.contains(importH.reformatToASCIITranscription(transliteration.getTranscription()))){
                        buffer.append(this.character);
                        buffer.append("\\");
                        buffer.append("<b>Transliteration: </b>");
                        buffer.append(transliteration.getTransliteration()
                        /*+"<br><br><b>Transkription:</b> "+importH.reformatToASCIITranscription(transliteration.getTranscription())*/);
                        buffer.append(System.lineSeparator());
                        collect.add(importH.reformatToASCIITranscription(transliteration.getTranscription()));
                    }
                }
                collect=new HashSet<>();
                for(Transliteration transliteration:this.transliterations.keySet()){
                    if(!collect.contains(importH.reformatToASCIITranscription(transliteration.getTranscription()))){
                        buffer.append("<b>Transliteration: </b>");
                        buffer.append(transliteration.getTransliteration()/*+"<br><br><b>Transkription:</b> "+importH.reformatToASCIITranscription(transliteration.getTranscription())*/);
                        buffer.append("\\");
                        buffer.append(this.character);
                        buffer.append(System.lineSeparator());
                        collect.add(importH.reformatToASCIITranscription(transliteration.getTranscription()));
                    }
                }
                result=buffer.toString();
                break;
            case ANDROID:
                strwriter=new StringWriter();
                output3 = XMLOutputFactory.newInstance();
                output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
                try {
                    writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
                    for(Transliteration transliteration:this.transliterations.keySet()){
                        writer.writeStartElement(Tags.W);
                        writer.writeAttribute(Tags.F,this.transliterations.get(transliteration)==null?0+"":this.transliterations.get(transliteration).intValue()+"");
                        writer.writeAttribute(Tags.W,this.character);
                        writer.writeEndElement();
                    }
                } catch (XMLStreamException | FactoryConfigurationError e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                result=strwriter.toString();
                break;
            case IBUS:
            case SCIM:
                buffer=new StringBuilder();
                for(Transliteration transliteration:this.transliterations.keySet()){
                    if(!collect.contains(importH.reformatToASCIITranscription(transliteration.getTranscription()))){
                        buffer.append(importH.reformatToASCIITranscription(transliteration.getTranscription()));
                        buffer.append('\t');
                        buffer.append(this.character);
                        buffer.append('\t');
                        buffer.append(this.transliterations.get(transliteration)==null?0:this.transliterations.get(transliteration).intValue());
                        buffer.append(System.lineSeparator());
                        collect.add(importH.reformatToASCIITranscription(transliteration.getTranscription()));
                    }

                }
                result=buffer.toString();
                break;
            case GOTTSTEIN:
                strwriter=new StringWriter();
                output3 = XMLOutputFactory.newInstance();
                output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
                if(this.paintInformation!=null) {
                    try {
                        writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
                        writer.writeStartElement(Tags.ENTRY);
                        writer.writeAttribute(Tags.KEY, this.paintInformation);
                        writer.writeAttribute(Tags.VAL, this.character);
                        writer.writeEndElement();
                        writer.writeCharacters(System.lineSeparator());
                    } catch (XMLStreamException | FactoryConfigurationError e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                result=strwriter.toString();
                break;
            case GOTTSTEINJSON:
                strwriter=new StringWriter();
                output3 = XMLOutputFactory.newInstance();
                output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
                if(this.paintInformation!=null) {
                    try {
                        strwriter.write("\"" + this.paintInformation + "\":\"" + this.character + "\"," + System.lineSeparator());
                        writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
                    } catch (XMLStreamException | FactoryConfigurationError e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                result=strwriter.toString();
                break;
            case JIME:
                strwriter=new StringWriter();
                output3 = XMLOutputFactory.newInstance();
                output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
                try {
                    writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
                    for(Transliteration transliteration:this.transliterations.keySet()){
                        if(!collect.contains(importH.reformatToASCIITranscription(transliteration.getTranscription()))) {
                            writer.writeStartElement(Tags.ENTRY);
                            writer.writeAttribute(Tags.KEY, new CuneiImportHandler().reformatToASCIITranscription(transliteration.getTranscription()));
                            writer.writeCharacters(this.character);
                            writer.writeEndElement();
                            writer.writeCharacters(System.lineSeparator());
                            collect.add(importH.reformatToASCIITranscription(transliteration.getTranscription()));
                        }
                    }
                } catch (XMLStreamException | FactoryConfigurationError e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                result=strwriter.toString();
                break;
            case JQUERY:
                strwriter=new StringWriter();
                output3 = XMLOutputFactory.newInstance();
                output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
                try {
                    writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
                    for(Transliteration transliteration:this.transliterations.keySet()){
                        if(!collect.contains(importH.reformatToASCIITranscription(transliteration.getTranscription()))) {
                            writer.writeStartElement(Tags.WORD);
                            writer.writeAttribute(Tags.FREQ,this.transliterations.get(transliteration)==null?0+"":this.transliterations.get(transliteration).intValue()+"");
                            writer.writeAttribute(Tags.MEANING.toString(),this.meaning);
                            writer.writeAttribute(Tags.CONCEPT,this.conceptURI);
                            writer.writeAttribute(Tags.TRANSLATION,(this.getFirstTranslation(Locale.ENGLISH)!=null?this.getFirstTranslation(Locale.ENGLISH):""));
                            writer.writeAttribute(Tags.POSTAG,(this.postags!=null && !this.postags.isEmpty()?this.postags.toString():""));
                            writer.writeAttribute(Tags.CHARS,this.character);
                            writer.writeAttribute(Tags.TRANSLIT,new CuneiImportHandler().reformatToASCIITranscription(transliteration.getTranscription()));
                            writer.writeEndElement();
                            writer.writeCharacters(System.lineSeparator());
                            collect.add(importH.reformatToASCIITranscription(transliteration.getTranscription()));
                        }
                    }
                } catch (XMLStreamException | FactoryConfigurationError e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                result=strwriter.toString();
                break;
            default:

        }
        return result;
    }

    @Override
    public String toString(){
        return this.character;
    }

    /**Enforces an xml representation of the character.
     *
     * @param startelement the startlement to choose
     * @return the xml representation as String
     */
    public String toXML(String startelement,Boolean statistics){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartElement(startelement);
            writer.writeAttribute(Tags.RIGHTACCVAR,this.rightaccessorvariety.toString());
            writer.writeAttribute(Tags.LEFTACCVAR,this.leftaccessorvariety.toString());
            for(Transliteration akkadtrans:this.transliterations.keySet()){
                writer.writeCharacters(akkadtrans.toXML(statistics) + System.lineSeparator());
            }
            for(String locale:this.translations.keySet()){
                for(Translation trans:this.translations.get(locale).keySet()){
                    writer.writeCharacters(trans.toXML() + System.lineSeparator());
                }
            }
            for(Preceding prec:this.precedingChars.values()){
                writer.writeCharacters(System.lineSeparator()+prec.toXML());
            }
            Map<String,Following> followingwords=this.getFollowingWords();
            //System.out.println(followingwords);
            for(String akkadfollow:followingwords.keySet()){
                writer.writeStartElement(Tags.FOLLOWING);
                writer.writeAttribute(Tags.ABSOCC.toString(), followingwords.get(akkadfollow).getFollowing().getOne().toString());
                writer.writeAttribute(Tags.ABSBD.toString(),followingwords.get(akkadfollow).getFollowing().getTwo().toString());
                writer.writeAttribute(Tags.PRECEDING, followingwords.get(akkadfollow).getPreceding().keySet().iterator().next());
                writer.writeCharacters(akkadfollow);
                writer.writeEndElement();
                writer.writeCharacters(System.lineSeparator());
            }
            writer.writeCharacters(this.character);
            writer.writeEndElement();
            writer.writeCharacters(System.lineSeparator());
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strwriter.toString();
    }

    public void toJSON(){

    }

    public TreeSet<String> getLogographs() {
        return logographs;
    }
}

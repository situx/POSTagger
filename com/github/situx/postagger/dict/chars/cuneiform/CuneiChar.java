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

package com.github.situx.postagger.dict.chars.cuneiform;

import com.github.situx.postagger.dict.chars.PositionableChar;
import com.github.situx.postagger.dict.importhandler.cuneiform.CuneiImportHandler;
import com.github.situx.postagger.dict.utils.*;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.dict.utils.*;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * Class for defining a cuneiform character/Word
 * User: Timo Homburg
 * Date: 26.10.13
 * Time: 14:30
 * Created with IntelliJ IDEA.
 */
public abstract class CuneiChar extends PositionableChar {
    public void setLhaNumber(String lhaNumber) {
        this.lhaNumber = lhaNumber;
    }

    protected String lhaNumber;

    public String getHethzlNumber() {
        return hethzlNumber;
    }

    protected String hethzlNumber;
    /**Affix of this character/word.*/
    protected String affix;

    protected String mezlNumber;

    public String getaBzlNumber() {
        return aBzlNumber;
    }

    public void setaBzlNumber(final String aBzlNumber) {
        this.aBzlNumber = aBzlNumber;
    }

    protected String aBzlNumber;

    protected String unicodeID;

    protected String unicodeCodePage;

    public String getMezlNumber() {
        return mezlNumber;
    }

    public void setMezlNumber(final String mezlNumber) {
        this.mezlNumber = mezlNumber;
    }

    public String getUnicodeID() {
        return unicodeID;
    }

    public void setUnicodeID(final String unicodeID) {
        this.unicodeID = unicodeID;
    }

    public String getUnicodeCodePage() {
        return unicodeCodePage;
    }

    public void setUnicodeCodePage(final String unicodeCodePage) {
        this.unicodeCodePage = unicodeCodePage;
    }

    public String getCuneiutf8translit() {
        return cuneiutf8translit;
    }

    public void setCuneiutf8translit(final String cuneiutf8translit) {
        this.cuneiutf8translit = cuneiutf8translit;
    }

    protected CuneiImportHandler cuneiHandler;

    protected String cuneiutf8translit;
    /**Indicates if this character is a determinative.*/
    protected Boolean determinative;
    protected Boolean isNumberChar;
    /**Indicates if this character has a logographic interpretation.*/
    protected Boolean logograph;
    /**Indicates if this character has a phonographic interpretation.*/
    protected Boolean phonogram;
    /**Stem of this word/character.*/
    protected String stem;
    /**Suffix of this word/character.*/
    protected String suffix;
    /**Indicates if this character is a sumerogram.*/
    protected Boolean sumerogram;
    /**The relative occurance of this char/word in the corpusimport.*/
    private Double relativeoccurance;

    /**
     * Constructor for this class.
     * @param character the character to add
     */
    public CuneiChar(final String character) {
        super(character);
        this.character=character;
        this.logograph = false;
        this.phonogram = false;
        this.mezlNumber="";
        this.aBzlNumber="";
        this.hethzlNumber="";
        this.lhaNumber="";
        this.determinative = false;
        this.sumerogram=false;
        this.cuneiutf8translit = " ";
        this.charlength= CharTypes.CUNEICHAR.getChar_length();
        this.stem="";
        this.isNumberChar=false;
        this.cuneiHandler=new CuneiImportHandler();

    }

    @Override
    public void addTransliteration(final Transliteration transliteration) {
        String asciitransstr=this.cuneiHandler.reformatToASCIITranscription(transliteration.getTransliteration());
        Transliteration asciitrans=new Transliteration(asciitransstr,"");
        if(transliterations.keySet().contains(asciitrans)){
            return;
        }else{
            transliteration.setTransliterationString(asciitransstr);
            transliteration.setUtf8transliteration(this.cuneiHandler.reformatToUnicodeTranscription(transliteration.getTransliteration()));
        }
        if(!transliterations.keySet().contains(transliteration)){
            this.transliterations.put(transliteration,0.);
        }
        this.transliterations.put(transliteration,this.transliterations.get(transliteration)+1);
        transliteration.setAbsoluteOccurance(this.transliterations.get(transliteration));
    }

    /**
     * Gets the name of this character.
     * @return the name as String
     */
    public String getCharName(){
        return this.charName;
    }

    /**
     * Sets the name of this character.
     * @param name the name of the character as String
     */
    public void setCharName(final String name) {
        this.charName=name;
    }

    /**
     * Indicates if this character is a determinative.
     * @return true if it is false otherwise
     */
    public Boolean getDeterminative(){
        return this.determinative;
    }

    /**
     * Sets if the character represents a determinative.
     * @param determinative determinative indicator
     */
    public void setDeterminative(final Boolean determinative){
        this.determinative=determinative;
    }

    /**
     * Indicates if this character is a character representing a number.
     * @return true if it is false otherwise
     */
    public Boolean getIsNumberChar() {
        return isNumberChar;
    }

    /**
     * Sets if this character is a character representing a number.
     * @param isNumberChar the number indicator as boolean
     */
    public void setIsNumberChar(final Boolean isNumberChar) {
        this.isNumberChar = isNumberChar;
    }

    public String getLhaNumber() {
        return lhaNumber;
    }

    /**
     * Indicates if this character is a logograph.
     * @return true if it is false otherwise
     */
    public Boolean getLogograph(){
        return this.logograph;
    }

    /**
     * Set if the character is a logograph.
     * @param logograph logograph indicator
     */
    public void setLogograph(final Boolean logograph){
        this.logograph=logograph;
    }

    /**
     * Indicates if the character is a phonogram.
     * @return true if it is false otherwise
     */
    public Boolean getPhonogram(){
        return this.phonogram;
    }

    /**
     * Sets if the character is a phonogram.
     * @param phonogram phonogram indicator
     */
    public void setPhonogram(final Boolean phonogram){
        this.phonogram=phonogram;
    }

    @Override
    public Double getRelativeOccurance() {
        return this.relativeoccurance;
    }

    @Override
    public void setRelativeOccurance(final Double total) {
        this.relativeoccurance = this.occurances / total;
    }

    /**
     * Incidicates if this character is a sumerogram.
     * @return true if it is false otherwise
     */
    public Boolean getSumerogram() {
        return sumerogram;
    }

    /**
     * Sets if the character is a sumerogram.
     * @param sumerogram sumerogram indicator
     */
    public void setSumerogram(final Boolean sumerogram) {
        this.sumerogram = sumerogram;
    }

    @Override
    public Integer length() {
        return this.character.length();
    }

    @Override
    public void setRelativeOccuranceFromDict(final Double relocc){
        this.relativeoccurance=relocc;
    }

    /**
     * Sets the stem of this character.
     * @param stem the stem to set.
     */
    public void setStem(final String stem){
        this.stem=stem;
    }

    @Override
    public String getCharInformation(final String lineSeparator,final Boolean translitList,final Boolean html) {
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
            }
            result.append(lineSeparator);
        } else{
            result.append((this.charName!=null?this.charName:""));
            result.append(lineSeparator);
        }
        if(!this.isWord){
            result.append("Codepoint: U+");
            result.append(Integer.toHexString(this.toString().codePointAt(0)).toUpperCase());
            result.append(lineSeparator);
            if(this.mezlNumber!=null && !this.mezlNumber.isEmpty()){
                result.append("MeZL: ");
                result.append(this.mezlNumber);
                result.append(lineSeparator);
            }
            if(this.aBzlNumber!=null && !this.aBzlNumber.isEmpty()){
                result.append("aBZL: ");
                result.append(this.aBzlNumber);
                result.append(lineSeparator);
            }
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
        }
        return result.toString();
    }

    @Override
    public String toString() {
        return this.character;
    }

    @Override
    public String toXML(String startelement,Boolean statistics) {
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
        writer.writeStartElement(startelement);
        //System.out.println(akkadchar.getCharacter() + " - " + akkadchar.getAkkadLogoName());
        writer.writeAttribute(Tags.LOGOGRAM, this.character);
        writer.writeAttribute(Tags.DETERMINATIVE.toString(),this.determinative.toString());
        writer.writeAttribute(Tags.LOGO.toString(),this.logograph.toString());
        writer.writeAttribute(Tags.PHONO.toString(),this.phonogram.toString());
        writer.writeAttribute(Tags.GOTTSTEIN,this.paintInformation);
        writer.writeAttribute(Tags.UTF8CODEPOINT,this.unicodeCodePage);
        writer.writeAttribute(Tags.MEZL,this.mezlNumber);
            writer.writeAttribute(Tags.ABZL,this.aBzlNumber);
        writer.writeAttribute(Tags.MEANING.toString(),this.meaning);
            if(statistics) {
                writer.writeAttribute(Tags.ABSOCC.toString(), CuneiImportHandler.formatDouble(this.occurances));
                writer.writeAttribute(Tags.RELOCC.toString(), CuneiImportHandler.formatDouble(this.relativeoccurance));
                writer.writeAttribute(Tags.BEGIN.toString(), this.beginoccurance.toString());
                writer.writeAttribute(Tags.MIDDLE.toString(), this.middleoccurance.toString());
                writer.writeAttribute(Tags.END.toString(), this.endoccurance.toString());
                writer.writeAttribute(Tags.SINGLE.toString(), this.singleoccurance.toString());
                writer.writeAttribute(Tags.LEFTACCVAR, this.leftaccessorvariety.toString());
                writer.writeAttribute(Tags.RIGHTACCVAR, this.rightaccessorvariety.toString());
            }
                writer.writeAttribute(Tags.ISNUMBERCHAR,this.isNumberChar.toString());
        writer.writeAttribute(Tags.SUMEROGRAM,this.sumerogram.toString());
        writer.writeAttribute(Tags.STEM.toString(),this.stem);
        writer.flush();
        //System.out.println("toXML Transliterations: "+this.transliterations.toString());
        for(Transliteration akkadtrans:this.transliterations.keySet()){
            writer.writeCharacters(System.lineSeparator()+akkadtrans.toXML(statistics));
        }
        writer.flush();
        //System.out.println("toXML Translations: "+this.translations.toString());
        for(String locale:this.translations.keySet()){
            for(Translation trans:this.translations.get(locale).keySet()){
                writer.writeCharacters(System.lineSeparator()+trans.toXML());
            }
        }
        writer.flush();
        for(POSTag pos:this.postags){
            writer.writeCharacters(System.lineSeparator()+pos.toXML());
        }
        writer.flush();
            if(statistics) {
                Map<String, Preceding> precedingwords = this.precedingChars;
                for (Preceding akkadfollow : precedingChars.values()) {
                    writer.writeCharacters(System.lineSeparator() + akkadfollow.toXML());
                }
                Map<String, Following> followingwords = this.getFollowingWords();
                for (Following akkadfollow : followingwords.values()) {
                    writer.writeCharacters(System.lineSeparator() + akkadfollow.toXML());
                }
            }
        writer.writeCharacters(System.lineSeparator()+this.character);
        writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println("ToString: "+strwriter.toString());
        return strwriter.toString();
    }

    public void setHethzlNumber(String hethzlNumber) {
        this.hethzlNumber = hethzlNumber;
    }
}

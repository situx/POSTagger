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

package com.github.situx.postagger.main.gui.ime.jquery.tree;


import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import com.github.situx.postagger.util.enums.util.Tags;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Implementation of an IMETree.
 */
public class IMETree implements Comparable<IMETree>{

    private Map<Integer,Map<String,Set<IMETree>>> cachewords;
    private List<IMETree> children;
    private Integer frequency;
    private Integer length;
    private Boolean isWord;
    private String word,chars;
    private String meaning;
    private String translation;
    private String postag;
    private String concept;

    public IMETree(String word,String chars,Integer frequency){
        this.children=new LinkedList<>();
        this.cachewords=new TreeMap<>();
        this.isWord=true;
        this.word=word;
        this.chars=chars;
        this.frequency=frequency;
        this.length=0;
    }

    public IMETree(String word,String meaning,String translation, String postag,String concept){
        this.chars=word;
        this.word=word;
        this.concept=concept;
        this.meaning=meaning;
        this.translation=translation;
        this.postag=postag;
        this.frequency=0;
        this.length=0;
    }

    public IMETree(){
        this.children=new LinkedList<>();
        this.cachewords=new TreeMap<>();
        this.isWord=false;
        this.word="";
        this.chars="";
        this.frequency=0;
        this.length=0;
    }

    public void addChild(IMETree child){
        this.children.add(child);
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @Override
    public int compareTo(final IMETree imeTree) {
        return chars.compareTo(imeTree.chars);
    }

    public IMETree containsChild(String nodevalue){
        for(IMETree tree:this.children){
            if(tree.word.equals(nodevalue)){
                return tree;
            }
        }
        return null;
    }

    public final Map<Integer,Map<String, Set<IMETree>>> getCachewords() {
        return cachewords;
    }

    public final void setCachewords(final Map<Integer,Map<String, Set<IMETree>>> cachewords) {
        this.cachewords = cachewords;
    }

	public String getChars() {
        return chars;
    }

	public void setChars(String chars) {
		this.chars = chars;
	}

    public List<IMETree> getChildren(){
        return this.children;
    }

    public void setChildren(final List<IMETree> children) {
        this.children = children;
    }

    public Integer getFrequency() {
		return frequency;
	}

	public void setFrequency(Integer frequency) {
		this.frequency = frequency;
	}

    public Boolean getIsWord() {
        return isWord;
    }

    public void setIsWord(final Boolean isWord) {
        this.isWord = isWord;
    }

    public String getWord() {
        return word;
    }

    public void setWord(final String word) {
        this.word = word;
    }

    public Boolean hasChildren(){
        return !this.children.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("Nodevalue: ");
        builder.append(word);
        builder.append(" Chars: ");
        builder.append(chars);
        builder.append(" IsWord: ");
        builder.append(isWord);
        builder.append(" Freq: ");
        builder.append(frequency);
        builder.append(" Children: ");
        builder.append(this.children.size());
        builder.append(" ");
        builder.append(this.cachewords.toString());
        return builder.toString();
    }

    public String toXML(){
        StringWriter strwriter;
        XMLOutputFactory output3;
        XMLStreamWriter writer;
        strwriter=new StringWriter();
        output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            for(Integer key:this.cachewords.keySet()){
                for(String innerkey:this.cachewords.get(key).keySet()){
                    for(IMETree form:this.cachewords.get(key).get(innerkey)) {
                        writer.writeStartElement(Tags.CANDIDATES);
                        writer.writeAttribute("isWord",((Boolean)!form.word.matches("[A-Z]")).toString());
                        writer.writeAttribute(Tags.FREQ,form.frequency.toString());
                        writer.writeAttribute(Tags.MEANING.toString(),form.meaning.toString());
                        writer.writeAttribute(Tags.POSTAG,form.postag);
                        writer.writeAttribute(Tags.CONCEPT,form.concept);
                        writer.writeAttribute(Tags.TRANSLATION,form.translation);
                        writer.writeAttribute(Tags.TRANSLIT,innerkey);
                        writer.writeAttribute(Tags.CHARS,form.word);
                        writer.writeEndElement();
                        writer.writeCharacters(System.lineSeparator());
                    }

                }

            }

        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strwriter.toString();
    }

    public String toJSON(){
        StringWriter writer=new StringWriter();
        writer.write("\""+word+"\":["+System.lineSeparator());
        Iterator<Integer> freqiter= this.cachewords.keySet().iterator();
        for(;freqiter.hasNext();){
            Integer key=freqiter.next();
            Iterator<String> outeriter=this.cachewords.get(key).keySet().iterator();
            for(;outeriter.hasNext();){
                String innerkey= outeriter.next();

                Iterator<IMETree> iter=this.cachewords.get(key).get(innerkey).iterator();
                for(;iter.hasNext();) {
                    IMETree form=iter.next();
                    writer.write("{ \""+Tags.FREQ+"\":\""+form.frequency.toString()+"\", ");
                    writer.write("\""+Tags.MEANING+"\":\""+form.meaning+"\", ");
                    writer.write("\""+Tags.TRANSLATION+"\":\""+form.translation+"\", ");
                    writer.write("\""+Tags.CONCEPT+"\":\""+form.concept+"\", ");
                    writer.write("\""+Tags.POSTAG+"\":\""+form.postag+"\", ");
                    writer.write("\""+Tags.CHARS+"\":\""+ CuneiDictHandler.reformatToASCIITranscription2(form.word)+"\", ");
                    writer.write("\""+Tags.LENGTH+"\": "+ this.length+" }");
                    if(iter.hasNext() || outeriter.hasNext() || freqiter.hasNext()){
                        writer.write(",");
                    }
                    writer.write(System.lineSeparator());
                }

            }
        }
        writer.write("],");
            /*if(!outeriter.hasNext()){
                writer.write(",");
            }*/
        writer.write(System.lineSeparator());
        return writer.toString();
        /*JSONObject xmlJSONObj = XML.toJSONObject(this.toXML());
        return xmlJSONObj.toString(4);*/
    }

    public String toJSObject(){
        return "var tree="+this.toJSON();
    }

    public void setPostag(String postag) {
        this.postag = postag;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public void setLength(int length) {
        this.length = length;
    }
}

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

package com.github.situx.postagger.dict.utils;

import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by timo on 15.08.14.
 */
public class StopChar implements Comparable<StopChar>{
    /**Absolute occurance of this stopchar.*/
    private Double absoluteOccurance;
    /**The following map of this stopchar.*/
    private Map<String,Double> following;
    /**The left accessor variety of this stopchar.*/
    private Double leftaccessorvariety;
    /**The preceding map of this stopchar.*/
    private Map<String,Double> preceding;
    /**The relative occuracne of this stopchar.*/
    private Double relativeOccurance;
    /**The right accessor variety of this stopchar.*/
    private Double rightaccessorvariety;
    /**The string of this stopchar.*/
    private String stopchar;

    /**
     * Constructor for this class.
     */
    public StopChar(){
        this.preceding=new TreeMap<>();
        this.following=new TreeMap<>();
        this.relativeOccurance=0.;
        this.absoluteOccurance=0.;
        this.leftaccessorvariety=0.;
        this.rightaccessorvariety=0.;
        this.stopchar="";
    }

    /**
     * Adds a following word to this stopchar.
     * @param following the word to add
     */
    public void addFollowing(final String following){
        if(!this.following.containsKey(following)){
            this.following.put(following,0.);
        }
        this.following.put(following,this.following.get(following)+1);
    }

    /**
     * Adds a precding word to this stopchar.
     * @param preceding the preceding word to add
     */
    public void addPreceding(final String preceding){
        if(!this.preceding.containsKey(preceding)){
            this.preceding.put(preceding,0.);
        }
        this.preceding.put(preceding,this.preceding.get(preceding)+1);
    }

    /**
     * Calculates the left punctuationvariety for this stopchar.
     * @param charType the chartype to consider
     */
    public void calculateLeftPunctuationVariety(CharTypes charType){
        Double leftaccessor=0.;
        Set<String> foundchars=new TreeSet<String>();
        for(String preceding:this.preceding.keySet()){
            if(preceding.length()>=charType.getChar_length() && !foundchars.contains(preceding.substring(0, charType.getChar_length()))){
                foundchars.add(preceding.substring(0,charType.getChar_length()));
                leftaccessor++;
            }
        }
        this.leftaccessorvariety=leftaccessor;
    }

    /**
     * Calculates the right punctuation variety of this stopchar.
     * @param charType the chartype to consider
     */
    public void calculateRightPunctuationVariety(CharTypes charType){
        Double rightaccessor=0.;
        Set<String> foundchars=new TreeSet<>();
        for(String following:this.following.keySet()){
            System.out.println("Following: "+following);
            if(following.length()>=charType.getChar_length() && !foundchars.contains(following.substring(0,charType.getChar_length()))){
                foundchars.add(following.substring(0,charType.getChar_length()));
                rightaccessor++;
            }
        }
        this.rightaccessorvariety=rightaccessor;
    }

    @Override
    public int compareTo(final StopChar stopChar) {
        return this.stopchar.compareTo(stopChar.stopchar);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof StopChar){
            return this.stopchar.equals(((StopChar)obj).stopchar);
        }
        return false;
    }

    /**
     * Gets the absolute occurance of this stopchar.
     * @return  the occurance as Double
     */
    public Double getAbsoluteOccurance() {
        return absoluteOccurance;
    }

    /**
     * Sets the absolute occurance of this stopchar.
     * @param absoluteOccurance the occurance as Double
     */
    public void setAbsoluteOccurance(final Double absoluteOccurance) {
        this.absoluteOccurance = absoluteOccurance;
    }

    /**
     * Gets the following char map of this stopchar.
     * @return the following char map
     */
    public Map<String, Double> getFollowing() {
        return following;
    }

    /**
     * Sets the following map of this character.
     * @param following the map to set
     */
    public void setFollowing(final Map<String, Double> following) {
        this.following = following;
    }

    /**
     * Gets the left accessorvariety of this stopchar.
     * @return the left accessorvariety as Double
     */
    public Double getLeftaccessorvariety() {
        return leftaccessorvariety;
    }

    /**
     * Sets the left accessorvariety of this stopchar.
     * @param leftaccessorvariety the left accessorvariety to set
     */
    public void setLeftaccessorvariety(final Double leftaccessorvariety) {
        this.leftaccessorvariety = leftaccessorvariety;
    }

    /**
     * Gets the precedingmap of this stopchar.
     * @return the precedingmap
     */
    public Map<String, Double> getPreceding() {
        return preceding;
    }

    /**
     * Sets the precedingmap of this stopchar.
     * @param preceding the precedingmap to set
     */
    public void setPreceding(final Map<String, Double> preceding) {
        this.preceding = preceding;
    }

    /**
     * Gets the relative occurance of this stopchar.
     * @return  the relative occurance as Double
     */
    public Double getRelativeOccurance() {
        return relativeOccurance;
    }

    /**
     * Sets the relative occurance of this stopchar.
     * @param relativeOccurance the relative occurance to set
     */
    public void setRelativeOccurance(final Double relativeOccurance) {
        this.relativeOccurance = relativeOccurance;
    }

    /**
     * Gets the right accessor variety.
     * @return the right accessor variety as Double
     */
    public Double getRightaccessorvariety() {
        return rightaccessorvariety;
    }

    /**
     * Sets the right accessor variety.
     * @param rightaccessorvariety the right accessor variety to set
     */
    public void setRightaccessorvariety(final Double rightaccessorvariety) {
        this.rightaccessorvariety = rightaccessorvariety;
    }

    /**
     * Gets the stopchar.
     * @return  the stopchar as String
     */
    public String getStopchar() {
        return stopchar;
    }

    /**
     * Sets the stopchar string.
     * @param stopchar the string to set
     */
    public void setStopchar(final String stopchar) {
        this.stopchar = stopchar;
    }

    @Override
    public String toString() {
        return this.stopchar;
    }

    /**
     * Gets an xml representation of this Stopchar.
     * @return the xml representation as String
     */
    public String toXML(){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartElement(Tags.STOPCHAR);
            writer.writeAttribute(Tags.LEFTACCVAR,this.leftaccessorvariety.toString());
            writer.writeAttribute(Tags.RIGHTACCVAR,this.rightaccessorvariety.toString());
            for(String preceding:this.preceding.keySet()){
                writer.writeStartElement(Tags.PRECEDING);
                writer.writeAttribute(Tags.ABSOCC.toString(),this.preceding.get(preceding).toString());
                writer.writeCharacters(System.lineSeparator()+preceding);
                writer.writeEndElement();
            }
            for(String following:this.following.keySet()){
                writer.writeStartElement(Tags.FOLLOWING);
                writer.writeAttribute(Tags.ABSOCC.toString(),this.following.get(following).toString());
                writer.writeCharacters(System.lineSeparator()+following);
                writer.writeEndElement();
            }
            writer.writeCharacters(this.stopchar);
            writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strwriter.toString();
    }
}

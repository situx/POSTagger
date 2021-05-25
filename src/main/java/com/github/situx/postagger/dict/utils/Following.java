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

import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.enums.util.Tags;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class symbolising a following word and a preceding word/character.
 */
public class Following implements Comparable<Following>{
    /**Map of following characters with a boundary and without a boundary.*/
    private Tuple<Double,Double> following;
    /**The String of the following word.*/
    private String followingstr;
    /**Indicates if this followingchar is a stopchar.*/
    private Boolean isStopChar;
    /**Map of precedingchars if the following char is this char.*/
    private Map<String,Tuple<Double,Double>> preceding;

    /**
     * Constructor for this class.
     */
    public Following(){
        this.preceding=new TreeMap<>();
        this.following=new Tuple<>(0.,0.);
    }

    /**
     * Constructor for this class.
     * @param following  frequencies for this Following
     * @param followingstr the following word string
     * @param preceding the preceding string while adding this following
     * @param isStopChar stopchar indicator
     * @param separator indicates if a separator is present
     */
    public Following(final Tuple<Double,Double> following,final String followingstr,final String preceding,final Boolean isStopChar,final Boolean separator){
        this.preceding=new TreeMap<>();
        this.following=following;
        if(separator){
            this.preceding.put(preceding,new Tuple<>(0.,1.));
        }else{
            this.preceding.put(preceding,new Tuple<>(1.,0.));
        }
        this.isStopChar=isStopChar;
        this.followingstr=followingstr;
    }

    /**
     * Adds a preceding char to this following char.
     * @param precedingcollector the preceding char
     * @param precedingtemp  preceding frequency without borders
     * @param precedingtemp2 preceding frequency with borders
     */
    public void addPreceding(final String precedingcollector, final Double precedingtemp, final Double precedingtemp2) {
        this.preceding.put(precedingcollector,new Tuple<Double, Double>(precedingtemp,precedingtemp2));
    }

    @Override
    public int compareTo(final Following following) {
        return this.followingstr.compareTo(following.followingstr);
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof Following){
           Following fol=(Following)obj;
           if(following.getOne().equals(fol.following.getOne()) && following.getTwo().equals(fol.following.getTwo())
                   && preceding.equals(fol.preceding)
                   && isStopChar.equals(fol.isStopChar)){
               return true;
           }
        }
        return false;
    }

    /**
     * Gets the following frequencies.
     * @return the following frequencies as Tuple<Double,Double>
     */
    public Tuple<Double,Double> getFollowing() {
        return following;
    }

    /**
     * Gets the following string.
     * @return the following string
     */
    public String getFollowingstr() {
        return followingstr;
    }

    /**
     * Sets the followingstring.
     * @param followingstr the following word string to set
     */
    public void setFollowingstr(final String followingstr) {
        this.followingstr = followingstr;
    }

    /**
     * Indicates if this following char is a stopchar.
     * @return true if it is false otherwise
     */
    public Boolean getIsStopChar() {
        return isStopChar;
    }

    /**
     * Sets if this following is a stopchar.
     * @param isStopChar stopchar indicator
     */
    public void setIsStopChar(final Boolean isStopChar) {
        this.isStopChar = isStopChar;
    }

    /**
     * Gets the precedingmap of this following.
     * @return the precedingmap
     */
    public Map<String, Tuple<Double,Double>> getPreceding() {
        return preceding;
    }

    /**
     * Sets the precedingmap of this following.
     * @param preceding the precedingmap to set
     */
    public void setPreceding(final Map<String, Tuple<Double,Double>> preceding) {
        this.preceding = preceding;
    }

    /**
     * Increments the frequencies of following.
     * @param border with or without border
     */
    public void incFollowing(final Boolean border){
        if(border){
            this.following.setTwo(this.following.getTwo()+1);
        }else{
            this.following.setOne(this.following.getOne() + 1);
        }
    }

    /**
     * Increments the frequencies of the precedingmap.
     * @param preceding which preceding string
     * @param boundary with or without border
     */
    public void incPreceding(final String preceding,final Boolean boundary) {
        if(!this.preceding.containsKey(preceding)){
            this.preceding.put(preceding,new Tuple<>(1.,0.));
        }
        if(boundary){
            this.preceding.get(preceding).setTwo(this.preceding.get(preceding).getTwo()+1);
        }else{
            this.preceding.get(preceding).setOne(this.preceding.get(preceding).getOne()+1);
        }
    }

    /**
     * Sets the following frequency map-
     * @param following with border
     * @param following2 without border
     */
    public void setFollowing(final Double following,Double following2) {
        this.following = new Tuple<>(following,following2);
    }

    @Override
    public String toString() {
        return this.preceding+" "+this.following;
    }

    /**
     * Creates a xml representation of this following.
     * @return the xml representation as String
     */
    public String toXML(){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartElement(Tags.FOLLOWING);
            writer.writeAttribute(Tags.ABSOCC.toString(), this.following.getOne().toString());
            writer.writeAttribute(Tags.ABSBD.toString(), this.following.getTwo().toString());
            for(String key:this.preceding.keySet()){
                writer.writeStartElement(Tags.PRECEDING);
                writer.writeAttribute(Tags.PRECEDING, key);
                writer.writeAttribute(Tags.ABSOCC.toString(),this.preceding.get(key).getOne().toString());
                writer.writeAttribute(Tags.ABSBD.toString(),this.preceding.get(key).getTwo().toString());
                writer.writeEndElement();
            }
            writer.writeCharacters("\n"+this.followingstr);
            writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strwriter.toString();
    }
}

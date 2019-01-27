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

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.util.enums.pos.Case;
import com.github.situx.postagger.util.enums.pos.POSTags;
import com.github.situx.postagger.util.enums.pos.PersonNumberCases;
import com.github.situx.postagger.util.enums.util.Tags;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

/**
 * Created by timo on 03.07.14.
 */
public class POSTag implements Comparable<POSTag>{

    private PersonNumberCases personnumbercase=PersonNumberCases.NONE;
    private Case poscase=Case.NOMINATIVE_DUAL;

    public String getConceptURI() {
        return conceptURI;
    }

    public void setConceptURI(String conceptURI) {
        this.conceptURI = conceptURI;
    }

    private POSTags postag=POSTags.NOUN;
    private String postagstr="";
    private String conceptURI="";

    /**
     * Constructor for this class.
     * @param postag the postag
     * @param personnumbercase the grammar case
     * @param poscase the case of the postag
     */
    public POSTag(final POSTags postag,final PersonNumberCases personnumbercase,final Case poscase){
        this.postag=postag;
        this.personnumbercase=personnumbercase;
        this.poscase=poscase;
    }

    /**
     * Constructor for this class.
     * @param postag the postag
     * @param poscase the case of the postag
     */
    public POSTag(final POSTags postag,final Case poscase){
        this.postag=postag;
        this.poscase=poscase;
        this.personnumbercase=PersonNumberCases.NONE;
    }

    /**
     * Constructor for this class.
     * @param postag  the postag
     */
    public POSTag(final String postag){
        this.postagstr=postag;
        this.personnumbercase=PersonNumberCases.NONE;
    }

    @Override
    public int compareTo(final POSTag posTag) {
        return this.postagstr.compareTo(posTag.postagstr);
    }

    /**
     * Gets the poscase of this postag.
     * @return the poscase
     */
    public PersonNumberCases getPoscase() {
        return personnumbercase;
    }

    /**
     * Sets the poscase of this postag.
     * @param personnumbercase the poscase to set
     */
    public void setPoscase(final PersonNumberCases personnumbercase) {
        this.personnumbercase = personnumbercase;
    }

    /**
     * Gets the postag type.
     * @return the postag type to get
     */
    public POSTags getPostag() {
        return postag;
    }

    /**
     * Sets the postag type.
     * @param postag the postag type to set
     */
    public void setPostag(final POSTags postag) {
        this.postag = postag;
    }

    @Override
    public String toString() {
        return this.postagstr;
    }

    /**
     * Creates an xml representation of this postag.
     * @return the xml string
     */
    public String toXML(){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartElement(Tags.POSTAG);
            writer.writeAttribute(Tags.URI,this.conceptURI);
            writer.writeAttribute(Tags.POSCASE, this.poscase.toString());
            writer.writeAttribute(Tags.POSTAG, this.postagstr);
            writer.writeAttribute(Tags.PERSONNUMBERCASE.toString(), this.personnumbercase.toString());
            writer.flush();
            writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strwriter.toString();
    }

    /**
     * Creates an xml representation of this postag.
     * @return the xml string
     */
    public String toXMLForDict(){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartElement(Tags.POSTAG);
            writer.writeAttribute(Tags.URI,this.conceptURI);
            writer.writeCharacters(this.postagstr);
            writer.flush();
            writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strwriter.toString();
    }
}

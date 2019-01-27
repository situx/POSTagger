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
import com.github.situx.postagger.util.enums.util.Tags;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

/**
 * Created by timo on 24.04.17 .
 */
public class MorphPattern implements Comparable<MorphPattern> {

    public String pattern="";

    public String gender;

    public String postag="";

    public String representation="";

    public String mood;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MorphPattern that = (MorphPattern) o;

        if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) return false;
        if (gender != null ? !gender.equals(that.gender) : that.gender != null) return false;
        if (postag != null ? !postag.equals(that.postag) : that.postag != null) return false;
        if (representation != null ? !representation.equals(that.representation) : that.representation != null)
            return false;
        if (mood != null ? !mood.equals(that.mood) : that.mood != null) return false;
        if (voice != null ? !voice.equals(that.voice) : that.voice != null) return false;
        if (person != null ? !person.equals(that.person) : that.person != null) return false;
        if (animacy != null ? !animacy.equals(that.animacy) : that.animacy != null) return false;
        if (transprefix != null ? !transprefix.equals(that.transprefix) : that.transprefix != null) return false;
        if (wordcase != null ? !wordcase.equals(that.wordcase) : that.wordcase != null) return false;
        if (number != null ? !number.equals(that.number) : that.number != null) return false;
        if (tense != null ? !tense.equals(that.tense) : that.tense != null) return false;
        return transsuffix != null ? transsuffix.equals(that.transsuffix) : that.transsuffix == null;
    }

    @Override
    public int hashCode() {
        int result = pattern != null ? pattern.hashCode() : 0;
        result = 31 * result + (gender != null ? gender.hashCode() : 0);
        result = 31 * result + (postag != null ? postag.hashCode() : 0);
        result = 31 * result + (representation != null ? representation.hashCode() : 0);
        result = 31 * result + (mood != null ? mood.hashCode() : 0);
        result = 31 * result + (voice != null ? voice.hashCode() : 0);
        result = 31 * result + (person != null ? person.hashCode() : 0);
        result = 31 * result + (animacy != null ? animacy.hashCode() : 0);
        result = 31 * result + (transprefix != null ? transprefix.hashCode() : 0);
        result = 31 * result + (wordcase != null ? wordcase.hashCode() : 0);
        result = 31 * result + (number != null ? number.hashCode() : 0);
        result = 31 * result + (tense != null ? tense.hashCode() : 0);
        result = 31 * result + (transsuffix != null ? transsuffix.hashCode() : 0);
        return result;
    }

    public String voice;

    public String person;

    public String animacy;

    public String transprefix="";

    public String wordcase;

    public String number;

    public String tense;

    public String transsuffix="";


    @Override
    public String toString() {
        return "MorphPattern{" +
                "pattern='" + pattern + '\'' +
                ", gender='" + gender + '\'' +
                ", postag='" + postag + '\'' +
                ", representation='" + representation + '\'' +
                ", person='" + person + '\'' +
                ", animacy='" + animacy + '\'' +
                ", transprefix='" + transprefix + '\'' +
                ", wordcase='" + wordcase + '\'' +
                ", number='" + number + '\'' +
                ", transsuffix='" + transsuffix + '\'' +
                '}';
    }

    public String toXML(){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartElement(Tags.PATTERN);
            writer.writeAttribute(Tags.TENSE,this.tense);
            writer.writeAttribute(Tags.MOOD,this.mood);
            writer.writeAttribute(Tags.POSTAG,this.postag);
            writer.writeAttribute(Tags.WORDCASE,this.wordcase);
            if(voice!=null){
                writer.writeAttribute(Tags.VOICE,this.voice);
            }

            writer.writeAttribute(Tags.PERSON,this.person);
            writer.writeAttribute(Tags.NUMBER,this.number);
            writer.writeAttribute(Tags.ANIMACY,this.animacy);
            writer.writeAttribute(Tags.PATTERN,this.pattern);
            writer.writeAttribute(Tags.REPRESENTATION, this.representation);
            writer.writeAttribute(Tags.TRANSPREFIX, this.transprefix);
            writer.writeAttribute(Tags.TRANSSUFFIX, this.transsuffix);
            writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strwriter.toString();
    }

    @Override
    public int compareTo(MorphPattern o) {
        return gender.compareTo(o.gender)+postag.compareTo(o.postag)+representation.compareTo(o.representation)+person.compareTo(o.person)+wordcase.compareTo(o.wordcase)+number.compareTo(o.number)+animacy.compareTo(o.animacy);
    }
}

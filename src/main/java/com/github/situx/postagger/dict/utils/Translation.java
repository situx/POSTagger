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
import java.util.Locale;

/**
 * Represents as translation.
 */
public class Translation implements Comparable<Translation> {
    /**The locale of this Translation.*/
    private Locale locale;
    /**The occurance of this translation.*/
    private Integer occurance;
    /**The translation string.*/
    private String translation;

    /**
     * Constructor for this class.
     * @param translation the translation String
     * @param locale the locale of the translation
     */
    public Translation(final String translation,final Locale locale){
         this.translation=translation;
        this.locale=locale;

    }

    @Override
    public int compareTo(final Translation translation) {
        return this.translation.compareTo(translation.translation);
    }

    /**
     * Gets the locale.
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale of this translation.
     * @param locale the locale of the translation
     */
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Gets the occurance of this translation.
     * @return the occurance as Integer
     */
    public Integer getOccurance() {
        return occurance;
    }

    /**
     * Sets the occurance of the translation.
     * @param occurance the occurance to set
     */
    public void setOccurance(final Integer occurance) {
        this.occurance = occurance;
    }

    /**
     * Gets the translation String.
     * @return the translation String
     */
    public String getTranslation() {
        return translation;
    }

    /**
     * Sets the translation String.
     * @param translation the translation String
     */
    public void setTranslation(final String translation) {
        this.translation = translation;
    }

    @Override
    public String toString() {
        return this.translation;
    }

    /**
     * Gets the xml representation of this translation.
     * @return the xml String
     */
    public String toXML(){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartElement(Tags.TRANSLATION);
            writer.writeAttribute(Tags.LOCALE.toString(), this.locale.toString());
            writer.writeCharacters(this.translation);
            writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strwriter.toString();
    }
}

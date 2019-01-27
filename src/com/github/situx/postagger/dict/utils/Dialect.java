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
 * Created by timo on 04.05.17 .
 */
public class Dialect implements Comparable<Dialect> {

    String name;

    String uri;

    public Dialect(String name, String uri) {
        this.name=name;
        this.uri=uri;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dialect dialect = (Dialect) o;

        if (name != null ? !name.equals(dialect.name) : dialect.name != null) return false;
        return uri != null ? uri.equals(dialect.uri) : dialect.uri == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Dialect o) {
        return this.name.compareTo(o.name)+this.uri.compareTo(o.uri);

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
            writer.writeStartElement(Tags.DIALECT);
            writer.writeAttribute(Tags.URI, this.uri);
            writer.writeAttribute(Tags.NAME, this.name);
            writer.writeCharacters(this.name);
            writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strwriter.toString();
    }
}

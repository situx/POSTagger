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

import com.github.situx.postagger.util.enums.util.Tags;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by timo on 04.05.17 .
 */
public class Epoch implements Comparable<Epoch> {

    public Epoch(String name,String epochuri){
        this.name=name;
        this.uri=epochuri;
    }


    public Epoch(String name,String epochuri,Integer startyear,Integer endyear,String locationuri){
        this.name=name;
        this.uri=epochuri;
        Calendar calendar = GregorianCalendar.getInstance();
        if(startyear<0){
            calendar.set(Calendar.ERA,GregorianCalendar.BC);
        }
        calendar.roll(Calendar.YEAR, startyear);
        this.start=calendar.getTime();
        if(this.start==null){
            this.start=new Date(System.currentTimeMillis());
        }
        calendar = GregorianCalendar.getInstance();
        if(endyear<0){
            calendar.set(Calendar.ERA,GregorianCalendar.BC);
        }
        calendar.roll(Calendar.YEAR, endyear);
        this.end=calendar.getTime();
        if(this.end==null){
            this.end=new Date(System.currentTimeMillis());
        }
        this.mainlocation=locationuri;
    }

    String mainlocation;

    String name;

    String uri;

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

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    Date start;

    Date end;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Epoch epoch = (Epoch) o;

        if (mainlocation != null ? !mainlocation.equals(epoch.mainlocation) : epoch.mainlocation != null) return false;
        if (name != null ? !name.equals(epoch.name) : epoch.name != null) return false;
        if (uri != null ? !uri.equals(epoch.uri) : epoch.uri != null) return false;
        if (start != null ? !start.equals(epoch.start) : epoch.start != null) return false;
        return end != null ? end.equals(epoch.end) : epoch.end == null;
    }

    @Override
    public int hashCode() {
        int result = mainlocation != null ? mainlocation.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Epoch o) {
        return this.name.compareTo(o.name)+this.uri.compareTo(o.uri)+this.start.compareTo(o.start)+this.end.compareTo(o.end)+this.mainlocation.compareTo(o.mainlocation);

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
            writer.writeStartElement(Tags.EPOCH);
            writer.writeAttribute(Tags.START, this.start.getYear()+"");
            writer.writeAttribute(Tags.END.toString(), this.end.getYear()+"");
            writer.writeAttribute(Tags.MAINLOCATION, this.mainlocation);
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

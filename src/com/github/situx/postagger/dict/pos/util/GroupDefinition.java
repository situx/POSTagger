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

package com.github.situx.postagger.dict.pos.util;

import com.google.re2j.Pattern;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.util.enums.util.Tags;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;

/**
 * Created by timo on 11.10.14.
 */
public class GroupDefinition {

    private Pattern regex;

    private String equals;

    private String name;

    private String uri;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    private String groupCase;

    private String tag;

    private Integer group;


    public void setRegex(final Pattern regex) {
        this.regex = regex;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(final Integer group) {
        this.group = group;
    }

    private String value;

    public String getGroupCase() {
        return groupCase;
    }

    public void setGroupCase(final String groupCase) {
        this.groupCase = groupCase;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(final String tag) {
        this.tag = tag;
    }

    public GroupDefinition(final String regex, final String equals, final String uri,final String description,final String groupCase,final String value,final String tag, final Integer group) {
        if(regex!=null)
            this.regex = Pattern.compile(regex);
        else
            this.regex=Pattern.compile(".*");
        if(equals!=null)
            this.equals = equals;
        else
            this.equals="";
        this.uri=uri;
        this.name = description;
        if(groupCase==null){
            this.groupCase="";
        }else{
            this.groupCase=groupCase;
        }
        this.value=value;
        this.group=group;
        this.tag=tag;

    }

    public Pattern getRegex() {

        return regex;
    }

    public void setRegex(final String regex) {
        this.regex = Pattern.compile(regex);
    }

    public String getEquals() {
        return equals;
    }

    public void setEquals(final String equals) {
        this.equals = equals;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String toXML(){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartElement("group");
            writer.writeAttribute("name",this.name);
            writer.writeAttribute("tag",this.tag);
            writer.writeAttribute("uri",this.uri);
            writer.writeAttribute("regex",this.regex.toString());
            writer.writeAttribute("group",this.group.toString());
            writer.writeAttribute("case",this.groupCase);
            writer.writeAttribute("value",this.value);
            writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println("ToString: "+strwriter.toString());
        return strwriter.toString();

    }
}

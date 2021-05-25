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

package com.github.situx.postagger.main.gui.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import java.util.TreeMap;

/**
 * Created by timo on 11.05.17 .
 */
public class MapHandler extends DefaultHandler2 {

    public java.util.Map<String,String> resultmap;

    public String tag1;

    public String tag2;

    public MapHandler(){
        this.resultmap=new TreeMap<>();
        this.tag1="origvalue";
        this.tag2="concept";
    }

    public MapHandler(String tag1,String tag2){
        this.resultmap=new TreeMap<>();
        this.tag1=tag1;
        this.tag2=tag2;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch(qName){
            case "translation":
                resultmap.put(attributes.getValue(tag1),attributes.getValue(tag2));
                break;
        }
    }
}

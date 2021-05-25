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

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by timo on 07.05.17 .
 */
public class RelationParser extends DefaultHandler2 {

    public Map<String,Map<String,String>> relations=new TreeMap<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch(qName){
            case "relation":
                if(!relations.containsKey(attributes.getValue("akk"))){
                    relations.put(attributes.getValue("akk"),new TreeMap<>());
                }
                relations.get(attributes.getValue("akk")).put(attributes.getValue("sum"),attributes.getValue("translation"));
                //System.out.println(relations.keySet());
        }
    }
}

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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Created by timo on 13.06.17
 * POSTagFeatureGenerator after Ratnaparkhi 96
 */
public class POSTagFeatureGenerator extends DefaultHandler2 {

    public String lastTag;

    public StringBuilder builder=new StringBuilder();

    public java.util.Map<String,Integer> posToIntMap;


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        switch (qName){
            case "pos":
                if(lastTag!=null){
                    builder.append("");
                }else{

                }
                lastTag=attributes.getValue("pos");
                builder.append(","+attributes.getValue("pos"));
                builder.append(System.lineSeparator());
                break;
        }
    }
}

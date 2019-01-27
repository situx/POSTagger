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

package com.github.situx.postagger.dict.corpusimport.util;

import com.github.situx.postagger.util.enums.util.Tags;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;

/**
 * Created by timo on 14.09.14.
 */
public class CNXMLToSet extends DefaultHandler2 {

    private StringBuilder result=new StringBuilder();

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        this.result.append(new String(ch,start,length));
    }

    public String convert(final String file) throws ParserConfigurationException, SAXException, IOException {
        this.result.delete(0,result.length());
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File("beeeep")));
        writer.write(file);
        writer.close();
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        SAXParserFactory.newInstance().newSAXParser().parse(new InputSource(new StringReader(file)),this);
        return this.result.toString();
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if(qName.equals(Tags.S)){
            result.append(System.lineSeparator());
        }
        if(qName.equals(Tags.C)){
            result.append(System.lineSeparator());
        }
    }

    public String getResult() {
        return result.toString();
    }
}

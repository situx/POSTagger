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

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.util.enums.util.Tags;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;

/**
 * Created by timo on 6/3/15.
 */
public class PDFExtractor {

    public static void main(String[] args) throws IOException, XMLStreamException {
        BufferedReader reader=new BufferedReader(new FileReader(new File("logogramme.txt")));
        BufferedWriter strwriter=new BufferedWriter(new FileWriter(new File("logexport.txt")));
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
        writer.writeStartDocument();
        writer.writeStartElement("data");
        String temp;
        String[] tempelems,refs;
        while((temp=reader.readLine())!=null){
            if(!temp.contains("\\") || StringUtils.countMatches(temp,"\\")==1)
                continue;
            System.out.println("Temp: "+temp);
            tempelems=temp.split("\\\\");
            writer.writeStartElement("map");
            writer.writeAttribute("sum", WordUtils.capitalizeFully(tempelems[0]));
            writer.writeAttribute("cunei","");
            writer.writeAttribute("akk", tempelems[1]);
            writer.writeAttribute("trans",tempelems[2]);
            if(tempelems.length>3) {
                refs = tempelems[3].split("[ ][A-Z]");
                boolean first=true;
                for (String ref : refs) {
                    if(first){
                        writer.writeStartElement("ref");
                    }
                    writer.writeCharacters(ref);
                    if(!first){
                        writer.writeEndElement();
                    }
                    first=!first;
                }
                if(!first){
                    writer.writeEndElement();
                }
            }
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeEndDocument();
        reader.close();
        writer.close();
    }
}

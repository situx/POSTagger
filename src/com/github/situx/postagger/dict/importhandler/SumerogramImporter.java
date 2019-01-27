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

package com.github.situx.postagger.dict.importhandler;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by timo on 01.02.16.
 */
public class SumerogramImporter extends  ImportHandler{
    @Override
    public String reformatToASCIITranscription(String transcription) {
        return null;
    }

    @Override
    public String reformatToUnicodeTranscription(String transcription) {
        return null;
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        switch(qName){
            case "akk":

        }
    }

    public static void main(String[] args) throws IOException {
        File file=new File("corpus.atf");
        BufferedReader reader=new BufferedReader(new FileReader(file));
        String line;
        Set<String> result=new TreeSet<String>();
        while((line=reader.readLine())!=null){
            for(String sum:line.replaceAll("[^[A-Z]+\\s*]+$","").split(" ")){
                result.add(sum);
            }
        }
        reader.close();
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File("foundsumerogramsincorpus.txt")));
        for(String ress:result){
            System.out.println(ress);
            writer.write(ress+"\n");
        }
        writer.close();
    }
}

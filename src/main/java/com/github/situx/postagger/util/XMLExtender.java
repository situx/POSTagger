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

package com.github.situx.postagger.util;

import java.io.*;
import java.util.TreeMap;
import java.util.Map;

/**
 * Created by timo on 4/26/15.
 */
public class XMLExtender {

    public static void main(String[] args) throws IOException {
        Map<Integer,String> charToName=new TreeMap<>();
        Map<Integer,String> charToStuff=new TreeMap<>();
        String tempLine;
        FileInputStream fis=new FileInputStream("akkad.xml");
        BufferedReader reader3=new BufferedReader(new InputStreamReader(fis, "UTF-16"));
        while((tempLine=reader3.readLine())!=null){
            System.out.println(tempLine);
            if(tempLine.contains("<sign ") && tempLine.contains("id=")) {
                String character=tempLine.substring(tempLine.indexOf("id=\"")+3, tempLine.indexOf("\"",tempLine.indexOf("id=\"")+4));
                System.out.println("Character: "+character);
                Integer characterPoint=Integer.valueOf(character.substring(3),16);
                charToStuff.put(characterPoint,tempLine.substring(tempLine.indexOf("\"",tempLine.indexOf("id=\"")+4),tempLine.indexOf(">")));
                charToStuff.put(characterPoint,charToStuff.get(characterPoint).replace("/",""));
                System.out.println(tempLine.substring(tempLine.indexOf("\"",tempLine.indexOf("id=\"")+4),tempLine.indexOf(">")));
            }
        }
        reader3.close();
        BufferedReader reader2=new BufferedReader(new FileReader(new File("akkmap.xml")));
        while((tempLine=reader2.readLine())!=null){
            if(tempLine.contains("=")) {
                String[] tempspl=tempLine.split("\"");
                charToName.put(Integer.valueOf(tempspl[3].substring(2),16),tempspl[1].replace("<sub>","").replace("</sub>","").toUpperCase());
                System.out.println("1: "+Integer.toHexString(Integer.valueOf(tempspl[3].substring(2),16))+" - "+tempspl[1].replace("<sub>","").replace("</sub>",""));
            }
        }
        reader2.close();
        BufferedReader reader=new BufferedReader(new FileReader(new File("testfile.xml")));
        StringBuilder result=new StringBuilder();
        while((tempLine=reader.readLine())!=null){
             if(tempLine.contains("logogram")){
                 String character=tempLine.substring(tempLine.indexOf("logogram=\"")+10, tempLine.indexOf("logogram=\"")+12);
                 if(charToStuff.containsKey(character.codePointAt(0))){
                     result.append(tempLine.replace(" logogram", " id=\"U+" + Integer.toHexString(character.codePointAt(0)).toUpperCase() +" logogram").replace(" logogram", charToStuff.get(character.codePointAt(0))+" logogram") + System.lineSeparator());
                 }else{
                     result.append(tempLine.replace(" logogram", " id=\"U+" + Integer.toHexString(character.codePointAt(0)).toUpperCase() + "\" logogram").replace(" logogram", " signName=\"" + charToName.get(character.codePointAt(0)) + "\" logogram") + System.lineSeparator());
                 }
             }else{
                 result.append(tempLine+System.lineSeparator());
             }
        }
        reader.close();
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File("testout.xml")));
        writer.write(result.toString());
        writer.close();
    }
}

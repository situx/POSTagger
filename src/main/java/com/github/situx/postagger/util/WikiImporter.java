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

/**
 * Created by timo on 2/25/15.
 */
public class WikiImporter  {

    public WikiImporter(){

    }

    public String removeLeadingZeros(String leading){
          leading=leading.toLowerCase();
          if((leading.charAt(1)+"").matches("[A-z]")){
              return leading.substring(0,2)+leading.substring(2).replaceFirst("^0+(?!$)", "");
          }
          return leading.charAt(0)+leading.substring(1).replaceFirst("^0+(?!$)", "");
          //return leading.replaceAll("[0]+","");
    }

    public void parseFile(String filePath) throws IOException {
        BufferedReader reader=new BufferedReader(new FileReader(new File(filePath)));
        String temp,trans=null;
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File("ime/egypt.txt")));
        writer.write("<?xml version=\"1.0\" ?>"+System.lineSeparator());
        writer.write("<data>"+System.lineSeparator());
        java.util.Map<String,String> result=new TreeMap<>();
        while((temp=reader.readLine())!=null){
            if(temp.contains("w:") && trans!=null){
                System.out.println(temp);
                result.put(trans,temp.substring(temp.indexOf("w:")+2,temp.indexOf("\"",temp.indexOf("w:")+2)).trim());
                //writer.write("<word freq=\"1\" chars=\""+temp.substring(temp.indexOf("w:")+2,temp.indexOf("\"",temp.indexOf("w:")+2)).trim()+"\" translit=\""+trans+"\"/>"+System.lineSeparator());
            }
            if(temp.contains("HIEROGLYPH")){
                trans=this.removeLeadingZeros(temp.substring(temp.indexOf("HIEROGLYPH")+11,temp.indexOf("<",temp.indexOf("HIEROGLYPH")+11)));
                System.out.println(temp);
            }

        }
        System.out.println(result);
        reader.close();
        reader=new BufferedReader(new FileReader(new File("egypttrans.txt")));
        while((temp=reader.readLine())!=null){
            if(temp.contains("\"")){
                String[] quotes=temp.split ("\"");
                if(result.get(quotes[3].toLowerCase())==null){
                    System.out.println(temp);
                    System.out.println("NULL: "+quotes[3]);
                }else{
                    writer.write("<word freq=\"1\" chars=\""+result.get(quotes[3].toLowerCase())+"\" translit=\""+quotes[1].toLowerCase()+"\"/>"+System.lineSeparator());
                }
            }
        }
        reader.close();
        for(String key:result.keySet()){
             writer.write("<word freq=\"1\" chars=\""+result.get(key)+"\" translit=\""+key.toLowerCase()+"\"/>"+System.lineSeparator());
        }
        writer.write("</data>"+System.lineSeparator());
        writer.close();
        /*java.io.InputStream in = new FileInputStream(new File(filePath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        net.htmlparser.jericho.Source htmlparser=new Source(in);

        List<StartTag> elems=htmlparser.getAllStartTags("a");
        List<String> urls=new LinkedList<String>();
        for(StartTag elem:elems){
            if(elem.getAttributeValue("title")!=null){
                System.out.println(elem.getAttributeValue("title"));
            }

        } */
    }


    public static void main(String[] args) throws IOException {
        WikiImporter importer=new WikiImporter();
        importer.parseFile("egypt.html");
    }

}

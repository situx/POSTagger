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

package com.github.situx.postagger.dict.corpusimport.cuneiform;

import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Created by timo on 13.07.14.
 * Parses akkadian verbs for postagging
 */
public class VerblistParser {
    /**
     * Set of regexes for recognizing verbs.
     */
    public Set<String> verbregexes;
    /**HTMLParser for parsing the source file.*/
    private Source source;

    /**
     * Constructor for this class.
     * @param filepath the path to the verb file
     * @throws IOException on error
     */
    public VerblistParser(String filepath) throws IOException {
        this.source=new Source(new File(filepath));

    }

    /**
     * Test main method for verblist parser.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        VerblistParser parser=new VerblistParser("verbs.html");
        parser.parseList();
    }

    /**
     * Parses the verbs from the given input file.
     * @throws IOException on error
     */
    public void parseList() throws IOException {
        List<StartTag> elems=source.getAllStartTags("tr");
        List<String> urls=new LinkedList<String>();
        for(StartTag elem:elems){
            if(!elem.getElement().getChildElements().isEmpty())  {
                System.out.println(elem.getElement().getChildElements().get(1).getTextExtractor().toString());
                urls.add(elem.getElement().getChildElements().get(1).getTextExtractor().toString());
            }
        }
        System.out.println(urls.toString());
        verbregexes=new TreeSet<>();
        String buildregex1="(^(l[aeiu]-)?[aeiu]?[",buildregex2="]-(ta-)?[",buildregex3="{1-2}][aeiu]-[",buildregex4="][ui]?.*$)";
        for(String url:urls){
            if(!url.contains("ʾ")){
                verbregexes.add(buildregex1+url.substring(0,1)+buildregex2+url.substring(1,2)+buildregex3+url.substring(2,3)+buildregex4);
            }else{
                if(url.substring(0,1).equals("ʾ") && !url.substring(1,2).equals("ʾ") && !url.substring(2,3).equals("ʾ")){
                    verbregexes.add(buildregex1.substring(0,buildregex1.length()-8)+buildregex2.substring(2,buildregex2.length())+url.substring(1,2)+buildregex3+url.substring(2,3)+buildregex4);
                }else if(!url.substring(0,1).equals("ʾ") && url.substring(1,2).equals("ʾ") && !url.substring(2,3).equals("ʾ")){
                    verbregexes.add(buildregex1+url.substring(0,1)+buildregex2.substring(0,buildregex2.length()-1)+"["+url.substring(2,3)+buildregex4);
                }else if(!url.substring(0,1).equals("ʾ") && !url.substring(1,2).equals("ʾ") && url.substring(2,3).equals("ʾ")){
                    verbregexes.add(buildregex1+url.substring(0,1)+buildregex2+url.substring(1,2)+buildregex3.substring(0,buildregex3.length()-2)+buildregex4.substring(6,buildregex4.length()));
                }
            }
        }
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File("verbregexes.txt")));
        for(String regex:verbregexes){
            writer.write("<tag desc=\"verb\" name=\"VV\" equals=\"\" regex=\""+regex+"\" case=\"VERB\" value=\"\" />\n");
        }
        writer.close();
        System.out.println(verbregexes.toString());
    }


}

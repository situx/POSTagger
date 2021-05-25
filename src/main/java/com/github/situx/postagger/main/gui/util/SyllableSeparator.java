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

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.util.enums.util.Tags;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;
import org.apache.http.client.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by timo on 17.04.17 .
 */
public class SyllableSeparator extends DefaultHandler2 {

    static Set<String> vowels;
    private Locale origlocale,destlocale;
    private String origvalue,destvalue;
    private LangChar tempchar;

    CloseableHttpClient client;

    BufferedWriter writer;

    Map<String,Set<String>> refer;

    boolean referparse=true;

    public SyllableSeparator() throws IOException {
        vowels = new TreeSet<String>();
        vowels.add("a");
        vowels.add("e");
        vowels.add("i");
        vowels.add("o");
        vowels.add("u");
        vowels.add("Å«");
        vowels.add("Ä�");
        vowels.add("Ä“");
        vowels.add("Ä«");
        vowels.add("Ãª");
        vowels.add("Ã¢");
        vowels.add("Ã»");
        vowels.add("Ã®");
        client= HttpClients.createDefault();
        writer=new BufferedWriter(new FileWriter(new File("akkdictwithsyllsep.xml")));
        writer.write("<? xml version=\"1.1\"?>"+System.lineSeparator());
        writer.write("<dictentries>"+System.lineSeparator());
        refer=new TreeMap<>();
    }


    public static boolean isVowel(String test){
        return vowels.contains(test);
    }


    public static String separateAkkadian(String akkadword, String separator) {

        boolean first = true, syllcontainsvow = false, syllcontainsconso = false, lastsyllvow = false, lastsyllconso = false;
        StringBuilder builder = new StringBuilder();
        String tempsyll = "",lastchar="";
        for (int i=0;i<akkadword.toCharArray().length;) {
            String charstr = akkadword.toCharArray()[i] + "";
            if(lastchar.equals("s") && charstr.equals("z")){
                tempsyll+=charstr;
                lastchar="z";
                i++;
                continue;
            }
            if(lastchar.equals("s") && charstr.equals(",")){
                tempsyll+=charstr;
                lastchar=",";
                i++;
                continue;
            }
            if(charstr.matches(" ")){
                tempsyll+=charstr;
                lastsyllvow=false;
                lastsyllconso=false;
                syllcontainsvow=false;
                syllcontainsconso=false;
                first=true;
                i++;
                continue;
            }
            if(charstr.matches("[0-9]")){
                tempsyll+=charstr;
                i++;
                continue;
            }
            if (first) {
                tempsyll += charstr;
                if (vowels.contains(charstr)) {
                    syllcontainsvow = true;
                    lastsyllvow = true;
                } else {
                    syllcontainsconso = true;
                    lastsyllconso = true;
                }
                first = false;
                i++;
            } else {
                //System.out.println("Tempsyll: "+tempsyll);
                //System.out.println("LastSyllvow: "+lastsyllvow+" syllcontainsvow: "+syllcontainsvow+" syllcontainsconso: "+syllcontainsconso+" lastsyllconso: "+lastsyllconso);
                if (syllcontainsvow && lastsyllvow && isVowel(charstr)) {
                    builder.append(tempsyll).append(separator);
                    tempsyll = charstr;
                    syllcontainsvow = true;
                    syllcontainsconso=false;
                    lastsyllconso=false;
                    lastsyllvow = true;
                    i++;
                } else if (syllcontainsconso && lastsyllconso && syllcontainsvow && !isVowel(charstr)) {
                    builder.append(tempsyll).append(separator);
                    tempsyll = charstr;
                    syllcontainsconso = true;
                    syllcontainsvow=false;
                    lastsyllvow=false;
                    lastsyllconso = true;
                    i++;
                }else if(lastsyllconso && syllcontainsvow && isVowel(charstr)){
                    builder.append(tempsyll.substring(0,tempsyll.length()-1)).append(separator);
                    tempsyll=tempsyll.substring(tempsyll.length()-1);
                    if (vowels.contains(tempsyll)) {
                        syllcontainsvow = true;
                        lastsyllvow = true;
                        lastsyllconso=false;
                        syllcontainsconso=false;
                    } else {
                        syllcontainsconso = true;
                        lastsyllconso = true;
                        lastsyllvow=false;
                        syllcontainsvow=false;
                    }
                }else{
                    tempsyll+=charstr;
                    if (vowels.contains(charstr)) {
                        syllcontainsvow = true;
                        lastsyllvow = true;
                        lastsyllconso=false;
                    } else {
                        syllcontainsconso = true;
                        lastsyllconso = true;
                        lastsyllvow=false;
                    }
                    i++;
                }
            }
            lastchar=charstr;

        }
        builder.append(tempsyll);
        return builder.toString().replace("- "," ").replace(" -"," ").replace("s-z","sz-");
    }


    public String cuneify(String separated) throws IOException {
        HttpGet get=new HttpGet("http://oracc.museum.upenn.edu/cgi-bin/cuneify?input="+ URLEncoder.encode(separated));
        HttpResponse response=client.execute(get);
        ResponseHandler<String> handler = new BasicResponseHandler();
        String result=handler.handleResponse(response);
        result=result.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ");
        result=result.replaceAll("[A-z-]","");
        result=result.trim();
        result=result.replace(" ","");
        return result;
    }

    public String[] prepareTranslations(String translationString, Locale locale){
        if(translationString==null)
            return new String[0];
        String[] translations=translationString.split(",|;|:|\\/");
        for(String trans:translations){
            trans=trans.replaceAll("to ","").replaceAll("the","").replaceAll("a ","")
                    .replaceAll("\\(?\\)","").replaceAll("\\(?","").replaceAll("[0-9]\\)","")
                    .replaceAll("\\?","").replaceAll("[0-9]", "")
                    .replaceAll("\\[","").replaceAll("]","").replaceAll("\\...","");
            trans=trans.trim();
            /*if(!trans.isEmpty()) {
                this.tempchar.addTranslation(trans, locale);
                //System.out.println("Add Translation: " + trans);
            }*/
        }
        return translations;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        switch (qName) {
            case Tags.TRANSLATION:
                this.origvalue = attributes.getValue("origvalue");

                this.destvalue = attributes.getValue("destvalue");
                this.destlocale = new Locale(attributes.getValue("destlocale"));
                String[] trans=this.prepareTranslations(destvalue, destlocale);
                if(!referparse && trans.length!=0 && !destvalue.contains("â†’")) {
                    for(String orival:origvalue.split(",")){
                        try {
                            writer.write("<dictentry ref=\"akkdict\"");
                            if(attributes.getValue("concept")!=null){
                                writer.write(" concept=\""+attributes.getValue("concept")+"\">"+System.lineSeparator());
                            }else{
                                writer.write(">"+System.lineSeparator());
                            }
                            System.out.println(orival + " contained? " + refer.containsKey(origvalue));
                            if (refer.containsKey(orival)) {
                                for (String referer : refer.get(orival)) {
                                    System.out.println("Fefer add: " + referer);
                                    String sep = this.separateAkkadian(referer, "-");
                                    writer.write("<transliteration transcription=\"" + referer + "\" cunei=\"" + this.cuneify(sep) + "\">");
                                    writer.write(sep);
                                    writer.write("</transliteration>");
                                }
                            }
                            orival = orival.replace("(", "").replace(")", "")
                                    .replace("á¹£", "s").replace("*", "");
                            writer.write("<transliteration transcription=\"" + orival + "\">");
                            String infinitive = this.separateAkkadian(orival, "-");
                            infinitive = infinitive
                                    .replace("- ", " ")
                                    .replace(" -", " ")
                                    .replace("--", "-")
                                    .replace("Å¡", "sz");
                            String tocuneify = infinitive.replace("Å«", "u")
                                    .replace("Ä�", "a")
                                    .replace("Ä“", "e")
                                    .replace("Ä«", "i")
                                    .replace("Ãª", "e")
                                    .replace("Ã¢", "a")
                                    .replace("Ã»", "u")
                                    .replace("Ã®", "i")
                                    .replace("Å¡", "sz")
                                    .replace("á¹£", "s")
                                    .replace("á¹­", "t")
                                    .replace("á¸«", "h")
                                    .replace("(", "").replace(")", "");
                            writer.write(infinitive);
                            writer.write("</transliteration>" + System.lineSeparator());
                            if(attributes.getValue("stem")!=null){
                                writer.write("<transliteration transcription=\"" + attributes.getValue("stem") + "\" stem=\"true\">");
                                writer.write(attributes.getValue("stem"));
                                writer.write("</transliteration>" + System.lineSeparator());
                            }else {
                                String stem = "";
                                if (infinitive.charAt(infinitive.length() - 1) == 'u' || infinitive.charAt(infinitive.length() - 1) == 'û'
                                        || infinitive.charAt(infinitive.length() - 1) == 'ū') {
                                    stem = infinitive.substring(0, infinitive.length() - 1);
                                } else if (infinitive.charAt(infinitive.length() - 2) == 'u' || infinitive.charAt(infinitive.length() - 2) == 'û'
                                        || infinitive.charAt(infinitive.length() - 2) == 'ū') {
                                    stem = infinitive.substring(0, infinitive.length() - 2);
                                } else {
                                    stem = infinitive;
                                }
                                writer.write("<transliteration transcription=\"" + stem + "\" stem=\"true\">");
                                writer.write(stem);
                                writer.write("</transliteration>" + System.lineSeparator());
                            }
                            for (String transs : trans) {
                                writer.write("<translation locale=\"en\">");
                                writer.write(transs.trim());
                                writer.write("</translation>" + System.lineSeparator());
                            }
                            String postag;
                            if(attributes.getValue("pos")!=null){
                                postag=attributes.getValue("pos");
                                switch(attributes.getValue("pos")){
                                    case "VV":
                                        writer.write("<postag uri=\"http://purl.org/olia/olia.owl#Verb\">VV</postag>" + System.lineSeparator());
                                    break;
                                    case "ADV":
                                        writer.write("<postag uri=\"http://purl.org/olia/olia.owl#Adverb\">ADV</postag>" + System.lineSeparator());
                                        break;
                                    case "ADJ":
                                        writer.write("<postag uri=\"http://purl.org/olia/olia.owl#Adjective\">ADJ</postag>" + System.lineSeparator());
                                        break;
                                    case "NN":
                                        writer.write("<postag uri=\"http://purl.org/olia/olia.owl#Noun\">NN</postag>" + System.lineSeparator());
                                        break;
                                    case "PPRO":
                                        writer.write("<postag uri=\"http://purl.org/olia/olia.owl#PersonalPronoun\">PPRO</postag>" + System.lineSeparator());
                                        break;
                                    case "INTPRO":
                                        writer.write("<postag uri=\"http://purl.org/olia/olia.owl#InterrogativePronoun\">INTPRO</postag>" + System.lineSeparator());
                                        break;
                                    case "NE":
                                        writer.write("<postag uri=\"http://purl.org/olia/olia.owl#NamedEntity\">NE</postag>" + System.lineSeparator());
                                        break;
                                    default:


                                }
                            }else {

                                if (destvalue.contains("to ")) {
                                    writer.write("<postag uri=\"http://purl.org/olia/olia.owl#Verb\">VV</postag>" + System.lineSeparator());
                                    postag = "VV";
                                } else if (destvalue.contains("ly ")) {
                                    writer.write("<postag uri=\"http://purl.org/olia/olia.owl#Adverb\">ADV</postag>" + System.lineSeparator());
                                    postag = "ADV";
                                } else if (destvalue.contains("y ")) {
                                    writer.write("<postag uri=\"http://purl.org/olia/olia.owl#Adjective\">ADJ</postag>" + System.lineSeparator());
                                    postag = "ADJ";
                                } else {
                                    writer.write("<postag uri=\"http://purl.org/olia/olia.owl#Noun\">NN</postag>" + System.lineSeparator());
                                    postag = "NN";
                                }
                            }
                            String cunei = this.cuneify(tocuneify);
                            System.out.println(infinitive + " - " + cunei + " - " + postag + " - " + Arrays.toString(trans));
                            writer.write(cunei);
                            writer.write("</dictentry>" + System.lineSeparator());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else if(referparse && destvalue.contains("â†’")) {
                    if(!refer.containsKey(destvalue.replace("â†’",""))){
                        refer.put(destvalue.replace("â†’",""),new TreeSet<>());
                    }
                    refer.get(destvalue.replace("â†’","")).add(origvalue);
                }
                //System.out.println(refer);
                break;
        }
    }



    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        SyllableSeparator syllsep=new SyllableSeparator();
        SAXParserFactory.newInstance().newSAXParser().parse("newwordsShort2.xml",syllsep);
        syllsep.referparse=false;
        SAXParserFactory.newInstance().newSAXParser().parse("newwordsShort2.xml",syllsep);
        syllsep.writer.write("</dictentries>"+System.lineSeparator());
        syllsep.writer.close();
    }
/*
                String input="amÄ“lÄ�nu";
                String infinitive=syllsep.separateAkkadian(input,"-");
                String tocuneify=infinitive.replace("Å«","u")
                        .replace("Ä�","a")
                        .replace("Ä“","e")
                        .replace("Ä«","i")
                        .replace("Ãª","e")
                        .replace("Ã¢","a")
                        .replace("Ã»","u")
                        .replace("Ã®","i");
                String stem="";
                if(infinitive.charAt(infinitive.length()-1)=='u'){
                    stem=infinitive.substring(0,infinitive.length()-1);
                }else if(infinitive.charAt(infinitive.length()-1)=='u'){
                    stem=infinitive.substring(0,infinitive.length()-2);
                }
                System.out.println("Infinitive: "+infinitive+System.lineSeparator()+"Stem: "+stem+" Cunei: "+syllsep.cuneify(tocuneify));
                StringBuilder builder=new StringBuilder();

    }*/

}

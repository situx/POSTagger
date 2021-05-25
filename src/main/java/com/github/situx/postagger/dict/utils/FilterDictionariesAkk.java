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

package com.github.situx.postagger.dict.utils;

import com.github.situx.postagger.main.gui.util.SyllableSeparator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by timo on 01.05.17 .
 */
public class FilterDictionariesAkk extends DefaultHandler2 {

    public String refdict;
    java.util.Map<String,MorphPattern> file1;

    java.util.Map<String,MorphPattern> file2;

    SyllableSeparator syllsep;

    Integer matchCounter=0,notfileCounter=0;

    boolean file1bool=true,nostem=false;
    private String destvalue;
    private String origvalueAttName="";
    private Locale destlocale;

    private Boolean needToSeparate=true;

    private FileWriter writer;

    Map<String,TreeMap<String,String>> wordToAttributes;

    Map<String,String> wordToXMLTree;

    Map<String,Set<String>> refer;

    boolean referparse=false;

    public FilterDictionariesAkk(String exportfile) throws IOException {
        this.file1=new TreeMap<>();
        this.file2=new TreeMap<>();
        this.wordToAttributes=new TreeMap<>();
        this.wordToXMLTree=new TreeMap<>();
        this.syllsep=new SyllableSeparator();
        this.refer=new TreeMap<>();
        this.writer=new FileWriter(exportfile);
        this.refdict="";
        this.origvalueAttName="origvalue";
    }

    public FilterDictionariesAkk(String exportfile,String origvalueAttName) throws IOException {
        this.origvalueAttName=origvalueAttName;
        this.file1=new TreeMap<>();
        this.file2=new TreeMap<>();
        this.wordToAttributes=new TreeMap<>();
        this.wordToXMLTree=new TreeMap<>();
        this.syllsep=new SyllableSeparator();
        this.refer=new TreeMap<>();
        this.writer=new FileWriter(exportfile);
        this.refdict="";
    }

    public FilterDictionariesAkk(String exportfile,String origvalueAttName,Boolean needToSeparate,Boolean nostem) throws IOException {
        this.needToSeparate=needToSeparate;
        this.nostem=nostem;
        this.origvalueAttName=origvalueAttName;
        this.file1=new TreeMap<>();
        this.file2=new TreeMap<>();
        this.wordToAttributes=new TreeMap<>();
        this.wordToXMLTree=new TreeMap<>();
        this.syllsep=new SyllableSeparator();
        this.refer=new TreeMap<>();
        this.writer=new FileWriter(exportfile);
        this.refdict="";
    }

    public void addToMap(Map<String,String> map,String name,String value){
        if(name!=null && value!=null && !map.containsKey(name)){
            map.put(name,value);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName){
            case "translation":
                String origvalue=attributes.getValue(origvalueAttName);
                if(origvalue!=null)
                    origvalue=origvalue.toLowerCase().trim();
                if(needToSeparate){
                    origvalue= syllsep.separateAkkadian(origvalue.replace("Ê¾",""),"-").replace("--","-");
                }
                if(file1bool){
                    //this.file1.put(origvalue,new MorphPattern());
                    //matchCounter++;
                    this.destvalue = StringEscapeUtils.escapeXml10(attributes.getValue("destvalue"));
                    //this.destlocale = new Locale(attributes.getValue("destlocale"));
                    String[] trans=syllsep.prepareTranslations(destvalue, destlocale);
                    if(!referparse && trans.length!=0 && !destvalue.contains("â†’")) {
                        for(String orival:origvalue.split(",")){
                            StringBuilder xmltree=new StringBuilder();
                            try {
                                if(!wordToAttributes.containsKey(origvalue)){
                                    wordToAttributes.put(origvalue,new TreeMap<>());
                                }
                                addToMap(wordToAttributes.get(origvalue),"ref",refdict);
                                addToMap(wordToAttributes.get(origvalue),"logogram",attributes.getValue("logogram"));
                                addToMap(wordToAttributes.get(origvalue),"concept",attributes.getValue("concept"));
                                /*writer.write("<dictentry ref=\""+refdict+";"
                                        +(attributes.getValue("ref")!=null?attributes.getValue("ref"):"")+"\"");
                                writer.write(attributes.getValue("logogram")!=null?"logogram=\""+attributes.getValue("logogram")+"\" ":"");
                                if(attributes.getValue("concept")!=null){
                                    writer.write(" concept=\""+attributes.getValue("concept")+"\">"+System.lineSeparator());
                                }else{
                                    writer.write(">"+System.lineSeparator());
                                }*/
                                System.out.println(orival + " contained? " + refer.containsKey(origvalue));
                                if (refer.containsKey(orival)) {
                                    for (String referer : refer.get(orival)) {
                                        System.out.println("Refer add: " + referer);
                                        String sep;
                                        if(needToSeparate){
                                            sep=syllsep.separateAkkadian(referer, "-");
                                        }else{
                                            sep=referer;
                                        }
                                        xmltree.append("<transliteration transcription=\"" + referer.replace("-","").replaceAll("[0-9]","") + "\" cunei=\"" + syllsep.cuneify(sep).replaceAll("[0-9]","") + "\">");
                                        xmltree.append(sep);
                                        xmltree.append("</transliteration>");
                                    }
                                }
                                orival = orival.replace("(", "").replace(")", "")
                                        .replace("á¹£", "s").replace("*", "");

                                String tocuneify;
                                String infinitive="";
                                if(!nostem) {
                                    xmltree.append("<transliteration transcription=\"" + orival.replace("-","").replaceAll("[0-9]","") + "\">");
                                    if (needToSeparate) {
                                        infinitive = syllsep.separateAkkadian(orival, "-");
                                    } else {
                                        infinitive = orival;
                                    }
                                    infinitive = infinitive
                                            .replace("- ", " ")
                                            .replace(" -", " ")
                                            .replace("--", "-")
                                            .replace("Å¡", "sz");
                                    tocuneify = infinitive.replace("Å«", "u")
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
                                    xmltree.append(infinitive);
                                    xmltree.append("</transliteration>").append(System.lineSeparator());
                                    String stem = "";
                                    if (attributes.getValue("stem") != null) {
                                        xmltree.append("<transliteration transcription=\"").append(attributes.getValue("stem")).append("\" stem=\"true\">");
                                        xmltree.append(attributes.getValue("stem"));
                                        xmltree.append("</transliteration>").append(System.lineSeparator());
                                    } else {

                                        if (infinitive.length() >= 1 && (infinitive.charAt(infinitive.length() - 1) == 'u' || infinitive.charAt(infinitive.length() - 1) == 'û'
                                                || infinitive.charAt(infinitive.length() - 1) == 'ū')) {
                                            stem = infinitive.substring(0, infinitive.length() - 1);
                                        } else if (infinitive.length() > 1 && (infinitive.charAt(infinitive.length() - 2) == 'u' || infinitive.charAt(infinitive.length() - 2) == 'û'
                                                || infinitive.charAt(infinitive.length() - 2) == 'ū')) {
                                            stem = infinitive.substring(0, infinitive.length() - 2);
                                        } else {
                                            stem = infinitive;
                                        }
                                    }
                                    xmltree.append("<transliteration transcription=\"").append(stem.replace("-", "").replaceAll("[0-9]", "")).append("\" stem=\"true\">");
                                    xmltree.append(stem);
                                    xmltree.append("</transliteration>").append(System.lineSeparator());
                                }else{
                                    tocuneify = orival.replace("Å«", "u")
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
                                    xmltree.append("<transliteration transcription=\"").append(tocuneify.replace("-", "").replaceAll("[0-9]", "")).append("\">");
                                    xmltree.append(tocuneify);
                                    xmltree.append("</transliteration>").append(System.lineSeparator());
                                }
                                for (String transs : trans) {
                                    xmltree.append("<translation locale=\"en\">");
                                    xmltree.append(transs.trim());
                                    xmltree.append("</translation>").append(System.lineSeparator());
                                }
                                String postag;
                                if(attributes.getValue("pos")!=null){
                                    postag=attributes.getValue("pos");
                                    switch(attributes.getValue("pos")){
                                        case "VV":
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Verb\">VV</postag>" + System.lineSeparator());
                                            break;
                                        case "ADV":
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Adverb\">ADV</postag>" + System.lineSeparator());
                                            break;
                                        case "ADJ":
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Adjective\">ADJ</postag>" + System.lineSeparator());
                                            break;
                                        case "CARD":
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Cardinal\">CARD</postag>" + System.lineSeparator());
                                            break;
                                        case "NN":
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Noun\">NN</postag>" + System.lineSeparator());
                                            break;
                                        case "PPRO":
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#PersonalPronoun\">PPRO</postag>" + System.lineSeparator());
                                            break;
                                        case "INTPRO":
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#InterrogativePronoun\">INTPRO</postag>" + System.lineSeparator());
                                            break;
                                        case "NE":
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#NamedEntity\">NE</postag>" + System.lineSeparator());
                                            break;
                                        default:


                                    }

                                }else {

                                    if (destvalue.contains("to ")) {
                                        xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Verb\">VV</postag>" + System.lineSeparator());
                                        postag = "VV";
                                    } else if (destvalue.contains("ly ")) {
                                        xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Adverb\">ADV</postag>" + System.lineSeparator());
                                        postag = "ADV";
                                    } else if (destvalue.contains("y ")) {
                                        xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Adjective\">ADJ</postag>" + System.lineSeparator());
                                        postag = "ADJ";
                                    } else {
                                        xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Noun\">NN</postag>" + System.lineSeparator());
                                        postag = "NN";
                                    }
                                }
                                if(attributes.getValue("epoch")!=null){
                                    String[] epoch=attributes.getValue("epoch").split(";|,");
                                    for(String ep:epoch) {
                                        switch (ep) {
                                            case "OldAkkadian":
                                                xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29690309\" start=\"-2500\" end=\"-1950\" name=\"OldAkkadian\">OAkk</epoch>" + System.lineSeparator());
                                                break;
                                            case "OldBabylonian":
                                                xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29652754\" start=\"-1950\" end=\"-1530\" mainlocation=\"https://www.wikidata.org/wiki/Q47690\" name=\"OldBabylonian\">OB</epoch>" + System.lineSeparator());
                                                break;
                                            case "MiddleBabylonian":
                                                xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29652823\" start=\"-1530\" end=\"-1000\" mainlocation=\"https://www.wikidata.org/wiki/Q47690\" name=\"MiddleBabylonian\">MB</epoch>" + System.lineSeparator());
                                                break;
                                            case "NeoBabylonian":
                                                xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29688754\" start=\"-1000\" end=\"-600\" mainlocation=\"https://www.wikidata.org/wiki/Q47690\" name=\"NeoBabylonian\">NB</epoch>" + System.lineSeparator());
                                                break;
                                            case "UrIII":
                                                xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q723587\" start=\"-2112\" end=\"-2004\" mainlocation=\"https://www.wikidata.org/wiki/Q47690\" name=\"UrIII\">UrIII</epoch>" + System.lineSeparator());
                                                break;
                                            case "OldAssyrian":
                                                xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29652754\" start=\"-1950\" end=\"-1530\" mainlocation=\"https://www.wikidata.org/wiki/Q41137\" name=\"OldAssyrian\">OA</epoch>" + System.lineSeparator());
                                                break;
                                            case "MiddleAssyrian":
                                                xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29652823\" start=\"-1530\" end=\"-1000\"  mainlocation=\"https://www.wikidata.org/wiki/Q41137\" name=\"MiddleAssyrian\">MA</epoch>" + System.lineSeparator());
                                                break;
                                            case "NeoAssyrian":
                                                xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29688754\" start=\"-1000\" end=\"-600\"  mainlocation=\"https://www.wikidata.org/wiki/Q41137\" name=\"NeoAssyrian\">NA</epoch>" + System.lineSeparator());
                                                break;
                                            case "Assyrian":
                                                xmltree.append("<dialect uri=\"http://purl.org/olia/olia.owl\" name=\"Assyrian\">Assyrian</dialect>" + System.lineSeparator());
                                                break;
                                            case "Babylonian":
                                                xmltree.append("<dialect uri=\"http://purl.org/olia/olia.owl\" name=\"Babylonian\">Babylonian</dialect>" + System.lineSeparator());
                                                break;
                                            case "SB":
                                                xmltree.append("<dialect uri=\"http://purl.org/olia/olia.owl\" name=\"Standard Babylonian\">Standard Babylonian</dialect>" + System.lineSeparator());
                                                break;
                                            case "Nuzi":
                                                xmltree.append("<dialect uri=\"https://www.wikidata.org/wiki/Q29688886\" name=\"Nuzi\">Nuzi</dialect>" + System.lineSeparator());
                                                break;
                                            case "Mari":
                                                xmltree.append("<dialect uri=\"https://www.wikidata.org/wiki/Q29689558\" name=\"Mariotic\">Mariotic</dialect>" + System.lineSeparator());
                                                break;
                                            default:

                                        }


                                    }}
                                    String cunei;
                                    if(attributes.getValue("cunei")==null){
                                        System.out.println("No Cunei for "+origvalue);
                                        cunei = syllsep.cuneify(tocuneify);
                                    }else{
                                        cunei=attributes.getValue("cunei");
                                    }

                                System.out.println(infinitive + " - " + cunei + " - " + postag + " - " + Arrays.toString(trans));
                                xmltree.append(cunei.replaceAll("[0-9]",""));
                                xmltree.append("</dictentry>" + System.lineSeparator());
                                this.wordToXMLTree.put(origvalue,xmltree.toString());
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
                    this.file1.put(origvalue, new MorphPattern());
                }else if(!file1bool){
                    notfileCounter++;
                    if(origvalue!=null && !this.file1.containsKey(origvalue)) {
                        this.destvalue = StringEscapeUtils.escapeXml10(attributes.getValue("destvalue"));
                        //this.destlocale = new Locale(attributes.getValue("destlocale"));
                        String[] trans=syllsep.prepareTranslations(destvalue, destlocale);
                        if(!referparse && trans.length!=0 && !destvalue.contains("â†’")) {
                            for(String orival:origvalue.split(",")){
                                StringBuilder xmltree=new StringBuilder();
                                try {
                                    if(!wordToAttributes.containsKey(origvalue)){
                                        wordToAttributes.put(origvalue,new TreeMap<>());
                                    }
                                    addToMap(wordToAttributes.get(origvalue),"ref",refdict);
                                    addToMap(wordToAttributes.get(origvalue),"logogram",attributes.getValue("logogram"));
                                    addToMap(wordToAttributes.get(origvalue),"meaning",attributes.getValue("meaning"));
                                    addToMap(wordToAttributes.get(origvalue),"concept",attributes.getValue("concept"));
                                /*writer.write("<dictentry ref=\""+refdict+";"
                                        +(attributes.getValue("ref")!=null?attributes.getValue("ref"):"")+"\"");
                                writer.write(attributes.getValue("logogram")!=null?"logogram=\""+attributes.getValue("logogram")+"\" ":"");
                                if(attributes.getValue("concept")!=null){
                                    writer.write(" concept=\""+attributes.getValue("concept")+"\">"+System.lineSeparator());
                                }else{
                                    writer.write(">"+System.lineSeparator());
                                }*/
                                    System.out.println(orival + " contained? " + refer.containsKey(origvalue));
                                    if (refer.containsKey(orival)) {
                                        for (String referer : refer.get(orival)) {
                                            System.out.println("Refer add: " + referer);
                                            String sep;
                                            if(needToSeparate){
                                                sep=syllsep.separateAkkadian(referer, "-");
                                            }else{
                                                sep=referer;
                                            }
                                            xmltree.append("<transliteration transcription=\"" + referer.replace("-","").replaceAll("[0-9]","") + "\" cunei=\"" + syllsep.cuneify(sep).replaceAll("[0-9]","") + "\">");
                                            xmltree.append(sep);
                                            xmltree.append("</transliteration>");
                                        }
                                    }
                                    orival = orival.replace("(", "").replace(")", "")
                                            .replace("á¹£", "s").replace("*", "");
                                    String tocuneify;
                                    String infinitive="";
                                    if(!nostem) {
                                        xmltree.append("<transliteration transcription=\"" + orival.replace("-","").replaceAll("[0-9]","") + "\">");
                                        if (needToSeparate) {
                                            infinitive = syllsep.separateAkkadian(orival, "-");
                                        } else {
                                            infinitive = orival;
                                        }
                                        infinitive = infinitive
                                                .replace("- ", " ")
                                                .replace(" -", " ")
                                                .replace("--", "-")
                                                .replace("Å¡", "sz");
                                        tocuneify = infinitive.replace("Å«", "u")
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
                                        xmltree.append(infinitive);
                                        xmltree.append("</transliteration>" + System.lineSeparator());
                                        if (attributes.getValue("stem") != null) {
                                            xmltree.append("<transliteration transcription=\"" + attributes.getValue("stem") + "\" stem=\"true\">");
                                            xmltree.append(attributes.getValue("stem"));
                                            xmltree.append("</transliteration>" + System.lineSeparator());
                                        } else {
                                            String stem = "";
                                            if (infinitive.length() >= 1 && (infinitive.charAt(infinitive.length() - 1) == 'u' || infinitive.charAt(infinitive.length() - 1) == 'û'
                                                    || infinitive.charAt(infinitive.length() - 1) == 'ū')) {
                                                stem = infinitive.substring(0, infinitive.length() - 1);
                                            } else if (infinitive.length() > 1 && (infinitive.charAt(infinitive.length() - 2) == 'u' || infinitive.charAt(infinitive.length() - 2) == 'û'
                                                    || infinitive.charAt(infinitive.length() - 2) == 'ū')) {
                                                stem = infinitive.substring(0, infinitive.length() - 2);
                                            } else {
                                                stem = infinitive;
                                            }

                                            xmltree.append("<transliteration transcription=\"").append(stem.replace("-", "").replaceAll("[0-9]", "")).append("\" stem=\"true\">");
                                            xmltree.append(stem);
                                            xmltree.append("</transliteration>").append(System.lineSeparator());
                                        }
                                    }else{
                                        tocuneify = orival.replace("Å«", "u")
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
                                        xmltree.append("<transliteration transcription=\"").append(tocuneify.replace("-", "").replaceAll("[0-9]", "")).append("\">");
                                        xmltree.append(tocuneify);
                                        xmltree.append("</transliteration>").append(System.lineSeparator());
                                    }
                                    for (String transs : trans) {
                                        xmltree.append("<translation locale=\"en\">");
                                        xmltree.append(transs.trim());
                                        xmltree.append("</translation>").append(System.lineSeparator());
                                    }
                                    String postag;
                                    if(attributes.getValue("pos")!=null){
                                        postag=attributes.getValue("pos");
                                        switch(attributes.getValue("pos")){
                                            case "VV":
                                                xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Verb\">VV</postag>").append(System.lineSeparator());
                                                break;
                                            case "ADV":
                                                xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Adverb\">ADV</postag>").append(System.lineSeparator());
                                                break;
                                            case "ADJ":
                                                xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Adjective\">ADJ</postag>").append(System.lineSeparator());
                                                break;
                                            case "CARD":
                                                xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Cardinal\">CARD</postag>").append(System.lineSeparator());
                                                break;
                                            case "NN":
                                                xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Noun\">NN</postag>").append(System.lineSeparator());
                                                break;
                                            case "PPRO":
                                                xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#PersonalPronoun\">PPRO</postag>").append(System.lineSeparator());
                                                break;
                                            case "INTPRO":
                                                xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#InterrogativePronoun\">INTPRO</postag>").append(System.lineSeparator());
                                                break;
                                            case "NE":
                                                xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#NamedEntity\">NE</postag>").append(System.lineSeparator());
                                                break;
                                            default:


                                        }

                                    }else {

                                        if (destvalue.contains("to ")) {
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Verb\">VV</postag>" + System.lineSeparator());
                                            postag = "VV";
                                        } else if (destvalue.contains("ly ")) {
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Adverb\">ADV</postag>" + System.lineSeparator());
                                            postag = "ADV";
                                        } else if (destvalue.contains("y ")) {
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Adjective\">ADJ</postag>" + System.lineSeparator());
                                            postag = "ADJ";
                                        } else {
                                            xmltree.append("<postag uri=\"http://purl.org/olia/olia.owl#Noun\">NN</postag>" + System.lineSeparator());
                                            postag = "NN";
                                        }
                                    }
                                    if(attributes.getValue("epoch")!=null){
                                        String[] epoch=attributes.getValue("epoch").split(";|,");
                                        for(String ep:epoch) {
                                            switch (ep) {
                                                case "OldAkkadian":
                                                    xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29690309\" start=\"-2500\" end=\"-1950\" name=\"OldAkkadian\">OAkk</epoch>" + System.lineSeparator());
                                                    break;
                                                case "OldBabylonian":
                                                    xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29652754\" start=\"-1950\" end=\"-1530\" mainlocation=\"https://www.wikidata.org/wiki/Q47690\" name=\"OldBabylonian\">OB</epoch>" + System.lineSeparator());
                                                    break;
                                                case "MiddleBabylonian":
                                                    xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29652823\" start=\"-1530\" end=\"-1000\" mainlocation=\"https://www.wikidata.org/wiki/Q47690\" name=\"MiddleBabylonian\">MB</epoch>" + System.lineSeparator());
                                                    break;
                                                case "UrIII":
                                                    xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q723587\" start=\"-2112\" end=\"-2004\" mainlocation=\"https://www.wikidata.org/wiki/Q47690\" name=\"UrIII\">UrIII</epoch>" + System.lineSeparator());
                                                    break;
                                                case "NeoBabylonian":
                                                    xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29688754\" start=\"-1000\" end=\"-600\" mainlocation=\"https://www.wikidata.org/wiki/Q47690\" name=\"NeoBabylonian\">NB</epoch>" + System.lineSeparator());
                                                    break;
                                                case "OldAssyrian":
                                                    xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29652754\" start=\"-1950\" end=\"-1530\" mainlocation=\"https://www.wikidata.org/wiki/Q41137\" name=\"OldAssyrian\">OA</epoch>" + System.lineSeparator());
                                                    break;
                                                case "MiddleAssyrian":
                                                    xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29652823\" start=\"-1530\" end=\"-1000\"  mainlocation=\"https://www.wikidata.org/wiki/Q41137\" name=\"MiddleAssyrian\">MA</epoch>" + System.lineSeparator());
                                                    break;
                                                case "NeoAssyrian":
                                                    xmltree.append("<epoch uri=\"https://www.wikidata.org/wiki/Q29688754\" start=\"-1000\" end=\"-600\"  mainlocation=\"https://www.wikidata.org/wiki/Q41137\" name=\"NeoAssyrian\">NA</epoch>" + System.lineSeparator());
                                                    break;
                                                case "Assyrian":
                                                    xmltree.append("<dialect uri=\"http://purl.org/olia/olia.owl\" name=\"Assyrian\">Assyrian</dialect>" + System.lineSeparator());
                                                    break;
                                                case "Babylonian":
                                                    writer.write("<dialect uri=\"http://purl.org/olia/olia.owl\" name=\"Babylonian\">Babylonian</dialect>" + System.lineSeparator());
                                                    break;
                                                case "SB":
                                                    writer.write("<dialect uri=\"http://purl.org/olia/olia.owl\" name=\"Standard Babylonian\">Standard Babylonian</dialect>" + System.lineSeparator());
                                                    break;
                                                case "Nuzi":
                                                    writer.write("<dialect uri=\"https://www.wikidata.org/wiki/Q29688886\" name=\"Nuzi\">Nuzi</dialect>" + System.lineSeparator());
                                                    break;
                                                case "Mari":
                                                    writer.write("<dialect uri=\"https://www.wikidata.org/wiki/Q29689558\" name=\"Mariotic\">Mariotic</dialect>" + System.lineSeparator());
                                                    break;
                                                default:

                                            }


                                        }}
                                    String cunei;
                                    if(attributes.getValue("cunei")==null){
                                        cunei = syllsep.cuneify(tocuneify);
                                    }else{
                                        cunei=attributes.getValue("cunei");
                                    }

                                    System.out.println(infinitive + " - " + cunei + " - " + postag + " - " + Arrays.toString(trans));
                                    xmltree.append(cunei.replaceAll("[0-9]",""));
                                    xmltree.append("</dictentry>").append(System.lineSeparator());
                                    this.wordToXMLTree.put(origvalue,xmltree.toString());
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
                        this.file2.put(origvalue, new MorphPattern());
                    }else{
                        if(origvalue!=null && !this.wordToAttributes.get(origvalue).containsKey("concept") && attributes.getValue("concept")!=null){
                            this.wordToAttributes.get(origvalue).put("concept",attributes.getValue("concept"));
                        }
                        if(origvalue!=null && !this.wordToAttributes.get(origvalue).containsKey("logogram") && attributes.getValue("logogram")!=null){
                            this.wordToAttributes.get(origvalue).put("logogram",attributes.getValue("logogram"));
                        }
                    }
                }break;
        }
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        FilterDictionariesAkk akk=new FilterDictionariesAkk("newwordmergedmatch2.xml","logogram",false,true);
        /*akk.refdict="akklog";
        SAXParserFactory.newInstance().newSAXParser().parse(new File("sumToAkkRel.xml_modified3_sum"),akk);
        */
        akk.file1bool=false;
        akk.refdict="epsd";
        akk.origvalueAttName="origvalue";
        SAXParserFactory.newInstance().newSAXParser().parse(new File("newwordsumerian___.xml_modified3_sum"),akk);
        akk.refdict="sumdict";
        akk.file1bool=false;
        akk.origvalueAttName="origvalue";
        SAXParserFactory.newInstance().newSAXParser().parse(new File("sumdict.xml_modified3_sum"),akk);
        System.out.println(akk.file1.keySet());
        System.out.println(akk.file2.keySet());
        for(String word:akk.wordToAttributes.keySet()){
            akk.writer.write("<dictentry ");
            for(String key:akk.wordToAttributes.get(word).keySet()){
                akk.writer.write(key+"=\""+akk.wordToAttributes.get(word).get(key)+"\" ");
            }
            akk.writer.write(">"+System.lineSeparator());
            akk.writer.write(akk.wordToXMLTree.get(word));
        }
        akk.writer.close();
    }

    /*public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
            FilterDictionariesAkk akk=new FilterDictionariesAkk("newwordmergedmatch.xml");
            akk.refdict="akkverb";
            SAXParserFactory.newInstance().newSAXParser().parse(new File("verbsakk.xml_modified3"),akk);
            akk.file1bool=false;
            akk.refdict="epsd";
           SAXParserFactory.newInstance().newSAXParser().parse(new File("newwordsShort2.xml_modified3_sum"),akk);
            System.out.println(akk.file1.keySet());
            System.out.println(akk.file2.keySet());
       // System.out.println(akk.file1.size());
        //System.out.println(akk.file2.size());
        System.out.println("NoFile: "+akk.notfileCounter);
        System.out.println("Matches: "+akk.matchCounter);
        akk.notfileCounter=0;
        akk.matchCounter=0;
        akk.file2.clear();
        akk.refdict="akkdict";
        SAXParserFactory.newInstance().newSAXParser().parse(new File("newwords22.xml_modified3_sum"),akk);
        System.out.println(akk.file1.size());
        System.out.println(akk.file2.size());
        System.out.println("NoFile: "+akk.notfileCounter);
        System.out.println("Matches: "+akk.matchCounter);
        akk.notfileCounter=0;
        akk.matchCounter=0;
        akk.file2.clear();
        akk.refdict="akklog";
        /*SAXParserFactory.newInstance().newSAXParser().parse(new File("sumToAkkRel.xml_modified3"),akk);
        //System.out.println(akk.file1.keySet());
        //System.out.println(akk.file2.keySet());
        System.out.println(akk.file1.size());
        System.out.println(akk.file2.size());
        System.out.println("NoFile: "+akk.notfileCounter);
        System.out.println("Matches: "+akk.matchCounter);
        for(String word:akk.wordToAttributes.keySet()){
            akk.writer.write("<dictentry ");
            for(String key:akk.wordToAttributes.get(word).keySet()){
                akk.writer.write(key+"=\""+akk.wordToAttributes.get(word).get(key)+"\" ");
            }
            akk.writer.write(">"+System.lineSeparator());
            akk.writer.write(akk.wordToXMLTree.get(word));
        }
            akk.writer.close();
        }
    /*public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        FilterDictionariesAkk akk=new FilterDictionariesAkk("newwordmergedmatch_hit.xml");
        akk.refdict="hitlex";
        SAXParserFactory.newInstance().newSAXParser().parse(new File("hitlex.xml_modified3_sum"),akk);
        System.out.println(akk.file1.size());
        System.out.println(akk.file2.size());
        System.out.println("NoFile: "+akk.notfileCounter);
        System.out.println("Matches: "+akk.matchCounter);
        akk.notfileCounter=0;
        akk.matchCounter=0;
        akk.file2.clear();
        akk.refdict="akklog";
        /*SAXParserFactory.newInstance().newSAXParser().parse(new File("sumToAkkRel.xml_modified3"),akk);
        //System.out.println(akk.file1.keySet());
        //System.out.println(akk.file2.keySet());
        System.out.println(akk.file1.size());
        System.out.println(akk.file2.size());
        System.out.println("NoFile: "+akk.notfileCounter);
        System.out.println("Matches: "+akk.matchCounter);
        for(String word:akk.wordToAttributes.keySet()){
            akk.writer.write("<dictentry ");
            for(String key:akk.wordToAttributes.get(word).keySet()){
                akk.writer.write(key+"=\""+akk.wordToAttributes.get(word).get(key)+"\" ");
            }
            akk.writer.write(">"+System.lineSeparator());
            akk.writer.write(akk.wordToXMLTree.get(word));
        }
        akk.writer.close();
    }*/
}

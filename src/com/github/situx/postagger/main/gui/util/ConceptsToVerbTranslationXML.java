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

import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created by timo on 11.05.17 .
 */
public class ConceptsToVerbTranslationXML extends DefaultHandler2 {

    private Map<String, String> origvalueToConcept;
    private Map<String, String> origvalueToPos;
    private Map<String, String> wordToURI;
    FileWriter writer;

    Boolean separate=true;

    java.util.Map<String,String> verbToURI;

    java.util.Map<String,String> wordToEpoch;

    SyllableSeparator syllsep;

    String attribute="origvalue";


    CuneiDictHandler dictHandler=new CuneiDictHandler(null,null,null) {
        @Override
        public void importMappingFromXML(String filepath) throws ParserConfigurationException, SAXException, IOException {

        }

        @Override
        public String translitWordToCunei(CuneiChar word) {
            return null;
        }

        @Override
        public void importDictFromXML(String filepath) throws ParserConfigurationException, SAXException, IOException {

        }

        @Override
        public void importReverseDictFromXML(String filepath) throws ParserConfigurationException, SAXException, IOException {

        }

        @Override
        public void parseDictFile(File file) throws IOException, ParserConfigurationException, SAXException {

        }
    };

    Integer matchcounter=0,matchcounter2=0;

    public ConceptsToVerbTranslationXML(String filepath,Boolean separate,String attribute) throws IOException {
        this.writer=new FileWriter(new File(filepath));
        this.separate=separate;
        this.syllsep=new SyllableSeparator();
        this.writer.write("<?xml version=\"1.0\"?>");
        this.writer.write(System.lineSeparator());
        this.writer.write("<translations>");
        this.writer.write(System.lineSeparator());
        this.attribute=attribute;
    }

    public ConceptsToVerbTranslationXML(String filepath,java.util.Map<String,String> verbToURI,java.util.Map<String,String> wordToURI,java.util.Map<String,String> wordToEpoch,java.util.Map<String,String> origvalueToConcept,java.util.Map<String,String> origvalueToPos,Boolean separate,String attribute) throws IOException {
        this.separate=separate;
        this.attribute=attribute;
        this.syllsep=new SyllableSeparator();
        this.writer=new FileWriter(new File(filepath));
        this.writer.write("<?xml version=\"1.0\"?>");
        this.writer.write(System.lineSeparator());
        this.writer.write("<translations>");
        this.writer.write(System.lineSeparator());
        this.verbToURI =verbToURI;
        this.wordToURI=wordToURI;
        this.wordToEpoch=wordToEpoch;
        this.origvalueToConcept=origvalueToConcept;
        this.origvalueToPos=origvalueToPos;
    }


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //switch(qName){
           // case "translation":
                try {
                    writer.write("<"+qName+" ");
                    for (int i = 0; i < attributes.getLength(); i++) {
                        if(!attributes.getQName(i).equals("cunei"))
                            writer.write(attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\" ");
                    }
                    if(attributes.getValue(attribute)!=null) {
                        String separated=attributes.getValue(attribute).toLowerCase().replace(".","-");
                        String reformatted=dictHandler.reformatToASCIITranscription(separated).replace("y","i");
                        if(attributes.getValue("epoch")==null && this.wordToEpoch!=null && this.wordToEpoch.containsKey(reformatted)){

                            writer.write("epoch=\""+wordToEpoch.get(reformatted)+"\" ");
                            System.out.println("Found epoch for: "+reformatted);
                        }
                        if(attributes.getValue("concept")==null && attributes.getValue("destvalue")!=null && this.origvalueToConcept!=null && this.origvalueToConcept.containsKey(attributes.getValue("destvalue")) && this.origvalueToConcept.get(attributes.getValue("destvalue"))!=null){
                            writer.write("concept=\""+origvalueToConcept.get(attributes.getValue("destvalue"))+"\" ");
                            System.out.println("Found concept for: "+attributes.getValue("destvalue"));
                        }
                        if(attributes.getValue("pos")==null && attributes.getValue("destvalue")!=null && this.origvalueToPos!=null && this.origvalueToPos.containsKey(attributes.getValue("destvalue")) && this.origvalueToPos.get(attributes.getValue("destvalue"))!=null){
                            writer.write("pos=\""+origvalueToPos.get(attributes.getValue("destvalue"))+"\" ");
                            System.out.println("Found concept for: "+attributes.getValue("destvalue"));
                        }
                        //System.out.println("Before Sep: "+separated);
                        if(separate){
                            separated=syllsep.separateAkkadian(attributes.getValue(attribute).replace("-","").replace("'","").replace("’","").trim(), "-").replace("ʾ", "").replace("(","").replace(")","").replace("--","-").replace("?","").replace(".","").replace("’","");
                        }

                        if(separated.endsWith("-m")){
                            separated=new StringBuilder(separated).deleteCharAt(separated.length()-2).toString();
                        }
                        reformatted=dictHandler.reformatToASCIITranscription(separated).replace("y","i");
                        //System.out.println(separated+" - "+reformatted);
                        String towrite=syllsep.cuneify(reformatted).replace(";", "").replace(",", "").replaceAll("[0-9]","");

                        System.out.println(separated+" - "+reformatted+" - "+towrite);
                        writer.write("cunei=\"" + towrite + "\" ");
                    }
                    if(this.verbToURI !=null && attributes.getValue("concept")==null) {
                        String destvalue = StringEscapeUtils.escapeXml10(attributes.getValue("destvalue"));
                        if (destvalue!=null && destvalue.contains("to ") || (attributes.getValue("pos") != null && attributes.getValue("pos").equals("VV"))) {
                            //destvalue = destvalue.replace("to ", "").trim();
                            if (destvalue.contains(",") || destvalue.contains(";")) {
                                String[] values = destvalue.split(",|;");
                                for (String val : values) {
                                    String newval = val.replace("to ", "").trim();
                                    if (this.verbToURI.containsKey(newval)) {
                                        System.out.println(newval + " - " + verbToURI.get(newval));
                                        this.matchcounter++;
                                        writer.write("concept=\"" + verbToURI.get(newval) + "\" ");
                                        if (attributes.getValue("pos") == null) {
                                            writer.write("pos=\"VV\" ");
                                        }
                                        //writer.write("/>"+System.lineSeparator());
                                        break;
                                    }

                                }

                                //destvalue=destvalue.substring(0,destvalue.indexOf(',')).trim();
                            } else {
                                destvalue = destvalue.replace("to ", "").trim();
                                if (this.verbToURI.containsKey(destvalue)) {
                                    System.out.println(attributes.getValue("destvalue") + " - " + verbToURI.get(destvalue));
                                    this.matchcounter++;
                                    writer.write("concept=\"" + verbToURI.get(destvalue) + "\" ");
                                    if (attributes.getValue("pos") == null) {
                                        writer.write("pos=\"VV\" ");
                                    }
                                    //writer.write("/>"+System.lineSeparator());


                                }
                            }


                        }else if(attributes.getValue("concept")!=null && attributes.getValue("pos")==null || (attributes.getValue("pos")!=null && !attributes.getValue("pos").equals("VV") && !attributes.getValue("pos").equals("NN"))){
                            if(destvalue!=null && this.wordToURI.containsKey(destvalue)){
                                writer.write("concept=\"" + wordToURI.get(destvalue) + "\" ");
                                System.out.println(attributes.getValue("destvalue") + " - " + wordToURI.get(destvalue));
                            }
                            this.matchcounter2++;
                        }

                    }


                    writer.write("/>"+System.lineSeparator());

                } catch (IOException e) {
                    e.printStackTrace();
                }
               // break;
        //}
    }



   /* @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            writer.write("<" + qName + " ");
            for (int i = 0; i < attributes.getLength(); i++) {
                writer.write(attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\" ");
            }
            if (this.verbToURI != null && attributes.getValue("concept") == null) {
                if(attributes.getValue("origvalue")!=null && verbToURI.containsKey(attributes.getValue("origvalue"))){
                    writer.write("concept=\""+verbToURI.get(attributes.getValue("origvalue"))+"\" ");
                }
            }
            writer.write("/>"+System.lineSeparator());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /*@Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        try {
            writer.write("<" + qName + " ");
            for (int i = 0; i < attributes.getLength(); i++) {
                if(!attributes.getQName(i).equals("cunei"))
                    writer.write(attributes.getQName(i) + "=\"" + attributes.getValue(i) + "\" ");
            }
            if(attributes.getValue("origvalue")!=null) {
                String separated=syllsep.separateAkkadian(attributes.getValue("origvalue"), "-").replace("ʾ", "").replace("(","").replace(")","").replace("--","-").replace("?","").replace(".","").replace("’","");
                if(separated.endsWith("-m")){
                    separated=new StringBuilder(separated).deleteCharAt(separated.length()-2).toString();
                }
                String reformatted=dictHandler.reformatToASCIITranscription(separated).replace("y","i");
                String towrite=syllsep.cuneify(reformatted).replace(";", "").replace(",", "").replaceAll("[0-9]","");

                System.out.println(separated+" - "+reformatted+" - "+towrite);
                writer.write("cunei=\"" + towrite + "\"");
            }
            /*if (this.verbToURI != null && attributes.getValue("concept") == null) {
                if(attributes.getValue("origvalue")!=null && verbToURI.containsKey(attributes.getValue("origvalue"))){
                    writer.write("concept=\""+verbToURI.get(attributes.getValue("origvalue"))+"\" ");
                }
            }
            writer.write("/>"+System.lineSeparator());
        }catch(Exception e){
            e.printStackTrace();
        }
    }*/

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        MapHandler maphandler=new MapHandler();
        SAXParserFactory.newInstance().newSAXParser().parse(new File("vn_small.xml"),maphandler);
        java.util.Map<String,String> verbToUri=maphandler.resultmap;
        maphandler=new MapHandler();
        SAXParserFactory.newInstance().newSAXParser().parse(new File("wn_small.xml"),maphandler);
        java.util.Map<String,String> wordToUri=maphandler.resultmap;
        maphandler=new MapHandler();
        SAXParserFactory.newInstance().newSAXParser().parse(new File("ur3wordlist20110805.xml"),maphandler);
        java.util.Map<String,String> wordToEpoch=maphandler.resultmap;
        maphandler=new MapHandler("destvalue","concept");
        SAXParserFactory.newInstance().newSAXParser().parse(new File("newwordssumerian.xml"),maphandler);
        java.util.Map<String,String> origvalueToConcept=maphandler.resultmap;
        maphandler=new MapHandler("destvalue","pos");
        //SAXParserFactory.newInstance().newSAXParser().parse(new File("newwordssumerian.xml"),maphandler);
        java.util.Map<String,String> origvalueToPos=maphandler.resultmap;
        System.out.println("WordToEpoch: "+wordToEpoch);
        String[] files=new String[]{/*"newwords22.xml","verbsakk.xml","newwordsShort2.xml",/*"sumToAkkRel.xml",*/"sumdict.xml",/*"newwordsumerian___.xml?",*/"hitlex.xml"};
        ConceptsToVerbTranslationXML contover;
        File file;
        /*file=new File("sumToAkkRel.xml");
        System.out.println(verbToUri.size());
        contover=new ConceptsToVerbTranslationXML(file.getName()+"_modified3_sum",verbToUri,wordToUri,wordToEpoch,origvalueToConcept,origvalueToPos,false,"logogram");
        SAXParserFactory.newInstance().newSAXParser().parse(file,contover);
        contover.writer.write("</translations>");
        contover.writer.close();
        System.out.println(contover.matchcounter+" verbnet matches");
        System.out.println(contover.matchcounter2+" wordnet matches");*/
        for(String filepath:files){
            file=new File(filepath);
            System.out.println(verbToUri.size());
            contover=new ConceptsToVerbTranslationXML(file.getName()+"_modified3_sum",verbToUri,wordToUri,wordToEpoch,origvalueToConcept,origvalueToPos,true,"origvalue");
            SAXParserFactory.newInstance().newSAXParser().parse(file,contover);
            contover.writer.write("</translations>");
            contover.writer.close();
            System.out.println(contover.matchcounter+" verbnet matches");
            System.out.println(contover.matchcounter2+" wordnet matches");
        }


    }
}

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

package com.github.situx.postagger.methods.textanalysis.parser;

import com.github.situx.postagger.dict.utils.Tablet;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.palette.ColorPalette;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.github.situx.postagger.dict.DictWebCrawler;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.utils.Epoch;
import com.github.situx.postagger.dict.utils.MatrixCalc;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.*;

import java.net.URLEncoder;
import java.util.*;
import java.util.List;

/**
 * Created by timo on 11.06.17 .
 */
public class ATFParser extends DefaultHandler2 {

    BufferedReader reader;

    Map<String,MatrixCalc> langToEpochToTermDocumentMatrix=new TreeMap<>();

    Map<Tablet,Tuple<String,Double>> documentCoocurrence=new TreeMap<>();
    Map<String,Map<String,Map<String,Double>>> langCoocurrence=new TreeMap<>();
    Map<Tablet,Map<String,Map<String,Double>>> bigramCoocurrence=new TreeMap<>();

    WKTReader wktreader=new WKTReader();

    MatrixCalc matrix;

    public Map<String,Map<String,Tablet>> locationToLangToTablets=new TreeMap<>();

    BufferedWriter geojsonwriter=new BufferedWriter(new FileWriter("/home/timo/SemanticDictionary/js/wordmap.js"));

    public Map<String,Tablet> strToMap=new TreeMap<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName){
            case "tablet":
                Tablet tablet=new Tablet();
                tablet.tabletID=attributes.getValue("id");
                tablet.place=attributes.getValue("place");
                tablet.objectType=attributes.getValue("objectType");
                tablet.museumID=attributes.getValue("museumID");
                tablet.genre=attributes.getValue("genre");
                tablet.collection=attributes.getValue("collection");
                tablet.langStr=attributes.getValue("lang");
                if(attributes.getValue("point").contains("POINT")){
                    try {
                        tablet.point= (Point) this.wktreader.read(attributes.getValue("point"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                tablet.epochs.add(new Epoch(attributes.getValue("epoch"),""));
                this.strToMap.put(tablet.tabletID,tablet);

        }
    }

    public ATFParser(String filepath) throws IOException, ParserConfigurationException, SAXException {
        this.reader=new BufferedReader(new FileReader(new File(filepath)));
        SAXParserFactory.newInstance().newSAXParser().parse("cdlitabletparse.xml",this);
    }

    public void integrateMaps(){

    }

    public void parseATFFile() throws IOException {
        DictHandling dicth=CharTypes.SUMERIAN.getCorpusHandlerAPI().getUtilDictHandler();
        String line,lastword=null;
        Tablet currenttablet=null;
        Integer linecounter=0;
        Integer linestart=1732000;
        Integer linestop=1732000;
        Integer tabletcounter=1;
        boolean nosux=false;
        //FileWriter writer=new FileWriter(new File("cdlitabletparse.xml"),true);
        //writer.write("<?xml version=\"1.0\"?>"+System.lineSeparator()+"<tablets>"+System.lineSeparator());
        geojsonwriter.write("var wordmap={\"type\":\"FeatureCollection\","+System.lineSeparator()+"\"features\":[");
        while((line=this.reader.readLine())!=null && linecounter<linestop){
            System.out.println("Line: "+linecounter++);
            //System.out.println("Matches Line? "+line+" - "+line.matches("^[0-9].*"));
            if(line.startsWith("&")) {
                if(currenttablet!=null && currenttablet.place!=null && currenttablet.langStr!=null) {
                    currenttablet.place=currenttablet.place.replace("?","").trim();
                    currenttablet.langStr=currenttablet.langStr.replace("?","").trim();
                    if (!this.locationToLangToTablets.containsKey(currenttablet.place)) {
                        this.locationToLangToTablets.put(currenttablet.place, new TreeMap<>());
                    }
                    if (!this.locationToLangToTablets.get(currenttablet.place).containsKey(currenttablet.langStr)) {
                        this.locationToLangToTablets.get(currenttablet.place).put(currenttablet.langStr, currenttablet);
                    }
                        Tablet tab = this.locationToLangToTablets.get(currenttablet.place).get(currenttablet.langStr);
                        for (String key : currenttablet.keyWordToOccurance.keySet()) {
                            if (!tab.keyWordToOccurance.containsKey(key)) {
                                tab.keyWordToOccurance.put(key, currenttablet.keyWordToOccurance.get(key));
                            } else {
                                tab.keyWordToOccurance.put(key, tab.keyWordToOccurance.get(key) + currenttablet.keyWordToOccurance.get(key));
                            }
                        }
                        tab.tabletCount++;

                }
                String[] splitted = line.split("=");
                if(this.strToMap.containsKey(splitted[0].replace("&",""))){
                    currenttablet=this.strToMap.get(splitted[0].replace("&",""));
                }else{
                    currenttablet = new Tablet();
                    currenttablet.tabletID = splitted[0];
                    System.out.println("TabletID: "+currenttablet.tabletID+" - "+ URLEncoder.encode("&")+currenttablet.tabletID.substring(1));
                    currenttablet = DictWebCrawler.crawlCDLIForTabletMetadata(currenttablet.tabletID.substring(1));
                    currenttablet.tabletID = splitted[0].replace("&","");
                    //System.out.println(currenttablet.toXML());
                    // writer.write(currenttablet.toXML()+System.lineSeparator());
                    if(splitted.length>1)
                        currenttablet.museumID = splitted[1];
                }
                lastword=null;
                System.out.println("Tablets: "+tabletcounter++);
            }else if(line.startsWith("#atf: lang") && !line.contains("sux")){
                nosux=true;
            }else if(line.startsWith("#atf: lang") && line.contains("sux")){
                nosux=false;
            } else if(!nosux && line.matches("^[0-9].*") && currenttablet!=null){
                line=line.substring(line.indexOf('.')+1);
                line=line.replace("[...]","").replace("#","").replace("?","").replace("<","").replace(">","").replace("[","").replace("]","").replace("*","").replace("~","-").toLowerCase();
                String[] words=line.split(" ");
                System.out.println(Arrays.toString(words));
                //DictHandling dicthandler= currenttablet.chartype.getCorpusHandlerAPI().getUtilDictHandler();
                if(currenttablet.langStr==null)
                    currenttablet.langStr="undefined";
                if(!langToEpochToTermDocumentMatrix.containsKey(currenttablet.langStr)){
                    langToEpochToTermDocumentMatrix.put(currenttablet.langStr,new MatrixCalc(new TreeMap<>(),null,null));
                }
                MatrixCalc curmat=langToEpochToTermDocumentMatrix.get(currenttablet.langStr);
                if(!curmat.wordmap.containsKey(currenttablet)){
                    curmat.wordmap.put(currenttablet,new TreeMap<>());
                }
                Integer matchedwords=0;
                for(String word:words){
                    if(word.trim().isEmpty()){
                        continue;
                    }
                    boolean matched=false;
                    if(dicth.getTranslitToWordDict().containsKey(word)){
                        LangChar lchar=dicth.getDictionary().get(dicth.getTranslitToWordDict().get(word));
                        System.out.println("Matched word: "+word);
                        word=word+" ("+lchar.getFirstTranslation(Locale.ENGLISH)+" - "+lchar.getConceptURI()+")";
                        System.out.println("Result: "+word);
                        matchedwords++;
                    }else{
                        while(word.contains("-")){
                            word=word.substring(0,word.lastIndexOf('-'));
                            if(dicth.getTranslitToWordDict().containsKey(word)){
                                LangChar lchar=dicth.getDictionary().get(dicth.getTranslitToWordDict().get(word));
                                System.out.println("Matched word: "+word);
                                word=word+" ("+lchar.getFirstTranslation(Locale.ENGLISH)+" - "+lchar.getConceptURI()+")";
                                System.out.println("Result: "+word);
                                matchedwords++;
                                break;
                            }
                        }
                    }
                    /*if(lastword!=null){
                        if(!bigramCoocurrence.containsKey(currenttablet)){
                            bigramCoocurrence.put(currenttablet,new TreeMap<>());
                        }
                        if(!bigramCoocurrence.get(currenttablet).containsKey(word)){
                            bigramCoocurrence.get(currenttablet).put(word,new TreeMap<>());
                        }
                        if(!bigramCoocurrence.get(currenttablet).get(word).containsKey(lastword)){
                            bigramCoocurrence.get(currenttablet).get(word).put(lastword,0.);
                        }
                        bigramCoocurrence.get(currenttablet).get(word).put(word,bigramCoocurrence.get(currenttablet).get(word).get(lastword)+1);
                    }*/
                    currenttablet.totalWords+=words.length;
                    currenttablet.matchedwords+=matchedwords;
                    if(!currenttablet.keyWordToOccurance.containsKey(word)){
                        currenttablet.keyWordToOccurance.put(word,0.);
                    }
                    currenttablet.keyWordToOccurance.put(word,currenttablet.keyWordToOccurance.get(word)+1);
                    lastword=word;
                }
            }
        }
        System.out.println(this.locationToLangToTablets.toString());
        for(String key:locationToLangToTablets.keySet()){
            for(Tablet tab:locationToLangToTablets.get(key).values()){
                if(tab!=null) {
                    String geoj = tab.toGeoJSON();
                    if (geoj!=null && !geoj.trim().isEmpty()) {
                        geojsonwriter.write(tab.toGeoJSON()+ "," + System.lineSeparator() );
                    }
                    geojsonwriter.flush();
                    final List<WordFrequency> wordFrequencies = tab.getWordFrequencies();
                    //System.out.println(wordFrequencies);
                    final Dimension dimension = new Dimension(500, 312);
                    final WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
                    wordCloud.setPadding(2);
                    wordCloud.setBackgroundColor(Color.BLACK);
                    //wordCloud.setBackground(new PixelBoundryBackground("backgrounds/whale_small.png"));
                    wordCloud.setColorPalette(new ColorPalette(new Color(0x4055F1), new Color(0x408DF1), new Color(0x40AAF1), new Color(0x40C5F1), new Color(0x40D3F1), new Color(0xFFFFFF)));
                    wordCloud.setFontScalar(new LinearFontScalar(10, 40));
                    wordCloud.build(wordFrequencies);
                    wordCloud.writeToFile("/home/timo/SemanticDictionary/img/"+tab.langStr+"_"+(tab.epochs.iterator().hasNext()?tab.epochs.iterator().next().getName():"").replace("?","_")+"_"+tab.place.replace("?","_")+".png");
                }
            }
        }
        geojsonwriter.write("]}");
        geojsonwriter.close();
        /*String graphml=this.coocToGraphML(bigramCoocurrence);
        //writer.write("</tablets>");
        //writer.close();
        FileWriter writer=new FileWriter(new File("bigram.graphml"));
        writer.write(graphml);
        writer.close();*/
        /*for(String lang:langToEpochToTermDocumentMatrix.keySet()){
            langToEpochToTermDocumentMatrix.get(lang).exportMatrixToFile();
        }*/
    }

    public String coocToGraphML(Map<Tablet,Map<String,Map<String,Double>>> coocmap) throws IOException {
        Map<String,Double> mostconnected=new TreeMap<>();
        Map<String,Map<String,Double>> mostconnected2=new TreeMap<>();
        StringBuilder builder=new StringBuilder(),edgebuilder=new StringBuilder();
        FileWriter tabconcepts=new FileWriter(new File("tabconcepts.txt"));
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  \n" + "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + "      xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n" + "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">").append(System.lineSeparator());
        builder.append("<graph id=\"G\" edgedefault=\"directed\">").append(System.lineSeparator());
        int nodeid=0,keyid=0,edgeid=0;
        Map<String,String> strToNodeID=new TreeMap<>();
        for(Tablet tab:coocmap.keySet()) {
            tabconcepts.write(tab.tabletID+"{"+System.lineSeparator());
            for (String node : coocmap.get(tab).keySet()) {
                strToNodeID.put(node, "n" + nodeid);
                builder.append("<node id=\"n").append(nodeid++).append("\">").append(System.lineSeparator());
                builder.append("<data key=\"d").append(keyid++).append("\">").append(node).append("</data>");
                builder.append("</node>").append(System.lineSeparator());
                for (String tup : coocmap.get(tab).get(node).keySet()) {
                    if (!strToNodeID.containsKey(tup)) {
                        strToNodeID.put(tup,"n"+nodeid);
                        builder.append("<node id=\"n").append(nodeid++).append("\">").append(System.lineSeparator());
                        if(tup.contains(" - ")){
                            builder.append("<data key=\"d").append(keyid++).append("\">").append(node).append("</data>");
                            builder.append("<data key=\"d").append(keyid++).append("\">").append(tup.substring(0,tup.indexOf(" - "))).append("</data>");
                            builder.append("<data key=\"d").append(keyid++).append("\">").append(tup.substring(tup.indexOf(" - "))).append("</data>");
                            tabconcepts.write(tup+","+System.lineSeparator());
                        }else {
                            builder.append("<data key=\"d").append(keyid++).append("\">").append(tup).append("</data>");
                        }
                        builder.append("</node>").append(System.lineSeparator());
                    }
                    if(tup!=null && node!=null && strToNodeID.get(tup)!=null) {
                        if(!mostconnected.containsKey(tup)) {
                            mostconnected.put(tup,0.);
                        }
                        mostconnected.put(tup,mostconnected.get(tup)+1);
                        if(!mostconnected.containsKey(node)) {
                            mostconnected.put(node,0.);
                        }
                        mostconnected.put(node,mostconnected.get(node)+1);
                        if(!mostconnected2.containsKey(tup)){
                            mostconnected2.put(tup,new TreeMap<>());
                        }
                        if(!mostconnected2.get(tup).containsKey(node)){
                            mostconnected2.get(tup).put(node,0.);
                        }
                        mostconnected2.get(tup).put(node,mostconnected2.get(tup).get(node)+1);
                    }
                    edgebuilder.append("<edge id=\"e").append(edgeid++).append("\" source=\"n").append(nodeid).append("\" target=\"").append(strToNodeID.get(tup)).append("\">").append(System.lineSeparator());
                    edgebuilder.append("<data key=\"d1\">").append(coocmap.get(tab).get(node).get(tup)).append("</data>").append(System.lineSeparator());
                    edgebuilder.append("</edge>").append(System.lineSeparator());
                }
            }
            tabconcepts.write("}"+System.lineSeparator());
        }
        builder.append(edgebuilder.toString());
        builder.append("</graph>").append(System.lineSeparator());
        builder.append("</graphml>").append(System.lineSeparator());
        tabconcepts.close();
        mostconnected = sortByValue(mostconnected);

        FileWriter writer=new FileWriter(new File("largestconnectedcomponents.txt"));
        for(String key:mostconnected.keySet()){
            if(key.contains("http")){
                writer.write(key+" -> "+mostconnected.get(key)+System.lineSeparator());
            }

        }
        writer.close();
        writer=new FileWriter(new File("largestconnectedcomponents2.txt"));
        for(String key:mostconnected2.keySet()){
            if(key.contains("http")){
                writer.write(key+" -> {"+System.lineSeparator());
                for(String key2:mostconnected2.get(key).keySet()){
                    if(key2.contains("http")) {
                        writer.write("                    " + key2 + "=" + mostconnected2.get(key).get(key2) + System.lineSeparator());
                    }
                }
                writer.write("}"+System.lineSeparator());
            }

        }
        writer.close();
        return builder.toString();
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> e1, Map.Entry<K, V> e2) {
                return (e1.getValue()).compareTo(e2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public void tfidfToGraphML(){
        StringBuilder builder=new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  \n" +
                "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "      xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n" +
                "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
    }

    public void termDocumentMatrixToGraphML(){
        StringBuilder builder=new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"  \n" +
                "      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "      xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns \n" +
                "        http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">");
    }

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        ATFParser parser=new ATFParser("cdli_atffull.atf");
        parser.parseATFFile();
    }


}

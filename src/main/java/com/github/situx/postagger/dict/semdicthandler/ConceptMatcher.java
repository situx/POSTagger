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

package com.github.situx.postagger.dict.semdicthandler;

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.corpusimport.util.dictToLemon;
import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.utils.*;
import com.github.situx.postagger.main.gui.ime.descriptor.GenericInputMethodDescriptor;
import com.github.situx.postagger.main.gui.ime.jquery.tree.builder.TreeBuilder;
import com.github.situx.postagger.main.gui.util.RelationParser;
import com.github.situx.postagger.main.gui.util.SyllableSeparator;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.ExportMethods;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.XSD;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.semdicthandler.conceptresolver.connector.TripleStoreConnector;
import com.github.situx.postagger.dict.semdicthandler.conceptresolver.connector.WikiDataConnection;
import com.github.situx.postagger.dict.utils.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by timo on 24.05.16.
 * Class to match possible concepts in nouns in recognized words of a given text.
 * LOD Cloud
 */
public class ConceptMatcher {

    private static final String ENDPOINT="";

    private static final Integer FONTSIZE=18;

    private static ConceptMatcher instance;

    private List<TripleStoreConnector> connectorList;

    public static ConceptMatcher getInstance() {
        if(instance==null) {
            instance=new ConceptMatcher();
        }
        return instance;
    }

    private ConceptMatcher(){
        this.connectorList=new LinkedList<>();
        this.connectorList.add(new WikiDataConnection());
    }

    public String resolveConcept(String queryTerm,String language) {
        //System.out.println("Query: "+queryTerm);
        for(TripleStoreConnector connector:this.connectorList){
            Set<String> result=connector.matchConceptsByLabel(queryTerm,language);
            if(!result.isEmpty())
                return result.iterator().next();
        }
        return null;
    }


    public OntModel createOntModelForTablet(String tabletnumber, String originlanguage, String dialect, String tablettext, POSTagger postagger){
        OntModel model= ModelFactory.createOntologyModel();
        OntClass classs=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Tablet");
        DatatypeProperty prop=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#tabletnumber");
        prop.addDomain(classs);
        prop.addRange(XSD.xstring);
        DatatypeProperty prop2=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#language");
        prop2.addDomain(classs);
        prop2.addRange(XSD.xstring);
        DatatypeProperty prop3=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#dialect");
        prop3.addDomain(classs);
        prop3.addRange(XSD.xstring);
        DatatypeProperty prop4=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#epoch");
        prop4.addDomain(classs);
        prop4.addRange(XSD.xstring);
        //TODO For every word (noun) on this tablet create a link into the semantic web
        return model;
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }


    public OntModel createOntModelForTerms(String filepath) throws IOException {
        OntModel model= ModelFactory.createOntologyModel();
        dictToLemon dtoL=new dictToLemon(CharTypes.HITTITE,model,null);
        File file=new File("result.txt");
        BufferedWriter writer=new BufferedWriter(new FileWriter(file));
        String filecon=readFile(filepath,Charset.defaultCharset());
        Integer terms=0,nonNull=0;
        model=dtoL.createNewLemonDictFromResource(model,null);
        for(String word:filecon.split("[,;@/\n]")){
            word=word.trim();
            word=word.replace("?","");
            if(word.isEmpty())
                continue;
            try {
                String res = this.resolveConcept(word, (!word.isEmpty() && Character.isUpperCase(word.charAt(0)) ? "de" : "en"));
                if (res != null) {
                    nonNull++;
                    dtoL.wordCombToLemon("null",word,res,null,CharTypes.HITTITE,model,null,CharTypes.HITTITE.getCorpusHandlerAPI().getUtilDictHandler(), true);
                }
                terms++;
                System.out.println(word + " - " + res);
                writer.write(word + " - " + res + System.lineSeparator());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        System.out.println(nonNull+"/"+terms+" recognized");
        writer.write(nonNull+"/"+terms+" recognized");
        writer.close();
        model.write(new FileWriter(new File("mylemonoutputmodelll.owl")));
        return model;
    }

    public OntModel createOntModelForDictionary(CharTypes chartype,String path,String comment,boolean map) throws IOException {
        OntModel model= ModelFactory.createOntologyModel();
        dictToLemon dtoL=new dictToLemon(chartype,model,comment);
        model=dtoL.createNewLemonDictFromResource(model,comment);
        int terms=0;
        for(LangChar word:chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictionary().values()){
            dtoL.wordCombToLemon(word.getCharacter(),word.getTransliterationSet().iterator().next().toString(),word.getConceptURI(),null,chartype,model,word,chartype.getCorpusHandlerAPI().getUtilDictHandler(),map);
            terms++;
        }
        System.out.println(terms+" recognized");
        StringWriter strw=new StringWriter();
        model.write(strw,"TTL");
        FileWriter writer=new FileWriter(new File(path+chartype.getSmallstr()+".ttl"));
        writer.write(strw.toString().replace("@en","@"+chartype.getLocale()));
        writer.close();
        return model;
    }

    public static void createAnkiDeckForDictionary(CharTypes chartype,String path) throws IOException {
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File(path)));
        int terms=0;
        for(LangChar word:chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictionary().values()){
            writer.write("Transliteration of "+word.getCharacter()+"?");
            writer.write(";");
            writer.write(word.getTransliterationSet().toString());
            writer.write(System.lineSeparator());
            writer.write("Sign for "+word.getTransliterationSet().toString()+"?");
            writer.write(";");
            writer.write(word.getCharacter());
            writer.write(System.lineSeparator());
            writer.write("Translations for "+word.getTransliterationSet().toString()+"?");
            writer.write(";");
            writer.write(word.getTranslationSet(Locale.ENGLISH).keySet().toString().replace("{","").replace("}",""));
            writer.write(System.lineSeparator());
            writer.write("Translations for "+word.getCharacter()+"?");
            writer.write(";");
            writer.write(word.getTranslationSet(Locale.ENGLISH).keySet().toString().replace("{","").replace("}",""));
            writer.write(System.lineSeparator());
        }
        writer.close();
    }


    private static void createHomepageCharList(CharTypes chartype,String jsexport){
        StringBuilder builderw2UI=new StringBuilder();
        builderw2UI.append("var "+chartype.getSmallstr()+"_map={"+System.lineSeparator()+" \"name\": \""+chartype.getSmallstr()+"\","+System.lineSeparator()+" \"header\":\""+chartype.getSmallstr()+" Dictionary\","+System.lineSeparator()+"\"show\":{" +System.lineSeparator()+"\"toolbar\":true,"+System.lineSeparator()+"\"footer\":true"+System.lineSeparator()+"},"+System.lineSeparator()+"\"columns\":["+System.lineSeparator());
        builderw2UI.append("{\"field\":\"script\",\"caption\":\"Script\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\",\"style\":\"font-family:"+chartype.getSmallstr()+";font-size:"+FONTSIZE+"px;\"},"+System.lineSeparator());
        builderw2UI.append("{\"field\":\"transliteration\",\"caption\":\"Transliteration\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"},"+System.lineSeparator());
        builderw2UI.append("{\"field\":\"charname\",\"caption\":\"charname\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"},"+System.lineSeparator());
        builderw2UI.append("{\"field\":\"meaning\",\"caption\":\"Meaning\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\","+System.lineSeparator() +
                "    render:function(record){"+System.lineSeparator() +
                "        if(record.concept!=undefined){"+System.lineSeparator() +
                "            return \"<a href=\\\"\"+record.concept+\"\\\" target=\\\"_blank\\\">\"+record.translation+\"</a>\""+System.lineSeparator() +
                "        }else{"+System.lineSeparator() +
                "            return record.translation;"+System.lineSeparator() +
                "        }"+System.lineSeparator() +
                "}},"+System.lineSeparator());
        builderw2UI.append("{\"field\":\"got\",\"caption\":\"Gottstein\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"}"+System.lineSeparator());
        builderw2UI.append("]"+System.lineSeparator()+",\"records\":["+System.lineSeparator());
        for(LangChar lchar:chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictMap().values()){
            builderw2UI.append("{\"script\":\""+lchar.getCharacter()+"\",\"transliteration\":\""+lchar.getTransliterationSet().iterator().next()+"\",\"charname\":\""+lchar.getCharName()+"\",\"meaning\":\""+lchar.getMeaning()+"\",\"got\":\""+lchar.getPaintInformation()+"\"},");
        }
        builderw2UI.delete(builderw2UI.length()-2,builderw2UI.length());
        builderw2UI.append("]}");
        try {
            FileWriter writer=new FileWriter(new File(jsexport));
            writer.write(builderw2UI.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createHomepageDictionary(CharTypes chartype,String relationsfile,String jsexport){
        StringBuilder builder=new StringBuilder(),builderw2UI=new StringBuilder();
        builderw2UI.append("var "+chartype.getSmallstr()+"={"+System.lineSeparator()+" \"name\": \""+chartype.getSmallstr()+"\","+System.lineSeparator()+" \"header\":\""+chartype.getSmallstr()+" Dictionary\","+System.lineSeparator()+"\"selectType\":\"cell\","+System.lineSeparator()+"\"show\":{" +System.lineSeparator()+"\"toolbar\":true,"+System.lineSeparator()+"\"footer\":true"+System.lineSeparator()+"},"+System.lineSeparator()+"\"columns\":["+System.lineSeparator());
        builderw2UI.append("{\"field\":\"script\",\"caption\":\"Script\",\"selectable\":true,\"sortable\":true,\"resizable\":true,\"size\":\"20%\",\"style\":\"font-family:"+chartype.getSmallstr()+";font-size:"+FONTSIZE+"px;\"},"+System.lineSeparator());
        builderw2UI.append("{\"field\":\"transliteration\",\"caption\":\"Transliteration\",\"selectable\":true,\"sortable\":true,\"resizable\":true,\"size\":\"20%\",},"+System.lineSeparator());
        builderw2UI.append("{\"field\":\"transcription\",\"caption\":\"Transcription\",\"selectable\":true,\"sortable\":true,\"resizable\":true,\"size\":\"20%\"},"+System.lineSeparator());
        builderw2UI.append("{\"field\":\"translation\",\"caption\":\"Translation\",\"selectable\":true,\"sortable\":true,\"resizable\":true,\"size\":\"20%\","+System.lineSeparator() +
                "    render:function(record){\n" +
                        "        result=\"\"\n" +
                        "        if(record.concept!=undefined && record.concept!=\"\"){\n" +
                        "            result+=\"<a href=\\\"\"+record.concept+\"\\\" target=\\\"_blank\\\">\"\n" +
                        "            result+=record.translation\n" +
                        "            if(record.meaning!=undefined && record.meaning!=\"\"){\n" +
                        "                    result+=\" (\"+record.meaning+\")\"\n" +
                        "            }\n" +
                        "            result+=\"</a>\"\n" +
                        "        }else{\n" +
                        "            result=record.translation\n" +
                        "             if(record.meaning!=undefined && record.meaning!=\"\"){\n" +
                        "                 result+=\" (\"+record.meaning+\")\"\n" +
                        "             }\n" +
                        "        }\n" +
                        "        return result\n" +
                        "}},"+System.lineSeparator());
        builderw2UI.append("{\"field\":\"pos\",\"caption\":\"POSTag\",\"selectable\":true,\"sortable\":true,\"resizable\":true,\"size\":\"20%\"},"+System.lineSeparator());
        builderw2UI.append("{\"field\":\"ref\",\"caption\":\"Reference\",\"selectable\":true,\"sortable\":true,\"resizable\":true,\"size\":\"20%\"}"+System.lineSeparator());
        builderw2UI.append("]"+System.lineSeparator()+",\"records\":["+System.lineSeparator());
        Integer recid=0;
        Map<String,Map<String,String>> relationss=new TreeMap<>();
        SyllableSeparator syllsep=null;
        try {
            syllsep=new SyllableSeparator();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(relationsfile!=null){
            RelationParser relparser=new RelationParser();
            try {
                SAXParserFactory.newInstance().newSAXParser().parse(new File(relationsfile),relparser);
                relationss=relparser.relations;
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }
        }

        CuneiDictHandler cunhandler=new CuneiDictHandler(null,null,null) {
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
        DictHandling handler=chartype.getCorpusHandlerAPI().getUtilDictHandler();
        builder.append("<?xml version=\"1.1\"?>");
        builder.append("<dictentries>");
        for(Set<MorphPattern> patterns:handler.morphpattern.values()){
            for(MorphPattern pat:patterns){
                builder.append(pat.toXML());
            }
        }
        FileWriter missingakkwriter=null;
        try {
            missingakkwriter=new FileWriter(new File("missingakk.xml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Dictionary: "+handler.getDictionary().keySet().size());
        for (String key : handler.getDictionary().keySet()) {
            LangChar curchar=handler.getDictionary().get(key);
            Transliteration foundstem=null;
            for(Transliteration trans:curchar.getTransliterationSet()) {
                if(trans.getStem()){
                    foundstem=trans;
                    continue;
                }
                builderw2UI.append("{\"recid\":"+recid+++",\"concept\":\""+curchar.getConceptURI()+"\",");
                builder.append("<dictentry concept=\"").append(curchar.getConceptURI()).append("\" ");
                if(curchar.getMeaning()!=null && !curchar.getMeaning().isEmpty()){
                    builder.append(" meaning=\""+curchar.getMeaning()+"\" ");
                    builderw2UI.append("\"meaning\":\""+curchar.getMeaning()+"\", ");
                }else {
                    builder.append(" meaning=\"");
                    builderw2UI.append("\"meaning\":\"");
                    String[] spl = trans.getTransliteration().split("-");
                    StringBuilder builder2 = new StringBuilder();
                    boolean append = true;
                    if (spl.length > 1) {
                        for (String str : trans.getTransliteration().split("-")) {
                            String str2 = cunhandler.reformatToUnicodeTranscription(str);
                            System.out.println("Working on: " + trans.getTransliteration() + " - " + str2);
                            System.out.println("Contained! " + handler.getTranslitToWordDict().containsKey(str2));
                            if (handler.getTranslitToWordDict().containsKey(str2)) {
                                //System.out.println(str2 + " in " + handler.getDictionary().get(handler.getTranslitToWordDict().get(str2)).getTransliterationSet() + "? ");
                                for (Transliteration translit : handler.getDictionary().get(handler.getTranslitToWordDict().get(str2)).getTransliterationSet()) {
                                    // System.out.println(translit.getTransliteration() + " - " + str + " - " + translit.getTransliteration().equals(str));
                                    if (translit.getTransliteration().equals(str)) {
                                        // System.out.println("Translations: " + translit.getTranslations());
                                        builder2.append(translit.getTranslations().iterator().next().getTranslation()).append("-");
                                    }
                                }
                            } else {
                                append = false;
                                builder2.append("?-");
                            }
                        }
                        if (builder2.length() > 0 && builder2.charAt(builder2.length() - 1) == '-')
                            builder2.delete(builder2.length() - 1, builder2.length());
                    }
                    if (append) {
                        builder.append(builder2.toString());
                        builderw2UI.append(builder2.toString());
                    }
                    builder.append("\" ");
                    builderw2UI.append("\", ");
                }
                builder.append("ref=\"").append(curchar.getRefString());
                builder.append("\"");
                builderw2UI.append("\"ref\":\""+curchar.getRefString()+"\",");

                System.out.println(trans.getTransliteration()+" contained? "+relationss.containsKey(trans.getTransliteration()));
                System.out.println(cunhandler.reformatToUnicodeTranscription(trans.getTransliteration())+" contained? "+relationss.containsKey(cunhandler.reformatToUnicodeTranscription(trans.getTransliteration())));
                //System.out.println(relationss.keySet());
                if(!curchar.getLogographs().isEmpty()){

                    builder.append(" logogram=\"").append(StringEscapeUtils.escapeXml10(curchar.getLogographs().iterator().next())+"\" ");
                    builderw2UI.append("\"logogram\":\""+StringEscapeUtils.escapeXml10(curchar.getLogographs().iterator().next())+"\",");
                }else {

                    if (relationss.containsKey(cunhandler.reformatToUnicodeTranscription(trans.getTransliteration()))) {
                        builder.append(" logogram=\"");
                        builderw2UI.append("\"logogram\":\"");
                        Set<String> logograms = relationss.get(cunhandler.reformatToUnicodeTranscription(trans.getTransliteration())).keySet();
                        for (String log : logograms) {
                            try {
                                builder.append(log.replace("&", "&amp;").toUpperCase()).append(",").append(syllsep.cuneify(log).replace("&", "&amp;")).append(";");
                                builderw2UI.append(log.replace("&", "&amp;").toUpperCase()).append(",").append(syllsep.cuneify(log).replace("&", "&amp;")).append(";");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        builder.append("\"");
                        builderw2UI.append("\",");
                    } else if (relationss.containsKey(trans.getTransliterationString())) {
                        builder.append(" logogram=\"");
                        builderw2UI.append("\"logogram\":\"");
                        Set<String> logograms = relationss.get(trans.getTransliterationString()).keySet();
                        for (String log : logograms) {
                            try {
                                builder.append(log.replace("&", "&amp;").toUpperCase()).append(",").append(syllsep.cuneify(log).replace("&", "&amp;")).append(";");
                                builderw2UI.append(                                log.replace("&", "&amp;").toUpperCase()).append(",").append(syllsep.cuneify(log).replace("&", "&amp;")).append(";");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        builder.append("\"");
                        builderw2UI.append("\",");
                    } else {
                        try {
                            missingakkwriter.write("<relation akk=\"" + trans.getTransliteration() + "\"/>" + System.lineSeparator());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                builder.append(">").append(System.lineSeparator());
                String postag = (curchar.getPostags() != null && !curchar.getPostags().isEmpty()) ? curchar.getPostags().iterator().next().toString() : "";
                builderw2UI.append("\"pos\":\""+postag+"\",");
                System.out.println("POSTAG: " + postag + " - " + curchar.getPostags().toString());
                builderw2UI.append("\"transcription\":\"");
                builder.append("<transliteration transcription=\""+trans.getTranscription().replace("-","").replaceAll("[0-9]","").replace("&","&amp;")+"\", ");
                builderw2UI.append(trans.getTranscription().replace("-","").replaceAll("[0-9]","").replace("&","&amp;")+"\", ");
                if(foundstem!=null){
                    builder.append("\" stem=\"").append(foundstem.getTransliteration().replace("&","&amp;"));
                }
                builder.append("\">");
                builder.append(trans.getTransliteration().replace("&","&amp;"));
                builderw2UI.append("\"transliteration\":\""+trans.getTransliteration().replace("&","&amp;")+"\",");
                builder.append("</transliteration>").append(System.lineSeparator());
                //builder.append(dictToLemon.generateAnnotatedWordVariantsXML(curchar.getCharacter(), cunhandler.reformatToUnicodeTranscription(trans.getTransliteration()), postag, handler));
                builder.append("<translation locale=\"en\">");
                builderw2UI.append("\"translation\":\""+trans.getTranslations().toString().replace("[", "").replace("]", "").replace("&","&amp;")+"\",");
                builder.append(trans.getTranslations().toString().replace("[", "").replace("]", "").replace("&","&amp;"));
                builder.append("</translation>").append(System.lineSeparator());
                for (POSTag transs : trans.getPostags()) {
                    builder.append(transs.toXMLForDict());
                }
                for (Epoch epoch : curchar.getEpochs()) {
                    builder.append(epoch.toXML());
                }
                for (Dialect dia : curchar.getDialects()) {
                    builder.append(dia.toXML());
                }
                builderw2UI.append("\"script\":\""+curchar.getCharacter().replace("&","&amp;")+"\"},"+System.lineSeparator());
                builder.append(curchar.getCharacter().replace("&","&amp;"));
                builder.append("</dictentry>").append(System.lineSeparator());
            }
        }
        builderw2UI.delete(builderw2UI.length()-2,builderw2UI.length());
        builderw2UI.append("]"+System.lineSeparator()+"}");
        try {
            FileWriter writer=new FileWriter(new File(jsexport));
            writer.write(builderw2UI.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        builder.append("</dictentries>");
        try {
            missingakkwriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    public static void main(String[] args) throws IOException {
        //OntModel model=ConceptMatcher.getInstance().createOntModelForTerms("hethOntologyTranlation.txt");
        //model.write(new FileWriter(new File("outmodellll.rdf")));
        //processBeinlichList("beinlich.txt");
       CharTypes[] chartypes=new CharTypes[]{CharTypes.AKKADIAN,CharTypes.EGYPTIANCHAR,CharTypes.SUMERIAN,CharTypes.HITTITE,CharTypes.ELAMITE,CharTypes.LUWIANCN};
        for(CharTypes chartype:chartypes){
            try {
                String dictionary=createHomepageDictionary(chartype,"sumToAkkRel.xml","/home/timo/SemanticDictionary/js/"+chartype.getSmallstr()+".js");
                String comment="";
                /*String dictionary=new String(Files.readAllBytes(Paths.get("dict/"+chartype.getLocale()+"_dict.xml")));

                String temp=dictionary.split("</comment>")[0].split("</license>")[1];
                String comment=temp.substring(temp.indexOf('>')+1);
                System.out.println(comment);*/
                System.out.println("Export XML: \"/home/timo/SemanticDictionary/dict/"+chartype.getSmallstr()+".xml");
                FileWriter  writer=new FileWriter(new File("/home/timo/SemanticDictionary/dict/"+chartype.getSmallstr()+".xml"));
                writer.write(dictionary);
                writer.close();
                try {
                    JSONObject xmlJSONObj = XML.toJSONObject(dictionary);
                    String jsonPrettyPrintString = xmlJSONObj.toString(4);
                    //System.out.println(jsonPrettyPrintString);
                    writer = new FileWriter(new File("/home/timo/SemanticDictionary/dict/" + chartype.getSmallstr() + ".json"));
                    writer.write(jsonPrettyPrintString);
                    writer.close();
                }catch(Exception e){
                   e.printStackTrace();
                    }
                ConceptMatcher.createAnkiDeckForDictionary(chartype,"/home/timo/SemanticDictionary/anki/"+chartype.getSmallstr()+".anki");
                /*writer=new FileWriter(new File("/home/timo/SemanticDictionary/js/"+chartype.getSmallstr()+".js"));
                writer.write("var "+chartype.getSmallstr()+"="+jsonPrettyPrintString);
                writer.close();*/
                try {
                    chartype.getCorpusHandlerAPI().getUtilDictHandler().toIME(ExportMethods.GOTTSTEINJSON, chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictMap(), chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictionary(),"","/home/timo/SemanticDictionary/ime/"+chartype.getSmallstr()+"_map.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    chartype.getCorpusHandlerAPI().getUtilDictHandler().toIME(ExportMethods.GOTTSTEINJSON, chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictMap(), chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictionary(),"","/home/timo/SemanticDictionary/ime/"+chartype.getSmallstr()+"_map.json");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    chartype.getCorpusHandlerAPI().getUtilDictHandler().toIME(ExportMethods.IBUS, chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictMap(), chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictionary(),"","/home/timo/SemanticDictionary/ime/"+chartype.getSmallstr());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String lang=chartype.getSmallstr();
                try {
                    TreeBuilder builder=new TreeBuilder(new FileInputStream(new File("tmp/"+lang+"_dict.json_jquery.xml")), GenericInputMethodDescriptor.AKKADIAN,false);
                    //builder.treeToXML("/home/timo/SemanticDictionary/ime/"+lang+"_dict.xml");
                    builder.treeToJSON("/home/timo/SemanticDictionary/ime/"+lang+"_dict.json");
                } catch (FileNotFoundException | XMLStreamException e) {
                    e.printStackTrace();
                }
                try {
                    TreeBuilder builder=new TreeBuilder(new FileInputStream(new File("tmp/"+lang+"_dict.json_jquery.xml")), GenericInputMethodDescriptor.AKKADIAN,true);
                    ///builder.treeToXML("/home/timo/SemanticDictionary/ime/"+lang+"_trans_dict.xml");
                    builder.treeToJSON("/home/timo/SemanticDictionary/ime/"+lang+"_trans_dict.json");
                } catch (FileNotFoundException | XMLStreamException e) {

  e.printStackTrace();
                    }

                ConceptMatcher.getInstance().createOntModelForDictionary(chartype,"/home/timo/SemanticDictionary/dict/",comment,false);
            } catch (JSONException je) {
                je.printStackTrace();
            }

        }
        for(CharTypes chartype:chartypes){
            try {
                String dictionary=new String(Files.readAllBytes(Paths.get("dict/"+chartype.getLocale()+"_map.xml")));
                String comment="";
                if(dictionary.contains("</comment>")) {
                    String temp = dictionary.split("</comment>")[0].split("</license>")[1];
                    comment = temp.substring(temp.indexOf('>') + 1);
                    System.out.println(comment);
                }
                JSONObject xmlJSONObj = XML.toJSONObject(dictionary);
                String jsonPrettyPrintString = xmlJSONObj.toString(4);
                //System.out.println(jsonPrettyPrintString);
                FileWriter writer=new FileWriter(new File("/home/timo/SemanticDictionary/map/"+chartype.getSmallstr()+".json"));
                writer.write(jsonPrettyPrintString);
                writer.close();
                writer=new FileWriter(new File("/home/timo/SemanticDictionary/js/"+chartype.getSmallstr()+"_map.js"));
                writer.write("var "+chartype.getSmallstr()+"_map="+jsonPrettyPrintString);
                writer.close();
                writer=new FileWriter(new File("/home/timo/SemanticDictionary/map/"+chartype.getSmallstr()+".xml"));
                writer.write(dictionary);
                writer.close();
                ConceptMatcher.getInstance().createOntModelForDictionary(chartype,"/home/timo/SemanticDictionary/map/",comment,true);
            } catch (JSONException je) {
                System.out.println(je.toString());
            }

        }
        for(CharTypes chartype:chartypes) {
            /*if(chartype.equals(CharTypes.EGYPTIANCHAR))
                continue;*/
            try {
                java.util.Map<String, String> translitToCunei = new TreeMap<String, String>();
                DictHandling dicth = chartype.getCorpusHandlerAPI().getUtilDictHandler();
                if(chartype.equals(CharTypes.EGYPTIANCHAR)){
                    translitToCunei=chartype.getCorpusHandlerAPI().getUtilDictHandler().getTranslitToCharMap();
                }else {

                    java.util.Map<String, ? extends LangChar> dict = chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictionary();

                    for (String key : dict.keySet()) {
                        for (Transliteration trans : dict.get(key).getTransliterationSet()) {
                            //System.out.println("Cunei: "+key);
                            //System.out.println("Translit: "+trans.getTransliteration());
                            String translitt = trans.getTransliteration();
                            if (translitt.contains("...")) {
                                //System.out.println("Key before: "+key+" - "+translitt);
                                key = key.replace("...", "");
                                translitt = translitt.replace("...", "-");
                                //System.out.println("Key after: "+key+" - "+translitt);
                            }
                            Integer numchars = StringUtils.countMatches(trans.getTransliteration(), "-") + 1;
                            Integer numunicodechars = key.length() / 2;
                            System.out.println(numchars + " " + numunicodechars);
                            if (numchars.equals(numunicodechars)) {
                                String[] translit = translitt.split("-");
                                String[] script = key.codePoints()
                                        .mapToObj(cp -> new String(Character.toChars(cp)))
                                        .toArray(size -> new String[size]);
                                //String[] script = key.split("");
                                int i = 0;
                                for (String transs : translit) {
                                    try {
                                        translitToCunei.put(transs, script[i] + "");
                                    } catch (StringIndexOutOfBoundsException e) {
                                        e.printStackTrace();
                                    }
                                    i++;
                                }
                            } else if (numchars == 1 && numunicodechars > numchars) {
                                translitToCunei.put(trans.getTransliteration(), key);
                            }
                        }
                    }
                }
                //System.out.println(translitToCunei);
                StringBuilder result = new StringBuilder();
                StringBuilder result2=new StringBuilder();
                StringBuilder builderw2UI=new StringBuilder();
                Integer recid=0;
                result.append("<?xml version=\"1.1\"?>\n<mapentries>");
                result2.append("<?xml version=\"1.1\"?>\n<mapentries>");
                builderw2UI.append("var "+chartype.getSmallstr()+"_map={"+System.lineSeparator()+" \"name\": \""+chartype.getSmallstr()+"map\","+System.lineSeparator()+" \"header\":\""+chartype.getSmallstr()+" Dictionary\","+System.lineSeparator()+"\"show\":{" +System.lineSeparator()+"\"toolbar\":true,"+System.lineSeparator()+"\"footer\":true"+System.lineSeparator()+"},"+System.lineSeparator()+"\"columns\":["+System.lineSeparator());
                builderw2UI.append("{\"field\":\"script\",\"caption\":\"Script\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\",\"style\":\"font-family:" + chartype.getSmallstr() + ";font-size:"+FONTSIZE+"px;\"},"+System.lineSeparator());
                builderw2UI.append("{\"field\":\"transliteration\",\"caption\":\"Transliteration\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"},"+System.lineSeparator());
                builderw2UI.append("{\"field\":\"charName\",\"caption\":\"SignName\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"},"+System.lineSeparator());
                builderw2UI.append("{\"field\":\"meaning\",\"caption\":\"Meaning\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\","+System.lineSeparator() +
                        "    render:function(record){"+System.lineSeparator() +
                        "        if(record.concept!=undefined && record.concept!=\"\"){"+System.lineSeparator() +
                        "            return \"<a href=\\\"\"+record.concept+\"\\\" target=\\\"_blank\\\">\"+record.meaning+\"</a>\""+System.lineSeparator() +
                        "        }else{"+System.lineSeparator() +
                        "            return record.meaning;"+System.lineSeparator() +
                        "        }"+System.lineSeparator() +
                        "}},"+System.lineSeparator());
                    if(chartype.equals(CharTypes.AKKADIAN) || chartype.equals(CharTypes.SUMERIAN) || chartype.equals(CharTypes.HITTITE) || chartype.equals(CharTypes.LUWIANCN) || chartype.equals(CharTypes.ELAMITE)) {
                        builderw2UI.append("{\"field\":\"got\",\"caption\":\"Gottstein\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"}," + System.lineSeparator());
                        builderw2UI.append("{\"field\":\"MesZL\",\"caption\":\"MesZL\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"}," + System.lineSeparator());
                        builderw2UI.append("{\"field\":\"aBZL\",\"caption\":\"aBZL\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"}," + System.lineSeparator());
                        builderw2UI.append("{\"field\":\"HethZL\",\"caption\":\"HethZL\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"}," + System.lineSeparator());
                        builderw2UI.append("{\"field\":\"LHA\",\"caption\":\"LHA\",\"sortable\":true,\"resizable\":true,\"size\":\"20%\"}" + System.lineSeparator());
                    }
                    builderw2UI.append("]" + System.lineSeparator() + ",\"records\":[" + System.lineSeparator());
                    System.out.println("CHARMAP: "+translitToCunei.keySet().size());
                    System.out.println("CHARMAP2: "+chartype.getCorpusHandlerAPI().getUtilDictHandler().getDictMap().size());
                for (String translit : translitToCunei.keySet()) {
                    LangChar langc=dicth.getDictMap().get(translitToCunei.get(translit));
                    if(langc==null || langc.getCharacter()==null || langc.getCharacter().length()>2) {
                        System.out.println("SKIPPED "+translit+" - "+langc);
                        continue;
                    }
                    result.append("<mapentry");
                    result2.append("<mapentry");
                    builderw2UI.append("{ \"recid\":"+recid+++", ");
                    if (langc != null && langc.getPaintInformation() != null) {
                        result.append(" paint=\"").append(langc.getPaintInformation()).append("\" ");
                        result2.append(" got=\"").append(langc.getPaintInformation()).append("\" ");
                        builderw2UI.append("\"got\":\"").append(langc.getPaintInformation()).append("\", ");
                    }
                    if(langc != null && langc.getCharName() != null){
                        result.append(" signName=\"").append(langc.getCharName()).append("\" ");
                        result2.append(" signName=\"").append(langc.getCharName()).append("\" ");
                        builderw2UI.append("\"charName\":\"").append(langc.getCharName()).append("\", ");
                    }
                    result.append(">").append(System.lineSeparator());
                    if (langc != null && langc instanceof CuneiChar) {
                        result.append(((CuneiChar) langc).getMezlNumber() != null && !((CuneiChar) langc).getMezlNumber().trim().isEmpty() ? "<representation ref=\"https://openlibrary.org/works/OL15890317W\" title=\"Mesopotamisches Zeichenlexikon\" short=\"MeZl\">" + ((CuneiChar) langc).getMezlNumber() + "</representation>" : "<representation ref=\"https://openlibrary.org/works/OL15890317W\" title=\"Mesopotamisches Zeichenlexikon\" short=\"MeZl\"></representation>");
                        result2.append(" MesZL=\"").append(((CuneiChar) langc).getMezlNumber()).append("\"");
                        builderw2UI.append("\"MesZL\":\"").append(((CuneiChar) langc).getMezlNumber()).append("\",");
                        result.append(((CuneiChar) langc).getaBzlNumber() != null && !((CuneiChar) langc).getaBzlNumber().trim().isEmpty() ? "<representation ref=\"https://openlibrary.org/works/OL9899303W\" title=\"Altbabylonische Zeichenliste der sumerisch-literarischen Texte\" short=\"AbZl\">" + ((CuneiChar) langc).getaBzlNumber() + "</representation>" : "<representation ref=\"https://openlibrary.org/works/OL9899303W\" title=\"Altbabylonische Zeichenliste der sumerisch-literarischen Texte\" short=\"AbZl\"></representation>");
                        result2.append(" aBZL=\"").append(((CuneiChar) langc).getaBzlNumber()).append("\"");
                        builderw2UI.append("\"aBZL\":\"").append(((CuneiChar) langc).getaBzlNumber()).append("\",");
                        result.append(((CuneiChar) langc).getLhaNumber() != null && !((CuneiChar) langc).getLhaNumber().trim().isEmpty() ? "<representation ref=\"lha\" short=\"LHA\" title=\"The Deimel Numbers\">" + ((CuneiChar) langc).getLhaNumber() + "</representation>" : "<representation ref=\"lha\" short=\"LHA\" title=\"The Deimel Numbers\"></representation>");
                        result2.append(" LHA=\"").append(((CuneiChar) langc).getLhaNumber()).append("\"");
                        builderw2UI.append("\"LHA\":\"").append(((CuneiChar) langc).getLhaNumber()).append("\",");
                        result.append(((CuneiChar) langc).getHethzlNumber() != null && !((CuneiChar) langc).getHethzlNumber().trim().isEmpty() ? "<representation ref=\"hethzl\" title=\"Hethitisches Zeichenlexikon\" short=\"HethZl\">" + ((CuneiChar) langc).getHethzlNumber() + "</representation>" : "<representation ref=\"hethzl\" title=\"Hethitisches Zeichenlexikon\" short=\"HethZl\"></representation>");
                        result2.append(" HethZL=\"").append(((CuneiChar) langc).getHethzlNumber()).append("\"");
                        builderw2UI.append("\"HethZL\":\"").append(((CuneiChar) langc).getHethzlNumber()).append("\",");
                    }
                    result2.append(">");
                    if(chartype!=CharTypes.EGYPTIANCHAR && dicth.getDictionary().containsKey(translitToCunei.get(translit))){
                        LangChar cur=dicth.getDictionary().get(translitToCunei.get(translit));
                        //System.out.println("TranslitToCunei: "+translit+" - "+translitToCunei.get(translit)+" - "+dicth.getDictionary().get(translitToCunei.get(translit))+" - "+cur.getTransliterations());


                        Set<Transliteration> translitSet=dicth.getDictionary().get(translitToCunei.get(translit)).getTransliterationSet();
                        Transliteration current=null;
                        for(Transliteration transslit: translitSet){
                            if(transslit.getTransliteration().equals(translit)){
                                current=transslit;
                                break;
                            }
                        }
                        if(current!=null){
                            //System.out.println("TranslitTrans: "+current+" - "+current.getTranslations().toString());
                            String translations=current.getTranslations().toString();
                            translations=translations.substring(1,translations.length()-1);
                            translations=translations.replace(",",";");
                            result.append("<meaning concept=\"").append(current.getConceptURI()).append("\">").append(translations).append("</meaning>");

                            result2.append("<meaning concept=\"").append(current.getConceptURI()).append("\">").append(translations).append("</meaning>");
                            builderw2UI.append("\"concept\":\"").append(current.getConceptURI()).append("\", ");
                            builderw2UI.append("\"meaning\":\"").append(translations).append("\", ");
                        }else{
                            System.out.println("TranslitTrans: "+translit+" - {}");
                        }
                    }else{
                        if(langc.getConceptURI()!=null){
                            builderw2UI.append("\"concept\":\"").append(langc.getConceptURI()).append("\", ");
                        }
                        if(langc.getMeaning()!=null){
                            builderw2UI.append("\"meaning\":\"").append(langc.getMeaning()).append("\", ");
                        }
                    }
                    result.append("<transliteration valid=\"true\">").append(translit).append("</transliteration>\n").append(translitToCunei.get(translit)).append("</mapentry>\n");
                    result2.append("<transliteration valid=\"true\">").append(translit).append("</transliteration>\n").append(translitToCunei.get(translit)).append("</mapentry>\n");
                    builderw2UI.append("\"transliteration\":\"").append(translit).append("\",");
                    builderw2UI.append("\"script\":\"").append(translitToCunei.get(translit)).append("\"},"+System.lineSeparator());
                }
                result.append("</mapentries>");
                result2.append("</mapentries>");
                builderw2UI.delete(builderw2UI.length()-2,builderw2UI.length());
                builderw2UI.append("]}");
                FileWriter writer = new FileWriter(new File("/home/timo/SemanticDictionary/map/" + chartype.getSmallstr() + ".xml"));
                writer.write(result2.toString());
                writer.close();
                writer=new FileWriter(new File("/home/timo/SemanticDictionary/js/"+chartype.getSmallstr()+"_map.js"));
                writer.write(builderw2UI.toString());
                writer.close();
                JSONObject xmlJSONObj = XML.toJSONObject(result.toString());
                String jsonPrettyPrintString = xmlJSONObj.toString(4);
                System.out.println("=======================PRINT IT\n"+builderw2UI.toString());

                xmlJSONObj = XML.toJSONObject(result2.toString());
                jsonPrettyPrintString = xmlJSONObj.toString(4);
                writer=new FileWriter(new File("/home/timo/SemanticDictionary/map/"+chartype.getSmallstr()+".json"));
                writer.write(jsonPrettyPrintString);
                writer.close();
                ConceptMatcher.getInstance().createOntModelForDictionary(chartype,"/home/timo/SemanticDictionary/map/","",false);
            } catch (JSONException je) {
                je.printStackTrace();
            }
        }
       /* Map<String, String> map=CharTypes.EGYPTIANCHAR.getCorpusHandlerAPI().getUtilDictHandler().getTranslitToCharMap();
        BufferedReader reader=new BufferedReader(new FileReader(new File("beinlich.txt")));
        StringBuilder builder=new StringBuilder();
        builder.append("<?xml version=\"1.1\"?>\n<dictentries>\n");
        String line;
        AkkadianImportHandler handler=new AkkadianImportHandler(null,null,null,null,null,null,null);
        while((line=reader.readLine())!=null){
            String[] linesplit=line.split("\\|");
            builder.append("<dictentry representation=\"").append(linesplit[2]).append("\">\n<transliteration>");
            builder.append(linesplit[0]);
            builder.append("</transliteration>\n");
            builder.append("<translation locale=\"de\">");
            builder.append(linesplit[1]);
            builder.append("</translation>\n");
            System.out.print("Insert ["+linesplit[0]);
            String output="", longestsub;
            for(int c=0;c<linesplit[0].length();c++){
                longestsub="";
                for(int i = 1 ; i <= linesplit[0].length() - c ; i++ )
                {
                    String sub = handler.reformatToUnicodeTranscription(linesplit[0].substring(c, c+i));
                    if(map.containsKey(sub) && sub.length()!=linesplit[0].length()){
                        System.out.println("\nMatched: "+sub+" - "+map.get(sub));
                        longestsub=map.get(sub);
                    }else{
                        System.out.println("Not matched: "+sub+" setting Substring to: "+linesplit[0].substring(c+i-1,linesplit[0].length()));
                        output+=map.get(sub);
                        builder.append(longestsub);
                        //if(sub.length()!=linesplit[0].length() && !sub.substring(sub.length()-1).equals(linesplit[0].substring(linesplit[0].length()-1))){
                            c=c+i-1;
                            i = linesplit[0].length();

                        //}
                    }
                    System.out.println(sub);
                }

            }
            System.out.println(" - "+output+"]");
            builder.append("</dictentry>\n");
        }
        builder.append("<dictentries>");
        System.out.println(map);
        FileWriter writer=new FileWriter("/home/timo/SemanticDictionary/js/"+CharTypes.EGYPTIANCHAR.getSmallstr()+"_dictgen.js");
        writer.write(builder.toString());
        writer.close();*/
        /*ConceptMatcher.getInstance().createOntModelForDictionary(CharTypes.AKKADIAN);
        ConceptMatcher.getInstance().createOntModelForDictionary(CharTypes.EGYPTIANCHAR);
        ConceptMatcher.getInstance().createOntModelForDictionary(CharTypes.HITTITE);
        ConceptMatcher.getInstance().createOntModelForDictionary(CharTypes.SUMERIAN);*/
    }




    public static void processBeinlichList(String path) throws IOException {
        Map<String,String> translitToCharr=CharTypes.EGYPTIANCHAR.getCorpusHandlerAPI().getUtilDictHandler().getTranslitToCharMap();
        Map<String,String> translitToChar=new TreeMap<>();
        for(String key:translitToCharr.keySet()){
            translitToChar.put(key.replace("Ḫ","H").replace("ḫ","h"),translitToCharr.get(key));
        }
        System.out.println(translitToChar);
        BufferedReader reader=new BufferedReader(new FileReader(new File(path)));
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File("beinlichxmlresult.xml")));
        BufferedWriter writer2=new BufferedWriter(new FileWriter(new File("beinlichunicoderesult.txt")));
        String line;
        StringBuilder beinlichbuilder=new StringBuilder();
        beinlichbuilder.append("<?xml version=\"1.1\"?><dictentries>\n");
        while((line=reader.readLine())!=null){
            String[] splitline=line.split("\\|");
            beinlichbuilder.append("<dictentry ref=\""+splitline[2]+"\">\n<transliteration transcription=\""+splitline[0]+"\">"+splitline[0]+"</transliteration>\n<translation locale=\"de\">"+splitline[1]+"</translation>\n"+codageToUnicode(splitline[0],translitToChar)+"</dictentry>\n");
            writer2.write(codageToUnicode(splitline[0],translitToChar)+"|"+line+System.lineSeparator());
        }
        writer2.close();
        beinlichbuilder.append("</dictentries>");
        writer.write(beinlichbuilder.toString());
        writer.close();
        reader.close();
    }

    private static String codageToUnicode(String codage,Map<String,String> translitToChar){
        StringBuilder result=new StringBuilder();
        for(Character ch:codage.toCharArray()){
            String cha=ch+"";
            System.out.println(cha+" - "+translitToChar.get(cha));
            String replacement=translitToChar.get(cha);
            if(replacement!=null){
                result.append(replacement);
            }else if(cha.equals(" ") || cha.equals("-") || cha.equals("(") || cha.equals(")")){
                result.append(" ");
            }else if(cha.equals("?") || cha.equals("*")) {
                result.append(ch+"");
            }else{
                return null;
            }

        }
        return result.toString();
    }
}

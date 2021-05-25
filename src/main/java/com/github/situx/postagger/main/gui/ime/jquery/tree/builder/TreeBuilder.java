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

package com.github.situx.postagger.main.gui.ime.jquery.tree.builder;


import com.github.situx.postagger.dict.utils.Translation;
import com.github.situx.postagger.main.gui.ime.descriptor.GenericInputMethodDescriptor;
import com.github.situx.postagger.util.enums.util.Tags;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.main.gui.ime.jquery.AkkIMEDict;
import com.github.situx.postagger.main.gui.ime.jquery.AkkIMEEntry;
import com.github.situx.postagger.main.gui.ime.jquery.AkkUnit;
import com.github.situx.postagger.main.gui.ime.jquery.GenericInputMethodControl;
import com.github.situx.postagger.main.gui.ime.jquery.tree.IMETree;
import org.json.JSONObject;
import org.json.XML;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TreeBuilder class for building in IME Tree
 */
public class TreeBuilder extends DefaultHandler2 implements AkkIMEDict {

    protected IMETree root=new IMETree();

    private Integer currentnum,currentposition;

    private String[] resultBuffer;

    private Boolean useTranslations=false;

    private Map<String,Map<String,Integer>> queryCache;

    public TreeBuilder(final InputStream file,final Locale locale,Boolean useTranslations){
        this.useTranslations=useTranslations;
        this.queryCache=new TreeMap<>();
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        System.out.println("Now parsing TreeBuilder....");
        SAXParser parser;
        try {
            parser = SAXParserFactory.newInstance().newSAXParser();
            parser.parse(file,this);
            //this.treeToXML("");
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.currentnum=5;

        this.currentposition=0;
        this.resultBuffer=new String[0];
    }

    /**
     * Constructor for this class.
     * @param file the input file to read from
     */
    public TreeBuilder(final InputStream file,final Locale locale){
        this.queryCache=new TreeMap<>();
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        System.out.println("Now parsing TreeBuilder....");
    	SAXParser parser;
		try {
			parser = SAXParserFactory.newInstance().newSAXParser();
	    	parser.parse(file,this);
            //this.treeToXML("");
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.currentnum=5;

        this.currentposition=0;
        this.resultBuffer=new String[0];
    }

    /**
     * Constructor for this class.
     * @param dictHandler the dicthandler for data
     */
    public TreeBuilder(DictHandling dictHandler){
        this.queryCache=new TreeMap<>();
        buildTree(root,dictHandler);
    }

    /**
     * Main testing method.
     * @param args
     */
    public static void main(String[] args) throws IOException, XMLStreamException {
        String[] langs=new String[]{"akkadian","sumerian","egyptian"};
        for(String lang:langs){
            try {
                TreeBuilder builder=new TreeBuilder(new FileInputStream(new File("tmp/"+lang+"_dict.json_jquery.xml")), GenericInputMethodDescriptor.AKKADIAN,false);
                //builder.treeToXML("/home/timo/SemanticDictionary/ime/"+lang+"_dict.xml");
                builder.treeToJSON("/home/timo/SemanticDictionary/ime/"+lang+"_dict.json");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                TreeBuilder builder=new TreeBuilder(new FileInputStream(new File("tmp/"+lang+"_dict.json_jquery.xml")), GenericInputMethodDescriptor.AKKADIAN,true);
                //builder.treeToXML("/home/timo/SemanticDictionary/ime/"+lang+"_trans_dict.xml");
                builder.treeToJSON("/home/timo/SemanticDictionary/ime/"+lang+"_trans_dict.json");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }



    public static Map<Integer, Map<String ,String>> sortByValues(final Map<Integer, Map<String ,String>> map) {
    	Comparator<Integer> valueComparator = (k1, k2) -> {
            int compare = k2.compareTo(k1);
            if (compare == 0) return 1;
            else return compare;
        };
    	Map<Integer, Map<String ,String>> sortedByValues = new TreeMap<>(valueComparator);
    	sortedByValues.putAll(map);
    	return sortedByValues;
    }

    protected void addWordComboToTree(String translit,String chars,Integer frequency,String meaning, String translation,String postag,String concept,Integer depth){
        IMETree curnode=root;
        for(int i=0;i<translit.length();i++){
        	String wordchar=translit.substring(i,i+1);
            IMETree res=curnode.containsChild(curnode.getWord()+wordchar);
            if(res==null){
                curnode=depthbuildWODictHandler(curnode, wordchar,chars,translit,frequency,meaning,translation,postag,concept, (i == translit.length() - 1),++depth,translit.length(),chars.length());
                //System.out.println("Created Curnode: "+curnode.toString()+" "+curnode.getCachewords().toString());
            }else{
                curnode=res;
                //System.out.println("Found Curnode: "+curnode.toString()+" "+curnode.getCachewords().toString());
                if(!curnode.getCachewords().containsKey(frequency)){
                	curnode.getCachewords().put(frequency, new TreeMap<>());
                }
                if(i+1==translit.length()){
                	curnode.setIsWord(true);
                	if(!curnode.getCachewords().get(frequency).containsKey(curnode.getWord())){
                		curnode.getCachewords().get(frequency).put(curnode.getWord(),new TreeSet<>());
                	}
                    //if(!chars.isEmpty())
                    //    this.queryCache.get(curnode.getWord()).put(chars,frequency);
                    curnode.getCachewords().get(frequency).get(curnode.getWord()).add(new IMETree(chars,meaning,translation,postag,concept));
                }else{
                	if(!curnode.getCachewords().get(frequency).containsKey(curnode.getWord())){
                		curnode.getCachewords().get(frequency).put(curnode.getWord(),new TreeSet<>());
                	}
                	curnode.getCachewords().get(frequency).get(curnode.getWord()).add(new IMETree(chars+translit.substring(++depth,translit.length()),meaning,translation,postag,concept));
                    //this.queryCache.get(curnode.getWord()).put(chars+translit.substring(++depth,translit.length()),frequency);
                }
            }
        }
    }

    public void buildTree(IMETree root,DictHandling dictHandler){
        for(String str:dictHandler.getTranscriptToWordDict().keySet()){
            LangChar lchar=dictHandler.matchWord(dictHandler.getTranscriptToWordDict().get(str));
            System.out.println("UseTranslations: "+useTranslations);
            if(this.useTranslations){
                if(lchar.getTranslations()!=null && !lchar.getTranslations().isEmpty()){
                    for(Translation trans:lchar.getTranslationSet(Locale.ENGLISH).keySet()){
                        this.addWordComboToTree(trans.getTranslation(), dictHandler.getTranscriptToWordDict().get(str), lchar.getOccurances().intValue(),lchar.getMeaning(),str,lchar.getPostags().toString(),lchar.getConceptURI(), trans.getTranslation().length());
                    }
                }
                //String trans=((lchar.getTranslations()!=null && !lchar.getTranslations().isEmpty())?lchar.getTranslations().keySet().iterator().next():"");

            }else{
                this.addWordComboToTree(str, dictHandler.getTranscriptToWordDict().get(str), lchar.getOccurances().intValue(),lchar.getMeaning(),((lchar.getTranslations()!=null && !lchar.getTranslations().isEmpty())?lchar.getTranslations().keySet().iterator().next():""),lchar.getPostags().toString(),lchar.getConceptURI(), str.length());
            }

        }
    }
    
    public IMETree depthbuildWODictHandler(IMETree node,String newchar,String chars,String translit,Integer frequency,String meaning,String translation,String postag,String concept,Boolean isWord,Integer depth,Integer curmaxdepth,Integer wordlength){
        IMETree newnode=new IMETree();
        newnode.setIsWord(isWord);
        newnode.setWord(node.getWord()+newchar);
        newnode.setMeaning(meaning);
        newnode.setLength(wordlength);
        newnode.setTranslation(translation);
        newnode.setPostag(postag);
        /*if(!this.queryCache.containsKey(newnode.getWord()) && !newnode.getWord().isEmpty()){
            this.queryCache.put(newnode.getWord(),new TreeMap<String, Integer>());
        }*/
        if(isWord){
        	newnode.setFrequency(frequency);
            newnode.setChars(chars);
        	if(!newnode.getCachewords().containsKey(newnode.getFrequency())){
        		newnode.getCachewords().put(newnode.getFrequency(),new TreeMap<>());
        	}
        	if(!newnode.getCachewords().get(frequency).containsKey(newnode.getWord())){
        		newnode.getCachewords().get(frequency).put(newnode.getWord(),new TreeSet<>());
        	}
            //this.queryCache.get(newnode.getWord()).put(newnode.getWord(),frequency);
        	newnode.getCachewords().get(frequency).get(newnode.getWord()).add(new IMETree(newnode.getChars(),meaning,translation,postag,concept));

        }else{
        	newnode.setFrequency(frequency);
            newnode.setChars(chars);
        	if(!newnode.getCachewords().containsKey(newnode.getFrequency())){
        		newnode.getCachewords().put(newnode.getFrequency(),new TreeMap<>());
        	}
        	if(!newnode.getCachewords().get(frequency).containsKey(newnode.getWord())){
        		newnode.getCachewords().get(frequency).put(newnode.getWord(),new TreeSet<>());
        	}
            //this.queryCache.get(newnode.getWord()).put(newnode.getChars()+translit.substring(depth,curmaxdepth),frequency);
        	newnode.getCachewords().get(frequency).get(newnode.getWord()).add(new IMETree(newnode.getChars()+translit.substring(depth,curmaxdepth),meaning,translation,postag,concept));
        }
        node.addChild(newnode);
        return newnode;
    }

    static private final Pattern AKK_PATTERN = Pattern.compile("([^A-z0-9]+)*([A-z]+)*([0-9]+)*");

    @Override
    public List<AkkIMEEntry> lookup(final List<AkkUnit> input, final boolean anticipate) {
        StringBuilder lookupstr=new StringBuilder();
        List<AkkIMEEntry> result=new LinkedList<>();
        for(AkkUnit unit:input){
            lookupstr.append((unit.getSyllable()==null?"":unit.getSyllable().toString().toLowerCase()));
        }
        Map<String,Integer> map=this.queryToMap(lookupstr.toString(),-1,true);
        System.out.println("Map: "+map);
        for(String str:map.keySet()){
            System.out.println("Add IMEEntry: "+str+" - "+map.get(str));
            Matcher matcher=AKK_PATTERN.matcher(str);
            StringBuilder missing=new StringBuilder();
            String actualStr="";

            if(matcher.find()){
                if(matcher.group(1)!=null){
                    actualStr=matcher.group(1);
                }
                for(int i=2;i<=matcher.groupCount();i++){
                    if(matcher.group(i)!=null){
                        missing.append(matcher.group(i));
                    }
                }
            }
           result.add(new AkkIMEEntry(actualStr,map.get(str),missing.toString()));
        }
        return result;
    }

    private Map<Integer,Map<String,Set<IMETree>>> query(IMETree node,String query,Map<Integer,Map<String,Set<IMETree>>> result){
        if(query.isEmpty()){
            return node.getCachewords();
        }
        IMETree curnode=node.containsChild(node.getWord()+query.substring(0,1));
        if(curnode==null){
            return new TreeMap<>();
        }else{
            result=this.query(curnode, query.substring(1, query.length()), result);
        }
        return result;
    }


    public String query(String query,Integer num){
        this.currentnum=num;
        this.currentposition=0;
    	Map<Integer,Map<String,Set<IMETree>>> result=this.query(this.root, query, new TreeMap<>());
    	if(result.isEmpty()){
    		return "";
    	}
    	StringBuilder ret=new StringBuilder();
        ret.append("_callbacks_.loadWords([\"SUCCESS\",[[\"");
        ret.append(query);
        ret.append("\",[");
    	System.out.println(result.toString());
    	//result=sortByValues(result);
    	int i=0;
    	Iterator<Integer> iter1=result.keySet().iterator();
    	System.out.println(result.toString());
    	for(;iter1.hasNext();){
    		Integer outerkey=iter1.next();
    		for(String middlekey:result.get(outerkey).keySet()){
    			for(IMETree key:result.get(outerkey).get(middlekey)){
        			if(i>num){
        				break;
        			}
            		ret.append("\"");
                    ret.append(key.getWord());
                    ret.append("\",");
            		i++;
    			}
    		}
    	}
        ret.delete(ret.length()-1,ret.length());
    	//ret=ret.substring(0,ret.length()-1);
    	ret.append("]]]])");
    	System.out.println("Queryresult for "+query+": "+ret.toString());
        return ret.toString();
    }

    public boolean shouldPassThrough(char keyChar){
        String keyStr=keyChar+"";
        if(keyStr.matches("[A-z0-9]"))
            return false;
        return true;
    }




    public List<String> next(){
        List<String> result=new LinkedList<>();
        if(this.currentposition>=resultBuffer.length){
            return null;
        }else{
            for(int i=this.currentposition;i<resultBuffer.length && i<this.currentposition+currentnum;i++){
                result.add(resultBuffer[i]);
            }
            this.currentposition=currentposition+currentnum;
            return result;
        }
    }

    public List<String> previous(){
        List<String> result=new LinkedList<>();
        if(this.currentposition-currentnum<0){
            return null;
        }else{
            for(int i=this.currentposition;i>0 && i>this.currentposition-currentnum;i++){
                result.add(resultBuffer[i]);
            }
            this.currentposition=currentposition-currentnum;
            return result;
        }
    }
    
    public String[] queryToArray(String query,Integer num){
        this.currentnum=num;
        this.currentposition=0;
        Map<Integer,Map<String,Set<IMETree>>> result=this.query(this.root, query, new TreeMap<>());
        if(result.isEmpty()){
            return new String[0];
        }
        String[] ret=new String[num+1];
        System.out.println(result.toString());
        int i=0;
        Iterator<Integer> iter1=result.keySet().iterator();
        for(;iter1.hasNext();){
            Integer outerkey=iter1.next();
            for(String middlekey:result.get(outerkey).keySet()){
                for(IMETree key:result.get(outerkey).get(middlekey)){
                    if(i>num){
                        break;
                    }
                    ret[i]=(i+1)+". "+key.getWord();
                    i++;
                }

            }
        }
        this.resultBuffer=ret;
        System.out.println("Queryresult for "+query+": "+ret);
        return ret;
    }

    public Map<String,Integer> queryToMap(String query,Integer num,Boolean save){
        Map<String,Integer> ret=new TreeMap<>();
        if(save) {
            this.currentnum = num;
            this.currentposition = 0;
        }
        if(this.queryCache.containsKey(query)) {
            ret=this.queryCache.get(query);
            if(ret.isEmpty()){
                return new TreeMap<>();
            } else if(ret.containsKey("")){
                ret.remove("");
            }

            System.out.println("Cached Query: "+ret);

            return ret;
        }
        System.out.println("QueryCache: "+this.queryCache.size());
        /*if(this.queryCache.containsKey(query)){
            ret=this.queryCache.get(query);
            if(ret.isEmpty()){
                return new TreeMap<>();
            }

            System.out.println("Cached Query: "+ret);

            return ret;
        }*/
        Map<Integer,Map<String,Set<IMETree>>> result=this.query(this.root, query, new TreeMap<>());
        if(result.isEmpty()){
            return new TreeMap<>();
        }
        //System.out.println("Result: "+result);
        System.out.println(result.toString());
        int i=0;
        Iterator<Integer> iter1=result.keySet().iterator();
        for(;iter1.hasNext();){
            Integer outerkey=iter1.next();
            for(String middlekey:result.get(outerkey).keySet()){
                for(IMETree key:result.get(outerkey).get(middlekey)){
                    if(i>num && num!=-1){
                        return ret;
                    }
                    ret.put(key.getWord(),outerkey);
                    i++;
                }

            }
        }
        queryCache.put(query,ret);
        //System.out.println("Queryresult for "+query+": "+ret);
        return ret;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        switch(qName){
            case "word":
                if(this.useTranslations){
                    this.addWordComboToTree((attributes.getValue("translation")!=null?attributes.getValue("translation"):""),attributes.getValue("chars"),Integer.valueOf(attributes.getValue("freq")),(attributes.getValue("meaning")!=null?attributes.getValue("meaning"):""),attributes.getValue("translit"),(attributes.getValue("postag")!=null?attributes.getValue("postag"):""),(attributes.getValue("concept")!=null?attributes.getValue("concept"):""),0);
                }else{
                    this.addWordComboToTree(attributes.getValue("translit"),attributes.getValue("chars"),Integer.valueOf(attributes.getValue("freq")),(attributes.getValue("meaning")!=null?attributes.getValue("meaning"):""),(attributes.getValue("translation")!=null?attributes.getValue("translation"):""),(attributes.getValue("postag")!=null?attributes.getValue("postag"):""),(attributes.getValue("concept")!=null?attributes.getValue("concept"):""),0);
                }



                break;
        }
    }

    public String toXML(IMETree node) throws IOException, XMLStreamException {
        if(!node.hasChildren()) {
            return "";
        }
        StringWriter strwriter;
        XMLOutputFactory output3;
        XMLStreamWriter writer;
        strwriter=new StringWriter();
        output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
        for(IMETree child:node.getChildren()){
            writer.writeStartElement(Tags.NODE);
            writer.writeCharacters(child.toXML());
            writer.writeCharacters(this.toXML(child));
            writer.writeEndElement();
            writer.writeCharacters(System.lineSeparator());
        }
        return strwriter.toString();
    }

    public String toJSON(IMETree node) throws IOException, XMLStreamException {
        if(!node.hasChildren()) {
            return "";
        }
        StringWriter writer=new StringWriter();
        Iterator<IMETree> iter=node.getChildren().iterator();
        for(;iter.hasNext();){
            IMETree child=iter.next();
            writer.write(child.toJSON());
            writer.write(this.toJSON(child));
        }
        return writer.toString();
    }


    public void treeToXML(String filepath) throws IOException, XMLStreamException {
        StringWriter strwriter;
        XMLOutputFactory output3;
        XMLStreamWriter writer;
        strwriter=new StringWriter();
        output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(new BufferedWriter(new FileWriter(new File(filepath)))));
        writer.writeStartDocument();
        writer.writeStartElement("data");
        writer.writeCharacters(this.toXML(root));
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }

    public void treeToJSON(String filepath) throws IOException, XMLStreamException {
        FileWriter writer;
        writer = new FileWriter(new File(filepath));
        writer.write("{"+System.lineSeparator());
        String toJSONStr=this.toJSON(root);
        toJSONStr=toJSONStr.substring(0,toJSONStr.length()-2);
        writer.write(toJSONStr);
        writer.write(System.lineSeparator()+"}"+System.lineSeparator());
        writer.close();
    }

    public void treeToJSObject(String filepath) throws IOException, XMLStreamException {
        JSONObject xmlJSONObj = XML.toJSONObject(this.toXML(root));
        FileWriter writer=new FileWriter(new File(filepath));
        writer.write("var tree="+xmlJSONObj.toString(4));
        writer.close();
    }

    /**
     * A control object, with toggleability of character mode.
     * Gives access to change the character mode without having
     * to set the Locale through the InputContext, which is not
     * normally publicly accessible.
     */
    public class AkkadianMethodControl extends GenericInputMethodControl {
        // default to simplified
        private boolean characterMode = true;

        /**
         * @return true for simplified false for traditional
         */
        public boolean getCharacterMode() {
            return this.characterMode;
        }

        /**
         * Set the character mode.
         * @param simplified true for simplified false for traditional
         */
        public void setCharacterMode(boolean simplified) {
            this.characterMode = simplified;
        }
    }
    
}

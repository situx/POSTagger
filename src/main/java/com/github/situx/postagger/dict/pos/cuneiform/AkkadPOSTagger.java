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

package com.github.situx.postagger.dict.pos.cuneiform;

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.pos.util.GroupDefinition;
import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.util.enums.pos.POSTags;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Akkadian POSTagger.
 */
public class AkkadPOSTagger extends CuneiPOSTagger {



    /**
     * Constructor for Akkadian POSTagger.
     */
    public AkkadPOSTagger() {

        super(new TreeMap<>(), CharTypes.AKKADIAN);
    }

    /**
     * Gets the postag of a given word.
     *
     * @param word    the word
     * @param handler the dicthandler to use
     * @return the list of Integers to use
     */
    public java.util.List<Integer> getPosTag(String word, final DictHandling handler) {
        List<Integer> result = new LinkedList<>();
        Boolean onlyVerbmatch=true;
        POSDefinition tempmatch=unknownPOS,
                firstmatch=null;
        word = word.replace("]", "").replace("[", "");
        for (Integer key : this.classifiers.keySet()) {
            if (this.classifiers.get(key).isEmpty()) {
                continue;
            }
            for (POSDefinition def : this.classifiers.get(key)) {
                List<Integer> res = def.performCheck(word);
                if (!res.isEmpty()) {
                    System.out.println("RGB: " + this.poscolors.get(def.getDesc()).getRGB());
                    System.out.println("POSDefinition: " + def.toString());
                    if(firstmatch==null){
                        firstmatch=def;
                    }
                    for(int j=0;j<res.size();j+=3){
                        res.add(j, this.poscolors.get(def.getDesc()).getRGB());
                    }
                    result.addAll(res);
                    tempmatch=def;
                    /*res.add(0, this.poscolors.get(def.getTag()).getRGB());
                    result.addAll(res); */
                    if((def.getPosTag()!=POSTags.VERB && def.getPosTag()!=POSTags.NOUNORADJ && def.getPosTag()!=POSTags.POSSESSIVE) && onlyVerbmatch){
                       onlyVerbmatch=false;
                    }
                   /* if (res.get(2).equals(word.length()) && res.get(1).equals(0)) {
                       return result;
                    }
                    */
                }
            }
        }
        System.out.println("IsAllUpperCase? "+lastmatchedword+" "+ Translator.isAllUpperCaseOrNumberOrDelim(lastmatchedword));
        if(result.isEmpty() || onlyVerbmatch && lastmatched!=null && lastmatched.getTag().equals("NN")){
            boolean matched=false;
            //this.classifiers.get(this.orderToPOS.get("DET")).stream().findFirst().ifPresent(def -> def.getRegex().matcher(lastmatchedword).find());
            for(POSDefinition def:this.classifiers.get(this.orderToPOS.get("DET"))){
                /*if(!lastmatchedword.endsWith("-")){
                    lastmatchedword+="-";
                }*/
                //System.out.println("Currentregex: "+def.getRegex().toString());
                if(def.getRegex().matcher(lastmatchedword).find()){
                    matched=true;
                    break;
                }
            }
            System.out.println("Matches DetString?: "+matched);
            if((!matched && Translator.isAllUpperCaseOrNumberOrDelim(lastmatchedword)) || (matched && Translator.isAllUpperCaseOrNumberOrDelim(lastmatchedword) && result.isEmpty())){
                result.clear();
                result.add(0, this.poscolors.get("namedentity").getRGB());
                result.add(0);
                result.add(word.length());
                POSDefinition posdef=new POSDefinition("","","","",new String[0],"namedentity","","","","","",new TreeMap<>(), CharTypes.AKKADIAN);
                posdef.setPosTag(POSTags.NAMEDENTITY);
                posdef.setClassification("NE");
                posdef.currentword=word;
                posdef.setValue(new String[]{word});
                posdef.setEquals("");
                posdef.setRegex(POSTagger.generalPattern);
                lastmatched=posdef;
                lastmatchedword=word;
            }
            firstmatch=lastmatched;
        }
        lastmatched=tempmatch;
        lastmatchedword=word;
        this.classificationResult.put(this.wordcounter++,new Tuple<>(word,firstmatch));
        return result;
    }

    @Override
    public List<POSDefinition> getPosTagDefs(String word, final DictHandling handler/*,final Boolean dummy*/) {
        List<POSDefinition> result = new LinkedList<>();
        Boolean onlyVerbmatch=true;
        POSDefinition tempmatch=new POSDefinition("","","","",new String[0],"UNKNOWN","","","","","",new TreeMap<>(), CharTypes.AKKADIAN),
                firstmatch=null;
        word = word.replace("]", "").replace("[", "");
        for (Integer key : this.classifiers.keySet()) {
            if (this.classifiers.get(key).isEmpty()) {
                continue;
            }
            for (POSDefinition def : this.classifiers.get(key)) {
                List<Integer> res = def.performCheck(word);
                if (!res.isEmpty()) {
                    System.out.println("RGB: " + this.poscolors.get(def.getDesc()).getRGB());
                    System.out.println("POSDefinition: " + def.toString());
                    if(firstmatch==null){
                        firstmatch=def;
                    }
                    //if((!namedentity && def.getPosTag()==POSTags.NOUN) || def.getPosTag()!=POSTags.NOUN){
                        tempmatch=def;
                        result.add(def);
                    //}

                    if((def.getPosTag()!=POSTags.VERB && def.getPosTag()!=POSTags.NOUNORADJ && def.getPosTag()!=POSTags.POSSESSIVE)&& onlyVerbmatch){
                        onlyVerbmatch=false;
                    }
                    //if(def.getPosTag()==POSTags.NAMEDENTITY){
                    //    namedentity=true;
                    //}
                    if(res.size()>1)
                        def.currentword=word.substring(res.get(0),res.get(1));
                   /* if (res.get(2).equals(word.length()) && res.get(1).equals(0)) {
                       return result;
                    }
                    */
                }
            }
        }
        //System.out.println("IsAllUpperCase? "+lastmatchedword+" "+Translator.isAllUpperCaseOrNumberOrDelim(lastmatchedword));

        if(result.isEmpty() || onlyVerbmatch && lastmatched!=null && lastmatched.getTag().equals("NN")){
            boolean matched=false;
            for(POSDefinition def:this.classifiers.get(this.orderToPOS.get("DET"))){
                /*if(!lastmatchedword.endsWith("-")){
                    lastmatchedword+="-";
                }*/
                //System.out.println("Currentregex: "+def.getRegex().toString());
                if(def.getRegex().matcher(lastmatchedword).find()){
                    matched=true;
                    break;
                }
            }
            //System.out.println("Matches DetString?: "+matched);
            if((!matched && Translator.isAllUpperCaseOrNumberOrDelim(lastmatchedword)) || (matched && Translator.isAllUpperCaseOrNumberOrDelim(lastmatchedword) && result.isEmpty())){
                result.clear();
                POSDefinition posdef=new POSDefinition("","","","",new String[0],"namedentity","","","","","",new TreeMap<>(),CharTypes.AKKADIAN);
                posdef.setPosTag(POSTags.NAMEDENTITY);
                posdef.setClassification("NE");
                posdef.currentword=word;
                posdef.setValue(new String[]{word});
                posdef.setEquals("");
                posdef.setRegex(POSTagger.generalPattern);
                result.add(posdef);
                lastmatched=posdef;
                lastmatchedword=word;
            }
            firstmatch=lastmatched;
        }
        //if(!result.isEmpty()){
            lastmatched=tempmatch;
            lastmatchedword=word;
        this.classificationResult.put(this.wordcounter++,new Tuple<>(word,firstmatch));
        //}
        return result;
    }

    public Map<Integer,String> sentenceDetector(String[] words,String[] lines){
        List<Integer> linecount=this.getNumberOfWordsPerLine(lines);
        int beginposition=0;
        //System.out.println("Linecount: "+linecount);
        this.sentences=new TreeMap<>();
        this.sentencesByWordPosition=new TreeMap<>();
        StringBuilder collectwords=new StringBuilder();
        /*for(int i=0;i<words.length;i++){
            System.out.print(words[i]+",");
        }*/
        //System.out.println(System.lineSeparator());
        //System.out.println("Words.length(): "+words.length);
        //System.out.println("Wordcounter: "+this.wordcounter);
        int currentline=1,beginline=1,lastpos=0;
        for(Integer position:this.classificationResult.keySet()){
            //System.out.println("Position: "+position+" Currentline: "+currentline);
            if(currentline<linecount.size() && position>linecount.get(currentline)){
                currentline++;
            }
            switch (classificationResult.get(position).getTwo().getPosTag()) {
                case VERB:  if(classificationResult.size()>position+1){
                    POSTags next=classificationResult.get(position+1).getTwo().getPosTag();
                    if(position>=words.length)
                        break;
                    collectwords.append(words[position]);
                    collectwords.append(" ");
                    switch (next){
                        case CONJUNCTION: if(!words[position+1].replace("[","").replace("]","").equals("u3")){
                            for(int i=beginline;i<=currentline+1;i++){
                                //System.out.println("Adding: "+i+" "+collectwords);
                                sentences.put(i,collectwords+".");
                                this.sentencesByWordPosition.put(i,new Tuple<>(beginposition,position));
                            }
                            beginposition=position;
                            beginline=currentline+2;
                            collectwords=new StringBuilder();
                        }
                        break;
                        default:
                            for(int i=beginline;i<=currentline+1;i++){
                                sentences.put(i,collectwords+".");
                                this.sentencesByWordPosition.put(i,new Tuple<>(beginposition,position));
                                //System.out.println("Adding: "+i+" "+collectwords);
                            }
                            beginposition=position;
                            beginline=currentline+2;
                            collectwords=new StringBuilder();
                    }
                }break;
                default:
                    if(position<words.length)
                        collectwords.append(words[position]);
                        collectwords.append(" ");
            }
            lastpos=position;
        }
        for(int i=beginline;i<=currentline+1;i++){
            sentences.put(i,collectwords.toString()+".");
            this.sentencesByWordPosition.put(i,new Tuple<>(beginposition,lastpos));
            //System.out.println("Adding: "+i+" "+collectwords);
        }
        //System.out.println("Sentences: "+sentences);
        return sentences;
    }

    public Set<String> verbGenerator(String verb,String root){
        Set<String> result=new TreeSet<>();
        String root1=root.substring(0,1),root2=root.substring(1,2),root3=root.substring(2,3);
        if(root.startsWith("n")){

        }else{
           result.add("a-"+root1+root2+"u"+root3);
           result.add("ni-"+root1+root2+"u"+root3);
           result.add("ta-"+root1+root2+"u"+root3);
           result.add("ta-"+root1+root2+"u"+root3+"-i2");
           result.add("ta-"+root1+root2+"u"+root3+"-a2");
           result.add("i-"+root1+root2+"u"+root3);
           result.add("i-"+root1+root2+"u"+root3+"-u2");
           result.add("i-"+root1+root2+"u"+root3+"-a2");
            result.add("a-"+root1+"a"+root2+root2+"u"+root3);
            result.add("ni-"+root1+"a"+root2+root2+"u"+root3);
            result.add("ta-"+root1+"a"+root2+root2+"u"+root3);
            result.add("ta-"+root1+"a"+root2+root2+"u"+root3+"-i2");
            result.add("ta-"+root1+"a"+root2+root2+"u"+root3+"-a2");
            result.add("i-"+root1+"a"+root2+root2+"u"+root3);
            result.add("i-"+root1+"a"+root2+root2+"u"+root3+"-u2");
            result.add("i-"+root1+"a"+root2+root2+"u"+root3+"-a2");
           result.add("a-"+root1+"-ta-"+root2+"u"+root3);
           result.add("ni-"+root1+"-ta-"+root2+"u"+root3);
           result.add("ta-"+root1+"-ta-"+root2+"u"+root3);
           result.add("ta-"+root1+"-ta-"+root2+"u"+root3+"-i2");
           result.add("ta-"+root1+"-ta-"+root2+"u"+root3+"-a2");
           result.add("i-"+root1+"-ta-"+root2+"u"+root3);
           result.add("i-"+root1+"-ta-"+root2+"u"+root3+"-u2");
           result.add("i-"+root1+"-ta-"+root2+"u"+root3+"-a2");
           result.add("u-"+root1+"a"+root2+root2+"i"+root3);
           result.add("nu-"+root1+"a"+root2+root2+"i"+root3);
           result.add("tu-"+root1+"a"+root2+root2+"i"+root3);
           result.add("tu-"+root1+"a"+root2+root2+"i"+root3+"-i2");
           result.add("tu-"+root1+"a"+root2+root2+"i"+root3+"-a2");
           result.add("u-"+root1+"a"+root2+root2+"i"+root3+"-u2");
           result.add("u-"+root1+"a"+root2+root2+"i"+root3+"-a2");
        }
        return result;
    }

    /**
     * Generates an xml representation of the postags.
     *
     * @param path the path for saving the xml representation
     * @throws XMLStreamException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    public void toXML(String path) throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        output.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        OutputStreamWriter outwriter = new OutputStreamWriter(new FileOutputStream(path), Tags.UTF8.toString());
        XMLStreamWriter writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(outwriter));
        writer.writeStartDocument(Tags.UTF8.toString(), Tags.XMLVERSION.toString());
        //writer.writeCharacters("\n");
        writer.writeStartElement(Tags.DATA.toString());
        writer.writeCharacters(System.lineSeparator());
        writer.writeStartElement("colors");
        for (String poscol:this.poscolors.keySet()) {
            writer.writeStartElement("tagcolor");
            writer.writeAttribute("tag", poscol);
            writer.writeAttribute("pos", poscol);
            writer.writeAttribute("color", String.format("#%02x%02x%02x", this.poscolors.get(poscol).getRed(), this.poscolors.get(poscol).getGreen(), this.poscolors.get(poscol).getBlue()));
            writer.writeEndElement();
        }
        writer.writeEndElement();
        writer.writeStartElement("dependencies");
        for(String dep:this.dependencies.keySet()){
             for(String dependee:this.dependencies.get(dep)){
                 writer.writeStartElement("dependence");
                 writer.writeAttribute("dependee",dependee);
                 writer.writeAttribute("depender",dep);
                 writer.writeEndElement();
             }
        }
        writer.writeEndElement();
        writer.writeStartElement("groupconfigs");
        for(String groupid:this.groupconfigs.keySet()){
              for(Integer id:this.groupconfigs.get(groupid).keySet()){
                   writer.writeStartElement("groupconfig");
                   for(GroupDefinition group:this.groupconfigs.get(groupid).get(id)){
                       writer.writeCharacters(group.toXML()+System.lineSeparator());
                   }
                   writer.writeEndElement();
              }
        }
        writer.writeEndElement();
        writer.writeStartElement("tags");
        for (List<POSDefinition> poss : this.classifiers.values()) {
            for (POSDefinition akkadchar : poss) {
                writer.writeCharacters(akkadchar.toXML() + System.lineSeparator());
            }
        }
        writer.writeEndElement();
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }

}

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

import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.pos.POSTagger;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * POSTagger for Sumerian.
 */
public class SumerianPOSTagger extends CuneiPOSTagger {

    /**
     * Constructor for this class.
     */
    public SumerianPOSTagger() {
        super(new TreeMap<>(), CharTypes.SUMERIAN);
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
        CuneiChar wordchar;
        int i = 1;
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
                    if((def.getPosTag()!=POSTags.VERB && def.getPosTag()!=POSTags.NOUNORADJ) && onlyVerbmatch){
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
            for(POSDefinition def:this.classifiers.get(this.orderToPOS.get("DET"))){
                /*if(!lastmatchedword.endsWith("-")){
                    lastmatchedword+="-";
                }*/
                System.out.println("Currentregex: "+def.getRegex().toString());
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
                POSDefinition posdef=new POSDefinition("","","","",new String[0],"namedentity","","","","","",new TreeMap<>(),this.charType);
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
        Boolean onlyVerbmatch=true,namedentity=false;
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
                    //if((!namedentity && def.getPosTag()==POSTags.NOUN) || def.getPosTag()!=POSTags.NOUN){
                    tempmatch=def;
                    result.add(def);
                    //}

                    if((def.getPosTag()!=POSTags.VERB && def.getPosTag()!=POSTags.NOUNORADJ)&& onlyVerbmatch){
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
        System.out.println("IsAllUpperCase? "+lastmatchedword+" "+Translator.isAllUpperCaseOrNumberOrDelim(lastmatchedword));

        if((result.isEmpty() || onlyVerbmatch) && lastmatched!=null && lastmatched.getTag().equals("NN") && Translator.isAllUpperCaseOrNumberOrDelim(lastmatchedword)){
            result.clear();
            POSDefinition posdef=new POSDefinition("","","","",new String[0],"namedentity","","","","","",new TreeMap<>(),this.charType);
            posdef.setPosTag(POSTags.NAMEDENTITY);
            posdef.setClassification("NE");
            posdef.currentword=word;
            posdef.setValue(new String[]{word});
            posdef.setEquals("");
            posdef.setRegex(POSTagger.generalPattern);
            result.add(posdef);
            tempmatch=posdef;
            firstmatch=tempmatch;
            lastmatchedword=word;
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
        System.out.println("Linecount: "+linecount);
        this.sentences=new TreeMap<>();
        this.sentencesByWordPosition=new TreeMap<>();
        String collectwords="";
        for (final String word : words) {
            System.out.print(word + ",");
        }
        System.out.println(System.lineSeparator());
        System.out.println("Words.length(): "+words.length);
        System.out.println("Wordcounter: "+this.wordcounter);
        int currentline=1,beginline=1,lastpos=0;
        for(Integer position:this.classificationResult.keySet()){
            System.out.println("Position: "+position+" Currentline: "+currentline);
            if(position>linecount.get(currentline)){
                currentline++;
            }
            switch (classificationResult.get(position).getTwo().getPosTag()) {
                case VERB:  if(classificationResult.size()>position+1){
                    POSTags next=classificationResult.get(position+1).getTwo().getPosTag();
                    collectwords+=words[position]+" ";
                    switch (next){
                        case CONJUNCTION: if(!words[position+1].replace("[","").replace("]","").equals("u3")){
                            for(int i=beginline;i<=currentline+1;i++){
                                System.out.println("Adding: "+i+" "+collectwords);
                                sentences.put(i,collectwords+".");
                                this.sentencesByWordPosition.put(i,new Tuple<Integer, Integer>(beginposition,position));
                            }
                            beginposition=position;
                            beginline=currentline+2;
                            collectwords="";
                        }
                            break;
                        default:
                            for(int i=beginline;i<=currentline+1;i++){
                                sentences.put(i,collectwords+".");
                                this.sentencesByWordPosition.put(i,new Tuple<>(beginposition,position));
                                System.out.println("Adding: "+i+" "+collectwords);
                            }
                            beginposition=position;
                            beginline=currentline+2;
                            collectwords="";
                    }
                }break;
                default:
                    collectwords+=words[position]+" ";
            }
            lastpos=position;
        }
        for(int i=beginline;i<=currentline+1;i++){
            sentences.put(i,collectwords+".");
            this.sentencesByWordPosition.put(i,new Tuple<>(beginposition,lastpos));
            System.out.println("Adding: "+i+" "+collectwords);
        }
        System.out.println("Sentences: "+sentences);
        return sentences;
    }

    /**
     * Generates an xml representation of the postags.
     *
     * @param path the path for saving the xml representation
     * @throws javax.xml.stream.XMLStreamException
     * @throws java.io.FileNotFoundException
     * @throws java.io.UnsupportedEncodingException
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
        for (String poscol : this.poscolors.keySet()) {
            writer.writeStartElement("tagcolor");
            writer.writeAttribute("tag", poscol);
            writer.writeAttribute("pos", poscol);
            writer.writeAttribute("color", this.poscolors.get(poscol).toString());
            writer.writeEndElement();
        }
        for (java.util.List<POSDefinition> poss : this.classifiers.values()) {
            for (POSDefinition akkadchar : poss) {
                writer.writeCharacters(akkadchar.toXML() + System.lineSeparator());
            }
        }
        writer.writeEndElement();
        writer.writeEndDocument();
        writer.close();
    }
}



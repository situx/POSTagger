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

package com.github.situx.postagger.dict.corpusimport.util;

import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by timo on 24.08.14.
 * Collects ngram information from a given unsegmented corpus without paying attention to possible known words.
 */
public class NGramStat {
    /**
     * Map from the size of the ngram to the ngrams and their frequency.
     */
    private Map<Integer,Map<String,Double>> ngramSizeToWords;
    /**
     * Map from the ngrams to their size.
     */
    private Map<String,Double> ngramWordsToSize;

    /**
     * Constructor for this class.
     */
    public NGramStat(){
        this.ngramSizeToWords=new TreeMap<>();
        this.ngramWordsToSize=new TreeMap<>();
    }

    /**
     * Adds a given ngram to the list of ngrams.
     * @param chartype
     * @param ngram
     */
    public void addNGram(CharTypes chartype, String ngram){
        if(!this.ngramSizeToWords.containsKey((ngram.length()/chartype.getChar_length()))){
            this.ngramSizeToWords.put((ngram.length()/chartype.getChar_length()),new TreeMap<String,Double>());
            this.ngramSizeToWords.get((ngram.length()/chartype.getChar_length())).put(ngram,0.0);
        }else if(!this.ngramSizeToWords.get((ngram.length()/chartype.getChar_length())).containsKey(ngram)){
            this.ngramSizeToWords.get((ngram.length()/chartype.getChar_length())).put(ngram,0.0);
        }
        this.ngramSizeToWords.get((ngram.length()/chartype.getChar_length())).put(ngram,this.ngramSizeToWords.get((ngram.length()/chartype.getChar_length())).get(ngram)+1);
        if(!this.ngramWordsToSize.containsKey(ngram)){
            this.ngramWordsToSize.put(ngram,0.0);
        }
        this.ngramWordsToSize.put(ngram,this.ngramWordsToSize.get(ngram)+1);
    }

    /**
     * Adds a given ngram to the list of ngrams.
     * @param chartype the chartyoe to add
     * @param ngram  the ngram to add
     */
    public void addNGram(CharTypes chartype,String ngram,Integer length,Double freq){
        if(!this.ngramSizeToWords.containsKey(length)) {
            this.ngramSizeToWords.put(length, new TreeMap<String, Double>());
        }
        this.ngramSizeToWords.get(length).put(ngram,freq);
        this.ngramWordsToSize.put(ngram,freq);
    }


    /**
     * Generates ngrams until a specified size from an unsegmented line of text.
     * @param chartype
     * @param line
     * @param ngramlength
     */
    public void generateNGramsFromLine(CharTypes chartype,String line,Integer ngramlength){
        if(line.isEmpty()){
            return;
        }
        String currentngram;
        int charlength=chartype.getChar_length();
        for(int i=1;i<ngramlength;i++){
            for(int j=0;j<=(line.length()-(i*charlength));j+=(i*charlength)){
                currentngram=line.substring(j,j+i*charlength);
                this.addNGram(chartype,currentngram);
            }
        }
    }

    /**
     * Gets the occurance of the current ngram.
     * @param ngram the ngram to check.
     * @return
     */
    public Double getNGramOccurance(final String ngram){
        if(this.ngramWordsToSize.containsKey(ngram)){
            return this.ngramWordsToSize.get(ngram);
        }
        return 0.;
    }

    /**
     * Gets the representation of ngrams as XML
     * @return the representation as String
     */
    public String toXML(){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            for(Integer key:this.ngramSizeToWords.keySet()){
                for(String tuple:this.ngramSizeToWords.get(key).keySet()){
                    writer.writeStartElement(Tags.NGRAM);
                    writer.writeAttribute(Tags.LENGTH,key.toString());
                    writer.writeAttribute(Tags.FREQ,this.ngramSizeToWords.get(key).get(tuple).toString());
                    writer.writeCharacters(tuple);
                    writer.writeEndElement();
                    writer.writeCharacters("\n");
                }
            }
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println("ToString: "+strwriter.toString());
        return strwriter.toString();
    }
}

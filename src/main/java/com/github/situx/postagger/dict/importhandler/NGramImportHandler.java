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

package com.github.situx.postagger.dict.importhandler;

import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.github.situx.postagger.dict.corpusimport.util.NGramStat;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Created by timo on 31.08.14.
 */
public class NGramImportHandler extends ImportHandler {
    /**The chartype to use.*/
    private final CharTypes charType;
    /**Ngramstats to savve.*/
    private final NGramStat ngramstat;
    /**Temp frequency.*/
    private Double freq;
    /**Temp length of the ngram.*/
    private Integer length;
    /**Temp ngram.*/
    private StringBuilder ngram;
    /**Ngram boolean for xml navigation purposes.*/
    private Boolean ngramb=false;

    /**
     * Constructor for this class.
     * @param ngramstat  the ngramstat to fill
     * @param charTypes the chartype to use
     */
    public NGramImportHandler(final NGramStat ngramstat,final CharTypes charTypes){
         this.ngramstat=ngramstat;
         this.charType=charTypes;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
         if(ngramb){
             this.ngram.append(new String(ch,start,length));
         }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        switch (qName){
            case Tags.NGRAM: this.ngramstat.addNGram(charType,ngram.toString(),length,freq);
                ngram=new StringBuilder();
                ngramb=false;
            break;
            default:
        }
    }

    @Override
    public String reformatToASCIITranscription(final String transcription) {
        return transcription;
    }

    @Override
    public String reformatToUnicodeTranscription(final String transcription) {
        return transcription;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        switch(qName){
            case Tags.NGRAM: this.length=Integer.valueOf(attributes.getValue(Tags.LENGTH));
                this.freq= Double.valueOf(attributes.getValue(Tags.FREQ));
                ngramb=true;
                break;
            default:
        }
    }
}

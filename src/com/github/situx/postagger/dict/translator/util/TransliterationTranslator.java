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

package com.github.situx.postagger.dict.translator.util;

import com.github.situx.postagger.main.gui.util.HighlightData;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.methods.Methods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by timo on 6/13/15.
 */
public class TransliterationTranslator extends Translator {
    CharTypes charType;

    public TransliterationTranslator(final CharTypes hittitechar) {
        this.charType=hittitechar;
        this.dictHandler=hittitechar.getCorpusHandlerAPI().getUtilDictHandler();
        this.posTagger=hittitechar.getCorpusHandlerAPI().getPOSTagger(false);
        this.lineToWordCount=new TreeMap<>();
    }

    @Override
    public String wordByWordPOStranslate(final String translationText, final Boolean translit, final Integer initialPos) {
        this.length=new LinkedList<>();
        this.result=new StringBuilder();
        this.currentpos=initialPos;
        this.lastWritten="";
        this.lasttranslation=null;
        this.lineToWordCount.put(linecount,wordcount);
        List<Tuple<String,POSDefinition>> res=new LinkedList<>();

        for(String word:translationText.split(" ")) {
            List<POSDefinition> defs = this.posTagger.getPosTagDefs(word, dictHandler);
            if (defs.isEmpty()) {
                lastWritten = charType.getCorpusHandlerAPI().getUtilDictHandler().reformatToASCIITranscription(this.POSTagToRule(word, translit)) + "* ";
                result.append(lastWritten);
                this.length.add(new HighlightData(this.currentpos, this.currentpos += lastWritten.length(), "DEFAULT", lasttranslation, new LinkedList<>(), word, false, null));
            } else {
                lastWritten = charType.getCorpusHandlerAPI().getUtilDictHandler().reformatToASCIITranscription(this.POSTagToRule(word, translit)) + " ";
                result.append(lastWritten);
                this.length.add(new HighlightData(this.currentpos, this.currentpos += lastWritten.length(), defs.get(0).getDesc(), lasttranslation, res, word, false, null));
            }
            this.wordcount++;
        }
        this.linecount++;
        return result.toString();
    }

    public String POSTagToRule(String word,Boolean translit) {

        StringBuilder result=new StringBuilder();
            if (translit) {
                System.out.println("TransliterationToCuneiform");
                word = charType.getCorpusHandlerAPI().transliterationToText(word.toLowerCase(), 0, charType.getCorpusHandlerAPI().getUtilDictHandler(), false, true);
                if (word == null || word.isEmpty() || word.matches("[ ]+")) {
                    result.append("[");
                    result.append(word);
                    result.append("]");
                } else {
                    result.append(word.substring(0, word.length() - 1));
                    result.append(" ");
                }

            } else {
                System.out.println("CuneiformToTransliteration");
                word = Methods.assignTransliteration(word.split(" "), charType.getCorpusHandlerAPI().getUtilDictHandler(), TransliterationMethod.PROB,false) + "*";
                if (word.isEmpty() || word.matches("[ ]+")) {
                    result.append("");
                } else {
                    result.append(word.substring(0, word.length() - 1));
                    result.append(" ");
                }
            }
        return result.toString();
    }

}

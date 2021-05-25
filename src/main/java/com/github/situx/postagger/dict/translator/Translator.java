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

package com.github.situx.postagger.dict.translator;

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.translator.cunei.ToHittiteTranslator;
import com.github.situx.postagger.dict.translator.cunei.ToSumerianTranslator;
import com.github.situx.postagger.main.gui.util.HighlightData;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.dict.translator.cunei.ToAkkadTranslator;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by timo on 13.10.14.
 */
public abstract class Translator {

    protected List<HighlightData> length;

    public Map<Integer,Integer> lineToWordCount;

    protected POSTagger posTagger;

    protected DictHandling dictHandler;

    public Map<String,List<String>> sumdict;

    protected Integer currentpos;

    protected POSDefinition lasttranslation;

    protected POSDefinition lastlasttranslation;

    protected String lastWritten;
    protected StringBuilder result;

    protected Integer wordcount=0,linecount=1;

    public static String translateTo(CharTypes from,CharTypes to,String toTranslate,TranslationMethod translationMethod){
        String result;
        switch (from){
            case AKKADIAN: result=new ToAkkadTranslator().translate(to, toTranslate, translationMethod);
                break;
            default: result="";
        }
        return result;
    }

    public List<HighlightData> getLength() {
        return length;
    }

    public String getResult() {
        return result.toString();
    }

    public static Translator getTranslator(CharTypes from,CharTypes to){
        switch (from){
            case AKKADIAN: return new ToAkkadTranslator().getTranslator(to);
            case HITTITE: return new ToHittiteTranslator().getTranslator(to);
            case SUMERIAN: return new ToSumerianTranslator().getTranslator(to);
            default: return new ToAkkadTranslator().getTranslator(to);
        }
    }

    public static Translator getTranslator(CharTypes from,CharTypes to,POSTagger frompos){
        switch (from){
            case AKKADIAN: return new ToAkkadTranslator().getTranslator(to,frompos);
            case HITTITE: return new ToHittiteTranslator().getTranslator(to,frompos);
            case SUMERIAN: return new ToSumerianTranslator().getTranslator(to,frompos);
            default: return new ToAkkadTranslator().getTranslator(to,frompos);
        }
    }



    public abstract String wordByWordPOStranslate(String translationText,Boolean pinyin,Integer initialPos);

    public static String separateConsonants(final String word){
        Boolean wasConsonant=false,isConsonant;
        String wasConsonantStr="",isConsonantStr="";
        StringBuilder result=new StringBuilder();
        for(int i=0;i<word.length();i++){
            isConsonant=isConsonant(word.substring(i, i + 1));
            if(isConsonant){
                isConsonantStr=word.substring(i,i+1);
            }else {
                isConsonantStr="";
            }
            if(isConsonant && wasConsonant && wasConsonantStr.equals(isConsonantStr)){
                result.append(" ");
                result.append(word.substring(i,i+1).toUpperCase());
            }else if(isConsonant){
                result.append(word.substring(i,i+1));
                wasConsonantStr=word.substring(i,i+1);
                wasConsonant=true;
            }else{
                result.append(word.substring(i,i+1));
                wasConsonantStr="";
                wasConsonant=false;
            }
        }
        return result.toString();
    }

    public static boolean isVowel(String c){
        String vowels = "aeiouAEIOU";
        return vowels.contains(c+"");
    }

    public static boolean isAllUpperCaseOrNumber(String c){
        Pattern upperregex=Pattern.compile("[A-Z0-9Š]+");
        return upperregex.matcher(c).matches();
    }

    public static boolean isAllUpperCaseOrNumberOrDelim(String c){
        Pattern upperregex=Pattern.compile("[A-ZŠ0-9 -]+");
        return upperregex.matcher(c).matches();
    }

    public static boolean isConsonant(String c){
        String cons = "bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ";
        return cons.contains(c+"");
    }
}

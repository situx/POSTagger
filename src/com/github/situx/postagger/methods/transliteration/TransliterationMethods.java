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

package com.github.situx.postagger.methods.transliteration;

import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.methods.Methods;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.util.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by timo on 22.06.14.
 */
public abstract class TransliterationMethods extends Methods {

    /**
     * Assigns a transliteration to a given segmented line.
     * @param cuneiwords The line segmentation as String array
     * @param dicthandler the dicthandler needed for transliterating
     * @param transliterationMethod the transliteration method to choose
     * @return the segmented, transliterated String
     */
    public String assignTransliteration(String[] cuneiwords, DictHandling dicthandler, TransliterationMethod transliterationMethod){
        LangChar tempword;
        String result="";
        for(int i=0;i<cuneiwords.length;){
            cuneiwords[i]=cuneiwords[i].toLowerCase();
        }
        for(int i=0;i<cuneiwords.length;){
            //System.out.println("Stringarray["+i+"]: "+cuneiwords[i]);
            if(cuneiwords[i].equals(" ") || cuneiwords[i].isEmpty()){
                i++;
                continue;
            }
            else if(i==cuneiwords.length-1){
                tempword=dicthandler.matchChar(cuneiwords[cuneiwords.length-1]);
                if(tempword!=null) {
                    //System.out.println("Result+= "+"[" + ((CuneiChar) tempword).getFirstSingleTransliteration() + "] ");
                    result+="[" + dicthandler.getDictTransliteration (tempword,transliterationMethod)/*.getFirstSingleTransliteration()*/ + "] ";
                }
                else{
                    //System.out.println("Result+= "+dicthandler.getNoDictTransliteration(cuneiwords[i], transliterationMethod));
                    result+=dicthandler.getNoDictTransliteration(cuneiwords[i], transliterationMethod);
                }
                i++;
                continue;
            }
            tempword=dicthandler.matchWord(cuneiwords[i]);
            if(tempword!=null) {
                //System.out.println("Result+= "+"[" + tempword.getTransliterationSet().iterator().next() + "] ");
                result+="[" + dicthandler.getDictTransliteration (tempword,transliterationMethod)/*tempword.getTransliterationSet().iterator().next()*/ + "] ";
            }
            else{
                //System.out.println("Result+= "+dicthandler.getNoDictTransliteration(cuneiwords[i], transliterationMethod));
                result+=dicthandler.getNoDictTransliteration(cuneiwords[i], transliterationMethod);
            }
            i++;
            /*if(cuneiwords[i].length()/2==0){
                i++;
            }else{
                i+=cuneiwords[i].length()/2;
            }*/
        }
        if(( !result.isEmpty() || result.equals(" ")) && result.substring(result.length()-1).equals("-")){
            result=result.substring(0,result.length()-1)+"]";
        }
        return result;
    }

    /**
     * Matches the most appropriate transliteration according to the position of the character in the word.
     * @param position the position to search for
     * @param currentchar the char to search for
     * @return the corresponding transliteration
     */
    public Transliteration getTransliterationByPosition(final int position, final LangChar currentchar){
        if(currentchar.getTransliterationSet().isEmpty()){
            return null;
        }
        for(Transliteration translit:currentchar.getTransliterationSet()){
            switch(position){
                case 0: if(translit.isBeginTransliteration()){
                    return translit;
                }break;
                case 1: if(translit.isMiddleTransliteration()){
                    return translit;
                }break;
                case 2: if(translit.isEndTransliteration()){
                    return translit;
                }break;
                case 3: if(translit.isSingleTransliteration()){
                    return translit;
                }break;

                default:
            }
        }
        return currentchar.getTransliterationSet().iterator().next();
    }

    /**
     * Converts a transliterated text to a cuneiform text.
     * @param dicthandler dicthandler for translation
     * @throws IOException on error
     */
    protected String translitToCuneiform(DictHandling dicthandler,String outfile) throws IOException {
        String[]temparray=this.text.split(" |-");
        StringBuilder result=new StringBuilder();
        this.translitResultWriter = new BufferedWriter(new FileWriter(new File(Files.CUNEIOUT.toString()),false));
        System.out.println(temparray[0]);
        for(String word:temparray){
            word=word.toLowerCase();
            //System.out.println(word);
        }
        for(String word:temparray){
            System.out.print(word+" - ");
            System.out.println(dicthandler.translitToChar(word));
            if(dicthandler.translitToChar(word)!=null && !word.equals("null") && !dicthandler.translitToChar(word).equals("null")) {
                result.append(dicthandler.translitToChar(word));
                this.translitResultWriter.write(dicthandler.translitToChar(word).getCharacter());
            }
            else if(word!=null && !word.equals("null")) {
                this.translitResultWriter.write(word);
                result.append(dicthandler.translitToChar(word));
            }
        }
        this.translitResultWriter.close();
        return result.toString();
    }
}

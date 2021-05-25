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

package com.github.situx.postagger.dict.importhandler.cuneiform;

import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.importhandler.ImportHandler;

import java.util.Map;

/**
 * Created by timo on 24.06.14.
 */
public class CuneiImportHandler extends ImportHandler {

    protected Map<String,CuneiChar> resultmap;
    protected Map<String,String> transcriptToCuneiMap;
    protected Map<String, CuneiChar> logographs;
    protected Map<String,String> translitToCuneiMap;




    /**
     * Reformats a string to the ATF format.
     * @param transcription the String to reformat
     * @return the reformatted String
     */
    @Override
    public String reformatToASCIITranscription(final String transcription) {
        if(transcription.matches(".*[0-9].*")){
            return transcription;
        }
        String result=transcription;
        int i=0,length=0;
        if(transcription.isEmpty()){
            return "";
        }
        result=transcription.replace("!", "").replace("#","").replaceAll("\\*","");
        result=result.replace("š","sz").replace("Š","SZ").replace("ṣ","s,").replace("Ṣ","S,")
                .replace("ḫ","h").replace("Ḫ","H").replace("ĝ","g").replace("ṭ","t,").replace("Ṭ","T,");
        result=result.replace("â","a").replace("ā","a").replace("Á","A2").replace("á","a2").replace("À","A3").replace("à","a3")
                .replace("ê","e").replace("ē","e").replace("É","E2").replace("é","e2").replace("È","E3").replace("è","e3")
                .replace("î","i").replace("ī","i").replace("Í","I2").replace("í","i2").replace("Ì","I3").replace("ì","i3")
                .replace("û","u").replace("ū", "u").replace("Ú","U2").replace("ú","u2").replace("Ù","U3").replace("ù","u3");
        result=result.replace("₀", "0").replace("₁","1").replace("₂","2").replace("₃","3")
                .replace("₄","4").replace("₅","5").replace("₆","6").replace("₇","7").replace("₈","8").replace("₉","9");
        length=result.length();
        while(!(length<2) && Character.isDigit(result.toCharArray()[length-1])){
            length-=1;
        }
        for(i=0;i<length;i++){
            if(Character.isDigit(result.charAt(i))){
                result.replace(""+result.charAt(i),"");
                //result+=result.charAt(i);
            }
        }
        return result;
    }


    @Override
    public String reformatToUnicodeTranscription(final String transcription) {
        System.out.println("ReformatToUnicode: "+transcription);
        String result=transcription;
        int i=0,length=0;
        result=transcription.replace("!","").replace("#","");
        result=result.replace("sz","š").replace("SZ","Š").replace("s,","ṣ").replace("S,","Ṣ").
                replace("h","ḫ").replace("H","Ḫ").replace("ĝ","g").replace("t,", "ṭ").replace("T,", "Ṭ");
        result=result.replace("a:","ā").replace("a2","á").replace("a3","à")
                .replace("e:","ē").replace("e2","é").replace("e3","è")
                .replace("i:", "ī").replace("i2,", "í").replace("i3", "ì")
                .replace("u:", "ū").replace("u2", "ú").replace("u3,","ù");
        result=result.replace("0", "₀").replace("1", "₁").replace("2", "₂").replace("3", "₃")
                .replace("4", "₄").replace("5", "₅").replace("6", "₆").replace("7", "₇").replace("8", "₈").replace("9","₉");
        length=result.length();
        while(!result.isEmpty() && Character.isDigit(result.toCharArray()[length-1])){
            length-=1;
        }
        for(i=0;i<length;i++){
            if(Character.isDigit(result.charAt(i))){
                result.replace(""+result.charAt(i),"");
                result+=result.charAt(i);
            }
        }
        System.out.println("Reformatted: "+result.toLowerCase());
        return result;
        //return result.toLowerCase();
    }


}

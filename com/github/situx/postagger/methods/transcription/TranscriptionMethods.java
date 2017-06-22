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

package com.github.situx.postagger.methods.transcription;

import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TranscriptionMethod;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.importhandler.cuneiform.CuneiImportHandler;
import com.github.situx.postagger.methods.Methods;
import com.github.situx.postagger.util.enums.methods.ClassificationMethod;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.util.Files;

import java.io.*;
import java.util.*;

/**
 * Created by timo on 29.06.14.
 */
public class TranscriptionMethods extends Methods {

     public TranscriptionMethods(){

     }

    private static String minwctranscriptToTranslit(String word, DictHandling dicthandler){
        String tempwordstr;
        Map<Integer,Map<Integer,Set<String>>> posToWord=new TreeMap<>();
        Map<Integer,List<Integer>> posToChosenWords=new TreeMap<>();
        //System.out.println("Currentstr: "+word);
        Integer[] posToAmount=new Integer[word.length()];
        for(int i=0;i<word.length();i++){
            posToAmount[i]=(word.length())-i;
            posToChosenWords.put(i,new LinkedList<Integer>());
            /*for(int j=0;j<(word.length())-i;j++){
                posToChosenWords.get(i).add(1);
            }*/
            posToWord.put(i,new TreeMap<Integer,Set<String>>());
            //posToWord.get(i).put(1,new TreeSet<String>());
            //posToWord.get(i).get(1).add(word.substring(i, i+1));
        }
        for(int i=0;i<word.length();i++){
            for(int j=i+1;j<=word.length();j++){
                tempwordstr= word.substring(i,j);
                //System.out.println("i "+i+" stringarray.length "+word.length()+" - "+tempwordstr);
                if(dicthandler.translitToChar(tempwordstr)!=null) {
                    //System.out.println("Add Word:" + tempwordstr);

                    if(!posToWord.get(i).containsKey(tempwordstr.length())){
                        posToWord.get(i).put(tempwordstr.length(),new TreeSet<String>());
                    }
                    posToWord.get(i).get(tempwordstr.length()).add(tempwordstr);
                }
            }
        }
        //System.out.println("PosToWord: "+posToWord);
        int index;
        for(int i=word.length();i>-1;i--){
            //probs[i-(i/2)]=0.;
            index=i;
            int remembercurpos=index,amount=0;
            List<Integer> currentlychosenwords=new LinkedList<>();
            //System.out.println("posToWord.containsKey("+index+") "+posToWord.containsKey(index));
            if(posToWord.containsKey(index)) {
                //System.out.println("posToWord.get("+(index)+").keySet()"+posToWord.get(index).keySet());
                for (Integer keyAtPosition : posToWord.get(index).keySet()) {
                    amount = 0;
                    remembercurpos = index + keyAtPosition;
                    amount++;
                    currentlychosenwords.add(keyAtPosition);
                    //System.out.println("Remembercurpos: " + remembercurpos + " Currentline.length()/2 " + word.length());
                    if (remembercurpos == word.length()) {
                        if ((posToAmount[index] > amount && !posToChosenWords.get(index).isEmpty()) || posToChosenWords.get(index).isEmpty()) {
                            posToAmount[index] = amount;
                            posToChosenWords.put(index, currentlychosenwords);
                        }
                    } else if (remembercurpos < word.length()) {
                        if (posToAmount[remembercurpos] != Integer.MAX_VALUE) {
                            currentlychosenwords.addAll(posToChosenWords.get(remembercurpos));
                            amount += posToAmount[remembercurpos];
                            if (posToAmount[index] > amount) {
                                posToAmount[index] = amount;
                                posToChosenWords.put(index, currentlychosenwords);
                            }

                            //System.out.println(posToChosenWords.get(remembercurpos));
                        } else if (posToAmount[index] > amount) {
                                posToAmount[index] = amount;
                                posToChosenWords.put(index, currentlychosenwords);
                        }
                    }
                    /*System.out.print("PosToAmount [");
                    for (Integer inter : posToAmount) {
                         System.out.print(inter + ",");
                     }
                    System.out.println("]");
                    System.out.println("PosToWord " + posToWord);
                    System.out.println("PosToChosenWord " + posToChosenWords);
                    */
                    currentlychosenwords = new LinkedList<>();
                }


            }
            //System.out.println(currentlychosenwords);

        }           int positioncounter=0,cuneiwordcounter=0;
        String cuneiword,result="[",cnresult="";
        String[] cuneiwords=posToChosenWords.containsKey(0)?new String[posToChosenWords.get(0).size()]:new String[0];
        if(posToChosenWords.containsKey(0)){
            for(Integer chosen:posToChosenWords.get(0)) {
                //System.out.println("Positioncounter: " + positioncounter + " Chosen*2 " + chosen + " Currentline.length() " + word.length());
                cuneiword=word.substring(positioncounter,positioncounter+chosen);
                result+=cuneiword+"-";
                cnresult+=dicthandler.translitToChar(cuneiword)+"-";
                cuneiwords[cuneiwordcounter++]=cuneiword;
                positioncounter+=chosen;
            }}
        if(result.length()>1){
            result=result.substring(0,result.length()-1)+"] ";
        }else{
            result+="] ";
        }

        //System.out.println("Cuneiwords: "+result+" - "+cnresult);
        return result;
    }

    public static String transcriptToTranslit(String word, DictHandling dictHandler){
        //System.out.println("Originalword: "+word);
        if(word.contains("-")){
            return word;
        }
        String temp,part="",result="",cuneiresult="[ ";
        Integer currentposition=0;
        Boolean hasmatched=false;
        if(word.contains(" ")){
            for(String w:word.split(" ")) {
                result+=minwctranscriptToTranslit(new CuneiImportHandler().reformatToASCIITranscription(w),dictHandler);
                /*currentposition = 0;
                for (int i = 0; i < temp.length(); i++) {
                    part = temp.substring(currentposition, i);
                    System.out.println("Current part in dict? " + part + " " + dictHandler.translitToChar(part));
                    if (dictHandler.translitToChar(part) == null && currentposition != i && hasmatched) {
                        result += part.substring(0, part.length() - 1) + "-";
                        currentposition = i - 1;
                        cuneiresult += dictHandler.translitToChar(part.substring(0, part.length() - 1)) + " - ";
                        hasmatched = false;
                    } else if (dictHandler.translitToChar(part) != null) {
                        hasmatched = true;
                    }
                }
                if (hasmatched) {
                    result += temp.substring(currentposition, temp.length());
                    cuneiresult += dictHandler.translitToChar(temp.substring(currentposition, temp.length()));
                } else {
                    cuneiresult += " ]";
                }
                //result += temp.substring(currentposition, temp.length());
                //cuneiresult += dictHandler.translitToChar(temp.substring(currentposition, temp.length())) + " ]";
                if (result.charAt(result.length() - 1) == '-') {
                    result = result.substring(0, result.length() - 1) + "]";
                } else {
                    result += "]";
                }*/
            }}else{
            result=minwctranscriptToTranslit(new CuneiImportHandler().reformatToASCIITranscription(word),dictHandler);
                /*for(int i=0;i<temp.length();i++){
                    part=temp.substring(currentposition,i);
                    System.out.println("Current part in dict? "+part+" "+dictHandler.translitToChar(part));
                    if(dictHandler.translitToChar(part)==null && currentposition!=i && hasmatched){
                        result+=part.substring(0,part.length()-1)+"-";
                        currentposition=i-1;
                        cuneiresult+=dictHandler.translitToChar(part.substring(0,part.length()-1))+" - ";
                        hasmatched=false;
                    }else if(dictHandler.translitToChar(part)!=null){
                        hasmatched=true;
                    }
                }
                result+=temp.substring(currentposition,temp.length());
                cuneiresult+=dictHandler.translitToChar(temp.substring(currentposition,temp.length()))+" ]";
                if(result.charAt(result.length()-1)=='-'){
                    result=result.substring(0,result.length()-1)+"]";
                }else{
                    result+="]";
                }*/
        }
        //System.out.println("Word Syllables: "+result);
        return result;
    }

    public static String translitTotranscript(String word){
        return word.replaceAll("\\[","").replaceAll("]","").replaceAll("-","").replaceAll("aa","a1").replaceAll("ee","e1").replaceAll("uu","u1").replaceAll("oo","o1").replaceAll("ii","i1");
    }

    /**
     * Initializes parameters needed for parsing.
     * @param sourcepath the path of the sourcefile
     * @param destpath the path of the destination file
     * @param dictHandler the dicthandler to use
     * @param transcriptionMethod the transcriptmethod to use
     * @throws java.io.IOException
     */
    public void initParsing(final String sourcepath, final String destpath, final DictHandling dictHandler, final TranscriptionMethod transcriptionMethod, final TransliterationMethod transliterationMethod, final ClassificationMethod classificationMethod, final CharTypes chartype) throws IOException {
        String currentline;
        LangChar tempchar=null;
        System.out.println("Sourcepath: "+sourcepath+" Destpath: "+destpath);
        this.reader=new BufferedReader(new FileReader(new File(Files.RESULTDIR.toString()+Files.TRANSCRIPTDIR.toString()+sourcepath)));
        this.translitResultWriter =new BufferedWriter(new FileWriter(new File(Files.RESULTDIR.toString()+Files.SYLLDIR.toString()+destpath)));
        while((currentline=this.reader.readLine())!=null){
            this.linecounter++;
            System.out.println("Currentline: "+currentline);
            switch (transcriptionMethod){
                case TRANSCRIPTTOTRANSLIT: this.translitResultWriter.write(transcriptToTranslit(currentline, dictHandler)+"\n");break;
                //case TRANSLITTOTRANSCRIPT: translitTotranscript(tempchar, currentline, dicthandler,transcriptionMethod);break;
                default:
            }
        }
        this.translitResultWriter.close();
        this.reader.close();
    }

    public void transcriptToTranslit(final String filepath,final DictHandling dictHandler,final TranscriptionMethod transcriptionMethod,final TransliterationMethod transliterationMethod,final ClassificationMethod classificationMethod) throws IOException {
        this.initParsing(filepath.substring(filepath.lastIndexOf("/")+1),filepath.substring(filepath.lastIndexOf("/")+1),dictHandler,transcriptionMethod,transliterationMethod,classificationMethod,CharTypes.AKKADIAN);
    }
}

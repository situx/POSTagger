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

package com.github.situx.postagger.methods.translation;

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.methods.Methods;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.util.Files;

import java.io.*;
import java.util.List;
import java.util.Locale;

/**
 * Created by timo on 22.06.14.
 */
public class TranslationMethods extends Methods {

    private Locale fromLocale;
    private Locale toLocale;
    private BufferedWriter translationResultWriter;

    public TranslationMethods(){

    }

        private void POSTagLemmaTranslation(LangChar tempword,final String currentline,final DictHandling dicthandler,final TranslationMethod translationMethod, Locale locale) throws IOException {
            String[] words=currentline.split(" ");
            List<POSDefinition> posdeflist;
            StringBuilder result=new StringBuilder();
            for(String word:words){
                posdeflist=dicthandler.getPosTagger().getPosTag(word,dicthandler,true);
                for(POSDefinition posdef:posdeflist){
                    if(posdef.getValue().length!=0){
                       result.append(posdef.getValue()[0]);
                    }
                }

                //System.out.print("Locale: "+locale.toString());
                if((tempword=dicthandler.matchWordByTransliteration(word.replace("]","").replace("[","")))!=null && tempword.getTranslationSet(locale)!=null && !tempword.getTranslationSet(locale).isEmpty()){
                    //System.out.print("Word: " + word.replace("]", "").replace("[", ""));
                    //System.out.println(" OK");
                    this.translationResultWriter.write("["+dicthandler.getDictTranslation(tempword,translationMethod,locale)+"] ");
                }else if((tempword=dicthandler.matchWordByTranscription(TranscriptionMethods.translitTotranscript(word.replace("]", "").replace("[", "")),true))!=null && tempword.getTranslationSet(locale)!=null && !tempword.getTranslationSet(locale).isEmpty()){
                    System.out.println("Non Cunei Word: "+word);
                    this.translationResultWriter.write("["+dicthandler.getDictTranslation(tempword,translationMethod,locale)+"] ");
                }else{
                    this.translationResultWriter.write(dicthandler.getNoDictTranslation(word,translationMethod,locale).trim()+" ");
                    //System.out.println(" Not OK");
                }
            }
            this.translationResultWriter.write("\n");
        }

    private void posTagBasedDirectTranslation(POSTagger posTagger, Translator translator){

    }

    /**
     * Initializes parameters needed for parsing.
     * @param sourcepath the path of the sourcefile
     * @param destpath the path of the destination file
     * @param dicthandler the dicthandler to use
     * @throws java.io.IOException
     */
    public void initTranslation(final String sourcepath,final String destpath,final DictHandling dicthandler,final TranslationMethod translationMethod,final Locale locale) throws IOException {
        String currentsentence;
        LangChar tempchar=null;
        this.reader=new BufferedReader(new FileReader(new File(sourcepath)));
        File writefile=new File(Files.RESULTDIR.toString()+Files.TRANSLATIONDIR.toString()+locale.toString());
        writefile.mkdirs();
        writefile=new File(Files.RESULTDIR.toString()+Files.TRANSLATIONDIR.toString()+locale.toString()+File.separator+destpath);
        this.translationResultWriter =new BufferedWriter(new FileWriter(writefile));
        while((currentsentence=this.reader.readLine())!=null) {
            this.linecounter++;
            switch (translationMethod) {
                case LEMMAFIRST:
                case LEMMA:
                case LEMMAPROB:
                case LEMMARANDOM:
                default:   this.lemmaTranslation(null, currentsentence, dicthandler, translationMethod,locale);
            }
            this.translationResultWriter.write("\n");
        }
        this.translationResultWriter.close();
        this.reader.close();

    }

        public void lemmaTranslation(LangChar tempword,final String currentline,final DictHandling dicthandler,final TranslationMethod translationMethod,Locale locale) throws IOException {
              String[] words=currentline.split(" ");
              StringBuilder result=new StringBuilder();
              for(String word:words){

                  //System.out.print("Locale: "+locale.toString());
                  if((tempword=dicthandler.matchWordByTransliteration(word.replace("]","").replace("[","")))!=null && tempword.getTranslationSet(locale)!=null && !tempword.getTranslationSet(locale).isEmpty()){
                      //System.out.print("Word: " + word.replace("]", "").replace("[", ""));
                      //System.out.println(" OK");
                       this.translationResultWriter.write("["+dicthandler.getDictTranslation(tempword,translationMethod,locale)+"] ");
                  }else if((tempword=dicthandler.matchWordByTranscription(TranscriptionMethods.translitTotranscript(word.replace("]", "").replace("[", "")),true))!=null && tempword.getTranslationSet(locale)!=null && !tempword.getTranslationSet(locale).isEmpty()){
                      System.out.println("Non Cunei Word: "+word);
                      this.translationResultWriter.write("["+dicthandler.getDictTranslation(tempword,translationMethod,locale)+"] ");
                  }else{
                      this.translationResultWriter.write(dicthandler.getNoDictTranslation(word,translationMethod,locale).trim()+" ");
                      //System.out.println(" Not OK");
                  }
              }


        }

    /**
     * MinMatch method.
     * @param filepath the path of the file to use
     * @param dicthandler the dicthandler to use
     * @throws IOException
     */
    public void lemmaTranslation(final String filepath,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,final Locale locale) throws IOException {
        this.initTranslation(filepath, filepath.substring(filepath.lastIndexOf("/") + 1), dicthandler,TranslationMethod.LEMMA,locale);
    }


}

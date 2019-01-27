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

package com.github.situx.postagger.methods.segmentation.rule;

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.PositionableChar;
import com.github.situx.postagger.methods.segmentation.SegmentationMethods;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.util.enums.methods.ClassificationMethod;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.util.Files;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 17.11.13
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */
public class RuleMethods extends SegmentationMethods implements RuleAPI{
    /**Random object for random segmentation.*/
    private final Random random;
    Integer avglength,wordmatches=0;
    boolean begin=true;
    public RuleMethods(){
        super();
        this.random=new Random(System.currentTimeMillis());

    }

    @Override
    public String charSegmentParse(final String filepath, final String destpath, final DictHandling dict, final TransliterationMethod transliterationMethod, final CharTypes chartype, final Boolean transcriptToTranslit, final Boolean corpusstr, final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.CHARSEGMENTPARSE.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dict, ClassificationMethod.CHARSEGMENTPARSE,transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.CHARSEGMENTPARSE.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dict, ClassificationMethod.CHARSEGMENTPARSE,transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);

        }
        return this.cuneiBuffer.toString();
        }

    /**
     * Implements the char segment parse method.
     * @param tempchar the LangChar to use
     * @param temp
     * @param dicthandler the dicthandler to use
     * @throws IOException
     */
    private String[] charSegmentParse(LangChar tempchar, final String temp, final DictHandling dicthandler, final TransliterationMethod transliterationMethod, final CharTypes chartype) throws IOException {
        String transset="",res="";
        int charlength=chartype.getChar_length();
        System.out.println(temp);
        List<String> result=new LinkedList<>();
        for(int i=0;i<temp.length()-1;i+=charlength){
            tempchar=dicthandler.matchChar(temp.substring(i,i+charlength));
            if(tempchar!=null){
                res+=tempchar.getCharacter()+" ";
                //this.cuneiResultWriter.write();
                result.add(tempchar.getCharacter());
                this.words.add(transset);
                this.wordboundaries.add(1);
            }
        }
        String translit=assignTransliteration(result.toArray(new String[result.size()]), dicthandler, transliterationMethod,false);
        return new String[]{res.trim(),translit.trim()};
    }

    @Override
    public void initParsing(final String sourcepath, final String destpath, final DictHandling dicthandler, final ClassificationMethod method, final TransliterationMethod transliterationMethod, final CharTypes chartype, final Boolean transcriptToTranslit, final Boolean corpusstr, final Boolean printFiles) throws IOException {
        String currentline;
        LangChar tempchar=null;
        int currentposition;
        Set<String> stopwords=new HashSet<>(chartype.getStopchars());
        List<String> segments=new LinkedList<>();
        this.words.clear();
        this.wordboundaries.clear();
        if(corpusstr){
            this.reader=new BufferedReader(new StringReader(sourcepath));
        }else{
            this.reader=new BufferedReader(new FileReader(new File(sourcepath)));
        }
        this.cuneiBuffer=new StringBuilder();
        if(printFiles) {
            if (transcriptToTranslit) {
                this.translitResultWriter = new BufferedWriter(new FileWriter(new File(Files.RESULTDIR.toString() + Files.TRANSLITDIR.toString() + destpath)));
            } else {
                this.translitResultWriter = new BufferedWriter(new FileWriter(new File(Files.RESULTDIR.toString() + Files.TRANSLITDIR.toString() + destpath)));
                this.transcriptResultWriter = new BufferedWriter(new FileWriter(new File(Files.RESULTDIR.toString() + Files.TRANSCRIPTDIR.toString() + destpath)));
                this.cuneiResultWriter = new BufferedWriter(new FileWriter(new File(Files.RESULTDIR.toString() + Files.CUNEIFORMDIR.toString() + destpath)));
            }
        }
        if(method== ClassificationMethod.PREFSUFF){
            this.begin=true;
        }
        if(method== ClassificationMethod.AVGWORDLEN){
            this.avglength=(dicthandler.getAvgWordLength()==null)?dicthandler.getChartype().getChar_length():dicthandler.getAvgWordLength().intValue();
        }
        while((currentline=this.reader.readLine())!=null){
            System.out.println("Currentline: "+currentline);
            segments.clear();
            currentposition=0;
            this.linecounter++;
            for(int i=0;i<currentline.length()-chartype.getChar_length();i+=chartype.getChar_length()){
                String currentchar=currentline.substring(i,i+=chartype.getChar_length());
                if(stopwords.contains(currentchar)){
                    segments.add(currentline.substring(currentposition,i));
                    segments.add(currentchar);
                    currentposition=i+chartype.getChar_length();
                }
            }
            if(segments.isEmpty()){
                segments.add(currentline);
            }
            String[] result=new String[0];
            for(String segment:segments) {
                switch (method) {
                    case AVGWORDLEN:
                        result=this.matchByAvgWordLength(tempchar, segment, dicthandler, transliterationMethod, chartype);
                        break;
                    case CHARSEGMENTPARSE:
                        result=this.charSegmentParse(tempchar, segment, dicthandler, transliterationMethod, chartype);
                        break;
                    case PREFSUFF:
                        result=this.prefixSuffixMatching((CuneiChar) tempchar, segment, dicthandler, transliterationMethod, chartype);
                        break;
                    case RANDOMSEGMENTPARSE:
                        result=this.randomSegmentParse(tempchar, segment, dicthandler, transliterationMethod, chartype);
                        break;
                    case TANGO:
                        result=this.tangoAlgorithm(tempchar, segment, dicthandler, 2, transliterationMethod, chartype);
                        break;
                    default:
                }
                this.cuneiBuffer.append(result[0]);
                this.cuneiBuffer.append(System.lineSeparator());
                if(printFiles){
                    this.translitResultWriter.write(result[1]);
                    this.translitResultWriter.write(System.lineSeparator());
                    if(!transcriptToTranslit){
                        this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(result[1]));
                        this.cuneiResultWriter.write(result[0]);

                        this.transcriptResultWriter.write(System.lineSeparator());
                        this.cuneiResultWriter.write(System.lineSeparator());
                    }
            }

            }
        }
        if(printFiles) {
            if (!transcriptToTranslit) {
                this.transcriptResultWriter.close();
                this.cuneiResultWriter.close();
            }
            this.translitResultWriter.close();
        }
        this.reader.close();
    }

    public String matchByAvgWordLength(final String filepath,final String destpath, final DictHandling dicthandler, final TransliterationMethod transliterationMethod,final CharTypes chartype,final Boolean transcriptToTranslit,final Boolean corpusstr,final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.AVGWORDLEN.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.AVGWORDLEN,transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.AVGWORDLEN.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.AVGWORDLEN,transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);

        }
        return this.cuneiBuffer.toString();
       }

    private String[] matchByAvgWordLength(LangChar tempchar,String currentline,DictHandling dicthandler,final TransliterationMethod transliterationMethod,final CharTypes chartype) throws IOException {
        int beginvalue=0,charlength=chartype.getChar_length();
        this.tempstr=currentline;
        this.avglength=(dicthandler.getAvgWordLength()==null || dicthandler.getAvgWordLength()==0)?dicthandler.getChartype().getChar_length():dicthandler.getAvgWordLength().intValue();
        System.out.println("Avglength: "+avglength);
        if(this.tempstr.length()<charlength){
            return new String[]{"",""};
        }
        StringBuilder cuneiresult=new StringBuilder();
        int i=0;
        for(i=0;i<=this.tempstr.length()-avglength;i+=avglength){
            beginvalue=i;
            String tmp=this.tempstr.substring(i,i+avglength);
            System.out.println("Tmp: "+tmp);
            String translitresult="[";

            for(int j=0;j<=tmp.length()-charlength;j+=charlength){
                cuneiresult.append(tmp.substring(j,j+charlength));
            }
            cuneiresult.append(/*tmp.substring(tmp.length()-charlength,tmp.length())+*/" ");
        }

        String tmp=this.tempstr.substring(i,this.tempstr.length());
        System.out.println("Tmp: "+tmp);
        if(tmp.length()>charlength){
            //String translitresult+="[";
            //String cuneiresult+="";
            for(int j=0;j<=tmp.length()-charlength;j+=charlength){
                //translitresult+=(dicthandler.matchChar(tmp.substring(j,j+2))).getTransliterationSet().iterator().next().toString()+"-";
                cuneiresult.append(tmp.substring(j,j+charlength));
            }
            cuneiresult.append(/*tmp.substring(tmp.length() - charlength, tmp.length())+*/" ");
        }
        String translit=assignTransliteration(cuneiresult.toString().split(" "),dicthandler,transliterationMethod,false);
        return new String[]{cuneiresult.toString().trim(),translit.trim()};

    }

    @Override
    public void matchWordByFakePOS() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private String[] prefixSuffixMatching(PositionableChar curchar, String currentline, DictHandling dicthandler, final TransliterationMethod transliterationMethod, final CharTypes chartype) throws IOException {
            int charlength=chartype.getChar_length();
            this.tempstr=currentline;
            StringBuilder translitBuilder=new StringBuilder(),cuneiBuilder=new StringBuilder();
            if(this.tempstr.isEmpty()){
                return new String[]{"",""};
            }
            for(int i=0;i<=this.tempstr.length()-charlength;i+=charlength){
                curchar=(PositionableChar)dicthandler.matchChar(this.tempstr.substring(i,i+charlength));
                if(curchar!=null){
                    System.out.println(curchar);
                    System.out.println("Abs: "+curchar.getOccurances()+" Begin: "+curchar.getBeginOccurance()+" Middle: "+curchar.getMiddleoccurance()+" End: "+curchar.getEndOccurance());
                    if(begin){
                        if(curchar.isSingleCharacter() && !curchar.isBeginningCharacter()){
                            //this.translitResultWriter.write("["+this.getTransliterationByPosition(0,curchar).toString()+"]");
                            //this.cuneiResultWriter.write(curchar.getCharacter());
                            translitBuilder.append("[");
                            translitBuilder.append(this.getTransliterationByPosition(0,curchar).toString());
                            translitBuilder.append("]");
                            cuneiBuilder.append(curchar.getCharacter());
                        }
                        if(curchar.isBeginningCharacter()){
                            //this.translitResultWriter.write("["+this.getTransliterationByPosition(0,curchar).toString());
                            translitBuilder.append("[");
                            translitBuilder.append(this.getTransliterationByPosition(0,curchar).toString());
                            //this.cuneiResultWriter.write(curchar.getCharacter());
                            cuneiBuilder.append(curchar.getCharacter());
                            begin=false;
                        }
                    }else if(curchar.isSingleCharacter() && !curchar.isMiddleCharacter() && !curchar.isEndingCharacter()){
                        //this.translitResultWriter.write("] ["+this.getTransliterationByPosition(0,curchar).toString()+"] ");
                        translitBuilder.append("] [");
                        translitBuilder.append(this.getTransliterationByPosition(0,curchar).toString());
                        translitBuilder.append("] ");
                        //this.cuneiResultWriter.write(curchar.getCharacter() + " ");
                        cuneiBuilder.append(curchar.getCharacter());
                        cuneiBuilder.append(" ");
                        begin=true;
                    }
                    else if(curchar.getBeginOccurance()>curchar.getEndOccurance()){
                        translitBuilder.append("] [");
                        translitBuilder.append(this.getTransliterationByPosition(0,curchar).toString());
                        //this.translitResultWriter.write("] ["+this.getTransliterationByPosition(0,curchar).toString());
                        //this.cuneiResultWriter.write(" "+curchar.getCharacter());
                        cuneiBuilder.append(" ");
                        cuneiBuilder.append(curchar.getCharacter());
                    }
                    else if(curchar.isMiddleCharacter() && curchar.getMiddleoccurance()>curchar.getEndOccurance()){
                        translitBuilder.append("-");
                        translitBuilder.append(this.getTransliterationByPosition(1,curchar).toString());
                        //this.translitResultWriter.write("-"+this.getTransliterationByPosition(1,curchar).toString());
                        //this.cuneiResultWriter.write(curchar.getCharacter());
                        cuneiBuilder.append(curchar.getCharacter());
                    }
                    else if(curchar.isEndingCharacter()){
                        //this.translitResultWriter.write("-"+this.getTransliterationByPosition(2,curchar).toString()+"] ");
                        translitBuilder.append("-");
                        translitBuilder.append(this.getTransliterationByPosition(2,curchar).toString());
                        translitBuilder.append("] ");
                        //this.cuneiResultWriter.write(curchar.getCharacter());
                        cuneiBuilder.append(curchar.getCharacter());
                        begin=true;
                    }

                }
            }
            if(!begin){
                translitBuilder.append("]");
                //this.translitResultWriter.write("]");
            }
            begin=true;
            return new String[]{cuneiBuilder.toString(),translitBuilder.toString()};
    }

    @Override
    public String prefixSuffixMatching(final String filepath, final String destpath, final DictHandling dicthandler, final TransliterationMethod transliterationMethod, final CharTypes chartype, final Boolean transcriptToTranslit, final Boolean corpusstr, final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.PREFSUFF.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.PREFSUFF,transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.PREFSUFF.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.PREFSUFF,transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);

        }
        return this.cuneiBuffer.toString();
        }



    /**
     * Random segment parse method.
     * Performs a random segmentation.
     * @param tempchar
     * @param currentline the current line of text to analyse
     * @param dicthandler the dicthandler to use
     * @throws IOException on error
     */
    private String[] randomSegmentParse(LangChar tempchar,final String currentline,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,final CharTypes chartype) throws IOException {
        Integer randomvalue,random2,charlength=chartype.getChar_length();
        Set<Integer> segmentArray=new TreeSet<>();
        randomvalue=this.random.nextInt((currentline.length()/charlength)+charlength);
        System.out.println(""+randomvalue);
        for(int j=0;j<(randomvalue/charlength);j++){//Generate random values for this line
            j--;
            random2=this.random.nextInt(randomvalue);
            if(random2%charlength!=0){
                random2++;
            }
            if(!segmentArray.contains(random2)){
                System.out.println("Add random value: "+random2);
                segmentArray.add(random2);
                j++;
            }
        }
        int currentbegin=0;
        String currentstr="";
        StringBuilder cuneiformresult=new StringBuilder();
        for(Integer seg:segmentArray){
            if(seg==currentbegin)
                continue;

            currentstr=currentline.substring(currentbegin,(seg*charlength)-charlength);
            /*for(int i=0;i<currentstr.length()-1;i+=2){
                tempchar=dicthandler.matchChar(currentstr.substring(i,i+2));
                if(tempchar!=null){
                    resultstr+=tempchar.getTransliterationSet().iterator().next()+"-";
                }
            }*/
            //if(!resultstr.isEmpty()){
            /*this.translitResultWriter.write(dicthandler.getNoDictTransliteration(currentstr,transliterationMethod));
            this.cuneiResultWriter.write(currentstr+" ");*/
            if(!currentstr.isEmpty()) {
                cuneiformresult.append(currentstr);
                cuneiformresult.append(" ");
            }
            this.words.add(dicthandler.getNoDictTransliteration(currentstr,transliterationMethod));
            this.wordboundaries.add(dicthandler.getNoDictTransliteration(currentstr,transliterationMethod).length());
            //}
            currentbegin=(seg*charlength)-charlength;

        }
        currentstr=currentline.substring(currentbegin,currentline.length());
        for(int i=0;i<=currentstr.length()-charlength;i+=charlength){
            tempchar=dicthandler.matchChar(currentstr.substring(i,i+charlength));
            if(tempchar!=null){
                //translitresult+=tempchar.getTransliterationSet().isEmpty()?"":tempchar.getTransliterationSet().iterator().next()+"-";
                cuneiformresult.append(tempchar.getCharacter());
            }
        }
        String translit=assignTransliteration(cuneiformresult.toString().split(" "),dicthandler,transliterationMethod,false);
        return new String[]{cuneiformresult.toString().trim(),translit.trim()};
        /*if(!translitresult.isEmpty()){
            return new String[]{cuneiformresult,"["+translitresult.substring(0,translitresult.length()-1)+"]"};
            //this.translitResulWriter.write("["+translitresult.substring(0,translitresult.length()-1)+"]");
            //this.cuneiResultWriter.write(cuneiformresult);
            //this.words.add(translitresult.substring(0,translitresult.length()-1));
        }else{
            return new String[]{"",""};
        }*/
    }

    @Override
    public String randomSegmentParse(final String filepath, final String destpath, final DictHandling dicthandler, final TransliterationMethod transliterationMethod, final CharTypes chartype, final Boolean transcriptToTranslit, final Boolean corpusstr, Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.RANDOMSEGMENTPARSE.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.RANDOMSEGMENTPARSE, transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.RANDOMSEGMENTPARSE.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.RANDOMSEGMENTPARSE, transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);
        }
        return this.cuneiBuffer.toString();
       }

    @Override
    public String tangoAlgorithm(final String filepath, final String destpath, final DictHandling dicthandler, final int ngramsize, final TransliterationMethod transliterationMethod, final CharTypes chartype, final Boolean transcriptToTranslit, final Boolean corpusstr, final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.TANGO.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.TANGO,transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.TANGO.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.TANGO,transliterationMethod,chartype,transcriptToTranslit,corpusstr,printFiles);

        }
        return this.cuneiBuffer.toString();
         }

    /**
     * Implements the tangoAlgorithm approach.
     * @param tempchar
     * @param line the current line to work with
     * @param dicthandler the dicthandler to use
     * @param ngramsize the ngramsize
     */
    private String[] tangoAlgorithm(final LangChar tempchar,final String line, final DictHandling dicthandler, int ngramsize,final TransliterationMethod transliterationMethod,CharTypes charTypes){
        String ngram,initialngram,initialwordsuffix="",concludingwordprefix="";
        LangChar tempword=null,initialword=null,concludingword=null,middleword=null;
        int charlength=charTypes.getChar_length();
        double maxi=0,maxfrequency=0,normalizedvote;
        double threshold=0,votes=0,votesum=0;
        List<Double> voteslist=new LinkedList<>();
        List<Double> votepositions=new LinkedList<>();
        String max;
        ngramsize*=charlength;
        //System.out.println(ngramsize);
        List<String> ngramlist=new LinkedList<>();
        for(int i=0;i<line.length();i+=charlength){
            //System.out.println(i+ngramsize*2);
            if(i+ngramsize*2<line.length()){
                ngramlist.clear();
                ngram=line.substring(i,i+ngramsize);
                //System.out.println("Ngram: "+ngram);
                for(int j=0;j<ngram.length();j+=charlength){
                    //System.out.println((j+ngramsize/2));
                    if((j+ngramsize/charlength)<=ngram.length()){
                        ngramlist.add(ngram.substring(j,j+ngramsize/charlength));
                    }else{
                        break;
                    }
                }
                System.out.println("Ngram: "+ngram);
                System.out.println("NgramList: "+ngramlist);
                initialngram=ngramlist.get(0);

                votes=0;
                votesum=0;
                for(String anngram:ngramlist){
                    Double initialocc=dicthandler.matchNGramOccurance(initialngram)
                            ,concludingocc=dicthandler.matchNGramOccurance(ngramlist.get(ngramlist.size()-1))
                            ,tempocc=dicthandler.matchNGramOccurance(anngram);
                    /*initialword=;
                    concludingword=dicthandler.matchWord(ngramlist.get(ngramlist.size()-1));
                    tempword=dicthandler.matchWord(anngram);*/
                    if(initialocc!=null){

                        if(tempocc!=null && initialocc>tempocc/*>initialword.getOccurances()*/){
                            votes++;
                            System.out.println(anngram+" ("+tempocc+") - "+initialword+" ("+initialocc+")");
                        }else{
                            System.out.println(anngram+" (FAIL) - "+initialword+" ("+initialocc+")");
                        }
                    }
                    if(concludingocc!=null){
                        if(tempocc!=null && concludingocc>tempocc/*>concludingword.getOccurances()*/){
                            votes++;
                            System.out.println(anngram+" ("+tempword+") - "+concludingword+" ("+concludingocc+")");
                        }else{
                            System.out.println(anngram+" (FAIL) - "+concludingword+" ("+concludingocc+")");
                        }
                    }

                    initialwordsuffix=ngramlist.get(0).substring(ngramlist.get(0).length()/charlength,ngramlist.get(0).length());
                    concludingwordprefix=ngramlist.get(ngramlist.size()-1).substring(0,ngramlist.get(ngramlist.size()-1).length()/charlength);
                    /*initialword=dicthandler.matchWord(initialwordsuffix);
                    concludingword=dicthandler.matchWord(concludingwordprefix);
                    middleword=dicthandler.matchWord(initialwordsuffix.substring(initialwordsuffix.length()/2)+concludingwordprefix.substring(0,concludingwordprefix.length()/2));
                    */
                    Double initialwordocc=dicthandler.matchNGramOccurance(initialwordsuffix),
                            concludingwordocc=dicthandler.matchNGramOccurance(concludingwordprefix),
                            middlewordocc=dicthandler.matchNGramOccurance(initialwordsuffix.substring(initialwordsuffix.length()/charlength)+concludingwordprefix.substring(0,concludingwordprefix.length()/2));
                    if(initialwordocc!=null){
                        if(middlewordocc!=null && initialwordocc>middlewordocc){
                            System.out.println(initialword+" > "+middleword+": "+(initialwordocc>middlewordocc));
                            votes++;
                        }
                    }
                    if(concludingwordocc!=null){
                        if(middlewordocc!=null && concludingwordocc>middlewordocc){
                            System.out.println(concludingwordocc+" > "+middlewordocc+": "+(concludingwordocc>middlewordocc));
                            votes++;
                        }
                    }
                    normalizedvote=votes/(2*(ngram.length()-1));
                    voteslist.add(normalizedvote);
                    votesum+=normalizedvote;
                }

                votepositions.add(votesum/line.length());

            }else{
                break;
            }

        }
        int lastboundary=0;
        String cuneiresult="",translitresult="";
        int k=0;
        this.words.clear();
        for(int j=1;j<votepositions.size()-1;j++){
            System.out.println("["+votepositions.get(j-1)+","+votepositions.get(j)+","+votepositions.get(j+1)+"] threshold: "+threshold/ngramlist.size());
            System.out.println(votepositions.get(j)+" > "+votepositions.get(j-1)+":"+(votepositions.get(j)>votepositions.get(j+1)));
            System.out.println(votepositions.get(j)+" > "+votepositions.get(j-1)+":"+(votepositions.get(j)>votepositions.get(j-1)));
            if((votepositions.get(j)>votepositions.get(j+1) && votepositions.get(j)>votepositions.get(j-1)) || votepositions.get(j)>threshold/ngramlist.size()){
                this.wordmatches++;
                this.wordboundaries.add(k);
                System.out.println("Found Word: "+line.substring(lastboundary,k));
                System.out.println(dicthandler.matchWord(line.substring(lastboundary,k)));
                lastboundary=k;
            }
            k+=charlength;
        }
        Set<Integer> boundaries=new TreeSet<Integer>(this.wordboundaries);
        //Collections.sort(this.wordboundaries);
        System.out.println("Currentline.length(): "+line.length());
        System.out.println("Word Boundaries: "+this.wordboundaries);

        int lastbound=0;
        for(Integer boundary:boundaries){
            if(boundary==0)
                continue;
            if(boundary<line.length()){
                cuneiresult+=line.substring(lastbound,boundary)+" ";
                this.words.add(line.substring(lastbound,boundary));
                lastbound=boundary;
            }
        }
        this.words.add(line.substring(lastbound));
        cuneiresult+=line.substring(lastbound);

        return new String[]{cuneiresult,assignTransliteration(this.words.toArray(new String[this.words.size()]),dicthandler,transliterationMethod,false)};
    }
}

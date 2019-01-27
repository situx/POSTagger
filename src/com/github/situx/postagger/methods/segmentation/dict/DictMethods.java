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

package com.github.situx.postagger.methods.segmentation.dict;

import com.github.situx.postagger.methods.segmentation.SegmentationMethods;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.PositionableChar;
import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.ClassificationMethod;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.util.Files;

import java.io.*;
import java.util.*;
/**
 * Class for parsing a text to segment using dictionary based approaches.
 * @author Timo Homburg
 *
 */
public class DictMethods extends SegmentationMethods implements DictMethodAPI {
    /**A set of candidates temporarily saved for performance reasons.*/
    private Set<String> candidates;
    /**Integer variables for statistical purposes.*/
    private Integer wordmatches=0,globalcharcounter=0,charmatches=0,notmatched=0,exactmatch=-1;
    /**
     * Constructor for DictMethods - Initializes the random generator.
     * @throws IOException on error
     */
	public DictMethods() {
        super();
        this.candidates=new TreeSet<>();

	}

    /**
     * Breakpoint Matching method.
     * @param filepath the path of the file to use
     * @param dicthandler the dicthandler to use
     * @throws IOException
     */
    public String breakPointMatching(final String filepath,final String destpath,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,final CharTypes chartype,final Boolean corpusstr,Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.BREAKPOINT.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.BREAKPOINT,transliterationMethod,chartype,corpusstr,printFiles);
        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.BREAKPOINT.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.BREAKPOINT,transliterationMethod,chartype,corpusstr,printFiles);
        }
        return this.cuneiBuffer.toString();
    }

    private String[] breakPointMatching(LangChar tempchar,final String currentline,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,CharTypes chartype) throws IOException {
        int charlength=chartype.getChar_length();
        StringBuilder cuneiBuilder=new StringBuilder();
        System.out.println("Currentstr: "+currentline);
        List<Double> charToSingleOccurance=new LinkedList<>();
        for(int i=0;i<=currentline.length()-charlength;i+=charlength){
           charToSingleOccurance.add(((((PositionableChar)dicthandler.matchChar(currentline.substring(i,i+charlength))).getSingleOccurance()+((PositionableChar)dicthandler.matchChar(currentline.substring(i,i+charlength))).getEndOccurance())/((PositionableChar)dicthandler.matchChar(currentline.substring(i,i+charlength))).getOccurances())*100);
        }
        System.out.println("charToSingleOccurance: "+charToSingleOccurance);
        List<String> result=new LinkedList<>();
        int currentpos=0,j=0;
        String temp;
        for(Double d:charToSingleOccurance){
           if(d>0.7){
              result.add(currentline.substring(currentpos,j));
              currentpos=j;
           }
           j++;
        }
        String[] cuneiwords=result.toArray(new String[result.size()]);
        for(String str:cuneiwords){
            cuneiBuilder.append(str);
            cuneiBuilder.append(" ");
            //this.cuneiResultWriter.write(str+" ");
        }
        System.out.print("Cuneiwords: ");
        //ArffHandler.arrayToStr(cuneiwords);
        String translit=assignTransliteration(cuneiwords, dicthandler, transliterationMethod,false);
        cuneiBuilder.append(System.lineSeparator());
        return new String[]{cuneiBuilder.toString(),translit};
        /*this.translitResultWriter.write(translit);
        this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(translit));
        transcriptResultWriter.write("\n");
        this.cuneiResultWriter.write("\n");
        this.translitResultWriter.write("\n");*/
    }

    private Double calculateFollowingSum(String currentline,List<Integer> posTochosenWords,Integer position,DictHandling dictHandling,CharTypes chartype){
        Integer positioncounter=0,charlength=chartype.getChar_length();
        String curword;
        Double result=0.;
        List<String> words;
        for(Integer word:posTochosenWords){
            curword=currentline.substring(positioncounter,positioncounter+word*charlength);
            if(dictHandling.matchWord(curword)!=null && positioncounter+word*charlength+charlength<currentline.length()-charlength) {
                if(dictHandling.matchWord(curword).getFollowingWords().containsKey(currentline.substring(positioncounter,positioncounter+word*charlength+charlength))){
                    result+=dictHandling.matchWord(curword).getFollowingWords().get(currentline.substring(positioncounter,positioncounter+word*charlength+charlength)).getFollowing().getOne();
                }
            }else if(dictHandling.matchChar(curword)!=null && positioncounter+word*charlength+charlength<currentline.length()-charlength){
                if(dictHandling.matchChar(curword).getFollowingWords().containsKey(currentline.substring(positioncounter,positioncounter+word*charlength+charlength))){
                    result+=dictHandling.matchChar(curword).getFollowingWords().get(currentline.substring(positioncounter,positioncounter+word*charlength+charlength)).getFollowing().getOne();
                }
            }
            positioncounter+=word*charlength;
        }
        return result;
    }

    private Double calculateMutualInformation(String word1,String word2,DictHandling dictHandler){
        Double x=0.,y=0.,following=0.;
        LangChar wor1,wor2;
        if((wor1=dictHandler.matchWord(word1))!=null){
            x=wor1.getOccurances();
            if(wor1.getFollowingWords().containsKey(word2)){
                following=wor1.getFollowingWords().get(word2).getFollowing().getOne()-wor1.getFollowingWords().get(word2).getFollowing().getTwo();
            }
        }
        if((wor2=dictHandler.matchWord(word2))!=null){
            x=wor2.getOccurances();
        }
        return Math.log(following/(x*y));
    }

    private Double calculateOccSum(String currentline,List<Integer> posTochosenWords,Integer position,DictHandling dictHandling,CharTypes chartype){
        Integer positioncounter=0,charlength=chartype.getChar_length();
        String curword;
        Double result=0.;
        List<String> words;
        for(Integer word:posTochosenWords){
            curword=currentline.substring(positioncounter,positioncounter+word*charlength);
            if(dictHandling.matchWord(curword)!=null) {
                result+=dictHandling.matchWord(curword).getOccurances();
            }else if(dictHandling.matchChar(curword)!=null){
                result+=dictHandling.matchChar(curword).getOccurances();
            }
            positioncounter+=word*charlength;
        }
        return result;
    }

    private String[] combinedMaxMatch(LangChar tempchar, final String currentline, final DictHandling dicthandler,final TransliterationMethod transliterationMethod,final CharTypes chartype) throws IOException {
         String left,right,temp;
         System.out.println("MAXMATCHCOMBINED!!!!!");
         int charlength=chartype.getChar_length();
         left=currentline.substring(0,currentline.length()/2);
         right=currentline.substring(currentline.length()/2,currentline.length());
         Set<LangChar> ngrams=new TreeSet<LangChar>();
        List<String> results=new LinkedList<>();
        StringBuilder res=new StringBuilder();
        for(int i=0;i<currentline.length()-charlength;i+=charlength){
            for(int j=i+charlength;j<=currentline.length();j+=charlength){
                temp=currentline.substring(i,j);
                //System.out.println("Tempchar: "+temp+" "+(tempchar=dicthandler.matchWord(temp)));
                if((tempchar=dicthandler.matchWord(temp))!=null || (tempchar=dicthandler.matchChar(temp))!=null){
                    ngrams.add(tempchar);
                }
            }
        }
        System.out.println("NGrams: "+ngrams.toString());
        temp=currentline;
        for(LangChar curchar:ngrams){
            if(temp.contains(curchar.getCharacter())){
                temp=temp.replace(curchar.getCharacter(),"");
                results.add(curchar.getCharacter());
            }
        }
        System.out.println("Result unsorted: "+results);
        Map<Integer,List<String>> resultmap=new TreeMap<>();
        for(int i=0;i<results.size();i++){
            System.out.println("Currentline.indexOf "+results.get(i)+" "+currentline.indexOf(results.get(i))+" "+currentline.contains(results.get(i)));
            if(!resultmap.containsKey(currentline.indexOf(results.get(i)))){
                resultmap.put(currentline.indexOf(results.get(i)),new LinkedList<String>());
            }
            resultmap.get(currentline.indexOf(results.get(i))).add(results.get(i));
        }
        System.out.println("Result sorted: "+resultmap.toString());
        for(List<String> list:resultmap.values()){
            for(String str:list){
                res.append(str);
                res.append(" ");
            }
        }
        System.out.println(res);
        if(res.length()==(currentline.length()+resultmap.values().size())){
            System.out.println("Final Result: "+res);
            //this.cuneiResultWriter.write(res);
            //if(!left)
            //    Collections.reverse(cuneiwords);
            //String translit=this.assignTransliteration(res.split(" "),dicthandler,transliterationMethod);
            //this.translitResultWriter.write(translit);
            //this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(translit));
            return new String[]{res.toString(),assignTransliteration(res.toString().split(" "), dicthandler, transliterationMethod,false)};
        }
        return new String[]{res.toString(),assignTransliteration(res.toString().split(" "),dicthandler,transliterationMethod,false)};

        /*this.cuneiResultWriter.write(res);
        //if(!left)
        //    Collections.reverse(cuneiwords);
        String translit=this.assignTransliteration(res.split(" "),dicthandler,transliterationMethod);
        this.translitResultWriter.write(translit);
        this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(translit));
        return new String[0];
        */ //this.maxMatchImproved(tempchar,left,dicthandler,true,transliterationMethod,chartype);
         //this.maxMatchImproved(tempchar,right,dicthandler,true,transliterationMethod,chartype);

    }

    public List<Integer> containsKeyinRange(Map<Integer,LangChar> check,Integer position,Integer range,LangChar newchar,Integer charlength){
        List<Integer> result=new LinkedList<>();
        for(int i=0;i<position+range;i++){
            if(check.containsKey(i)){
                System.out.println(newchar.toString()+" "+check.get(i)+" "+(check.get(i).length()/(charlength)+i)+">"+position);
            }
            if(check.containsKey(i) && (check.get(i).length()/(charlength)+i)>=position){
                if(newchar.getOccurances()<check.get(i).getOccurances()){
                    return null;
                }else{
                    result.add(i);
                }
            }
        }
        return result;
    }

    /**
     * Initializes parameters needed for parsing.
     * @param sourcepath the path of the sourcefile
     * @param destpath the path of the destination file
     * @param dicthandler the dicthandler to use
     * @param method the dictmethod to use
     * @throws IOException
     */
    public void initParsing(final String sourcepath,final String destpath,final DictHandling dicthandler,final ClassificationMethod method,final TransliterationMethod transliterationMethod,final CharTypes chartype,final Boolean corpusstr, Boolean printFiles) throws IOException {
        String currentline;
        LangChar tempchar=null;
        int currentposition;
        Set<String> stopwords=new HashSet<String>(chartype.getStopchars());
        List<String> segments=new LinkedList<>();
        this.words.clear();
        this.wordboundaries.clear();
        if(corpusstr){
            this.reader=new BufferedReader(new StringReader(sourcepath));
        }else{
            this.reader=new BufferedReader(new FileReader(new File(sourcepath)));
        }
        if(printFiles) {
            this.translitResultWriter = new BufferedWriter(new FileWriter(new File(Files.RESULTDIR.toString() + Files.TRANSLITDIR.toString() + destpath)));
            this.transcriptResultWriter = new BufferedWriter(new FileWriter(new File(Files.RESULTDIR.toString() + Files.TRANSCRIPTDIR.toString() + destpath)));
            this.cuneiResultWriter = new BufferedWriter(new FileWriter(new File(Files.RESULTDIR.toString() + Files.CUNEIFORMDIR.toString() + destpath)));
        }
            this.cuneiBuffer=new StringBuilder();
        /*if(method== ClassificationMethod.MAXMATCH2){
            this.text=this.reverseWholeFile(new File(sourcepath));
            this.reader=new BufferedReader(new StringReader(this.text));
        }*/

        while((currentline=this.reader.readLine())!=null){
            System.out.println("Currentline: "+currentline);
            segments.clear();
            currentposition=0;
            this.linecounter++;
            if(currentline.trim().isEmpty()) {
                if(printFiles){
                    this.transcriptResultWriter.write(System.lineSeparator());
                    this.cuneiResultWriter.write(System.lineSeparator());
                    this.translitResultWriter.write(System.lineSeparator());
                }
                    this.cuneiBuffer.append(System.lineSeparator());
                continue;
            }
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
                /*if(stopwords.contains(segment)){
                    this.translitResultWriter.write(segment+"\n");
                    this.transcriptResultWriter.write(segment+"\n");
                }*/
                switch (method) {
                    case BREAKPOINT:
                        result=this.breakPointMatching(tempchar, currentline, dicthandler, transliterationMethod, chartype);
                        break;
                    case LCUMATCHING:
                        result=this.lcuMatching(tempchar, currentline, dicthandler, 8, transliterationMethod, chartype);
                        break;
                    case MAXMATCHCOMBINED:
                        result=this.combinedMaxMatch(tempchar, currentline, dicthandler, transliterationMethod, chartype);
                        break;
                    case MINMATCH:
                        result=this.minMatch(tempchar, currentline, dicthandler, transliterationMethod, chartype, true);
                        break;
                    case MINWCMATCH:
                        result=this.minWCMatching(tempchar, currentline, dicthandler, transliterationMethod, chartype);
                        break;
                    case MINWCMATCH2:
                        result=this.minWCMatching2(tempchar, currentline, dicthandler, transliterationMethod, chartype);
                        break;
                    case MAXMATCH:
                        result=this.maxMatch(tempchar, currentline, dicthandler, true, transliterationMethod, chartype);
                        break;
                    case MAXMATCH2:
                        result=this.maxMatch(tempchar, currentline.length() > 0 && currentline.length() % 2 != 0 ? currentline.substring(1) : currentline, dicthandler, false, transliterationMethod, chartype);
                        break;
                    case MAXENT:
                        result=this.maxEntropyMatching(tempchar, currentline, dicthandler, 5, transliterationMethod, chartype);
                        break;
                    case POSMATCH:
                        result=this.minWCPOSMatching(tempchar,currentline,dicthandler,transliterationMethod,chartype);
                    default:
                }
                this.cuneiBuffer.append(result[0]);
                this.cuneiBuffer.append(System.lineSeparator());
                if(printFiles){
                    this.translitResultWriter.write(result[1]);
                    this.translitResultWriter.write(System.lineSeparator());
                        this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(result[1]));
                        this.cuneiResultWriter.write(result[0]);

                        this.transcriptResultWriter.write(System.lineSeparator());
                        this.cuneiResultWriter.write(System.lineSeparator());
                }
            }
        }
        //System.out.println(wordboundaries);
        //System.out.println(words);
        System.out.println("Char Matches: "+this.charmatches);
        System.out.println("Word Matches: "+this.wordmatches);
        System.out.println("Not Matched: "+this.notmatched);
        System.out.println("Global Chars: "+this.globalcharcounter);
        int sumwordboundaries=0;
        for(int j:wordboundaries){
            sumwordboundaries+=j;
        }
        System.out.println("Sum of Word Boundaries: "+sumwordboundaries);
        System.out.println("Char Matches %: "+((double)this.charmatches)/((double)this.globalcharcounter));
        System.out.println("Word Matches %: "+((double)this.wordmatches)/((double)this.globalcharcounter));
        System.out.println("Not Matched %: "+((double)this.notmatched)/((double)this.globalcharcounter));
        if(printFiles){
            this.translitResultWriter.close();
            this.transcriptResultWriter.close();
            this.cuneiResultWriter.close();
        }
        this.reader.close();
    }

    public String lcuMatching(final String filepath, final String destpath,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,CharTypes chartype,final Boolean corpusstr,final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.LCUMATCHING.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.LCUMATCHING,transliterationMethod,chartype,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.LCUMATCHING.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.LCUMATCHING,transliterationMethod,chartype,corpusstr,printFiles);

        }
        return this.cuneiBuffer.toString();
    }

    private String[] lcuMatching(final LangChar tempchar, final String currentline, final DictHandling dicthandler,final Integer ngramvalue,final TransliterationMethod transliterationMethod,final CharTypes chartype) throws IOException {
         int charlength=chartype.getChar_length();
         Map<Integer,List<LangChar>> posToWord=new HashMap<>();
         String temp;
         LangChar curchar;
         for(int i=0;i<currentline.length();i+=charlength){
              for(int j=i;j<=currentline.length();j+=charlength){
                  temp=currentline.substring(i,j);
                  if((curchar=dicthandler.matchWord(temp))!=null || (curchar=dicthandler.matchChar(temp))!=null){
                      if(!posToWord.containsKey(i/charlength)) {
                          posToWord.put(i/charlength,new LinkedList<LangChar>());
                      }
                      posToWord.get(i/charlength).add(curchar);
                  }
              }
          }
        System.out.println("LCU: " + posToWord.toString());
        Map<Integer,LangChar> fitsmap=new TreeMap<Integer,LangChar>();
        String result="";
        for(Integer key:posToWord.keySet()){
            for(LangChar lchar:posToWord.get(key)){
                if(lchar.length().equals(ngramvalue*charlength)){
                    List<Integer> keyrange=this.containsKeyinRange(fitsmap,key,ngramvalue,lchar,charlength);
                    if(keyrange!=null){
                        for(Integer k:keyrange){
                           fitsmap.remove(k);
                        }
                    }
                    if(!fitsmap.containsKey(key) && keyrange!=null){
                        System.out.println("Add to Fitsmap: ("+key+") "+lchar.toString());
                        fitsmap.put(key,lchar);
                        result+=lchar;
                    }
                }
            }
        }
        System.out.println("Fitsmap("+ngramvalue+"): "+fitsmap.toString());
        //for(key)
        for(int i=ngramvalue;i>1;i--){
            for(Integer key:posToWord.keySet()){
                for(LangChar lchar:posToWord.get(key)){
                    if(lchar.length()==i*charlength){
                        List<Integer> keyrange=this.containsKeyinRange(fitsmap,key,i,lchar,charlength);
                        if(!fitsmap.containsKey(key) && keyrange!=null && keyrange.isEmpty() ){
                            System.out.println("Add to Fitsmap: ("+key+") "+lchar.toString());
                            fitsmap.put(key,lchar);
                            result+=lchar;
                        }
                    }
                }
            }
            System.out.println("Fitsmap("+i+"): "+fitsmap.toString());
        }
        int currentposition=0;
        while(currentposition<(currentline.length()/charlength)){
            System.out.println("Currentposition: "+currentposition);
           if(!fitsmap.containsKey(currentposition)){
               if((currentposition*charlength)<(currentline.length()-charlength)){
                   System.out.println("Add Char: "+currentline.substring(currentposition*charlength,currentposition*charlength+charlength));
                   fitsmap.put(currentposition,dicthandler.matchChar(currentline.substring(currentposition*charlength,currentposition*charlength+charlength)));
               }
;
               currentposition++;
           } else{
               System.out.println("Use Word: "+fitsmap.get(currentposition)+"("+fitsmap.get(currentposition).length()/charlength+")");
               currentposition+=fitsmap.get(currentposition).length()/charlength;
           }
        }
        String[] cuneiwords=new String[fitsmap.keySet().size()];
        int i=0;
        for(Integer key:fitsmap.keySet()){
            LangChar lchar=fitsmap.get(key);
            if(lchar!=null){
                this.cuneiResultWriter.write(lchar.getCharacter()+" ");
                this.cuneiBuffer.append(lchar.getCharacter()+" ");
                cuneiwords[i++]=lchar.getCharacter();
            }else{
                cuneiwords[i++]="";
            }

        }
        System.out.println("Final Fitsmap: "+fitsmap.toString());
        System.out.println("Final Cuneiwords: "+cuneiwords.toString());
        String translit=assignTransliteration(cuneiwords, dicthandler, transliterationMethod,false);
        this.translitResultWriter.write(translit);
        this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(translit));



        //TODO: Kombination aus Scoringsystem für folgende Chars und Häufigkeit des Auftretens
         //for()
        return new String[]{"",""};
    }

    @Override
    public String maxEntropyMatching(final String filepath, final String destpath, final DictHandling dicthandler, final TransliterationMethod transliterationMethod, CharTypes chartype, final Boolean corpusstr, final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath,destpath,dicthandler, ClassificationMethod.MAXENT,transliterationMethod,chartype,corpusstr,printFiles);

        }else{
            this.initParsing(filepath,filepath,dicthandler, ClassificationMethod.MAXENT,transliterationMethod,chartype,corpusstr,printFiles);

        }
        return this.cuneiBuffer.toString();
       }

    /**
     * Performs maxEntropyMatching.
     * @param tempchar
     * @param line the current line to work with
     * @param dicthandler the dicthandler to use
     * @param ngramvalue the ngramvalue
     */
    private String[] maxEntropyMatching(final LangChar tempchar, final String line, final DictHandling dicthandler,final int ngramvalue,final TransliterationMethod transliterationMethod,final CharTypes chartype){
         String ngram;
         for(int i=0;i<line.length();i+=2){
             ngram=line.substring(i,i+=ngramvalue);

         }
        return new String[]{"",""};
    }

    @Override
    public String maxMatch(final String filepath, final String destpath, final DictHandling dicthandler, final boolean left, final TransliterationMethod transliterationMethod, final CharTypes chartype, final Boolean corpusstr, final Boolean printFiles) throws IOException{
         this.linecounter=0;
         this.charcounter=0;
         this.candidates.clear();
         if(left){
             if(corpusstr){
                 this.initParsing(filepath, destpath+"_"+ ClassificationMethod.MAXMATCH.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.MAXMATCH,transliterationMethod,chartype,corpusstr,printFiles);

             }else{
                 this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.MAXMATCH.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.MAXMATCH,transliterationMethod,chartype,corpusstr,printFiles);
             }
             }else{
                if(corpusstr){
                    this.initParsing(filepath, destpath+"_"+ ClassificationMethod.MAXMATCH2.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.MAXMATCH2,transliterationMethod,chartype,corpusstr,printFiles);

                }else{
                    this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.MAXMATCH2.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.MAXMATCH2,transliterationMethod,chartype,corpusstr,printFiles);

                }
             }
        return this.cuneiBuffer.toString();
    }

    /**
     * MaxMatch method parsing from left to right or right to left.
     * @param tempchar char for temporary usage
     * @param currentline the current line of text to analyse
     * @param dict the dicthandler to use
     * @param left indicates if we want to use to parse form left to right or right to left
     * @throws IOException on error
     */
	private String[] maxMatch(final LangChar tempchar, final String currentline, final DictHandling dict, final boolean left,final TransliterationMethod transliterationMethod,final CharTypes charType) throws IOException {
        return this.maxMatchImproved(tempchar, currentline, dict, left, transliterationMethod, charType);
    }

    public String maxMatchCombined(final String filepath,final String destpath, final DictHandling dicthandler, final TransliterationMethod transliterationMethod, final CharTypes chartype,final Boolean corpusstr,final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.MAXMATCHCOMBINED.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.MAXMATCHCOMBINED,transliterationMethod,chartype,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.MAXMATCHCOMBINED.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.MAXMATCHCOMBINED,transliterationMethod,chartype,corpusstr,printFiles);
        }
        return this.cuneiBuffer.toString();
        }

    private String[] maxMatchImproved(final LangChar tempchar,final String currentline, final DictHandling dicthandler,final boolean left, final TransliterationMethod transliterationMethod,final CharTypes chartype) throws IOException {
        String tempwordstr;
        int charlength=chartype.getChar_length();
        Boolean probOrFirst=true;
        System.out.println("Currentstr: "+currentline);
        Map<Integer,Map<Integer,Set<String>>> posToWord=new TreeMap<>();

        if(left){
            for(int i=0;i<currentline.length()/charlength;i++){
                posToWord.put(i,new TreeMap<Integer,Set<String>>());
                posToWord.get(i).put(1,new TreeSet<String>());
                posToWord.get(i).get(1).add(currentline.substring(i*charlength,(i*charlength)+charlength));
            }
            for(int i=0;i<currentline.length()-1;i+=charlength){
                for(int j=i+charlength;j<=currentline.length();j+=charlength){
                    tempwordstr= currentline.substring(i,j);
                    //System.out.println("Candidates: ("+tempwordstr+") "+dicthandler.getCandidatesForChar(tempwordstr));
                    if(dicthandler.matchWord(tempwordstr)!=null) {
                        //System.out.println("Add Word:" + tempwordstr.toString());

                        //System.out.println("i "+i/charlength+" stringarray.length "+currentline.length()/charlength);
                        if(!posToWord.get(i/charlength).containsKey(tempwordstr.length()/charlength)){
                            posToWord.get(i/charlength).put(tempwordstr.length()/charlength,new TreeSet<String>());
                        }
                        posToWord.get(i/charlength).get(tempwordstr.length()/charlength).add(tempwordstr);
                    }
                    if(dicthandler.matchChar(tempwordstr,chartype)!=null){
                        //System.out.println("Add Word:" + tempwordstr.toString());
                        //System.out.println("i "+i/charlength+" stringarray.length "+currentline.length()/charlength);
                        if(!posToWord.get(i/charlength).containsKey(tempwordstr.length()/charlength)){
                            posToWord.get(i/charlength).put(tempwordstr.length()/charlength,new TreeSet<String>());
                        }
                        posToWord.get(i/charlength).get(tempwordstr.length()/charlength).add(tempwordstr);
                    }
                }
            }
        }else{
            System.out.println("Stringlength: "+currentline.length()+" until: "+currentline.length()/charlength);
            for(int i=(currentline.length()/charlength);i>0;i--){
                posToWord.put(i-1,new TreeMap<Integer,Set<String>>());
                posToWord.get(i-1).put(1,new TreeSet<String>());
                System.out.println("From "+((i*charlength)-charlength)+" to "+(i*charlength));
                if(i==0)
                    posToWord.get(i-1).get(1).add(currentline.substring((i*charlength),i*charlength));
                else
                    posToWord.get(i-1).get(1).add(currentline.substring((i*charlength)-charlength,i*charlength));
            }
            System.out.println("PosToWord: "+posToWord.toString());
            for(int i=currentline.length();i>charlength;i-=charlength){
                System.out.println("i: "+i+" j: "+(i-charlength));
                for(int j=i-charlength;j>-1;j-=charlength){
                    tempwordstr= currentline.substring(j,i);

                    //System.out.println("Tempwordstr: "+tempwordstr+" "+((CuneiDictHandler)dicthandler).getReverseCandidatesForChar(tempwordstr));
                    //System.out.println("Candidates: ("+tempwordstr+") "+dicthandler.getCandidatesForChar(tempwordstr));
                    if(dicthandler.matchWord(tempwordstr)!=null) {
                        System.out.println("Add Word:" + tempwordstr+" j: "+j);
                        //System.out.println("i "+i/charlength+" stringarray.length "+currentline.length()/charlength);
                        if(!posToWord.get(j/charlength).containsKey(tempwordstr.length() / charlength)){
                            posToWord.get(j/charlength).put(tempwordstr.length() / charlength, new TreeSet<String>());
                        }
                        posToWord.get(j/charlength).get(tempwordstr.length()/charlength).add(tempwordstr);
                    }
                    if(dicthandler.matchChar(tempwordstr,chartype)!=null){
                        System.out.println("Add Word:" + tempwordstr);
                        //System.out.println("i "+i/charlength+" stringarray.length "+currentline.length()/charlength);
                        if(!posToWord.get(j/charlength).containsKey(tempwordstr.length() / charlength)){
                            posToWord.get(j/charlength).put(tempwordstr.length() / charlength, new TreeSet<String>());
                        }
                        posToWord.get(j/charlength).get(tempwordstr.length() / charlength).add(tempwordstr);
                    }
                }
            }
        }
        System.out.println("PosToWord " + posToWord);
        List<String> cuneiwords=new LinkedList<>();
            for (int i = 0; i < posToWord.size(); i++) {
                int maxlength = 0;
                for (Integer keyAtPosition : posToWord.get(i).keySet()) {
                    if (keyAtPosition > maxlength) {
                        maxlength = keyAtPosition;
                    }
                }
                System.out.println("Chose: " + posToWord.get(i).get(maxlength).iterator().next());
                if(left)
                    cuneiwords.add(posToWord.get(i).get(maxlength).iterator().next());
                else
                    cuneiwords.add(posToWord.get(i).get(maxlength).iterator().next());
                i += maxlength - 1;
            }
        StringBuilder cunei=new StringBuilder();
        for(String word:cuneiwords){
            cunei.append(word);
            cunei.append(" ");
        }
        //if(!left)
        //    Collections.reverse(cuneiwords);
        String translit=assignTransliteration(cuneiwords.toArray(new String[cuneiwords.size()]), dicthandler, transliterationMethod,false);
        return new String[]{cunei.toString().trim(),translit.trim()};
        //this.translitResultWriter.write(translit);
        //this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(translit));

    }

    /**
     * MinMatch method.
     * @param filepath the path of the file to use
     * @param dicthandler the dicthandler to use
     * @throws IOException
     */
    public String minMatch(final String filepath,final String destpath,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,CharTypes chartype,final Boolean corpusstr,final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.MINMATCH.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.MINMATCH,transliterationMethod,chartype,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.MINMATCH.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT.toString(),dicthandler, ClassificationMethod.MINMATCH,transliterationMethod,chartype,corpusstr,printFiles);

        }
        return this.cuneiBuffer.toString();
        }

    /**
     * MinMatch implementation.
     * @param tempchar
     * @param currentline the current line of text to analyse
     * @param dicthandler the dicthandler to use
     * @throws IOException
     */
    private String[] minMatch(LangChar tempchar,final String currentline,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,CharTypes chartype,final Boolean corpusstr) throws IOException {
        int begin=0,charlength=chartype.getChar_length();
        Transliteration translit;
        for(int i=0;i<currentline.length()-1;i+=charlength){
            tempchar=dicthandler.matchWord(currentline.substring(begin, i + charlength));
            if(dicthandler.getCandidatesForChar(currentline.substring(begin, i + charlength)).isEmpty() && begin-i==0){
                this.translitResultWriter.write("["+currentline.substring(begin,i+charlength)+"] ");
                this.cuneiResultWriter.write(currentline.substring(begin,i+charlength)+" ");
                this.cuneiBuffer.append(currentline.substring(begin,i+charlength)+" ");
                this.words.add(currentline.substring(begin, i + charlength));
                this.wordboundaries.add(1);
            }
            System.out.println(currentline.substring(begin, i + charlength));
            System.out.println(tempchar);
            if(tempchar!=null && !tempchar.getTransliterationSet().isEmpty()){
                translit=tempchar.getTransliterationSet().iterator().next();
                this.translitResultWriter.write("["+translit+"] ");
                this.cuneiResultWriter.write(" "+translit+" ");
                this.cuneiBuffer.append(" "+translit+" ");
                this.words.add(translit.toString());
                this.wordboundaries.add(tempchar.length());
                begin=i;
            }
        }
        return new String[]{"",""};
    }

    /**
     * MinMatch method.
     *
     * @param filepath the path of the file to use
     * @param dicthandler the dicthandler to use
     * @param chartype
     * @throws IOException
     */
    @Override
    public String minWCMatching(final String filepath, final String destpath, final DictHandling dicthandler, final TransliterationMethod transliterationMethod, final CharTypes chartype, final Boolean corpusstr, final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath, destpath+"_"+ ClassificationMethod.MINWCMATCH.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.MINWCMATCH,transliterationMethod,chartype,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.MINWCMATCH.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.MINWCMATCH,transliterationMethod,chartype,corpusstr,printFiles);
        }
        return this.cuneiBuffer.toString();
        }

    private String[] minWCMatching(LangChar tempword,final String currentline,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,CharTypes chartype) throws IOException{
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File("minwclog.log")));
        String tempwordstr;
        int charlength=chartype.getChar_length();
        Boolean probOrFirst=true;
        System.out.println("Currentstr: "+currentline);
        writer.write("Currentstr: "+currentline+System.lineSeparator());
        Map<Integer,Map<Integer,Set<String>>> posToWord=new TreeMap<>();
        Map<Integer,List<Integer>> posToChosenWords=new TreeMap<>();
        Integer[] posToAmount=new Integer[currentline.length()/charlength];
        for(int i=0;i<currentline.length()/charlength;i++){
            posToAmount[i]=(currentline.length()/charlength)-i;
            posToChosenWords.put(i,new LinkedList<Integer>());
            for(int j=0;j<(currentline.length()/charlength)-i;j++){
                posToChosenWords.get(i).add(1);
            }
            posToWord.put(i,new TreeMap<Integer,Set<String>>());
            posToWord.get(i).put(1,new TreeSet<String>());
            posToWord.get(i).get(1).add(currentline.substring(i*charlength,(i*charlength)+charlength));
        }
        for(int i=0;i<currentline.length()-1;i+=charlength){
            for(int j=i+charlength;j<=currentline.length();j+=charlength){
                tempwordstr= currentline.substring(i,j);
                System.out.println("Candidates: ("+tempwordstr+") "+dicthandler.getCandidatesForChar(tempwordstr));
                writer.write("Candidates: ("+tempwordstr+") "+dicthandler.getCandidatesForChar(tempwordstr)+System.lineSeparator());
                if(dicthandler.matchWord(tempwordstr)!=null || dicthandler.matchChar(tempwordstr)!=null) {
                    System.out.println("Add Word:" + tempwordstr.toString());
                    writer.write("Add Word:" + tempwordstr.toString());
                    System.out.println("i "+i/charlength+" stringarray.length "+currentline.length()/charlength);
                    writer.write("i "+i/charlength+" stringarray.length "+currentline.length()/charlength+System.lineSeparator());
                    writer.write(posToWord.toString()+System.lineSeparator());
                    if(!posToWord.get(i/charlength).containsKey(tempwordstr.length()/charlength)){
                        posToWord.get(i/charlength).put(tempwordstr.length()/charlength,new TreeSet<String>());
                    }
                    posToWord.get(i/charlength).get(tempwordstr.length()/charlength).add(tempwordstr);
                }
            }
        }
        System.out.println("PosToWord: "+posToWord);
        writer.write("PosToWord: "+posToWord+System.lineSeparator());
        int index;
        for(int i=currentline.length();i>-1;i-=charlength){
            //probs[i-(i/2)]=0.;
            index=charlength>1?i-((i/charlength)+1):i;
            int remembercurpos=index,amount=0;
            List<Integer> currentlychosenwords=new LinkedList<>();
            System.out.println("posToWord.containsKey("+index+") "+posToWord.containsKey(index)+" i: "+i);
            writer.write("posToWord.containsKey("+index+") "+posToWord.containsKey(index)+" i: "+i+System.lineSeparator());
            if(posToWord.containsKey(index)) {
                //System.out.println("posToWord.get("+(index)+").keySet()"+posToWord.get(index).keySet());
                for (Integer keyAtPosition : posToWord.get(index).keySet()) {
                    amount=0;
                    remembercurpos=index+keyAtPosition;
                    amount++;
                    currentlychosenwords.add(keyAtPosition);
                    System.out.println("Remembercurpos: " + remembercurpos + " Currentline.length()/"+charlength+" "+ ((currentline.length()/charlength)-1));
                    writer.write("Remembercurpos: " + remembercurpos + " Currentline.length()/"+charlength+" "+ ((currentline.length()/charlength)-1)+System.lineSeparator());
                    if (remembercurpos == ((currentline.length()/charlength))) {
                        if ((posToAmount[index] > amount && !posToChosenWords.get(index).isEmpty()) || posToChosenWords.get(index).isEmpty()) {
                            posToAmount[index] = amount;
                            posToChosenWords.put(index, currentlychosenwords);
                        }
                    } else if (remembercurpos < ((currentline.length()/charlength)-1)) {
                        if (posToAmount[remembercurpos] != Integer.MAX_VALUE) {
                            currentlychosenwords.addAll(posToChosenWords.get(remembercurpos));
                            amount+=posToAmount[remembercurpos];
                            if(posToAmount[index]>amount){
                                posToAmount[index]=amount;
                                posToChosenWords.put(index,currentlychosenwords);
                            }

                            //System.out.println(posToChosenWords.get(remembercurpos));
                        }else{
                            if(posToAmount[index]>amount){
                                posToAmount[index]=amount;
                                posToChosenWords.put(index,currentlychosenwords);
                            }
                        }
                    }
                    writer.write("PosToAmount [");
                    System.out.print("PosToAmount [");
                    for(Integer inter:posToAmount){
                        System.out.print(inter+",");
                        writer.write(inter+",");
                    }
                    System.out.println("]");
                    writer.write("]"+System.lineSeparator());
                    System.out.println("PosToWord " + posToWord);
                    writer.write("PosToWord " + posToWord+System.lineSeparator());
                    System.out.println("PosToChosenWord " + posToChosenWords);
                    writer.write("PosToChosenWord " + posToChosenWords+System.lineSeparator());
                    currentlychosenwords=new LinkedList<>();
                }
            }
        }

        int positioncounter=0,cuneiwordcounter=0;
        String cuneiword;
        String[] cuneiwords=new String[posToChosenWords.get(0).size()];
        StringBuilder cunei=new StringBuilder();
        for(Integer chosen:posToChosenWords.get(0)) {
            System.out.println("Positioncounter: "+positioncounter+" Chosen*2 "+chosen*charlength+" Currentline.length() "+currentline.length());
            writer.write("Positioncounter: "+positioncounter+" Chosen*2 "+chosen*charlength+" Currentline.length() "+currentline.length()+System.lineSeparator());
            cuneiword=currentline.substring(positioncounter,positioncounter+chosen*charlength).replace("[","").replace("]","");
            cunei.append(cuneiword);
            cunei.append(" ");
            cuneiwords[cuneiwordcounter++]=cuneiword;
            positioncounter+=chosen*charlength;
        }
        System.out.print("Cuneiwords: ");
        //ArffHandler.arrayToStr(cuneiwords);
        writer.close();
        String translit=assignTransliteration(cuneiwords, dicthandler, transliterationMethod,false);
        return new String[]{cunei.toString().trim(),translit.trim()};
        //this.translitResultWriter.write(translit);
        //this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(translit));
    }


    public String minWCPOSMatching(final String filepath, final String destpath, final DictHandling dicthandler, final TransliterationMethod transliterationMethod, final CharTypes chartype, final Boolean corpusstr, final Boolean printFiles) throws IOException {
        this.initParsing(filepath,destpath,dicthandler,ClassificationMethod.POSMATCH,transliterationMethod,chartype,corpusstr,printFiles);
        return this.cuneiBuffer.toString();
    }

    private String[] minWCPOSMatching(LangChar tempword,final String currentline,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,CharTypes chartype) throws IOException{
        String tempwordstr;
        POSTagger postagger=chartype.getCorpusHandlerAPI().getPOSTagger(false);
        int charlength=chartype.getChar_length();
        Boolean probOrFirst=true;
        System.out.println("Currentstr: "+currentline);
        Map<Integer,Map<Integer,Map<String,List<POSDefinition>>>> posToWord=new TreeMap<>();
        Map<Integer,List<Integer>> posToChosenWords=new TreeMap<>();
        Integer[] posToAmount=new Integer[currentline.length()/charlength];
        for(int i=0;i<currentline.length()/charlength;i++){
            posToAmount[i]=(currentline.length()/charlength)-i;
            posToChosenWords.put(i,new LinkedList<Integer>());
            for(int j=0;j<(currentline.length()/charlength)-i;j++){
                posToChosenWords.get(i).add(1);
            }
            posToWord.put(i, new TreeMap<>());
            posToWord.get(i).put(1,new TreeMap<String,List<POSDefinition>>());
            posToWord.get(i).get(1).put(currentline.substring(i * charlength, (i * charlength) + charlength), new LinkedList<POSDefinition>());
        }
        for(int i=0;i<currentline.length()-1;i+=charlength){
            for(int j=i+charlength;j<=currentline.length();j+=charlength){
                tempwordstr= currentline.substring(i,j);
                //System.out.println("Candidates: ("+tempwordstr+") "+dicthandler.getCandidatesForChar(tempwordstr));
                List<POSDefinition> posdefs=postagger.getPosTagDefs(tempwordstr, dicthandler);
                if(!posdefs.isEmpty()){
                    System.out.println("Add Word:" + tempwordstr);

                    //System.out.println("i "+i/charlength+" stringarray.length "+currentline.length()/charlength);
                    if(!posToWord.get(i/charlength).containsKey(tempwordstr.length()/charlength)){
                        posToWord.get(i/charlength).put(tempwordstr.length()/charlength,new TreeMap<String,List<POSDefinition>>());
                    }
                    posToWord.get(i/charlength).get(tempwordstr.length()/charlength).put(tempwordstr, posdefs);
                }
            }
            posToWord.put(i/charlength,postagger.getMostRelevantPOSTag(posToWord.get(i/charlength)));
        }
        System.out.println("PosToWord: "+posToWord);
        int index;
        for(int i=currentline.length();i>-1;i-=charlength){
            //probs[i-(i/2)]=0.;
            index=charlength>1?i-((i/charlength)+1):i;
            int remembercurpos,amount;
            List<Integer> currentlychosenwords=new LinkedList<>();
            System.out.println("posToWord.containsKey("+index+") "+posToWord.containsKey(index)+" i: "+i);
            if(posToWord.containsKey(index)) {
                //System.out.println("posToWord.get("+(index)+").keySet()"+posToWord.get(index).keySet());
                for (Integer keyAtPosition : posToWord.get(index).keySet()) {
                    amount=0;
                    remembercurpos=index+keyAtPosition;
                    amount++;
                    currentlychosenwords.add(keyAtPosition);
                    //System.out.println("Remembercurpos: " + remembercurpos + " Currentline.length()/"+charlength+" "+ ((currentline.length()/charlength)-1));
                    if (remembercurpos == ((currentline.length()/charlength))) {
                        if ((posToAmount[index] > amount && !posToChosenWords.get(index).isEmpty()) || posToChosenWords.get(index).isEmpty()) {
                            posToAmount[index] = amount;
                            posToChosenWords.put(index, currentlychosenwords);
                        }
                    } else if (remembercurpos < ((currentline.length()/charlength)-1)) {
                        if (posToAmount[remembercurpos] != Integer.MAX_VALUE) {
                            currentlychosenwords.addAll(posToChosenWords.get(remembercurpos));
                            amount+=posToAmount[remembercurpos];
                            if(posToAmount[index]>amount){
                                posToAmount[index]=amount;
                                posToChosenWords.put(index,currentlychosenwords);
                            }

                            //System.out.println(posToChosenWords.get(remembercurpos));
                        }else{
                            if(posToAmount[index]>amount){
                                posToAmount[index]=amount;
                                posToChosenWords.put(index,currentlychosenwords);
                            }
                        }
                    }
                    System.out.print("PosToAmount [");
                    for(Integer inter:posToAmount){
                        System.out.print(inter+",");
                    }
                    System.out.println("]");
                    System.out.println("PosToWord " + posToWord);
                    System.out.println("PosToChosenWord " + posToChosenWords);
                    currentlychosenwords=new LinkedList<>();
                }
            }
        }

        int positioncounter=0,cuneiwordcounter=0;
        String cuneiword;
        String[] cuneiwords=new String[posToChosenWords.get(0).size()];
        StringBuilder cunei=new StringBuilder();
        for(Integer chosen:posToChosenWords.get(0)) {
            //System.out.println("Positioncounter: "+positioncounter+" Chosen*2 "+chosen*charlength+" Currentline.length() "+currentline.length());
            cuneiword=currentline.substring(positioncounter,positioncounter+chosen*charlength).replace("[","").replace("]","");
            cunei.append(cuneiword);
            cunei.append(" ");
            cuneiwords[cuneiwordcounter++]=cuneiword;
            positioncounter+=chosen*charlength;
        }
        System.out.print("Cuneiwords: ");
        //ArffHandler.arrayToStr(cuneiwords);
        String translit=assignTransliteration(cuneiwords, dicthandler, transliterationMethod,false);
        return new String[]{cunei.toString().trim(),translit.trim()};
        //this.translitResultWriter.write(translit);
        //this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(translit));
    }

    public void minWCMatching2(final String filepath,final String destpath, final DictHandling dicthandler, final TransliterationMethod transliterationMethod, final CharTypes chartype,final Boolean corpusstr,final Boolean printFiles) throws IOException {
        if(corpusstr){
            this.initParsing(filepath,destpath+"_"+ ClassificationMethod.MINWCMATCH2.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.MINWCMATCH2,transliterationMethod,chartype,corpusstr,printFiles);

        }else{
            this.initParsing(filepath, filepath.substring(filepath.lastIndexOf("/")+1,filepath.lastIndexOf('.'))+"_"+ ClassificationMethod.MINWCMATCH2.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+ Files.RESULT.toString(),dicthandler, ClassificationMethod.MINWCMATCH2,transliterationMethod,chartype,corpusstr,printFiles);

        }
        }

    private String[] minWCMatching2(LangChar tempword,final String currentline,final DictHandling dicthandler,final TransliterationMethod transliterationMethod,CharTypes chartype) throws IOException{
        String tempwordstr;
        int charlength=chartype.getChar_length();
        Boolean probOrFirst=true;
        System.out.println("Currentstr: "+currentline);
        Map<Integer,Map<Integer,Set<String>>> posToWord=new TreeMap<>();
        Map<Integer,List<Integer>> posToChosenWords=new TreeMap<>();
        Integer[] posToAmount=new Integer[currentline.length()/charlength];
        for(int i=0;i<currentline.length()/charlength;i++){
            posToAmount[i]=(currentline.length()/charlength)-i;
            posToChosenWords.put(i,new LinkedList<Integer>());
            for(int j=0;j<(currentline.length()/charlength)-i;j++){
                posToChosenWords.get(i).add(1);
            }
            posToWord.put(i,new TreeMap<Integer,Set<String>>());
            posToWord.get(i).put(1,new TreeSet<String>());
            posToWord.get(i).get(1).add(currentline.substring(i*charlength,(i*charlength)+charlength));
        }
        for(int i=0;i<currentline.length()-1;i+=charlength){
            for(int j=i+charlength;j<=currentline.length();j+=charlength){
                tempwordstr= currentline.substring(i,j);
                //System.out.println("Candidates: ("+tempwordstr+") "+dicthandler.getCandidatesForChar(tempwordstr));
                if(dicthandler.matchWord(tempwordstr)!=null || dicthandler.matchChar(tempwordstr)!=null) {
                    //System.out.println("Add Word:" + tempwordstr.toString());

                    //System.out.println("i "+i/charlength+" stringarray.length "+currentline.length()/charlength);
                    if(!posToWord.get(i/charlength).containsKey(tempwordstr.length()/charlength)){
                        posToWord.get(i/charlength).put(tempwordstr.length()/charlength,new TreeSet<String>());
                    }
                    posToWord.get(i/charlength).get(tempwordstr.length()/charlength).add(tempwordstr);
                }
            }
        }
        //System.out.println("PosToWord: "+posToWord);
        int index;
        for(int i=currentline.length();i>-1;i-=charlength){
            //probs[i-(i/2)]=0.;
            index=i-((i/charlength)+1);
            int remembercurpos=index,amount=0;
            List<Integer> currentlychosenwords=new LinkedList<>();
            //System.out.println("posToWord.containsKey("+index+") "+posToWord.containsKey(index)+" i: "+i);
            if(posToWord.containsKey(index)) {
                //System.out.println("posToWord.get("+(index)+").keySet()"+posToWord.get(index).keySet());
                for (Integer keyAtPosition : posToWord.get(index).keySet()) {
                    amount=0;
                    remembercurpos=index+keyAtPosition;
                    amount++;
                    currentlychosenwords.add(keyAtPosition);
                    //System.out.println("Remembercurpos: " + remembercurpos + " Currentline.length()/"+charlength+" "+ ((currentline.length()/charlength)-1));
                    if (remembercurpos == ((currentline.length()/charlength))) {
                        if ((posToAmount[index] > amount && !posToChosenWords.get(index).isEmpty()) || posToChosenWords.get(index).isEmpty()) {
                            posToAmount[index] = amount;
                            posToChosenWords.put(index, currentlychosenwords);
                        }
                    } else if (remembercurpos < ((currentline.length()/charlength)-1)) {
                        if (posToAmount[remembercurpos] != Integer.MAX_VALUE
                                && (calculateFollowingSum(currentline,posToChosenWords.get(index),index,dicthandler,chartype)
                                -calculateOccSum(currentline,posToChosenWords.get(index),index,dicthandler,chartype))<
                                calculateFollowingSum(currentline,currentlychosenwords,index,dicthandler,chartype)
                                        -calculateOccSum(currentline,currentlychosenwords,index,dicthandler,chartype)) {
                            currentlychosenwords.addAll(posToChosenWords.get(remembercurpos));
                            amount+=posToAmount[remembercurpos];
                            if(posToAmount[index]>amount){
                                posToAmount[index]=amount;
                                posToChosenWords.put(index,currentlychosenwords);
                            }

                            System.out.println(posToChosenWords.get(remembercurpos));
                        }else{
                            if(posToAmount[index]>amount
                                    && (calculateFollowingSum(currentline,posToChosenWords.get(index),index,dicthandler,chartype)-calculateOccSum(currentline,posToChosenWords.get(index),index,dicthandler,chartype))<
                                    calculateFollowingSum(currentline,currentlychosenwords,index,dicthandler,chartype)-calculateOccSum(currentline,currentlychosenwords,index,dicthandler,chartype)){
                                posToAmount[index]=amount;
                                posToChosenWords.put(index,currentlychosenwords);
                            }
                        }
                    }
                    System.out.print("PosToAmount [");
                    for(Integer inter:posToAmount){
                        System.out.print(inter+",");
                    }
                    System.out.println("]");
                    System.out.println("PosToWord " + posToWord);
                    System.out.println("PosToChosenWord " + posToChosenWords);
                    currentlychosenwords=new LinkedList<>();
                }
            }
        }

        int positioncounter=0,cuneiwordcounter=0;
        String cuneiword;
        List<String> cuneiwords=new LinkedList<>();
        StringBuilder cuneiresult=new StringBuilder();
        for(positioncounter=0;positioncounter<currentline.length()/charlength;){
            for(Integer chosen:posToChosenWords.get(positioncounter/charlength)) {
                if(positioncounter+chosen*charlength>(currentline.length()+cuneiwords.size())){
                    break;
                }
                //System.out.println("Positioncounter: "+positioncounter+" Chosen*2 "+chosen*charlength+" Currentline.length() "+currentline.length());
                cuneiword=currentline.substring(positioncounter,positioncounter+chosen*charlength).replace("[","").replace("]","");
                cuneiresult.append(cuneiword);
                cuneiresult.append(" ");
                //this.cuneiResultWriter.write(cuneiword+" ");
                cuneiwords.add(cuneiword);
                positioncounter+=chosen*charlength;
            }
        }

        System.out.print("Cuneiwords: "+cuneiwords.toString());
        String translit=assignTransliteration(cuneiwords.toArray(new String[cuneiwords.size()]), dicthandler, transliterationMethod,false);
        //this.translitResultWriter.write(translit);
        //this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(translit));
        return new String[]{cuneiresult.toString(),translit};
    }

    public String[] morfessorSegmenting(final String sourcepath,final String testpath,final CuneiDictHandler dictHandler,boolean modelexists,final TransliterationMethod transliterationMethod,final CharTypes chartype) throws IOException, InterruptedException {
        dictHandler.morfessorExport(sourcepath.substring(0,sourcepath.lastIndexOf('.'))+Files.MORFESSORSUFFIX);
        File subfiletrainpath=new File(sourcepath.substring(0, sourcepath.lastIndexOf('.'))+Files.MORFESSORSUFFIX);
        File subfilepath=new File(sourcepath.substring(sourcepath.lastIndexOf('/')+1,sourcepath.lastIndexOf('.')));
        File modelpath=new File(Files.TRAININGDATADIR.toString()+Files.MODELDIR.toString()+subfilepath+".model");
        File testpathfile=new File(testpath);
        File cuneiformresultpath=new File(Files.RESULTDIR.toString()+Files.CUNEIFORMDIR.toString()+testpath.substring(testpath.lastIndexOf('/')+1,testpath.lastIndexOf('.'))+"_"+ClassificationMethod.MORFESSOR.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT);
        File translitresultpath=new File(Files.RESULTDIR.toString()+Files.TRANSLITDIR.toString()+subfilepath+"_"+ClassificationMethod.MORFESSOR.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT);
        File transcriptresultpath=new File(Files.RESULTDIR.toString()+Files.TRANSCRIPTDIR.toString()+subfilepath+"_"+ ClassificationMethod.MORFESSOR.toString().toLowerCase()+"_"+transliterationMethod.getShortlabel().toLowerCase()+Files.RESULT);

        if(modelexists){
            System.out.println("/home/timo/workspace2/Master/morfessor-2.0.0/scripts/morfessor --traindata-list -l "+modelpath.getAbsolutePath()+" -T "+testpathfile.getAbsolutePath()+" > "+cuneiformresultpath.getAbsolutePath());
            String[] cmd = { "/bin/sh", "-c", "/home/timo/workspace2/Master/morfessor-2.0.0/scripts/morfessor--traindata-list -l " + modelpath.getAbsolutePath() + " -T " + testpathfile.getAbsolutePath() + " > " + cuneiformresultpath.getAbsolutePath() };
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
        }else{
            System.out.println("/home/timo/workspace2/Master/morfessor-2.0.0/scripts/morfessor --traindata-list -t "+subfiletrainpath.getAbsolutePath()+" -s "+modelpath.getAbsolutePath()+" -T "+testpath+" > "+cuneiformresultpath.getAbsolutePath());
            //"/home/timo/workspace2/Master/morfessor-2.0.0/scripts/morfessor--traindata-list -l " + modelpath.getAbsolutePath() + " -T " + testpathfile.getAbsolutePath() + " > " + cuneiformresultpath.getAbsolutePath()
            String[] cmd = { "/bin/sh", "-c","/home/timo/workspace2/Master/morfessor-2.0.0/scripts/morfessor --traindata-list -t " + subfiletrainpath.getAbsolutePath() + " -s " + modelpath.getAbsolutePath() + " -T " + testpath + " > " + cuneiformresultpath.getAbsolutePath()};
            Process process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
        }
        /*Runtime r = Runtime.getRuntime();
        System.out.println("morfessor --traindata-list -L "+subfiletrainpath.getAbsolutePath()+" -s "+modelpath.getAbsolutePath());
        Process p = r.methods("morfessor --traindata-list -L "+subfiletrainpath.getAbsolutePath()+" -s "+modelpath.getAbsolutePath());
        p.waitFor();
        System.out.println("morfessor-segment -l "+modelpath.getAbsolutePath()+" "+testpath);
        Process p2 = r.methods("morfessor-segment -l "+modelpath.getAbsolutePath()+" "+testpath);
        p2.waitFor();*/

        this.reader = new BufferedReader(new FileReader(cuneiformresultpath.getAbsolutePath()));
        this.translitResultWriter=new BufferedWriter(new FileWriter(translitresultpath));
        this.cuneiResultWriter=new BufferedWriter(new FileWriter(cuneiformresultpath));
        this.transcriptResultWriter=new BufferedWriter(new FileWriter(transcriptresultpath));
        String temp,translit;
        String[] temparray;
        List<String> cuneiwords=new LinkedList<>();
        CuneiChar matchword;

        while((temp=this.reader.readLine())!=null){
            cuneiwords.clear();
            temparray=temp.split(" ");
            for(String word:temparray){
                cuneiwords.add(word);
               /*if((matchword=(CuneiChar)dictHandler.matchWord(word))!=null){

                   this.translitResultWriter.write("[" + matchword.getTransliterationSet().iterator().next().toString() + "] ");
               }else{
                   this.translitResultWriter.write(dictHandler.getNoDictTransliteration(word, transliterationMethod));
               } */

            }
            for(String cunei:cuneiwords){
                this.cuneiResultWriter.write(cunei+" ");
            }
            this.cuneiResultWriter.write(System.lineSeparator());
            translit=assignTransliteration(cuneiwords.toArray(new String[cuneiwords.size()]),dictHandler,transliterationMethod,false);
            this.transcriptResultWriter.write(TranscriptionMethods.translitTotranscript(translit)+System.lineSeparator());
            this.translitResultWriter.write(translit+System.lineSeparator());
        }
        this.translitResultWriter.close();
        this.cuneiResultWriter.close();
        this.translitResultWriter.close();
        this.transcriptResultWriter.close();
        return new String[]{"",""};

    }

    public void posInductionSegmenting(final String trainpath, final String testpath){

    }


}
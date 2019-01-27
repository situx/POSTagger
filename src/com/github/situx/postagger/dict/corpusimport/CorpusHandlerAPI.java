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

package com.github.situx.postagger.dict.corpusimport;

import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TestMethod;
import com.github.situx.postagger.util.enums.util.Files;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 06.11.13
 * Time: 14:21
 * Handles corpora and produces DictHandlers.
 */
public abstract class CorpusHandlerAPI extends DefaultHandler2 {
    /**The list of stopchars to consider.*/
    protected final List<String> stopchars;
    /**The writer for segmented cuneiform.*/
    public BufferedWriter reformattedCuneiWriter;
    /**The writer for unsegmented cuneiform.*/
    public BufferedWriter reformattedUSegCuneiWriter;
    /**Reader for the corpusimport file.*/
    protected BufferedReader corpusReader;
    /**Array for containing testcrossfolds.*/
    protected String[] crossfold;
    /**Array for containing trainingcrossfolds.*/
    protected String[] crossfoldTrain;
    /**Writes the corpusimport in cuneiform including the correct segmentation.*/
    protected BufferedWriter cuneiSegmentExport;
    /**Writes the corpusimport in cuneiform.*/
    protected BufferedWriter cuneiWOSegmentExport;
    /**Map from testingmethod to dicthandlers.*/
    protected Map<TestMethod,List<DictHandling>> dictHandlers;
    /**Counts matches and mismatches of characters.*/
    protected Double matches=0.,nomatches=0.;
    /**Map of mismatches and their frequency.*/
    protected Map<String,Integer> nomatchesmap=new TreeMap<>();
    /**Writes testing data using word boundaries in a separate file.*/
    protected BufferedWriter normalizedTestDataWriter;
    /**The number of lines to process.*/
    protected Integer numberOfLines;
    /**Randomgenerator to generate a randomized selection of trainingset and testset.*/
    protected Random randomGenerator;
    /**FileWriter for writing the boundary file of this corpusimport.*/
    protected BufferedWriter reformattedBoundaryWriter;
    /**Writes a normalized transliterated version of the corpusimport.*/
    protected BufferedWriter reformattedTranslitWriter;
    /**The current test set file.*/
    protected String testFile;
    /**The current trainset,testset as String.*/
    protected String testSet,trainSet;
    /**Counter for words and chars.*/
    protected Double wordcounter,charcounter;
    /**Path for the testset.*/
    private String testSetPath;

    /**
     * Constructor for this class.
     * @param stopchars the stopchars to consider
     */
    public CorpusHandlerAPI(final List<String> stopchars){
        this.randomGenerator=new Random(System.currentTimeMillis());
        this.dictHandlers=new TreeMap<>();
        this.stopchars=stopchars;
    }

    /**
     * Appends translations to the given dicthandler.
     * @param file translation file
     * @param testMethod1 the testmethod to choose
     * @throws ParserConfigurationException on error
     * @throws SAXException on error
     * @throws IOException on error
     */
    public abstract void addTranslations(final String file, final TestMethod testMethod1) throws ParserConfigurationException, SAXException, IOException;

    /**
     * Converts characters to a boundary representation.
     * @param chars the characters to consider
     * @param chartype the chartype for conversion
     * @return the boundary representation as String
     */
    public String charsToBoundaries(final String chars,final CharTypes chartype){
        StringBuilder result=new StringBuilder();
        String tempchar="";
        Set<String> stopchars=new HashSet(Arrays.asList(chartype.getStopchars()));
        for(int i=0;i<=chars.length()-chartype.getChar_length();i+=chartype.getChar_length()){
            tempchar=chars.substring(i,i+chartype.getChar_length());
            if(!tempchar.equals(" ") && !stopchars.contains(tempchar) && i<chars.length()-chartype.getChar_length()){
                result.append("0,");
            }else{
                result.append("1,");
            }
        }
        return result.toString();
    }

    /**
     * Checks for an already created dicthandler to reuse.
     * @param corpus the corpus to check
     * @param testMethod the testmethod to use
     * @return the existence of a dicthandler as true or false
     */
    private Boolean checkForDict(final String corpus,TestMethod testMethod){
        File file= new File(Files.DICTDIR.toString());
        for(String fil:file.list()){
            if(!new File(fil).isDirectory() && corpus.contains(".") && fil.equals(corpus.substring(0,corpus.lastIndexOf("."))+"_"+testMethod+Files.DICTSUFFIX)){
                return true;
            }
        }
        return false;
    }

    /**
     * Cleaning a String from unnecessary characters.
     * @param word the current word or part of a word
     * @param reformat Indicates if it is a part of a word or a whole word
     * @return the modified word
     */
    public abstract String cleanWordString(String word, final boolean reformat,final boolean toLowerCase);

    /**
     * Converts the corpus representation in the representation to work with.
     * @param text the text to convert
     * @return the converted result
     */
    public abstract String corpusToReformatted(final String text);

    /**
     * Builds a crossvalidation from the current corpus.
     * @param foldparam the fold parameter to consider
     * @param corpusfile the corpusfile to split
     * @throws IOException on error
     * @throws ArithmeticException on error
     */
    public void crossValidation(final Double foldparam,final String corpusfile) throws IOException,ArithmeticException {
        this.crossfold=new String[foldparam.intValue()];
        this.crossfoldTrain=new String[foldparam.intValue()];
        this.numberOfLines=this.getLineNumbers(Files.SOURCEDIR.toString().toLowerCase()+corpusfile);
        Integer partsize = this.numberOfLines / foldparam.intValue();
        Integer foldrest=this.numberOfLines%foldparam.intValue();
        Integer linecounter=0,i=0;
        String temp;
        BufferedReader reader=new BufferedReader(new FileReader(new File(Files.SOURCEDIR.toString().toLowerCase()+corpusfile)));
        while((temp=reader.readLine())!=null){
            if(linecounter<partsize*(i+1)){
                crossfold[i]+=this.cleanWordString(temp,true,true)+System.lineSeparator();
            }else if(i==foldparam-1 && linecounter==partsize*(i+1)+foldrest) {
                System.out.println("Part "+i+" "+partsize*(i+1));
                crossfold[++i]="";
                crossfold[i]+=this.cleanWordString(temp,true,true)+System.lineSeparator();
            }else if(i<foldparam-1 && linecounter==partsize*(i+1)) {
                System.out.println("Part "+i+" "+partsize*(i+1));
                crossfold[++i]="";
                crossfold[i]+=this.cleanWordString(temp,true,true)+System.lineSeparator();
            }
            linecounter++;
        }
        reader.close();
        i=0;
        int j=0;
        for(i=0;i<crossfold.length;i++){
            crossfoldTrain[i]="";
            for(j=0;j<crossfold.length;j++){
                if(j!=i){
                    crossfoldTrain[i]+=crossfold[j];
                }
            }
        }
        i=0;
        for(String cross:crossfold){
            cross=corpusToReformatted(cross);
            this.exportTestSet(TestMethod.CROSSVALIDATION,cross,corpusfile.substring(0,corpusfile.lastIndexOf("."))+"_"+(++i)+corpusfile.substring(corpusfile.lastIndexOf(".")));
        }
    }

    /**
     * Imports the dictionary from the given corpus.
     * @param corpus the given corpus as String
     * @param testMethod the method to use for extracting
     * @param sourcelang the source language to assume
     * @return the dicthandler
     * @throws IOException on error
     * @throws SAXException on error
     * @throws ParserConfigurationException on error
     */
    public abstract DictHandling dictImport(final String corpus,final TestMethod testMethod,final CharTypes sourcelang,final Boolean map,final Boolean dict,final Boolean reverse, final Boolean ngram)throws IOException, SAXException, ParserConfigurationException;

    /**
     * Enriches an already existing corpusimport with a new resource.
     * @param filepath the annotated corpusimport
     * @param dicthandler the dicthandler to store the dictionary
     * @throws IOException on error
     */
    public abstract void enrichExistingCorpus(final String filepath,final DictHandling dicthandler) throws IOException;

    /**
     * Exports the test set to harddisk.
     * @param testmethod the testmethod to use
     * @param testSet the testset to export
     * @param corpusfile the corpusfile
     * @throws IOException on error
     */
    protected void exportTestSet(final TestMethod testmethod,final String testSet,final String corpusfile) throws IOException {
        File export=new File(Files.REFORMATTEDDIR.toString()+Files.TRANSLITDIR.toString()+File.separator+testmethod.toString().toLowerCase());
        export.mkdirs();
        export=new File(Files.REFORMATTEDDIR.toString()+Files.TRANSLITDIR.toString()+File.separator+testmethod.toString().toLowerCase()+File.separator+corpusfile);
        BufferedWriter writer=new BufferedWriter(new FileWriter(export));
        writer.write(testSet);
        writer.close();
        this.testFile=Files.REFORMATTEDDIR.toString()+Files.TRANSLITDIR.toString()+testmethod.toString().toLowerCase()+File.separator+corpusfile;
        this.testSetPath=testmethod.toString().toLowerCase()+File.separator+corpusfile;
    }

    /**
     * Exports the test set in a cuneiform representation.*
     * @param testmethod the method to use
     * @param testSet  the testset to export
     * @param corpusfile the corpusfile
     * @param segmented indicates if the output should be segmented
     * @throws IOException  on error
     */
    protected void exportTestSetCunei(TestMethod testmethod,String testSet,final String corpusfile,final Boolean segmented) throws IOException {
        if(segmented){
            File export=new File(Files.REFORMATTEDDIR.toString()+Files.CUNEI_SEGMENTEDDIR.toString()+File.separator+testmethod.toString().toLowerCase());
            export.mkdirs();
            export=new File(Files.REFORMATTEDDIR.toString()+Files.CUNEI_SEGMENTEDDIR.toString()+File.separator+testmethod.toString().toLowerCase()+File.separator+corpusfile);
            BufferedWriter writer=new BufferedWriter(new FileWriter(export));
            writer.write(testSet);
            writer.close();
            this.testFile=Files.REFORMATTEDDIR.toString()+Files.CUNEI_SEGMENTEDDIR.toString()+testmethod.toString().toLowerCase()+File.separator+corpusfile;
        }else{
            File export=new File(Files.REFORMATTEDDIR.toString()+Files.CUNEIFORMDIR.toString()+File.separator+testmethod.toString().toLowerCase());
            export.mkdirs();
            export=new File(Files.REFORMATTEDDIR.toString()+Files.CUNEIFORMDIR.toString()+File.separator+testmethod.toString().toLowerCase()+File.separator+corpusfile);
            BufferedWriter writer=new BufferedWriter(new FileWriter(export));
            writer.write(testSet);
            writer.close();
            this.testFile=Files.REFORMATTEDDIR.toString()+Files.CUNEIFORMDIR.toString()+testmethod.toString().toLowerCase()+File.separator+corpusfile;
        }

        this.testSetPath=testmethod.toString().toLowerCase()+File.separator+corpusfile;
    }

    /**
     * Generates a dictionary out of a given annotated corpusimport.
     * @param filepath the annotated corpusimport
     * @param wholecorpus indicates if the whole corpusimport should be used as a dictionary or if a test set should be generated
     * @throws IOException on error
     */
    public abstract DictHandling generateCorpusDictionaryFromFile(final List<String> filepath,final String signpath,final String filename,boolean wholecorpus,boolean corpusstr,TestMethod testMethod) throws IOException, ParserConfigurationException, SAXException, XMLStreamException;

    /**
     * Generates test/train sets as need by the current testmethod.
     * @param corpus the corpusfile
     * @param signpath the signlist path
     * @param foldOrPercent fold parameter or percent split parameter
     * @param start the split start
     * @param testMethod the method to use for extracting
     * @param charTypes  the chartype to assume
     * @return the needed dicthandler
     * @throws IOException on error
     * @throws ArithmeticException on error
     * @throws SAXException on error
     * @throws ParserConfigurationException on error
     * @throws XMLStreamException on error
     */
    public DictHandling generateTestTrainSets(final String corpus,final String signpath,final Double foldOrPercent,final Double start,final TestMethod testMethod,final CharTypes charTypes) throws IOException,ArithmeticException, SAXException, ParserConfigurationException, XMLStreamException {
        DictHandling dictHandler;
        if(checkForDict(corpus,testMethod)){
            dictHandler=this.dictImport(corpus,testMethod,charTypes,true,true,true,true);
        }else{
            List<String> paramlist;
            switch (testMethod){
                case CROSSVALIDATION: this.crossValidation(foldOrPercent,corpus);
                    if(!this.dictHandlers.containsKey(TestMethod.CROSSVALIDATION)){
                        this.dictHandlers.put(TestMethod.CROSSVALIDATION,new LinkedList<DictHandling>());
                    }
                    this.dictHandlers.get(TestMethod.CROSSVALIDATION).clear();
                    for(String cross:this.crossfold){
                        paramlist=new LinkedList<String>();
                        paramlist.add(cross);
                        this.dictHandlers.get(TestMethod.CROSSVALIDATION).add(this.generateCorpusDictionaryFromFile(paramlist, signpath,corpus, true, true,testMethod));
                    }
                    dictHandler=this.dictHandlers.get(TestMethod.CROSSVALIDATION).get(0);
                    this.exportTestSetCunei(testMethod,this.transliterationToText(testSet,0,dictHandler,false,true),corpus,true);
                    this.testSet=this.transliterationToText(testSet,0,dictHandler,false,false);
                    this.exportTestSetCunei(testMethod,this.testSet,corpus,false);
                    break;
                case RANDOMSAMPLE:this.randomSample(foldOrPercent,corpus);
                    if(!this.dictHandlers.containsKey(TestMethod.RANDOMSAMPLE)){
                        this.dictHandlers.put(TestMethod.RANDOMSAMPLE,new LinkedList<DictHandling>());
                    }
                    paramlist=new LinkedList<>();
                    paramlist.add(trainSet);
                    this.dictHandlers.get(TestMethod.RANDOMSAMPLE).add(this.generateCorpusDictionaryFromFile(paramlist, signpath, corpus, true, true,testMethod));
                    dictHandler=this.dictHandlers.get(TestMethod.RANDOMSAMPLE).get(0);
                    this.exportTestSetCunei(testMethod,this.transliterationToText(testSet,0,dictHandler,false,true),corpus,true);
                    this.testSet=this.transliterationToText(testSet,0,dictHandler,false,false);
                    this.exportTestSetCunei(testMethod,this.testSet,corpus,false);
                    break;
                case PERCENTAGE:this.percentSegmentation(0.1,start,corpus);
                    if(!this.dictHandlers.containsKey(TestMethod.PERCENTAGE)){
                        this.dictHandlers.put(TestMethod.PERCENTAGE,new LinkedList<DictHandling>());
                    }
                    paramlist=new LinkedList<>();
                    paramlist.add(trainSet);
                    this.dictHandlers.get(TestMethod.PERCENTAGE).add(
                            this.generateCorpusDictionaryFromFile(paramlist, signpath, corpus, true, true,testMethod));
                    dictHandler=this.dictHandlers.get(TestMethod.PERCENTAGE).get(0);
                    this.exportTestSetCunei(testMethod,this.transliterationToText(testSet,0,dictHandler,false,true),corpus,true);
                    this.testSet=this.transliterationToText(testSet,0,dictHandler,false,false);
                    this.exportTestSetCunei(testMethod,this.testSet,corpus,false);
                    break;
                case TEXTPERCENTAGE:this.textPercentageSplit(0.1, start, false, corpus,charTypes);
                    if(!this.dictHandlers.containsKey(TestMethod.TEXTPERCENTAGE)){
                        this.dictHandlers.put(TestMethod.TEXTPERCENTAGE,new LinkedList<DictHandling>());
                    }
                    paramlist=new LinkedList<>();
                    paramlist.add(trainSet);
                    this.dictHandlers.get(TestMethod.TEXTPERCENTAGE).add(
                            this.generateCorpusDictionaryFromFile(paramlist, signpath, corpus, true, true,testMethod));
                    dictHandler=this.dictHandlers.get(TestMethod.TEXTPERCENTAGE).get(0);
                    this.exportTestSetCunei(testMethod,this.transliterationToText(testSet,0,dictHandler,false,true),corpus,true);
                    this.testSet=this.transliterationToText(testSet,0,dictHandler,false,false);
                    this.exportTestSetCunei(testMethod,this.testSet,corpus,false);
                    break;
                case RANDOMTEXTPERCENTAGE:this.textPercentageSplit(0.1, start, true, corpus,charTypes);
                    if(!this.dictHandlers.containsKey(TestMethod.RANDOMTEXTPERCENTAGE)){
                        this.dictHandlers.put(TestMethod.RANDOMTEXTPERCENTAGE,new LinkedList<DictHandling>());
                    }
                    paramlist=new LinkedList<>();
                    paramlist.add(trainSet);
                    this.dictHandlers.get(TestMethod.RANDOMTEXTPERCENTAGE).add(
                            this.generateCorpusDictionaryFromFile(paramlist, signpath, corpus, true, true,testMethod));
                    dictHandler=this.dictHandlers.get(TestMethod.RANDOMTEXTPERCENTAGE).get(0);
                    this.exportTestSetCunei(testMethod,this.transliterationToText(testSet,0,dictHandler,false,true),corpus,true);
                    this.testSet=this.transliterationToText(testSet,0,dictHandler,false,false);
                    this.exportTestSetCunei(testMethod,this.testSet,corpus,false);
                    break;
                case FOREIGNTEXT:
                default:
                    if(!this.dictHandlers.containsKey(TestMethod.FOREIGNTEXT)){
                        this.dictHandlers.put(TestMethod.FOREIGNTEXT,new LinkedList<DictHandling>());
                    }
                    paramlist=new LinkedList<>();
                    paramlist.add(corpus);
                    this.dictHandlers.get(TestMethod.FOREIGNTEXT).add(
                            this.generateCorpusDictionaryFromFile(paramlist,signpath,corpus,true, false,TestMethod.FOREIGNTEXT));
                    dictHandler=this.dictHandlers.get(TestMethod.FOREIGNTEXT).get(0);
            }
        }
        return dictHandler;
    }

    /**
     * Gets the ith test crossfold.
     * @param i the ith part
     * @return the crossfold as String
     */
    public String getCrossFold(Integer i) {
        return this.crossfold[i];
    }

    /**
     * Gets the corssvalidation dicthandler if it already exists.
     * @param fold the fold to get
     * @return the dicthandler
     */
    public DictHandling getCrossValidation(final Integer fold){
          return this.dictHandlers.get(TestMethod.CROSSVALIDATION).get(fold);

    }

    /**
     * Gets a crossvalidation train set.
     * @param number the fold
     * @return the trainset as String
     */
    public String getCrossValidationTrainSet(final Integer number){
           if(this.crossfold==null || number>this.crossfold.length){
               return null;
           }
           StringBuilder result=new StringBuilder();
           for(int i=0;i<this.crossfold.length;i++){
               if(i!=number){
                   result.append(this.crossfold[i]);
               }
           }
        return result.toString();
    }

    /**
     * Gets the length of the crossfold.
     * @return the length as Integer
     */
    public Integer getCrossfoldLength() {
        return crossfold.length;
    }

    /**
     * Gets the amount of line numbers of a file.
     * @param filename the filename
     * @return the amount as Integer
     * @throws IOException on error
     */
    public Integer getLineNumbers(final String filename) throws IOException {
        LineNumberReader lnr = new LineNumberReader(new FileReader(new File(filename)));
        lnr.skip(Long.MAX_VALUE);
        this.numberOfLines=lnr.getLineNumber();
        System.out.println(this.numberOfLines);
        lnr.close();
        return this.numberOfLines;
    }

    /**Gets a corresponding POSTagger.*/
    public abstract POSTagger getPOSTagger(Boolean newPosTagger);

    /**
     * Gets the current testset.
     * @return the current testset as String
     */
    public String getTestSet() {
        return testSet;
    }

    /**
     * Sets the current testset.
     * @param testSet the testset as String
     */
    public void setTestSet(final String testSet) {
        this.testSet = testSet;
    }

    /**
     * Gets the current testsetpath.
     * @return the path as String
     */
    public String getTestSetPath() {
        return testSetPath;
    }

    /**Gets a utility dictHandler which is not initialized.*/
    public abstract DictHandling getUtilDictHandler();

    /**
     * Executes the precent segmenation algorithm.
     * @param perc the percentage of the testset
     * @param startline the line to start
     * @param corpusfile the corpusfile
     * @throws IOException on error
     */
    public void percentSegmentation(final Double perc,final Double startline,final String corpusfile) throws IOException, ParserConfigurationException, SAXException {
         this.numberOfLines=this.getLineNumbers(Files.SOURCEDIR.toString().toLowerCase()+corpusfile);
         Integer linecounter=0,testSetLines=(int)((Double.valueOf(perc)/100)*this.numberOfLines),trainsetLines=this.numberOfLines-testSetLines;
         String temp;
        System.out.println("Testsetlines: "+testSetLines);
         BufferedReader reader=new BufferedReader(new FileReader(new File(Files.SOURCEDIR.toString().toLowerCase()+corpusfile)));
         while((temp=reader.readLine())!=null){

             if(linecounter<startline || linecounter>(startline+testSetLines)){
                 //System.out.println("Trainset: "+linecounter+" "+temp);
                 this.trainSet+=temp+System.lineSeparator();
             }else{
                 System.out.println("Testset: "+linecounter+" "+temp);
                 this.testSet+=temp+System.lineSeparator();
             }
             linecounter++;
         }
         reader.close();
         this.testSet=this.corpusToReformatted(this.testSet);
         this.exportTestSet(TestMethod.PERCENTAGE,this.testSet,corpusfile);
    }

    /**
     * Random sample set algorithm.
     * @param sampleSize the size of the sample
     * @param corpusfile the corpusfile
     * @throws IOException on error
     */
    public void randomSample(Double sampleSize,final String corpusfile) throws IOException, ParserConfigurationException, SAXException {
        this.numberOfLines=this.getLineNumbers(Files.SOURCEDIR.toString().toLowerCase()+corpusfile);
        if(sampleSize>this.numberOfLines){
            sampleSize=Double.valueOf(numberOfLines);
        }
        Set<Integer> sampleKeys=new TreeSet<>();
        int tempint;
        while(sampleKeys.size()<sampleSize){
            tempint=this.randomGenerator.nextInt()%sampleSize.intValue();
            if(!sampleKeys.contains(tempint)){
                sampleKeys.add(tempint);
            }
        }
        Integer linecounter=0,i=0;
        String temp;
        BufferedReader reader=new BufferedReader(new FileReader(new File(Files.SOURCEDIR.toString().toLowerCase()+corpusfile)));
        while((temp=reader.readLine())!=null){
            if(sampleKeys.contains(linecounter)){
                testSet+=this.cleanWordString(temp,true,true)+System.lineSeparator();
            }else {
                trainSet+=temp+System.lineSeparator();
            }
            linecounter++;
        }
        reader.close();
        this.testSet=this.corpusToReformatted(this.testSet);
        this.exportTestSet(TestMethod.RANDOMSAMPLE,this.testSet,corpusfile);
    }

    public abstract void textPercentageSplit(final Double perc,final Double startline,final Boolean random,final String corpusfile,final CharTypes charTypes) throws IOException, ParserConfigurationException, SAXException;

    public String transliterationToBoundaries(String chars,CharTypes chartype){
        chars="["+chars+"]";
        StringBuilder result=new StringBuilder();
        String tempchar="";
        Set<String> stopchars=new HashSet<>(chartype.getStopchars());
        String[] sylls=chars.split(" |-");
        System.out.println("TransliterationToBoundaries: "+chars+" ");
        for(int i=0;i<sylls.length;i++){
            if(!sylls[i].endsWith("]")){
                result.append("0,");
            }else if(i==sylls.length-1){
                result.append("1,");
            }
        }
        return result.toString();
    }

    public abstract String transliterationToText(String text,Integer duplicator, DictHandling dicthandler,Boolean countmisses,final Boolean segmented);
}

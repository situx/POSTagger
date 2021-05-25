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

package com.github.situx.postagger.dict.corpusimport.asian;

import com.github.situx.postagger.dict.dicthandler.asian.CNDictHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.utils.POSTag;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TestMethod;
import com.github.situx.postagger.util.enums.util.Tags;
import com.github.situx.postagger.dict.chars.asian.CNChar;
import com.github.situx.postagger.dict.corpusimport.CorpusHandlerAPI;
import com.github.situx.postagger.dict.corpusimport.util.CNXMLToSet;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.importhandler.asian.CNImportHandler;
import com.github.situx.postagger.dict.utils.StopChar;
import com.github.situx.postagger.util.enums.util.Files;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by timo on 03.07.14.
 */
public class CNCorpusHandler extends CorpusHandlerAPI  {
    /**The list of words.*/
    private List<String> cnwords;
    private boolean collectSplit;
    /**The DictHandler to choose.*/
    private DictHandling dictHandler;
    private Map<Integer,Integer> fileToAmount;
    /**Indicates if the parses is within a stopchar.*/
    private boolean insidedotcomma=false,splitOrParse=false;
    /**The last seen stopchar.*/
    private StopChar laststopchar;
    private Integer lines,start;
    /**The list of pinyin to use.*/
    private List<String> pinyin;
    /**Indicators for parsing.*/
    private Boolean pinyinOrcnword=false,insideword=false,writepinyin=false,writecnwords=false,justWroteStopChar=false;
    /**List of postags to parse.*/
    private List<String> postags;
    private boolean splitText;
    /**Temp strings for parsing.*/
    private String temppinyin="",tempcnchar="",tempdotcomma="",curpostag="",lasttempcnchar="",templine="";

    /**
     * Constructor for this class.
     * @param stopchars the list of stopchars
     */
    public CNCorpusHandler(final List<String> stopchars){
        super(stopchars);
        this.pinyin=new LinkedList<>();
        this.cnwords=new LinkedList<>();
        this.postags=new LinkedList<>();
        this.lines=0;
    }

    /**
     * Testing main method for parsing Lcmc corpus.
     *
     * @param args
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XMLStreamException
     */
    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
          File file=new File("/home/timo/workspace2/Master/2474/2474/Lcmc/data/character/");
          File file2=new File("/home/timo/workspace2/Master/2474/2474/Lcmc/data/pinyin/");
          CNCorpusHandler handler=new CNCorpusHandler(CharTypes.CHINESE.getStopchars());
          List<String> paramlist=new LinkedList<>();paramlist.add("/home/timo/workspace2/Master/2474/2474/Lcmc/data/");
          handler.textPercentageSplit(0.2,0.,true,"Lcmc",CharTypes.CHINESE);
          /*handler.generateCorpusDictionaryFromFile(paramlist,null,"Lcmc",true,false,TestMethod.FOREIGNTEXT);
            CEDictImportHandler importHandler=new CEDictImportHandler();
            importHandler.parseCNCeDict(Files.DICTDIR + "cedict_ts.u8", Locale.ENGLISH, handler.dictHandler);
        handler.dictHandler.exportToXML("source/cn1.txt","source/cn2.txt","source/cn3.txt","source/cn4.txt");*/
    }

    public void CNFileToCNText(){
         this.writecnwords=true;
         this.writecnwords=false;
    }

    @Override
    public void addTranslations(final String file, final TestMethod testMethod1) throws ParserConfigurationException, SAXException, IOException {

    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if(insideword && pinyinOrcnword){
            this.temppinyin+=new String(ch,start,length);
        }
        if(insideword && !pinyinOrcnword){
            this.tempcnchar+=new String(ch,start,length);
        }
        if(insidedotcomma && !pinyinOrcnword){
            this.tempdotcomma+=new String(ch,start,length);
        }
        if(insidedotcomma && pinyinOrcnword){
            this.tempdotcomma+=new String(ch,start,length);
        }

    }

    @Override
    public String cleanWordString(final String word, final boolean reformat,boolean lowercase) {
        return word;
    }

    @Override
    public String corpusToReformatted(final String text) {
        return null;
    }

    @Override
    public void crossValidation(final Double foldparam, final String corpusfile) throws IOException {

        super.crossValidation(foldparam, corpusfile);
    }

    @Override
    public DictHandling dictImport(final String corpus, final TestMethod testMethod, final CharTypes sourcelang,final Boolean map,final Boolean dict,final Boolean reverse, final Boolean ngram) throws IOException, SAXException, ParserConfigurationException {
        final CNDictHandler dictHandler=new CNDictHandler(this.stopchars);
        dictHandler.importMappingFromXML(Files.DICTDIR+sourcelang.getLocale()+Files.MAPSUFFIX);
        dictHandler.importDictFromXML(Files.DICTDIR+sourcelang.getLocale()+Files.DICTSUFFIX);
        dictHandler.importReverseDictFromXML(Files.DICTDIR+sourcelang.getLocale()+Files.REVERSE+Files.DICTSUFFIX);
        dictHandler.importNGramsFromXML(Files.DICTDIR+sourcelang.getLocale()+Files.NGRAMSUFFIX);
        return dictHandler;
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        switch(qName){
            case Tags.W:
                if(!splitOrParse){
                insideword=false;
                if(this.pinyinOrcnword){
                    this.postags.add(curpostag);
                    String res=" ";
                    Pattern pattern= Pattern.compile("([A-Za-z]+[0-9])");
                    Matcher matcher=pattern.matcher(this.temppinyin);
                    while(matcher.find()){
                        res+=(this.temppinyin.substring(matcher.start(),matcher.end())+"-");
                    }
                    res=res.substring(0,res.length()-1);
                    if(this.writepinyin){
                        try {
                            //this.temppinyin=this.temppinyin.replaceAll("([0-9])","$1-");
                            this.reformattedTranslitWriter.write("["+ res.trim()+ "] ");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                    //if(!res.isEmpty())
                        this.pinyin.add(this.temppinyin);
                    temppinyin="";
                }else if(!this.pinyinOrcnword){
                    if(justWroteStopChar){
                        this.laststopchar.addFollowing(this.tempcnchar);
                        this.dictHandler.getNGramStats().generateNGramsFromLine(dictHandler.getChartype(),templine,templine.length());
                        this.justWroteStopChar=false;
                        this.templine="";
                    }
                    if(this.writecnwords){
                        try {
                            this.dictHandler.incLengthOfWordsInCorpus(tempcnchar.length()/CharTypes.CHINESE.getChar_length());
                            this.cuneiSegmentExport.write(this.tempcnchar+" ");
                            this.cuneiWOSegmentExport.write(this.tempcnchar);
                            this.reformattedBoundaryWriter.write(this.charsToBoundaries(this.tempcnchar, CharTypes.CHINESE));
                            this.templine+=tempcnchar;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    this.cnwords.add(tempcnchar);
                    this.lasttempcnchar=tempcnchar;
                    this.tempcnchar="";
                }
                }break;
            case Tags.FILE:
                if(splitOrParse){
                  this.fileToAmount.put(start,lines-start);
                }
                break;
            case Tags.S:
                if(splitOrParse){

                }
                lines++;
                break;
            case Tags.C:
                if(!splitOrParse) {
                    if (this.writecnwords) {
                        try {
                            this.cuneiSegmentExport.write(/*tempdotcomma + */System.lineSeparator());
                            this.cuneiWOSegmentExport.write(/*tempdotcomma + */System.lineSeparator());
                            this.reformattedBoundaryWriter.write(System.lineSeparator());
                            StopChar stopChar = new StopChar();
                            stopChar.setStopchar(Pattern.quote(tempdotcomma));
                            stopChar.addPreceding(lasttempcnchar);
                            dictHandler.addStopWord(stopChar);
                            this.laststopchar = stopChar;
                            this.justWroteStopChar = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (this.writepinyin) {
                        try {
                            this.reformattedTranslitWriter.write(/*tempdotcomma + */System.lineSeparator());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                    this.tempdotcomma = "";
                    this.insidedotcomma = false;
                }
                break;
            default:
        }
    }

    @Override
    public void enrichExistingCorpus(final String filepath, final DictHandling dicthandler) throws IOException {

    }

    @Override
    public DictHandling generateCorpusDictionaryFromFile(final List<String> filepath,final String signpath,final String filename,final boolean wholecorpus,final boolean fileOrStr,final TestMethod testMethod) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        this.dictHandler=new CNDictHandler(CharTypes.CHINESE.getStopchars());
        File file=new File(Files.SOURCEDIR.toString()+filepath.get(0)+"/character");
        File file2=new File(Files.SOURCEDIR.toString()+filepath.get(0)+"/pinyin");
        this.cuneiSegmentExport =new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString()+Files.CUNEI_SEGMENTEDDIR.toString()+filename+Files.REFORMATTED)));
        this.cuneiWOSegmentExport =new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString()+Files.CUNEIFORMDIR.toString()+filename+Files.REFORMATTED)));
        this.reformattedTranslitWriter =new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString()+Files.TRANSLITDIR.toString()+filename+Files.REFORMATTED)));
        this.reformattedBoundaryWriter =new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString()+Files.BOUNDARYDIR.toString()+filename+Files.BOUNDARIES)));
        int i=0,leaveout=-1;
        File[] array1=file.listFiles();
        File[] array2=file2.listFiles();
        Arrays.sort(array1);
        Arrays.sort(array2);
        if(!wholecorpus){
            Random random=new Random();
            leaveout=random.nextInt(array1.length);
        }
        int j=0;
        for(File fil:array1){
            System.out.println("File: "+fil.getAbsolutePath()+" File2: "+array2[i].getName());
            if(i==leaveout || j++<0){
                System.out.println("Leaving out: "+fil.getName());
                i++;
            }else{
                try {
                    this.parseFiles(fil, array2[i++],-1);
                }catch(Exception e){
                    e.printStackTrace();
                    break;
                }
            }
        }
        System.out.println("MIXED!");
        //this.dictHandler.exportToXML("source/cn1.txt","source/cn2.txt","source/cn3.txt","source/cn4.txt");
        this.reformattedBoundaryWriter.close();
        this.reformattedTranslitWriter.close();
        this.cuneiSegmentExport.close();
        this.cuneiWOSegmentExport.close();
        return this.dictHandler;
    }

    @Override
    public Integer getLineNumbers(final String filename) throws IOException {
        File file=new File(filename);
        Integer result=0;
        if(file.isDirectory()){
            for(File fil:file.listFiles()) {
                if(fil.isDirectory()){
                    result+=this.getLineNumbers(fil.getAbsolutePath());
                }else{
                    result += super.getLineNumbers(filename);
                }
            }
        }else{
            result+=super.getLineNumbers(filename);
        }
        return result;
    }

    @Override
    public POSTagger getPOSTagger(Boolean newPosTagger) {
        return null;
    }

    @Override
    public DictHandling getUtilDictHandler() {
        return null;
    }

    /**
     * Parses two files of Lcmc in accordance with pinyin and chinese char representation.
     * @param file character file
     * @param file2 pinyin file
     * @param limit limits parsing to a certain extent
     * @throws ParserConfigurationException on error
     * @throws SAXException on error
     * @throws IOException on error
     */
    public void parseFiles(final File file,final File file2,final Integer limit) throws ParserConfigurationException, SAXException, IOException {
        this.pinyinOrcnword=false;
        this.writecnwords=true;
        this.charcounter=0.;
        this.wordcounter=0.;
        Integer lowerbound=0,upperbound=1/*275000*/;//270980;
        SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
        parser.parse(file,this);
        this.pinyinOrcnword=true;
        this.writepinyin=true;
        parser= SAXParserFactory.newInstance().newSAXParser();
        parser.parse(file2,this);
        Iterator<String> pinyiniter=pinyin.iterator();
        Iterator<String> cnworditer=cnwords.iterator();
        Iterator<String> pos=postags.iterator();
        String currentpinyinword="",currentpinyinsyll="",currentCNchar,str;
        String[] pinyinsplitword=new String[0];
        Boolean newword=false;
        Integer numberOfPinyin=0,numberOfChars,j=0;

        for(;cnworditer.hasNext();){
            str=cnworditer.next();
            str=str.replace("·","").replace("-","").replace(">","")/*.replace("．","")*/;
            if(CNImportHandler.isFullWidthNumeric(str) || str.matches("[A-Za-zＡ-ｚ×①②③④⑤⑥⑦⑧⑨⑩⑾⑿⒀⒂－．]+")){
                //if(j>35000 && j<45000)
                //System.out.println("Skip: "+str);
                currentpinyinword=pinyiniter.next();
                continue;
            }else if(str.split("\\p{Nd}").length>0){
                str=str.replaceAll("\\p{Nd}","");
            }
            if(str.matches("[A-Za-zＡ-ｚ×①②③④⑤⑥⑦⑧⑨⑩⑾⑿⒀⒂－．]+")){
                currentpinyinword=pinyiniter.next();
                continue;
            }
            str=str.replaceAll("[A-Za-zＡ-ｚ]","");
            //if(j>lowerbound && j<upperbound)
            //System.out.println("Str: "+str);
            for(int i=0;i<str.length();i+=1){
                currentCNchar=str.substring(i,i+1);
                if(CNImportHandler.isFullWidthNumeric(currentCNchar) || currentCNchar.matches("[A-Za-zＡ-ｚ×①②③④⑤⑥⑦⑧⑨⑩⑾⑿⒀⒂－-]+")){
                    continue;
                }
                if(numberOfPinyin==0){
                    currentpinyinword=pinyiniter.next();
                    if(currentpinyinword.matches("□□")){
                        currentpinyinword="□3□";
                    }
                    if(currentpinyinword.equals("C") || currentpinyinword.equals(" ") || currentpinyinword.equals("-") || currentpinyinword.equals("--") || currentpinyinword.equals("──")){
                        currentpinyinword=pinyiniter.next();
                    }
                    currentpinyinword=currentpinyinword.replaceAll("[Ａ-ｚ]","");
                    pinyinsplitword=currentpinyinword.split("(?<=\\p{Nd})");
                    while(pinyinsplitword.length==0 || CNImportHandler.isFullWidthNumeric(currentpinyinword)  || currentCNchar.matches("[Ａ-ｚ]+") || currentCNchar.matches("^[\\?]+$")){
                        if(currentCNchar.matches("^[\\?]+$")){
                            str=cnworditer.next();
                            i=0;
                            currentCNchar=str.substring(i,i+1);
                            if(j>lowerbound && j<upperbound)
                                System.out.println("Str: "+str);
                        }
                        currentpinyinword=pinyiniter.next();
                        if(currentpinyinword.equals("C")){
                            currentpinyinword=pinyiniter.next();
                        }
                        pinyinsplitword=currentpinyinword.split("(?<=\\p{Nd})");
                    }
                    //System.out.print("pinyinsplitword: ");
                    //if(j>lowerbound && j<upperbound)
                    //    ArffHandler.arrayToStr(pinyinsplitword);
                    //System.out.println(" "+currentpinyinword);
                    numberOfPinyin=pinyinsplitword.length;
                }
                //if(j>lowerbound && j<upperbound)
                    //System.out.println("Match Test Outer: "+currentCNchar+" - "+currentpinyinsyll+" "+currentpinyinsyll.matches("^[\\?]+$"));
                if(!newword){
                    currentpinyinsyll=pinyinsplitword[pinyinsplitword.length-numberOfPinyin--];
                }else{
                    newword=false;
                }
                while((CNImportHandler.isFullWidthNumeric(currentpinyinsyll) || currentpinyinsyll.contains("?")) || currentpinyinsyll.matches("[Ａ-ｚ]+")  && !newword){
                    if(j>lowerbound && j<upperbound)
                        System.out.println("Match Test Inner 1: "+currentCNchar+" - "+currentpinyinsyll+" "+currentpinyinsyll.matches("^[\\?]+$"));
                    if(currentpinyinsyll.contains("?")){
                        if(!currentpinyinsyll.matches("^[\\?]+$")){
                            currentpinyinsyll=currentpinyinsyll.replace("?","");
                        }

                        if(i==str.length()-1){
                            if(j>lowerbound && j<upperbound)
                                System.out.println("Next CNWord");

                            str=cnworditer.next();
                            i=0;

                            if(j>lowerbound && j<upperbound)
                                System.out.println("Str: "+str);
                        }else{
                            i++;
                            if(j>lowerbound && j<upperbound)
                                System.out.println("Next Syllable");
                        }
                        currentCNchar=str.substring(i,i+1);
                        newword=true;
                    }
                    if(numberOfPinyin>0){
                        currentpinyinsyll=pinyinsplitword[pinyinsplitword.length-numberOfPinyin--];
                        if(j>lowerbound && j<upperbound)
                        System.out.println("Match Test Inner 2: "+currentCNchar+" - "+currentpinyinsyll+" "+currentpinyinsyll.matches("^[\\?]+$"));
                        if(currentpinyinsyll.matches("[Ａ-ｚ]+") && numberOfPinyin>0){
                            currentpinyinsyll=pinyinsplitword[pinyinsplitword.length-numberOfPinyin--];
                        }else if((currentpinyinsyll.matches("[Ａ-ｚ]+")||currentpinyinsyll.matches("^[\\?]+$")) && numberOfPinyin<1){
                            currentpinyinword=pinyiniter.next();
                            pinyinsplitword=currentpinyinword.split("(?<=\\p{Nd})");
                            while(pinyinsplitword.length==0 || CNImportHandler.isFullWidthNumeric(currentpinyinword) || currentCNchar.matches("[Ａ-ｚ]+")){
                                currentpinyinword=pinyiniter.next();
                                if(currentpinyinword.equals("C")){
                                    currentpinyinword=pinyiniter.next();
                                }
                                pinyinsplitword=currentpinyinword.split("(?<=\\p{Nd})");
                            }
                            //System.out.print("pinyinsplitword: ");
                            if(j>lowerbound && j<upperbound)
                            //ArffHandler.arrayToStr(pinyinsplitword);
                            //System.out.println(" "+currentpinyinword);
                            numberOfPinyin=pinyinsplitword.length;
                        }
                    }else{
                        currentpinyinword=pinyiniter.next();
                        if(currentpinyinword.equals("C")){
                            currentpinyinword=pinyiniter.next();
                        }
                        pinyinsplitword=currentpinyinword.split("(?<=\\p{Nd})");
                        while(pinyinsplitword.length==0 || CNImportHandler.isFullWidthNumeric(currentpinyinword) || currentCNchar.matches("[Ａ-ｚ]+")){
                            currentpinyinword=pinyiniter.next();
                            if(currentpinyinword.equals("C")){
                                currentpinyinword=pinyiniter.next();
                            }
                            pinyinsplitword=currentpinyinword.split("(?<=\\p{Nd})");
                        }
                        //System.out.print("pinyinsplitword: ");
                        //if(j>lowerbound && j<upperbound)
                            //ArffHandler.arrayToStr(pinyinsplitword);
                        //System.out.println(" "+currentpinyinword);
                        numberOfPinyin=pinyinsplitword.length;
                    }

                }
                if(newword){
                    continue;
                }
                if(j>lowerbound && j<upperbound)
                System.out.println(j+". CurrentCNChar: "+currentCNchar+" - "+currentpinyinsyll);
                j++;
                CNChar addchar=new CNChar(currentCNchar);
                addchar.addTransliteration(new Transliteration(currentpinyinsyll.replaceAll("-","").trim(),currentpinyinsyll.replaceAll("-","").trim()));
                this.dictHandler.addChar(addchar);
                this.charcounter++;
            }
            if(!CNImportHandler.isFullWidthNumeric(str)){
                CNChar addword=new CNChar(str);
                if(currentpinyinword.length()>1){
                    String res=" ";
                    Pattern pattern= Pattern.compile("([A-Za-z]+[0-9])");
                    Matcher matcher=pattern.matcher(currentpinyinword);
                    while(matcher.find()){
                        res+=(currentpinyinword.substring(matcher.start(),matcher.end())+"-");
                    }
                    res=res.substring(0,res.length()-1);
                    currentpinyinword=res;
                    /*if(currentpinyinword.replace("^[-]*","")){
                        currentpinyinword=currentpinyinword.substring(1,currentpinyinword.length());
                    }*/
                    addword.addTransliteration(new Transliteration(res.trim(), currentpinyinword.replaceAll("-","")));
                    addword.addPOSTag(new POSTag(pos.next()));
                    /*currentpinyinword=currentpinyinword.substring(0,currentpinyinword.length()-1);
*/

                }
                this.dictHandler.addWord(addword,CharTypes.CHINESE);
                this.wordcounter++;
                this.charcounter+=addword.getNumberOfChars();
            }
        }
        if(!splitOrParse) {
            dictHandler.calculateRightLeftAccessorVariety();
            dictHandler.calculateRelativeCharOccurances(this.charcounter);
            dictHandler.calculateRelativeWordOccurances(this.wordcounter);
            dictHandler.calculateAvgWordLength();
        }
    }

    @Override
    public void percentSegmentation(final Double perc, final Double startline, final String corpusfile) throws IOException, ParserConfigurationException, SAXException {
        Integer lineNumbers=this.getLineNumbers(corpusfile);
        Integer linecounter=0,testSetLines=(int)((Double.valueOf(perc)/100)*this.numberOfLines),trainsetLines=this.numberOfLines-testSetLines;
        String temp;
        System.out.println("Testsetlines: "+testSetLines);
        File file=new File(Files.SOURCEDIR.toString()+corpusfile+"/character/");
        File file2=new File(Files.SOURCEDIR.toString()+corpusfile+"/pinyin/");
        File[] array1=file.listFiles();
        File[] array2=file2.listFiles();
        Arrays.sort(array1);
        Arrays.sort(array2);
        int j=0;
        lines=0;
        this.fileToAmount=new TreeMap<>();
        for(File fil:array1){
            System.out.println("File: "+fil.getAbsolutePath()+" File2: "+array2[j].getName());
            this.splitOrParse=true;
            this.splitText=false;
            this.parseFiles(fil,array2[j++],-1);
        }
        int border=-1,linec=0;
        String line;
        this.trainSet="";
        linec=0;
        for(File fil:array1){
            BufferedReader reader=new BufferedReader(new FileReader(fil));
            while((line=reader.readLine())!=null){
                if(line.contains("<s ") && linec>startline && linec<startline+(perc*lines)){
                    this.trainSet+=line+System.lineSeparator();
                }else{
                    this.testSet+=line+System.lineSeparator();
                }
                if(line.contains("<s ")){
                    linec++;
                }
            }
            reader.close();
        }
        this.testSet=new CNXMLToSet().convert(this.testSet);
        this.exportTestSet(TestMethod.PERCENTAGE,this.testSet,corpusfile);

    }

    public void pinyinFileToPinyinText(){
          this.writepinyin=true;
          this.writepinyin=false;
    }

    @Override
    public void randomSample(final Double sampleSize, final String corpusfile) throws IOException, ParserConfigurationException, SAXException {
        File file=new File(Files.SOURCEDIR.toString()+corpusfile+"/character/");
        File file2=new File(Files.SOURCEDIR.toString()+corpusfile+"/pinyin/");
        File[] array1=file.listFiles();
        File[] array2=file2.listFiles();
        Arrays.sort(array1);
        Arrays.sort(array2);
        int j=0;
        lines=0;
        this.fileToAmount=new TreeMap<>();
        for(File fil:array1){
            System.out.println("File: "+fil.getAbsolutePath()+" File2: "+array2[j].getName());
            this.splitOrParse=true;
            this.parseFiles(fil,array2[j++],-1);
        }
        Double chosenpercentage=0.;
        List<Integer> entrysetlist=new LinkedList<>(this.fileToAmount.keySet());
        List<Integer> chosenkeys=new LinkedList<>();
            Random random=new Random(System.currentTimeMillis());
            while(chosenpercentage<sampleSize && chosenkeys.size()<entrysetlist.size()){
                int rand=random.nextInt(entrysetlist.size());
                if(!chosenkeys.contains(rand)){
                    chosenkeys.add(entrysetlist.get(rand));
                    chosenpercentage+=Double.valueOf(this.fileToAmount.get(entrysetlist.get(rand)))/Double.valueOf(lines);
                }
            }
        int border=-1,linec=0;
        String line;
        this.trainSet="";
        linec=0;
        for(File fil:array1){
            BufferedReader reader=new BufferedReader(new FileReader(fil));
            while((line=reader.readLine())!=null){
                if(line.contains("<s ") && chosenkeys.contains(linec) && linec>border){
                    border=linec+this.fileToAmount.get(linec);
                }else if(line.contains("<s ") && linec<border){
                    this.trainSet+=line+System.lineSeparator();
                }
                if(line.contains("<s ")){
                    linec++;
                }
            }
            reader.close();
        }
        this.trainSet=new CNXMLToSet().convert(this.trainSet);
        this.exportTestSet(TestMethod.RANDOMSAMPLE,this.trainSet,corpusfile);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        switch (qName){
            case Tags.W: if(!splitOrParse){insideword=true;this.curpostag=attributes.getValue("POS");}break;
            case Tags.C: if(!splitOrParse){insidedotcomma=true; this.curpostag=attributes.getValue("POS");} break;
            case Tags.FILE: if(splitOrParse){
                start=lines;
            }
                break;
            default:
        }
    }

    @Override
    public void textPercentageSplit(final Double perc, final Double startline,final Boolean randomm,final String corpusfile, final CharTypes charTypes) throws IOException, ParserConfigurationException, SAXException {
        File file=new File(Files.SOURCEDIR.toString()+corpusfile+"/character/");
        File file2=new File(Files.SOURCEDIR.toString()+corpusfile+"/pinyin/");
        File[] array1=file.listFiles();
        File[] array2=file2.listFiles();
        Arrays.sort(array1);
        Arrays.sort(array2);
        int j=0;
        lines=0;
        this.fileToAmount=new TreeMap<>();
        for(File fil:array1){
            System.out.println("File: "+fil.getAbsolutePath()+" File2: "+array2[j].getName());
            this.splitOrParse=true;

            this.parseFiles(fil,array2[j++],-1);
        }
        Double chosenpercentage=0.;
        List<Integer> entrysetlist=new LinkedList<>(this.fileToAmount.keySet());
        List<Integer> chosenkeys=new LinkedList<>();
        if(randomm){
            Random random=new Random(System.currentTimeMillis());
            while(chosenpercentage<perc && chosenkeys.size()<entrysetlist.size()){
                int rand=random.nextInt(entrysetlist.size());
                if(!chosenkeys.contains(rand)){
                    chosenkeys.add(entrysetlist.get(rand));
                    chosenpercentage+=Double.valueOf(this.fileToAmount.get(entrysetlist.get(rand)))/Double.valueOf(lines);
                }
            }
        }else{
            int rand=0;
            while(chosenpercentage<perc && chosenkeys.size()<entrysetlist.size()){
                chosenkeys.add(entrysetlist.get(rand));
                chosenpercentage+=Double.valueOf(this.fileToAmount.get(entrysetlist.get(rand++)))/Double.valueOf(lines);
            }
        }
        int border=-1,linec=0;
        String line,trainsetpinyin="<?xml version=\"1.0\"?><data>";
        this.trainSet="<?xml version=\"1.0\"?><data>";
        String testSet="<?xml version=\"1.0\"?><data>",testSetPinyin="<?xml version=\"1.0\"?><data>";
        /*linec=0;
        for(File fil:array1){
            System.out.println("Cunei: "+fil.getAbsolutePath());
            BufferedReader reader=new BufferedReader(new FileReader(fil));
            while((line=reader.readLine())!=null){
                if(line.contains("<s ") && chosenkeys.contains(linec) && linec>border){
                    border=linec+this.fileToAmount.get(linec);
                }else if(line.contains("<s ") && linec<border){
                    if(!line.trim().endsWith("</s>")){
                        this.trainSet+=line+"</s>"+System.lineSeparator();
                    }else{
                        this.trainSet+=line+System.lineSeparator();
                    }

                }else if(line.contains("<s ")){
                    testSet+=line+System.lineSeparator();
                }
                if(line.contains("<s ")){
                    linec++;
                }
            }
            reader.close();
        }*/
        this.trainSet+="</data>";
        testSet+="</data>";
        testSetPinyin+="<data>";
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File("train")));
        /*writer.write(testSet);
        writer.close();
        writer=new BufferedWriter(new FileWriter(new File("test")));
        writer.write(trainSet);
        writer.close();
        writer=new BufferedWriter(new FileWriter(new File("train2")));
        writer.write(new CNXMLToSet().convert(testSet));
        writer.close();
        writer=new BufferedWriter(new FileWriter(new File("test2")));
        writer.write(new CNXMLToSet().convert(trainSet));
        writer.close();*/

        linec=0;
        for(File fil:array2){
            System.out.println("Pinyin: "+fil.getAbsolutePath());
            BufferedReader reader=new BufferedReader(new FileReader(fil));
            while((line=reader.readLine())!=null){
                if(line.contains("<s ") && chosenkeys.contains(linec) && linec>border){
                    border=linec+this.fileToAmount.get(linec);
                }else if(line.contains("<s ") && linec<border){
                    if(!line.trim().endsWith("</s>")){
                        trainsetpinyin+=line+"</s>"+System.lineSeparator();
                    }else{
                        trainsetpinyin+=line+System.lineSeparator();
                    }
                }else if(line.contains("<s ")){
                    testSetPinyin+=line+System.lineSeparator();
                }
                if(line.contains("<s ")){
                    linec++;
                }
            }
            reader.close();
        }
        testSetPinyin+="</data>";
        trainsetpinyin+="</data>";
        //trainsetpinyin=new CNXMLToSet().convert(trainsetpinyin);

        writer=new BufferedWriter(new FileWriter(new File("trainpinyin")));
        writer.write(testSetPinyin);
        writer.close();
        writer=new BufferedWriter(new FileWriter(new File("testpinyin")));
        writer.write(trainsetpinyin);
        writer.close();

        /*if(randomm){
            this.exportTestSet(TestMethod.RANDOMTEXTPERCENTAGE,trainsetpinyin,corpusfile);
            this.exportTestSetCunei(TestMethod.RANDOMTEXTPERCENTAGE,this.trainSet,corpusfile,false);
        }else {
            this.exportTestSet(TestMethod.TEXTPERCENTAGE,trainsetpinyin,corpusfile);
            this.exportTestSetCunei(TestMethod.TEXTPERCENTAGE,this.testSet,corpusfile,false);
        }*/

    }

    @Override
    public String transliterationToText(final String text, final Integer duplicator, final DictHandling dicthandler,final Boolean countmatches,final Boolean segmented) {
        return null;
    }
}

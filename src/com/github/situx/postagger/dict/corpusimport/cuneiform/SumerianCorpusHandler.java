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

package com.github.situx.postagger.dict.corpusimport.cuneiform;

import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import com.github.situx.postagger.dict.dicthandler.cuneiform.HittiteDictHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TestMethod;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.cuneiform.SumerianChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.dicthandler.cuneiform.SumerianDictHandler;
import com.github.situx.postagger.dict.pos.cuneiform.SumerianPOSTagger;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.util.Files;
import com.github.situx.postagger.util.enums.util.Tags;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * Created by timo on 30.07.14.
 * SumerianCorpusHandler
 */
public class SumerianCorpusHandler extends CuneiCorpusHandler {
    /**
     * Constructor for this class.
     * @param stopchars the stopchars to consider
     */
    public SumerianCorpusHandler(List<String> stopchars){
        super(stopchars);
    }

    @Override
    public void addTranslations(final String file, final TestMethod testMethod1) throws ParserConfigurationException, SAXException, IOException {

    }

    @Override
    public String cleanWordString(String word, final boolean reformat,boolean lowercase) {
        word=word.replace("<", "").replace(">","").replace("[","").replace("]","").replace("|","");
        word=word.replace("_", "").replace(";","").replace("+","").replace("?","").replace("!","").replace("...","");
        word=word.replace("#","").replace("/","").replace("'","").replace("\"","").replace("“","").replace("*","").replace("@","").replace("$","").replace("%","");
        if(!reformat)
            return word.replace("-","").replace("{","").replace("}", "").replace("(", "").replace(")","");
        word=word.replace("}","-").replace("{","-");
        word=word.replace(")","-").replace("(","-");
        word=word.replace(".","-");
        word=word.replace("--","-");
        if(word.indexOf("-")==0){
            word=word.substring(1);
        }
        if(!word.isEmpty() && word.lastIndexOf("-")==word.length()-1){
            word=word.substring(0,word.length()-1);
        }
        return word;
    }

    @Override
    public String corpusToReformatted(final String text) {
        return null;
    }

    @Override
    public void enrichExistingCorpus(final String filepath, final DictHandling dicthandler) throws IOException {

    }

    /**
     * Generates an Sumerian corpusimport out of the source file.
     * @param filepath the filepath to the source file
     * @param wholecorpus
     * @throws IOException on error
     */
    public DictHandling generateCorpusDictionaryFromFile(final List<String> filepath,final String signpath,final String filename,final boolean wholecorpus,final boolean corpusstr,final TestMethod testMethod) throws IOException, ParserConfigurationException, SAXException {
        System.out.println("Filepath "+filepath);
        final HittiteDictHandler dicthandler=new HittiteDictHandler(this.stopchars);
        if(signpath!=null){
            dicthandler.parseDictFile(new File(signpath));
        }
        dicthandler.setCharType(CharTypes.SUMERIAN);
        double matches=0,nomatches=0;
        Map<String,Integer> nomatchesmap=new TreeMap<>();
        this.randomGenerator=new Random();
        this.corpusReader =new BufferedReader(new FileReader(new File(Files.SOURCEDIR+filepath.get(0))));
        this.cuneiSegmentExport =new BufferedWriter(new FileWriter(new File(Files.TESTDATADIR.toString()+ Files.CORPUSOUT.toString())));
        this.cuneiWOSegmentExport =new BufferedWriter(new FileWriter(new File(Files.TESTDATADIR.toString()+ Files.CORPUSOUT2.toString())));
        this.reformattedTranslitWriter =new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR+filepath.get(0).substring(0,filepath.lastIndexOf('.'))+Files.REFORMATTED)));
        this.reformattedBoundaryWriter =new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString()+Files.BOUNDARYDIR.toString()+filepath.get(0).substring(0,filepath.lastIndexOf('.'))+Files.BOUNDARIES.toString())));
        this.reformattedCuneiWriter =new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString()+Files.CUNEI_SEGMENTEDDIR.toString()+filepath.get(0).substring(0, filepath.lastIndexOf('.')))));

        this.normalizedTestDataWriter=new BufferedWriter(new FileWriter(new File(Files.TESTDATA.toString())));
        String line,cuneiword="",modword="";
        StringBuffer boundaryBuffer=new StringBuffer();
        SumerianChar lastchar,lastlastchar;
        String[] words,chars;
        boolean notakk=false,nocunei,dictOrTest=true;
        System.out.println("CREATE CORPUS");
        while((line=this.corpusReader.readLine())!=null){
            ((CuneiDictHandler)dicthandler).newLine();
            lastchar=new SumerianChar(" ");
            if(line.isEmpty()){
                continue;
            }
            line=line.toLowerCase();
            if(line.substring(0,1).equals("#") && !line.contains(Tags.SUM.toString())){
                dictOrTest = !(this.randomGenerator.nextInt(10) > 7 && !wholecorpus);
                notakk=true;
            }else if(line.substring(0,1).equals("#") && line.contains(Tags.SUM.toString())){
                notakk=false;
            }
            if(!notakk && !line.isEmpty() && line.substring(0,1).matches("[0-9]")){
                line=line.substring(line.indexOf('.')+1);
                words=line.split(" ");
                for(String word:words){
                    //System.out.println("Word: "+word);
                    modword="";
                    //System.out.println("Word: "+word);
                    cuneiword="";
                    word=word.replaceAll(" ","");
                    if(word.equals("{ }") || word.equals("{}") || word.equals("x") || word.equals(":r:") || word.equals("...")){
                        continue;
                    }
                    if(word.startsWith("{}")){
                        word=word.substring(word.indexOf("}")+1);
                    }
                    word=word.replaceAll("[0-9]+\\{","{");
                    word=word.replaceAll("[0-9]+\\(","(");
                    if(word.startsWith("[0-9](")){
                        word=word.substring(word.indexOf("("),word.lastIndexOf(")"));
                    }
                    word=word.replace("x","");
                    if(!word.isEmpty()){
                        String temp=this.cleanWordString(word,true,true);
                        if(!temp.isEmpty() && !temp.equals("x") && !temp.equals("r")){
                            if(dictOrTest)
                                this.reformattedTranslitWriter.write("["+this.cleanWordString(word,true,true)+"] ");
                            else
                                this.normalizedTestDataWriter.write("["+this.cleanWordString(word,true,true)+"] ");
                        }
                    }
                    if(word.contains("{") && word.contains("}") ){
                        if(word.indexOf("{")>word.indexOf("}")){
                            word.replace("}","");
                            break;
                        }
                        //System.out.println(word);
                        //System.out.println(word.substring(word.indexOf("{") + 1, word.indexOf("}")));

                        LangChar chara=dicthandler.translitToChar(this.cleanWordString(word.substring(word.indexOf("{") + 1, word.indexOf("}")), true,true));
                        //System.out.println(chara);
                        SumerianChar akkad;
                        if(chara==null){
                            akkad=new SumerianChar(this.cleanWordString(word.substring(word.indexOf("{")+1,word.indexOf("}")),true,true));
                            //dicthandler.addWord(akkad);
                        }
                        else{
                            akkad=new SumerianChar(chara.getCharacter());
                        }
                        akkad.addTransliteration(new Transliteration(word.substring(word.indexOf("{") + 1, word.indexOf("}")), TranscriptionMethods.translitTotranscript(word.substring(word.indexOf("{") + 1, word.indexOf("}"))),true));
                        akkad.setDeterminative(true);
                        dicthandler.addFollowingWord(lastchar.getCharacter(),akkad.getCharacter());
                        dicthandler.addWord(akkad, CharTypes.SUMERIAN);
                        //lastchar=akkad;
                        //System.out.println("WRITE: "+akkad.getCharacter());
                        if(!akkad.getCharacter().equals("")){
                            LangChar tempcharr;
                            if((tempcharr=dicthandler.matchChar(word.substring(word.indexOf("{") + 1, word.indexOf("}"))))!=null && !tempcharr.getTransliterationSet().isEmpty()){
                                this.cuneiSegmentExport.write(dicthandler.matchChar(word.substring(word.indexOf("{") + 1, word.indexOf("}"))).getTransliterationSet().iterator().next().toString());
                                this.cuneiWOSegmentExport.write(dicthandler.matchChar(word.substring(word.indexOf("{") + 1, word.indexOf("}"))).getTransliterationSet().iterator().next().toString()+";");
                            }
                        }else{
                            //System.out.println(word);
                        }
                        //System.out.println("Found Determiner: "+word.substring(word.indexOf("{")+1,word.indexOf("}")));
                        word=word.replace("{","-").replace("}","-");
                        //System.out.println(word);
                    }
                    chars=word.split("-|_|\\.");
                    nocunei=false;
                    for(String character:chars){
                        //System.out.println("Curchar: "+character);
                        if("-".equals(character) || "x".equals(character))
                            continue;
                        //System.out.println("CURCHAR: "+character);
                        character=cleanWordString(character,false,true);
                        //System.out.println("Char: "+character+"\nMatch: "+dicthandler.translitToChar(character, 0));
                        if(dicthandler.translitToChar(character)!=null) {
                            //System.out.println(character+" -> "+dicthandler.translitToChar(character));
                            matches++;
                            cuneiword+=dicthandler.translitToChar(character).getCharacter();
                            this.cuneiSegmentExport.write(dicthandler.translitToChar(character).getCharacter());
                            this.reformattedCuneiWriter.write(dicthandler.translitToChar(character).getCharacter());
                            this.cuneiWOSegmentExport.write(dicthandler.translitToChar(character)+";");
                            boundaryBuffer.append("0,");
                            modword+="-"+character;
                        }
                        else if(dicthandler.translitToChar(character)==null && !character.isEmpty()){
                            //System.out.println("NO MATCH: "+character);
                            nomatches++;
                            boundaryBuffer.append("0,");
                            if(nomatchesmap.get(character)==null){
                                nomatchesmap.put(character,1);
                            }else{
                                nomatchesmap.put(character,nomatchesmap.get(character)+1);
                            }
                            //System.out.println("MODWORD: "+modword);
                            nocunei=true;
                        }

                    }
                    if(boundaryBuffer.length()>1) {
                        boundaryBuffer = new StringBuffer(boundaryBuffer.substring(0, boundaryBuffer.length() - 2) + "1,");
                        this.reformattedCuneiWriter.write(" ");

                    }

                    if(!nocunei && !modword.isEmpty()){
                        SumerianChar akkad=new SumerianChar(cuneiword);
                        akkad.addTransliteration(new Transliteration(modword.substring(1),TranscriptionMethods.translitTotranscript(modword.substring(1)),true));

                        //System.out.println("WRITE: "+akkad.getCharacter());
                        dicthandler.addFollowingWord(lastchar.getCharacter(),akkad.getCharacter());
                        dicthandler.addWord(akkad,CharTypes.SUMERIAN);
                        lastlastchar=lastchar;
                        lastchar=akkad;
                    }
                }
                this.reformattedBoundaryWriter.write(boundaryBuffer.toString());
                boundaryBuffer.delete(0,boundaryBuffer.length());
                this.cuneiSegmentExport.write("\n");
                this.cuneiWOSegmentExport.write("\n");
                this.reformattedTranslitWriter.write("\n");
                this.reformattedBoundaryWriter.write("\n");
                this.reformattedCuneiWriter.write("\n");
            }
        }
        this.cuneiSegmentExport.close();
        this.cuneiWOSegmentExport.close();
        this.reformattedCuneiWriter.close();
        this.normalizedTestDataWriter.close();
        this.reformattedBoundaryWriter.close();
        this.reformattedTranslitWriter.close();
        dicthandler.calculateRelativeWordOccurances(dicthandler.getAmountOfWordsInCorpus());
        dicthandler.calculateRelativeCharOccurances(dicthandler.getAmountOfWordsInCorpus());
        System.out.println("Translit To Cunei Matches: "+matches);
        System.out.println("Translit To Cunei Fails: "+nomatches);
        System.out.println("Total % "+(matches+nomatches));
        System.out.println("% "+(matches/(matches+nomatches)));
        System.out.println("No matches list: "+nomatchesmap+"\nSize: "+nomatchesmap.keySet().size());
        return dicthandler;
    }

    @Override
    public POSTagger getPOSTagger(Boolean newPosTagger) {
        if(this.posTagger==null || newPosTagger){
            this.posTagger=new SumerianPOSTagger();
        }
        return this.posTagger;
    }

    @Override
    public DictHandling getUtilDictHandler() {
        if(this.utilDictHandler==null){
            this.utilDictHandler=new SumerianDictHandler(new LinkedList<>());
            try {
                this.utilDictHandler.importMappingFromXML("dict/sux_map.xml");
                this.utilDictHandler.importDictFromXML("dict/sux_dict.xml");
                System.out.println("GetUtilDictHandler");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            }
        }
        return this.utilDictHandler;
    }
}

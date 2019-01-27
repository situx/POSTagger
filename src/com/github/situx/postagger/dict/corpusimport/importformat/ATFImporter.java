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

package com.github.situx.postagger.dict.corpusimport.importformat;

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.cuneiform.AkkadChar;
import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.chars.cuneiform.HittiteChar;
import com.github.situx.postagger.dict.corpusimport.cuneiform.CuneiCorpusHandler;
import com.github.situx.postagger.dict.corpusimport.cuneiform.CuneiTablet;
import com.github.situx.postagger.dict.dicthandler.cuneiform.AkkadDictHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.utils.Following;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.XSD;
import com.github.situx.postagger.dict.DictWebCrawler;
import com.github.situx.postagger.dict.chars.cuneiform.SumerianChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

/**
 * Created by timo on 08.09.14.
 * Importer for CDLI ATF.
 */
public class ATFImporter extends CuneiCorpusHandler implements FileFormatImporter {

    /**
     * Constructor for this class.
     * @param stopchars language specific stopchars
     * @param corpusReader the reader for the atf file
     * @param reformattedTranslitWriter writer for transliterations
     * @param reformattedBoundaryWriter writer for boundaries
     * @param reformattedCuneiWriter writer for cuneiform segmented text
     * @param reformattedUsegCuneiWriter writer for cuneiform unsegmented text
     */
    public ATFImporter(final List<String> stopchars,final BufferedReader corpusReader,
                       final BufferedWriter reformattedTranslitWriter,final BufferedWriter reformattedBoundaryWriter,
                       final BufferedWriter reformattedCuneiWriter, final BufferedWriter reformattedUsegCuneiWriter
                        ,final Map<String,Integer> nomatchesmap) {
        super(stopchars);
        this.corpusReader=corpusReader;
        this.reformattedTranslitWriter=reformattedTranslitWriter;
        this.reformattedUSegCuneiWriter=reformattedUsegCuneiWriter;
        this.reformattedCuneiWriter=reformattedCuneiWriter;
        this.reformattedBoundaryWriter=reformattedBoundaryWriter;
        this.nomatchesmap=nomatchesmap;
    }

    public ATFImporter(){
        super(new LinkedList<String>());
    }

    public static String cleanWordStringForOntology(String word){
        word = word.replace("<", "").replace(">", "").replace("[", "").replace("]", "").replace("|", "");
        word = word.replace("_", "").replace(";", "").replace("+", "").replace("?", "").replace("!", "").replace("...", "");
        word = word.replace("#", "").replace("/", "").replace("'", "").replace("\"", "").replace("“", "").replace("*", "").replace("@", "").replace("$", "").replace("%", "");
        return word.replace("{", "").replace("}", "");//.replace("(", "").replace(")", "");
    }


    /**
     * Gets the char in a char type need for processing
     * @param charTypes the current chartype
     * @return the needed char
     */
    public LangChar getChar(final CharTypes charTypes){
         switch (charTypes){
             case AKKADIAN: return new AkkadChar("");
             case SUMERIAN: return new SumerianChar("");
             case HITTITE: return new HittiteChar("");
             default: return new AkkadChar("");
         }
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
     * Importere for akkadian.
     * @param dicthandler the dicthandler to usw
     * @param charTypes the chartype to use
     * @throws IOException on error
     */
    public void importAkkadian(DictHandling dicthandler,CharTypes charTypes) throws IOException {
        String line, cuneiword, modword, cuneiline;
        StringBuilder boundaryBuffer = new StringBuilder();
        String[] words, chars;
        boolean notakk = false, nocunei, sumerogram, logogram = false;
        System.out.println("CREATE CORPUS");
        while ((line = this.corpusReader.readLine()) != null) {
            ((AkkadDictHandler) (dicthandler)).newLine();
            if (line.isEmpty()) {
                continue;
            }
            if (line.substring(0, 1).equals("#") && !line.contains(Tags.AKK.toString())) {
                notakk = true;
            } else if (line.substring(0, 1).equals("#") && line.contains(Tags.AKK.toString())) {
                notakk = false;
            }
            if (!notakk && !line.isEmpty() && line.substring(0, 1).matches("[0-9]")) {

                line = line.substring(line.indexOf('.') + 1);
                words = line.split(" ");
                for (String word : words) {

                    word = word.toLowerCase();
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.replaceAll(" ", "");
                    if (word.equals("{ }") || word.equals("{}") || word.equals("x") || word.equals(":r:") || word.equals("...")) {
                        continue;
                    }
                    //System.out.println("Word: "+word);
                    modword = "";
                    //System.out.println("Word: "+word);
                    cuneiword = "";

                    if (word.startsWith("{}")) {
                        word = word.substring(word.indexOf("}") + 1);
                    }
                    Integer isNumber = 0;
                    boolean determinative = false;

                    /*if (word.contains("(") && word.contains(")") && word.indexOf("(") < word.indexOf(")") && word.matches("^[0-9]\\(.*")) {
                        isNumber = Integer.valueOf(word.split("\\(")[0]) - 1;
                        System.out.println("IsNumber: " + isNumber);
                    }*/
                    if (word.contains("{") && word.contains("}") && word.indexOf("{") > word.indexOf("}")) {
                        determinative = true;
                    }
                    if (word.contains("_")) {
                        logogram = true;
                    }
                    word = word.replaceAll("[0-9]+\\{", "{");
                    //word=word.replaceAll("[0-9]+\\(","(");
                    /*if (word.startsWith("[0-9](")) {
                        word = word.substring(word.indexOf("("), word.lastIndexOf(")"));
                    }*/
                    String numberchar="";
                    word = word.replace("x", "");
                    if (!word.isEmpty()) {
                        String cleaned = this.cleanWordString(word, true,true);
                        /*if (!cleaned.isEmpty() && !cleaned.equals("x") && !cleaned.equals("r")) {
                            this.reformattedTranslitWriter.write("[");
                            /*if (isNumber > 1) {
                                this.reformattedTranslitWriter.write("(");
                            }*/
                            numberchar=cleaned;
                          /*  if(isNumber>1){
                            for (int i = 0; i < (isNumber + 1); i++) {

                                if (i < isNumber) {
                                    this.reformattedBoundaryWriter.write("0,");
                                    this.reformattedTranslitWriter.write(cleaned+"-");
                                }
                            }
                            }
                            this.reformattedTranslitWriter.write(cleaned);
                            /*if (isNumber > 1) {
                                this.reformattedTranslitWriter.write(cleaned+")");
                                //this.reformattedBoundaryWriter.write("1,");
                            }*/
                          //  this.reformattedTranslitWriter.write("] ");
                        //}
                    }
                    AkkadChar akkad;
                    if (word.contains("{") && word.contains("}")) {
                        if (word.indexOf("{") > word.indexOf("}")) {
                            word = word.replace("}", "");
                            break;
                        }
                        //System.out.println("Word: "+word);
                        //System.out.println(word.substring(word.indexOf("{") + 1, word.indexOf("}")));

                        LangChar chara = dicthandler.translitToChar(this.cleanWordString(word.substring(word.indexOf("{") + 1, word.indexOf("}")), true,false));
                        //System.out.println(chara);
                        if (chara == null) {
                            akkad = new AkkadChar(this.cleanWordString(word.substring(word.indexOf("{") + 1, word.indexOf("}")), true,true));
                            //dicthandler.addChar(akkad);
                        } else {
                            akkad = new AkkadChar(chara.getCharacter());
                        }
                        // System.out.println("Sumerogram: " + word.substring(word.indexOf("{") + 1, word.indexOf("}")));
//System.out.println("No Sumerogram: "+word.substring(word.indexOf("{") + 1, word.indexOf("}")));
                        sumerogram = StringUtils.isAllUpperCase(word.substring(word.indexOf("{") + 1, word.indexOf("}")));
                        akkad.addTransliteration(new Transliteration(word.substring(word.indexOf("{") + 1, word.indexOf("}")).toLowerCase(), TranscriptionMethods.translitTotranscript(word.substring(word.indexOf("{") + 1, word.indexOf("}"))).toLowerCase(), true));
                        akkad.setDeterminative(determinative);
                        akkad.setIsNumberChar(isNumber > 0);
                        akkad.setSumerogram(sumerogram);
                        akkad.setLogograph(logogram);
                        dicthandler.addWord(akkad, CharTypes.AKKADIAN);
                        if (lastlastword != null && lastword != null) {
                            dicthandler.addFollowingWord(lastword.getCharacter(), akkad.getCharacter(), lastlastword.getCharacter());
                        }
                        if (lastword != null) {
                            akkad.addPrecedingWord(lastword.getCharacter());
                        }
                        lastlastword = lastword;
                        lastword = akkad;
                        //System.out.println("WRITE: "+akkad.getCharacter());
                        if (!akkad.getCharacter().equals("")) {
                            LangChar tempcharr;
                            if ((tempcharr = dicthandler.matchChar(word.substring(word.indexOf("{") + 1, word.indexOf("}")))) != null && !tempcharr.getTransliterationSet().isEmpty()) {
                                //this.cuneiSegmentExport.write(dicthandler.matchChar(word.substring(word.indexOf("{") + 1, word.indexOf("}"))).getTransliterationSet().iterator().next().toString());
                                //this.cuneiWOSegmentExport.write(dicthandler.matchChar(word.substring(word.indexOf("{") + 1, word.indexOf("}"))).getTransliterationSet().iterator().next().toString() + ";");
                                boundaryBuffer.append("0,");
                                this.reformattedTranslitWriter.write(word.substring(word.indexOf("{") + 1, word.indexOf("}")));
                                this.reformattedCuneiWriter.write(dicthandler.matchChar(word.substring(word.indexOf("{") + 1, word.indexOf("}"))).getTransliterationSet().iterator().next().toString());
                                this.reformattedUSegCuneiWriter.write(dicthandler.matchChar(word.substring(word.indexOf("{") + 1, word.indexOf("}"))).getTransliterationSet().iterator().next().toString());
                            }
                        } else {
                            //System.out.println(word);
                        }
                        //System.out.println("Found Determiner: "+word.substring(word.indexOf("{")+1,word.indexOf("}")));
                        word = word.replace("{", "-").replace("}", "-");
                        //System.out.println(word);
                    }
                    //System.out.println("Origintext: "+this.cleanWordString(word,true));
                    //System.out.println("CharsToBoundaries: "+this.transliterationToBoundaries(this.cleanWordString(word,true),CharTypes.AKKADIAN));
                    //this.reformattedBoundaryWriter.write(this.transliterationToBoundaries(this.cleanWordString(word,true),CharTypes.AKKADIAN));
                    //System.out.println("TransliterationToText: "+this.transliterationToText(this.cleanWordString(word,true),isNumber,dicthandler));
                    chars = word.split("-|_|\\.");
                    int[] isLogogram = new int[word.length() + 1];
                    int j = 0;
                    for (int i = 1; i < word.length(); i++) {
                        if (word.substring(i, i + 1).equals("-")) {
                            isLogogram[j++] = 0;
                        } else if (word.substring(i, i + 1).equals("_")) {
                            //System.out.println("Logogram: "+word.substring(i));
                            isLogogram[j++] = 1;
                        }
                    }
                    nocunei = false;
                    int i = 0;
                    StringBuilder writetranslit=new StringBuilder();
                    writetranslit.append("[");
                    for (String character : chars) {
                        //System.out.println("Curchar: "+character);
                        if ("-".equals(character) || "x".equals(character))
                            continue;
                        System.out.println("CURCHAR: "+character);
                        isNumber=0;
                        if (character.contains("(") && character.contains(")") && character.indexOf("(") < character.indexOf(")") && character.matches("^[0-9]\\(.*")) {
                            isNumber = Integer.valueOf(character.split("\\(")[0]) - 1;
                            System.out.println("IsNumber: " + isNumber);
                            character=character.substring(1,character.length());
                        }
                        character = cleanWordString(character, false,true);
                        //System.out.println("Char: "+character+"\nMatch: "+dicthandler.translitToChar(character, 0));
                        if (dicthandler.translitToChar(character) != null) {
                            if (character.matches("[0-9]?[\\(]?disz[\\)]?") || character.matches("[0-9]?[\\(]?u[\\)]?")) {
                                ((CuneiChar) dicthandler.translitToChar(character)).setIsNumberChar(true);
                            }
                            //System.out.println(character+" -> "+dicthandler.translitToChar(character));
                            matches++;
                            cuneiword += dicthandler.translitToChar(character).getCharacter();
                            //this.cuneiSegmentExport.write(dicthandler.translitToChar(character).getCharacter());

                            if(isNumber>0){
                                for(int k=0;k<=isNumber;k++){
                                    boundaryBuffer.append("0,");
                                    writetranslit.append(character);
                                    writetranslit.append("-");
                                    this.reformattedCuneiWriter.write(dicthandler.translitToChar(character).getCharacter());
                                    this.reformattedUSegCuneiWriter.write(dicthandler.translitToChar(character).getCharacter());
                                }
                            }else{
                                    boundaryBuffer.append("0,");
                                    writetranslit.append(character);
                                    writetranslit.append("-");
                                   this.reformattedCuneiWriter.write(dicthandler.translitToChar(character).getCharacter());
                                   this.reformattedUSegCuneiWriter.write(dicthandler.translitToChar(character).getCharacter());
                            }

                            //this.cuneiWOSegmentExport.write(dicthandler.translitToChar(character) + ";");
                            modword += "-" + character;
                            if (isLogogram[i] == 1) {
                                //System.out.println("Logogram: " + character);
                                ((CuneiChar) dicthandler.translitToChar(character)).setLogograph(true);
                            }

                        } else if (dicthandler.translitToChar(character) == null && !character.isEmpty()) {
                            //System.out.println("NO MATCH: "+character);
                            nomatches++;
                            boundaryBuffer.append("0,");
                            if (nomatchesmap.get(character) == null) {
                                nomatchesmap.put(character, 1);
                            } else {
                                nomatchesmap.put(character, nomatchesmap.get(character) + 1);
                            }
                            //System.out.println("MODWORD: "+modword);
                            nocunei = true;
                        }
                        i++;

                    }
                    if (boundaryBuffer.length() > 1) {
                        boundaryBuffer = new StringBuilder(boundaryBuffer.substring(0, boundaryBuffer.length() - 2) + "1,");
                        //System.out.println("BoundaryBuffer: "+boundaryBuffer.toString());
                        this.reformattedCuneiWriter.write(" ");
                        this.reformattedTranslitWriter.write(writetranslit.toString().equals("[")?" ":writetranslit.toString().substring(0, writetranslit.length() - 1)+"] ");

                    }
                    if (!nocunei && !modword.isEmpty()) {
                        akkad = new AkkadChar(cuneiword);
                        //System.out.println("Sumerogram: "+modword.substring(1));
//System.out.println("No Sumerogram: "+modword.substring(1));
                        sumerogram = StringUtils.isAllUpperCase(modword.substring(1));
                        akkad.setIsNumberChar(isNumber > 0);
                        akkad.setDeterminative(determinative);
                        akkad.setSumerogram(sumerogram);
                        akkad.setLogograph(logogram);
                        akkad.addTransliteration(new Transliteration(modword.substring(1).toLowerCase(), TranscriptionMethods.translitTotranscript(modword.substring(1).toLowerCase()), true));
                        //System.out.println("WRITE: "+akkad.getCharacter());
                        Following following = new Following();
                        following.setIsStopChar(false);
                        if (lastlastword != null && lastword != null) {
                            dicthandler.addFollowingWord(lastword.getCharacter(), akkad.getCharacter(), lastlastword.getCharacter());
                        }
                        if (lastword != null) {
                            akkad.addPrecedingWord(lastword.getCharacter(), true);
                        }
                        dicthandler.addWord(akkad, CharTypes.AKKADIAN);
                        lastlastword = lastword;
                        lastword = akkad;
                    }
                }
                this.reformattedBoundaryWriter.write(boundaryBuffer.toString());
                boundaryBuffer.delete(0, boundaryBuffer.length());
                //this.cuneiSegmentExport.write(System.lineSeparator());
                //this.cuneiWOSegmentExport.write(System.lineSeparator());
                this.reformattedTranslitWriter.write(System.lineSeparator());
                this.reformattedBoundaryWriter.write(System.lineSeparator());
                this.reformattedCuneiWriter.write(System.lineSeparator());
                this.reformattedUSegCuneiWriter.write(System.lineSeparator());
            }
            cuneiline = this.transliterationToText(line, 0, dicthandler, false, false);
            dicthandler.getNGramStats().generateNGramsFromLine(CharTypes.AKKADIAN, cuneiline, cuneiline.length());
        }
   }

    public static OntModel analyzeTablets(String filepath) throws IOException {
        OntModel model= ModelFactory.createOntologyModel();
        Map<String,String> knownwords=new HashMap<String,String>();
        BufferedReader reader=new BufferedReader(new FileReader(new File(filepath)));
        DictWebCrawler crawler=new DictWebCrawler();
        String line;
        CuneiTablet tablet;
        String tabletID="";
        String language="";
        CharTypes curchartype=CharTypes.AKKADIAN;
        StringBuilder tabletText=new StringBuilder();
        Boolean begintablet=false;
        Individual curword;
        OntClass tabletClass=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Tablet");
        OntClass epochClass=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Epoch");
        DatatypeProperty ucode=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform/character#unicodepoint");
        DatatypeProperty abzl=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform/character#abzl");
        DatatypeProperty mezl=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform/character#mezl");
        DatatypeProperty hethzl=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform/character#hethzl");
        DatatypeProperty lha=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform/character#lha");
        DatatypeProperty got=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform/character#gottstein");
        DatatypeProperty transliteration=model.createDatatypeProperty("http://www.isocat.org/rest/dc/4146#transliteration");
        DatatypeProperty sum=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform/character#name");
        ObjectProperty epochprop=model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#hasEpoch");
        ObjectProperty lemonEntryProp=model.createObjectProperty("http://lemon-model.net/lemon#entry");
        OntClass lemonLexicalSense=model.createClass("http://lemon-model.net/lemon#lexicalSense");
        epochprop.addDomain(tabletClass);
        epochprop.addRange(XSD.xstring);
        OntClass lemonForm=model.createClass("http://lemon-model.net/lemon#Form");
        ObjectProperty isSenseOf=model.createObjectProperty("http://lemon-model.net/lemon#isSenseOf");
        ObjectProperty lemonReference=model.createObjectProperty("http://lemon-model.net/lemon#representation");
        ObjectProperty hasSense=model.createObjectProperty("http://lemon-model.net/lemon#sense");
        hasSense.addDomain(tabletClass);
        hasSense.addRange(lemonLexicalSense);
        OntClass lemonPart=model.createClass("http://lemon-model.net/lemon#Part");
        OntClass lemonLexicon=model.createClass("http://lemon-model.net/lemon#Lexicon");
        DictHandling dictHandler=CharTypes.AKKADIAN.getCorpusHandlerAPI().getUtilDictHandler();
        Individual lexicon=lemonLexicon.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#"+CharTypes.AKKADIAN.getLocale());
        lexicon.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#language"),CharTypes.AKKADIAN.getLocale());
        for(String cunei:dictHandler.getDictMap().keySet()){
            CuneiChar cuneic= (CuneiChar) dictHandler.getDictMap().get(cunei);
            curword=lemonPart.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/character#"+cunei.replace(" ",""));
            curword.addLabel(cunei,"en");
            if(cuneic.getUnicodeCodePage()!=null && !cuneic.getUnicodeCodePage().isEmpty() && cuneic.getUnicodeCodePage().contains(" ") && cuneic.getUnicodeCodePage().contains("+"))
                curword.addLiteral(ucode,cuneic.getUnicodeCodePage().substring(cuneic.getUnicodeCodePage().indexOf('+')-1,cuneic.getUnicodeCodePage().indexOf(' ')));
            if(cuneic.getMezlNumber()!=null && !cuneic.getMezlNumber().isEmpty()) {
                curword.addLiteral(mezl, cuneic.getMezlNumber());
                curword.addProperty(epochprop,epochClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#mbab"));
            }
            if(cuneic.getaBzlNumber()!=null && !cuneic.getaBzlNumber().isEmpty()) {
                curword.addLiteral(abzl, cuneic.getaBzlNumber());
                curword.addProperty(epochprop,epochClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#oldakk"));
            }
            if(cuneic.getHethzlNumber()!=null && !cuneic.getHethzlNumber().isEmpty()) {
                curword.addLiteral(hethzl, cuneic.getHethzlNumber());
            }
            if(cuneic.getLhaNumber()!=null && !cuneic.getLhaNumber().isEmpty()) {
                curword.addLiteral(lha, cuneic.getLhaNumber());
            }
            if(cuneic.getPaintInformation()!=null)
                curword.addLiteral(got,cuneic.getPaintInformation());
            if(cuneic.getCharName()!=null)
                curword.addLiteral(sum,cuneic.getCharName());
            if(cuneic.getConceptURI()!=null && !cuneic.getConceptURI().isEmpty()) {
                System.out.println("ConceptURI: " + cuneic.getConceptURI().toString());
                Individual cursense;
                if (cuneic.getConceptURI().contains("#")) {
                    cursense = lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#" + cuneic.getConceptURI().substring(cuneic.getConceptURI().lastIndexOf('#')+1).replace(" ","") );
                } else if (cuneic.getConceptURI().contains("/")) {
                    cursense = lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#" + cuneic.getConceptURI().substring(cuneic.getConceptURI().lastIndexOf('/') + 1).replace(" ",""));
                } else {
                    cursense = lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#" + UUID.randomUUID().toString());
                }
                curword.addProperty(hasSense, cursense);
                cursense.addProperty(isSenseOf, curword);
                if(!cuneic.getConceptURI().contains("http"))
                    cursense.addProperty(lemonReference, model.createOntResource("http://acoli.uni-frankfurt.de/ontology/cuneiform/sense#"+cuneic.getConceptURI()));
                else
                    cursense.addProperty(lemonReference, model.createOntResource(cuneic.getConceptURI()));
            }
            for(Transliteration translit:cuneic.getTransliterations().keySet()){
                curword.addProperty(transliteration,translit.getTransliteration());
            }
        }
        while((line=reader.readLine())!=null){
            if(line.trim().isEmpty() || line.trim().startsWith("$") || line.trim().startsWith("#"))
                continue;
            if(line.startsWith("&") && !begintablet){
                begintablet=true;
                tabletID=line.substring(line.indexOf('&')+1,line.indexOf('=')-1);
            }else if(line.startsWith("&") && begintablet){
                tablet=new CuneiTablet(tabletID,tabletText.toString(),curchartype);
                crawler.crawlCDLIForTabletMetadata(model,tabletID.substring(1));
                        tabletText=new StringBuilder();
                model=tablet.tabletToOntModel(model,knownwords);
                tabletID=line.substring(line.indexOf('&')+1,line.indexOf('=')-1);
            }else if(line.startsWith("#atf: lang")){
                language=line.substring(line.indexOf("lang ")+6);
                if(language.contains("akk")){
                    curchartype=CharTypes.AKKADIAN;
                }else if(language.contains("sux")){
                    curchartype=CharTypes.SUMERIAN;
                }else if(language.contains("hit")){
                    curchartype=CharTypes.HITTITE;
                }
                System.out.println("Language: "+language+" - "+curchartype);
            }else if(!line.startsWith("@")){
                line=line.substring(line.indexOf('.')+1);
                //line=new ATFImporter().cleanWordString(line,false);
                System.out.println("Cleaned line: "+line);
                tabletText.append(line);
                tabletText.append(System.lineSeparator());
            }
        }
        reader.close();
        OntClass lemonWord=model.createClass("http://lemon-model.net/lemon#Word");
        ObjectProperty lemonLexicalForm=model.createObjectProperty("http://lemon-model.net/lemon#lexicalForm");
        for(LangChar cuneiChar:dictHandler.getDictionary().values()){


            if(cuneiChar!=null) {
                Individual word=lemonWord.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+cuneiChar.getCharacter().replace(" ",""));
                lexicon.addProperty(lemonEntryProp,word);
                Individual ind=lemonForm.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+UUID.randomUUID().toString());
                word.addProperty(lemonLexicalForm,ind);
                ind.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#writtenRep"),cuneiChar.getCharacter());
                if(cuneiChar.getConceptURI()!=null && !cuneiChar.getConceptURI().isEmpty()){
                    System.out.println("ConceptURI: "+cuneiChar.getConceptURI().toString());
                    Individual cursense;
                    if(cuneiChar.getConceptURI().contains("#")){
                        cursense=lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+cuneiChar.getConceptURI().substring(cuneiChar.getConceptURI().lastIndexOf('#')+1).replace(" ",""));
                    }else if(cuneiChar.getConceptURI().contains("/")) {
                        cursense = lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#" + cuneiChar.getConceptURI().substring(cuneiChar.getConceptURI().lastIndexOf('/')+1).replace(" ",""));
                    }else{
                        cursense = lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#" + UUID.randomUUID().toString());
                    }
                    ind.addProperty(hasSense,cursense);
                    cursense.addProperty(isSenseOf,ind);
                    if(!cuneiChar.getConceptURI().contains("http"))
                        cursense.addProperty(lemonReference, model.createOntResource("http://acoli.uni-frankfurt.de/ontology/cuneiform/sense#"+cuneiChar.getConceptURI()));
                    else
                        cursense.addProperty(lemonReference, model.createOntResource(cuneiChar.getConceptURI()));
                }
            }
        }
        AkkadDictHandler handler=new AkkadDictHandler(new LinkedList<>());
        lexicon=lemonLexicon.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#"+CharTypes.SUMERIAN.getLocale());
        lexicon.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#language"),CharTypes.SUMERIAN.getLocale());
        try {
            handler.importDictFromXML("newwordssumerian2.xml");
            for(LangChar cuneiChar:handler.getDictionary().values()){

                if(cuneiChar!=null) {
                    Individual word=lemonWord.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+UUID.randomUUID().toString());
                    lexicon.addProperty(lemonEntryProp,word);
                    Individual ind=lemonForm.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+UUID.randomUUID().toString());
                    word.addProperty(lemonLexicalForm,ind);
                    ind.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#writtenRep"),cuneiChar.getCharacter());
                    if(cuneiChar.getConceptURI()!=null && !cuneiChar.getConceptURI().isEmpty()){
                        System.out.println("ConceptURI: "+cuneiChar.getConceptURI().toString());
                        Individual cursense;
                        if(cuneiChar.getConceptURI().contains("#")){
                            cursense=lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+cuneiChar.getConceptURI().substring(cuneiChar.getConceptURI().lastIndexOf('#')+1).replace(" ",""));
                        }else if(cuneiChar.getConceptURI().contains("/")) {
                            cursense = lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#" + cuneiChar.getConceptURI().substring(cuneiChar.getConceptURI().lastIndexOf('/')+1).replace(" ",""));
                        }else{
                            cursense = lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#" + UUID.randomUUID().toString());
                        }
                        ind.addProperty(hasSense,cursense);
                        cursense.addProperty(isSenseOf,ind);
                        cursense.addProperty(lemonReference,model.createOntResource(cuneiChar.getConceptURI()));
                    }
                    for(Transliteration translit:cuneiChar.getTransliterations().keySet()){
                        word.addProperty(transliteration,translit.getTransliteration());
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }


        try {
            model.write(new FileWriter(new File("outmodel.rdf")),"TTL");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return model;
    }

    public static void main(String[] args) throws IOException {
        ATFImporter.analyzeTablets("../Master/source/corpus.atf");
    }

    @Override
    public void importFromFormat(final CharTypes charType, final DictHandling dictHandler) throws IOException {
        switch(charType){
            case AKKADIAN:
            case SUMERIAN:
            case HITTITE:
                this.importAkkadian(dictHandler,charType);
                break;
            default:
        }
    }
}

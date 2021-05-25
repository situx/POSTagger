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

import com.github.situx.postagger.dict.corpusimport.importformat.ATFImporter;
import com.github.situx.postagger.dict.corpusimport.importformat.FileFormatImporter;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.dicthandler.cuneiform.AkkadDictHandler;
import com.github.situx.postagger.dict.importhandler.cuneiform.TranslationImportHandler2;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.pos.cuneiform.AkkadPOSTagger;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TestMethod;
import com.github.situx.postagger.util.enums.util.Files;
import com.github.situx.postagger.util.enums.util.Tags;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: Timo Homburg
 * Date: 06.11.13
 * Time: 14:21
 * CorpusHandler for the akkadian language processing a corpusimport.
 */
public class AkkadCorpusHandler extends CuneiCorpusHandler {

    /**
     * Constructor for this class.
     */
    public AkkadCorpusHandler(List<String> stopChars) {
        super(stopChars);

    }

    @Override
    public void addTranslations(final String file, final TestMethod testMethod1) throws ParserConfigurationException, SAXException, IOException {
        TranslationImportHandler2 importHandler = new TranslationImportHandler2(this.dictHandlers.get(testMethod1).get(0));
        javax.xml.parsers.SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(file));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        //ImportHandler imp=new ImportHandler(Options.FILLDICTIONARY,this.dictionary,this.translitToWordDict, CharTypes.AKKADIAN);
        parser.parse(in, importHandler);
    }

    @Override
    public DictHandling dictImport(final String corpus, final TestMethod testMethod, final CharTypes sourcelang,final Boolean map,final Boolean dict,final Boolean reverse, final Boolean ngram) throws IOException, SAXException, ParserConfigurationException {
        final AkkadDictHandler dictHandler = new AkkadDictHandler(this.stopchars);
        if(map)
            dictHandler.importMappingFromXML(Files.DICTDIR + sourcelang.getLocale() + Files.MAPSUFFIX);
        if(dict)
            dictHandler.importDictFromXML(Files.DICTDIR + sourcelang.getLocale() + Files.DICTSUFFIX);
        if(reverse)
            dictHandler.importReverseDictFromXML(Files.DICTDIR + sourcelang.getLocale() + Files.REVERSE + Files.DICTSUFFIX);
        if(ngram)
            dictHandler.importNGramsFromXML(Files.DICTDIR + sourcelang.getLocale() + Files.NGRAMSUFFIX);
        return dictHandler;
    }

    @Override
    public void enrichExistingCorpus(final String filepath, final DictHandling dicthandler) throws IOException {

    }

    /**
     * Generates an Akkadian corpusimport out of the source file.
     *
     * @param filepath    the filepath to the source file
     * @param wholecorpus
     * @throws IOException on error
     */
    @Override
    public DictHandling generateCorpusDictionaryFromFile(final List<String> filepath, final String signpath, final String filename, final boolean wholecorpus, final boolean corpusstr,final TestMethod testMethod) throws IOException, ParserConfigurationException, SAXException {
        final AkkadDictHandler dicthandler = new AkkadDictHandler(this.stopchars);
        if (signpath != null) {
            dicthandler.parseDictFile(new File(signpath));
        }
        dicthandler.setCharType(CharTypes.AKKADIAN);
        if (!corpusstr)
            System.out.println("Filepath " + filepath);
        matches = 0.;
        nomatches = 0.;
        nomatchesmap = new TreeMap<>();
        this.randomGenerator = new Random();
        //this.cuneiSegmentExport = new BufferedWriter(new FileWriter(new File(Files.TESTDATADIR.toString() + Files.CORPUSOUT.toString())));
        //this.cuneiWOSegmentExport = new BufferedWriter(new FileWriter(new File(Files.TESTDATADIR.toString() + Files.CORPUSOUT2.toString())));
        this.reformattedTranslitWriter = new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString()+Files.TRANSLITDIR.toString()+ testMethod.toString().toLowerCase()+File.separator+ filename)));
        this.reformattedBoundaryWriter = new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString() + Files.BOUNDARYDIR.toString()+testMethod.toString().toLowerCase()+File.separator+ filename)));
        this.reformattedCuneiWriter = new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString() + Files.CUNEI_SEGMENTEDDIR +testMethod.toString().toLowerCase()+File.separator+ filename)));
        this.reformattedUSegCuneiWriter = new BufferedWriter(new FileWriter(new File(Files.REFORMATTEDDIR.toString() + Files.CUNEIFORMDIR.toString() +testMethod.toString().toLowerCase()+File.separator+ filename)));
        this.normalizedTestDataWriter = new BufferedWriter(new FileWriter(new File(Files.TESTDATA.toString())));
        String fileext;
        for (String str : filepath) {
            if (corpusstr) {
                this.corpusReader = new BufferedReader(new StringReader(str));
                fileext=filename.substring(filename.lastIndexOf("."));
                this.importCorpus(dicthandler, fileext).importFromFormat(dicthandler.getChartype(),dicthandler);
            } else {
                File file = new File(Files.SOURCEDIR + str);
                if (file.exists() && file.isDirectory()) {
                    for (File fil : file.listFiles()) {
                        this.corpusReader = new BufferedReader(new FileReader(fil));
                        fileext=str.substring(str.lastIndexOf("."));
                        this.importCorpus(dicthandler, fileext).importFromFormat(dicthandler.getChartype(),dicthandler);
                    }
                } else if (file.exists() && !file.isDirectory()) {
                    this.corpusReader = new BufferedReader(new FileReader(file));
                    fileext=str.substring(str.lastIndexOf("."));
                    this.importCorpus(dicthandler, fileext).importFromFormat(dicthandler.getChartype(),dicthandler);
                }

            }
        }
        //this.cuneiSegmentExport.close();
        //this.cuneiWOSegmentExport.close();
        this.reformattedCuneiWriter.close();
        this.normalizedTestDataWriter.close();
        this.reformattedBoundaryWriter.close();
        this.reformattedTranslitWriter.close();
        this.reformattedUSegCuneiWriter.close();
        dicthandler.calculateRightLeftAccessorVariety();
        dicthandler.calculateRelativeWordOccurances(dicthandler.getAmountOfWordsInCorpus());
        dicthandler.calculateRelativeCharOccurances(dicthandler.getAmountOfWordsInCorpus());
        dicthandler.calculateAvgWordLength();
        System.out.println("Translit To Cunei Matches: " + matches);
        System.out.println("Translit To Cunei Fails: " + nomatches);
        System.out.println("Total % " + (matches + nomatches));
        System.out.println("% " + (matches / (matches + nomatches)));
        System.out.println("No matches list: " + nomatchesmap + "\nSize: " + nomatchesmap.keySet().size());
        return dicthandler;
    }

    @Override
    public POSTagger getPOSTagger(Boolean newPosTagger) {
        if(this.posTagger==null || newPosTagger){
            this.posTagger=new AkkadPOSTagger();
        }
        return this.posTagger;
    }

    @Override
    public DictHandling getUtilDictHandler() {
        if(this.utilDictHandler==null){
            System.out.println("Need to create new DictHandler");
            this.utilDictHandler=new AkkadDictHandler(new LinkedList<>());
            try {
                this.utilDictHandler.importMappingFromXML("dict/akk_map.xml");
                this.utilDictHandler.importDictFromXML("dict/akk_dict.xml");
                //this.utilDictHandler.parseDictFile(new File("akkad.xml"));

                System.out.println("GetUtilDictHandler");
            } catch (IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }
        }
        return this.utilDictHandler;
    }

    /**
     * According to the file extension chooses the correct importer.
     * @param dicthandler the dicthandler to choose
     * @param fileext the fileextension
     * @return the file format importer to choose
     * @throws IOException on error
     */
    public FileFormatImporter importCorpus(DictHandling dicthandler,String fileext) throws IOException {
           switch (fileext.toLowerCase()){
               case ".atf": return new ATFImporter(this.stopchars,this.corpusReader,this.reformattedTranslitWriter,this.reformattedBoundaryWriter,this.reformattedCuneiWriter,this.reformattedUSegCuneiWriter,nomatchesmap);
               default:  return new ATFImporter(this.stopchars,this.corpusReader,this.reformattedTranslitWriter,this.reformattedBoundaryWriter,this.reformattedCuneiWriter,this.reformattedUSegCuneiWriter,nomatchesmap);
           }
    }
}
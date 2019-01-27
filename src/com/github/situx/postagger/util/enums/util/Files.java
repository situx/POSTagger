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

package com.github.situx.postagger.util.enums.util;

/**
 * Enum for file names.
 * User: Timo Homburg
 * Date: 03.12.13
 * Time: 21:37
 */
public enum Files {
    /**Generated akkad dictionary file.*/
    AKKADXML("akkad2.xml"),
    /**Arff Suffix for writing arff result files.*/
    ARFFOUT("_out.arff"),
    /**Arff Suffix for writing arff files.*/
    ARFFSUFFIX(".arff"),
    /**Average word length infix.*/
    AVGWORDLENGTH("avg_wordlength"),
    /**Bigram infix.*/
    BIGRAM("bigram"),
    /**Bigram hmm infix.*/
    BIGRAMHMM("bigram_hmm"),
    /**Source corpusimport directory.*/
    BOUNDARIES("_boundaries.txt"),
    /**Corpusout input file.*/
    CORPUSOUT("corpusout.txt"),
    /**Corpusout2 input file.*/
    CORPUSOUT2("corpusout2.txt"),
    /**Reformatted corpusimport boundaries file.*/
    CORPUSBOUNDARIESREFORMATTED("corpusboundariesreformatted"),
    /**Reformatted corpusimport file.*/
    CORPUSREFORMATTED("corpusreformatted.txt"),
    /**Cuneiform result directory.*/
    CUNEIFORMDIR("cuneiform/"),
    /**Cuneiform out file.*/
    CUNEIOUT("cuneiout.txt"),
    /**Dictionary directory.*/
    DICTDIR("dicts/"),
    /**Dictionary file suffix.*/
    DICTSUFFIX("_dict.xml"),
    /**Dictionary file suffix.*/
    EVAL("_eval.txt"),
    /**Dictionary file suffix.*/
    EVALDIR("eval/"),
    /**First 20 lines of the corpusimport file.*/
    FIRST20("first20.txt"),
    /**First 20 lines without suffix.*/
    FIRST20NOSUF("first20"),
    /**hmm infix.*/
    HMM("hmm"),

    IBUS_DIR("ibus/"),

    IBUS_HEADER("ibus_header.txt"),

    IBUS_FOOTER("ibus_footer.txt"),
    /**Mallet suffix.*/
    MALLETSUFFIX(".mallet"),
    /**Mallet output suffix.*/
    MALLETOUT("_out.mallet"),
    /**Dictionary map suffix.*/
    MAPSUFFIX("_map.xml"),
    /**Maximum entropy infix.*/
    MAXENT("maxent"),
    /**Maxmatch infix.*/
    MAXMATCH("maxmatch"),
    /**Maximum probability infix.*/
    MAXPROB("maxprob"),
    /**MinMatch infix.*/
    MINMATCH("minmatch"),
    /**Model directory.*/
    MODELDIR("model/"),
    /**NaiveBayes infix.*/
    NAIVEBAYES("naivebayes"),
    /**Prefix Suffix infix.*/
    PREFSUF("prefixsuffix"),
    /**Random Segmentation infix.*/
    RANDOMSEG("randomseg"),
    /**Source corpusimport directory.*/
    REFORMATTED("_reformatted.txt"),
    /**Source corpusimport directory.*/
    REFORMATTEDDIR("reformatted/"),
    /**Result suffix.*/
    RESULT("_result.txt"),
    /**Result directory.*/
    RESULTDIR("results/"),
    /**Char segmentation infix.*/
    SEGCHAR("segchar"),
    /**Source corpusimport directory.*/
    SOURCEDIR("source/"),
    /**SVM infix.*/
    SVM("svm"),
    SYLLDIR("syll/"),
    /**Tango algorithm infix.*/
    TANGO("tango"),
    /**Testdata suffix.*/
    TESTDATA("testdata.txt"),
    /**Testdata directory.*/
    TESTDATADIR("testdata/"),
    /**Trainingdata suffix.*/
    TRAININGDATADIR("trainingdata/"),
    TRANSCRIPTDIR("transcript/"),
    /**Transliteration result directory.*/
    TRANSLITDIR("translit/"),
    TRANSLATIONDIR("translation/"),
    /**Winnow algorithm infix.*/
    WINNOW("winnow"), REVERSE("_reverse"), BOUNDARYDIR("boundary/"), MORFESSORSUFFIX("_morfessor.txt"), SCIM_DIR("scim/"), SCIM_HEADER("scim_header.txt"),SCIM_FOOTER("scim_footer.txt"), FOOTER("_footer"), HEADER("_header"), MODELSUFFIX(".model"), XMLSUFFIX(".xml");
    public static final String ANDROID_DIR = "android/";
    public static final String IME_DIR = "ime/";
    public static final String CUNEI_SEGMENTEDDIR = "cuneiform_segmented/";
    public static final String NGRAMSUFFIX = "_ngram.txt";
    public static final String ANKI_DIR = "anki/";
    public static final String POSDIR = "pos/";
    /**Filename as String.*/
    private final String value;

    /**
     * Constructor for this class.
     * @param value the String value
     */
    private Files(final String value){
        this.value=value;
    }

    /**
     * Gets the value of this enum.
     * @return the value as String
     */
    public String getValue(){
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

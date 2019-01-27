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

package com.github.situx.postagger.util.enums.methods;

import com.github.situx.postagger.methods.segmentation.dict.DictMethods;
import com.github.situx.postagger.methods.segmentation.rule.RuleMethods;
import com.github.situx.postagger.util.enums.util.Options;

/**
 * Enumeration for storing methods names for parsing.
 * User: Timo Homburg
 * Date: 03.12.13
 * Time: 22:46
 */
public enum ClassificationMethod implements MethodEnum {


    /**Average word length segmentation method.*/
    AVGWORDLEN("AVGWORDLEN","AVG",false, Options.NATIVE),

    AODE("AODE","AODE",true, Options.WEKA),
    BAYESNET("BAYESNET","BANET",true, Options.WEKA),
    /**Bigram segmentation method.*/
    BIGRAM("BIGRAM","BIG",false, Options.NATIVE),
    /**Bigram hmm segmentation method.*/
    BIGRAMHMM("BIGRAMHMM","BIGHMM",false, Options.NATIVE),
    /**Breakpointmatching segmentation method.*/
    BREAKPOINT("BREAKPOINT","BRK",false, Options.NATIVE),
    /**C45 word segmentation method.*/
    C45("C45","C45",true, Options.WEKA),
    /**Char segment parse segmentation method.*/
    CHARSEGMENTPARSE("CHARSEGMENTPARSE","CHAR",false, Options.NATIVE),
    /**Conditional Random Fields segmentation method.*/
    CRF("CRF","CRF",true, Options.MALLET),
    /**Decision Tree segmentation method.*/
    DECISIONTREE("DECISIONTREE","DTREE",true, Options.WEKA),
    /**Highest Occurance segmentation method.*/
    HIGHESTOCCURANCE("HIGHESTOCCURANCE","HOCC",false, Options.NATIVE),
    /**Hidden Markov Model segmentation method.*/
    HMM("HMM","HMM",true, Options.MALLET),
    LCUMATCHING("LCUMATCHING","LCU",false, Options.NATIVE),
    /**MaxMatch segmentation method.*/
    MAXMATCH("MAXMATCH","MAXM",false, Options.NATIVE),
    /**MaxMatch2 segmentation method.*/
    MAXMATCH2("MAXMATCH2","MAXM2",false, Options.NATIVE),
    /**MaxMatch2 segmentation method.*/
    MAXMATCHCOMBINED("MAXMATCHCOMBINED","MAXMC",false, Options.NATIVE),
    /**MaxProbability segmentation method.*/
    MAXPROB("MAXPROB","MAXP",false, Options.NATIVE),
    /**Maximum Entropy segmentation method.*/
    MAXENT("MAXENT","MAXENT",true, Options.MALLET),
    /**Maximum Entropy MC segmentation method.*/
    MAXMCENT("MAXMCENT","MAXMCENT",true, Options.MALLET),
    /**MinMatch segmentation method.*/
    MINMATCH("MINMATCH","MINM",false, Options.NATIVE),
    /**Min Wordcount Matching segmentation method.*/
    MINWCMATCH("MINWCMATCH","MINWC",false, Options.NATIVE),
    /**Min Wordcount Matching segmentation method.*/
    MINWCMATCH2("MINWCMATCH2","MINWC2",false, Options.NATIVE),
    /**Morfessor segmentation method.*/
    MORFESSOR("MORFESSOR","MORF",false, Options.NATIVE),
    /**Naive Bayes segmentation method.*/
    NAIVEBAYES("NAIVEBAYES","BAY",true, Options.WEKA),
    /**Perceptron segmentation method.*/
    PERCEPTRON("PERCEPTRON","PERCEP",true, Options.WEKA),
    /**Prefix Suffix segmentation method.*/
    PREFSUFF("PREFSUFF","PRSF",false, Options.NATIVE),
    /**Random Segment Parse segmentation method.*/
    RANDOMSEGMENTPARSE("RANDOMSEGMENTPARSE","RAND",false, Options.NATIVE),
    /**SVM segmentation method.*/
    SVM("SVM","SVM",true, Options.WEKA),
    /**Tango segmentation method.*/
    TANGO("TANGO","TANGO",false, Options.NATIVE),
    /**Winnow segmentation method.*/
    WINNOW("WINNOW","WNW",true, Options.WEKA),
    NAIVEBAYESSIMPLE("NAIVEBAYESSIMPLE","BAYSI",true, Options.WEKA),
    IB1("IB1","IB1",true, Options.WEKA),
    CLUSTERING_META("Clustering-Meta","CLUSM",true, Options.WEKA),
    LOGISTICREGRESSION("LogisticRegression","LOGREG",true, Options.WEKA),
    LOGISTIC("Logistic","LOGREG",true, Options.WEKA),
    KMEANS("K-Means","kMeans",true, Options.WEKA), POSMATCH("PosMatch","PosMatch",false,Options.NATIVE);
    private Options framework;
    private Boolean hasFeatureSet;
    /**String value (name of the method).*/
    private String value,shortLabel;
    private DictMethods dictMethods;

    private RuleMethods ruleMethods;
    /**Constructor for this class.*/
    private ClassificationMethod(){

    }


    /**Constructor using a description parameter.*/
    private ClassificationMethod(String value,String shortLabel,Boolean hasFeatureSet,Options framework){
        this.hasFeatureSet=hasFeatureSet;
        this.shortLabel=shortLabel;
        this.value=value;
        this.framework=framework;
    }

    public Options getFramework() {
        return framework;
    }

    public Boolean getHasFeatureSet(){
        return this.hasFeatureSet;
    }

    public String getShortLabel(){return this.shortLabel;}

    @Override
    public String getShortname() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}

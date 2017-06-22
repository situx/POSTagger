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

import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.ClassificationMethod;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 17.11.13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
public interface RuleAPI {

    /**
     * Tries to match every character as a word. While in Chinese this is quite effective it is not guaranteed for other languages.
     * @param filepath  the path of the file to analyse
     * @param dict
     * @param transliterationMethod @throws IOException
     */
    public String charSegmentParse(String filepath, String destpath, DictHandling dict, TransliterationMethod transliterationMethod, CharTypes chartype, Boolean transcriptToTranslit, Boolean corpusstr, Boolean printFiles) throws IOException;


    public void initParsing(String sourcepath, String destpath, DictHandling dicthandler, ClassificationMethod method, TransliterationMethod transliterationMethod, CharTypes chartype, Boolean transcriptToTranslit, Boolean corpusstr, Boolean printFiles) throws IOException;

    /**Try to match suffixes from the given words and try to find word classes in the dictionary.*/
    public void matchWordByFakePOS();

    /**
     * PrefixSuffixMatching method.
     * Constructs words by given starting,ending oder middle markers.
     * @param filepath
     * @param dicthandler
     * @throws FileNotFoundException
     */
    public String prefixSuffixMatching(String filepath, String destpath, DictHandling dicthandler, TransliterationMethod transliterationMethod, CharTypes chartype, Boolean transcriptToTranslit, Boolean corpusstr, Boolean printFiles) throws IOException;

    /**
     * Calculates word boundaries for every line at random.
     * @param filepath  the path of the file to analyse
     * @param dicthandler  the dicthandler to use
     * @throws IOException
     */
    public String randomSegmentParse(String filepath, String destpath, DictHandling dicthandler, TransliterationMethod transliterationMethod, CharTypes chartype, Boolean transcriptToTranslit, Boolean corpusstr, Boolean printFiles) throws IOException;

    /**
     * Calculates word boundaries using the tangoAlgorithm.
     *
     * @param filepath the path of the file to analyse
     * @param dicthandler the dicthandler to use
     * @param ngramsize  the size of the ngrams to use
     * @param chartype
     * @throws IOException
     */
    public String tangoAlgorithm(String filepath, String destpath, DictHandling dicthandler, int ngramsize, TransliterationMethod transliterationMethod, CharTypes chartype, Boolean transcriptToTranslit, Boolean corpusstr, Boolean printFiles) throws IOException;
}

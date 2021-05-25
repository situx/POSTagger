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

import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.ClassificationMethod;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;

import java.io.IOException;

/**
 * Interface for implementing dictionary based applications.
 * @author Timo Homburg
 * Date: 12.11.13
 * Time: 12:37
 */
public interface DictMethodAPI {

    /**Initialization method for the parsing methods.
     *
     * @param sourcepath  the path of the file to analyse
     * @param destpath  the result path
     * @param dicthandler the dicthandler to use
     * @param method the method to use
     * @throws IOException on error
     */
    public void initParsing(final String sourcepath, final String destpath, final DictHandling dicthandler, final ClassificationMethod method, final TransliterationMethod transliterationMethod, final CharTypes chartype, final Boolean corpusstr, final Boolean printFiles) throws IOException;

    /**
     * Performs maxEntropyMatching for segmentation.
     *
     * @param filepath  the file to perform maxEntropyMatching on
     * @param dicthandler the dicthandler to use
     *@param chartype @throws IOException on error
     */
    public String maxEntropyMatching(String filepath, String destpath, DictHandling dicthandler, TransliterationMethod transliterationMethod, CharTypes chartype, Boolean corpusstr, Boolean printFiles) throws IOException;

    /**
     * Dictionarybased approach. Matches the longest word possible.
     *
     * @param filepath  the path of the file to analyse
     * @param dicthandler the dicthandler to use
     * @param left indicactes if it should be matched from left to right or right to left
     * @param chartype
     * @throws IOException  on error
     */
    public String maxMatch(String filepath, String destpath, DictHandling dicthandler, boolean left, TransliterationMethod transliterationMethod, CharTypes chartype, Boolean corpusstr, Boolean printFiles) throws IOException;

    /**
     * Dictionarybased approach. Matches the shortest word possible.
     *
     * @param filepath  the path of the file to analyse
     * @param dicthandler the dicthandler to use
     * @param chartype
     * @throws IOException  on error
     */
    public String minMatch(final String filepath,final String destpath, final DictHandling dicthandler, final TransliterationMethod transliterationMethod, final CharTypes chartype,final Boolean corpusstr,final Boolean printFiles) throws IOException;


    public String minWCMatching(String filepath, String destpath, DictHandling dicthandler, TransliterationMethod transliterationMethod, CharTypes chartype, Boolean corpusstr, Boolean printFiles) throws IOException;
}

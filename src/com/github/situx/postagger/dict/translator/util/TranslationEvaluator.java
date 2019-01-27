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

package com.github.situx.postagger.dict.translator.util;

import com.pwnetics.metric.WordSequenceAligner;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by timo on 26.06.16.
 */
public class TranslationEvaluator {

    public static Double evaluationByWordErrorRate(List<String> goldstandard, List<String> generated){
        WordSequenceAligner aligner=new WordSequenceAligner();
        WordSequenceAligner.Alignment result=aligner.align(goldstandard.toArray(new String[goldstandard.size()]),generated.toArray(new String[generated.size()]));
        List<WordSequenceAligner.Alignment> reslist=new LinkedList<>();
        reslist.add(result);
        WordSequenceAligner.SummaryStatistics ss = aligner.new SummaryStatistics(reslist);
        return Double.valueOf(ss.getWordErrorRate());
    }


}

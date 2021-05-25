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

package com.github.situx.postagger.dict.pos.util;

import java.util.List;

/**
 * Created by timo on 26.06.16.
 */
public class POSTagEvaluator {

    public Double evaluateTokenAccuracyAllConditions(List<POSDefinition> goldstandard, List<POSDefinition> generated){
        Double posdefcounter=0.,truepositive=0.,truenegative=0.;
        for(POSDefinition posdef:goldstandard){
            if(posdef.equals(generated.get(posdefcounter.intValue()))){
                truepositive++;
            }else{
                truenegative++;
            }
            posdefcounter++;
        }
        return ((truepositive+truenegative)/goldstandard.size());
    }

    public Double evaluateTokenAccuracyOnWordClass(List<POSDefinition> goldstandard, List<POSDefinition> generated){
        Double posdefcounter=0.,truepositive=0.,truenegative=0.;
        for(POSDefinition posdef:goldstandard){
            if(posdef.getPosTag().equals(generated.get(posdefcounter.intValue()).getPosTag())){
                truepositive++;
            }else{
                truenegative++;
            }
            posdefcounter++;
        }
        return ((truepositive+truenegative)/goldstandard.size());
    }
}

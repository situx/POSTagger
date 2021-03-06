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

package com.github.situx.postagger.dict.translator.cunei;

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.dict.translator.util.TransliterationTranslator;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TranslationMethod;

/**
 * Created by timo on 6/3/15.
 */
public class ToHittiteTranslator extends Translator {
    public String translate(CharTypes to,String translationText,TranslationMethod translationMethod){
        String result="";
        switch (to){
            case ENGLISH: result=this.hitToEnglish(translationText,translationMethod);
                break;
            default:
        }
        return result;
    }

    public Translator getTranslator(CharTypes to){
        switch (to){
            case ENGLISH: return new SumToEngTranslator(CharTypes.AKKADIAN);
            default:
                return this;
        }
    }

    public Translator getTranslator(CharTypes to,POSTagger fromPos){
        switch (to){
            case ENGLISH: return new HitToEngTranslator(CharTypes.HITTITE,fromPos);
            case HITTITE: return new TransliterationTranslator(CharTypes.HITTITE);
            default:
                return this;
        }
    }

    public String hitToEnglish(String translationText,TranslationMethod translationMethod){
        switch (translationMethod){
            default:
            case LEMMA: //return new AkkadToEngTranslator().wordByWordPOStranslateToEnglish(translationText,true);
        }
        return "";
    }

    @Override
    public String wordByWordPOStranslate(final String translationText, final Boolean pinyin, final Integer initialPos) {
        return null;
    }
}

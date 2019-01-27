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

package com.github.situx.postagger.dict.importhandler;

import com.github.situx.postagger.dict.dicthandler.DictHandling;
import org.xml.sax.ext.DefaultHandler2;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * ImportHandler for dictionary files.
 */
public abstract class ImportHandler extends DefaultHandler2 {

    public DictHandling dictHandler;

    public ImportHandler(){

    }

    public static String formatDouble(Double d){
        if(d==null){
            return "";
        }
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        if(d==Double.POSITIVE_INFINITY){
            return d.toString();
        }
        return new DecimalFormat("#0.000000",otherSymbols).format(d);
    }

    public abstract String reformatToASCIITranscription(String transcription);

    public abstract String reformatToUnicodeTranscription(String transcription);
}

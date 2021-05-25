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

package com.github.situx.postagger.dict.chars.cuneiform;

import com.github.situx.postagger.util.enums.methods.CharTypes;

/**
 * Created with IntelliJ IDEA.
 * User: timo
 * Date: 25.10.13
 * Time: 12:00
 * Class for modelling a hittitian cuneiform character.
 */
public class HittiteChar extends CuneiChar {

    public String getHethZLNumber() {
        return hethZLNumber;
    }

    public void setHethZLNumber(final String hethZLNumber) {
        this.hethZLNumber = hethZLNumber;
    }

    private String hethZLNumber;

    /**
     * Constructor for this class
     * @param character the cuneiform character modelled by this class.
     */
    public HittiteChar(final String character){
        super(character);
        this.hethZLNumber="";
        this.charlength= CharTypes.HITTITE.getChar_length();
    }

    @Override
    public Boolean getDeterminative() {
        return this.determinative;
    }

    @Override
    public void setDeterminative(final Boolean determinative) {
        this.determinative=determinative;
    }

    @Override
    public Boolean getLogograph() {
        return this.logograph;
    }

    @Override
    public void setLogograph(final Boolean logograph) {
        this.logograph=logograph;
    }

    @Override
    public Boolean getPhonogram() {
        return this.phonogram;
    }

    @Override
    public void setPhonogram(final Boolean phonogram) {
        this.phonogram=phonogram;
    }

    @Override
    public void setStem(final String stem) {

    }

    @Override
    public String getCharInformation(final String lineSeparator,final Boolean translitList,final Boolean html) {
        StringBuilder result=new StringBuilder();
        if(translitList){
            if(html){
                result.append("<font face=\"Akkadian\">");
                result.append(this.toString());
                result.append("</font>");
            }else{
                result.append(this.toString());
            };
            result.append((this.charName!=null?" ("+this.charName+") ":""));
            if(!this.transliterations.keySet().isEmpty()) {
                result.append(this.transliterations.keySet().toString());
            }
            result.append(lineSeparator);
        } else{
            result.append((this.charName!=null?this.charName:""));
            result.append(lineSeparator);
        }
        if(!this.isWord && !this.toString().isEmpty()){
            result.append("Codepoint: U+");
            result.append(Integer.toHexString(this.toString().codePointAt(0)).toUpperCase());
            result.append(lineSeparator);
            if(!this.mezlNumber.isEmpty()){
                result.append("MeZL: ");
                result.append(this.mezlNumber);
                result.append(lineSeparator);
            }
            if(!this.aBzlNumber.isEmpty()){
                result.append("aBZL: ");
                result.append(this.aBzlNumber);
                result.append(lineSeparator);
            }
            if(!this.hethZLNumber.isEmpty()){
                result.append("HethZL: ");
                result.append(this.hethZLNumber);
                result.append(lineSeparator);
            }
            if(this.paintInformation!=null){
                result.append("PaintInfo: ");
                result.append(this.paintInformation);
            }
        } else {
            result.append("Occurance: ");
            result.append(this.occurances);
            result.append(lineSeparator);
            result.append("POSTags: ");
            result.append(this.postags);
            result.append(lineSeparator);
        }
        return result.toString();
    }


    @Override
    public String toXML(String startelement,Boolean statistics) {
        return super.toXML(startelement,statistics);
    }
}

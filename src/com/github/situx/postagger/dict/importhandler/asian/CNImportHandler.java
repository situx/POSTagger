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

package com.github.situx.postagger.dict.importhandler.asian;

import com.github.situx.postagger.dict.utils.Translation;
import com.github.situx.postagger.dict.utils.Transliteration;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.github.situx.postagger.dict.chars.asian.CNChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.importhandler.ImportHandler;
import com.github.situx.postagger.dict.utils.Following;
import com.github.situx.postagger.util.enums.util.Options;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Locale;
import java.util.Map;

/**
 * Created by timo on 03.07.14.
 */
public class CNImportHandler extends ImportHandler{

    public Double amountOfWordsInCorpus=0.;
    public Double lengthOfWordsInCorpus=0.;
    protected Map<String,CNChar> resultmap;
    protected Map<String,String> transcriptToCuneiMap;
    protected Map<String,String> translitToCuneiMap;
    private StringBuilder charcollector=new StringBuilder();
    /**The CharType to use.*/
    private CharTypes chartype;
    private StringBuilder followingcollector=new StringBuilder(),languagecollector=new StringBuilder(),precedingcollector=new StringBuilder();
    /**Option to fill a dictionary or a map.*/
    private Options mapOrDict;
    private CNChar newChar;
    private Double precedingtemp,precedingtemp2,occurancetemp;
    private Following tempfollowing;
    private Translation temptranslat;
    private Transliteration temptranslit;
    private boolean translation;
    private boolean transliteration,following,mapentry,preceding;

    /**
     * Constructor for this class.
     * @param mapOrDict indicates if we are parsing a mapping or dictionary file
     * @param resultmap the map to put the resulting chars in
     * @param translitToCuneiMap the map to put the resulting transliterations in
     * @param chartype the language char type to use
     */
    public CNImportHandler(final Options mapOrDict,final DictHandling dictHandler, final Map<String, CNChar> resultmap, final Map<String, String> translitToCuneiMap, final Map<String, String> transcriptToCuneiMap, final CharTypes chartype){
        this.resultmap=resultmap;
        this.mapOrDict=mapOrDict;
        this.translitToCuneiMap=translitToCuneiMap;
        this.transcriptToCuneiMap=transcriptToCuneiMap;
        this.chartype=chartype;
        this.dictHandler=dictHandler;
    }

    public static Boolean isFullWidthNumeric(String numericString){
        return numericString.matches("[-+]*\\p{Nd}+");
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        //System.out.println(new String(ch,start,length));
        if(this.transliteration){
            this.temptranslit.setTransliterationString((this.temptranslit.getTransliteration()+new String(ch, start, length)).replace("\n", "").trim());
            this.temptranslit.setTranscription(TranscriptionMethods.translitTotranscript(this.temptranslit.getTransliteration()));
        }else if(this.following){
            this.followingcollector.append(new String(ch,start,length));
        }else if(this.preceding){
            this.precedingcollector.append(new String(ch,start,length));
        } else if(this.translation){
            this.languagecollector.append(new String(ch,start,length));
        }else if(this.mapentry){
            this.charcollector.append(new String(ch,start,length));
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        switch(qName){
            case Tags.TRANSLITERATION: this.transliteration=false;this.newChar.addTransliteration(this.temptranslit);break;
            case Tags.TRANSLATION: this.translation=false;this.newChar.addTranslation(this.languagecollector.toString().replace("\n", "").trim(),this.temptranslat.getLocale());
                this.languagecollector.delete(0,languagecollector.length());break;
            case Tags.PRECEDING:if(this.following){
                this.tempfollowing.addPreceding(this.precedingcollector.toString().replace("\n", "").trim(),this.precedingtemp,this.precedingtemp2);
            }else{
                this.newChar.addPrecedingWord(this.precedingcollector.toString().replace("\n", "").trim());
            }
                this.precedingcollector.delete(0,precedingcollector.length());this.preceding=false;break;
            case Tags.FOLLOWING: this.following=false;
                this.tempfollowing.setFollowingstr(followingcollector.toString().replace("\n", "").trim());
                this.newChar.addFollowingWord(this.tempfollowing);
                followingcollector.delete(0,followingcollector.length());this.following=false;break;
            case Tags.MAPENTRY:
            case Tags.DICTENTRY: this.mapentry=false;this.newChar.setCharacter(this.charcollector.toString().replace("\n", "").trim());
                this.lengthOfWordsInCorpus+=this.newChar.length();
                for(Transliteration translit:this.newChar.getTransliterationSet()){
                    this.translitToCuneiMap.put(translit.toString(),this.newChar.getCharacter());
                    this.transcriptToCuneiMap.put(TranscriptionMethods.translitTotranscript(translit.toString()),this.newChar.getCharacter());
                }
                this.resultmap.put(this.newChar.getCharacter(), this.newChar);
                //System.out.println(this.newChar);
                break;
            default:
        }
    }

    /**
     * Converts a unicode pinyin string to its ascii representation.
     * @param transcription the pinyin String to convert
     * @return  the result
     */
    @Override
    public String reformatToASCIITranscription(final String transcription) {
        return transcription.replaceAll("ā","a1").replaceAll("á","a2").replaceAll("ǎ","a3").replaceAll("à","a4")
                .replaceAll("ī","i1").replaceAll("í","i2").replaceAll("ǐ","i3").replaceAll("ì","i4")
                .replaceAll("ō","o1").replaceAll("ó","o2").replaceAll("ǒ","o3").replaceAll("ò","o4")
                .replaceAll("ū","u1").replaceAll("ú","u2").replaceAll("ǔ","u3").replaceAll("ù","u4")
                .replaceAll("ē","e1").replaceAll("é","e2").replaceAll("ě","e3").replaceAll("è","e4")
                .replaceAll("ǖ","ü1").replaceAll("ǘ","ü2").replaceAll("ǚ","ü3").replaceAll("ǜ","ü4")
                .replaceAll("Ā","A1").replaceAll("Á","A2").replaceAll("Ǎ","A3").replaceAll("À","A4")
                .replaceAll("Ī","I1").replaceAll("Í","I2").replaceAll("Ǐ","I3").replaceAll("Ì","I4")
                .replaceAll("Ō","O1").replaceAll("Ó","O2").replaceAll("Ǒ","O3").replaceAll("Ò","O4")
                .replaceAll("Ū","U1").replaceAll("Ú","U2").replaceAll("Ǔ","U3").replaceAll("Ù","U4")
                .replaceAll("Ē","E1").replaceAll("É","E2").replaceAll("Ě","E3").replaceAll("È","E4")
                .replaceAll("Ǖ","Ü1").replaceAll("Ǘ","Ü2").replaceAll("Ǚ","Ü3").replaceAll("Ǜ","Ü4");
    }

    /**
     * Converts a unicode pinyin string to its ascii representation.
     * @param transcription the pinyin String to convert
     * @return  the result
     */
    public String reformatToASCIITranscription2(final String transcription) {
        return transcription.replaceAll("ā","a1").replaceAll("á","a2").replaceAll("ǎ","a3").replaceAll("à","a4")
                .replaceAll("ī","i1").replaceAll("í","i2").replaceAll("ǐ","i3").replaceAll("ì","i4")
                .replaceAll("ō","o1").replaceAll("ó","o2").replaceAll("ǒ","o3").replaceAll("ò","o4")
                .replaceAll("ū","u1").replaceAll("ú","u2").replaceAll("ǔ","u3").replaceAll("ù","u4")
                .replaceAll("ē","e1").replaceAll("é","e2").replaceAll("ě","e3").replaceAll("è","e4")
                .replaceAll("ǖ","uu1").replaceAll("ǘ","uu2").replaceAll("ǚ","uu3").replaceAll("ǜ","uu4")
                .replaceAll("Ā","A1").replaceAll("Á","A2").replaceAll("Ǎ","A3").replaceAll("À","A4")
                .replaceAll("Ī","I1").replaceAll("Í","I2").replaceAll("Ǐ","I3").replaceAll("Ì","I4")
                .replaceAll("Ō","O1").replaceAll("Ó","O2").replaceAll("Ǒ","O3").replaceAll("Ò","O4")
                .replaceAll("Ū","U1").replaceAll("Ú","U2").replaceAll("Ǔ","U3").replaceAll("Ù","U4")
                .replaceAll("Ē","E1").replaceAll("É","E2").replaceAll("Ě","E3").replaceAll("È","E4")
                .replaceAll("Ǖ","UU1").replaceAll("Ǘ","UU2").replaceAll("Ǚ","UU3").replaceAll("Ǜ","UU4");
    }

    @Override
    public String reformatToUnicodeTranscription(final String transcription) {
        return transcription.replaceAll("a1","ā").replaceAll("a2","á").replaceAll("a3","ǎ").replaceAll("a4","à")
                .replaceAll("i1","ī").replaceAll("i2","í").replaceAll("i3","ǐ").replaceAll("i4","ì").replaceAll("i5","i")
                .replaceAll("o1","ō").replaceAll("o2","ó").replaceAll("o3","ǒ").replaceAll("o4","ò").replaceAll("o5","o")
                .replaceAll("u1","ū").replaceAll("u2","ú").replaceAll("u3","ǔ").replaceAll("u4","ù").replaceAll("u5","u")
                .replaceAll("e1","ē").replaceAll("e2","é").replaceAll("e3","ě").replaceAll("e4","è").replaceAll("e5","e")
                .replaceAll("uu1","ǖ").replaceAll("uu2","ǘ").replaceAll("uu3","ǚ").replaceAll("uu4","ǜ").replaceAll("uu5","ü")
                .replaceAll("A1","Ā").replaceAll("A2","Á").replaceAll("A3","Ǎ").replaceAll("A4","À").replaceAll("A5","A")
                .replaceAll("I1","Ī").replaceAll("I2","Í").replaceAll("I3","Ǐ").replaceAll("I4","Ì").replaceAll("I5","I")
                .replaceAll("O1","Ō").replaceAll("O2","Ó").replaceAll("O3","Ǒ").replaceAll("O4","Ò").replaceAll("O5","O")
                .replaceAll("U1","Ū").replaceAll("U2","Ú").replaceAll("U3","Ǔ").replaceAll("U4","Ù").replaceAll("U5","U")
                .replaceAll("E1","Ē").replaceAll("E2","É").replaceAll("E3","Ě").replaceAll("E4","È").replaceAll("E5","E")
                .replaceAll("UU1","Ǖ").replaceAll("UU2","Ǘ").replaceAll("UU3","Ǚ").replaceAll("UU4","Ǜ").replaceAll("UU5","Ü");
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        switch(qName) {
            case Tags.DICTENTRIES:
                this.dictHandler.setAmountOfWordsInCorpus(Double.valueOf(attributes.getValue(Tags.NUMBEROFWORDS)));
                this.dictHandler.setAvgWordLength(Double.valueOf(attributes.getValue(Tags.AVGWORDLENGTH)));
                break;
            case Tags.MAPENTRY:
            case Tags.DICTENTRY:
                this.mapentry = true;
                this.charcollector.delete(0,charcollector.length());
                switch (chartype) {
                    case CHINESE:
                        this.newChar = new CNChar("");
                        break;
                    default:
                        this.newChar = new CNChar("");
                }
                this.newChar.setLeftaccessorvariety(Double.valueOf(attributes.getValue(Tags.LEFTACCVAR)));
                this.newChar.setRightaccessorvariety(Double.valueOf(attributes.getValue(Tags.RIGHTACCVAR)));
                if (this.mapOrDict == Options.FILLMAP) {
                    this.occurancetemp = Double.valueOf(attributes.getValue(Tags.SINGLE.toString()));
                    if (occurancetemp > 0) {
                        newChar.setSingleOccurance(occurancetemp);
                        newChar.setSingleCharacter(true);
                    } else {
                        newChar.setEndingCharacter(false);
                    };
                    this.occurancetemp = Double.valueOf(attributes.getValue(Tags.BEGIN.toString()));
                    if (occurancetemp > 0) {
                        newChar.setBeginOccurance(occurancetemp);
                        newChar.setBeginningCharacter(true);
                    } else {
                        newChar.setBeginningCharacter(false);
                    }
                    this.occurancetemp = Double.valueOf(attributes.getValue(Tags.MIDDLE.toString()));
                    if (occurancetemp > 0) {
                        newChar.setMiddleOccurance(occurancetemp);
                        newChar.setMiddleCharacter(true);
                    } else {
                        newChar.setMiddleCharacter(false);
                    }
                    this.occurancetemp = Double.valueOf(attributes.getValue(Tags.END.toString()));
                    if (occurancetemp > 0) {
                        newChar.setEndOccurance(occurancetemp);
                        newChar.setEndingCharacter(true);
                    } else {
                        newChar.setEndingCharacter(false);
                    }

                }
                this.newChar.setAbsOccurance(Double.valueOf(attributes.getValue(Tags.ABSOCC.toString())));
                //System.out.println("RelativeOccurance: "+attributes.getValue(Tags.RELOCC.toString()));
                this.newChar.setRelativeOccuranceFromDict(Double.valueOf(attributes.getValue(Tags.RELOCC.toString())));
                //System.out.println("RelativeOccurance: "+this.newChar.getRelativeOccurance());
                break;
            case Tags.TRANSLITERATION:
                this.temptranslit = new Transliteration("", "");
                this.temptranslit.setAbsoluteOccurance(Double.valueOf(attributes.getValue(Tags.ABSOCC.toString())));
                //System.out.println("RelativeOccurance: "+attributes.getValue(Tags.RELOCC.toString()));
                this.temptranslit.setRelativeOccuranceFromDict(Double.valueOf(attributes.getValue(Tags.RELOCC.toString())));
                //System.out.println("RelativeOccurance: "+this.temptranslit.getRelativeOccurance());
                if (this.mapOrDict == Options.FILLMAP) {
                    Double temp;
                    temp = Double.valueOf(attributes.getValue(Tags.BEGIN.toString()));
                    this.temptranslit.setBeginTransliteration(Boolean.valueOf(attributes.getValue(Tags.BEGIN.toString())), temp.intValue());
                    temp = Double.valueOf(attributes.getValue(Tags.MIDDLE.toString()));
                    this.temptranslit.setMiddleTransliteration(Boolean.valueOf(attributes.getValue(Tags.MIDDLE.toString())), temp.intValue());
                    temp = Double.valueOf(attributes.getValue(Tags.END.toString()));
                    this.temptranslit.setEndTransliteration(Boolean.valueOf(attributes.getValue(Tags.END.toString())), temp.intValue());
                    temp = Double.valueOf(attributes.getValue(Tags.SINGLE.toString()));
                    this.temptranslit.setSingleTransliteration(Boolean.valueOf(attributes.getValue(Tags.SINGLE.toString())), temp.intValue());
                }
                this.temptranslit.setIsWord(Boolean.valueOf(attributes.getValue(Tags.ISWORD.toString())));
                this.temptranslit.setTranscription(attributes.getValue(Tags.TRANSCRIPTION.toString()));

                this.transliteration=true;break;
            case Tags.TRANSLATION:
                this.temptranslat=new Translation("", new Locale(attributes.getValue(Tags.LOCALE.toString())));
                this.translation=true;break;
            case Tags.FOLLOWING: this.following=true;
                this.tempfollowing=new Following();
                this.tempfollowing.setFollowing((attributes.getValue(Tags.ABSOCC.toString())!=null?Double.valueOf(attributes.getValue(Tags.ABSOCC.toString())):0.0),(attributes.getValue(Tags.ABSBD.toString())!=null?Double.valueOf(attributes.getValue(Tags.ABSBD.toString())):0.0));
                break;
            case Tags.PRECEDING: this.preceding=true;
                if(attributes.getValue(Tags.ABSOCC.toString())!=null){
                    this.precedingtemp=Double.valueOf(attributes.getValue(Tags.ABSOCC.toString()));
                }
                if(attributes.getValue(Tags.ABSBD.toString())!=null)
                    this.precedingtemp2=Double.valueOf(attributes.getValue(Tags.ABSBD.toString()));
                break;
            default:

        }

    }
}

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

package com.github.situx.postagger.dict.importhandler.cuneiform;

import com.github.situx.postagger.dict.chars.cuneiform.*;
import com.github.situx.postagger.dict.utils.*;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.cuneiform.*;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.utils.*;
import com.github.situx.postagger.methods.transcription.TranscriptionMethods;
import com.github.situx.postagger.util.enums.util.Options;
import com.github.situx.postagger.util.enums.util.Tags;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Timo Homburg
 * Date: 06.11.13
 * Time: 17:59
 * ImportHandler for the cunei dictionaries and map format.
 */
public class AkkadianImportHandler extends CuneiImportHandler {
    public Double amountOfWordsInCorpus=0.;
    public Double lengthOfWordsInCorpus=0.;
    private StringBuilder charcollector =new StringBuilder();
    /**The CharType to use.*/
    private CharTypes chartype;
    private StringBuilder followingcollector=new StringBuilder(),languagecollector=new StringBuilder(),precedingcollector=new StringBuilder(),postagcollector=new StringBuilder(),morphpatternCollector=new StringBuilder();
    private String postaguri;
    /**Option to fill a dictionary or a map.*/
    private Options mapOrDict;
    private CuneiChar newChar;
    private String conceptURI;
    private Double precedingtemp,precedingtemp2;
    private Following tempfollowing;
    private Translation temptranslat;
    private Transliteration temptranslit;
    private List<Transliteration> transliterationsPerChar=new LinkedList<Transliteration>();
    private List<POSTag> postagsPerChar=new LinkedList<POSTag>();
    private List<Translation> translationsPerChar=new LinkedList<Translation>();
    private boolean translation,logograph,morphpatternbool,epochbool,dialectbool;
    private boolean transliteration,following,mapentry,preceding,postag;
    private MorphPattern morphpattern;
    private Epoch currentepoch;
    private Dialect currentdialect;

    /**
     * Constructor for this class.
     * @param mapOrDict indicates if we are parsing a mapping or dictionary file
     * @param resultmap the map to put the resulting chars in
     * @param translitToCuneiMap the map to put the resulting transliterations in
     * @param chartype the language char type to use
     */
    public AkkadianImportHandler(final Options mapOrDict, final DictHandling dictHandler,final Map<String, CuneiChar> resultmap, final Map<String, String> translitToCuneiMap, final Map<String, String> transcriptToCuneiMap,final Map<String,CuneiChar> logographs,CharTypes chartype){
          this.resultmap=resultmap;
          this.mapOrDict=mapOrDict;
          this.translitToCuneiMap=translitToCuneiMap;
        this.logographs=logographs;
          this.transcriptToCuneiMap=transcriptToCuneiMap;
          this.chartype=chartype;
          this.dictHandler=dictHandler;
    }


    public void mergeSameCharacterWords(LangChar currentChar,LangChar newChar){
        for(Transliteration translit:newChar.getTransliterations().keySet()){
            currentChar.getTransliterations().put(translit,0.);
        }
        //currentChar.getTransliterationSet().addAll(newChar.getTransliterationSet());
        for(String lang:newChar.getTranslations().keySet()){
            if(!currentChar.getTranslations().containsKey(lang)){
                currentChar.getTranslations().put(lang,new TreeMap<>());
            }
            for(Translation key:newChar.getTranslations().get(lang).keySet()){
                currentChar.getTranslations().get(lang).put(key,newChar.getTranslations().get(lang).get(key));
            }
        }
        currentChar.getPostags().addAll(newChar.getPostags());
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        //System.out.println(new String(ch,start,length));
        if(this.transliteration){
            this.temptranslit.setTransliterationString((this.temptranslit.getTransliteration()+new String(ch, start, length)).replace(System.lineSeparator(), "").trim());
            this.temptranslit.setTranscription(TranscriptionMethods.translitTotranscript(this.temptranslit.getTransliteration()));
        }else if(this.following){
            this.followingcollector.append(new String(ch, start, length));
        }else if(this.preceding){
            this.precedingcollector.append(new String(ch, start, length));
        } else if(this.translation){
            this.languagecollector.append(new String(ch,start,length));
        }else if(this.postag){
            this.postagcollector.append(new String(ch,start,length));
        }else if(this.mapentry && !epochbool && !dialectbool){
            this.charcollector.append(new String(ch,start,length));
        }else if(this.morphpatternbool){
            this.morphpatternCollector.append(new String(ch,start,length));
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        switch(qName){
            case Tags.TRANSLITERATION: this.transliteration=false;
                this.newChar.addTransliteration(this.temptranslit);
                this.transliterationsPerChar.add(this.temptranslit);
                break;
            case Tags.PATTERN:
                this.morphpatternbool=false;
                this.morphpattern.pattern=this.morphpatternCollector.toString();
                if(!this.dictHandler.morphpattern.containsKey(this.morphpattern.postag)){
                    this.dictHandler.morphpattern.put(this.morphpattern.postag,new TreeSet<>());
                }
                this.dictHandler.morphpattern.get(this.morphpattern.postag).add(this.morphpattern);
                this.morphpatternCollector.delete(0,morphpatternCollector.length());
                break;
            case Tags.TRANSLATION: this.translation=false;
                Translation added=this.newChar.addTranslation(this.languagecollector.toString().replace(System.lineSeparator(), "").trim(),this.temptranslat.getLocale());
                this.translationsPerChar.add(added);
                this.languagecollector.delete(0,languagecollector.length());
                break;
            case Tags.POSTAG: this.postag=false;
                POSTag pos=new POSTag(this.postagcollector.toString().replace(System.lineSeparator(), "").trim());
                if(this.postaguri!=null){
                    pos.setConceptURI(postaguri);
                }
                this.newChar.addPOSTag(pos);
                this.postagsPerChar.add(pos);
                this.postagcollector.delete(0,postagcollector.length());
                break;
            case Tags.PRECEDING:if(this.following){
                this.tempfollowing.addPreceding(this.precedingcollector.toString().replace(System.lineSeparator(), "").trim(),this.precedingtemp,this.precedingtemp2);
            }else{
                this.newChar.addPrecedingWord(this.precedingcollector.toString().replace(System.lineSeparator(), "").trim());
            }
                this.precedingcollector.delete(0,this.precedingcollector.length()); this.preceding=false;break;
            case Tags.FOLLOWING: this.following=false;
                this.tempfollowing.setFollowingstr(followingcollector.toString().replace(System.lineSeparator(), "").trim());
                this.newChar.addFollowingWord(this.tempfollowing);
                followingcollector.delete(0,followingcollector.length());this.following=false;break;
            case Tags.EPOCH:
                this.epochbool=false;
                break;
            case Tags.DIALECT:
                this.dialectbool=false;
                break;
            case Tags.MAPENTRY:
            case Tags.DICTENTRY:
                this.mapentry=false;
            this.newChar.setCharacter(this.charcollector.toString().replace(System.lineSeparator(), "").trim());
                this.lengthOfWordsInCorpus+=this.newChar.length();

                if(this.newChar.getTransliterationSet().isEmpty() && this.mapOrDict==Options.FILLMAP){
                    this.temptranslit = new Transliteration(this.newChar.getCharName(),this.newChar.getCharName());
                    if(this.temptranslit.getTransliteration()!=null)
                        this.newChar.getTransliterations().put(this.temptranslit,0.0);
                }
                for(Transliteration translit:this.newChar.getTransliterationSet()){
                    this.translitToCuneiMap.put(translit.toString(),this.newChar.getCharacter());
                    this.transcriptToCuneiMap.put(TranscriptionMethods.translitTotranscript(translit.toString()),this.newChar.getCharacter());
                    for(POSTag post:newChar.getPostags()){
                        this.dictHandler.addPOSTagForWord(post,translit.getTransliteration(),newChar);
                    }
                }

                if(this.newChar.getPaintInformation()!=null && !this.newChar.getPaintInformation().isEmpty()){
                    this.dictHandler.getPaintTree().addWordComboToTree(this.newChar.getPaintInformation(),this.newChar.getCharacter(),1,this.newChar.getMeaning(),((this.newChar.getTranslations()!=null && !this.newChar.getTranslations().isEmpty())?this.newChar.getTranslations().keySet().iterator().next():""),((this.newChar.getPostags()!=null && !this.newChar.getPostags().isEmpty())?this.newChar.getPostags().toString():""),((this.newChar.getConceptURI()!=null && !this.newChar.getConceptURI().isEmpty())?this.newChar.getConceptURI():""),4);
                }
                if(this.newChar.getPaintInformation()!=null){
                    if(!this.dictHandler.getPaintInfoToCharStrings().containsKey(this.newChar.getPaintInformation())){
                        this.dictHandler.getPaintInfoToCharStrings().put(this.newChar.getPaintInformation(),new TreeSet<>());
                    }
                    this.dictHandler.getPaintInfoToCharStrings().get(this.newChar.getPaintInformation()).add(this.newChar.getCharacter());
                }
                for(Transliteration translit:this.transliterationsPerChar){
                    translit.setTranslations(this.translationsPerChar);
                    translit.setPostags(this.postagsPerChar);
                    translit.setConceptURI(this.conceptURI);
                }
                this.translationsPerChar=new LinkedList<>();
                this.transliterationsPerChar=new LinkedList<>();
                this.postagsPerChar=new LinkedList<>();
                if(this.resultmap.containsKey(this.newChar.getCharacter())){
                    mergeSameCharacterWords(this.resultmap.get(this.newChar.getCharacter()),this.newChar);
                }else{
                    this.resultmap.put(this.newChar.getCharacter(), this.newChar);
                }
            //System.out.println(this.newChar);
            break;
            default:
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        switch(qName) {
            case Tags.DICTENTRIES:
                if(attributes.getValue(Tags.NUMBEROFWORDS)!=null)
                    this.dictHandler.setAmountOfWordsInCorpus(Double.valueOf(attributes.getValue(Tags.NUMBEROFWORDS)));
                if(attributes.getValue(Tags.AVGWORDLENGTH)!=null)
                    this.dictHandler.setAvgWordLength(Double.valueOf(attributes.getValue(Tags.AVGWORDLENGTH)));
                break;
            case Tags.MAPENTRY:
            case Tags.DICTENTRY:
                this.mapentry = true;
                this.charcollector.delete(0,charcollector.length());
                switch (chartype) {
                    case AKKADIAN:
                        this.newChar = new AkkadChar("");
                        break;
                    case HITTITE:
                        //System.out.println("Parse new HittiteChar!");
                        this.newChar = new HittiteChar("");

                        break;
                    case EGYPTIANCHAR:
                        this.newChar = new EgyptChar("");
                        break;
                    case SUMERIAN:
                        this.newChar = new SumerianChar("");
                        /*if(attributes.getValue(Tags.SLHA)!=null && !attributes.getValue(Tags.SLHA).isEmpty()){
                            ((SumerianChar)this.newChar).setSHAnumber(attributes.getValue(Tags.SLHA));
                        }*/
                        break;
                    default:
                        this.newChar = new AkkadChar("");
                }
                if(attributes.getValue(Tags.HBZL)!=null && !attributes.getValue(Tags.HBZL).isEmpty()){
                    this.newChar.setHethzlNumber(attributes.getValue(Tags.HBZL));
                }
                if(attributes.getValue(Tags.SLHA)!=null && !attributes.getValue(Tags.SLHA).isEmpty()){
                    this.newChar.setLhaNumber(attributes.getValue(Tags.SLHA));
                }
                if(attributes.getValue(Tags.REF)!=null && !attributes.getValue(Tags.REF).isEmpty()){
                    this.newChar.setRefString(attributes.getValue(Tags.REF));
                }
                this.newChar.setPaintInformation(attributes.getValue(Tags.GOTTSTEIN));
                this.newChar.setUnicodeCodePage(attributes.getValue(Tags.UTF8CODEPOINT));
                if(attributes.getValue(Tags.MEZL)!=null && !attributes.getValue(Tags.MEZL).isEmpty()){
                    this.newChar.setMezlNumber(attributes.getValue(Tags.MEZL));
                }
                if(attributes.getValue(Tags.ABZL)!=null && !attributes.getValue(Tags.ABZL).isEmpty()){
                    this.newChar.setaBzlNumber(attributes.getValue(Tags.ABZL));
                }
                this.newChar.setCharName(attributes.getValue(Tags.SIGNNAME.toString()));
                this.newChar.setPhonogram(Boolean.valueOf(attributes.getValue(Tags.PHONO.toString())));
                this.newChar.setDeterminative(Boolean.valueOf(attributes.getValue(Tags.DETERMINATIVE.toString())));
                this.newChar.setLogograph(Boolean.valueOf(attributes.getValue(Tags.LOGO.toString())));
                this.conceptURI=attributes.getValue(Tags.CONCEPT);
                if(attributes.getValue(Tags.CONCEPT)!=null) {
                    this.newChar.setConceptURI(attributes.getValue(Tags.CONCEPT));
                }
                if(attributes.getValue(Tags.LOGOGRAM)!=null && !attributes.getValue(Tags.LOGOGRAM).isEmpty()){
                    this.newChar.logographs.add(attributes.getValue(Tags.LOGOGRAM));
                }
                if(attributes.getValue(Tags.MEANING.toString())!=null && !attributes.getValue(Tags.MEANING.toString()).isEmpty()){
                    this.newChar.setMeaning(attributes.getValue(Tags.MEANING.toString()));
                }
                if(attributes.getValue(Tags.LEFTACCVAR)!=null)
                    this.newChar.setLeftaccessorvariety(Double.valueOf(attributes.getValue(Tags.LEFTACCVAR)));
                if(attributes.getValue(Tags.RIGHTACCVAR)!=null)
                    this.newChar.setRightaccessorvariety(Double.valueOf(attributes.getValue(Tags.RIGHTACCVAR)));
                if (this.mapOrDict == Options.FILLMAP) {
                    double occurancetemp;
                    if(attributes.getValue(Tags.SINGLE.toString())!=null) {
                        occurancetemp = Double.valueOf(attributes.getValue(Tags.SINGLE.toString()));
                        if (occurancetemp > 0) {
                            newChar.setSingleOccurance(occurancetemp);
                            newChar.setSingleCharacter(true);
                        } else {
                            newChar.setEndingCharacter(false);
                        }
                    }
                    if(attributes.getValue(Tags.BEGIN.toString())!=null) {
                        occurancetemp = Double.valueOf(attributes.getValue(Tags.BEGIN.toString()));
                        if (occurancetemp > 0) {
                            newChar.setBeginOccurance(occurancetemp);
                            newChar.setBeginningCharacter(true);
                        } else {
                            newChar.setBeginningCharacter(false);
                        }
                    }
                    if(attributes.getValue(Tags.MIDDLE.toString())!=null) {
                        occurancetemp = Double.valueOf(attributes.getValue(Tags.MIDDLE.toString()));
                        if (occurancetemp > 0) {
                            newChar.setMiddleOccurance(occurancetemp);
                            newChar.setMiddleCharacter(true);
                        } else {
                            newChar.setMiddleCharacter(false);
                        }
                    }
                    if(attributes.getValue(Tags.END.toString())!=null) {
                        occurancetemp = Double.valueOf(attributes.getValue(Tags.END.toString()));
                        if (occurancetemp > 0) {
                            newChar.setEndOccurance(occurancetemp);
                            newChar.setEndingCharacter(true);
                        } else {
                            newChar.setEndingCharacter(false);
                        }
                    }

                }
                if(attributes.getValue(Tags.ABSOCC.toString())!=null && !attributes.getValue(Tags.ABSOCC.toString()).isEmpty())
                    this.newChar.setAbsOccurance(Double.valueOf(attributes.getValue(Tags.ABSOCC.toString())));
                //System.out.println("RelativeOccurance: "+attributes.getValue(Tags.RELOCC.toString()));
                if(attributes.getValue(Tags.RELOCC.toString()) !=null && !attributes.getValue(Tags.RELOCC.toString()).isEmpty())
                    this.newChar.setRelativeOccuranceFromDict(Double.valueOf(attributes.getValue(Tags.RELOCC.toString())));
                //System.out.println("RelativeOccurance: "+this.newChar.getRelativeOccurance());
                break;
            case Tags.EPOCH:
                this.currentepoch=new Epoch((attributes.getValue("name")!=null?attributes.getValue("name"):""),(attributes.getValue("uri")!=null?attributes.getValue("uri"):""),Integer.valueOf(attributes.getValue("start")),Integer.valueOf(attributes.getValue("end")),(attributes.getValue("mainlocation")!=null?attributes.getValue("mainlocation"):""));
                this.newChar.addEpoch(this.currentepoch);
                this.epochbool=true;
                break;
            case Tags.DIALECT:
                this.currentdialect=new Dialect((attributes.getValue("name")!=null?attributes.getValue("name"):""),(attributes.getValue("uri")!=null?attributes.getValue("uri"):""));
                this.dialectbool=true;
                this.newChar.addDialect(this.currentdialect);
                break;
            case Tags.PATTERN:
                this.morphpatternbool=true;
                this.morphpattern=new MorphPattern();
                this.morphpattern.gender=attributes.getValue("gender")!=null?attributes.getValue("gender"):"";
                this.morphpattern.number=attributes.getValue("number")!=null?attributes.getValue("number"):"";
                this.morphpattern.animacy=attributes.getValue("animacy")!=null?attributes.getValue("animacy"):"";
                this.morphpattern.tense=attributes.getValue("tense")!=null?attributes.getValue("tense"):"";
                this.morphpattern.transprefix=attributes.getValue("transprefix")!=null?attributes.getValue("transprefix"):"";
                this.morphpattern.transsuffix=attributes.getValue("transsuffix")!=null?attributes.getValue("transsuffix"):"";
                this.morphpattern.person=attributes.getValue("person")!=null?attributes.getValue("person"):"";
                this.morphpattern.mood=attributes.getValue("mood")!=null?attributes.getValue("mood"):"";
                this.morphpattern.mood=attributes.getValue("voice")!=null?attributes.getValue("voice"):"";
                this.morphpattern.representation =attributes.getValue("rep")!=null?attributes.getValue("rep"):"";
                this.morphpattern.wordcase=attributes.getValue("wordcase")!=null?attributes.getValue("wordcase"):"";
                this.morphpattern.postag=attributes.getValue("pos")!=null?attributes.getValue("pos"):"";
                break;
            case Tags.TRANSLITERATION:
                this.temptranslit = new Transliteration("", "");
                if(attributes.getValue("stem")!=null){
                    this.temptranslit.setStem(Boolean.valueOf(attributes.getValue("stem")));
                }
                this.temptranslit.setAbsoluteOccurance(attributes.getValue(Tags.ABSOCC.toString())==null?0.:Double.valueOf(attributes.getValue(Tags.ABSOCC.toString())));
                //System.out.println("RelativeOccurance: "+attributes.getValue(Tags.RELOCC.toString()));
                this.temptranslit.setRelativeOccuranceFromDict(attributes.getValue(Tags.RELOCC.toString())==null?0.:Double.valueOf(attributes.getValue(Tags.RELOCC.toString())));
                //System.out.println("RelativeOccurance: "+this.temptranslit.getRelativeOccurance());
                if (this.mapOrDict == Options.FILLMAP) {
                    Double temp;
                    if(attributes.getValue(Tags.BEGIN.toString())!=null) {
                        temp = Double.valueOf(attributes.getValue(Tags.BEGIN.toString()));
                        this.temptranslit.setBeginTransliteration(attributes.getValue(Tags.BEGIN.toString()) == null ? false : Boolean.valueOf(attributes.getValue(Tags.BEGIN.toString())), temp.intValue());
                    }
                    if(attributes.getValue(Tags.MIDDLE.toString())!=null) {
                        temp = Double.valueOf(attributes.getValue(Tags.MIDDLE.toString()));
                        this.temptranslit.setMiddleTransliteration(Boolean.valueOf(attributes.getValue(Tags.MIDDLE.toString())), temp.intValue());
                    }
                    if(attributes.getValue(Tags.END.toString())!=null) {
                        temp = Double.valueOf(attributes.getValue(Tags.END.toString()));
                        this.temptranslit.setEndTransliteration(Boolean.valueOf(attributes.getValue(Tags.END.toString())), temp.intValue());
                    }
                    if(attributes.getValue(Tags.SINGLE.toString())!=null) {
                        temp = Double.valueOf(attributes.getValue(Tags.SINGLE.toString()));
                        this.temptranslit.setSingleTransliteration(Boolean.valueOf(attributes.getValue(Tags.SINGLE.toString())), temp.intValue());
                    }
                }
                this.temptranslit.setIsWord(attributes.getValue(Tags.ISWORD.toString())==null?false:Boolean.valueOf(attributes.getValue(Tags.ISWORD.toString())));
                this.temptranslit.setTranscription(attributes.getValue(Tags.TRANSCRIPTION.toString())==null?"":attributes.getValue(Tags.TRANSCRIPTION.toString()));

                this.transliteration=true;break;
            case Tags.TRANSLATION:
                this.temptranslat=new Translation("", new Locale(attributes.getValue(Tags.LOCALE.toString())));
                this.translation=true;break;
            case Tags.POSTAG:
                this.postaguri=attributes.getValue("uri");
                this.postag=true;
                    break;
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

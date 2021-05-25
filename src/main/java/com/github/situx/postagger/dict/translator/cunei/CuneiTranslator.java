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

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.dict.utils.Translation;
import com.github.situx.postagger.main.gui.util.HighlightData;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.util.enums.pos.POSTags;
import com.github.situx.postagger.util.enums.pos.PersonNumberCases;
import com.github.situx.postagger.util.enums.pos.WordCase;
import org.apache.commons.lang3.StringUtils;


import simplenlg.features.*;
import simplenlg.framework.NLGFactory;
import simplenlg.phrasespec.NPPhraseSpec;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.Realiser;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by timo on 13.10.14.
 */
public abstract class CuneiTranslator extends Translator {

    protected NLGFactory factory;
    protected Realiser realiser;
    protected Boolean manyhighlights;
    protected Map<String,String> detlist;
    protected Map<PersonNumberCases,String> personCasesToWords;
    protected Map<WordCase,String> wordCasesToWords;
    protected static Pattern vowelRegex=Pattern.compile("^[aeiou].*$");

    public CuneiTranslator(CharTypes charTypes, POSTagger frompos){
        this.posTagger=frompos;
        this.dictHandler=charTypes.getCorpusHandlerAPI().getUtilDictHandler();
        this.lastWritten="";
        this.lineToWordCount=new TreeMap<>();
        this.length=new LinkedList<>();
    }

    protected String createNEStr(String word, LangChar translations, Locale locale, String stem){
        System.out.println("CreateNEString: "+word);
        String det="";
        for(String log:(((CuneiDictHandler)this.dictHandler).getLogographs()).keySet()){
            if(word.contains(log+"-") && (((CuneiDictHandler)this.dictHandler).getLogographs())!=null &&
                    (((CuneiDictHandler)this.dictHandler).getLogographs()).get(log)!=null &&
                    (((CuneiDictHandler)this.dictHandler).getLogographs()).get(log).getTranslationSet(locale)!=null &&
                    !(((CuneiDictHandler)this.dictHandler).getLogographs()).get(log).getTranslationSet(locale).isEmpty()){
                System.out.println("NEReplace: "+word+" "+log+" - "+(((CuneiDictHandler)this.dictHandler).getLogographs()).get(log).getTranslationSet(locale).keySet().iterator().next().getTranslation()+" ");
                det=(((CuneiDictHandler)this.dictHandler).getLogographs()).get(log).getTranslationSet(locale).keySet().iterator().next().getTranslation()+" ";
                word=word.replaceFirst(log,"");
                if(word.startsWith("-")){
                    word=word.substring(1);
                }
                break;
            }
        }
        return det+(word.isEmpty()?"":Character.toString(word.substring(0,1).charAt(0))
                .toUpperCase()+word.substring(1,word.length()).replace("a-a-a","aja").replace("e-e-e","eje").replaceAll("-","").replaceAll("[0-9]","")
                .replaceAll("[a]+","a").replaceAll("[e]+","e").replaceAll("[u]+","u").replaceAll("[i]+","i").toLowerCase());
    }

    protected List<Tuple<String,POSDefinition>> handleNamedEntity(String word, LangChar translations, POSDefinition def, final List<POSDefinition> defs, Locale locale){
        System.out.println("handleNamedEntity============================================"+word+" - "+def.getVerbStem());
        List<Tuple<String,POSDefinition>> resultList=new LinkedList<>();
        resultList.add(new Tuple<>(this.getRightNounConfiguration(def, this.createNEStr(def.getVerbStem()!=null?def.getVerbStem():word,translations,locale,def.getVerbStem())),def));
        System.out.println("ResultList: "+resultList);
        return resultList;
    }

    protected List<Tuple<String,POSDefinition>> handleVerb(String word, LangChar translations, POSDefinition def, final List<POSDefinition> defs, Locale locale){
        System.out.println("handleVerb============================================"+word+" - "+def.getVerbStem());
        List<Tuple<String,POSDefinition>> resultList=new LinkedList<>();
        if(def.getValue().length==0 || (def.getVerbStem()!=null && !def.getVerbStem().isEmpty())){
            if(translations!=null){
                resultList.add(new Tuple<>(this.getRightVerbConfiguration(def, translations.getFirstTranslation(locale)),def));
            }else{
                if(def.getVerbStem()!=null){
                    System.out.println("Stem: "+def.getVerbStem());
                    //System.out.println("Dict: "+this.dictHandler.getTranslitToWordDict().toString());
                    translations=this.dictHandler.matchWordByTransliteration(def.getVerbStem());
                    if(translations==null){
                        translations=this.dictHandler.matchWord(def.getVerbStem());
                    }
                    if(translations!=null){
                        resultList.add(new Tuple<>(this.getRightVerbConfiguration(def, translations.getFirstTranslation(locale)),def));
                    } else{
                        resultList.add(new Tuple<>(this.getRightVerbConfiguration(def, "(" + word.replace("[", "").replace("]", "") + ")"), def));
                    }
                }else{
                    resultList.add(new Tuple<>(this.getRightVerbConfiguration(def,"("+word.replace("[","").replace("]","")+")"),def));
                }
            }
            return resultList;
        }
        List<String> trans=new LinkedList<>();
        int i=0;
        for(String val:def.getValue()){
            if(i++==0){
                trans.add(this.getRightVerbConfiguration(def, val));
            }else{
                trans.add(this.getRightVerbConfiguration(def, val.trim()));
            }
        }
        //resultList.add(new Tuple<String, POSDefinition>(def.getValue()[0],def));
        resultList.addAll(trans.stream().map(str -> new Tuple<>(str, def)).collect(Collectors.toList()));
        return resultList;
    }

    protected List<Tuple<String,POSDefinition>> handleNoun(String word,String stem, LangChar translations, POSDefinition def, Locale locale){
        System.out.println("HandleNoun============================"+word+" - "+def.getVerbStem());
        List<Tuple<String,POSDefinition>> resultList=new LinkedList<>();
        //System.out.println("Logographs: "+((CuneiDictHandler)this.dictHandler).getLogographs());
        if(def.getValue().length==0){
            if(translations!=null && translations.getTranslations()!=null && !translations.getTranslations().isEmpty()){
                //System.out.println("Has translations: "+translations.getTranslations()+" - "+translations.getTransliterationSet());
                for(Translation trans:translations.getTranslationSet(locale).keySet()){
                    resultList.add(new Tuple<>(this.getRightNounConfiguration(def, trans.getTranslation()),def));
                }
            }else if(stem!=null && ((CuneiDictHandler)this.dictHandler).getLogographs()!=null
                    && ((CuneiDictHandler)this.dictHandler).getLogographs().containsKey(stem)
                    && ((CuneiDictHandler)this.dictHandler).getLogographs().get(stem)!=null){
                //System.out.println("Has translations: "+translations.getTranslations()+" - "+translations.getTransliterationSet());
                resultList.add(new Tuple<>(this.getRightNounConfiguration(def, ((CuneiDictHandler) this.dictHandler).getLogographs().get(stem).getFirstTranslation(locale)), def));
            }else{
                //System.out.println("Does not have translations -- checking stem "+stem);
                if(stem!=null){
                    translations=this.dictHandler.matchWordByPOSandTransliteration(stem,def.getPosTag());
                    if(translations==null){
                        System.out.println("Stem "+stem+" has no translations");
                        if(((CuneiDictHandler)this.dictHandler).getLogographs().containsKey(stem)){
                            translations=((CuneiDictHandler)this.dictHandler).getLogographs().get(stem);
                        }else {
                            translations = this.dictHandler.matchWord(stem);
                        }
                    }
                    if(translations!=null && translations.getTranslations()!=null && !translations.getTranslations().isEmpty()){
                        System.out.println("Stem "+stem+" has translations "+translations);
                        resultList.add(new Tuple<>(this.getRightNounConfiguration(def, translations.getFirstTranslation(locale)),def));
                    } else{
                        resultList.add(new Tuple<>(this.getRightNounConfiguration(def,"(" + word.replace("[", "").replace("]", "") + ")"), def));
                    }
                }else{
                    resultList.add(new Tuple<>(this.getRightNounConfiguration(def,"(" + word.replace("[", "").replace("]", "") + ")"),def));
                }
            }
            return resultList;
        }
        List<String> trans=new LinkedList<>();
        for(String val:def.getValue()){
            trans.add(val.trim());
        }
        //resultList.add(new Tuple<String, POSDefinition>(def.getValue()[0],def));
        resultList.addAll(trans.stream().map(str -> new Tuple<>(str, def)).collect(Collectors.toList()));
        System.out.println("===================================HandleNoun");
        return resultList;
    }


    public List<Tuple<String,POSDefinition>> handleNumber(POSDefinition def,String word){
        List<Tuple<String,POSDefinition>> resultList=new LinkedList<>();
        if(lasttranslation!=null && lasttranslation.getTag().equals(def.getTag())){
            result.delete(result.length()-lastWritten.length(),result.length());
            //result=result.substring(0,result.length()-lastWritten.length());
            this.length.remove(length.size()-1);
            this.currentpos-=lastWritten.length();
            resultList.add(new Tuple<>(Integer.valueOf(lastWritten.trim())+Integer.valueOf(def.getValue()[0])*(StringUtils.countMatches(word,"-")+1)+"",def));
            return resultList;
        }
        resultList.add(new Tuple<>(Integer.valueOf(def.getValue()[0])*(StringUtils.countMatches(word,"-")+1)+"",def));
        return resultList;
    }

    public String wordByWordPOStranslate(String translationText,Boolean pinyin,Integer initialPos){
        this.result=new StringBuilder();
        this.length=new LinkedList<>();
        this.currentpos=initialPos;
        this.lastWritten="";
        this.lasttranslation=null;
        LangChar tempword;
        List<Tuple<String,POSDefinition>> res=new LinkedList<>();
        this.lineToWordCount.put(linecount,wordcount);
            for(String word:translationText.split(" ")){
                this.manyhighlights=false;
                word=word.replace("[","").replace("]","");
                System.out.println("Word: "+word);
                List<POSDefinition> defs=this.posTagger.getPosTagDefs(word,dictHandler);
                tempword=dictHandler.matchWordByTransliteration(word);
                res.clear();
                if(defs.isEmpty()){
                    lastWritten="("+word+") ";
                    this.lastlasttranslation=lasttranslation;
                    lasttranslation=new POSDefinition("","","","",new String[0],"UNKNOWN","","","","","",new TreeMap<>(),this.posTagger.getCharType());
                    result.append(lastWritten);
                    this.length.add(new HighlightData(this.currentpos,this.currentpos+=lastWritten.length(),"DEFAULT",lasttranslation,new LinkedList<>(),word,false,null));
                    continue;
                }
                else if(defs.size()>0){
                    res=this.POSTagToRule(defs,word,tempword);
                    System.out.println("Wordres: "+res);
                    if(this.manyhighlights){
                        //Boolean doubleClass=!this.checkForDoubleClassification(defs);
                        for(Tuple<String,POSDefinition> tup:res){
                            this.lastWritten=tup.getOne()+" ";
                            this.lastlasttranslation=lasttranslation;
                            this.lasttranslation=tup.getTwo();
                            result.append(lastWritten);
                            this.length.add(new HighlightData(this.currentpos,this.currentpos+=lastWritten.length(),tup.getTwo().getDesc(),lasttranslation,res,word,true,null));
                        }
                    }else{
                        if(!res.isEmpty()) {
                            this.lastWritten = res.get(0).getOne() + " ";
                            this.lastlasttranslation = lasttranslation;
                            this.lasttranslation = defs.get(0);
                            result.append(lastWritten);
                            HighlightData toAdd = new HighlightData(this.currentpos, this.currentpos += lastWritten.length(), defs.get(0).getDesc(), lasttranslation, res, word, false, null);
                            this.length.add(toAdd);
                        }
                    }
                }
                this.wordcount++;
                System.out.println("Wordcount: "+wordcount);
            }
        this.linecount++;
        return result.toString();
    }

    protected void getWordTranslation(String word){
        word=word.replace("[","").replace("]","");
        LangChar tempword=null;
        System.out.println("Word: "+word);
        List<POSDefinition> defs=this.posTagger.getPosTagDefs(word,dictHandler);
        if(defs.isEmpty()){
            lastWritten="("+word+") ";
            this.lastlasttranslation=lasttranslation;
            lasttranslation=new POSDefinition("","","","",new String[0],"UNKNOWN","","","","","",new TreeMap<>(),this.posTagger.getCharType());
            result.append(lastWritten);
            this.length.add(new HighlightData(this.currentpos,this.currentpos+=lastWritten.length(),"DEFAULT",lasttranslation,new LinkedList<>(),word,false,null));
        }
        else if(defs.size()>0){
            this.lastWritten=this.POSTagToRule(defs,word,tempword).get(0).getOne()+" ";
            this.lastlasttranslation=lasttranslation;
            this.lasttranslation=defs.get(0);
            result.append(lastWritten);
        }
        this.wordcount++;
        List<Tuple<String,POSDefinition>> addlist=new LinkedList<>();
        addlist.add(new Tuple<>(word,defs.get(0)));
        this.length.add(new HighlightData(this.currentpos,this.currentpos+=lastWritten.length(),defs.get(0).getDesc(),lasttranslation,addlist,word,defs.size()>1,null));

    }

    protected Boolean checkForDoubleClassification(List<POSDefinition> posdefs){
        Set<String> seenStrings=new TreeSet<>();
        for(POSDefinition def:posdefs){
            System.out.println("SeenStrings: "+seenStrings);
            System.out.println("CurrentWord: "+def.currentword);
            if(seenStrings.contains(def.currentword)){
                return true;
            }else{
                seenStrings.add(def.currentword);
            }
        }
        return false;
    }

    protected abstract List<Tuple<String,POSDefinition>> POSTagToRule(List<POSDefinition> defs,String word,LangChar translations);

    protected String getRightNounConfiguration(final POSDefinition def, String trans){
        Boolean modified=false,plural=false;
        System.out.println("GetRightNounConfiguration for: "+trans);
        StringBuilder translation=new StringBuilder();
        NPPhraseSpec p = this.factory.createNounPhrase();
        p.setNoun(trans);
        p.getNoun().setPlural(def.getWordCase().contains(WordCase.PLURAL));
        System.out.println("Posdef: "+def.toString());
        System.out.println("Enumset: "+def.getWordCase().toString());
        System.out.println("PersonNumberCase: "+def.getPersonNumberCase());
        if(def.getPersonNumberCase()!=PersonNumberCases.NONE) {
            modified=true;
            switch (def.getPersonNumberCase()) {
                case FIRST_SINGULAR:
                    p.setSpecifier(this.personCasesToWords.get(PersonNumberCases.FIRST_SINGULAR));
                    p.getSpecifier().setFeature(Feature.POSSESSIVE, true);
                    p.getSpecifier().setFeature(Feature.PERSON, Person.FIRST);
                    p.getSpecifier().setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case SECOND_SINGULAR:
                case SECOND_SINGULAR_FEMALE:
                    p.setSpecifier(this.personCasesToWords.get(PersonNumberCases.SECOND_SINGULAR));
                    p.getSpecifier().setFeature(Feature.POSSESSIVE, true);
                    p.getSpecifier().setFeature(Feature.PERSON, Person.SECOND);
                    p.getSpecifier().setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case THIRD_SINGULAR:
                case THIRD_SINGULAR_MALE:
                    p.setSpecifier(this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR));
                    p.getSpecifier().setFeature(Feature.POSSESSIVE, true);

                    p.getSpecifier().setFeature(Feature.PERSON, Person.THIRD);
                    p.getSpecifier().setFeature(simplenlg.features.Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case THIRD_SINGULAR_FEMALE:
                    p.setSpecifier(this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR_FEMALE));
                    p.getSpecifier().setFeature(Feature.POSSESSIVE, true);
                    p.getSpecifier().setFeature(Feature.PERSON, Person.THIRD);
                    p.getSpecifier().setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case THIRD_SINGULAR_THING:
                    p.setSpecifier(this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR_THING));
                    p.getSpecifier().setFeature(Feature.POSSESSIVE, true);
                    p.getSpecifier().setFeature(Feature.PERSON, Person.THIRD);
                    p.getSpecifier().setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case FIRST_PLURAL:
                    p.setSpecifier(this.personCasesToWords.get(PersonNumberCases.FIRST_PLURAL));
                    p.getSpecifier().setFeature(Feature.POSSESSIVE, true);
                    p.getSpecifier().setFeature(Feature.PERSON, Person.FIRST);
                    p.getSpecifier().setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    break;
                case SECOND_PLURAL:
                    p.setSpecifier(this.personCasesToWords.get(PersonNumberCases.SECOND_PLURAL));
                    p.getSpecifier().setFeature(Feature.POSSESSIVE, true);
                    p.getSpecifier().setFeature(Feature.PERSON, Person.SECOND);
                    p.getSpecifier().setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    break;
                case THIRD_PLURAL:
                case THIRD_PLURAL_MALE:
                case THIRD_PLURAL_FEMALE:
                case THIRD_PLURAL_THING:
                    p.setSpecifier(this.personCasesToWords.get(PersonNumberCases.THIRD_PLURAL));
                    p.getSpecifier().setFeature(Feature.POSSESSIVE, true);
                    p.getSpecifier().setFeature(Feature.PERSON, Person.THIRD);
                    p.getSpecifier().setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    break;
                default:
            }
        }
        if(def.getWordCase().contains(WordCase.PLURAL)){
            plural=true;
            p.setFeature(Feature.NUMBER,NumberAgreement.PLURAL);
        }
        if(def.getWordCase().contains(WordCase.GENITIVE)){
            modified=true;
            translation.append(this.wordCasesToWords.get(WordCase.GENITIVE));
        }
        if(def.getWordCase().contains(WordCase.DATIVE)){
            modified=true;
            translation.append(this.wordCasesToWords.get(WordCase.DATIVE));
        }
        if(def.getWordCase().contains(WordCase.LOCATIVE)){
            modified=true;
            translation.append(this.wordCasesToWords.get(WordCase.LOCATIVE));
        }
        if(def.getWordCase().contains(WordCase.TERMINATIVE)){
            modified=true;
            translation.append(this.wordCasesToWords.get(WordCase.TERMINATIVE));
        }
        if(def.getWordCase().contains(WordCase.ABLATIVE)){
            modified=true;
            translation.append(this.wordCasesToWords.get(WordCase.ABLATIVE));
        }
        translation.append(realiser.realise(p).getRealisation().toString());
        if(def.getWordCase().contains(WordCase.COMITATIVE)){
            modified=true;
            translation.append(this.wordCasesToWords.get(WordCase.COMITATIVE));
        }
        if(def.getWordCase().contains(WordCase.ISPART)){
            modified=true;
            translation.append(this.wordCasesToWords.get(WordCase.ISPART));
        }
        if(!modified && def.getPosTag().equals(POSTags.NOUN)){
            System.out.println("Trans: "+trans+" - "+vowelRegex.matcher(trans).matches());
            if(plural){
                translation.insert(0,"the ");
            }else if(vowelRegex.matcher(trans).matches()){
                translation.insert(0,"an ");
            }else{
                translation.insert(0,"a ");
            }
        }

        System.out.println("Noun Translation: "+translation.toString());
        return translation.toString().trim();
    }


    protected String getRightVerbConfiguration(final POSDefinition def,String trans){
        System.out.println("Verb Config: "+trans);
        StringBuilder translation=new StringBuilder();
        SPhraseSpec p = this.factory.createClause();
        if(def.getExtrainfo().substring(def.getExtrainfo().lastIndexOf(":")+1).trim().equals("Sz")){
            p.setVerb("cause to "+trans);
        }else if(def.getExtrainfo().substring(def.getExtrainfo().lastIndexOf(":")+1).trim().equals("D")){
            p.setVerb("make "+trans);
        }else{
            p.setVerb(trans);
        }
        if(def.getPersonNumberCase()==null && this.lasttranslation!=null && lasttranslation.getPosTag().equals(POSTags.PRONOUN) && lasttranslation.getClassification()!=null){
            System.out.println("PRONOUN: "+this.lasttranslation.getValue()[0]);
            if(!lasttranslation.getClassification().isEmpty() && lasttranslation.getClassification().contains("_"))
                def.setPersonNumberCase(PersonNumberCases.valueOf(lasttranslation.getClassification().substring(0, lasttranslation.getClassification().indexOf("_", lasttranslation.getClassification().indexOf("_") + 1))));
        }
        if(def.getWordCase()!= null){
            for(WordCase caase:def.getWordCase()) {
                switch (caase) {
                    case NEGATIVE:
                        translation.append(this.wordCasesToWords.get(WordCase.NEGATIVE));
                        break;
                    case GENITIVE:
                    case ACCUSATIVE:
                        if(def.getObjectPersonCase()!= PersonNumberCases.NONE){
                            p.setIndirectObject(this.personCasesToWords.get(def.getObjectPersonCase()));
                        }else{
                            p.setIndirectObject(this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR));
                        }
                        break;
                    case DATIVE:
                        if(def.getAgensPersonCase()!= PersonNumberCases.NONE){
                            p.setIndirectObject(this.personCasesToWords.get(def.getAgensPersonCase()));
                        }else{
                            p.setIndirectObject(this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR));
                        }
                        break;
                    default:
                }
            }
        }
        System.out.println("LastTranslation: "+(this.lasttranslation!=null?this.lasttranslation.getPersonNumberCase():"null"));
        System.out.println("WORDCASE: "+def.getPersonNumberCase());
        if(def.getPersonNumberCase()!=null){
            switch (def.getPersonNumberCase()){
                case FIRST_SINGULAR:
                    p.setSubject(this.personCasesToWords.get(PersonNumberCases.FIRST_SINGULAR));
                    p.setFeature(Feature.PRONOMINAL,true);
                    p.setFeature(Feature.PERSON, Person.FIRST);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case SECOND_SINGULAR:
                case SECOND_SINGULAR_FEMALE:
                    p.setSubject(this.personCasesToWords.get(PersonNumberCases.SECOND_SINGULAR));
                    p.setFeature(Feature.PRONOMINAL,true);
                    p.setFeature(Feature.PERSON,Person.SECOND);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case THIRD_SINGULAR:
                case THIRD_SINGULAR_MALE:
                    p.setSubject(this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR));
                    p.setFeature(Feature.PRONOMINAL, true);
                    p.setFeature(Feature.PERSON,Person.THIRD);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case THIRD_SINGULAR_FEMALE:
                    p.setFeature(Feature.PRONOMINAL,true);
                    p.setSubject(this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR_FEMALE));
                    p.setFeature(Feature.PERSON,Person.THIRD);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case THIRD_SINGULAR_THING:
                    p.setFeature(Feature.PRONOMINAL,true);
                    p.setSubject(this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR_THING));
                    p.setFeature(Feature.PERSON,Person.THIRD);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case FIRST_PLURAL:
                    p.setFeature(Feature.PRONOMINAL,true);
                    p.setSubject(this.personCasesToWords.get(PersonNumberCases.FIRST_PLURAL));
                    p.setFeature(Feature.PERSON,Person.FIRST);
                    p.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    break;
                case SECOND_PLURAL:
                    p.setFeature(Feature.PRONOMINAL,true);
                    p.setSubject(this.personCasesToWords.get(PersonNumberCases.SECOND_PLURAL));
                    p.setFeature(Feature.PERSON,Person.SECOND);
                    p.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    break;
                case THIRD_PLURAL:
                case THIRD_PLURAL_MALE:
                case THIRD_PLURAL_FEMALE:
                case THIRD_PLURAL_THING:
                    p.setFeature(Feature.PRONOMINAL,true);
                    p.setSubject(this.personCasesToWords.get(PersonNumberCases.THIRD_PLURAL));
                    p.setFeature(Feature.PERSON,Person.THIRD);
                    p.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    break;
            }
        }
        if(def.getTense()!=null){
            switch (def.getTense()){
                case PAST:
                    p.setFeature(Feature.TENSE, Tense.PAST);
                    break;
                case PERFECT:
                    p.setFeature(Feature.PERFECT,true);
                    break;
                case FUTURE:
                    p.setFeature(Feature.TENSE,Tense.FUTURE);
                    break;
                case PRESENT:
                    p.setFeature(Feature.TENSE, Tense.PRESENT);
                    break;
                case IMPERATIVE:
                    p.setFeature(Feature.FORM, Form.IMPERATIVE);
                    break;
            }
        }
        switch (def.getExtrainfo().substring(def.getExtrainfo().lastIndexOf(":")+1).trim()){
            case "N":
                p.setFeature(Feature.PASSIVE,true);
                break;
            case "Gtn":
                translation.append("keep ");
                p.setFeature(Feature.PROGRESSIVE,true);
                break;
        }
        System.out.println("Groupresults: " + def.getCurrentgroupResults().toString());
        translation.append(realiser.realise(p).getRealisation().toString());
        if(def.getExtrainfo().substring(def.getExtrainfo().lastIndexOf(":")+1).trim().equals("Gt"))
            translation.append(" one another");
        if(def.getWordCase().contains(WordCase.VENTIVE)){
            translation.append(this.wordCasesToWords.get(WordCase.VENTIVE));
        }
        System.out.println("Translation: "+translation);
        return translation.toString();
    }


    protected String handleConjugation(final POSDefinition def,String trans){
        System.out.println("Conjunction: " + trans);
        StringBuilder translation=new StringBuilder();
        SPhraseSpec p=this.factory.createClause();
        translation.append(trans);

        /*if(def.getPersonNumberCase()==null && this.lasttranslation!=null && lasttranslation.getPosTag().equals(POSTags.PRONOUN) && lasttranslation.getClassification()!=null){
            System.out.println("PRONOUN: "+this.lasttranslation.getValue()[0]);
            if(!lasttranslation.getClassification().isEmpty() && lasttranslation.getClassification().contains("_"))
                def.setPersonNumberCase(PersonNumberCases.valueOf(lasttranslation.getClassification().substring(0, lasttranslation.getClassification().indexOf("_", lasttranslation.getClassification().indexOf("_") + 1))));
        }*/
        if(def.getWordCase()!= null && def.getWordCase().contains(WordCase.DATIVE)){
            if(def.getAgensPersonCase()!= PersonNumberCases.NONE){
                p.setIndirectObject(this.personCasesToWords.get(def.getAgensPersonCase()));
            }else{
                p.setIndirectObject(this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR));
            }
        }
        System.out.println("LastTranslation: "+(this.lasttranslation!=null?this.lasttranslation.getPersonNumberCase():"null"));
        System.out.println("WORDCASE: "+def.getPersonNumberCase());
        if(def.getPersonNumberCase()!=null){
            switch (def.getPersonNumberCase()){
                case FIRST_SINGULAR:
                    translation.append(" "+this.personCasesToWords.get(PersonNumberCases.FIRST_SINGULAR));
                    p.setFeature(Feature.PERSON, Person.FIRST);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case SECOND_SINGULAR:
                case SECOND_SINGULAR_FEMALE:
                    translation.append(" "+this.personCasesToWords.get(PersonNumberCases.SECOND_SINGULAR));
                    p.setFeature(Feature.PERSON,Person.SECOND);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case THIRD_SINGULAR:
                case THIRD_SINGULAR_MALE:
                    translation.append(" "+this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR));
                    p.setFeature(Feature.PERSON, Person.THIRD);
                    p.setFeature(Feature.FORM,Form.IMPERATIVE);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case THIRD_SINGULAR_FEMALE:
                    translation.append(" "+this.personCasesToWords.get(PersonNumberCases.THIRD_PLURAL_FEMALE));
                    p.setFeature(Feature.PERSON, Person.THIRD);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case THIRD_SINGULAR_THING:
                    translation.append(" "+this.personCasesToWords.get(PersonNumberCases.THIRD_SINGULAR_THING));
                    p.setFeature(Feature.PERSON,Person.THIRD);
                    p.setFeature(Feature.NUMBER, NumberAgreement.SINGULAR);
                    break;
                case FIRST_PLURAL:
                    translation.append(" "+this.personCasesToWords.get(PersonNumberCases.FIRST_PLURAL));
                    p.setFeature(Feature.PERSON,Person.FIRST);
                    p.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    break;
                case SECOND_PLURAL:
                    translation.append(" "+this.personCasesToWords.get(PersonNumberCases.SECOND_PLURAL));
                    p.setFeature(Feature.PERSON,Person.SECOND);
                    p.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    break;
                case THIRD_PLURAL:
                case THIRD_PLURAL_MALE:
                case THIRD_PLURAL_FEMALE:
                case THIRD_PLURAL_THING:
                    translation.append(" "+this.personCasesToWords.get(PersonNumberCases.THIRD_PLURAL));
                    p.setFeature(Feature.PERSON,Person.THIRD);
                    p.setFeature(Feature.NUMBER, NumberAgreement.PLURAL);
                    break;
                default:
            }
        }
        if(def.getWordCase().contains(WordCase.DATIVE)){
            translation.append(this.wordCasesToWords.get(WordCase.DATIVE));
        }
        if(def.getWordCase().contains(WordCase.LOCATIVE)){
            translation.append(this.wordCasesToWords.get(WordCase.LOCATIVE));
        }
        if(def.getWordCase().contains(WordCase.QUOTATIVE)){
            translation.append(this.wordCasesToWords.get(WordCase.QUOTATIVE));
        }
        /*if(def.getTense()!=null){
            switch (def.getTense()){
                case PAST:
                    p.setFeature(Feature.TENSE, Tense.PAST);
                    break;
                case PERFECT:
                    p.setFeature(Feature.PERFECT,true);
                    break;
                case FUTURE:
                    p.setFeature(Feature.TENSE,Tense.FUTURE);
                    break;
                case PRESENT:
                    p.setFeature(Feature.TENSE,Tense.PRESENT);
                    break;
                case IMPERATIVE:
                    p.setFeature(Feature.FORM, Form.IMPERATIVE);
                    break;
            }
        }*/
        //translation.append(realiser.realise(p).getRealisation().toString());
        System.out.println("Translation: "+translation);
        return translation.toString();
    }

}

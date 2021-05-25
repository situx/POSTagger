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
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.pos.PersonNumberCases;
import com.github.situx.postagger.util.enums.pos.WordCase;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.util.enums.pos.POSTags;
import org.apache.commons.lang3.StringUtils;
import simplenlg.features.Feature;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.Realiser;

import java.util.*;


/**
 * Created by timo on 13.10.14.
 */
public class AkkadToGerTranslator extends CuneiTranslator {


    public AkkadToGerTranslator(CharTypes charTypes){
        super(charTypes,charTypes.getCorpusHandlerAPI().getPOSTagger(false));
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        this.factory = new NLGFactory(lexicon);
        this.realiser = new Realiser(lexicon);
        this.initializePNMap();
    }

    private void initializePNMap(){
        this.personCasesToWords=new TreeMap<>();
        this.personCasesToWords.put(PersonNumberCases.FIRST_SINGULAR,"ich");
        this.personCasesToWords.put(PersonNumberCases.SECOND_SINGULAR,"du");
        this.personCasesToWords.put(PersonNumberCases.THIRD_SINGULAR,"er");
        this.personCasesToWords.put(PersonNumberCases.THIRD_SINGULAR_FEMALE,"sie");
        this.personCasesToWords.put(PersonNumberCases.THIRD_SINGULAR_THING,"es");
        this.personCasesToWords.put(PersonNumberCases.FIRST_SINGULAR,"wir");
        this.personCasesToWords.put(PersonNumberCases.SECOND_PLURAL,"ihr");
        this.personCasesToWords.put(PersonNumberCases.THIRD_PLURAL,"sie");
        this.wordCasesToWords=new TreeMap<>();
        this.wordCasesToWords.put(WordCase.COMITATIVE," mit ");
        this.wordCasesToWords.put(WordCase.LOCATIVE," in ");
        this.wordCasesToWords.put(WordCase.NEGATIVE," nicht ");
        this.wordCasesToWords.put(WordCase.VENTIVE," hier ");
        this.wordCasesToWords.put(WordCase.VETITIVE, " ist verboten ");
        this.wordCasesToWords.put(WordCase.GENITIVE, " von ");
        this.wordCasesToWords.put(WordCase.DATIVE, " für ");
        this.wordCasesToWords.put(WordCase.ABLATIVE," von ");
        this.wordCasesToWords.put(WordCase.EQUATIVE," wie ");
        this.wordCasesToWords.put(WordCase.QUOTATIVE,": ");
    }

    public AkkadToGerTranslator(CharTypes charTypes,POSTagger frompos){
        super(charTypes,frompos);
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        this.factory = new NLGFactory(lexicon);
        this.realiser = new Realiser(lexicon);
        this.initializePNMap();
    }

    public List<Tuple<String,POSDefinition>> POSTagToRule(List<POSDefinition> defs, String word, LangChar translations){
        POSDefinition def=defs.get(0);
        String res;
        List<Tuple<String,POSDefinition>> resultList=new LinkedList<>();
        //System.out.println("GETPOSTAG: "+def.getTag()+" "+word);
        switch (def.getPosTag()){
            case ADJECTIVE:
                if(translations==null){
                    resultList.add(new Tuple<String, POSDefinition>("("+word.replace("[","").replace("]","")+")",def));
                }else{
                    resultList.add(new Tuple<String, POSDefinition>(translations.getFirstTranslation(Locale.ENGLISH),def));
                }
                break;
            case PARTICLE:
                res=def.getValue()[0]+" ";
                if(defs.size()>1){
                    for(int i=1;i<defs.size();i++){
                        switch (defs.get(i).getPosTag()){
                            case NOUN:
                            case NOUNORADJ:
                                this.manyhighlights=true;
                                resultList.addAll(this.createNounStr(word, translations, defs.get(i), defs, Locale.ENGLISH));
                                resultList.add(new Tuple<String, POSDefinition>(res,defs.get(i)));
                                return resultList;
                            case NAMEDENTITY:
                                this.manyhighlights=true;
                                POSDefinition nedef=this.posTagger.getClassifiers().get(this.posTagger.getOrderToPOS().get("NE")).get(0);
                                resultList.add(new Tuple<String, POSDefinition>(this.createNEStr(word.substring(0,word.length()-res.length()),translations,Locale.ENGLISH,nedef.getVerbStem()),nedef));
                                resultList.add(new Tuple<String, POSDefinition>(res,def));
                                return resultList;
                            case DETERMINATIVE:
                                this.manyhighlights=true;
                                res+=this.createDETStr(word,translations,def);
                                resultList.add(new Tuple<String, POSDefinition>(res,defs.get(i)));
                                return resultList;
                            case VERB:
                                this.manyhighlights=true;
                                resultList.add(new Tuple<String, POSDefinition>(res,defs.get(i)));
                                resultList.addAll(handleVerb(word, translations, def, defs, Locale.ENGLISH));
                                return resultList;
                            default:
                        }
                    }
                }
                resultList.add(new Tuple<String, POSDefinition>(res.trim(),def));
                break;
            case POSSESSIVE:
                res=def.getValue()[0]+" ";
                if(defs.size()>1){
                    for(int i=1;i<defs.size();i++){
                        switch (defs.get(i).getPosTag()){
                            case NOUN:
                            case NOUNORADJ:
                                this.manyhighlights=true;
                                resultList.add(new Tuple<String, POSDefinition>(res,defs.get(i)));
                                resultList.addAll(this.createNounStr(word, translations, defs.get(i), defs, Locale.ENGLISH));
                                return resultList;
                            case NAMEDENTITY:
                                this.manyhighlights=true;
                                POSDefinition nedef=this.posTagger.getClassifiers().get(this.posTagger.getOrderToPOS().get("NE")).get(0);
                                resultList.add(new Tuple<String, POSDefinition>(this.createNEStr(word.substring(0,word.length()-res.length()),translations,Locale.ENGLISH,nedef.getVerbStem()),nedef));
                                resultList.add(new Tuple<String, POSDefinition>(res,def));
                                return resultList;
                            case DETERMINATIVE:
                                this.manyhighlights=true;
                                res+=this.createDETStr(word,translations,def);
                                resultList.add(new Tuple<String, POSDefinition>(res,defs.get(i)));
                                return resultList;
                            default:
                        }
                    }

                }
                resultList.add(new Tuple<String, POSDefinition>(res.trim(),def));
                break;
            case NOUN:
                return this.createNounStr(word,translations,def,defs,Locale.ENGLISH);
            case NOUNORADJ:
            /*List<POSDefinition> nedefs=((AkkadPOSTagger)this.posTagger).getPosTag(suffix.replace("[", "").replace("]",""), dictHandler, false);
            System.out.println("Postags: "+nedefs.toString());
            if(!nedefs.isEmpty()) {
                result.add(new Tuple<String, POSDefinition>(suffix, nedefs.get(0)));
                this.manyhighlights = true;
            }*/
                if(def.getValue().length>0){
                    for(String str:def.getValue()){
                        resultList.add(new Tuple<>(str,def));
                    }
                    return resultList;
                }
                if(word.replace("[","").replace("]","").equals("ma-ru-ti")){
                    resultList.add(new Tuple<>("sonship",def));
                    return resultList;
                }
                if(translations!=null){
                    resultList.add(new Tuple<>(translations.getFirstTranslation(Locale.ENGLISH),def));
                }else{
                    resultList.add(new Tuple<>("("+word.replace("[","").replace("]","")+")",def));
                }
                break;
            case NUMBER:
                //System.out.println("LastTranslation: "+lastWritten+" - "+lasttranslation);
                if(lasttranslation!=null && lasttranslation.getTag().equals(def.getTag())){
                    result.delete(result.length()-lastWritten.length(),result.length());
                    //result=result.substring(0,result.length()-lastWritten.length());
                    this.length.remove(length.size()-1);
                    this.currentpos-=lastWritten.length();
                    resultList.add(new Tuple<>(Integer.valueOf(lastWritten.trim())+Integer.valueOf(def.getValue()[0])*(StringUtils.countMatches(word,"-")+1)+"",def));
                    return resultList;
                }
                resultList.add(new Tuple<>(Integer.valueOf(def.getValue()[0])*(StringUtils.countMatches(word,"-")+1)+"",def));
                break;
            case DETERMINATIVE:
                this.manyhighlights=true;
                resultList.addAll(this.createDETStr(word,translations,def));
                break;
            case NAMEDENTITY:
                this.manyhighlights=true;
                resultList.add(new Tuple<>(this.createNEStr(word,translations,Locale.ENGLISH,def.getVerbStem()),def));
                break;
            case PREPOSITION:
                if(lasttranslation!=null && lasttranslation.getTag().equals(def.getTag())){
                    result.delete(result.length()-lastWritten.length(),result.length());
                    //result=result.substring(0,result.length()-lastWritten.length());
                    this.length.remove(length.size()-1);
                    this.currentpos-=lastWritten.length();
                }
                for(String str:def.getValue()){
                    resultList.add(new Tuple<String, POSDefinition>(str,def));
                }
                break;
            case PRONOUN:
                if(lasttranslation!=null && lasttranslation.getPosTag().equals(POSTags.NOUN) && lastlasttranslation!=null && lastlasttranslation.getPosTag().equals(POSTags.PREPOSITION)){
                    result.delete(result.length()-lastWritten.length(),result.length());
                    //result=result.substring(0,result.length()-lastWritten.length());
                    this.length.remove(length.size()-1);
                    this.currentpos-=lastWritten.length();
                    resultList.add(new Tuple<>(def.getValue()[0],def));
                    if(this.lasttranslation.getValue().length>0)
                        resultList.add(new Tuple<>(this.lasttranslation.getValue()[0],this.lasttranslation));
                    this.manyhighlights=true;
                    return resultList;
                }
                for(String str:def.getValue()){
                    resultList.add(new Tuple<>(str,def));
                }
                break;
            case VERB:
                return handleVerb(word,translations,def,defs,Locale.GERMAN);
            default:if(def.getValue().length!=0){
                //resultList.add(new Tuple<String, POSDefinition>(def.getValue()[0],def));
                for(String str:def.getValue()){
                    resultList.add(new Tuple<>(str,def));
                }
            }else{
                resultList.add(new Tuple<>("("+word+") ",def));
            }
        }
        return resultList;
    }


    private List<Tuple<String,POSDefinition>> createDETStr(String word,LangChar translations,POSDefinition def){
        List<Tuple<String,POSDefinition>> resultList=new LinkedList<>();
        if(word.equals("GESZ-KIRI6")) {
            resultList.add(new Tuple<>("Garten",def));
            return resultList;
        }else if("GESZ-BAN".equals(word)){
            resultList.add(new Tuple<>("Bogen",def));
            return resultList;
        }
        String detstranslation="";
        String detcollections="";
        for(String spl:word.split("-")){
            if(Translator.isAllUpperCaseOrNumber(spl)){
                System.out.println("IsAllUpperCaseOrNumber: "+spl);
                detcollections+=spl+"-";
            }else{
                break;
            }
        }
        if(detcollections.isEmpty()){
            return resultList;
        }else{
            System.out.println("Detcollection: "+detcollections);
            switch (detcollections.substring(0,detcollections.length()-1)){
                case "MUNUS":
                    detstranslation+="Frau ";
                    break;
                case "D":
                    detstranslation="der Gott";
                    break;
                case "DISZ-MUNUS":  detstranslation+="Ehemann von";
                    break;
                case "NA4-KISZIB":
                    detstranslation+="Unterzeichnet von";
                    break;
                case "DUMU-MESZ":
                    detstranslation+="Sohn des";
                    break;
                case "KI-MIN":
                    detstranslation+="der Zweite";
                    break;
                case "SZESZ":
                    detstranslation+="Bruder";
                    break;
                case "SZESZ-MESZ":
                    detstranslation+="Brüder";
                    break;
                case "DISZ-D":
                case "DISZ":  detstranslation+="Herr";
                    break;
                case "URU":  detstranslation+="der Ort";
                    break;
                default:
            }
        }
        if(!detstranslation.isEmpty()) {
            resultList.add(new Tuple<>(detstranslation,def));
            word=word.substring(detcollections.substring(0,detcollections.length()-1).length());
        }
        while(word.startsWith("-") || word.startsWith(" ")){
            word=word.substring(1,word.length());
        }
        POSDefinition nedef=this.posTagger.getClassifiers().get(this.posTagger.getOrderToPOS().get("NE")).get(0);
        String origwordend=word.replaceAll("[a-z]+", "").replace("LU2-DI-KU5-", "").replace("LUGAL","").replace("MESZ", "").replaceAll("[0-9]+", "");
        System.out.println("Origwordend: "+origwordend);
        String wordend=word.replaceAll("[a-z]+","").replace("LU2-DI-KU5-", "Richter").replace("LUGAL","der König").replace("MESZ","s").replace("]","").replace("-","").replaceAll("[0-9]+", "");
        wordend=wordend.replaceAll("[A-Z]+", "");
        if(!wordend.isEmpty()){
            word=word.substring(0,word.length()-origwordend.length());//replace(origwordend,"");
        }
        System.out.println("Before WOOOORD: "+word);
        if(!word.isEmpty()){
            word=Character.toString(word.substring(0,1).charAt(0))
                    .toUpperCase()+word.substring(1,word.length()).replace("a-a-a","aja").replace("e-e-e","eje").replaceAll("-","").replaceAll("[0-9]","")
                    .replaceAll("[a]+","a").replaceAll("[e]+","e").replaceAll("[u]+","u").replaceAll("[i]+","i");
            word=Translator.separateConsonants(word);
            resultList.add(new Tuple<>(word,nedef));
            System.out.println("WOOOORD: "+word);
        }
        if(!wordend.isEmpty()){
            resultList.add(new Tuple<>(wordend,def));
        }
        return resultList;
    }

    private List<Tuple<String,POSDefinition>> createNounStr(String word, LangChar translations, POSDefinition def, final List<POSDefinition> defs, Locale locale){
        List<Tuple<String,POSDefinition>> result=new LinkedList<>();
        word=word.replace("[","").replace("]","");
        if(def.getValue().length>0 && locale==Locale.ENGLISH){
            for(String str:def.getValue()){
                result.add(new Tuple<>(str,def));
            }
            return result;
        }
        /*if(!word.startsWith("IGI") && this.sumdict.containsKey(word)){
            for(String str:this.sumdict.get(word)){
                result.add(new Tuple<String, POSDefinition>(str,def));
            }
            return result;
            //return this.sumdict.get(word).toArray(new String[this.sumdict.get(word).size()]);
        }*/
        String solu="";
        String sub = word.split("[^A-Z0-9-]+")[0];
        if(sub!=null) {
            while (sub.endsWith("-")){
                sub=sub.substring(0,sub.length()-1);
            }
            System.out.println("Word to CreateNounStr: "+sub);
            switch (sub) {
                case "AB2":
                    solu = "Kuh";
                    break;
                case "AGA-US2":
                    solu = "Soldat";
                    break;
                case "AN-DUL3":
                    solu = "Schutz";
                    break;
                case "A-SZA3":
                    solu = "Feld";
                    break;
                case "ANSZE":
                    solu = "Esel";
                    break;
                case "DAM-at":
                    solu = "Ehefrau von";
                    break;
                case "DINGIR":
                    solu = "Gott";
                    break;
                case "DUB-SAR":
                    solu = "der Schreiber";
                    break;
                case "DUMU":
                    solu = "Sohn;Sohn von";
                    break;
                case "DUMU-MESZ":
                    solu = "Kind;Kind von";
                    break;
                case "DUMU-MUNUS":
                    solu = "Tochter;Tochter von";
                    break;
                case "E2-GAL":
                    solu = "Palast";
                    break;
                case "GAL-GAL":
                    solu = "riesig";
                    break;
                case "GIRI3":
                    solu = "Fuß";
                    break;
                case "GIN2":
                    solu = "Gin";
                    break;
                case "GU4":
                    solu = "Rind";
                    break;
                case "HI-A":
                    solu = "viele";
                    break;
                case "HE2-GAL2":
                    solu = "Überfluss";
                    break;
                case "IBILA":
                    solu = "Erbe";
                    break;
                case "ITI":
                    solu = "monatlich";
                    break;
                case "KALAM":
                    solu = "Land";
                    break;
                case "KASZ":
                    solu = "Bier";
                    break;
                case "KU3-BABBAR":
                    solu = "Silber";
                    break;
                case "KU3-GI":
                    solu = "Gold";
                    break;
                case "LU":
                    solu = "oder";
                    break;
                case "LUGAL":
                    solu = "König";
                    break;
                case "LUKUR":
                    solu = "Priester";
                    break;
                case "MA-NA":
                    solu = "Mana";
                    break;
                case "NI-GA":
                case "NIGA":
                    solu = "Gefährt";
                    break;
                case "NA4":
                    solu = "unterzeichnet von";
                    break;
                case "NIN-DINGIR":
                    solu = "Hohepriester";
                    break;
                case "NUMUN":
                    solu = "Samen";
                    break;
                case "NU-MU-SU":
                    solu = "Witwe";
                    break;
                case "SAG-GE6":
                    solu = "schwarze Leute";
                    break;
                case "SZAMAN2-LA2":
                    solu = "Lehrling";
                    break;
                case "SZA3":
                    solu = "in";
                    break;
                case "SZU-KU6":
                    solu = "Ration";
                    break;
                case "SZU":
                    solu = "Hand des";
                    break;
                case "SUHUSZ":
                    solu = "Wurzel";
                    break;
                case "U3":
                    solu = "und";
                    break;
                case "U8":
                    solu = "Schafmutter";
                    break;
                case "UDU":
                    solu = "Schaf";
                    break;
                case "UGULA":
                    solu = "General";
                    break;
                case "UR-SAG":
                    solu = "Held";
                    break;
                case "URU":
                    solu = "Stadt";
                    break;
                case "USZUMGAL":
                    solu = "Drache";
                    break;
                default:

            }
        }
        String suffix= "-"+word.replaceAll("[A-Z0-9-]+","");
        if(!suffix.isEmpty()){
            System.out.println("Suffix: "+suffix);
            for(POSDefinition ddef:defs){
                System.out.println("Suffix: "+suffix+" Regex: " + ddef.getRegex().toString()+": "+suffix.matches(ddef.getRegex().toString()));
                if (suffix.matches(ddef.getRegex().toString())){
                    result.add(new Tuple<>((ddef.getValue().length>0?ddef.getValue()[0]:""), ddef));
                    this.manyhighlights = true;
                    break;
                }
            }
            /*List<POSDefinition> nedefs=((AkkadPOSTagger)this.posTagger).getPosTag(suffix.replace("[", "").replace("]",""), dictHandler, false);
            System.out.println("Postags: "+nedefs.toString());
            if(!nedefs.isEmpty()) {
                result.add(new Tuple<String, POSDefinition>(suffix, nedefs.get(0)));
                this.manyhighlights = true;
            }*/
        }
        if(word.replace("[","").replace("]","").startsWith("EME-")){
            solu="Aussage";
        }
        else if(word.replace("[","").replace("]","").startsWith("SZESZ-")){
            solu="Bruder";
        }
        else if(word.replace("[","").replace("]","").startsWith("NIG2-BA")){
            solu="Geschenk";
        }else if(word.replace("[","").replace("]","").matches("LU2(-)?")){
            solu="Person";
        }else if(word.replace("[","").replace("]","").matches("IGI(-)?")){
            solu="Zeuge";
        }

        word=word.replace(suffix,"");
        while (word.endsWith("-")){
            word=word.substring(1,word.length());
        }
        if(solu.isEmpty()){
            result.add(new Tuple<>("("+word.replace("[", "").replace("]","")+")",def));
        }else{
            SPhraseSpec p = this.factory.createClause();
            p.setSubject(solu);
            p.setFeature(Feature.FORM, Feature.POSSESSIVE);
            result.add(new Tuple<>(solu,def));
        }
        return result;
    }

}


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
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.pos.POSTags;
import com.github.situx.postagger.util.enums.pos.PersonNumberCases;
import com.github.situx.postagger.util.enums.pos.WordCase;
import org.apache.commons.lang3.StringUtils;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.realiser.english.Realiser;

import java.util.*;
import java.util.List;

/**
 * Created by timo on 13.10.14.
 */
public class AkkadToEngTranslator extends CuneiTranslator {

    public AkkadToEngTranslator(CharTypes charTypes,POSTagger frompos){
        super(charTypes,frompos);
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        this.factory = new NLGFactory(lexicon);
        this.realiser = new Realiser(lexicon);
        this.initializePNMap();
    }

    public AkkadToEngTranslator(CharTypes charTypes){
        super(charTypes,charTypes.getCorpusHandlerAPI().getPOSTagger(false));
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        this.factory = new NLGFactory(lexicon);
        this.realiser = new Realiser(lexicon);
        this.initializePNMap();
    }

    private void initializePNMap(){
        this.personCasesToWords=new TreeMap<>();
        this.personCasesToWords.put(PersonNumberCases.FIRST_SINGULAR,"I");
        this.personCasesToWords.put(PersonNumberCases.SECOND_SINGULAR,"you");
        this.personCasesToWords.put(PersonNumberCases.THIRD_SINGULAR,"he");
        this.personCasesToWords.put(PersonNumberCases.THIRD_SINGULAR_FEMALE,"she");
        this.personCasesToWords.put(PersonNumberCases.THIRD_SINGULAR_THING,"it");
        this.personCasesToWords.put(PersonNumberCases.FIRST_PLURAL,"we");
        this.personCasesToWords.put(PersonNumberCases.SECOND_PLURAL,"you");
        this.personCasesToWords.put(PersonNumberCases.THIRD_PLURAL,"they");
        this.wordCasesToWords=new TreeMap<>();
        this.wordCasesToWords=new TreeMap<>();
        this.wordCasesToWords.put(WordCase.COMITATIVE," with ");
        this.wordCasesToWords.put(WordCase.LOCATIVE," at ");
        this.wordCasesToWords.put(WordCase.NEGATIVE," not ");
        this.wordCasesToWords.put(WordCase.VENTIVE," here ");
        this.wordCasesToWords.put(WordCase.VETITIVE, " is not allowed ");
        this.wordCasesToWords.put(WordCase.GENITIVE, " of ");
        this.wordCasesToWords.put(WordCase.DATIVE, " for ");
        this.wordCasesToWords.put(WordCase.ABLATIVE," from ");
        this.wordCasesToWords.put(WordCase.EQUATIVE," like ");
        this.wordCasesToWords.put(WordCase.TERMINATIVE," to the ");
        this.wordCasesToWords.put(WordCase.QUOTATIVE,": ");
    }




    public List<Tuple<String,POSDefinition>> POSTagToRule(List<POSDefinition> defs,String word,LangChar translations){
        POSDefinition def=defs.get(0);
        String res;
        List<Tuple<String,POSDefinition>> resultList=new LinkedList<>();
        //System.out.println("GETPOSTAG: "+def.getTag()+" "+word);
        switch (def.getPosTag()){
            case ADJECTIVE:
                if(translations==null){
                    resultList.add(new Tuple<>("("+word.replace("[","").replace("]","")+")",def));
                }else{
                    resultList.add(new Tuple<>(translations.getFirstTranslation(Locale.ENGLISH),def));
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
                                resultList.add(new Tuple<>(res,defs.get(i)));
                                return resultList;
                            case NAMEDENTITY:
                                this.manyhighlights=true;
                                POSDefinition nedef=this.posTagger.getClassifiers().get(this.posTagger.getOrderToPOS().get("NE")).get(0);
                                resultList.add(new Tuple<>(this.createNEStr(word.substring(0,word.length()-res.length()),translations,Locale.ENGLISH,nedef.getVerbStem()),nedef));
                                resultList.add(new Tuple<>(res,def));
                                return resultList;
                            case DETERMINATIVE:
                                this.manyhighlights=true;
                                res+=this.createDETStr(word,translations,def);
                                resultList.add(new Tuple<>(res,defs.get(i)));
                                return resultList;
                            case VERB:
                                this.manyhighlights=true;
                                resultList.add(new Tuple<>(res,defs.get(i)));
                                resultList.addAll(handleVerb(word, translations, def, defs, Locale.ENGLISH));
                                return resultList;
                            default:
                        }
                    }
                }
                resultList.add(new Tuple<>(res.trim(),def));
                break;
            case POSSESSIVE:
                   res=def.getValue()[0]+" ";
                   if(defs.size()>1){
                       for(int i=1;i<defs.size();i++){
                           switch (defs.get(i).getPosTag()){
                               case NOUN:
                                   this.manyhighlights=true;
                                   resultList.add(new Tuple<>(res,defs.get(i)));
                                   resultList.addAll(this.createNounStr(word, translations, defs.get(i), defs, Locale.ENGLISH));
                                   return resultList;
                               case NOUNORADJ:
                                   this.manyhighlights=true;
                                   resultList.add(new Tuple<>(res,defs.get(i)));
                                   resultList.addAll(this.handleNoun(word,defs.get(i).getVerbStem(), translations, defs.get(i), Locale.ENGLISH));
                                   return resultList;
                               case NAMEDENTITY:
                                   this.manyhighlights=true;
                                   POSDefinition nedef=this.posTagger.getClassifiers().get(this.posTagger.getOrderToPOS().get("NE")).get(0);
                                   resultList.add(new Tuple<>(this.createNEStr(word.substring(0,word.length()-res.length()),translations,Locale.ENGLISH,nedef.getVerbStem()),nedef));
                                   resultList.add(new Tuple<>(res,def));
                                   return resultList;
                               case DETERMINATIVE:
                                   this.manyhighlights=true;
                                   res+=this.createDETStr(word,translations,def);
                                   resultList.add(new Tuple<>(res,defs.get(i)));
                                   return resultList;
                               default:
                           }
                       }

                   }
                resultList.add(new Tuple<>(res.trim(),def));
                break;
            case NOUN:

                    return this.createNounStr(word,translations,def,defs,Locale.ENGLISH);
            case NOUNORADJ:
                return this.handleNoun(word,def.getVerbStem(),translations,def,Locale.ENGLISH);
            /*List<POSDefinition> nedefs=((AkkadPOSTagger)this.posTagger).getPosTag(suffix.replace("[", "").replace("]",""), dictHandler, false);
            System.out.println("Postags: "+nedefs.toString());
            if(!nedefs.isEmpty()) {
                result.add(new Tuple<String, POSDefinition>(suffix, nedefs.get(0)));
                this.manyhighlights = true;
            }*/

               /* if(def.getValue().length>0){
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
                break;*/
            case NUMBER:
                //System.out.println("LastTranslation: "+lastWritten+" - "+lasttranslation);
                if(lasttranslation!=null && lasttranslation.getTag().equals(def.getTag())){
                    result.delete(result.length()-lastWritten.length(),result.length());
                    //result.substring(0,result.length()-lastWritten.length());
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
                    resultList.addAll(this.handleNamedEntity(word,translations,def,defs,Locale.ENGLISH));
                   //resultList.add(new Tuple<>(this.createNEStr(word,translations),def));
                   break;
            case PREPOSITION:
                if(lasttranslation!=null && lasttranslation.getTag().equals(def.getTag())){
                    result.delete(result.length()-lastWritten.length(),result.length());
                    //result=result.substring(0,result.length()-lastWritten.length());
                    this.length.remove(length.size()-1);
                    this.currentpos-=lastWritten.length();
                }
                for(String str:def.getValue()){
                    resultList.add(new Tuple<>(str,def));
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
                return handleVerb(word,translations,def,defs,Locale.ENGLISH);
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

    private List<Tuple<String,POSDefinition>> createNounStr(String word, LangChar translations, POSDefinition def, final List<POSDefinition> defs, Locale locale) {
        System.out.println("createNounString from " + word + " with stem " + def.getVerbStem());
        String[] subs=word.split("[^A-Z0-9-]+");
        String sub;
        if(subs.length!=0){
            sub = word.split("[^A-Z0-9-]+")[0];
            while (sub!=null && sub.endsWith("-")) {
                sub = sub.substring(0, sub.length() - 1);
            }
        }else{
            sub=word;
        }
        return this.handleNoun(word,sub,translations,def,locale);
    }



    private List<Tuple<String,POSDefinition>> createDETStr(String word,LangChar translations,POSDefinition def){
        List<Tuple<String,POSDefinition>> resultList=new LinkedList<>();
        if(word.equals("GESZ-KIRI6")) {
            resultList.add(new Tuple<>("garden",def));
            return resultList;
        }else if("GESZ-BAN".equals(word)){
            resultList.add(new Tuple<>("bow",def));
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
                    detstranslation+="Ms. ";
                    break;
                case "D":
                    detstranslation="the god";
                    break;
                case "DISZ-MUNUS":  detstranslation+="Husband of";
                    break;
                case "NA4-KISZIB":
                    detstranslation+="Signed by";
                    break;
                case "DUMU-MESZ":
                    detstranslation+="sons of";
                    break;
                case "KI-MIN":
                    detstranslation+="the second";
                    break;
                case "SZESZ":
                    detstranslation+="brother";
                    break;
                case "SZESZ-MESZ":
                    detstranslation+="brothers";
                    break;
                case "DISZ-D":
                case "DISZ":  detstranslation+="Mr.";
                    break;
                case "URU":  detstranslation+="the place of";
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
        String wordend=word.replaceAll("[a-z]+","").replace("LU2-DI-KU5-", "judge").replace("LUGAL","the king").replace("MESZ","s").replace("]","").replace("-","").replaceAll("[0-9]+", "");
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


}

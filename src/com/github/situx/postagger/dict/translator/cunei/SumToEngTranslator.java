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
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.util.enums.pos.PersonNumberCases;
import com.github.situx.postagger.util.enums.pos.WordCase;
import simplenlg.features.Feature;
import simplenlg.framework.NLGFactory;
import simplenlg.lexicon.Lexicon;
import simplenlg.phrasespec.SPhraseSpec;
import simplenlg.realiser.english.Realiser;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Created by timo on 6/3/15.
 */
public class SumToEngTranslator extends CuneiTranslator {

    public SumToEngTranslator(CharTypes charTypes, POSTagger frompos){
        super(charTypes,frompos);
        Lexicon lexicon = Lexicon.getDefaultLexicon();
        this.factory = new NLGFactory(lexicon);
        this.realiser = new Realiser(lexicon);
        this.initializePNMap();
    }

    public SumToEngTranslator(CharTypes charTypes){
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
        this.wordCasesToWords.put(WordCase.ISPART," is ");
    }

    public List<Tuple<String,POSDefinition>> POSTagToRule(List<POSDefinition> defs, String word, LangChar translations){
        POSDefinition def=defs.get(0);
        System.out.println("POSTAG to rule: "+def.getTag());
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
                        System.out.println("Tempres: "+res);
                        System.out.println("Tempres: "+defs.get(i));
                        switch (defs.get(i).getPosTag()){
                            case NOUN:
                            case NOUNORADJ:
                                this.manyhighlights=true;
                                resultList.addAll(this.handleNoun(word,defs.get(i).getVerbStem()!=null && word.contains(defs.get(i).getVerbStem())?defs.get(i).getVerbStem():null,translations,defs.get(i),Locale.ENGLISH));
                                //resultList.addAll(this.createNounStr(word, translations, defs.get(i), defs, Locale.ENGLISH));
                                //resultList.add(new Tuple<>(res,defs.get(i)));
                                return resultList;
                            case NAMEDENTITY:
                                this.manyhighlights=true;
                                POSDefinition nedef=this.posTagger.getClassifiers().get(this.posTagger.getOrderToPOS().get("NE")).get(0);
                                resultList.addAll(this.handleNamedEntity(word,translations,nedef,defs,Locale.ENGLISH));
                                return resultList;
                            case DETERMINATIVE:
                                this.manyhighlights=true;
                                /*List<Tuple<String,POSDefinition>> templist=this.createDETStr(word,translations,def);
                                for(Tuple<String,POSDefinition> tup:templist){
                                    res+=tup.getOne()+" ";
                                }
                                //res+=this.createDETStr(word,translations,def);
                                System.out.println("DETRes; "+res);
                                resultList.add(new Tuple<>(res,defs.get(i)));*/
                                resultList.addAll(this.createDETStr(word,translations,def));
                                return resultList;
                            case VERB:
                                this.manyhighlights=true;
                                //resultList.add(new Tuple<>(res,defs.get(i)));
                                resultList.addAll(handleVerb(word, translations, defs.get(i), defs, Locale.ENGLISH));
                                return resultList;
                            default:
                        }
                        System.out.println("Tempres2: "+defs.get(i));
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
                            case NOUNORADJ:
                                this.manyhighlights=true;
                                /*resultList.add(new Tuple<>(res,defs.get(i)));
                                resultList.addAll(this.createNounStr(word, translations, defs.get(i), defs, Locale.ENGLISH));
*/
                                resultList.addAll(this.handleNoun(word,defs.get(i).getVerbStem()!=null && word.contains(defs.get(i).getVerbStem())?defs.get(i).getVerbStem():null,translations,defs.get(i),Locale.ENGLISH));
                                return resultList;
                            case NAMEDENTITY:
                                this.manyhighlights=true;
                                POSDefinition nedef=this.posTagger.getClassifiers().get(this.posTagger.getOrderToPOS().get("NE")).get(0);
                                resultList.addAll(this.handleNamedEntity(word,translations,nedef,defs,Locale.ENGLISH));
                                //resultList.add(new Tuple<>(this.createNEStr(word.substring(0,word.length()-res.length()),translations),nedef));

                                //resultList.add(new Tuple<>(res,def));
                                return resultList;
                            case DETERMINATIVE:
                                this.manyhighlights=true;
                                resultList.add(new Tuple<>(def.getValue()[0],def));

                                //resultList.add(new Tuple<String,POSDefinition>(def.toString(),def));
                                resultList.addAll(this.createDETStr(word,translations,def));
                                //res+=this.createDETStr(word,translations,def);
                                //resultList.add(new Tuple<>(res,defs.get(i)));
                                return resultList;
                            default:
                        }
                    }

                }
                resultList.add(new Tuple<>(res.trim(),def));
                break;
            case NOUN:
                return this.handleNoun(word,def.getVerbStem()!=null && word.contains(def.getVerbStem())?def.getVerbStem():null, translations, def, Locale.ENGLISH);
            /*case NOUNORADJ:
            /*List<POSDefinition> nedefs=((AkkadPOSTagger)this.posTagger).getPosTag(suffix.replace("[", "").replace("]",""), dictHandler, false);
            System.out.println("Postags: "+nedefs.toString());
            if(!nedefs.isEmpty()) {
                result.add(new Tuple<String, POSDefinition>(suffix, nedefs.get(0)));
                this.manyhighlights = true;
            }

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
                break;*/
            case NUMBER:
                //System.out.println("LastTranslation: "+lastWritten+" - "+lasttranslation);
                /*if(lasttranslation!=null && lasttranslation.getTag().equals(def.getTag())){
                    result.delete(result.length()-lastWritten.length(),result.length());
                    //result=result.substring(0,result.length()-lastWritten.length());
                    this.length.remove(length.size()-1);
                    this.currentpos-=lastWritten.length();
                    resultList.add(new Tuple<>(Integer.valueOf(lastWritten.trim())+Integer.valueOf(def.getValue()[0])*(StringUtils.countMatches(word,"-")+1)+"",def));
                    return resultList;
                }
                resultList.add(new Tuple<>(Integer.valueOf(def.getValue()[0])*(StringUtils.countMatches(word,"-")+1)+"",def));
                */
                resultList.addAll(this.handleNumber(def,word));
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
                res=def.getValue()[0]+" ";
                if(defs.size()>1){
                    for(int i=1;i<defs.size();i++){
                        switch (defs.get(i).getPosTag()){
                            case NOUN:
                            case NOUNORADJ:
                                this.manyhighlights=true;
                                /*resultList.add(new Tuple<>(res,defs.get(i)));
                                resultList.addAll(this.createNounStr(word, translations, defs.get(i), defs, Locale.ENGLISH));
*/
                                resultList.addAll(this.handleNoun(word,defs.get(i).getVerbStem()!=null && word.contains(defs.get(i).getVerbStem())?defs.get(i).getVerbStem():null,translations,defs.get(i),Locale.ENGLISH));
                                return resultList;
                            case NAMEDENTITY:
                                this.manyhighlights=true;
                                POSDefinition nedef=this.posTagger.getClassifiers().get(this.posTagger.getOrderToPOS().get("NE")).get(0);
                                resultList.addAll(this.handleNamedEntity(word,translations,nedef,defs,Locale.ENGLISH));
                                //resultList.add(new Tuple<>(this.createNEStr(word.substring(0,word.length()-res.length()),translations),nedef));

                                //resultList.add(new Tuple<>(res,def));
                                return resultList;
                            case VERB:
                                this.manyhighlights=true;
                                //resultList.add(new Tuple<>(res,defs.get(i)));
                                resultList.addAll(handleVerb(word, translations, defs.get(i), defs, Locale.ENGLISH));
                                return resultList;
                            default:
                        }
                    }

                }
                /*resultList.add(new Tuple<>(res.trim(),def));
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
                }*/
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

    private List<Tuple<String,POSDefinition>> createNounStr(String word, LangChar translations, POSDefinition def, final List<POSDefinition> defs, Locale locale){
        List<Tuple<String,POSDefinition>> result=new LinkedList<>();
        word=word.replace("[","").replace("]","");
        /*if(def.getValue().length>0 && locale==Locale.ENGLISH){
            for(String str:def.getValue()){
                result.add(new Tuple<String, POSDefinition>(str,def));
            }
            return result;
            //return def.getValue();
        }*/
        LangChar matchresult = this.dictHandler.matchWordByTransliteration(word);
        if(matchresult!=null){
            result.addAll(matchresult.getTranslationSet(locale).keySet()
                    .stream().map(str -> new Tuple<>(str.getTranslation(), def)).collect(Collectors.toList()));
            return result;
            //return this.sumdict.get(word).toArray(new String[this.sumdict.get(word).size()]);
        }
        String solu="";
        switch (word){
            case "a-sza3":
                solu="field";
                break;
            case "ansze":
                solu="donkey";
                break;
            case "d":
                solu="god";
                break;
            case "dam":
                solu="wife of";
                break;
            case "dub-sar":
                solu="the scribe";
                break;
            /*case "dumu":
                solu="son of";
                break; */
            case "dumu-mesz":
                solu="children of";
                break;
            case "dumu-munus":
                solu="daughter of";
                break;
            case "giri3":
                solu="foot";
                break;
            case "gin2":
                solu="gin";
                break;
            case "ku3-babbar":
                solu="silver";
                break;
            case "ku3-gi":
                solu="gold";
                break;
           /* case "lugal":
                solu="king";
                break; */
            case "ma-na":
                solu="mana";
                break;
            case "ni-ga":
            case "niga":
                solu="vessel";
                break;
            case "na4":
                solu="signed by";
                break;
            case "sza3":
                solu="inside of";
                break;
            case "szu":
                solu="hand of";
                break;
            case "udu":
                solu="sheep";
                break;
            case "uru":
                solu="city";
                break;
            default:

        }
        String suffix= word.replaceAll("[A-Z0-9]+","");
        /*while (suffix.startsWith("-")){
            suffix=suffix.substring(1,suffix.length());
        }*/
        if(!suffix.isEmpty()){
            System.out.println("Suffix: "+suffix);
            for(POSDefinition ddef:defs){
                if(suffix.matches(ddef.getRegex().toString())){
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
        if(word.replace("[","").replace("]","").startsWith("eme-")){
            solu="testimony";
        }
        else if(word.replace("[","").replace("]","").startsWith("szesz-")){
            solu="brother";
        }
        else if(word.replace("[","").replace("]","").startsWith("nig-ba")){
            solu="present";
        }else if(word.replace("[","").replace("]","").matches("lu2(-)?")){
            solu="person";
        }else if(word.replace("[","").replace("]","").matches("igi(-)?")){
            solu="witness";
        }
        System.out.println("Word to CreateNounStr: "+word.replace(suffix,""));
        word=word.replace(suffix,"");
        while (word.endsWith("-")){
            word=word.substring(1,word.length());
        }
        if(solu.isEmpty()){
            result.add(new Tuple<>("("+word.replace("[", "").replace("]","")+")",def));
        }else{
            if(def.getWordCase().contains(WordCase.GENITIVE)){
                SPhraseSpec p = this.factory.createClause();
                p.setSubject(solu);
                p.setFeature(Feature.FORM, Feature.POSSESSIVE);
                solu=realiser.realise(p).getRealisation().toString();
            }
            result.add(new Tuple<>(solu,def));
        }
        return result;
    }

    private List<Tuple<String,POSDefinition>> createDETStr(String word,LangChar translations,POSDefinition def){
        System.out.println("Create DET String==========================="+word);
        List<Tuple<String,POSDefinition>> resultList=new LinkedList<>();
        String detstranslation="";
        String[] splitted=word.split("-");
        String detcollections=splitted[0];
        String detcollections2=splitted[splitted.length-1];
        /*for(String spl:word.split("-")){
            if(Translator.isAllUpperCaseOrNumber(spl)){
                System.out.println("IsAllUpperCaseOrNumber: "+spl);
                detcollections+=spl+"-";
            }else{
                break;
            }
        }*/
        Integer firstfound=0,lastfound=0;
        Boolean foundit=false;
        int count=0;
        for(String split:splitted){
            foundit=false;
            switch (split){
                case "munus":
                    detstranslation+="Ms. ";
                    foundit=true;
                    break;
                case "d":
                    detstranslation="god";
                    foundit=true;
                    break;
                case "disz-munus":  detstranslation+="Husband of"; foundit=true;
                    break;
                case "nin":
                    if(count>0 && splitted[count-1].equals("d")){
                        detstranslation+="dess";
                        foundit=true;
                    }
                    break;
                case "na4-kiszib":
                    detstranslation+="Signed by";
                    foundit=true;
                    break;
                case "dumu-mesz":
                    detstranslation+="sons of";
                    foundit=true;
                    break;
                case "ki-min":
                    detstranslation+="the second";
                    foundit=true;
                    break;
                case "szesz":
                    detstranslation+="brother";
                    foundit=true;
                    break;
                case "szesz-mesz":
                    detstranslation+="brothers";
                    foundit=true;
                    break;
                case "disz-d":
                case "disz":  detstranslation+="Mr.";
                    foundit=true;
                    break;
                case "uru":  detstranslation+="at";
                    foundit=true;
                    break;
                default:
            }
            if(foundit)
                firstfound++;
            else
                break;
            count++;
        }
        for(int i=splitted.length-1;i>0 && foundit;i--){
            foundit=false;
            switch (splitted[i]){
                case "ki":
                    if(!detcollections.equals("d")) {
                        if(detcollections.equals("iri")){
                            detstranslation += "the city";
                            foundit = true;
                        }else{
                            detstranslation += "the country";
                            foundit = true;
                        }

                    }
                    break;
                default:
            }
            if(foundit)
                lastfound++;
        }

        if(!detstranslation.isEmpty()) {
            resultList.add(new Tuple<>(detstranslation,def));
            word="";
            System.out.println(firstfound+" - "+lastfound+" - "+word);
            for(int i=firstfound;i<splitted.length-lastfound;i++){
                word+=splitted[i];
            }
            /*if(firstfound)
                word=word.substring(detcollections.length());
            if(lastfound)
                word=word.substring(0,word.length()-detcollections2.length());*/
            System.out.println("Word without DET: "+word);
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
            word= Translator.separateConsonants(word);
            resultList.add(new Tuple<>(word,nedef));
            System.out.println("WOOOORD: "+word);
        }
        if(!wordend.isEmpty()){
            resultList.add(new Tuple<>(wordend,def));
        }
        return resultList;
    }

}

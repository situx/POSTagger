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

package com.github.situx.postagger.dict.corpusimport.util;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.vocabulary.XSD;
import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import com.github.situx.postagger.dict.utils.MorphPattern;
import com.github.situx.postagger.dict.utils.POSTag;
import com.github.situx.postagger.util.enums.methods.CharTypes;

import java.util.*;

/**
 * Created by timo on 29.09.16 .
 */
public class dictToLemon {

    private CharTypes chartype;

    private OntClass lemonWord;

    private OntClass lemonChar;

    private OntClass lemonLexicon;

    private OntClass owlList;

    private ObjectProperty isFormOf;
    private OntClass lemonForm;
    private OntClass otherForm;
    private OntClass abstractForm;
    private OntClass canonicalForm;
    private ObjectProperty isSenseOf;
    private ObjectProperty lemonReference;
    private ObjectProperty hasSense;
    private ObjectProperty lemonLexicalForm;
    private ObjectProperty partOfSpeech;
    private ObjectProperty grammaticalNumber;
    private ObjectProperty wordcase;
    private ObjectProperty person;
    private ObjectProperty gender;
    private ObjectProperty tense;
    private ObjectProperty lemonProperty;
    private ObjectProperty lemonDecomposition;
    private ObjectProperty morphologicalUnit;
    private ObjectProperty stem;
    private ObjectProperty hasContents;
    private ObjectProperty hasNext;
    private OntClass lemonReferenceClass;

    private static String NAMESPACE="https://github.com/situx/ontology/cuneiform";
    private ObjectProperty license;
    private DatatypeProperty representation;
    private DatatypeProperty transliteration;
    private DatatypeProperty transliteration_ascii;
    private DatatypeProperty transliteration_utf8;

    public dictToLemon(CharTypes chartypes,OntModel model,String description){
        this.chartype=chartypes;
        this.createNewLemonDictFromResource(model,description);

    }

    public OntModel createNewLemonDictFromResource(OntModel model,String description){
        this.lemonLexicon=model.createClass("http://lemon-model.net/lemon#Lexicon");
        this.lemonWord=model.createClass("http://lemon-model.net/lemon#Word");
        this.lemonChar=model.createClass("http://purl.org/olia/ubyCat.owl#Character");
        this.owlList=model.createClass("http://www.co-ode.org/ontologies/list.owl#OWLList");
        this.hasContents=model.createObjectProperty("http://www.co-ode.org/ontologies/list.owl#hasContents");
        this.hasNext=model.createObjectProperty("http://www.co-ode.org/ontologies/list.owl#hasNext");
        ObjectProperty lemonEntryProp=model.createObjectProperty("http://lemon-model.net/lemon#entry");
        this.lemonProperty=model.createObjectProperty("http://lemon-model.net/lemon#property");
        license=model.createObjectProperty("http://www.linkedmodel.org/schema/vaem#hasLicenseType");
        this.lemonForm=model.createClass("http://lemon-model.net/lemon#LexicalForm");
        this.otherForm=model.createClass("http://lemon-model.net/lemon#otherForm");
        this.abstractForm=model.createClass("http://lemon-model.net/lemon#abstractForm");
        this.canonicalForm=model.createClass("http://lemon-model.net/lemon#canonicalForm");
        this.lemonReferenceClass=model.createClass("http://lemon-model.net/lemon#OntologyReference");
        OntClass lemonLexicalSense=model.createClass("http://lemon-model.net/lemon#LexicalSense");
        this.lemonDecomposition=model.createObjectProperty("http://lemon-model.net/lemon#decomposition");
        this.lemonLexicalForm=model.createObjectProperty("http://lemon-model.net/lemon#form");
        this.lemonForm.addSubClass(this.canonicalForm);
        this.lemonForm.addSubClass(this.otherForm);
        this.lemonForm.addSubClass(this.abstractForm);
        this.morphologicalUnit=model.createObjectProperty("http://www.isocat.org/datcat/morphologicalUnit");
        this.stem=model.createObjectProperty("http://www.isocat.org/datcat/stem");
        this.tense=model.createObjectProperty("http://purl.org/olia/ubyCat.owl#Tense");
        this.grammaticalNumber=model.createObjectProperty("\"http://purl.org/olia/ubyCat.owl#GrammaticalNumber");
        this.person=model.createObjectProperty("http://purl.org/olia/ubyCat.owl#Person");
        this.partOfSpeech=model.createObjectProperty("http://purl.org/olia/ubyCat.owl#PartOfSpeech");
        this.wordcase=model.createObjectProperty("http://purl.org/olia/ubyCat.owl#Case");
        this.gender=model.createObjectProperty("http://purl.org/olia/ubyCat.owl#Gender");
        this.partOfSpeech.addSuperProperty(lemonProperty);
        this.wordcase.addSuperProperty(lemonProperty);
        this.person.addSuperProperty(lemonProperty);
        this.grammaticalNumber.addSuperProperty(lemonProperty);
        this.tense.addSuperProperty(lemonProperty);
        this.gender.addSuperProperty(lemonProperty);
        OntClass tabletClass=model.createClass(NAMESPACE+"#Tablet");
        DatatypeProperty tabletNumberProp=model.createDatatypeProperty(NAMESPACE+"#tabletnumber");
        tabletNumberProp.addDomain(tabletClass);
        tabletNumberProp.addRange(XSD.xstring);
        ObjectProperty postag=model.createObjectProperty("http://purl.org/olia/ubyCat.owl#has_partOfSpeech");
        DatatypeProperty languageprop=model.createDatatypeProperty(NAMESPACE+"#language");
        languageprop.addDomain(tabletClass);
        languageprop.addRange(XSD.xstring);
        DatatypeProperty dialectprop=model.createDatatypeProperty(NAMESPACE+"#dialect");
        dialectprop.addDomain(tabletClass);
        dialectprop.addRange(XSD.xstring);
        representation=model.createDatatypeProperty("http://lemon-model.net/lemon#representation");
        transliteration=model.createDatatypeProperty("http://www.isocat.org/datcat/transliteration");
        representation.addSubProperty(transliteration);
        transliteration.addSuperProperty(representation);
        transliteration_ascii=model.createDatatypeProperty("http://www.isocat.org/datcat/transliteration_ascii");
        transliteration_utf8=model.createDatatypeProperty("http://www.isocat.org/datcat/transliteration_utf8");
        transliteration.addSubProperty(transliteration_ascii);
        transliteration.addSubProperty(transliteration_utf8);
        DatatypeProperty epochprop=model.createDatatypeProperty(NAMESPACE+"#epoch");
        epochprop.addDomain(tabletClass);
        epochprop.addRange(XSD.xstring);
        ObjectProperty contains=model.createObjectProperty(NAMESPACE+"#contains");
        contains.addDomain(tabletClass);
        contains.addRange(lemonWord);
        this.hasSense=model.createObjectProperty("http://lemon-model.net/lemon#sense");
        hasSense.addDomain(tabletClass);
        hasSense.addRange(lemonLexicalSense);
        this.isSenseOf=model.createObjectProperty("http://lemon-model.net/lemon#isSenseOf");
        this.isFormOf=model.createObjectProperty("http://lemon-model.net/lemon#isFormOf");
        this.lemonReference=model.createObjectProperty("http://lemon-model.net/lemon#representation");
        ObjectProperty isIncludedIn=model.createObjectProperty(NAMESPACE+"#isIncludedIn");
        epochprop.addDomain(lemonWord);
        epochprop.addRange(tabletClass);
        Individual lexicon=lemonLexicon.createIndividual(NAMESPACE+"/"+chartype.getLocale());
        lexicon.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#language"),chartype.getLocale());
        lexicon.addProperty(license,model.createOntResource("https://www.wikidata.org/wiki/Q10513445"));
        lexicon.addLiteral(model.createDatatypeProperty("http://purl.org/dc/terms/created"),model.createTypedLiteral(new Date(System.currentTimeMillis()),XSD.dateTime.toString()));
        lexicon.addLabel(chartype.getSmallstr().substring(0, 1).toUpperCase() + chartype.getSmallstr().substring(1)+" Semantic Dictionary","en");
        lexicon.addLiteral(model.createDatatypeProperty("http://purl.org/dc/terms/title"),model.createTypedLiteral(chartype.getSmallstr().substring(0, 1).toUpperCase() + chartype.getSmallstr().substring(1)+" Semantic Dictionary"));
        lexicon.addLiteral(model.createDatatypeProperty("http://www.linkedmodel.org/schema/vaem#owner"),model.createTypedLiteral("Timo Homburg"));
        if(description!=null)
            lexicon.addLiteral(model.createDatatypeProperty("http://www.linkedmodel.org/schema/vaem#description"),description);
        return model;
    }

    public static String generateAnnotatedWordVariantsXML(String word, String transliteration,String postag,DictHandling dicthandler){
        //System.out.println("Postag: "+postag+" Contained? "+dicthandler.morphpattern.containsKey(postag));
        Set<MorphPattern> toAppend=dicthandler.morphpattern.get(postag);
        if(toAppend==null)
            return "";
        System.out.println(dicthandler.morphpattern.keySet());
        StringBuilder builder=new StringBuilder();
        for(MorphPattern morph:toAppend){
            String morphedtranslit=transliteration+morph.pattern.substring(1);
            builder.append("<transliteration transcription=\"");
            builder.append(morphedtranslit.replace("&","&amp;"));
            builder.append("\" cunei=\"");
            builder.append(translitToScript(dicthandler,morphedtranslit,true)+"\" ");
            builder.append(!morph.gender.isEmpty()?"gender=\""+morph.gender+"\" ":"");
            builder.append(!morph.wordcase.isEmpty()?"wordcase=\""+morph.wordcase+"\" ":"");
            builder.append(!morph.person.isEmpty()?"person=\""+morph.person+"\" ":"");
            builder.append(!morph.tense.isEmpty()?"tense=\""+morph.tense+"\" ":"");
            builder.append(!morph.animacy.isEmpty()?"animacy=\""+morph.animacy+"\" ":"");
            builder.append(!morph.number.isEmpty()?"number=\""+morph.number+"\" ":"");
            builder.append(!morph.transprefix.isEmpty()?"transprefix=\""+morph.transprefix+"\" ":"");
            builder.append(!morph.transsuffix.isEmpty()?"transsuffix=\""+morph.transsuffix+"\" ":"");
            builder.append(">");
            builder.append(morphedtranslit.replace("&","&amp;"));
            builder.append("</transliteration>").append(System.lineSeparator());
        }
        return builder.toString();
    }

    public void generateAnnotatedWordVariants(String word, String transliteration, Set<MorphPattern> toAppend, DictHandling dicthandler, Individual postag, OntClass pos, Individual curword){
        if(toAppend==null)
            return;
        //System.out.println(toAppend);
        Individual otherf=abstractForm.createIndividual(NAMESPACE+"/word#"+ transliteration.replace(" ","")+"_form"
        );
        otherf.addProperty(this.transliteration_utf8,transliteration);
        otherf.addProperty(this.transliteration_ascii, CuneiDictHandler.reformatToASCIITranscription2(transliteration));
        otherf.addProperty(this.morphologicalUnit,this.stem);
        otherf.addProperty(this.isFormOf,curword);
        for(MorphPattern morph:toAppend){
            String morphedtranslit=transliteration+morph.pattern.substring(1);
            otherf=otherForm.createIndividual(NAMESPACE+"/word#"+ word.replace(" ","")+morph.pattern.substring(1)+"_form"
                    );
            otherf.addProperty(this.transliteration_utf8,morphedtranslit);
            otherf.addProperty(this.transliteration_ascii, CuneiDictHandler.reformatToASCIITranscription2(morphedtranslit));
            String res=translitToScript(dicthandler,morphedtranslit,false);
            otherf.addProperty(this.representation,res!=null?res:"");
            otherf.addProperty(this.isFormOf,curword);
            otherf.addProperty(partOfSpeech, postag);
            otherf.addProperty(person,pos.createIndividual(morph.person));
            otherf.addProperty(wordcase,pos.createIndividual(morph.wordcase));
            otherf.addProperty(grammaticalNumber,pos.createIndividual(morph.number));
            otherf.addProperty(gender,pos.createIndividual(morph.gender));
            //otherf.addProperty(this.)
        }
    }


    public static String translitToScript(DictHandling dicthandler,String translit,Boolean withCharMisses){
        StringBuilder result=new StringBuilder();
        for(String spl:translit.split("-|:|\\*")){
            if(!dicthandler.getTranslitToCharMap().containsKey(spl) && !dicthandler.getTranslitToWordDict().containsKey(spl) && !withCharMisses){
                return null;
            }else if(!dicthandler.getTranslitToCharMap().containsKey(spl) && !dicthandler.getTranslitToWordDict().containsKey(spl) && withCharMisses){
                result.append("-?-");
            }else if(dicthandler.getTranslitToCharMap().containsKey(spl)){
                result.append(dicthandler.getTranslitToCharMap().get(spl));
            }else{
                result.append(dicthandler.getTranslitToWordDict().get(spl));
            }
        }
        return result.toString().replace("--","-");
    }


    public void wordCombToLemon(String word, String transliteration, String concept, String inTablet, CharTypes language, OntModel model, LangChar langchar, DictHandling dictHandler, boolean map){
        OntClass lemonWord=model.createClass("http://lemon-model.net/lemon#Word");
        OntClass lemonForm=model.createClass("http://lemon-model.net/lemon#LexicalForm");
        ObjectProperty isFormOf=model.createObjectProperty("http://lemon-model.net/lemon#isFormOf");
        ObjectProperty lemonEntryProp=model.createObjectProperty("http://lemon-model.net/lemon#entry");
        //System.out.println("Word: "+word);
        String uuid= UUID.randomUUID().toString();
        Individual curword;
        if(map){
            curword=lemonChar.createIndividual(NAMESPACE+"/word#"+word.replace(" ","")+"_word");
        }else{
            curword=lemonWord.createIndividual(NAMESPACE+"/word#"+word.replace(" ","")+"_word");
        }
        OntClass lemonLexicon=model.createClass("http://lemon-model.net/lemon#Lexicon");
        Individual lexicon=lemonLexicon.createIndividual(NAMESPACE+"/"+chartype.getLocale());
        lexicon.addProperty(license,model.createOntResource("https://www.wikidata.org/wiki/Q10513445"));
        lexicon.addProperty(lemonEntryProp,curword);
        OntClass lemonLexicalSense=model.createClass("http://lemon-model.net/lemon#LexicalSense");
        Individual cursense;
        Individual curform=canonicalForm.createIndividual(NAMESPACE+"/word#"+word.replace(" ","")+"_form");
        curform.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#writtenRep"),word);
        curform.addProperty(this.transliteration_utf8,transliteration);
        curform.addProperty(this.transliteration_ascii, CuneiDictHandler.reformatToASCIITranscription2(transliteration));
        Literal label = model.createLiteral( transliteration, language.getShortname() );
        curword.addLabel(transliteration,"en");
        //curword.addLiteral(model.createAnnotationProperty("http://www.w3.org/2000/01/rdf-schema#label"),label);
        label = model.createLiteral( word );

        curform.addLabel(word,"en");
        //Literal(model.createAnnotationProperty("http://www.w3.org/2000/01/rdf-schema#label"),label);
        //curword.addLabel(transliteration,language.getShortname());
        //curform.addLabel(word,language.getShortname());
        curform.addProperty(isFormOf,curword);
        curword.addProperty(this.lemonLexicalForm,curform);
        if(langchar==null) {
            langchar= (CuneiChar) dictHandler.matchWordByTransliteration(word);
        }

        if(langchar!=null) {
            curform.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#writtenRep"), langchar.getCharacter());
            if(langchar.getConceptURI()!=null && !langchar.getConceptURI().isEmpty()){
                System.out.println("ConceptURI: "+langchar.getConceptURI().toString());

                if(langchar.getConceptURI().contains("#")){
                    cursense=lemonLexicalSense.createIndividual(NAMESPACE+"/word#"+langchar.getConceptURI().substring(langchar.getConceptURI().lastIndexOf('#')+1).replace(" ",""));
                    cursense.addLabel(langchar.getConceptURI().substring(langchar.getConceptURI().lastIndexOf('#')+1).replace(" ","")+"_sense","en");
                }else if(langchar.getConceptURI().contains("/")) {
                    cursense = lemonLexicalSense.createIndividual(NAMESPACE+"/word#" + langchar.getConceptURI().substring(langchar.getConceptURI().lastIndexOf('/')+1).replace(" ",""));
                    cursense.addLabel(langchar.getConceptURI().substring(langchar.getConceptURI().lastIndexOf('/')+1).replace(" ","")+"_sense","en");
                }else{
                    String uuuid=UUID.randomUUID().toString();
                    cursense = lemonLexicalSense.createIndividual(NAMESPACE+"/word#" + uuuid);
                    cursense.addLabel(uuuid+"_sense",null);
                }
                curword.addProperty(hasSense,cursense);
                cursense.addProperty(isSenseOf,curword);
                Individual lemref=lemonReferenceClass.createIndividual(langchar.getConceptURI());
                cursense.addProperty(lemonReference,lemref);
                lemref.addProperty(model.createObjectProperty("http://lemon-model.net/lemon#prefRef"),cursense);
                OntClass pos=model.createClass("http://purl.org/olia/ubyCat.owl#PartOfSpeech");
                System.out.println("Postags: "+langchar.getPostags());
                if(!langchar.getPostags().isEmpty()) {
                    POSTag postag=langchar.getPostags().iterator().next();
                    System.out.println("Adding postag: "+postag.getConceptURI());
                    Individual ind=pos.createIndividual( langchar.getPostags().iterator().next().getConceptURI());
                    curform.addProperty(partOfSpeech, pos.createIndividual( langchar.getPostags().iterator().next().getConceptURI()));
                    curform.addProperty(person,pos.createIndividual("http://purl.org/olia/ubyCat.owl#FirstPerson"));
                    curform.addProperty(wordcase,pos.createIndividual("http://purl.org/olia/ubyCat.owl#Nominative"));
                    curform.addProperty(grammaticalNumber,pos.createIndividual("http://purl.org/olia/ubyCat.owl#Singular"));
                    curform.addProperty(gender,pos.createIndividual("http://purl.org/olia/ubyCat.owl#Male"));
                    //System.out.println(dictHandler.morphpattern);
                    //System.out.println(postag.getPostag().toString());
                    this.generateAnnotatedWordVariants(word,transliteration,dictHandler.morphpattern.get(postag.getPostag().toString()),dictHandler,ind,pos,curword);
                }
            }
            if(word.length()>2) {
                boolean compose = true;
                List<LangChar> decomposition = new LinkedList<>();
                for (int i = 0; i < word.length(); i += 2) {
                    if (i + 2 <= word.length()) {
                        String substr = word.substring(i, i + 2);
                        if (!dictHandler.getDictionary().containsKey(substr)) {
                            compose = false;
                            break;
                        } else {
                            decomposition.add(dictHandler.getDictionary().get(substr));
                        }
                    }
                }
                if (compose) {
                    int i = 1;
                    Individual lastlist = owlList.createIndividual(NAMESPACE + "/word#" + word + "_decomp");
                    Individual listbegin = lastlist;
                    for (Iterator<LangChar> iter = decomposition.iterator(); iter.hasNext(); ) {
                        LangChar langc = iter.next();
                        lastlist.addProperty(this.hasContents, lemonWord.createIndividual(NAMESPACE + "/word#" + langc.getCharacter() + "_word"));
                        if (iter.hasNext()) {
                            Individual list = owlList.createIndividual(NAMESPACE + "/word#" + word + "_decomp_" + i++);
                            lastlist.addProperty(this.hasNext, list);
                            lastlist = list;
                        }
                    }
                    curword.addProperty(this.lemonDecomposition, listbegin);
                }
            }
        }
        /*if(!result.isEmpty() ) {
            if(postagger.getPosToURI().containsKey(result.iterator().next().getTag())){
                curword.addProperty(postag, model.createOntResource(postagger.getPosToURI().get(result.iterator().next().getTag())));
                POSDefinition firstresult=result.iterator().next();
                if(firstresult.getValue()!=null && firstresult.getValue().length>0){
                    curword.addProperty(model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#value"),firstresult.getValue()[0]);
                }
                if(firstresult.getVerbStem()!=null && !firstresult.getVerbStem().isEmpty()){
                    curword.addProperty(model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#stem"),firstresult.getVerbStem());
                }
                Map<Integer, String> groupresults=firstresult.getCurrentgroupResults();
                for(Integer key:result.iterator().next().getCurrentgroupResults().keySet()){
                    if(groupresults.get(key)!=null){
                        for(GroupDefinition groupdef:firstresult.getGroupconfig().get(key)){
                            Boolean matched=groupdef.getRegex().matcher(groupresults.get(key)).matches();
                            if(matched && !groupdef.getGroupCase().equals("stem")){
                                curword.addProperty(postag,model.createOntResource(groupdef.getUri()));
                            }else if(matched){
                                curword.addProperty(model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#lemma"),model.createOntResource(groupdef.getUri()));
                            }
                        }
                    }
                }
            }else{
                curword.addProperty(postag, result.iterator().next().getTag());
            }
        }
        curword.addProperty(lemonLexicalForm,ind);
        knownwords.put(word,uuid);
    }
            currentTablet.addProperty(contains,curword);
            curword.addProperty(isIncludedIn,currentTablet);
     */
    }

}

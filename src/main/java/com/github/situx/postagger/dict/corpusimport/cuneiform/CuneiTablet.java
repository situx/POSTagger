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

package com.github.situx.postagger.dict.corpusimport.cuneiform;

import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.pos.util.GroupDefinition;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.vocabulary.XSD;
import com.github.situx.postagger.dict.corpusimport.importformat.ATFImporter;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.util.POSDefinition;

import java.util.*;

/**
 * Created by timo on 04.06.16.
 */
public class CuneiTablet {

    private String tabletNumber;

    private Set<String> words;

    private String tabletText;

    private CharTypes chartype;

    public CuneiTablet(String tabletNumber,String tabletText,CharTypes charType){
        this.tabletNumber=tabletNumber;
        this.chartype=charType;
        this.tabletText=tabletText;
        words=new TreeSet<String>();
        for(String word:tabletText.split(" ")){
            word=word.trim();
            words.add(word);
        }
    }

    public OntModel tabletToOntModel(OntModel model,Map<String,String> knownwords){
        //OntModel model= ModelFactory.createOntologyModel();
        OntClass lemonLexicon=model.createClass("http://lemon-model.net/lemon#Lexicon");
        OntClass lemonWord=model.createClass("http://lemon-model.net/lemon#Word");
        ObjectProperty lemonEntryProp=model.createObjectProperty("http://lemon-model.net/lemon#entry");
        OntClass lemonForm=model.createClass("http://lemon-model.net/lemon#Form");
        OntClass lemonLexicalSense=model.createClass("http://lemon-model.net/lemon#lexicalSense");
        ObjectProperty lemonLexicalForm=model.createObjectProperty("http://lemon-model.net/lemon#lexicalForm");

        OntClass tabletClass=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Tablet");
        DatatypeProperty tabletNumberProp=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#tabletnumber");
        tabletNumberProp.addDomain(tabletClass);
        tabletNumberProp.addRange(XSD.xstring);
        ObjectProperty postag=model.createObjectProperty("http://purl.org/olia/ubyCat.owl#has_partOfSpeech");
        DatatypeProperty languageprop=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#language");
        languageprop.addDomain(tabletClass);
        languageprop.addRange(XSD.xstring);
        DatatypeProperty dialectprop=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#dialect");
        dialectprop.addDomain(tabletClass);
        dialectprop.addRange(XSD.xstring);
        DatatypeProperty epochprop=model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#epoch");
        epochprop.addDomain(tabletClass);
        epochprop.addRange(XSD.xstring);
        ObjectProperty contains=model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#contains");
        contains.addDomain(tabletClass);
        contains.addRange(lemonWord);
        ObjectProperty hasSense=model.createObjectProperty("http://lemon-model.net/lemon#sense");
        hasSense.addDomain(tabletClass);
        hasSense.addRange(lemonLexicalSense);
        ObjectProperty isSenseOf=model.createObjectProperty("http://lemon-model.net/lemon#isSenseOf");
        ObjectProperty isFormOf=model.createObjectProperty("http://lemon-model.net/lemon#isFormOf");
        ObjectProperty lemonReference=model.createObjectProperty("http://lemon-model.net/lemon#representation");
        ObjectProperty isIncludedIn=model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#isIncludedIn");
        epochprop.addDomain(lemonWord);
        epochprop.addRange(tabletClass);
        Individual currentTablet=tabletClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#"+this.tabletNumber);
        currentTablet.addLiteral(tabletNumberProp,this.tabletNumber);
        currentTablet.addLiteral(languageprop, chartype.getLocale());
        Individual lexicon=lemonLexicon.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#"+chartype.getLocale());
        lexicon.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#language"),chartype.getLocale());
        POSTagger postagger=chartype.getCorpusHandlerAPI().getPOSTagger(false);
        DictHandling dictHandler=chartype.getCorpusHandlerAPI().getUtilDictHandler();
        Individual curword;
        for(String word:words){
            word=ATFImporter.cleanWordStringForOntology(word);
            if(knownwords.containsKey(word)){
                curword=lemonWord.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+ knownwords.get(word).replace(" ",""));
            }else{

                System.out.println("Word: "+word);
                List<POSDefinition> result=postagger.getPosTagDefs(word,dictHandler);
                System.out.println("POSDefinitions: "+result);
                String uuid=UUID.randomUUID().toString();
                curword=lemonWord.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+ uuid);
                lexicon.addProperty(lemonEntryProp,curword);
                Individual ind=lemonForm.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+UUID.randomUUID().toString());
                ind.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#writtenRep"),word);
                ind.addProperty(isFormOf,curword);
                CuneiChar cuneiChar= (CuneiChar) dictHandler.matchWordByTransliteration(word);
                if(cuneiChar!=null) {
                    ind.addProperty(model.createDatatypeProperty("http://lemon-model.net/lemon#writtenRep"), dictHandler.matchWordByTransliteration(word).getCharacter());
                    if(cuneiChar.getConceptURI()!=null && !cuneiChar.getConceptURI().isEmpty()){
                        System.out.println("ConceptURI: "+cuneiChar.getConceptURI().toString());
                        Individual cursense;
                        if(cuneiChar.getConceptURI().contains("#")){
                            cursense=lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#"+cuneiChar.getConceptURI().substring(cuneiChar.getConceptURI().lastIndexOf('#')+1).replace(" ",""));
                        }else if(cuneiChar.getConceptURI().contains("/")) {
                            cursense = lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#" + cuneiChar.getConceptURI().substring(cuneiChar.getConceptURI().lastIndexOf('/')+1).replace(" ",""));
                        }else{
                            cursense = lemonLexicalSense.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform/word#" + UUID.randomUUID().toString());
                        }
                        ind.addProperty(hasSense,cursense);
                        cursense.addProperty(isSenseOf,ind);
                        cursense.addProperty(lemonReference,model.createOntResource(cuneiChar.getConceptURI()));
                    }
                }
                if(!result.isEmpty() ) {
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
            //curword.addLiteral(lemonLexicalForm,dictHandler.get)
        }
        //TODO For every word (noun) on this tablet create a link into the semantic web
        return model;
    }

}

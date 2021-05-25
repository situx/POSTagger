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

package com.github.situx.postagger.dict.semdicthandler;

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import eu.monnetproject.lemon.*;
import eu.monnetproject.lemon.model.LexicalForm;
import eu.monnetproject.lemon.model.Lexicon;
import eu.monnetproject.lemon.model.Text;
import net.lexinfo.LexInfo;

import java.net.URI;

/**
 * Created by timo on 04.02.16.
 * Creates a wordnet equivalent of the generated dictionary.
 * The wordnet equivalent can be use as a replacement of the original dictionary.
 */
public class SemDictHandler {

    DictHandling dictHandler;

    final LemonSerializer serializer = LemonSerializer.newInstance();
    final LemonModel model = serializer.create();
    final Lexicon lexicon = model.addLexicon(
            URI.create("http://www.example.com/mylexicon"),
            "en" /*English*/);

    public SemDictHandler(DictHandling dictHandler){
        this.dictHandler=dictHandler;
    }

    private void initializeOntModel(){


        final LemonFactory factory = model.getFactory();
        final LexicalForm pluralForm = factory.makeForm();
        pluralForm.setWrittenRep(new Text("cats", "en"));
        final LinguisticOntology lingOnto = new LexInfo();
        pluralForm.addProperty(
                lingOnto.getProperty("number"),
                lingOnto.getPropertyValue("plural"));
        //entry.addOtherForm(pluralForm);

        //serializer.writeEntry(model, entry, lingOnto,
         //       new OutputStreamWriter(System.out));
        //this.model.createClass("http://acoli.uni-frankfurt.de/lang#");
    }

    public void semanticizeDict(){
        //OntModel model= ModelFactory.createOntologyModel();
        for(String dictEntry:this.dictHandler.getDictionary().keySet()){
            LangChar langChar=this.dictHandler.getDictionary().get(dictEntry);
            //final LexicalEntry entry = LemonModels.addEntryToLexicon(
                    //lexicon,
                   // IRI.create("http://acoli.uni-frankfurt.de/lang#"+langChar.getCharacter()),
                    //"cat",
                    //IRI.create("http://dbpedia.org/resource/Cat"));

        }
    }



}

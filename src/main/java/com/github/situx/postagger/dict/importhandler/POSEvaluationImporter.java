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

package com.github.situx.postagger.dict.importhandler;

import com.github.situx.postagger.util.enums.pos.Tenses;
import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import com.github.situx.postagger.dict.importhandler.cuneiform.CuneiImportHandler;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.pos.POSTags;
import com.github.situx.postagger.util.enums.pos.PersonNumberCases;
import com.github.situx.postagger.util.enums.pos.WordCase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by timo on 26.06.16.
 */
public class POSEvaluationImporter extends ImportHandler {

    public List<POSDefinition> posdefs;

    private POSDefinition curdef;

    private Boolean word;

    private CuneiImportHandler importhandler;

    private Integer lastsize=-1;

    public POSEvaluationImporter(){
        this.posdefs=new LinkedList<>();
        this.importhandler=new CuneiImportHandler();
    }

    @Override
    public String reformatToASCIITranscription(String transcription) {
        return null;
    }

    @Override
    public String reformatToUnicodeTranscription(String transcription) {
        return null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch(qName){
            case "word":
                word=true;
                POSDefinition definition=new POSDefinition("","","","",new String[0],"UNKNOWN","","","","","",new TreeMap<>(), CharTypes.CUNEICHAR);

                if(attributes.getValue("tense")!=null) {
                    try {
                        definition.setTense(Tenses.valueOf(attributes.getValue("tense").toUpperCase()));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                if(attributes.getValue("person")!=null) {
                    try {
                        definition.setPersonNumberCase(PersonNumberCases.valueOf(attributes.getValue("person").toUpperCase()));
                        //definition.setTense(Tenses.valueOf(attributes.getValue("tense").toUpperCase()));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                if(attributes.getValue("wordcase")!=null) {
                    try {
                        definition.getWordCase().add(WordCase.valueOf(attributes.getValue("wordcase").toUpperCase()));
                        //definition.setTense(Tenses.valueOf(attributes.getValue("tense").toUpperCase()));
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                try {
                    definition.setPosTag(POSTags.valueOf(attributes.getValue("postag")));
                }catch(Exception e){
                    e.printStackTrace();
                }
                definition.setTag(attributes.getValue("postag"));
                definition.setValue(new String[]{attributes.getValue("translation")});
                posdefs.add(definition);
                break;
                //definition.setPersonNumberCase(PersonNumberCases.valueOf(attributes.getValue("person")));
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(!posdefs.isEmpty() && this.word){
            String wordstr=new String(ch,start,length);
            if(wordstr.trim().contains(" ")){
                Boolean first=true;
                for(String subword:wordstr.split(" ")){
                    if(first){
                        posdefs.get(posdefs.size()-1).setVerbStem(CuneiDictHandler.reformatToASCIITranscription2(subword));
                        first=false;
                    }else{
                        POSDefinition newdef=new POSDefinition(posdefs.get(posdefs.size()-1));
                        newdef.setVerbStem(CuneiDictHandler.reformatToASCIITranscription2(subword));
                        posdefs.add(newdef);
                    }

                }
            }else{
                if(lastsize==posdefs.size()){
                    String toadd= CuneiDictHandler.reformatToASCIITranscription2(new String(ch,start,length));
                    if(toadd!=null)
                        posdefs.get(posdefs.size()-1).setVerbStem(posdefs.get(posdefs.size()-1).getVerbStem()+toadd);
                }else{
                    String toadd=CuneiDictHandler.reformatToASCIITranscription2(new String(ch,start,length));
                    if(toadd!=null)
                        posdefs.get(posdefs.size()-1).setVerbStem(toadd);
                    lastsize=posdefs.size();
                }
            }

        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if(qName.equals("word")){
            this.word=false;
        }
    }

    public List<POSDefinition> getPosdefs() {
        return posdefs;
    }
}

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

import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.dicthandler.cuneiform.SumerianDictHandler;
import com.github.situx.postagger.dict.pos.cuneiform.SumerianPOSTagger;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by timo on 12.04.17 .
 */
public class LuwianCuneiCorpusHandler extends CuneiCorpusHandler {
    /**
     * Constructor for this class.
     *
     * @param stopchars stopchars to consider
     */
    public LuwianCuneiCorpusHandler(List<String> stopchars) {
        super(stopchars);
    }

    public POSTagger getPOSTagger(Boolean newPosTagger) {
        if(this.posTagger==null || newPosTagger){
            this.posTagger=new SumerianPOSTagger();
        }
        return this.posTagger;
    }

    @Override
    public DictHandling getUtilDictHandler() {
        if(this.utilDictHandler==null){
            this.utilDictHandler=new SumerianDictHandler(new LinkedList<>());
            try {
                this.utilDictHandler.importMappingFromXML("dict/luwcn_map.xml");
                this.utilDictHandler.importDictFromXML("dict/luwcn_dict.xml");
                System.out.println("GetUtilDictHandler");
            } catch (IOException | ParserConfigurationException | SAXException e) {
                e.printStackTrace();
            }
        }
        return this.utilDictHandler;
    }
}

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

package com.github.situx.postagger.main.gui.ime.jquery.tree.builder;

import com.github.situx.postagger.main.gui.ime.jquery.tree.PaintIMETree;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.main.gui.ime.jquery.tree.IMETree;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.*;

/**
 * Created by timo on 3/26/15.
 */
public abstract class PaintTreeBuilder extends TreeBuilder {

    public PaintIMETree root;

    private Map<Integer,List<PaintIMETree>> lengthToSubTree;

    private List<IMETree> queryresult;

    private Integer wordCounter;

    public PaintTreeBuilder(final DictHandling dictHandler) {
        super(dictHandler);
        this.wordCounter=0;
        this.root=new PaintIMETree();
        this.lengthToSubTree=new TreeMap<>();
        this.queryresult=new LinkedList<>();
    }


    public abstract Integer getLength(String description);


    public PaintIMETree depthbuildWODictHandler(IMETree node,String newchar,String chars,String translit,String meaning,String translation,String postag,String concept,Integer frequency,Boolean isChar,Integer depth,Integer curmaxdepth){
        PaintIMETree newnode=new PaintIMETree();
        newnode.setIsWord(isChar);
        newnode.setLength(this.getLength(node.getWord()));
        newnode.setWord(node.getWord()+newchar);
        newnode.setMeaning(meaning);
        newnode.setTranslation(translation);

        /*if(!this.queryCache.containsKey(newnode.getWord()) && !newnode.getWord().isEmpty()){
            this.queryCache.put(newnode.getWord(),new TreeMap<String, Integer>());
        }*/
        if(isChar){
            newnode.setFrequency(frequency);
            newnode.setChars(chars==null?"":chars);
            if(!newnode.getPaintCachewords().containsKey(newnode.getFrequency())){
                newnode.getPaintCachewords().put(newnode.getFrequency(),new TreeMap<Integer,Set<String>>());
            }
            if(!newnode.getPaintCachewords().get(frequency).containsKey(newnode.getWord())){
                newnode.getPaintCachewords().get(frequency).put(this.getLength(newnode.getWord()),new TreeSet<String>());
            }
            //this.queryCache.get(newnode.getWord()).put(newnode.getWord(),frequency);
            newnode.getCachewords().get(frequency).get(newnode.getWord()).add(new IMETree(newnode.getChars(),meaning,translation,postag,concept));

        }else{
            newnode.setFrequency(frequency);
            newnode.setChars(chars);
            if(!newnode.getCachewords().containsKey(newnode.getFrequency())){
                newnode.getCachewords().put(newnode.getFrequency(),new TreeMap<String,Set<IMETree>>());
            }
            if(!newnode.getCachewords().get(frequency).containsKey(newnode.getWord())){
                newnode.getCachewords().get(frequency).put(newnode.getWord(),new TreeSet<IMETree>());
            }
            //this.queryCache.get(newnode.getWord()).put(newnode.getChars()+translit.substring(depth,curmaxdepth),frequency);
            newnode.getCachewords().get(frequency).get(newnode.getWord()).add(new IMETree(newnode.getChars()+translit.substring(depth,curmaxdepth),meaning,translation,postag,concept));
        }
        node.addChild(newnode);
        return newnode;
    }

    public Map<Integer, List<PaintIMETree>> getLengthToSubTree() {
        return lengthToSubTree;
    }

    public void setLengthToSubTree(final Map<Integer, List<PaintIMETree>> lengthToSubTree) {
        this.lengthToSubTree = lengthToSubTree;
    }

    public Integer getWordCounter() {
        return wordCounter;
    }

    public void setWordCounter(final Integer wordCounter) {
        this.wordCounter = wordCounter;
    }

    @Override
    public void addWordComboToTree(final String translit, final String chars, final Integer frequency, final String meaning,final String translation,final String postag,final String concept,Integer depth) {
        Integer length=this.getLength(translit);
        this.wordCounter++;
        //System.out.println("AddWordComboToPaintTree: "+translit+" "+length);
        if(!this.lengthToSubTree.containsKey(length)){
              this.lengthToSubTree.put(length,new LinkedList<PaintIMETree>());
        }
        Integer[] translitarray=this.formatqueryString(translit);
        int i=0;
        PaintIMETree[] nodelist=new PaintIMETree[translitarray.length];
        for(Integer wordchar:translitarray){
            nodelist[i]=new PaintIMETree();
            nodelist[i].setFrequency(1);
            nodelist[i].setWord(wordchar.toString());
            nodelist[i].setMeaning(meaning);
            nodelist[i].setPostag(postag);
            nodelist[i].setConcept(concept);
            nodelist[i].setTranslation(translation);
            nodelist[i].setPaintCachewords(new TreeMap<Integer, Map<Integer, Set<String>>>());
            nodelist[i++].setChars(chars);
        }
        root.addChild(nodelist[0]);
        for(i=1;i<nodelist.length;i++){
             nodelist[i-1].addChild(nodelist[i]);
        }
        /*for(IMETree tree:root.getChildren()){
            System.out.println("Tree: "+tree.getChars()+" "+tree.getWord());
        }*/
        //System.out.println("Node: "+nodelist[0]);
        this.lengthToSubTree.get(length).add(nodelist[0]);
        //System.out.println("LengthToSubTree: "+this.lengthToSubTree);
    }

    public Boolean checkIfValid(Integer[] queryArray,Integer looseness,IMETree curTree,Integer depth,List<IMETree> currentNodeChain){
        //System.out.println("QueryString: "+queryArray[0]+" "+queryArray[1]+" "+queryArray[2]+" "+queryArray[3]);
        //System.out.println("CurrentNodeChain: "+currentNodeChain.toString());
        if(depth==queryArray.length){
            //System.out.println("Found: "+curTree.getChars());
            this.queryresult.add(curTree);
            return true;
        }

        int lowerBound=queryArray[depth]-looseness;
        int upperBound=queryArray[depth]+looseness;
        //System.out.println("Depth: "+depth);
        for(IMETree tree:curTree.getChildren()) {
            //System.out.println(Integer.valueOf(tree.getWord())+" "+queryArray[depth]);
            int value = Integer.valueOf(tree.getWord());
            if ((lowerBound <= value && value <= upperBound) && depth < (queryArray.length)) {
                this.checkIfValid(queryArray, looseness, tree, depth + 1, currentNodeChain);
            }
        }
        return true;
    }

    public List<IMETree> query(Integer[] queryArray,Integer looseness){
        List<IMETree> result=new LinkedList<>();
        result.add(root);
        this.queryresult.clear();
        this.checkIfValid(queryArray,looseness,root,0,result);
        return this.queryresult;
    }

    public Integer[] formatqueryString(String querystring){
        Integer a,b,c,d;
        if(querystring.contains("a")){
              int aindex=querystring.indexOf("a");
              int endaindex=aindex+2;
              while((endaindex+1)<=querystring.length() && querystring.substring(endaindex,endaindex+1).matches("[0-9]")){
                   endaindex++;
              }
              //System.out.println("QueryString: "+querystring+" "+(aindex+1)+" "+(endaindex+1));
              a=Integer.valueOf(querystring.substring(aindex+1,endaindex));
        }else {
            a=0;
        }
        if(querystring.contains("b")){
            int aindex=querystring.indexOf("b");
            int endaindex=aindex+2;
            while((endaindex+1)<=querystring.length() && querystring.substring(endaindex,endaindex+1).matches("[0-9]")){
                endaindex++;
            }
            //System.out.println("QueryString: "+querystring+" "+(aindex+1)+" "+(endaindex+1));
            b=Integer.valueOf(querystring.substring(aindex+1,endaindex));
        }else {
            b=0;
        }
        if(querystring.contains("c")){
            int aindex=querystring.indexOf("c");
            int endaindex=aindex+2;
            while((endaindex+1)<=querystring.length() && querystring.substring(endaindex,endaindex+1).matches("[0-9]")){
                endaindex++;
            }
            //System.out.println("QueryString: "+querystring+" "+(aindex+1)+" "+(endaindex+1));
            c=Integer.valueOf(querystring.substring(aindex+1,endaindex));
        }else {
            c=0;
        }
        if(querystring.contains("d")){
            int aindex=querystring.indexOf("d");
            int endaindex=aindex+2;
            while((endaindex+1)<=querystring.length() && querystring.substring(endaindex,endaindex+1).matches("[0-9]")){
                endaindex++;
            }
            //System.out.println("QueryString: "+querystring+" "+(aindex+1)+" "+(endaindex+1));
            d=Integer.valueOf(querystring.substring(aindex+1,endaindex));
        }else {
            d=0;
        }
        return new Integer[]{a,b,c,d};
    }

    @Override
    public String toXML(final IMETree node) throws IOException, XMLStreamException {
        return super.toXML(node);
    }
}

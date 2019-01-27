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

package com.github.situx.postagger.dict.pos.cuneiform;

import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import com.github.situx.postagger.dict.importhandler.POSEvaluationImporter;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.translator.Translator;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.dict.translator.cunei.HitToEngTranslator;
import com.github.situx.postagger.main.gui.util.POSInBox;
import com.github.situx.postagger.util.enums.methods.EvaluationMethod;
import com.github.situx.postagger.util.enums.pos.WordCase;
import com.github.situx.postagger.util.enums.util.Tags;
import org.abego.treelayout.TreeForTreeLayout;
import org.abego.treelayout.util.DefaultTreeForTreeLayout;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.awt.*;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.List;

/**
 * Created by timo on 4/29/15.
 */
public class CuneiPOSTagger extends POSTagger {
    /**
     * Constructor for this class.
     *
     * @param poscolors the colors to set
     * @param locale
     */
    public CuneiPOSTagger(final Map<String, Color> poscolors, final CharTypes chartype) {

        super(poscolors, chartype);

    }

    public String detectEpoch(String text,CharTypes charType,Boolean cuneiformFlag){
        DictHandling dictHandler=charType.getCorpusHandlerAPI().getUtilDictHandler();
        for(String line:text.split(System.lineSeparator())){
            for(String word:line.split(" ")){
                if(cuneiformFlag){
                for(int i=0;i<=word.length()-charType.getChar_length();i++) {
                        CuneiChar charr = (CuneiChar) dictHandler.getDictMap().get(word.substring(i, i + charType.getChar_length()));
                        if(charr==null)
                            continue;
                        System.out.println("Charr: "+charr.toString());
                        if (!charr.getMezlNumber().isEmpty() && charr.getaBzlNumber().isEmpty()) {
                            return "MiddleBabylonian";
                        }
                    }
                } else{
                    for(String syll:word.split("-")){
                        CuneiChar charr = (CuneiChar) dictHandler.translitToChar(syll);
                        if(charr==null)
                            continue;
                        System.out.println("Charr: "+charr.toString());
                        if (!charr.getMezlNumber().isEmpty() && charr.getaBzlNumber().isEmpty()) {
                            return "MiddleBabylonian";
                        }
                    }
                }
            }
        }
        return "OldBabylonian";
    }

    private TreeForTreeLayout<POSInBox> posConstituencyTreeBuilder(final Integer lineNumber){
        int start=this.sentencesByWordPosition.get(lineNumber).getOne();
        int end=this.sentencesByWordPosition.get(lineNumber).getTwo();
        int length = end-start;
        this.constTable = new ArrayList[length][];
        for (int i = 0; i < length; ++i)
        {
            this.constTable[i] = new ArrayList[length];
            for (int j = 0; j < length; ++j)
                this.constTable[i][j] = new ArrayList < String > ();
        }
        java.util.List<POSInBox> leafs=new LinkedList<>();
        for (int i = 0; i < length; ++i)
        {
            Set<String> keys = terminals.keySet();
            for (String key : keys)
            {
                if (terminals.get(key).equals(this.classificationResult.get(start + i).getTwo().getTag())) {
                    this.constTable[i][i].add(key);
                    leafs.add(new POSInBox(key,this.classificationResult.get(start + i).getTwo(),100,36));
                }
            }
        }
        for (int l = 2; l <= length; ++l)
        {
            for (int i = 0; i <= length - l; ++i)
            {
                java.util.List<POSInBox> currentheads=new LinkedList<>();
                int j = i + l - 1;
                for (int k = i; k <= j - 1; ++k)
                {
                    Set<String> keys = this.nonterminals.keySet();
                    for (String key : keys)
                    {
                        String[] values = this.nonterminals.get(key);
                        if ( this.constTable[i][k].contains((values[0]))
                                &&  this.constTable[k + 1][j].contains(values[1])){
                            POSInBox posin=new POSInBox(key,this.classificationResult.get(i).getTwo(),150,36);
                            posin.children.add(leafs.get(j));
                            currentheads.add(posin);
                            this.constTable[i][j].add(key);
                        }

                    }

                }
                leafs.clear();
                leafs.addAll(currentheads);
                currentheads.clear();
            }
        }
        if (this.constTable[0][length - 1].contains("S")) // we started from 0
            return new DefaultTreeForTreeLayout<>(
                    leafs.get(0));
        return null;
    }

    public TreeForTreeLayout<POSInBox> buildConstituencyTree(Integer lineNumber){
        return this.posConstituencyTreeBuilder(lineNumber);

    }

    public java.util.List<Integer> getNumberOfWordsPerLine(String[] line){
        java.util.List<Integer> result=new LinkedList<>();
        int total=0;
        for(String lin:line){
            System.out.println("Line: "+lin);
            total+=(StringUtils.countMatches(lin.trim(), " ")+1);
            result.add(total);
        }
        return result;
    }

    /**
     * Gets the nth occurance of char c in string str.
     *
     * @param str the string to search in
     * @param c   the char to search
     * @param n   the occurance
     * @return the position
     */
    public int getNthOccurrence(String str, char c, int n) {
        int pos = str.indexOf(c, 0);
        while (n-- > 0 && pos != -1)
            pos = str.indexOf(c, pos + 1);
        return pos;
    }


    /**
     * Detects phrases within a sentence to build a parse tree.
     */
    protected java.util.List<POSInBox> phraseDetector(final java.util.List<POSInBox> posNodes){
        java.util.List<POSInBox> headlist=new LinkedList<>();
        java.util.List<POSInBox> wordlist=new LinkedList<>();
        for(POSInBox posbox:posNodes){
            System.out.println("Headlist: "+headlist);
            System.out.println("Wordlist: "+wordlist);
            System.out.println("Dependencies: "+dependencies);
            wordlist.add(0,posbox);
            java.util.List<POSInBox> toremove=new LinkedList<>();
            java.util.List<POSInBox> toadd=new LinkedList<>();
            for(POSInBox head:headlist){
                System.out.println(posbox.posdef.getTag()+" "+head.posdef.getTag());
                if(this.dependencies.get(head.posdef.getTag()).contains(posbox.posdef.getTag())){
                    System.out.println("YES! Add "+head.posdef.getTag()+" to "+posbox.posdef.getTag());
                    posbox.children.add(head);
                    if(!headlist.contains(posbox)){
                        toadd.add(posbox);
                    }
                    toremove.add(head);
                }
            }
            headlist.addAll(toadd);
            for(POSInBox rem:toremove){
                headlist.remove(rem);
            }
            boolean wasfound=false;
            for(POSInBox word:wordlist){
                System.out.println(posbox.posdef.getTag()+" "+word.posdef.getTag());
                if(this.dependencies.get(posbox.posdef.getTag()).contains(word.posdef.getTag())){
                    System.out.println("YES2! Add "+word.posdef.getTag()+" to "+posbox.posdef.getTag());
                    word.children.add(posbox);
                    wasfound=true;
                    break;
                }
            }
            if(!wasfound){
                headlist.add(posbox);
            }
        }
        return headlist;
    }

    public TreeForTreeLayout<POSInBox> posDependencyTreeBuilder(final Integer lineNumber){
        POSInBox root = new POSInBox("root",unknownPOS, 100, 20);
        DefaultTreeForTreeLayout<POSInBox> tree = new DefaultTreeForTreeLayout<>(
                root);
        //Generate leafs of the tree
        java.util.List<POSInBox> leafs=new LinkedList<>();
        String sentence=this.sentences.get(lineNumber);
        for(int i=this.sentencesByWordPosition.get(lineNumber).getOne();i<=this.sentencesByWordPosition.get(lineNumber).getTwo();i++){
            leafs.add(new POSInBox(POSDefinition.splitString(this.classificationResult.get(i).getOne()+"("+this.classificationResult.get(i).getTwo().getTag()+")"+"["+this.classificationResult.get(i).getTwo().getValue()+"]",System.lineSeparator(),210),this.classificationResult.get(i).getTwo(),210,36));
        }
        java.util.List<POSInBox> nextstage=this.phraseDetector(leafs);
        for(POSInBox posInBox:nextstage){
            try {
                tree.addChild(root,posInBox);
            }catch(IllegalArgumentException e){

            }
            buildTreeRecursive(posInBox,tree);
        }

        return tree;
    }

    protected void buildTreeRecursive(POSInBox box,DefaultTreeForTreeLayout<POSInBox> tree){
        for(POSInBox b:box.children){
            try {
                tree.addChild(box,b);
            }catch(IllegalArgumentException e){

            }
            buildTreeRecursive(b,tree);
        }
    }


    public String evaluatePosTagForText(String goldstandardfilePath, String translittext, EvaluationMethod evalmethod,Boolean transliterationOrCunei){
        POSEvaluationImporter importer=new POSEvaluationImporter();
        Double totalamountofwords=0.,counter=0.;
        Double truepositive=0.;
        Map<String,Integer> posCountGold=new TreeMap<>();
        Map<String,Integer> posCountGenerated=new TreeMap<>();
        Map<String,Integer> posCountMatched=new TreeMap<>();
        Set<String> mismatches=new TreeSet<String>();
        Set<String> skipped=new TreeSet<String>();
        Integer linebreakcounter=0;
        Translator translator=new HitToEngTranslator(CharTypes.HITTITE);
        mismatches.add("[Original Word - Generated Word - Original POSTag - Generated POSTag]"+System.lineSeparator());
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(goldstandardfilePath,importer);
            List<POSDefinition> goldstandarddefintions=importer.posdefs;
            java.util.List<String> revised = Arrays.asList(translittext.split(System.lineSeparator()));
            for (String revi : revised) {
                String[] revisedwords;
                if(transliterationOrCunei)
                    revisedwords = revi.split(" \\[");
                else{
                    revisedwords = revi.split(" ");
                }
                for (final String revisedword : revisedwords) {
                    String word = revisedword.trim();
                    if(counter>=goldstandarddefintions.size()){
                        continue;
                    }
                    System.out.println("Equals? "+word.trim().replace("[","").replace("]","")+" - "+goldstandarddefintions.get(counter.intValue()).getVerbStem()+word.trim().replace("[","").replace("]","").equals(goldstandarddefintions.get(counter.intValue()).getVerbStem().replace(".","-")));
                    if(word.trim().isEmpty() || !sameChars(word.trim().replace("[","").replace("]","").replace("-",""),goldstandarddefintions.get(counter.intValue()).getVerbStem().replace(".","-").replace("-",""))) {
                        if(!transliterationOrCunei) {
                            skipped.add(word.trim().replace("[","").replace("]","").replace("-","")+" - "+goldstandarddefintions.get(counter.intValue()).getVerbStem().replace(".","-").replace("-",""));
                            counter++;
                        }
                        continue;
                    }
                    totalamountofwords++;
                    POSDefinition originalposdef=goldstandarddefintions.get(counter.intValue());
                    counter++;
                    if(originalposdef==null || originalposdef.getTag()==null){
                        originalposdef=unknownPOS;
                        //counter++;
                        continue;
                    }

                    if(!posCountGold.containsKey(originalposdef.getTag().toString())){
                        posCountGold.put(originalposdef.getTag().toString(),0);
                    }
                    posCountGold.put(originalposdef.getTag().toString(),posCountGold.get(originalposdef.getTag().toString())+1);
                    List<POSDefinition> result = this.getPosTagDefs(word, CharTypes.AKKADIAN.getCorpusHandlerAPI().getUtilDictHandler());
                    POSDefinition generatedposdef;
                    if (result.isEmpty() || counter>=goldstandarddefintions.size()) {
                        generatedposdef=unknownPOS;
                        //counter++;
                        //continue;
                    }else {
                        generatedposdef = result.iterator().next();
                    }
                    if(!posCountGenerated.containsKey(generatedposdef.getTag().toString())){
                        posCountGenerated.put(generatedposdef.getTag().toString(),0);
                    }
                    posCountGenerated.put(generatedposdef.getTag().toString(),posCountGenerated.get(generatedposdef.getTag().toString())+1);
                    if(originalposdef.getTag()==null)
                        continue;
                    System.out.println("Word: " + word+" - "+originalposdef.getVerbStem()+" - "+Arrays.toString(originalposdef.getValue()));
                    System.out.println(generatedposdef.getTag()+" - "+originalposdef.getTag());
                    if(generatedposdef.getTag()==null)
                        continue;
                    switch(evalmethod){
                        case WERRATE:
                            String translation=translator.wordByWordPOStranslate(word,transliterationOrCunei,0);
                            translation=translation.replace("URU","of ").replace("MM","M");
                            if(translation.matches("^M\\[A-Z\\]")){
                                translation=translation.substring(1,translation.length());
                            }
                            Boolean match=false;
                            for(String val:originalposdef.getValue()) {
                                if (val != null && translation != null){
                                System.out.println(val.trim() + " - " + translation.trim() + " - " + val.trim().toLowerCase().contains(translation.trim().toLowerCase()) + " - " + translation.trim().toLowerCase().contains(val.trim().toLowerCase()));
                                if (val.trim().toLowerCase().contains(translation.trim().toLowerCase()) || translation.trim().toLowerCase().contains(val.trim().toLowerCase())) {
                                    truepositive++;
                                    match = true;
                                }
                            }
                            }
                            if(!match){
                                mismatches.add(Arrays.toString(originalposdef.getValue())+" - "+translation+System.lineSeparator());
                            }
                            break;
                        case TOKENACCBASIC:
                            if(generatedposdef.getTag().equals(originalposdef.getTag())){
                                truepositive++;
                                if(!posCountMatched.containsKey(generatedposdef.getTag())){
                                    posCountMatched.put(originalposdef.getTag(),0);
                                }
                                posCountMatched.put(originalposdef.getTag(),posCountMatched.get(originalposdef.getTag())+1);
                            }else{
                                //if(linebreakcounter==2){
                                    mismatches.add(originalposdef.getVerbStem()+" - "+word+" - "+originalposdef.getTag()+" - "+generatedposdef.getTag()+System.lineSeparator());
                                    //linebreakcounter=0;
                                /*}else{
                                    mismatches.add(word+" - "+originalposdef.getVerbStem()+" - "+originalposdef.getTag()+" - "+generatedposdef.getTag());
                                    linebreakcounter++;
                                }*/

                            }
                            break;
                        case TOKENACC:
                            if(generatedposdef.getTag().equals(originalposdef.getTag())){
                                truepositive++;
                                if(!posCountMatched.containsKey(generatedposdef.getTag())){
                                    posCountMatched.put(originalposdef.getTag(),0);
                                }
                                posCountMatched.put(originalposdef.getTag(),posCountMatched.get(originalposdef.getTag())+1);
                                if(originalposdef.getPersonNumberCase()!=null){
                                    totalamountofwords++;
                                    if(originalposdef.getPersonNumberCase().equals(generatedposdef.getPersonNumberCase())) {
                                        truepositive++;
                                    }else{
                                        mismatches.add("Person: "+originalposdef.getVerbStem()+" - "+word+" - "+originalposdef.getPersonNumberCase()+" - "+generatedposdef.getPersonNumberCase()+System.lineSeparator());
                                    }
                                }
                                /*if(originalposdef.getVerbStem()!=null){
                                    totalamountofwords++;
                                    if(originalposdef.getVerbStem().equals(generatedposdef.getVerbStem())) {
                                        truepositive++;
                                    }else{
                                        mismatches.add("Verbstem: "+originalposdef.getVerbStem()+" - "+word+" - "+originalposdef.getVerbStem()+" - "+generatedposdef.getVerbStem()+System.lineSeparator());
                                    }
                                }*/
                                if(originalposdef.getTense()!=null){
                                    totalamountofwords++;
                                    if(originalposdef.getTense().equals(generatedposdef.getTense())) {
                                        truepositive++;
                                    }else{
                                        mismatches.add("Tense: "+originalposdef.getVerbStem()+" - "+word+" - "+originalposdef.getTense()+" - "+generatedposdef.getTense()+System.lineSeparator());
                                    }
                                }
                                if(originalposdef.getWordCase()!=null){
                                    for(WordCase cas:originalposdef.getWordCase()){
                                        totalamountofwords++;
                                        if(generatedposdef.getWordCase()!=null && generatedposdef.getWordCase().contains(cas)){
                                            truepositive++;
                                        }else{
                                            mismatches.add("Wordcase: "+originalposdef.getVerbStem()+" - "+word+" - "+cas.toString()+" - "+generatedposdef.getWordCase().toString()+System.lineSeparator());
                                        }
                                    }
                                }
                                /*if(originalposdef.getValue()!=null){
                                    int i=0;
                                    for(String val:originalposdef.getValue()){
                                        totalamountofwords++;
                                        if(generatedposdef.getValue()!=null && generatedposdef.getValue().length>i && val.equals(generatedposdef.getValue()[i++])) {
                                            truepositive++;
                                        }else{
                                            mismatches.add(originalposdef.getVerbStem()+" - "+word+" - "+Arrays.toString(originalposdef.getValue())+" - "+(generatedposdef.getValue()!=null?Arrays.toString(generatedposdef.getValue()):"null")+System.lineSeparator());
                                        }
                                    }
                                }*/
                            }else{
                                //if(linebreakcounter==2){
                                mismatches.add("Tag: "+originalposdef.getVerbStem()+" - "+word+" - "+originalposdef.getTag()+" - "+generatedposdef.getTag()+System.lineSeparator());
                                //linebreakcounter=0;
                                /*}else{
                                    mismatches.add(word+" - "+originalposdef.getVerbStem()+" - "+originalposdef.getTag()+" - "+generatedposdef.getTag());
                                    linebreakcounter++;
                                }*/

                            }
                            break;

                        default:
                            break;
                    }
                }
            }
            System.out.println("Goldstandardamount: "+goldstandarddefintions.size());
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        StringBuilder builder=new StringBuilder();
        builder.append("GoldStandard: ").append(posCountGold).append(System.lineSeparator());
        builder.append("Skipped: ").append(skipped.size()).append(System.lineSeparator());
        builder.append("Generated: ").append(posCountGenerated).append(System.lineSeparator());
        builder.append("Matched: ").append(posCountMatched).append(System.lineSeparator());
        builder.append("Total: ").append(totalamountofwords).append(System.lineSeparator());
        builder.append("Recognized: ").append(truepositive).append(System.lineSeparator());
        builder.append("Score: ").append((truepositive / totalamountofwords) * 100).append(System.lineSeparator());
        System.out.println(builder.toString());
        System.out.println(skipped.toString());
        return "<html>"+builder.toString().replace(System.lineSeparator(),"<br>")+mismatches.toString()+"</html>";
    }

    private boolean sameChars(String firstStr, String secondStr) {
        char[] first = firstStr.toCharArray();
        char[] second = secondStr.toCharArray();
        Arrays.sort(first);
        Arrays.sort(second);
        return Arrays.equals(first, second);
    }

    public String textToPosTagXML(String translittext) {
        StringWriter strwriter = new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartDocument();
            writer.writeStartElement(Tags.TEXT);
            java.util.List<String> revised = Arrays.asList(translittext.split(System.lineSeparator()));
            for (String revi : revised) {
                String[] revisedwords = revi.split(" \\[");
                for (final String revisedword : revisedwords) {
                    String word = revisedword.trim();
                    System.out.println("Word: " + word);
                    List<POSDefinition> result = this.getPosTagDefs(word, CharTypes.AKKADIAN.getCorpusHandlerAPI().getUtilDictHandler());
                    writer.writeStartElement(Tags.WORD);
                    writer.writeAttribute(Tags.POSTAG, (result == null || result.isEmpty() || result.get(0).getTag() == null) ? " " : result.get(0).getTag());
                    //writer.writeAttribute(Tags.RULE, (result == null || result.isEmpty() || result.get(0).getRegex() == null) ? " " : result.get(0).getRegex().toString());
                    writer.writeAttribute(Tags.POSCASE, (result == null || result.isEmpty() || result.get(0).getClassification() == null) ? " " : result.get(0).getClassification());
                    writer.writeAttribute("person",(result == null || result.isEmpty() || result.get(0).getAgensPersonCase() == null) ? " " : result.get(0).getAgensPersonCase().toString());
                    writer.writeAttribute("tense",(result == null || result.isEmpty() || result.get(0).getTense() == null) ? " " : result.get(0).getTense().toString());
                    writer.writeAttribute("wordcase",(result == null || result.isEmpty() || result.get(0).getWordCase() == null) ? " " : result.get(0).getWordCase().toString());
                    writer.writeAttribute("stem",(result == null || result.isEmpty() || result.get(0).getVerbStem() == null) ? " " : result.get(0).getVerbStem());
                    writer.writeAttribute("uri",(result == null || result.isEmpty() || result.get(0).getUri() == null) ? " " : result.get(0).getUri());
                    writer.writeAttribute("translation",(result == null || result.isEmpty() || result.get(0).getValue() == null) ? " " : Arrays.toString(result.get(0).getValue()));
                    writer.writeCharacters(word.replace("[", "").replace("]", ""));
                    writer.writeEndElement();
                    writer.writeCharacters("\n");
                }
            }
            writer.writeEndElement();
            writer.writeEndDocument();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return strwriter.toString();
    }

    public static void main(String[] args){
        System.out.println(CuneiDictHandler.reformatToASCIITranscription2("NU-GÀL"));
    }

}

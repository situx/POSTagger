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

package com.github.situx.postagger.dict.pos.util;

import com.github.situx.postagger.dict.chars.LangChar;
import com.github.situx.postagger.dict.chars.cuneiform.CuneiChar;
import com.github.situx.postagger.dict.dicthandler.cuneiform.AkkadDictHandler;
import com.github.situx.postagger.dict.dicthandler.cuneiform.CuneiDictHandler;
import com.github.situx.postagger.dict.dicthandler.cuneiform.HittiteDictHandler;
import com.github.situx.postagger.dict.dicthandler.cuneiform.SumerianDictHandler;
import com.github.situx.postagger.dict.utils.POSTag;
import com.github.situx.postagger.methods.Methods;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.TransliterationMethod;
import com.github.situx.postagger.util.enums.pos.POSTags;
import com.github.situx.postagger.util.enums.pos.PersonNumberCases;
import com.github.situx.postagger.util.enums.pos.Tenses;
import com.github.situx.postagger.util.enums.pos.WordCase;
import com.github.situx.postagger.util.enums.util.Tags;
import com.google.re2j.Matcher;
import com.google.re2j.Pattern;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.github.situx.postagger.util.enums.pos.*;
import nl.flotsam.xeger.Xeger;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.util.*;

/**
 * Class for defining a postag according to the regex postag specification.
 */
public class POSDefinition implements Comparable<POSDefinition>{
    public String getMeaningConcept() {
        return meaningConcept;
    }

    private String meaningConcept;
    public String currentword="";

    public LangChar currentLangChar;
    /**The classification of the postag.*/
    private String classification;
    private String desc;

    private String uri;
    /**The equals value of the postag.*/
    private String equals;
    private String extrainfo;
    /**The regex of the postag.*/
    private Pattern regex;
    /**The tag of the postag..*/
    private String tag;

    @Override
    public int compareTo(final POSDefinition o) {
        return desc.compareTo(o.desc)+regex.toString().compareTo(o.regex.toString());
    }

    public POSTags getPosTag() {
        return posTag;
    }

    public String getVerbStem() {
        return verbStem;
    }

    public EnumSet<WordCase> getWordCase() {
        return this.wordcases;
    }

    public void setPosTag(final POSTags posTag) {
        this.posTag = posTag;
    }

    /**The value of the postag.*/
    private String[] value;

    private String targetScript;

    private String verbStem;

    private POSTags posTag;

    private EnumSet<WordCase> wordcases;

    private Tenses tense;

    private PersonNumberCases objectPersonCase;

    private PersonNumberCases agensPersonCase;

    private PersonNumberCases personNumberCase;

    private Map<Integer,List<GroupDefinition>> groupconfig;

    private Map<Integer,String> currentgroupResults;

    private static final String HTMLLINEBREAK="<br>";

    private static final String VALUE="value";
    /**Defines if the stem of the PosDefinition is allowed to be equal with the word String.
     * If it is not allowed to be equal and the wordstem is equal to the word the POSTag is not matched.
     */
    private Boolean stemeqword=true;

    private CharTypes charType;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Constructor for the postag.
     * @param posTag  the posTag to set
     * @param regex the regex to define
     * @param equals equals string
     * @param wordCase the personNumberCase
     * @param value the value
     */
    public POSDefinition(String posTag,String regex,String equals, String wordCase,String[] value,String desc,String uri,String meaningConcept,String  stemeqword,String extrainfo,String targetScript,Map<Integer,List<GroupDefinition>> groupconfig,CharTypes chartype){
        this.tag=posTag;
        this.uri=uri;
        this.meaningConcept=meaningConcept;
        try{
            this.stemeqword=stemeqword!=null?Boolean.valueOf(stemeqword):true;
        }catch(Exception e){
            this.stemeqword=true;
        }
        this.regex=Pattern.compile(regex);
        this.equals=equals;
        this.classification=wordCase;
        if(value==null)
            this.value=new String[0];
        else
            this.value=value;
        if(extrainfo==null)
            this.extrainfo="";
        else
            this.extrainfo=extrainfo;

        if(targetScript==null)
            this.targetScript ="";
        else
            this.targetScript =targetScript;
        this.desc=desc;
        this.groupconfig=groupconfig;
        this.posTag= POSTags.valueOf(desc.toUpperCase());
        this.currentgroupResults=new TreeMap<>();
        this.wordcases=EnumSet.noneOf(WordCase.class);
        this.wordcases.clear();
        this.personNumberCase=PersonNumberCases.NONE;
        this.objectPersonCase=PersonNumberCases.NONE;
        this.agensPersonCase=PersonNumberCases.NONE;
        this.verbStem=null;
        this.charType=chartype;
    }

    public POSDefinition(POSDefinition copydef){
        this.tag=copydef.tag;
        this.uri=copydef.uri;
        this.regex=copydef.regex;
        this.equals=copydef.equals;
        this.classification=copydef.classification;
        this.value=copydef.value;
        this.extrainfo=copydef.extrainfo;
        this.targetScript =copydef.targetScript;
        this.desc=copydef.desc;
        this.groupconfig=copydef.groupconfig;
        this.posTag= copydef.posTag;
        this.currentgroupResults=new TreeMap<>();
        this.wordcases=EnumSet.noneOf(WordCase.class);
        this.wordcases.clear();
        this.personNumberCase=PersonNumberCases.NONE;
        this.objectPersonCase=PersonNumberCases.NONE;
        this.agensPersonCase=PersonNumberCases.NONE;
        this.verbStem=null;
    }

    public void clearPOSDefinition(){
        this.currentgroupResults=new TreeMap<>();
        this.wordcases=EnumSet.noneOf(WordCase.class);
        this.wordcases.clear();
        this.personNumberCase=PersonNumberCases.NONE;
        this.objectPersonCase=PersonNumberCases.NONE;
        this.agensPersonCase=PersonNumberCases.NONE;
        this.verbStem=null;
    }

    /**
     * Gets the classification of this POSDefinition.
     * @return the classification as String
     */
    public String getClassification() {
        return classification;
    }

    /**
     * Sets the classification of this POSDefinition.
     * @param classification the classification to set
     */
    public void setClassification(final String classification) {
        this.classification = classification;
    }

    /**
     * Gets the equals String of this postagger.
     * @return the equals String
     */
    public String getEquals() {
        return equals;
    }

    /**Sets the equals String of this postagger.
     *
     * @param equals the equals String to set
     */
    public void setEquals(final String equals) {
        this.equals = equals;
    }

    /**
     * Gets the regex of this POSDefinition.
     * @return the regex
     */
    public Pattern getRegex() {
        return regex;
    }

    /**
     * Sets the regex of this POSDefinition.
     * @param regex  the regex to set
     */
    public void setRegex(final Pattern regex) {
        this.regex = regex;
    }

    /**
     * Gets the tag of this POSDefinition.
     * @return  the tag as String
     */
    public String getTag() {
        return tag;
    }

    /**
     * Sets the tag of this POSDefinition.
     * @param tag the tag to set
     */
    public void setTag(final String tag) {
        this.tag = tag;
    }

    /**
     * Gets the value of this POSDefinition.
     * @return the value as String
     */
    public String[] getValue() {
        return value;
    }

    /**
     * Sets the value of this POSDefinition.
     * @param value the value to set
     */
    public void setValue(final String[] value) {
        this.value = value;
    }

    public Tenses getTense() {
        return tense;
    }

    public void setTense(final Tenses tense) {
        this.tense = tense;
    }

    public PersonNumberCases getPersonNumberCase() {
        return personNumberCase;
    }

    public void setPersonNumberCase(final PersonNumberCases personNumberCase) {
        this.personNumberCase = personNumberCase;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof POSDefinition && this.desc.equals(((POSDefinition) obj).desc) && this.regex.equals(((POSDefinition) obj).regex) && this.equals.equals(((POSDefinition) obj).equals);
    }

    public PersonNumberCases getObjectPersonCase() {
        return objectPersonCase;
    }

    public PersonNumberCases getAgensPersonCase() {
        return agensPersonCase;
    }

    public Boolean getStemeqword() {
        return stemeqword;
    }

    public void setStemeqword(Boolean stemeqword) {
        this.stemeqword = stemeqword;
    }

    /**
     * Performs a check to confirm that the current POSDefinition applies.
     * @param tocheck the String to check
     * @return
     */
    public List<Integer> performCheck(final String tocheck){
        //System.out.println("Regex: "+regex.toString()+" ToCheck: "+tocheck);
        this.clearPOSDefinition();
        List<Integer> result=new LinkedList<>();
        Integer temp;
        if(!equals.isEmpty()){
            if((temp=tocheck.equals(equals)?equals.length():-1)==-1){
                result.clear();
                return result;
            }else{
                result.add(0);
                result.add(temp);
            }
        }
        if(!regex.toString().isEmpty()){
            Matcher m=regex.matcher(tocheck);
            if(!(m.find())){
                result.clear();
                return result;
            }else{
                do {
                    for(int k=0;k<=m.groupCount() && !groupconfig.isEmpty();k++){
                        if(this.groupconfig.containsKey(k)){
                            this.currentgroupResults.put(k,m.group(k));
                            //System.out.println("Group: "+m.group(k));
                            if(m.group(k)!=null) {
                                for (GroupDefinition groupdef : groupconfig.get(k)) {
                                   //System.out.println("groupDef: "+groupdef.toString());
                                    Matcher mat = groupdef.getRegex().matcher(this.currentgroupResults.get(k));

                                    if (mat.matches() && groupdef.getGroupCase()!=null) {
                                        //System.out.println("Match: "+this.currentgroupResults.get(k));
                                        //System.out.println("currentGroupResults: "+this.currentgroupResults);
                                        switch (groupdef.getGroupCase()) {
                                            case "stem":
                                                this.verbStem = this.currentgroupResults.get(k).trim();
                                                this.currentLangChar=getTargetWord(this.charType,tocheck,this.verbStem);
                                                if(this.value.length==0 && this.verbStem.equals(tocheck) && !this.stemeqword && this.currentLangChar!=null && this.currentLangChar.getPostags()!=null){
                                                    boolean postagok=false;
                                                    for(POSTag postag:this.currentLangChar.getPostags()){
                                                        System.out.println("POSTag ok? "+postag.getPostag().toString()+" - "+this.tag+" "+postag.getPostag().toString().equals(this.tag));
                                                        if(postag.getPostag().toString().equals(this.tag)){
                                                            postagok=true;
                                                            break;
                                                        }
                                                    }
                                                    if(!postagok){
                                                        result.clear();
                                                        return result;
                                                    }

                                                }

                                                /*if(this.currentLangChar!=null && this.currentLangChar.getPostags()!=null){
                                                    boolean postagok=false;
                                                    for(POSTag postag:this.currentLangChar.getPostags()){
                                                        System.out.println("POSTag ok? "+postag.getPostag().toString()+" - "+this.tag+" "+postag.getPostag().toString().equals(this.tag));
                                                        if(postag.getPostag().toString().equals(this.tag)){
                                                            postagok=true;
                                                            break;
                                                        }
                                                    }
                                                    if(!postagok && this.currentLangChar!=null && this.currentLangChar.getPostags()!=null && !this.currentLangChar.getPostags().isEmpty()){
                                                        System.out.println("Setting new POSTAG: "+this.currentLangChar.getPostags().iterator().next().getPostag());
                                                        this.posTag=this.currentLangChar.getPostags().iterator().next().getPostag();
                                                        this.classification=this.currentLangChar.getPostags().iterator().next().getPostag().toString();
                                                    }
                                                }*/
                                                break;
                                            case "tense":
                                                this.tense = Tenses.valueOf(groupdef.getValue());
                                                break;
                                            case "declination":
                                                this.personNumberCase =PersonNumberCases.valueOf(groupdef.getValue());
                                                //System.out.println("Groupdef: "+groupdef.toString()+" Value: "+groupdef.getValue()+" "+this.personNumberCase.toString());
                                                break;
                                            case "wordcase":
                                                this.wordcases.add(WordCase.valueOf(groupdef.getValue()));
                                                break;
                                            case "directobjectdeclination":
                                                this.objectPersonCase=PersonNumberCases.valueOf(groupdef.getValue());
                                                break;
                                            case "indirectobjectdeclination":
                                                this.agensPersonCase=PersonNumberCases.valueOf(groupdef.getValue());
                                                break;
                                        }
                                    }
                                }
                            }

                        }
                    }
                    result.add(m.start());
                    result.add(m.end());
                }while (m.find());
            }
        }
        return result;
    }

    public static String splitString(String toSplit,String splitter,Integer length){
        StringBuilder buf = new StringBuilder();

        if (toSplit != null)
        {
            while(toSplit.length() > length)
            {
                int index = length;
                if (index >= 0){
                    buf.append(toSplit.substring(0, index));
                    buf.append(splitter);
                }
                toSplit = toSplit.substring(index+1);
            }
            buf.append(toSplit);

        }else{
            buf.append(" ");
        }
        return buf.toString();
    }

    public Map<Integer, String> getCurrentgroupResults() {
        return currentgroupResults;
    }

    public void setCurrentgroupResults(final Map<Integer, String> currentgroupResults) {
        this.currentgroupResults = currentgroupResults;
    }

    public Map<Integer, List<GroupDefinition>> getGroupconfig() {
        return groupconfig;
    }

    public void setGroupconfig(final Map<Integer, List<GroupDefinition>> groupconfig) {
        this.groupconfig = groupconfig;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(final String desc) {
        this.desc = desc;
    }

    public String getExtrainfo() {
        return extrainfo;
    }

    public void setExtrainfo(final String extrainfo) {
        this.extrainfo = extrainfo;
    }

    public String getTargetScript() {
        return targetScript;
    }

    public void setTargetScript(final String targetScript) {
        this.targetScript = targetScript;
    }

    public static LangChar getTargetWord(CharTypes charType,String currentword,String verbStem){
        while(currentword!=null && currentword.startsWith("-")){
            currentword=currentword.substring(1);
        }
        LangChar result;
            result = charType.getCorpusHandlerAPI().getUtilDictHandler().matchChar(currentword.trim());
            if(result==null){
                result=charType.getCorpusHandlerAPI().getUtilDictHandler().matchWordByTransliteration(currentword.trim());
            }
            System.out.println("ToHTMLString 1st: " + (result!=null?result.toString():"null"));

            if(result==null && verbStem!=null){
                result=charType.getCorpusHandlerAPI().getUtilDictHandler().matchWord(verbStem);

                if(result==null){
                    result=charType.getCorpusHandlerAPI().getUtilDictHandler().matchWordByTransliteration(verbStem);
                }
                System.out.println("ToHTMLString 2nd: "+(result!=null?result.toString():"null"));
            }
            if(result==null && charType.getCorpusHandlerAPI().getUtilDictHandler() instanceof CuneiDictHandler){
                Map<String,CuneiChar> logs=((CuneiDictHandler)charType.
                        getCorpusHandlerAPI().getUtilDictHandler())
                        .getLogographs();
                //System.out.println(currentword.trim()+" in logs? "+logs.containsKey(currentword.trim()));
                //System.out.println(verbStem+" in logs? "+logs.containsKey(verbStem));
                if(logs.containsKey(currentword.trim())){
                    result=logs.get(currentword.trim());
                }else if(verbStem!=null && logs.containsKey(verbStem)){
                    result=logs.get(verbStem);
                }
            }
            return result;
    }


    public static String getTargetString(CharTypes charType, String currentword, String verbStem, String detectedpostag){
        LangChar lchar=charType.getCorpusHandlerAPI().getUtilDictHandler().matchChar(currentword.trim());
        StringBuilder result=new StringBuilder();
        String temp;
        if(lchar!=null){
            result.append((lchar.getCharName()!=null?" ("+lchar.getCharName()+") ":""));
            result.append(lchar.getTransliterationSet().toString());
        }else if(currentword.matches(charType.getLegalTranslitCharsRegex())) {
            temp = charType.getCorpusHandlerAPI().transliterationToText(currentword.toLowerCase(), 0,
                    charType.getCorpusHandlerAPI().getUtilDictHandler(), false, true);
            if (temp == null || temp.isEmpty() || temp.matches("[ ]+")) {
                return "";
            }
            result.append(" (");
            result.append(temp);
            result.append(")");
        } else{
            temp = Methods.assignTransliteration(currentword.split(" "), charType.getCorpusHandlerAPI().getUtilDictHandler(),
                    TransliterationMethod.FIRST,true) + "*";
            if (temp.isEmpty() || temp.matches("[ ]+")) {
                return "";
            }
            result.append(" (");
            result.append(temp);
            result.append(")");
        }
        if(verbStem!=null){
            lchar=charType.getCorpusHandlerAPI().getUtilDictHandler().matchWord(verbStem);
            if(lchar==null){
                lchar=charType.getCorpusHandlerAPI().getUtilDictHandler().matchWordByTransliteration(currentword.trim());
                LangChar fortrans=charType.getCorpusHandlerAPI().getUtilDictHandler().matchWordByTransliteration(verbStem);
                if(fortrans!=null && fortrans.getFirstTranslation(Locale.ENGLISH)!=null) {
                    result.append("<br>Translation: ");
                    result.append(fortrans.getFirstTranslation(Locale.ENGLISH));
                    result.append(" [");
                    result.append(fortrans.getPostags()+" - "+detectedpostag);
                    result.append("]");
                }
                System.out.println("Stem: "+verbStem);
                if(fortrans!=null && fortrans.getFirstTranslation(Locale.ENGLISH)!=null){
                    System.out.println("Translation: "+fortrans.getFirstTranslation(Locale.ENGLISH));//+ charType.getCorpusHandlerAPI().getUtilDictHandler().getTranslitToWordDict());
                }
            }
            else {
                if(lchar.getTranslationSet(Locale.ENGLISH)!=null && !lchar.getTranslationSet(Locale.ENGLISH).isEmpty() && lchar.getPostags().contains(detectedpostag)) {
                    result.append("<br>Translation: ");
                    result.append(lchar.getFirstTranslation(Locale.ENGLISH));
                    result.append(" [");
                    result.append(lchar.getPostags());
                    result.append("]");
                }
                else if(charType.getCorpusHandlerAPI().getUtilDictHandler() instanceof AkkadDictHandler
                        || charType.getCorpusHandlerAPI().getUtilDictHandler() instanceof HittiteDictHandler
                        || charType.getCorpusHandlerAPI().getUtilDictHandler() instanceof SumerianDictHandler) {
                    CuneiDictHandler handler = (CuneiDictHandler) charType.getCorpusHandlerAPI().getUtilDictHandler();
                    if(handler.getLogographs().containsKey(verbStem)){
                        result.append("<br>Translation: ");
                        result.append(handler.getLogographs().get(verbStem).getFirstTranslation(Locale.ENGLISH));
                        result.append(" [");
                        result.append(lchar.getPostags());
                        result.append("]");
                    }

                }
                System.out.println("LChar Translation: "+lchar.toString());
                System.out.println("LChar Translation: "+lchar.getTransliterationSet());
                System.out.println("LChar Translation: "+lchar.getTranslations().toString());

            }
        }else if(lchar!=null){
            result.append("<br>Translation: ");
            result.append(lchar.getFirstTranslation(Locale.ENGLISH));
            result.append(" [");
            result.append(lchar.getPostags());
            result.append("]");
        }
        return result.toString();
    }


    public void setVerbStem(String verbStem) {
        this.verbStem = verbStem;
    }

   /* public static LangChar matchCharsInAllWays(CharTypes charType,String currentword){
        LangChar lchar=charType.getCorpusHandlerAPI().getUtilDictHandler().matchChar(currentword.trim());
        if(lchar!=null)
            return lchar;
        if(charType.getCorpusHandlerAPI().getUtilDictHandler() instanceof AkkadDictHandler
                || charType.getCorpusHandlerAPI().getUtilDictHandler() instanceof HittiteDictHandler
                || charType.getCorpusHandlerAPI().getUtilDictHandler().instanceof SumerianDictHandler){
            CuneiDictHandler handler = (CuneiDictHandler) charType.getCorpusHandlerAPI().getUtilDictHandler();
            if(handler.getLogographs().containsKey(verbStem)){
                result.append("<br>Translation: ");
                result.append(handler.getLogographs().get(verbStem).getFirstTranslation(Locale.ENGLISH));;
            }

        }

    }*/


    public String toHTMLString(ResourceBundle bundle, CharTypes charType, Boolean alltranslits) {
        StringBuilder result = new StringBuilder();
        System.out.println("Creating LangChar...");
        try {
        this.currentLangChar=POSDefinition.getTargetWord(charType,this.currentword,this.verbStem);
        if (!currentword.isEmpty()) {
            result.append(currentword.endsWith("-") ? currentword.substring(0, currentword.length() - 1) : currentword);
            if (!targetScript.isEmpty()) {
                result.append(" (");
                result.append(targetScript);
                result.append(")");
            } else {
                result.append(getTargetString(charType,currentword,this.verbStem,this.tag));
            }
        }
        result.append(HTMLLINEBREAK);
        if (this.currentLangChar != null) {
            result.append(this.currentLangChar.getCharInformation(HTMLLINEBREAK,false,true));
            //result.append(HTMLLINEBREAK);
            if(this.meaningConcept!=null && !this.meaningConcept.isEmpty()){
                result.append("Concept: <a href=\"");
                result.append(this.meaningConcept);
                result.append("\">");
                result.append(this.meaningConcept);
                result.append("</a>");
                result.append(HTMLLINEBREAK);
            } else if(this.currentLangChar.getConceptURI()!=null && !this.currentLangChar.getConceptURI().isEmpty()){
                result.append("Concept: <a href=\"");
                result.append(this.currentLangChar.getConceptURI());
                result.append("\">");
                result.append(this.currentLangChar.getConceptURI());
                result.append("</a>");
                result.append(HTMLLINEBREAK);
            }
        }
       /* if(lchar!=null && lchar.getTranslations()!=null && lchar.getTranslationSet(Locale.ENGLISH)!=null){
                result.append("Translation: ");
                result.append(lchar.getFirstTranslation(Locale.ENGLISH));
                result.append(HTMLLINEBREAK);

        }*/
        if(!desc.isEmpty() && alltranslits){
            result.append(bundle.getString("type"));
            result.append(": ");
            result.append(splitString(!bundle.containsKey(desc)?desc:bundle.getString(desc),HTMLLINEBREAK,100));
            if(classification!=null && !classification.isEmpty()){
                result.append(" (");
                result.append(splitString(classification,HTMLLINEBREAK,100));
                result.append(")"+HTMLLINEBREAK);
            } else{
                result.append(HTMLLINEBREAK);
            }
        }
        if(!regex.toString().isEmpty() && alltranslits && false){
            result.append(bundle.getString("regex"));
            result.append(": ");
            result.append(splitString(regex.toString(), HTMLLINEBREAK,100));
            result.append(HTMLLINEBREAK);
        }
        if(!equals.isEmpty() && alltranslits){
            result.append(bundle.getString("eq"));
            result.append(": ");
            result.append(splitString(equals,HTMLLINEBREAK,100));
            result.append(HTMLLINEBREAK);
        }
        if(value.length>0){
            switch (posTag){
                case NUMBER:
                    result.append(bundle.getString(VALUE));
                    result.append(": ");
                    result.append(Integer.valueOf(value[0])*(StringUtils.countMatches(currentword,"-")+1));
                    result.append(HTMLLINEBREAK);
                    break;
                case VERB:
                    result.append(bundle.getString(VALUE));
                    result.append(": to ");
                    result.append(splitString(value[0],HTMLLINEBREAK,100));
                    result.append(HTMLLINEBREAK);
                    break;
                default:
                    result.append(bundle.getString(VALUE));
                    result.append(": ");
                    result.append(splitString(value[0],HTMLLINEBREAK,100));
                    result.append(HTMLLINEBREAK);
            }
        }else{
            switch (posTag){
                case NAMEDENTITY:
                    currentword=currentword.replaceAll("[A-Z]","").replace("a-a-a", "aja").replace("e-e-e", "eje").replaceAll("-", "").replaceAll("[0-9]", "")
                            .replaceAll("[a]+", "a").replaceAll("[e]+", "e").replaceAll("[u]+", "u").replaceAll("[i]+", "i")+HTMLLINEBREAK;
                    result.append(bundle.getString(VALUE));
                    result.append(": ");
                    result.append((currentword.charAt(0)+"").toUpperCase());
                    result.append(currentword.substring(1,currentword.length()));
                    break;

                default:
            }
        }
        System.out.println(this.groupconfig);
        //System.out.println("CurrentGroupResults: "+currentgroupResults);
        boolean matched;
        for(Integer key:this.currentgroupResults.keySet()){
            if(this.currentgroupResults.get(key)!=null){
                for(GroupDefinition groupdef:this.groupconfig.get(key)){
                    matched=groupdef.getRegex().matcher(this.currentgroupResults.get(key)).matches();
                    if(matched && !groupdef.getGroupCase().equals("stem")){
                        result.append(groupdef.getName());
                        result.append(HTMLLINEBREAK);
                    }else if(matched){
                        result.append(groupdef.getName());
                        result.append(this.currentgroupResults.get(key));
                        result.append(HTMLLINEBREAK);
                    }
                }
            }
        }
        if(!extrainfo.isEmpty()){
            result.append(bundle.getString("info"));
            result.append(": ");
            result.append(splitString(StringEscapeUtils.unescapeJava(extrainfo),HTMLLINEBREAK,100));
            result.append(HTMLLINEBREAK);
        }
        currentword="";
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString().substring(0,result.length()-4);
    }

    @Override
    public String toString() {
        return this.tag + " " + /*this.regex.toString() + " " + */this.classification + " " + this.equals;
    }


    public String toJSONString(ResourceBundle bundle){
        StringBuilder result = new StringBuilder();
        if(!desc.isEmpty()){
            result.append(bundle.getString("type"));
            result.append(": ");
            result.append(splitString(!bundle.containsKey(desc)?desc:bundle.getString(desc),HTMLLINEBREAK,100));
            if(classification!=null && !classification.isEmpty()){
                result.append(" (");
                result.append(splitString(classification,HTMLLINEBREAK,100));
                result.append(")<br>");
            } else{
                result.append(HTMLLINEBREAK);
            }
        }
        /*if(!regex.toString().isEmpty()){
            result.append(bundle.getString("regex"));
            result.append(": ");
            result.append(splitString(regex.toString(), "<br>",100));
            result.append("<br>");
        }
        if(!equals.isEmpty()){
            result.append(bundle.getString("eq"));
            result.append(": ");
            result.append(splitString(equals,"<br>",100));
            result.append("<br>");
        }*/
        if(posTag!=null){
            result.append("POSTag: ");
            result.append(this.posTag.toString());
            result.append(HTMLLINEBREAK);
        }
        if(!extrainfo.isEmpty()){
            result.append(bundle.getString("info"));
            result.append(": ");
            result.append(splitString(StringEscapeUtils.unescapeJava(extrainfo),HTMLLINEBREAK,100));
            result.append(HTMLLINEBREAK);
        }
        if(value.length>0){
            result.append("Value");
            result.append(": ");
            for(String val:value){
                  result.append(val);
                  result.append(",");
            }
            result.delete(result.length()-1,result.length());
            result.append(HTMLLINEBREAK);
        }
        return result.toString();
    }

    /**
     * Converts the POSDefinition to XML.
     * @return the XML String
     */
    public String toXML(){
        StringWriter strwriter=new StringWriter();
        XMLOutputFactory output3 = XMLOutputFactory.newInstance();
        output3.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        XMLStreamWriter writer;
        try {
            writer = new IndentingXMLStreamWriter(output3.createXMLStreamWriter(strwriter));
            writer.writeStartElement("tag");
            writer.writeAttribute("equals",this.equals);
            writer.writeAttribute("name",this.tag);
            writer.writeAttribute("uri",this.uri);
            writer.writeAttribute("regex",this.regex.toString());
            writer.writeAttribute("case",this.classification);
            writer.writeEndElement();
        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println("ToString: "+strwriter.toString());
        return strwriter.toString();
    }


    public static void main(String[] args){
        Xeger xeger=new Xeger("(^(l[aeiu]-|n[aeiu]-)?([aeiu][0-9]?-|[aeiu][0-9]?)?(g)([aeiu][0-9]?)?-(ta-|te-)?([aeiu][0-9]?)?(m-m|mm|m)([aeiu][0-9]?-)?([aeiu][0-9]?)?((-)?d)([aeiu][0-9]?)?((-ia|-i|-ka|-ki|-szu|-sza|-ni|-ku-nu|-ki-na|-szu-nu|-szi-na)?$|(-am|-nim|-kum|-kim|-szu(m)?|-szi(m)?|-ni-a-szi(m)?|-ku-nu-szi(m)?|-ki-na-szi(m)?|-szu-nu-szi(m)?|-szi-na-szi(m)?)?(-ni|-ka|-ki|-szu|-szi|-ni-a-ti|-ku-nu-ti|-ki-na-ti|-szu-nu-ti|-szi-na-ti)?|(-am|-nim|-kum|-kim|-szu(m)?|-szi(m)?|-ni-a-szi(m)?|-ku-nu-szi(m)?|-ki-na-szi(m)?|-szu-nu-szi(m)?|-szi-na-szi(m)?)?$|(-a-ku|-a-ta|-a-ti|-at|-anu|-a-tu-nu|-a-ti-na|-u|-a)?)(-m[ai])?$)|(^((\uD808\uDE61|\uD808\uDDF7|\uD808\uDE4C|\uD808\uDC73|\uD808\uDE3E|\uD808\uDE48|\uD808\uDE4C)?(\uD808\uDC00|\uD808\uDC09|\uD808\uDE7F|\uD808\uDC8A|\uD808\uDD3F|\uD808\uDD47|\uD808\uDF11|\uD808\uDF13)?(\uD808\uDC1D|\uD808\uDD45|\uD808\uDD65)(\uD808\uDEEB|\uD808\uDEFC)?(\uD808\uDF05)(\uD808\uDD40|\uD808\uDD3F|\uD808\uDD57|\uD808\uDDA0|\uD808\uDED7|\uD809\uDC3C|\uD808\uDE4C|\uD808\uDD65\uD808\uDE61|\uD808\uDDA0\uD808\uDE3E|\uD808\uDED7\uD808\uDE61)?|(\uD808\uDD20|\uD808\uDE4F|\uD808\uDD23|\uD808\uDED7|\uD808\uDED7|\uD808\uDE4C\uD808\uDC00\uD808\uDED7|\uD808\uDED7\uD808\uDE61\uD808\uDED7)?|(\uD808\uDE4C|\uD808\uDD57|\uD808\uDDA0|\uD808\uDED7|\uD808\uDD46|\uD808\uDE4C\uD808\uDC00\uD808\uDEFE|\uD808\uDDAA\uD808\uDE61\uD808\uDEFE|\uD808\uDDA0\uD808\uDE3E\uD808\uDEFE|\uD808\uDED7\uD808\uDE61\uD808\uDEFE|\uD808\uDD46\uD808\uDE3E\uD808\uDEFE)?(\uD808\uDC00\uD808\uDDAA|\uD808\uDC00\uD808\uDEEB|\uD808\uDC00\uD808\uDEFE|\uD808\uDC1C|\uD808\uDC00\uD808\uDE61|\uD808\uDC00\uD808\uDF05\uD808\uDE61|\uD808\uDC1C\uD808\uDEFE\uD808\uDE3E|\uD808\uDF0B|\uD808\uDC00)?(\uD808\uDE20|\uD808\uDE2A)?)$)");
        System.out.println(xeger.generate());
    }
}

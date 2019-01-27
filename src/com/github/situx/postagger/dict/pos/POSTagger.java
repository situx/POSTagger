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

package com.github.situx.postagger.dict.pos;

import com.github.situx.postagger.dict.importhandler.POSEvaluationImporter;
import com.github.situx.postagger.dict.pos.util.GroupDefinition;
import com.github.situx.postagger.util.Tuple;
import com.google.re2j.Pattern;
import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.pos.util.POSDefinition;
import com.github.situx.postagger.main.gui.tool.MainFrame;
import com.github.situx.postagger.main.gui.util.POSInBox;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.methods.EvaluationMethod;
import com.github.situx.postagger.util.enums.util.Files;
import com.github.situx.postagger.util.enums.util.Tags;
import org.abego.treelayout.TreeForTreeLayout;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * Created by timo on 21.07.14.
 */
public class POSTagger extends DefaultHandler2 {
    private final String locale;
    protected final CharTypes charType;
    /**The map of POSDefinitions.*/
    protected Map<Integer,List<POSDefinition>> classifiers;
    /**The map of POSDefinitions.*/
    protected Map<Integer,Tuple<String,POSDefinition>> classificationResult;
    /**Map from classification to color for the GUI.*/
    protected Map<String, Color> poscolors;

    protected Map<String,Integer> orderToPOS;

    private Map<String,String> posToURI;

    FileWriter writer;
    protected Map<String,String> terminals;

    protected Map<String,String[]> nonterminals;

    protected ArrayList[][] constTable;

    protected Map<String,Map<Integer,List<GroupDefinition>>> groupconfigs;

    protected static Pattern generalPattern= Pattern.compile("(.*)");

    protected boolean groupconfig=false;
    protected Integer wordcounter=0,linecounter=0;

    private String constituencyTemp1="";
    private List<String> constittemp3;
    public Map<Integer, String> sentences;
    protected Map<Integer, Tuple<Integer,Integer>> sentencesByWordPosition;

    protected Map<String,List<String>> dependencies;
    private boolean constit=false;

    public Map<Integer, List<POSDefinition>> getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(final Map<Integer, List<POSDefinition>> classifiers) {
        this.classifiers = classifiers;
    }

    public Map<Integer, String> getColorToPos() {
        return colorToPos;
    }

    public void setColorToPos(final Map<Integer, String> colorToPos) {
        this.colorToPos = colorToPos;
    }

    /**Map from classification to color for the GUI.*/
    protected Map<Integer, String> colorToPos;

    protected POSDefinition lastmatched;
    protected String lastmatchedword="";

    private static final String TAG="tag";

    private static final String GROUP="group";

    private static final String NAME="name";

    private static final String REGEX="regex";

    private static final String CONST="const";

    private static final String DESC="desc";

    private static final String COLOR="color";

    private static final String URI="uri";

    private static final String VALUE="value";

    private static final String EQUALS="equals";

    protected POSDefinition unknownPOS=new POSDefinition("UNKNOWN","","","",new String[0],"UNKNOWN","","","","","",new TreeMap<>(),CharTypes.CUNEICHAR);

    /**
     * Constructor for this class.
     * @param poscolors the colors to set
     */
    public POSTagger(final Map<String, Color> poscolors,final CharTypes chartype){
        this.poscolors=poscolors;
        this.dependencies=new TreeMap<>();
        this.classifiers =new TreeMap<>();
        this.classificationResult=new TreeMap<>();
        this.colorToPos=new TreeMap<>();
        this.posToURI=new TreeMap<>();
        this.orderToPOS=new TreeMap<>();
        this.groupconfigs=new TreeMap<>();
        this.terminals=new TreeMap<>();
        this.nonterminals=new TreeMap<>();
        this.constittemp3=new LinkedList<>();
        this.locale=chartype.getLocale();
        this.charType=chartype;
        try {
            this.importFromXML(Files.POSDIR+locale+Files.XMLSUFFIX.toString());
        } catch (ParserConfigurationException | SAXException | XMLStreamException | IOException e) {
            e.printStackTrace();
        }


    }

    public void reset(){
        this.classificationResult.clear();
        this.wordcounter=0;
    }

    public Map<Integer,String> sentenceDetector(String[] words,String[] lines){
        return new TreeMap<>();
    }
    public TreeForTreeLayout<POSInBox> buildConstituencyTree(Integer lineNumber){
        return new TreeForTreeLayout<POSInBox>() {
            @Override
            public POSInBox getRoot() {
                return null;
            }

            @Override
            public boolean isLeaf(final POSInBox posInBox) {
                return false;
            }

            @Override
            public boolean isChildOfParent(final POSInBox posInBox, final POSInBox posInBox2) {
                return false;
            }

            @Override
            public Iterable<POSInBox> getChildren(final POSInBox posInBox) {
                return null;
            }

            @Override
            public Iterable<POSInBox> getChildrenReverse(final POSInBox posInBox) {
                return null;
            }

            @Override
            public POSInBox getFirstChild(final POSInBox posInBox) {
                return null;
            }

            @Override
            public POSInBox getLastChild(final POSInBox posInBox) {
                return null;
            }
        };
    }
    /**
     *
     * @param word
     * @param dicthandler
     * @return
     */
    public List<Integer> getPosTag(String word,DictHandling dicthandler){
        return new LinkedList<>();
    }

    public List<POSDefinition> getPosTag(String word,DictHandling dicthandler,Boolean dummy){
        return new LinkedList<>();
    }

    public List<POSDefinition> getPosTagDefs(String word, DictHandling handler/*,final Boolean dummy*/){
        return null;
    }

    /**
     * Gets the POSColors for this POSTagger.
     * @return  the colormap
     */
    public Map<String, Color> getPoscolors() {
        return poscolors;
    }

    /**
     * Imporst POSTag Definitions from XML.
     * @param filepath the filepath for reading the definitions
     * @throws ParserConfigurationException on error
     * @throws SAXException on error
     * @throws IOException on error
     */
    public void importFromXML(String filepath) throws ParserConfigurationException, SAXException, IOException, XMLStreamException {
        this.classifiers.clear();
        this.colorToPos.clear();
        System.setProperty("avax.xml.parsers.SAXParserFactory",
                "org.apache.xerces.parsers.SAXParser");
        SAXParser parser= SAXParserFactory.newInstance().newSAXParser();
        java.io.InputStream in = new FileInputStream(new File(filepath));
        InputSource is = new InputSource(in);
        is.setEncoding(Tags.UTF8.toString());
        parser.parse(in, this);
        parser.reset();
        //System.out.println(this.poscolors.toString());
        //System.out.println(this.classifiers.toString());
        /*for(Integer key:this.classifiers.keySet()){
            StringBuffer res=new StringBuffer();
            this.classifiers.get(key).stream().filter(def -> !def.getRegex().toString().isEmpty()).forEach(def -> {
                res.append(def.getRegex().toString() + "|");
            });
        }
        this.toXML("outtest.xml");
        *///System.out.println("JoinedRegexes: "+this.joinedregexes.toString());
    }

    /**
     * Util method for parsing html colors.
     * @param htmlcolor the html color to parse
     * @return the html color as Color object
     */
    public static Color parseHTMLColor(String htmlcolor){
        int red,green,blue;
        htmlcolor=htmlcolor.substring(1);
        red=Integer.valueOf(htmlcolor.substring(0,2),16);
        green=Integer.valueOf(htmlcolor.substring(2,4),16);
        blue=Integer.valueOf(htmlcolor.substring(4,6),16);
        return new Color(red,green,blue);
    }

    public static boolean isRegex(final String str) {
        try {
            java.util.regex.Pattern.compile(str);
            return true;
        } catch (java.util.regex.PatternSyntaxException e) {
            return false;
        }
    }

    public TreeForTreeLayout<POSInBox> posDependencyTreeBuilder(Integer lineNumber){
          return new TreeForTreeLayout<POSInBox>() {
              @Override
              public POSInBox getRoot() {
                  return null;
              }

              @Override
              public boolean isLeaf(final POSInBox posInBox) {
                  return false;
              }

              @Override
              public boolean isChildOfParent(final POSInBox posInBox, final POSInBox posInBox2) {
                  return false;
              }

              @Override
              public Iterable<POSInBox> getChildren(final POSInBox posInBox) {
                  return null;
              }

              @Override
              public Iterable<POSInBox> getChildrenReverse(final POSInBox posInBox) {
                  return null;
              }

              @Override
              public POSInBox getFirstChild(final POSInBox posInBox) {
                  return null;
              }

              @Override
              public POSInBox getLastChild(final POSInBox posInBox) {
                  return null;
              }
          };
    }

    public Map<String, String> getPosToURI() {
        return posToURI;
    }

    public void setPosToURI(Map<String, String> posToURI) {
        this.posToURI = posToURI;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        switch (qName){
            case "tagcolor": this.poscolors.put(attributes.getValue(DESC),parseHTMLColor(attributes.getValue(COLOR)));
                this.colorToPos.put(parseHTMLColor(attributes.getValue(COLOR)).getRGB(),attributes.getValue(DESC));
                this.orderToPOS.put(attributes.getValue(TAG),Integer.valueOf(attributes.getValue("matchorder")));
                if(attributes.getValue(URI)!=null)
                    this.posToURI.put(attributes.getValue(TAG),attributes.getValue(URI));
                this.dependencies.put(attributes.getValue(TAG),new LinkedList<String>());
                break;
            case "groupconfig": groupconfig=true;
                this.groupconfigs.put(attributes.getValue(TAG),new TreeMap<Integer,List<GroupDefinition>>());
                break;
            case "dependence": groupconfig=true;
                String dependee=attributes.getValue("dependee");
                this.dependencies.get(dependee).add(attributes.getValue("depender"));
                break;
            case "constituencies": constit=true;
                this.constituencyTemp1=attributes.getValue(CONST);
                this.constittemp3.clear();
                break;
            case "constituency": constit=true;
                this.constittemp3.add(attributes.getValue(CONST));
                break;
            case "group":
                int group=Integer.valueOf(attributes.getValue(GROUP));
                System.out.println("Group: "+group);
                if(groupconfig){
                    if(!this.groupconfigs.get(attributes.getValue(TAG)).containsKey(group)){
                        this.groupconfigs.get(attributes.getValue(TAG)).put(group,new LinkedList<>());
                    }
                    this.groupconfigs.get(attributes.getValue(TAG)).get(group).add(new GroupDefinition(attributes.getValue(REGEX),attributes.getValue(EQUALS),attributes.getValue(URI),attributes.getValue(NAME),attributes.getValue("case"),attributes.getValue(VALUE),attributes.getValue(TAG),Integer.valueOf(attributes.getValue(GROUP))));
                }
                System.out.println("Groupconfigs: "+this.groupconfigs);
                break;
            case "tag":  String tag=attributes.getValue(NAME);
                Integer order= this.orderToPOS.get(tag);
                if(!this.classifiers.containsKey(order)){
                    this.classifiers.put(order,new LinkedList<>());
                }
                POSDefinition def=new POSDefinition(tag,attributes.getValue(REGEX),attributes.getValue(EQUALS),attributes.getValue("case"),(attributes.getValue(VALUE)==null?new String[0]:attributes.getValue(VALUE).split(";")),attributes.getValue(DESC),attributes.getValue(URI)==null?"":attributes.getValue(URI),attributes.getValue("concept"),attributes.getValue("stemeqword"),attributes.getValue("extrainfo"),attributes.getValue("target"),!this.groupconfigs.containsKey(tag)?new TreeMap<Integer,List<GroupDefinition>>():this.groupconfigs.get(tag),this.charType);
                this.classifiers.get(order).add(def);
                break;
        }
    }

    public Map<String, Integer> getOrderToPOS() {
        return orderToPOS;
    }

    public void setOrderToPOS(final Map<String, Integer> orderToPOS) {
        this.orderToPOS = orderToPOS;
    }

    public void setPoscolors(final Map<String, Color> poscolors) {

        this.poscolors = poscolors;
    }

    private POSDefinition getMostRelevantPOSTag(List<POSDefinition> posdefs){
        POSDefinition result=posdefs.get(0);
        Integer curpriority=Integer.MAX_VALUE;
        for(POSDefinition posd:posdefs){
            for(Integer order:this.classifiers.keySet()){
                if(this.classifiers.get(order).contains(posd)){
                    if(curpriority>order){
                        curpriority=order;
                        result=posd;
                    }else if(curpriority.equals(order) && result.currentword.length()>posd.currentword.length()){
                        result=posd;
                    }
                    break;
                }
            }
        }
        return result;
    }

    public Map<Integer,Map<String,List<POSDefinition>>> getMostRelevantPOSTag(Map<Integer,Map<String,List<POSDefinition>>> posdefs){
        Map<Integer,Map<String,List<POSDefinition>>> resmap=new TreeMap<>();
        POSDefinition result=null;
        Integer curpriority=Integer.MAX_VALUE;
        String curString="";
        for(Integer posid:posdefs.keySet()){
            for(String str:posdefs.get(posid).keySet()){
                if(posdefs.get(posid).get(str).isEmpty())
                    continue;
                POSDefinition posd=this.getMostRelevantPOSTag(posdefs.get(posid).get(str));
                for(Integer order:this.classifiers.keySet()){
                    if(this.classifiers.get(order).contains(posd)){
                        if(curpriority>order){
                            curpriority=order;
                            curString=str;
                            result=posd;
                        }else if(curpriority.equals(order) && result!=null && result.currentword.length()>posd.currentword.length()){
                            result=posd;
                            curString=str;
                        }
                        break;
                    }
                }
            }

        }
        resmap.put(curpriority, new TreeMap<>());
        resmap.get(curpriority).put(curString,new LinkedList<>());
        resmap.get(curpriority).get(curString).add(result);
        return resmap;
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        switch (qName){
            case "groupconfig":
                this.groupconfig=false;
                break;
            case "constituencies": this.constit=false;
                if(this.constittemp3.size()>1){
                    this.nonterminals.put(constituencyTemp1,constittemp3.toArray(new String[constittemp3.size()]));
                }else if(!constittemp3.isEmpty()){
                    this.terminals.put(constituencyTemp1,constittemp3.get(0));
                }
                break;
            default:
        }
    }

    public String evaluatePosTagForText(String goldstandardfilePath, String translittext, EvaluationMethod evalmethod,Boolean transliterationOrCunei){
        POSEvaluationImporter importer=new POSEvaluationImporter();
        Integer totalamountofwords=0,counter=0;
        Integer truepositive=0;
        Map<String,Integer> posCountGold=new TreeMap<>();
        Map<String,Integer> posCountGenerated=new TreeMap<>();
        try {
            SAXParserFactory.newInstance().newSAXParser().parse(goldstandardfilePath,importer);
            List<POSDefinition> goldstandarddefintions=importer.posdefs;
            java.util.List<String> revised = Arrays.asList(translittext.split(System.lineSeparator()));
            for (String revi : revised) {
                String[] revisedwords = revi.split(" \\[");
                for (final String revisedword : revisedwords) {
                    String word = revisedword.trim();
                    System.out.println("Word: " + word);
                    totalamountofwords++;
                    List<POSDefinition> result = this.getPosTagDefs(word, CharTypes.AKKADIAN.getCorpusHandlerAPI().getUtilDictHandler());
                    POSDefinition generatedposdef=result.iterator().next();
                    POSDefinition originalposdef=goldstandarddefintions.get(counter++);
                    if(!posCountGold.containsKey(originalposdef.getTag())){
                        posCountGold.put(originalposdef.getTag(),0);
                    }
                    posCountGold.put(originalposdef.getTag(),posCountGold.get(originalposdef.getTag())+1);
                    switch(evalmethod){
                        case TOKENACCBASIC:
                            //System.out.println(word+" - "+)
                            if(generatedposdef.getTag().equals(originalposdef.getTag())){
                                truepositive++;
                                if(!posCountGenerated.containsKey(generatedposdef.getTag())){
                                    posCountGenerated.put(originalposdef.getTag(),0);
                                }
                                posCountGenerated.put(originalposdef.getTag(),posCountGenerated.get(originalposdef.getTag())+1);
                            }
                            break;
                        case TOKENACC:
                            if(generatedposdef.getTag().equals(originalposdef.getTag())){

                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        System.out.println("GoldStandard: "+posCountGold);
        System.out.println("Generated: "+posCountGenerated);
        System.out.println("Total: "+totalamountofwords);
        System.out.println("Recognized: "+truepositive);
        System.out.println("Accuracy: "+(truepositive/totalamountofwords));
        return null;
    }

    public String textToPosTagXML(String translittext){
        return null;
    }

    public void toXML(String path) throws XMLStreamException, FileNotFoundException, UnsupportedEncodingException {

    }

    public void toHighlightJSON(String path) throws IOException {
          Set<POSDefinition> posdefs=new TreeSet<>();
        Map<POSDefinition,Integer> prios=new TreeMap<>();
        Map<Integer,POSDefinition> prios2=new TreeMap<>();
        BufferedWriter writer=new BufferedWriter(new FileWriter(new File(path)));
        writer.write("var "+locale+"_matches={matches: [");
        for(Integer def:this.classifiers.keySet()){
            for(POSDefinition posdef:this.classifiers.get(def)){
                posdefs.add(posdef);
                prios.put(posdef, def);
                prios2.put(def,posdef);
                writer.write("{" + System.lineSeparator());
                writer.write("\"match\":"+"/"+posdef.getRegex().toString().replace("^","[\\s\\[]").replace("$","")+"[^\\s]*/gm,"+System.lineSeparator());
                writer.write("\"matchClass\":\"" + posdef.getDesc() + "\"," + System.lineSeparator());
                writer.write("\"priority\":" + def +","+ System.lineSeparator());
                writer.write("\"tag\":\"" + posdef.getTag() +"\","+ System.lineSeparator());
                writer.write("\"description\":\""+posdef.toJSONString(MainFrame.bundle)+"\""+System.lineSeparator());
                writer.write("}," + System.lineSeparator());
            }
        }
        writer.write("]");
        writer.close();
                 /*for(Integer posid:prios2.keySet()){
                        POSDefinition posdef=prios2.get(posid);

                 }*/

          Set<String> useddescs=new HashSet<>();
          writer=new BufferedWriter(new FileWriter(new File(path.replace(".js",".css"))));
            for(POSDefinition posdef:posdefs){
                if(!useddescs.contains(posdef.getDesc())){
                    writer.write("."+posdef.getDesc()+" {"+System.lineSeparator());
                    writer.write("color: #"+Integer.toHexString(this.poscolors.get(posdef.getDesc()).getRGB())+";"+System.lineSeparator());
                    writer.write("},"+System.lineSeparator());
                    useddescs.add(posdef.getDesc());
                }
            }
        writer.close();
        writer=new BufferedWriter(new FileWriter(new File(path.replace(".js","_groups.js"))));
        writer.write("var "+locale+"_matches_groups={");

        for(String def:this.groupconfigs.keySet()){
            writer.write("\""+def+"\":[" + System.lineSeparator());
            for(Integer defkey:this.groupconfigs.get(def).keySet()){
                for(GroupDefinition deff:this.groupconfigs.get(def).get(defkey)){
                    writer.write("{"+System.lineSeparator()+"\"match\":"+"/"+deff.getRegex().toString().replace("^","[\\s\\[]").replace("$","")+"[^\\s]*/gm,"+System.lineSeparator());
                    writer.write("\"matchClass\":\"" + deff.getGroupCase() + "\"," + System.lineSeparator());
                    writer.write("\"group\": "+defkey+","+System.lineSeparator());
                    writer.write("\"description\":\"" + deff.getName() +"\""+ System.lineSeparator());
                    writer.write("}," + System.lineSeparator());
                }
            }
            writer.write("]," + System.lineSeparator());
        }
        writer.write("};");
        writer.close();
    }

    public CharTypes getCharType() {
        return charType;
    }
}

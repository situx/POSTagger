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

package com.github.situx.postagger.dict;

import com.github.situx.postagger.dict.dicthandler.DictHandling;
import com.github.situx.postagger.dict.importhandler.cuneiform.CuneiImportHandler;
import com.github.situx.postagger.dict.pos.POSTagger;
import com.github.situx.postagger.dict.semdicthandler.conceptresolver.connector.WikiDataConnection;
import com.github.situx.postagger.dict.utils.HitTabletDescription;
import com.github.situx.postagger.dict.utils.Tablet;
import com.github.situx.postagger.main.gui.util.SyllableSeparator;
import com.github.situx.postagger.util.Tuple;
import com.github.situx.postagger.util.UnicodeXMLStreamWriter;
import com.github.situx.postagger.util.enums.methods.CharTypes;
import com.github.situx.postagger.util.enums.util.Tags;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.vocabulary.XSD;
import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.github.situx.postagger.dict.utils.Epoch;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * WebCrawler for crawling a dictionary corpusimport file.
 * User: timo
 * Date: 13.10.13
 * Time: 17:15
 */
public class DictWebCrawler {
    /**URL Prefixes for DictWebCrawler.*/
    public static final String urlprefix="http://www.premiumwanadoo.com/cuneiform.languages/dictionary/";
    public static final String urlprefix2="http://www.assyrianlanguages.org/akkadian/";
    public static final String urlprefix3="http://psd.museum.upenn.edu/epsd/";
    public static final String sumeriantoc="http://psd.museum.upenn.edu/epsd/signnames-toc-";
    public static final String akkadiantoc="http://psd.museum.upenn.edu/epsd/akkadian-toc-";
    static HttpClient httpclient=new DefaultHttpClient();;
    String url;

    /**Constructor for this class.*/
    public DictWebCrawler(){
         this.httpclient=new DefaultHttpClient();
    }

    /**
     * Testing main method.
     * @param args
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XMLStreamException
     */
public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        DictWebCrawler crawler=new DictWebCrawler();
        //crawler.getAkkSumRelationsFromAssDict("");
        //crawler.crawlUniLP(null,null);
        crawler.crawlAssDict(null,null);
    //AkkadDictHandler akkad=new AkkadDictHandler(CharTypes.AKKADIAN.getStopchars());
    //akkad.parseDictFile(new File("akkad.xml"));
        //crawler.crawlAssDict(akkad,"http://www.premiumwanadoo.com/cuneiform.languages/dictionary/list.php");
    //crawler.crawlHitDict(akkad);
    //crawler.crawlCDLIForTabletMetadata(akkad,Arrays.asList(new String[]{"P000001"}));
    //crawler.useCuneify(akkad,"tu5");
    }


    public String useCuneify(final DictHandling dict, final String translit) throws IOException {
        StringBuilder result=new StringBuilder();
        HttpGet method = new HttpGet("http://oracc.museum.upenn.edu/cgi-bin/cuneify?input="+ URLEncoder.encode(translit));
        HttpResponse response=this.httpclient.execute(method);
        Source htmlparser = new Source(response.getEntity().getContent());
        result.append(htmlparser.getAllStartTags("p").get(1).getElement().getTextExtractor().toString());
        System.out.println("Response: "+ result);
        return result.toString();
    }

    /**
     * Starts the crawling method for the given url.
     * @param dict the dicthandler to fill
     * @param urlstring the String of the url
     * @throws IOException on error
     */
    public void crawl(final DictHandling dict, final String urlstring) throws IOException {
        HttpGet method = new HttpGet(urlstring);
        HttpResponse response=this.httpclient.execute(method);
        Source htmlparser = new Source(response.getEntity().getContent());
        List<StartTag> elems=htmlparser.getAllStartTags("td");
        List<String> urls=new LinkedList<String>();
        for(StartTag elem:elems){
                if(!elem.getElement().getChildElements().isEmpty())  {
                    System.out.println(urlprefix+elem.getElement().getChildElements().get(0).getAttributeValue("href"));
                    urls.add(urlprefix+elem.getElement().getChildElements().get(0).getAttributeValue("href"));
                }
        }
        System.out.println(urls.size());
        for(String url:urls){
            htmlparser=new Source(this.httpclient.execute(new HttpGet(url)).getEntity().getContent());
            StartTag tag=htmlparser.getFirstStartTag("font");
            StartTag tag2=htmlparser.getFirstStartTag("img");
            System.out.println(tag.getElement().getContent().getTextExtractor().toString());
            System.out.println(dict.matchWord(tag.getElement().getContent().getTextExtractor().toString()));
            if(tag2!=null){
                System.out.println(tag2.getElement().getAttributeValue("src").substring(tag2.getElement().getAttributeValue("src").indexOf("=")+1));
            }
        }
        System.out.println();

    }

    public static Tablet crawlCDLIForTabletMetadata(String tabletID) throws IOException{
        Tablet tablet=new Tablet();
        Integer charCount=200000,maxlength=6;
        int numDigits= (int)(Math.log10(charCount)+1);
        String tabID="";
        for(int j=numDigits;j<maxlength;j++){
            tabID+="0";
        }
        tabID+=charCount.toString();
        charCount++;
            System.out.println("http://cdli.ucla.edu/search/search_results.php?SearchMode=Text&ObjectID=" + tabletID);
            HttpGet method = new HttpGet("http://cdli.ucla.edu/search/search_results.php?SearchMode=Text&ObjectID=" +URLEncoder.encode(tabletID));
            HttpResponse response = httpclient.execute(method);
            if (response.getStatusLine().getStatusCode() == 404) {
                System.out.println("StatusCode 404");
                return tablet;
            }
            Source htmlparser = new Source(response.getEntity().getContent());
            List<StartTag> paras = htmlparser.getAllStartTags("tr");
            //if (paras.size() <= 4) {
            //    return tablet;
            //}
            tablet.tabletID = "P" + tabID;
            for (int i = 4; i < paras.size(); i++) {
                String tag = paras.get(i).getElement().getChildElements().get(0).getTextExtractor().toString();
                String value = paras.get(i).getElement().getChildElements().get(1).getTextExtractor().toString();
                System.out.println("Tag: " + tag);
                System.out.println("Value: " + value);
                if (tag.equals("Period") && value.contains("(")) {
                    try {
                        Epoch epoch = new Epoch(value.substring(0, value.indexOf('(')), "", Integer.valueOf(value.substring(value.indexOf('(') + 2, value.lastIndexOf('-')).replaceAll("[A-z]", "").replace(".","").trim()), Integer.valueOf(value.substring(value.indexOf('-') + 1, value.lastIndexOf(')')).replaceAll("[A-z]", "").replace(".","").trim()), "");
                        tablet.epochs.add(epoch);
                    }catch(Exception e){
                        e.printStackTrace();
                        tablet.epochs.add(new Epoch(value.substring(0, value.indexOf('(')), ""));
                    }

                }else if (tag.equals("Genre")) {
                    tablet.genre = value;
                } else if (tag.equals("Collection")) {
                    tablet.collection = value;
                } else if (tag.equals("Language")) {
                    tablet.langStr=value;
                    /*try {
                        tablet.chartype = CharTypes.valueOf(value);
                    }catch(IllegalArgumentException e){
                        e.printStackTrace();
                    }*/
                } else if (tag.equals("Object type")) {
                    tablet.objectType = value;
                } else if (tag.equals("Provenience")) {
                    tablet.place = value;
                }
            }
        return tablet;
    }

    public static Point getPointFromWikidata(String label){
        WikiDataConnection connection=new WikiDataConnection();
        Set<String> res=connection.getPointForLabel(label);
        String pointstr=null;
        if(res.iterator().hasNext()){
            pointstr=res.iterator().next();
            pointstr=pointstr.substring(0,pointstr.indexOf("^^")).replace("(","").replace(")","").replaceAll("[A-z]","");
            GeometryFactory fac=new GeometryFactory();
            try {
                Point point = fac.createPoint(new Coordinate(Double.valueOf(pointstr.split(" ")[0]), Double.valueOf(pointstr.split(" ")[1])));
                System.out.println("Point:"+point.toString());
                return point;
            }catch(NumberFormatException e){
                return null;
            }

        }else{
            return null;
        }

    }

    public void crawlCDLIForTabletMetadata(final OntModel model,String tabletID) throws IOException {
        this.crawlCDLIForTabletMetadata(model,Arrays.asList(new String[]{tabletID}));
    }
    public void crawlCDLIForTabletMetadata(final OntModel model,List<String> tabletIDs) throws IOException {
        OntClass tabletClass=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Tablet");
        OntClass epochClass=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Epoch");
        OntClass genreClass=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Genre");
        OntClass languageClass=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Language");
        OntClass placeClass=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Place");
        OntClass excavationObjectClass=model.createClass("http://acoli.uni-frankfurt.de/contology/cuneiform#ExcavationObject");
        OntClass collectionClass=model.createClass("http://acoli.uni-frankfurt.de/ontology/cuneiform#Collection");
        Boolean continueIt=true;
        Integer charCount=200000,maxlength=6;
        while(continueIt && charCount<220000){
            int numDigits= (int)(Math.log10(charCount)+1);
            String tabID="";
            for(int j=numDigits;j<maxlength;j++){
                tabID+="0";
            }
            tabID+=charCount.toString();
            charCount++;try {
                System.out.println("http://cdli.ucla.edu/search/search_results.php?SearchMode=Text&ObjectID=" + tabID);
                HttpGet method = new HttpGet("http://cdli.ucla.edu/search/search_results.php?SearchMode=Text&ObjectID=" + tabID);
                HttpResponse response = this.httpclient.execute(method);
                if(response.getStatusLine().getStatusCode()==404){
                    continueIt=false;
                }
                Source htmlparser = new Source(response.getEntity().getContent());
                List<StartTag> paras=htmlparser.getAllStartTags("tr");
                if(paras.size()<=4)
                    continue;
                Individual tabInd=tabletClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#P"+tabID);
                for(int i=4;i<paras.size();i++) {
                    String tag = paras.get(i).getElement().getChildElements().get(0).getTextExtractor().toString();
                    String value = paras.get(i).getElement().getChildElements().get(1).getTextExtractor().toString();
                    System.out.println("Tag: " + tag);
                    System.out.println("Value: " + value);
                    if (tag.equals("Period") && value.contains("(")) {
                        Individual epochInd = epochClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#" + URLEncoder.encode(value.substring(0, value.indexOf('('))));
                        epochInd.addLabel(value.substring(0, value.indexOf('(')), "en");
                        if (value.contains("-")) {
                            epochInd.addProperty(model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#period"), value.substring(value.indexOf('(') + 1, value.lastIndexOf(')')));
                            epochInd.addProperty(model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#epochBegin"), value.substring(value.indexOf('(') + 2, value.lastIndexOf('-')).replaceAll("[A-z]\\.", "").trim());
                            epochInd.addProperty(model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#epochEnd"), value.substring(value.indexOf('-') + 1, value.lastIndexOf(')')).replaceAll("[A-z]\\.", ""));
                        }
                        tabInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#hasEpoch"), epochInd);
                        epochInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#hasObject"),tabInd);
                    } else if (tag.equals("Genre")) {
                        Individual genreInd = genreClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#" + URLEncoder.encode(value));
                        genreInd.addLabel(value, "en");
                        //TODO Integrate Subgenre as Property of Genre
                        tabInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#hasGenre"), genreInd);
                        genreInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#hasObject"),tabInd);
                    } else if (tag.equals("Collection")) {
                        Individual genreInd = collectionClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#" + URLEncoder.encode(value));
                        genreInd.addLabel(value, "en");
                        tabInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#inCollection"), genreInd);
                        genreInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#hasObject"),tabInd);
                    } else if (tag.equals("Language")) {
                        Individual genreInd = languageClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#" + URLEncoder.encode(value));
                        genreInd.addLabel(value, "en");
                        tabInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#hasLanguage"), genreInd);
                        genreInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#inObject"),tabInd);
                    }else if (tag.equals("Object type")) {
                        Individual genreInd = excavationObjectClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#" + URLEncoder.encode(value));
                        genreInd.addLabel(value, "en");
                        tabInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#hasExcavationObject"), genreInd);
                    } else if (tag.equals("Provenience")) {
                        Individual genreInd = placeClass.createIndividual("http://acoli.uni-frankfurt.de/ontology/cuneiform#" + URLEncoder.encode(value));
                        //Set<String> res=DBPediaConnection.getInstance().matchPropertiesByLabel(value);
                        //System.out.println("=========================FOUND Place: "+res.toString());
                        genreInd.addLabel(value, "en");
                        tabInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#hasPlace"), genreInd);
                        genreInd.addProperty(model.createObjectProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#fromPlace"),tabInd);
                    }else if(!value.isEmpty()){
                        tabInd.addProperty(model.createDatatypeProperty("http://acoli.uni-frankfurt.de/ontology/cuneiform#" + URLEncoder.encode(paras.get(i).getElement().getChildElements().get(0).getTextExtractor().toString())), model.createTypedLiteral(paras.get(i).getElement().getChildElements().get(1).getTextExtractor().toString(), XSD.xstring.toString()));
                    }
                }
            }catch(Exception e){
                    continue;
                }




        }
        model.write(new FileWriter(new File("cdli.owl")),"TTL");
    }

    public void crawlETCSL(final DictHandling dict) throws IOException {
        HttpGet method = new HttpGet("http://etcsl.orinst.ox.ac.uk/edition2/etcslbynumb.php");
        HttpResponse response=this.httpclient.execute(method);
        Source htmlparser = new Source(response.getEntity().getContent());
        List<StartTag> paras=htmlparser.getAllStartTags("tr");
    }

    public void crawlHitDict(final DictHandling dict) throws IOException {
        HttpGet method = new HttpGet("http://www.hethport.uni-wuerzburg.de/txhet_svh/textindex.php?g=svh&x=x");
        HttpResponse response=this.httpclient.execute(method);
        Source htmlparser = new Source(response.getEntity().getContent());
        List<StartTag> paras=htmlparser.getAllStartTags("tr");
        List<HitTabletDescription> tabletList=new LinkedList<>();
        POSTagger postagger= CharTypes.HITTITE.getCorpusHandlerAPI().getPOSTagger(false);
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        output.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        OutputStreamWriter outwriter=new OutputStreamWriter(new FileOutputStream("newwordshittite2.xml",false), Tags.UTF8.toString());
        Boolean first=true;
        for(StartTag par:paras){
            if(first){
                first=false;
                continue;
            }
            int i=0;
            //for(Element elem:par.getElement().getChildElements()){
                Element elem1=par.getElement().getChildElements().get(i++);
                String cth=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String title=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String editor=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String link="https://www.hethport.uni-wuerzburg.de/txhet_svh/"+elem1.getChildElements().iterator().next().getChildElements().iterator().next().getAttributeValue("href").replace(" ","%20");
                tabletList.add(new HitTabletDescription(cth,title,editor,link));
            //}
        }
        method = new HttpGet("http://www.hethport.uni-wuerzburg.de/txhet_myth/textindex.php?g=myth&x=x&n=");
        response=this.httpclient.execute(method);
        htmlparser = new Source(response.getEntity().getContent());
        paras=htmlparser.getAllStartTags("tr");
        for(StartTag par:paras){
            if(first){
                first=false;
                continue;
            }
            int i=0;
            /*for(Element elem:par.getElement().getChildElements()){
                Element elem1=par.getElement().getChildElements().get(i++);
                String cth=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String title=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String editor=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String link=elem1.getChildElements().iterator().next().getChildElements().iterator().next().getAttributeValue("href");
                tabletList.add(new HitTabletDescription(cth,title,editor,link));

            }*/
        }
        method = new HttpGet("http://www.hethport.uni-wuerzburg.de/txhet_ke/textindex.php?g=ke&x=x");
        response=this.httpclient.execute(method);
        htmlparser = new Source(response.getEntity().getContent());
        paras=htmlparser.getAllStartTags("tr");
        for(StartTag par:paras){
            if(first){
                first=false;
                continue;
            }
            int i=0;
            /*for(Element elem:par.getElement().getChildElements()){
                Element elem1=par.getElement().getChildElements().get(i++);
                String cth=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String title=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String editor=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String link=elem1.getChildElements().iterator().next().getChildElements().iterator().next().getAttributeValue("href");
                tabletList.add(new HitTabletDescription(cth,title,editor,link));

            }*/
        }
        method = new HttpGet("http://www.hethport.uni-wuerzburg.de/txhet_besrit/textindex.php?g=besrit&x=x");
        response=this.httpclient.execute(method);
        htmlparser = new Source(response.getEntity().getContent());
        paras=htmlparser.getAllStartTags("tr");
        for(StartTag par:paras){
            if(first){
                first=false;
                continue;
            }
            int i=0;
            /*for(Element elem:par.getElement().getChildElements()){
                Element elem1=par.getElement().getChildElements().get(i++);
                String cth=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String title=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String editor=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                elem1=par.getElement().getChildElements().get(i++);
                String link=elem1.getChildElements().iterator().next().getChildElements().iterator().next().getAttributeValue("href");
                tabletList.add(new HitTabletDescription(cth,title,editor,link));

            }*/
        }
        System.out.println(tabletList);
        for(HitTabletDescription tab:tabletList){
            method = new HttpGet(tab.url);
            response=this.httpclient.execute(method);
            htmlparser = new Source(response.getEntity().getContent());
            paras=htmlparser.getAllStartTags("a");
            List<String> tabletlinks=new LinkedList<String>();
            for(StartTag href:paras){
                if(href.getElement().getAttributeValue("href").contains("exemplar")){
                    tabletlinks.add("https://www.hethport.uni-wuerzburg.de/txhet_svh/"+href.getElement().getAttributeValue("href").replace(" ","%20"));
                }
            }
            System.out.println("TabletLinks: "+tabletlinks);
            for(String link:tabletlinks){
                method = new HttpGet(link);
                response=this.httpclient.execute(method);
                htmlparser = new Source(response.getEntity().getContent());
                paras=htmlparser.getAllStartTags("tr");
                first=true;
                for(Iterator<StartTag> sttag=paras.iterator();sttag.hasNext();){
                    if(first){
                        first=false;
                        sttag.next();
                        sttag.next();
                        sttag.next();
                        continue;
                    }
                    StartTag par=sttag.next();
                    int i=1;
                    Element elem1=par.getElement().getChildElements().get(i++);
                    String cth=elem1.getTextExtractor().toString();
                    System.out.println(cth);
                    if(par.getElement().getChildElements().size()>i) {
                        elem1 = par.getElement().getChildElements().get(i++);
                        List<Element> words = elem1.getChildElements().iterator().next().getChildElements();
                        for (Element word : words) {
                            if (!word.getTextExtractor().toString().matches("^[0-9]$")) {
                                System.out.println("Word: " + word.getTextExtractor().toString());
                                postagger.getPosTag(word.getTextExtractor().toString(), dict);

                                for (Element postag : word.getChildElements()) {
                                    if (postag.getAttributeValue("class") != null && !postag.getAttributeValue("class").equals("steif"))
                                        System.out.println("POSTAG: " + postag.getAttributeValue("class") + " - " + postag.getTextExtractor().toString());
                                }
                            }
                            //System.out.println(.iterator().next().getAttributeValue("class"));
                        }
                    }
                    //System.out.println(title);
                    /*elem1=par.getElement().getChildElements().get(i++);
                    String editor=elem1.getChildElements().iterator().next().getTextExtractor().toString();
                    System.out.println(editor);
                    elem1=par.getElement().getChildElements().get(i++);*/
                    //String link=elem1.getChildElements().iterator().next().getChildElements().iterator().next().getAttributeValue("href");
                    //tabletList.add(new HitTabletDescription(cth,title,editor,link));
                }

            }
        }
    }


    public void getAkkSumRelationsFromAssDict(final String urlstring) throws IOException {
        Map<String,Map<String,Tuple<String,String>>> sumToAkk=new TreeMap<>();
        char c='A';
        Source htmlparser;
        SyllableSeparator syllsep=new SyllableSeparator();
        while(c<'Z'+1){
            HttpGet method = new HttpGet(sumeriantoc+c+++".html");
            System.out.println(sumeriantoc+c+".html");
            HttpResponse response=this.httpclient.execute(method);
            htmlparser = new Source(response.getEntity().getContent());
            List<StartTag> elems=htmlparser.getAllStartTags("span");
            for(StartTag elem:elems){
                if(elem.getElement().getAttributes().getValue("class")!=null &&
                        elem.getElement().getAttributes().getValue("class").equals("summary")){
                    System.out.println(elem.getElement().getTextExtractor().toString());
                    Source htmlparser2 = new Source(elem.getElement());
                    List<StartTag> elemlist=htmlparser2.getAllStartTags("span");
                    String sumerian="",akkadian="";
                    for(StartTag elem2:elemlist){
                        if(elem2.getElement().getAttributes().getValue("class")!=null &&
                                elem2.getElement().getAttributes().getValue("class").equals("wr")){

                            sumerian=elem2.getElement().getTextExtractor().toString();
                            System.out.println(sumerian.replace("mušen","-mušen"));
                            if(!sumToAkk.containsKey(sumerian.replace("mušen","-mušen"))) {
                                sumToAkk.put(sumerian.replace("mušen","-mušen"), new TreeMap<>());
                            }
                        }
                        if(elem2.getElement().getAttributes().getValue("class")!=null &&
                                elem2.getElement().getAttributes().getValue("class").equals("akk")){
                            akkadian=elem2.getElement().getTextExtractor().toString();
                            akkadian=syllsep.separateAkkadian(akkadian,"-").replace("--","-");
                            System.out.println(akkadian);
                            sumToAkk.get(sumerian.replace("mušen","-mušen")).put(akkadian,new Tuple<String,String>("",""));
                        }
                    }
                    System.out.println(sumerian.replace("mušen","-mušen"));
                }
            }
        }
        BufferedReader reader=new BufferedReader(new FileReader(new File("logogramme.txt")));
        String line;

        while((line=reader.readLine())!=null){
            if(line.contains("\\")) {
                String[] linespl = line.split("\\\\");
                if (!sumToAkk.containsKey(linespl[0])) {
                    sumToAkk.put(linespl[0], new TreeMap<>());
                }
                String akk=syllsep.separateAkkadian(linespl[1],"-").replace("--","-");
                if(linespl.length>3) {
                    sumToAkk.get(linespl[0]).put(akk,new Tuple<String,String>(linespl[2],linespl[3]));
                }else if(linespl.length>2){
                    sumToAkk.get(linespl[0]).put(akk,new Tuple<String,String>(linespl[2],""));
                }else{
                    sumToAkk.get(linespl[0]).put(akk,new Tuple<String,String>("",""));
                }

            }

        }
        FileWriter writer=new FileWriter(new File("sumToAkkRel.xml"));
        writer.write("<?xml version=\"1.0\"?><relations>"+System.lineSeparator());
        for(String sumerian:sumToAkk.keySet()){
            System.out.println(sumerian);
            for(String akk:sumToAkk.get(sumerian).keySet()){
                writer.write("<relation sum=\""+sumerian.replace("\uE0AA","Š").replace("mušen","-mušen")+"\" sumcun=\""+syllsep.cuneify(sumerian.replace("\uE0AA","Š").replace("mušen","-mušen"))+"\" akk=\""+akk+"\" akkcun=\""+syllsep.cuneify(akk)+"\" destvalue=\""+sumToAkk.get(sumerian).get(akk).getOne()+"\" ref=\""+sumToAkk.get(sumerian).get(akk).getTwo()+"\"/>"+System.lineSeparator());
            }
        }
        writer.write("</relations>");
        writer.close();
    }

    /**
     * Starts the crawling method for the given url.
     * @param dict the dicthandler to fill
     * @param urlstring the String of the url
     * @throws IOException on error
     */
    public void crawlAssDict(final DictHandling dict, final String urlstring) throws IOException {
        char c='A';
        List<String> urls=new LinkedList<String>();
        Map<String,String> meaningToURI=new TreeMap<>();
        Source htmlparser;
        while(c<'Z'+1){
            HttpGet method = new HttpGet(sumeriantoc+c+++".html");
            HttpResponse response=this.httpclient.execute(method);
            htmlparser = new Source(response.getEntity().getContent());
            List<StartTag> elems=htmlparser.getAllStartTags("a");
            for(StartTag elem:elems){
                if(!elem.getElement().getChildElements().isEmpty())  {
                    System.out.println(elem.getAttributeValue("href"));
                    if(elem.getAttributeValue("href").contains("\'")){
                        System.out.println(urlprefix3+elem.getAttributeValue("href").substring(elem.getAttributeValue("href").indexOf("\'")+1,elem.getAttributeValue("href").lastIndexOf("\'")));
                        urls.add(urlprefix3+elem.getAttributeValue("href").substring(elem.getAttributeValue("href").indexOf("\'")+1,elem.getAttributeValue("href").lastIndexOf("\'")));

                    }
                }
            }
        }
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        output.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        OutputStreamWriter outwriter=new OutputStreamWriter(new FileOutputStream("newwordsumerian___.xml",false), Tags.UTF8.toString());
        try{
            UnicodeXMLStreamWriter writer = new UnicodeXMLStreamWriter(outwriter,output.createXMLStreamWriter(outwriter));
            writer.writeStartDocument();
            writer.writeStartElement("translations");
            System.out.println(urls.size());
            int i=-1;
            for(String url:urls){
                i++;
                System.out.println("URL: "+url+"("+i+"/"+urls.size()+")");

                htmlparser=new Source(this.httpclient.execute(new HttpGet(url)).getEntity().getContent());
                List<StartTag> tag=htmlparser.getAllStartTags("h3");
                List<StartTag> tag2=htmlparser.getAllStartTags("p");
                List<StartTag> tag3=htmlparser.getAllStartTags("span");
                List<String> transliterations=new LinkedList<String>();
                Set<String> akkrefs=new HashSet<String>();
                List<String> translations=new LinkedList<String>();
                String transcription="";
                String meaning="";
                String temp=tag2.get(1).getElement().getTextExtractor().toString();
                String epochs="";
                if(temp!=null && !temp.isEmpty() && temp.contains("(") && temp.contains(")"))
                    epochs=temp.substring(temp.indexOf(':')+1,temp.indexOf(')')).replace(",",";");
                Boolean verb=false;
                for(StartTag tagg:tag3){
                    //System.out.println("Attributes: "+tagg.getAttributes().toString());
                    if(tagg.getAttributes().get("class")!=null && tagg.getAttributes().getValue("class").equals("orth1")){
                        transliterations.add(tagg.getElement().getTextExtractor().toString());
                    }
                    if(tagg.getAttributes().get("class")!=null && tagg.getAttributes().getValue("class").equals("akk")){
                        akkrefs.add(tagg.getElement().getTextExtractor().toString());
                    }
                    if(tagg.getAttributes().get("class")!=null && tagg.getAttributes().getValue("class").equals("cf")){
                        transcription=tagg.getElement().getTextExtractor().toString();
                    }

                }
                for(StartTag tagg:tag2) {
                    if(tagg.getAttributes().get("class")!=null && tagg.getAttributes().getValue("class").equals("cpd")){
                        meaning=tagg.getElement().getTextExtractor().toString();
                    }
                }
                for(StartTag tagg:tag) {
                    if(tagg.getAttributes().get("class")!=null && tagg.getAttributes().getValue("class").equals("sense")){
                        String addtrans=tagg.getElement().getTextExtractor().toString().replace("(","").replace(")","").replace(".","").replace("?","");//.replaceAll("[0-9]","").replace("x/%","");
                        translations.add(addtrans.contains(",")?addtrans.substring(0,addtrans.indexOf(',')):addtrans);
                        if(tagg.getElement().getTextExtractor().toString().contains("to ")){
                            verb=true;
                        }
                    }
                }
                System.out.println("Transliterations: "+transliterations);
                System.out.println("Translations: "+translations);
                int counter=0;
                SyllableSeparator syllableSeparator=new SyllableSeparator();
                for(String trans:transliterations){
                    writer.writeStartElement("translation");
                    List<String> translats=Arrays.asList(translations.get(counter).split(" "));
                    //System.out.println(translats);
                    String translation="";
                    for(String str:translats){
                        if(str.contains("x/")){
                            writer.writeAttribute("occ",str);
                        }else{
                            translation+=str+" ";
                        }
                    }
                    translation=translation.replaceAll("[0-9]","").trim();
                    writer.writeAttribute("transcription",transcription);
                    if(!meaning.isEmpty()){
                        writer.writeAttribute("meaning",meaning);
                    }
                    if(!epochs.isEmpty()){
                        writer.writeAttribute("epoch",epochs.trim());
                    }
                    /*writer.writeAttribute("determinative","false");
                    writer.writeAttribute("logograph","true");
                    writer.writeAttribute("phonogram","false");
                    writer.writeAttribute("absoluteOccurance","1.000000");
                    writer.writeAttribute("relativeOccurance","0.000018");
                    writer.writeAttribute("begin","0.0");
                    writer.writeAttribute("middle","0.0");
                    writer.writeAttribute("end","0.0");
                    writer.writeAttribute("single","0.0");
                    writer.writeAttribute("stem","");*/
                    //writer.writeAttribute("meaning",tag2.get(0).getElement().getChildElements().get(1).getTextExtractor().toString().replace("[","").replace("]","").replace("?","").replace("~","").toLowerCase());
                   // String translation=tag2.get(0).getElement().getChildElements().get(1).getTextExtractor().toString().replace("[","").replace("]","").replace("?","").replace("~","").toLowerCase().trim();
                    /*if(meaningToURI.containsKey(translation)){
                        if(meaningToURI.get(translation)!=null)
                            writer.writeAttribute("concept",meaningToURI.get(translation));
                    }else {
                        String concept = ConceptMatcher.getInstance().resolveConcept(translation);
                        if (concept != null && !concept.isEmpty()) {
                            if(concept.startsWith("#"))
                                concept= "http://dbpedia.org/resource/"+concept.substring(1);
                            writer.writeAttribute("concept", concept);
                            meaningToURI.put(translation, concept);
                        } else {
                            meaningToURI.put(translation, null);
                        }
                    }*/
                    writer.flush();
                    //outwriter.write("\n");
                    if(akkrefs!=null && akkrefs.size()>0){
                        StringBuilder builder=new StringBuilder();
                        for(String akkref:akkrefs){
                            builder.append(syllableSeparator.separateAkkadian(akkref,"-")+";");
                        }
                        writer.writeAttribute("logogram",builder.toString());
                    }
                    String origvalue=
                            trans.replace("?","").replaceAll("\\([A-z0-9š]+\\)","").replaceAll("([0-9])([a-zĝš])","$1-$2").replaceAll("([bcdfgĝhjklmnpqrsštvwxz])([bcdfgĝhjklmnpqrsštvwxz])","$1-$2");
                    writer.writeAttribute("origvalue",origvalue);
                    //writer.writeAttribute("transcription",trans.replace("-",""));
                    //writer.writeCharacters(trans.replace("?","").replaceAll("\\([A-z0-9š]+\\)","").replaceAll("([0-9])([a-zĝš])","$1-$2").replaceAll("([bcdfgĝhjklmnpqrsštvwxz])([bcdfgĝhjklmnpqrsštvwxz])","$1-$2"));
                    //writer.writeEndElement();
                    if(translations.size()>counter) {
                        writer.flush();
                        writer.writeAttribute("destvalue",translation);
                    }
                    if(verb){
                        writer.flush();
                        writer.writeAttribute("postag","VV");
                    }else{
                        writer.writeAttribute("postag","NN");
                    }
                    String reformatted=new CuneiImportHandler().reformatToASCIITranscription(origvalue).replace("y","i");
                    //System.out.println(separated+" - "+reformatted);
                    /*String towrite=syllableSeparator.cuneify(reformatted).replace(";", "").replace(",", "").replaceAll("[0-9]","");
                    outwriter.write(" cunei=\""+towrite+"\" ");*/
                    //writer.writeAttribute("cunei",syllableSeparator.cuneify(origvalue));
                    //new Methods().assignTransliteration(new String[]{trans},dict,T)
                    //writer.writeCharacters(dict.translitToWord(trans.replace("?","").replaceAll("\\([A-z0-9š]+\\)","").replaceAll("([0-9])([a-zĝš])","$1-$2").replaceAll("([bcdfgĝhjklmnpqrsštvwxz])([bcdfgĝhjklmnpqrsštvwxz])","$1-$2").replace("š","sz").replace("ĝ","g")).getCharacter());

                    writer.writeEndElement();
                    writer.flush();
                    outwriter.write(System.lineSeparator());
                }
               /* int tag3count=0;
                for(StartTag start:tag){
                    writer.writeStartElement("dictentry");
                    writer.writeAttribute("determinative","false");
                    writer.writeAttribute("logograph","true");
                    writer.writeAttribute("phonogram","false");
                    writer.writeAttribute("absoluteOccurance","1.000000");
                    writer.writeAttribute("relativeOccurance","0.000018");
                    writer.writeAttribute("begin","0.0");
                    writer.writeAttribute("middle","0.0");
                    writer.writeAttribute("end","0.0");
                    writer.writeAttribute("single","0.0");
                    writer.writeAttribute("stem","");
                    writer.writeAttribute("meaning",tag2.get(0).getElement().getChildElements().get(1).getTextExtractor().toString());
                    for()
                    while(tag3count<tag3.size()) {
                    writer.writeStartElement("translation");
                    writer.writeAttribute("locale","en");
                    writer.writeCharacters(start.getElement().getTextExtractor().toString().substring(start.getTextExtractor().toString().indexOf('.')+1));
                    writer.writeEndElement();

                    writer.writeAttribute("origlocale",Tags.AKKADIAN.toString());
                    writer.writeAttribute("destlocale",Locale.ENGLISH.toString());
                    System.out.println("Meaning: "+tag2.get(0).getElement().getChildElements().get(1).getTextExtractor().toString());
                    writer.writeAttribute("meaning",tag2.get(0).getElement().getChildElements().get(1).getTextExtractor().toString());
                    System.out.println("DestValue: "+start.getElement().getTextExtractor().toString().substring(start.getTextExtractor().toString().indexOf('.')+1));
                    writer.writeAttribute("destvalue",start.getElement().getTextExtractor().toString().substring(start.getTextExtractor().toString().indexOf('.')+1));

                        if(tag3.get(5+tag3count).getElement().getTextExtractor().toString().contains("1.")){
                            break;
                        }
                        writer.writeStartElement("value");
                        System.out.println("Origvalue: "+tag3.get(5+tag3count).getElement().getTextExtractor().toString());
                        writer.writeAttribute("origvalue",tag3.get(5+tag3count).getElement().getTextExtractor().toString());

                        tag3count++;
                        writer.writeEndElement();
                    }

                    writer.writeEndElement();
                }*/

                writer.flush();
                //outwriter.write("\n");
                i++;

            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.flush();
            writer.close();
            /*BufferedWriter fwriter=new BufferedWriter(new FileWriter(new File("newwords.xml")));
            fwriter.write(outwriter.toString());
            writer.close();*/
        } catch (FactoryConfigurationError | Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            dict.exportToXML("outexport/dictout.xml","outexport/revdict.xml","outexport/map.xml","outexport/ngram.xml",false);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        System.out.println("FINISHED");

    }

        /**
     * Starts the crawling method for the given url.
     * @param dict the dicthandler to fill
     * @param urlstring the String of the url
     * @throws IOException on error
     */
    public void crawlUniLP(final DictHandling dict, final String urlstring) throws IOException {
        HttpGet method = new HttpGet("http://www.assyrianlanguages.org/akkadian/list.php");
        HttpResponse response=this.httpclient.execute(method);
        Source htmlparser = new Source(response.getEntity().getContent());
        List<StartTag> elems=htmlparser.getFirstElement("table").getAllStartTags("td");
        List<String> urls=new LinkedList<String>();
        for(StartTag elem:elems){
            if(!elem.getElement().getChildElements().isEmpty())  {
                System.out.println(urlprefix2+elem.getElement().getChildElements().get(0).getAttributeValue("href"));
                urls.add(urlprefix2+elem.getElement().getChildElements().get(0).getAttributeValue("href"));
            }
        }
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        output.setProperty(Tags.ESCAPECHARACTERS.toString(), true);
        OutputStreamWriter outwriter=new OutputStreamWriter(new FileOutputStream("newwords___.xml",false), Tags.UTF8.toString());
        try{
        XMLStreamWriter writer = new IndentingXMLStreamWriter(output.createXMLStreamWriter(outwriter));
            writer.writeStartDocument();
            writer.writeStartElement("translations");
        System.out.println(urls.size());
            int i=0;
        for(String url:urls){
            if(i>-2){
                //i+=5;
                System.out.println(i);
                //continue;
            }
            writer.writeStartElement(Tags.TRANSLATION);
            System.out.println("URL: "+url);

            htmlparser=new Source(this.httpclient.execute(new HttpGet(url)).getEntity().getContent());
            StartTag tag=htmlparser.getFirstStartTag("font");
            String imgtitle="";
            if(htmlparser.getAllStartTags("img")!=null && htmlparser.getAllStartTags("img").size()>0){
                imgtitle=htmlparser.getAllStartTags("img").get(0).getAttributeValue("title");
            }
            String otherfeatures=htmlparser.getAllStartTags("p").get(0).getElement().getTextExtractor().toString();
            System.out.println("Other features: "+otherfeatures);
            StartTag tag2=htmlparser.getAllStartTags("p").get(2);
            //System.out.println(tag.getElement().getContent().getTextExtractor().toString());
            //System.out.println(dict.matchWord(tag.getElement().getContent().getTextExtractor().toString()));
            if(tag2!=null){
                writer.writeAttribute("origlocale",Tags.AKKADIAN.toString());
                writer.writeAttribute("destlocale",Locale.ENGLISH.toString());
                writer.writeAttribute("origvalue",tag.getElement().getContent().getTextExtractor().toString());
                writer.writeAttribute("destvalue",tag2.getElement().getContent().getTextExtractor().toString());
                if(otherfeatures.contains("pl. ")){
                    writer.writeAttribute("number","Plural");
                }
                if(otherfeatures.contains("du. ")){
                    writer.writeAttribute("number","Dual");
                }
                if(otherfeatures.contains("f. ") || otherfeatures.contains("fem. ")){
                    writer.writeAttribute("gender","Female");
                }
                if(otherfeatures.contains("n. ")){
                    writer.writeAttribute("pos","NN");
                }
                if(otherfeatures.contains("vb. ") || otherfeatures.contains("v.i. ") || otherfeatures.contains("v.t. ")){
                    writer.writeAttribute("pos","VV");
                }
                if(otherfeatures.contains("adj. ")){
                    writer.writeAttribute("pos","ADJ");
                }
                if(imgtitle!=null && !"".equals(imgtitle)){
                    String epochs="",dialects="";
                    for(String spl:imgtitle.split(",")){
                        if("Nuzi".equals(spl) || "Mari".equals(spl) || "Ug".equals(spl)){
                            dialects+=spl+";";
                        }else{
                            epochs+=spl+";";
                        }
                    }
                    if(imgtitle.contains("A,") || imgtitle.endsWith("A")){
                        dialects+="Assyrian;";
                    }
                    if(imgtitle.contains("B,") || imgtitle.endsWith("B")){
                        dialects+="Babylonian;";
                    }
                    writer.writeAttribute("epoch",epochs.length()>0?epochs.substring(0,epochs.length()-1):"");
                    writer.writeAttribute("dialect",dialects.length()>0?dialects.substring(0,dialects.length()-1):"");
                }
                System.out.println(tag.getElement().getContent().getTextExtractor().toString()+" - "+tag2.getElement().getTextExtractor().toString());
            }
            writer.writeEndElement();
            i++;

        }

        writer.writeEndDocument();
            writer.close();
            /*BufferedWriter fwriter=new BufferedWriter(new FileWriter(new File("newwords.xml")));
            fwriter.write(outwriter.toString());
            writer.close();*/
        } catch (FactoryConfigurationError | Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("FINISHED");

    }
/**
     * Starts the crawling method for the given url.
     * @param dict the dicthandler to fill
     * @param urlstring the String of the url
     * @throws IOException on error
     */
   /* public void crawlUniLP(final DictHandling dict, final String urlstring) throws IOException {
        HttpGet method = new HttpGet("http://www.assyrianlanguages.org/akkadian/list.php");
        HttpResponse response=this.httpclient.execute(method);
        Source htmlparser = new Source(response.getEntity().getContent());
        List<StartTag> elems=htmlparser.getFirstElement("table").getAllStartTags("td");
        List<String> urls=new LinkedList<String>();
        for(StartTag elem:elems){
            if(!elem.getElement().getChildElements().isEmpty())  {
                System.out.println(urlprefix2+elem.getElement().getChildElements().get(0).getAttributeValue("href"));
                urls.add(urlprefix2+elem.getElement().getChildElements().get(0).getAttributeValue("href"));
            }
        }
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        output.setProperty(Tags.ESCAPECHARACTERS.toString(), false);
        OutputStreamWriter outwriter=new OutputStreamWriter(new FileOutputStream("newwords.xml",true), Tags.UTF8.toString());
        try{
        XMLStreamWriter writer = output.createXMLStreamWriter(outwriter);
            writer.writeStartDocument();
            writer.writeStartElement("translations");
        System.out.println(urls.size());
            int i=0;
        for(String url:urls){
            if(i<8588){
                i++;
                continue;
            }
            writer.writeStartElement(Tags.TRANSLATION);
            System.out.println("URL: "+url);

            htmlparser=new Source(this.httpclient.execute(new HttpGet(url)).getEntity().getContent());
            StartTag tag=htmlparser.getFirstStartTag("font");
            StartTag tag2=htmlparser.getAllStartTags("p").get(2);
            //System.out.println(tag.getElement().getContent().getTextExtractor().toString());
            //System.out.println(dict.matchWord(tag.getElement().getContent().getTextExtractor().toString()));
            if(tag2!=null){
                writer.writeAttribute("origlocale",Tags.AKKADIAN.toString());
                writer.writeAttribute("destlocale",Locale.ENGLISH.toString());
                writer.writeAttribute("origvalue",tag.getElement().getContent().getTextExtractor().toString());
                writer.writeAttribute("destvalue",tag2.getElement().getContent().getTextExtractor().toString());

                System.out.println(tag.getElement().getContent().getTextExtractor().toString()+" - "+tag2.getElement().getTextExtractor().toString());
            }
            writer.writeEndElement();
            writer.flush();
            outwriter.write("\n");
            i++;

        }

        writer.writeEndDocument();
            writer.flush();
            /*BufferedWriter fwriter=new BufferedWriter(new FileWriter(new File("newwords.xml")));
            fwriter.write(outwriter.toString());
            writer.close();*
        } catch (FactoryConfigurationError | Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.out.println("FINISHED");

    } */
}

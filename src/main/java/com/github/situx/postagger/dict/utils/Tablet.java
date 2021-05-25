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

package com.github.situx.postagger.dict.utils;

import com.kennycason.kumo.WordFrequency;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.github.situx.postagger.dict.DictWebCrawler;
import com.github.situx.postagger.util.enums.methods.CharTypes;

import java.util.*;

/**
 * Created by timo on 10.06.17 .
 */
public class Tablet implements Comparable<Tablet> {

    public List<Epoch> epochs=new LinkedList<>();

    public CharTypes chartype;

    public String place;

    public String langStr;

    public String genre;

    public String tabletID;

    public String museumID;

    public String collection;

    public String objectType;

    public Integer tabletCount=0;

    public Double matchedwords=0.;

    public Double totalWords=0.;

    public Point point;

    public java.util.Map<String,Double> keyWordToOccurance=new TreeMap<>();

    public java.util.Map<String,Double> conceptToOccurance=new TreeMap<>();

    @Override
    public String toString() {
        return "Tablet{" +
                "epochs=" + epochs +
                ", chartype=" + chartype +
                ", place='" + place + '\'' +
                ", point='" + point + '\'' +
                ", genre='" + genre + '\'' +
                ", langStr='" + langStr + '\'' +
                ", tabletID='" + tabletID + '\'' +
                ", museumID='" + museumID + '\'' +
                ", collection='" + collection + '\'' +
                ", objectType='" + objectType + '\'' +
                '}';
    }

    @Override
    public int compareTo(Tablet o) {
        return tabletID.compareTo(o.tabletID);
    }

    public void getGeoLocation(){
        String modified=this.place;
        if(this.place==null)
            return;
        if(this.place.contains("mod.")){
            modified=modified.substring(0,modified.indexOf('(')).trim();
        }
        this.point=DictWebCrawler.getPointFromWikidata(modified);
        if(this.point==null){
            modified=modified.substring(0,modified.indexOf('.')+1).replace(")","").trim();
            this.point=DictWebCrawler.getPointFromWikidata(modified);
        }
        if(this.point==null){
            GeometryFactory fac=new GeometryFactory();
            this.point= fac.createPoint(new Coordinate(44.416667,33.35));
        }
    }

    public List<WordFrequency> getWordFrequencies(){
        List<WordFrequency> result=new LinkedList<>();
        for(String key:this.keyWordToOccurance.keySet()){
            if(key.contains("(") && key.contains(" - "))
                result.add(new WordFrequency(key.substring(key.indexOf("(")+1,key.indexOf(" - ")),this.keyWordToOccurance.get(key).intValue()));
        }
        return result;
    }


    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o1.getValue()).compareTo( o2.getValue() )*-1;
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }


    public String toGeoJSON(){
        if(point==null){
            return "";
        }else{
            String ret="{\"type\":\"Feature\"," +
                    "\"geometry\":{ \"type\":\"Point\",\"coordinates\":[" + point.getX() + "," + point.getY() + "]}, " +
                    "\"properties\":{\"tabletID\":\"" + tabletID + "\", \"tabletcount\":"+tabletCount+", \"language\":\""+langStr+"\",\"place\":\""+place.replace("?","_")+"\",\"matchpercent\":\""+(matchedwords/totalWords)*100+"%\",\"currentwords\":\""+totalWords+"\",\"matchedwords\":\""+matchedwords+"\", \"epoch\":\""+epochs.iterator().next().name+"\", " +
                    "\"keywords\":[";
            keyWordToOccurance=sortByValue(keyWordToOccurance);
            for(String keyword:keyWordToOccurance.keySet()){
                if(keyword.contains("http")){
                    System.out.println(keyword);
                    ret+="{ \"keyword\":\""+(keyword.contains("(")?keyword.substring(keyword.indexOf("(")+1):keyword)+"\", \"translation\":\""+(keyword.contains("-") && keyword.contains("(")?keyword.substring(keyword.indexOf("(")+1,keyword.lastIndexOf(" - ")):keyword.split("-")[0])+"\", \"uri\":\""+keyword.substring(keyword.lastIndexOf(" - ")+2,keyword.lastIndexOf(")")).trim()+"\", \"occ\":\""+keyWordToOccurance.get(keyword).intValue()+"\"},"+System.lineSeparator();
                }

            }
            if(!ret.endsWith("["))
                ret=ret.substring(0,ret.length()-1);
            return ret+"]}}";
        }

    }

    public static void main(String[] args){
        Tablet tablet=new Tablet();
        tablet.place="Lagash";
        tablet.getGeoLocation();
    }

    public String toXML(){
        if(this.point==null){
            this.getGeoLocation();
        }
        StringBuilder builder=new StringBuilder();
        builder.append("<tablet "+"id=\""+tabletID+"\" place=\""+place+"\" lang=\""+langStr+"\" genre=\""+genre+"\" objectType=\""+objectType+"\" collection=\""+collection+"\" museumID=\""+museumID+"\" point=\""+(point!=null?point.toText():"")+"\" epoch=\""+(epochs.isEmpty()?"":epochs.iterator().next().name.trim())+"\"/>");
        //builder.append(epochs.iterator().next().toXML()+System.lineSeparator());
        //builder.append("</tablet>");
        return builder.toString();
    }
}

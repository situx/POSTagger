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

package com.github.situx.postagger.dict.semdicthandler.conceptresolver.connector;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;


public class WikiDataConnection extends TripleStoreConnector {

	public WikiDataConnection(){
		this.queryurl="https://query.wikidata.org/sparql";
		this.queryurl2="https://query.wikidata.org/sparql";
		this.prefix=new TreeSet<String>(Arrays.asList(new String[]{"http://www.wikidata.org/ontology#"}));
		this.babelnetCompatible=false;
		this.geospatialComaptible=false;
		this.matchLabels=false;
		this.defaultRadius=0.0001;
	}
	
	public static TripleStoreConnector getInstance(){
		if(instance==null || !(instance instanceof DBPediaConnection)){
			instance=new WikiDataConnection();
		}
		return instance;
	}
	
	@Override
	public Set<String> getConceptsGeoGraphicallyNearTo(Double lat, Double lon, Double radius) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ClassResLabResult> getConceptsGeoGraphicallyNearTo(Double lat, Double lon, Double radius,
			Boolean dummy) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Set<String> getExactResourceOfType(String resourceString,String classs){
		String language="en";
		Set<String> result=new TreeSet<String>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append(" SELECT DISTINCT ?class ?classLabel WHERE { "+System.lineSeparator());
			classOfResourceQuery.append( "?class rdfs:label \""+resourceString+"\"@"+language+" . "
					+ "?class wdt:P31 ?super . "
					+ "SERVICE wikibase:label { bd:serviceParam wikibase:language \""+language+"\" . } }");
			System.out.println(classOfResourceQuery);
			/*classOfResourceQuery.append("ASK { "+System.lineSeparator());
			classOfResourceQuery.append("<"+pref+resourceString+">");
			classOfResourceQuery.append(" a ");
			classOfResourceQuery.append("<"+classs+"> .}");*/
			//System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				System.out.println("Class? "+solu.get("?class"));
				result.add(solu.get("?class").toString());
			}
			qexec.close();
		}
		return result;
	}
	
	@Override
	public Set<ClassResLabResult> getExactResourceOfType(String resourceString,String classs,String language){
		Set<ClassResLabResult> result=new TreeSet<ClassResLabResult>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append(" SELECT DISTINCT ?super ?superLabel ?class ?classLabel WHERE { "+System.lineSeparator());
			classOfResourceQuery.append( "?class rdfs:label \""+resourceString+"\"@"+language+" . "
					+ "?class wdt:P31 ?super . "
					+ "SERVICE wikibase:label { bd:serviceParam wikibase:language \""+language+"\" . } }");
			System.out.println(classOfResourceQuery);
			/*classOfResourceQuery.append("ASK { "+System.lineSeparator());
			classOfResourceQuery.append("<"+pref+resourceString+">");
			classOfResourceQuery.append(" a ");
			classOfResourceQuery.append("<"+classs+"> .}");*/
			//System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				System.out.println("Class? "+solu.get("?super")+" - Classlabel: "+solu.get("?superLabel")+" - Res: "+solu.get("?class")+" - Label: "+solu.get("?classLabel"));
				result.add(new ClassResLabResult(solu.get("?super").toString(),solu.get("?superLabel").toString(),solu.get("?class").toString(),solu.get("?classLabel").toString()));
			}
			qexec.close();
		}
		return result;
	}
	

	
	@Override
	public Set<String> getLabelForClass(String classname) {
		Set<String> result=new TreeSet<String>();
		//for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("SELECT DISTINCT ?labelLabel WHERE { "+System.lineSeparator());
			classOfResourceQuery.append("<"+classname+">");
			classOfResourceQuery.append(" rdfs:label ");
			classOfResourceQuery.append("?label.   SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" . } }");
			System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl2, query);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				//System.out.println("?labelLabel: "+solu.get("?labelLabel"));
				result.add(solu.get("?labelLabel").toString());
			}
			qexec.close();
		//}
		return result;
	}


	public Set<String> getPointForLabel(String label) {
		Set<String> result = new TreeSet<String>();
		//for(String pref:prefix){
		StringBuilder classOfResourceQuery = new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?point WHERE { " + System.lineSeparator());
		classOfResourceQuery.append("?ind");
		classOfResourceQuery.append(" rdfs:label ");
		classOfResourceQuery.append("\"" + label + "\"@en.   SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" . }" +
				"?ind wdt:P625 ?point ." +
				" }");
		System.out.println(classOfResourceQuery);
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl2, query);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			QuerySolution solu = results.next();
			//System.out.println("?labelLabel: "+solu.get("?labelLabel"));
			result.add(solu.get("?point").toString());
		}
		qexec.close();
		//}
		return result;
	}
	

	
	@Override
	public Boolean isSubClassOf(String classname,String classname2) {
		//Set<String> result=new TreeSet<String>();
		//for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("ASK { "+System.lineSeparator());
			classOfResourceQuery.append("<"+classname+">");
			classOfResourceQuery.append(" wdt:P279* ");
			classOfResourceQuery.append("<"+classname2+">.   SERVICE wikibase:label { bd:serviceParam wikibase:language \"en\" . } }");
			System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl2, query);
			Boolean results = qexec.execAsk();
			qexec.close();
			return results;
		//}
		//return result;
	}

	@Override
	public Set<String> getSuperConceptsHierarchy(String originConcept) {
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?superclass WHERE { "+System.lineSeparator());
		classOfResourceQuery.append("<"+originConcept+"> wdt:P271* ?superclass . }");
		System.out.println("Query: "+classOfResourceQuery.toString());
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			System.out.println("SuperClass? "+solu.get("?superclass"));
			result.add(solu.get("?superclass").toString());
		}
		qexec.close();
		return result;
	}
	
	
	//TODO Distance Calculations
	public Set<Integer> getPathDistance(String uri1,String uri2){
		Set<Integer> result=new TreeSet<Integer>();
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("select ?a ?b ?super (?aLength + ?bLength as ?length)) { "+System.lineSeparator());
		classOfResourceQuery.append("values (?a ?b) { (<"+uri1+"> <"+uri2+">) }");
		classOfResourceQuery.append("{"+System.lineSeparator());
		classOfResourceQuery.append("?a wdt:P279* ?mid .");
		classOfResourceQuery.append("?mid wdt:P279+ ?super . ");
		classOfResourceQuery.append("}"+System.lineSeparator());
		classOfResourceQuery.append("group by ?a ?super");
		classOfResourceQuery.append("limit 1");
		System.out.println("Query: "+classOfResourceQuery.toString());
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			System.out.println("Length? "+solu.get("?aLength"));
			result.add(Integer.valueOf(solu.get("?aLength").toString()));
		}
		qexec.close();
		return result;
	}
	
	@Override
	public Set<String> matchConceptsByLabel(String resourceString,String language){
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append(" SELECT DISTINCT ?class ?classLabel WHERE { "+System.lineSeparator());
		classOfResourceQuery.append( "?class rdfs:label \""+resourceString+"\"@"+language+" . "
				+ "?class wdt:P279 ?super . "
				+ "SERVICE wikibase:label { bd:serviceParam wikibase:language \""+language+"\" . } }");
				//+ "FILTER(regex(STR(?classLabel),\"^"+resourceString+"$\"))\n}");
		//System.out.println("Query: "+classOfResourceQuery.toString());
        //QueryExecution qexec = new com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP(URI.encode(this.queryurl), classOfResourceQuery.toString());
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		if(!results.hasNext()){
			classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append(" SELECT DISTINCT ?class ?classLabel WHERE { "+System.lineSeparator());
			classOfResourceQuery.append( "?class rdfs:label \""+resourceString+"\"@"+language+" . "
					+ "SERVICE wikibase:label { bd:serviceParam wikibase:language \""+language+"\" . } }");
					//+ "FILTER(regex(STR(?classLabel),\"^"+resourceString+"$\"))\n}");
			//System.out.println("Query: "+classOfResourceQuery.toString());
			query = QueryFactory.create(classOfResourceQuery.toString());
			qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			results = qexec.execSelect();
		}
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			//System.out.println("Class? "+solu.get("?class"));
			result.add(solu.get("?class").toString());
		}
		qexec.close();
		return result;
	}

	
	public static void main(String[] args) {
		WikiDataConnection connection=new WikiDataConnection();
		//System.out.println(connection.getPathDistance("http://www.wikidata.org/entity/Q2106349", "http://www.wikidata.org/entity/Q2106349"));
		System.out.println(connection.getPointForLabel("Lagash"));
		//System.out.println(connection.matchConceptsByLabel("Grundschule"));
	}
}

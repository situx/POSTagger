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

import java.util.*;


public class LinkedGeoDataConnection extends TripleStoreConnector {
	
	
	public LinkedGeoDataConnection(){
		this.queryurl="http://linkedgeodata.org/sparql";
		this.queryurl2="http://linkedgeodata.org/vsparql";
		this.prefix=new TreeSet<String>(Arrays.asList(new String[]{"http://linkedgeodata.org/ontology/"}));
		this.babelnetCompatible=false;
		this.geospatialComaptible=true;
		this.matchLabels=true;
		this.defaultRadius=0.000001;
	}
	
	public static TripleStoreConnector getInstance(){
		if(instance==null || !(instance instanceof LinkedGeoDataConnection)){
			instance=new LinkedGeoDataConnection();
		}
		return instance;
	}
	
	public static void main(String[] args){
		LinkedGeoDataConnection.getInstance().getConceptsGeoGraphicallyNearTo(7.0502212095794423, 50.939348493848669);
		//System.out.println(LinkedGeoDataConnection.getInstance().getClassesWithGeometries());
	}

	@Override
	public Set<String> getConceptsGeoGraphicallyNearTo(Double lat, Double lon, Double radius) {
		return null;
	}

	@Override
	public Set<ClassResLabResult> getConceptsGeoGraphicallyNearTo(Double lat, Double lon, Double radius, Boolean dummy) {
		return null;
	}

	/*@Override
	public Set<ClassResLabResult> getConceptsGeoGraphicallyNearTo(Double lat, Double lon,Double radius,Boolean dummy) {
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class ?label ?s WHERE { "+System.lineSeparator());
		classOfResourceQuery.append("?s rdf:type ?class ."+System.lineSeparator());
		classOfResourceQuery.append("?s rdfs:label ?label ."+System.lineSeparator());

		classOfResourceQuery.append("?s geom:geometry ?geom ."+System.lineSeparator());
		classOfResourceQuery.append("?geom ogc:asWKT ?g ."+System.lineSeparator());
		classOfResourceQuery.append("Filter(bif:st_intersects (?g, bif:st_point ("+lon+","+lat+"), "+radius+")) ");
		classOfResourceQuery.append("FILTER NOT EXISTS { ?x rdfs:subClassOf ?class. FILTER (?x != ?class) }}");
		System.out.println("Query: "+classOfResourceQuery.toString());
        QueryExecution qexec = new com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP(URI.encode(this.queryurl), classOfResourceQuery.toString());
		ResultSet results = qexec.execSelect();
		Set<ClassResLabResult> result=new TreeSet<ClassResLabResult>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			//System.out.println("Class? "+solu.get("?class"));
			if(solu.get("?class").toString().contains(this.prefix.iterator().next().toString())){
				result.add(new ClassResLabResult(solu.get("?class").toString(),"",solu.get("?s").toString(),solu.get("?label").toString()));
				System.out.println("Valid Class? "+solu.get("?class"));
				System.out.println("Label? "+solu.get("?label"));
				System.out.println("Resource: "+solu.get("?s"));
			}
		}
		qexec.close();
		return result;
	}
	
	public Set<ClassResLabResult> getConceptsGeoGraphicallyNearToWithAddress(Double lat, Double lon,Double radius) {
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class ?label ?s WHERE { "+System.lineSeparator());
		classOfResourceQuery.append("?s rdf:type ?class ."+System.lineSeparator());
		classOfResourceQuery.append("?s rdfs:label ?label ."+System.lineSeparator());
		classOfResourceQuery.append("?s geom:geometry ?geom ."+System.lineSeparator());
		classOfResourceQuery.append("?geom ogc:asWKT ?g ."+System.lineSeparator());
		classOfResourceQuery.append("Filter(bif:st_intersects (?g, bif:st_point ("+lon+","+lat+"), "+radius+")) ");
		classOfResourceQuery.append("FILTER NOT EXISTS { ?x rdfs:subClassOf ?class. FILTER (?x != ?class) }}");
		System.out.println("Query: "+classOfResourceQuery.toString());
        QueryExecution qexec = new com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP(URI.encode(this.queryurl), classOfResourceQuery.toString());
		ResultSet results = qexec.execSelect();
		Set<ClassResLabResult> result=new TreeSet<ClassResLabResult>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			//System.out.println("Class? "+solu.get("?class"));
			if(solu.get("?class").toString().contains(this.prefix.iterator().next().toString())){
				result.add(new ClassResLabResult(solu.get("?class").toString(),"",solu.get("?s").toString(),solu.get("?label").toString()));
				System.out.println("Valid Class? "+solu.get("?class"));
				System.out.println("Label? "+solu.get("?label"));
				System.out.println("Resource: "+solu.get("?s"));
			}
		}
		qexec.close();
		return result;
	}

	@Override
	public Set<String> getConceptsGeoGraphicallyNearTo(Double lat, Double lon, Double radius) {
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class ?label ?s WHERE { "+System.lineSeparator());
		classOfResourceQuery.append("?s rdf:type ?class ."+System.lineSeparator());
		classOfResourceQuery.append("?s rdfs:label ?label ."+System.lineSeparator());
		classOfResourceQuery.append("?s geom:geometry ?geom ."+System.lineSeparator());
		classOfResourceQuery.append("?geom ogc:asWKT ?g ."+System.lineSeparator());
		classOfResourceQuery.append("Filter(bif:st_intersects (?g, bif:st_point ("+lon+","+lat+"), "+radius+")) ");
		classOfResourceQuery.append("FILTER NOT EXISTS { ?x rdfs:subClassOf ?class. FILTER (?x != ?class) }}");
		System.out.println("Query: "+classOfResourceQuery.toString());
        QueryExecution qexec = new com.hp.hpl.jena.sparql.engine.http.QueryEngineHTTP(URI.encode(this.queryurl), classOfResourceQuery.toString());
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			//System.out.println("Class? "+solu.get("?class"));
			if(solu.get("?class").toString().contains(this.prefix.iterator().next().toString())){
				result.add(solu.get("?class").toString());
				System.out.println("Valid Class? "+solu.get("?class"));
				System.out.println("Label? "+solu.get("?label"));
				System.out.println("Resource: "+solu.get("?s"));
			}
		}
		qexec.close();
		return result;
	}
	
	@Override
	public Set<String> getClassesWithGeometries(){
		Set<String> result=new TreeSet<String>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("SELECT ?class WHERE { "+System.lineSeparator());
			classOfResourceQuery.append("?ind rdf:type ?class .");
			classOfResourceQuery.append("?ind <http://geovocab.org/geometry#geometry> ?geom . ");
			classOfResourceQuery.append("}");
			System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				if(solu.get("?class").toString().contains(pref)){
					result.add(solu.get("?class").toString());
				}
			}
			qexec.close();
		}
		return result;
	}*/
	


}

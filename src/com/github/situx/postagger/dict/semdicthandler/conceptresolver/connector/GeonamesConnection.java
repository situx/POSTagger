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


public class GeonamesConnection extends TripleStoreConnector {

	public GeonamesConnection(){
		this.queryurl="http://factforge.net/sparql";
		this.babelnetCompatible=false;
		this.prefix=new TreeSet<String>(Arrays.asList(new String[]{"http://www.geonames.org/ontology#","http://dbpedia.org/ontology/"}));
		this.geospatialComaptible=true;
		this.matchLabels=true;
		this.defaultRadius=0.0001;
	}
	
	public static TripleStoreConnector getInstance(){
		if(instance==null || !(instance instanceof GeonamesConnection)){
			instance=new GeonamesConnection();
		}
		return instance;
	}
	
	public Set<String> getExactConcept(String resourceString){
		Set<String> result=new TreeSet<String>();
		for(String pref:prefix){
			StringBuilder classOfResourceQuery=new StringBuilder();
			classOfResourceQuery.append(prefixCollection);
			classOfResourceQuery.append("SELECT DISTINCT ?class WHERE { "+System.lineSeparator());
			classOfResourceQuery.append("<"+pref+resourceString+">");
			classOfResourceQuery.append(" a ");
			classOfResourceQuery.append("?class . }");
			System.out.println(classOfResourceQuery);
			Query query = QueryFactory.create(classOfResourceQuery.toString());
			QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution solu=results.next();
				result.add(resourceString);
			}
			qexec.close();
		}
		return result;
	}
	
	

	@Override
	public Set<String> getConceptsGeoGraphicallyNearTo(Double lat, Double lon,Double radius) {
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class ?type WHERE { "+System.lineSeparator());
		classOfResourceQuery.append( "?class omgeo:nearby("+lat+" "+lon+" "+"\""+radius+"mi\") . "
				+ "?class rdf:type ?type .\n}");
		System.out.println("Query: "+classOfResourceQuery.toString());
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			System.out.println("Class? "+solu.get("?class")+" - "+solu.get("?type"));
			result.add(solu.get("?class").toString());
		}
		qexec.close();
		return result;
	}
	
	public static void main(String[] args){
		GeonamesConnection.getInstance().getConceptsGeoGraphicallyNearTo(50.930572016711935,7.0421669331051575);
	}

	@Override
	public Set<ClassResLabResult> getConceptsGeoGraphicallyNearTo(Double lat, Double lon, Double radius,
			Boolean dummy) {
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class ?type WHERE { "+System.lineSeparator());
		classOfResourceQuery.append( "?class omgeo:nearby("+lat+" "+lon+" "+"\""+radius+"mi\") . "
				+ "?class rdf:type ?type .\n}");
		System.out.println("Query: "+classOfResourceQuery.toString());
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<ClassResLabResult> result=new TreeSet<ClassResLabResult>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			System.out.println("Class? "+solu.get("?class")+" - "+solu.get("?type"));
			//result.add(solu.get("?class").toString());
		}
		qexec.close();
		return result;
	}

	
}

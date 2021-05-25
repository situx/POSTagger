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



public class DBPediaConnection extends TripleStoreConnector {
	
	
	public DBPediaConnection(){
		this.queryurl="http://dbpedia.org/sparql";
		this.prefix=new TreeSet<String>(Arrays.asList(new String[]{"http://dbpedia.org/ontology/","http://dbpedia.org/resource/"}));
		this.babelnetCompatible=false;
		this.geospatialComaptible=false;
		this.matchLabels=false;
		this.defaultRadius=0.0001;
	}
	
	public static TripleStoreConnector getInstance(){
		if(instance==null || !(instance instanceof DBPediaConnection)){
			instance=new DBPediaConnection();
		}
		return instance;
	}
	
	public Set<String> getDBPediaPageRedirects(String resourceString){
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class WHERE { "+System.lineSeparator());
		classOfResourceQuery.append("<"+resourceString+">");
		classOfResourceQuery.append(" dbo:wikiPageRedirects ");
		classOfResourceQuery.append("?class . }");
		System.out.println(classOfResourceQuery);
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			result.add(solu.get("?class").toString());
		}
		qexec.close();
		return result;
	}

	@Override
	public Set<String> getExactConcept(String resourceString) {
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
		if(!result.isEmpty())
			return result;
		StringBuilder classOfResourceQuery2=new StringBuilder();
		classOfResourceQuery2.append(prefixCollection);
		classOfResourceQuery2.append("SELECT DISTINCT ?class WHERE { "+System.lineSeparator());
		classOfResourceQuery2.append("<"+resourceString+">");
		classOfResourceQuery2.append(" <http://dbpedia.org/ontology/wikiPageRedirects> ");
		classOfResourceQuery2.append("?class . }");
		System.out.println(classOfResourceQuery2);
		Query query2 = QueryFactory.create(classOfResourceQuery2.toString());
		QueryExecution qexec2 = QueryExecutionFactory.sparqlService(this.queryurl, query2);
		ResultSet results2 = qexec2.execSelect();
		Set<String> result2=new TreeSet<String>();
		while(results2.hasNext()){
			QuerySolution solu2=results2.next();
			result2.add(solu2.get("?class").toString());
		}
		qexec2.close();
		System.out.println("Result2: "+result2.toString());
		return result2;
	}
	
	
	public Set<String> getDBPediaSubjectsOfResource(String resourceString){
		StringBuilder classOfResourceQuery=new StringBuilder();
		classOfResourceQuery.append(prefixCollection);
		classOfResourceQuery.append("SELECT DISTINCT ?class WHERE { "+System.lineSeparator());
		classOfResourceQuery.append("<"+resourceString+">");
		classOfResourceQuery.append(" <http://purl.org/dc/terms/subject> ");
		classOfResourceQuery.append("?classs . }");
		System.out.println(classOfResourceQuery);
		Query query = QueryFactory.create(classOfResourceQuery.toString());
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);
		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			result.add(solu.get("?class").toString());
		}
		qexec.close();
		return result;
	}

	@Override
	public Set<String> getConceptsGeoGraphicallyNearTo(Double lat, Double lon,Double defaultRadius) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ClassResLabResult> getConceptsGeoGraphicallyNearTo(Double lat, Double lon, Double radius,
			Boolean dummy) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}

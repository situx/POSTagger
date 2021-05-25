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

import it.uniroma1.lcl.babelnet.BabelNet;

import java.util.Set;

public class BabelAPIConnection extends TripleStoreConnector {
	
	public BabelAPIConnection(){
		this.queryurl="http://babelnet.org/sparql";
		this.babelnetCompatible=true;
		this.geospatialComaptible=false;
	}
	
	public BabelNet getBabelNetInstance(){
		return BabelNet.getInstance();
	}
	
	public static TripleStoreConnector getInstance(){
		if(instance==null || !(instance instanceof BabelAPIConnection)){
			instance=new BabelAPIConnection();
		}
		return instance;
	}

	@Override
	public Set<String> getConceptsGeoGraphicallyNearTo(Double lat, Double lon, Double radius) {
		return null;
	}

	@Override
	public Set<ClassResLabResult> getConceptsGeoGraphicallyNearTo(Double lat, Double lon, Double radius, Boolean dummy) {
		return null;
	}
	/*
	public static List<BabelSynset> getBabelSynsets(Language loc,String word) throws IOException{
		BabelNet babel=BabelNet.getInstance();
		return babel.getSynsets(loc, word);
	}
	
	public static Set<String> getTranslationSet(Language loc,String conceptString){
		BabelAPIConnection connection=new BabelAPIConnection();
		Set<String> result=new TreeSet<String>();
		List<BabelSense> synsets=connection.getBabelNetInstance().getSenses(loc, conceptString);
		for(BabelSense synset:synsets){
			for(BabelSense sense:synset.getSynset().getTranslations().values()){
				result.add(sense.getLemma());
			}
		}
		return result;
	}
	
	
	@Override
	public Set<String> getRelatedConcept(String loc,String word,Boolean restrictDBPedia) {
	String queryy=
	prefixCollection+
	" SELECT DISTINCT ?entries ?exact ?exactrel WHERE {"+
    "?entries a lemon:LexicalEntry ."+
    "?entries lemon:language ?lang ."+
    "?entries rdfs:label ?label ."+
    "?entries lemon:sense ?sense ."+
    "?sense  lemon:representation ?ref ."+
    "?ref skos:exactMatch ?exact ."+
    "?ref skos:related ?ref2 ."+
    "?ref2 skos:exactMatch ?exactrel ."+
    "FILTER(?lang = \""+loc.toString()+"\")"+
    "FILTER(regex(?label,\""+word+"\"))"+
"} LIMIT 100";
	System.out.println("Query: "+queryy);
		Query query = QueryFactory.create(queryy);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);

		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			System.out.println("Result: "+solu.getResource("?exactrel").toString());
			String sol=solu.getResource("?exact").toString();
			sol+=(";"+solu.getResource("?exact").toString().substring(solu.getResource("?exact").toString().lastIndexOf('/')+1).replace("_", " "));
			String sol2=solu.getResource("?exactrel").toString();
			sol+=(";"+solu.getResource("?exactrel").toString().substring(solu.getResource("?exactrel").toString().lastIndexOf('/')+1).replace("_", " "));
			if((restrictDBPedia && sol.contains("dbpedia")) || !restrictDBPedia){
				result.add(sol);
			}
			if((restrictDBPedia && sol2.contains("dbpedia")) || !restrictDBPedia){
				result.add(sol2);
			}
		}    
		qexec.close() ;
		return result;
	}
	
	
@Override
public Set<String> matchConceptsByLabel(String loc, String word, Boolean restrictDBPedia) {
	String queryy=
	prefixCollection+
	" SELECT DISTINCT ?entries ?exact WHERE {"+
    "?entries a lemon:LexicalEntry ."+
    "?entries lemon:language ?lang ."+
    "?entries rdfs:label ?label ."+
    "?entries lemon:sense ?sense ."+
    "?sense  lemon:representation ?ref ."+
    "?ref skos:exactMatch ?exact ."+
    "FILTER(?lang = \""+loc.toString()+"\")"+
    "FILTER(regex(?label,\""+word+"\"))"+
"} LIMIT 100";
	System.out.println("Query: "+queryy);
		Query query = QueryFactory.create(queryy);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);

		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			String sol=solu.getResource("?exact").toString();
			sol+=(";"+solu.getResource("?exact").toString().substring(solu.getResource("?exact").toString().lastIndexOf('/')+1).replace("_", " "));
			if((restrictDBPedia && sol.contains("dbpedia")) || !restrictDBPedia){
				result.add(sol);
			}
		}    
		qexec.close() ;
		return result;
	}
	

	public Set<String> getExactConcept(Language loc,String word,Boolean restrictDBPedia,
			Map<String,Map<String,Set<Integer>>> categories,Map<Integer,Map<String,Set<String>>> contoCat,Integer colid){
		word=word.toLowerCase();
		word = word.substring(0, 1).toUpperCase() + word.substring(1);
		String regex;
		if(word.contains(" ")){
			StringBuilder regexx=new StringBuilder();
			regexx.append("?label,\"");
			for(String split:word.split(" ")){
				regexx.append("^"+split+"[\\\\s]*|");
			}
			regexx.delete(regexx.length()-1, regexx.length());
			regexx.append("\"");
			regex=regexx.toString();
		}else{
			regex="?label,\"^"+word+"[\\\\s]*$\"";
		}
		String queryy=
				prefixCollection+
				" SELECT DISTINCT ?entries ?exact ?label ?cat WHERE {"+
			    "?entries a lemon:LexicalEntry ."+
			    "?entries lemon:language ?lang ."+
			    "?entries rdfs:label ?label ."+
			    //"?entries lexinfo:partOfSpeech 	lexinfo:noun."+
			    "?entries lemon:sense ?sense ."+
			    "?sense  lemon:representation ?ref ."+
			    "?ref bn-lemon:dbpediaCategory ?cat ."+
			    "?ref skos:exactMatch ?exact .\n"+
			    "FILTER(?lang = \""+loc.toString()+"\")"+
			    "FILTER(regex("+regex+"))"+

						System.out.println(solu.getLiteral("?label"));
						String sol=solu.getResource("?exact").toString();
						System.out.println("Solution: "+sol);
						String cat=solu.getResource("?cat").toString();
						System.out.println("Category: "+cat);
						if(!categories.containsKey(sol)){
							categories.put(sol, new TreeMap<>());				
						}
						if(!categories.get(sol).containsKey(cat)){
							categories.get(sol).put(cat, new TreeSet<Integer>());
						}
						categories.get(sol).get(cat).add(colid);
						if(colid!=null){
							if(!contoCat.containsKey(colid)){
								contoCat.put(colid,new TreeMap<>());
							}
							if(!contoCat.get(colid).containsKey(cat)){
								contoCat.get(colid).put(cat, new TreeSet<String>());					
							}
							if((restrictDBPedia && sol.contains("dbpedia")) || !restrictDBPedia)
								contoCat.get(colid).get(cat).add(sol);
						}
						//sol+=(";"+solu.getResource("?exact").toString().substring(solu.getResource("?exact").toString().lastIndexOf('/')+1).replace("_", " "));
						if((restrictDBPedia && sol.contains("dbpedia")) || !restrictDBPedia){
							result.add(sol);
						}
						if(sol.contains("dbpedia") || sol.contains("geonames")){
							resultonlydb.add(sol);
						}							
					}    
					qexec.close() ;
					if(!resultonlydb.isEmpty())
						return resultonlydb;
					else
						return result;
	}
	
	public static Set<String> getBabelConcepts2(Language loc,String word,boolean restrictDBPedia){
		BabelAPIConnection connection=new BabelAPIConnection();
		Set<String> result=new TreeSet<String>();
		List<BabelSense> synsets=connection.getBabelNetInstance().getSenses(loc, word);
		for(BabelSense synset:synsets){
			if(synset.getDBPediaURI()==null){
				if(synset.getFreebaseURI()!=null && !restrictDBPedia){
					result.add(synset.getFreebaseURI()+";"+
							synset.getFreebaseURI().toString().substring(synset.getFreebaseURI().toString().lastIndexOf('/')+1).replace("_", " "));
					//System.out.println(synset.getFreebaseURI()+";"+
					//synset.getFreebaseURI().toString().substring(synset.getFreebaseURI().toString().lastIndexOf('/')+1).replace("_", " "));
				}
			}
			else{
				//System.out.println(synset.getDBPediaURI()+";"+
				//		synset.getDBPediaURI().toString().substring(synset.getDBPediaURI().toString().lastIndexOf('/')+1).replace("_", " "));
				result.add(synset.getDBPediaURI()+";"+
						synset.getDBPediaURI().toString().substring(synset.getDBPediaURI().toString().lastIndexOf('/')+1).replace("_", " "));
			}
		}
		return result;
	}
	
	public static Set<String> getRelatedBabelConcepts2(String loc,String word,Boolean restrictDBPedia){
		return null;
	}
	
	public Set<String> matchConceptsByLabel(Language loc,List<PropertyDescriptor> word,Boolean restrictDBPedia){
		StringBuilder builder=new StringBuilder();
		for(PropertyDescriptor wor:word){
			builder.append(wor.getName().toString()+"|");
		}
	String queryy=
	prefixCollection+
	" SELECT DISTINCT ?entries ?exact ?exactrel WHERE {"+
    "?entries a lemon:LexicalEntry ."+
    "?entries lemon:language ?lang ."+
    "?entries rdfs:label ?label ."+
    "?entries lemon:sense ?sense ."+
    "?sense  lemon:representation ?ref ."+
    "?ref skos:exactMatch ?exact ."+
    "?exact rdfs:label ?exlabel ."+
    "FILTER(?lang = \""+loc.toString()+"\")"+
    "FILTER(regex(?label,\""+builder.substring(0,builder.length()-1)+"\"))"+
"} LIMIT 100";
	System.out.println("Query: "+queryy);
		Query query = QueryFactory.create(queryy);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(this.queryurl, query);

		ResultSet results = qexec.execSelect();
		Set<String> result=new TreeSet<String>();
		while(results.hasNext()){
			QuerySolution solu=results.next();
			//System.out.println("Result: "+solu.getResource("?exactrel").toString());
			String sol=solu.getResource("?exact").toString();
			sol+=(";"+solu.getResource("?exact").toString().substring(solu.getResource("?exact").toString().lastIndexOf('/')+1).replace("_", " "));
			//String sol2=solu.getResource("?exactrel").toString();
			//sol+=(";"+solu.getResource("?exactrel").toString().substring(solu.getResource("?exactrel").toString().lastIndexOf('/')+1).replace("_", " "));
			if((restrictDBPedia && sol.contains("dbpedia")) || !restrictDBPedia){
				result.add(sol);
			}
			//if((restrictDBPedia && sol2.contains("dbpedia")) || !restrictDBPedia){
			//	result.add(sol2);
			//}
		}    
		qexec.close() ;
		return result;
	}
	
	public static Map<Integer,Set<String>> generateSuperClassSet(List<String> conceptValues,String language){
		Set<String> concepts=new TreeSet<String>();
		Map<Integer,Set<String>> result=new TreeMap<Integer,Set<String>>();
		int i=1;
		for(String concept:conceptValues){
			System.out.println("Processing value: "+concept);
			Set<String> babcon=BabelAPIConnection.getInstance().getExactConcept(Language.DE, concept.trim(),false,new TreeMap<>(),new TreeMap<>(),null);
			if(babcon.isEmpty()){
				result.put(i++, new TreeSet<String>());
			}else{
				result.put(i, new TreeSet<String>());
				Set<String> temp=DBPediaConnection.getInstance().getClassOfResource(babcon.iterator().next());
				result.get(i++).addAll(temp);
				concepts.addAll(temp);
			}
		}
		result.put(0,concepts);
		return result;
	}
	
	public static Set<String> addSuperClassesToSet(Set<String> uris,String language){
		Set<String> result=uris;
		for(String concept:uris){
			System.out.println("Processing value: "+concept);
			result.addAll(DBPediaConnection.getInstance().getSuperConcepts( concept));			
		}
		return result;
	}
	
	public static void main(String[] args) throws IOException{
		BabelAPIConnection connection=new BabelAPIConnection();

		/*List<BabelSense> synsets=connection.getInstance().getSenses(Language.DE, "Fotograf");
		for(BabelSense synset:synsets){
			if(synset.getDBPediaURI()==null){
				if(synset.getFreebaseURI()!=null)
					System.out.println(synset.getFreebaseURI());
			}
			else
				System.out.println(synset.getDBPediaURI());
		}
		System.out.println(connection.getExactConcept(Language.DE, "Adresse", true,new TreeMap<>(),new TreeMap<>(),null));
		
		/*Map<Integer,Set<String>> res=connection.generateSuperClassSet(Arrays.asList(new String[]{"Wiesbaden","Mainz","Frankfurt","Berlin","Paris","Peking"}), "DE");		
		for(Integer key:res.keySet()){
			System.out.println(key+": "+res.get(key));
		}
		for(String ress:res.get(0)){
			System.out.println(ress);
		}
		Gson gson=new Gson();
		System.out.println(gson.toJson(res));
		Set<String> babcon=connection.getExactBabelConcept(Language.DE, "Wiesbaden",false);
		for(String ress:babcon){
			System.out.println(ress);
		}
		System.out.println(System.lineSeparator());
		Set<String> resss=connection.getDBPediaClassOfResource(Language.DE, "<"+babcon.iterator().next()+">");
		for(String ress:resss){
			System.out.println(ress);
			System.out.println(connection.getDBPediaIndividualsOfClass(Language.DE, "<"+ress+">"));
		}

	}

	@Override
	public Set<String> getExactConcept(String loc, String word, Boolean restrictDBPedia) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getExactConcept(String loc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getConceptsGeoGraphicallyNearTo(Double lat, Double lon,Double radius) {
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
	public Set<ClassResLabResult> localityQuery(String resourceString, GoogleMapping mapping, String language) {
		// TODO Auto-generated method stub
		return null;
	}*/

}

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

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Class for creating the tfidf matrix.
 */
public class MatrixCalc {
    /**The amount of documents to use.*/
    private final Double amountofDocuments;

    private final String currentdoc;
    /**Wordmap of one document.*/
        public Map<Tablet,Map<String,Double>> wordmap;

        /**
         * Constructor for the Matrix calculations class.
         */
        public MatrixCalc(final Map<Tablet,Map<String,Double>> wordmap,Double amountofDocuments,String currentdoc) {
            super();
            this.wordmap=wordmap;
            this.amountofDocuments=amountofDocuments;
            this.currentdoc=currentdoc;
        }

    /**
     * Calculates the tfidf measure for every term in every document.
     */
        public void calculate_tfidf(){
               for(String token:wordmap.get(currentdoc).keySet()){
                   Double tfidf=Double.parseDouble(this.wordmap.get(currentdoc).get(token)+"")
                           *(-Math.log(this.checkInHowManyDocuments(token)/amountofDocuments)/Math.log(2));
                   this.wordmap.get(currentdoc).put(token,tfidf);
                   System.out.println(token + ": " + this.wordmap.get(currentdoc).get(token));
               }
        }

    /**
     * Calculates the tfidf measure for every term in every document.
     */
    public TreeMap<String,Double> calculate_tfidf2(TreeMap<String,Double> wordmap){
        for(String token:wordmap.keySet()){
            Double tfidf=Double.parseDouble(wordmap.get(token)+"")
                    *(-Math.log(this.checkInHowManyDocuments(token)/this.amountofDocuments)/Math.log(2));
            wordmap.put(token,tfidf);
            System.out.println(token + ": " + wordmap.get(token));
        }
        return wordmap;
    }

        public Set<String> getAllWords(){
             Set<String> result=new TreeSet<>();
             for(Tablet doc:this.wordmap.keySet()){
                 for(String word:this.wordmap.get(doc).keySet()){
                     result.add(word);
                 }
             }
            return result;
        }

    /**
     * Checks in how many documents of the currently parsed documents a term occurs.
     * @param term the term to check for
     * @return the amount of occurances as Double
     */
        public Double checkInHowManyDocuments(String term){
            Double result=0.;
            for(Tablet document:wordmap.keySet()){
                if(wordmap.get(document).get(term)!=null && wordmap.get(document).get(term)>0){
                    result++;
                }
            }
            return result;
        }

    /**
     * Calculates the similarity between two documents using the cosinus measure.
     * @param documentOne the first document
     * @param documentTwo the second document
     * @return the score
     */
        public Double calculateCosinusSimilarity(String documentOne,String documentTwo){
             Double result=0.;
             Double weightSumBoth=0.;
             Double weightSumOneSquared=0.,weightSumTwoSquared=0.;
            Iterator<String> docTwoit=this.wordmap.get(documentTwo).keySet().iterator();
             String tokenTwo="";
             for(String token:this.wordmap.get(documentOne).keySet()){

                  if(docTwoit.hasNext())
                    tokenTwo=docTwoit.next();
                  if(this.wordmap.get(documentOne).get(token)==null){
                      this.wordmap.get(documentOne).put(token,0.);
                  }
                  weightSumOneSquared+=this.wordmap.get(documentOne).get(token)*this.wordmap.get(documentOne).get(token);
                 if(this.wordmap.get(documentTwo).get(tokenTwo)==null){
                     this.wordmap.get(documentTwo).put(tokenTwo,0.);
                 }
                  weightSumTwoSquared+=this.wordmap.get(documentTwo).get(tokenTwo)*this.wordmap.get(documentTwo).get(tokenTwo);
                  weightSumBoth+=this.wordmap.get(documentOne).get(token)*this.wordmap.get(documentTwo).get(tokenTwo);
             }
             weightSumOneSquared=Math.sqrt(weightSumOneSquared);
             weightSumTwoSquared=Math.sqrt(weightSumTwoSquared);
             result=weightSumBoth/(weightSumOneSquared*weightSumTwoSquared);
             return result;
        }

    /**
     * Calculates the similarity between two documents using the cosinus measure.
     * @param documentOne the first document
     * @param documentTwo the second document
     * @return the score
     */
    public Double calculateCosinusSimilarity2(String documentOne,Map<String,Double> originalmap,String documentTwo,Map<String,Double> comparemap){
        Double result=0.;
        Double weightSumBoth=0.;
        Double weightSumOneSquared=0.,weightSumTwoSquared=0.;
        Iterator<String> docTwoit=comparemap.keySet().iterator();
        String tokenTwo="";
        for(String token:originalmap.keySet()){

            if(docTwoit.hasNext())
                tokenTwo=docTwoit.next();
            if(originalmap.get(token)==null){
                originalmap.put(token,0.);
            }
            weightSumOneSquared+=originalmap.get(token)*originalmap.get(token);
            if(comparemap.get(tokenTwo)==null){
                comparemap.put(tokenTwo,0.);
            }
            weightSumTwoSquared+=comparemap.get(tokenTwo)*comparemap.get(tokenTwo);
            weightSumBoth+=originalmap.get(token)*comparemap.get(tokenTwo);
        }
        weightSumOneSquared=Math.sqrt(weightSumOneSquared);
        weightSumTwoSquared=Math.sqrt(weightSumTwoSquared);
        result=weightSumBoth/(weightSumOneSquared*weightSumTwoSquared);
        return result;
    }

    /**
     * Gets the result (the matrix).
     * @return
     */
        public Map<Tablet,Map<String,Double>> getResult(){
            return this.wordmap;
        }

    /**
     * Exports the given matrix to the file matrix.txt
     * @throws IOException on error
     */
        public void exportMatrixToFile() throws IOException {
            DecimalFormat df=new DecimalFormat("0.0");;
            File export=new File("matrix.txt");
            BufferedWriter writer=new BufferedWriter(new FileWriter(export));
            writer.write("     ");
            for(Tablet doc:this.wordmap.keySet()){
                writer.write(doc+"  ");
            }
            writer.write("\n");
            for(String word:this.getAllWords()){
                writer.write(word+"  ");
               for(Tablet doc:this.wordmap.keySet()){
                   if(this.wordmap.get(doc).get(word)==null){
                       this.wordmap.get(doc).put(word,0.);
                   }
                   writer.write(df.format(this.wordmap.get(doc).get(word))+"  ");
               }
               writer.write("\n");
            }
            writer.close();
        }

}

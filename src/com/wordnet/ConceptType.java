package com.wordnet;

import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

public class ConceptType {
	public final static String QUANTITY 		= "measure";
	public final static String ENTITY	 		= "entity";
	public final static String NONE	 			= "";
	
	public final static String[] Types			= {QUANTITY, ENTITY, NONE};
	public final static double Threshold		= 0.2;
	
	private static ILexicalDatabase db = new NictWordNet();
    private static RelatednessCalculator[] rcs = {
                    new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db), 
                    new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
                    };
    
    public static String GetClosestConcept(String word) {
    	WS4JConfiguration.getInstance().setMFS(true);
    	RelatednessCalculator rc = rcs[3];
    	
    	if(word.isEmpty()) return NONE;
    	
    	int maxIndx = -1;
    	double maxClose = Double.MIN_VALUE;
    	for(int i = 0; i < Types.length - 1; i++) {
    		double closeness = rc.calcRelatednessOfWords(word, Types[i]);
    		if(closeness > maxClose) {
    			maxClose = closeness;
    			maxIndx = i;
    		}
    	}
    	if(maxIndx < 0 || maxClose < Threshold) {
    		return NONE;
    	}
    	return Types[maxIndx];
    }
    
    public static boolean AreSimilar(String word1, String word2) {
    	WS4JConfiguration.getInstance().setMFS(true);
    	RelatednessCalculator rc = rcs[3];
    	
    	if(word1.isEmpty()) return false;
    	
    	double closeness = rc.calcRelatednessOfWords(word1, word2);
    	if( closeness > 0.3 ) 
    		return true;
    	return false;
    }
    
    private static void run( String word1, String word2 ) {
            WS4JConfiguration.getInstance().setMFS(true);
            for ( RelatednessCalculator rc : rcs ) {
                    double s = rc.calcRelatednessOfWords(word1, word2);
                    /*Concept synset1;
                    rc.calcRelatednessOfSynset(synset1, synset2);*/
                    System.out.println( rc.getClass().getName()+"\t"+s );
            }
    }
    public static void main(String[] args) {
            long t0 = System.currentTimeMillis();
            run( "prism#n","object#n" );
            long t1 = System.currentTimeMillis();
            System.out.println( "Done in "+(t1-t0)+" msec." );
    }

}

package com.wordnet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.data.relationship.Relationship;
import net.didion.jwnl.data.relationship.RelationshipFinder;
import net.didion.jwnl.data.relationship.RelationshipList;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.dictionary.morph.DefaultMorphologicalProcessor;


public class WordNet {
	final static public String PROP_FILE_PATH			= "./config/file_properties.xml";
	static public Dictionary dictionary					= null;
	
	static {
		try {
			JWNL.initialize(new FileInputStream(PROP_FILE_PATH));
			dictionary = Dictionary.getInstance();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JWNLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static double NounRelationship(String toTest, String target, int... targetSense) throws Exception {
		double ret = 0.0;
		RelationshipFinder rf = RelationshipFinder.getInstance();
		
		IndexWord targetWord = dictionary.getIndexWord(POS.NOUN, target);
		if(targetWord == null)
			return ret;
		
		IndexWord sourceWord = dictionary.getIndexWord(POS.NOUN, toTest);
		Synset[] sset = sourceWord.getSenses();
		if(sset == null || sset.length == 0)
			return ret;

		int min = 10000;
		for(int s = 0; s < targetSense.length; s++) {
			Synset tset = targetWord.getSense(targetSense[s]);
			if(tset == null)
				continue;

			for(int i = 0; i < sset.length; i++) {
				RelationshipList r = rf.findRelationships(sset[i], tset, PointerType.HYPERNYM);
				if(r.size() == 0) {
					r = rf.findRelationships(sset[i], tset, PointerType.HYPONYM);
					if(r.size() == 0)
						continue;
				}

				Relationship best = r.getShallowest();
				min = Math.min(min, best.getDepth());
				
			}
		}
		ret = (1.0/(double)min);
		return ret;
	}


	// for testing only
	public static void relationship() throws Exception {
		RelationshipFinder rf = RelationshipFinder.getInstance();
		DefaultMorphologicalProcessor dmp = new DefaultMorphologicalProcessor();
		IndexWord sourceWord = dictionary.getIndexWord(POS.VERB, "alternate");
		Synset[] sset = sourceWord.getSenses();
		Pointer[] spointerArr = sset[0].getPointers(PointerType.HYPONYM);
		
		IndexWord targetWord = dictionary.getIndexWord(POS.VERB, "change");
		Synset tset = targetWord.getSense(3);
		Pointer[] tpointerArr = tset.getPointers(PointerType.HYPONYM);

		for(int i = 0; i < sset.length; i++) {
			RelationshipList r = rf.findRelationships(sset[i], tset, PointerType.HYPERNYM);
			if(r.size() == 0) {
				r = rf.findRelationships(sset[i], tset, PointerType.HYPONYM);
				if(r.size() == 0)
					continue;
			}
			
			Relationship best = r.getShallowest();
			System.out.println("Path - Found : depth : " + best.getDepth() + " size: " + best.getSize() + " type:" + best.getType().getLabel());
			PointerTargetNodeList list = best.getNodeList();
		}
	}
	
	/*protected Synset getLCSbyIC(Synset s1, Synset s2) throws JWNLException   
    {   
        //TODO Handle the different types of LCS handled by the perl version which are   
        //   1) Largest IC value   
        //   2) Results in shortest path   
        //   3) Greatest depth (i.e. the LCS whose shortest path to root is longest)   
        //Although in here we only need the IC based one   
                   
        @SuppressWarnings("unchecked")   
        List<List<PointerTargetNode>> trees1 = PointerUtils.getInstance().getHypernymTree(s1).toList();   
           
        @SuppressWarnings("unchecked")   
        List<List<PointerTargetNode>> trees2 = PointerUtils.getInstance().getHypernymTree(s2).toList();   
           
        Set<Synset> pLCS = new HashSet<Synset>();   
           
        for (List<PointerTargetNode> t1 : trees1)   
        {   
            for (List<PointerTargetNode> t2 : trees2)   
            {   
                for (PointerTargetNode node : t1)   
                {   
                    if (contains(t2,node.getSynset()))   
                    {   
                        pLCS.add(node.getSynset());   
                        break;   
                    }   
                }   
                   
                for (PointerTargetNode node : t2)   
                {   
                    if (contains(t1,node.getSynset()))   
                    {   
                        pLCS.add(node.getSynset());   
                        break;   
                    }   
                }   
            }   
        }   
           
        Synset lcs = null;   
        double score = 0;   
           
        for (Synset s : pLCS)   
        {   
            if (lcs == null)   
            {   
                lcs = s;   
                score = getIC(s);   
            }   
            else   
            {   
                double ic = getIC(s);   
                   
                if (ic > score)   
                {   
                    score = ic;   
                    lcs = s;   
                }   
            }   
        }   
           
           
        if (lcs == null && useSingleRoot())   
        {      
            //link the two synsets by a fake root node   
               
            //TODO: Should probably create one of these for each POS tag and cache them so that we can always return the same one   
            lcs = new Synset(s1.getPOS(),0l,new Word[0],new Pointer[0],"",new java.util.BitSet());   
                           
        }   
           
        return lcs;   
    }   

	public static boolean contains(List<PointerTargetNode> ls, Synset ss) {
		for(PointerTargetNode t : ls) {
			Synset st = t.getSynset();
			if(st.equals(ss)) {
				return true;
			}
		}
		return false;
	}*/
	
	public static void main(String[] args) throws Exception {
		relationship();
		int senses[] = {1};
		double val = NounRelationship("Prism", "Measure", senses);
		System.out.println(val);
	}

}

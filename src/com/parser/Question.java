package com.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wordnet.ConceptType;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.semgraph.SemanticGraphEdge;

public class Question {

	public int questNum = 0;
	public String m_question;
	public int m_mainQuestWordIndx;	// the word that defines question like what, why
	public List<String> m_questWords = new ArrayList<String>();
	public List<String> m_questPos = new ArrayList<String>();
	public List<String> m_questNER = new ArrayList<String>();
	public SemanticGraph m_dependencyTree;
	
	public ArrayList<String> types = new ArrayList<String>(
			Arrays.asList("what", "what's", "how", "who", "whom", "which", "when" ,"whence", 
					"whither","name"));

	
	public int GetWordIndex(String word) {
		return m_questWords.indexOf(word);
	}
	
	// To be written
	public int GetQuestWordIndx() {
		int index = 0;
		for (String token: m_questWords){
			if(types.contains(token.toLowerCase())){
				return m_questWords.indexOf(token);
			}
		}
		return index;
	}
	
	public IndexedWord GetIndexedWord(int wordIdx) {
		return m_dependencyTree.getNodeByWordPattern(m_questWords.get(wordIdx));
	}
	
	public List<String> GetFullEntityR() {
		List<String> ret = new ArrayList<String>();
		for (int i = 0; i < m_questWords.size(); i++){
			if(m_questPos.get(i).contains("NN") || m_questPos.get(i).contains("JJ") ) {
				ret.add(m_questWords.get(i));
			}
			else{
				
			}
		}
		return ret;
	}
	
	
	public List<String> GetFullEntity(int wordIndx) {
		List<String> ret = new ArrayList<String>();
		int minIndx = wordIndx;
		int maxIndx = wordIndx;
		for(int i = wordIndx; i >= 0; i-- ) {
			if(m_questPos.get(i).contains("NN") || m_questPos.get(i).contains("JJ") ) {
				minIndx = i;
				if(m_questPos.get(i).contains("JJ")) break;
			} else {
				break;
			}
		}
		for(int i = wordIndx; i < m_questPos.size(); i++ ) {
			if(m_questPos.get(i).contains("NN") || m_questPos.get(i).contains("JJ") ) {
				maxIndx = i;
				if(m_questPos.get(i).contains("JJ")) break;
			} else {
				break;
			}
		}
		
		for(int k = minIndx; k <= maxIndx; k++) {
			ret.add(this.m_questWords.get(k));
		}
		return ret;
	}
	
	public List<String> GetListStringFromIndex(List<Integer> indxList) {
		List<String> words = new ArrayList<String>();
		for(int i = 0; i < indxList.size(); i++) {
			words.add(m_questWords.get(indxList.get(i)));
		}
		return words;
	}
	
	public int FindClosestWordByPOS(int wordIndx, String Pos) {
		IndexedWord sourceWord = GetIndexedWord(wordIndx);
		if(sourceWord == null) return -1;
		int minDist = 10000;
		int minWord = -1;
		for(int i = 0; i < m_questPos.size(); i++) {
			if(m_questPos.get(i).contains(Pos)) {
				IndexedWord targetWord = GetIndexedWord(i);
				List<IndexedWord> wordList = m_dependencyTree.getShortestUndirectedPathNodes(sourceWord, targetWord);
				if(wordList.size() < minDist) {
					minDist = wordList.size();
					minWord = i;
				}
			}
		}
		return minWord;
	}
	
	public List<Integer> GetSimilarWords(String target, String posT) {
		List<Integer> simWords = new ArrayList<Integer>();
		for(int i = 0 ; i < m_questWords.size(); i++) {
			String word = m_questWords.get(i);
			String pos = m_questPos.get(i);
			
			if(posT.charAt(0) == pos.charAt(0)) {
				boolean sim = ConceptType.AreSimilar(word, target);
				if(sim == true)
					simWords.add(i);
			}
		}
		return simWords;
	}
	
	public List<IndexedWord> GetShortestPathNodes(int wordIndx, int swordIndx) {
		IndexedWord sourceWord = GetIndexedWord(wordIndx);
		IndexedWord targetWord = GetIndexedWord(swordIndx);
		List<IndexedWord> wordList = m_dependencyTree.getShortestUndirectedPathNodes(sourceWord, targetWord);
		return wordList;
	}
	
	public int GetRelation(int wordIndx, String relName) {
		 List<SemanticGraphEdge> it = m_dependencyTree.edgeListSorted();
		 String relWord = "";
		 for(int i = 0;i < it.size(); i++) {
			 SemanticGraphEdge edge = it.get(i);
			 if(edge.getRelation().getShortName().equalsIgnoreCase(relName)) {
				 if(edge.getDependent().originalText().equalsIgnoreCase(m_questWords.get(wordIndx))) { 
					 relWord = edge.getGovernor().originalText();
					 break;
				 }
				 if(edge.getGovernor().originalText().equalsIgnoreCase(m_questWords.get(wordIndx))) {
					 relWord = edge.getDependent().originalText();
					 break;
				 }
			 }
		 }
		 int indx = GetWordIndex(relWord);
		 return indx;
	}
	
	// get all the relations from the specific word
	// outputs two lists, first gives relation words, second relation names
	public Map<Integer, String> GetRelations(int wordIndx) {
		 Map<Integer, String> wordRelMap = new HashMap<Integer, String>();
		 
		 List<SemanticGraphEdge> it = m_dependencyTree.edgeListSorted();
		 String relWord = "";
		 for(int i = 0;i < it.size(); i++) {
			 SemanticGraphEdge edge = it.get(i);
			 if(edge.getDependent().originalText().equalsIgnoreCase(m_questWords.get(wordIndx))) { 
				 relWord = edge.getGovernor().originalText();
				 String relation = edge.getRelation().getShortName();
				 Integer indx = GetWordIndex(relWord);
				 wordRelMap.put(indx, relation);
				 break;
			 }
			 if(edge.getGovernor().originalText().equalsIgnoreCase(m_questWords.get(wordIndx))) {
				 relWord = edge.getGovernor().originalText();
				 String relation = edge.getRelation().getShortName();
				 Integer indx = GetWordIndex(relWord);
				 wordRelMap.put(indx, relation);
				 break;
			 }
		 }
		 
		 return wordRelMap;
	}
	
	// get the dependency tree covering the whole sentence
	public List<String> GetSentenceFocusWords() {
		List<String> indxList = new ArrayList<String>();
		
		for(int i = 0; i < m_questPos.size(); i++) {
			if(m_questPos.get(i).contains("NN")){
				indxList.add(this.m_questWords.get(i));
			}	
			else if(m_questPos.get(i).contains("JJ")){
				indxList.add(this.m_questWords.get(i));
			}
			else if(m_questPos.get(i).contains("VB")) {
				indxList.add(this.m_questWords.get(i));
			}
			else{
				//System.out.println(m_questPos.get(i));
			}
		}
		return indxList;
	}
	
	/*
	public List<String> GetWordListFromIndxs(List<Integer> indxList) {
		List<String> wordList = new ArrayList<String>();
		for(int i = 0;i<indxList.size(); i++) {
			wordList.add(this.m_questWords.get(i));
		}
		return wordList;
	}*/
}

package com.arorarahul.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.store.FSDirectory;

public class SearchEngine {
	public static ArrayList<SearchResult> query(Searcher S, Integer i, String query){
		FSDirectory dirPath;
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		try {
			dirPath = FSDirectory.open(new File(LuceneConstants.indexRoot + i.toString()));
			try {
				results = S.performBooleanSearch(query, dirPath);
			} catch (ParseException e) {
				e.printStackTrace();
				System.out.println("ParseException. No hits.");
			}
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return results;
	}
	
	public static ArrayList<SearchResult> queryWiki(Searcher S, Integer i, String query){
		FSDirectory dirPath;
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		try {
			dirPath = FSDirectory.open(new File(LuceneConstants.indexWikiRoot + i.toString()));
			try {
				results = S.performBooleanSearch(query, dirPath);
			} catch (ParseException e) {
				e.printStackTrace();
				System.out.println("ParseException. No hits.");
			}
		}
		catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return results;
	}
	
	
	public static void main(String[] args){
		Searcher S = new Searcher();
		for (Integer i=230; i<400; i++){
			ArrayList<SearchResult> searchResults = query(S,i,"(erupt*)AND (vesuvius)");
			System.out.println("***");
			for (SearchResult searchResult : searchResults){
				System.out.println(searchResult.getID());
				System.out.println(searchResult.getID());
			}
			/*for (String sentence : sentences){
				Sentence s = new Sentence(sentence);
				System.out.println();
				for (String str : s.getSentenceTerms()){
					System.out.print(str + " ");
				}
				System.out.println();
				for (String str : s.getSentenceTermsPOS()){
					System.out.print(str + " ");
				}
				System.out.println();
				for (String str : s.getSentenceTermsNER()){
					System.out.print(str + " ");
				}
			}*/
			break;
		}
	}
}
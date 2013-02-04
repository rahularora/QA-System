package com.arorarahul.lucene;

public class SearchResult {
	private String id;
	private String sentenceString;
	
	public SearchResult(String id, String sentenceString){
		this.id = id;
		this.sentenceString = sentenceString;
	}
	
	public String toString(){
		return "id: "+this.id + " sentenceString: "+ this.sentenceString;
	}
	
	public String getID(){
		return id;
	}
	
	public String getSentenceString(){
		return sentenceString;
	}
}

package com.parser;

import java.util.List;


public class Sentence {
	static SParser sParser = Parser.m_sparser;
	private List<String> sentenceTerms;
	private List<String> sentenceTermsPOS;
	private List<String> sentenceTermsNER;
	private String 		 sentenceStr;
	
	public Sentence(String sentence) {
		//sParser.Init();
		List<List<String>> results = sParser.Parse(sentence);
		this.sentenceTerms = results.get(0);
		this.sentenceTermsPOS = results.get(1);
		this.sentenceTermsNER = results.get(2);
		this.sentenceStr = sentence;
	}

	public List<String> getSentenceTerms(){
		return sentenceTerms;
	}
	
	public List<String> getSentenceTermsPOS(){
		return sentenceTermsPOS;
	}
	
	public List<String> getSentenceTermsNER(){
		return sentenceTermsNER;
	}
	
	public String getSentence() {
		return sentenceStr;
	}
}

package com.parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import com.arorarahul.lucene.Indexer;
import com.arorarahul.lucene.LuceneConstants;
import com.arorarahul.lucene.SearchEngine;
import com.arorarahul.lucene.SearchResult;
import com.arorarahul.lucene.Searcher;
import com.googlesearch.GoogleSearch;
import com.parser.SParser;
import com.wordnet.ConceptType;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.semgraph.SemanticGraph;

public class Parser {

	public static final String QUESTIONS_FILE = "./data/test/questions.txt";

	public static SParser m_sparser = new SParser();
	public List<Question> m_questions = new ArrayList<Question>();
	BufferedWriter subFile;

	public void Init() {
		m_sparser.Init();
		try {
			subFile = new BufferedWriter(new FileWriter("submission" +
					".txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// parse doc to get documents
	public void GetQuestions() throws IOException {
		BufferedReader fp = new BufferedReader(new FileReader(QUESTIONS_FILE));

		boolean bquest = false;
		int num = 0;

		while(true)
		{
			String line = fp.readLine();
			if(line == null) break;

			StringTokenizer st = new StringTokenizer(line,"\n\r");
			if(st.countTokens() <= 0) continue;
			String txt = st.nextToken();
			if(txt.isEmpty() == false) {
				if(txt.contains("<num>")) {
					String[] p = txt.split(":");
					num = atoi(p[1].trim());
				} else if(txt.contains("<desc>")) {
					bquest = true;
				} else if(bquest == true) {
					Question quest = new Question();
					quest.m_question = txt;
					quest.questNum = num;
					quest.m_mainQuestWordIndx = quest.GetQuestWordIndx();
					m_questions.add(quest);
					bquest = false;

					// split question into words
					quest.m_questWords = m_sparser.GetSentWords(txt);
					quest.m_questPos = m_sparser.GetPosTags(txt);
					quest.m_questNER = m_sparser.GetNERTags(txt);
					quest.m_dependencyTree = m_sparser.GetDependencyTree(txt);

					System.out.println("question " + quest.m_question + " num " + num);
				}
			}

		}
	}

	public void Run() {
		for(int i = 0; i < m_questions.size(); i++) {
			try{
				RunQA(m_questions.get(i));
			}
			catch (OutOfMemoryError e) {
				System.out.println("What dafuq happened? I alloted 4GB of memory.");
				int j = i + LuceneConstants.startDoc;
				System.out.print(""+j);
				System.out.print(" top_docs."+j);
				System.out.print(" nil\n");
			}
		}
	}

	public void printResult(Question quest, String answer){
		try {
			subFile.write(""+quest.questNum);
			subFile.write(" top_docs."+quest.questNum);
			subFile.write(" " +answer+"\n");
			System.out.print(""+quest.questNum);
			System.out.print(" top_docs."+quest.questNum);
			System.out.print(" " +answer+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printWikiResult(Question quest, String answer){
		try {
			subFile.write(""+quest.questNum);
			subFile.write(" top_docs."+quest.questNum);
			subFile.write(" " +answer+"\n");
			System.out.print(""+quest.questNum);
			System.out.print(" top_docs."+quest.questNum);
			System.out.print(" " +answer+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// analysis each question
	public void RunQA(Question quest) {
		System.out.println("Question Sentence: " + quest.m_question);
		System.out.println("Question Word: " + quest.m_questWords.get(0));

		String queryStr = "";
		String ansTypeWord = "";

		Searcher S = new Searcher();

		HashSet<String> queryStrSet = new HashSet<String>();

		int ansTypeIndx = -1;
		int focusWordIndx = -1;
		String answerEntityType = ConceptType.NONE;

		if(quest.m_questWords.contains("What") || quest.m_questWords.contains("what") ||
				quest.m_questWords.contains("What's")) {

			List<String> focusWordList = quest.GetSentenceFocusWords();

			System.out.print("Question Focus: " );
			for (String focusWord : focusWordList){
				System.out.print(focusWord);
				if(!focusWord.equals(focusWordList.get(focusWordList.size()-1))){
					
					queryStrSet.add(focusWord);
				}
				else{
					queryStrSet.add(focusWord);
				}
			}
			System.out.print("\n");

			// get all relations from first word of question (how, why etc)
			Map<Integer, String> relWords = quest.GetRelations(quest.m_mainQuestWordIndx);
			if(relWords.size() == 0) {
				System.out.println("The first word in question doesnt have any relations - Wow :O");
			}

			// get the first relation - as mostly it wud have only one relation
			int indx = 0;
			String relation = "";
			for(Integer key: relWords.keySet())	{
				indx = key;
				relation = relWords.get(key);
			}

			// Ask Rudhir : WHat is the use of this?
			int ansTypeWordIndx = -1;

			if(quest.m_questPos.get(indx).contains("NN") || quest.m_questPos.get(indx).contains("VBN")) {
				ansTypeWord = quest.m_questWords.get(indx);
				ansTypeWordIndx = indx;
				ansTypeWord.replaceAll("s$", "");
				queryStrSet.add(ansTypeWord);
			} else {
				int relIndx = quest.GetRelation(indx, "nsubj");
				if( relIndx >= 0 ) {
					ansTypeWord = quest.m_questWords.get(relIndx);
					ansTypeWordIndx = relIndx;
					ansTypeWord.replaceAll("s$", "");
					queryStrSet.add(ansTypeWord);
				} else {
					System.out.println("nsubj type relation not found");
				}
			}

			ansTypeIndx = ansTypeWordIndx;
			
			// get the focus word
			/*int idxNN = quest.FindClosestWordByPOS(ansTypeIndx, "NN");
			focusWordIndx = idxNN;*/
			
			answerEntityType = ConceptType.GetClosestConcept(ansTypeWord);
			if (answerEntityType.equalsIgnoreCase("")){
				answerEntityType = "none";
			}
			System.out.println("Question Answer Type: " + ansTypeWord + "\nEntity: " + answerEntityType);
		}
		// else every other question type
		else {

			// get all relations from first word of question (how, why etc)
			Map<Integer, String> relWords = quest.GetRelations(quest.m_mainQuestWordIndx);
			if(relWords.size() == 0) {
				System.out.println("The first word in question doesnt have any relations - Wow :O");
				printResult(quest, "nil");
				return;
			}

			// get the first relation - as mostly it wud have only one relation
			int indx = 0;
			String relation = "";
			for(Integer key: relWords.keySet())	{
				indx = key;
				relation = relWords.get(key);
			}

			// find all NN's
			int idxNN = quest.FindClosestWordByPOS(indx, "NN");
			ansTypeIndx = indx;

			answerEntityType = quest.m_questWords.get(indx);

			if  (!m_sparser.stopWords.contains(answerEntityType)){
				queryStrSet.add(SParser.stem(answerEntityType));
			}

			System.out.println("Question Type: " + answerEntityType);
			System.out.println("Question Focus: " + quest.GetFullEntityR());
		}

		String nerTag = "";
		//Find relevant NERtag 
		if (quest.m_questWords.contains("Who") || quest.m_questWords.contains("Whom")){
			if (answerEntityType.equals("won")){
				nerTag = "LOCATION";
			}
			else{
				nerTag = "PERSON_ORGANIZATION";
			}
		}
		else if (quest.m_questWords.contains("Which")){
			nerTag = "ORGANIZATION";
		}
		else if (quest.m_questWords.contains("When")){
			nerTag = "DATE";
		}
		else if (quest.m_questWords.contains("How")){
			nerTag = "NUMBER";
		}
		else if (quest.m_questWords.contains("Where") || quest.m_questWords.contains("Whence") 
				|| quest.m_questWords.get(0).contains("Whither")){
			nerTag = "LOCATION";
		}
		else if (quest.m_questWords.contains("What")
				|| quest.m_questWords.contains("What's") || quest.m_questWords.contains("what")){

			if (answerEntityType.equals("entity") || (answerEntityType.equals("none"))){
				Sentence s = new Sentence(ansTypeWord);
				List<String> nerStr = s.getSentenceTermsNER();

				try{
					nerTag = nerStr.get(0);
					System.out.println("Nertag from ansTypeWord:"+ansTypeWord + " is: "+nerTag);
					if (nerTag.equals("O")){
						if(ansTypeWord.equalsIgnoreCase("nationality") || ansTypeWord.equalsIgnoreCase("city") 
								|| ansTypeWord.equalsIgnoreCase("country") || ansTypeWord.equalsIgnoreCase("state")){
							nerTag = "LOCATION";
						}
						else if (ansTypeWord.equalsIgnoreCase("population") || ansTypeWord.equalsIgnoreCase("rate") 
								|| ansTypeWord.equalsIgnoreCase("salary") || ansTypeWord.equalsIgnoreCase("zip") ||
								ansTypeWord.equalsIgnoreCase("code")){
							nerTag = "NUMBER";
						}
						else if (ansTypeWord.equalsIgnoreCase("birthday") || ansTypeWord.equalsIgnoreCase("date") ||
								ansTypeWord.equalsIgnoreCase("year") || ansTypeWord.equalsIgnoreCase("dates")){
							nerTag = "DATE";
						}
						else{
							nerTag = "PERSON_LOCATION_ORGANIZATION";
						}
					}
				}
				catch (Exception e){
					nerTag = "PERSON_LOCATION_ORGANIZATION";
				}
			}
			else if (answerEntityType.equals("measure")){
				nerTag = "NUMBER";
			}
			else if (answerEntityType.equals("date")){
				nerTag = "DATE";
			}
			else{
				nerTag = "PERSON_LOCATION_ORGANIZATION_NUMBER";
			}
		}
		else if (quest.m_questWords.contains("Name")){
			nerTag = "PERSON_LOCATION_ORGANIZATION";
		}
		else {
			nerTag = "PERSON_LOCATION_ORGANIZATION_NUMBER";
		}


		//Figure out all the JJ and NN in the question
		for (String str : quest.GetFullEntityR()){
			queryStrSet.add(str);
		}

		HashSet <String> oldQueryStrSet = new HashSet <String>();

		//Don't use this data structure for constructing answer. Just to figure out whether answer does not have the query string as well.
		ArrayList<String> answers = new ArrayList<String>();

		oldQueryStrSet.addAll(queryStrSet);

		Iterator<String> it = oldQueryStrSet.iterator();
		while(it.hasNext()){
			String queryStr1 = it.next();
			answers.add(queryStr1);
			if (m_sparser.stopWords.contains(queryStr1.toLowerCase())){
				queryStrSet.remove(queryStr1);
			}
		}

		Iterator<String> itr = queryStrSet.iterator();

		int i = 0;

		while(itr.hasNext()){
			queryStr += "(" + SParser.stem(itr.next()) + "*)";
			if(i < queryStrSet.size() - 1)
				queryStr += " AND ";
			i++;
		}

		System.out.println("QueryString: "+queryStr);
		System.out.println("NERTag: "+nerTag);

		ArrayList <SearchResult> searchResults = SearchEngine.query(S,quest.questNum,queryStr);

		if (searchResults.size() > 0){
			for (SearchResult srchResult : searchResults){
				Sentence s = new Sentence(srchResult.getSentenceString());

				List<String> str = s.getSentenceTerms();
				List<String> nerStr = s.getSentenceTermsNER();

				String answer = "";

				for (i = 0; i < nerStr.size(); i++){
					boolean flag = false;
					if(nerTag.equals("PERSON_LOCATION_ORGANIZATION")){
						if(nerStr.get(i).equals("PERSON")){
							flag = true;
						}
						else if(nerStr.get(i).equals("LOCATION")){
							flag = true;
						}
						else if(nerStr.get(i).equals("ORGANIZATION")){
							flag = true;
						}

					}
					else if(nerTag.equals("PERSON_LOCATION_ORGANIZATION_NUMBER")){
						if(nerStr.get(i).equals("PERSON")){
							flag = true;
						}
						else if(nerStr.get(i).equals("LOCATION")){
							flag = true;
						}
						else if(nerStr.get(i).equals("ORGANIZATION")){
							flag = true;
						}
						else if(nerStr.get(i).equals("NUMBER")){
							flag = true;
						}
					}
					else if(nerTag.equals("PERSON_ORGANIZATION")){
						if(nerStr.get(i).equals("PERSON")){
							flag = true;
						}
						else if(nerStr.get(i).equals("ORGANIZATION")){
							flag = true;
						}
					}
					else if(nerStr.get(i).equals(nerTag)){					
						flag = true;
					}

					//CHeck nerTag = date and ensure that the values are all digits and then do flag = true	

					if(flag){
						if(nerStr.get(i).equals("DATE")){
							if (str.get(i).matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {  
								flag = true;
							} else {  
								flag = false;  
							}  
						}
					}

					if (flag){
						if(!str.get(i).equals("")){
							if (!answers.contains(str.get(i))){
								answers.add(str.get(i));
								answer = answer + str.get(i) + " ";
							}
						}
					}
				}
				if (!answer.equals("")){
					printResult(quest, answer);
				}
				////////////////////////////////////
				/*
				String sentStr = s.getSentence();
				if(quest.m_questWords.contains("What")) {
					if(focusWordIndx < 0 || ansTypeIndx < 0) continue;
					// get the dependency parsing
					SemanticGraph sg = m_sparser.GetDependencyTree(sentStr);
					Question ans = new Question();
					ans.m_question = sentStr;

					// split question into words
					ans.m_questWords = s.getSentenceTerms();
					ans.m_questPos = s.getSentenceTermsPOS();
					ans.m_questNER = s.getSentenceTermsNER();
					ans.m_dependencyTree = sg;

					List<IndexedWord> wList = quest.GetShortestPathNodes(ansTypeIndx, focusWordIndx);
					List<Integer> prospectiveFocusList = ans.GetSimilarWords(quest.m_questWords.get(focusWordIndx), 
							quest.m_questPos.get(focusWordIndx));
					List<Integer> prospectiveAnsTypeList = ans.GetSimilarWords(quest.m_questWords.get(ansTypeIndx), 
							quest.m_questPos.get(ansTypeIndx));

					List<IndexedWord> relList = ans.GetShortestPathNodes(focusWordIndx, ansTypeIndx);
					if(relList == null) continue;
					int qDist = relList.size();

					for(int k = 0; k < prospectiveFocusList.size(); k++) {
						for(int i1 = 0; i1 < prospectiveAnsTypeList.size(); i1++) {
							if( k == i1 ) continue;
							int findx = prospectiveFocusList.get(k);
							int aindx = prospectiveAnsTypeList.get(i1);
							List<IndexedWord> relAnsList = ans.GetShortestPathNodes(findx, aindx);
							if(relList == null) continue;
							int aDist = relList.size();

							if(aDist < qDist + 2) {
								System.out.println("Prospective Answer couple: " + ans.m_questWords.get(aindx) +
										" Focus: " + ans.m_questWords.get(findx));
							}
						}
					}
				}*/
			}
		}

		if (answers.size() == oldQueryStrSet.size()){
			Indexer I = new Indexer();
			IndexWriterConfig config = new IndexWriterConfig(LuceneConstants.LUCENE_VERSION, LuceneConstants.analyzer);
			FSDirectory dirPath;
			ArrayList<String> docList = new ArrayList <String>();
			try {
				dirPath = FSDirectory.open(new File(LuceneConstants.indexWikiRoot + quest.questNum));
				docList = GoogleSearch.findWikipediaText(queryStr);
				//if (docList.size() > 0)
				I.createWikiIndex(dirPath, config, docList);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (docList.size() > 0){
				System.out.println("Searching inside wikipedia");
				searchResults = SearchEngine.queryWiki(S,quest.questNum,queryStr);
				searchResults.add(new SearchResult("1",docList.get(0)));

				if (searchResults.size() == 0){
					printWikiResult(quest, "nil");
				}
				else{
					for (SearchResult srchResult : searchResults){
						//System.out.println(srchResult.toString());
						String result = srchResult.getSentenceString();
						String resultStr = "";

						String[] sentences = result.split("(?<=[a-z])\\.\\s+");
						int lenSentences = sentences.length;
						if (lenSentences > 10){
							lenSentences = 10;
						}

						for (i=0; i<lenSentences; i++){
							resultStr = resultStr + sentences[i];
						}

						printWikiResult(quest, resultStr);

					}
				}
			}
		}


	}

	private SearchResult SearchResult(String string, String string2) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Parser parser = new Parser();
		parser.Init();
		parser.GetQuestions();

		parser.Run();
		parser.subFile.close();
	}

	private static int atoi(String s)
	{
		return Integer.parseInt(s);
	}

}

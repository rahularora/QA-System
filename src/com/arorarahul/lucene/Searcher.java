package com.arorarahul.lucene;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {
	Analyzer analyzer = LuceneConstants.analyzer;
	int hitsPerPage = LuceneConstants.hitsPerPage;
	IndexSearcher searcher;
	
	@SuppressWarnings("deprecation")
	public ArrayList<SearchResult> performBooleanSearch(String querystr, FSDirectory dir) throws IOException, ParseException{
		ArrayList <SearchResult> results = new ArrayList <SearchResult>();
		Query q = new QueryParser(LuceneConstants.LUCENE_VERSION, LuceneConstants.IndexField, analyzer).parse(querystr);
		searcher = new IndexSearcher(dir, true);
		TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
		searcher.search(q, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		//System.out.println("Found " + hits.length + " hits.");
		for(int i=0;i<hits.length;++i) {
			int docId = hits[i].doc;
			Document d = searcher.doc(docId);
			results.add(new SearchResult(d.get("id"), d.get(LuceneConstants.IndexField)));
		}
		searcher.close();
		return results;
	}
}

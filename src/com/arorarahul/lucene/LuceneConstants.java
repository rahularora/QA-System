package com.arorarahul.lucene;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.Version;

public class LuceneConstants {
	public static final Version LUCENE_VERSION = Version.LUCENE_36;
	public static final int hitsPerPage = 10;

	public static Analyzer analyzer = new StandardAnalyzer(LuceneConstants.LUCENE_VERSION);
	public static IndexWriterConfig config = new IndexWriterConfig(LuceneConstants.LUCENE_VERSION, analyzer);
	
	public static String IndexField = "contents";
	public static String docRoot = "test_lucenedocs/top_docs.";
	public static String indexRoot = "index_test/";
	public static int startDoc = 400;
	public static String indexWikiRoot = "index_test/wiki/";
	public static final int totalDocs = 200;
}

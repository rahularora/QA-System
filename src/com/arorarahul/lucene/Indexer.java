package com.arorarahul.lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;

public class Indexer {
	static Analyzer analyzer = LuceneConstants.analyzer;

	public boolean createIndex(FSDirectory dir, IndexWriterConfig config, ArrayList<String> docList){
		try {
			IndexWriter w = new IndexWriter(dir, config);
			int i = 0; 
			while (i < docList.size()){
				Document d = new Document();
				d.add(new Field("id", docList.get(i), Field.Store.YES, Field.Index.ANALYZED));
				d.add(new Field(LuceneConstants.IndexField, docList.get(i+1), Field.Store.YES, Field.Index.ANALYZED));
				w.addDocument(d);
				i = i + 2;
			}
			w.close();
			return true;
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean createWikiIndex(FSDirectory dir, IndexWriterConfig config, ArrayList<String> docList){
		try {
			IndexWriter w = new IndexWriter(dir, config);
			int i = 0; 
			while (i < docList.size()){
				Document d = new Document();
				d.add(new Field("id", "1", Field.Store.YES, Field.Index.ANALYZED));
				d.add(new Field(LuceneConstants.IndexField, docList.get(i), Field.Store.YES, Field.Index.ANALYZED));
				w.addDocument(d);
				i = i + 1;
			}
			w.close();
			return true;
		} catch (CorruptIndexException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LockObtainFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static void main(String[] args){
		for (Integer i=LuceneConstants.startDoc; i< (LuceneConstants.startDoc + LuceneConstants.totalDocs); i++){
			IndexWriterConfig config = new IndexWriterConfig(LuceneConstants.LUCENE_VERSION, analyzer);
			Indexer I = new Indexer();
			String filePath = LuceneConstants.docRoot + i.toString();
			FSDirectory dirPath;
			try {
				dirPath = FSDirectory.open(new File(LuceneConstants.indexRoot + i.toString()));
				try {
					FileReader fr = new FileReader(filePath);
					BufferedReader br = new BufferedReader(fr);
					String s;
					ArrayList <String> docList = new ArrayList <String>();
					try {
						while((s = br.readLine()) != null){
							docList.add(s);
						}
					} catch (IOException e) {
						System.out.println(filePath + " not able to found");
					}

					I.createIndex(dirPath, config, docList);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.out.println(filePath + " not found");
				}
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Index created for file "+ i.toString());
		}
	}

}
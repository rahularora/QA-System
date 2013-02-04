package com.googlesearch;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.googlesearch.GoogleResults.Result;


public class GoogleSearch {
	public static void main(String[] args) throws Exception {

	}

	public static ArrayList<String> findWikipediaText(String queryString) throws IOException {
		String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
		String charset = "UTF-8";

		URL url = new URL(google + URLEncoder.encode(queryString, charset));
		Reader reader = new InputStreamReader(url.openStream(), charset);
		GoogleResults results = new Gson().fromJson(reader, GoogleResults.class);

		// Show title and URL of 1st result.
		ArrayList<String> sentenceList = new ArrayList<String>();
		
		try{
			List<Result> SearchResults = results.getResponseData().getResults();
			boolean flag = false;

			for (Result r : SearchResults){
				String urlAddress = r.getUrl();   	
				if (urlAddress.matches("(.*)wikipedia(.*)")){
					Document doc = Jsoup.connect(urlAddress).get();
					String text = doc.select("p").text();
					String[] sentences = text.split("(?<=[a-z])\\.\\s+");
					sentenceList.add(r.getTitle().split("-")[0]);
					for (int i=0; i<sentences.length; i++){
						sentenceList.add(sentences[i]);
					}

					flag = true;
				}
				if(flag)
					break;
			}
		}catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return sentenceList;
	}

}

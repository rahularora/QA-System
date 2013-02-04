package com.parser;

import java.util.ArrayList;
import java.util.HashSet;

import edu.smu.tspell.wordnet.*;

/**
 * Displays word forms and definitions for synsets containing the word form
 * specified on the command line. To use this application, specify the word
 * form that you wish to view synsets for, as in the following example which
 * displays all synsets containing the word form "airplane":
 * <br>
 * java TestJAWS airplane
 */
public class wordnet
{
	public static HashSet<String> getSynonyms (String term){
		System.setProperty("wordnet.database.dir", "/Users/rahularora/Downloads/WordNet-3.0/dict");
		HashSet <String> synResults = new HashSet <String>();

		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets(term);
		synResults.add(term);
		if (synsets.length > 0)
		{
			for (int i = 0; i < synsets.length; i++)
			{
				String[] wordForms = synsets[i].getWordForms();

				for (int j = 0; j < wordForms.length; j++)
				{
					synResults.add(wordForms[j]);
				}
			}
		}
		else
		{
			System.err.println("No synsets exist that contain the word form '" + term + "'");
		}

		System.out.println("***" + term);
		for (String res : synResults){
			System.out.println(res);
		}
		
		return synResults;
	}
	
	public static void main(String[] args){
		wordnet.getSynonyms("thought");
	}
}
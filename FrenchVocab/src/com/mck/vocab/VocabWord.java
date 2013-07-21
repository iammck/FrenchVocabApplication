/**
 * 
 */
package com.mck.vocab;

/**
 * @author Michael
 *
 */
public class VocabWord {
	String eWord;
	String fWord;
	Integer id;
	String wordType;
	String currentLanguage;
	@Override
	public String toString() {
		return "eWord " + eWord
		 + " fWord " + fWord
		 +" id " + id
		 +" wordType " + wordType
		 +" currentLanguage " + currentLanguage
		 +"\n";
	}
	
	
}

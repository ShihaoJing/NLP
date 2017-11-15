/**
 * Generate sentences from a CFG
 * 
 * @author sihong
 *
 */

import java.io.*;
import java.util.*;

public class Generator {
	
	private Grammar grammar;

	/**
	 * Constructor: read the grammar.
	 */
	public Generator(String grammar_filename) {
		grammar = new Grammar(grammar_filename);
	}

	private String generate(String symbol) {
		if (grammar.symbolType(symbol) == 0)
			return symbol;
		else {
			ArrayList<RHS> rhs = grammar.findProductions(symbol);
			RHS next = rhs.get(0);
			Random rnd = new Random();
			double WSum = next.getProb();
			for (int i = 1; i < rhs.size(); ++i) {
				WSum += rhs.get(i).getProb();
				double p = rhs.get(i).getProb() / WSum;
				if (rnd.nextDouble() <= p)
					next = rhs.get(i);
			}
			return generate(next.first()) + " " + (next.second() != null ? generate(next.second()) : "");
		}
	}
	/**
	 * Generate a number of sentences.
	 */
	public ArrayList<String> generate(int numSentences) {
		ArrayList<String> sentence = new ArrayList<>();
		for (int i = 0; i < numSentences; ++i)
			sentence.add(generate("ROOT"));
		return sentence;
	}
	
	public static void main(String[] args) {
		// the first argument is the path to the grammar file.
		Generator g = new Generator(args[0]);
		ArrayList<String> res = g.generate(1);
		for (String s : res) {
			System.out.println(s);
		}
	}
}

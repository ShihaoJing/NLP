/**
 * Parser based on the CYK algorithm.
 */

import java.io.*;
import java.util.*;

public class Parser {

	class Pair {
		String first;
		int first_pos;
		String second;
		int second_pos;
		double prob;
	}

	public Grammar g;
	public HashMap<String, Pair> table[][];

	/**
	 * Constructor: read the grammar.
	 */
	public Parser(String grammar_filename) {
		g = new Grammar(grammar_filename);
	}

	/**
	 * Parse one sentence given in the array.
	 */
	public void parse(ArrayList<String> sentence) {
		int length = sentence.size();
		// Number of non-terminals
		int V = 10;
		table = new HashMap[length][length];
		for (int i = 0; i < length; ++i) {
			for (int j = 0; j < length; ++j) {
				table[i][j] = new HashMap<>();
			}
		}
		for (int j = 0; j < length; ++j) {
			for (String lhs: g.findPreTerminals(sentence.get(j))) {
				for (RHS rhs: g.findProductions(lhs)) {
					if (rhs.first().equals(sentence.get(j))) {
						Pair p = new Pair();
						p.prob = rhs.getProb();
						p.first = rhs.first(); // no second
						p.first_pos = j * length + j;
						table[j][j].put(lhs, p);
					}
				}
			}
			for (int i = j - 1; i >= 0; --i) {
				for (int k = i; k < j; ++k) {
					// A -> BC
					for (String B: table[i][k].keySet()) {
						for (String C: table[k+1][j].keySet()) {
							Pair rule_B = table[i][k].get(B);
							Pair rule_C = table[k+1][j].get(C);

							String rhs_str =  B + " " + C;
							if (g.findLHS(rhs_str) != null) {
								for (String lhs : g.findLHS(rhs_str)) {
									for (RHS rhs : g.findProductions(lhs)) {
										if (rhs.first().equals(B) && rhs.second().equals(C)) {
											Pair A = new Pair();
											A.prob = rhs.getProb() * rule_B.prob * rule_C.prob;
											A.first = B;
											A.first_pos = i * length + k;
											A.second = C;
											A.second_pos = (k + 1) * length + j;

											// check if A exist
											// If A exist, replace with higher prob one
											// If not, add A to table[i][j]
											if (table[i][j].containsKey(lhs)) {
												Pair org_A = table[i][j].get(lhs);
												if (org_A.prob < A.prob) {
													table[i][j].replace(lhs, A);
												}
											} else {
												table[i][j].put(lhs, A);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public String DFS(String lhs, int pos) {
		int i = pos / table.length;
		int j = pos % table.length;
		Pair rhs = table[i][j].get(lhs);
		if (rhs.second == null) {
			return " (" + lhs + " " + rhs.first + ")";
		}
		else {
			return " (" + lhs + DFS(rhs.first, rhs.first_pos) + DFS(rhs.second, rhs.second_pos) + ")";
		}
	}
	
	/**
	 * Print the parse obtained after calling parse()
	 */
	public String PrintOneParse() {
		return DFS("S", table.length - 1);
	}
	
	public static void main(String[] args) {
		// read the grammar in the file args[0]
		Parser parser = new Parser(args[0]);
		ArrayList<String> sentence = new ArrayList<>();
		String end = "";
		
		// read a parse tree from a bash pipe
		try {
			InputStreamReader isReader = new InputStreamReader(System.in);
			BufferedReader bufReader = new BufferedReader(isReader);
			while(true) {
				String line = null;
				if((line=bufReader.readLine()) != null) {
					String []words = line.split(" ");
					for (String word : words) {
						if (word.equals(".") || word.equals("!")) {
							end = word;
							continue;
						}
						word = word.replaceAll("[^a-zA-Z]", "");
						if (word.length() == 0) {
							continue;
						}
						// use the grammar to filter out non-terminals and pre-terminals
						if (parser.g.symbolType(word) == 0 && (!word.equals(".") && !word.equals("!"))) {
							sentence.add(word);
						}

					}
				}
				else {
					break;
				}
			}
			bufReader.close();
			isReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		parser.parse(sentence);
		System.out.println("(ROOT " + parser.PrintOneParse() + " " + end + ")");
	}
}

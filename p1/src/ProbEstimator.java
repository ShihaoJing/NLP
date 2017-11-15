import java.io.*;
import java.util.HashMap;

import org.apache.commons.math3.stat.regression.SimpleRegression;

public class ProbEstimator {
    public static void main(String[] args) throws IOException {
        String inputFile = args[0];
        String output_bigrams = args[1];
        String output_ff = args[2];
        String output_GTTable = args[3];

        FileReader fr = new FileReader(inputFile);
        BufferedReader br = new BufferedReader(fr);

        FileWriter fw_bigram = new FileWriter(output_bigrams);
        BufferedWriter bw_bigram = new BufferedWriter(fw_bigram);

        FileWriter fw_ff = new FileWriter(output_ff);
        BufferedWriter bw_ff = new BufferedWriter(fw_ff);

        FileWriter fw_GTtable = new FileWriter(output_GTTable);
        BufferedWriter bw_GTTable = new BufferedWriter(fw_GTtable);

        String word_i = br.readLine(), word_j= br.readLine();
        HashMap<String, Integer> token_count = new HashMap<>();
        HashMap<String, Integer> word_count = new HashMap<>();
        word_count.put(word_i, 1);
        word_count.put(word_j, 1);
        int total_N = 0;
        int max_token_count = 0;
        while (word_j != null) {
            ++total_N;
            word_count.put(word_j, word_count.getOrDefault(word_j, 0) + 1);

            String token_i_j = word_i + " " + word_j;
            token_count.put(token_i_j, token_count.getOrDefault(token_i_j, 0)+1);

            max_token_count = Math.max(max_token_count, token_count.get(token_i_j));
            word_i = word_j;
            word_j = br.readLine();
        }
        br.close();
        fr.close();

        int V = word_count.size(), N = token_count.size();
        System.out.println(V);
        System.out.println(total_N);
        System.out.println(N);

        HashMap<Integer, Integer> ff = new HashMap<>();
        ff.put(0, V*V - N);

        for (HashMap.Entry<String, Integer> me : token_count.entrySet()) {
            String token = me.getKey();
            int count = me.getValue();
            ff.put(count, ff.getOrDefault(count, 0) + 1);
            bw_bigram.write(me.getKey().toString() + '\n');
            bw_bigram.write(me.getValue().toString() + '\n');
        }

        /*bw_bigram.write('\n');

        for (HashMap.Entry<String, Integer> me : word_count.entrySet()) {
            bw_bigram.write(me.getKey().toString() + '\n');
            bw_bigram.write(me.getValue().toString() + '\n');
        }*/

        bw_bigram.close();
        fw_bigram.close();

        SimpleRegression simpleRegression = new SimpleRegression();

        for (int i = 1; i <= max_token_count; ++i) {
            if (ff.containsKey(i)) {
                bw_ff.write(String.valueOf(Math.log(i)) + " " + String.valueOf(Math.log(ff.get(i))));
                bw_ff.write('\n');
            }
        }

        for (int i = 1; i < 100; ++i) {
            if (ff.containsKey(i))
                simpleRegression.addData(Math.log(i), Math.log(ff.get(i)));
        }

        bw_ff.close();
        fw_ff.close();

        for (int i = 0; i <= max_token_count; ++i) {
            if (ff.containsKey(i)) {
                int count = ff.get(i);
                bw_GTTable.write(String.valueOf(count));
            }
            else {
                double predict_count = Math.exp(simpleRegression.predict(Math.log(i)));
                bw_GTTable.write(String.valueOf(predict_count));
            }
            bw_GTTable.write('\n');
        }

        bw_GTTable.close();
        fw_GTtable.close();

    }
}

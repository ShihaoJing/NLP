import java.io.*;
import java.util.*;

public class Predictor {

    static HashMap<String, Integer> bigrams = new HashMap<>();
    static HashMap<String, String> confusingWords = new HashMap<>();
    static HashMap<Integer, Double> GT = new HashMap<>();


    public static void main(String[] args) throws IOException {
        // Input: data/test_tokens_fake.txt data/all_confusingWords.txt results/bigrams.txt results/GTTable.txt
        // Output: results/test_predictions.txt

        // read bigrams


        String file_bigrams = args[2];
        FileReader fr_bigrams = new FileReader(file_bigrams);
        BufferedReader br_bigrams = new BufferedReader(fr_bigrams);

        String token;
        while ((token = br_bigrams.readLine()) != null) {
            int token_count = Integer.valueOf(br_bigrams.readLine());
            bigrams.put(token, token_count);
        }

        br_bigrams.close();
        fr_bigrams.close();

        // read GTTable
        String file_GTTable = args[3];
        FileReader fr_GT = new FileReader(file_GTTable);
        BufferedReader br_GT = new BufferedReader(fr_GT);

        String Nc;
        int C = 0;
        while ((Nc = br_GT.readLine()) != null) {
            GT.put(C++, Double.valueOf(Nc));
        }

        br_GT.close();
        fr_GT.close();

        // read confusingWords
        String file_confusingWords = args[1];
        FileReader fr_confWords = new FileReader(file_confusingWords);
        BufferedReader br_confWords = new BufferedReader(fr_confWords);

        String tmp;
        while ((tmp = br_confWords.readLine()) != null) {
            String[] words = tmp.split(":");
            confusingWords.put(words[0], words[1]);
            confusingWords.put(words[1], words[0]);
        }

        br_confWords.close();
        fr_confWords.close();


        // read test_tokens_fake
        String file_test_tokens = args[0];
        FileReader fr_test_tokens = new FileReader(file_test_tokens);
        BufferedReader br_test_tokens = new BufferedReader(fr_test_tokens);


        int line = -1, col = -1;
        String cur_word;
        String pre_word = "";
        TreeMap<Integer, ArrayList<Integer>> predict_result = new TreeMap<>();
        while ((cur_word = br_test_tokens.readLine()) != null) {
            if (cur_word.equals("<s>")) {
                ++line;
                col = -1;
            }

            ++col;
            if (confusingWords.containsKey(cur_word)) {
                if (compare_GT(100, pre_word, cur_word, confusingWords.get(cur_word))) {
                    ArrayList<Integer> list = predict_result.getOrDefault(line, new ArrayList<>());
                    list.add(col);
                    predict_result.put(line, list);
                }
            }

            pre_word = cur_word;
        }

        br_test_tokens.close();
        fr_test_tokens.close();



        // write to predictions
        String file_predictions = args[4];
        FileWriter fw_predictions = new FileWriter(file_predictions);
        BufferedWriter bw_predictions = new BufferedWriter(fw_predictions);

        for (Map.Entry<Integer, ArrayList<Integer>> me: predict_result.entrySet()) {
            int line_no = me.getKey();
            ArrayList<Integer> list = me.getValue();
            bw_predictions.write(String.valueOf(line_no) + ":");
            for (int i = 0; i < list.size(); ++i) {
                bw_predictions.write(String.valueOf(list.get(i)));
                if (i < list.size() - 1)
                    bw_predictions.write(',');
            }
            bw_predictions.write('\n');
        }

        bw_predictions.close();
        fw_predictions.close();
    }

    public static boolean compare_GT(int K, String pre_word, String cur_word, String conf_word) {
        int cur_token_count = bigrams.getOrDefault(pre_word + " " + cur_word, 0);
        int conf_token_count = bigrams.getOrDefault(pre_word + " " + conf_word, 0);

        double NC_cur = GT.get(cur_token_count);
        double NC_cur_1 = GT.get(cur_token_count+1);

        double NC_conf = GT.get(conf_token_count);
        double NC_conf_1 = GT.get(conf_token_count+1);

        double GT_cur = cur_token_count;
        double GT_conf = conf_token_count;
        if (cur_token_count < K)
            GT_cur = ((double)(cur_token_count + 1)) * (NC_cur_1 / NC_cur);


        if (conf_token_count < K)
            GT_conf = ((double)(conf_token_count + 1)) * (NC_conf / NC_conf_1);

        return GT_conf > GT_cur;
    }

    public static boolean compare_Laplacian(String pre_word, String cur_word, String conf_word, int N, int V) {
        int cur_token_count = bigrams.getOrDefault(pre_word + " " + cur_word, 0);
        int conf_token_count = bigrams.getOrDefault(pre_word + " " + conf_word, 0);

        double prob_cur_token = (double)(cur_token_count + 1) / (N + V);
        double prob_conf_token = (double)(conf_token_count + 1) / (N + V);

        return prob_conf_token > prob_conf_token;
    }
}

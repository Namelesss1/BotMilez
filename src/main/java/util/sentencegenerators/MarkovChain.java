package util.sentencegenerators;

import java.util.*;

/**
 * A class that is responsible for generating "random" sentences through
 * the use of Markov Chains and training data.
 */
public class MarkovChain {

    /* For use later */
    private final int MAX_SENTENCES = 10;
    private String[] words;

    /* Sequence of words (prefix) -> possible strings that can follow that word (suffix) */
    private Map<String, List<String>> chain;

    /* Amount of words to consider in prefix */
    private int order;
    private int maxOutputWords;

    public MarkovChain(int orderIn, String[] wordsIn, int maxIn) {
        chain = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        order = orderIn;
        words = wordsIn;
        maxOutputWords = maxIn;
        loadDictionary();
    }

    /**
     * This method maps what words are likely to come after a certain sequence of
     * prior words.
     *
     * From the given input training text, this method creates a mapping between
     * every k words called "prefixes" (determined by the order variable when creating an object of this
     * class), and a list of words ("suffixes") that can possibly follow right after the list of k words.
     */
    private void loadDictionary() {

        for (int word_i = 0; word_i < words.length - order; word_i++) {
            StringBuilder keyBuilder = new StringBuilder("");
            for (int offset = 0; offset < order; offset++) {
                String token = words[word_i + offset];
                keyBuilder.append(token);
                if (offset != order - 1) {
                    keyBuilder.append(" ");
                }
            }

            String key = keyBuilder.toString();
            if (!chain.containsKey(key)) {
                chain.put(key, new ArrayList<>());
            }
            chain.get(key).add(words[word_i + order]);
        }
    }


    /**
     * This method takes the prefix-suffix mappings of k order and then uses
     * it to generate a seemingly random sentence through the probabilities
     * created by the frequency of items in the mappings.
     * @return a string representing the generated text.
     */
    public String generateSentence() {
        Random random = new Random();
        int count = 0;
        int index = random.nextInt(chain.size());
        String prefix = (String) chain.keySet().toArray()[index];
        List<String> output = new ArrayList<>(Arrays.asList(prefix.split(" ")));

        while (true) {
            List<String> suffix = chain.get(prefix);
            if (suffix == null) {
                return output.stream().reduce("", (a, b) -> a + " " + b);
            }
            if (suffix.size() == 1) {
                if (Objects.equals(suffix.get(0), "")) {
                    return output.stream().reduce("", (a, b) -> a + " " + b);
                }
                output.add(suffix.get(0));
            } else {
                index = random.nextInt(suffix.size());
                output.add(suffix.get(index));
            }
            if (output.size() >= maxOutputWords) {
                return output.stream().limit(maxOutputWords).reduce("", (a, b) -> a + " " + b);
            }
            count++;
            prefix = output.stream().skip(count).limit(order).reduce("", (a, b) -> a + " " + b).trim();
        }
    }

}

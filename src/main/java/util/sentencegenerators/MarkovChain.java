package util.sentencegenerators;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

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

    private int fileCount = 0;

    private String path;

    public MarkovChain(int orderIn, String[] wordsIn, int maxIn) {
        chain = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        order = orderIn;
        words = wordsIn;
        maxOutputWords = maxIn;
        System.out.println("Loading MarkovChain");
        loadDictionary();
    }

    public MarkovChain(int orderIn, String pathIn, int maxIn) {
        chain = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        order = orderIn;
        path = pathIn;
        maxOutputWords = maxIn;
        System.out.println("Loading MarkovChain");
        loadDictionaryFromFile();
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
     * This method maps what words are likely to come after a certain sequence of
     * prior words. Same function as loadDictionary() but does so from a file directly to
     * save memory space.
     *
     * From the given input training text, this method creates a mapping between
     * every k words called "prefixes" (determined by the order variable when creating an object of this
     * class), and a list of words ("suffixes") that can possibly follow right after the list of k words.
     */
    private void loadDictionaryFromFile() {

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(path));
            String line;

            final int MAX_WORDS = 20000000;
            int wordCount = 0;

            System.out.println("Inputting words now");
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    String[] lineWords = line.split("\\s+");
                    for (int word_i = 0; word_i < lineWords.length - order; word_i++) {
                        wordCount++;
                        System.out.println(wordCount);

                        if (wordCount >= MAX_WORDS) {
                            PrintWriter writer = new PrintWriter(path + fileCount);
                            for (String key : chain.keySet()) {
                                writer.print(key);
                                writer.print(" ");
                                writer.println(chain.get(key).toString());
                            }
                            writer.close();
                            chain.clear();
                            wordCount = 0;
                            fileCount++;
                        }

                        StringBuilder keyBuilder = new StringBuilder("");
                        for (int offset = 0; offset < order; offset++) {
                            String token = lineWords[word_i + offset];
                            keyBuilder.append(token);
                            if (offset != order - 1) {
                                keyBuilder.append(" ");
                            }
                        }

                        String key = keyBuilder.toString();

                        /* Ensure that the key is not contained within any of the other files. */
                        if (!addExistingKeyToFile(key, lineWords[word_i + order])) {
                            if (!chain.containsKey(key)) {
                                chain.put(key, new ArrayList<>());
                            }
                            chain.get(key).add(lineWords[word_i + order]);
                        }

                    }
                }
            }
        }
        catch (FileNotFoundException ex) {
            System.out.println("MarkovChain: File not found");
            System.out.println(ex.getLocalizedMessage());
        }
        catch(IOException e) {
            System.out.println("MarkovChain: An error has occurred when reading words into array");
            System.out.println(e.getLocalizedMessage());
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


    /**
     * Checks if any split files already contain the given key. If so, add the values to
     * the current existing values in that file.
     *
     * @param key key to search for within the files
     * @param value string representing the value to add.
     * @return true if a file containing the key was found, false if not
     */
    private boolean addExistingKeyToFile(String key, String value) {
        boolean success = false;

        try {
            for (int file_i = 0; file_i < fileCount; file_i++) {
                PrintWriter writer = new PrintWriter(path + "temp");
                BufferedReader reader = new BufferedReader(new FileReader(path + file_i));

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() >= key.length() && line.substring(0, key.length()).equals(key)) {
                        line = line + " " + value;
                        success = true;
                    }
                    writer.println(line);
                }
                writer.close();
                reader.close();
                File original = new File("path" + file_i);
                original.delete();
                File temp = new File("path" + "temp");
                temp.renameTo(original);
            }
        }
        catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
            success = false;
        }

        return success;
    }
}

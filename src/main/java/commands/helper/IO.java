package commands.helper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

/**
 * Class to contain many helper functions related to IO and files as needed by
 * specific commands
 */
public class IO {

    /**
     * Reads the entire file given by the String path, returns an array
     * where each element is a line from the file. (Index 0 = first line,
     * index 1 = second line, etc.)
     *
     * @param path Location of file to read from
     * @return A String array where each element is a line from the file.
     */
    public static String[] readAllFileLinesIntoArray(String path) {

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(path));
            return Arrays.stream(reader.lines().toArray()).toArray(String[]::new);
        }
        catch (FileNotFoundException ex) {
            System.out.println("File not found");
            System.out.println(ex.getLocalizedMessage());
            return null;
        }

    }

}

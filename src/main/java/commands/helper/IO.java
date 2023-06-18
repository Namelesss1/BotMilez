package commands.helper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;

public class IO {

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

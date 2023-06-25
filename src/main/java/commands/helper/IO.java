package commands.helper;

import java.io.*;
import java.util.Arrays;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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


    public static JSONObject readJsonObj(String path) {
        JSONParser parser = new JSONParser();
        JSONObject result = null;

        try {
            Reader jsonReader = new FileReader(path);
            result = (JSONObject)parser.parse(jsonReader);
        }
        catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
        catch (ParseException e) {
            System.out.println(e.getLocalizedMessage());
        }

        return result;
    }


    public static boolean writeJson(JSONObject jsonObj, String path) {
        boolean successful = true;
        FileWriter writer;

        try {
            writer = new FileWriter(path);
            writer.write(jsonObj.toJSONString());
        }
        catch(IOException e) {
            System.out.println(e.getLocalizedMessage());
            successful = false;
        }

        return successful;
    }

}

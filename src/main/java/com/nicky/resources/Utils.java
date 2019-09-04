package com.nicky.resources;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nicky.brdfs.BRDF;
import com.nicky.brdfs.CompositeBRDF;
import javafx.util.Pair;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * <h1>Utility Methods</h1>
 * A set of utility methods.
 *
 * @author Nicky Buttigieg
 * @version 1.0
 * @since 2018-05-23
 */
public class Utils {

    /**
     * Read a file's contents.
     *
     * @param path The path to the file
     * @return String The file contents.
     */
    public static String loadResource(String path) throws IOException {
        String sourceCode = "";

        try {
            sourceCode = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw e;
        }

        return sourceCode;
    }

    /**
     * Load a resource from the Resources folder
     *
     * @param folderPath
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static List<String> loadResourcesFromFolder(String folderPath) throws IOException, URISyntaxException {
        ClassLoader classLoader = Utils.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(folderPath);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String filename;
        StringBuilder fileContent = new StringBuilder();

        List<String> contents = new ArrayList<>();

        while ((filename = bufferedReader.readLine()) != null) {
            Path path = Paths.get(classLoader.getResource(folderPath + "/" + filename).toURI());
            Stream<String> lines = Files.lines(path);
            lines.forEach(linee -> fileContent.append(linee).append("\n"));
            lines.close();
            contents.add(fileContent.toString());
            fileContent.setLength(0);
        }
        bufferedReader.close();
        return contents;
    }

    /**
     * Deserialise a string into a JSON
     *
     * @param jsonString The JSON stored as a String
     * @return JsonObject Returns JSON
     */
    public static JsonObject deserialiseJSON(String jsonString) {
        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(jsonString, JsonElement.class);
        JsonObject obj = jsonElement.getAsJsonObject();

        return obj;
    }

    /**
     * Serialise a BRDF into JSON
     * @param brdfPair BRDF Name and Instance to be serialised
     * @return String Returns serialised BRDF.
     */
    public static String serialiseBRDFJson(Pair<String, BRDF> brdfPair) {
        String alias = brdfPair.getKey();
        BRDF brdf = brdfPair.getValue();
        boolean simple = brdf.getClass() != CompositeBRDF.class;
        StringBuilder sb = new StringBuilder();

        sb.append("{");
        sb.append("\"alias\": ").append("\"").append(alias).append("\",");
        if (simple) {
            sb.append("\"type\": ").append("\"simple\",");
            sb.append("\"components\": [");
            sb.append("{");
            sb.append("\"name\": ").append("\"").append(brdf.getName()).append("\",");
            sb.append("\"type\": ").append("\"simple\",");
            sb.append(brdf.serialise());
            sb.append("}");
            sb.append("]");
        } else {
            sb.append("\"type\": ").append("\"composite\",");
            sb.append(brdf.serialise());
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Save string to a file
     * @param filepath The filepath
     * @param json File content
     * @return boolean Returns true if the file was successfully saved.
     */
    public static boolean saveJsonToFile(String filepath, String json) {
        BufferedWriter writer = null;
        boolean success = false;
        try {
            File file = new File(filepath);

            System.out.println("Writing to " + file.getCanonicalPath());
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(json);
            success = true;
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                writer.close();
                return success;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
}

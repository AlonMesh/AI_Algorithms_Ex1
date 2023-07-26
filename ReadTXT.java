import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * This utility class contains static methods for processing input data from a .txt file.
 */
public class ReadTXT {

    /**
     * Reads the content of a .txt file and returns it as a string.
     *
     * @param fileName The name of the .txt file to be read.
     * @return The content of the .txt file as a string.
     * @throws RuntimeException If an I/O error occurs while reading the file.
     */
    public static String readFileAsString(String fileName) {
        try {
            return new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts the XML file content from the input data.
     *
     * @param data The input data containing both the XML file and queries.
     * @return The XML file content as a string.
     */
    public static String getXmlFile(String data) {
        return data.substring(0, data.indexOf("\n"));
    }

    /**
     * Extracts the array of inputs (queries) from the input data.
     *
     * @param data The input data containing the queries.
     * @return An array of strings representing the queries, each cell is a query.
     */
    public static String[] getArrayOfInputs(String data) {
        String subData = data.substring(data.indexOf("\n") + 1);
        return subData.split("\n");
    }

    /**
     * Extracts the details of the query from the input line and the network.
     *
     * @param line    The input line representing the query.
     * @param network The network containing variables and their properties.
     * @return An array of strings representing the query details (arr[0] = name, arr[1] = given outcome).
     */
    public static String[] getQuery(String line, Network network) {
        String[] queryDetails = new String[2]; // Creating array with 2 elements
        queryDetails[0] = line.substring(2, line.indexOf("=")); // Represents the query's name.
        queryDetails[1] = line.substring(line.indexOf("=") + 1, line.indexOf("|")); // Represents the query's given outcome.

        int minOcLen = 999;
        for (String outcome : network.findVarByName(queryDetails[0]).getOutcomes()) {
            minOcLen = Math.min(minOcLen, outcome.length());
        }

        queryDetails[1] = queryDetails[1].substring(0, minOcLen);

        return queryDetails;
    }

    /**
     * Extracts the evidence line from the input query.
     *
     * @param line The input line representing the query.
     * @return The evidence line as a string.
     */
    public static String getEvidenceLine(String line) {
        return line.substring(line.indexOf('|') + 1, line.indexOf(')'));
    }

    /**
     * Creates a LinkedHashMap of given evidences with variable names as keys and evidence data as values.
     *
     * @param line          The evidence line from the query.
     * @param variableNames The list of variable names in the network.
     * @param network       The network containing variables and their properties.
     * @return A LinkedHashMap with variable names as keys and evidence data as values.
     */
    public static LinkedHashMap<String, String> get_linkedHashMap_of_given_evidences(String line, List<String> variableNames, Network network) {
        LinkedHashMap<String, String> evidences = new LinkedHashMap<>();
        String[] tempArr = line.split(",");

        // Every variable in the network gets a default value "%%%".
        for (String name : variableNames) {
            evidences.put(name, "%%%");
        }

        // Give the given variables their outcome.
        for (String info : tempArr) {
            String variableName = info.substring(0, info.indexOf('='));

            int minOcLen = 999;
            for (String outcome : network.findVarByName(variableName).getOutcomes()) {
                minOcLen = Math.min(minOcLen, outcome.length());
            }

            String variableData = info.substring(info.indexOf('=') + 1).substring(0, minOcLen);
            evidences.replace(variableName, "%%%", variableData);
        }
        return evidences;
    }

    /**
     * Extracts the chosen algorithm from the input query.
     *
     * @param line The input line representing the query.
     * @return The chosen algorithm as a string.
     */
    public static String getChosenAlgo(String line) {
        return line.substring(line.indexOf("),") + 2, (line.indexOf("),") + 3));
    }
}

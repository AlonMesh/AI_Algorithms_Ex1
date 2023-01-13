import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/*
    This file is a class file, however it contains no objects, but only static methods.
    That file exists for aesthetic reasons.
    All the methods are using to receive input.
 */
public class ReadTXT {

    //Get a string of .txt file.
    public static String readFileAsString(String fileName) throws IOException {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    public static String getXmlFile(String data) {
        return data.substring(0, data.indexOf("\n"));
    }

    public static String[] getArrayOfInputs(String data) {
        data = data.substring(data.indexOf("\n")+1);
        String[] arr = data.split("\n");
        return arr;
    }

    public static String[] getQuery(String line, Network network) {
        String[] queryDetails = new String[2];
        queryDetails[0] = line.substring(2, line.indexOf("=")); //The query's name.
        queryDetails[1] = line.substring(line.indexOf("=")+1, line.indexOf("|")); //The query's given outcome.

        int minOcLen = 999;
        for (String outcome : network.find_variable_by_name(queryDetails[0]).getOutcomes()) {
            minOcLen = Math.min(minOcLen, outcome.length());
        }

        queryDetails[1] = queryDetails[1].substring(0, minOcLen);

        return queryDetails;
    }

    public static String getEvidenceLine(String line) {
        return line.substring(line.indexOf('|')+1, line.indexOf(')'));
    }

    public static LinkedHashMap<String, String> get_linkedHashMap_of_given_evidences(String line, List<String> variableNames, Network network) {
        LinkedHashMap<String, String> evidences = new LinkedHashMap<String, String>();
        String[] tempArr = line.split(",");

        //Every variable in the network gets a default value "%%%".
        for (String name : variableNames) {
            evidences.put(name, "%%%");
        }

        //Give the given variables their outcome.
        for (String info : tempArr) {
            String variableName = info.substring(0, info.indexOf('='));

            int minOcLen = 999;
            for (String outcome : network.find_variable_by_name(variableName).getOutcomes()) {
                minOcLen = Math.min(minOcLen, outcome.length());
            }

            String variableData = info.substring(info.indexOf('=')+1).substring(0,minOcLen);
            evidences.replace(variableName, "%%%", variableData);
        }

        //The reason I used LinkedHashMap is cuz it save the INSERTION ORDER (unlike HaspMap).
        //HashMap<String, String> evidences2 = (HashMap<String, String>) evidences;

        //Sorting by given amount

        return evidences;
    }

    public static String getChosenAlgo(String line) {
        return line.substring(line.indexOf("),")+2, (line.indexOf("),")+3));
    }
}

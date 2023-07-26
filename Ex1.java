import java.io.IOException;
import java.util.*;

public class Ex1 {

    /**
     * Checks if the current input appears in the query's conditional probability table (CPT).
     * The principle is simple:
     * We have a given list: query.givens list, and we find another list of given variables in the query.
     * If the query's parents list is a subset of the given variable list, then we can find the answer easily.
     * @param network      The network containing variables and their CPTs.
     * @param queryDetails An array containing the query variable name at index 0 and the query value at index 1.
     * @return true if the answer to the query is found and written to the output file, false otherwise.
     */
    private static boolean checkIfQueryInCpt(Network network, String[] queryDetails) {
        List<Variable> listOfKnownVars = new ArrayList<>();

        // Iterate over each variable in the list of variables in the "network" and add the known var to the list.
        for (Variable variable : network.getVariables()) {
            String valueByKey = network.getEvidences().get(variable.getName());
            if (!valueByKey.equals("%%%"))
                listOfKnownVars.add(variable);
        }

        // Finding the main variable based on the queryDetails.
        Variable mainVar = network.findVarByName(queryDetails[0]);

        // If A is subset of B and B is subset of A then A==B. The function equals() doesn't work because order is critical.
        if (new HashSet<>(listOfKnownVars).containsAll(mainVar.getGivens()) && mainVar.getGivens().containsAll(listOfKnownVars)) {
            StringBuilder str = new StringBuilder();
            for (Variable parent : mainVar.getGivens()) {
                str.append(network.getEvidences().get(parent.getName()));
            }
            str.append(queryDetails[1]);
            try {
                GlobalVars.outputFile.write(Algorithms.printRoundedDouble(mainVar.getProb(str.toString(), network)) + ",0,0");
                GlobalVars.outputFile.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        return false;
    }

    /**
     * Sends the query to the selected algorithm for processing.
     * @param network      The network containing variables and their conditional probability tables (CPTs).
     * @param queryDetails An array containing the query variable name at index 0 and the query value at index 1.
     * @param chosenAlgo   The chosen algorithm for processing the query (1 for simple conclusion, 2 for VE by a,b,c order, 3 for VE by appearance order).
     * @throws RuntimeException If an IOException occurs during the algorithm's execution.
     */
    private static void sendQueryToAlgo(Network network, String[] queryDetails, String chosenAlgo) {
        // Clean the query before sending it
        for (Variable variable : network.getVariables()) {
            if (variable.getGivens().size() > 0)
                if (variable.getGivens().get(variable.getGivens().size() - 1).getName().equals(variable.getName()))
                    // If the name of the last given is the same as the name of the variable, remove the last given from the list
                    variable.getGivens().remove(variable.getGivens().get(variable.getGivens().size() - 1));
        }

        // Switch statement based on the value of the variable "chosenAlgo"
        try {
            switch (chosenAlgo) {
                case "1" -> //Algo 1, simple conclusion
                        Algorithms.simpleConclusion(network, queryDetails);
                case "2" -> //Algo 2, VE by a,b,c order
                        Algorithms.conclusionByVE(network, queryDetails, '2');
                case "3" -> //Algo 3, VE by appearance order
                        Algorithms.conclusionByVE(network, queryDetails, '3');
                default -> throw new IllegalArgumentException("Invalid algorithm choice: " + chosenAlgo);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles the given query by processing it using the specified algorithm or checking if it appears in the conditional probability table (CPT).
     * @param network The network containing variables and their conditional probability tables (CPTs).
     * @param queryArray   Array which contains any query string to be processed.
     */
    private static void handleQueries(Network network, String[] queryArray) {
        for (String query : queryArray) {
            // At the start of each iteration, set the multiplying and summing amount to 0.
            GlobalVars.setGlobalSum(0);
            GlobalVars.setGlobalMulti(0);

            // Extract initial data for a query from the string
            String[] queryDetails = ReadTXT.getQuery(query, network);
            String evidenceLine = ReadTXT.getEvidenceLine(query);
            LinkedHashMap<String, String> evidences = ReadTXT.get_linkedHashMap_of_given_evidences(evidenceLine, network.getVariablesNames(), network);
            network.setEvidences(evidences);
            String chosenAlgo = ReadTXT.getChosenAlgo(query);

            // In the if statement, check if the query is appears in CPT
            // If it is, then print it to the file. Else, find probability by the given Algorithm.
            if (!checkIfQueryInCpt(network, queryDetails)) {
                sendQueryToAlgo(network, queryDetails, chosenAlgo);
            }

        }
    }

    public static void main(String[] args) {
        // Get the text file as input and extract the path to the xml file
        String inputStr = ReadTXT.readFileAsString("input7.txt");
        String pathXML = ReadTXT.getXmlFile(inputStr);

        // Creating the network by the given XML details
        Network network = Network.createNetworkByXmlPath(pathXML);

        // Extract the rest of the text file as an array of queries
        String[] queryArray = ReadTXT.getArrayOfInputs(inputStr);

        // Iterate of all the queries and handle them
        handleQueries(network, queryArray);

        // Save the output file as requested
        try {
            GlobalVars.outputFile.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
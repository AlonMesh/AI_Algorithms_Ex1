import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.*;

public class Ex1 {

    public static void handleQuery(Network network, String query) {
        // At the start of each iteration, set the multiplying and summing amount to 0.
        GlobalVars.setGlobalSum(0);
        GlobalVars.setGlobalMulti(0);

        // Extract initial data for a query from the string
        String[] queryDetails = ReadTXT.getQuery(query, network);
        String evidenceLine = ReadTXT.getEvidenceLine(query);
        LinkedHashMap<String, String> evidences = ReadTXT.get_linkedHashMap_of_given_evidences(evidenceLine, network.getVariablesNames(), network);
        network.setEvidences(evidences);
        String chosenAlgo = ReadTXT.getChosenAlgo(query);

            /*Checking if the current input appears at the quarry's cpt.
            The principal is simple we have 2 lists:
                - quarry.parents list
                - given variable list
            if quarry's parents list is a subset of the given variable list, then we can find the ans easily. */

        List<Variable> list_of_known_variables = new ArrayList<Variable>();

        // Iterate over each variable in the list of variables in the "network", add the known var to a list.
        for (Variable variable : network.getVariables()) {
            String valueByKey = network.getEvidences().get(variable.getName());
            if (!valueByKey.equals("%%%"))
                list_of_known_variables.add(variable);
        }

        Variable temp_query_holder = network.find_variable_by_name(queryDetails[0]);

        //If A is subset of B and B is subset of A then A==B.
        //The function equals() doesn't work because order is critic.
        if (list_of_known_variables.containsAll(temp_query_holder.getGivens()) && temp_query_holder.getGivens().containsAll(list_of_known_variables)) {
            String str = "";
            for (Variable parent : temp_query_holder.getGivens()) {
                str = str + network.getEvidences().get(parent.getName());
            }
            str = str + queryDetails[1];
            try {
                GlobalVars.outputFile.write(Algorithms.printRoundedDouble(temp_query_holder.getProb(str, network)) + ",0,0");
                GlobalVars.outputFile.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        else {

            // Iterate over each variable in the list of variables called "network.variables"
            for (Variable variable : network.getVariables()) {
                if (variable.getGivens().size()>0)
                    // Check if the name of the last given in the list of givens is the same as the name of the variable
                    if (variable.getGivens().get(variable.getGivens().size()-1).getName().equals(variable.getName()))
                        // If the name of the last given is the same as the name of the variable, remove the last given from the list
                        variable.getGivens().remove(variable.getGivens().get(variable.getGivens().size()-1));
            }

            // Switch statement based on the value of the variable "chosenAlgo"
            try {
                switch (chosenAlgo) {
                    case "1": //Algo 1, simple conclusion
                        Algorithms.simpleConclusion(network, queryDetails);
                        break;
                    case "2": //Algo 2, VE by a,b,c order
                        Algorithms.conclusionByVE(network, queryDetails, '2');
                        break;
                    case "3": //Algo 3, VE by appearance order
                        Algorithms.conclusionByVE(network, queryDetails, '3');
                        break;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        // Get the text file as input and extract the path to the xml file
        String inputStr;
        try {
            inputStr = ReadTXT.readFileAsString("input7.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String pathXML = ReadTXT.getXmlFile(inputStr);

        // Creating the network by the given XML details
        Network network = Network.createNetworkByXMLpath(pathXML);

        // Extract the rest of the text file as an array of queries
        String[] queryArray = ReadTXT.getArrayOfInputs(inputStr);

        for (String query : queryArray) {
            handleQuery(network, query);
        }

        try {
            GlobalVars.outputFile.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
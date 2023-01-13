import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.util.*;

public class Ex1 {

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {

        Network network = new Network();

        List<String> outcomesList = new ArrayList<String>();
        List<String> givensList = new ArrayList<String>();
        List<Double> valuesList = new ArrayList<Double>();

        String inputStr = ReadTXT.readFileAsString("input.txt");
        String pathXML = ReadTXT.getXmlFile(inputStr);

        ReadXML.creating_network_and_set_lists_for_each_variable(pathXML, network, outcomesList, givensList, valuesList);

        String[] arrayOfInputs = ReadTXT.getArrayOfInputs(inputStr);

        for (String inputLine : arrayOfInputs) {

            // At the start of each iteration, set the multiplying and summing amount to 0.
            GlobalVars.setGlobalSum(0);
            GlobalVars.setGlobalMulti(0);

            String[] queryDetails = ReadTXT.getQuery(inputLine, network);

            String evidenceLine = ReadTXT.getEvidenceLine(inputLine);

            LinkedHashMap<String, String> evidences = ReadTXT.get_linkedHashMap_of_given_evidences(evidenceLine, network.getVariablesNames(), network);
            network.setEvidences(evidences);

            String chosenAlgo = ReadTXT.getChosenAlgo(inputLine);

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
                    GlobalVars.outputFile.write(Algorithms.printRoundedDouble(temp_query_holder.getProb(str, network)) + ",0,0");
                    GlobalVars.outputFile.newLine();
            }

            else {

                // Iterate over each variable in the list of variables called "network.variables"
                for (Variable variable : network.variables) {
                    if (variable.givens.size()>0)
                        // Check if the name of the last given in the list of givens is the same as the name of the variable
                        if (variable.givens.get(variable.givens.size()-1).getName().equals(variable.name))
                            // If the name of the last given is the same as the name of the variable, remove the last given from the list
                            variable.givens.remove(variable.givens.get(variable.givens.size()-1));
                }

                // Switch statement based on the value of the variable "chosenAlgo"
                switch (chosenAlgo) {
                    case "1": //Algo 1
                        Algorithms.simpleConclusion(network, queryDetails);
                        break;
                    case "2": //Algo 2
                        Algorithms.conclusionByVE(network, queryDetails, '2');
                        break;
                    case "3": //Algo 3
                        Algorithms.conclusionByVE(network, queryDetails, '3');
                        break;
                }
            }
        }

        GlobalVars.outputFile.flush();
    }
}


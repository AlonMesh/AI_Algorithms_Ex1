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

        //network.print();

        ///Reading .txt file

        //System.out.println("Our file is: ");
        //String inputStr = ReadTXT.readFileAsString("C:\\Users\\Alon\\input.txt");
        String inputStr = ReadTXT.readFileAsString("input2.txt");
        //System.out.println(inputStr);
        String pathXML = ReadTXT.getXmlFile(inputStr);
        //System.out.println("A: " + pathXML);

        ReadXML.creating_network_and_set_lists_for_each_variable(pathXML, network, outcomesList, givensList, valuesList);

        String[] arrayOfInputs = ReadTXT.getArrayOfInputs(inputStr);
        System.out.println("B: " + arrayOfInputs[0]);
        //System.out.println("C: " + arrayOfInputs[1]);

        //System.out.println("B: " + arrayOfInputs[5]);

        for (String inputLine : arrayOfInputs) {

            GlobalVars.setGlobalSum(0);
            GlobalVars.setGlobalMulti(0);
            System.out.println("D: " + inputLine);

            String[] queryDetails = ReadTXT.getQuery(inputLine, network);

            System.out.println("E: " + queryDetails[0]);
            System.out.println("F: " + queryDetails[1]);

            String evidenceLine = ReadTXT.getEvidenceLine(inputLine);
            System.out.println("G: " + evidenceLine);

            LinkedHashMap<String, String> evidences = ReadTXT.get_linkedHashMap_of_given_evidences(evidenceLine, network.getVariablesNames(), network);

            System.out.println("H: " + evidences);
            network.setEvidences(evidences);

            String chosenAlgo = ReadTXT.getChosenAlgo(inputLine);
            System.out.println("I: " + chosenAlgo);


            /*Checking if the current input appears at the quarry's cpt.
            The principal is simple we have 2 lists:
                - quarry.parents list
                - given variable list
            if quarry's parents list is a subset of the given variable list, then we can find the ans easily. */

            List<Variable> list_of_known_variables = new ArrayList<Variable>();

            for (Variable variable : network.getVariables()) {
                String valueByKey = network.getEvidences().get(variable.getName());
                if (!valueByKey.equals("%%%"))
                    list_of_known_variables.add(variable);
            }

            //System.out.println("Known: " + list_of_known_variables);
            //System.out.println("Unknown: " + list_of_unknown_variables);
            //System.out.println("Query: " + queryDetails[0]);

            System.out.println("network's evid: " + network.getVariables());

            Variable temp_query_holder = network.findByName(queryDetails[0]);

            //If A is subset of B and B is subset of A then A==B.
            //The function equals() doesn't work because order is critic.
            if (list_of_known_variables.containsAll(temp_query_holder.getGivens()) && temp_query_holder.getGivens().containsAll(list_of_known_variables)) {
                    String str = "";
                    for (Variable parent : temp_query_holder.getGivens()) {
                        str = str + network.getEvidences().get(parent.getName());
                    }
                    str = str + queryDetails[1];

                    System.out.println("DIDN'T GET INTO ALGO - " + str);
                    System.out.println("p=" + Algorithms.printRoundedDouble(temp_query_holder.getProb(str, network)) + ",0,0");
                    GlobalVars.outputFile.write(Algorithms.printRoundedDouble(temp_query_holder.getProb(str, network)) + ",0,0");
                    GlobalVars.outputFile.newLine();
            }

            else {

                //System.out.println(network + " net: " + network.variables);
                System.out.println("Eviddd " + network.getEvidences());
                for (Variable var : network.variables) {
                    System.out.print(var.name + "'s parents: " + var.givens);

                    if (var.givens.size()>0)
                        if (var.givens.get(var.givens.size()-1).getName().equals(var.name))
                            var.givens.remove(var.givens.get(var.givens.size()-1));

                    System.out.println(" -> " + var.givens);
                }

                switch (chosenAlgo) {
                    case "1":
                        System.out.println("Send to 1");
                        Algorithms.simpleConclusion(network, queryDetails);
                        break;
                    case "2":
                        System.out.println("Send to 2");
                        Algorithms.conclusionByVE(network, queryDetails, '2');
                        //Algo2
                        break;
                    case "3":
                        Algorithms.conclusionByVE(network, queryDetails, '3');
                        System.out.println("Send to 3");
                        //Algo3
                        break;
                }
            }
        }

        System.out.println(network.variables);

        for (Variable variable : network.variables) {
            if (variable.givens.size() == 0)
                System.out.print("(" + variable.name + "), ");
            else
                System.out.print("(" + variable.name + " | " + variable.givens + "), ");
        }

        System.out.println();

        Variable TEMPVAR = network.findByName("C3");
        for (Variable variable : network.variables) {
            if (variable.givens.contains(TEMPVAR) || variable.equals(TEMPVAR))
                System.out.print(variable.name + ", ");
        }


        //List<String> evidences = new ArrayList<>();
        //evidences.add("F");

        //For debugging, print data of a var.
        /*System.out.println(network.variables.get(0).getName());
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        network.variables.get(0).print();
        System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");*/

        //For debugging, print all the vars in the net.
        //network.print();


        //System.out.println(network.variables.get(0).getProb("T", evidences));
        //System.out.println(network.variables.get(1).getProb("T", evidences));
        //System.out.println(network.variables.get(2).getProb("", network.);
        //System.out.println(network.variables.get(3).getProb("T", evidences));
        //System.out.println(network.variables.get(4).getProb("T", evidences));

        GlobalVars.outputFile.flush();
        System.out.println("\nProgram get to its end");
    }
}


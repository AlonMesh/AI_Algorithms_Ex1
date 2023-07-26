import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class Algorithms {
    /**
     * Fills the lists of unknown and known variables based on the query details and evidence in the network.
     * @param network                  The Bayesian network.
     * @param queryDetails             An array containing the details of the query (variable name and evidence value).
     * @param list_of_unknown_variables A list to store unknown variables.
     * @param list_of_known_variables   A list to store known variables.
     */
    static private void fillVarLists(Network network, String[] queryDetails, List<Variable> list_of_unknown_variables, List<Variable> list_of_known_variables) {
        // Iterate through all variables in the network
        for (Variable variable : network.getVariables()) {
            String valueByKey = network.getEvidences().get(variable.getName());

            // Check if the variable has a known evidence value (not "%%%")
            if (!valueByKey.equals("%%%")) {
                list_of_known_variables.add(variable);
            }

            // Check if the variable matches the query variable
            if (variable.getName().equals(queryDetails[0])) {
                // Set the evidence value for the query variable in the network
                network.getEvidences().put(queryDetails[0], queryDetails[1]);
            }

            // Check if the variable has an unknown evidence value (value is "%%%")
            if (valueByKey.equals("%%%") && !(variable.getName().equals(queryDetails[0]))) {
                list_of_unknown_variables.add(variable);
            }
        }
    }

    /**
     * Performs a simple conclusion for a query in the Bayesian network.
     * @param network      The Bayesian network.
     * @param queryDetails An array containing the details of the query (variable name and evidence value).
     * @throws RuntimeException If an I/O error occurs while writing the output.
     */
    public static void simpleConclusion(Network network, String[] queryDetails) {

        List<Variable> list_of_unknown_variables = new ArrayList<>();
        List<Variable> list_of_known_variables = new ArrayList<>();

        fillVarLists(network, queryDetails, list_of_unknown_variables, list_of_known_variables);


        if (network.areThereHiddenVariable()) {
            // If there are no hidden variables, calculate the probability of the query variable given the known variables and evidence directly
            Variable queryVariable = network.findVarByName(queryDetails[0]);
            String queryOutcome = queryDetails[1];

            // Calculate the probability
            double probability = queryVariable.getProb(queryOutcome, network);

            try {
                GlobalVars.outputFile.write(printRoundedDouble(probability) + ",0,0");
                GlobalVars.outputFile.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // Print the score
        } else {
            double[] numerator = getScores(network, list_of_unknown_variables, list_of_unknown_variables);

            list_of_known_variables.remove(network.findVarByName(queryDetails[0]));
            list_of_unknown_variables.add(network.findVarByName(queryDetails[0]));

            double[] denominator = getScores(network, list_of_unknown_variables, list_of_unknown_variables);

            double score = numerator[0] / denominator[0];

            String toPrint = printRoundedDouble(score) + "," + (int) denominator[1] + "," + (int) denominator[2];

            try {
                GlobalVars.outputFile.write(toPrint);
                GlobalVars.outputFile.newLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Formats a double number to a string with 5 decimal places and returns the formatted string.
     * @param number The double number to be formatted.
     * @return The formatted string representation of the input double number with 5 decimal places.
     */
    public static String printRoundedDouble(double number) {
        // Create a DecimalFormat object with 5 decimal places
        DecimalFormat df = new DecimalFormat("0.00000");

        // Use the format() method of the DecimalFormat object to format the double number
        return df.format(number);
    }

    /**
     * Calculates the numerator scores for a given Bayesian network, list of unknown variables, and list of known variables.
     * The numerator scores include probability, total sums, and total scores.
     *
     * @param network     The Bayesian network used for probability calculations.
     * @param unknownVars The list of unknown variables for which numerator scores are calculated.
     * @param knownVars   The list of known variables with fixed values.
     * @return An array of double values representing the numerator scores [probability, total sums, total scores].
     */
    private static double[] getScores(Network network, List<Variable> unknownVars, List<Variable> knownVars) {
        double[] scores = new double[3];
        scores[0] = 0; //probability.
        scores[1] = 0; //total sums.
        scores[2] = 0; //total scores.

        int jumps = unknownVars.size();
        int[] dict = calculateNumOutcomes(unknownVars);
        int[] arr = new int[jumps];

        // Creating a list of all the possible variations of the unknown variables.
        StringBuilder sb = new StringBuilder();
        generator(arr, dict, 0, sb);

        ArrayList<String> variations = new ArrayList<>();
        for (int i = 0; i < sb.length(); i += jumps) {
            variations.add(sb.substring(i, i + jumps));
        }

        for (String variation : variations) {
            double[] current = fill_network_then_return_prob(network, knownVars, variation);
            scores[1]++;
            scores[0] += current[0];
            scores[2] += current[2];
        }

        scores[1]--;
        return scores;
    }

    /**
     * Fills the evidence data in the Bayesian network, then calculates and returns the probability and relevant scores.
     *
     * @param network     The Bayesian network used for probability calculations.
     * @param unknownVars The list of unknown variables for which evidence data is filled.
     * @param str         A string representing the values of the unknown variables to be filled in the evidence data.
     * @return An array of double values representing the probability and scores [probability, total sums, total scores].
     */
    private static double[] fill_network_then_return_prob(Network network,
                                                         List<Variable> unknownVars, String str) {
        double[] scores = new double[3];
        scores[0] = 1; //probability.
        scores[1] = 0; //total sums.
        scores[2] = 0; //total scores.

        LinkedHashMap<String, String> evidences = network.getEvidences();
        int amount_to_add = unknownVars.size();

        // Add the unknown variables, along with their optional values, to the evidence data.
        for (int i = 0; i < amount_to_add; i++) {
            Variable added_variable = unknownVars.get(i);
            String added_variable_value = added_variable.getOutcomes().get(Integer.parseInt(String.valueOf(str.charAt(i))));
            evidences.put(added_variable.getName(), added_variable_value);
        }

        List<Variable> checkedVariables = new ArrayList<>();

        // First, insert the independent variables to checkedVariables and calculate the initial probability.
        for (Variable variable : network.getVariables()) {
            if (variable.getGivens().size() == 0) {

                // I want to avoid the first multiplication, so I'll assign scores[0] as the first variable's probability,
                // instead of multiplying it as 1 * variable's probability.
                if (scores[0] == 1)
                    scores[0] = variable.getProb(network.getEvidences().get(variable.getName()), network);
                else {

                    scores[0] *= variable.getProb(network.getEvidences().get(variable.getName()), network);
                    scores[2]++;
                }
                checkedVariables.add(variable);
            }
        }

        // insert any variable whose parents have already been inserted until all variables are inserted.
        int index1 = 0;
        while (!new HashSet<>(checkedVariables).containsAll(network.getVariables())) {
            Variable variable = network.getVariables().get(index1);

            // checks if variable's parent are found.
            if (new HashSet<>(checkedVariables).containsAll(variable.getGivens()) && !checkedVariables.contains(variable)) {
                StringBuilder strToSend = new StringBuilder();
                for (Variable parent : variable.getGivens()) {
                    strToSend.append(network.getEvidences().get(parent.getName()));
                }

                scores[0] *= variable.getProb(strToSend.toString(), network);
                scores[2]++;

                checkedVariables.add(variable);
            }

            // Circular iteration to avoid infinite loops when some variable's parents are not yet inserted.
            if (index1 == network.getVariables().size() - 1)
                index1 = 0;
            else
                index1++;
        }
        return scores;
    }

    /**
     * Calculate and return the number of outcomes for each variable in the list of unknown variables.
     *
     * @param unknownVars The list of unknown variables for which to calculate the number of outcomes.
     * @return An integer array containing the number of outcomes for each variable in the list.
     */
    private static int[] calculateNumOutcomes(List<Variable> unknownVars) {

        // Create a new integer array called "numOutcomes" with the same number of elements as the size of the list of variables
        int[] numOutcomes = new int[unknownVars.size()];

        // Iterate over each element in the "dict" array
        for (int i = 0; i < numOutcomes.length; i++) {

            // Set the value of the current element in the "numOutcomes" array to the size of the list of outcomes for the corresponding variable in the "list_of_unknown_variables" list
            numOutcomes[i] = unknownVars.get(i).getOutcomes().size();
        }
        // Return the "numOutcomes" array
        return numOutcomes;
    }

    // Output = A StringBuilder that hold all the optional variations outcomes for n variables.
    // for example, when 2 variables has 2 optional outcomes: String builder = "00"+"01"+"10"+"11".
    private static void generator(int[] arr, int[] dict, int index, StringBuilder sb) {
        int digits = arr.length;
        StringBuilder str = new StringBuilder();

        if (index == digits) {
            for (int j : arr) {
                str.append(j);
            }
            sb.append(str);
            return;
        }

        int lim = dict[index];
        for (int i = 0; i < lim; i++) {
            arr[index] = i;
            generator(arr, dict, index + 1, sb);
        }
    }
    //---------end of Algorithm1------------

    /**
     * Joins two factors and stores the result in a new factor.
     *
     * @param f1         The first factor to be joined.
     * @param f2         The second factor to be joined.
     * @param new_factor The new factor where the result of the join operation will be stored.
     * @return The new factor containing the result of the join operation.
     */
    private static Factor join(Factor f1, Factor f2, Factor new_factor) {
        new_factor.cpt = new LinkedHashMap<>();
        // Find all the common variables in f1 and f2
        ArrayList<Variable> commonVariables = new ArrayList<>();
        for (Variable var1 : f1.getContainedList()) {
            for (Variable var2 : f2.getContainedList()) {
                if (var1.getName().equals(var2.getName())) {
                    commonVariables.add(var1);
                }
            }
        }

        // Order f1 and f2's keys and CPT such that the common variables are the leftmost in the keys.
        f1.orderOutcomesByList(commonVariables);
        f2.orderOutcomesByList(commonVariables);

        ArrayList<Variable> restVariables;
        Factor chosenFactor;

        // Determine which factor is smaller and store its list of variables in restVariables
        if (f1.getContainedList().size() < f2.getContainedList().size()) {
            restVariables = new ArrayList<>(f1.getContainedList()); // clone
            chosenFactor = f1;
        } else {
            restVariables = new ArrayList<>(f2.getContainedList());
            chosenFactor = f2;
        }

        // Remove common variables from restVariables
        for (Variable common_variable : commonVariables) {
            restVariables.remove(common_variable);
        }

        // Calculate the number of iterations (jumps) required for the nested loop over restVariables
        int jumper = 1;
        for (Variable variable : restVariables) {
            jumper *= variable.getOutcomes().size();
        }

        // Calculate the length of the common variables' outcomes in the keys (used for substring)
        int lenOfCVinStr = 0;
        for (Variable variable : commonVariables) {
            lenOfCVinStr += variable.getOutcomes().get(0).length();
        }


        int i = 0;
        ArrayList<String> starters = new ArrayList<>();
        while (starters.size() < (chosenFactor.getCpt().size() / jumper)) {
            if (starters.size() < i) {
                System.out.println("Weak warning: not enough common keys");
            }

            // Extract the common key for each unique combination
            String commonKey = chosenFactor.getCpt().keySet().toArray()[i].toString().substring(0, lenOfCVinStr);
            if (!starters.contains(commonKey)) {
                starters.add(commonKey);
            }
            i++;
        }

        // Perform the join operation by combining the permutations of common variables' outcomes from both factors
        for (String commonKey : starters) {
            int commonVariableAmount = commonVariables.size();

            // Slicing VariableA [A3, B3] / {TT=0.5, TF=0.5, FT=0.1, FF=0.9}
            // By multiSlice=4 will give {TT=0.5, TF=0.5}
            // By "TTT".substring(commonVariableAmount) -> TT.

            for (int j = 0; j < (f1.getCpt().size()); j++) {
                for (int k = 0; k < (f2.getCpt().size()); k++) {
                    int slice = commonVariableAmount;
                    for (Variable com : commonVariables) {
                        slice += com.getOutcomes().get(0).length() - 1;
                    }

                    String key1, key2;
                    key1 = f1.getCpt().keySet().toArray()[j].toString().substring(slice);
                    key2 = f2.getCpt().keySet().toArray()[k].toString().substring(slice);

                    double value1 = f1.getCpt().get(commonKey + key1);
                    double value2 = f2.getCpt().get(commonKey + key2);

                    if (!new_factor.getCpt().containsKey((commonKey + key1 + key2))) {
                        new_factor.getCpt().put((commonKey + key1 + key2), value1 * value2);
                        GlobalVars.setGlobalMulti(GlobalVars.getGlobalMulti() + 1);
                    }
                }
            }
        }

        // Add any remaining variables to commonVariables
        for (Variable variable : f1.getContainedList()) {
            if (!commonVariables.contains(variable)) {
                commonVariables.add(variable);
            }
        }

        for (Variable variable : f2.getContainedList()) {
            if (!commonVariables.contains(variable)) {
                commonVariables.add(variable);
            }
        }

        // Set the list of variables in the new factor and return it
        new_factor.setContainedList(commonVariables);
        return new_factor;
    }

    // Define a method called "deepCopyLinkedHashMap" that takes in a linked hash map called "original" and returns a linked hash map
    public static LinkedHashMap<String, Double> deepCopyLinkedHashMap(LinkedHashMap<String, Double> original) {
        LinkedHashMap<String, Double> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : original.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            copy.put(key, value);
        }
        return copy;
    }

    /**
     * Eliminates hidden variables from the list of hidden variables if they are not ancestors of the query or any of the known variables.
     *
     * @param hiddenVariables      The list of hidden variables to be considered for elimination.
     * @param list_of_known_variables The list of known variables in the network.
     * @param query                The query variable in the network.
     */
    private static void eliminateHiddenVariables(ArrayList<Variable> hiddenVariables, ArrayList<Variable> list_of_known_variables, Variable query) {
        // Create a copy of the list of hidden variables to iterate and remove non-ancestor variables
        ArrayList<Variable> hiddenVariablesClone = new ArrayList<>(hiddenVariables);

        // Iterate over the hidden variables
        for (Variable hidden : hiddenVariablesClone) {
            // Check if the hidden variable is an ancestor of the query or any of the known variables
            boolean isAncestor = false;
            for (Variable evidence : list_of_known_variables) {
                if (evidence.isAncestor(hidden)) {
                    isAncestor = true;
                    break;
                }
            }
            if (query.isAncestor(hidden)) {
                isAncestor = true;
            }

            // If the hidden variable is not an ancestor of the query or any of the known variables, eliminate it
            if (!isAncestor) {
                hiddenVariables.remove(hidden);
            }
        }
    }

    /**
     * Creates a list of basic factors based on the given network and variables.
     * @param network    The probabilistic graphical network.
     * @param unknownVars The list of unknown variables in the network.
     * @param knownVars   The list of known variables in the network.
     * @param queryVar   The query variable in the network.
     * @return An ArrayList of basic factors.
     */
    private static ArrayList<Factor> createBasicFactorList(Network network, ArrayList<Variable> unknownVars, ArrayList<Variable> knownVars, Variable queryVar) {
        ArrayList<Factor> factors = new ArrayList<>();

        int index = 1;
        // Iterate over each variable in the network
        for (Variable variable : network.getVariables()) {
            // Check if a variable wasn't eliminated
            if (unknownVars.contains(variable) || knownVars.contains(variable) || queryVar.equals(variable)) {
                // Create a new factor with the current variable and a unique name
                Factor factor = new Factor(variable, ("Factor" + index));
                factors.add(factor);
                index++;
            }
        }

        // For each factor, set factor.containsList to be its parents + the variable it is based on
        for (Factor factor : factors) {
            Variable basedVar = factor.getContainedList().get(factor.getContainedList().size() - 1);
            factor.setContainedList(factor.getGivens());
            factor.getContainedList().add(basedVar);
        }

        return factors;
    }

    /**
     * Switches the factors in the list based on the known variables and their evidence values.
     * @param factors     The list of factors to switch.
     * @param knownVars   The list of known variables in the network.
     * @param evidences   The evidence values for the known variables.
     * @return The updated ArrayList of factors after switching.
     */
    private static ArrayList<Factor> switchFactors(ArrayList<Factor> factors, ArrayList<Variable> knownVars, LinkedHashMap<String, String> evidences) {
        // Create a temporary copy of the factors to avoid modifying the original list directly
        ArrayList<Factor> temp_holder_factors = new ArrayList<>(factors);

        int iterations = factors.size();
        for (int i = 0; i < iterations; i++) {
            // Get the current factor from the list
            Factor factor = factors.get(i);

            // Iterate over the known variables
            for (Variable known_var : knownVars) {
                boolean is_contained_by_name = false;

                // Check if the current known variable is contained in the factor's containedList
                for (int j = 0; j < factor.getContainedList().size(); j++) {
                    Variable variable = factor.getContainedList().get(j);
                    if (variable.getName().equals(known_var.getName())) {
                        is_contained_by_name = true;

                        // Replace the known variable in the factor's containedList with the actual known variable
                        factor.getContainedList().remove(variable);
                        factor.getContainedList().add(j, known_var);
                        break;
                    }
                }

                // Check if the factor's containedList contains the known variable or if it was just replaced by name
                if (factor.getContainedList().contains(known_var) || is_contained_by_name) {
                    // Create a new factor based on the original factor and the temporary factors list
                    Factor new_factor = new Factor(factor, temp_holder_factors);

                    // Get the evidence value for the known variable
                    String outcome = evidences.get(known_var.getName());

                    // Restrict the new factor's CPT based on the known variable's evidence value
                    LinkedHashMap<String, Double> newCpt = factor.restrict(known_var, outcome);

                    // If the new factor's containedList doesn't contain the known variable, clear irrelevant keys from its CPT
                    if (!new_factor.getGivens().contains(known_var)) {
                        factor.clear_ir_char_key(known_var, outcome, newCpt);
                    }

                    // Update the new factor's containedList and CPT
                    new_factor.getContainedList().remove(known_var);
                    new_factor.setCpt(newCpt);

                    // Remove the original factor from the temporary factors list and add the new factor
                    temp_holder_factors.remove(factor);
                    temp_holder_factors.add(new_factor);

                    // Decrement the iterations and break out of the loop to reprocess the updated factors
                    i--;
                    break;
                }
            }
            // Update the factors list with the temporary factors list
            factors = temp_holder_factors;
        }

        return factors;
    }

    /**
     * Joins the factors in the given list based on the specified hidden variable.
     * @param factors    The list of factors to join.
     * @param hiddenVar  The hidden variable based on which factors are joined.
     */
    private static void joinFactorsByHiddenVar(ArrayList<Factor> factors, Variable hiddenVar) {
        // Create a list to store the factors that will be combined based on the hidden variable
        ArrayList<Factor> factorsToCombine = new ArrayList<>();

        // Find all factors that contain the hidden variable and add them to the list
        for (Factor factor : factors) {
            for (Variable varInFactor : factor.getContainedList()) {
                if (varInFactor.getName().equals(hiddenVar.getName())) {
                    factorsToCombine.add(factor);
                    break;
                }
            }
        }

        // If there is only 1 factor in the list to combine, it remains the same.
        // If there are 0, it's irrelevant.
        if (factorsToCombine.size() > 1) {

            // Get the 2 most left factors and join them; put the new factor in the list.
            // Repeat the process until there is only one factor left.
            while (factorsToCombine.size() != 1) {
                // Get the first two factors from the list
                Factor f1 = factorsToCombine.get(0);
                Factor f2 = factorsToCombine.get(1);

                // Join them to create a new factor
                Factor new_factor = join(f1, f2, new Factor(factors));

                // Remove f1 and f2 from the original list and add the new factor instead
                factors.add(new_factor);
                factors.remove(f1);
                factors.remove(f2);

                // Remove f1 and f2 from the "factorsToCombine" list
                factorsToCombine.remove(f1);
                factorsToCombine.remove(f2);

                // Add the new factor to the "factorsToCombine" list
                factorsToCombine.add(new_factor);
            }
        }

        // Now, "factorsToCombine" is sure to contain only one factor that contains the "hiddenVar".
        // Let's eliminate the hidden variable from that factor.

        if (factorsToCombine.size() == 1) {
            // Create a new factor based on the factor with the hidden variable and the original list of factors
            Factor new_factor = new Factor(factorsToCombine.get(0), factors);

            // Eliminate the hidden variable from the new factor
            new_factor.eliminateVarFromFactor(hiddenVar);

            // Add the new factor to the original list and remove the old factor
            factors.add(new_factor);
            factors.remove(factorsToCombine.get(0));
        } else {
            throw new RuntimeException("Error: There are more than one factor in factor_list_to_combine, so it didn't eliminate " + hiddenVar);
        }
    }

    /**
     * Joins the factors in the given list by combining the two most left factors until there is only one factor left.
     * @param factors The list of factors to join.
     */
    private static void joinLeftFactors(ArrayList<Factor> factors) {
        while (factors.size() > 1) {
            // Create a new factor based on the two most left factors in the list
            Factor tempFactor = join(factors.get(0), factors.get(1), new Factor(factors));

            // Remove the two most left factors from the list
            factors.remove(factors.get(0));
            factors.remove(factors.get(0)); // After removing the most left, the next one becomes the most left

            // Add the new factor to the list
            factors.add(tempFactor);
        }
    }

    /**
     * Executes the Variable Elimination (VE) algorithm to compute the probability for the given query.
     * @param network      The Bayesian network.
     * @param queryDetails An array containing the details of the query (variable name and evidence value).
     * @param chosenAlgo   A character representing the chosen algorithm (2 or 3).
     * @throws IOException If an I/O error occurs while writing to the output file.
     */
    public static void conclusionByVE(Network network, String[] queryDetails, char chosenAlgo) throws IOException {

        // Create lists to store unknown and known variables based on the query details
        ArrayList<Variable> unknownVars = new ArrayList<>();
        ArrayList<Variable> knownVars = new ArrayList<>();

        // Fill the unknownVars and knownVars lists based on the query details
        fillVarLists(network, queryDetails, unknownVars, knownVars);

        // Elimination section: Eliminate unknown variables that are not ancestors of known variables or the query variable
        eliminateHiddenVariables(unknownVars, knownVars, network.findVarByName(queryDetails[0]));

        // Create a list of factors where each factor is based on a non-eliminated variable
        ArrayList<Factor> factors = createBasicFactorList(network, unknownVars, knownVars, network.findVarByName(queryDetails[0]));

        // Switch factors by considering known variables' evidences
        factors = switchFactors(factors, knownVars, network.getEvidences());

        // Ordering the expected elimination order based on the chosen algorithm
        if (chosenAlgo == '2') {
            // Algorithm 2: Order by ASCII values of variable names
            orderByASCII(unknownVars);
        } else if (chosenAlgo == '3') { // Algo 3's method
            // Algorithm 3: Order by appearance of variables in the factors
            orderByAppearance(unknownVars, factors);
        } else {
            // Invalid algorithm choice, throw an exception
            throw new IllegalArgumentException("Invalid algorithm choice. Please choose either Algorithm 2 ('2') or Algorithm 3 ('3').");
        }

        // Join section: Join factors based on hidden variables one by one
        for (Variable hiddenVar : unknownVars) {
            joinFactorsByHiddenVar(factors, hiddenVar);
        }

        // Join the two most left factors repeatedly until only one factor is left
        joinLeftFactors(factors);

        // After one factor is left, normalize it
        Factor final_factor = new Factor(factors.get(0), factors);
        final_factor.normalize();

        // Print the scores (probability, total sums, and total multiplies) to the output file
        GlobalVars.outputFile.write((printRoundedDouble(final_factor.getCpt().get(queryDetails[1])) + "," + GlobalVars.getGlobalSum() + "," + GlobalVars.getGlobalMulti()).trim());
        GlobalVars.outputFile.newLine();
    }

    /**
     * Sorts a list of variables based on their names in ascending order using ASCII values for character comparison.
     * @param variables The list of variables to be sorted.
     */
    private static void orderByASCII(ArrayList<Variable> variables) {
        variables.sort((v1, v2) -> {
            String s1 = v1.getName();
            String s2 = v2.getName();
            // Compare the characters of each string
            // until a difference is found
            for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
                int cmp = Character.compare(s1.charAt(i), s2.charAt(i));
                if (cmp != 0) {
                    return cmp;
                }
            }
            // If all characters are the same, compare the length of the strings
            return Integer.compare(s1.length(), s2.length());
        });
    }

    /**
     * Heuristic function for the variable elimination (VE) algorithm.
     * <p>
     * Given a list of variables: v1, v2, ..., vk and a list of factors: f1, f2, ..., fn, where each factor
     * contains one to k variables. The heuristic orders the variables according to their appearance in the factors list
     * in order to minimize the number of calls to the * and +* operations during the VE process.
     * <p>
     * Specifically, if a variable vi appears in p factors, then we will use the *join* operation p-1 times,
     * which involves an increasing exponential amount of multiplications. On the other hand, if a variable vk appears
     * in only one factor, then we can avoid using the *join* operation altogether and use the *eliminate* operation
     * only once. However, if vk is not the first variable in the list, we may have already used the *join* and
     * *eliminate* operations on the factor it appears in, leading to an unnecessarily large number of + and * operations.
     * <p>
     * This function orders the variables such that the variables with the least number of appearances in the factors
     * list come first, minimizing the overall number of + and * operations.
     */
    private static void orderByAppearance(ArrayList<Variable> variables, ArrayList<Factor> factors) {
        // Sort the variables according to the number of factors they appear in
        variables.sort(Comparator.comparingInt(variable -> countFactors(factors, variable)));
    }

    // Helper function to count the number of factors a variable appears in
    private static int countFactors(List<Factor> factors, Variable variable) {
        return (int) factors.stream().filter(f -> f.getContainedList().contains(variable)).count();
    }
}



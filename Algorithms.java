import java.io.IOException;
import java.math.RoundingMode;
import java.sql.SQLOutput;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.DoubleToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class Algorithms {
    //Algorithm 1:
    static public void simpleConclusion(Network network, String[] queryDetails) throws IOException {

        List<Variable> list_of_unknown_variables = new ArrayList<Variable>();
        List<Variable> list_of_known_variables = new ArrayList<Variable>();

        System.out.println("NET: " + network.getVariables());
        for (Variable variable : network.getVariables()) {
            String valueByKey = network.getEvidences().get(variable.getName());

            if (!valueByKey.equals("%%%")) {
                list_of_known_variables.add(variable);
            }
            if (variable.getName().equals(queryDetails[0])) {
                list_of_known_variables.add(variable);
                network.getEvidences().put(queryDetails[0], queryDetails[1]);
            }

            if (valueByKey.equals("%%%") && !(variable.getName().equals(queryDetails[0]))) {
                list_of_unknown_variables.add(variable);
            }
        }

        if (network.getEvidences().values().stream().noneMatch(value -> value.equals("%%%"))) {
            // If there are no hidden variables, calculate the probability of the query variable given the known variables and evidence directly
            Variable queryVariable = network.findByName(queryDetails[0]);
            String queryOutcome = queryDetails[1];
            double probability = queryVariable.getProb(queryOutcome, network);

            // Print the probability
            System.out.println("CATCH IT!!");
            System.out.println("p=" + printRoundedDouble(probability) + ",0,0");
            GlobalVars.outputFile.write(printRoundedDouble(probability) + ",0,0");
            GlobalVars.outputFile.newLine();
        }

        //System.out.println("FirstList:\n" + list_of_known_variables + "\n" + list_of_unknown_variables);

        else {
            System.out.println(network);
            System.out.println(list_of_unknown_variables);
            System.out.println(list_of_known_variables);
            double[] numerator = getNumerator(network, list_of_unknown_variables, list_of_unknown_variables);


            Variable temp_var_for_query = network.findByName(queryDetails[0]);
            List<String> all_outcomes_but_given = temp_var_for_query.getOutcomes();
            //System.out.println("Qua options1: " + temp_var_for_query.outcomes);
            //all_outcomes_but_given.remove(queryDetails[1]);
            //network.findByName(queryDetails[0]).setOutcomes(all_outcomes_but_given);
            //System.out.println("Qua options2: " + temp_var_for_query.outcomes);

            list_of_known_variables.remove(network.findByName(queryDetails[0]));
            list_of_unknown_variables.add(network.findByName(queryDetails[0]));


            System.out.println("secondtime");
            double[] denominator = getNumerator(network, list_of_unknown_variables, list_of_unknown_variables);

            //double curr = numerator / (numerator + denominator);
            double curr = numerator[0] / denominator[0];


//        DecimalFormat format = new DecimalFormat("#.#####");
//        format.setRoundingMode(RoundingMode.HALF_EVEN);

            System.out.println("p=" + printRoundedDouble(curr) + "," + (int) denominator[1] + "," + (int) denominator[2]);
            String toPrint = printRoundedDouble(curr) + "," + (int) denominator[1] + "," + (int) denominator[2];
            GlobalVars.outputFile.write(toPrint);
            GlobalVars.outputFile.newLine();
        }
    }

    public static String printRoundedDouble(double number) {
        // Create a DecimalFormat object with 5 decimal places
        DecimalFormat df = new DecimalFormat("0.00000");

        // Use the format() method of the DecimalFormat object to format the double number
        String formattedNumber = df.format(number);

        // Print the formatted number
        return formattedNumber;
    }

    public static double[] getNumerator(Network network, List<Variable> list_of_unknown_variables, List<Variable> list_of_known_variables) {
        System.out.println("got in");
        double[] scores = new double[3];
        scores[0] = 0; //probability.
        scores[1] = 0; //total sums.
        scores[2] = 0; //total scores.

        int jumps = list_of_unknown_variables.size();
        int[] dict = getDict(list_of_unknown_variables);
        int[] arr = new int[jumps];

        for (int i = 0; i < arr.length; i++) {
            arr[i] = 0;
        }

        //Creating a list of all the possible variations of the unknown variables.
        StringBuilder sb = new StringBuilder();
        generator(arr, dict, 0, sb);
        ArrayList<String> variations = new ArrayList<String>();
        for (int i = 0; i < sb.length(); i+=jumps) {
            variations.add(sb.substring(i, i+jumps));
        }

        int sumCounting = 0;
        double sum = 0;

        for (String variation : variations) {
            sumCounting++;
            double[] current = fill_network_then_return_prob(network, list_of_unknown_variables, list_of_known_variables, variation);
            scores[1]++;
            System.out.println("add to total " + current[0]);
            scores[0] += current[0];
            System.out.println("and now the total is " + scores[0]);
            scores[2] += current[2];
        }

        System.out.println("gg1");

        scores[1]--;
        return scores;
    }

    /*This function get the known data so far, including a String that
    contains all the optional outcomes variations for the unknown variables.
    Then, it "fills" the unknown variables with the current outcomes (given by the String)
    and sent it to another function that calculate the probability.
     */
    public static double[] fill_network_then_return_prob(Network network, List<Variable> list_of_known_variables,
                                                       List<Variable> list_of_unknown_variables, String str) {
        double[] scores = new double[3];
        scores[0] = 1;//probability.
        scores[1] = 0;//total sums.
        scores[2] = 0;//total scores.

        LinkedHashMap<String, String> evidences = network.getEvidences();
        int amount_to_add = list_of_unknown_variables.size();
        //System.out.println("amount_to_add=" + amount_to_add);

        //Add the unknown variable, with their optional value, to the evidences' data.
        for (int i = 0; i < amount_to_add; i++) {
            Variable added_variable = list_of_unknown_variables.get(i);
            String added_variable_value = added_variable.getOutcomes().get(Integer.parseInt(String.valueOf(str.charAt(i))));
            evidences.put(added_variable.getName(), added_variable_value);
        }
        //Now, we have a full network.

        double multi = 1;
        int multiCount = 0;
        List<Variable> checkedVariables = new ArrayList<Variable>();

        //First, Inserting the independent variables to checkedVariables.
        for (Variable variable : network.getVariables()) {
            if (variable.getGivens().size() == 0) {
                System.out.print(variable.getProb(network.getEvidences().get(variable.getName()), network)+"*");

                //I want to avoid the first multiply, so I'll assign scores[0] as the first variable,
                //Instead of multiply it as 1*variable.
                if (scores[0] == 1)
                    scores[0] = variable.getProb(network.getEvidences().get(variable.getName()), network);
                else {
                    multi *= variable.getProb(network.getEvidences().get(variable.getName()), network);
                    multiCount++;

                    scores[0] *= variable.getProb(network.getEvidences().get(variable.getName()), network);
                    scores[2]++;
                }
                checkedVariables.add(variable);
            }
        }

        //Now, Inserting, by while-loop, any variable that his parents was inserted.
        //Till all variables are inserted.
        int index1 = 0;
        while (!checkedVariables.containsAll(network.getVariables())) {
            Variable variable = network.getVariables().get(index1);

            //checks if variable's parent are found.
            if (checkedVariables.containsAll(variable.getGivens()) && !checkedVariables.contains(variable)) {
                String strToSend = "";
                for (Variable parent : variable.getGivens()) {
                    strToSend = strToSend + network.getEvidences().get(parent.getName());
                }
                //System.out.print(variable.getProb(strToSend, network)+"*");
                //System.out.println("Var name: " + variable + ", outcome: " + strToSend + " which equals " + variable.getProb(strToSend, network));
                //System.out.println("Var cpt:  " + variable.cpt);
                multi *= variable.getProb(strToSend, network);
                System.out.println("mmm: " + multi);
                multiCount++;

                scores[0] *= variable.getProb(strToSend, network);
                scores[2]++;

                checkedVariables.add(variable);
            }

            //System.out.println("nnn1 " + checkedVariables);
            //System.out.println("nnn2 " + network.getVariables());

            //System.out.println("gg2");

            if (index1 == network.getVariables().size()-1)
                index1 = 0;
            else
                index1++;
        }
        return scores;
    }


    //-------------------

    //The function get a network which contains full data of all variations.
    //It returns the probability.
    public static double given_fully_network_return_prob(Network network) {
        double multi = 1;
        int multiCount = 0;
        List<Variable> checkedVariables = new ArrayList<Variable>();

        //First, Inserting the independent variables.
        for (Variable variable : network.variables) {
            if (variable.givens.size() == 0) {
                multi *= variable.getProb(network.evidences.get(variable.getName()), network);
                multiCount++;
                checkedVariables.add(variable);
            }
        }

        //Now, Inserting, by while-loop, any variable that his parents was inserted.
        //Till all variables are inserted.
        int index = 0;
        while (!checkedVariables.equals(network.getVariables())) {
            Variable variable = network.getVariables().get(index);

            //checks if variable's parent are found.
            if (checkedVariables.containsAll(variable.getGivens()) && !checkedVariables.contains(variable)) {
                String str = "";
                for (Variable parent : variable.getGivens()) {
                    str = str + network.getEvidences().get(parent.getName());
                }
                multi *= variable.getProb(str, network);
                multiCount++;
                checkedVariables.add(variable);
            }

            if (index == network.getVariables().size()-1)
                index = 0;
            else
                index++;

        }

        System.out.println("Multi Count: " + multiCount);
        return multi;
    }

    //Input = all the unknown variable of a network.
    //Output = an array called dict[], when dict[i] represent the amount of optional outcomes for variable i.
    public static int[] getDict(List<Variable> list_of_unknown_variables) {
        int[] dict = new int[list_of_unknown_variables.size()];

        for (int i = 0; i < dict.length; i++) {
            dict[i] = list_of_unknown_variables.get(i).outcomes.size();
        }
        return dict;
    }

    //Input = Zero's array, dict array (explain: getDict function).
    //Output = A StringBuilder that hold all the optional variations outcomes for n variables.
    //for example, when 2 variables has 2 optional outcomes: String builder = "00"+"01"+"10"+"11".
    public static StringBuilder generator(int[] arr, int[] dict, int index, StringBuilder sb) {
        int digits = arr.length;
        String str = "";

        if (index == digits) {
            for (int i = 0; i < digits; i++) {
                str = str + arr[i];
            }
            sb.append(str);
            return sb;
        }

        int lim = dict[index];
        for (int i = 0; i < lim; i++) {
            arr[index] = i;
            generator(arr, dict, index+1, sb);
        }
        return sb;
    }
    //---------end of Algorithm1------------

    private static ArrayList<Factor> orderByCptSize(ArrayList<Factor> factors) {
        // Create a new ArrayList to store the sorted factors
        ArrayList<Factor> sortedFactors = new ArrayList<>();

        // Use a TreeMap to sort the factors by their cpt size
        TreeMap<Integer, List<Factor>> map = new TreeMap<>();
        for (Factor factor : factors) {
            if (!map.containsKey(factor.cpt.size())) {
                map.put(factor.cpt.size(), new ArrayList<>());
            }
            map.get(factor.cpt.size()).add(factor);
        }

        // Add the sorted factors to the ArrayList
        for (Map.Entry<Integer, List<Factor>> entry : map.entrySet()) {
            sortedFactors.addAll(entry.getValue());
        }

        // Return the sorted ArrayList
        return sortedFactors;
    }

    public static ArrayList<Variable> orderByASCII(ArrayList<Variable> variables) { //Checked only once. NEEDS MORE TESTS.
        Collections.sort(variables, new Comparator<Variable>() {
            @Override
            public int compare(Variable v1, Variable v2) {
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
            }
        });
        return variables;
    }

    public static Factor join(Factor f1, Factor f2, Factor new_factor) {
        new_factor.cpt = new LinkedHashMap<String, Double>();
        // Find all the commonVariables
        List<Variable> commonVariables = new ArrayList<>();
        for (Variable var1 : f1.getContainedList()) {
            for (Variable var2 : f2.getContainedList()) {
                if (var1.getName().equals(var2.getName())) {
                    commonVariables.add(var1);
                }
            }
        }

//        System.out.println("Original f1 (" + f1.getName() + ") " + f1.containedList + " / " + f1.getCpt());
//        System.out.println("Original f2 (" + f2.getName() + ") " + f2.containedList + " / " + f2.getCpt());
//        System.out.println("Common vars: " + commonVariables);

        LinkedHashMap<String, Double> originalCopyCpt1 = deepCopyLinkedHashMap(f1.getCpt()); //(LinkedHashMap<String, Double>) f1.getCpt().clone();
        LinkedHashMap<String, Double> originalCopyCpt2 = deepCopyLinkedHashMap(f2.getCpt());

        // This function order f1 and f2 's keys and CPT such that the common vars are the most-left.
        //System.outRprintln("REORDER CHECK <");
        f1.orderOutcomesByList(commonVariables);
        f2.orderOutcomesByList(commonVariables);
        //System.out.println("REORDER CHECK >");

//        System.out.println("now f1 (" + f1.getName() + ") " + f1.containedList + " / " + f1.getCpt());
//        System.out.println("now f2 (" + f2.getName() + ") " + f2.containedList + " / " + f2.getCpt());


        ArrayList<Variable> restVariables;
        Factor chosenFactor;
        if (f1.getContainedList().size() < f2.getContainedList().size()) {
            restVariables = new ArrayList<Variable>(f1.getContainedList()); //(ArrayList<Variable>) f1.getContainedList().clone();
            chosenFactor = f1;
        }
        else {
            restVariables = new ArrayList<Variable>(f2.getContainedList());
            chosenFactor = f2;
        }

        for (Variable common_variable : commonVariables) {
            restVariables.remove(common_variable);
        }

            int jumper = 1;
            for (Variable variable : restVariables) {
                jumper *= variable.getOutcomes().size();
            }


            int lenOfCVinStr = 0;
            for (Variable variable : commonVariables) {
                lenOfCVinStr += variable.getOutcomes().get(0).length();
            }

        System.out.println("check:");
        System.out.println(f1.containedList);
        System.out.println(f2.containedList);

        //-zzz

        int i = 0;
        ArrayList<String > starters = new ArrayList<>();
        //for (int i = 0; i < chosenFactor.cpt.size(); i += jumper) {
        while (starters.size() < (chosenFactor.cpt.size()/jumper)) {
            if (i > starters.size()) {
                System.out.println("Problem: not enough commonkeys");
            }

            String commonKey = chosenFactor.getCpt().keySet().toArray()[i].toString().substring(0, lenOfCVinStr);
            if (!starters.contains(commonKey)) {
                starters.add(commonKey);
            }
            i++;
        }

        System.out.println("Jumper is " + jumper);
        System.out.println("known cpt: " + chosenFactor.getCpt());
        for (String starter : starters) {
            System.out.println(starter);
        }

        //-zzz

        for (String commonKey : starters) {
//        for (int i = 0; i < chosenFactor.cpt.size(); i += jumper) {
//            //for (int i = 0; i <= jumper; i++) {
//            String commonKey = chosenFactor.getCpt().keySet().toArray()[i].toString().substring(0, lenOfCVinStr);
            System.out.println(chosenFactor.getCpt());
            //System.out.println(i + " j=" + jumper + " CK: " + commonKey + " cut " + chosenFactor.getCpt().keySet().toArray()[i].toString() + " in indexes 0-" + lenOfCVinStr + " to " + commonKey);
            /*
             * ASSUME THAT [A,B,C] AND [A,B,D,E] ARE ABOUT TO JOIN.
             * i've got all permutations of the common variables [A,B].
             * i need to get all permutations of [C] and all permutations of [D,E]
             * Combining perm(A,B)+perm(C) will allow me to pull of f1.cpt.get(key) when key is any combination.
             * the same for perm(A,B)+perm(D,E).
             * then I can insert it to the new cpt by:
             * for ex: [A,B,C]=>tft=0.1, [A,B,D,E]=>"tfft"=0.5. now [A,B,C,D,E] => "tftft"=0.05.
             */

            int commonVariableAmount = commonVariables.size();

            //int multiSlice = 1;
            //for (Variable variable : commonVariables) {
            //multiSlice *= variable.getOutcomes().size();
            //} //Ex: Slicing {TTT=0.3, TTF=0.7, TFT=0.45, TFF=0.55, FTT=0.6, FTF=0.4, FFT=0.15, FFF=0.85}
            //By multiSlice=4 will give {TTT=0.3, TTF=0.7, TFT=0.45, TFF=0.55}
            //By "TTT".substring(commonVariableAmount) -> TT.

            for (int j = 0; j < (f1.getCpt().size()); j++) {
                for (int k = 0; k < (f2.getCpt().size()); k++) {
                    /* Explaining where I went by Ex:
                     * (Factor7) [A3, B3] / {TT=0.5, TF=0.5, FT=0.1, FF=0.9}
                     * (Factor19) [A3, C1, D1] / {TTT=0.3, TTF=0.7, TFT=0.45, TFF=0.55, FTT=0.6, FTF=0.4, FFT=0.15, FFF=0.85}
                     * The j for will run on TT, TF and will just hold it as T, F
                     * The k for will run on TTT,...,TFF and will just hold it as TT,TF,FT,FF.
                     */

                    int slice = commonVariableAmount;

                    for (Variable com : commonVariables) {
                        slice += com.outcomes.get(0).length() - 1;
                    }
//                        System.out.println(commonVariableAmount + " slice is " + slice);

                    String key1 = "", key2 = "";
                    String key1cpt = "", key2cpt = "";
                    key1 = f1.getCpt().keySet().toArray()[j].toString().substring(slice);
                    key1cpt = key1;
                    key2 = f2.getCpt().keySet().toArray()[k].toString().substring(slice);
                    key2cpt = key2;

                    if (key1.length() == 0) {
                        key1cpt = f1.getCpt().keySet().toArray()[j].toString();
                    }

                    if (key2.length() == 0) {
                        key2cpt = f1.getCpt().keySet().toArray()[k].toString();
                    }

//                        System.out.println("Commonkey " + commonKey);
//                        System.out.println("key1: " + key1 + " key1cpt: " + key1cpt);
//                        System.out.println("key2: " + key2 + " key1cpt: " + key2cpt);

//                        System.out.println(f1.getCpt().get(commonKey + key1));
//                        System.out.println(f2.getCpt().get(commonKey + key2));


//                        System.out.println("Let's combine1: " + f1);
//                        System.out.println("Let's combine2: " + f2);

//                        System.out.println("Final1: " + (commonKey + key1));
//                        System.out.println("Final2: " + (commonKey + key2));


                    double value1 = f1.getCpt().get(commonKey + key1);
                    double value2 = f2.getCpt().get(commonKey + key2);

                    //System.out.println("Put: <key=" + (commonKey + key1 + key2) + ">, value=" + (value1*value2));
                    if (!new_factor.getCpt().containsKey((commonKey + key1 + key2))) {
                        new_factor.cpt.put((commonKey + key1 + key2), value1 * value2); // I shall sum that^^
                        System.out.println("Multi: " + value1 + " * " + value2);
                        GlobalVars.setGlobalMulti(GlobalVars.getGlobalMulti() + 1);
                    }
                }
            }
        }

        ArrayList<Variable> newOrder = (ArrayList<Variable>) commonVariables;

            for (Variable variable : f1.getContainedList()) {
                if (!newOrder.contains(variable)) {
                    newOrder.add(variable);
                }
            }

        for (Variable variable : f2.getContainedList()) {
            if (!newOrder.contains(variable)) {
                newOrder.add(variable);
            }
        }

        new_factor.containedList = newOrder;
        //System.out.println("New ORDER: " + newOrder);
        return new_factor;
    }

    public static LinkedHashMap<String, Double> deepCopyLinkedHashMap(LinkedHashMap<String, Double> original) {
        LinkedHashMap<String, Double> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : original.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            copy.put(key, value);
        }
        return copy;
    }



    public static void eliminateHiddenVariables(ArrayList<Variable> list_of_hidden_variables, ArrayList<Variable> list_of_known_variables, Variable query) {
        // Create a copy of the list of hidden variables
        ArrayList<Variable> hiddenVariables = new ArrayList<>(list_of_hidden_variables);

        // Iterate over the hidden variables
        for (Variable hidden : hiddenVariables) {
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
                list_of_hidden_variables.remove(hidden);
            }
        }
    }

    public static void sortLHM(LinkedHashMap<String, Double> originalBackUp, LinkedHashMap<String, Double> newCpt) {
        LinkedHashMap<String, Double> sortedCpt = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : originalBackUp.entrySet()) {
            String key = entry.getKey();
            if (newCpt.containsKey(key)) {
                Double value = newCpt.get(key);
                sortedCpt.put(key, value);
            }
        }
        newCpt.clear();
        newCpt.putAll(sortedCpt);
    }



    public static void conclusionByVE(Network network, String[] queryDetails, char chosenAlgo) throws IOException {
        System.out.println("GOT HERE, algo2");

        ArrayList<Variable> list_of_unknown_variables = new ArrayList<Variable>();
        ArrayList<Variable> list_of_known_variables = new ArrayList<Variable>();

        System.out.println("Eviddd " + network.getEvidences());

        for (Variable variable : network.getVariables()) {
            String valueByKey = network.getEvidences().get(variable.getName());

            if (!valueByKey.equals("%%%")) {
                list_of_known_variables.add(variable);
            }
            if (variable.getName().equals(queryDetails[0])) {
                //list_of_known_variables.add(variable);
                network.getEvidences().put(queryDetails[0], queryDetails[1]);
            }

            if (valueByKey.equals("%%%") && !(variable.getName().equals(queryDetails[0]))) {
                list_of_unknown_variables.add(variable);
            }
        }

                /* Eliminate every hidden variable that is not an ancestor

              X
              |
              Y
             / \
            Q   Z

            For Ex: if i want to calculate X=t given Q=f, Z doesn't matter cuz
            I'll calculate any scenario of x,y,q when z=T and z=F (and p(z=T)+p(z=F)=1)
        */

        ArrayList<Variable> list_of_vars_tobe_eliminated = new ArrayList<>(list_of_unknown_variables);

        System.out.println("Eliminate every hidden which isn't a ancestor of query/evidence");
        System.out.println("All hidden: " + list_of_unknown_variables);
        eliminateHiddenVariables(list_of_vars_tobe_eliminated, list_of_known_variables, network.findByName(queryDetails[0]));
        System.out.println("The vars that should be eliminated: " + list_of_vars_tobe_eliminated);
        list_of_unknown_variables = list_of_vars_tobe_eliminated;
        System.out.println("Now we have: " + list_of_unknown_variables);
        System.out.println("Query: " + queryDetails[0]);
        //----

        System.out.println("Known list: " + list_of_known_variables);
        ArrayList<Factor> factors = new ArrayList<Factor>();
        int index = 1;

        for (Variable variable : network.getVariables()) {
            if (list_of_unknown_variables.contains(variable) || list_of_known_variables.contains(variable) || variable.getName().equals(queryDetails[0])) {
                ArrayList<Variable> containedList = new ArrayList<Variable>();
                containedList.addAll(variable.getGivens());
                containedList.add(variable);
                Factor factor = new Factor(variable, containedList, ("Factor" + index));
                factors.add(factor);
                index++;
            }
        }



        System.out.println("Factors before rest: " + factors.size());
        for (Factor factor : factors) {
            System.out.println(factor.toString() + " // cpt.size: " + factor.getCpt().size());
            //System.out.println(factor.containedList);
        }

        // For each factor, set factor.containsList to be its parents + the Var it is based on.
        // That way we can work well with that list.

        for (Factor factor : factors) {
            Variable basedVar = factor.containedList.get(factor.containedList.size() - 1);
            //System.out.println(basedVar.getName() + " yyyy");
            factor.containedList = (ArrayList<Variable>) factor.getGivens();
            factor.containedList.add(basedVar);
        }

//        ArrayList<Variable> temp_holder_for_variables = new ArrayList<Variable>();
//        // Eliminate every variable that isn't evidence or query, or their parent (including ancestor).
//        // Get a list of the variables that weren't eliminated.
//        for (Variable variable : network.getVariables()) {
//            if (variable.is_son(list_of_known_variables) || list_of_known_variables.contains(variable)) {
//                temp_holder_for_variables.add(variable);
//            }
//        }
//
//        Make a factor for each variable in out network.
//        Factor will be based on Variable class.
//        int index = 1;
//        ArrayList<Factor> factors = new ArrayList<Factor>();
//        for (Variable variable : temp_holder_for_variables) {
//            Factor factor = new Factor(variable, ("Factor" + index));
//            factors.add(factor);
//            index++;
//        }
//
//        System.out.println("DB: Unknown variables: " + list_of_unknown_variables);
//        System.out.println("DB: known variables:   " + list_of_known_variables);
//
//        System.out.println("DB: Evidence is: " + network.getEvidences());

        LinkedHashMap<String, String> evidences = network.getEvidences();
        ArrayList<Factor> temp_holder_factors = new ArrayList<Factor>(factors);
        ArrayList<Factor> advancedList = new ArrayList<Factor>();

        System.out.println("LIST OF KNOWN: " + list_of_known_variables);


        list_of_known_variables = orderByASCII(list_of_known_variables);
        System.out.println("and now:       " + list_of_known_variables);

        int iterations = factors.size();


        for (int i = 0; i < iterations; i++) {
            Factor factor = factors.get(i);
            //System.out.println("This index = " + i + " and factors.size = " + factors.size());

            for (Variable known_var : list_of_known_variables) {

                //System.out.println("CHECK " + factor.name + ": " + factor.containedList + " / " + known_var);
                boolean is_contained_by_name = false;

                for (int j = 0; j < factor.getContainedList().size(); j++) {
                    Variable variable = factor.getContainedList().get(j);
                    if (variable.getName().equals(known_var.getName())) {
                        is_contained_by_name = true;

                        factor.containedList.remove(variable);
                        factor.containedList.add(j, known_var);
                    }
                }

                System.out.println(is_contained_by_name);

                // Check if "factor" contains "known_var"
                if (factor.getContainedList().contains(known_var) || is_contained_by_name) {
                    // Start a process to switch "factor" with a new factor that doesn't contain "known_var".
                    // First by creating a new factor, based on "factor".
                    Factor new_factor = new Factor(factor, temp_holder_factors);

                    String outcome = evidences.get(known_var.getName());

                    // Updating its cpt by restricting "known_var".
                    LinkedHashMap<String, Double> newCpt = new LinkedHashMap<String, Double>();
                    newCpt = factor.restrict(known_var, outcome);

                    //if (new_factor.getGivens().indexOf(known_var) == -1 && Integer.parseInt(factor.getName().substring(6)) <= iterations && new_factor.containedList.size()>0) {
                    if (!new_factor.getGivens().contains(known_var)) {
                        newCpt = factor.clear_ir_char_key(known_var, outcome, newCpt);
                    }

                    //System.out.print(factor.name + ": " + new_factor.containedList + " becomes-> ");
                    new_factor.containedList.remove(known_var); // "new_factor" won't contain "known_var" anymore.
                    //System.out.println(new_factor.containedList + " (" + known_var.getName() + "=" + outcome + ")");
                    //System.out.print(new_factor.cpt + " -> ");
                    new_factor.setCpt(newCpt);
                    //System.out.println(new_factor.cpt);


                    // Adding "new_factor" to the list and remove "factor".
                    temp_holder_factors.remove(factor);
                    temp_holder_factors.add(new_factor);
                    i--;
                    break;
                }


            }
            //System.out.println(factor.getName() + ": " + factor.cpt);
            factors = temp_holder_factors;
        }

        ArrayList<Factor> cloneList = new ArrayList<>(temp_holder_factors);

        for (Factor factor : cloneList) {
            if (factor.getCpt().size()==1) {
                temp_holder_factors.remove(factor);
            }
        }

        System.out.println("Factors after restrict: " + temp_holder_factors.size());
        for (Factor factor : temp_holder_factors) {
            System.out.println(factor.toString() + " / Cpt.size = " + factor.getCpt().size() + " / " + factor.getGivens());
        }

        ArrayList<Variable> hiddenVariableList = new ArrayList<>(list_of_unknown_variables);


        // >end any elimination of hidden variables which are not ancestor of query or evidences.

        System.out.println("Keep going");
        System.out.println("List of unknown: " + list_of_unknown_variables);

        if (chosenAlgo == '2') {
            System.out.println("order by abc");
            list_of_unknown_variables = orderByASCII(list_of_unknown_variables);
        }
        if (chosenAlgo == '3') {
            System.out.println("order by heuristic");
            orderByAppearance(list_of_unknown_variables, temp_holder_factors);
        }
        System.out.println("List of unknown: " + list_of_unknown_variables);

        ArrayList<Factor> factor_list_to_combine = new ArrayList<Factor>();
        ArrayList<String> factor_list_to_combine_names = new ArrayList<String>(); //useless
        ArrayList<Factor> holder_for_factors = new ArrayList<>(factors);

        // ### Join section
        for (Variable hidden_variable : list_of_unknown_variables) {

            factor_list_to_combine.clear();
            factor_list_to_combine_names.clear();

            for (int i = 0; i < holder_for_factors.size(); i++) {
                Factor factor = holder_for_factors.get(i);
                System.out.println(factor.getName() + ": " + factor.getContainedList());


                // For each var in a factor - if it's equaled to "hidden_variable" then add it to "list_to_combine".
                // List to combine will use for joining factors later.
                for (Variable varInFactor : factor.getContainedList()) {
                    if (varInFactor.getName().equals(hidden_variable.getName())) {
//                        System.out.println(varInFactor.getName() + "=" + hidden_variable.getName());
                        factor_list_to_combine.add(factor);
                        factor_list_to_combine_names.add(factor.getName());
                        break;
                    }
                }
            }

            System.out.println("Working with " + hidden_variable.getName() + " has " + factor_list_to_combine.size() + " factors to combine");
            // If there is only 1 factor in list to combine, it'll will remain the same.
            //  If there is 0, it's irrelevant.
            if (factor_list_to_combine.size() > 1) {

                //System.out.println("THIS ELSE ");
                //System.out.println("dv: " + factor_list_to_combine_names + " total " + factor_list_to_combine.size());
                //System.out.println("dv: " + factor_list_to_combine);

                // Let's run on any 2 adjacent factors in "list_to_combine".\
                Factor f1 = null;
                Factor f2 = null;
                //for (int i = 0; i < factor_list_to_combine.size() ; i++) {
                while (factor_list_to_combine.size() != 1) {

                    System.out.println("Not order: ");
                    for (Factor factor : factor_list_to_combine) {
                        System.out.println(factor.getName() + ": " + factor.cpt + " , total " + factor.cpt.size());
                    }
                    factor_list_to_combine = orderByCptSize(factor_list_to_combine);
                    System.out.println("Order: ");
                    for (Factor factor : factor_list_to_combine) {
                        System.out.println(factor.getName() + ": " + factor.cpt + " , total " + factor.cpt.size());
                    }

                    // Declare on those 2 adjacent factors.
                    f1 = factor_list_to_combine.get(0);
                    f2 = factor_list_to_combine.get(1);

                    Factor new_factor = new Factor(holder_for_factors);

                    System.out.println("Let's combine1: " + f1.cpt.size() + ": " + f1);
                    System.out.println("Let's combine1: " + f2.cpt.size() + ": " + f2);
                    System.out.println("total mulites supposed to be " + f1.cpt.size());

                    System.out.println("hhh1.0 " + new_factor);
                    new_factor = join(f1, f2, new_factor);

                    int index_of_hidden_inCL = new_factor.containedList.indexOf(hidden_variable.getName());
//                    System.out.println(hidden_variable + " is in place " + index_of_hidden_inCL);


                    //****
                    // For case when CL=[... , B, B, ...] when B=hidden_variable. Now check from left and from right.
//                    if (new_factor.getContainedList().size() > index_of_hidden_inCL + 1) {
//                        if (new_factor.getContainedList().get(index_of_hidden_inCL + 1).getName().equals(hidden_variable.getName())) {
//                            System.out.println("bett1");
//                            new_factor.marginalization(index_of_hidden_inCL); // updates cpt
//                            new_factor.containedList.remove(hidden_variable);
//                        }
//                    }
//                        if (index_of_hidden_inCL > 0 && new_factor.getContainedList().get(index_of_hidden_inCL-1).getName().equals(hidden_variable.getName())) {
//                            System.out.println("bett2");
//                            new_factor.marginalization(index_of_hidden_inCL); // updates cpt
//                            new_factor.containedList.remove(hidden_variable);
//                        }


                    System.out.println("hhh1.1 " + new_factor + " after " + hidden_variable);
                    System.out.println("Now remove " + f1.getName() + ", " + f2.getName());

                    holder_for_factors.add(new_factor);
                    holder_for_factors.remove(f1);
                    holder_for_factors.remove(f2);
                    factor_list_to_combine.remove(f1);
                    factor_list_to_combine.remove(f2);
                    factor_list_to_combine.add(new_factor);
                    //i--;
                    System.out.println("will " + factor_list_to_combine.size() + ">" + 1 + " ?");
                }

                //...
            }

            // Now out list is sure to contain only one factor that contains "hidden_variable".
            // Let's eliminate it.

            if (factor_list_to_combine.size() == 1) {
                Factor new_factor = new Factor(factor_list_to_combine.get(0), holder_for_factors);
                new_factor.eliminateVarFromFactor(hidden_variable);
                holder_for_factors.add(new_factor);
                holder_for_factors.remove(factor_list_to_combine.get(0));
            }
            else {
                System.out.println("Error: There are more than one factor in factor_list_to_combine, so it didn't eliminate " + hidden_variable);
            }
        }

//        System.out.println("FACTOR HOLDER");
//        for (Factor factor : holder_for_factors) {
//            System.out.println(factor);
//        }

        Factor temp;
        while (holder_for_factors.size() > 1) {
            holder_for_factors = orderByCptSize(holder_for_factors); // Make sure that the little factors will be joined first.
            temp = new Factor(holder_for_factors);
            temp = join(holder_for_factors.get(0), holder_for_factors.get(1), temp);
            System.out.println(temp);
            holder_for_factors.remove(holder_for_factors.get(0));
            holder_for_factors.remove(holder_for_factors.get(0));
            holder_for_factors.add(temp);
        }

        Factor final_factor = holder_for_factors.get(0);

        // Avoiding normalize a factor that its cpt values are equal already to 1.
        //if (holder_for_factors.get(0).getCpt().values().stream().mapToDouble(Double::doubleValue).sum() != 1) {
            final_factor = new Factor(holder_for_factors.get(0), holder_for_factors);
            final_factor.normalize();
        //}

        System.out.println("FACTOR HOLDER - normalized");
        System.out.println(final_factor);

        //System.out.println("LastFactor? " + holder_for_factors.get(0));
        System.out.println("2.p=" + printRoundedDouble(final_factor.getCpt().get(queryDetails[1])) + "," + GlobalVars.getGlobalSum() + "," + GlobalVars.getGlobalMulti());
        GlobalVars.outputFile.write((printRoundedDouble(final_factor.getCpt().get(queryDetails[1])) + "," + GlobalVars.getGlobalSum() + "," + GlobalVars.getGlobalMulti()).trim());
        GlobalVars.outputFile.newLine();
    }

    /**
     * Heuristic function for the variable elimination (VE) algorithm.
     *
     * Given a list of variables: v1, v2, ..., vk and a list of factors: f1, f2, ..., fn, where each factor
     * contains one to k variables. The heuristic orders the variables according to their appearance in the factors list
     * in order to minimize the number of calls to the * and +* operations during the VE process.
     *
     * Specifically, if a variable vi appears in p factors, then we will use the *join* operation p-1 times,
     * which involves an increasing exponential amount of multiplications. On the other hand, if a variable vk appears
     * in only one factor, then we can avoid using the *join* operation altogether and use the *eliminate* operation
     * only once. However, if vk is not the first variable in the list, we may have already used the *join* and
     * *eliminate* operations on the factor it appears in, leading to an unnecessarily large number of + and * operations.
     *
     * This function orders the variables such that the variables with the least number of appearances in the factors
     * list come first, minimizing the overall number of + and * operations.
     */
    public static void orderByAppearance (ArrayList<Variable> variables, ArrayList<Factor> factors) {
        // Sort the variables according to the number of factors they appear in
        Collections.sort(variables, (variable1, variable2) -> Integer.compare(countFactors(factors, variable1), countFactors(factors, variable2)));
    }

    // Helper function to count the number of factors a variable appears in
    private static int countFactors(List<Factor> factors, Variable variable) {
        return (int) factors.stream().filter(f -> f.containedList.contains(variable)).count();
    }
}



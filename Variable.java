import java.util.*;

/**
 * Represents a variable in a Bayesian network.
 * <p>
 * A variable in a Bayesian network is a random variable or node that represents different factors or events of interest.
 * Each variable can have one or more possible outcomes, also known as states. The relationships between variables are
 * represented by conditional probability tables (CPTs) that define the probability of each outcome based on the values
 * of its parent variables.
 */
public class Variable {
    protected String name; // The name of the variable
    private ArrayList<String> outcomes; // The possible outcomes or states of the variable
    private ArrayList<Double> values; // The values associated with each outcome
    private ArrayList<Variable> givens; // The parent variables on which this variable depends
    protected LinkedHashMap<String, Double> cpt; // The conditional probability table for the variable

    /**
     * Default constructor for the Variable class.
     * Initializes the variable fields to empty values.
     */
    public Variable() {
        String name = "";
        List<String> outcomes = new ArrayList<>();
        List<Variable> givens = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        LinkedHashMap<String, Double> cpt = new LinkedHashMap<>();
    }

    /**
     * Copy constructor for the Variable class.
     * Creates a new Variable object by copying the values from another Variable object.
     *
     * @param other The Variable object to be copied.
     */
    public Variable(Variable other) {
        this.name = other.getName();
        this.outcomes = other.getOutcomes();
        this.givens = other.getGivens();
        this.values = other.getValues();
        this.cpt = other.getCpt();
    }

    /**
     * Constructor for the Variable class with parameters.
     * Creates a new Variable object with the specified name, outcomes, values, and parent variables.
     *
     * @param name The name of the variable.
     * @param outcomes The possible outcomes or states of the variable.
     * @param values The values associated with each outcome.
     * @param givens The parent variables on which this variable depends.
     */
    public Variable(String name, ArrayList<String> outcomes, ArrayList<Double> values, ArrayList<Variable> givens) {
        this.name = name;
        this.outcomes = outcomes;
        this.values = values;
        this.givens = givens;
        updateCPT();
    }


    /**
     * Updates the conditional probability table (CPT) for the variable based on its outcomes, values, and parent variables (givens).
     * If the variable has no parent variables (givenAmount == 0), it creates a simple CPT where each outcome is mapped to its corresponding value.
     * If the variable has parent variables, it creates a more complex CPT by combining the outcomes of the parent variables to build the key for the CPT entries.
     */
    public void updateCPT() {
        LinkedHashMap<String, Double> CPT = new LinkedHashMap<>();

        String currOutcome;
        Double currValue;

        int givenAmount = this.givens.size();

        if (givenAmount == 0) {
            for (int i = 0; i < this.outcomes.size(); i++) {
                currOutcome = this.outcomes.get(i);
                currValue = this.values.get(i);
                CPT.put(currOutcome, currValue);
            }

        } else {
        /*  Rational:
            Multiply each given's outcome by all other given's outcomes.
            so we will have some strings that combine optional outcomes.
            We don't pop probability in this function, only build/update the CPT.

            Example:
            var has 3 givens: father (one, two, three), mother (t, f) and son (v1, v2, v3)
            while father=two
                  mother=t
                  son=v3.
            so we will check the key "twotv3", which return the double value in that index.
        */

            int jumps = 1;

            for (Variable given : this.givens) {
                jumps *= given.outcomes.size();
            }

            String[] options = new String[jumps];

            Arrays.fill(options, "");

            int count = 1;

            for (Variable given : this.givens) {
                jumps = jumps / given.outcomes.size();
                count *= given.outcomes.size();
                int currIndex = 0;
                for (int i = 0; i < count; i++) { //*2
                    for (int j = 0; j < jumps; j++) {
                        options[currIndex] = options[currIndex] + given.outcomes.get(i % given.outcomes.size());
                        currIndex++;
                    }
                }
            }

            List<String> tempOptionsList = Arrays.stream(options).toList();
            List<Double> tempValuesList = this.values;

            int place = 0;
            for (String s : tempOptionsList) {
                for (String outcome : this.outcomes) {
                    CPT.put(s + outcome, tempValuesList.get(place));
                    place++;
                }
            }
        }

        // At the end of (if/else), set cpt
        this.cpt = CPT;
    }

    /**
     * Checks if the current variable is a descendant (son) of any of the ancestor variables in the given list.
     * It does this by recursively checking if any of its parent variables are in the list of ancestors.
     *
     * @param ancestors A list of ancestor variables to check.
     * @return true if the current variable is a descendant of any of the ancestors, false otherwise.
     */
    public boolean is_son(List<Variable> ancestors) {
        // Check if the list of ancestors is empty, in which case the variable cannot have an ancestor in the list
        if (ancestors.isEmpty()) {
            return false;
        }

        // Get the list of parents for the variable
        List<Variable> parents = this.getGivens();

        // Check if any of the parents are in the list of ancestors
        for (Variable parent : parents) {
            if (ancestors.contains(parent)) {
                // If the variable has a parent in the list of ancestors, return true
                return true;
            }

            // Recursively check the ancestors of the parent
            if (parent.is_son(ancestors)) {
                return true;
            }
        }

        // If the variable does not have any ancestors in the list of ancestors, return false
        return false;
    }

    /**
     * Returns a list of all the ancestors of the variable in the network.
     *
     * @return An ArrayList containing all the ancestor variables of the current variable.
     */
    public ArrayList<Variable> getAncestors() {
        // Create an empty list of ancestors
        ArrayList<Variable> ancestors = new ArrayList<>();

        // Get the list of parents for the variable
        List<Variable> parents = this.getGivens();


        // Check if the variable has any parents
        if (!parents.isEmpty()) {
            // If the variable has parents, add them to the list of ancestors
            ancestors.addAll(parents);

            // Recursively check the ancestors of the parents
            for (Variable parent : parents) {
                ancestors.addAll(parent.getAncestors());
            }
        }

        // Return the list of ancestors
        return ancestors;
    }

    /**
     * Checks if the other variable is an ancestor of the current variable.
     *
     * @param other The other Variable object to check for ancestor relationship.
     * @return true if the other variable is an ancestor of the current variable, false otherwise.
     */
    public boolean isAncestor(Variable other) {
        // Check if the other variable is an ancestor of this variable
        for (Variable ancestor : this.getAncestors()) {
            if (ancestor.equals(other)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Calculates and returns the probability of a given string representation of outcomes for the variable.
     * If this variable has no parent variables (givens), it directly looks up the probability in the CPT based on the provided string.
     * If the variable has parent variables, it builds the key for the CPT using the values of the given variables stored in the evidences field of the Network.
     *
     * @param str     The string representation of outcomes for which to calculate the probability.
     * @param network The Network object containing the evidences used to build the key for CPT (in case of parent variables).
     * @return The probability value corresponding to the provided string in the CPT.
     */
    public double getProb(String str, Network network) {

        // If this variable has no givens, return the probability value corresponding to str in the cpt
        if (this.givens.size() == 0) {
            if (this.cpt.get(str) == null) {
                for (String name : this.cpt.keySet()) {
                    if (str.contains(name)) {
                        return this.cpt.get(name);
                    }
                }
            }

            return this.cpt.get(str);
        }

        if (this.cpt.get(str) != null)
            return this.cpt.get(str);

        // else (which means: this.givens.size() > 0)

        // If this variable has givens,
        // Build the key for the cpt using the values of the given variables as stored in the
        // evidences field of the Network

        StringBuilder strBuilder = new StringBuilder();
        for (Variable variable : this.givens) {
            strBuilder.append(network.getEvidences().get(variable.getName()));
        }


        for (String outcome : this.outcomes) {
            if (network.getEvidences().get(this.name).equals(outcome)) {
                strBuilder.append(outcome);
            }
        }
        return this.cpt.get(strBuilder.toString());
    }

    /**
     * Prints information about the variable, including its name, outcomes, parent variables (givens), and values.
     */
    public void print() {
        System.out.println("Variable " + this.name);
        System.out.println("has " + this.outcomes.size() + " out comes: " + outcomes);
        System.out.println("has " + this.givens.size() + " parents: " + givens);
        System.out.println("values: " + this.values);
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getOutcomes() {
        return outcomes;
    }

    public ArrayList<Variable> getGivens() {
        return givens;
    }

    public ArrayList<Double> getValues() {
        return values;
    }

    public LinkedHashMap<String, Double> getCpt() {
        return cpt;
    }

    public void setCpt(LinkedHashMap<String, Double> cpt) {
        this.cpt = cpt;
    }

}

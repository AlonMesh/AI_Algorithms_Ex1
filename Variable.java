import java.util.*;
import java.util.stream.Collectors;

public class Variable {
    protected String name;
    private List<String> outcomes; // The options for each variable
    private List<Double> values; // Values of each outcome (and even more)
    private List<Variable> givens; // If variable depends on another variables, list them. //add()
    protected LinkedHashMap<String, Double> cpt;

    public Variable() {
        String name = "";
        List<String> outcomes = new ArrayList<String>();
        List<Variable> givens = new ArrayList<Variable>();
        List<Double> values = new ArrayList<Double>();
        LinkedHashMap<String, Double> cpt = new LinkedHashMap<String, Double>();
    }

    public Variable(Variable other) {
        this.name = other.getName();
        this.outcomes = other.getOutcomes();
        this.givens = other.getGivens();
        this.values = other.getValues();
        this.cpt = other.getCpt();
    }

    //If we get all parameters but cpt, build (update) the cpt.
    public Variable(String name, List<String> outcomes, List<Double> values, List<Variable> givens) {
        this.name = name;
        this.outcomes = outcomes;
        this.values = values;
        this.givens = givens;
        this.cpt = this.updateCPT();
    }

    //set values for the list values;
    public LinkedHashMap<String, Double> updateCPT() {
        LinkedHashMap<String, Double> CPT = new LinkedHashMap<String, Double>();

        String currOutcome;
        Double currValue;

        int givenAmount = this.givens.size();

        if (givenAmount == 0) {
            for (int i = 0; i < this.outcomes.size(); i++) {
                currOutcome = this.outcomes.get(i);
                currValue = this.values.get(i);
                CPT.put(currOutcome, currValue);
            }

            return CPT;
        }

        //else:
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

        for (int i = 0; i < options.length; i++) {
            options[i] = "";
        }

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

        List<String> tempOptionsList = Arrays.stream(options).collect(Collectors.toList());
        List<Double> tempValuesList = this.values;

        int place = 0;
        for (int i = 0; i < tempOptionsList.size(); i++) {
            for (int j = 0; j < this.outcomes.size(); j++) {
                CPT.put(tempOptionsList.get(i) + (this.outcomes.get(j)), tempValuesList.get(place));
                place++;
            }
        }

        return CPT;
    }

    // This method takes another Variable as an argument and returns true if the other variable is an ancestor of the current variable, and false otherwise.
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

    // This method returns a list of all the ancestors of the variable in the network
    public ArrayList<Variable> getAncestors() {
        // Create an empty list of ancestors
        ArrayList<Variable> ancestors = new ArrayList<Variable>();

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

    public boolean isAncestor(Variable other) {
        // Check if the other variable is an ancestor of this variable
        for (Variable ancestor : this.getAncestors()) {
            if (ancestor.equals(other)) {
                return true;
            }
        }
        return false;
    }


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

        //else (which means -> this.givens.size() > 0)

        // If this variable has givens,
        // Build the key for the cpt using the values of the given variables as stored in the
        // evidences field of the Network

        str = "";

        for (Variable variable : this.givens) {
            str = str + network.getEvidences().get(variable.getName());
        }


        for (int i = 0; i < this.outcomes.size() ; i++) {
            if (network.getEvidences().get(this.name).equals(this.outcomes.get(i))) {
                str = str + this.outcomes.get(i);
            }
        }
        return this.cpt.get(str);
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getOutcomes() {
        return outcomes;
    }

    public void setOutcomes(List<String> outcomes) {
        this.outcomes = outcomes;
    }

    public List<Variable> getGivens() {
        return givens;
    }

    public void setGivens(List<Variable> givens) {
        this.givens = givens;
    }

    public List<Double> getValues() {
        return values;
    }

    public void setValues(List<Double> values) {
        this.values = values;
    }

    public LinkedHashMap<String, Double> getCpt() {
        return cpt;
    }

    public void setCpt(LinkedHashMap<String, Double> cpt) {
        this.cpt = cpt;
    }

}

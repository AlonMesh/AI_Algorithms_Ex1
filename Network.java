import java.util.*;

public class Network {
    public List<Variable> variables; //All variables in our network
    public LinkedHashMap<String, String> evidences;

    public Network() {
        this.variables = new ArrayList<Variable>();
        this.evidences = new LinkedHashMap<String, String>();
    }

    public Network (Network other) {
        this.variables = other.getVariables();
        this.evidences = other.getEvidences();
    }

    public void addVariable(Variable variable) {
        this.variables.add(variables.size(), variable);
    }

    public Variable findByName(String str) {
        //Input: A string, as the name of existing Variable in net
        //Output: Variable named by the string.
        int index = -1;

        for (int i = 0; i < this.variables.size(); i++) {
            if (this.variables.get(i).getName().equals(str)) {
                index = i;
            }
        }

        if (index == -1) {
            //System.out.println("There isn't Variable named \"" + str + "\" at this net");
            return null;
        }

        return this.variables.get(index);
    }

/*
    //Calculation
    public double findNumerator(HashMap<String, String> evidence_map, String[] quarry) {
        //String[] quarry - array of 2 places, quarry[0] = given name
        //                                     quarry[1] = given outcome

        int baseMulti = 1;
        int counterMulti = 1, counterSum = 1;

        for (Variable variable : this.variables) {
            if ( evidence_map.get(variable.name).equals("%%%") && !variable.name.equals(quarry[0]) )
                counterSum *= variable.outcomes.size();
        }

       counterSum -= 1;



        for (Variable variable : this.variables) {
            if (!evidence_map.get(variable.name).equals("%%%")) {
                for (String outcome : variable.outcomes)
                baseMulti *= variable.getProb(evidence_map.get(variable.name)); //i need to getProb to 2 functions.
            }
            else {
                base multi * all the outcomes options.
            }
        }


        int sum = 0;

        //network = B J M E A
       //evidence = T T T N N //N = null
        //N = for when i<VAR.OUTCOME, another N = another inner for.

        //if value == T (or F) then getProb(String outcome)
        //else then getProb(evidence);



    }

    public double findDenominator() {

    }


 */

    //getter and setter
    public List<Variable> getVariables() {
        return variables;
    }

    public List<String> getVariablesNames() {
        List<String> names = new ArrayList<>(this.variables.size());

        for (int i = 0; i < this.variables.size(); i++) {
            names.add(this.variables.get(i).name);
        }
        return names;
    }


    public void setVariables(List<Variable> variables) {
        this.variables = variables;
    }

    public void print() {
        for (int i = 0; i < this.variables.size(); i++) {
            this.variables.get(i).print();
        }
        System.out.print("Evidence map: ");
        System.out.println(this.evidences.toString());
    }

    public LinkedHashMap<String, String> getEvidences() {
        return this.evidences;
    }

    public void setEvidences(LinkedHashMap<String, String> evidences) {
        this.evidences = evidences;
    }
}

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

public class Factor extends Variable {
    public ArrayList<Variable> containedList;

    public Factor(ArrayList<Factor> factorArrayList) {
        super(new Variable());

        String lastName = factorArrayList.get(factorArrayList.size()-1).getName();
        lastName = lastName.substring((6)); //"FactorXY" -> "XY"
        int foo = Integer.parseInt(lastName);
        foo++;
        this.name = "Factor" + foo;

        this.containedList = new ArrayList<Variable>();
    }


    public Factor(Factor other, ArrayList<Factor> factorArrayList) {
        super(other);

        String lastName = factorArrayList.get(factorArrayList.size()-1).getName();
        lastName = lastName.substring((6)); //"FactorXY" -> "XY"
        int foo = Integer.parseInt(lastName);
        foo++;
        this.name = "Factor" + foo;

        this.containedList = new ArrayList<Variable>(other.getContainedList()); //(ArrayList<Variable>) other.getContainedList().clone();
    }

    public Factor(Variable variable, ArrayList<Variable> containedList, String name) {
        super(variable);

        this.name = name;
        this.containedList = new ArrayList<Variable>();

        for (Variable parent : variable.givens)
            this.containedList.add(new Variable(parent));

        this.containedList.add(new Variable(variable));

    }
    public Factor(Factor other) {
        super(other);

        this.cpt = other.getCpt();
        this.containedList = other.getContainedList();
    }

    public LinkedHashMap<String, Double> marge(Variable irrelevant_variable) {
        LinkedHashMap<String, Double> newCpt = new LinkedHashMap<String, Double>();

        int irrelevant_variable_index = this.containedList.indexOf(irrelevant_variable);
        for (String oldKey : this.cpt.keySet()) {
            String newkey = oldKey.substring(0, irrelevant_variable_index) + oldKey.substring(irrelevant_variable_index+1);

            if (newCpt.containsKey(newkey)) {
                newCpt.put(newkey, (newCpt.get(newkey)+this.cpt.get(oldKey)));
            }

            else {
                newCpt.put(newkey, this.cpt.get(oldKey));
            }
        }
        return newCpt;
    }

        public LinkedHashMap<String, Double> restrict(Variable known_var, String chosenOutcome) {
            int index_of_given_parent = -2;
            int index_of_given_outcome = -1;

            String newKey = "";
            LinkedHashMap<String, Double> lhm = this.getCpt(); // lhm = LinkedHashMap

            if (this.getGivens() == null) {
                index_of_given_outcome = 0;
            }
            else {
                index_of_given_parent = this.getGivens().indexOf(known_var);
            }

            // If the known_var is contained in factor.ContainedList (=-1) but doesn't in factor's parents,
            // It means that this.factor is based on known_var. therefor it holds the last char.
            if (index_of_given_parent == -1) {
                // Find the index of outcome the chosen parent.
                for (int i = 0; i < known_var.outcomes.size(); i++) {
                    if (chosenOutcome.equals(known_var.outcomes.get(i)))
                        index_of_given_outcome = i;
                }

                if (index_of_given_outcome == -1)
                    System.out.println("Illegal outcome.");

                else {
                    LinkedHashMap<String, Double> tempLHM = Algorithms.deepCopyLinkedHashMap(lhm);
                    int amount_of_outcomes = known_var.outcomes.size();
                    //Remove any line that doesn't equal to the given outcome.
                    for (int i = 0; i < lhm.size(); i++) {
                        if (i % amount_of_outcomes != index_of_given_outcome) {
                            List<String> keyList = lhm.keySet().stream().collect(Collectors.toList());
                            tempLHM.remove(keyList.get(i));
                        }
                    }
                    lhm = tempLHM;
                }
            }

            else { //If a parent's value is given.
                index_of_given_parent = 0;

                for (int i=0; i < this.containedList.size(); i++) {
                    if ((known_var.getName().equals(this.containedList.get(i).getName()))) {
                        index_of_given_parent += i;
                        break;
                    }
                    index_of_given_parent += this.containedList.get(i).outcomes.get(0).length() - 1; //If A=T then +1-1=0, but if A=v1 then +2-1=+1
                }

                if (index_of_given_parent == -1)
                    System.out.println("THE PARENT " + known_var + " IS ILLEGAL");


                LinkedHashMap<String, Double> tempLHM = Algorithms.deepCopyLinkedHashMap(lhm);


                //Now let's remove
                for (int i = 0; i < lhm.size(); i++) {
                    List<String> keyList = lhm.keySet().stream().collect(Collectors.toList());
                    if (!keyList.get(i).startsWith(chosenOutcome, index_of_given_parent)) {
                        tempLHM.remove(keyList.get(i));
                    }

                }
                lhm = tempLHM;
                for (int i = 0; i < tempLHM.size(); i++) {
                    newKey = "";
                    List<String> keyList = lhm.keySet().stream().collect(Collectors.toList());
                    String oldKey = keyList.get(0);
                    newKey = oldKey.substring(0, index_of_given_parent) + oldKey.substring((index_of_given_parent+chosenOutcome.length()));

                    lhm.put(newKey, lhm.get(oldKey));
                    lhm.remove(oldKey);
                }
                lhm = tempLHM;
            }
        return lhm;
    }

    // This function removes an outcome that appears in a factor.
    public LinkedHashMap<String, Double> clear_ir_char_key(Variable variable, String outcome, LinkedHashMap<String, Double> lhm) {
        int indexBeg = 0, temp = 0;

        if (this.getContainedList().contains(variable)) {
            temp = this.getContainedList().indexOf(variable);

            // If factorX contains [A, B, C, D] and variable is C, then sum outcome.length of A,B.
            for (int i = 0; i < temp; i++) {
                indexBeg += this.getContainedList().get(i).getOutcomes().get(0).length();
            }

        }
        else {
            for (int i = 0; i < this.getGivens().size(); i++) {
                indexBeg += this.getGivens().get(i).getOutcomes().get(0).length();
            }
            indexBeg++;
        }

        int indexEnd = indexBeg + outcome.length();

        Set<String> newKeys = lhm.keySet();
        LinkedHashMap<String, Double> cloneLHM = Algorithms.deepCopyLinkedHashMap(lhm);

        for (String key : newKeys) {
            String newKey = key.substring(0, indexBeg) + key.substring(indexEnd);
            cloneLHM.put(newKey, lhm.get(key));
            cloneLHM.remove(key);
        }
        return cloneLHM;
    }

    //Given a list like [C, B] and this.contains is [A, B, C, D]
    //Order this.contains list to be [C, B, A, D]
    public void orderOutcomesByList(List<Variable> orderList) {
        List<Variable> rest = new ArrayList<Variable>(this.getContainedList());

        for (Variable variable1 : orderList) {
            for (Variable variable2 : this.getContainedList()) {
                if (variable1.getName().equals(variable2.getName())) {
                    rest.remove(variable2);
                }
            }
        }

        /*
        * Creating varArr when:
        * i = index
        * varArr[i] = variable in place i.
         */

        ArrayList<Variable> newCL = new ArrayList<Variable>();
        for (Variable variable : orderList) {
            newCL.add(variable);
        }
        for (Variable variable : rest) {
            newCL.add(variable);
        }

        LinkedHashMap<String, Double> newCpt = Algorithms.deepCopyLinkedHashMap(this.getCpt());
        LinkedHashMap<String, Double> originalBackUp = Algorithms.deepCopyLinkedHashMap(newCpt);

        newCpt.clear();
        // Changing each key in cpt.
        for (String key : this.getCpt().keySet()) {
            // Turn each key to Array (then we'll order it and return it to String).

            int startIndex = 0;
            int[] arr = new int[this.getContainedList().size()];
            for (int i = 0; i < this.getContainedList().size(); i++) {
                arr[i] = startIndex;
                startIndex = arr[i] + this.containedList.get(i).getOutcomes().get(0).length();
            }

            String[] brr = new String[this.getContainedList().size()];
            for (int i = 0; i < this.getContainedList().size(); i++) {
                if (i == this.getContainedList().size() - 1) {
                    brr[i] = key.substring(arr[i]);
                } else {
                    brr[i] = key.substring(arr[i], arr[i + 1]);
                }
            }

            String[] crr = new String[this.getContainedList().size()];
            int originalIndex;
            int newIndex = 0;

            for (Variable variable : orderList) {
                originalIndex = this.getNamesOfContainedList().indexOf(variable.name);
                crr[newIndex] = brr[originalIndex];
                brr[originalIndex] = "%%%";
                newIndex++;
            }


            for (String oc : brr) {
                if (!oc.equals("%%%")) {
                    crr[newIndex] = oc;
                    newIndex++;
                }
            }

            String newKey = "";
            for (int i = 0; i < this.getContainedList().size(); i++) {
                newKey = newKey + crr[i];
            }

            newCpt.put(newKey, this.getCpt().get(key));
        }

        this.containedList = newCL;
        this.cpt = newCpt;
    }

    public void eliminateVarFromFactor(Variable variable) {
        /* To eliminate a var from Factor we need to:
         * Remove it from containedList
         * Remove it from Cpt in the right way.
        */

        if (!this.getNamesOfContainedList().contains(variable.getName()))
            System.out.println("Error: The variable " + variable + " is not in " + this.getName());

        int var_outcome_len = variable.getOutcomes().get(0).length(); //2
        int index_in_CL = this.getNamesOfContainedList().indexOf(variable.getName()); //1
        int pureIndex = index_in_CL;

        // Now let's calculate its index in cpt by finding the lens of each outcome before it. //v1v2T
        for (int i = 0; i < index_in_CL; i++) {
            pureIndex += this.getContainedList().get(i).getOutcomes().get(0).length() - 1; // for (0 < 1) -> 1+2-1=2
        }

        // Update cpt:
        LinkedHashMap<String, Double> newCpt = new LinkedHashMap<String, Double>();
        for (String key : this.getCpt().keySet()) {
            String newKey = key.substring(0, pureIndex) + key.substring(pureIndex+var_outcome_len); // i shall sum that.^^

            if (newCpt.containsKey(newKey)) {
                double newVal = newCpt.get(newKey) + this.getCpt().get(key);
                GlobalVars.setGlobalSum(GlobalVars.getGlobalSum()+1);
                newCpt.put(newKey, newVal);
            }
            else {
                newCpt.put(newKey, this.getCpt().get(key));
            }
        }
        this.cpt = newCpt;

        // Update CL:
        if (this.getContainedList().contains(variable))
            this.containedList.remove(variable);
        else {
            int index = this.getNamesOfContainedList().indexOf(variable.getName());
            this.containedList.remove(index);
        }
    }

    public void normalize() {
        double totalSum = 0;

        for (String key : this.getCpt().keySet()) {
            if (totalSum == 0) {
                totalSum = this.getCpt().get(key);
            }
            else {
                totalSum += this.getCpt().get(key);
                GlobalVars.setGlobalSum(GlobalVars.getGlobalSum() + 1);
            }
        }

        for (String key : this.getCpt().keySet()) {
            this.cpt.put(key, (this.getCpt().get(key)/totalSum));
        }
    }
    public ArrayList<String> getNamesOfContainedList() {
        ArrayList<String> namesOfContainedList = new ArrayList<String>();
        for (Variable variable : this.getContainedList()) {
            namesOfContainedList.add(variable.getName());
        }
        return namesOfContainedList;
    }

    public ArrayList<Variable> getContainedList() {
        return containedList;
    }

    public void setContainedList(ArrayList<Variable> containedList) {
        this.containedList = containedList;
    }

    public String toString() {
        return this.getName() + "'s contained list is: " + this.getContainedList() + " and cpt: " + this.getCpt();
    }
}

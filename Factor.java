import java.awt.image.AreaAveragingScaleFilter;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

public class Factor extends Variable {
    public ArrayList<Variable> containedList;

    public Factor(Variable variable, ArrayList<Factor> factorArrayList) {
        //super(new Variable(variable));
        super(variable);

        String lastName = factorArrayList.get(factorArrayList.size()-1).getName();
        lastName = lastName.substring((6)); //"FactorXY" -> "XY"
        int foo = Integer.parseInt(lastName);
        foo++;
        this.name = "Factor" + foo;

        this.containedList = new ArrayList<Variable>();
        if (variable.getName().length() < 7) //Avoid adding FactorX.
            this.containedList.add(new Variable(variable));
        for (Variable parent : variable.givens)
            this.containedList.add(new Variable(parent));
    }

    public Factor(ArrayList<Factor> factorArrayList) {
        //super(new Variable(variable));
        super(new Variable());

        String lastName = factorArrayList.get(factorArrayList.size()-1).getName();
        lastName = lastName.substring((6)); //"FactorXY" -> "XY"
        int foo = Integer.parseInt(lastName);
        foo++;
        this.name = "Factor" + foo;

        this.containedList = new ArrayList<Variable>();
    }


    public Factor(Factor other, ArrayList<Factor> factorArrayList) {
        //super(new Variable(other));
        super(other);

        String lastName = factorArrayList.get(factorArrayList.size()-1).getName();
        lastName = lastName.substring((6)); //"FactorXY" -> "XY"
        int foo = Integer.parseInt(lastName);
        foo++;
        this.name = "Factor" + foo;

        this.containedList = new ArrayList<Variable>(other.getContainedList()); //(ArrayList<Variable>) other.getContainedList().clone();
    }

    public Factor(Variable variable, String name) {
        //super(new Variable(variable));
        super(variable);

        this.name = name;
        this.containedList = new ArrayList<Variable>();
        if (variable.getName().length() < 7) //Avoid adding FactorX.
            this.containedList.add(new Variable(variable));
        for (Variable parent : variable.givens)
            this.containedList.add(new Variable(parent));
    }

    public Factor(Variable variable, ArrayList<Variable> containedList, String name) {
        //super(new Variable(variable));
        super(variable);

        this.name = name;
        //this.containedList = containedList;
        this.containedList = new ArrayList<Variable>();

        for (Variable parent : variable.givens)
            this.containedList.add(new Variable(parent));

        //System.out.println("Last var: " + variable.getName());
        this.containedList.add(new Variable(variable));

    }
    public Factor(Factor other) {
        //super(new Variable(other));
        super(other);

        this.cpt = other.getCpt();
        this.containedList = other.getContainedList();
    }

    public Factor() {
        super(new Variable());
        this.containedList = new ArrayList<Variable>();
    }

    public boolean isFactorContainsVariable(Variable variable) {
        return this.containedList.contains(variable);
    }

//    public Factor sum_out_var_from_factor(Variable variable) {
//        if (!this.containedList.equals(variable))
//            System.out.println("THAT FACTOR DOESN'T CONTAIN " + variable.name);
//        //keep going
//
//    }

    public LinkedHashMap<String, Double> marge(Variable irrelevant_variable) {
        LinkedHashMap<String, Double> newCpt = new LinkedHashMap<String, Double>();

//        System.out.println("This.name = " + this.name + " , " + this.containedList);
//
//        System.out.println("TS: ire var is " + irrelevant_variable.getName());
//        System.out.println("TS: containedList is " + this.containedList);

        int irrelevant_variable_index = this.containedList.indexOf(irrelevant_variable);
        for (String oldKey : this.cpt.keySet()) {
//            System.out.println("TS: oldKey = " + oldKey);
//            System.out.println("TS: ire = " + irrelevant_variable + ", index = " + irrelevant_variable_index);
            String newkey = oldKey.substring(0, irrelevant_variable_index) + oldKey.substring(irrelevant_variable_index+1);
//            System.out.println("TS: newKey = " + newkey);

            if (newCpt.containsKey(newkey)) { //THIS + SHOULD BE COUNTED.
                newCpt.put(newkey, (newCpt.get(newkey)+this.cpt.get(oldKey)));
            }

            else { //(newCpt.containsKey(newkey) == false)
                newCpt.put(newkey, this.cpt.get(oldKey));
            }
        }
//        System.out.println("TS: NEW CPT IS " + newCpt);
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
//                System.out.println(this.getName() + " got to inner if");
                // Find the index of outcome the chosen parent.
//                System.out.println("WE ARE IN IF");
                for (int i = 0; i < known_var.outcomes.size(); i++) {
                    if (chosenOutcome.equals(known_var.outcomes.get(i)))
                        index_of_given_outcome = i;
                }

//                System.out.println("index of chosen outcome: " + index_of_given_outcome);

                if (index_of_given_outcome == -1)
                    System.out.println("Illegal outcome.");

                else {
                    LinkedHashMap<String, Double> tempLHM = Algorithms.deepCopyLinkedHashMap(lhm);
                    int amount_of_outcomes = known_var.outcomes.size();
                    //Remove any line that doesn't equal to the given outcome.
                    for (int i = 0; i < lhm.size(); i++) {
                        if (i % amount_of_outcomes != index_of_given_outcome) {
//                            System.out.println(i + "%" + this.outcomes.size() +"=="+ index_of_given_outcome);
//                            System.out.println("lets throw away " + lhm.keySet().stream().toList().get(i));
                            //tempLHM.remove(lhm.keySet().stream().toList().get(i));
                            List<String> keyList = lhm.keySet().stream().collect(Collectors.toList());
                            tempLHM.remove(keyList.get(i));
                        }
                    }
                    lhm = tempLHM;
                }
            }

            else { //If a parent's value is given.
//                System.out.println(this.getName() + " got to inner else");
                index_of_given_parent = 0;

                for (int i=0; i < this.containedList.size(); i++) {
                    if ((known_var.getName().equals(this.containedList.get(i).getName()))) {
                        index_of_given_parent += i;
                        break;
                    }
                    index_of_given_parent += this.containedList.get(i).outcomes.get(0).length() - 1; //If A=T then +1-1=0, but if A=v1 then +2-1=+1
                }

                //System.out.println("The deleted parent: " + this.givens.get(index_of_given_parent) + " which in place " + index_of_given_parent + "-" + (index_of_given_parent+chosenOutcome.length()));

                if (index_of_given_parent == -1)
                    System.out.println("THE PARENT " + known_var + " IS ILLEGAL");


                LinkedHashMap<String, Double> tempLHM = Algorithms.deepCopyLinkedHashMap(lhm);
                //int outComeSize = this.outcomes.get(index_of_given_outcome).length();

                //System.out.println("His outcome " + this.outcomes.get(index_of_given_outcome) + " has length in the key of " + outComeSize);

//                System.out.println("WE ARE IN ELSE, deleting " + chosenOutcome + " in place " + index_of_given_parent + "-" + (index_of_given_parent+chosenOutcome.length()));
//                System.out.println(this.cpt);
//                System.out.println(this.containedList);

                //Now let's remove
                //System.out.println("Stage0 " + tempLHM);
                for (int i = 0; i < lhm.size(); i++) {
                    //System.out.println("Key=" + lhm.keySet().stream().toList().get(i));
                    List<String> keyList = lhm.keySet().stream().collect(Collectors.toList());
                    if (!keyList.get(i).startsWith(chosenOutcome, index_of_given_parent)) {
                        tempLHM.remove(keyList.get(i));
                    }

                }
                lhm = tempLHM;
                for (int i = 0; i < tempLHM.size(); i++) {

                    //System.out.println("TEMP: " + tempLHM);
                    //System.out.println("GIVEN INDEX = " + index_of_given_parent + "-" + (index_of_given_parent+chosenOutcome.length()));
                    newKey = "";
                    List<String> keyList = lhm.keySet().stream().collect(Collectors.toList());
                    String oldKey = keyList.get(0);
                    //System.out.print("OLDKEY2 = " + oldKey);
                    newKey = oldKey.substring(0, index_of_given_parent) + oldKey.substring((index_of_given_parent+chosenOutcome.length()));

                    //System.out.println(" and NEWKEY2 = " + newKey);
                    //System.out.println("DB: new " + newKey);
                    lhm.put(newKey, lhm.get(oldKey));
                    lhm.remove(oldKey);
                }
                //System.out.println("qq " + tempLHM);
                lhm = tempLHM;
            }
        return lhm;
    }



    public LinkedHashMap<String, Double> clear_ir_char_key(Variable variable, String outcome, LinkedHashMap<String, Double> lhm) {
        int indexBeg = 0, temp = 0;
//        System.out.println("ARE YOU HERE??");
//        System.out.println("Factor: " + this.getName() + ", contains " + this.getGivens() + " , outcome: " + this.getOutcomes());
//        System.out.println("Var:    " + variable.getName() + ", has " + outcome + " / " + variable.outcomes);
//        System.out.println("qq: " + lhm);

        if (this.getContainedList().contains(variable)) {
            System.out.println("INNER IF");
            temp = this.getContainedList().indexOf(variable);

            // If factorX contains [A, B, C, D] and variable is C, then sum outcome.length of A,B.
            for (int i = 0; i < temp; i++) {
                System.out.println(this.getContainedList().get(i).getOutcomes().get(0) + " in size " + this.getContainedList().get(i).getOutcomes().get(0).length());
                indexBeg += this.getContainedList().get(i).getOutcomes().get(0).length();
                //System.out.println(indexBeg);
            }
            //indexBeg++;

        }
        else {
            System.out.println("INNER ELSE");
//            System.out.println("Parents: " + this.getGivens() + " , total " + this.getGivens().size());
//            System.out.println("Contained: " + this.getContainedList() + " , total " + this.getContainedList().size());
            for (int i = 0; i < this.getGivens().size(); i++) {
                indexBeg += this.getGivens().get(i).getOutcomes().get(0).length();
                //System.out.println(indexBeg); // The variable itself is in the last char of the key.
            }
            indexBeg++;
        }

        int indexEnd = indexBeg + outcome.length();
        System.out.println("Index to be removed: " + indexBeg + "-" + indexEnd);

        Set<String> newKeys = lhm.keySet();
        LinkedHashMap<String, Double> cloneLHM = Algorithms.deepCopyLinkedHashMap(lhm);

        for (String key : newKeys) {
            System.out.println("key = " + key);
            String newKey = key.substring(0, indexBeg) + key.substring(indexEnd);
            System.out.println("new = " + newKey);
            cloneLHM.put(newKey, lhm.get(key));
            cloneLHM.remove(key);
        }
        return cloneLHM;
    }

    public Factor production(ArrayList<Factor> factors, ArrayList<Factor> originalList) {
        Factor toReturn = new Factor(this, originalList);
        LinkedHashMap<String, Double> newCpt = new LinkedHashMap<String, Double>();

        // In case that factors contains only this.factor
        if (factors.size() == 1) {
            System.out.println("innner if");
            //this.containedList.clear();

            // When a factor doesn't contain variables, it becomes a pure number.
            double sum = 0.0;
            for (String key : toReturn.cpt.keySet()) {
                sum += toReturn.cpt.get(key);
            }
            toReturn.cpt = new LinkedHashMap<String, Double>();
            toReturn.cpt.put("number", sum); // "number" - a keycode that I'll use when multiplying.

            return toReturn;
        }
        System.out.println("innner else" + factors.get(1).name + " " + factors.get(1).containedList);

        ArrayList<Variable> f1CL = new ArrayList<Variable>();
        ArrayList<Variable> f2CL = new ArrayList<Variable>();
        ArrayList<Variable> common_variables = new ArrayList<Variable>();
        LinkedHashMap<String, Double> newList = new LinkedHashMap<String, Double>();

        toReturn.containedList.clear();

        System.out.println("vvv " + toReturn.name + ": " + toReturn.containedList);
        for (Factor f : factors) {
            System.out.println("vvv " + f.name + ": " + f.containedList);
        }

        for (int i = 0; i < factors.size()-1; i++) {
            Factor f1 = factors.get(i);
            Factor f2 = factors.get(i+1);

            f1CL = f1.getContainedList();
            f2CL = f2.getContainedList();

            // Make sure f1.containedList will be smaller.
            if (f1.containedList.size() > f2.containedList.size()) {
                Factor temp = f1;
                f1 = f2;
                f2 = temp;
            }

            for (int j = 0; j < f1.containedList.size(); j++) {
                if (f2.containedList.contains(f1.containedList.get(j))) {
                    common_variables.add(f1.containedList.get(j));
                }
            }

            // Now take care of this.contained list by the same order: [common, f1.containedlist, f2.containedlist]
            for (Variable variable : common_variables) {
                if (!toReturn.containedList.contains(variable))
                    toReturn.containedList.add(variable);
            }

            for (Variable variable : f1CL) {
                if (!toReturn.containedList.contains(variable))
                    toReturn.containedList.add(variable);
            }

            for (Variable variable : f2CL) {
                if (!toReturn.containedList.contains(variable))
                    toReturn.containedList.add(variable);
            }

            System.out.println("ppp " + f1CL + ", " + f2CL + ", Common vars: " + common_variables);

            // Now we have all common variable between those two factors.
            for (Variable common_variable : common_variables) {
                for (String outcome : common_variable.outcomes) {
                    Factor f1Copy = new Factor(f1);
                    Factor f2Copy = new Factor(f2);
                    LinkedHashMap<String, Double> f1Cpt = f1Copy.restrict(common_variable, outcome);
                    LinkedHashMap<String, Double> f2Cpt = f2Copy.restrict(common_variable, outcome);
                    System.out.println("f1 for outcome " + outcome + ": " + f1Cpt);
                    System.out.println("f2 for outcome " + outcome + ": " + f2Cpt);
                    for (String oc1 : f1Cpt.keySet()) {
                        for (String oc2 : f2Cpt.keySet()) {
                            String key = outcome + oc1 + oc2; //T+v1+TT -> Tv1TT, then Tv1TF...
                            double value = f1Cpt.get(oc1) * f2Cpt.get(oc2); //i shall sum that.
                            newList.put(key, value);
                        }
                    }
                }
            }
        }
        toReturn.cpt = newList;

        System.out.println("ppp " + toReturn.containedList + " / " + toReturn.cpt);

        System.out.println("common_variables.size()="+common_variables.size() + ": " + common_variables);
        for (int i = 0; i < common_variables.size(); i++) {
            toReturn = new Factor(toReturn.marginalization(), originalList);
            toReturn.containedList.remove(0);
        }

        return toReturn;
    }

    // This function Updates a factor's cpt - it removes one variable from each key
    // then it sums the reaming keys and if they're the same - combine it.
    // Ex: When index=1, cpt = {TTT=0.1, ... , FTT=0.3, ... , FFF=0.9} -> {TT=0.1, ... , TT=0.3, ... , FF=0.9} -> {TT=0.4, ...}
    public Factor marginalization() {
        LinkedHashMap<String, Double> newCpt = new LinkedHashMap<String, Double>();
        LinkedHashMap<String, Double> originalCpt = this.getCpt();

        for (String key : originalCpt.keySet()) {
            String newKey = key.substring(1); //The common variable is always the 0'index. //WHAT IF ITS V1??? it won't work
            double value = originalCpt.get(key);

            // If this combination doesn't exist, insert it.
            if (!newCpt.containsKey(newKey)) {
                newCpt.put(newKey, value);
            }
            else { // This combination was already inserted.
                newCpt.put(newKey, value+newCpt.get(newKey));
            }
        }
        this.cpt = newCpt;
        return this;
    }

    //Given a list like [C, B] and this.contains is [A, B, C, D]
    //Order this.contains list to be [C, B, A, D]
    public void orderOutcomesByList(List<Variable> orderList) {
//        System.out.println("ORDER LIST: " + orderList);
        List<Variable> rest = new ArrayList<Variable>(this.getContainedList());

        for (Variable variable1 : orderList) {
            for (Variable variable2 : this.getContainedList()) {
                if (variable1.getName().equals(variable2.getName())) {
                    rest.remove(variable2);
                }
            }
        }
//        System.out.println("Rest: " + rest);
//        System.out.println("TOTAL: " + this.getContainedList());
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
        //System.out.println(this.getCpt().keySet());
        for (String key : this.getCpt().keySet()) {
            // Turn each key to Array (then we'll order it and return it to String).
            //System.out.println("s1");

            int startIndex = 0;
            int[] arr = new int[this.getContainedList().size()];
            for (int i = 0; i < this.getContainedList().size(); i++) {
                arr[i] = startIndex;
                //System.out.println( arr[i] + "+" + this.containedList.get(i).getOutcomes().get(0).length());
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

//            for (int i = 0; i < arr.length; i++) {
//                System.out.println("arr["+i+"] = " +arr[i]);
//            }
//            for (int i = 0; i < brr.length; i++) {
//                System.out.println("brr["+i+"] = " +brr[i]);
//            }


            String[] crr = new String[this.getContainedList().size()];
            int originalIndex;
            int newIndex = 0;

            for (Variable variable : orderList) {
//                System.out.println("Place: " + this.getNamesOfContainedList().indexOf(variable.name));
                originalIndex = this.getNamesOfContainedList().indexOf(variable.name);
//                System.out.println("C["+this.getNamesOfContainedList().indexOf(variable.name)+"] = " + brr[originalIndex]);
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

//            for (int i = 0; i < crr.length; i++) {
//                System.out.println("crr["+i+"] = " +crr[i]);
//            }

            String newKey = "";
            for (int i = 0; i < this.getContainedList().size(); i++) {
                newKey = newKey + crr[i];
            }

            //System.out.println("Check if it works: " + key + " --> " + newKey);
            newCpt.put(newKey, this.getCpt().get(key));

//            if (!newKey.equals(key))
//                newCpt.remove(key);
        }

        System.out.println("e1 " + newCL);
        System.out.println("e1 " + this.containedList);
        System.out.println("e2.1" + originalBackUp);
        System.out.println("e2.1 " + newCpt);
        //Algorithms.sortLHM(originalBackUp, newCpt);
        System.out.println("e2.2 " + newCpt);

        this.containedList = newCL;
        this.cpt = newCpt;
    }

    public static ArrayList<String> stringList(ArrayList<Variable> oldList) {
        ArrayList<String> newList = new ArrayList<String>();
        for (Variable variable : oldList) {
            newList.add(variable.getName());
        }
        return newList;
    }

    public void eliminateVarFromFactor(Variable variable) {
        /* To eliminate a var from Factor we need to:
         * Remove it from containedList
         * Remove it from Cpt in the right way.
        */
        //System.out.println("For " + this.getName() + " and " + variable.getName());
        //System.out.println(this.containedList + ", index of " + variable.getName() + " is: "+ this.getNamesOfContainedList().indexOf(variable.getName()));

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
        // System.out.println("Old cpt: " + this.getCpt());
        LinkedHashMap<String, Double> newCpt = new LinkedHashMap<String, Double>();
        for (String key : this.getCpt().keySet()) {
            // System.out.println("(0-"+pureIndex+ ") + (" + (pureIndex+var_outcome_len) + "-"+(key.length()-1)+ ")");
            String newKey = key.substring(0, pureIndex) + key.substring(pureIndex+var_outcome_len); // i shall sum that.^^

            if (newCpt.containsKey(newKey)) {
                double newVal = newCpt.get(newKey) + this.getCpt().get(key);
                System.out.println("sum: " + newCpt.get(newKey) + " + " + this.getCpt().get(key) + " = " + newKey);
                GlobalVars.setGlobalSum(GlobalVars.getGlobalSum()+1);
                newCpt.put(newKey, newVal);
            }
            else {
                System.out.println("no sum, added " + this.getCpt().get(key) + " = " + newKey + ", which based on ");
                newCpt.put(newKey, this.getCpt().get(key));
            }
        }
        System.out.println("New cpt: " + newCpt);
        this.cpt = newCpt;

        // Update CL:
        System.out.println("Old CL: " + this.getContainedList());
        if (this.getContainedList().contains(variable))
            this.containedList.remove(variable);
        else {
            int index = this.getNamesOfContainedList().indexOf(variable.getName());
            this.containedList.remove(index);
        }
        System.out.println("New CL: " + this.getContainedList());
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

    public void turnFactorToPureNumber(ArrayList<Factor> factors, Factor old_factor, Variable hidden_variable) {
        System.out.println("Making pure num");

        double sum = 0.0;

        for (String key : this.getCpt().keySet()) {
            sum += this.getCpt().get(key);
        }

        this.cpt = new LinkedHashMap<String, Double>();
        this.cpt.put("PureNum", sum); // "PureNum" - a keycode that I'll use when multiplying.
        //NORMALIZATION?
        factors.add(this); // Directly to the main factors list.
        factors.remove(old_factor);

        int index_hidden_variable = old_factor.getNamesOfContainedList().indexOf(hidden_variable.getName());
        System.out.println("dd1 " + this.containedList);
        this.containedList.remove(index_hidden_variable); //Ex: FactorX.cl=[A], cpt={0.5,0,5} --> FactorY.cl=[], cpt=[number].
        System.out.println("dd2 " + this.containedList);
        System.out.println("The new factor1: " + this);
        //i-- ?
    }

    public void turnFactorToPureNumber(ArrayList<Factor> factors, Factor old_factor, int index_hidden_variable) {
        System.out.println("Making pure num");

        double sum = 0.0;

        for (String key : this.getCpt().keySet()) {
            sum += this.getCpt().get(key);
        }

        this.cpt = new LinkedHashMap<String, Double>();
        this.cpt.put("PureNum", sum); // "PureNum" - a keycode that I'll use when multiplying.
        //NORMALIZATION?
        factors.add(this); // Directly to the main factors list.
        factors.remove(old_factor);

        //int index_hidden_variable = old_factor.getNamesOfContainedList().indexOf(hidden_variable.getName());
        System.out.println("dd1 " + this.containedList);
        this.containedList.remove(index_hidden_variable); //Ex: FactorX.cl=[A], cpt={0.5,0,5} --> FactorY.cl=[], cpt=[number].
        System.out.println("dd2 " + this.containedList);
        System.out.println("The new factor1: " + this);
        //i-- ?
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

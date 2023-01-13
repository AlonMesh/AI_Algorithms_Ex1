/*import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CPT {
    public Variable variable;
    public List<String> outcomes;
    public List<Double> values;
    public List<Variable> givens;
    public HashMap<String, Double> table = new HashMap<>();

    //Evidences = a list of the known values of variables that showed in the input.
    public double getProbability(String outcome, List<String> evidences) {
        
        int givenAmount = this.variable.givens.size();

        //If there is no parents.
        if (givenAmount == 0)
            return this.variable.values.get(this.variable.outcomes.indexOf(outcome));

        int jumps = 1;
        for (int i = 0; i < givenAmount; i++) {
            jumps *= this.variable.givens.get(i).outcomes.size();
        }

        //Creating an array that looks exactly the same as the list.
        double[] valueArray = new double[this.variable.values.size()];
        for (int i = 0; i < valueArray.length; i++) {
            valueArray[i] = this.variable.values.get(i);
        }

        return recArray(valueArray, jumps, 0, evidences);


        int wantedIndex = this.variable.values.size();

        for (int i = 0; i < givenAmount; i++) {
            int outComeAmount = this.variable.givens.get(i).outcomes.size();
            jumps = jumps / outComeAmount;

            for (int j = 0; j < outComeAmount; j++) {
                if (this.variable.givens.get(i).outcomes.get(j).equals(outcome)) {
                    wantedIndex = (wantedIndex / jumps) * j;
                    break;
                }
            }


        }

    }

    public double recArray(double[] arr, int jumps, int index, List<String> evidences) {
        if (arr.length == 1)
            return arr[0];

        String evidence = evidences.remove(0);
        int outComeAmount = this.variable.givens.get(index).outcomes.size();
        jumps = jumps / outComeAmount;

        for (int i = 0; i < arr.length; i+=jumps) {
            if (this.variable.values.get(i).equals(evidences.get(index))) {
                double[] subArr = Arrays.copyOfRange(arr, i, i+jumps);
                return recArray(subArr, jumps, index+1, evidences);
            }

        }
        return -1;
    }

}
*/
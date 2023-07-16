import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

public class Network {
    private List<Variable> variables; // All variables in our network
    private LinkedHashMap<String, String> evidences;

    public Network() {
        this.variables = new ArrayList<Variable>();
        this.evidences = new LinkedHashMap<String, String>();
    }

    public static Network createNetworkByXMLpath(String xmlPath) {
        Network network = new Network();
        try {
            ReadXML.creating_network_and_set_lists_for_each_variable(xmlPath, network);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        }
        return network;
    }

    public void addVariable(Variable variable) {
        this.variables.add(variables.size(), variable);
    }

    public Variable find_variable_by_name(String str) {
        //Input: A string, as the name of existing Variable in net
        //Output: Variable named by the string.
        int index = -1;

        for (int i = 0; i < this.variables.size(); i++) {
            if (this.variables.get(i).getName().equals(str)) {
                index = i;
            }
        }

        if (index == -1) {
            return null;
        }

        return this.variables.get(index);
    }

    //getter and setter
    public List<Variable> getVariables() {
        return variables;
    }

    public List<String> getVariablesNames() {
        List<String> names = new ArrayList<>(this.variables.size());

        for (int i = 0; i < this.variables.size(); i++) {
            names.add(this.variables.get(i).getName());
        }
        return names;
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

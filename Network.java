import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;

/**
 * Represents a Bayesian network containing variables and their conditional probability tables (CPTs).
 * <p>
     * In a Bayesian network, "variables" are random variables or nodes in the network that represent different
     * factors or events of interest. Each variable can have one or more possible outcomes, also known as states.
     * The relationships between variables are represented by conditional probability tables (CPTs) which define
     * the probability of each outcome based on the values of its parent variables.
 * <p>
     * "Evidences" are specific observations or evidence data associated with the variables in the network.
     * These observations represent known information or data points that can be used to infer probabilities
     * of other variables in the network using Bayesian inference techniques.
 * <p>
     * The Bayesian network allows us to model uncertain knowledge and make probabilistic inferences based on
     * observed evidence, making it a powerful tool for reasoning and decision-making under uncertainty.
 */
public class Network {
    final private List<Variable> variables;
    private LinkedHashMap<String, String> evidences;

    /**
     * Initializes a new instance of the Network class with empty variables and evidences.
     */
    public Network() {
        this.variables = new ArrayList<>();
        this.evidences = new LinkedHashMap<>();
    }

    /**
     * Creates a network based on the XML file located at the specified path.
     *
     * @param xmlPath The path to the XML file containing the network structure and variable details.
     * @return The created Network instance.
     */
    public static Network createNetworkByXmlPath(String xmlPath) {
        Network network = new Network();
        try {
            ReadXML.creating_network_and_set_lists_for_each_variable(xmlPath, network);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
        return network;
    }

    /**
     * Adds a variable to the network.
     *
     * @param variable The Variable object to be added to the network.
     */
    public void addVariable(Variable variable) {
        this.variables.add(variable);
    }

    /**
     * Finds a variable in the network based on its name.
     *
     * @param name The name of the variable to find.
     * @return The Variable object with the specified name or null if not found.
     */
    public Variable findVarByName(String name) {
        for (Variable variable : this.variables) {
            if (variable.getName().equals(name)) {
                return variable;
            }
        }
        return null;
    }

    // TODO: add /**
    public boolean areThereHiddenVariable() {
        return this.getEvidences().values().stream().noneMatch(value -> value.equals("%%%"));
    }

    /**
     * Gets the list of variables in the network.
     *
     * @return The list of Variable objects in the network.
     */
    public List<Variable> getVariables() {
        return variables;
    }

    /**
     * Gets a list of variable names in the network.
     *
     * @return The list of variable names in the network.
     */
    public List<String> getVariablesNames() {
        List<String> names = new ArrayList<>();
        for (Variable variable : this.variables) {
            names.add(variable.getName());
        }
        return names;
    }

    /**
     * Prints the details of the network, including variables and evidences.
     */
    public void print() {
        for (Variable variable : this.variables) {
            variable.print();
        }
        System.out.println("Evidence map: " + this.evidences.toString());
    }

    /**
     * Gets the evidences associated with the network.
     *
     * @return The LinkedHashMap of evidences with variable names as keys and evidence data as values.
     */
    public LinkedHashMap<String, String> getEvidences() {
        return evidences;
    }

    /**
     * Sets the evidences for the network.
     *
     * @param evidences The LinkedHashMap of evidences with variable names as keys and evidence data as values.
     */
    public void setEvidences(LinkedHashMap<String, String> evidences) {
        this.evidences = evidences;
    }
}

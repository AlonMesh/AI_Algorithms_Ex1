import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ReadXML {

    /**
     * Creates the network and sets lists for each variable based on the given XML file.
     *
     * @param path    The path to the XML file containing the network structure and variable details.
     * @param network The network object to be populated with variables and their properties.
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created.
     * @throws SAXException                 If there is an error in parsing the XML file.
     * @throws IOException                  If an I/O error occurs while reading the file.
     */
    public static void creating_network_and_set_lists_for_each_variable(String path, Network network)
            throws ParserConfigurationException, SAXException, IOException {
        File file = new File(path.trim());
        // An instance of factory that gives a document builder
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        // An instance of builder to parse the specified xml file
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        doc.getDocumentElement().normalize();

        NodeList nodeList_of_names = doc.getElementsByTagName("VARIABLE");
        NodeList nodeList_of_defs = doc.getElementsByTagName("DEFINITION");

        boolean all_parents_already_inserted;

        int index = 0;
        while (network.getVariables().size() < nodeList_of_names.getLength()) {
            Node node_of_names = nodeList_of_names.item(index);
            Node node_of_defs = nodeList_of_defs.item(index);

            all_parents_already_inserted = true;

            Node temp_node_of_names = nodeList_of_names.item(index);
            Node temp_node_of_defs = nodeList_of_defs.item(index);

            Element e_temp_node_of_names = (Element) temp_node_of_names;
            Element e_temp_node_of_defs = (Element) temp_node_of_defs;

            String name = e_temp_node_of_names.getElementsByTagName("NAME").item(0).getTextContent();

            int givenCount = e_temp_node_of_defs.getElementsByTagName("GIVEN").getLength();

            if (givenCount > 0) {
                // For each founded parent, check if it is already in the network
                for (int j = 0; j < givenCount; j++) {
                    String nameToFind = e_temp_node_of_defs.getElementsByTagName("GIVEN").item(j).getTextContent();
                    if (!network.getVariables().contains(network.findVarByName(nameToFind))) {
                        all_parents_already_inserted = false;
                    }
                }
            }

            if (node_of_names.getNodeType() == Node.ELEMENT_NODE && node_of_defs.getNodeType() == Node.ELEMENT_NODE &&
                    all_parents_already_inserted && !network.getVariables().contains(network.findVarByName(name))) {
                Element e_node_of_names = (Element) node_of_names;
                Element e_node_of_defs = (Element) node_of_defs;

                String[] valuesArr = e_node_of_defs.getElementsByTagName("TABLE").item(0).getTextContent().split(" ");

                ArrayList<Double> valuesList = new ArrayList<>();
                ArrayList<String> outcomesList = new ArrayList<>();

                // Add values to the valuesList based on whether there are any givens
                for (String s : valuesArr) {
                    valuesList.add(Double.valueOf(s));
                }

                int outcomesCount = e_node_of_names.getElementsByTagName("OUTCOME").getLength();

                // My code was based on assuming that all variable's outcomes have equal length.
                // Finding the minimum outcome length
                int minLen = e_node_of_names.getElementsByTagName("OUTCOME").item(0).getTextContent().length();
                for (int j = 1; j < outcomesCount; j++) {
                    minLen = Math.min(minLen, e_node_of_names.getElementsByTagName("OUTCOME").item(j).getTextContent().length());
                }

                // Add outcomes to the outcomesList based on the calculated minimum length
                for (int j = 0; j < outcomesCount; j++) {
                    outcomesList.add(e_node_of_names.getElementsByTagName("OUTCOME").item(j).getTextContent().substring(0, minLen));
                }

                name = e_node_of_names.getElementsByTagName("NAME").item(0).getTextContent();
                ArrayList<Variable> givens = new ArrayList<>();

                // For each founded parent, add it to the temp list (also find() in network?)
                for (int j = 0; j < givenCount; j++) {
                    String nameToFind = e_node_of_defs.getElementsByTagName("GIVEN").item(j).getTextContent();
                    Variable variable = network.findVarByName(nameToFind);
                    givens.add(variable);
                }

                Variable tempVar = new Variable(name, outcomesList, valuesList, givens);
                network.addVariable(tempVar);
            }

            if (index == nodeList_of_names.getLength() - 1)
                index = 0;
            else
                index++;
        }
    }
}

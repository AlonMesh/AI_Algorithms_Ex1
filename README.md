# Variable_Elimination_Algorithm

## Overview
This project is a Bayesian Network implementation that uses Variable Elimination (VE) algorithm to perform probabilistic inference. The network and the queries are read from an XML file and an input file respectively.
This project was made as exhbit of the course "AI algorithms".

## How to Run

## How to Run

1. Download the project files and navigate to the project directory in the command line. Make sure you have the necessary libraries installed:
   - javax.xml.parsers
   - org.w3c.dom
   - org.xml.sax
   - java.io

2. Compile the project by running the command:
`javac *.java`

3. Run the program by executing the command:
`java Main`

The program will read the input file and the XML file from the same directory and write the results to an `output.txt` file, also located in the same directory.

### Input File (input.txt)

Format for the `input.txt` file:

```
<name_of_xml_file>
<query1>,<algorithm_choice>
<query2>,<algorithm_choice>
...
```

**Example:**
```
net.xml
P(X=1|Y=2,Z=0),1
P(A=0|B=1),2
```

In this format:
- `<name_of_xml_file>`: Replace this with the name of the XML file that represents the Bayesian network (e.g., `net.xml`).
- `<query>`: Define your queries using the following format:
`P(<Variable>=<outcome>|<Evidence1>=<outcome>,<Evidence2>=<outcome>,...),<Algorithm_Choice>`

- `<Variable>`: The main variable being queried.
- `<outcome>`: One of all the possible values that the variable can take.
- `<Evidence>`: A given variable (which hasn't appeared yet) as evidence for the query.
- `<Algorithm_Choice>`: The desired algorithm for the query.
   - 1: Simple conclusion
   - 2: Variable elimination by a, b, c... order
   - 3: Variable elimination by appearance order

Make sure to include all the algorithms (1, 2, and 3) for each query.

### XML File
Create an XML file named `net.xml` to represent the Bayesian network. The XML structure should follow the format below:

```xml
<network>
<VARIABLE>
 <!-- Define variables and their outcomes -->
 <NAME>Variable1</NAME>
 <OUTCOME>Outcome1</OUTCOME>
 <OUTCOME>Outcome2</OUTCOME>
 <!-- Add more outcomes if needed -->
</VARIABLE>

<VARIABLE>
 <!-- Define another variable -->
 <NAME>Variable2</NAME>
 <OUTCOME>OutcomeA</OUTCOME>
 <OUTCOME>OutcomeB</OUTCOME>
 <!-- Add more outcomes if needed -->
 <!-- Add <DEFINITION> for CPT table if the variable has parents -->
 <DEFINITION>
   <FOR>Variable2</FOR>
   <GIVEN>Parent1</GIVEN>
   <GIVEN>Parent2</GIVEN>
   <!-- Add more <GIVEN> for additional parents -->
   <TABLE>
     0.8 0.2
   </TABLE>
 </DEFINITION>
</VARIABLE>

<!-- Define more variables with their outcomes and definitions if needed -->

</network>
```

Make sure to replace <VARIABLE_NAME> and <OUTCOME> with appropriate names and outcomes, and define <DEFINITION> with conditional probability values for variables with parents.
## File Structure
The project consists of several java classes including:

* **Ex1.java** (main) which is the entry point of the program and contains the main function that controls the flow of the program.
* **Network.java** which represents the Bayesian Network and contains the variables and the factors of the network.
* **Variable.java** which represents a variable in the network and contains its properties such as its name, values, and parents.
* **Factor.java** which represents a factor in the network and contains its properties such as the variables it contains and its probability table.
* **Algorithms.java** which contains the implementation of the Variable Elimination algorithm in three different versions.
* **GlobalVars.java** which contains global variables and methods used throughout the program.
* **ReadTXT.java** which contains methods for reading the input file and parsing its contents.
* **ReadXML.java** which contains methods for reading the XML file and parsing its contents.

## Conclusion
This implementation of Bayesian Network using Variable Elimination algorithm can be used for performing probabilistic inference on different types of networks and queries. However, it is worth noting that the performance of the algorithm may be affected by the structure of the network and the order of elimination of variables. The project compares the performance of three algorithms for finding probability in a graphical model. The algorithms compared are:

1. **Simple conclusion:** A basic algorithm for finding probability in a graphical model.
2. **Variable Elimination** Algorithm with ABC elimination order: An optimized algorithm that uses the ABC elimination order to find probability in a graphical model.
3. **Variable Elimination Algorithm with custom heuristic elimination order:** An optimized algorithm that uses a custom heuristic elimination order based on the number of appearances of variables in factors to find probability in a graphical model.

The project includes the implementation of each algorithm and the calculation of both the probability and the number of additions and multiplications required for each algorithm.

![image](https://user-images.githubusercontent.com/97172662/212310971-c9cde4f4-c974-4ecd-a06a-c571f582f25e.png)

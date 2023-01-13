# AI Algorithms Ex1

## Overview
This project is a Bayesian Network implementation that uses Variable Elimination (VE) algorithm to perform probabilistic inference. The network and the queries are read from an XML file and an input file respectively.

## How to Run

Download the project files and navigate to the project directory in the command line.
Make sure you have the necessary libraries installed: javax.xml.parsers, org.w3c.dom, org.xml.sax and java.io.
Compile the project by running the command javac \*.java
Run the program by running the command java Main
The program will read the input file and the XML file in the same directory, and write the results to an output file also in the same directory.

## File Structure
The project consists of several java classes including:

* **Main.java** which is the entry point of the program and contains the main function that controls the flow of the program.
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

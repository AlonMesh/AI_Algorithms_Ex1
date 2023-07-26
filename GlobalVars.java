import java.io.*;

/**
 * The GlobalVars class contains static fields and methods to manage global variables that can be accessed and modified from various parts of the program.
 * It provides a way to store and retrieve integer values for globalSum and globalMulti, as well as a static BufferedWriter named outputFile that is used to write data to an output file.
 */
public class GlobalVars {
    // Integer fields to store global variables
    public static int globalMulti; // Stores the global value for multiplication
    public static int globalSum; // Stores the global value for summation

    // Static BufferedWriter used for writing data to the output file
    static BufferedWriter outputFile;

    // Static block to initialize the outputFile when the class is loaded
    static {
        try {
            // Initialize the outputFile by opening a FileWriter and wrapping it with a BufferedWriter
            outputFile = new BufferedWriter(new FileWriter("output.txt"));
        } catch (IOException e) {
            // If an exception occurs while initializing the outputFile, throw a RuntimeException
            // Note: In practice, a better error handling strategy should be employed (e.g., logging or displaying a meaningful error message to the user).
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the value of the globalMulti variable.
     *
     * @return The current value of the globalMulti variable.
     */
    public static int getGlobalMulti() {
        return globalMulti;
    }

    /**
     * Sets the value of the globalMulti variable.
     *
     * @param globalMulti The new value to be set for the globalMulti variable.
     */
    public static void setGlobalMulti(int globalMulti) {
        GlobalVars.globalMulti = globalMulti;
    }

    /**
     * Returns the value of the globalSum variable.
     *
     * @return The current value of the globalSum variable.
     */
    public static int getGlobalSum() {
        return globalSum;
    }

    /**
     * Sets the value of the globalSum variable.
     *
     * @param globalSum The new value to be set for the globalSum variable.
     */
    public static void setGlobalSum(int globalSum) {
        GlobalVars.globalSum = globalSum;
    }
}

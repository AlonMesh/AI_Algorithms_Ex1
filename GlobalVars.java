import java.io.*;

public class GlobalVars {
    public static int globalMulti;
    public static int globalSum;
    public static String file;

    static BufferedWriter outputFile;

    static {
        try {
            outputFile = new BufferedWriter(new FileWriter("output.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getGlobalMulti() {
        return globalMulti;
    }

    public static void setGlobalMulti(int globalMulti) {
        GlobalVars.globalMulti = globalMulti;
    }

    public static int getGlobalSum() {
        return globalSum;
    }

    public static void setGlobalSum(int globalSum) {
        GlobalVars.globalSum = globalSum;
    }

}

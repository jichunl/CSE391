// This program produces console output and file output.
import java.io.*;

public class Crunch {
    public static void main(String[] args) throws IOException {
        PrintStream out = new PrintStream(new File("crunch.txt"));
        out.println("Hello, output file!");
        out.println("This file was produced by the Crunch Java program.");
        out.close();
        
        System.out.println("Hello, world!");
        System.out.println("This console output is being produced by");
        System.out.println("the Crunch Java program.  Make it go away!");
        System.out.println(">_<   kekekekekek");
    }
}

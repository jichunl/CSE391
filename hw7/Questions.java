/* My program, by Josh G */

/* This program asks the user a bunch of questions.
   And it wants to have them answered immediately! */

import java.util.*;

public class Questions {
    public static void main(String[] args) {
        Scanner console = new Scanner(System.in);
        String[] questions = {
            "Do you like this class? ",
            "Are you a country music fan? ",
            "Are you at least 7 feet tall? ",
            "Do you swear allegiance to the republic of Steve Jobs? ",
	    "Are you publicly embarrassed when you burp?"
        };
        printQuestions(console, questions);  /*call the method*/
    }

    /* Prints out the given questions and reads the user's answers. */
    public static void printQuestions(Scanner console, String[] questions) {
        int yes = 0;
        for (String question : questions) {
            System.out.println(question);
            String answer = console.nextLine();
            if (answer.equals("y")) {
                yes++;
            } /*else...*/
        }
        System.out.println("You said yes to " + yes + " of "
                + questions.length + " questions.");
    }
}

/* The end! */

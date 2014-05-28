package org.talend.geat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class used to ask something to the user.
 */
public class InputsUtils {

    public static String askUser(String question) {
        return askUser(question, "");
    }

    public static String askUser(List<String> choices, String defaultValue) {
        Collections.sort(choices);

        Map<Integer, String> choicesMap = new TreeMap<Integer, String>();

        Integer defaultValueIndex = null;

        int i = 1;
        String question = "";
        for (String currentChoice : choices) {
            choicesMap.put(i, currentChoice);
            question += i + ") " + currentChoice + "\n";
            if (defaultValue.equals(currentChoice)) {
                defaultValueIndex = i;
            }
            i++;
        }
        int answer = askUserAsInt(question, defaultValueIndex);
        return choicesMap.get(answer);
    }

    public static String askUser(String question, String defaultValue) {
        if (question != null) {
            System.out.print(question);
        }
        if (defaultValue != null && defaultValue.length() > 0) {
            System.out.print(" [" + defaultValue + "]");
        }
        System.out.println(" ? ");

        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        try {
            String str = br.readLine();
            if (str == null || str.length() < 1) {
                return defaultValue;
            } else {
                return str;
            }
        } catch (IOException e) {
            System.out.println("WARN: " + e.getMessage());
            return defaultValue;
        }
    }

    public static int askUserAsInt(String question, Integer defaultValue) {
        String defaultValueAsString = "" + defaultValue;
        if (defaultValue == null) {
            defaultValueAsString = null;
        }
        String answer = askUser(question, defaultValueAsString);
        return Integer.parseInt(answer);
    }

    public static boolean askUserAsBoolean(String question) {
        String answer = askUser(question + " (Yes/No)", "Yes");
        return answer.equalsIgnoreCase("yes");
    }
}

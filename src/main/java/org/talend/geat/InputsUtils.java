package org.talend.geat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class InputsUtils {

    public static String askUser(String question) {
        return askUser(question, "");
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

    public static boolean askUserAsBoolean(String question) {
        String answer = askUser(question + " (Yes/No)", "Yes");
        return answer.equalsIgnoreCase("yes");
    }
}

package org.talend.geat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.talend.geat.commands.Command;
import org.talend.geat.commands.FeatureStart;

public class GeatMain {

    private static void usage(Map<String, Command> commands) {
        System.out.println("Available commands are:");
        for (Entry<String, Command> command : commands.entrySet()) {
            System.out.println(" - " + command.getKey() + " - " + command.getValue().getDescription());
        }
    }

    public static void main(String[] args) {
        if (args.length == 1 && args[0] == "dev")
            args = new String[] { "feature-start", "feat_13" };

        Map<String, Command> commands = new HashMap<String, Command>();
        commands.put("hello", new Command() {

            public void run(String[] args) {
                System.out.println("Hello!");
            }

            public String getDescription() {
                return "Will say hello";
            }
        });
        commands.put("feature-start", new FeatureStart());

        if (args.length < 1 || !commands.containsKey(args[0])) {
            usage(commands);
            System.exit(0);
        }
        Command command = commands.get(args[0]);
        command.run(args);
    }

}

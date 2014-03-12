package org.talend.geat;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.talend.geat.commands.Command;
import org.talend.geat.commands.FeatureFinish;
import org.talend.geat.commands.FeatureStart;

import com.google.common.base.Strings;

public class GeatMain {

    private static void usage(Map<String, Command> commands) {
        System.out.println("Available commands are:");
        for (Entry<String, Command> command : commands.entrySet()) {
            System.out.println(" - " + command.getKey() + " - " + command.getValue().getDescription());
        }
    }

    public static void main(String[] args) {
        String workingDir = System.getProperty("user.dir");

        if (args.length == 1 && args[0].equals("dev")) {
            args = new String[] { "feature-finish", "tagada", "f" };
            workingDir = "/tmp/repo-test";
        }

        Map<String, Command> commands = new HashMap<String, Command>();
        commands.put("feature-start", new FeatureStart());
        commands.put("feature-finish", new FeatureFinish());

        if (args.length < 1 || !commands.containsKey(args[0])) {
            usage(commands);
            System.exit(1);
        }
        Command command = commands.get(args[0]);

        if (command.getArgsNumber() != args.length - 1) {
            System.out.println("Wrong number of parameters for this command!\nUsage is:\n");
            System.out.println(Strings.repeat(" ", Configuration.indentForCommandTemplates)  + args[0] + " " + command.getUsage());
            System.exit(1);
        }

        command.setWorkingDir(workingDir).run(args);
    }

}

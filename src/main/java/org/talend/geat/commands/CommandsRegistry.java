package org.talend.geat.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CommandsRegistry {

    public static final CommandsRegistry INSTANCE = new CommandsRegistry();

    private Map<String, Command>         commands = new HashMap<String, Command>();

    private CommandsRegistry() {
        registerCommands();
    }

    public Command getCommand(String key) {
        if (commands.containsKey(key)) {
            return commands.get(key);
        } else {
            return null;
        }
    }

    private void registerCommands() {
        commands.put("help", new AbstractCommand() {

            public void run(String[] args) {
                System.out.println("Available commands are:");
                for (Entry<String, Command> command : commands.entrySet()) {
                    System.out.println(" - " + command.getKey() + " - " + command.getValue().getDescription());
                }
            }

            public String getUsage() {
                return "";
            }

            public String getDescription() {
                return "Displays this help";
            }

            public int getArgsNumber() {
                return 0;
            }
        });
        commands.put("feature-start", new FeatureStart());
        commands.put("feature-finish", new FeatureFinish());
    }

}

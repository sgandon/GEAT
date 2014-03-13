package org.talend.geat.commands;

import java.util.Map.Entry;

public class Help extends AbstractCommand {

    public void run(String[] args) {
        System.out.println("Available commands are:");
        for (Entry<String, Command> command : CommandsRegistry.INSTANCE.getCommands().entrySet()) {
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
}

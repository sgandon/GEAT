package org.talend.geat.commands;

import java.io.IOException;
import java.io.Writer;
import java.util.Map.Entry;

/**
 * Displays all commands name and description.
 */
public class Help extends Command {

    public static final String NAME = "help";

    protected Help() {
        super();
    }

    public void execute(Writer writer) throws IOException {
        writer.write("Available commands are:");
        for (Entry<String, Command> command : CommandsRegistry.INSTANCE.getCommands().entrySet()) {
            writer.write(" - " + command.getKey() + " - " + command.getValue().getDescription());
        }
    }

    public String getUsage() {
        return "";
    }

    public String getDescription() {
        return "Displays this help";
    }

    public String getCommandName() {
        return NAME;
    }

}

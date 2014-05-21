package org.talend.geat.commands;

import java.io.IOException;
import java.io.Writer;

import org.talend.geat.SanityCheck.CheckLevel;

/**
 * Displays all commands name and description.
 */
public class Help extends Command {

    public static final String NAME = "help";

    protected Help() {
        super();
    }

    @Override
    public CheckLevel getCheckLevel() {
        return CheckLevel.NONE;
    }

    public void execute(Writer writer) throws IOException {
        writer.write("Available commands are:");
        for (String key : CommandsRegistry.INSTANCE.orderedCommands) {
            writer.write(" - " + key + " - " + CommandsRegistry.INSTANCE.getCommand(key).getDescription());
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

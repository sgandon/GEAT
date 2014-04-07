package org.talend.geat.commands;

import java.io.IOException;
import java.io.Writer;

import org.talend.geat.Configuration;
import org.talend.geat.SanityCheck.CheckLevel;

/**
 * Displays this product version.
 */
public class Version extends Command {

    public static final String NAME = "version";

    protected Version() {
        super();
    }

    @Override
    public CheckLevel getCheckLevel() {
        return CheckLevel.NONE;
    }

    public void execute(Writer writer) throws IOException {
        writer.write(Configuration.INSTANCE.getAsString("geat.name") + " "
                + Configuration.INSTANCE.getAsString("geat.version"));
    }

    public String getUsage() {
        return "";
    }

    public String getDescription() {
        return "Displays GEAT version";
    }

    public String getCommandName() {
        return NAME;
    }

}

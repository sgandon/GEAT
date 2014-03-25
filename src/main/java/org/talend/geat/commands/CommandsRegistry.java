package org.talend.geat.commands;

import java.util.HashMap;
import java.util.Map;

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
        commands.put(Help.NAME, new Help());
        commands.put("feature-start", new FeatureStart());
        commands.put(FeatureFinish.NAME, new FeatureFinish());
        commands.put(FeaturePush.NAME, new FeaturePush());
    }

    protected Map<String, Command> getCommands() {
        return commands;
    }

}

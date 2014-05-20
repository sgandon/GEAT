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
            try {
                return commands.get(key).getClass().newInstance();
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
                return null;
            }
        } else {
            return null;
        }
    }

    private void registerCommands() {
        commands.put(Help.NAME, new Help());
        commands.put(Version.NAME, new Version());
        commands.put(FeatureStart.NAME, new FeatureStart());
        commands.put(FeatureFinish.NAME, new FeatureFinish());
        commands.put(FeaturePush.NAME, new FeaturePush());
        commands.put(FeaturePull.NAME, new FeaturePull());
        commands.put(BugfixStart.NAME, new BugfixStart());
        commands.put(BugfixFinish.NAME, new BugfixFinish());
    }

    protected Map<String, Command> getCommands() {
        return commands;
    }

}

package org.talend.geat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.talend.geat.SanityCheck.CheckLevel;
import org.talend.geat.exception.IncorrectRepositoryStateException;

/**
 * Used to interact with git config.
 * 
 * This class must be initialized with a working directory path. This path must be a GIT repository.
 * 
 * When first init, default values are written in config.
 */
public class GitConfiguration {

    public static final String      CONFIG_PREFIX = "geat";

    private static GitConfiguration singleton;

    private StoredConfig            config;

    public static GitConfiguration getInstance() {
        if (singleton == null) {
            try {
                SanityCheck.check(CheckLevel.GIT_REPO_ONLY);
            } catch (IncorrectRepositoryStateException e) {
                throw new IllegalStateException("Cannot read configuration (cause:" + e.getMessage() + ")");
            }

            String workingDir = System.getProperty("user.dir");
            File workingDirFile = new File(workingDir);

            try {
                Git repo = Git.open(workingDirFile);
                singleton = new GitConfiguration();
                singleton.config = repo.getRepository().getConfig();
                singleton.setDefaultValues();
            } catch (IOException e) {
                // Should not occurs (SanityCheck bellow)
            }
        }
        return singleton;
    }

    private static final Map<String, String> defaultValues = new HashMap<String, String>();

    static {
        defaultValues.put("finishmergemode", "squash");
        defaultValues.put("featureStartPoint", "master");
        defaultValues.put("featurePrefix", "feature");
        defaultValues.put("bugfixStartPoint", "master");
        defaultValues.put("bugfixPrefix", "bugfix");
        defaultValues.put("maintenancePrefix", "maintenance");
        defaultValues.put("releasePrefix", "release");
    }

    private void setDefaultValues() {
        for (Entry<String, String> current : defaultValues.entrySet()) {
            if (get(current.getKey()) == null) {
                set(CONFIG_PREFIX, current.getKey(), current.getValue());
            }
        }
    }

    /**
     * Sets in the local config a param value.
     * 
     * All param are supposed to belongs to 'geat' section. So param key must not contains it.
     * 
     * Correct value are: finishmergemode - which will return value of param geat.finishmergemode
     */
    public void set(String section, String key, String value) {
        set(section, null, key, value);
    }

    /**
     * Sets in the local config a param value in geat section.
     */
    public void set(String key, String value) {
        set(CONFIG_PREFIX, null, key, value);
    }

    public void set(String section, String subsection, String key, String value) {
        config.setString(section, subsection, key, value);
        try {
            config.save();
        } catch (IOException e) {
            System.out.println("WARN: Cannot write configuration (" + e.getMessage() + "");
        }
    }

    /**
     * Gets from config, the value of param 'key' where key contains the git config separator '.'.
     * 
     * Corrects values are user.email, geat.finishmergemode
     */
    public String get(String key) {
        if (!key.contains(".")) {
            key = CONFIG_PREFIX + "." + key;
        }
        String[] split = key.split("\\.", 2);
        if (split.length != 2) {
            System.out.println("Malformed configuration key '" + key + "'");
            System.exit(1);
        }
        return config.getString(split[0], null, split[1]);
    }

}

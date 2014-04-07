// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.geat;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Handles the configuration stored in the configuration.properties file.
 */
public class Configuration {

    public static Configuration INSTANCE = new Configuration("configuration.properties");

    private Properties          props;

    protected Configuration(String name) {
        super();
        try {
            props = new Properties();
            final InputStream systemResourceAsStream = Configuration.class.getClassLoader().getResourceAsStream(name);
            if (systemResourceAsStream == null) {
                throw new FileNotFoundException("Cannot load file " + name);
            }
            props.load(systemResourceAsStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getAsString(String key) {
        return this.props.getProperty(key);
    }

    public int getAsInt(String key) {
        return Integer.parseInt(getAsString(key));
    }

    public boolean getAsBoolean(String key) {
        return new Boolean(getAsString(key));
    }
}

// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.colibri.properties;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class Bundles {

    private static Logger log = Logger.getLogger(Bundles.class);

    private static ResourceBundle configuration = ResourceBundle.getBundle("configuration");

    private static ResourceBundle additionnalBundle;

    public static void init(String additionnalBundle) {
        additionnalBundle = additionnalBundle.replace('.', '_');
        log.info("Loading property file '" + additionnalBundle + "'");
        try {
            Bundles.additionnalBundle = ResourceBundle.getBundle(additionnalBundle);
        } catch (MissingResourceException e) {
            log.warn("Property file '" + additionnalBundle + "' not found");
        }
    }

    public static String getString(String key) {
        try {
            if (additionnalBundle != null) {
                return additionnalBundle.getString(key);
            } else {
                return configuration.getString(key);
            }
        } catch (MissingResourceException e) {
            return configuration.getString(key);
        }
    }

    public static int getInt(String key) {
        return Integer.parseInt(getString(key));
    }

    public static Set<String> keySet() {
        return configuration.keySet();
    }

    public static void main(String[] args) {
        // System.out.println(MessageFormat.format("/tmp/tuj/{0}/{1}/", "trunk", "12000"));
        init("3_2");
    }
}

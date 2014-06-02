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
package org.talend.colibri;

import org.talend.colibri.properties.Bundles;


/**
 * DOC stephane class global comment. Detailled comment
 */
public class Misc {

    public static String getOutputFolderPath(String branch, String timestamp) {
        return Bundles.getString("jobs.outputFolder") + branch + "/" + timestamp + "/";
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }
}

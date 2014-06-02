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
package org.talend.colibri.database;

import java.text.SimpleDateFormat;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class DateFormater {

    private static SimpleDateFormat instance;

    public static SimpleDateFormat getInstance() {
        if (instance == null)
            instance = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return instance;
    }
}

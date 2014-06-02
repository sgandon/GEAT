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
package org.talend.colibri.execution.log;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class MyLogger extends Logger {

    public static final String EXECUTION_PREFFIX = "Execution.";

    /**
     * DOC stephane MyLogger constructor comment.
     * 
     * @param name
     * @throws IOException
     */
    protected MyLogger(String name) {
        super(EXECUTION_PREFFIX + name);
    }

}

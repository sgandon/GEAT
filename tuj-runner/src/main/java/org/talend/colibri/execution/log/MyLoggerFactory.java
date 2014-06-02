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

import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class MyLoggerFactory implements LoggerFactory {

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.spi.LoggerFactory#makeNewLoggerInstance(java.lang.String)
     */
    public Logger makeNewLoggerInstance(String name) {
        return new MyLogger(name);
    }

}

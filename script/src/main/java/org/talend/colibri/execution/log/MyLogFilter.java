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

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class MyLogFilter extends Filter {

    public MyLogFilter() {
        super();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.log4j.spi.Filter#decide(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public int decide(LoggingEvent event) {
        if (event.getLoggerName().startsWith(MyLogger.EXECUTION_PREFFIX))
            return Filter.DENY;
        return Filter.NEUTRAL;
    }

}

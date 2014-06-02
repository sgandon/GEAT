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
package org.talend.colibri.files;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class ResourcesUtils {

    private static Logger log = Logger.getLogger(ResourcesUtils.class);

    public static File getResource(String name) throws FileNotFoundException {
        log.debug("Getting resource '" + name + "'");
        URL resource = ResourcesUtils.class.getClassLoader().getResource(name);
        try {
            log.trace(" -> " + resource.toString());
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            log.warn("Unable to find file " + name);
            throw new FileNotFoundException("Unable to find file '" + name + "'-'" + resource + "'");
        } catch (NullPointerException e) {
            log.warn("Unable to find file " + name);
            throw new FileNotFoundException("Unable to find file '" + name + "'-'" + resource + "'");
        }
    }
}

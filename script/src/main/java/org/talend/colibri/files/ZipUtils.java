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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class ZipUtils {

    public static void zip(String source, String target) throws IOException {
        ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(target));
        zip.setMethod(ZipOutputStream.DEFLATED);
        zip.setLevel(Deflater.BEST_COMPRESSION);

        File file = new File(source);
        FileInputStream in = new FileInputStream(file);
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        in.close();

        ZipEntry entry = new ZipEntry(file.getName());
        entry.setTime(file.lastModified());
        zip.putNextEntry(entry);
        zip.write(bytes);
        zip.closeEntry();
        zip.close();
    }
}

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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class FilesUtils {

    public static void emptyFolder(File toEmpty) {
        if (toEmpty.exists()) {
            for (File current : toEmpty.listFiles()) {
                if (current.isDirectory()) {
                    emptyFolder(current);
                }
                current.delete();
            }
        }
    }

    public static void copyFile(File source, File target) throws IOException {
        // Need to recopy the file in one of these cases:
        // 1. target doesn't exists (never copied)
        // 2. if the target exists, compare their sizes, once defferent, for the copy.
        // 2. target exists but source has been modified recently(not used right now)

        if (!target.exists() || source.lastModified() > target.lastModified()) {
            copyFile(new FileInputStream(source), target);
        }
    }

    private static void copyFile(InputStream source, File target) throws IOException {
        FileOutputStream fos = null;
        try {
            if (!target.getParentFile().exists()) {
                target.getParentFile().mkdirs();
            }

            fos = new FileOutputStream(target);
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = source.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        } finally {
            try {
                source.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    public static void copyFolder(File source, File target) throws IOException {
        if (!target.exists()) {
            target.mkdirs();
        }

        FileFilter folderFilter = new FileFilter() {

            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }

        };
        FileFilter fileFilter = new FileFilter() {

            public boolean accept(File pathname) {
                return !pathname.isDirectory();
            }

        };

        for (File current : source.listFiles(folderFilter)) {
            File newFolder = new File(target, current.getName());
            newFolder.mkdir();
            copyFolder(current, newFolder);
        }

        for (File current : source.listFiles(fileFilter)) {
            File out = new File(target, current.getName());
            copyFile(current, out);
        }
    }

    public static void appendTextToFile(String fileName, String theText) throws IOException {
        FileOutputStream file = new FileOutputStream(fileName, true);
        DataOutputStream out = new DataOutputStream(file);
        out.writeBytes(theText);
        out.flush();
        out.close();
    }

    public static String getFirstString(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        try {
            return in.readLine();
        } finally {
            in.close();
        }
    }
}

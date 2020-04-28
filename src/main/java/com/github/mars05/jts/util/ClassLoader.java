package com.github.mars05.jts.util;

import sun.net.www.ParseUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * @author yu.xiao
 */
public class ClassLoader extends URLClassLoader {
    public ClassLoader(String cp) {
        super(getUrls(cp));
    }

    public ClassLoader(java.lang.ClassLoader parent) {
        super(new URL[]{}, parent);
    }

    public ClassLoader(String cp, java.lang.ClassLoader parent) {
        super(getUrls(cp), parent);
    }

    private static URL[] getUrls(String cp) {
        ArrayList<URL> path = new ArrayList<>();
        if (cp != null) {
            // map each element of class path to a file URL
            int off = 0, next;
            do {
                next = cp.indexOf(File.pathSeparator, off);
                String element = (next == -1)
                        ? cp.substring(off)
                        : cp.substring(off, next);
                URL url = toFileURL(element);
                if (url != null) {
                    path.add(url);
                }
                off = next + 1;
            } while (next != -1);
        }
        return path.toArray(new URL[]{});
    }

    private static URL toFileURL(String s) {
        try {
            File f = new File(s).getCanonicalFile();
            return ParseUtil.fileToEncodedURL(f);
        } catch (IOException e) {
            return null;
        }
    }

    public boolean isExistClass(String className) {
        String path = className.replace('.', '/').concat(".class");
        URL res = this.getResource(path);
        return res != null;
    }
}

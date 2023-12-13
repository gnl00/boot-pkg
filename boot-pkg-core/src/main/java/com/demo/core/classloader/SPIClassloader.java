package com.demo.core.classloader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * SPIClassloader
 *
 * @author gnl
 * @since 2023/5/10
 */
public class SPIClassloader extends URLClassLoader {
    public SPIClassloader(URL[] urls) {
        super(urls);
    }

    public void loadExternalJar(File externalJar) throws MalformedURLException {
        URL url = externalJar.toURI().toURL();
        addURL(url);
    }

    public void loadExternalJar(String jarPath) throws MalformedURLException {
        File externalJar = new File(jarPath);
        if (externalJar.exists()) {
            URL url = externalJar.toURI().toURL();
            addURL(url);
        }
    }
}

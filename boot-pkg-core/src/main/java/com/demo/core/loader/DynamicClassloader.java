package com.demo.core.loader;

import com.demo.core.tools.ReflectTool;
import jdk.internal.loader.URLClassPath; // Java 17 VM 添加 --add-exports java.base/jdk.internal.loader=ALL-UNNAMED 否则反射获取异常
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

import java.net.URL; // Java 17 需要在 VM 中添加 --add-opens=java.base/java.net=ALL-UNNAMED 否则反射获取异常
import java.net.URLClassLoader;
import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * DynamicClassloader
 *
 * @author gnl
 * @since 2023/5/10
 */
@Getter
@Setter
@Slf4j
public class DynamicClassloader extends URLClassLoader {

    private String name;

    {
        setName("DynamicClassloader");
    }

    public DynamicClassloader(URL[] urls) {
        super(urls);
    }

    public void loadURL(URL url) {
        addURL(url);
    }

    public void unLoad(URL url) {
        doUnLoad(url);
    }

    private void doUnLoad(URL removeUrl) {
        unsafeUnload(removeUrl);
    }

    private void unsafeUnload(URL removeUrl) {
        try {
            Unsafe unsafe = ReflectTool.getUnsafe();
            Class<?> urlClassLoaderClass = getClass().getSuperclass();

            long ucpOffset = unsafe.objectFieldOffset(urlClassLoaderClass.getDeclaredField("ucp"));
            URLClassPath ucp = (URLClassPath) unsafe.getObject(this, ucpOffset);
            long unopenedUrlsOffset = unsafe.objectFieldOffset(ucp.getClass().getDeclaredField("unopenedUrls"));
            long pathOffset = unsafe.objectFieldOffset(ucp.getClass().getDeclaredField("path"));

            ArrayDeque<URL> unopenedUrls = (ArrayDeque<URL>) unsafe.getObject(ucp, unopenedUrlsOffset);
            ArrayList<URL> path = (ArrayList<URL>) unsafe.getObject(ucp, pathOffset);

            // System.out.println(unopenedUrls);
            // System.out.println(path);

            synchronized (unopenedUrls) {
                unopenedUrls.remove(removeUrl);
                path.remove(removeUrl);
            }

            log.info("url unloaded");
            // System.out.println("*** after remove ***");
            // System.out.println(unopenedUrls); // []
            // System.out.println(path); // []

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}

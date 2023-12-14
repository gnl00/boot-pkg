package com.demo.core.plugin;

import com.demo.core.loader.DynamicClassloader;
import com.demo.core.loader.DynamicLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.NoOp;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Plugins {
    private static DynamicLoader dynamicLoader;
    private static Map<String, PluginMetadata> installedPlugins;

    static {
        installedPlugins = new HashMap<>();
    }

    private static void initLoader() {
        if (Objects.isNull(dynamicLoader)) {
            dynamicLoader = new DynamicLoader();
        }
    }

    public static void install(String name, URL url) {
        DynamicClassloader dcl = loadURL(url);
        PluginMetadata metadata = PluginMetadata.builder()
                .name(name)
                .url(url)
                .classLoader(dcl)
                .build();

        installedPlugins.put(name, metadata);
    }

    private static DynamicClassloader loadURL(URL url) {
        DynamicClassloader dynamicClassloader = new DynamicClassloader(new URL[]{});
        dynamicClassloader.loadURL(url);
        return dynamicClassloader;
    }

    public static boolean unInstall(String pluginName) {
        PluginMetadata plugin = installedPlugins.get(pluginName);
        if (Objects.nonNull(plugin)) {
            DynamicClassloader dcl = plugin.getClassLoader();
            URL url = plugin.getUrl();
            dcl.unLoad(url);
            installedPlugins.remove(pluginName);
            return true;
        }
        return false;
    }

    public static int LoadCheck(String pluginName) {
        if (installedPlugins.containsKey(pluginName)) return -1;
        return 1;
    }

    public static Map<String, PluginMetadata> getPlugins() {
        return installedPlugins;
    }

    public static void classpathExecute() {
        if (Objects.isNull(dynamicLoader)) {
            initLoader();
        }
        dynamicLoader.loadClasspath();
    }
    public static boolean executeByName(String pluginName) {
        if (Objects.isNull(dynamicLoader)) {
            initLoader();
        }
        PluginMetadata plugin = installedPlugins.get(pluginName);
        if (Objects.isNull(plugin)) {
            log.error("plugin not found");
            return false;
        }
        // create a proxy classloader
        DynamicClassloader classLoader = plugin.getClassLoader();
        DynamicClassloader pcl = getProxyClassLoader(classLoader);
        dynamicLoader.load(pcl);
        pcl = null; // release resource
        return true;
    }

    private static DynamicClassloader getProxyClassLoader(DynamicClassloader cl) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cl.getClass());
        enhancer.setCallback(NoOp.INSTANCE); // 设置 Callback 为 NoOp.INSTANCE，表示不对代理对象执行的方法进行任何处理
        return (DynamicClassloader) enhancer.create(new Class[]{URL[].class}, new Object[]{cl.getURLs()});
    }
}

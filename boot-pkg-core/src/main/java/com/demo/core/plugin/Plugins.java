package com.demo.core.plugin;

import com.demo.core.loader.DynamicClassloader;
import com.demo.core.loader.DynamicLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.NoOp;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Plugins {
    private static DynamicLoader dynamicLoader;
    private static final Map<String, PluginMetadata> installedPlugins;
    // WeakReference JVM 只要发现不可达便立即回收
    private static final Map<String, WeakReference<DynamicClassloader>> classloaderWRMap;

    static {
        installedPlugins = new HashMap<>();
        classloaderWRMap = new HashMap<>();
    }

    private static void initLoader() {
        if (Objects.isNull(dynamicLoader)) {
            dynamicLoader = new DynamicLoader();
        }
    }

    public static void install(String pluginName, URL url) {
        WeakReference<DynamicClassloader> srcl = loadURL(url);
        PluginMetadata metadata = PluginMetadata.builder()
                .name(pluginName)
                .url(url)
                .classLoader(srcl.get())
                .build();

        installedPlugins.put(pluginName, metadata);
        classloaderWRMap.put(pluginName, srcl);
    }

    private static WeakReference<DynamicClassloader> loadURL(URL url) {
        WeakReference<DynamicClassloader> wkcl = new WeakReference<>(new DynamicClassloader(new URL[]{}));
        wkcl.get().loadURL(url);
        return wkcl;
    }

    public static boolean unInstall(String pluginName) {
        PluginMetadata plugin = installedPlugins.get(pluginName);
        if (Objects.nonNull(plugin)) {
            // 卸载插件需要同时清除 classloader 引用
            WeakReference<DynamicClassloader> weakReference = classloaderWRMap.get(pluginName);
            weakReference.clear();
            classloaderWRMap.remove(pluginName);
            installedPlugins.remove(pluginName);
            return true;
        }
        return false;
    }

    /**
     * @link https://github.com/gnl00/boot-pkg/releases/tag/install-uninstall-1.0
     */
    @Deprecated
    public static boolean unInstallByReflect(String pluginName) {
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
        DynamicClassloader classLoader = plugin.getClassLoader();
        dynamicLoader.load(classLoader);
        return true;
    }

    /**
     * @link https://github.com/gnl00/boot-pkg/releases/tag/install-uninstall-1.0
     */
    @Deprecated
    public static boolean executeByProxy(String pluginName) {
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

    /**
     * @link https://github.com/gnl00/boot-pkg/releases/tag/install-uninstall-1.0
     */
    @Deprecated
    private static DynamicClassloader getProxyClassLoader(DynamicClassloader cl) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cl.getClass());
        enhancer.setCallback(NoOp.INSTANCE); // 设置 Callback 为 NoOp.INSTANCE，表示不对代理对象执行的方法进行任何处理
        return (DynamicClassloader) enhancer.create(new Class[]{URL[].class}, new Object[]{cl.getURLs()});
    }
}

package com.demo.core.plugin;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
public class PluginTools {

    private static Map<String, String> jarsRootCache;

    static {
        jarsRootCache = getRootJar();
    }

    public static Map<String, String> getRootJar() {
        Map<String, String> jarMap = new HashMap<>();
        // get jars file from project root path
        try (Stream<Path> pathStream = Files.list(Paths.get("."))) {
            pathStream.filter(p -> p.toString().endsWith(".jar")).forEach(p -> {
                jarMap.put(p.getFileName().toString().substring(0, p.getFileName().toString().length() - 4), p.toString());
            });
        } catch (IOException e) {
            log.error("get local jars error", e);
        }
        return jarMap;
    }

    public static String getJarPath(String jarName) {
        return jarsRootCache.get(jarName);
    }

}

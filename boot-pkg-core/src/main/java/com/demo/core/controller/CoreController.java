package com.demo.core.controller;

import com.demo.core.plugin.PluginMetadata;
import com.demo.core.plugin.PluginTools;
import com.demo.core.plugin.Plugins;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * CoreController
 *
 * @author gnl
 * @since 2023/5/8
 */
@Slf4j
@RestController
public class CoreController {

    @Autowired
    private ConfigurableApplicationContext ac;

    @GetMapping("/classpath")
    public String listClasspath() {
        Map<String, Object> properties = ac.getEnvironment().getSystemProperties();
        return (String) properties.get("java.class.path");
    }

    @GetMapping("/info")
    public Map<String, Object> getInfos() {
        Map<String, Object> systemProperties = ac.getEnvironment().getSystemProperties();
        Map<String, Object> retVal = new HashMap<>(systemProperties); // add system properties
        return retVal;
    }

    @GetMapping("/rootJars")
    public Map<String, String> getLocalJars() {
        return PluginTools.getRootJar();
    }

    private String getJarPath(String jarName) {
        return PluginTools.getJarPath(jarName);
    }

    /**
     * dynamic load jar from external path
     * 使用自定义的 ClassLoader（SPIClassloader）来加载外部 Jar，并执行对应的方法，
     * 但是这种方式会导致外部 Jar 中的类无法被 Spring 扫描到，所以无法使用 @Autowired 注入外部 Jar 中的类
     * <p>
     * 为了解决这个问题，可以使用 Spring 的 BeanUtils 来实现动态加载外部 Jar
     */
    @GetMapping("/loadFromRoot")
    public String loadFromRoot(@RequestParam String jarName) {
        String pathStr = getJarPath(jarName);
        Path path = Paths.get(pathStr);
        File jarFile = new File(path.getFileName().toString());
        return loadJar(jarName, jarFile);
    }

    @GetMapping("/loadFromPath")
    public String loadFromPath(@RequestParam String jarPath) {
        Path path = Paths.get(jarPath);
        File jarFile = new File(path.getFileName().toString());
        String jarName = getJarName(jarPath); // TODO: check name here
        return loadJar(jarName, jarFile);
    }

    private String getJarName(String jarPath) {
        return jarPath.substring(jarPath.lastIndexOf("/") + 1, jarPath.lastIndexOf("."));
    }

    @PostMapping("/loadFromFile")
    public String loadJarFromFile(MultipartFile multipartFile) {
        try {
            byte[] bytes = multipartFile.getBytes();
            String jarName = multipartFile.getName(); // TODO: check name here
            File jarFile = new File(jarName);
            try (FileOutputStream fos = new FileOutputStream(jarFile)) {
                fos.write(bytes);
            }
            return loadJar(jarName, jarFile);
        } catch (IOException e) {
            log.error("load jar from file failed {}", e.getMessage());
            return "load jar failed";
        }
    }

    private String loadJar(String jarName, File jar) {
        try {
            URL jarURL = jar.toURI().toURL();
            int check = 0;
            if (1== (check = Plugins.LoadCheck(jarName))) {
                Plugins.install(jarName, jarURL);
            } else if (-1 == check) {
                return "jar already exists";
            }
            return "jar load successfully";
        } catch (MalformedURLException e) {
            log.error("load jar failed {}", e.getMessage());
            return "load jar failed";
        }
    }

    /**
     * 使用自定义的 ClassLoader 加载对应的 SPI 实现类并执行相应的方法。
     */
    @GetMapping("/execute")
    public String dynamicLoad(@RequestParam String pluginName) {
        return Plugins.executeByName(pluginName) ? "execute successfully" : "execute failed";
    }

    // load from classpath
    @GetMapping("/classpathLoad")
    public void loadFromClasspath() {
        Plugins.classpathExecute();
    }

    @GetMapping("/plugins")
    public Map<String, PluginMetadata> getPlugins() {
        return Plugins.getPlugins();
    }

    @GetMapping("/unload")
    public String unLoadPlugin(@RequestParam String pluginName) {
        Plugins.unInstall(pluginName);
        return "unloaded";
    }
}

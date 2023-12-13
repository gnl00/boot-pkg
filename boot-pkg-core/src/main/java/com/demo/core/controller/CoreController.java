package com.demo.core.controller;

import com.demo.core.classloader.SPIClassloader;
import com.demo.core.component.SpiLoader;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * CoreController
 *
 * @author gnl
 * @since 2023/5/8
 */
@Slf4j
@RestController
public class CoreController {

    private static SPIClassloader spiClassloader;
    private Map<String, String> jarsCache;

    @Autowired
    private SpiLoader spiLoader;

    @Autowired
    private ConfigurableApplicationContext ac;

    {
        jarsCache = getLocalJars();
    }

    @GetMapping("/str")
    public String str() {
        return "str()";
    }

    @GetMapping("/classpath")
    public String listClasspath() {
        Map<String, Object> properties = ac.getEnvironment().getSystemProperties();
        return (String) properties.get("java.class.path");
    }

    @GetMapping("/info")
    public Map<String, Object> getInfos() {
        Map<String, Object> systemProperties = ac.getEnvironment().getSystemProperties();
        Map<String, Object> retVal = new HashMap<>(systemProperties); // add system properties
        retVal.putAll(getLocalJars());
        return retVal;
    }

    @GetMapping("/localJars")
    public Map<String, String> getLocalJars() {
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

    /**
     * dynamic load jar from external path
     * 使用自定义的 ClassLoader（SPIClassloader）来加载外部 Jar，并执行对应的方法，
     * 但是这种方式会导致外部 Jar 中的类无法被 Spring 扫描到，所以无法使用 @Autowired 注入外部 Jar 中的类
     * <p>
     * 为了解决这个问题，可以使用 Spring 的 BeanUtils 来实现动态加载外部 Jar
     */
    @GetMapping("/loadFromLocal")
    public String loadExtJar(@RequestParam String jarName) throws IOException {
        String jarPath = jarsCache.get(jarName);
        Path filePath = Paths.get(jarPath);
        File externalJar = new File(filePath.getFileName().toString());
        if (Objects.isNull(spiClassloader)) {
            spiClassloader = new SPIClassloader(new URL[]{});
        }
        spiClassloader.loadExternalJar(externalJar);
        System.out.println("jar loaded: " + jarName);
        return "jar loaded: " + jarName;
    }

    /**
     * 使用自定义的 ClassLoader 加载对应的 SPI 实现类并执行相应的方法。
     */
    @GetMapping("/dynamicLoad")
    public void dynamicLoad() {
        spiLoader.load(spiClassloader);
    }

    // load from classpath
    @GetMapping("/classpathLoad")
    public void loadFromClasspath() {
        spiLoader.loadClasspath();
    }

    @PostMapping("/loadFromFile")
    public void loadJarFromFile(MultipartFile multipartFile) throws IOException {
        byte[] bytes = multipartFile.getBytes();
        File externalJar = new File(multipartFile.getName());
        try (FileOutputStream fos = new FileOutputStream(externalJar)) {
            fos.write(bytes);
        }
        if (Objects.isNull(spiClassloader)) {
            spiClassloader = new SPIClassloader(new URL[]{});
        }
        spiClassloader.loadExternalJar(externalJar);
        System.out.println("jar loaded");
    }

    @GetMapping("/loadFromPath")
    public void loadJarFromPath(@RequestParam String path) throws MalformedURLException {
        if (Objects.isNull(spiClassloader)) {
            spiClassloader = new SPIClassloader(new URL[]{});
        }
        spiClassloader.loadExternalJar(path);
        System.out.println("jar loaded");
    }
}

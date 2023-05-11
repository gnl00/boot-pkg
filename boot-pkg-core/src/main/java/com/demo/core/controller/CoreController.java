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

    private static SPIClassloader spiClassloader;

    @Autowired
    private SpiLoader spiLoader;

    @Autowired
    private ConfigurableApplicationContext ac;

    @GetMapping("/str")
    public String str() {
        return "str()";
    }

    @GetMapping("/classpath")
    public void listClasspath() {
        Map<String, Object> properties = ac.getEnvironment().getSystemProperties();
        System.out.println(properties.get("java.class.path"));
    }

    @GetMapping("/loadExtJar")
    public void loadExtJar() throws IOException {
        File externalJar = new File("./boot-pkg-spi-impl-a-1.0-SNAPSHOT.jar");
        spiClassloader = new SPIClassloader(new URL[]{});
        spiClassloader.loadExternalJar(externalJar);
        System.out.println("jar loaded");
    }

    @PostMapping("/loadJarFromFile")
    public void loadJarFromFile(MultipartFile multipartFile) throws IOException {
        byte[] bytes = multipartFile.getBytes();
        File externalJar = new File(multipartFile.getName());
        try (FileOutputStream fos = new FileOutputStream(externalJar)) {
            fos.write(bytes);
        }
        spiClassloader = new SPIClassloader(new URL[]{});
        spiClassloader.loadExternalJar(externalJar);
        System.out.println("jar loaded");
    }

    @GetMapping("/loadJarFromPath")
    public void loadJarFromPath(@RequestParam String path) throws MalformedURLException {
        spiClassloader = new SPIClassloader(new URL[]{});
        spiClassloader.loadExternalJar(path);
        System.out.println("jar loaded");
    }

    @GetMapping("/load")
    public void loadSpi() {
        spiLoader.load(spiClassloader);
    }

    @GetMapping("/loadFromClasspath")
    public void loadClasspath() {
        spiLoader.loadClasspath();
    }
}

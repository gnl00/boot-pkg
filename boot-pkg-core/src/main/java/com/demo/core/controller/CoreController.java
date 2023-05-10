package com.demo.core.controller;

import com.demo.core.classloader.SPIClassloader;
import com.demo.core.component.SpiLoader;
import com.demo.spi.BootSpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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
        log.info("TestController --> str()");
        return "str()";
    }

    @GetMapping("/classpath")
    public void listClasspath() {
        Map<String, Object> properties = ac.getEnvironment().getSystemProperties();
        System.out.println(properties.get("java.class.path"));
    }

    @GetMapping("/loadJar")
    public void loadJar(@RequestParam String jarPath) throws MalformedURLException {
        String path = "boot-pkg-spi-impl-a.jar";
        spiClassloader = new SPIClassloader(new URL[]{});
        spiClassloader.loadExternalJar(path);
        System.out.println("jar loaded");
    }

    @GetMapping("/loadClass")
    public void loadClass() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = spiClassloader.loadClass("com.demo.impl.BootSpiImplA");
        Object newInstance = clazz.getConstructor().newInstance();
        BootSpi spi = (BootSpi) newInstance;
        spi.load();
    }

    @GetMapping("/load")
    public void loadSpi() {
        spiLoader.load(spiClassloader);
    }

    @GetMapping("/loadcp")
    public void loadClasspath() {
        spiLoader.loadClasspath();
    }
}

package com.demo.core.controller;

import com.demo.core.classloader.SPIClassloader;
import com.demo.core.component.SpiLoader;
import com.demo.spi.BootSpi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

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

    @GetMapping("/str")
    public String str() {
        log.info("TestController --> str()");
        return "str()";
    }

    @GetMapping("/loadJar")
    public void loadJar() throws MalformedURLException {
        String jatPath = "boot-pkg-spi-impl-a.jar";
        spiClassloader = new SPIClassloader(new URL[]{});
        spiClassloader.loadExternalJar(jatPath);
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
}

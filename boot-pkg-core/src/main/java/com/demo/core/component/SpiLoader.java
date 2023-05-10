package com.demo.core.component;

import com.demo.core.classloader.SPIClassloader;
import com.demo.spi.BootSpi;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * SpiLoader
 *
 * @author gnl
 * @since 2023/5/10
 */
@Component
public class SpiLoader {

    private static ClassLoader classloader;

    public void load(ClassLoader cl) {
        Assert.notNull(cl, "spi classloader must not be null");

        classloader = cl;
        Thread.currentThread().setContextClassLoader(cl);
        ServiceLoader<BootSpi> services = ServiceLoader.load(BootSpi.class);
        for (BootSpi service : services) {
            service.load();
        }
    }

    public void getTargetClass() throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        if (Objects.nonNull(classloader)) {
            Class<?> clazz = classloader.loadClass("com.demo");
            BootSpi spi = (BootSpi)clazz.getConstructor().newInstance();
            spi.load();
        }
    }

}

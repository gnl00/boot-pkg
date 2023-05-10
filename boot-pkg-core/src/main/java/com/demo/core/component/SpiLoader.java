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
        // 设置当前线程的上下文类加载器
        Thread.currentThread().setContextClassLoader(cl);
        doServiceLoad();

        // 加载完成后将上下文类加载器设置回原来的值，以避免影响其他模块的加载
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    }

    public void loadClasspath() {
        doServiceLoad();
    }

    public void doServiceLoad() {
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

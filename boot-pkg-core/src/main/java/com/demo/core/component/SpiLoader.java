package com.demo.core.component;

import com.demo.spi.BootSpi;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.ServiceLoader;

/**
 * SpiLoader
 *
 * @author gnl
 * @since 2023/5/10
 */
@Component
public class SpiLoader {

    public void load(ClassLoader cl) { // 必须使用自定义的 ClassLoader
        Assert.notNull(cl, "SPI ClassLoader must not be null");

        // 设置当前线程的上下文类加载器
        Thread.currentThread().setContextClassLoader(cl);
        serviceLoad(cl);
        // 加载完成后将上下文类加载器设置回原来的值，以避免影响其他模块的加载
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    }

    public void loadClasspath() {
        serviceLoad(getClass().getClassLoader());
    }

    public void serviceLoad(Object classloader) {
        System.out.println("current classloader: " + classloader);
        ServiceLoader<BootSpi> services = ServiceLoader.load(BootSpi.class);
        for (BootSpi spiImpl : services) {
            spiImpl.load();
        }
    }

}

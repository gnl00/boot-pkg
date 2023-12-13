package com.demo.impl;

import com.demo.spi.BootSpi;

/**
 * BootSpiImplA
 *
 * @author gnl
 * @since 2023/5/10
 */
public class BootSpiImplA implements BootSpi {
    @Override
    public void load() {
        System.out.println("BootSpiImplAAA load ");
    }
}

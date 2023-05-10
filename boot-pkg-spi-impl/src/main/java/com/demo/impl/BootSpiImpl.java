package com.demo.impl;

import com.demo.spi.BootSpi;

/**
 * BootSpiImpl
 *
 * @author gnl
 * @since 2023/5/10
 */
public class BootSpiImpl implements BootSpi {
    @Override
    public void load() {
        System.out.println("BootSpiImpl");
    }
}

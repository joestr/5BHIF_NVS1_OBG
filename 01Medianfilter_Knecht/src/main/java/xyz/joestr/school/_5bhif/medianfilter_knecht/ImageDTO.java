/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.joestr.school._5bhif.medianfilter_knecht;

import java.util.Arrays;
import java.util.logging.Logger;

/**
 *
 * @author Joel
 */
public class ImageDTO {
    
    private int width;
    private int height;
    private byte[] bytes;
    private int filter;
    private int readius;
    private int stripePosition;

    public ImageDTO(int width, int height, byte[] bytes, int filter, int readius, int stripePosition) {
        this.width = width;
        this.height = height;
        this.bytes = bytes;
        this.filter = filter;
        this.readius = readius;
        this.stripePosition = stripePosition;
    }

    public ImageDTO() {
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getFilter() {
        return filter;
    }

    public void setFilter(int filter) {
        this.filter = filter;
    }

    public int getReadius() {
        return readius;
    }

    public void setReadius(int readius) {
        this.readius = readius;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getStripePosition() {
        return stripePosition;
    }

    public void setStripePosition(int stripePosition) {
        this.stripePosition = stripePosition;
    }

    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.joestr.school._5bhif.medianfilter_knecht;

import com.google.gson.Gson;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.imageio.ImageIO;
import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 *
 * @author Joel
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket socket = new Socket("192.168.197.8", 55555);
        DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
        //BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        byte[] bytes = socket.getInputStream().readAllBytes();
        
        ImageDTO imagedata = new Gson().fromJson(new String(bytes), ImageDTO.class);
        
        
        if(imagedata.getFilter() == 1 || imagedata.getFilter() == 2 || imagedata.getFilter() == 3) {
            
            BufferedImage bmp = ImageIO.read(new ByteArrayInputStream(imagedata.getBytes()));
            
            for (int y = 0; y < bmp.getHeight(); y++){
                for (int x = 0; x < bmp.getWidth(); x++){

                    int rgb = bmp.getRGB(x, y);
                    Color c = new Color(bmp.getRGB(x, y));
                    Color newC = new Color(
                        imagedata.getFilter() == 1 ? 0 : c.getRed(),
                        imagedata.getFilter() == 2 ? 0 : c.getGreen(),
                        imagedata.getFilter() == 3 ? 0 : c.getBlue(),
                        c.getAlpha()
                    );
                    bmp.setRGB(x, y, newC.getRGB());
                }
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(bmp, "bmp", stream);
            byte[] byteArray = stream.toByteArray();
            
            outToServer.write(byteArray, 0, byteArray.length);
            socket.close();
        } else if(imagedata.getFilter() == 4) {
            
            BufferedImage bmp = ImageIO.read(new ByteArrayInputStream(imagedata.getBytes()));
            
            int r = imagedata.getReadius();
            
            int runex = 0 + 1 + 2 * r;
            
            /**
             * 0 -> bottom
             * 1 -> top
             * 2 -> sandwich
             * 3 -> whole pic
             */
            
            int tmpSPBottom = 0;
            int tmpSPTop = 0;
            
            if(imagedata.getStripePosition() == 0) {
                tmpSPBottom = imagedata.getReadius();
            }
            
            if(imagedata.getStripePosition() == 1) {
                tmpSPTop = imagedata.getReadius();
            }
            
            if(imagedata.getStripePosition() == 2) {
                tmpSPBottom = imagedata.getReadius();
                tmpSPTop = imagedata.getReadius();
            }
            
            for (int y = 0 + tmpSPBottom; y < imagedata.getHeight() - tmpSPTop; y++){
                for (int x = 0; x < imagedata.getWidth(); x++){
                    double bv[] = new double[runex * runex];
                    double rv[] = new double[runex * runex];
                    double gv[] = new double[runex * runex];
                    int addCount = 0;
                    for(int a = x - r; a  < x + 1 + r; a++) {
                        for(int b = y - r; b  < y + 1 * r; b++) {

                            if(a < 0 || b < 0) continue;

                            Color color;
                            try {
                                color = new Color(bmp.getRGB(a, b));
                            } catch (Exception e) {
                                continue;
                            }

                            rv[addCount] = color.getRed();
                            gv[addCount] = color.getGreen();
                            bv[addCount] = color.getBlue();

                            addCount++;
                        }
                    }

                    Median median = new Median();

                    int rm = (int) median.evaluate(rv, 0, addCount);
                    int bm = (int) median.evaluate(bv, 0, addCount);
                    int gm = (int) median.evaluate(gv, 0, addCount);

                    for(int a = x - r; a  < x + 1 +  r; a++) {
                        for(int b = y - r; b  < y + 1 + r; b++) {

                            if(a < 0 || b < 0) continue;

                            try {
                                Color c = new Color(bmp.getRGB(a, b));
                                Color newC = new Color(
                                    rm,
                                    gm,
                                    bm,
                                    c.getAlpha()
                                );
                                bmp.setRGB(a, b, newC.getRGB());
                            } catch (Exception e) {

                            }
                        }
                    }

                }
            }
            
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(bmp, "bmp", stream);
            byte[] byteArray = stream.toByteArray();
            
            outToServer.write(byteArray, 0, byteArray.length);
            socket.close();
        }
        
        
            
        //SocketConnection t2 = new SocketConnection();
        //t2.start();
    }
    
}

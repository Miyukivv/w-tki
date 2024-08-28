
package org.example;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;

import static org.example.ImageProcessor.clamp;

//Klasa powstała do obsługi wątków
//Runnable mówi co wątek ma robić, gdy się uruchomi
public class ThreadJob implements Runnable {

    private int end, begin, level;
    private BufferedImage image;

    public ThreadJob(int begin, int end, int level, BufferedImage image) {
        this.end = end;
        this.begin = begin;
        this.level = level;
        this.image = image;
    }

    @Override
    public void run() {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = begin; y < end; y++) {
                int rgb = image.getRGB(x, y);

                int b = rgb & 0XFF;
                int g = (rgb & 0XFF00) >> 8;
                int r = (rgb & 0XFF0000) >> 16;

                int newB = clamp(b + level, 0, 255);
                int newG = clamp(g + level, 0, 255);
                int newR = clamp(r + level, 0, 255);

                image.setRGB(x, y, (newR << 16) + (newG << 8) + newB);

            }
        }
    }
}

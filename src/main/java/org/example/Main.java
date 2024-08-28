package org.example;

import java.awt.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        ImageProcessor imp=new ImageProcessor();
        imp.readImage("src/gorski.jpg");
        imp.increaseBrigthness(50);
        imp.writeImage("src/gorskiii.jpg");
        int [] blue=imp.calculateHistogram("blue");
        int [] green=imp.calculateHistogram("green");
        int [] red=imp.calculateHistogram("red");

        System.out.println();
        System.out.println(red[51]);

        System.out.println(imp.calculateHistogram("green"));
        imp.saveHistogramImage("src/green.jpg", imp.generateHistogramImage(green));
        System.out.println(imp.calculateHistogram("red"));
        imp.saveHistogramImage("src/red.jpg", imp.generateHistogramImage(red));
        System.out.println(imp.calculateHistogram("blue"));
        imp.saveHistogramImage("src/blue.jpg", imp.generateHistogramImage(blue));
    }
}
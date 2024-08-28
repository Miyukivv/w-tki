package org.example;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ImageProcessor {
    private BufferedImage image;

    /*Napisz klasę posiadającą:
    - metodę, która otrzyma ścieżkę i wczyta obraz do pola klasy typu BufferedImage,*/

    public void readImage(String path){ //To nam da wartosc (read -> select)
        File imgFile = new File(path);

        try {
            image=ImageIO.read(imgFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//- metodę, która zapisze obraz z tego pola do podanej ścieżki.
    public void writeImage(String path){
        File imgFileToSave = new File(path);

        try {
            ImageIO.write(image, "jpg", imgFileToSave); //To nam nie da wartosci -> creat (wiec nie przypisujemy do image)

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Metoda do rozjaśniania (2 zadania)
    public static int clamp(int value, int min, int max){
        if (value > max)
            return max;
        if (value<min)
            return min;
        return value;
        }
    //Dodaj metodę, która zwiększy jasność obrazu o podaną stałą.

    /* Obraz -> Zbiór pixeli; tablica trójwymiarowa (każdy pixel ma osobno czerwony,zielony,niebieski; jeden to szerokość, drugi wysokość,
    a trzeci to RGB (trzeci wymiar jest umowny, więc można go potraktować jako nie wymiar)

    obrazek -> tablica 2x2

    Mamy kolor 0,0,0 kolor czarny
     255,255,255 -> kolor biały (bo 8bitów, kolor łącznie ma 24 bitów)
     */

    /*Musimy przeiterować po szerokości, wysokości, a potem po RGB*/
    public void  increaseBrigthness(int level) {

        //Iterujemy po szerokości
        for (int x = 0; x < image.getWidth(); x++) {
            //Iterujemy po wysokości
            for (int y = 0; y < image.getHeight(); y++) {
                //Musimy pobrać 24-bitową liczbę
                int rgb = image.getRGB(x, y);
                //Musimy zrobic dzielenie modulo 8 dla niebieskiego, zielonego, czerwonego

                //123456 789 -> odpadaja  trzy cyfry -> 123456 zostaje
                //(255,255,255) -> odcinamy 255 z prawej; to dostajemy sie do zielonego, odcinamy kolejne 255 (255,0,0) -> to do czerwonego

                int b = rgb & 0XFF; //0XFF -> 255
                int g = (rgb & 0XFF00) >> 8; //środkowa, przesunięcie bitowe o 8
                int r = (rgb & 0XFF0000) >> 16; //ostatni, czerwony, przesunięcie  o 16

                //Będziemy dodawać do każdego koloru to, o ile chcemy rozjaśnić: (Musimy mieć metodę, która nam da zakres o ile max możemy rozjaśnić, o ile min możemy rozjaśnić

                int newB = clamp(b + level, 0, 255);
                int newG = clamp(g + level, 0, 255);
                int newR = clamp(r + level, 0, 255);

                image.setRGB(x, y, (newR << 16) + (newG << 8) + newB);
            }
        }
    }
    /*
    Dodaj metodę, która wykona to samo działanie, dzieląc zadanie na określoną liczbę wątków.
    Liczbę wątków należy powiązać z liczbą dostępnych rdzeni procesora. Porównaj czas wykonania obu metod.
     */
    public void increaseBrightnessWithThreads(int level){
        int threadCount = Runtime.getRuntime().availableProcessors(); //Ile mamy dostepnych rdzeni procesora(to nam sie do tego dobiera)
        //Dzielimy obrazek na kilka części (kilka wątków będzie to wywoływało)

        //Zbadać jaka jest szerokość i wysokość, podzielić to przez liczbę wątków
        int chunk = image.getHeight()/threadCount;

        //Tworzenie wątków
        Thread[] threads=new Thread[threadCount];

        //Pętla która leci po wszystkich wątkach; rozdzielamy robotę
        for (int i=0; i<threadCount; i++){
            //jak mamy 1000 wierszy i dziele na 4 wątki; pierwszy będzie od 0 do 250
            //musimy powiedzieć gdzie się zaczyna robota, gdzie się kończy:
            int begin =i*chunk; //bo dla pierwszego to 0, ale dla drugiego juz 250... gdyby byl tylko pierwszy to by było samo i
            int end;

            if (i==threadCount-1){ //to ostatni wątek (te części możliwe, że nie będą sobie równe)
                end=image.getHeight()-(threadCount-1)*chunk; // threadCount-1*chunk -> to to samo co 3*chunk
            } else{
                end=begin+chunk;
            }
            threads[i]=new Thread(new ThreadJob(begin,end,level,image)); //w nawiasie metoda którą ma to wykonywać -> metoda lub klasa, my wybieramy klasę
        }
        //Może się okazać że ostatni wątek będzie wykonywał swoją pracę najszybciej, więc musimy kazać jemu poczekać na skończenie pracy innych wątków

        for (int i=0; i<threadCount; i++){
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
Zadanie 4.
Dodaje metodę, która wykona to samo działanie w oparciu o pulę wątków.
Jeden wątek powinien obsłużyć jeden wiersz obrazu. Dodaj czas wykonania tej metody do porównania.
     */

    public void setBrightnessThreadPool(int level){
        //Ustalenie liczby wątków:
        int threadsCount = Runtime.getRuntime().availableProcessors();

        //Tworzenie puli wątków:
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);

        //Przydzielenie zadań do wątków
        for (int i=0; i<image.getHeight(); i++) {
            final int y=i;
            executor.execute(() -> { //Przekazanie kodu do wykonania dla puli wątków
                for (int x=0; x<image.getWidth(); x++){
                    int rgb = image.getRGB(x,y);
                    int b = rgb & 0xFF;
                    int g = (rgb & 0xFF00) >> 8;
                    int r = (rgb & 0xFF0000) >> 16;
                    b = clamp(b + level, 0, 255);
                    g = clamp(g + level, 0, 255);
                    r = clamp(r + level, 0, 255);
                    rgb = (r << 16) + (g << 8) + b;
                    image.setRGB(x, y, rgb);
                }
            });
        }
        executor.shutdown();

        /*czekamy, aż wszystkie wątki zakończą swoje zadania (awaitTermination),
         ustawiając limit czasu na 5 sekund. Jeśli po upływie tego czasu jakieś wątki nadal będą działać,
         metoda zwróci false.*/

        try {
            boolean b = executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /*
    Zadanie 5.
    Napisz metodę, która w oparciu o pulę wątków obliczy histogram wybranego kanału obrazu.
     */
    public int [] calculateHistogram(String channel){
        int threadCount=Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        int[] histogram=new int[256];

        for (int y=0; y<image.getHeight(); y++){
            final int row = y;
            executor.execute(() -> { //jeden wątek -> jeden wiersz
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, row);

                    int value;

                    //channel.toLowerCase() ma na celu zapewnienie, że nazwa kanału (tj. "red", "green", "blue")
                    //będzie porównywana w jednolity sposób, niezależnie od tego, czy została podana wielkimi, czy małymi literami.
                    // Oznacza to, że metoda będzie działać poprawnie, niezależnie od tego, czy użytkownik poda nazwę kanału jako "Red", "RED", "rEd", itp.

                    switch (channel.toLowerCase()) {
                        case "red":
                            value = (rgb >> 16) & 0xFF;
                            break;
                        case "green":
                            value = (rgb >> 8) & 0xFF;
                            break;

                        case "blue":
                            value = (rgb & 0xFF);
                            break;
                        default:
                            throw new IllegalArgumentException("Nieprawidłowy kanał: " + channel);
                    }

                    /*
                     synchronized (histogram) {....}  - Oznacza to, że tylko jeden wątek na raz może modyfikować tablicę histogram,
                    co zapobiega problemom związanym z równoczesnym dostępem do tej tablicy przez wiele wątków
                     */
                    synchronized (histogram) {
                        histogram[value]++;
                    }
//                    if(value == 51) {
//                        System.out.print("d");
//                    }
                }
            });
        }
//        }});
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return histogram;
    }

    /*
 Zadanie 6.
    Napisz metodę, która przyjmie histogram obrazu i wygeneruje obraz przedstawiający wykres tego histogramu.
     */
    public BufferedImage generateHistogramImage(int [] histogram){
        int width=256; //Szerokość obrazu histogramu
        int height=500; //Wysokość obrazu histogramu

        BufferedImage histogramImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = histogramImage.createGraphics();

        //Ustawianie tła na biało
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0,0,width,height);

        //Normalizacja histogramu do wysokości obrazu
        int max = 0;
        for (int value : histogram){
            if (value > max) {
                max = value;
            }
        }

        //Rysowanie histogramu
        g2d.setColor(Color.BLACK);

        for (int i=0; i<histogram.length; i++){
            int value = histogram[i];

            //To linia kodu obliczająca wysokość słupka histogramu na obrazie.
            int barHeight=(int)((double) value / max * height);
            g2d.drawLine(i,height,i,height - barHeight);
        }
        g2d.dispose();
        return histogramImage;
    }

    public void saveHistogramImage(String path, BufferedImage histogramImage){
        File imgFileToSave = new File(path);

        try {
            ImageIO.write(histogramImage,"jpg",imgFileToSave);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

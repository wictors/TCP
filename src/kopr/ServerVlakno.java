package kopr;

import java.io.*;
import java.io.OutputStream;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerVlakno implements Runnable {

    private File SUBOR;
    private Socket soket;
    private int poradie;
    private int pocetVlakien;
    private OutputStream outStream;
    private int velkost = 10000;
    private int chunks;
    private byte[] data;
    private RandomAccessFile raf;
    private int offset;
    private int castSuboru;

    public ServerVlakno(Socket soket, int poradie, int pocetVlakien, File subor) {
        this.soket = soket;
        this.poradie = poradie;
        this.pocetVlakien = pocetVlakien;
        this.SUBOR = subor;
        
    }

    @Override
    public void run() {
        try {
            raf = new RandomAccessFile(SUBOR, "r");
            outStream = soket.getOutputStream(); 
            castSuboru = (int) Math.ceil((double) SUBOR.length() / pocetVlakien);
            offset = castSuboru*poradie;
            chunks = (int)Math.ceil((double) castSuboru/velkost);
            raf.seek(offset);
            data = new byte[velkost];
            for (int i = 0; i < chunks; i++) {
                int precitane = raf.read(data);
                offset = offset + precitane;
               // System.out.println("Soket " + poradie + " posiela " + precitane);
                if (precitane <= 0) {
                    break;
                }
                outStream.write(data, 0, precitane);
                outStream.flush();                
            }
        } catch (IOException ex) {
            System.err.println("Koniec !");
        }

    }

}

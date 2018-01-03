package kopr;

import java.io.*;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerVlakno implements Runnable {

    private final File SUBOR;
    private final Socket soket;
    private final int poradie;
    private final int pocetVlakien;
    private OutputStream outStream;
    private final int velkost = 10000;
    private int chunks;
    private byte[] data;
    private RandomAccessFile raf;
    private int offset;
    private int castSuboru;
    private final CountDownLatch cdl;
    private ArrayList<Integer> info;

    public ServerVlakno(Socket soket, int poradie, int pocetVlakien,
            File subor, CountDownLatch cdl, ArrayList<Integer> info) {
        this.soket = soket;
        this.poradie = poradie;
        this.pocetVlakien = pocetVlakien;
        this.SUBOR = subor;
        this.cdl = cdl;
        this.info = info;
        
        
    }

    @Override
    public void run() {
        try {
            raf = new RandomAccessFile(SUBOR, "r");
            outStream = soket.getOutputStream(); 
            castSuboru = (int) Math.ceil((double) SUBOR.length() / pocetVlakien);
            
            if(info.size() > 1){
                offset = info.get(poradie+2);
            }else{          
                offset = castSuboru * poradie;
            }
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
            
            outStream.write(new byte[0]);
            outStream.flush();
     
        } catch (IOException ex) {
            koniec();
        }finally{
            koniec();        
        }

    }
    private void koniec(){
        try {
                raf.close();
                outStream.close();
                soket.close();
                cdl.countDown();
            } catch (IOException ex) {
                Logger.getLogger(ServerVlakno.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

}

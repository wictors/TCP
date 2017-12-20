package kopr;

import java.io.*;
import java.net.*;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientVlakno implements Runnable {

    private final File SUBOR;
    private final Socket soket;
    private final int pocetVlakien;
    private final int poradie;
    private InputStream inStream;
    private final int velkost = 10000;
    private byte[] data;
    private RandomAccessFile raf;
    private int offset;
    private final Long velkostSuboru;
    private int castSuboru;
    private final int[] prijate;
    private final String[] spravy;
    private final CountDownLatch cdl;

    public ClientVlakno(Socket soket, int poradie, int pocetVlakien, long velkostSuboru, int[] prijate, CountDownLatch cdl,
            File file, String[] spravy) {
        this.soket = soket;
        this.poradie = poradie;
        this.pocetVlakien = pocetVlakien;
        this.velkostSuboru = velkostSuboru;
        this.prijate = prijate;
        this.cdl = cdl;
        this.SUBOR = file;
        this.spravy = spravy;
    }

    @Override
    public void run() {
        try {
            raf = new RandomAccessFile(SUBOR, "rw");
            inStream = soket.getInputStream();
            castSuboru = (int) Math.ceil((double) velkostSuboru / pocetVlakien);
            offset = castSuboru * poradie;
            raf.seek(offset);
           
            while (true) {
                data = new byte[velkost];
                int prislo = inStream.read(data);
                if ( prislo <= 0){
                    break;
                }                   
                raf.write(data, 0, prislo);
                offset = offset + prislo;
                prijate[poradie] += prislo;
                //System.out.println("Soket " + poradie + " prijal " + prislo);
                if (Thread.currentThread().isInterrupted()) {
                    zrus();
                }             
            }       
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ClientVlakno.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            System.err.println("IO Exception !!!");
        }finally{
            try {
                raf.close();
                inStream.close();
                soket.close();             
            } catch (IOException ex) {
                Logger.getLogger(ClientVlakno.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void zrus() {
        try {
            raf.close();
            soket.close();
            String sprava = new String(Integer.toString(offset));
            spravy[poradie] = sprava;
            Thread.currentThread().interrupt();
            System.out.println("Soket " + poradie + "skoncil na " + offset );
            cdl.countDown();
        } catch (IOException ex) {
            System.err.println("IO Exception TAOT ?  !!!");
        }
        
    }

}

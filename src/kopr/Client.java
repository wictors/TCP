package kopr;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import static kopr.Server.prenos;

public class Client {

    //public static int pocetVlakien = 3;
    private static File SUBOR = new File("C:\\Users\\Admin\\Desktop\\novy\\how.avi");
    private static File ZAPISNIK = new File("C:\\Users\\Admin\\Desktop\\novy\\text.txt");
    private static Socket[] sokety;
    private static int velkostSuboru;
    public static String[] spravy;
    public static int[] prijate;
    private static ExecutorService exekutor;
    private static CountDownLatch cdl;
    private static Form form;
   

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                form = new Form();
                form.setVisible(true);
                
            }
        });       
    }

    public static Integer komunikacia(int pocetSoketov) {
        try {
            spravy = new String[pocetSoketov];
            prijate = new int[pocetSoketov];
            Socket komSocket = new Socket(Server.siet, Server.PORT1);
            OutputStream outStream = komSocket.getOutputStream();
            String text = Integer.toString(pocetSoketov);
            System.out.println(text);
            byte[] sprava = text.getBytes(StandardCharsets.UTF_8);
            outStream.write(sprava);
            outStream.flush();
            InputStream inStream = komSocket.getInputStream();
            sprava = new byte[1024];
            inStream.read(sprava);
            text = new String(sprava, Charset.defaultCharset());
            text = text.trim();
            System.out.println("Velkost " + text);
            velkostSuboru = Integer.parseInt(text);

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return velkostSuboru;
    }

    public static void prenos(int pocetVlakien) {
        sokety = new Socket[pocetVlakien];
        cdl = new CountDownLatch(pocetVlakien);
        exekutor = Executors.newFixedThreadPool(pocetVlakien);
        for (int i = 0; i < pocetVlakien; i++) {
            try {
                sokety[i] = new Socket(Server.siet, Server.PORT2);
                exekutor.execute(new ClientVlakno(sokety[i], i, pocetVlakien, velkostSuboru, prijate, cdl, SUBOR, spravy));
            } catch (IOException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void stop() throws InterruptedException{
        exekutor.shutdownNow();
        cdl.await();
        System.out.println("Brana sa otvorila");
        exekutor.shutdown();
        SUBOR.delete();
        form.dispose();
        System.exit(0);
    }
    
    public static void pauza() throws InterruptedException{
        exekutor.shutdownNow();
        cdl.await();
        System.out.println("Brana sa otvorila");
        exekutor.shutdown();
    }

}

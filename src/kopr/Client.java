package kopr;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
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

    private static File SUBOR = new File("C:\\Users\\Admin\\Desktop\\prijma\\film.avi");
    private static File ZAPISNIK = new File("C:\\Users\\Admin\\Desktop\\prijma\\text.txt");
    private static Socket[] sokety;
    private static int velkostSuboru = 0;
    public static String[] spravy;
    private static ExecutorService exekutor;
    private static CountDownLatch cdl;
    private static Form form;
    private static ArrayList<Integer> info = new ArrayList<>();
    private static int pocetS;
    private static int prijate = 0;
    private static Priebeh priebeh;

    public static void main(String[] args) {
        boolean prerusenie;
        priebeh = new Priebeh();
        prerusenie = ZAPISNIK.length() > 0;
        if(prerusenie){
            citajStav();
            prijate = info.get(1);
        }       
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {              
                    form = new Form(prerusenie, prijate, priebeh);
                    form.setVisible(true);               
            }
        });
    }

    public static Integer komunikacia(int pocetSoketov) {
        try {
            if (pocetSoketov > -1){
                info.add(pocetSoketov);
                pocetS = pocetSoketov;
            }
            spravy = new String[pocetS];   
            
            Socket komSocket = new Socket(Server.siet, Server.PORT1);
            ObjectOutputStream outStream = new ObjectOutputStream(komSocket.getOutputStream());  
            ObjectInputStream inStream = new ObjectInputStream(komSocket.getInputStream());
            
            outStream.writeObject(info);            
            outStream.flush();
                                             
            
            velkostSuboru = inStream.readInt();
            System.out.println("Velkost " + velkostSuboru);
            inStream.close();
            outStream.close(); 
            komSocket.close();

        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        return velkostSuboru;
    }

    public static void prenos() {
        sokety = new Socket[pocetS];
        cdl = new CountDownLatch(pocetS);
        exekutor = Executors.newFixedThreadPool(pocetS);
        try {
            for (int i = 0; i < pocetS; i++) {
                sokety[i] = new Socket(Server.siet, Server.PORT2);
                exekutor.execute(new ClientVlakno(sokety[i], i, pocetS, velkostSuboru, cdl, SUBOR, spravy, priebeh, info));
            }
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void stop() throws InterruptedException {
        exekutor.shutdownNow();
        cdl.await();
        System.out.println("Brana sa otvorila");
        exekutor.shutdown();
        SUBOR.delete();
        form.dispose();
        System.exit(0);
    }

    public static void pauza() throws InterruptedException {
        exekutor.shutdownNow();
        cdl.await();
        System.out.println("Brana sa otvorila");
        exekutor.shutdown();
        try {
            PrintWriter writer = new PrintWriter(ZAPISNIK);
            writer.println(spravy.length);
            writer.println(priebeh.dajPriebeh());
            for (int i = 0; i < spravy.length; i++) {
                writer.println(spravy[i]);
            }
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            form.dispose();
            System.exit(0);
        }

    }
    
    public static void citajStav() {
        if (ZAPISNIK.length() != 0) {
            try {
                Scanner scan = new Scanner(ZAPISNIK);                
                while (scan.hasNext()) {               
                    info.add(scan.nextInt());                                    
                }
                pocetS = info.get(0);               
                scan.close();
                System.out.println("Pocet " + pocetS + " miesta " + Arrays.toString(info.toArray()));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    

}

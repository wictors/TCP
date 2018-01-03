package kopr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static File SUBOR = new File("D:\\odosiela\\film.avi");
    private static int velkostSuboru = (int) SUBOR.length();
    private static int pocetVlakien = 0;
    public static final String siet = "localhost";
    public static final int PORT1 = 6789;
    public static final int PORT2 = 12345;
    private static Socket[] sokety;
    private static ServerSocket server;
    private static ServerSocket komunikacny;
    private static ExecutorService exekutor;
    private static CountDownLatch cdl;
    private static ArrayList<Integer> info = new ArrayList<>();
    
    public static void main(String[] args) throws ClassNotFoundException {
        komunikacia();
    }
    
    public static void komunikacia() throws ClassNotFoundException{
        try {
            komunikacny = new ServerSocket(PORT1);
            Socket komSocket = komunikacny.accept();
            ObjectInputStream inStream = new ObjectInputStream(komSocket.getInputStream());
            ObjectOutputStream outStream = new ObjectOutputStream(komSocket.getOutputStream());
            
            info = (ArrayList<Integer>)inStream.readObject();
            System.out.println("Pole " + Arrays.toString(info.toArray()));                    
            pocetVlakien = info.get(0);
            System.out.println(pocetVlakien);          
            
            
            outStream.writeInt(velkostSuboru);
            outStream.flush();
            inStream.close();
            outStream.close();
            komunikacny.close();
            if (pocetVlakien > 0) {
                prenos();
            } 
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void prenos() throws ClassNotFoundException {
        cdl = new CountDownLatch(pocetVlakien);
        sokety = new Socket[pocetVlakien];
        exekutor = Executors.newFixedThreadPool(pocetVlakien);
        try {
            server = new ServerSocket(PORT2);
            for (int i = 0; i < pocetVlakien; i++) {
                sokety[i] = server.accept();
                exekutor.execute(new ServerVlakno(sokety[i], i, pocetVlakien, SUBOR, cdl, info));
            }
            cdl.await();
            exekutor.shutdown();
            server.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            komunikacia();
        }
    }

}

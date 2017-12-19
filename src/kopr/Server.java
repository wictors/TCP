package kopr;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {

    private static File SUBOR = new File("D:\\test\\how.avi");
    public static int velkostSuboru = (int) SUBOR.length();
    private static int pocetVlakien = 0;
    public static final String siet = "localhost";
    public static final int PORT1 = 6789;
    public static final int PORT2 = 12345;
    private static Socket[] sokety;
    private static ServerSocket server;

    public static void main(String[] args) {
        try {
            ServerSocket komunikacia = new ServerSocket(PORT1);
            Socket komSocket = komunikacia.accept();
            InputStream inStream = komSocket.getInputStream();
            byte[] sprava = new byte[10];
            inStream.read(sprava);
            String text = new String(sprava, Charset.defaultCharset());
            text = text.trim();
            pocetVlakien = Integer.parseInt(text);
            System.out.println(pocetVlakien);
            OutputStream outStream = komSocket.getOutputStream();
            text = Integer.toString(velkostSuboru);
            sprava = text.getBytes(StandardCharsets.UTF_8);
            outStream.write(sprava);
            outStream.flush();
            if (pocetVlakien > 0) {
                prenos();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void prenos() {
        sokety = new Socket[pocetVlakien];
        Executor exekutor = Executors.newFixedThreadPool(pocetVlakien);
        try {
            server = new ServerSocket(PORT2);
            for (int i = 0; i < pocetVlakien; i++) {
                sokety[i] = server.accept();
                exekutor.execute(new ServerVlakno(sokety[i], i, pocetVlakien, SUBOR));
            }

        } catch (IOException ex) {
            //Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Chyba server");
        }
    }

}

import java.io.*;
import java.net.*;
import java.text.DecimalFormat;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class GameServer {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final DecimalFormat decfor = new DecimalFormat("0.00"); 
    public static int number = new Random().nextInt(100);

    public static void main(String[] args) {
        if (args.length < 1) return;
 
        int port = Integer.parseInt(args[0]);

        System.out.println("Secret number: " + number);
 
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
 
            while (true) {
            Socket socket = serverSocket.accept();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);

                        String name = reader.readLine();

                        System.out.println("New client connected: "+ name);

                        long start = System.nanoTime();

                        Integer num = Integer.valueOf(reader.readLine());
                        
                        while (num != number) {
                            writer.println("Wrong Guess: " + num);
                            num = Integer.valueOf(reader.readLine());
                        }

                        writer.println("Right Guess!");

                        long end = System.nanoTime();
                        double time = Math.round((end - start) * 1e-9 * 100.0) / 100.0;

                        writer.println("Time: " + time + " seconds");

                        System.out.println("Client " + name + " guessed in " +  time + " seconds!");

                    } catch (IOException ex) {
                        System.out.println("Server exception: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            }).start();
        }
 
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
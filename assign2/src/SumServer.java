import java.io.*;
import java.net.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This program demonstrates a simple TCP/IP socket server.
 *
 * @author www.codejava.net
 */
public class SumServer {
    private static final ReentrantLock lock = new ReentrantLock();
    public static int globalSum = 0;

    public static void main(String[] args) {
        if (args.length < 1) return;
 
        int port = Integer.parseInt(args[0]);
 
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
 
            while (true) {
            Socket socket = serverSocket.accept();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int localSum = 0;

                        InputStream input = socket.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output, true);

                        String name = reader.readLine();

                        System.out.println("New client connected: "+ name);

                        Integer num = null;
                        do {
                            num = Integer.valueOf(reader.readLine());
                            localSum += num;
                            writer.println("Current Sum: " + localSum);
                        } while (num != 0);

                        lock.lock();
                        try {
                            globalSum += localSum;
                            writer.println("Global Sum: " + globalSum);
                        } finally {
                            lock.unlock();
                        }
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
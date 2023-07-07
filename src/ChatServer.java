// This program was written by Peter Ye. Peter Ye used Stack Overflow for graphics.
// This program runs Server end of the code.

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatServer {
    public static ConcurrentHashMap<String, LinkedBlockingQueue<String>> nameToLinkedBlockingQueue = new ConcurrentHashMap<>();

    private static int port = 8012;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(port);
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client request received : " + socket);
            ChatConnectionServerEnd chatConnection = new ChatConnectionServerEnd(socket);
        }
    }
}
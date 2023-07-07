import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.LinkedBlockingQueue;

class ChatConnectionServerEnd{
    private Socket socket;
    private Scanner scanner = new Scanner(System.in);
    private PrintStream printStream;
    private BufferedReader bufferedReader;
    private String name;
    LinkedBlockingQueue<String> messages;

    ChatConnectionServerEnd(Socket socket) throws IOException, InterruptedException {
        messages = new LinkedBlockingQueue<>(10);
        this.socket = socket;

        // I initialize PrintStream and BufferedReader.
        OutputStream outputStream = socket.getOutputStream();
        printStream = new PrintStream(outputStream);
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        bufferedReader = new BufferedReader(inputStreamReader);
        // Name takes the username of the person.
        name = bufferedReader.readLine();
        ChatServer.nameToLinkedBlockingQueue.put(name, messages);
        ChatServer.nameToLinkedBlockingQueue.get(name).put("Your username is: " + name);

        // Puts the names of people already currently in chat in the GUI.
        for (LinkedBlockingQueue<String> value : ChatServer.nameToLinkedBlockingQueue.values()) {
            value.put("#" + name + " has joined chat");
        }
        for (String input : ChatServer.nameToLinkedBlockingQueue.keySet()) {
            if (input != name)
                ChatServer.nameToLinkedBlockingQueue.get(name).put("#" + input + " is already in chat.");
        }
        SendMessage sendMessage = new SendMessage();
        ReadMessage readMessage = new ReadMessage();
        sendMessage.start();
        System.out.println("Listener Activated");
        readMessage.start();
        System.out.println("Speaker Activated");
    }

    class SendMessage extends Thread {

        public void run() {
            try {
                String line;
                do {
                    // takes message from the sockets own linkedBlockingQueue.
                    line = messages.take();
                    printStream.println(line);
                } while (!line.equals("QUIT!"));
                System.out.println(line + " is the line");
                System.out.println("ChatSpeaker finished");
                socket.shutdownInput();
            } catch (IOException | InterruptedException e) {
                System.out.println("ACK! ACK!! It's an Speaker Exception!!");
                System.out.println(e);
            }
        }
    }

    class ReadMessage extends Thread {

        public void run() {
            try {
                String message;
                do {
                    message = bufferedReader.readLine();
                    //Contains @ means the message is directed to someone.
                    if (message != null) {
                        if (message.contains("@")) {
                            String messageParts[] = message.split("@");
                            if (ChatServer.nameToLinkedBlockingQueue.containsKey(messageParts[messageParts.length - 1])) {
                                //Appends a "*" so the client end knows to code the text blue.
                                messageParts[0] = "*" + messageParts[0];
                                (ChatServer.nameToLinkedBlockingQueue.get(messageParts[1])).put(messageParts[0]);
                            } else {
                                messages.put("#Server: Your message does not have a valid receiver.");
                            }
                        } else {
                            for (LinkedBlockingQueue<String> value : ChatServer.nameToLinkedBlockingQueue.values()) {
                                value.put("^" + message);
                            }
                        }
                    }
                }
                // message will be null if someone types "QUIT!".
                while (message != null);
                System.out.println("ChatListener finished");
                System.out.println("ChatSpeaker finished");
                //deletes a user if they leave the chatting system.
                ChatServer.nameToLinkedBlockingQueue.remove(name);
                for (LinkedBlockingQueue<String> value : ChatServer.nameToLinkedBlockingQueue.values()) {
                    try {
                        value.put("$" + name + " has left the server");
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
                try {
                    socket.shutdownOutput();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            } catch (InterruptedException | IOException e) {
                // This will happen if someone closes their GUI only. I still need to remove their username and linkedblockingqueue when this happens.
                System.out.println("ChatListener finished");
                System.out.println("ChatSpeaker finished");
                //deletes a user if they leave the chatting system.
                ChatServer.nameToLinkedBlockingQueue.remove(name);
                for (LinkedBlockingQueue<String> value : ChatServer.nameToLinkedBlockingQueue.values()) {
                    try {
                        value.put("$" + name + " has left the server");
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
                    }
                }
                try {
                    socket.shutdownOutput();
                    socket.shutdownInput();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                System.out.println("ACK! ACK!! It's an Listener Exception!!");
                System.out.println(e);
            }
        }
    }
}
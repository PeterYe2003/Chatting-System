// This program was written by Peter Ye. Peter Ye used Stack Overflow for graphics.
// This program runs the ChatClient with graphics.

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;


class ChatClient {
    private static String serverName = "cs.catlin.edu";
    private static int port = 8012;

    private JPanel myPanel;
    private JFrame myJFrame;
    private JTextField textField;
    private String name;
    private Socket socket;
    private PrintStream printStream;
    private BufferedReader bufferedReader;
    private JButton joinChatButton;
    private JScrollPane scrolling;

    public static void main(String[] args) {
        new ChatClient();
    }

    ChatClient() {

        myJFrame = new JFrame("Peter's Chatting System");
        myJFrame.setSize(200, 200);
        // Creates join chat button
        joinChatButton = new JButton("Join to Talk");
        joinChatButton.setMaximumSize(new Dimension(500, 200));
        joinChatButton.addActionListener(new MyButtonListener());

        myPanel = new JPanel();
        myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.PAGE_AXIS));
        myPanel.add(joinChatButton);

        scrolling = new JScrollPane(myPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrolling.setMinimumSize(new Dimension(200, 50));
        scrolling.setPreferredSize(new Dimension(500, 200));

        //Allows the panel to scroll.
        myJFrame.add(scrolling);

        textField = new JTextField("Username", 20);
        textField.setMaximumSize(new Dimension(100, 23));
        myJFrame.add(textField, BorderLayout.PAGE_END);
        myJFrame.pack();
        myJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        myJFrame.setLocationRelativeTo(null);
        myJFrame.setVisible(true);
    }

    //The username an argument in startSocket because the name is entered before the socket is started.
    public void startSocket(String username) throws IOException {
        socket = new Socket(serverName, port);
        OutputStream outputStream = socket.getOutputStream();
        printStream = new PrintStream(outputStream);
        InputStream inputStream = socket.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        bufferedReader = new BufferedReader(inputStreamReader);
        this.name = username;
        printStream.println(this.name);
        ReadMessage readMessage = new ReadMessage();
        textField.addActionListener(new myTextBoxListener());
        System.out.println("Listener Activated");
        readMessage.start();
        System.out.println("Speaker Activated");
    }


    class ReadMessage extends Thread {

        public void run() {
            try {
                String message;
                do {
                    message = bufferedReader.readLine();
                    if (message != null) {
                        // the Server appends * at the start of a message if it is a DM. If a message starts with *, client knows it's a DM.
                        if (message.charAt(0) == '*') {
                            message = message.substring(1);
                            JLabel messagePrint = new JLabel(message);
                            messagePrint.setForeground(new Color(0, 102, 255));
                            myPanel.add(messagePrint);
                            myJFrame.pack();
                            JScrollBar vertical = scrolling.getVerticalScrollBar();
                            vertical.setValue(vertical.getMaximum());
                        }
                        // the Server appends # at the start of a message if the message indicates someone is joining chat.
                        else if (message.charAt(0) == '#') {
                            message = message.substring(1);
                            JLabel messagePrint = new JLabel(message);
                            messagePrint.setForeground(new Color(255, 80, 80));
                            myPanel.add(messagePrint);
                            myJFrame.pack();
                            JScrollBar vertical = scrolling.getVerticalScrollBar();
                            vertical.setValue(vertical.getMaximum());
                        }
                        // the Server appends # at the start of a message if the message indicates someone is leaving chat.
                        else if (message.charAt(0) == '$') {
                            message = message.substring(1);
                            JLabel messagePrint = new JLabel(message);
                            messagePrint.setForeground(new Color(51, 153, 51));
                            myPanel.add(messagePrint);
                            myJFrame.pack();
                            JScrollBar vertical = scrolling.getVerticalScrollBar();
                            vertical.setValue(vertical.getMaximum());
                        } else if (message.charAt(0) == '^') {
                            message = message.substring(1);
                            JLabel messagePrint = new JLabel(message);
                            myPanel.add(messagePrint);
                            myJFrame.pack();
                            JScrollBar vertical = scrolling.getVerticalScrollBar();
                            vertical.setValue(vertical.getMaximum());
                        }
                    }
                } while (message != null);
            } catch (IOException e) {
                System.out.println("ACK! ACK!! It's an Listener Exception!!");
                System.out.println(e);
            }
        }
    }


    class MyButtonListener implements ActionListener {

        private String name;

        /* A function that will get called whenever the button is clicked. */
        public void actionPerformed(ActionEvent e) {
            name = textField.getText();
            if (!name.isBlank()) {
                textField.setText("");
                try {
                    startSocket(name);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                joinChatButton.setVisible(false);
            }
        }
    }

    class myTextBoxListener implements ActionListener {
        //This is run whenever the user presses enter.
        public void actionPerformed(ActionEvent e) {
            String line = textField.getText();
            textField.setText("");
            // So if the line is blank, it doesn't do anything but clears the textField.
            if (!line.isBlank()) {
                if (line.equals("QUIT!")) {
                    System.out.println("ChatSpeaker finished");
                    System.out.println("ChatListener finished");
                    try {
                        socket.shutdownOutput();
                        socket.shutdownInput();
                        System.exit(0);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    printStream.println(name + ": " + line);
                }
            }
        }
    }
}
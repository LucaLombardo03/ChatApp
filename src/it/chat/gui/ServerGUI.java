package it.chat.gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import javax.swing.border.TitledBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class ServerGUI extends JFrame implements ActionListener {
    //data per messaggio
    public static SimpleDateFormat formatter = new SimpleDateFormat("[hh:mm a]");
    private static HashMap<String, PrintWriter> connectedClients = new HashMap<>();
    //numero utenti massimi
    private static final int MAX_CONNECTED = 50;
    private static final int PORT = 5000;
    private static ServerSocket server;
    private static boolean EXIT = false;
    //base grafica
    private JPanel panel;
    private JTextArea txtAreaLogs;
    private JButton btnStart;
    private JLabel lblChatServer;

    public static void main(String[] args) {
        //parte la grafica visiva
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ServerGUI frame = new ServerGUI();
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    SwingUtilities.updateComponentTreeUI(frame);
                    System.setOut(new PrintStream(new TextAreaOutputStream(frame.txtAreaLogs)));
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //grafica
    public ServerGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 400);
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(panel);
        panel.setLayout(new BorderLayout(0, 0));
        TitledBorder titleBorder = new TitledBorder("Server");
        titleBorder.setTitleFont(new Font("Arial Black", Font.BOLD, 15));
        panel.setBorder(titleBorder);

        lblChatServer = new JLabel("SERVER");
        lblChatServer.setHorizontalAlignment(SwingConstants.CENTER);
        lblChatServer.setFont(new Font("Arial Black", Font.PLAIN, 30));
        panel.add(lblChatServer, BorderLayout.NORTH);

        btnStart = new JButton("PRESS TO START");
        btnStart.addActionListener(this);
        btnStart.setFont(new Font("Arial Black", Font.PLAIN, 30));
        panel.add(btnStart, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);

        txtAreaLogs = new JTextArea();
        txtAreaLogs.setBackground(Color.BLUE);
        txtAreaLogs.setForeground(Color.WHITE);
        txtAreaLogs.setLineWrap(true);
        scrollPane.setViewportView(txtAreaLogs);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btnStart) {
            if(btnStart.getText().equals("PRESS TO START")) {
                EXIT = false;
                start();
                btnStart.setText("PRESS TO STOP");
            }else {
                addToLogs("Chat server stopped...");
                EXIT = true;
                btnStart.setText("PRESS TO START");
            }
        }

        //Refresh UI
        refreshUIComponents();
    }

    public void refreshUIComponents() {
        lblChatServer.setText("SERVER" + (!EXIT ? ": "+PORT:""));
    }

    public static void start() {
        new Thread(new ServerHandler()).start();
    }

    public static void stop() throws IOException {
        if (!server.isClosed()) server.close();
    }

    private static void broadcastMessage(String message) {
        for (PrintWriter p: connectedClients.values()) {
            p.println(message);
        }
    }

    public static void addToLogs(String message) {
        System.out.printf("%s %s\n", formatter.format(new Date()), message);
    }

    private static class ServerHandler implements Runnable{
        @Override
        public void run() {
            try {
                server = new ServerSocket(PORT);
                addToLogs("Server started on port: " + PORT);
                addToLogs("Now listening for connections...");
                while(!EXIT) {
                    if (connectedClients.size() <= MAX_CONNECTED){
                        new Thread(new ClientHandler(server.accept())).start();
                    }
                }
            }
            catch (Exception e) {
                addToLogs("\nError occured: \n");
                addToLogs(Arrays.toString(e.getStackTrace()));
                addToLogs("\nExiting...");
            }
        }
    }

    // Start
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String name;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run(){
            addToLogs("Utente connesso: " + socket.getInetAddress());
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                // ciclo infinito
                for(;;) {
                    name = in.readLine();
                    if (name == null) {
                        return;
                    }
                    synchronized (connectedClients) {
                        if (!name.isEmpty() && !connectedClients.keySet().contains(name)) break;
                        else out.println("INVALIDNAME");
                    }
                }
                out.println("Benvenuto nella chat, " + name.toUpperCase() + "!");
                addToLogs(name.toUpperCase() + " si é unito.");
                broadcastMessage("[SYSTEM] " + name.toUpperCase() + " si é unito.");
                connectedClients.put(name, out);
                String message;
                out.println("Puoi unirti alla chat adesso...");
                while ((message = in.readLine()) != null && !EXIT) {
                    if (!message.isEmpty()) {
                        if (message.toLowerCase().equals("/quit")) break;
                        broadcastMessage(String.format("[%s] %s", name, message));
                    }
                }
            } catch (Exception e) {
                addToLogs(e.getMessage());
            } finally {
                if (name != null) {
                    addToLogs(name + " ha abbandonato la chat.");
                    connectedClients.remove(name);
                    broadcastMessage(name + " ha abbandonato");
                }
            }
        }
    }

}

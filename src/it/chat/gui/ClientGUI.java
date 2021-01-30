package it.chat.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.FlowLayout;
import javax.swing.JTextField;

public class ClientGUI extends JFrame implements ActionListener {
    private static Socket clientSocket;
    private static int PORT;
    private PrintWriter out;

    // base grafica
    private JPanel panel;
    private JTextArea txtAreaLogs;
    private JButton btnStart;
    private JPanel panelNorth;
    private JLabel lblChatClient;
    private JPanel panelCenter;
    private JLabel lblPort;
    private JLabel lblName;
    private JPanel panelSouth;
    private JButton btnSend;
    private JTextField txtMessage;
    private JTextField txtNickname;
    private JTextField txtPort;
    private String clientName;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ClientGUI frame = new ClientGUI();
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    SwingUtilities.updateComponentTreeUI(frame);
                    //Logs
                    System.setOut(new PrintStream(new TextAreaOutputStream(frame.txtAreaLogs)));
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public ClientGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 400);
        panel = new JPanel();
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(panel);
        panel.setLayout(new BorderLayout(0, 0));

        panelNorth = new JPanel();
        panel.add(panelNorth, BorderLayout.NORTH);
        panelNorth.setLayout(new BorderLayout(0, 0));

        lblChatClient = new JLabel("CLIENT");
        lblChatClient.setHorizontalAlignment(SwingConstants.CENTER);
        lblChatClient.setFont(new Font("Arial Black ", Font.BOLD, 40));
        panelNorth.add(lblChatClient, BorderLayout.NORTH);

        panelCenter = new JPanel();
        panelNorth.add(panelCenter, BorderLayout.SOUTH);
        panelCenter.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        lblName = new JLabel("Nickname");
        panelCenter.add(lblName);

        txtNickname = new JTextField();
        txtNickname.setColumns(10);
        panelCenter.add(txtNickname);

        lblPort = new JLabel("Port");
        panelCenter.add(lblPort);

        txtPort = new JTextField();
        panelCenter.add(txtPort);
        txtPort.setColumns(10);

        btnStart = new JButton("START");
        panelCenter.add(btnStart);
        btnStart.addActionListener(this);
        btnStart.setFont(new Font("Arial Black", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane();
        panel.add(scrollPane, BorderLayout.CENTER);

        txtAreaLogs = new JTextArea();
        txtAreaLogs.setBackground(Color.BLUE);
        txtAreaLogs.setForeground(Color.WHITE);
        txtAreaLogs.setLineWrap(true);
        scrollPane.setViewportView(txtAreaLogs);

        panelSouth = new JPanel();
        FlowLayout fl_panelSouth = (FlowLayout) panelSouth.getLayout();
        fl_panelSouth.setAlignment(FlowLayout.RIGHT);
        panel.add(panelSouth, BorderLayout.SOUTH);

        txtMessage = new JTextField();
        panelSouth.add(txtMessage);
        txtMessage.setColumns(50);

        btnSend = new JButton("SEND");
        btnSend.addActionListener(this);
        btnSend.setFont(new Font("Arial Black", Font.PLAIN, 12));
        panelSouth.add(btnSend);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == btnStart) {
            if(btnStart.getText().equals("START")) {
                btnStart.setText("STOP");
                start();
            }else {
                btnStart.setText("START");
                stop();
            }
        }else if(e.getSource() == btnSend) {
            String message = txtMessage.getText().trim();
            if(!message.isEmpty()) {
                out.println(message);
                txtMessage.setText("");
            }
        }
        //Refresh UI
        refreshUIComponents();
    }

    public void refreshUIComponents() {

    }
    //azione dopo aver premuto il pulsante start
    public void start() {
        try {
            PORT = Integer.parseInt(txtPort.getText().trim());
            clientName = txtNickname.getText().trim();
            clientSocket = new Socket("localhost", PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            new Thread(new Listener()).start();
            out.println(clientName);
        } catch (Exception err) {
            addToLogs("[ERROR] "+err.getLocalizedMessage());
        }
    }
    //azione dopo aver premuto il pulsante stop
    public void stop(){
        if(!clientSocket.isClosed()) {
            try {
                clientSocket.close();
            } catch (IOException e1) {}
        }
    }

    public static void addToLogs(String message) {
        //formattazione data + nome utente + messaggio
        System.out.printf("%s %s\n", ServerGUI.formatter.format(new Date()), message);
    }

    private static class Listener implements Runnable {
        private BufferedReader in;
        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String read;
                //ciclo infinito
                for(;;) {
                    read = in.readLine();
                    if (read != null && !(read.isEmpty())) addToLogs(read);
                }
            } catch (IOException e) {
                return;
            }
        }

    }
}
package sn.dic3.chatroom.chatroomrest.client;

import java.awt.*;
import java.awt.event.*;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.*;
import sn.dic3.chatroom.chatroomrest.Message;

import javax.swing.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatUserImpl {

    private final int REFRESH_RATE = 1;
    private String title = "Logiciel de discussion en ligne";
    private String pseudo = null;
    private Client client = ClientBuilder.newClient();
    private WebTarget target = client.target("http://localhost:8080/api/chatroom");
    private JFrame window = new JFrame(this.title);
    private JTextArea txtOutput = new JTextArea();
    private JTextField txtMessage = new JTextField();
    private JButton btnSend = new JButton("Envoyer");

    private Instant lastUpdate = Instant.now().minusSeconds(24 * 60 * 60);
    public ChatUserImpl() {
        this.createIHM();
        this.requestPseudo();
    }

    public void createIHM() {
        // Assemblage des composants
        JPanel panel = (JPanel)this.window.getContentPane();
        JScrollPane sclPane = new JScrollPane(txtOutput);
        panel.add(sclPane, BorderLayout.CENTER);
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(this.txtMessage, BorderLayout.CENTER);
        southPanel.add(this.btnSend, BorderLayout.EAST);
        panel.add(southPanel, BorderLayout.SOUTH);
        // Gestion des événements
        window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                window_windowClosing(e);
            }
        });
        btnSend.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnSend_actionPerformed(e);
            }
        });

        txtMessage.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent event) {
                if (event.getKeyChar() == '\n')
                    btnSend_actionPerformed(null);
            }
        });

        // Initialisation des attributs
        this.txtOutput.setBackground(new Color(220,220,220));
        this.txtOutput.setEditable(false);
        this.window.setSize(500,400);
        this.window.setVisible(true);
        this.txtMessage.requestFocus();
    }

    public void requestPseudo() {
        this.pseudo = JOptionPane.showInputDialog(
                this.window, "Entrez votre pseudo : ",
                this.title,  JOptionPane.OK_OPTION
        );
        if (this.pseudo == null) System.exit(0);
        Response response = target.path("subscribe").path(pseudo).request().get();
        if (response.getStatus() == 200) {
            //this.txtOutput.append(this.pseudo +" Connected \n");
            target.path("post-message").request().post(Entity.json(new Message("SYSTEM", pseudo+" Connected")));
        } else if (response.getStatus() == 409) {
            JOptionPane.showMessageDialog(this.window, "Pseudo existe deja! Veuillez en choisir un autre");
            requestPseudo();
        }
        startPolling();
    }

    public void window_windowClosing(WindowEvent e) {
        Response response = target.path("unsubscribe").path(pseudo).request().get();
        target.path("post-message").request().post(Entity.json(new Message("SYSTEM", pseudo+" Disconnected")));
        if (response.getStatus() == 200) {
            System.exit(-1);
        }
    }

    public void startPolling() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            List<Message> messages = receiveMessages();
            if (!messages.isEmpty()) {
                for (Message message : messages) {
                    txtOutput.append(message.getPseudo() + " >>> " + message.getContent() + "\n");
                }
            }
        }, 0, REFRESH_RATE, TimeUnit.SECONDS);
    }


    public List<Message> receiveMessages() {
        List<Message> messages = new ArrayList<>();
        Response response = target.path("messages").path(lastUpdate.toEpochMilli()+"").request().get();
        if (response.getStatus() == 200) {
            messages = response.readEntity(new GenericType<List<Message>>() {});
        }
        lastUpdate = Instant.now();
        return messages;
    }


    public void btnSend_actionPerformed(ActionEvent e) {
        String message = this.txtMessage.getText();
        Response response = target.path("post-message").request().post(Entity.json(new Message(pseudo, message)));
        this.txtMessage.setText("");
        this.txtMessage.requestFocus();
    }

    public static void main(String[] args) {
        new ChatUserImpl();
    }
}
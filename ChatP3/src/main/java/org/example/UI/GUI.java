// package org.example.UI;

// import javax.swing.*;
// import java.awt.*;
// import java.awt.event.*;
// import java.io.File;
// import java.util.ArrayList;
// import java.util.logging.Level;
// import java.util.logging.Logger;

// public class GUIChat implements ActionListener {
//     public static String Nombre;
//     public int statusOp;
//     public int salida;
//     private JFrame ventana;
//     private JTabbedPane chats;
//     private JPanel panel;
//     private ArrayList<JTextArea> chatPersona;
//     private ArrayList<String> contactos;
//     private ArrayList<String> contactosChat;
//     private ArrayList<JTextField> textoEnviar;
//     private ArrayList<JButton> botonesEnviar;

//     public GUIChat(int operacion) {
//         statusOp = operacion;
//         salida = 0;
//         Nombre = JOptionPane.showInputDialog("Ingresa el nombre con el que te deseas identificar");
//         while (Nombre.isEmpty()) {
//             Nombre = JOptionPane.showInputDialog("Ingresa el nombre con el que te deseas identificar");
//         }
//         ventana = new JFrame();
//         ventana.setSize(500, 500);
//         ventana.setTitle(Nombre + ": Chat multicast");
//         ventana.setLocationRelativeTo(null);
//         ventana.setResizable(false);
//         ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//         ventana.addWindowListener(new java.awt.event.WindowAdapter() {
//             @Override
//             public void windowClosing(WindowEvent we) {
//                 statusOp = 1;
//                 salida = 1;
//                 try {
//                     Thread.sleep(1000);
//                 } catch (InterruptedException ex) {
//                     Logger.getLogger(GUIChat.class.getName()).log(Level.SEVERE, null, ex);
//                 }
//             }
//         });

//         chatPersona = new ArrayList<>();
//         contactosChat = new ArrayList<>();
//         contactos = new ArrayList<>();
//         textoEnviar = new ArrayList<>();
//         botonesEnviar = new ArrayList<>();

//         inicializarComponentes();
//         ventana.setVisible(true);
//     }

//     private void inicializarComponentes() {
//         panel = new JPanel();
//         JLabel titulo = new JLabel("Tus chats: " + Nombre);
//         panel.setLayout(null);
//         ventana.getContentPane().add(panel);
//         titulo.setBounds(10, 10, 180, 30);
//         panel.add(titulo);

//         setButtons();
//         setGeneral();
//     }

//     private void setButtons() {
//         JButton newChat = new JButton();
//         newChat.addActionListener(this);
//         newChat.setText("Nuevo chat privado");
//         newChat.setBounds(290, 10, 200, 30);
//         panel.add(newChat);
//     }

//     private void setGeneral() {
//         chats = new JTabbedPane();
//         chats.setBounds(10, 50, 480, 400);
//         newChat("General");
//         panel.add(chats);
//     }

//     private void newChat(String nombre) {
//         JPanel newPanel = new JPanel();
//         newPanel.setLayout(null);
//         chats.addTab(nombre, newPanel);

//         JTextArea conversacion = new JTextArea();
//         conversacion.setEditable(false);
//         conversacion.setForeground(Color.BLACK);
//         chatPersona.add(conversacion);
//         JScrollPane scroll = new JScrollPane(chatPersona.get(chatPersona.size() - 1));
//         scroll.setBounds(10, 10, 455, 300);
//         newPanel.add(scroll);

//         JTextField texto = new JTextField();
//         JLabel texto2 = new JLabel("Escribe aqui>>");
//         texto2.setBounds(10, 320, 100, 20);
//         textoEnviar.add(texto);
//         textoEnviar.get(textoEnviar.size() - 1).setBounds(100, 320, 260, 30);
//         newPanel.add(textoEnviar.get(textoEnviar.size() - 1));
//         newPanel.add(texto2);

//         // Botón de opciones para agregar archivos
//         JButton opciones = new JButton("...");
//         opciones.setIcon(new ImageIcon("doc.png"));
//         botonesEnviar.add(opciones);
//         opciones.setBounds(364, 320, 30, 30);
//         opciones.setName("" + (botonesEnviar.size() - 1));
//         opciones.addActionListener(e -> seleccionarArchivo());
//         newPanel.add(opciones);

//         // Botón de enviar
//         JButton enviar = new JButton();
//         enviar.setIcon(new ImageIcon("nueva1.png"));
//         botonesEnviar.add(enviar);
//         botonesEnviar.get(botonesEnviar.size() - 1).setBounds(400, 320, 56, 38);
//         botonesEnviar.get(botonesEnviar.size() - 1).setText("");
//         botonesEnviar.get(botonesEnviar.size() - 1).addActionListener(this);
//         botonesEnviar.get(botonesEnviar.size() - 1).setName("Enviar" + (botonesEnviar.size() - 1));
//         newPanel.add(botonesEnviar.get(botonesEnviar.size() - 1));

//         contactosChat.add(nombre);
//     }

//     private void seleccionarArchivo() {
//         JFileChooser fileChooser = new JFileChooser();
//         fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//         int result = fileChooser.showOpenDialog(ventana);
//         if (result == JFileChooser.APPROVE_OPTION) {
//             File file = fileChooser.getSelectedFile();
//             int selectedIndex = chats.getSelectedIndex();
//             textoEnviar.get(selectedIndex).setText("Archivo seleccionado: " + file.getName());
//         }
//     }

//     public int getStatus() {
//         return statusOp;
//     }

//     public int getSalida() {
//         return salida;
//     }

//     public void setStatus(int newStatus) {
//         statusOp = newStatus;
//     }

//     public String getActiveMessage() {
//         int selectedIndex = chats.getSelectedIndex();
//         String texto = textoEnviar.get(selectedIndex).getText();
//         textoEnviar.get(selectedIndex).setText("");
//         return texto;
//     }

//     public int getActiveTab() {
//         return chats.getSelectedIndex();
//     }

//     public String getContactosChat(int indice) {
//         return contactosChat.get(indice);
//     }

//     public void setNewMessage(String mensaje) {
//         if (mensaje.contains("<contactos>")) {
//             // Procesar contactos...
//         } else if (mensaje.startsWith("S<msj>")) {
//             // Procesar mensaje
//             if (mensaje.contains("Archivo:")) {
//                 String fileName = mensaje.substring(mensaje.indexOf("Archivo:") + 8).trim();
//                 int selectedIndex = chats.getSelectedIndex();
//                 chatPersona.get(selectedIndex).setText(chatPersona.get(selectedIndex).getText() + "\n[Archivo recibido: " + fileName + "]");
//             }
//         }
//     }

//     public String getNombre() {
//         return Nombre;
//     }

//     @Override
//     public void actionPerformed(ActionEvent ae) {
//         JButton boton = (JButton) ae.getSource();
//         if (boton.getActionCommand().equals("Nuevo chat privado")) {
//             if (contactos.isEmpty()) {
//                 JOptionPane.showMessageDialog(null, "En este momento no hay contactos en linea", "Contactos", JOptionPane.ERROR_MESSAGE);
//             } else {
//                 ArrayList<String> contactosMostrar = contactos;
//                 contactosMostrar.remove(Nombre);
//                 String seleccion = (String) JOptionPane.showInputDialog(null, "Nuevo chat", "Seleccione el usuario con el que desea crear un chat", JOptionPane.INFORMATION_MESSAGE, null, contactosMostrar.toArray(), contactosMostrar.toArray()[0]);
//                 if (contactosChat.contains(seleccion)) {
//                     chats.setSelectedIndex(contactosChat.indexOf(seleccion));
//                 } else {
//                     newChat(seleccion);
//                     chats.setSelectedIndex(contactosChat.indexOf(seleccion));
//                 }
//                 statusOp = 0;
//             }
//         } else if (boton.getActionCommand().equals("Enviar")) {
//             statusOp = 1;
//         }
//     }
// }

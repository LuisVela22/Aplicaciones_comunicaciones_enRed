package Chat;

import Interfaz.Fondo;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Cliente extends javax.swing.JFrame implements Runnable, ActionListener, DocumentListener
{
    public final static String GRUPO = "GRUPO";
    
    public final int U_ID = 0;
    public final int U_CONECTADO_ID = 1;
    public final int MENSAJE_PRIVADO_ID = 2;
    public final int MENSAJE_PUBLICO_ID = 3;
    public final int U_DESCONECTADO_ID = 4;
    
    private String nombre = null;
    private String nombreDestino = "";
    private Emoticono am;
    
    HashMap<String, String> conversaciones = new HashMap<>();
    HashMap<String, JButton> usuarios = new HashMap<>();
    JButton grupo;
    
    
    public Cliente() throws IOException
    {
        initComponents();
        init();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setTitle(nombre);
        am = new Emoticono();
        
        Conversacion.setContentType("text/html");
        Conversacion.setEditable(false);
        
         addWindowListener(new java.awt.event.WindowAdapter()
         {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                try
                {
                    close();
                }
                catch (IOException ex)
                {
                    Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                    
                }
            }
        });
    }
    
    private void init() throws IOException
    {
        iniciarChat();
        Enviar.addActionListener(this);
        Enviar.setEnabled(false);
        Texto.getDocument().addDocumentListener(this);
        grupo = new JButton("Sala");
        grupo.addActionListener(this);
        conversaciones.put(GRUPO, "");
        usuarios.put(GRUPO, grupo);
        actualizarBotones();
    }
    
    private void iniciarChat() throws IOException
    {
        Inicio login = new Inicio(new javax.swing.JFrame(), true);
        login.setVisible(true);
        nombre = login.getNombre();
        
        if(nombre == null)
        {
            System.exit(0);
        }
        else
        {
            nombre.trim();
            Servidor.getInstance().iniciarSesion(nombre);
        }
    }
    
    private void iniciarSesion(String nombreNuevo) throws IOException
    {   
        if (!existiaUsuario(nombreNuevo)) 
        {
            Servidor.getInstance().iniciarSesion(nombre);
            actualizarBotones();
        }
    }
    
    private boolean existiaUsuario(String nombre)
    {
        if(conversaciones.get(nombre) == null)
        {
            conversaciones.put(nombre, nombre + " ha ingresado al chat.");
            JButton b = new JButton(nombre);
            b.setBackground(Color.GRAY);
            b.addActionListener(this);
            usuarios.put(nombre, b);
            return false;
        }
        return true;
    }
    
    private void mostrarMensaje(Mensaje mensaje)
    {
        
        switch(mensaje.getId()){
            case 1: System.out.println("Operación: Usuario Conectado");
                    break;
            case 2: System.out.println("Operación: Envio de mensaje privado");
                    break;
            case 3: System.out.println("Operación: Envio de mensaje público");
                    break;
            case 4: System.out.println("Operación: Usuario desconectado");
                    break;
        }
                
        if(mensaje.getNombreOrigen() != null)
        {
            System.out.print("Origen: " + mensaje.getNombreOrigen() + "\n");
        }
        
        if(mensaje.getNombreDestino() != null)
        {
            System.out.print("Destino: " + mensaje.getNombreDestino() + "\n");
        }
        
        if(mensaje.getMensaje() != null)
        {
            System.out.print("Mensaje: " + mensaje.getMensaje() + "\n");
        }
    }
    
    private void actualizar() throws IOException
    {
        Mensaje mensaje = Servidor.getInstance().recibe();
        mostrarMensaje(mensaje); 
        
        if (mensaje.getNombreOrigen() != null)
        {
            switch (mensaje.getId())
            {
                case U_CONECTADO_ID:
                    if (!mensaje.getNombreOrigen().equals(nombre))
                    {
                        iniciarSesion(mensaje.getNombreOrigen());
                    }
                    break;
                    
                case U_DESCONECTADO_ID:
                    finSesion(mensaje.getNombreOrigen());
                    break;
                    
                case MENSAJE_PRIVADO_ID:
                    if (!mensaje.getNombreOrigen().equals(nombre)) 
                    {
                        if (mensaje.getNombreDestino().equals(nombre)) 
                        {
                            visualizarMensajePrivado(mensaje);
                        }
                    }
                    break;
                    
                case MENSAJE_PUBLICO_ID:
                    if (mensaje.getNombreOrigen().equals(nombre)) {
                        mensaje.setNombreOrigen("Tú");
                    }
                    visualizarMensajePublico(mensaje);
                    break;
                    
                default:
            }
        }

    }
    
    private void visualizarMensajePublico(Mensaje mensaje)
    {
        String   msj = conversaciones.get(GRUPO) + "<br>" + mensaje.getNombreOrigen() + ": " + mensaje.getMensaje();
        
        conversaciones.put(GRUPO, msj );
        Conversacion.setText(am.formatoAMensaje(msj));
        if(nombreDestino.equals(GRUPO))
        {
            Conversacion.setText(am.formatoAMensaje(msj));
            grupo.setBackground(Color.DARK_GRAY);
        }
        else
        {
            grupo.setBackground(Color.GRAY);
        }
    }
    
    private void visualizarMensajePrivado(Mensaje mensaje)
    {
        String   msj = mensaje.getNombreOrigen() + ": " + mensaje.getMensaje();
        conversaciones.put(mensaje.getNombreOrigen(), msj );
        if(nombreDestino.equals(mensaje.getNombreOrigen()))
        {
            Conversacion.setText(am.formatoAMensaje(mensaje.getMensaje()));
            usuarios.get(mensaje.getNombreOrigen()).setBackground(Color.DARK_GRAY);
        }
        else
        {
            usuarios.get(mensaje.getNombreOrigen()).setBackground(Color.GRAY);
        }
    }        
        
    private void finSesion(String usuario)
    {
        conversaciones.remove(usuario);
        usuarios.remove(usuario);
        actualizarBotones();
        
        if(usuario.equals(nombreDestino))
        {
            nombreDestino = "";
            setTitle(nombre);
        }
        
        habilitarEnvio();
    }
    
    private void actualizarBotones()
    {
        JPanel panel = new JPanel(new GridLayout(50,0));
        panel.setBackground(Color.WHITE);
        panel.add(grupo);
        
        Collection<JButton> usuariosConectados = usuarios.values();
        
        for (JButton u : usuariosConectados) 
            panel.add(u);
        
        UsuariosConectados.setViewportView(panel);
        
        if(usuariosConectados.size() == 1)
            grupo.setEnabled(false);
        else
            grupo.setEnabled(true);
            
    }
    
    private void close() throws IOException
    {
        if (JOptionPane.showConfirmDialog(rootPane, "¿Deseas abandonar el chat?", "Abandonar el chat", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        {
            Servidor.getInstance().salirSesion(nombre);
            System.exit(0);
        }
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        Conversacion = new javax.swing.JTextPane();
        UsuariosConectados = new javax.swing.JScrollPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        Texto = new javax.swing.JTextArea();
        Enviar = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setBackground(new java.awt.Color(254, 254, 254));

        jScrollPane2.setViewportView(Conversacion);

        UsuariosConectados.setBackground(new java.awt.Color(254, 254, 254));
        UsuariosConectados.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        Texto.setColumns(20);
        Texto.setRows(5);
        jScrollPane3.setViewportView(Texto);

        Enviar.setBackground(java.awt.SystemColor.activeCaption);
        Enviar.setText("Enviar");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 525, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 524, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 13, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(UsuariosConectados)
                    .addComponent(Enviar, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(UsuariosConectados)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Enviar, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    public static void main(String args[]) throws IOException
    {
       
        Cliente c =new Cliente();
        c.setVisible(true);
        Runnable run = c;
        Thread thread = new Thread(c);
        thread.start();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane Conversacion;
    private javax.swing.JButton Enviar;
    private javax.swing.JTextArea Texto;
    private javax.swing.JScrollPane UsuariosConectados;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables

    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                actualizar();
            }
            catch (IOException ex) 
            {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent ae) 
    {
        if(ae.getSource().equals(Enviar))
        {
            try
            { 
                if(nombreDestino.equals(GRUPO))
                {
                    
                    Servidor.getInstance().mensajeASala(Texto.getText(), nombre);
                }
                else
                {   
                    Servidor.getInstance().mensajePrivado(nombre, nombreDestino, Texto.getText());
                }
                
                Texto.setText("");
            }
            catch (IOException ex)
            {
                Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            JButton b = (JButton) ae.getSource();
            b.setBackground(Color.GRAY);
            nombreDestino = b.getText();
            Conversacion.setText(am.formatoAMensaje(conversaciones.get(nombreDestino)));
            this.setTitle(nombre + " - " + nombreDestino);
            habilitarEnvio();
        }
    }

    private boolean analizarTextoAEnviar(String Mensaje)
    {
        int tam = Mensaje.length(); 
        if( tam > 0)
        {
            char c;
            for(int i = 0; i < tam; i++)
            {
                c = Mensaje.charAt(i);
                if(c != ' ' && c != 10 )
                    return true;
            }
        }
        return false;
    }
            
    private void habilitarEnvio()
    {
        if(analizarTextoAEnviar(Texto.getText()) && nombreDestino.length() > 0)
            Enviar.setEnabled(true);
        else
            Enviar.setEnabled(false);   
    }
    
    @Override
    public void insertUpdate(DocumentEvent de) 
    {
        habilitarEnvio();
    }

    @Override
    public void removeUpdate(DocumentEvent de) 
    {
        habilitarEnvio();
    }

    @Override
    public void changedUpdate(DocumentEvent de) 
    {
        habilitarEnvio();
    }
}


package Chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Servidor
{
    private final String ip = "230.1.1.1";
    private final int puerto = 4000;
    private byte b[];
    public final int TAM_BUFFER = 500;
    
    private MulticastSocket cl;
    private DatagramPacket packet;
    private InetAddress grupo = null;
    
    private final String tag_inicio = "<inicio>";
    private final String tag_privado = "<privado>";
    private final String tag_publico = "<msj>";
    private final String tag_fin = "<fin>";
    
    public final int ID_error = 0;
    public final int ID_inicio = 1;
    public final int ID_privado = 2;
    public final int ID_publico = 3;
    public final int ID_fin = 4;
   
    private Servidor()
    {
        try
        {
            cl = new MulticastSocket(puerto);
            System.out.println("Cliente conectado desde: " + cl.getLocalPort());
            cl.setReuseAddress(true);
            cl.setTimeToLive(1);
            grupo = InetAddress.getByName(ip);
            cl.joinGroup(grupo);

            /*byte[] b = new byte[TAM_BUFFER];
            for(;;) {
                DatagramPacket packet = new DatagramPacket(b, b.length);
                cl.receive(packet);

                String
            }*/
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws IOException
    {

        Servidor s =new Servidor();
    }

    public static Servidor getInstance()
    {
        return AdministradorDeOperacionesHolder.INSTANCE;
    }

    private static class AdministradorDeOperacionesHolder
    {
        private static final Servidor INSTANCE = new Servidor();
    }

    public void iniciarSesion(String nombreOrigen) throws IOException
    {
        String msj = "<inicio>" + nombreOrigen;
        b = msj.getBytes();
        packet = new DatagramPacket(b, b.length, grupo, puerto);
        cl.send(packet);
    }
    
    public void mensajeASala(String mensaje, String nombreOrigen) throws IOException
    {
        String msj = "<msj>" + "<" + nombreOrigen + ">" + " " + mensaje + " ";
        b = msj.getBytes();
        packet = new DatagramPacket(b, b.length, grupo, puerto);
        cl.send(packet);
    }

    public void mensajePrivado(String nombreOrigen, String nombreDestino, String mensaje) throws IOException
    {
        String msj = "<privado>" + "<" + nombreOrigen + ">" + "<" + nombreDestino + ">" + " " + mensaje + " ";
        b = msj.getBytes();
        packet = new DatagramPacket(b, b.length, grupo, puerto);
        cl.send(packet);
    }

    public void salirSesion(String nombreOrigen) throws IOException
    {
        String msj = "<fin>" + nombreOrigen;
        b = msj.getBytes();
        packet = new DatagramPacket(b, b.length, grupo, puerto);
        cl.send(packet);
    }
    
    public Mensaje recibe() throws IOException
    {
        DatagramPacket packet = new DatagramPacket(new byte[TAM_BUFFER], TAM_BUFFER);
        cl.receive(packet);
        String mensaje = new String(packet.getData());
        System.out.print(mensaje + "\n");
        return getMensaje(mensaje);
    }

    private Mensaje getMensaje(String mensaje)
    {
        Mensaje msj = new Mensaje();
        switch (getEtiqueta(mensaje))
        {
            case tag_inicio:
                msj.setId(ID_inicio);
                msj.setNombreOrigen(getUsuarioDeLaSesion(mensaje));
            break;

            case tag_publico:
                msj.setId(ID_publico);
                String msjpublico[] = getUsuarioMensajeDeOrigenPublico(mensaje);
                
                if (msjpublico != null)
                {
                    msj.setNombreOrigen(msjpublico[0]);
                    msj.setMensaje(msjpublico[1]);
                }
            break;

            case tag_privado:
                msj.setId(ID_privado);
                String msjprivado[] = getUsuariosMensajeDeOrigenPrivado(mensaje);
                
                if (msjprivado != null)
                {
                    msj.setNombreOrigen(msjprivado[0]);
                    msj.setNombreDestino(msjprivado[1]);
                    msj.setMensaje(msjprivado[2]);
                }
            break;

            case tag_fin:
                msj.setId(ID_fin);
                msj.setNombreOrigen(getUsuarioDeSalidaSesion(mensaje));
            break;
            
            default:
                msj.setId(ID_error);
            break;
        }

        return msj;
    }

    private String getEtiqueta(String mensaje)
    {
        int tam = mensaje.length(), i = 0;
        char c;
        
        String etiqueta = "";
        
        if (mensaje.charAt(0) == '<')
        {
            while ((c = mensaje.charAt(i)) != '>' && i < tam)
            {
                etiqueta += c;
                i++;
            }
            
            if (c == '>')
            {
                etiqueta += c;
            }
        }
        
        return etiqueta;
    }

    private String getUsuarioDeLaSesion(String mensaje)
    {
        return mensaje.substring(tag_inicio.length()).trim();
    }

    private String getUsuarioDeSalidaSesion(String mensaje)
    {
        return mensaje.substring(tag_fin.length()).trim();
    }

    private String[] getUsuarioMensajeDeOrigenPublico(String mensaje)
    {
        mensaje = mensaje.substring(tag_publico.length());
        String usuario = "", msj = "";
        char c;
        
        if (mensaje.charAt(0) == '<')
        {
            int i = 1;
            
            while ((c = mensaje.charAt(i)) != '>' && i < mensaje.length())
            {
                usuario += c;
                i++;
            }
            
            i++;
            
            if (i < mensaje.length())
            {
                msj = mensaje.substring(i);
            }
            
            return new String[]{usuario, msj};
        }
        else
        {
            return null;
        }
    }

    private String[] getUsuariosMensajeDeOrigenPrivado(String mensaje)
    {
        mensaje = mensaje.substring(tag_privado.length());
        
        String origen = "", destino = "", msj = "";
        char c;
        int i = 1;
        
        if (mensaje.charAt(0) == '<')
        {
            while ((c = mensaje.charAt(i)) != '>' && i < mensaje.length())
            {
                origen += c;
                i++;
            }
        }
        else
        {
            return null;
        }
        
        i++;
        
        if (i < mensaje.length() && mensaje.charAt(i) == '<')
        {
            i++;
            
            while ((c = mensaje.charAt(i)) != '>' && i < mensaje.length())
            {
                destino += c;
                i++;
            }
        }
        else
        {
            return null;
        }
        
        i++;
        
        if (i < mensaje.length())
        {
            msj = mensaje.substring(i);
        }

        return new String[]{origen, destino, msj};
    }
}

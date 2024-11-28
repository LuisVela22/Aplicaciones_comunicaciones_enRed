
package Chat;

public class Emoticono
{
    //Emoticonos
    private final String risa = " <img src=" + this.getClass().getClassLoader().getResource("Imgs/risa.gif").toString() + " width='35' height='35' alt='emoji'> ";
    private final String wow = " <img src=" + this.getClass().getClassLoader().getResource("Imgs/wow.gif").toString() + " width='35' height='35' alt='emoji'> ";
    private final String enojado = " <img src=" + this.getClass().getClassLoader().getResource("Imgs/enojado.png").toString() + " width='35' height='35' alt='emoji'> ";
    private final String feliz = " <img src=" + this.getClass().getClassLoader().getResource("Imgs/feliz.png").toString() + " width='35' height='35' alt='emoji'> ";
    private final String triste = " <img src=" + this.getClass().getClassLoader().getResource("Imgs/triste.png").toString() + " width='35' height='35' alt='emoji'> ";
    
    public String formatoAMensaje(String mensaje)
    {
            // Verificar si el mensaje es null
        if (mensaje == null) {
            return "<html>No se ha recibido ningún mensaje.</html>"; // O cualquier mensaje predeterminado
        }
        int tam = mensaje.length();
        int i = 0;
        char c;
        String msj = "";
        
        while (i < tam)
        {
            c = mensaje.charAt(i);
            
            if (c == ' ')
            {
                c = mensaje.charAt(i + 1);
                
                switch (c) {
                    case 'R':
                        msj += risa;
                        i += 1;
                        System.out.print("Emoji Risa");
                        break;
                    case 'W':
                        msj += wow;
                        i += 1;
                        System.out.print("Emoji Wow");
                        break;
                    case 'F':
                        msj += feliz;
                        i += 1;
                        System.out.print("Emoji Feliz");
                        break;
                    case 'T':
                        msj += triste;
                        i += 1;
                        System.out.print("Emoji Triste");
                        break;
                    case 'E':
                        msj += enojado;
                        i += 1;
                        System.out.print("Emoji Enojado");
                        break;
                    default:
                        msj += ' ';
                        break;
                }
                }
                 else
                {
                    msj += c;
                }

                i++;
                
            }
           
        
        
        return "<html> " + msj + "</html>";
    }
    
    public String formatoDescripcion(String descripcion, int tope)
    {
        int aux = 0;
        char c;
        String msj = "<html><body>";
        
        for (int i = 0; i < descripcion.length(); i++)
        {
            if (descripcion.charAt(i) == ' ' || i == tope)
            {
                if (i < tope)
                {
                    aux = i;
                }
                else
                {
                    if (i == tope && aux == 0)
                    {
                        aux = tope;
                    }
                    
                    msj += descripcion.substring(0, aux) + " <br> ";
                    descripcion = descripcion.substring(aux, descripcion.length());
                    i = 0;
                }
            }
        }
        
        msj += descripcion;
        msj += "</body></html>";
        
        return msj;
    }
}

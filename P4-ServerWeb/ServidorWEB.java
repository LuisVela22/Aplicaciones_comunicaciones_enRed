import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class ServidorWEB {
	public static final int PUERTO = 8000;
	ServerSocket ss;
	private ExecutorService threadPool;

	class Manejador implements Runnable {
		protected Socket socket;
		protected PrintWriter pw;
		protected BufferedOutputStream bos;
		protected BufferedReader br;
		protected String FileName;

		public Manejador(Socket _socket) {
			this.socket = _socket;
		}

		@Override
		public void run() {
			try {
				br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				bos = new BufferedOutputStream(socket.getOutputStream());
				pw = new PrintWriter(new OutputStreamWriter(bos));
				String line = br.readLine();

				if (line == null) {
					pw.print("<html><head><title>Servidor WEB</title></head>");
					pw.print("<body bgcolor=\"#AACCFF\"><br>Linea Vacia</br>");
					pw.print("</body></html>");
					pw.flush();
					socket.close();
					return;
				}

				System.out.println("\nCliente Conectado desde: " + socket.getInetAddress());
				System.out.println("Por el puerto: " + socket.getPort());
				System.out.println("Datos: " + line + "\r\n\r\n");

				//Aqui entra porque la peticion no tiene parametros "?"
				if (line.indexOf("?") == -1) {
					getArch(line);
					if (FileName.compareTo("") == 0) {
						SendA("index.htm");
					}
					else { //esto respode a cualquier metodo
							//http con un archivo
						if(line.contains("GET")) {
							//System.out.println("ENTRO CORRECTO");
							String mimeType = getMimeType(FileName);
							SendA(FileName, mimeType);
						} else{
							//System.out.println("ENTRO CORRECTO");
							SendA("405.html");
						}


					}
					System.out.println(FileName);
				} else if (line.toUpperCase().startsWith("GET")) {
					StringTokenizer tokens = new StringTokenizer(line, "?");
					String req_a = tokens.nextToken();
					String req = tokens.nextToken();
					System.out.println("Token1: " + req_a + "\r\n\r\n");
					System.out.println("Token2: " + req + "\r\n\r\n");
					pw.println("HTTP/1.0 200 Okay");
					pw.flush();
					pw.println();
					pw.flush();
					pw.print("<html><head><title>SERVIDOR WEB</title></head>");
					pw.print("<body bgcolor=\"#AACCFF\"><center><h1><br>Parametros Obtenidos..</br></h1>");
					pw.print("<h3><b>" + req + "</b></h3>");
					pw.print("</center></body></html>");
					pw.flush();
				}
				else {
					pw.println("HTTP/1.0 501 Not Implemented");
					pw.println();
				}

				pw.flush();
				bos.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public String getMimeType(String fileName) {
			if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
				return "text/html";
			} else if (fileName.endsWith(".pdf")) {
				return "application/pdf";
			} else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
				return "image/jpeg";
			} else if (fileName.endsWith(".png")) {
				return "image/png";
			} else if (fileName.endsWith(".css")) {
				return "text/css";
			} else if (fileName.endsWith(".js")) {
				return "application/javascript";
			} else if (fileName.endsWith(".txt")) {
				return "text/plain";
			} else {
				return "application/octet-stream"; // Tipo MIME por defecto (descarga de archivos binarios)
			}
		}


		public void getArch(String line) {
			int i;
			int f;
			if (line.toUpperCase().startsWith("GET")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			} else if (line.toUpperCase().startsWith("POST")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			} else if (line.toUpperCase().startsWith("HEAD")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			} else if (line.toUpperCase().startsWith("PUT")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			} else if (line.toUpperCase().startsWith("DELETE")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			} else if (line.toUpperCase().startsWith("TRACE")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			} else if (line.toUpperCase().startsWith("CONNECT")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			} else if (line.toUpperCase().startsWith("PATCH")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			}
			else {
				FileName = "";
				System.out.println("HTTP/1.0 501 Not Implemented\nhttps://http.cat/status/501");

			}


			//System.out.println("nombre archivo: "+FileName);
		}

		public void SendA(String fileName, String mimeType) {
			try {
				// Abrir el archivo solicitado
				BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(fileName));
				byte[] buf = new byte[1024];
				int b_leidos;

				// Obtener el tamaño del archivo
				File file = new File(fileName);
				int tam_archivo = (int) file.length();

				// Crear encabezado HTTP
				StringBuilder sb = new StringBuilder();
				sb.append("HTTP/1.0 200 OK\n");
				sb.append("Server: Axel Server/1.0\n");
				sb.append("Date: " + new Date() + "\n");
				sb.append("Content-Type: " + mimeType + "\n");
				sb.append("Content-Length: " + tam_archivo + "\n");
				sb.append("\n");

				// Enviar encabezado HTTP
				bos.write(sb.toString().getBytes());
				bos.flush();

				// Leer y enviar el contenido del archivo
				while ((b_leidos = bis2.read(buf, 0, buf.length)) != -1) {
					bos.write(buf, 0, b_leidos);
				}
				bos.flush();

				// Cerrar flujo de entrada
				bis2.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		public void SendA(String arg)
		{
			try{
				int b_leidos=0;
				BufferedInputStream bis2=new BufferedInputStream(new FileInputStream(arg));
				byte[] buf=new byte[1024];
				int tam_bloque=0;
				if(bis2.available()>=1024)
				{
					tam_bloque=1024;
				}
				else
				{
					bis2.available();
				}

				int tam_archivo=bis2.available();
				/***********************************************/
				String sb = "";
				sb = sb+"HTTP/1.0 200 ok\n";
				sb = sb +"Server: Axel Server/1.0 \n";
				sb = sb +"Date: " + new Date()+" \n";
				sb = sb +"Content-Type: text/html \n";
				sb = sb +"Content-Length: "+tam_archivo+" \n";
				sb = sb +"\n";
				bos.write(sb.getBytes());
				bos.flush();

				//out.println("HTTP/1.0 200 ok");
				//out.println("Server: Axel Server/1.0");
				//out.println("Date: " + new Date());
				//out.println("Content-Type: text/html");
				//out.println("Content-Length: " + mifichero.length());
				//out.println("\n");

				/***********************************************/

				while((b_leidos=bis2.read(buf,0,buf.length))!=-1)
				{
					bos.write(buf,0,b_leidos);


				}
				bos.flush();
				bis2.close();

			}
			catch(Exception e)
			{
				System.out.println(e.getMessage());
			}

		}
	}

	public ServidorWEB() throws Exception {
		System.out.println("Iniciando Servidor.......");
		this.ss = new ServerSocket(PUERTO);
		//creamos una alberca de hilos con 100 hilos
		this.threadPool = Executors.newFixedThreadPool(100);
        System.out.println("Servidor iniciado:---OK");
        System.out.println("Esperando por Cliente....");
        for (;;) {
            Socket accept = ss.accept();
            // Se crea un nuevo hilo con un Runnable
			threadPool.execute(new Manejador(accept));
            //new Thread(new Manejador(accept)).start();
        }
    }

    public static void main(String[] args) throws Exception {
        ServidorWEB sWEB = new ServidorWEB();
    }
}

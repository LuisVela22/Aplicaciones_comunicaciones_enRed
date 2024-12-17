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

				//procesa GET sin parametros
				if (line.indexOf("?") == -1) { //aqui entra porque no hay parametros
					getArch(line);
					if (FileName.compareTo("") == 0) { // aqui entra si no hay archivo
						FileName = "zoo.jpg";
						//SendA("shaq.jpg");
					}
					if (FileName.endsWith(".jpg") || FileName.endsWith(".jpeg")) {
						pw.println("HTTP/1.0 200 OK");
						pw.println("Content-Type: image/jpg"); // Tipo MIME correcto para imágenes JPEG
						pw.println(); // Línea en blanco para separar los headers del contenido
						pw.flush();
						SendA(FileName);
					} else {
						FileName = "index.htm";
						SendA(FileName);
					}
					System.out.println(FileName);
				} //GET_FIN
				//procesa GET con parametros
				else if (line.toUpperCase().startsWith("GET")) { //aqui entra porque hay parametros
					if(line.toUpperCase().startsWith("GET /?")) {
						pw.println("HTTP/1.0 200 OK");
						pw.println("Content-Type: text/html");
						pw.println();
						pw.println("<html><head><title>SERVIDOR WEB</title></head>");
						pw.println("<body bgcolor=\"#AACCFF\"><center><h1><br>Parametros Obtenidos..</br></h1>");
						pw.println("<h3><b>No hay parametros</b></h3>");
						pw.println("</center></body></html>");
						pw.flush();
					}
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
				} else if (line.toUpperCase().startsWith("POST")) {
					int cl = 0, ii = 0;
					while (cl == 0) {
						String header = br.readLine();
						System.out.println("Recibiendo: " + header + "\r\n");
						if (header.startsWith("Content-Length:")) {
							String temp = header.substring(header.indexOf(" ") + 1, header.length());
							cl = Integer.parseInt(temp);
							System.out.println("Content Length: " + cl + "\r\n");
						}
						ii++;
					}
					String data = "";
					for (int j = 0; j < cl; j++) {
						data += (char) br.read();
					}
					System.out.println("Datos: " + data + "\r\n");
					pw.println("HTTP/1.0 200 Okay");
					pw.flush();
					pw.println();
					pw.flush();
					pw.print("<html><head><title>SERVIDOR WEB</title></head>");
					pw.print("<body bgcolor=\"#AACCFF\"><center><h1><br>Datos Obtenidos..</br></h1>");
					pw.print("<h3><b>" + data + "</b></h3>");
					pw.print("</center></body></html>");
					pw.flush();
				} else if (line.toUpperCase().startsWith("HEAD")) {
					pw.println("HTTP/1.0 200 Okay");
					pw.println();
				} else if (line.toUpperCase().startsWith("PUT")) {
					pw.println("HTTP/1.0 200 Okay");
					pw.println();
				} else if (line.toUpperCase().startsWith("DELETE")) {
					pw.println("HTTP/1.0 200 Okay");
					pw.println();
				} else if (line.toUpperCase().startsWith("TRACE")) {
					pw.println("HTTP/1.0 200 Okay");
					pw.println();
				} else if (line.toUpperCase().startsWith("OPTIONS")) {
					System.out.println("Opciones");
					pw.println("HTTP/1.0 200 Okay");
					pw.println();
				} else if (line.toUpperCase().startsWith("CONNECT")) {
					pw.println("HTTP/1.0 200 Okay");
					pw.println();
				} else if (line.toUpperCase().startsWith("PATCH")) {
					pw.println("HTTP/1.0 200 Okay");
					pw.println();
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

		/*public void SendA(String fileName) {
			try {
				int b_leidos = 0;
				BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(fileName));
				byte[] buf = new byte[1024];

				//int tam_archivo = bis2.available();
				long tam_archivo = new File(fileName).length();
				StringBuilder sb = new StringBuilder();
				sb.append("HTTP/1.0 200 OK\n");
				sb.append("Server: Axel Server/1.0 \n");
				sb.append("Date: ").append(new Date()).append(" \n");
				sb.append("Content-Type: image/jpg \n");
				sb.append("Content-Length: ").append(tam_archivo).append(" \n");
				sb.append("\n");
				bos.write(sb.toString().getBytes());
				bos.flush();

				while ((b_leidos = bis2.read(buf, 0, buf.length)) != -1) {
					bos.write(buf, 0, b_leidos);
				}
				bos.flush();
				bis2.close();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}*/
		public void SendA(String fileName) {
			try {
				File file = new File(fileName);
				long tam_archivo = file.length(); // Obtener el tamaño real del archivo

				BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(file));
				byte[] buf = new byte[1024];
				int b_leidos;

				// Encabezado HTTP
				StringBuilder sb = new StringBuilder();
				sb.append("HTTP/1.0 200 OK\r\n");
				sb.append("Server: Axel Server/1.0\r\n");
				sb.append("Date: ").append(new Date()).append("\r\n");
				sb.append("Content-Type: image/jpeg\r\n"); // Asegúrate de usar 'image/jpeg'
				sb.append("Content-Length: ").append(tam_archivo).append("\r\n");
				sb.append("\r\n");

				bos.write(sb.toString().getBytes());
				bos.flush();

				// Enviar los datos binarios del archivo
				while ((b_leidos = bis2.read(buf)) != -1) {
					bos.write(buf, 0, b_leidos);
				}
				bos.flush();
				bis2.close();
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
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

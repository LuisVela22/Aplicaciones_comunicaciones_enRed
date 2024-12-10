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

				if (line.indexOf("?") == -1) {
					getArch(line);
					if (FileName.compareTo("") == 0) {
						SendA("index.htm");
					} else {
						SendA(FileName);
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
				} else {
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
			}
		}

		public void SendA(String fileName) {
			try {
				int b_leidos = 0;
				BufferedInputStream bis2 = new BufferedInputStream(new FileInputStream(fileName));
				byte[] buf = new byte[1024];

				int tam_archivo = bis2.available();
				StringBuilder sb = new StringBuilder();
				sb.append("HTTP/1.0 200 OK\n");
				sb.append("Server: Axel Server/1.0 \n");
				sb.append("Date: ").append(new Date()).append(" \n");
				sb.append("Content-Type: text/html \n");
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

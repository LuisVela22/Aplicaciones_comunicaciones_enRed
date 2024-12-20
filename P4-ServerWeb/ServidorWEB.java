import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServidorWEB {
	public static final int PUERTO = 8000;
	ServerSocket ss;
	private final ExecutorService threadPool;

	class  Manejador implements Runnable {
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
				if (line.indexOf("?") == -1 && line.toUpperCase().startsWith("GET")) {
					getArch(line);
					if (FileName.compareTo("") == 0) {
						SendA("index.html");
					}
					else { //esto respode a cualquier metodo
						File file = new File("recursos/" + FileName);
						if(file.exists()) {
							String mimeType = getMimeType(FileName);
							SendA("recursos/"+FileName, mimeType);
						} else {
							SendA("404.html");
						}
					}
					System.out.println(FileName);
				} else if((line.toUpperCase().startsWith("POST") || line.toUpperCase().startsWith("DELETE") || line.toUpperCase().startsWith("PUT")) && line.indexOf("?") == -1 ) {
					SendA("405.html");
				}
                //en el caso de que la peticion tenga parametros
                else {
                    // Extraer el metodo HTTP de la línea
                    String method = line.split(" ")[0].toUpperCase();

                    switch (method) {
                        case "GET":
							StringTokenizer tokenizer = new StringTokenizer(line, " /?");
							tokenizer.nextToken(); // Salta el método HTTP (GET)
							String resource = tokenizer.nextToken(); // Captura el path segment (aesto)
							System.out.println("Recurso solicitado: " + resource);
							if (line.toUpperCase().contains("?")) {
								System.out.println("Aqui si entra no??");
								// Si tiene parámetros, verificamos el tipo de solicitud GET
								//esta parte esta TRUQUEADA-----------------------------------
								if (!(line.toUpperCase().startsWith("GET /" + resource + "?"))) {
									System.out.println("supongo que no entra aqui");
									// Caso: Petición GET con parámetros en un recurso específico ("/recurso?clave1=valor1")
									StringTokenizer tokens = new StringTokenizer(line, " /?");
									tokens.nextToken(); // Saltar el método HTTP (GET)
									//tokens.nextToken(); // Saltar el recurso
									String url = tokens.nextToken(); // Captura los parámetros después del ?
									System.out.println("URL: " + url);
									obtenerParametros(url); // Manejar parámetros
								} else {
									// Petición GET mal formada (no contiene parámetros en el lugar esperado)
									System.out.println("Petición GET mal formada");
									SendA("400.html"); // Bad Request
								}
							} else {
								// Si no tiene parámetros
								System.out.println("No se encontraron parámetros");
								SendA("404.html"); // Recurso no encontrado
							}
                            break;

                        case "POST":
							System.out.println("Entra aqui?");

							// Leer el encabezado `Content-Length` para determinar la longitud del cuerpo
							int contentLength = 0;
							String headerLine;
							while (!(headerLine = br.readLine()).isEmpty()) {
								if (headerLine.startsWith("Content-Length:")) {
									contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
								}
							}

							// Leer el cuerpo de la solicitud
							if (contentLength > 0) {
								char[] body = new char[contentLength];
								br.read(body, 0, contentLength); // Leer el cuerpo según la longitud indicada
								String postData = new String(body);
								System.out.println("Datos recibidos en el cuerpo de la solicitud POST: " + postData);

								// Parsear los parámetros (asumiendo formato clave1=valor1&clave2=valor2)
								obtenerParametros(postData);
							} else {
								System.out.println("No se recibieron datos en el cuerpo de la solicitud POST");
							}
                            // Lógica para manejar POST
                            //SendA("200.html"); // Ejemplo: Método no permitido
                            break;

                        case "PUT":
                        case "PATCH":
                        case "DELETE":
                        case "HEAD":
                            // Lógica para otros métodos
                            SendA("405.html"); // Ejemplo: Método no permitido
                            break;

                        default:
                            // Si el método no es reconocido o implementado
                            SendA("501.html");
                            break;
                    }
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

		public void obtenerParametros(String line) {
			pw.print("HTTP/1.0 200 OK\n");
			pw.print("Content-Type: text/html\n\n");
			pw.print("<html><head><title>Servidor WEB</title></head>");
			//System.out.println("Llego a obtener parametros");
			pw.print("<body bgcolor=\"#AACCFF\">");
			pw.print("<h1>Parametros Obtenidos</h1>");
			pw.print("<h3>Clave: Valor</h3>");
			pw.print("<br>");
			pw.print("<ul>");
			String[] parts = line.split("&");
			for (String part : parts) {
				pw.print("<li>"+part+"</li>");
				System.out.println(part);
			}
			pw.print("</ul>");
			pw.print("</body></html>");
			pw.flush();

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
			} else if (fileName.endsWith(".gif")) {
				return "image/gif";
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
			} /*else if (line.toUpperCase().startsWith("POST")) {
				i = line.indexOf("/");
				f = line.indexOf(" ", i);
				FileName = line.substring(i + 1, f);
			} */else if (line.toUpperCase().startsWith("HEAD")) {
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
				byte[] buf = new byte[65535];
				int b_leidos;

				// Obtener el tamaño del archivo
				File file = new File(fileName);
				int tam_archivo = (int) file.length();

				// Crear encabezado HTTP
				StringBuilder sb = new StringBuilder();
				sb.append("HTTP/1.0 200 OK\n");
				sb.append("Server: Luis Server/1.0\n");
				sb.append("Date: " + new Date() + "\n");
				sb.append("Content-Type: " + mimeType + "\n");
				sb.append("Content-Length: " + tam_archivo + "\n");
				sb.append("Connection: keep-alive\n");
				sb.append("\n");

				System.out.println("RESPONSE HEADER");
				System.out.println("HTTP/1.0 200 OK");
				System.out.println("Server: Luis Server/1.0");
				System.out.println("Date: " + new Date());
				System.out.println("Content-Type: " + mimeType);
				System.out.println("Content-Length: " + tam_archivo);
				System.out.println("Connection: keep-alive");
				System.out.println();  // Línea en blanco para separar los encabezados del cuerpo

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
				sb = sb +"Server: Luis Server/1.0 \n";
				sb = sb +"Date: " + new Date()+" \n";
				sb = sb +"Content-Type: text/html \n";
				sb = sb +"Content-Length: "+tam_archivo+" \n";
				sb = sb + "Connection: keep-alive\n";
				sb = sb +"\n";

				System.out.println("RESPONSE HEADER");
				System.out.println("HTTP/1.0 200 OK");
				System.out.println("Server: Luis Server/1.0");
				System.out.println("Date: " + new Date());
				System.out.println("Content-Type: " + "text/html");
				System.out.println("Content-Length: " + tam_archivo);
				System.out.println("Connection: keep-alive");
				System.out.println();  // Línea en blanco para separar los encabezados del cuerpo
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

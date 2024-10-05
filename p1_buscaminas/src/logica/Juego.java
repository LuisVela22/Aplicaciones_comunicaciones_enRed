/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package logica;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

public class Juego {

    //Atributos
    JFrame ventana;
    int filas;
    int columnas;
    int minas;
    String Dificultad;

    //Juego
    JPanel panelJuego;
    JLabel fondoJuego;
    JLabel marcadorTiempo;
    JLabel marcadorBanderas;
    JLabel matriz[][];
    Timer tiempo;
    int mat[][]; //Tiene toda la información de números y bombas
    int auxmat[][];
    Random aleatorio;
    int min;
    int seg;
    int contBanderas;
    int contRestante;

    //Constructor de la clase juego
    public Juego(String Dificultad){

        this.Dificultad = Dificultad;
        ventana = new JFrame("Buscaminas");
        ventana.setSize(1489,1009);
        ventana.setLocationRelativeTo(null);
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setResizable(false);
        ventana.setLayout(null);

        //JPanel Juego
        panelJuego = new JPanel();
        panelJuego.setSize(ventana.getWidth(), ventana.getHeight());
        panelJuego.setLocation(0,0);
        panelJuego.setLayout(null);
        panelJuego.setVisible(true);

        //Imagen de inicio juego
        fondoJuego = new JLabel();
        fondoJuego.setIcon(new ImageIcon("Imagenes/Juego.jpg"));
        fondoJuego.setBounds(0, 0, panelJuego.getWidth(), panelJuego.getHeight());
        fondoJuego .setVisible(true);
        panelJuego.add(fondoJuego, 0);

        ventana.add(panelJuego);
        ventana.setVisible(true);

        aleatorio = new Random();

        min = 0;
        seg = 0;

        marcadorTiempo = new JLabel("Tiempo:  "+ min+":"+seg);
        marcadorTiempo.setSize(90, 30);
        marcadorTiempo.setVisible(true);
        marcadorTiempo.setForeground(Color.WHITE);
        panelJuego.add(marcadorTiempo, 0);

        tiempo = new Timer(1000, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                seg++;
                if(seg == 60){
                    seg = 0;
                    min++;
                }
                marcadorTiempo.setText("Tiempo:  "+ min+":"+seg);
            }});


        marcadorBanderas = new JLabel("Banderas: "+ contBanderas);
        marcadorBanderas.setSize(80, 30);
        marcadorBanderas.setVisible(true);
        marcadorBanderas.setForeground(Color.WHITE);
        panelJuego.add(marcadorBanderas, 0);



        //Evento para empezar a jugar


        switch (Dificultad) {
            case "facil":
                filas = 9;
                columnas = 9;
                minas = 10;
                break;
            case "intermedio":
                filas = 16;
                columnas = 16;
                minas = 40;
                break;
            case "experto":
                filas = 16;
                columnas = 30;
                minas = 99;
                break;
            default:
                break;
        }


        mat = new int[filas][columnas];
        auxmat = new int[filas][columnas];
        matriz = new JLabel[filas][columnas];
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                matriz[i][j] = new JLabel();
            }
        }
        tiempo.start();
        contBanderas = minas;
        marcadorBanderas.setText("Banderas: "+contBanderas);

        //Agregar matriz de imágenes
        inicializarMatriz();

        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                //Evento del mouse para cada imágen de la matriz
                matriz[i][j].addMouseListener(new MouseAdapter(){
                    @Override
                    public void mousePressed(MouseEvent e){

                        for (int k = 0; k < filas; k++) {
                            for (int l = 0; l < columnas; l++) {
                                if(e.getSource() == matriz[k][l]){
                                    if(e.getButton() == MouseEvent.BUTTON1){

                                        System.out.println(k+" "+l);

                                        //Elegimos un número del tablero
                                        if(mat[k][l] != -2 && mat[k][l] != 0 && auxmat[k][l] != -3){
                                            auxmat[k][l] = mat[k][l];
                                            matriz[k][l].setIcon(new ImageIcon("Imagenes/"+auxmat[k][l]+".png"));
                                        }
                                        //Elegimos una mina(Perdió)
                                        if(mat[k][l] == -2){
                                            //Mostrando todas las minas
                                            for (int m = 0; m < filas; m++) {
                                                for (int n = 0; n < columnas; n++) {
                                                    if(mat[m][n] == -2){
                                                        auxmat[m][n] = mat[m][n];
                                                        matriz[m][n].setIcon(new ImageIcon("Imagenes/"+auxmat[m][n]+".png"));
                                                    }
                                                }

                                            }
                                            JOptionPane.showMessageDialog(ventana, "BOOM, perdiste");
                                            System.exit(0);
                                            tiempo.stop();

                                        }
                                        //Espacios en blanco
                                        if(mat[k][l] == 0 && auxmat[k][l] != 3){
                                            recursiva(k,l);
                                            numVecinos();

                                        }
                                        //Ganar el juego
                                        contRestante = 0;
                                        for (int m = 0; m < filas; m++) {
                                            for (int n = 0; n < columnas; n++) {
                                                if(/*auxmat[m][n] == -1 || */auxmat[m][n] == -3){
                                                    contRestante++;
                                                }

                                            }
                                            if(contRestante == minas){
                                                JOptionPane.showMessageDialog(ventana, "Ganaste, ¡Felicidades!");
                                                System.exit(0);
                                            }
                                        }
                                    }
                                    else if(e.getButton() == MouseEvent.BUTTON3){

                                        //-1 son las casillas tapadas
                                        if(auxmat[k][l] == -1 && contBanderas > 0){
                                            auxmat[k][l] = -3;
                                            contBanderas--;
                                            matriz[k][l].setIcon(new ImageIcon("Imagenes/"+auxmat[k][l]+".png"));
                                            marcadorBanderas.setText("Banderas: " + contBanderas);
                                        }
                                        else if(auxmat[k][l] == -3){
                                            auxmat[k][l] = -1;
                                            contBanderas++;
                                            matriz[k][l].setIcon(new ImageIcon("Imagenes/"+auxmat[k][l]+".png"));
                                            marcadorBanderas.setText("Banderas: " + contBanderas);

                                        }
                                    }
                                }
                            }

                        }

                    }});
            }
        }

    }

    public Juego() {

    }

    public void inicializarMatriz(){

        int f; //filas
        int c; //columnas

        for(int i = 0; i < filas; i++){
            for(int j = 0; j < columnas; j++){
                mat[i][j] = 0;
                auxmat[i][j] = -1;
            }
        }
        //Va a agregar minas de forma aleatoria
        for (int i = 0; i < minas; i++) {
            do{
                f = aleatorio.nextInt(filas);
                c = aleatorio.nextInt(columnas);
            }while(mat[f][c] == -2);
            mat[f][c] = -2;

        }

        for(int i = 0; i < filas; i++){
            for(int j = 0; j < columnas; j++){
                if(mat[i][j] == -2){
                    //hacia arriba
                    if(i > 0 && mat[i-1][j] != -2){
                        mat[i-1][j]++;
                    }
                    //hacia abajo
                    if(i < filas-1 && mat[i+1][j] != -2){
                        mat[i+1][j]++;
                    }
                    //hacia la izquierda
                    if(j > 0 && mat[i][j-1] != -2){
                        mat[i][j-1]++;
                    }
                    //hacia la derecha
                    if(j < columnas-1 && mat[i][j+1] != -2){
                        mat[i][j+1]++;
                    }
                    //Esquina superior izquierda
                    if(i>0 && j>0 && mat[i-1][j-1] != -2){
                        mat[i-1][j-1]++;
                    }
                    //Esquina superior derecha
                    if(i>0 && j<columnas-1 && mat[i-1][j+1] != -2){
                        mat[i-1][j+1]++;
                    }
                    //Esquina inferior izquierda
                    if(i<filas-1 && j>0 && mat[i+1][j-1] != -2){
                        mat[i+1][j-1]++;
                    }
                    //Esquina inferior derecha
                    if(i<filas-1 && j<columnas-1 && mat[i+1][j+1] != -2){
                        mat[i+1][j+1]++;
                    }
                }
            }
        }
        ventana.setSize(100+(columnas*30), 180+(filas*30));
        panelJuego.setSize(100+(columnas*30), 150+(filas*30));
        marcadorTiempo.setLocation(ventana.getWidth()-100, 30);
        marcadorBanderas.setLocation(10, 30);

        for(int i = 0; i < filas; i++){
            for(int j = 0; j < columnas; j++){
                matriz[i][j].setSize(30,30);
                //matriz[i][j].setLocation(j, j);
                matriz[i][j].setLocation(50+(j*30), 75+(i*30));
                matriz[i][j].setIcon(new ImageIcon("Imagenes/"+auxmat[i][j]+".png"));
                matriz[i][j].setVisible(true);
                panelJuego.add(matriz[i][j], 0);
            }
            System.out.println("");
        }
        panelJuego.revalidate();
        panelJuego.repaint();
    }//Fin inicializar matriz

    public void recursiva(int i, int j){

        auxmat[i][j] = mat[i][j];
        mat[i][j] = 9;
        //Recursividad aplicada a la derecha
        if(j < columnas-1 && mat[i][j+1] == 0 && auxmat[i][j+1] != -3){
            recursiva(i,j+1);
        }
        else if(j < columnas-1 && auxmat[i][j+1] != -3 && mat[i][j+1] != 0 && mat[i][j+1] != -2 && mat[i][j+1] != 9){
            auxmat[i][j+1] = mat[i][j+1];
        }
        //Recursividad aplicada a la izquierda
        if(j > 0 && mat[i][j-1] == 0 && auxmat[i][j-1] != -3){
            recursiva(i,j-1);
        }
        else if(j > 0 && auxmat[i][j-1] != -3 && mat[i][j-1] != 0 && mat[i][j-1] != -2 && mat[i][j-1] != 9){
            auxmat[i][j-1] = mat[i][j-1];
        }
        //Recursividad aplicada hacia arriba
        if(i > 0 && mat[i-1][j] == 0 && auxmat[i-1][j] != -3){
            recursiva(i-1,j);
        }
        else if(i > 0 && auxmat[i-1][j] != -3 && mat[i-1][j] != 0 && mat[i-1][j] != -2 && mat[i-1][j] != 9){
            auxmat[i-1][j] = mat[i-1][j];
        }
        //Recursividad aplicada hacia abajo
        if(i < filas-1 && mat[i+1][j] == 0 && auxmat[i+1][j] != -3){
            recursiva(i+1,j);
        }else if(i < filas-1 && auxmat[i+1][j] != -3 && mat[i+1][j] != 0 && mat[i+1][j] != -2 && mat[i+1][j] != 9){
            auxmat[i+1][j] = mat[i+1][j];
        }

        matriz[i][j].setIcon(new ImageIcon("Imagenes/"+auxmat[i][j]+".png"));

    }

    public void numVecinos(){
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                System.out.println(auxmat[i][j]+ " ");
                matriz[i][j].setIcon(new ImageIcon("Imagenes/"+auxmat[i][j]+".png"));
            }
            System.out.println("");
        }
    }

    public int[][] getTablero() {
        return mat;
        //return new int[0][];
    }
}

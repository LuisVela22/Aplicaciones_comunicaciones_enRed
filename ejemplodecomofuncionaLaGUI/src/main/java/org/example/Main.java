package org.example;

public class Main {

    public static void main(String[] args) {
        new UI().setVisible(true);
        System.out.println("Hello world!");
    }
    
    public void guardar(String eleccion, String texto){
        String eleccion1 = eleccion;
        String text = texto;
        System.out.println("eleccion1: "+ eleccion1 + "    texto: "+ text);
    }
}
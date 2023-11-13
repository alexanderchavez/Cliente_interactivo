/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente_interactivo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Alexander chavez
 */
public class Cliente_interactivo {

    /**
     * @param args the command line arguments
     */
    private static DataOutputStream dout = null;
    private static DataInputStream din = null;
    public static void main(String[] args) {
        try {
            String ipServer=args[2];
            String folder = args[0];
            String ext = args[1];
            Socket socket = new Socket(ipServer,4242);
            DataInputStream dinMain = new DataInputStream(socket.getInputStream());
            DataOutputStream doutMain=new DataOutputStream(socket.getOutputStream());
            BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
            String enviar=""; String recibido="";
            int opcion = 0;
            List<File> lista_archivos;
            Scanner scan = new Scanner(System.in);
            int op_archivo = 0;
            while(!enviar.equals("salir")) {
                opcion = displayMenu();
                switch (opcion) {
                    case 1:
                        enviar = "enviar";
                        doutMain.writeUTF(enviar); doutMain.flush();
                        lista_archivos = obtenListaArchivos(folder,ext);
                        displayMenuEnvio(lista_archivos,ipServer);
                        enviar = "";
                        break;
                    case 2:
                        enviar = "listar,"+folder+","+ext;
                        doutMain.writeUTF(enviar); doutMain.flush();
                        recibido = dinMain.readUTF();
                        System.out.println("Servidor dice :"+recibido);
                         System.out.println("Seleccion:>");
                        op_archivo = scan.nextInt();
                        enviar = "solicitar,"+op_archivo;
                        doutMain.writeUTF(enviar); doutMain.flush();
                        escucharyRecibirArchivo();
                        enviar="Archivo enviado!";
                        break;
                    case 3:
                        lista_archivos = obtenListaArchivos(folder,ext);
                        despliegaListaArchivos(lista_archivos);
                        break;
                    case 4:
                        enviar = "listar,"+folder+","+ext;
                        doutMain.writeUTF(enviar); doutMain.flush();
                        recibido = dinMain.readUTF();
                        System.out.println(recibido);
                        recibido = "";
                        break;
                    case 0:
                        enviar = "salir";

                        break;
                    case 99:
                        System.out.println("Opción no valida");
                        enviar = "*";
                }                   
                //enviar = buffer.readLine();
                doutMain.writeUTF(enviar); doutMain.flush();
                recibido = dinMain.readUTF();
               // System.out.println("Servidor dice:"+recibido);
            }
            dinMain.close();doutMain.close(); socket.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    private static boolean recibeArchivo() {
        boolean exito = false;
        try {
            int bytes = 0;
            String fileName = din.readUTF();
            FileOutputStream fos = new FileOutputStream(fileName);
            long size = din.readLong();
            byte[] buffer = new byte[4*1024];
            while ( size > 0 && ( bytes = din.read(buffer,0,
                   (int)Math.min(buffer.length, size))) !=-1) 
            {
                fos.write(buffer, 0, bytes);
                size = size - bytes;
            }
            fos.close();
            exito = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return exito;
    }
    public static int displayMenu() {
        System.out.println("===== MENU ====");
        System.out.println("[1]. Enviar archivo");
        System.out.println("[2]. Recibir archivo");
        System.out.println("[3]. Listar folder local");
        System.out.println("[4]. Listar folder servidor");
        System.out.println("[0]. Salir");
        System.out.print("Opcion:>");
        Scanner scan = new Scanner(System.in);
        int opcion = scan.nextInt();
        if (opcion <0 || opcion>4) {
            opcion = 99;
        }
        return opcion;
    }
    public static void displayMenuEnvio(List<File> lista,String ipServer) {
        despliegaListaArchivos(lista);
        System.out.print("Seleccione archivo a enviar (num):>");
        Scanner scan = new Scanner(System.in);
        int opcion = scan.nextInt();
        
        if (opcion < 0 || opcion >=lista.size()) {
            System.out.println("Archivo no enviado");
        } else {
            String archivo_a_enviar =lista.get(opcion).toString();
            System.out.println("archivo a enviar:"+archivo_a_enviar);
            // Iniciamos conexion
            try (Socket socket = new Socket(ipServer,800)) {
                //din = new DataInputStream(socket.getInputStream());
                dout= new DataOutputStream(socket.getOutputStream());
                System.out.println("Enviando archivo a servidor");
                if (enviarArchivo(archivo_a_enviar)) {
                    System.out.println("Archivo enviado con exito");
                } else {
                    System.out.println("Falla en envío de archivo");
            }
            dout.close(); socket.close();
            } catch (Exception e) { System.out.println(e.getMessage());}
        }
    }
    private static boolean enviarArchivo(String fileName) {
        boolean exito = false;
        try {
            int bytes = 0;
            File file = new File(fileName);
            FileInputStream fis = new FileInputStream(file);
            dout.writeUTF(fileName);
            dout.writeLong(file.length()); // enviamos longitud 
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fis.read(buffer)) != -1) {
                dout.write(buffer,0, bytes); //enviamos en partes
                dout.flush();
            }
            fis.close(); 
            exito = true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return exito;
    }    
    public static List<File> obtenListaArchivos(String ruta,
                String ext) {
        List<File> lista_archivos = new ArrayList<>();
        File directorio = new File(ruta);
        File[] lista = directorio.listFiles();
        for (File archivo : lista) {
            String nombre_archivo = archivo.toString();
            int indice = nombre_archivo.lastIndexOf(".");
            if (indice > 0) {
                String extension = nombre_archivo.substring(indice+1);
                if(extension.equals(ext)) {
                    lista_archivos.add(archivo);
                }
            }
        }
        return lista_archivos;
    }
    public static void despliegaListaArchivos(List<File> lista) {
        int i = 0;
        for (File archivo : lista) {
            System.out.println("["+i+"]"+archivo.toString());
            i++;
        }
    }
    private static void escucharyRecibirArchivo() {
        int puerto = 900;
        try ( ServerSocket server = new ServerSocket(puerto)) {
            System.out.println("Iniciamos Server en puerto"+puerto);
            Socket socket = server.accept();
            din = new DataInputStream(socket.getInputStream());
            dout= new DataOutputStream(socket.getOutputStream());
            if (recibeArchivo()) {
                System.out.println("Archivo recibido");
            }
            din.close();dout.close();socket.close();server.close();
            System.out.println("Fin");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
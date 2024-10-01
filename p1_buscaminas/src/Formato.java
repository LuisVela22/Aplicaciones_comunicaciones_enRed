import java.io.*;

/**
 *
 * @author axele
 */
public class Formato {
    public static void main(String[] args){
        try{
            //PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out,"Windows-1250"));
            //PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out,"ISO-8859-1"));
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out,"UTF8"));
            pw.println("á é í ó ú ñ Á É Í Ó Ú Ñ");
            pw.flush();
            pw.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}

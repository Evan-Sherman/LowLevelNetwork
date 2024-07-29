import java.io.*;
import java.util.*;
import java.lang.*;

/*
 */
public class Reader {

    int count;
    String pathname;
    
    /** Creates a new instance of Reader */
    public Reader(String path) 
    {
    
        try
        {
                    this.pathname = path;
                    
                    File SharedFile = new File(this.pathname);
                    FileWriter SFile = new FileWriter(SharedFile);
                    SFile.close();
                    count = 0;
                
            
        }
        catch(Exception e)
        {
            System.out.println(e + "in InitReader");
        }
    }
    
    /*========= read output files and write into input files ==========*/
    String readFile()
    {
        try
        {                              
                    String str = pathname;
                    BufferedReader ReadFile = new BufferedReader(new FileReader(str));
                    int temp = 0;
                    while((str = ReadFile.readLine()) != null)
                    {
                        ++temp;
                        if(temp > count) /* new msg */
                        {
                                   
                                    ReadFile.close();
                                    count = temp;
                                    return str;                       
                        }
                   }
                   count = temp;
                   ReadFile.close();

                   return "";
                   
        }
        catch(Exception e)
        {
            System.out.println(e + " in readFile()");
        }


        return "";
    }
}
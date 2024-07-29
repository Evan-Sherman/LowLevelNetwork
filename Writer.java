import java.io.*;
import java.util.*;
import java.lang.*;

public class Writer {

    int count;
    String pathname;
    
    /** Creates a new instance of writer */
    public Writer(String path) {
    
        try
        {
                    this.pathname = path;
                    File SharedFile = new File(pathname);
                    FileWriter SFile = new FileWriter(SharedFile);
                    SFile.close();
                    count = 0;
                
            
        }
        catch(Exception e)
        {
            System.out.println(e + "in writer");
        }
    }
    
    /*========= read output files and write into input files ==========*/
    void writeFile(String str)
    {
        try
        {                              
                    
                   String filePath = this.pathname;
                    BufferedWriter WriteFile = new BufferedWriter(new FileWriter(filePath,true));
                    WriteFile.write(str);
                    WriteFile.write("\n");
                    WriteFile.close();                       
                    count++;
                   
        }
        catch(Exception e)
        {
            System.out.println(e + " in writeFile()");
        }
    }
}
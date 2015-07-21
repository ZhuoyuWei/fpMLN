package wzy.func;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class FileTools {
	public static boolean CreateNewFolder(String dirname)
	{
		File file=new File(dirname);
		if(file.exists())
			return false;
		return file.mkdirs();
	}
	public static void CopyFile(String inputfile,String outputfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			String buffer=null;
			PrintStream ps=new PrintStream(outputfile);
			while((buffer=br.readLine())!=null)
			{
				ps.println(buffer);
			}
			ps.close();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

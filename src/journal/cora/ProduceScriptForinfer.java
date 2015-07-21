package journal.cora;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class ProduceScriptForinfer {

	
	public void ReadAndPrintScript(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		PrintStream ps=new PrintStream(outputfile);
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			
		}
	}
	
	public static void main(String[] args)
	{
		
	}
}

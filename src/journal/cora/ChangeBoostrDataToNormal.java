package journal.cora;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class ChangeBoostrDataToNormal {

	
	public void MergeTrueFalse(String truefile,String falsefile,String outputfile,String outfile2) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(truefile));
		PrintStream ps=new PrintStream(outputfile);
		PrintStream ps2=new PrintStream(outfile2);
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			ps.println(buffer);
			ps2.println(buffer+"\t1.0");
		}
		
		br.close();
		br=new BufferedReader(new FileReader(falsefile));
		
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			ps.println(buffer);
			ps2.println(buffer+"\t0.0");
		}
		
		br.close();
		
		ps.close();
		ps2.close();
		
	}
	
	public void ChangeInfoLittle(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		PrintStream ps=new PrintStream(outputfile);
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			buffer=buffer.toLowerCase();
			ps.println(buffer);
		}
	}
	
	
	
	public static void main(String[] args) throws IOException
	{
		ChangeBoostrDataToNormal cbdn=new ChangeBoostrDataToNormal();
		cbdn.MergeTrueFalse("", "", "", "");
		
	}
	
}

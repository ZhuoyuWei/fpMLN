package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class cora_double {

	
	
	public void ReadAndPrint(String inputfile,String outputfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			String buffer=null;
			PrintStream ps=new PrintStream(outputfile);
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				ps.println(buffer);
				String[] ss=buffer.split("[()]+");
				String[] token=ss[1].split(",");
				if(ss[0].indexOf("Same")==0)
				{
					ps.println(ss[0]+"("+token[1]+","+token[0]+")");
				}
			}
			br.close();
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args)
	{
		cora_double cd=new cora_double();
		
		cd.ReadAndPrint("D:\\data\\Experiments_6\\learn\\cora_data\\cora.er_6.11.no\\temp\\miss-5.db"
				, "D:\\data\\Experiments_6\\learn\\cora_data\\cora.er_6.11.no\\miss-5.db");
	}
	
	
}

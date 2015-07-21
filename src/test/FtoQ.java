package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class FtoQ {

	public void ReadAndWrite(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		StringBuilder sb=new StringBuilder();
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			sb.append(buffer);
			sb.append(",");
		}
		PrintStream ps=new PrintStream(outputfile);
		ps.println(sb.toString());
	}
	
	public static void main(String[] args) throws IOException
	{
		FtoQ fq=new FtoQ();
		fq.ReadAndWrite("D:\\data\\infer experiments\\infer_sub_lsm\\infer_data_100\\SameAuthor\\SameAuthor.query"
				, "D:\\data\\infer experiments\\infer_sub_lsm\\infer_data_100\\SameAuthor\\SameAuthor.query.new");
	}
	
}

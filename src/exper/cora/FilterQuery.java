package exper.cora;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class FilterQuery {

	
	public Set<String> entitySet=new HashSet<String>();
	
	public void ReadFile(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[()]+");
			String[] token=ss[1].split(",");
			for(int i=0;i<token.length;i++)
			{
				entitySet.add(token[i]);
			}
		}
		br.close();
	}
	
	public void Filter(String filename,String outfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		PrintStream ps=new PrintStream(outfile);
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[()]+");
			String[] token=ss[1].split(",");
			boolean flag=true;
			for(int i=0;i<token.length;i++)
			{
				if(!entitySet.contains(token[i]))
				{
					flag=false;
					break;
				}
			}
			if(flag)
			{
				ps.println(buffer);
			}
		}		
	}
	
	public static void main(String[] args) throws IOException
	{
		String dir="C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub\\";
		String dir2="C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub_lsm\\";
		for(int i=100;i<2501;i+=100)
		{
			FilterQuery fq=new FilterQuery();
			fq.ReadFile(dir+"infer_data_"+i+"\\SameVenue\\SameVenue.db");
			fq.Filter(dir+"infer_data_"+i+"\\SameVenue\\SameVenue.query",
					dir2+"infer_data_"+i+"\\SameVenue\\SameVenue.query");
		}
	}
	
}

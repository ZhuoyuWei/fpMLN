
package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import journal.cora.ChangeMyBoostrTofpMLN;

public class testJava {
	
	public static void ReadFile(String infile,String outfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(infile));
			PrintStream ps=new PrintStream(outfile);
			String buffer=null;
			List<String> sList=new ArrayList<String>();
			while((buffer=br.readLine())!=null)
			{
				//ps.println(buffer);
				sList.add(buffer);
			}
			for(int i=sList.size()-1;i>=0;i--)
			{
				ps.println(sList.get(i));
			}
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
		/*ReadFile("E:\\Workspaces2014\\fpMLN\\res\\probability.result","E:\\Workspaces2014\\fpMLN\\res\\probability.result2");
		System.out.println(Math.log(0.01));
		System.out.println(Math.log(0.99));
		
		System.out.println(Math.exp(-0.6980210679632892));*/
		
/*		double base=0.;
		for(int i=0;i<20;i++)
		{
			System.out.println(Math.pow(10., base));
			base+=0.2;
		}*/
/*		String s="abc";
		s=ChangeMyBoostrTofpMLN.ChangeToken(s);
		System.out.println(s);*/
		
	}
	
}

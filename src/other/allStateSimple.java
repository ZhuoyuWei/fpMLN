package other;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class allStateSimple {

	
	public Set<String> idSet=new HashSet<String>();
	
	public void ReadFile(String inputfile)
	{
		try {
			BufferedReader br=new BufferedReader (new FileReader(inputfile));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split(",");
				idSet.add(ss[0]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void ReadFileAndPrint(String inputfile,String outputfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			PrintStream ps=new PrintStream(outputfile);
			ps.println("customer_ID,plan");
			String buffer=null;
			int line=0;
			String id="";
			String temp="";
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				if(line==0)
				{
					line++;
					continue;
				}
				line++;
				String[] ss=buffer.split(",");
				String t="";
				for(int i=17;i<24;i++)
				{
					t+=ss[i];
				}
				//ps.println(ss[0]+","+t);
				if(ss[0].equals(id))
				{
					temp=t;
				}
				else
				{
					
					if(!idSet.contains(id)&&line>1)
					{
						ps.println(id+","+temp);
					}
					id=ss[0];
					temp=t;

				}
			}
			if(!idSet.contains(id))
			{
				ps.println(id+","+temp);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void ReadFileAndPrintCount(String inputfile,String outputfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			PrintStream ps=new PrintStream(outputfile);
			ps.println("customer_ID,plan");
			String buffer=null;
			int line=0;
			String id="";
			int[][] temp=new int[7][5];
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				if(line==0)
				{
					line++;
					continue;
				}
				line++;
				String[] ss=buffer.split(",");
				int[] token=new int[7];
				for(int i=0;i<7;i++)
				{
					token[i]=Integer.parseInt(ss[i+17]);
				}

				//ps.println(ss[0]+","+t);
				if(ss[0].equals(id))
				{
					for(int i=0;i<token.length;i++)
					{
						temp[i][token[i]]++;
					}					
				}
				else
				{
					
					if(!idSet.contains(id)&&line>1)
					{
						ps.print(id+",");
						for(int i=0;i<7;i++)
						{
							int tt=0;
							for(int j=1;j<5;j++)
							{
								if(temp[i][tt]<temp[i][j])
									tt=j;
							}
							ps.print(temp[i][tt]);
						}
						ps.println();
					}
					id=ss[0];
					temp=new int[7][5];

				}
			}
			if(!idSet.contains(id)&&line>1)
			{
				ps.print(id+",");
				for(int i=0;i<7;i++)
				{
					int tt=0;
					for(int j=1;j<5;j++)
					{
						if(temp[i][tt]<temp[i][j])
							tt=j;
					}
					ps.print(temp[i][tt]);
				}
				ps.println();
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
		allStateSimple ass=new allStateSimple();
		
		ass.ReadFileAndPrintCount("C:\\Users\\Administrator\\Desktop\\allState\\test_v2.csv\\test_v2.csv",
				"C:\\Users\\Administrator\\Desktop\\allState\\train.csv\\train.out.2");
	}
	
}

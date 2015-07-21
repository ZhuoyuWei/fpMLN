package exper.cora;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import wzy.func.FileTools;

public class DataForBP {

	
	public Set<String> lineSet=new HashSet<String>();
	
	public void ReadFile(String filename)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				lineSet.add(buffer);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void PrintFile(String filename)
	{
		try {
			PrintStream ps=new PrintStream(filename);
			Iterator it=lineSet.iterator();
			while(it.hasNext())
			{
				String buffer=(String)it.next();
				ps.println(buffer);
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void OneFile(String inputfile,String outputfile)
	{
		DataForBP dfb=new DataForBP();
		dfb.ReadFile(inputfile);
		dfb.PrintFile(outputfile);
	}
	
	public static void AllFile()
	{
		String[] querys={"SameAuthor","SameBib","SameTitle","SameVenue"};
		for(int i=100;i<=2500;i+=100)
		{
			for(int j=0;j<4;j++)
			{
				OneFile("C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub_lsm\\infer_data_"
							+i+"\\"+querys[j]+"\\"+querys[j]+".db","C:\\Users\\Administrator\\Desktop\\infer" +
									" experiments\\infer_sub_bp\\infer_data_"
							+i+"\\"+querys[j]+"\\"+querys[j]+".db");
				
				OneFile("C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub_lsm\\infer_data_"
							+i+"\\"+querys[j]+"\\"+querys[j]+".query","C:\\Users\\Administrator\\Desktop\\infer" +
									" experiments\\infer_sub_bp\\infer_data_"
							+i+"\\"+querys[j]+"\\"+querys[j]+".query");				
				
				OneFile("C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub_lsm\\infer_data_"
							+i+"\\"+querys[j]+"\\"+querys[j]+".result","C:\\Users\\Administrator\\Desktop\\infer" +
									" experiments\\infer_sub_bp\\infer_data_"
							+i+"\\"+querys[j]+"\\"+querys[j]+".result");					
			}
		}
	}
	
	public static void main(String[] args)
	{
		//DataForBP dfb=new DataForBP();
		//dfb.ReadFile("");
		//dfb.PrintFile("");
		
		AllFile();
		
		
	}
	
	
	
	
}

package exper.cora;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import wzy.func.FileTools;

public class FourQueryRemover {

	
	
	public void SplitQuery(String inputfile,String dbfile,String queryfile,
			String query,String resultfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		PrintStream psdb=new PrintStream(dbfile);
		PrintStream psquery=new PrintStream(queryfile);
		PrintStream psresult=new PrintStream(resultfile);
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			if(buffer.indexOf(query)==0)
			{
				psquery.println(buffer);
				psresult.println(buffer+"\t1.0");
			}
			else
			{
				psdb.println(buffer);
			}
		}
		psdb.close();
		psquery.close();
		br.close();
	}
	
	public static void OneTime(int seedNum) throws IOException
	{
		FourQueryRemover fqr=new FourQueryRemover();
		String inputfile="C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub\\sample"+seedNum+".db";
		String outputdir="C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub\\infer_data_"+seedNum+"\\";
		FileTools.CreateNewFolder(outputdir);
		String[] querys={"SameAuthor","SameBib","SameTitle","SameVenue"};
		for(int i=0;i<querys.length;i++)
			fqr.SplitQuery(inputfile, outputdir+querys[i]+".db", outputdir+querys[i]+".query", querys[i]
					,outputdir+querys[i]+".result");		
	}
	
/*	public static void main(String[] args) throws IOException
	{
		for(int i=100;i<2497;i+=100)
		{
			OneTime(i);
		}
	}*/
	
	public static void main(String[] args) throws IOException
	{
		FourQueryRemover fqr=new FourQueryRemover();
		String inputfile="C:\\Users\\Administrator\\Desktop\\infer experiments\\cora_data\\cora.er_6.11.no\\Whole.db";
		String outputdir="C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub\\infer_data_2500\\";
		FileTools.CreateNewFolder(outputdir);
		String[] querys={"SameAuthor","SameBib","SameTitle","SameVenue"};
		for(int i=0;i<querys.length;i++)
			fqr.SplitQuery(inputfile, outputdir+querys[i]+".db", outputdir+querys[i]+".query", querys[i]
					,outputdir+querys[i]+".result");			
	}
}

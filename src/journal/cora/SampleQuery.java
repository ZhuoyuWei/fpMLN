package journal.cora;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wzy.func.FileTools;

public class SampleQuery {

	
	public void FilterAndSample(String inputfile,String outputfile1,String outputfile2,int maxCount) throws IOException
	{
		List<SQ_flag_string> trueList=new ArrayList<SQ_flag_string>();
		List<SQ_flag_string> falseList=new ArrayList<SQ_flag_string>();
		
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("\t");
			SQ_flag_string sfs=new SQ_flag_string();
			if(Double.parseDouble(ss[1])>0.5)
			{
				sfs.flag=1;
				trueList.add(sfs);
			}
			else
			{
				sfs.flag=0;
				falseList.add(sfs);
			}
			sfs.query=ss[0];
		}
		
		PrintStream ps1=new PrintStream(outputfile1);
		PrintStream ps2=new PrintStream(outputfile2);
		
		for(int i=0;i<maxCount;i+=2)
		{
			ps1.println(trueList.get(i).query+"\t1.0");
			ps2.println(trueList.get(i).query);
		}
		for(int i=0;i<maxCount*2;i+=2)
		{
			ps1.println(falseList.get(i).query+"\t0.0");
			ps2.println(falseList.get(i).query);
		}
	}
	
	
	
	public void CopyFile(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		PrintStream ps=new PrintStream(outputfile);
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			ps.println(buffer);
		}
		ps.close();
		br.close();
		
	}
	
	
	public void CheckEntityCount(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		int entityCount=0;
		Set<String> entitySet=new HashSet<String>();
		int relCount=0;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[()]+");
			String rel=ss[0];
			String[] token=ss[1].split(",");
			if(rel.equals("SameVenue"))
			{
				relCount++;
				for(int i=0;i<token.length;i++)
				{
					entitySet.add(token[i]);
				}
			}
			
		}
		System.out.println(entitySet.size()+"\t"+relCount);
	}
	
	public void ReadTrueQuery(String inputfile,String outputfile,String query) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		PrintStream ps=new PrintStream(outputfile);
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			
			String[] ss=buffer.split("[()]+");
			if(ss[0].equals(query))
			{
				ps.println(buffer);
			}
		}
		ps.close();
		br.close();
	}
	
	
	public void ChangeTestDataForBoostr(String dbfile,String queryfile,
			String factfile,String posfile,String negfile) throws IOException
	{
		BufferedReader br_db=new BufferedReader(new FileReader(dbfile));
		PrintStream ps_fact=new PrintStream(factfile);
		String buffer=null;
		while((buffer=br_db.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			buffer=buffer.toLowerCase();
			ps_fact.println(buffer+".");
		}
		
		br_db.close();
		ps_fact.close();
		
		BufferedReader br_query=new BufferedReader(new FileReader(queryfile));
		buffer=null;
		PrintStream ps_pos=new PrintStream(posfile);
		PrintStream ps_neg=new PrintStream(negfile);
		while((buffer=br_query.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");			
			double score=Double.parseDouble(ss[1]);
			String temp=ss[0].toLowerCase();
			if(score>0.5)
			{
				ps_pos.println(temp+".");
			}
			else
			{
				ps_neg.println(temp+".");
			}
		}
		ps_pos.close();
		ps_neg.close();
		br_query.close();
		
	}
	
	public static void main(String[] args) throws IOException
	{
		SampleQuery sq=new SampleQuery();
		
		int index=4;
		
/*		FileTools.CreateNewFolder("D:\\data\\Experiments_6\\learn\\cora_data\\new_query_true_and_false\\mcsat_cora_new_sample\\SameVenue");
		FileTools.CreateNewFolder("D:\\data\\Experiments_6\\learn\\cora_data\\new_query_true_and_false\\mcsat_cora_new_sample\\SameVenue\\"+index);
		
		sq.FilterAndSample("D:\\data\\Experiments_6\\learn\\cora_data\\new_query_true_and_false\\mcsat_cora_new\\SameVenue\\"+index+"\\test.query",
				"D:\\data\\Experiments_6\\learn\\cora_data\\new_query_true_and_false\\mcsat_cora_new_sample\\SameVenue\\"+index+"\\test.query",
				"D:\\data\\Experiments_6\\learn\\cora_data\\new_query_true_and_false\\mcsat_cora_new_sample\\SameVenue\\"+index+"\\test.query_noresult"
				, 1000);
		sq.CopyFile("D:\\data\\Experiments_6\\learn\\cora_data\\new_query_true_and_false\\mcsat_cora_new\\SameVenue\\"+index+"\\test.db",
				"D:\\data\\Experiments_6\\learn\\cora_data\\new_query_true_and_false\\mcsat_cora_new_sample\\SameVenue\\"+index+"\\test.db");*/
		
		
	//	sq.CheckEntityCount("D:\\data\\Experiments_6\\learn\\cora_data\\cora.er_6.11.no\\coraSepFixedhwng.1.db");
		
		/*FileTools.CreateNewFolder("D:\\data\\Experiments_6\\learn\\cora_data\\cora.er_6.11.no\\fpMLN_Train_Query\\SameVenue");
		FileTools.CreateNewFolder("D:\\data\\Experiments_6\\learn\\cora_data\\cora.er_6.11.no\\fpMLN_Train_Query\\SameVenue\\"+index);
		
		sq.ReadTrueQuery("D:\\data\\Experiments_6\\learn\\cora_data\\cora.er_6.11.no\\coraSepFixedhwng."+index+".db",
				"D:\\data\\Experiments_6\\learn\\cora_data\\cora.er_6.11.no\\fpMLN_Train_Query\\SameVenue\\"+index+"\\query.db", 
				"SameVenue");*/
		
		
		sq.ChangeTestDataForBoostr("D:\\data\\Experiments_6\\learn\\cora_data\\new_query_true_and_false\\mcsat_cora_new_sample\\SameVenue\\"+index+"\\test.db",
				"D:\\data\\Experiments_6\\learn\\cora_data\\new_query_true_and_false\\mcsat_cora_new_sample\\SameVenue\\"+index+"\\test.query",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_samevenue\\test"+index+"_same\\test"+index+"_same_facts.txt",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_samevenue\\test"+index+"_same\\test"+index+"_same_pos.txt",
				"D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_samevenue\\test"+index+"_same\\test"+index+"_same_neg.txt");

		
	}
	
}

class SQ_flag_string
{
	int flag;
	String query;
}

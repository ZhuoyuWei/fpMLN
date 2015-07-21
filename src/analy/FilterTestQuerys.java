package analy;

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
import wzy.model.HyperEdge;

public class FilterTestQuerys {

	//public List<HyperEdge> heList=new ArrayList<HyperEdge>();
	public List<String[]> nodeList=new ArrayList<String[]>();
	public Set<String> tokenSet=new HashSet<String>();
	
	public void ReadtestDb(String testDbfile,String query)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(testDbfile));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[()]+");
				String rel=ss[0];
				if(rel.equals(query))
						continue;
				//List<String> tokenList=new ArrayList<String>();
				String[] tokens=ss[1].split(",");
				//nodeList.add(tokens);
				for(int i=0;i<tokens.length;i++)
				{
					tokenSet.add(tokens[i]);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	

	public int FilterQuery(String testQueryfile,String outFile)
	{
		int count=0;
		try {

			BufferedReader br=new BufferedReader(new FileReader(testQueryfile));
			PrintStream ps=new PrintStream(outFile);
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				String[] sss=ss[0].split("[()]+");
				String[] tokens=sss[1].split(",");
				boolean flag=true;
				for(int i=0;i<tokens.length;i++)
				{
					if(!tokenSet.contains(tokens[i]))
					{
						flag=false;
						break;
					}
				}
				if(flag)
				{
					ps.println(buffer);
					count++;
				}
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return count;
	}
	
	public static void FilterFunction(String query,String sourceDb,String testDb,String outputFile)
	{
		FilterTestQuerys ftq=new FilterTestQuerys();
		ftq.ReadtestDb(sourceDb, query);
		System.out.print(ftq.tokenSet.size()+"\t");
		int count=ftq.FilterQuery(testDb, outputFile);
		System.out.println(count);
	}
	
	public static void main(String[] args)
	{
		String[] sourceDbs=new String[5];
		//String[] dbnames=new String[5];
		String[] querys=new String[5];
		String[][] testDbs=new String[5][5];
		String[][] outputFile=new String[5][5];
		
/*		dbnames[0]="ai";
		dbnames[1]="graphics";		
		dbnames[2]="language";		
		dbnames[3]="systems";		
		dbnames[4]="theory";*/
		
/*		dbnames[0]="coraSepFixedhwng.1";
		dbnames[1]="coraSepFixedhwng.2";		
		dbnames[2]="coraSepFixedhwng.3";		
		dbnames[3]="coraSepFixedhwng.4";		
		dbnames[4]="coraSepFixedhwng.5";	*/	
		

		
		
/*		sourceDbs[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\ai.db";
		sourceDbs[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\graphics.db";
		sourceDbs[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\language.db";
		sourceDbs[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\systems.db";	
		sourceDbs[4]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\theory.db";		
		*/
/*		sourceDbs[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er_new_no\\coraSepFixedhwng.1.db";
		sourceDbs[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er_new_no\\coraSepFixedhwng.2.db";
		sourceDbs[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er_new_no\\coraSepFixedhwng.3.db";
		sourceDbs[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er_new_no\\coraSepFixedhwng.4.db";	
		sourceDbs[4]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er_new_no\\coraSepFixedhwng.5.db";	*/		
		
		/*sourceDbs[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.1.db";
		sourceDbs[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.2.db";
		sourceDbs[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.3.db";
		sourceDbs[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.4.db";	*/
		
		sourceDbs[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet\\test.db";
		
		
/*		querys[0]="advisedBy";
		querys[1]="ta";
		querys[2]="tempAdvisedBy";*/
		
/*		querys[0]="Author";
		querys[1]="HasWordAuthor";
		querys[2]="Title";*/	
		
		/*querys[0]="courseProf";
		querys[1]="courseTA";
		querys[2]="project";			
		querys[3]="samePerson";			
		querys[4]="student";*/
				
		querys[0]="_domain_region";
		
		
		FilterFunction(querys[0],sourceDbs[0]
				,"C:\\Users\\Administrator\\Documents\\Wordnet\\MaxFalseQuery\\1\\_domain_region\\res\\userview.result"
				,"C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet\\test.query");
		
		
		
	/*	for(int i=0;i<5;i++)
		{
			for(int j=0;j<4;j++)
			{
				testDbs[i][j]="C:\\Users\\Administrator\\Documents\\webkb_query\\"+querys[i]+"\\test_"+(j+1)+"\\res\\userview.result";
				System.out.println(testDbs[i][j]);
				outputFile[i][j]="C:\\Users\\Administrator\\Desktop\\mln_data\\testqueryFolder\\webkb\\";
				FileTools.CreateNewFolder(outputFile[i][j]);
				outputFile[i][j]+=querys[i]+"\\";
				FileTools.CreateNewFolder(outputFile[i][j]);
				outputFile[i][j]+=(j+1)+".query";
				//FileTools.CreateNewFolder(outputFile[i][j]);
				
				FilterFunction(querys[i],sourceDbs[j],testDbs[i][j],outputFile[i][j]);
				
			}
		}*/
		
		
		
		
		
		//testDbs[0][0]="C:\Users\Administrator\Documents\query\advisedBy\test_ai\res";
		

	}
	
}

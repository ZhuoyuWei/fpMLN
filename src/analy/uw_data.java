package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wzy.func.InputData;
import wzy.model.HyperEdge;
import wzy.model.HyperNode;

public class uw_data {

	public Map<String,Integer> relToLink=new HashMap<String,Integer>();
	public Map<String,Integer> entityToCount=new HashMap<String,Integer>();
	public Map<String,HyperEdge> relToConcept=new HashMap<String,HyperEdge>();
	public Map<Integer,Set<String>> typeToEntity=new HashMap<Integer,Set<String>>(); 
	
	
	public List<String> relList=new ArrayList<String>();
	public Map<String, Integer> relMap=new HashMap<String,Integer>();
	public List<String> typeList=new ArrayList<String>();
	public Map<String, Integer> typeMap=new HashMap<String,Integer>();
	public List<HyperEdge> conceptRelList=new ArrayList<HyperEdge>();
	
	public Map<String, Integer> entityMap=new HashMap<String,Integer>();
	public List<String> entityList=new ArrayList<String>();
	public List<HyperNode> nodeList=new ArrayList<HyperNode>();
	
	public void ReadOneFile(String filename)
	{
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[()]");
				//System.out.println(ss.length+"\t"+ss[0]+"\t"+ss[1]);
				if(ss.length!=2)
				{
					System.out.println("Error: the db file has a different format.");
					System.exit(-1);
				}
				String rel=ss[0];
				//System.out.println(ss[0]);
				//System.out.println(relMap.size());
				//System.out.println(rel);
				Integer relIndex=relMap.get(rel);
				//System.out.println(relIndex);
				//System.out.println(relToConcept.size());
				HyperEdge conceptRel=conceptRelList.get(relIndex);
				Integer relCount=relToLink.get(rel);
				if(relCount==null)
				{
					relToLink.put(rel, 1);
				}
				else
				{
					relCount++;
					relToLink.remove(rel);
					relToLink.put(rel, relCount);
				}
						
				String[] tokens=ss[1].split(",");
				for(int i=0;i<tokens.length;i++)
				{
					if(tokens[i].length()<1)
						continue;
					Integer typeIndex=conceptRel.nodeList.get(i);
					Set<String> typeSet=typeToEntity.get(typeIndex);
					if(typeSet==null)
					{
						typeSet=new HashSet<String>();
						typeToEntity.put(typeIndex,typeSet);
					}
					typeSet.add(tokens[i]);
					Integer entityCount=entityToCount.get(tokens[i]);
					if(entityCount==null)
					{
						entityToCount.put(tokens[i], 1);
					}
					else
					{
						entityCount++;
						entityToCount.remove(tokens[i]);
						entityToCount.put(tokens[i], entityCount);
					}
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
	
	public void PrintTwoCounts(String filename)
	{
		System.out.println("****************************************");
		System.out.println(filename+"\n");
		System.out.println(relToLink.size());
		Iterator it=relToLink.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			String rel=(String)entry.getKey();
			Integer count=(Integer)entry.getValue();
			System.out.println(rel+"\t"+count);
		}
		
		System.out.println("\n****************************************");		
		
	}
	
	
	public void BuildTypeCount(List<HyperNode> nodeList)
	{
		for(int i=0;i<nodeList.size();i++)
		{
			HyperNode hn=nodeList.get(i);
			
		}
	}
	
	public static void readOneFile(String info_filename,String db_filename)
	{
		//uw_data ud=new uw_data();
		//InputData.ReadInfo(info_filename, ud.relList, ud.relMap, ud.typeList, ud.typeMap, ud.conceptRelList);
		InputData.ReadDB(db_filename, ud.entityMap, ud.entityList, ud.nodeList, ud.relMap, ud.conceptRelList);
		ud.ReadOneFile(db_filename);
		System.out.println("****************************************");
		System.out.println(db_filename+"\n");
		System.out.println(ud.relToLink.size());
		Iterator it=ud.relToLink.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			String rel=(String)entry.getKey();
			Integer count=(Integer)entry.getValue();
			System.out.println(rel+"\t"+count);
		}
		System.out.println(ud.typeToEntity.size());
		it=ud.typeToEntity.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			Integer typeIndex=(Integer)entry.getKey();
			String type=ud.typeList.get(typeIndex);
			Set<String> typeSet=(Set<String>)entry.getValue();
			System.out.println(type+"\t"+typeSet.size());
		}		
		
		System.out.println("\n****************************************");

	}
	
	public static uw_data ud=new uw_data();
	
	public static void Read4Writer1(String[] inputfile,String outputfile)
	{
		try {
			PrintStream ps=new PrintStream(outputfile);
			for(int i=0;i<4;i++)
			{
				BufferedReader br=new BufferedReader(new FileReader(inputfile[i]));
				String buffer=null;
				while((buffer=br.readLine())!=null)
				{
					if(buffer.length()<2)
						continue;
					ps.println(buffer);
				}
			}
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
		
		
		String info_filename="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\uw.simple2.mln";
		String[] db_filename=new String[5];
		db_filename[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.1.db";
		db_filename[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.2.db";
		db_filename[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.3.db";
		db_filename[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.4.db";
		db_filename[4]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.5.db";		
		
		
		
		String[] db_filename_test=new String[5];
		db_filename_test[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\miss-1.db";
		db_filename_test[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\miss-2.db";		
		db_filename_test[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\miss-3.db";
		db_filename_test[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\miss-4.db";		
		db_filename_test[4]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\miss-5.db";		
		
/*		String info_filename="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\uw.simple2.mln";
		String[] db_filename=new String[5];
		db_filename[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\ai.db";
		db_filename[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\graphics.db";
		db_filename[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\language.db";
		db_filename[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\systems.db";
		db_filename[4]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\theory.db";
		
		String[] db_filename_test=new String[5];
		db_filename_test[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\miss_ai.db";
		db_filename_test[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\miss_graphics.db";
		db_filename_test[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\miss_language.db";
		db_filename_test[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\miss_systems.db";
		db_filename_test[4]="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\miss_theory.db";*/				
		
		ud=new uw_data();
		//InputData.ReadInfo(info_filename, ud.relList, ud.relMap, ud.typeList, ud.typeMap, ud.conceptRelList);
		
		
		for(int i=0;i<5;i++)
		{
			//readOneFile(info_filename,db_filename[i]);
			List<String> inputfile=new ArrayList<String>();
			for(int j=0;j<5;j++)
			{
				if(i==j)
					continue;
				inputfile.add(db_filename[j]);
			}
			Read4Writer1(inputfile.toArray(new String[0]),db_filename_test[i]);
		}
	}
	
}

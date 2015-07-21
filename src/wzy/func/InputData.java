package wzy.func;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import debug.InputOutputTest;

import wzy.model.BigQuery;
import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;


/**
 * This is a static toolbox class providing all kinds of functions for reading data from files. <br> 
 * It has no data fields and its functions have no return values. <br>
 * All in and out data are parameters of the functions. <br>
 * @author Zhuoyu Wei
 * @version 1.0
 */

public class InputData {
	
	/**
	 * This function reads info file which contains the conceptual relations or triples. At the same time, <br>
	 * this function turn all String from input to Integer, and keep the relations' list, set and types' list, set respectively. <br> 
	 * @author Zhouyu Wei
	 * @version 1.0
	 * @param infoFile info file name.
	 * @param relList static data structure, keep conceptual relations string from input file. It is a map from Integer to String.
	 * @param relMap static data structure, keep a map for conceptual relations string to index.
	 * @param typeList static data structure, keep conceptual types of nodes string from input file. It is a map from Integer to String.
	 * @param typeMap static data structure, keep a map for conceptual types of nodes string to index.
	 * @param conceptRelList keep the info file after changing string to integer. It is a list of conceptual hyper edges.
	 */
	public static void ReadInfo(String infoFile,List<String> relList,Map<String,Integer> relMap,
			List<String> typeList,Map<String,Integer> typeMap,List<HyperEdge> conceptRelList)
	{
		try {
			//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile),"utf-8"));
			BufferedReader br=new BufferedReader(new FileReader(infoFile));
			String buffer=null;
			int defultCount=0;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				buffer=buffer.replaceAll("[\\s]+", "");
				String[] ss=buffer.split("[()]");
				String[] tokens=ss[1].split(",");
				String rel=ss[0];
				if(tokens.length<2)
				{
					//rel="DefualtRelation"+defultCount++;
					tokens=new String[2];
					tokens[0]=ss[1];
					tokens[1]=ss[0];
				}
				HyperEdge he=new HyperEdge();
				Integer relIndex=relMap.get(rel);
				if(relIndex==null)
				{
					relIndex=relList.size();
					relMap.put(rel, relList.size());
					relList.add(rel);
					conceptRelList.add(he);
				}
				he.rel=relIndex;
				for(int i=0;i<tokens.length;i++)
				{
					Integer typeIndex=typeMap.get(tokens[i]);
					if(typeIndex==null)
					{
						typeIndex=typeList.size();
						typeMap.put(tokens[i], typeList.size());
						typeList.add(tokens[i]);
					}
					he.nodeList.add(typeIndex);
				}
				
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
	
	/**
	 * This function is used to read db file, it will cost sometime. The db file contains hyperedges' info. <br>
	 * It builds a hypergraph with hyperedges and conceptual info.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param dbFile db file name.
	 * @param entityMap It is a map of all entity from String(input file) to Integer
	 * @param entityList The reverse of entityMap. It is a map from Integer to String.
	 * @param nodeList A map from entity(Integer) to HyperNode
	 * @param relMap Keep a map for conceptual relations string to index. It was built in ReadInfo function.
	 * @param conceptRelList Keep the info file after changing string to integer. It is a list of conceptual hyper edges.
	 * 						 It was built in ReadInfo function.
	 */
	public static void ReadDB(String dbFile,Map<String,Integer> entityMap, List<String> entityList,
			List<HyperNode> nodeList,Map<String,Integer> relMap,List<HyperEdge> conceptRelList)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(dbFile));
			//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dbFile),"utf-8"));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				buffer=buffer.replaceAll("[\\s]+", "");
				String[] ss=buffer.split("[()]");
				String[] tokens=ss[1].split(",");
				String rel=ss[0];
				if(tokens.length<2)
				{
					//rel="DefualtRelation";
					tokens=new String[2];
					tokens[0]=ss[1];
					tokens[1]=ss[0];
				}
				HyperEdge he=new HyperEdge();
				he.rel=relMap.get(rel);
				if(he.rel==null)
				{
					LogPrintStream.getPs_InputData_ReadDB().println("Error in InputData->ReadDB: unknow relation. Rel:"+ss[0]);
					continue;
				}
				for(int i=0;i<tokens.length;i++)
				{
					Integer entityIndex=entityMap.get(tokens[i]);
					if(entityIndex==null)
					{
						entityIndex=entityList.size();
						entityMap.put(tokens[i], entityIndex);
						entityList.add(tokens[i]);
						
						//Build a new HyperNode, and add it to nodeList.
						HyperNode hn=new HyperNode();
						hn.entity=entityIndex;
						nodeList.add(hn);
					}
					he.nodeList.add(entityIndex);
				}
				
				//Set Type for each node
				HyperEdge conceptualEdge=conceptRelList.get(he.rel);
				if(conceptualEdge.nodeList.size()!=he.nodeList.size())
				{
					LogPrintStream.getPs_InputData_ReadDB().println("Error in InputData->ReadDB: Different size of Concept and entity. Rel:"
							+he.rel+"\tConcept:"+conceptualEdge.nodeList.size()+"\tEntity:"+he.nodeList.size());
					continue;
				}
				for(int i=0;i<he.nodeList.size();i++)
				{
					Integer entity=he.nodeList.get(i);
					HyperNode hn=nodeList.get(entity);
					if(hn.type!=null&&!hn.type.equals(conceptualEdge.nodeList.get(i)))
					{
						LogPrintStream.getPs_InputData_ReadDB().println("Error in InputData->ReadDB: Ambiguous types of entity. Rel:"
							+he.rel+"\tEntity:\t"+hn.entity+":"+hn.type+":"+conceptualEdge.nodeList.get(i));					
					}
					hn.type=conceptualEdge.nodeList.get(i);
				}
				
				//Set index between nodes in the same HyperEdge.
				for(int i=0;i<he.nodeList.size();i++)
				{
					Integer entity=he.nodeList.get(i);
					HyperNode hn=nodeList.get(entity);
					for(int j=0;j<he.nodeList.size();j++)
					{
						if(i==j)
							continue;
						Integer neighbour=he.nodeList.get(j);
						List<HyperEdge> heList=hn.edges.get(neighbour);
						if(heList==null)
						{
							heList=new ArrayList<HyperEdge>();
							hn.edges.put(neighbour, heList);
						}
						heList.add(he);
					}
				}
				
				//InputOutputTest.hyperEdgeList.add(he);  //for InputOutputTest
					
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * This function is another version of ReadDB, it has the same functions, and get the list of entities<br>
	 * appearing in query rdf at the same time<br>
	 * @author Zhuoyu Wei
	 * @version 1.1 add get the list of entities appearing in query rdf<br>
	 * appearing in query rdf
	 * @param dbFile db file name.
	 * @param entityMap It is a map of all entity from String(input file) to Integer
	 * @param entityList The reverse of entityMap. It is a map from Integer to String.
	 * @param nodeList A map from entity(Integer) to HyperNode
	 * @param relMap Keep a map for conceptual relations string to index. It was built in ReadInfo function.
	 * @param conceptRelList Keep the info file after changing string to integer. It is a list of conceptual hyper edges.
	 * 						 It was built in ReadInfo function.
	 * @param entityINqueryList the list of entities appearing in query rdf, for random walk seed selected.
	 * @param query it is a string, the query in parameters file
	 */
	public static void ReadDBgetentityInListSameTime(String dbFile,Map<String,Integer> entityMap, List<String> entityList,
			List<HyperNode> nodeList,Map<String,Integer> relMap,List<HyperEdge> conceptRelList,
			List<Integer> entityINqueryList, String query)
	{
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(dbFile));
			//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dbFile),"utf-8"));
			String buffer=null;
			
			while((buffer=br.readLine())!=null)
			{
			
				if(buffer.length()<2)
					continue;
				buffer=buffer.replaceAll("[\\s]+", "");
				String[] ss=buffer.split("[()]");
				String[] tokens=ss[1].split(",");
				String rel=ss[0];
				if(tokens.length<2)
				{
					//rel="DefualtRelation";
					tokens=new String[2];
					tokens[0]=ss[1];
					tokens[1]=ss[0];
				}
				HyperEdge he=new HyperEdge();
				he.rel=relMap.get(rel);
				//System.out.println(relMap+"\n"+rel);
				if(he.rel==null)
				{
					LogPrintStream.getPs_InputData_ReadDB().println("Error in InputData->ReadDB: unknow relation. Rel:"+ss[0]);
					continue;
				}
				for(int i=0;i<tokens.length;i++)
				{
					Integer entityIndex=entityMap.get(tokens[i]);
					if(entityIndex==null)
					{
						entityIndex=entityList.size();
						entityMap.put(tokens[i], entityIndex);
						entityList.add(tokens[i]);
						
						//Build a new HyperNode, and add it to nodeList.
						HyperNode hn=new HyperNode();
						hn.entity=entityIndex;
						nodeList.add(hn);
					}
					he.nodeList.add(entityIndex);
					if(rel.equals(query))
					{
						entityINqueryList.add(entityIndex);
					}
				}
				
				//by wzy, 5.22, keep the true query in memory set.
				if(rel.equals(query))
				{
					GlobalStaticData.trainQuerySet.add(he);
				}
				
				//Set Type for each node
				HyperEdge conceptualEdge=conceptRelList.get(he.rel);
				if(conceptualEdge.nodeList.size()!=he.nodeList.size())
				{
					LogPrintStream.getPs_InputData_ReadDB().println("Error in InputData->ReadDB: Different size of Concept and entity. Rel:"
							+he.rel+"\tConcept:"+conceptualEdge.nodeList.size()+"\tEntity:"+he.nodeList.size());
					continue;
				}
				for(int i=0;i<he.nodeList.size();i++)
				{
					Integer entity=he.nodeList.get(i);
					HyperNode hn=nodeList.get(entity);
					
					if(hn.type!=null&&!hn.type.equals(conceptualEdge.nodeList.get(i)))
					{
						System.out.println(GlobalStaticData.relMapItoS.get(conceptualEdge.rel));
						System.out.println(GlobalStaticData.relMapItoS.get(he.rel));						
						System.out.println(GlobalStaticData.typeMapItoS.get(hn.type)+
								"\t"+GlobalStaticData.typeMapItoS.get(conceptualEdge.nodeList.get(i)));
						LogPrintStream.getPs_InputData_ReadDB().println("Error in InputData->ReadDB: Ambiguous types of entity. Rel:"
							+he.rel+"\tEntity:\t"+hn.entity+":"+hn.type+":"+conceptualEdge.nodeList.get(i));					
					}
					hn.type=conceptualEdge.nodeList.get(i);
				}
				
				//Set index between nodes in the same HyperEdge.
				for(int i=0;i<he.nodeList.size();i++)
				{
					Integer entity=he.nodeList.get(i);
					HyperNode hn=nodeList.get(entity);
					for(int j=0;j<he.nodeList.size();j++)
					{
						if(i==j)
							continue;
						Integer neighbour=he.nodeList.get(j);
						List<HyperEdge> heList=hn.edges.get(neighbour);
						if(heList==null)
						{
							heList=new ArrayList<HyperEdge>();
							hn.edges.put(neighbour, heList);
						}
						heList.add(he);
					}
				}
				
				//InputOutputTest.hyperEdgeList.add(he);  //for InputOutputTest
					
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
	/**
	 * This function build or rebuild a List<Integer> for each HyperNode in entityToNode List, and indexs the node's neightbours<br>
	 * by traverse its edges map. The new list of neighbours is used in random walk for choosing next node.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param entityToNode The HyperNode List in the process.
	 */
	public static void IndexAllNodeNeighbours(List<HyperNode> entityToNode)
	{
		for(int i=0;i<entityToNode.size();i++)
		{
			HyperNode hn=entityToNode.get(i);
			Iterator it=hn.edges.entrySet().iterator();
			hn.neighbourList=new ArrayList<Integer>(hn.edges.size());
			while(it.hasNext())
			{
				Map.Entry entry=(Map.Entry)it.next();
				Integer key=(Integer)entry.getKey();
				hn.neighbourList.add(key);
			}
		}
	}
	public static void IndexAllNodeNeighbourstoType(List<HyperNode> entityToNode)
	{
		for(int i=0;i<entityToNode.size();i++)
		{
			HyperNode hn=entityToNode.get(i);
			Iterator it=hn.edges.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry entry=(Map.Entry)it.next();
				Integer key=(Integer)entry.getKey();
				List<HyperEdge> edges=(List<HyperEdge>)entry.getValue();
				
				for(int j=0;j<edges.size();j++)
				{
					List<HyperEdge> newEdgeList=hn.relToEdgeList.get(edges.get(j).rel);
					if(newEdgeList==null)
					{
						newEdgeList=new ArrayList<HyperEdge>();
						hn.relToEdgeList.put(edges.get(j).rel, newEdgeList);
					}
					newEdgeList.add(edges.get(j));
				}
			}
		}
	}	
	/**
	 * Get random seeds from train or test query(hyperedge) list.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param queryList
	 * @param seedList
	 */
	public static void GetSeedFromQueryListLeft(List<HyperEdge> queryList,List<Integer> seedList)
	{
		Set<Integer> seedSet=new HashSet<Integer>();
		for(int i=0;i<queryList.size();i++)
		{
			HyperEdge query=queryList.get(i);
			seedSet.add(query.nodeList.get(0));
		}
		//Integer[] seeds=seedSet.toArray(new Integer[0]);
		seedList.addAll(seedSet);
	}
	public static void GetSeedFromQueryListRight(List<HyperEdge> queryList,List<Integer> seedList)
	{
		Set<Integer> seedSet=new HashSet<Integer>();
		for(int i=0;i<queryList.size();i++)
		{
			HyperEdge query=queryList.get(i);
			seedSet.add(query.nodeList.get(1));
		}
		//Integer[] seeds=seedSet.toArray(new Integer[0]);
		seedList.addAll(seedSet);
	}	
	public static void GetSeedFromQueryListLeftAndRight(List<HyperEdge> queryList,List<Integer> seedList)
	{
		Set<Integer> seedSet=new HashSet<Integer>();
		for(int i=0;i<queryList.size();i++)
		{
			HyperEdge query=queryList.get(i);
			seedSet.add(query.nodeList.get(0));
			seedSet.add(query.nodeList.get(1));			
		}
		//Integer[] seeds=seedSet.toArray(new Integer[0]);
		seedList.addAll(seedSet);
	}	
	/**
	 * This function is used to read query db file when infer, the db file only contains queries include true and false ones. <br>
	 * It builds a hypergraph with hyperedges and conceptual info.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param dbFile db file name.
	 * @param entityMap It is a map of all entity from String(input file) to Integer
	 * @param entityList The reverse of entityMap. It is a map from Integer to String.
	 * @param nodeList A map from entity(Integer) to HyperNode
	 * @param relMap Keep a map for conceptual relations string to index. It was built in ReadInfo function.
	 * @param conceptRelList Keep the info file after changing string to integer. It is a list of conceptual hyper edges.
	 * 						 It was built in ReadInfo function.
	 * @param queryList the List of queries for infer process.
	 */
	public static void ReadQueryDB(String dbFile,Map<String,Integer> entityMap, List<String> entityList,
			List<HyperNode> nodeList,Map<String,Integer> relMap,List<HyperEdge> conceptRelList,
			List<HyperEdge> queryList,List<HyperEdge> answerList)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(dbFile));
			//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dbFile),"utf-8"));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] tAndv=buffer.split("[\\s]+");
				if(tAndv.length!=2)
					continue;
				buffer=tAndv[0];
				double queryvalue=Double.parseDouble(tAndv[1]);
				buffer=buffer.replaceAll("[\\s]+", "");
				String[] ss=buffer.split("[()]");
				String[] tokens=ss[1].split(",");
				String rel=ss[0];
				if(tokens.length<2)
				{
					//rel="DefualtRelation";
					tokens=new String[2];
					tokens[0]=ss[1];
					tokens[1]=ss[0];
				}
				HyperEdge he=new HyperEdge();
				he.rel=relMap.get(rel);
				he.value=queryvalue;
				
				HyperEdge here=null;
				if(GlobalStaticData.undirectRelSet.contains(he.rel))
				{
					here=new HyperEdge();
					here.rel=he.rel;
					here.value=he.value;
				}
				
				if(he.rel==null)
				{
					LogPrintStream.getPs_InputData_ReadDB().println("Error in InputData->ReadDB: unknow relation. Rel:"+ss[0]);
					continue;
				}
				for(int i=0;i<tokens.length;i++)
				{
					Integer entityIndex=entityMap.get(tokens[i]);
					if(entityIndex==null)
					{
						entityIndex=entityList.size();
						entityMap.put(tokens[i], entityIndex);
						entityList.add(tokens[i]);
						
						//Build a new HyperNode, and add it to nodeList.
						HyperNode hn=new HyperNode();
						hn.entity=entityIndex;
						nodeList.add(hn);
					}
					he.nodeList.add(entityIndex);
				}
				
				if(here!=null)
				{
					for(int i=he.nodeList.size()-1;i>=0;i--)
					{
						here.nodeList.add(he.nodeList.get(i));
					}
				}
				
				//Set Type for each node
				HyperEdge conceptualEdge=conceptRelList.get(he.rel);
				if(conceptualEdge.nodeList.size()!=he.nodeList.size())
				{
					LogPrintStream.getPs_InputData_ReadDB().println("Error in InputData->ReadDB: Different size of Concept and entity. Rel:"
							+he.rel+"\tConcept:"+conceptualEdge.nodeList.size()+"\tEntity:"+he.nodeList.size());
					continue;
				}
				for(int i=0;i<he.nodeList.size();i++)
				{
					Integer entity=he.nodeList.get(i);
					HyperNode hn=nodeList.get(entity);
					if(hn.type!=null&&!hn.type.equals(conceptualEdge.nodeList.get(i)))
					{
						LogPrintStream.getPs_InputData_ReadDB().println("Error in InputData->ReadDB: Ambiguous types of entity. Rel:"
							+he.rel+"\tEntity:\t"+hn.entity+":"+hn.type+":"+conceptualEdge.nodeList.get(i));					
					}
					hn.type=conceptualEdge.nodeList.get(i);
				}
				
				//Set index between nodes in the same HyperEdge.
				for(int i=0;i<he.nodeList.size();i++)
				{
					Integer entity=he.nodeList.get(i);
					HyperNode hn=nodeList.get(entity);
					for(int j=0;j<he.nodeList.size();j++)
					{
						if(i==j)
							continue;
						Integer neighbour=he.nodeList.get(j);
						List<HyperEdge> heList=hn.edges.get(neighbour);
						if(heList==null)
						{
							heList=new ArrayList<HyperEdge>();
							hn.edges.put(neighbour, heList);
						}
						heList.add(he);
						if(here!=null)
						{
							heList.add(here);
						}
					}
				}
				
				//InputOutputTest.hyperEdgeList.add(he);  //for InputOutputTest
					
				//add the query to the query list
				queryList.add(he);
				/*if(queryvalue<0.5)
				{
					HyperEdge queryAnswer=he.copyHyperEdge();
					queryAnswer.value=0.;
					answerList.add(queryAnswer);
				}
				else
				{
					answerList.add(he);
				}*/
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void FilterDuplicatePath(String inputfile,String outputfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			String buffer=null;
			Set<String> pathSet=new HashSet<String>();
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				pathSet.add(buffer);
			}
			br.close();
			Iterator it=pathSet.iterator();
			PrintStream ps=new PrintStream(outputfile);
			while(it.hasNext())
			{
				ps.println((String)it.next());
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
	
	public static List<String> ReadUndierectRel(String filename)
	{	
		List<String> relList=new ArrayList<String>();
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				relList.add(buffer);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(relList.size());
		return relList;
	}
	public static List<BigQuery> ReadFormatEntRelEntList(String filename,Boolean isLeft)
	{
		List<BigQuery> bqList=new ArrayList<BigQuery>();
		try{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			BigQuery bq=new BigQuery();
			bq.entity=GlobalStaticData.entityMapStoI.get(ss[0]);
			bq.rel=GlobalStaticData.relMapStoI.get(ss[1]);
			bq.isLeft=isLeft;
			if(ss.length<3)
				continue;
			String[] tokens=ss[2].split(";");
			for(int i=0;i<tokens.length;i++)
			{
				Integer entityIndex=GlobalStaticData.entityMapStoI.get(tokens[i]);
				if(entityIndex==null)
					continue;
				bq.entityList.add(entityIndex);
			}
			bq.BuildQueryList();
			bqList.add(bq);
		}
		br.close();
		}catch(IOException e)
		{
			e.printStackTrace();
		}
		return bqList;
	}
	
	public static Map<Integer,List<HyperEdge>> ReadAllFile(String dir,String filename)
	{
		Map<Integer,List<HyperEdge>> results=new HashMap<Integer,List<HyperEdge>>();
		File fdir=new File(dir);
		File[] files=fdir.listFiles();
		//System.out.println(files.length);
		for(int i=0;i<files.length;i++)
		{
			String[] ss=files[i].getPath().split("/");
			Integer rel=GlobalStaticData.relMapStoI.get(ss[ss.length-1]);
			List<HyperEdge> queries=results.get(rel);
			if(queries==null)
			{
				queries=new ArrayList<HyperEdge>();
				results.put(rel, queries);
			}
			try {
			
				BufferedReader br=new BufferedReader(new FileReader(files[i].getPath()+"/"+filename));
				String buffer=null;
				while((buffer=br.readLine())!=null)
				{
					if(buffer.length()<2)
						continue;
					String[] sss=buffer.split("[\\s]+")[0].split("[()]+");
					HyperEdge he=new HyperEdge();
					he.rel=GlobalStaticData.relMapStoI.get(sss[0]);
					String[] tokens=sss[1].split(",");
					he.nodeList.add(GlobalStaticData.entityMapStoI.get(tokens[0]));
					he.nodeList.add(GlobalStaticData.entityMapStoI.get(tokens[1]));
					queries.add(he);
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
		return results;
	}
	
	
	//Added by wzy, 2014.12.18
	public static Map<Integer,Double> ReadProbMapForCandidate(String filename) throws IOException
	{
		Map<Integer,Double> entityToScore=new HashMap<Integer,Double>();
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("\t");
			double candidateScore=CandidateScoreFunction(Double.parseDouble(ss[1]));
			entityToScore.put(GlobalStaticData.entityMapStoI.get(ss[0]), candidateScore);
		}
		return entityToScore;
	}
	public static double CandidateScoreFunction(double s)
	{
		return 1.+s*5.5;
	}
}

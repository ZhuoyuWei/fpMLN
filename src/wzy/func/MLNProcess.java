package wzy.func;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;


import wzy.infer.POG_typePathNode;
import wzy.model.*;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;

public class MLNProcess {
	
	private static Random rand=new Random();
	
	/**
	 * It is a pretreatment to keep lists of entities in the same types in memory, for create triples with<br>
	 * more than two variables and used to fill with the other variables fixed the type.<br>
	 * It need a large memory space, delete it timely when it is useless.
	 * There is a tip that you can check whether the number of variables of the present query is more than 2 before run this function.<br>
	 * If the number is 2 or less, you need not to run this function because it won't be used. 
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public static void KeepTypeIndexToEntitySet()
	{
		GlobalStaticData.IntiTypeToEntityList(GlobalStaticData.typeMapItoS.size());
		for(int i=0;i<GlobalStaticData.entityToNode.size();i++)
		{
			HyperNode hn=GlobalStaticData.entityToNode.get(i);
			List<Integer> entityList=GlobalStaticData.typeToEntityList.get(hn.type);
			entityList.add(hn.entity);
		}
	}
	/**
	 * It is correspongding to KeepTypeIndexToEntitySet(), and it is used to release the memory space of TypeToEntityList.
	 * @author Zhuoyu Wei
	 * @verison 1.0
	 */
	public static void DeleteTypeIndexToEntitySet()
	{
		GlobalStaticData.typeToEntityList=null;
	}
	/**
	 * Each pair of adjacent node in the same path can be formulized at least one true hyperedge,<br>
	 * and this function returns the list of all hyperedge such as these.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param firstEntity one of the two adjacent node in the same path.
	 * @param secondEntity one of the two adjacent node in the same path.
	 * @return the list of all hyperedges for the two adjacent nodes
	 */
	public static List<HyperEdge> FormulizeTrueEdgeFromTwoNode(Integer firstEntity,Integer secondEntity)
	{
		HyperNode firstNode=GlobalStaticData.entityToNode.get(firstEntity);
		List<HyperEdge> tempList=firstNode.edges.get(secondEntity);  //Take care about the returned List should be const, and it mustn't be changed.
		List<HyperEdge> resultList=new ArrayList<HyperEdge>();
		for(int i=0;i<tempList.size();i++)
		{
			HyperEdge tempEdge=tempList.get(i);
			if(GlobalStaticData.undirectRelSet.contains(tempEdge.rel))
			{
				if(tempEdge.nodeList.get(0).equals(firstEntity))
				{
					resultList.add(tempEdge);
				}
			}
			else
			{
				resultList.add(tempEdge);
			}
		}
		return resultList;
	}
	/**
	 * The Fomulizing algorithm need create queries with the head and end of the nodes' path, no matter they are true or false.<br>
	 * In this version of the algorithm, it is divided into two case:<br>
	 * As the FormulizeTrueEdgeFromTwoNode(first,second) function, if it can get at least one true hyperedge whose rel is<br>
	 * query in the parameters, the function return the List of the true hyperedges found. If these is no true hyperedge with<br>
	 * the query as rel, the function builds all possible false hyperedges.<br>
	 * In a special case, the query in conceptual hyperedge has only two variables just satisfying the two node in the source path.<br>
	 * If the query has more than  2 variables, it uses the GlobalStaticData.typeMapItoS to get all possible entities with<br>
	 * the 3th,4th,..., nth type to instead of the variables in the queries and do a Cartesian product to get all possible queries.
	 * @author Zhuoyu Wei
	 * @version 1.0 It has the Global query as parameter, and only keep and return entitative query of this type.
	 * @param firstEntity the first node of the query.
	 * @param secondEntity the second node of the query
	 * @param query the query for this process.
	 * @return the list of hyperedges got in this function, and it is used as query list.
	 */
	private static List<HyperEdge> FomulizeQueryFromTwoNode(Integer firstEntity,Integer secondEntity, Integer query)
	{
		HyperNode firstNode=GlobalStaticData.entityToNode.get(firstEntity);
		List<HyperEdge> queryList=firstNode.edges.get(secondEntity);
		if(queryList!=null)
		{
			List<HyperEdge> trueQueryList=new ArrayList<HyperEdge>();
			for(int i=0;i<queryList.size();i++)
			{
				if(queryList.get(i).rel.equals(query))
					trueQueryList.add(queryList.get(i));
			}
			queryList=trueQueryList;
		}
		if(queryList!=null&&queryList.size()>0)  //exist true query
			return queryList;
		else //exist no true query, and then build all probable false query.
		{
			queryList=new ArrayList<HyperEdge>();
			HyperEdge conceptQuery=GlobalStaticData.conceptRelList.get(query);
			if(conceptQuery==null)
				return queryList;
			HyperNode secondNode=GlobalStaticData.entityToNode.get(secondEntity);
			int startTypeIndex;
			int endTypeIndex;
			for(startTypeIndex=0;startTypeIndex<conceptQuery.nodeList.size();startTypeIndex++)
			{
				if(conceptQuery.nodeList.get(startTypeIndex).equals(firstNode.type))
					break;
			}
			for(endTypeIndex=0;endTypeIndex<conceptQuery.nodeList.size();endTypeIndex++)
			{
				if(endTypeIndex==startTypeIndex)
					continue;
				if(conceptQuery.nodeList.get(endTypeIndex).equals(secondNode.type))
					break;
			}
			if(startTypeIndex>=conceptQuery.nodeList.size()||endTypeIndex>=conceptQuery.nodeList.size())
				return queryList;
			HyperEdge queryTemplate=new HyperEdge();
			queryTemplate.rel=query;
			queryTemplate.value=0.;
			queryTemplate.nodeList=new ArrayList<Integer>(conceptQuery.nodeList.size());
			for(int i=0;i<conceptQuery.nodeList.size();i++)
			{
				queryTemplate.nodeList.add(-1);
			}
			//System.out.println(startTypeIndex+"\t"+endTypeIndex+"\t"+conceptQuery.nodeList.size());
			queryTemplate.nodeList.set(startTypeIndex, firstEntity);
			queryTemplate.nodeList.set(endTypeIndex,secondEntity);
			if(conceptQuery.nodeList.size()>2)
			{
				queryList.add(queryTemplate);
				for(int i=0;i<conceptQuery.nodeList.size();i++)
				{
					if(i==startTypeIndex||i==endTypeIndex)
						continue;
					List<HyperEdge> tempQueryList=queryList;
					queryList=new ArrayList<HyperEdge>();
					List<Integer> entityList=GlobalStaticData.typeToEntityList.get(conceptQuery.nodeList.get(i));
					for(int j=0;j<entityList.size();j++)
					{
						for(int k=0;k<tempQueryList.size();k++)
						{
							HyperEdge templateHe=tempQueryList.get(k);
							HyperEdge newTemplateHe=templateHe.copyHyperEdge();
							newTemplateHe.nodeList.set(i, entityList.get(j));
							queryList.add(newTemplateHe);
						}
					}
				}
			}
			else
			{
				queryList.add(queryTemplate);
			}
			if(firstNode.type.equals(secondNode.type))
			{
				List<HyperEdge> reverseList=new ArrayList<HyperEdge>();
				for(int i=0;i<queryList.size();i++)
				{
					HyperEdge reverseHe=queryList.get(i).copyHyperEdge();
					Integer temp=reverseHe.nodeList.get(startTypeIndex);
					reverseHe.nodeList.set(startTypeIndex, reverseHe.nodeList.get(endTypeIndex));
					reverseHe.nodeList.set(endTypeIndex, temp);
					reverseList.add(reverseHe);
				}
				queryList.addAll(reverseList);
			}
			return queryList;
		}
	}
	/**
	 * The Fomulizing algorithm similar to the FomulizeQueryFromTwoNode() function, and you can get the detail introduction<br>
	 * in the FomulizeQueryFromTwoNode's introduction. The difference from the early version is its random mechanism. It choose<br>
	 * at most MaxFalseQuery false queries when there are no true query for the two nodes.
	 * @author Zhuoyu Wei
	 * @version 1.1 It is a random version from the verison 1.0
	 * @param firstEntity the first node of the query.
	 * @param secondEntity the second node of the query
	 * @param query the query for this process.
	 * @return the list of hyperedges got in this function, and it is used as query list.
	 */
	private static List<HyperEdge> FomulizeQueryFromTwoNodeRandomly(Integer firstEntity,Integer secondEntity,Integer MaxFalseQuery)
	{
		HyperNode firstNode=GlobalStaticData.entityToNode.get(firstEntity);
		HyperNode secondNode=GlobalStaticData.entityToNode.get(secondEntity);
		//Filter undirect Rel, by wzy 2014.7.9
		List<HyperEdge> tempList=firstNode.edges.get(secondEntity);
		List<HyperEdge> queryList=new ArrayList<HyperEdge>();
		if(tempList!=null)
		for(int i=0;i<tempList.size();i++)
		{
			HyperEdge tempEdge=tempList.get(i);
			if(GlobalStaticData.undirectRelSet.contains(tempEdge.rel))
			{
				if(tempEdge.nodeList.get(0).equals(firstEntity))
				{
					queryList.add(tempEdge);
				}
			}
			else
			{
				queryList.add(tempEdge);
			}
		}
		
		
		//return queryList;
		if(queryList!=null&&queryList.size()>0)  //exist true query
		{
			//LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\t\ttrue\t"+queryList.size());
			return queryList;
		}
		else//exist no true query, and then build all probable false query.
		{
			queryList=new ArrayList<HyperEdge>();
			int conceptRelListSize=GlobalStaticData.conceptRelList.size();
			if(conceptRelListSize>MaxFalseQuery*2)
			{
				conceptRelListSize=MaxFalseQuery*2;
			}
			for(int i=0;i<conceptRelListSize;i++)
			{
				int index=i;
				if(conceptRelListSize<GlobalStaticData.conceptRelList.size())
				{
					index=Math.abs(rand.nextInt())%GlobalStaticData.conceptRelList.size();
				}
				HyperEdge conceptQuery=GlobalStaticData.conceptRelList.get(index);
				int startTypeIndex;
				int endTypeIndex;
				for(startTypeIndex=0;startTypeIndex<conceptQuery.nodeList.size();startTypeIndex++)
				{
					if(conceptQuery.nodeList.get(startTypeIndex).equals(firstNode.type))
						break;
				}
				for(endTypeIndex=0;endTypeIndex<conceptQuery.nodeList.size();endTypeIndex++)
				{
					if(endTypeIndex==startTypeIndex)
						continue;
					if(conceptQuery.nodeList.get(endTypeIndex).equals(secondNode.type))
						break;
				}
				if(startTypeIndex>=conceptQuery.nodeList.size()||endTypeIndex>=conceptQuery.nodeList.size())
					continue;
				HyperEdge queryTemplate=new HyperEdge();
				queryTemplate.rel=index;
				queryTemplate.value=0.;
				queryTemplate.nodeList=new ArrayList<Integer>(conceptQuery.nodeList.size());
				for(int j=0;j<conceptQuery.nodeList.size();j++)
				{
					queryTemplate.nodeList.add(-1);
				}
				queryTemplate.nodeList.set(startTypeIndex, firstEntity);
				queryTemplate.nodeList.set(endTypeIndex,secondEntity);
				List<HyperEdge> subQueryList=new ArrayList<HyperEdge>();
				if(conceptQuery.nodeList.size()>2)
				{
					subQueryList.add(queryTemplate);
					for(int j=0;j<conceptQuery.nodeList.size();j++)
					{
						if(j==startTypeIndex||j==endTypeIndex)
							continue;
						List<HyperEdge> tempQueryList=subQueryList;
						subQueryList=new ArrayList<HyperEdge>();
						List<Integer> entityList=GlobalStaticData.typeToEntityList.get(conceptQuery.nodeList.get(j));
						for(int h=0;h<entityList.size();h++)
						{
							for(int k=0;k<tempQueryList.size();k++)
							{
								HyperEdge templateHe=tempQueryList.get(k);
								HyperEdge newTemplateHe=templateHe.copyHyperEdge();
								newTemplateHe.nodeList.set(j, entityList.get(h));
								subQueryList.add(newTemplateHe);
							}
						}
					}
				}
				else
				{
					subQueryList.add(queryTemplate);
				}
				if(firstNode.type.equals(secondNode.type))
				{
					
					//by wzy,暂时注释掉，我也不知道这为啥报错
					/*List<HyperEdge> reverseList=new ArrayList<HyperEdge>();
					for(int j=0;j<queryList.size();j++)
					{
						HyperEdge reverseHe=queryList.get(j).copyHyperEdge();
						Integer temp=reverseHe.nodeList.get(startTypeIndex);
						reverseHe.nodeList.set(startTypeIndex, reverseHe.nodeList.get(endTypeIndex));
						reverseHe.nodeList.set(endTypeIndex, temp);
						reverseList.add(reverseHe);
					}
					subQueryList.addAll(reverseList);*/
				}
				if(subQueryList!=null&&subQueryList.size()>0)
				{
					queryList.addAll(subQueryList);
				}
			}
			
			//LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\t\ttfalse\t"+queryList.size());
			//random choose
			
			//Filter undirect Rel, by wzy 2014.7.9
			tempList=queryList;
			queryList=new ArrayList<HyperEdge>();
			for(int i=0;i<tempList.size();i++)
			{
				HyperEdge tempEdge=tempList.get(i);
				if(GlobalStaticData.undirectRelSet.contains(tempEdge.rel))
				{
					if(tempEdge.nodeList.get(0).equals(firstEntity))
					{
						queryList.add(tempEdge);
					}
				}
				else
				{
					queryList.add(tempEdge);
				}
			}
			
			if(queryList.size()>MaxFalseQuery)
			{
				List<HyperEdge> tempQueryList=queryList;
				queryList=new ArrayList<HyperEdge>();
				for(int i=0;i<MaxFalseQuery;i++)
				{
					int queryIndex=Math.abs(rand.nextInt())%tempQueryList.size();
					queryList.add(tempQueryList.get(queryIndex));
				}
			}
			
			return queryList;
		}
	}	


	/**
	 * The Fomulizing algorithm need create queries with the head and end of the nodes' path, no matter they are true or false.<br>
	 * In this version of the algorithm, it is divided into two case:<br>
	 * As the FormulizeTrueEdgeFromTwoNode(first,second) function, if it can get at least one true hyperedge whose rel is<br>
	 * query in the parameters, the function return the List of the true hyperedges found. If these is no true hyperedge with<br>
	 * the query as rel, the function builds all possible false hyperedges.<br>
	 * In a special case, the query in conceptual hyperedge has only two variables just satisfying the two node in the source path.<br>
	 * If the query has more than  2 variables, it uses the GlobalStaticData.typeMapItoS to get all possible entities with<br>
	 * the 3th,4th,..., nth type to instead of the variables in the queries and do a Cartesian product to get all possible queries.
	 * @author Zhuoyu Wei
	 * @version 1.1 Whatever global query is, this version of the fomulizing algorithm is same,<br>
	 * but it need run filter by global query after it.
	 * @param firstEntity the first node of the query.
	 * @param secondEntity the second node of the query
	 * @return the list of hyperedges got in this function, and it is used as query list.
	 */
	private static List<HyperEdge> FomulizeQueryFromTwoNode(Integer firstEntity,Integer secondEntity)
	{
		HyperNode firstNode=GlobalStaticData.entityToNode.get(firstEntity);
		HyperNode secondNode=GlobalStaticData.entityToNode.get(secondEntity);
		List<HyperEdge> queryList=firstNode.edges.get(secondEntity);	
		//return queryList;
		if(queryList!=null&&queryList.size()>0)  //exist true query
			return queryList;
		else//exist no true query, and then build all probable false query.
		{
			queryList=new ArrayList<HyperEdge>();
			for(int i=0;i<GlobalStaticData.conceptRelList.size();i++)
			{
				HyperEdge conceptQuery=GlobalStaticData.conceptRelList.get(i);
				int startTypeIndex;
				int endTypeIndex;
				for(startTypeIndex=0;startTypeIndex<conceptQuery.nodeList.size();startTypeIndex++)
				{
					if(conceptQuery.nodeList.get(startTypeIndex).equals(firstNode.type))
						break;
				}
				for(endTypeIndex=0;endTypeIndex<conceptQuery.nodeList.size();endTypeIndex++)
				{
					if(endTypeIndex==startTypeIndex)
						continue;
					if(conceptQuery.nodeList.get(endTypeIndex).equals(secondNode.type))
						break;
				}
				if(startTypeIndex>=conceptQuery.nodeList.size()||endTypeIndex>=conceptQuery.nodeList.size())
					continue;
				HyperEdge queryTemplate=new HyperEdge();
				queryTemplate.rel=i;
				queryTemplate.value=0.;
				queryTemplate.nodeList=new ArrayList<Integer>(conceptQuery.nodeList.size());
				for(int j=0;j<conceptQuery.nodeList.size();j++)
				{
					queryTemplate.nodeList.add(-1);
				}
				queryTemplate.nodeList.set(startTypeIndex, firstEntity);
				queryTemplate.nodeList.set(endTypeIndex,secondEntity);
				List<HyperEdge> subQueryList=new ArrayList<HyperEdge>();
				if(conceptQuery.nodeList.size()>2)
				{
					subQueryList.add(queryTemplate);
					for(int j=0;j<conceptQuery.nodeList.size();j++)
					{
						if(j==startTypeIndex||j==endTypeIndex)
							continue;
						List<HyperEdge> tempQueryList=subQueryList;
						subQueryList=new ArrayList<HyperEdge>();
						List<Integer> entityList=GlobalStaticData.typeToEntityList.get(conceptQuery.nodeList.get(j));
						for(int h=0;h<entityList.size();h++)
						{
							for(int k=0;k<tempQueryList.size();k++)
							{
								HyperEdge templateHe=tempQueryList.get(k);
								HyperEdge newTemplateHe=templateHe.copyHyperEdge();
								newTemplateHe.nodeList.set(j, entityList.get(h));
								subQueryList.add(newTemplateHe);
							}
						}
					}
				}
				else
				{
					subQueryList.add(queryTemplate);
				}
				if(firstNode.type.equals(secondNode.type))
				{
					List<HyperEdge> reverseList=new ArrayList<HyperEdge>();
					for(int j=0;j<queryList.size();j++)
					{
						HyperEdge reverseHe=queryList.get(j).copyHyperEdge();
						Integer temp=reverseHe.nodeList.get(startTypeIndex);
						reverseHe.nodeList.set(startTypeIndex, reverseHe.nodeList.get(endTypeIndex));
						reverseHe.nodeList.set(endTypeIndex, temp);
						reverseList.add(reverseHe);
					}
					subQueryList.addAll(reverseList);
				}
				if(subQueryList!=null&&subQueryList.size()>0)
				{
					queryList.addAll(subQueryList);
				}
			}
			return queryList;
		}
	}	
	/**
	 * This is a function to formulize a entitative path of hyper nodes to obtain paths of hyperedges.<br>
	 * In this function, there is 4 step (or more in the future) as follow:<br>
	 * 1. Get query List by the function FomulizeQueryFromTwoNode. If it is empty, the function can stop here.<br>
	 * 2. Create all possible true paths of hyperedges as bodies by Cartesian product for the results of running<br>
	 * FormulizeTrueEdgeFromTwoNode for each two adjacent nodes.<br>
	 * 3. Use the similar mechanism to do Cartesian product and add queries for all paths of hyperedges.<br>
	 * 4. Filter paths got by check if it satisfies the hypothesis that each of node in hyperpath must appear more than twice.<br> 
	 * Tips: It will spend some time. That would be nice if use multithreads to deal with it.
	 * @param statepog the path of nodes, and it is noted as a state in the previous process of random walk. 
	 * @author Zhuoyu Wei
	 * @version 1.0 It keep the global query and keep the query always in the station of head in a formula.
	 * @param query the query for this process.
	 * @return Satisfied all hypotheses entitative hyperpath.
	 */
	public static List<HyperPath> FormulizeByQuery(StatePOG statepog,Integer query)
	{
		List<HyperPath> resultList=new ArrayList<HyperPath>();
		if(statepog.state.size()<2)
			return resultList;
		//Filter for query
		List<HyperEdge> queryList=FomulizeQueryFromTwoNode(statepog.state.get(0),
				statepog.state.get(statepog.state.size()-1),query);
		if(queryList==null||queryList.size()<=0)
			return resultList;
		
		//Cartesian product for formulizing body path
		resultList.add(new HyperPath());
		for(int i=0;i<statepog.state.size()-1;i++)
		{
			List<HyperPath> tempPathList=resultList;
			resultList=new ArrayList<HyperPath>();
			List<HyperEdge> trueEdgeList=FormulizeTrueEdgeFromTwoNode(statepog.state.get(i),statepog.state.get(i+1));
			if(trueEdgeList==null)
			{
				for(int j=0;j<statepog.state.size();j++)
				{
					HyperNode hn=GlobalStaticData.entityToNode.get(statepog.state.get(j));
					LogPrintStream.getPs_alwaysdebug().println(hn.entity);
					for(int k=0;k<hn.neighbourList.size();k++)
					{
						LogPrintStream.getPs_alwaysdebug().print(hn.neighbourList.get(k)+"\t");
					}
					LogPrintStream.getPs_alwaysdebug().println();
				}
				LogPrintStream.getPs_alwaysdebug().println();				
				LogPrintStream.getPs_alwaysdebug().println(statepog.state.get(i)+"\t"+statepog.state.get(i+1));
			}
			for(int j=0;j<tempPathList.size();j++)
			{
				HyperPath nowPath=tempPathList.get(j);
				for(int k=0;k<trueEdgeList.size();k++)
				{
					HyperPath newPath=nowPath.copyOnePath();
					newPath.path.add(trueEdgeList.get(k));
					resultList.add(newPath);
				}
			}
		}
		//add query at the end of path.
		List<HyperPath> tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		List<HyperEdge> trueEdgeList=queryList;
		for(int j=0;j<tempPathList.size();j++)
		{
			HyperPath nowPath=tempPathList.get(j);
			for(int k=0;k<trueEdgeList.size();k++)
			{
				HyperPath newPath=nowPath.copyOnePath();
				newPath.path.add(trueEdgeList.get(k));
				resultList.add(newPath);
			}
		}		
		
		//Filter for at least twice appearing of each entity.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckTwiceForNodes(hp))
			{
				resultList.add(hp);
			}
		}
		
		return resultList;
	}
	
	/**
	 * This is a function to formulize a entitative path of hyper nodes to obtain paths of hyperedges.<br>
	 * In this function, there is 4 step (or more in the future) as follow:<br>
	 * 1. Get query List by the function FomulizeQueryFromTwoNode. If it is empty, the function can stop here.<br>
	 * 2. Create all possible true paths of hyperedges as bodies by Cartesian product for the results of running<br>
	 * FormulizeTrueEdgeFromTwoNode for each two adjacent nodes.<br>
	 * 3. Use the similar mechanism to do Cartesian product and add queries for all paths of hyperedges.<br>
	 * 4. Filter paths got by check if it satisfies the hypothesis that each of node in hyperpath must appear more than twice.<br> 
	 * Tips: It will spend some time. That would be nice if use multithreads to deal with it.
	 * @author Zhuoyu Wei
	 * @version 1.1 no query for find the loop, the query can be at the position Body or Head.
	 * @param statepog the path of nodes, and it is noted as a state in the previous process of random walk. 
	 * @param query the query for this process.
	 * @return Satisfied all hypotheses entitative hyperpath.
	 */
	public static List<HyperPath> FormulizeByQuery(StatePOG statepog)
	{
		List<HyperPath> resultList=new ArrayList<HyperPath>();
		if(statepog==null||statepog.state==null||statepog.state.size()<2)
			return resultList;
		//Filter for query
		if(GlobalStaticData.MaxFalseQuery<0)
			GlobalStaticData.MaxFalseQuery=2;
		List<HyperEdge> queryList=FomulizeQueryFromTwoNodeRandomly(statepog.state.get(0),
				statepog.state.get(statepog.state.size()-1),GlobalStaticData.MaxFalseQuery);
		//List<HyperEdge> queryList=FomulizeQueryFromTwoNode(statepog.state.get(0),
				//statepog.state.get(statepog.state.size()-1));
		if(queryList==null||queryList.size()<=0)
			return resultList;
		
		//Cartesian product for formulizing body path
		resultList.add(new HyperPath());
		for(int i=0;i<statepog.state.size()-1;i++)
		{
			List<HyperPath> tempPathList=resultList;
			resultList=new ArrayList<HyperPath>();
			List<HyperEdge> trueEdgeList=FormulizeTrueEdgeFromTwoNode(statepog.state.get(i),statepog.state.get(i+1));
			//debug--->debug ps log
			if(trueEdgeList==null)
			{
				for(int j=0;j<statepog.state.size();j++)
				{
					HyperNode hn=GlobalStaticData.entityToNode.get(statepog.state.get(j));
					LogPrintStream.getPs_alwaysdebug().println(hn.entity);
					for(int k=0;k<hn.neighbourList.size();k++)
					{
						LogPrintStream.getPs_alwaysdebug().print(hn.neighbourList.get(k)+"\t");
					}
					LogPrintStream.getPs_alwaysdebug().println();
				}
				LogPrintStream.getPs_alwaysdebug().println();				
				LogPrintStream.getPs_alwaysdebug().println(statepog.state.get(i)+"\t"+statepog.state.get(i+1));
			}
			else
			{
				//Undirect Edges, by wzy,2014.7.9
				
				/*for(int j=0;j<trueEdgeList.size();j++)
				{
					HyperEdge tempEdge=trueEdgeList.get(j);
					if(GlobalStaticData.undirectRelSet.contains(tempEdge.rel))
					{
						Collections.sort(tempEdge.nodeList);
					}
				}*/
				
				for(int j=0;j<tempPathList.size();j++)
				{
					HyperPath nowPath=tempPathList.get(j);
					for(int k=0;k<trueEdgeList.size();k++)
					{
						HyperPath newPath=nowPath.copyOnePath();
						newPath.path.add(trueEdgeList.get(k));
						resultList.add(newPath);
					}
			}
			}
		}
		//add query at the end of path.
		List<HyperPath> tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		List<HyperEdge> trueEdgeList=queryList;
		for(int j=0;j<tempPathList.size();j++)
		{
			HyperPath nowPath=tempPathList.get(j);
			for(int k=0;k<trueEdgeList.size();k++)
			{
				HyperPath newPath=nowPath.copyOnePath();
				newPath.path.add(trueEdgeList.get(k));
				resultList.add(newPath);
			}
		}	
		
		LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
		//LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\t"+resultList.size()+"\t");
		//Filter for at least twice appearing of each entity.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckTwiceForNodes(hp))
			{
				resultList.add(hp);
			}
		}
		LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
		
		//Filter by query, appearing once and only once.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckQuery(hp,GlobalStaticData.query))
			{
				resultList.add(hp);
			}
		}		
		LogPrintStream.getPs_alwaysdebug().println(resultList.size()+"\t");
		


		
		return resultList;
	}
	/**
	 * Modify at 2014.8.13 by wzy 
	 * Its function is as the same as the original one, but for large size of edges for nodes.
	 * It changes to formulize one clause one time, and most MaxFalseQuery tims.
	 * Remove Cartesian product.
	 * @author Zhuoyu Wei
	 * @version 2.0
	 * @param statepog the path of nodes, and it is noted as a state in the previous process of random walk. 
	 * @param query the query for this process.
	 * @return Satisfied all hypotheses entitative hyperpath.
	 */
	public static List<HyperPath> FormulizeByQueryRandomly(StatePOG statepog)
	{
		List<HyperPath> resultList=new ArrayList<HyperPath>();
		if(statepog==null||statepog.state==null||statepog.state.size()<2)
			return resultList;
		//Filter for query
		if(GlobalStaticData.MaxFalseQuery<0)
			GlobalStaticData.MaxFalseQuery=2;
		List<HyperEdge> queryList=FomulizeQueryFromTwoNodeRandomly(statepog.state.get(0),
				statepog.state.get(statepog.state.size()-1),GlobalStaticData.MaxFalseQuery);
		//List<HyperEdge> queryList=FomulizeQueryFromTwoNode(statepog.state.get(0),
				//statepog.state.get(statepog.state.size()-1));
		if(queryList==null||queryList.size()<=0)
			return resultList;
		
		//Cartesian product for formulizing body path
		HyperPath onlyOnePath=new HyperPath();
		for(int i=0;i<statepog.state.size()-1;i++)
		{
			List<HyperEdge> trueEdgeList=FormulizeTrueEdgeFromTwoNode(statepog.state.get(i),statepog.state.get(i+1));
			Integer randIndex=Math.abs(rand.nextInt())%trueEdgeList.size();
			HyperEdge randEdge=trueEdgeList.get(randIndex);
			onlyOnePath.path.add(randEdge);
		}
		resultList.add(onlyOnePath);
		
		//add query at the end of path.
		List<HyperPath> tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		List<HyperEdge> trueEdgeList=queryList;
		for(int j=0;j<tempPathList.size();j++)
		{
			HyperPath nowPath=tempPathList.get(j);
			for(int k=0;k<trueEdgeList.size();k++)
			{
				HyperPath newPath=nowPath.copyOnePath();
				newPath.path.add(trueEdgeList.get(k));
				resultList.add(newPath);
			}
		}	
		
		LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
		//LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\t"+resultList.size()+"\t");
		//Filter for at least twice appearing of each entity.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckTwiceForNodes(hp))
			{
				resultList.add(hp);
			}
		}
		LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
		
		//Filter by query, appearing once and only once.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckQuery(hp,GlobalStaticData.query))
			{
				resultList.add(hp);
			}
		}		
		LogPrintStream.getPs_alwaysdebug().println(resultList.size()+"\t");
		


		
		return resultList;
	}
	
	
	/**
	 * Check whether each entity in the hyperpath appears more than twice. It is according to a hypothesis for MLN by Mrs. Sun
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param hp the hyperpath being check.
	 * @return the boolean value for this check.
	 */
	public static boolean CheckTwiceForNodes(HyperPath hp)
	{
		Map<Integer,Integer> nodeCount=new TreeMap<Integer,Integer>();
		for(int i=0;i<hp.path.size();i++)
		{
			HyperEdge he=hp.path.get(i);
			for(int j=0;j<he.nodeList.size();j++)
			{
				Integer count=nodeCount.get(he.nodeList.get(j));
				if(count==null)
				{
					count=1;
					nodeCount.put(he.nodeList.get(j), count);
				}
				else
				{
					count++;
					nodeCount.remove(he.nodeList.get(j));
					nodeCount.put(he.nodeList.get(j), count);
				}
				
			}
		}
		boolean flag=true;
		Iterator it=nodeCount.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			if((Integer)entry.getValue()<2)
			{
				flag=false;
				break;
			}
		}
		return flag;
	}
	
	/**
	 * It concepts a entitative hyperpath with replacing the entity in the hyperpath with its type and index.<br>
	 * Its type and the type's index composes a variable for the conceptual path. 
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param entityHp the path of entity.
	 * @return the conceptual hyperpath
	 */
	public static HyperPath Conceptualize(HyperPath entityHp)
	{
		HyperPath conceptHp=new HyperPath();
		Map<Integer,Integer> entityToConcept=new TreeMap<Integer,Integer>();
		Map<Integer,Integer> conceptToCount=new TreeMap<Integer,Integer>();
		for(int i=0;i<entityHp.path.size();i++)
		{
			HyperEdge entityHe=entityHp.path.get(i);
			HyperEdge conceptHe=new HyperEdge();
			conceptHe.rel=entityHe.rel;
			for(int j=0;j<entityHe.nodeList.size();j++)
			{
				HyperNode hn=GlobalStaticData.entityToNode.get(entityHe.nodeList.get(j));
				Integer conceptIndex=entityToConcept.get(hn.entity);
				if(conceptIndex==null)
				{
					Integer conceptCount=conceptToCount.get(hn.type);
					if(conceptCount==null)
					{
						conceptCount=0;
						conceptToCount.put(hn.type, 0);
					}
					else
					{
						conceptCount++;
						conceptToCount.remove(hn.type);
						conceptToCount.put(hn.type, conceptCount);
					}
					conceptIndex=hn.type+GlobalStaticData.typeMapItoS.size()*conceptCount; //encoding function.
					entityToConcept.put(hn.entity, conceptIndex);
				}
				conceptHe.nodeList.add(conceptIndex);
			}
			conceptHp.path.add(conceptHe);
		}
		return conceptHp;
	}
	
	
	/**
	 * The Formulize and Concept main process, it reads paths of nodes from middle file.<br>
	 * For each line from the file, we formulize it and concept all entivative hyperpaths in the result of formulizing.
	 * @author Zhuoyu Wei
	 * @version 1.0 single thread.
	 * @param middleFile middle file name
	 * @deprecated
	 *//*
	public static void ReadPathsAndProduceTrainningData(String middleFile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(middleFile));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				StatePOG statepog=new StatePOG();
				for(int i=0;i<ss.length;i++)
				{
					statepog.state.add(Integer.parseInt(ss[i]));
				}
				//Don't forget to be sure the query is set.
				List<HyperPath> hyperPathList=FormulizeByQuery(statepog,GlobalStaticData.query);
				//List<HyperPath> hyperPathList=FormulizeByQuery(statepog);
				for(int i=0;i<hyperPathList.size();i++)
				{
					HyperPath entityHp=hyperPathList.get(i);
					HyperPath conceptualHp=Conceptualize(entityHp);
					HyperEdge hyperQuery=entityHp.path.get(entityHp.path.size()-1);
					Map<HyperPath,TrueFalseCount> hmap=GlobalStaticData.queryToCount.get(hyperQuery);
					if(hmap==null)
					{
						hmap=new TreeMap<HyperPath,TrueFalseCount>();
						GlobalStaticData.queryToCount.put(hyperQuery,hmap);
					}
					TrueFalseCount tfCount=hmap.get(conceptualHp);
					if(tfCount==null)
					{
						tfCount=new TrueFalseCount();
						hmap.put(conceptualHp, tfCount);
					}
					if(hyperQuery.value>0.5)
					{
						tfCount.trueCount+=1;
					}
					else
					{
						tfCount.falseCount+=1;
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
	}*/
	
	/**
	 * The Formulize and Concept main process, it reads paths of nodes from middle file.<br>
	 * For each line from the file, we formulize it and concept all entivative hyperpaths in the result of formulizing.
	 * @author Zhuoyu Wei
	 * @version 1.1 single thread. The query can be anywhere of the formula
	 * @param middleFile middle file name
	 */
	public static void ReadPathsAndProduceTrainningData(String middleFile
			,Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount)
	{
		try {
			//BufferedReader br=new BufferedReader(new FileReader(middleFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(middleFile),"utf-8"));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				StatePOG statepog=new StatePOG();
				for(int i=0;i<ss.length;i++)
				{
					statepog.state.add(Integer.parseInt(ss[i]));
				}
				//Don't forget to be sure the query is set.
				//List<HyperPath> hyperPathList=FormulizeByQuery(statepog,GlobalStaticData.query);
				//System.out.println(buffer);
				long tempstarttime=System.currentTimeMillis();
				List<HyperPath> hyperPathList=FormulizeByQuery(statepog);
				//List<HyperPath> hyperPathList=FormulizeByQueryRandomly(statepog);				
				long tempendtime=System.currentTimeMillis();	
				//System.out.println("FormulizeByQueryRandomly is over:\t"+(tempendtime-tempstarttime)+"ms");
				//LogPrintStream.getPs_RandomWalkThread_RandomWalk().println((tempendtime-tempstarttime)+"ms\t"
						//+hyperPathList.size()+"\t"+statepog.state);
				for(int i=0;i<hyperPathList.size();i++)
				{
					HyperPath entityHp=hyperPathList.get(i);
					HyperPath conceptualHp=Conceptualize(entityHp);
					HyperEdge hyperQuery=null;
					int queryIndex=-1;
					for(int j=0;j<entityHp.path.size();j++)
					{
						HyperEdge tempEdge=entityHp.path.get(j);
						if(tempEdge.rel.equals(GlobalStaticData.query))
						{
							queryIndex=j;
							hyperQuery=tempEdge;
							break;
						}
					}
					if(hyperQuery==null)
					{
						LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println("Error: No query in the entity fomular.");
						LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print("\t");
						OutputData.PrintOneEntityHyperPath(LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData(), entityHp);
						LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println();
					}
					Map<HyperPath,TrueFalseCount> hmap=queryToCount.get(hyperQuery);
					if(hmap==null)
					{
						hmap=new TreeMap<HyperPath,TrueFalseCount>();
						queryToCount.put(hyperQuery,hmap);
					}
					TrueFalseCount tfCount=hmap.get(conceptualHp);
					if(tfCount==null)
					{
						tfCount=new TrueFalseCount();
						hmap.put(conceptualHp, tfCount);
					}
					// count the true value.
					Boolean[] truthQueryList=new Boolean[entityHp.path.size()];
					for(int j=0;j<entityHp.path.size();j++)
					{
						truthQueryList[j]=entityHp.path.get(j).value>0.5;
					}
					truthQueryList[queryIndex]=true;
					if(CheckFormulaTrueOrFalse(truthQueryList))
					{
						tfCount.trueCount+=1;
					}
					truthQueryList[queryIndex]=false;					
					if(CheckFormulaTrueOrFalse(truthQueryList))
					{
						//by wzy, change back immediately
						tfCount.falseCount+=1;
					}
					
					//print
					char[] charlist=new char[entityHp.path.size()];
					for(int j=0;j<truthQueryList.length;j++)
					{
						if(j==queryIndex)
							charlist[j]='q';
						else if(truthQueryList[j])
							charlist[j]='t';
						else
							charlist[j]='f';
					}

					OutputData.PrintOneEntityHyperEdge(LogPrintStream.ps_debug_testcount, hyperQuery);
					LogPrintStream.ps_debug_testcount.print("\t");
					OutputData.PrintOneConceptHyperPath(LogPrintStream.ps_debug_testcount, conceptualHp);
					LogPrintStream.ps_debug_testcount.print("\t");
					for(int j=0;j<charlist.length;j++)
					{
						LogPrintStream.ps_debug_testcount.print(charlist[j]);
					}
					LogPrintStream.ps_debug_testcount.println();
					

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
	/**
	 * This function is a complex function, it take several task at the same time, which contains:<br>
	 * 1. Add query itself to its feature as a conceptual formula.<br>
	 * 2. Index all conceptual formula use Integer, and use these index for train or test, these map relations are saved<br>
	 * in GlobalStaticData.conceptFormulaToInde and indexToConceptFormula.<br>
	 * 3. Produce train or test datapoint by indexing the map from entity query to conceptual pair.<br>
	 * @author Zhuoyu Wei
	 * @version 1.0 the query must be at the position of the head, so the boolean value of the formula is the same as the query triple.
	 * @param queryMap The map from entitative query to counts of conceptual formulas
	 * @param dpList data point list
	 * @deprecated
	 */
	/*public static void IndexQueryAndProduceDataPoint(Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryMap,List<DataPoint> dpList)
	{
		Iterator it=queryMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			HyperEdge queryHe=(HyperEdge)entry.getKey();
			Map<HyperPath,TrueFalseCount> conceptCount=(Map<HyperPath,TrueFalseCount>)entry.getValue();
			
			//Conceptualize and add the path with only one hyperedge(the query).
			HyperPath oneHp=new HyperPath();
			oneHp.path.add(queryHe);
			oneHp=MLNProcess.Conceptualize(oneHp); 
			TrueFalseCount tfc=new TrueFalseCount();
			if(queryHe.value>0.5)
				tfc.trueCount+=1;
			else
				tfc.falseCount+=1;
			conceptCount.put(oneHp, tfc);
			
			Iterator itCount=conceptCount.entrySet().iterator();
			DataPoint datapoint=new DataPoint();
			datapoint.query=queryHe;
			datapoint.nodeList=new ArrayList<Integer>(conceptCount.size());
			datapoint.countList=new ArrayList<TrueFalseCount>(conceptCount.size());
			while(itCount.hasNext())
			{
				Map.Entry entryCount=(Map.Entry)itCount.next();
				HyperPath conceptRelation=(HyperPath)entryCount.getKey();
				Integer conceptIndex=GlobalStaticData.conceptFormulaToIndex.get(conceptRelation);
				if(conceptIndex==null)
				{
					conceptIndex=GlobalStaticData.indexToConceptFormula.size();
					GlobalStaticData.conceptFormulaToIndex.put(conceptRelation, conceptIndex);
					GlobalStaticData.indexToConceptFormula.add(conceptRelation);
				}
				TrueFalseCount nodeCount=(TrueFalseCount)entryCount.getValue();
				datapoint.nodeList.add(conceptIndex);
				datapoint.countList.add(nodeCount);
			}
			dpList.add(datapoint);
		}
	}*/
	/**
	 * This function is a complex function, it take several task at the same time, which contains:<br>
	 * 1. Add query itself to its feature as a conceptual formula.<br>
	 * 2. Index all conceptual formula use Integer, and use these index for train or test, these map relations are saved<br>
	 * in GlobalStaticData.conceptFormulaToInde and indexToConceptFormula.<br>
	 * 3. Produce train or test datapoint by indexing the map from entity query to conceptual pair.<br>
	 * @author Zhuoyu Wei
	 * @version 1.1 Add the query can be any where, body or head. Therefore, there is two changes:<br>
	 * 1. The formular which is query itself can not be set the last hyperedge in the path,  but need for 0 to size() to find.<br>
	 * 2. No more temply...... 
	 * @param queryMap The map from entitative query to counts of conceptual formulas
	 * @param dpList data point list
	 */
	public static void IndexQueryAndProduceDataPointTrain(Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryMap,List<DataPoint> dpList
			,List<HyperPath> indexToConceptFormula,Map<HyperPath,Integer> conceptFormulaToIndex)
	{
		Iterator it=queryMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			HyperEdge queryHe=(HyperEdge)entry.getKey();
			Map<HyperPath,TrueFalseCount> conceptCount=(Map<HyperPath,TrueFalseCount>)entry.getValue();
			
			//Conceptualize and add the path with only one hyperedge(the query).
			HyperPath oneHp=new HyperPath();
			oneHp.path.add(queryHe);
			oneHp=MLNProcess.Conceptualize(oneHp); 
			TrueFalseCount tfc=new TrueFalseCount();
			/*if(queryHe.value>0.5)
				tfc.trueCount+=1;
			else
				tfc.falseCount+=1;*/
			tfc.trueCount=1; //query => query, no matter query is true or false, this formula is true.
			tfc.falseCount=0;
			
			conceptCount.put(oneHp, tfc);
			
			Iterator itCount=conceptCount.entrySet().iterator();
			DataPoint datapoint=new DataPoint();
			datapoint.query=queryHe;
			datapoint.nodeList=new ArrayList<Integer>(conceptCount.size());
			datapoint.countList=new ArrayList<TrueFalseCount>(conceptCount.size());
			while(itCount.hasNext())
			{
				Map.Entry entryCount=(Map.Entry)itCount.next();
				HyperPath conceptRelation=(HyperPath)entryCount.getKey();
				Integer conceptIndex=conceptFormulaToIndex.get(conceptRelation);
				if(conceptIndex==null)
				{
					conceptIndex=indexToConceptFormula.size();
					conceptFormulaToIndex.put(conceptRelation, conceptIndex);
					indexToConceptFormula.add(conceptRelation);
				}
				TrueFalseCount nodeCount=(TrueFalseCount)entryCount.getValue();
				datapoint.nodeList.add(conceptIndex);
				datapoint.countList.add(nodeCount);
			}
			dpList.add(datapoint);
		}
	}	
	
	/**
	 * As the IndexQueryAndProduceDataPointTrain() function, this function produce the data points by transform test data.<br>
	 * You can see the introduction of IndexQueryAndProduceDataPointTrain() to learn more detail information about this function.<br>
	 * There are two difference:<br>
	 * 1. This function produce a data point for each query in the queryList be inferenced, no matter whether it appears in the path finds 
	 * 2. The conceptual formulas found in Formulizing process are abandoned if they never appear when learn the model.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param queryMap The map from entitative query to counts of conceptual formulas
	 * @param queryList all queries you need infer no matter they are true or false, it is for testing.
	 * @param dpList data point list
	 */
	public static void IndexQueryAndProduceDataPointTest(Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryMap
			,List<HyperEdge> queryList,List<DataPoint> dpList,
			List<HyperPath> indexToConceptFormula,Map<HyperPath,Integer> conceptFormulaToIndex)
	{
		
		//dpList=new ArrayList<DataPoint>();
		for(int i=0;i<queryList.size();i++)
		{
			HyperEdge query=queryList.get(i);
			DataPoint dp=new DataPoint();
			dp.query=query;
			dp.nodeList=new ArrayList<Integer>();
			dp.countList=new ArrayList<TrueFalseCount>();
			Map<HyperPath,TrueFalseCount> conceptCount=queryMap.get(query);
			if(conceptCount==null)
			{
				conceptCount=new TreeMap<HyperPath,TrueFalseCount>();
			}
			//HyperPath oneHp=new HyperPath();
			//oneHp.path.add(query);
			//oneHp=MLNProcess.Conceptualize(oneHp); 
			TrueFalseCount tfc=new TrueFalseCount();
			/*if(queryHe.value>0.5)
				tfc.trueCount+=1;
			else
				tfc.falseCount+=1;*/
			tfc.trueCount=1; //query => query, no matter query is true or false, this formula is true.
			tfc.falseCount=0;
			conceptCount.put(GlobalStaticData.ProduceOneCoceptQueryPath(), tfc);
			Iterator it=conceptCount.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry entry=(Map.Entry)it.next();
				HyperPath formula=(HyperPath)entry.getKey();
				Integer formulaIndex=conceptFormulaToIndex.get(formula);
				if(formulaIndex!=null)
				{
					tfc=(TrueFalseCount)entry.getValue();
					dp.nodeList.add(formulaIndex);
					dp.countList.add(tfc);
				}
			}
			dpList.add(dp);
		}
	}	
	/**
	 * Check if a entity formula(hyperpath) has only one query, no more and no less.
	 * @author Zhuoyu Wei
	 * @version 1.0 
	 * @param formula the entity hyperpath
	 * @param query
	 * @return a boolean value, true for only one query appearing in the hyperpath.
	 */
	public static boolean CheckQuery(HyperPath formula,Integer query)
	{
		int count=0;
		for(int i=0;i<formula.path.size();i++)
		{
			HyperEdge he=formula.path.get(i);
			if(he.rel.equals(query))
			{
				count++;
			}
		}
		return count==1;
	}
	
	/**
	 * Check a formula's boolean list is true or false. The last boolean value is head, and others is body.<br>
	 * The final truth value is flag=!body[0]V!body[1]V....V!body[i-1]Vbody[i].
	 * @author Zhuoyu Wei
	 * @verison 1.0
	 * @param boolList The list of truth value formed from a formula.
	 * @return the flag value.
	 */
	public static boolean CheckFormulaTrueOrFalse(Boolean[] boolList)
	{
		boolean flag=false;
		for(int i=0;i<boolList.length-1;i++)
		{
			flag=flag||!boolList[i];
			if(flag)
				break;
		}
		flag=flag||boolList[boolList.length-1];
		return flag;
	}
	
	/**
	 * After producing the train data points, this function traverse all conceptual formulas got in the formulizing process<br>
	 * to keep ones appearing at least once. And it also rebuild the map from conceptual formulas to index and reproduce new<br>
	 * data points from the old ones.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public static void FilterFormulaByAtLeastOnceTrueForTrain()
	{
		//choose the conceptual formulas with at least once true example in the knowledge base
		List<DataPoint> dpList=GlobalStaticData.trainDataPointList;
		int[] trueCount=new int[GlobalStaticData.indexToConceptFormula.size()];
		for(int i=0;i<dpList.size();i++)
		{
			DataPoint dp=dpList.get(i);
			boolean truthvalue=dp.query.value>0.5;
			for(int j=0;j<dp.countList.size();j++)
			{
				TrueFalseCount tfc=dp.countList.get(j);
				if((truthvalue&&tfc.trueCount>0)||(!truthvalue&&tfc.falseCount>0))
				{
					trueCount[dp.nodeList.get(j)]++;
				}
			}
		}
		List<HyperPath> indexToConceptFormula=new ArrayList<HyperPath>();
		Map<HyperPath,Integer> conceptFormulaToIndex=new TreeMap<HyperPath,Integer>();
		
		//remove the conceptual formula with zero appearings from the global index.
		for(int i=0;i<trueCount.length;i++)
		{
			if(trueCount[i]>0)
			{
				HyperPath hp=GlobalStaticData.indexToConceptFormula.get(i);
				conceptFormulaToIndex.put(hp,indexToConceptFormula.size());
				indexToConceptFormula.add(hp);
			}
		}

		
		//rebuild the data points with the new indexs
		GlobalStaticData.trainDataPointList=new ArrayList<DataPoint>();
		for(int i=0;i<dpList.size();i++)
		{
			DataPoint dp=dpList.get(i);
			DataPoint dpnew=new DataPoint();
			dpnew.query=dp.query;
			dpnew.countList=new ArrayList<TrueFalseCount>();
			dpnew.nodeList=new ArrayList<Integer>();
			for(int j=0;j<dp.nodeList.size();j++)
			{
				HyperPath hp=GlobalStaticData.indexToConceptFormula.get(dp.nodeList.get(j));
				Integer newIndex=conceptFormulaToIndex.get(hp);
				if(newIndex!=null)
				{
					dpnew.nodeList.add(newIndex);
					dpnew.countList.add(dp.countList.get(j));
				}
			}
			if(dpnew.countList.size()>1)
			{
				GlobalStaticData.trainDataPointList.add(dpnew);
			}
		}
		
		
		GlobalStaticData.indexToConceptFormula=indexToConceptFormula;
		GlobalStaticData.conceptFormulaToIndex=conceptFormulaToIndex;	
	}
	
	
	/**
	 * The Formulize and Concept main process, it reads paths of nodes from middle file.<br>
	 * For each line from the file, we formulize it and concept all entivative hyperpaths in the result of formulizing.
	 * @author Zhuoyu Wei
	 * @version 1.1 single thread. The query can be anywhere of the formula
	 * @param middleFile middle file name
	 */
	public static void ReadPathsAndProduceTrainningTest(String middleFile,Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(middleFile));
			//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(middleFile),"utf-8"));
			String buffer=null;
			int statecount0=0;
			int statecount1=0;
			int statecount2=0;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				StatePOG statepog=new StatePOG();
				for(int i=0;i<ss.length;i++)
				{
					statepog.state.add(Integer.parseInt(ss[i]));
				}

/*				HyperNode head_node=GlobalStaticData.entityToNode.get(statepog.state.get(0));
				List<HyperEdge> queryList=head_node.edges.get(statepog.state.get(1));
				HyperEdge queryEdge=null;
				for(int i=0;i<queryList.size();i++)
				{
					queryEdge=queryList.get(i);
					if(queryEdge.rel.equals(GlobalStaticData.query))
					{
						break;
					}
				}
				if(queryEdge==null||!queryEdge.rel.equals(GlobalStaticData.query))
					continue;*/
				
				AIntSet ais=new AIntSet();
				ais.set.add(statepog.state.get(0));
				ais.set.add(statepog.state.get(1));
				HyperEdge queryEdge=GlobalStaticData.entitivesetToQuery.get(ais);
				if(queryEdge==null||!queryEdge.rel.equals(GlobalStaticData.query))
					continue;
				
				
				//Check
				
				int statetype=CheckHeadAndTailTriple(statepog);
				
				if(statetype==0)
				{
					statecount0++;
					continue;
				}
				else if(statetype==2)
				{
					statecount2++;
					statepog=ChangeStatePog(statepog);
				}
				else
				{
					statecount1++;
				}
				
				
				List<HyperPath> hyperPathList=FormulizeByType(statepog,queryEdge,statetype);
				
				
				for(int i=0;i<hyperPathList.size();i++)
				{
					HyperPath entityHp=hyperPathList.get(i);
					HyperPath conceptualHp=Conceptualize(entityHp);
					//Check the same edge appearing in the same conceptual formula.
					//if(!CheckConceptualTwoSameEdge(conceptualHp))
						//continue;
					HyperEdge hyperQuery=null;
					int queryIndex=-1;
					for(int j=0;j<entityHp.path.size();j++)
					{
						HyperEdge tempEdge=entityHp.path.get(j);
						if(tempEdge.rel.equals(GlobalStaticData.query))
						{
							queryIndex=j;
							hyperQuery=tempEdge;
							break;
						}
					}
					if(hyperQuery==null)
					{
						LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println("Error: No query in the entity fomular.");
						LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print("\t");
						OutputData.PrintOneEntityHyperPath(LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData(), entityHp);
						LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println();
					}
					Map<HyperPath,TrueFalseCount> hmap=queryToCount.get(hyperQuery);
					if(hmap==null)
					{
						hmap=new TreeMap<HyperPath,TrueFalseCount>();
						queryToCount.put(hyperQuery,hmap);
					}
					TrueFalseCount tfCount=hmap.get(conceptualHp);
					if(tfCount==null)
					{
						tfCount=new TrueFalseCount();
						hmap.put(conceptualHp, tfCount);
					}
					// count the true value.
					Boolean[] truthQueryList=new Boolean[entityHp.path.size()];
					for(int j=0;j<entityHp.path.size();j++)
					{
						truthQueryList[j]=entityHp.path.get(j).value>0.5;
					}
					truthQueryList[queryIndex]=true;
					if(CheckFormulaTrueOrFalse(truthQueryList))
					{
						tfCount.trueCount+=1;
					}
					truthQueryList[queryIndex]=false;					
					if(CheckFormulaTrueOrFalse(truthQueryList))
					{
						//by wzy, change back immediately
						tfCount.falseCount+=1;
					}
				}
			}
			
			System.out.println("Infer discriminally count:\t"+statecount0+"\t"+statecount1+"\t"+statecount2);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/**
	 * Check a statepog is belong to which type path, it works only in the new infer process.
	 * @author Zhuoyu Wei
	 * @version 2.0
	 * @param statepog
	 * @return a type
	 * 0: error, continue;
	 * 1: original head is false, keep the state, and query is in the body
	 * 2: original head is true, change the state by turn the path, query is to the head
	 */
	public static int CheckHeadAndTailTriple(StatePOG statepog)
	{
		if(statepog.state.size()<2)
			return 0;
		
		Integer head_node=statepog.state.get(0);
		Integer tail_node=statepog.state.get(statepog.state.size()-1);
		
		if(!CheckHeadAndTailTripleInDB(head_node,tail_node))
			return 1;
		else
			return 2;
	}
	private static boolean CheckHeadAndTailTripleInDB(Integer firstEntity,Integer secondEntity)
	{
		HyperNode firstNode=GlobalStaticData.entityToNode.get(firstEntity);
		
		List<HyperEdge> queryList=firstNode.edges.get(secondEntity);	
		//return queryList;
		if(queryList!=null&&queryList.size()>0)  //exist true query
		{
			return true;
		}
		else//exist no true query, and then build all probable false query.
		{
			return false;
		}
	}
	/**
	 * If CheckHeadAndTailTriple() return the type is 2, you need use this function to change the path(state)
	 * @author Zhuoyu Wei
	 * @version 2.0
	 * @param s the source state
	 * @return the new state(path)
	 */
	public static StatePOG ChangeStatePog(StatePOG s)
	{
		StatePOG d=new StatePOG();
		for(int i=1;i<s.state.size();i++)
		{
			d.state.add(s.state.get(i));
		}
		d.state.add(s.state.get(0));
		return d;
	}
	/**
	 * The Introduction about this is like the up one.
	 * 
	 * @author Zhuoyu Wei
	 * @version 1.1 no query for find the loop, the query can be at the position Body or Head.
	 * @param statepog the path of nodes, and it is noted as a state in the previous process of random walk. 
	 * @param type tttq->f or ttt->q
	 * @return Satisfied all hypotheses entitative hyperpath.
	 */
	public static List<HyperPath> FormulizeByType(StatePOG statepog,HyperEdge queryEdge,int type)
	{
		List<HyperPath> resultList=new ArrayList<HyperPath>();
		List<HyperEdge> queryList;
		if(type==1)
		{
			if(GlobalStaticData.MaxFalseQuery<0)
				GlobalStaticData.MaxFalseQuery=5;
			queryList=FomulizeQueryFromTwoNodeRandomly(statepog.state.get(0),
				statepog.state.get(statepog.state.size()-1),GlobalStaticData.MaxFalseQuery);
			if(queryList==null||queryList.size()<=0)
				return resultList;
			
			//Cartesian product for formulizing body path
			HyperPath queryHp=new HyperPath();
			queryHp.path.add(queryEdge);
			resultList.add(queryHp);
			for(int i=1;i<statepog.state.size()-1;i++)
			{
				List<HyperPath> tempPathList=resultList;
				resultList=new ArrayList<HyperPath>();
				List<HyperEdge> trueEdgeList=FormulizeTrueEdgeFromTwoNode(statepog.state.get(i),statepog.state.get(i+1));

				for(int j=0;j<tempPathList.size();j++)
				{
					HyperPath nowPath=tempPathList.get(j);
					for(int k=0;k<trueEdgeList.size();k++)
					{
						HyperPath newPath=nowPath.copyOnePath();
						newPath.path.add(trueEdgeList.get(k));
						resultList.add(newPath);
					}
				}
			}
			
			
			
			
		}
		else
		{
			queryList=new ArrayList<HyperEdge>();
			queryList.add(queryEdge);
			//Cartesian product for formulizing body path
			resultList.add(new HyperPath());
			for(int i=0;i<statepog.state.size()-1;i++)
			{
				List<HyperPath> tempPathList=resultList;
				resultList=new ArrayList<HyperPath>();
				List<HyperEdge> trueEdgeList=FormulizeTrueEdgeFromTwoNode(statepog.state.get(i),statepog.state.get(i+1));

				for(int j=0;j<tempPathList.size();j++)
				{
					HyperPath nowPath=tempPathList.get(j);
					for(int k=0;k<trueEdgeList.size();k++)
					{
						HyperPath newPath=nowPath.copyOnePath();
						newPath.path.add(trueEdgeList.get(k));
						resultList.add(newPath);
					}
				}
			}
		}

		
		//add query at the end of path.
		List<HyperPath> tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		List<HyperEdge> trueEdgeList=queryList;
		for(int j=0;j<tempPathList.size();j++)
		{
			HyperPath nowPath=tempPathList.get(j);
			for(int k=0;k<trueEdgeList.size();k++)
			{
				HyperPath newPath=nowPath.copyOnePath();
				newPath.path.add(trueEdgeList.get(k));
				resultList.add(newPath);
			}
		}	
		
		//LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
		//LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\t"+resultList.size()+"\t");
		//Filter for at least twice appearing of each entity.
		
	
		
		
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckTwiceForNodes(hp))
			{
				resultList.add(hp);
			}
		}
		//LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
				
		//Filter by query, appearing once and only once.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckQuery(hp,GlobalStaticData.query))
			{
				resultList.add(hp);
			}
		}	

		return resultList;
	}	
	
	/**
	 * It is build a new map from hyperpath to true and false count, and filter formulas <\b>
	 * which has the same head and body (despite order)
	 * @author Zhuoyu Wei
	 * @version 1.0 There is something wrong in this methods, which treats the some different formulas as the same
	 * @param queryMap
	 * @return
	 */
	public static Map<HyperEdge,Map<HyperPath,TrueFalseCount>> FilterQueryCount(Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryMap)
	{
		Map<HyperEdge,Map<HyperPath,TrueFalseCount>> result=new HashMap<HyperEdge,Map<HyperPath,TrueFalseCount>>();
		
		Iterator it=queryMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			HyperEdge he=(HyperEdge)entry.getKey();
			Map<HyperPath,TrueFalseCount> map=(Map<HyperPath,TrueFalseCount>)entry.getValue();
			Map<HyperPathUnique,TrueFalseCount> filtermap=new HashMap<HyperPathUnique,TrueFalseCount>();
			Iterator it2=map.entrySet().iterator();
			while(it2.hasNext())
			{
				Map.Entry entry2=(Map.Entry)it2.next();
				HyperPath duhp=(HyperPath)entry2.getKey();
				TrueFalseCount duhp_tf=(TrueFalseCount)entry2.getValue();
				HyperPathUnique duhpu=new HyperPathUnique();
				duhpu.CopyPathData(duhp);
				TrueFalseCount duhpu_tf=(TrueFalseCount)filtermap.get(duhpu);
				if(duhpu_tf==null)
				{
					filtermap.put(duhpu, duhp_tf);
				}
				else
				{
					duhpu_tf.trueCount+=duhp_tf.trueCount;
					duhpu_tf.falseCount+=duhp_tf.falseCount;
				}
			}
			it2=filtermap.entrySet().iterator();
			map=new TreeMap<HyperPath,TrueFalseCount>();
			
			while(it2.hasNext())
			{
				Map.Entry entry2=(Map.Entry)it2.next();
				HyperPathUnique newhpu=(HyperPathUnique)entry2.getKey();
				TrueFalseCount tf=(TrueFalseCount)entry2.getValue();
				HyperPath newhp=newhpu.ChangeToHyperPath();
				map.put(newhp, tf);
			}
			
			result.put(he, map);
		}
		
		return result;
	}
	
	/**
	 * It is build a new map from hyperpath to true and false count, and filter formulas <\b>
	 * which has the same head and body (despite order)
	 * @author Zhuoyu Wei
	 * @version 1.0 There is something wrong in this methods, which treats the some different formulas as the same
	 * @param queryMap
	 * @return
	 */
	public static Map<HyperEdge,Map<HyperPath,TrueFalseCount>> FilterQueryCountByOrders(Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryMap)
	{
		Map<HyperEdge,Map<HyperPath,TrueFalseCount>> result=new HashMap<HyperEdge,Map<HyperPath,TrueFalseCount>>();
		
		Iterator it=queryMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			HyperEdge he=(HyperEdge)entry.getKey();
			Map<HyperPath,TrueFalseCount> map=(Map<HyperPath,TrueFalseCount>)entry.getValue();
			Map<HyperPath,TrueFalseCount> filtermap=new HashMap<HyperPath,TrueFalseCount>();
			Iterator it2=map.entrySet().iterator();
			while(it2.hasNext())
			{
				Map.Entry entry2=(Map.Entry)it2.next();
				HyperPath duhp=(HyperPath)entry2.getKey();
				TrueFalseCount duhp_tf=(TrueFalseCount)entry2.getValue();
				HyperPath duhpu=duhp.copyOnePath();
				duhpu.PollMin();

				TrueFalseCount duhpu_tf=(TrueFalseCount)filtermap.get(duhpu);
				if(duhpu_tf==null)
				{
					filtermap.put(duhpu, duhp_tf);
				}
				else
				{
					duhpu_tf.trueCount+=duhp_tf.trueCount;
					duhpu_tf.falseCount+=duhp_tf.falseCount;
				}
			}
			it2=filtermap.entrySet().iterator();
			map=new TreeMap<HyperPath,TrueFalseCount>();
			
			while(it2.hasNext())
			{
				Map.Entry entry2=(Map.Entry)it2.next();
				HyperPath newhpu=(HyperPath)entry2.getKey();
				TrueFalseCount tf=(TrueFalseCount)entry2.getValue();
				HyperPath newhp=newhpu.copyOnePath();
				map.put(newhp, tf);
			}
			
			result.put(he, map);
		}
		
		return result;
	}
	
	/**
	 * The Formulize and Concept main process, it reads paths of nodes from middle file.<br>
	 * For each line from the file, we formulize it and concept all entivative hyperpaths in the result of formulizing.
	 * @author Zhuoyu Wei
	 * @version 1.1 single thread. The query can be anywhere of the formula
	 * @param middleFile middle file name
	 */
	public static void ReadPathsAndProduceTrainningDataWithCheck(String middleFile
			,Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount,Integer globalquery,boolean usemap)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(middleFile));
			//BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(middleFile),"utf-8"));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				StatePOG statepog=new StatePOG();
				for(int i=0;i<ss.length;i++)
				{
					statepog.state.add(Integer.parseInt(ss[i]));
				}
				//Don't forget to be sure the query is set.
				//List<HyperPath> hyperPathList=FormulizeByQuery(statepog,GlobalStaticData.query);
				//System.out.println(buffer);
				long tempstarttime=System.currentTimeMillis();
				List<HyperPath> hyperPathList=null;
				if(!usemap)
					hyperPathList=FormulizeByQueryWithCheck(statepog,globalquery);
				else
					hyperPathList=FormulizeByMLNMap(statepog,globalquery);
				if(hyperPathList==null||hyperPathList.size()<=0)
					continue;
				
				
				
				long tempendtime=System.currentTimeMillis();	
				//LogPrintStream.getPs_RandomWalkThread_RandomWalk().println((tempendtime-tempstarttime)+"ms\t"
						//+hyperPathList.size()+"\t"+statepog.state);
				for(int i=0;i<hyperPathList.size();i++)
				{
					HyperPath entityHp=hyperPathList.get(i);
					HyperPath conceptualHp=Conceptualize(entityHp);
					HyperEdge hyperQuery=null;
					int queryIndex=-1;
					for(int j=0;j<entityHp.path.size();j++)
					{
						HyperEdge tempEdge=entityHp.path.get(j);
						if(tempEdge.rel.equals(GlobalStaticData.query))
						{
							queryIndex=j;
							hyperQuery=tempEdge;
							break;
						}
					}
					if(hyperQuery==null)
					{
						LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println("Error: No query in the entity fomular.");
						LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print("\t");
						OutputData.PrintOneEntityHyperPath(LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData(), entityHp);
						LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println();
					}
					Map<HyperPath,TrueFalseCount> hmap=queryToCount.get(hyperQuery);
					if(hmap==null)
					{
						hmap=new TreeMap<HyperPath,TrueFalseCount>();
						queryToCount.put(hyperQuery,hmap);
					}
					TrueFalseCount tfCount=hmap.get(conceptualHp);
					if(tfCount==null)
					{
						tfCount=new TrueFalseCount();
						hmap.put(conceptualHp, tfCount);
					}
					// count the true value.
					Boolean[] truthQueryList=new Boolean[entityHp.path.size()];
					for(int j=0;j<entityHp.path.size();j++)
					{
						truthQueryList[j]=entityHp.path.get(j).value>0.5;
					}
					truthQueryList[queryIndex]=true;
					if(CheckFormulaTrueOrFalse(truthQueryList))
					{
						tfCount.trueCount+=1;
					}
					truthQueryList[queryIndex]=false;					
					if(CheckFormulaTrueOrFalse(truthQueryList))
					{
						//by wzy, change back immediately
						tfCount.falseCount+=1;
					}
					
					//print
					/*char[] charlist=new char[entityHp.path.size()];
					for(int j=0;j<truthQueryList.length;j++)
					{
						if(j==queryIndex)
							charlist[j]='q';
						else if(truthQueryList[j])
							charlist[j]='t';
						else
							charlist[j]='f';
					}

					OutputData.PrintOneEntityHyperEdge(LogPrintStream.ps_debug_testcount, hyperQuery);
					LogPrintStream.ps_debug_testcount.print("\t");
					OutputData.PrintOneConceptHyperPath(LogPrintStream.ps_debug_testcount, conceptualHp);
					LogPrintStream.ps_debug_testcount.print("\t");
					for(int j=0;j<charlist.length;j++)
					{
						LogPrintStream.ps_debug_testcount.print(charlist[j]);
					}
					LogPrintStream.ps_debug_testcount.println();*/
					

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
	/**
	 * This is a function to formulize a entitative path of hyper nodes to obtain paths of hyperedges.<br>
	 * In this function, there is 4 step (or more in the future) as follow:<br>
	 * 1. Get query List by the function FomulizeQueryFromTwoNode. If it is empty, the function can stop here.<br>
	 * 2. Create all possible true paths of hyperedges as bodies by Cartesian product for the results of running<br>
	 * FormulizeTrueEdgeFromTwoNode for each two adjacent nodes.<br>
	 * 3. Use the similar mechanism to do Cartesian product and add queries for all paths of hyperedges.<br>
	 * 4. Filter paths got by check if it satisfies the hypothesis that each of node in hyperpath must appear more than twice.<br> 
	 * Tips: It will spend some time. That would be nice if use multithreads to deal with it.
	 * @author Zhuoyu Wei
	 * @version 1.1 no query for find the loop, the query can be at the position Body or Head.
	 * @param statepog the path of nodes, and it is noted as a state in the previous process of random walk. 
	 * @param query the query for this process.
	 * @return Satisfied all hypotheses entitative hyperpath.
	 */
	public static List<HyperPath> FormulizeByQueryWithCheck(StatePOG statepog,Integer queryIndex)
	{
		List<HyperPath> resultList=new ArrayList<HyperPath>();
		if(statepog==null||statepog.state==null||statepog.state.size()<2)
			return resultList;
		//Filter for query
		if(GlobalStaticData.MaxFalseQuery<0)
			GlobalStaticData.MaxFalseQuery=5;
		List<HyperEdge> queryList=FomulizeQueryFromTwoNodeRandomly(statepog.state.get(0),
				statepog.state.get(statepog.state.size()-1),GlobalStaticData.MaxFalseQuery);
		//List<HyperEdge> queryList=FomulizeQueryFromTwoNode(statepog.state.get(0),
				//statepog.state.get(statepog.state.size()-1));
		if(queryList==null||queryList.size()<=0)
			return resultList;
		
		//Cartesian product for formulizing body path
		resultList.add(new HyperPath());
		for(int i=0;i<statepog.state.size()-1;i++)
		{
			List<HyperPath> tempPathList=resultList;
			resultList=new ArrayList<HyperPath>();
			List<HyperEdge> trueEdgeList=FormulizeTrueEdgeFromTwoNode(statepog.state.get(i),statepog.state.get(i+1));
			//debug--->debug ps log
			if(trueEdgeList==null)
			{
				for(int j=0;j<statepog.state.size();j++)
				{
					HyperNode hn=GlobalStaticData.entityToNode.get(statepog.state.get(j));
					LogPrintStream.getPs_alwaysdebug().println(hn.entity);
					for(int k=0;k<hn.neighbourList.size();k++)
					{
						LogPrintStream.getPs_alwaysdebug().print(hn.neighbourList.get(k)+"\t");
					}
					LogPrintStream.getPs_alwaysdebug().println();
				}
				LogPrintStream.getPs_alwaysdebug().println();				
				LogPrintStream.getPs_alwaysdebug().println(statepog.state.get(i)+"\t"+statepog.state.get(i+1));
			}
			for(int j=0;j<tempPathList.size();j++)
			{
				HyperPath nowPath=tempPathList.get(j);
				for(int k=0;k<trueEdgeList.size();k++)
				{
					HyperPath newPath=nowPath.copyOnePath();
					newPath.path.add(trueEdgeList.get(k));
					resultList.add(newPath);
				}
			}
		}
		//add query at the end of path.
		List<HyperPath> tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		List<HyperEdge> trueEdgeList=queryList;
		for(int j=0;j<tempPathList.size();j++)
		{
			HyperPath nowPath=tempPathList.get(j);
			for(int k=0;k<trueEdgeList.size();k++)
			{
				HyperPath newPath=nowPath.copyOnePath();
				newPath.path.add(trueEdgeList.get(k));
				resultList.add(newPath);
			}
		}	
		
		//LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
		//LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\t"+resultList.size()+"\t");
		//Filter for at least twice appearing of each entity.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckTwiceForNodes(hp))
			{
				resultList.add(hp);
			}
		}
		//LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
		
		//Filter by query, appearing once and only once.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckQuery(hp,GlobalStaticData.query))
			{
				resultList.add(hp);
			}
		}		
		//LogPrintStream.getPs_alwaysdebug().println(resultList.size()+"\t");
		
		/*//Get the postion of the query, and turn the order of the state
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			int j=0;
			for(;j<hp.path.size();j++)
			{
				if(hp.path.get(j).rel.equals(queryIndex))
				{
					break;
				}
			}
			if(j==hp.path.size()-1)
			{
				resultList.add(hp);
			}
			else if(j==hp.path.size())
			{
				continue;
			}
			else
			{
				HyperPath newhp=TurnOrderForHyperPath(hp,j);
				resultList.add(newhp);
			}
			
		}*/
		


		
		return resultList;
	}
	/**
	 * For infer on MLN Graph, It is a better method to turn state path to entity formulas.</br>
	 * It is a up to dowm method. It replace each node in path with its type and get a list of</br>
	 * formulas by mapping the type path(We have build the map from Pog_typePathNode to list<HyperPath>)</br>
	 * Then We check each formulas, keep ones whose bodies are true and head is true or false.</br>
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @date 2014.9.3
	 * @param statepog
	 * @param queryIndex
	 * @return
	 */
	public static List<HyperPath> FormulizeByMLNMap(StatePOG statepog,Integer queryIndex)
	{
		List<HyperPath> resultList=new ArrayList<HyperPath>();
		POG_typePathNode pog_typePathNode=new POG_typePathNode();
		for(int i=0;i<statepog.state.size();i++)
		{
			HyperNode hn=GlobalStaticData.entityToNode.get(statepog.state.get(i));
			pog_typePathNode.nodeList.add(hn.type);
		}
		List<HyperPath> formulaList=GlobalStaticData.map_PathToFormulaList.get(pog_typePathNode);
		if(formulaList==null)
			return resultList;
		for(int i=0;i<formulaList.size();i++)
		{
			HyperPath formula=formulaList.get(i);
			if(formula.path.size()<2)
				continue;
			List<Integer> typeList=new ArrayList<Integer>();
			for(int j=0;j<formula.path.size();j++)
			{
				HyperEdge edge1=formula.path.get((j-1+formula.path.size())%formula.path.size());
				HyperEdge edge2=formula.path.get(j);
				int same_index=-1;
				for(int k=0;k<edge1.nodeList.size();k++)
				{
					for(int h=0;h<edge2.nodeList.size();h++)
					{
						if(edge1.nodeList.get(k).equals(edge2.nodeList.get(h)))
						{
							same_index=edge1.nodeList.get(k);
							break;
						}
					}
					if(same_index>0)
						break;
				}
				if(same_index<0)
					break;
				typeList.add(same_index);
			}
			if(typeList.size()!=statepog.state.size())
				continue;
			Map<Integer,Integer> typeToEntity=new HashMap<Integer,Integer>();
			for(int j=0;j<typeList.size();j++)
			{
				typeToEntity.put(typeList.get(j), statepog.state.get(j));
			}
			
			//Replace
			HyperPath entityHp=new HyperPath();
			for(int j=0;j<formula.path.size();j++)
			{
				HyperEdge conceptHe=formula.path.get(j);
				HyperEdge entityHe=new HyperEdge();
				entityHe.rel=conceptHe.rel;
				for(int k=0;k<conceptHe.nodeList.size();k++)
				{
					int entityIndex=typeToEntity.get(conceptHe.nodeList.get(k));
					entityHe.nodeList.add(entityIndex);
				}
				entityHp.path.add(entityHe);
			}
			
			//Check
			boolean flag=true;
			for(int j=0;j<entityHp.path.size()-1;j++)
			{
				//must keep the edge has at least two nodes.
				HyperEdge he=entityHp.path.get(j);
				HyperNode hn=GlobalStaticData.entityToNode.get(he.nodeList.get(0));
				List<HyperEdge> heList=hn.edges.get(he.nodeList.get(1));
				
				boolean flagtemp=false;
				for(int k=0;k<heList.size();k++)
				{
					if(heList.get(k).equals(he))
					{
						flagtemp=true;
						break;
					}
				}
				if(!flagtemp)
				{
					flag=true;
					break;
				}
			}
			
			if(flag)
			{
				resultList.add(entityHp);
			}
			
		}
		return resultList;
	}
	/**
	 * Same as FormulizeByQueryRandomly
	 * @author Zhuoyu Wei
	 * @version 2.0
	 * @param statepog the path of nodes, and it is noted as a state in the previous process of random walk. 
	 * @param query the query for this process.
	 * @return Satisfied all hypotheses entitative hyperpath.
	 */
	public static List<HyperPath> FormulizeByQueryWithCheckRandomly(StatePOG statepog,Integer queryIndex)
	{
		List<HyperPath> resultList=new ArrayList<HyperPath>();
		if(statepog==null||statepog.state==null||statepog.state.size()<2)
			return resultList;
		//Filter for query
		if(GlobalStaticData.MaxFalseQuery<0)
			GlobalStaticData.MaxFalseQuery=5;
		List<HyperEdge> queryList=FomulizeQueryFromTwoNodeRandomly(statepog.state.get(0),
				statepog.state.get(statepog.state.size()-1),GlobalStaticData.MaxFalseQuery);
		//List<HyperEdge> queryList=FomulizeQueryFromTwoNode(statepog.state.get(0),
				//statepog.state.get(statepog.state.size()-1));
		if(queryList==null||queryList.size()<=0)
			return resultList;
		
		//Cartesian product for formulizing body path
		HyperPath onlyOnePath=new HyperPath();
		for(int i=0;i<statepog.state.size()-1;i++)
		{
			List<HyperEdge> trueEdgeList=FormulizeTrueEdgeFromTwoNode(statepog.state.get(i),statepog.state.get(i+1));
			Integer randIndex=Math.abs(rand.nextInt())%trueEdgeList.size();
			HyperEdge randEdge=trueEdgeList.get(randIndex);
			onlyOnePath.path.add(randEdge);
		}
		resultList.add(onlyOnePath);
		
		//add query at the end of path.
		List<HyperPath> tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		List<HyperEdge> trueEdgeList=queryList;
		for(int j=0;j<tempPathList.size();j++)
		{
			HyperPath nowPath=tempPathList.get(j);
			for(int k=0;k<trueEdgeList.size();k++)
			{
				HyperPath newPath=nowPath.copyOnePath();
				newPath.path.add(trueEdgeList.get(k));
				resultList.add(newPath);
			}
		}	
		
		//LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
		//LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\t"+resultList.size()+"\t");
		//Filter for at least twice appearing of each entity.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckTwiceForNodes(hp))
			{
				resultList.add(hp);
			}
		}
		//LogPrintStream.getPs_alwaysdebug().print(resultList.size()+"\t");
		
		//Filter by query, appearing once and only once.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			if(CheckQuery(hp,GlobalStaticData.query))
			{
				resultList.add(hp);
			}
		}		
		//LogPrintStream.getPs_alwaysdebug().println(resultList.size()+"\t");
		
		/*//Get the postion of the query, and turn the order of the state
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		
		for(int i=0;i<tempPathList.size();i++)
		{
			HyperPath hp=tempPathList.get(i);
			int j=0;
			for(;j<hp.path.size();j++)
			{
				if(hp.path.get(j).rel.equals(queryIndex))
				{
					break;
				}
			}
			if(j==hp.path.size()-1)
			{
				resultList.add(hp);
			}
			else if(j==hp.path.size())
			{
				continue;
			}
			else
			{
				HyperPath newhp=TurnOrderForHyperPath(hp,j);
				resultList.add(newhp);
			}
			
		}*/
		


		
		return resultList;
	}
	public static HyperPath TurnOrderForHyperPath(HyperPath source,int queryIndex)
	{
		HyperPath dispath=null;
		
		HyperEdge head=source.path.get(source.path.size()-1);
		if(head.value>0.5)
		{
			dispath=new HyperPath();
			for(int i=0;i<source.path.size();i++)
			{
				dispath.path.add(source.path.get((i+queryIndex+1)%source.path.size()));
			}
			int[] typecount=new int[GlobalStaticData.typeMapItoS.size()];
			Map<Integer,Integer> codeTocode=new HashMap<Integer,Integer>();
			
			for(int i=0;i<dispath.path.size();i++)
			{
				HyperEdge he=dispath.path.get(i);
				for(int j=0;j<he.nodeList.size();j++)
				{
					Integer code=he.nodeList.get(j);
					
					Integer newcode=codeTocode.get(code);
					if(newcode==null)
					{
						Integer type=code%GlobalStaticData.typeMapItoS.size();
						Integer index=code/GlobalStaticData.typeMapItoS.size();
					
						index=typecount[type]++;
						newcode=type+index*GlobalStaticData.typeMapItoS.size();
						codeTocode.put(code, newcode);
					}
					
					
					
					he.nodeList.set(j, newcode);
					
				}
				
			}
		}
		else
		{
			dispath=source;
		}
		
		
		return dispath;
		
	}
	
	
	public static void FilterDataPoint(Double[] weights,List<DataPoint> dpList)
	{
		
		for(int i=0;i<dpList.size();i++)
		{
			DataPoint dp=dpList.get(i);
			List<Integer> tempnodeList=dp.nodeList;
			List<TrueFalseCount> tempcountList=dp.countList;
			
			dp.nodeList=new ArrayList<Integer>();
			dp.countList=new ArrayList<TrueFalseCount>();
			
			for(int j=0;j<tempnodeList.size();j++)
			{
				if(Math.abs(weights[tempnodeList.get(j)])<1e-6)
					continue;
				dp.nodeList.add(tempnodeList.get(j));
				dp.countList.add(tempcountList.get(j));
				
			}
			
		}
	}
	

	
}

package wzy.infer;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import wzy.func.MLNProcess;
import wzy.func.OutputData;
import wzy.model.*;
import wzy.stat.GlobalStaticData;

public class K_PathGraph {

	public K_PathNode queryNode;  //queryNode
	public Integer queryId;
	
	public Map<K_PathNode,K_PathNode> nodeMap=new HashMap<K_PathNode,K_PathNode>();
	
	/**
	 * @deprecated
	 * @param clause
	 * @param queryIndex
	 */
	public void AddNodesToGraph(HyperPath clause, int queryIndex)
	{
		for(int i=0;i<=queryIndex;i++)
		{
			for(int j=i+1;j<clause.path.size();j++)
			{
				if(queryIndex>=i&&queryIndex<=j)
				{
					HyperPath newPath=new HyperPath();
					for(int k=i;k<=j;k++)
					{
						newPath.path.add(clause.path.get(k));
					}
					HyperPath conceptualPath=MLNProcess.Conceptualize(newPath);
					K_PathNode newKnode=new K_PathNode();
					newKnode.path=conceptualPath;
					K_PathNode addedNode=nodeMap.get(newKnode);
					if(addedNode==null)
					{
						nodeMap.put(newKnode, newKnode);
					}
				}
			}
		}
	}
	
	
	public K_PathNode GetKnodeFromPair(HyperPath clause,IndexPair pair)
	{
		K_PathNode knode=new K_PathNode();
		HyperPath tempPath=new HyperPath();
		for(int i=pair.x;i<=pair.y;i++)
		{
			tempPath.path.add(clause.path.get(i));
		}
		knode.path=MLNProcess.Conceptualize(tempPath);
		K_PathNode map_knode=nodeMap.get(knode);
		if(map_knode==null)
		{
			map_knode=knode;
			nodeMap.put(map_knode, map_knode);
		}
		return map_knode;
	}
	public void AddNodesToGraphBFS(HyperPath clause, int queryIndex)
	{
		Queue<IndexPair> queue=new LinkedList<IndexPair>();
		IndexPair startPair=new IndexPair();
		startPair.x=0;
		startPair.y=clause.path.size()-1;
		queue.add(startPair);
		Set<IndexPair> pairSet=new HashSet<IndexPair>();
		while(!queue.isEmpty())
		{
			IndexPair pair=queue.poll();
			if(pairSet.contains(pair))
				continue;
			
			K_PathNode knode=GetKnodeFromPair(clause,pair);
			
			pairSet.add(pair);
			
			
			if(pair.x<queryIndex&&pair.y-pair.x>0)
			{
				IndexPair newPair=new IndexPair();
				newPair.x=pair.x+1;
				newPair.y=pair.y;
				
				K_PathNode subnode=GetKnodeFromPair(clause,newPair);
				K_PathEdge kedge=new K_PathEdge();
				//kedge.addedEdge=clause.path.get(pair.x).copyHyperEdge();
				
				int superleft=knode.path.LeftestPosition();
				int inverseleft=superleft;
				if(superleft==0)
					inverseleft=1;
				else
					inverseleft=0;
				
				int subindex=subnode.path.LeftestPosition();
				
				//kedge.addedEdge.nodeList.set(inverseleft, subnode.path.path.get(0).nodeList.get(subindex));
				//kedge.addedEdge.nodeList.set(superleft, GlobalStaticData.entityToNode.size()-1); // set anther node as the most entity id
			
				kedge.relOfAddedEdge=clause.path.get(pair.x).rel;
				kedge.isLeftAdded=subindex==0;
				kedge.nodeposition=inverseleft;
				kedge.newNode=knode;
				
				subnode.superEdgeListleft.add(kedge);
				
				queue.add(newPair);
				
			}
			if(pair.y>queryIndex&&pair.y-pair.x>0)
			{
				IndexPair newPair=new IndexPair();
				newPair.x=pair.x;
				newPair.y=pair.y-1;
				
				K_PathNode subnode=GetKnodeFromPair(clause,newPair);
				K_PathEdge kedge=new K_PathEdge();
				//kedge.addedEdge=clause.path.get(pair.x).copyHyperEdge();
				
				int superright=knode.path.RightestPostition();
				int inverseright=superright;
				if(superright==0)
					inverseright=1;
				else
					inverseright=0;
				
				int subindex=subnode.path.RightestPostition();
				
				//kedge.addedEdge.nodeList.set(inverseleft, subnode.path.path.get(0).nodeList.get(subindex));
				//kedge.addedEdge.nodeList.set(superleft, GlobalStaticData.entityToNode.size()-1); // set anther node as the most entity id
			
				kedge.relOfAddedEdge=clause.path.get(pair.y).rel;
				kedge.isLeftAdded=subindex==0;
				kedge.nodeposition=inverseright;
				kedge.newNode=knode;
				
				subnode.superEdgeListright.add(kedge);
				
				queue.add(newPair);
			}
		}
		
	}
	/**
	 * @version 2.0
	 * @param clause
	 * @param queryIndex
	 */
	public void AddNodesToGraph(HyperPath clause)
	{
		int l=clause.path.size()-1;
		K_PathNode klast=null;
		for(int i=0;i<l;i++)
		{
			for(int j=0;j<l-i;j++)
			{
				K_PathNode knode=new K_PathNode();
				for(int k=j;k<=j+i;k++)
				{
					knode.path.path.add(clause.path.get(k));
				}
				knode.path=MLNProcess.Conceptualize(knode.path);
				if(nodeMap.get(knode)==null)
				{
					nodeMap.put(knode, knode);
				}
				else
				{
					knode=nodeMap.get(knode);
				}
				if(i>0)
				{
					K_PathNode kleft=knode.RemoveAhead();
					kleft.path=MLNProcess.Conceptualize(kleft.path);
					if(nodeMap.get(kleft)==null)
					{
						nodeMap.put(kleft, kleft);
					}
					else
					{
						kleft=nodeMap.get(kleft);
					}
					int[] poss=knode.path.LeftTwoPosition();
					K_PathEdge kleftedge=new K_PathEdge();
					kleftedge.relOfAddedEdge=knode.path.path.get(0).rel;
					kleftedge.isLeftAdded=poss[1]==0;
					int inverseleft=poss[0];
					if(poss[0]==0)
						inverseleft=1;
					else
						inverseleft=0;
					kleftedge.nodeposition=inverseleft;
					kleftedge.newNode=knode;
					kleft.superEdgeListleft.add(kleftedge);
							
					K_PathNode kright=knode.RemoveBack();
					kright.path=MLNProcess.Conceptualize(kright.path);
					if(nodeMap.get(kright)==null)
					{
						nodeMap.put(kright, kright);
					}
					else
					{
						kright=nodeMap.get(kright);
					}
					poss=knode.path.RightTwoPosition();
					K_PathEdge krightedge=new K_PathEdge();
					krightedge.relOfAddedEdge=knode.path.path.get(knode.path.path.size()-1).rel;
					krightedge.isLeftAdded=poss[1]==0;
					int inverseright=poss[0];
					if(poss[0]==0)
						inverseright=1;
					else
						inverseright=0;
					krightedge.nodeposition=inverseright;
					krightedge.newNode=knode;
					kright.superEdgeListright.add(krightedge);					
				}
				klast=knode;
				
			}
		}
		
		K_PathNode kparent=new K_PathNode();
		kparent.path=clause;
		kparent=nodeMap.get(kparent);
		
		klast.finalEdge=new K_PathEdge();
		klast.finalEdge.relOfAddedEdge=kparent.path.path.get(kparent.path.path.size()-1).rel;
		klast.finalEdge.freenodeposition=new int[2];
		klast.finalEdge.freenodeposition[0]=klast.path.LeftestPosition();
		klast.finalEdge.freenodeposition[1]=klast.path.RightestPostition();	
		//klast.finalEdge.newNode=kparent;
		//klast.finalEdge.newKnodeSet=new HashSet<K_PathNode>();
		klast.finalEdge.newKnodeSet.add(kparent);
		
	}
	
	public void AddAllFormulas(List<HyperPath> formulaList)
	{
		for(int i=0;i<formulaList.size();i++)
		{
			K_PathNode knode=new K_PathNode();
			knode.isClause=true;
			knode.path=formulaList.get(i);
			nodeMap.put(knode, knode);
		}
	}
	
	public void BuildGraph(List<HyperPath> formulaList)
	{
		AddAllFormulas(formulaList);
		//Build a virtual query
		HyperEdge queryFact=new HyperEdge();
		queryFact.rel=queryId;
		queryFact.nodeList.add(0);
		queryFact.nodeList.add(1);
		HyperPath queryPath=new HyperPath();
		queryPath.path.add(queryFact);
		HyperPath conceptual_queryPath=MLNProcess.Conceptualize(queryPath);
		
		queryNode=new K_PathNode();
		queryNode.path=conceptual_queryPath;
		K_PathNode tempNode=nodeMap.get(queryNode);
		if(tempNode==null)
		{
			nodeMap.put(queryNode, queryNode);
		}
		else
		{
			queryNode=tempNode;
		}
		//nodeMap.put(queryNode, queryNode);
		
		
		for(int i=0;i<formulaList.size();i++)
		{
			HyperPath clause=formulaList.get(i);
			for(int j=0;j<clause.path.size();j++)
			{
				if(clause.path.get(j).rel.equals(queryId))
				{
					AddNodesToGraphBFS(clause, j);
				}
			}
		}
	}
	
	public void BuildAllGraph(List<HyperPath> formulaList)
	{
		AddAllFormulas(formulaList);
		
		for(int i=0;i<formulaList.size();i++)
		{
			if(formulaList.get(i).path.size()<2)
				continue;
			AddNodesToGraph(formulaList.get(i));
		}
	}
	
	
	///for Debug///
	public void DebugPrintOneKnode(K_PathNode knode,PrintStream ps)
	{
		//System.out.println(knode.path.path.size());
		for(int i=0;i<knode.path.path.size();i++)
		{
			OutputData.PrintOneConceptHyperEdge(ps, knode.path.path.get(i));
			ps.print(" ");
		}
		ps.println();
		ps.println(knode.superEdgeListleft.size()+"\t"+knode.superEdgeListright.size());
		List<K_PathEdge> edgeList=new ArrayList<K_PathEdge>();
		edgeList.addAll(knode.superEdgeListleft);
		edgeList.addAll(knode.superEdgeListright);
		for(int i=0;i<edgeList.size();i++)
		{
			K_PathEdge pe=edgeList.get(i);
			ps.print("\t*: ("+pe.relOfAddedEdge+","+pe.nodeposition+")\t");
			for(int j=0;j<pe.newNode.path.path.size();j++)
			{
				OutputData.PrintOneConceptHyperEdge(ps, pe.newNode.path.path.get(j));
				ps.print(" ");
			}
			ps.println();
		}
	
	}
	public void DebugBFSGraph(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		Queue<K_PathNode> queue=new LinkedList<K_PathNode>();
		queue.add(queryNode);
		
		while(!queue.isEmpty())
		{
			K_PathNode knode=queue.poll();
			if(knode.isVisited)
				continue;
			knode.isVisited=true;
			DebugPrintOneKnode(knode,ps);
			List<K_PathEdge> edgeList=new ArrayList<K_PathEdge>();
			edgeList.addAll(knode.superEdgeListleft);
			edgeList.addAll(knode.superEdgeListright);
			for(int i=0;i<edgeList.size();i++)
			{
				queue.add(edgeList.get(i).newNode);
			}
		}
		ps.close();
	}
	
	public void DebugBFSGraph2(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		
		Iterator it=nodeMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			
			Queue<K_PathNode> queue=new LinkedList<K_PathNode>();
			queue.add((K_PathNode)entry.getKey());
		
			while(!queue.isEmpty())
			{
				K_PathNode knode=queue.poll();
				if(knode.isVisited)
					continue;
				knode.isVisited=true;
				DebugPrintOneKnode(knode,ps);
				if(knode.finalEdge!=null)
				{
					ps.print("\tFinal Clause: ");
					//OutputData.PrintOneConceptHyperPath(ps,knode.finalEdge.newNode.path);
					if(knode.finalEdge.newKnodeList==null)
					{
						knode.finalEdge.newKnodeList=new ArrayList<K_PathNode>();
						knode.finalEdge.newKnodeList.addAll(knode.finalEdge.newKnodeSet);
					}
					for(int i=0;i<knode.finalEdge.newKnodeList.size();i++)
					{
						OutputData.PrintOneConceptHyperPath(ps,knode.finalEdge.newKnodeList.get(i).path);
					}
					ps.println();	
				}
				ps.println("==================================");
				ps.println("==================================");	
				List<K_PathEdge> edgeList=new ArrayList<K_PathEdge>();
				edgeList.addAll(knode.superEdgeListleft);
				edgeList.addAll(knode.superEdgeListright);
				for(int i=0;i<edgeList.size();i++)
				{
					queue.add(edgeList.get(i).newNode);
				}
			}
		}
		
		ps.close();
	}
}

class IndexPair
{
	int x;
	int y;
	
	public int hashCode()
	{
		return x*31+y;
	}
	
	public boolean equals(Object o)
	{
		IndexPair i=(IndexPair)o;
		return x==i.x&&y==i.y;
	}
}


package exper.cora;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class SampleGraph {

	
	public Random rand=new Random();
	
	public void ReadDB(String filename,CoraGraph coraGraph) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			CoraEdge ce=new CoraEdge();
			String[] ss=buffer.split("[()]+");
			ce.rel=ss[0];
			String[] tokens=ss[1].split(",");
			for(int i=0;i<tokens.length;i++)
			{
				ce.entityList.add(tokens[i]);
			}
			ce.index=coraGraph.edgeList.size();
			coraGraph.edgeList.add(ce);
		}
		br.close();
		System.out.println("Total Edges:\t"+coraGraph.edgeList.size());
	}
	
	public void IndexNodes(CoraGraph coraGraph)
	{	
		for(int i=0;i<coraGraph.edgeList.size();i++)
		{
			CoraEdge ce=coraGraph.edgeList.get(i);
			
			for(int j=0;j<ce.entityList.size();j++)
			{
				String entity=ce.entityList.get(j);
				CoraNode cn=coraGraph.nodeMap.get(entity);
				if(cn==null)
				{
					cn=new CoraNode();
					cn.entity=entity;
					cn.index=coraGraph.nodeList.size();
					coraGraph.nodeList.add(cn);
					coraGraph.nodeMap.put(entity, cn);
				}
				cn.edgeList.add(ce);
			}
		}
		System.out.println("Total Nodes:\t"+coraGraph.nodeList.size());
	}
	
	public void BuildGraph(CoraGraph coraGraph)
	{
		for(int i=0;i<coraGraph.nodeList.size();i++)
		{
			CoraNode cn=coraGraph.nodeList.get(i);
			for(int j=0;j<cn.edgeList.size();j++)
			{
				CoraEdge ce=cn.edgeList.get(j);
				for(int k=0;k<ce.entityList.size();k++)
				{
					if(cn.entity.equals(ce.entityList.get(k)))
						continue;
					cn.neighboorSet.add(coraGraph.nodeMap.get(ce.entityList.get(k)).index);
				}
			}
			cn.neighboorList=cn.neighboorSet.toArray(new Integer[0]);
		}
	}
	
	
	public Integer[] SampleEntity(CoraGraph coraGraph,int entityNum,int seed)
	{
		Set<Integer> resultSet=new HashSet<Integer>();
		
		while(resultSet.size()<entityNum)
		{
			resultSet.add(seed);
			CoraNode cn=coraGraph.nodeList.get(seed);
			int index=Math.abs(rand.nextInt())%cn.neighboorList.length;
			
			seed=cn.neighboorList[index];
			//System.out.println(seed);
		}
		
		return resultSet.toArray(new Integer[0]);
	}
	
	public Integer[] GetAllEdges(CoraGraph coraGraph,Integer[] seeds)
	{
		Set<Integer> edgeSet=new HashSet<Integer>();
		for(int i=0;i<seeds.length;i++)
		{
			CoraNode cn=coraGraph.nodeList.get(seeds[i]);
			for(int j=0;j<cn.edgeList.size();j++)
			{
/*				if(cn.edgeList.get(j).index<0)
				{
					System.out.println(cn.edgeList.get(j).rel+"("+cn.edgeList.get(j)
							.entityList.get(0)+","+cn.edgeList.get(j).entityList.get(1)+")\t"
							+cn.edgeList.get(j).index);
				}*/
				edgeSet.add(cn.edgeList.get(j).index);
			}
		}
		return edgeSet.toArray(new Integer[0]);
	}
	
	public void PrintDB(PrintStream ps,Integer[] edges,CoraGraph coraGraph)
	{
		for(int i=0;i<edges.length;i++)
		{
			//System.out.println(i+"\t"+edges[i]+"\t"+coraGraph.edgeList.size());
			CoraEdge ce=coraGraph.edgeList.get(edges[i]);
			ps.print(ce.rel+"(");
			for(int j=0;j<ce.entityList.size()-1;j++)
			{
				ps.print(ce.entityList.get(j)+",");
			}
			ps.println(ce.entityList.get(ce.entityList.size()-1)+")");
		}
	}
	
	

	public static void SampleOneTime(int seedNum) throws IOException
	{
		SampleGraph sg=new SampleGraph();
		CoraGraph coraGraph=new CoraGraph();
		sg.ReadDB("C:\\Users\\Administrator\\Desktop\\infer experiments\\cora_data\\" +
				"cora.er_6.11.no\\Whole.db", coraGraph);
		sg.IndexNodes(coraGraph);
		sg.BuildGraph(coraGraph);
		Integer[] seeds=sg.SampleEntity(coraGraph, seedNum, Math.abs(sg.rand.nextInt())%coraGraph.nodeList.size());
		Integer[] edges=sg.GetAllEdges(coraGraph, seeds);
		PrintStream ps=new PrintStream("C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub\\sample"
				+seedNum+".db");
		sg.PrintDB(ps, edges, coraGraph);	
	}
	
	public static void main(String[] args) throws IOException
	{
		for(int i=100;i<2497;i+=100)
		{
			SampleOneTime(i);
		}
	}
	
}

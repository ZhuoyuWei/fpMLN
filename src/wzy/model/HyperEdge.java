package wzy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a data structure of Edge in HyperGraph. It has 1 to n nodes and they form a sequence.<br>
 * It has own type as Edge type.<br>
 * @author Zhuoyu Wei
 * @version 1.0
 * Field:<br>
 * nodeList, List<Integer>, the sequence of nodes' id. <br>
 * rel, Integer, Edge Type, called relation. <br>
 * value, Double, the weight of the edge, the default value is 1.0. <br>
 * @implements Comparable for TreeMap
 * @override equals for HashMap
 * @override hashcode for HashMap
 */

public class HyperEdge implements Comparable{
	public List<Integer> nodeList=new ArrayList<Integer>();   //the sequence of nodes' id.
	public Integer rel;    //Edge Type, called relation.
	public Double value=1.0;      //the weight of the edge, the default value is 1.0.
	@Override
	public int compareTo(Object o) {
		// TODO Auto-generated method stub
		HyperEdge he=(HyperEdge)o;
		if(rel<he.rel)
			return -1;
		else if(rel>he.rel)
			return 1;
		else
		{
			int minIndex=Math.min(nodeList.size(), he.nodeList.size());
			for(int i=0;i<minIndex;i++)
			{
				if(nodeList.get(i)<he.nodeList.get(i))
					return -1;
				else if(nodeList.get(i)>he.nodeList.get(i))
					return 1;
			}
			return nodeList.size()-he.nodeList.size();
		}
	}
	public boolean equals(Object o)
	{
		return compareTo(o)==0;
	}
	public int hashCode()
	{
		int code=rel;
		for(int i=0;i<nodeList.size();i++)
		{
			code*=31;
			code+=nodeList.get(i);
		}
		return code;
	}
	
	public HyperEdge copyHyperEdge()
	{
		HyperEdge he=new HyperEdge();
		he.rel=rel;
		he.nodeList.addAll(nodeList);
		he.value=value;
		return he;
	}
}

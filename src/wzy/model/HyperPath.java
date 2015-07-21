package wzy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * It is a sequence of HyperEdges. It can be used to record the entitative or conceptual formulas.
 * @author Zhuoyu Wei
 * @version 1.0
 * @implements Comparable for TreeMap
 */
public class HyperPath implements Comparable {
	public List<HyperEdge> path=new ArrayList<HyperEdge>();

	@Override
	public int compareTo(Object o) {
		HyperPath hp=(HyperPath)o;
		int minIndex=Math.min(path.size(), hp.path.size());
		for(int i=0;i<minIndex;i++)
		{
			int edgeResult=path.get(i).compareTo(hp.path.get(i));
			if(edgeResult!=0)
				return edgeResult;
		}
		return path.size()-hp.path.size();
	}
	
	public HyperPath copyOnePath()
	{
		HyperPath hp=new HyperPath();
		hp.path.addAll(path);
		return hp;
	}
	
	public void PollMin()
	{
		int minindex=0;
		for(int i=1;i<this.path.size()-1;i++)
		{
			if(this.path.get(i).compareTo(this.path.get(minindex))<0)
			{
				minindex=i;
			}
		}
		
		List<HyperEdge> newList=new ArrayList<HyperEdge>();
		
		for(int i=0;i<this.path.size()-1;i++)
		{
			int index=(minindex+i)%(this.path.size()-1);
			newList.add(this.path.get(index));
		}
		newList.add(this.path.get(this.path.size()-1));
		this.path=newList;
		
	}
	
	public boolean equals(Object o)
	{
		
		return this.compareTo(o)==0;
		
	}
	
	public int hashCode()
	{
		int sum=0;
		for(int i=0;i<path.size();i++)
		{
			sum*=31;
			sum+=path.get(i).hashCode();
		}
		return sum;
	}
	
	//For POG building by wzy in 2014.12.15,just for triplets.
	public int LeftestPosition()
	{
		if(path.size()==1)
			return 0;
		if(path.get(0).nodeList.get(0).equals(path.get(1).nodeList.get(0))
				||path.get(0).nodeList.get(0).equals(path.get(1).nodeList.get(1)))
				return 1;
		else
			return 0;
		
	}
	public int RightestPostition()
	{
		if(path.size()==1)
			return 1;
		int l=path.size();
		if(path.get(l-1).nodeList.get(0).equals(path.get(l-2).nodeList.get(0))
				||path.get(l-1).nodeList.get(0).equals(path.get(l-2).nodeList.get(1)))
			return 1;
		else
			return 0;
	}
	
	public int[] LeftTwoPosition()
	{
		int[] result=new int[2];
		
		if(path.get(0).nodeList.get(0).equals(path.get(1).nodeList.get(0)))
		{
			result[0]=1;
			result[1]=0;
		}
		else if(path.get(0).nodeList.get(0).equals(path.get(1).nodeList.get(1)))
		{
			result[0]=1;
			result[1]=1;			
		}
		else if(path.get(0).nodeList.get(1).equals(path.get(1).nodeList.get(0)))
		{
			result[0]=0;
			result[1]=0;
		}	
		else
		{
			result[0]=0;
			result[1]=1;			
		}
		return result;
	}
	
	public int[] RightTwoPosition()
	{
		int[] result=new int[2];
		
		if(path.get(path.size()-1).nodeList.get(0).equals(path.get(path.size()-2).nodeList.get(0)))
		{
			result[0]=1;
			result[1]=0;
		}
		else if(path.get(path.size()-1).nodeList.get(0).equals(path.get(path.size()-2).nodeList.get(1)))
		{
			result[0]=1;
			result[1]=1;			
		}
		else if(path.get(path.size()-1).nodeList.get(1).equals(path.get(path.size()-2).nodeList.get(0)))
		{
			result[0]=0;
			result[1]=0;
		}	
		else
		{
			result[0]=0;
			result[1]=1;			
		}
		
		return result;
	}
	
}

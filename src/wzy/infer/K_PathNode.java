package wzy.infer;

import java.util.ArrayList;
import java.util.List;

import wzy.func.MLNProcess;
import wzy.model.*;

public class K_PathNode {

	public HyperPath path=new HyperPath();
	public boolean isClause=false;
	//public Integer leftestNode;
	//public Integer rightestNode;
	public K_PathEdge finalEdge=null;
	public List<K_PathEdge> superEdgeListleft=new ArrayList<K_PathEdge>();
	public List<K_PathEdge> superEdgeListright=new ArrayList<K_PathEdge>();	
	
	//for debug, by wzy, 2014.12.16
	public boolean isVisited=false;
	
	////////////Operators///////////
	public K_PathNode RemoveAhead()
	{
		K_PathNode subNode=new K_PathNode();
		for(int i=1;i<path.path.size();i++)
		{
			subNode.path.path.add(path.path.get(i));
		}
		subNode.path=MLNProcess.Conceptualize(subNode.path);
		return subNode;
	}
	
	public K_PathNode RemoveBack()
	{
		K_PathNode subNode=new K_PathNode();
		for(int i=0;i<path.path.size()-1;i++)
		{
			subNode.path.path.add(path.path.get(i));
		}
		subNode.path=MLNProcess.Conceptualize(subNode.path);
		return subNode;		
	}
	
	public K_PathNode PushAhead(HyperEdge edge)
	{
		K_PathNode superNode=new K_PathNode();
		superNode.path.path.add(edge);
		superNode.path.path.addAll(path.path);
		return superNode;
	}
	
	public K_PathNode PushBack(HyperEdge edge)
	{
		K_PathNode superNode=new K_PathNode();
		superNode.path.path.addAll(path.path);
		superNode.path.path.add(edge);
		return superNode;		
	}
	
	
	
	public K_PathNode CopyKnode()
	{
		K_PathNode knode=new K_PathNode();
		knode.path=path.copyOnePath();
		return knode;
	}
	
	public int hashCode()
	{
		return path.hashCode();
	}
	public boolean equals(Object o)
	{
		K_PathNode k=(K_PathNode)o;
		return k.path.equals(path);
	}
	
	
	
}

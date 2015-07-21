package wzy.infer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * I need build a POG graph, whose node is a conceptual path as the same as the the node in POG of learning process.<\br>
 * This is the Node class. It contains a list of types, and the whole should be looked as a state(or a node) in POG.<\br>
 * It need hashCode and equals for hashmap.
 * @author Zhuoyu Wei
 *
 */
public class POG_typePathNode{
	
	public List<Integer> nodeList=new ArrayList<Integer>();
	
	//List<POG_typePathNode> superNeighbourList=new ArrayList<POG_typePathNode>();
	//List<POG_typePathNode> subNeighourList=new ArrayList<POG_typePathNode>();
	
	public List<POG_typePathEdge> superEdgeList=new ArrayList<POG_typePathEdge>();
	public Set<POG_typePathEdge> superEdgeSet=new HashSet<POG_typePathEdge>();


	public boolean equals(Object o)
	{
		POG_typePathNode pog=(POG_typePathNode)o;
		
		if(this.nodeList.size()!=pog.nodeList.size())
			return false;
		
		boolean flag=true;
		for(int i=0;i<pog.nodeList.size();i++)
		{
			if(pog.nodeList.get(i)!=this.nodeList.get(i))
			{
				flag=false;
				break;
			}
		}
		
		return flag;
		
	}
	
	public int hashCode()
	{
		int sum=0;
		for(int i=0;i<this.nodeList.size();i++)
		{
			sum*=31;
			sum+=this.nodeList.get(i);
		}
		return sum;
	}
	
	
}

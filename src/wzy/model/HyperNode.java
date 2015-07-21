package wzy.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import wzy.stat.GlobalStaticData;

/**
 * This is the data structure of Node in HyperGraph. It has own id noted as entity and own type. It has a edge set, <br>
 * which records neighbourhood directly and implicit hyper edges. <br>
 * @author Zhuoyu Wei
 * @version 1.0
 * Field:<br>
 * entity, Integer, the entity id. <br>
 * type, Integer, the node's type. <br>
 * edges, Map<Integer,List<HyperEdge>>, it is a map from neighbour nodes to a list of HyperEdge containing the neighbour and itself.<br>  
 * neighbourList, List<Integer>,  record nodes adjust to itself
 */


public class HyperNode {
	public Integer entity;  //the entity id.
	public Integer type;    //the node's type
	public Map<Integer,List<HyperEdge>> edges=new TreeMap<Integer,List<HyperEdge>>();    //it is a map from neighbour nodes to a list of HyperEdge containing the neighbour and itself.
	public List<Integer> neighbourList; //record nodes adjust to itself
	
	public Map<Integer,List<Integer>> typeToNextNodeList=new TreeMap<Integer,List<Integer>>();
	
	public Map<Integer,List<HyperEdge>> relToEdgeList=new HashMap<Integer,List<HyperEdge>>();  //it is a map from relation to a list of hyperedges with the relation.
	
	public void BuildTypeMapItself()
	{
		for(int i=0;i<neighbourList.size();i++)
		{
			HyperNode hn=GlobalStaticData.entityToNode.get(neighbourList.get(i));
			if(hn==null)
				continue;
			List<Integer> typeNodeList=typeToNextNodeList.get(hn.type);
			if(typeNodeList==null)
			{
				typeNodeList=new ArrayList<Integer>();
				typeToNextNodeList.put(hn.type, typeNodeList);
			}
			typeNodeList.add(hn.entity);
		}
	}
}

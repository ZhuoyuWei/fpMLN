package wzy.infer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import wzy.model.HyperEdge;


public class K_PathEdge {

	public Integer relOfAddedEdge;
	public boolean isLeftAdded;
	public int nodeposition;
	public K_PathNode newNode;	
	
	
	public int[] freenodeposition=null;
	public Set<K_PathNode> newKnodeSet=new HashSet<K_PathNode>();
	public List<K_PathNode> newKnodeList;
}

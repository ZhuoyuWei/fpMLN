package exper.cora;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CoraNode {

	int index;
	String entity;
	
	List<CoraEdge> edgeList=new ArrayList<CoraEdge>();
	Set<Integer> neighboorSet=new HashSet<Integer>();
	Integer[] neighboorList;

}

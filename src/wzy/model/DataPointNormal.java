package wzy.model;

import java.util.ArrayList;
import java.util.List;

public class DataPointNormal {
	public HyperEdge query;
	public List<Integer> nodeList=new ArrayList<Integer>();  //List<HyperPath>
	public List<TrueFalseNormal> countList=new ArrayList<TrueFalseNormal>();
}

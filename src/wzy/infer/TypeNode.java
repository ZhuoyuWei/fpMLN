package wzy.infer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeNode {

	public Integer typeIndex;
	public List<Integer> nextType=new ArrayList<Integer>();
	public List<Double> edgeValue=new ArrayList<Double>();
	public Map<Integer,Double> nextToValue=new HashMap<Integer,Double>();
}

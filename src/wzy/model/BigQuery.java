package wzy.model;
import java.util.*;

public class BigQuery {

	public Integer entity;
	public Integer rel;
	public List<Integer> entityList=new ArrayList<Integer>();
	public List<Double> debugScore=new ArrayList<Double>();
	public boolean isLeft;
	public List<HyperEdge> queryList;
	
	
	public void BuildQueryList()
	{
		queryList=new ArrayList<HyperEdge>();
		for(int i=0;i<entityList.size();i++)
		{
			HyperEdge query=new HyperEdge();
			if(isLeft)
				query.nodeList.add(entity);
			query.nodeList.add(entityList.get(i));
			if(!isLeft)
				query.nodeList.add(entity);
			query.rel=rel;
			query.value=1.0;
			queryList.add(query);
		}
	}
	
}

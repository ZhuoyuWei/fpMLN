package wzy.func;

import java.util.ArrayList;
import java.util.List;

import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.stat.GlobalStaticData;

public class GeneFalseQuery {

	
	/**
	 * Add false query to the nodes' neighbour index.
	 * @param queryindex
	 */
	public static void GeneFalseQueryForTrain(Integer queryindex)
	{
		//Integer 
		HyperEdge queryEdge=GlobalStaticData.conceptRelList.get(queryindex);
		
		
		Integer[] typeList=new Integer[queryEdge.nodeList.size()];
		List<Integer>[] entityLList=new ArrayList[queryEdge.nodeList.size()];
		for(int i=0;i<queryEdge.nodeList.size();i++)
		{
			typeList[i]=queryEdge.nodeList.get(i);
			//System.out.println(typeList[i]);
			entityLList[i]=GlobalStaticData.typeToEntityList.get(typeList[i]);
		}
		
		
		//ergodic
		List<HyperEdge> falseQueryList=new ArrayList<HyperEdge>();
		HyperEdge firstfalsequery=new HyperEdge();
		firstfalsequery.rel=queryindex;
		falseQueryList.add(firstfalsequery);
		
		for(int i=0;i<queryEdge.nodeList.size();i++)
		{
			List<HyperEdge> tempqueryList=falseQueryList;
			falseQueryList=new ArrayList<HyperEdge>();
			List<Integer> entityList=entityLList[i];
			for(int j=0;j<tempqueryList.size();j++)
			{
				HyperEdge tempquery=tempqueryList.get(j);
				for(int k=0;k<entityList.size();k++)
				{
					HyperEdge newquery=tempquery.copyHyperEdge();
					newquery.nodeList.add(entityList.get(k));
					falseQueryList.add(newquery);
				}
			}
		}
		
		//Filter true queries
		for(int k=0;k<falseQueryList.size();k++)
		{
			HyperEdge query=falseQueryList.get(k);
			if(GlobalStaticData.trainQuerySet.contains(query))
				continue;
			query.value=0.;
			for(int i=0;i<query.nodeList.size();i++)
			{
				Integer entity=query.nodeList.get(i);
				HyperNode hn=GlobalStaticData.entityToNode.get(entity);
				for(int j=0;j<query.nodeList.size();j++)
				{
					if(i==j)
						continue;
					Integer neighbour=query.nodeList.get(j);
					List<HyperEdge> heList=hn.edges.get(neighbour);
					if(heList==null)
					{
						heList=new ArrayList<HyperEdge>();
						hn.edges.put(neighbour, heList);
					}
					heList.add(query);
				}
			}
		}
	}
	
	
	public static List<Integer> GeneTrainQuerySeeds(Integer queryindex)
	{
		List<Integer> seedList=new ArrayList<Integer>();
		
		HyperEdge queryEdge=GlobalStaticData.conceptRelList.get(queryindex);
		
		

		
		for(int i=0;i<queryEdge.nodeList.size();i++)
		{
			Integer typeindex=queryEdge.nodeList.get(i);
		
			List<Integer> nodeList=GlobalStaticData.typeToEntityList.get(typeindex);
			seedList.addAll(nodeList);
		}
		
	
		
		return seedList;
	}
	
}

package wzy.func;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import wzy.model.AIntSet;
import wzy.model.DataPoint;
import wzy.model.DataPointNormal;
import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.model.TrueFalseCount;
import wzy.model.TrueFalseNormal;
import wzy.stat.GlobalStaticData;

public class InferenceTestTool {
	
	public static Random rand=new Random();
	
	/**
	 * Add the false query for test process via building and adding false hyperedges with the query as rel randomly.
	 * @author Zhuoyu Wei
	 * @verison 1.0
	 * @param falseRate the proportion of falseQuery in all queries. 
	 */
	public static void ProduceFalseQuery(double falseRate)
	{
		int maxSum=GlobalStaticData.entitativeTestQueryList.size()*GlobalStaticData.entitativeTestQueryList.size();
		int sum=(int)((falseRate)*GlobalStaticData.entitativeTestQueryList.size());
		System.out.println("***\t"+sum);
		HyperEdge queryEdge=GlobalStaticData.conceptRelList.get(GlobalStaticData.query);
		Integer[] typeList=new Integer[queryEdge.nodeList.size()];
		List<Integer>[] entityLList=new ArrayList[queryEdge.nodeList.size()];
		for(int i=0;i<queryEdge.nodeList.size();i++)
		{
			typeList[i]=queryEdge.nodeList.get(i);
			//System.out.println(typeList[i]);
			entityLList[i]=GlobalStaticData.typeToEntityList.get(typeList[i]);
		}
		
		List<HyperEdge> falseQueryList=new ArrayList<HyperEdge>();
		for(int i=0;i<maxSum;i++)
		{
			
			HyperEdge falseQuery=new HyperEdge();
			falseQuery.rel=GlobalStaticData.query;
			falseQuery.value=0.;
			for(int j=0;j<entityLList.length;j++)
			{
				int randIndex=Math.abs(rand.nextInt()%entityLList[j].size());
				int k;
				//System.out.println("*\t"+randIndex+"\t"+entityLList[j].size());
				for(k=0;k<j;k++)
				{
					if(falseQuery.nodeList.get(k).equals(entityLList[j].get(randIndex)))
					{
						break;
					}
				}
				if(k<j)
					j--;
				else
					falseQuery.nodeList.add(entityLList[j].get(randIndex));
			}
			if(falseQuery.nodeList.size()>1)
			{
				HyperNode hn=GlobalStaticData.entityToNode.get(falseQuery.nodeList.get(0));
				List<HyperEdge> trueQueryList=hn.edges.get(falseQuery.nodeList.get(1));
				boolean flag=false;
				if(trueQueryList!=null)
					for(int j=0;j<trueQueryList.size();j++)
					{
						if(trueQueryList.get(j).equals(falseQuery))
						{
							flag=true;
							break;
						}
					}
				if(!flag)
				{
					//build interactions between nodes by node list of falseQuery as the db file.
					for(int j=0;j<falseQuery.nodeList.size();j++)
					{
						hn=GlobalStaticData.entityToNode.get(falseQuery.nodeList.get(j));
						for(int k=0;k<falseQuery.nodeList.size();k++)
						{
							if(j==k)
								continue;
							List<HyperEdge> edgeList=hn.edges.get(falseQuery.nodeList.get(k));
							if(edgeList==null)
							{
								edgeList=new ArrayList<HyperEdge>();
								hn.edges.put(falseQuery.nodeList.get(k), edgeList);
							}
							edgeList.add(falseQuery);
						}
					}
					falseQueryList.add(falseQuery);  //add to the query list
					if(falseQueryList.size()>=sum)
						break;
				}
			}
		}
		GlobalStaticData.entitativeTestQueryList.addAll(falseQueryList);
	}
	
	public static List<HyperEdge> ProduceFalseQuery_return_falsequerys(double falseRate)
	{
		//read true query to a set
		Set<HyperEdge> truequerySet=new HashSet<HyperEdge>();
		for(int i=0;i<GlobalStaticData.entitativeTestQueryList.size();i++)
		{
			HyperEdge query=GlobalStaticData.entitativeTestQueryList.get(i);
			truequerySet.add(query);
		}
		
		//prepare to build false queries

		int maxSum=GlobalStaticData.entitativeTestQueryList.size()*100;
		int sum=(int)((falseRate)*GlobalStaticData.entitativeTestQueryList.size());
		System.out.println(maxSum+"\t***\t"+sum);
		HyperEdge queryEdge=GlobalStaticData.conceptRelList.get(GlobalStaticData.query);
		Integer[] typeList=new Integer[queryEdge.nodeList.size()];
		List<Integer>[] entityLList=new ArrayList[queryEdge.nodeList.size()];
		for(int i=0;i<queryEdge.nodeList.size();i++)
		{
			typeList[i]=queryEdge.nodeList.get(i);
			//System.out.println(typeList[i]);
			entityLList[i]=GlobalStaticData.typeToEntityList.get(typeList[i]);
			
		}
		
		List<HyperEdge> falseQueryList=new ArrayList<HyperEdge>();
		for(int i=0;i<maxSum;i++)
		{
			
			HyperEdge falseQuery=new HyperEdge();
			falseQuery.rel=GlobalStaticData.query;
			falseQuery.value=0.;
			for(int j=0;j<entityLList.length;j++)
			{
				int randIndex=Math.abs(rand.nextInt()%entityLList[j].size());
				int k;
				//System.out.println("*\t"+randIndex+"\t"+entityLList[j].size());
				for(k=0;k<j;k++)
				{
					//filter duplicate entity in the same query
					if(falseQuery.nodeList.get(k).equals(entityLList[j].get(randIndex)))  //filter duplicate entity in the same query
					{
						break;
					}
				}
				if(k<j)
					j--;
				else
					falseQuery.nodeList.add(entityLList[j].get(randIndex));
			}
	
			//
			if(!truequerySet.contains(falseQuery))
			{
					falseQueryList.add(falseQuery);  //add to the query list
					if(falseQueryList.size()>=sum)
						break;
			}
		}
		//GlobalStaticData.entitativeTestQueryList.addAll(falseQueryList);
		return falseQueryList;
	}
	/**
	 * After FindPath, Formulize, Concept and Produce data point, this function use the weights learnt to calculate the probability<br>
	 * of the query is true, and the cll can be calculated by the probability and the query's truth value.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param dpList the data point list.
	 * @return the probability array for each query in the data point list.
	 */
	public static Double[] CalcularProbability(List<DataPoint> dpList,Double[] weights)
	{
		Double[] probability=new Double[dpList.size()];
		for(int i=0;i<dpList.size();i++)
		{
			DataPoint dp=dpList.get(i);
			double value0=0.;
			double value1=0.;
			for(int j=0;j<dp.countList.size();j++)
			{
				Integer weightIndex=dp.nodeList.get(j);
				value0+=weights[weightIndex]*dp.countList.get(j).falseCount;
				value1+=weights[weightIndex]*dp.countList.get(j).trueCount;
			}
			//the naive method
			/*value0=Math.exp(value0);
			value1=Math.exp(value1);
			probability[i]=value1;
			probability[i]/=(value0+value1);*/
			
			value0-=value1;
			if(value0>0)
			{
				double tempexp=Math.exp(-value0);
				probability[i]=tempexp/(1+tempexp);
			}
			else
			{
				double tempexp=Math.exp(value0);
				probability[i]=1./(1+tempexp);
			}
			
		}
		return probability;
	}
	/**
	 * After FindPath, Formulize, Concept and Produce data point, this function use the weights learnt to calculate the probability<br>
	 * of the query is true, and the cll can be calculated by the probability and the query's truth value.
	 * @author Zhuoyu Wei
	 * @version 1.1, Normal the datapoints' formula counts.
	 * @param dpList the data point list.
	 * @return the probability array for each query in the data point list.
	 * @date 2014.8.28
	 */
	public static Double[] CalcularProbabilityNormal(List<DataPointNormal> dpList,Double[] weights)
	{
		Double[] probability=new Double[dpList.size()];
		for(int i=0;i<dpList.size();i++)
		{
			DataPointNormal dp=dpList.get(i);
			double value0=0.;
			double value1=0.;
			for(int j=0;j<dp.countList.size();j++)
			{
				Integer weightIndex=dp.nodeList.get(j);
				value0+=weights[weightIndex]*dp.countList.get(j).falseNormal;
				value1+=weights[weightIndex]*dp.countList.get(j).trueNormal;
			}
			//the naive method
			/*value0=Math.exp(value0);
			value1=Math.exp(value1);
			probability[i]=value1;
			probability[i]/=(value0+value1);*/
			
			value0-=value1;
			if(value0>0)
			{
				double tempexp=Math.exp(-value0);
				probability[i]=tempexp/(1+tempexp);
			}
			else
			{
				double tempexp=Math.exp(value0);
				probability[i]=1./(1+tempexp);
			}
			
		}
		return probability;
	}	
	/**
	 * Get all true queries from the nodes read from the test db file.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public static void ExtractTrueQueryFromNodes()
	{
		for(int i=0;i<GlobalStaticData.entityToNode.size();i++)
		{
			HyperNode hn=GlobalStaticData.entityToNode.get(i);
			for(int j=0;j<hn.neighbourList.size();j++)
			{
				List<HyperEdge> queryList=hn.edges.get(hn.neighbourList.get(j));
				for(int k=0;k<queryList.size();k++)
				{
					if(queryList.get(k).rel.equals(GlobalStaticData.query))
					{
						GlobalStaticData.entitativeTestQueryList.add(queryList.get(k));
					}
				}
			}
		}
	}
	
	
	public static Map<AIntSet,HyperEdge> MapQueryListToIntSet(List<HyperEdge> queryList)
	{
		Map<AIntSet,HyperEdge> resultMap=new HashMap<AIntSet,HyperEdge>();
		
		
		for(int i=0;i<queryList.size();i++)
		{
			HyperEdge query=queryList.get(i);
			AIntSet ais=new AIntSet();
			for(int j=0;j<query.nodeList.size();j++)
			{
				ais.set.add(query.nodeList.get(j));
			}
			resultMap.put(ais, query);
		}
		
		return resultMap;
	}
	
	public static void NormalDataPoints(List<DataPoint> dpList, List<DataPointNormal> dpnList)
	{
		for(int i=0;i<dpList.size();i++)
		{
			DataPoint dp=dpList.get(i);
			DataPointNormal dpn=new DataPointNormal();
			
			dpn.query=dp.query;
			int sum=0;
			for(int j=0;j<dp.nodeList.size();j++)
			{
				TrueFalseCount tfc=dp.countList.get(j);
				sum+=tfc.trueCount>tfc.falseCount?tfc.trueCount:tfc.falseCount;
			}
			for(int j=0;j<dp.nodeList.size();j++)
			{
				dpn.nodeList.add(dp.nodeList.get(j));
				TrueFalseCount tfc=dp.countList.get(j);
				TrueFalseNormal tfn=new TrueFalseNormal();
				if(sum>0)
				{
					tfn.trueNormal=(double)tfc.trueCount/(double)sum;
					tfn.falseNormal=(double)tfc.falseCount/(double)sum;
					if(tfn.trueNormal>tfn.falseNormal)
					{
						tfn.trueNormal-=tfn.falseNormal;
						tfn.falseNormal=0.;
					}
					else
					{
						tfn.falseNormal-=tfn.trueNormal;
						tfn.trueNormal=0.;
					}
				}
				dpn.countList.add(tfn);
			}
			
			dpnList.add(dpn);
		}
	}
	
}

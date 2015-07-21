package wzy.func;

import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.*;

import wzy.model.BigQuery;
import wzy.model.DataPoint;
import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.model.HyperPath;
import wzy.model.StatePOG;
import wzy.model.TrueFalseCount;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;

public class InferOneBigQueryThread implements Callable{

	public Integer bigqueryID;
	public BigQuery bigquery;
	public List<DataPoint> dpList=new ArrayList<DataPoint>();
	public Double[] scores;
	public Learner learner;

	//for random walk
	public Random rand=new Random();
	public Integer startPoint;
	public StatePOG obj_nowState;
	public Set<Integer> nodeSet=new HashSet<Integer>();
	public int maxround=GlobalStaticData.MaxRound;
	public Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount
		=new HashMap<HyperEdge,Map<HyperPath,TrueFalseCount>>();
	
	public Map<HyperPath,Integer> formulaToIndex;
	public List<HyperPath> indexToFormula;
	
	public static Set<Integer> printSet=new HashSet<Integer>();
	
	
	//for debug, logprint randomwalkpath, by wzy, 11.3
	//public PrintStream ps_rw_path;
	public static void InitSet()
	{
		printSet.add(7056);
		printSet.add(38517);
		printSet.add(28513);
		printSet.add(20961);	
		printSet.add(48625);
		printSet.add(55932);
		printSet.add(46290);			
	}
	
	public void InitThread()
	{
		startPoint=bigquery.entity;
		obj_nowState=new StatePOG();
		obj_nowState.state.add(startPoint);
		for(int i=0;i<bigquery.queryList.size();i++)
		{
			queryToCount.put(bigquery.queryList.get(i), new HashMap<HyperPath,TrueFalseCount>());
		}
	}
	/**
	 * a private function of copy a List from start index to end index
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param dlist Destination List for copy
	 * @param slist Sourse List for copy
	 * @param start start index
	 * @param end end index
	 */
	private void copyList(List dlist,List slist, int start,int end)
	{
		for(int i=start;i<=end;i++)
		{
			dlist.add(slist.get(i));
		}
	}
	
	/**
	 * Build a new State from the nowState by adding an entity at the head of the nowState
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param nowState
	 * @param entity
	 * @return the new state
	 */
	private StatePOG pushAhead(StatePOG nowState,Integer entity)
	{
		StatePOG statepog=new StatePOG();
		statepog.state.add(entity);
		statepog.state.addAll(nowState.state);
		return statepog;
	}
	/**
	 * Build a new State from the nowState by adding an entity at the end of the nowState
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param nowState
	 * @param entity
	 * @return the new state
	 */
	private StatePOG pushBack(StatePOG nowState,Integer entity)
	{
		StatePOG statepog=new StatePOG();
		statepog.state.addAll(nowState.state);
		statepog.state.add(entity);
		return statepog;
	}	
	/**
	 * Build a new State from the nowState by removing the entity at the head of the nowState
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param nowState
	 * @param entity
	 * @return the new state
	 */	
	private StatePOG removeAhead(StatePOG nowState)
	{
		StatePOG statepog=new StatePOG();
		copyList(statepog.state,nowState.state,1,nowState.state.size()-1);
		return statepog;
	}
	/**
	 * Build a new State from the nowState by removing the entity at the end of the nowState
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param nowState
	 * @param entity
	 * @return the new state
	 */		
	private StatePOG removeBack(StatePOG nowState)
	{
		StatePOG statepog=new StatePOG();
		copyList(statepog.state,nowState.state,0,nowState.state.size()-2);
		return statepog;
	}
	private boolean RandomWalk()
	{
		double randPro=Math.abs(rand.nextDouble());
		StatePOG next_state=null;
		if((obj_nowState.state.size()>=GlobalStaticData.MaxLength)||(obj_nowState.state.size()>1&&randPro<0.5))
		{
			//Minus nodes
			if(obj_nowState.state.get(0).equals(startPoint))
			{
				next_state=this.removeBack(obj_nowState);
				nodeSet.remove(obj_nowState.state.get(obj_nowState.state.size()-1));
			}
			else if(obj_nowState.state.get(obj_nowState.state.size()-1).equals(startPoint))
			{
				next_state=this.removeAhead(obj_nowState);
				nodeSet.remove(obj_nowState.state.get(0));
			}
			else
			{
				randPro=Math.abs(rand.nextDouble());
				if(randPro<0.5)
				{
					next_state=this.removeBack(obj_nowState);
					nodeSet.remove(obj_nowState.state.get(obj_nowState.state.size()-1));					
				}
				else
				{
					next_state=this.removeAhead(obj_nowState);
					nodeSet.remove(obj_nowState.state.get(0));					
				}
			}
		}
		else
		{
			//different rand for add nodes
			boolean turnon0=obj_nowState.state.get(0).equals(startPoint);
			boolean turnon1=obj_nowState.state.get(obj_nowState.state.size()-1).equals(startPoint);
			int leftIndex=obj_nowState.state.get(0);
			HyperNode leftHn=GlobalStaticData.entityToNode.get(leftIndex);
			int leftNeighbourSize=leftHn.neighbourList.size();
			if(turnon0)
			{
				leftNeighbourSize+=bigquery.entityList.size();
			}			
			
			int rightIndex=obj_nowState.state.get(obj_nowState.state.size()-1);
			HyperNode rightHn=GlobalStaticData.entityToNode.get(rightIndex);
			int rightNeighbourSize=rightHn.neighbourList.size();
			if(turnon1)
			{
				rightNeighbourSize+=bigquery.entityList.size();
			}
			
			
			int sumNeighbourSize=leftNeighbourSize+rightNeighbourSize;
			if(sumNeighbourSize==0)
				return false;
			
			
			
			int randNext=Math.abs(rand.nextInt())%sumNeighbourSize;
			if(randNext<leftNeighbourSize)
			{
				Integer temp;
				if(turnon0)
				{
					if(randNext<leftHn.neighbourList.size())
					{
						temp=leftHn.neighbourList.get(randNext);
					}
					else
					{
						temp=bigquery.entityList.get(randNext-leftHn.neighbourList.size());
					}
				}
				else
					temp=leftHn.neighbourList.get(randNext);
				int tcount=1;
				while(nodeSet.contains(temp))
				{
					if(turnon0)
					{
						if((randNext+tcount)%leftNeighbourSize<leftHn.neighbourList.size())
						{
							temp=leftHn.neighbourList.get((randNext+tcount)%leftNeighbourSize);
						}
						else
						{
							temp=bigquery.entityList.get((randNext+tcount)%leftNeighbourSize-leftHn.neighbourList.size());
						}		
						tcount++;
					}
					else
						temp=leftHn.neighbourList.get((randNext+(tcount++))%leftNeighbourSize);
					if(tcount>=leftNeighbourSize)
						break;
				}
				if(nodeSet.contains(temp))
				{
					return true;
				}
				next_state=this.pushAhead(obj_nowState, temp);
				nodeSet.add(temp);
			}
			else
			{
				Integer temp;
				randNext-=leftNeighbourSize;
				if(turnon1)
				{
					if(randNext<rightHn.neighbourList.size())
					{
						temp=rightHn.neighbourList.get(randNext);
					}
					else
					{
						temp=bigquery.entityList.get(randNext-rightHn.neighbourList.size());
					}
				}
				else
					temp=rightHn.neighbourList.get(randNext);
				int tcount=1;
				while(nodeSet.contains(temp))
				{
					if(turnon1)
					{
						if((randNext+tcount)%rightNeighbourSize<rightHn.neighbourList.size())
						{
							temp=rightHn.neighbourList.get((randNext+tcount)%rightNeighbourSize);
						}
						else
						{
							temp=bigquery.entityList.get((randNext+tcount)%rightNeighbourSize-rightHn.neighbourList.size());
						}	
						tcount++;
					}
					else
						temp=rightHn.neighbourList.get((randNext+(tcount++))%rightNeighbourSize);
					if(tcount>=rightNeighbourSize)
						break;
				}
				if(nodeSet.contains(temp))
				{
					return true;
				}
				next_state=this.pushBack(obj_nowState, temp);
				nodeSet.add(temp);
			}
		}
		
		//Finish a random walk and get a StatePog, use it generate all Formula, and for each formula,
		//It will band to its each atom as query.
		obj_nowState=next_state;
		//OutputData.logprint_randomwalkpath(obj_nowState, this.ps_rw_path);
		int hasCount=0;
		if(obj_nowState.state.size()>=GlobalStaticData.MinLength&&obj_nowState.state.size()<=GlobalStaticData.MaxLength)
		{
			List<HyperPath> hyperPathList=Formulize_knowledge(obj_nowState);
			for(int i=0;i<hyperPathList.size();i++)
			{
				CountFormulaForRels(hyperPathList.get(i));
			}
		}
		//System.out.println("True Count: "+hasCount);
		return true;
		
	}

	
	
	private List<HyperEdge> FomulizeQuery_knowledge(Integer firstEntity,Integer secondEntity,Integer MaxFalseQuery)
	{
		HyperNode firstNode=GlobalStaticData.entityToNode.get(firstEntity);
		HyperNode secondNode=GlobalStaticData.entityToNode.get(secondEntity);
		//Filter undirect Rel, by wzy 2014.7.9
		List<HyperEdge> tempList=firstNode.edges.get(secondEntity);
		List<HyperEdge> queryList=new ArrayList<HyperEdge>();
		Set<Integer> hasrelSet=new HashSet<Integer>();
		if(tempList!=null)
		for(int i=0;i<tempList.size();i++)
		{
			HyperEdge tempEdge=tempList.get(i);
			if(GlobalStaticData.undirectRelSet.contains(tempEdge.rel))
			{
				if(tempEdge.nodeList.get(0).equals(firstEntity))
				{
					queryList.add(tempEdge);
					hasrelSet.add(tempEdge.rel);
				}
			}
			else
			{
				queryList.add(tempEdge);
				hasrelSet.add(tempEdge.rel);
			}
		}
		//这可能会导致一些问题：如果R是对称关系，那么R(A,B)是DB中的，R(B,A)是带预测的，那么R(B,A)是否会受到影响
		if(firstEntity.equals(startPoint)||secondEntity.equals(startPoint))
		{
			HyperEdge tempHead=new HyperEdge();
			tempHead.rel=bigquery.rel;
			tempHead.nodeList.add(firstEntity);
			tempHead.nodeList.add(secondEntity);
			tempHead.value=1.;
			if(queryToCount.get(tempHead)!=null)
			{
				queryList.add(tempHead);
				hasrelSet.add(tempHead.rel);
			}
			
			HyperEdge tempHead2=new HyperEdge();
			tempHead2.rel=bigquery.rel;
			tempHead2.nodeList.add(secondEntity);
			tempHead2.nodeList.add(firstEntity);
			tempHead2.value=1.;
			if(queryToCount.get(tempHead2)!=null)
			{
				queryList.add(tempHead2);
				hasrelSet.add(tempHead2.rel);				
			}			
			
		}
		
		
		//return queryList;
		List<HyperEdge> trueheadList=queryList;
		queryList=new ArrayList<HyperEdge>();

		///////false head/////
			queryList=new ArrayList<HyperEdge>();
			int conceptRelListSize=GlobalStaticData.conceptRelList.size();
			if(conceptRelListSize>MaxFalseQuery*2)
			{
				conceptRelListSize=MaxFalseQuery*2;
			}
			for(int i=0;i<conceptRelListSize;i++)
			{
				int index=GlobalStaticData.conceptRelList.get(i).rel;
				if(conceptRelListSize<GlobalStaticData.conceptRelList.size())
				{
					index=GlobalStaticData.conceptRelList.get(Math.abs(rand.nextInt())
							%GlobalStaticData.conceptRelList.size()).rel;
				}

				if(hasrelSet.contains(index))
					continue;
				
				HyperEdge headahead=new HyperEdge();
				headahead.rel=index;
				headahead.value=0.;
				headahead.nodeList=new ArrayList<Integer>();
				headahead.nodeList.add(firstEntity);
				headahead.nodeList.add(secondEntity);	
				queryList.add(headahead);
				
				HyperEdge headback=new HyperEdge();
				headback.rel=index;
				headback.value=0.;
				headback.nodeList=new ArrayList<Integer>();
				headback.nodeList.add(secondEntity);
				headback.nodeList.add(firstEntity);
				queryList.add(headback);
				
			}
						
			if(queryList.size()>MaxFalseQuery)
			{
				List<HyperEdge> tempQueryList=queryList;
				queryList=new ArrayList<HyperEdge>();
				for(int i=0;i<MaxFalseQuery;i++)
				{
					int queryIndex=Math.abs(rand.nextInt())%tempQueryList.size();
					queryList.add(tempQueryList.get(queryIndex));
				}
			}
			
		//add truequery
		if(trueheadList.size()>0)
			queryList.addAll(trueheadList);
			
		return queryList;
	}	
	private List<HyperEdge> FormulizeTrueEdgeFromTwoNode_know_infer
		(Integer firstEntity,Integer secondEntity)
	{
		HyperNode firstNode=GlobalStaticData.entityToNode.get(firstEntity);
		
		List<HyperEdge> tempList=firstNode.edges.get(secondEntity);  //Take care about the returned List should be const, and it mustn't be changed.
		List<HyperEdge> resultList=new ArrayList<HyperEdge>();
		if(tempList!=null){
		for(int i=0;i<tempList.size();i++)
		{
			HyperEdge tempEdge=tempList.get(i);
			if(GlobalStaticData.undirectRelSet.contains(tempEdge.rel))
			{
				if(tempEdge.nodeList.get(0).equals(firstEntity))
				{
					resultList.add(tempEdge);
				}
			}
			else
			{
				resultList.add(tempEdge);
			}
		}
		}
		
		if(firstEntity.equals(startPoint)||secondEntity.equals(startPoint))
		{
			HyperEdge tempHead=new HyperEdge();
			tempHead.rel=bigquery.rel;
			tempHead.nodeList.add(firstEntity);
			tempHead.nodeList.add(secondEntity);
			tempHead.value=1.;
			if(queryToCount.get(tempHead)!=null)
			{
				resultList.add(tempHead);
				//hasrelSet.add(tempHead.rel);
			}
			
			HyperEdge tempHead2=new HyperEdge();
			tempHead2.nodeList.add(secondEntity);
			tempHead2.nodeList.add(firstEntity);
			tempHead2.value=1.;
			tempHead2.rel=bigquery.rel;
			if(queryToCount.get(tempHead2)!=null)
			{
				resultList.add(tempHead2);
				//hasrelSet.add(tempHead2.rel);				
			}			
			
		}
		
		return resultList;
	}
	
	public List<HyperPath> Formulize_knowledge(StatePOG statepog)
	{
		List<HyperPath> resultList=new ArrayList<HyperPath>();
		if(statepog==null||statepog.state==null||statepog.state.size()<2)
			return resultList;
		//Filter for query
		if(GlobalStaticData.MaxFalseQuery<0)
			GlobalStaticData.MaxFalseQuery=2;
		List<HyperEdge> queryList=FomulizeQuery_knowledge(statepog.state.get(0),
				statepog.state.get(statepog.state.size()-1),GlobalStaticData.MaxFalseQuery);

		if(queryList==null||queryList.size()<=0)
			return resultList;
		
		//Cartesian product for formulizing body path
		resultList.add(new HyperPath());
		for(int i=0;i<statepog.state.size()-1;i++)
		{
			List<HyperPath> tempPathList=resultList;
			resultList=new ArrayList<HyperPath>();
			List<HyperEdge> trueEdgeList=FormulizeTrueEdgeFromTwoNode_know_infer(statepog
					.state.get(i),statepog.state.get(i+1));
			//debug--->debug ps log
			if(trueEdgeList==null)
			{
				return null;
			}
			else
			{
				
				for(int j=0;j<tempPathList.size();j++)
				{
					HyperPath nowPath=tempPathList.get(j);
					for(int k=0;k<trueEdgeList.size();k++)
					{
						HyperPath newPath=nowPath.copyOnePath();
						newPath.path.add(trueEdgeList.get(k));
						resultList.add(newPath);
					}
				}
			}
		}
		//add query at the end of path.
		List<HyperPath> tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		List<HyperEdge> trueEdgeList=queryList;
		for(int j=0;j<tempPathList.size();j++)
		{
			HyperPath nowPath=tempPathList.get(j);
			for(int k=0;k<trueEdgeList.size();k++)
			{
				HyperPath newPath=nowPath.copyOnePath();
				newPath.path.add(trueEdgeList.get(k));
				resultList.add(newPath);
			}
		}			
		
		//this.ps_rw_path.println("Before mln filter: "+resultList.size());
		//FiterFormula by learned MLN and has and has only one query.
		tempPathList=resultList;
		resultList=new ArrayList<HyperPath>();
		for(int i=0;i<tempPathList.size();i++)
		{
			if(FilterFormula(tempPathList.get(i)))
			{
				resultList.add(tempPathList.get(i));
			}
		}
		//this.ps_rw_path.println("After mln filter: "+resultList.size());		
		return resultList;
		
	}
	
	
	
	public boolean FilterFormula(HyperPath formula)
	{
		//HyperPath conceptFormula=MLNProcess.Conceptualize(formula);
		//if(formulaToIndex.get(conceptFormula)==null)
			//return false;
		int count=0;
		for(int i=0;i<formula.path.size();i++)
		{
			if(queryToCount.get(formula.path.get(i))!=null)
			{
				count++;
			}
		}
		return count==1;
	}
	
	public static void IndexQueryAndProduceDataPointTest(Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryMap
			,List<HyperEdge> queryList,List<DataPoint> dpList,
			List<HyperPath> indexToConceptFormula,Map<HyperPath,Integer> conceptFormulaToIndex)
	{
		
		//dpList=new ArrayList<DataPoint>();
		for(int i=0;i<queryList.size();i++)
		{
			HyperEdge query=queryList.get(i);
			DataPoint dp=new DataPoint();
			dp.query=query;
			dp.nodeList=new ArrayList<Integer>();
			dp.countList=new ArrayList<TrueFalseCount>();
			Map<HyperPath,TrueFalseCount> conceptCount=queryMap.get(query);
			if(conceptCount==null)
			{
				conceptCount=new TreeMap<HyperPath,TrueFalseCount>();
			}

			
			
			Iterator it=conceptCount.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry entry=(Map.Entry)it.next();
				HyperPath formula=(HyperPath)entry.getKey();
				Integer formulaIndex=conceptFormulaToIndex.get(formula);
				if(formulaIndex!=null)
				{
					TrueFalseCount tfc=(TrueFalseCount)entry.getValue();
					dp.nodeList.add(formulaIndex);
					dp.countList.add(tfc);
				}
			}
			dpList.add(dp);
		}
	}	
	
	
	private void CountFormulaForRels(HyperPath hyperPath)
	{
		HyperPath conceptFormula=MLNProcess.Conceptualize(hyperPath);
		if(formulaToIndex.get(conceptFormula)==null)
			return;
		for(int i=0;i<hyperPath.path.size();i++)
		{
			Map<HyperPath,TrueFalseCount> formulaMap=queryToCount.get(hyperPath.path.get(i));
			if(formulaMap==null)
				continue;
			TrueFalseCount tfCount=formulaMap.get(conceptFormula);
			if(tfCount==null)
			{
				tfCount=new TrueFalseCount();
				formulaMap.put(conceptFormula, tfCount);
			}
			Boolean[] truthQueryList=new Boolean[hyperPath.path.size()];
			for(int j=0;j<hyperPath.path.size();j++)
			{
				truthQueryList[j]=hyperPath.path.get(j).value>0.5;
			}
			truthQueryList[i]=true;
			if(MLNProcess.CheckFormulaTrueOrFalse(truthQueryList))
			{
				tfCount.trueCount+=1;
			}
			truthQueryList[i]=false;					
			if(MLNProcess.CheckFormulaTrueOrFalse(truthQueryList))
			{
				//by wzy, change back immediately
				tfCount.falseCount+=1;
			}
		}
	}
	
	public void RandomWalkProcess()
	{
		for(int i=0;i<maxround;i++)
		{
			try{
				//boolean flag=RandomWalkRemVisited(obj_nowState,5,GlobalStaticData.restartProbability);
				boolean flag;
				
				flag=RandomWalk();
				
				if(!flag) //This thread has error, and stop it;
					break;
			}catch(Exception e)
			{
				e.printStackTrace(LogPrintStream.getPs_thread_info());
			}
			
		}
	}
	
	public void RankQueries()
	{
		List<QueryAndScore> qsList=new ArrayList<QueryAndScore>();
		for(int i=0;i<scores.length;i++)
		{
			QueryAndScore qs=new QueryAndScore();
			qs.entity=bigquery.entityList.get(i);
			qs.score=scores[i];
			qsList.add(qs);
		}
		Collections.sort(qsList,new QueryAndScore());
		bigquery.entityList=new ArrayList<Integer>(scores.length);
		for(int i=0;i<scores.length;i++)
		{
			bigquery.entityList.add(qsList.get(i).entity);
			bigquery.debugScore.add(qsList.get(i).score);
		}
	}
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		try{
		LogPrintStream.getPs_alwaysdebug().println("infer Thread "+this.bigqueryID+" is start.");
		long start=System.currentTimeMillis();
		//if(printSet.contains(bigqueryID))
		//if(true)
		{
			LogPrintStream.getPs_thread_info().println("Id: "+bigqueryID+
					" Bigquery size: "+this.bigquery.entityList.size()+
					" dpList size: "+this.dpList.size()+
					" Weight size: "+this.learner.getWeights().length);
		}
		
		//this.ps_rw_path.println("Formula: "+this.formulaToIndex.size()+" "+this.indexToFormula.size());
		
		RandomWalkProcess();
		
		//if(printSet.contains(bigqueryID))
		{
			LogPrintStream.getPs_thread_info().println(bigqueryID+" Random is over.");
		}		
		IndexQueryAndProduceDataPointTest(queryToCount,bigquery.queryList,dpList,
				indexToFormula,formulaToIndex);
		//if(printSet.contains(bigqueryID))
		{
			LogPrintStream.getPs_thread_info().println(bigqueryID+" DataPoint is over.");
		}		
		scores=InferenceTestTool.CalcularProbability(dpList,learner.getWeights());
		//if(printSet.contains(bigqueryID))
		{
			LogPrintStream.getPs_thread_info().println(bigqueryID+" CalcularPro is over.");
		}			
		RankQueries();
		{
			LogPrintStream.getPs_thread_info().println(bigqueryID+" Sort is over.");
		}			
		long end=System.currentTimeMillis();
		LogPrintStream.getPs_alwaysdebug().println("infer Thread "+this.bigqueryID+" is over. "+(end-start)+"ms");
		}catch(Exception e)
		{
			e.printStackTrace(LogPrintStream.getPs_alwaysdebug());
		}
		return null;
	}
	
}

class QueryAndScore implements Comparator
{
	//public HyperEdge query;
	public Integer entity;
	public Double score;
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		QueryAndScore q1=(QueryAndScore)o1;
		QueryAndScore q2=(QueryAndScore)o2;
		if(Math.abs(q1.score-q2.score)<1e-20)
			return 0;
		else if(q1.score>q2.score)
			return -1;
		else
			return 1;
	}
}

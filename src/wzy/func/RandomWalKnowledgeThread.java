package wzy.func;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;

import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.model.HyperPath;
import wzy.model.StatePOG;
import wzy.model.TrueFalseCount;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;

public class RandomWalKnowledgeThread implements Callable{

	private Integer threadId;
	private Integer startPoint;
	private Random rand=new Random();
	private StatePOG obj_nowState;
	private Set<Integer> nodeSet=new HashSet<Integer>();
	public boolean islearning=true;
	//public Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount=new HashMap<HyperEdge,Map<HyperPath,TrueFalseCount>>();
	public Map<Integer,Map<HyperEdge,Map<HyperPath,TrueFalseCount>>> relationToQueryToFormulaToCount
		=new HashMap<Integer,Map<HyperEdge,Map<HyperPath,TrueFalseCount>>>();
	public int maxround=GlobalStaticData.MaxRound;

	public void setThreadId(Integer threadId) {
		this.threadId = threadId;
	}
	public void setStartPoint(Integer startPoint) {
		this.startPoint = startPoint;
	}

	/**
	 * initialize obj_nowState and obj_preState, it must run before random walk
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public void InitStates()
	{
		//obj_preState=null;
		obj_nowState=new StatePOG();
		obj_nowState.state.add(startPoint);
		//stateList=new ArrayList<StatePOG>();
	}
	/**
	 * initialize obj_nowState(from an existence state) and obj_preState, it must run before random walk
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param state an existent state as start point
	 */
	public void InitStates(StatePOG state)
	{
		//obj_preState=null;
		obj_nowState=state;
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
		
		if(queryList!=null&&queryList.size()>0)
			return queryList;
		
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
			List<HyperEdge> trueEdgeList=MLNProcess.FormulizeTrueEdgeFromTwoNode(statepog.state.get(i),statepog.state.get(i+1));
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
		return resultList;
	}
	
	private void CountFormulaForRels(HyperPath hyperPath)
	{
		HyperPath conceptFormula=MLNProcess.Conceptualize(hyperPath);
		/*Integer formulaIndex=GlobalStaticData.conceptFormulaToIndex.get(hyperPath);
		if(islearning)
		{
			GlobalStaticData.conceptFormulaToIndex.put(conceptFormula, 
					GlobalStaticData.indexToConceptFormula.size());
			GlobalStaticData.indexToConceptFormula.add(conceptFormula);
		}
		else
		{
			return;
		}*/
		for(int i=0;i<hyperPath.path.size();i++)
		{
			HyperEdge query=hyperPath.path.get(i);
			Map<HyperEdge,Map<HyperPath,TrueFalseCount>> aRelMap
				=relationToQueryToFormulaToCount.get(query.rel);
			if(aRelMap==null)
			{
				aRelMap=new HashMap<HyperEdge,Map<HyperPath,TrueFalseCount>>();
				relationToQueryToFormulaToCount.put(query.rel, aRelMap);
			}
			Map<HyperPath,TrueFalseCount> aFormulaMap=aRelMap.get(query);
			if(aFormulaMap==null)
			{
				aFormulaMap=new HashMap<HyperPath,TrueFalseCount>();
				aRelMap.put(query, aFormulaMap);
			}
			TrueFalseCount tfCount=aFormulaMap.get(conceptFormula);
			if(tfCount==null)
			{
				tfCount=new TrueFalseCount();
				aFormulaMap.put(conceptFormula, tfCount);
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
			int leftIndex=obj_nowState.state.get(0);
			HyperNode leftHn=GlobalStaticData.entityToNode.get(leftIndex);
			int leftNeighbourSize=leftHn.neighbourList.size();
			
			int rightIndex=obj_nowState.state.get(obj_nowState.state.size()-1);
			HyperNode rightHn=GlobalStaticData.entityToNode.get(rightIndex);
			int rightNeighbourSize=rightHn.neighbourList.size();
			
			int sumNeighbourSize=leftNeighbourSize+rightNeighbourSize;
			if(sumNeighbourSize==0)
				return false;
			
			
			
			int randNext=Math.abs(rand.nextInt())%sumNeighbourSize;
			if(randNext<leftNeighbourSize)
			{
				Integer temp=leftHn.neighbourList.get(randNext);
				int tcount=0;
				while(nodeSet.contains(temp))
				{
					temp=leftHn.neighbourList.get((randNext+(++tcount))%leftHn.neighbourList.size());
					if(tcount>=leftHn.neighbourList.size())
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
				Integer temp=rightHn.neighbourList.get(randNext-leftNeighbourSize);
				int tcount=0;
				while(nodeSet.contains(temp))
				{
					temp=rightHn.neighbourList.get((randNext-leftNeighbourSize+(++tcount))%rightHn.neighbourList.size());
					if(tcount>=rightHn.neighbourList.size())
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
		//debug by wzy, 11.4
		/*for(int i=0;i<obj_nowState.state.size();i++)
		{
			LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print(obj_nowState.state.get(i)+"\t");
		}
		LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println();*/
		
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
	
	
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		//LogPrintStream.getPs_thread_info().println("Thread "+threadId+" is started!");
		//long startTimePoint=System.currentTimeMillis();
		this.InitStates();
		nodeSet.add(startPoint);
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
		GlobalStaticData.totalDataPointMapList.add(relationToQueryToFormulaToCount);
		//long endTimePoint=System.currentTimeMillis();
		//LogPrintStream.getPs_thread_info().println("Thread "+threadId+" is end in "+(endTimePoint-startTimePoint)+"ms");
		return null;
	}

}

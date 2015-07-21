package wzy.func;

import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.*;

import wzy.main.MultiSemanticLinkPrediction;
import wzy.model.*;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;

public class OneRelTaskThread implements Callable{

	public Integer rel;
	public Random rand=new Random();
	public List<HyperEdge> queryList;
	public Set<HyperEdge> querySet;
	public Double[] scores;
	public Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount=new HashMap<HyperEdge
			,Map<HyperPath,TrueFalseCount>>();
	public Learner learner=new Learner();
	public List<DataPoint> dpList=new ArrayList<DataPoint>();
	public List<Integer> trainseedList=new ArrayList<Integer>();
	public List<Integer> testseedList=new ArrayList<Integer>();
	
	public StatePOG obj_nowState;
	public int startPoint;
	private Set<Integer> nodeSet=new HashSet<Integer>();
	
	private Map<HyperPath,Integer> formulaIndex=new HashMap<HyperPath,Integer>();
	private List<HyperPath> indexFormula=new ArrayList<HyperPath>();
	
	
	public static int maxIterator;
	public static double lamada;
	public static double upsilon;
	public static double C;
	public static int K;
	public static double z;

	public boolean isLearning=true;
	
	public static boolean isDebug=true;
	public static String querydir="";
	
	private void GeneSeedList()
	{
		Set<Integer> seedSet=new HashSet<Integer>();
		for(int i=0;i<queryList.size();i++)
		{
			seedSet.addAll(queryList.get(i).nodeList);
		}
		testseedList.addAll(seedSet);
	}
	
	private void RandomWalkProcess(List<Integer> seedList)
	{
		for(int i=0;i<GlobalStaticData.RandomWalkNum;i++)
		{
			startPoint=Math.abs(rand.nextInt())%seedList.size();
			obj_nowState=new StatePOG();
			obj_nowState.state.add(startPoint);
			nodeSet=new HashSet<Integer>();
			nodeSet.add(startPoint);
			RandomWalk();
			
		}
	}
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
	private StatePOG RandomChooseNextStateAdvance(StatePOG nowState, Integer[] nextOperation)
	{
		StatePOG nextState=null;
		Integer randIndex=Math.abs(rand.nextInt())%nextOperation[3]+1;
		int i;
		for(i=0;i<5;i++)
		{
			if(randIndex<=nextOperation[i])
			{
				break;
			}
		}

		switch(i)
		{
		case 0:{
			nextState=removeAhead(nowState);
			break;
		}
		case 1:{
			nextState=removeBack(nowState);
			break;
		}
		case 2:{
			Integer nowStateHeadNodeEntity=nowState.state.get(0);
			HyperNode nowStateHeadNode=GlobalStaticData.entityToNode.get(nowStateHeadNodeEntity);
			nextState=pushAhead(nowState,nowStateHeadNode.neighbourList.get(randIndex-nextOperation[1]-1));
			break;
		}
		case 3:{
			Integer nowStateEndNodeEntity=nowState.state.get(nowState.state.size()-1);
			HyperNode nowStateEndNode=GlobalStaticData.entityToNode.get(nowStateEndNodeEntity);
			nextState=pushBack(nowState,nowStateEndNode.neighbourList.get(randIndex-nextOperation[2]-1));
		}
		case 4: //restart
			{
			nextState=new StatePOG();
			//nextState.state.add(Math.abs(rand.nextInt()%GlobalStaticData.entityToNode.size()));
			nextState.state.add(this.startPoint);
		}
		}
		return nextState;
	}	



	private void RandomWalk()
	{
		for(int i=0;i<GlobalStaticData.MaxRound;i++)
		{
			try{
				//boolean flag=RandomWalkRemVisited(obj_nowState,5,GlobalStaticData.restartProbability);
				boolean flag=RandomOneWalk();
				if(!flag) //This thread has error, and stop it;
					break;
			}catch(Exception e)
			{
				e.printStackTrace(LogPrintStream.getPs_thread_info());
			}
			
		}
	}
	
	private boolean RandomOneWalk()
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
		Integer findex=formulaIndex.get(conceptFormula);
		if(findex==null)
		{
			if(isLearning)
			{
				formulaIndex.put(conceptFormula, indexFormula.size());
				indexFormula.add(conceptFormula);
			}
			else
			{
				return;
			}
		}

		for(int i=0;i<hyperPath.path.size();i++)
		{
			HyperEdge query=hyperPath.path.get(i);
			if(!query.rel.equals(rel))
				continue;
			Map<HyperPath,TrueFalseCount> aFormulaMap=queryToCount.get(query);
			if(aFormulaMap==null)
			{
				aFormulaMap=new HashMap<HyperPath,TrueFalseCount>();
				queryToCount.put(query, aFormulaMap);
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
	
	/**
	 * After producing the train data points, this function traverse all conceptual formulas got in the formulizing process<br>
	 * to keep ones appearing at least once. And it also rebuild the map from conceptual formulas to index and reproduce new<br>
	 * data points from the old ones.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	private void FilterFormulaByAtLeastOnceTrueForTrain()
	{
		//choose the conceptual formulas with at least once true example in the knowledge base
		List<DataPoint> tempdpList=dpList;
		int[] trueCount=new int[this.indexFormula.size()];
		for(int i=0;i<tempdpList.size();i++)
		{
			DataPoint dp=tempdpList.get(i);
			boolean truthvalue=dp.query.value>0.5;
			for(int j=0;j<dp.countList.size();j++)
			{
				TrueFalseCount tfc=dp.countList.get(j);
				if((truthvalue&&tfc.trueCount>0)||(!truthvalue&&tfc.falseCount>0))
				{
					trueCount[dp.nodeList.get(j)]++;
				}
			}
		}
		List<HyperPath> indexToConceptFormula=new ArrayList<HyperPath>();
		Map<HyperPath,Integer> conceptFormulaToIndex=new HashMap<HyperPath,Integer>();
		
		//remove the conceptual formula with zero appearings from the global index.
		for(int i=0;i<trueCount.length;i++)
		{
			if(trueCount[i]>0)
			{
				HyperPath hp=indexFormula.get(i);
				conceptFormulaToIndex.put(hp,indexToConceptFormula.size());
				indexToConceptFormula.add(hp);
			}
		}

		
		//rebuild the data points with the new indexs
		dpList=new ArrayList<DataPoint>();
		for(int i=0;i<tempdpList.size();i++)
		{
			DataPoint dp=tempdpList.get(i);
			DataPoint dpnew=new DataPoint();
			dpnew.query=dp.query;
			dpnew.countList=new ArrayList<TrueFalseCount>();
			dpnew.nodeList=new ArrayList<Integer>();
			for(int j=0;j<dp.nodeList.size();j++)
			{
				HyperPath hp=indexFormula.get(dp.nodeList.get(j));
				Integer newIndex=conceptFormulaToIndex.get(hp);
				if(newIndex!=null)
				{
					dpnew.nodeList.add(newIndex);
					dpnew.countList.add(dp.countList.get(j));
				}
			}
			if(dpnew.countList.size()>1)
			{
				dpList.add(dpnew);
			}
		}
		
		
		indexFormula=indexToConceptFormula;
		formulaIndex=conceptFormulaToIndex;	
	}
	
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		try{
		LogPrintStream.getPs_thread_info().println("One rel task "+rel+" starts.");
		long start_time=System.currentTimeMillis();
		trainseedList=GlobalStaticData.relTotrainSeedList.get(rel);
		if(trainseedList==null)
		{
			scores=new Double[queryList.size()];
			for(int i=0;i<scores.length;i++)
			{
				scores[i]=0.;
			}
			return null;
		}
		///////////////////
		queryList=GlobalStaticData.relToQuery.get(rel);
		if(queryList==null)
			return null;
		querySet=new HashSet<HyperEdge>();
		querySet.addAll(queryList);
		//////////////////////
		RandomWalkProcess(trainseedList);	
		long debug_rw=0;
		if(isDebug)
		{
			debug_rw=System.currentTimeMillis();
			LogPrintStream.getPs_thread_info().println("task "+rel+" random walk ends "
					+(debug_rw-start_time)+"ms");
		}
		
		MLNProcess.IndexQueryAndProduceDataPointTrain(queryToCount, dpList,
				indexFormula, formulaIndex);
		FilterFormulaByAtLeastOnceTrueForTrain();
		long debug_traindata=0;
		if(isDebug)
		{
			debug_traindata=System.currentTimeMillis();
			LogPrintStream.getPs_thread_info().println("task "+rel+" produce train data points ends "
					+(debug_traindata-debug_rw)+"ms");			
		}
		
		learner.IntiLearner(maxIterator,lamada,upsilon,C,K,z,Learner.rand,formulaIndex.size());
		learner.Learning(dpList);
		long debug_learn=0;
		if(isDebug)
		{
			debug_learn=System.currentTimeMillis();
			LogPrintStream.getPs_thread_info().println("task "+rel+" produce learn ends "
					+(debug_learn-debug_traindata)+"ms");
		}
		if(isDebug)
		{
			LogPrintStream.getPs_thread_info().println("task "+rel+" dpList'size:"
					+dpList.size()+" formula'size:"+formulaIndex.size());
		}
		dpList=new ArrayList<DataPoint>();
		queryToCount=new HashMap<HyperEdge,Map<HyperPath,TrueFalseCount>>();
		trainseedList=null;
		//learn over.
		isLearning=false;
		
		GeneSeedList();	
		RandomWalkProcess(testseedList);
		long debug_test_rw=0;
		if(isDebug)
		{
			debug_test_rw=System.currentTimeMillis();
			LogPrintStream.getPs_thread_info().println("task "+rel+" test random walk ends "+
					(debug_test_rw-debug_learn)+"ms");
		}
		
		IndexQueryAndProduceDataPointTest(queryToCount,queryList,dpList,indexFormula,formulaIndex);
		long debug_testdata=0;
		if(isDebug)
		{
			debug_testdata=System.currentTimeMillis();
			LogPrintStream.getPs_thread_info().println("task "+rel+" produce test data "
					+(debug_testdata-debug_test_rw)+"ms");
		}
		
		scores=InferenceTestTool.CalcularProbability(dpList,learner.getWeights());
		
		queryToCount=null;
		dpList=null;
		learner=null;
		testseedList=null;
		querySet=null;
		long end_time=System.currentTimeMillis();
		LogPrintStream.getPs_thread_info().println("One rel task "+rel
				+" ends at "+(end_time-start_time)+"ms");
		
		//直接把结果写出去///////
		PrintStream ps=new PrintStream(querydir+"/"+GlobalStaticData.relMapItoS.get(rel)
				+"/"+"results.pro");
		for(int j=0;j<scores.length;j++)
		{
			ps.println(scores[j]);
		}
		ps.close();
		//////////////////////
		}catch(Exception e)
		{
			e.printStackTrace(MultiSemanticLinkPrediction.pslog);
		}
		return null;
	}

}

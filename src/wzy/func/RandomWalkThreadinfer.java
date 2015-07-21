package wzy.func;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import wzy.infer.POG_typePathNode;
import wzy.infer.TypeNode;
import wzy.model.HyperNode;
import wzy.model.StatePOG;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;

public class RandomWalkThreadinfer implements Callable{
	
	private static Integer stateBufferSize=1000; 
	private Integer threadId;
	private Integer startPoint;
	private List<StatePOG> stateList;
	private Random rand=new Random();
	private StatePOG obj_nowState;
	private StatePOG obj_preState;
	private PrintStream ps_middle_path;
	
	public int maxround=GlobalStaticData.MaxRound;
	
	//for ensure the state containing query node
	private StatePOG ensurepog;
	
	public boolean trainortestflag=true;
	
	
	//by wzy, for no duplication states
	private Map<StatePOG,Set<Integer>> visitedStates=new HashMap<StatePOG,Set<Integer>>();

	public void setThreadId(Integer threadId) {
		this.threadId = threadId;
	}
	public void setStartPoint(Integer startPoint) {
		this.startPoint = startPoint;
	}
	public void setPs_middle_path(PrintStream ps_middle_path) {
		this.ps_middle_path = ps_middle_path;
	}
	/**
	 * initialize obj_nowState and obj_preState, it must run before random walk
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public void InitStates()
	{
		obj_preState=null;
		obj_nowState=new StatePOG();
		obj_nowState.state.add(startPoint);
		stateList=new ArrayList<StatePOG>();
	}
	/**
	 * initialize obj_nowState(from an existence state) and obj_preState, it must run before random walk
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param state an existent state as start point
	 */
	public void InitStates(StatePOG state)
	{
		obj_preState=null;
		obj_nowState=state;
		ensurepog=obj_nowState.copyOne();
		stateList=new ArrayList<StatePOG>();
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
	
	/**
	 * This function compute the numbers of four possible types of operations. They are removeAhead, removeBack,<br> 
	 * pushAhead, pushBack respectively, and they are corresponding to 1st,2nd,3th,4th of the return list. For random walk,<br>
	 * it accumulates them, r[0]=a[0], r[1]=r[0]+a[1], r[2]=r[1]+a[2], r[3]=r[2]+a[3] 
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param nowState
	 * @return the accumulation results r[]
	 */
	private Integer[] ComputeNextOperation(StatePOG nowState)
	{
		Integer[] nextOperation=new Integer[4];
		if(nowState.state.size()==1)  //the nowState has only one node.
		{
			nextOperation[0]=0;
			nextOperation[1]=0;
			nextOperation[2]=0;
			Integer nowStateEndNodeEntity=nowState.state.get(nowState.state.size()-1);
			HyperNode nowStateEndNode=GlobalStaticData.entityToNode.get(nowStateEndNodeEntity);
			nextOperation[3]=nowStateEndNode.neighbourList.size();
		}
		else if(nowState.state.size()>=GlobalStaticData.MaxLength) //The length of state is maxLength, it can only remove node
		{
			nextOperation[0]=1;
			nextOperation[1]=2;
			nextOperation[2]=2;
			nextOperation[3]=2;
		}
		else  //the nowSate has more than one node.
		{
			nextOperation[0]=1; 
			nextOperation[1]=1+nextOperation[0];
			
			Integer nowStateHeadNodeEntity=nowState.state.get(0);
			HyperNode nowStateHeadNode=GlobalStaticData.entityToNode.get(nowStateHeadNodeEntity);
			nextOperation[2]=nowStateHeadNode.neighbourList.size()+nextOperation[1];
			
			Integer nowStateEndNodeEntity=nowState.state.get(nowState.state.size()-1);
			HyperNode nowStateEndNode=GlobalStaticData.entityToNode.get(nowStateEndNodeEntity);
			nextOperation[3]=nowStateEndNode.neighbourList.size()+nextOperation[2];
		}		
		return nextOperation;
	}
	/**
	 * It choose a next state by random choose equiprobably, it must run after ComputeNextOperation for the parameter nowState.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param nowState the current state
	 * @param nextOperation the nextOperation List of nowState by running the ComputeNextOperation function.
	 * @return the next state
	 */
	private StatePOG RandomChooseNextState(StatePOG nowState, Integer[] nextOperation)
	{
		StatePOG nextState=null;
		Integer randIndex=Math.abs(rand.nextInt())%nextOperation[3]+1;
		int i;
		for(i=0;i<4;i++)
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
		}
		return nextState;
	}
	/**
	 * It choose a next state by random choose equiprobably, it must run after ComputeNextOperation for the parameter nowState.
	 * @author Zhuoyu Wei
	 * @version 1.2 Add the restart operation in the random walk.
	 * @param nowState the current state
	 * @param nextOperation the nextOperation List of nowState by running the ComputeNextOperation function.
	 * @return the next state
	 */
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
	/**
	 * 
	 * @param newState
	 */
	private void saveStateToList(StatePOG newState)
	{
		if(newState.state.size()>=GlobalStaticData.MinLength&&newState.state.size()<=GlobalStaticData.MaxLength)
		{
			stateList.add(newState);
			if(stateList.size()>=stateBufferSize)
			{
				OutputData.PrintStateList(ps_middle_path, stateList);
				stateList.clear();
			}
		}
	}
	
	/**
	 * The function realize the MHDA algorithm. It is a whole process for one time random walk from the current state<br>
	 * to the next state with the previous state compared.<br>
	 * In this function, it used the ps_RandomWalkThread_RandomWalk as the log output, you should initizate it in<br> 
	 * LogPrintStream.InitAllPrintStream function, and it is a synchronized field.
	 * @author Zhuoyu Wei
	 * @version 1.0 This is the 1.0 version, and it realize the MHDA algorithm.
	 * @param preState the previous state ,take care it may be null
	 * @param nowState the current state
	 * @return a boolean result, true value meams the process is successful, and false value means the process if failure.
	 */
	private boolean RandomWalkMHDA(StatePOG preState,StatePOG nowState)
	{

		Integer[] nextOperation=ComputeNextOperation(nowState);
		//Random walk process
		StatePOG nextState=null;
		if(nextOperation[3]==0)
		{
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("Error: A hypernode has 0 edges or neighbours.");
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\tThreadId:"+threadId+"\tHeadnode:"+nowState.state.get(0)
					+"\tEndnode:"+nowState.state.get(nowState.state.size()-1));
			return false; //Kill the current thread.
		}
		else
		{
			nextState=RandomChooseNextState(nowState,nextOperation);  //Choose a next state first round.
			
			Integer[] futureOperation=ComputeNextOperation(nextState);
			double randValue=rand.nextDouble();
			double threshold=Math.min(1.,(double)nextOperation[3]/(double)futureOperation[3]);
			if(randValue<=threshold)  //Change to a new State
			{
				if(nextState.equals(preState)&&nextOperation[3]>1)  //In the MHDA algorithm, it is the situation when i=k
				{
					StatePOG nextState2=nextState;
					while(nextState2.equals(preState))  //Choose a next state second round.
					{
						nextState2=RandomChooseNextState(nowState,nextOperation);
					}
					Integer[] futureOperation2=ComputeNextOperation(nextState2);
					double randValue2=rand.nextDouble();
					double threshold2=Math.min(1.,
							Math.min(1., 
									Math.pow((double)nextOperation[3]/(double)futureOperation2[3],2.)
									)
							*Math.max(1.,
									Math.pow((double)futureOperation[3]/(double)nextOperation[3],2.)
									)
								);
					if(randValue2<=threshold2)  // choose the second round result state as the next state making i != k
					{
						nextState=nextState2;
					}
					else // keep the next state making i == k
					{
						//do nothing...
					}
				}
				else  //It uses the ordinary MA algorithm
				{
					//do nothing...
				}
				obj_preState=nowState;
				obj_nowState=nextState;
				saveStateToList(obj_nowState);
			}
			else  //Stay in the actual state (nowState)
			{
				nextState=nowState;
			}
			
			
		}
		
		return true;
	}
	
	/**
	 * Check if a path of hypernode contains a node appearing more than once.
	 * @author Zhuoyu WEi
	 * @version 1.0
	 * @param nowState the state(path)
	 * @return a boolean value for the check. true is satisfied the hypothesis.
	 */
	private boolean CheckDuplicate(StatePOG nowState)
	{
		boolean flag=true;
		Set<Integer> set=new TreeSet<Integer>();
		for(int i=0;i<nowState.state.size();i++)
		{
			if(set.contains(nowState.state.get(i)))
			{
				flag=false;
				break;
			}
			set.add(nowState.state.get(i));
		}
		return flag;
	}	
	
	/**
	 * A ordinary random walk, set the Q(i,j) as following:<br>
	 * if the state's length is 1, it can't remove node, and the two remove operations has 0 probability.<br>
	 * else each push operation has the same probability, and set 1/4 as the probability for the two remove operations.<br>   
	 * @author Zhuoyu Wei
	 * @version 1.1 Simple the MH, change the Q(i,j). 
	 * @param nowState The state in this step.
	 * @return a boolean value whether walk to next.
	 * @deprecated
	 */
	private boolean RandomWalkOrdinary(StatePOG nowState)
	{
		//Set the nextOperation List
		Integer[] nextOperation=new Integer[4];
		
		Integer nowStateHeadNodeEntity=nowState.state.get(0);
		HyperNode nowStateHeadNode=GlobalStaticData.entityToNode.get(nowStateHeadNodeEntity);
		nextOperation[2]=nowStateHeadNode.neighbourList.size();
		
		Integer nowStateEndNodeEntity=nowState.state.get(nowState.state.size()-1);
		HyperNode nowStateEndNode=GlobalStaticData.entityToNode.get(nowStateEndNodeEntity);
		nextOperation[3]=nowStateEndNode.neighbourList.size();
		if(nowState.state.size()<=1)
		{
			nextOperation[0]=0;
			nextOperation[1]=0;
		}
		else
		{
			int sum=nextOperation[2]+nextOperation[3];
			nextOperation[0]=sum/2;
			nextOperation[1]=sum;
			nextOperation[2]+=nextOperation[1];
			nextOperation[3]+=nextOperation[2];
		}
		
		//Random walk process
		StatePOG nextState=null;
		if(nextOperation[3]==0)
		{
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("Error: A hypernode has 0 edges or neighbours.");
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\tThreadId:"+threadId+"\tHeadnode:"+nowState.state.get(0)
					+"\tEndnode:"+nowState.state.get(nowState.state.size()-1));
			return false; //Kill the current thread.
		}
		else
		{
			while(true)
			{
				nextState=RandomChooseNextState(nowState,nextOperation);
				if(nextState.state.size()<=GlobalStaticData.MaxLength&&CheckDuplicate(nextState))
				{
					break;
				}
			}
			//MH's mechanism has a little problem, more to larger state with more nodes.
			obj_nowState=nextState;
			saveStateToList(obj_nowState);
		}
		
		return true;
	}
	
	
	
	/**
	 * A ordinary random walk, set the Q(i,j) as following:<br>
	 * if the state's length is 1, it can't remove node, and the two remove operations has 0 probability.<br>
	 * else each push operation has the same probability, and set 1/4 as the probability for the two remove operations.<br>   
	 * @author Zhuoyu Wei
	 * @version 1.2 Simple the MH, change the Q(i,j). And add the probability for restart.
	 * @param nowState The state in this step.
	 * @return a boolean value whether walk to next.
	 */
	private boolean RandomWalkOrdinaryOneDirect(StatePOG nowState,double restartProbability)
	{
		//Set the nextOperation List
		
		
		
		Integer[] nextOperation=new Integer[3];
		
		
		Integer nowStateEndNodeEntity=nowState.state.get(nowState.state.size()-1);
		HyperNode nowStateEndNode=GlobalStaticData.entityToNode.get(nowStateEndNodeEntity);
		nextOperation[1]=nowStateEndNode.neighbourList.size();
	
		
		if(nowState.state.size()<=1)
		{
			nextOperation[0]=0;
			//nextOperation[1]=0;
		}
		else
		{

			nextOperation[0]=nextOperation[1];

		}
		nextOperation[1]+=nextOperation[0];	
		//nextOperation[2]=(int)(nextOperation[1]*(1.+restartProbability));
		nextOperation[2]=nextOperation[1];
		//System.out.println(nextOperation[0]+"\t"+(nextOperation[1])
				//+"\t"+(nextOperation[2]));
		
		
		/*for(int i=0;i<nowState.state.size();i++)
		{
			LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print(nowState.state.get(i)+"\t");
		}
		LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print("\t***\t");
		for(int i=0;i<5;i++)
		{
			LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print(nextOperation[i]+"\t");
		}
		LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println();*/
		
		//Random walk process
		StatePOG nextState=null;
		if(nextOperation[2]==0)
		{
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("Error: A hypernode has 0 edges or neighbours.");
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\tThreadId:"+threadId+"\tHeadnode:"+nowState.state.get(0)
					+"\tEndnode:"+nowState.state.get(nowState.state.size()-1));
			return false; //Kill the current thread.
		}
		else
		{
			while(true)
			{
				//System.out.println("n"+nowState.state);
				nextState=RandomChooseNextStateAdvanceOneDirect(nowState,nextOperation);
				if(nextState.state.size()<=GlobalStaticData.MaxLength&&CheckDuplicate(nextState))
				{
					//System.out.println(nextState.state);
					break;
					
				}
			}
			//MH's mechanism has a little problem, more to larger state with more nodes.
			obj_nowState=nextState;
			
			saveStateToList(obj_nowState);
		}
		
		return true;
	}	
	
	
	/**
	 * It choose a next state by random choose equiprobably, it must run after ComputeNextOperation for the parameter nowState.
	 * @author Zhuoyu Wei
	 * @version 1.2 Add the restart operation in the random walk.
	 * @param nowState the current state
	 * @param nextOperation the nextOperation List of nowState by running the ComputeNextOperation function.
	 * @return the next state
	 */
	private StatePOG RandomChooseNextStateAdvanceOneDirect(StatePOG nowState, Integer[] nextOperation)
	{
		StatePOG nextState=null;
		Integer randIndex=Math.abs(rand.nextInt())%nextOperation[2]+1;
		//System.out.println(randIndex+"\t"+nextOperation[2]);
		int i;
		for(i=0;i<3;i++)
		{
			if(randIndex<=nextOperation[i])
			{
				break;
			}
		}

		switch(i)
		{
		case 0:{
			nextState=removeBack(nowState);
			break;
		}
		case 1:{
			Integer nowStateEndNodeEntity=nowState.state.get(nowState.state.size()-1);
			HyperNode nowStateEndNode=GlobalStaticData.entityToNode.get(nowStateEndNodeEntity);
			//System.out.println("i"+nowState.state);
			nextState=pushBack(nowState,nowStateEndNode.neighbourList.get(randIndex-nextOperation[0]-1));
			//System.out.println("i"+nextState.state);			
			//System.out.println("push: "+(randIndex-nextOperation[0]-1)+"\t"+nextState.state.size());
			break;
		}
		case 2: //restart
			{
			nextState=new StatePOG();
			nextState.state.add(Math.abs(rand.nextInt()%GlobalStaticData.entityToNode.size()));
		}
		}
		return nextState;
	}	
	
	
	
	/**
	 * A ordinary random walk, set the Q(i,j) as following:<br>
	 * if the state's length is 1, it can't remove node, and the two remove operations has 0 probability.<br>
	 * else each push operation has the same probability, and set 1/4 as the probability for the two remove operations.<br>   
	 * @author Zhuoyu Wei
	 * @version 1.2 Simple the MH, change the Q(i,j). And add the probability for restart.
	 * @param nowState The state in this step.
	 * @return a boolean value whether walk to next.
	 */
	private boolean RandomWalkOrdinary(StatePOG nowState,double restartProbability)
	{
		//Set the nextOperation List
		Integer[] nextOperation=new Integer[5];
		
		Integer nowStateHeadNodeEntity=nowState.state.get(0);
		HyperNode nowStateHeadNode=GlobalStaticData.entityToNode.get(nowStateHeadNodeEntity);
		nextOperation[2]=nowStateHeadNode.neighbourList.size();
		
		Integer nowStateEndNodeEntity=nowState.state.get(nowState.state.size()-1);
		HyperNode nowStateEndNode=GlobalStaticData.entityToNode.get(nowStateEndNodeEntity);
		nextOperation[3]=nowStateEndNode.neighbourList.size();
		
		if(nowState.state.size()<=1)
		{
			nextOperation[0]=0;
			nextOperation[1]=0;
		}
		else
		{
			int sum=nextOperation[2]+nextOperation[3];
			nextOperation[0]=sum/2;
			nextOperation[1]=sum;
		}
		nextOperation[2]+=nextOperation[1];
		nextOperation[3]+=nextOperation[2];		
		//remove restart
		//nextOperation[4]=(int)(nextOperation[3]*(1.+restartProbability));
		nextOperation[4]=nextOperation[3];
		
		/*for(int i=0;i<nowState.state.size();i++)
		{
			LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print(nowState.state.get(i)+"\t");
		}
		LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print("\t***\t");
		for(int i=0;i<5;i++)
		{
			LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print(nextOperation[i]+"\t");
		}
		LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println();*/
		
		//Random walk process
		StatePOG nextState=null;
		if(nextOperation[4]==0)
		{
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("Error: A hypernode has 0 edges or neighbours.");
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\tThreadId:"+threadId+"\tHeadnode:"+nowState.state.get(0)
					+"\tEndnode:"+nowState.state.get(nowState.state.size()-1));
			return false; //Kill the current thread.
		}
		else
		{
			while(true)
			{
				nextState=RandomChooseNextStateAdvance(nowState,nextOperation);
				if(nextState.state.size()<=GlobalStaticData.MaxLength&&CheckDuplicate(nextState))
				{
					break;
				}
			}
			//MH's mechanism has a little problem, more to larger state with more nodes.
			obj_nowState=nextState;
			saveStateToList(obj_nowState);
		}
		
		return true;
	}	
	
	
	/**
	 * A ordinary random walk, set the Q(i,j) as following:<br>
	 * if the state's length is 1, it can't remove node, and the two remove operations has 0 probability.<br>
	 * else each push operation has the same probability, and set 1/4 as the probability for the two remove operations.<br>   
	 * @author Zhuoyu Wei
	 * @version 1.3 Simple the MH, change the Q(i,j). No restart.
	 * @param nowState The state in this step.
	 * @return a boolean value whether walk to next.
	 */
	private boolean RandomWalkRemVisited(StatePOG nowState,int attenuation,double restartProbability)
	{
		StatePOG nextState=null;
		
		//Random choose sub or super operator
		double suborsuper=Math.abs(rand.nextDouble());
		Integer nowStateEndNodeEntity=nowState.state.get(nowState.state.size()-1);
		HyperNode nowStateEndNode=GlobalStaticData.entityToNode.get(nowStateEndNodeEntity);
		List<Integer> neighbourList=nowStateEndNode.neighbourList;
		if(nowState.state.size()>=GlobalStaticData.MaxLength||suborsuper<0.5)  //sub
		{
			//System.out.println("Sub pog: "+nowState.state.size());
			nextState=removeBack(nowState);
		}
		else  //super
		{
			
			
			Set<Integer> visitedSet=visitedStates.get(nowState);
			
			List<Integer> randList=new ArrayList();
			
			if(visitedSet==null)
			{

				visitedSet=new HashSet<Integer>();
				visitedStates.put(nowState, visitedSet);
			}
			if(visitedSet.size()==0)
			{
				for(int i=0;i<neighbourList.size();i++)
				{
					randList.add(neighbourList.get(i));
				}
			}
			else
			{
				for(int i=0;i<neighbourList.size();i++)
				{
					if(visitedSet.contains(neighbourList.get(i)))
					{
						randList.add(neighbourList.get(i));
					}
					else
					{
						for(int j=0;j<attenuation;j++)
						{
							randList.add(neighbourList.get(i));
						}
					}
				}
			}
			int countwhere=0;
			int countbreak=0;
			while(true)
			{
				//System.out.println("where count: "+countwhere);
				countwhere++;
				Integer randint=Math.abs(rand.nextInt())%randList.size();
				nextState=pushBack(nowState,randList.get(randint));
				
				//System.out.println("where count: "+countwhere+" "+nextState.state.size()+" "+randList.size());
				if(nextState.state.size()<=GlobalStaticData.MaxLength&&CheckDuplicate(nextState))
				{
					Integer lastnode=nextState.state.get(nextState.state.size()-1);
					visitedSet.add(lastnode);
					break;
				}
				else
				{
					countbreak++;
					if(countbreak>5)
					{
						//System.out.println("Break pog: "+nowState.state.size());
						nextState=removeBack(nowState);
						break;
					}		
				}
			}

		}
		
		//MH, due to direct random walk, the last node of state is must be the node resulted from the new operatiotn. 
		HyperNode newnode=GlobalStaticData.entityToNode.get(nextState.state.get(nextState.state.size()-1));
		double rate=1.;
		if(newnode.neighbourList.size()<nowStateEndNode.neighbourList.size())
		{
			rate=(double)newnode.neighbourList.size()/(double)nowStateEndNode.neighbourList.size();
		}
		double mh_rand=Math.abs(rand.nextDouble());
		if(mh_rand<rate)
		{
			obj_nowState=nextState;
			if(this.visitedStates.get(obj_nowState)==null)
				saveStateToList(obj_nowState);
		}
		


		return true;

	}	
	
	
	
	public boolean CheckContainQuery(StatePOG statepog)
	{
		int i;
		for(i=0;i<statepog.state.size();i++)
		{
			if(statepog.state.get(i).equals(ensurepog.state.get(0)))
				break;
		}
		if(i+ensurepog.state.size()>statepog.state.size())
			return false;
		boolean flag=true;
		for(int j=0;j<ensurepog.state.size();j++)
		{
			if(!statepog.state.get(i+j).equals(ensurepog.state.get(j)))
			{
				flag=false;
				break;
			}
		}
		return flag;
	}
	
	
	/**
	 * A ordinary random walk, set the Q(i,j) as following:<br>
	 * if the state's length is 1, it can't remove node, and the two remove operations has 0 probability.<br>
	 * else each push operation has the same probability, and set 1/4 as the probability for the two remove operations.<br>   
	 * @author Zhuoyu Wei
	 * @version 1.2 Simple the MH, change the Q(i,j). And add the probability for restart.
	 * @param nowState The state in this step.
	 * @return a boolean value whether walk to next.
	 */
	private boolean RandomWalkOrdinaryWithCheck(StatePOG nowState,double restartProbability)
	{
		//Set the nextOperation List
		Integer[] nextOperation=new Integer[5];
		
		Integer nowStateHeadNodeEntity=nowState.state.get(0);
		HyperNode nowStateHeadNode=GlobalStaticData.entityToNode.get(nowStateHeadNodeEntity);
		nextOperation[2]=nowStateHeadNode.neighbourList.size();
		
		Integer nowStateEndNodeEntity=nowState.state.get(nowState.state.size()-1);
		HyperNode nowStateEndNode=GlobalStaticData.entityToNode.get(nowStateEndNodeEntity);
		nextOperation[3]=nowStateEndNode.neighbourList.size();
		
		if(nowState.state.size()<=1)
		{
			nextOperation[0]=0;
			nextOperation[1]=0;
		}
		else
		{
			int sum=nextOperation[2]+nextOperation[3];
			nextOperation[0]=sum/2;
			nextOperation[1]=sum;
		}
		nextOperation[2]+=nextOperation[1];
		nextOperation[3]+=nextOperation[2];		
		//remove restart
		//nextOperation[4]=(int)(nextOperation[3]*(1.+restartProbability));
		nextOperation[4]=nextOperation[3];
		
		/*for(int i=0;i<nowState.state.size();i++)
		{
			LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print(nowState.state.get(i)+"\t");
		}
		LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print("\t***\t");
		for(int i=0;i<5;i++)
		{
			LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().print(nextOperation[i]+"\t");
		}
		LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println();*/
		
		//Random walk process
		StatePOG nextState=null;
		if(nextOperation[4]==0)
		{
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("Error: A hypernode has 0 edges or neighbours.");
			LogPrintStream.getPs_RandomWalkThread_RandomWalk().println("\tThreadId:"+threadId+"\tHeadnode:"+nowState.state.get(0)
					+"\tEndnode:"+nowState.state.get(nowState.state.size()-1));
			return false; //Kill the current thread.
		}
		else
		{
			while(true)
			{
				nextState=RandomChooseNextStateAdvance(nowState,nextOperation);
				if(nextState.state.size()<=GlobalStaticData.MaxLength&&CheckDuplicate(nextState))
				{
					break;
				}
			}
			//MH's mechanism has a little problem, more to larger state with more nodes.
			obj_nowState=nextState;
			//if(!CheckContainQuery(obj_nowState))
				//return false;
			saveStateToList(obj_nowState);
		}
		
		return true;
	}	
	
	
	public Integer ConceptStateToTypePath(StatePOG state)
	{
		POG_typePathNode pogNode=new POG_typePathNode();
		for(int i=0;i<state.state.size();i++)
		{
			HyperNode hn=GlobalStaticData.entityToNode.get(state.state.get(i));
			pogNode.nodeList.add(hn.type);
		}
		return GlobalStaticData.pogNodeToIndex.get(pogNode);
	}
	public boolean RandomWalkInfer(StatePOG nowState)
	{
		double randvalue=Math.abs(rand.nextDouble());
		if(nowState.state.size()>1&&randvalue>0.5)
		{
			if(nowState.state.size()==1)
			{
				this.obj_nowState=new StatePOG();
				return true;
			}
			randvalue=Math.abs(rand.nextDouble());
			if(randvalue>0.5)
			{
				this.obj_nowState=this.removeAhead(nowState);
			}
			else
			{
				this.obj_preState=this.removeBack(nowState);
			}
			return true;
		}
		
		if(nowState.state.size()>=GlobalStaticData.MaxLength)
			return true;
		
		Integer pogNodeIndex=this.ConceptStateToTypePath(nowState);
/*		for(int d=0;d<nowState.state.size();d++)
		{
			HyperNode hn=GlobalStaticData.entityToNode.get(nowState.state.get(d));
			System.out.print(hn.type+"("+GlobalStaticData.typeMapItoS.get(hn.type)+")\t");
		}
		System.out.println();*/
		//If this pogNode is not in GlobalList, it may be caught into infinite loop		
		if(pogNodeIndex>=GlobalStaticData.indexToPogNode.size())
			return true;
		POG_typePathNode pogNode=GlobalStaticData.indexToPogNode.get(pogNodeIndex);
		List<StatePOG> candidateList=new ArrayList<StatePOG>();
		for(int i=0;i<pogNode.superEdgeList.size();i++)
		{
			Integer state=pogNode.superEdgeList.get(i).state;
			Integer type=pogNode.superEdgeList.get(i).type;
			
			switch(state)
			{
			case 3:
			{
				HyperNode aheadHn=GlobalStaticData.entityToNode.get(nowState.state.get(0));
				List<Integer> aheadNodeList=aheadHn.typeToNextNodeList.get(type);
				if(aheadNodeList!=null)
				{
					for(int j=0;j<aheadNodeList.size();j++)
					{
						StatePOG nextTempState=this.pushAhead(nowState, aheadNodeList.get(j));
						candidateList.add(nextTempState);
					}
				}
				break;
			}
			case 4:
			{
				HyperNode backHn=GlobalStaticData.entityToNode.get(nowState.state.get(nowState.state.size()-1));
				List<Integer> backNodeList=backHn.typeToNextNodeList.get(type);
				if(backNodeList!=null)
				{
					for(int j=0;j<backNodeList.size();j++)
					{
						StatePOG nextTempState=this.pushBack(nowState, backNodeList.get(j));
						candidateList.add(nextTempState);
					}
				}
				break;
			}
			}
			
		}
		
		//Get all Candidate States for random
		if(candidateList.size()<=0)
			return true;
		int randone=Math.abs(rand.nextInt())%candidateList.size();
		this.obj_nowState=candidateList.get(randone);
		
		
		if(obj_nowState.state.size()>=GlobalStaticData.MinLength&&
				obj_nowState.state.size()<=GlobalStaticData.MaxLength)
		{
			saveStateToList(obj_nowState);
		}
		
		
		return true;
		
		
				
		/*Integer firstNodeIndex=nowState.state.get(0);
		Integer endNodeIndex=nowState.state.get(nowState.state.size()-1);
		
		
		HyperNode firstNode=GlobalStaticData.entityToNode.get(firstNodeIndex);
		HyperNode endNode=GlobalStaticData.entityToNode.get(endNodeIndex);
		
		
		
		
		
		TypeNode firstTypeNode=GlobalStaticData.typeNodeList[firstNode.type];
		List<Integer> firstNeighbour=firstNode.neighbourList;
		List<Double> firstValueList=new ArrayList<Double>();
		Double firstlastvalue=0.;
		for(int i=0;i<firstNeighbour.size();i++)
		{
			Integer neighbourIndex=firstNeighbour.get(i);
			HyperNode neighbourNode=GlobalStaticData.entityToNode.get(neighbourIndex);
			Integer neighbourType=neighbourNode.type;
			Double neighbourvalue=firstTypeNode.nextToValue.get(neighbourType);
			if(neighbourvalue==null)
			{
				continue;
			}
			else
			{
				firstValueList.add(firstlastvalue+neighbourvalue);
				firstlastvalue+=neighbourvalue;
			}
		}
		
		
	
		
		return true;*/
	}
	
	
	
	
	/**
	 * The callable override function call()<br>
	 * In this function, it use the version 1.1 RandomWalkOrdinary as the mechanism of random walk.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		LogPrintStream.getPs_thread_info().println("Thread "+threadId+" is started!");
		long startTimePoint=System.currentTimeMillis();
		
		//InitStates();
		LogPrintStream.getPs_thread_info().println(("Thread "+threadId+"init"));
		//System.out.println("START:\t"+this.startPoint+"\t"+GlobalStaticData.entityMapItoS.get(this.startPoint));
		for(int i=0;i<maxround;i++)
		{
			try{
				//boolean flag=RandomWalkRemVisited(obj_nowState,5,GlobalStaticData.restartProbability);
				boolean flag;
/*				if(trainortestflag)
					flag=RandomWalkOrdinary(obj_nowState,GlobalStaticData.restartProbability);
				else
					flag=RandomWalkOrdinaryWithCheck(obj_nowState,GlobalStaticData.restartProbability);
				//boolean flag=RandomWalkOrdinaryOneDirect(obj_nowState,GlobalStaticData.restartProbability);
*/			
				flag=RandomWalkInfer(obj_nowState);

				if(!flag) //This thread has error, and stop it;
					break;
				
				
				
			}catch(Exception e)
			{
				e.printStackTrace(LogPrintStream.getPs_thread_info());
			}
			
		}
		OutputData.PrintStateList(ps_middle_path, stateList);
		stateList.clear();
		
		long endTimePoint=System.currentTimeMillis();
		LogPrintStream.getPs_thread_info().println("Thread "+threadId+" is end in "+(endTimePoint-startTimePoint)+"ms");
		
		
		return null;
	}	
}

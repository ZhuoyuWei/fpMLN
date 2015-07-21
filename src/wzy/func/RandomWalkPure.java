package wzy.func;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import wzy.model.HyperNode;
import wzy.model.StatePOG;
import wzy.stat.GlobalStaticData;

public class RandomWalkPure {

	public PrintStream psPath;
	public StatePOG obj_state=new StatePOG();
	public Set<StatePOG> memorystateset;
	public Integer numnodes;
	public Random rand=new Random();
	
	public int MaxLength=5;
	public double restart_pro=0.1;
	public boolean[] flag;
	
	public int stopThrethold=0;
	public long printcount=0;
	public long randtime=0;
	public long accepttime=0;
	
	
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
	
	public int ComputeDegree(StatePOG state)
	{
		int sum=0;
		
		if(state.state.size()==0)
			return this.numnodes;
		if(state.state.size()==1)
			sum+=1;
		else
			sum+=2;
		Integer first=state.state.get(0);
		HyperNode firstNode=GlobalStaticData.entityToNode.get(first);
		sum+=firstNode.neighbourList.size();
		
		Integer last=state.state.get(state.state.size()-1);
		HyperNode lastNode=GlobalStaticData.entityToNode.get(last);
		sum+=lastNode.neighbourList.size();
		
		return sum;
	}
	
	public void RandOnceMH()
	{
		double randvalue=rand.nextDouble();
		double pro=(1-this.restart_pro)/2.;
		StatePOG new_state=null;
		if(randvalue<this.restart_pro)  //restart
		{
			for(int i=0;i<obj_state.state.size();i++)
			{
				flag[obj_state.state.get(i)]=false;
			}
			obj_state.state.clear();
			return;
		}
		else if(obj_state.state.size()>=MaxLength||randvalue<(this.restart_pro+pro)) //reduce
		{
			randvalue=rand.nextDouble();
			if(randvalue<0.5) //left
			{
				Integer firstNodeIndex=obj_state.state.get(0);
				new_state=this.removeAhead(obj_state);
				flag[firstNodeIndex]=false;
			}
			else //right
			{
				Integer lastNodeIndex=obj_state.state.get(obj_state.state.size()-1);
				new_state=this.removeBack(obj_state);
				flag[lastNodeIndex]=false;
			}
		}
		else  //addition
		{
			//need keep no loop
			randvalue=rand.nextDouble();
			if(randvalue<0.5) //left
			{
				Integer firstNodeIndex=obj_state.state.get(0);
				HyperNode firstNode=GlobalStaticData.entityToNode.get(firstNodeIndex);
				
				List<Integer> alternativeList=new ArrayList<Integer>();
				for(int i=0;i<firstNode.neighbourList.size();i++)
				{
					if(!flag[firstNode.neighbourList.get(i)])
					{
						alternativeList.add(firstNode.neighbourList.get(i));
					}
				}
				
				if(alternativeList.size()==0)
				{
					//re calculate pros
					double restart=this.restart_pro/(this.restart_pro+pro);
					randvalue=rand.nextDouble();
					if(randvalue<restart) //restart
					{
						for(int i=0;i<obj_state.state.size();i++)
						{
							flag[obj_state.state.get(i)]=false;
						}
						obj_state.state.clear();
						return;
					}
					else //reduce
					{
						randvalue=rand.nextDouble();
						if(randvalue<0.5) //left
						{
							firstNodeIndex=obj_state.state.get(0);
							new_state=this.removeAhead(obj_state);
							flag[firstNodeIndex]=false;
						}
						else //right
						{
							Integer lastNodeIndex=obj_state.state.get(obj_state.state.size()-1);
							new_state=this.removeBack(obj_state);
							flag[lastNodeIndex]=false;
						}
					}
				}
				else  //add
				{
					int randnext=Math.abs(rand.nextInt())%alternativeList.size();
					new_state=this.pushAhead(obj_state, alternativeList.get(randnext));
					flag[alternativeList.get(randnext)]=true;
				}

			}
			else //right
			{
				Integer lastNodeIndex=obj_state.state.get(obj_state.state.size()-1);
				HyperNode lastNode=GlobalStaticData.entityToNode.get(lastNodeIndex);
				
				List<Integer> alternativeList=new ArrayList<Integer>();
				for(int i=0;i<lastNode.neighbourList.size();i++)
				{
					if(!flag[lastNode.neighbourList.get(i)])
					{
						alternativeList.add(lastNode.neighbourList.get(i));
					}
				}
				
				if(alternativeList.size()==0)
				{
					//re calculate pros
					double restart=this.restart_pro/(this.restart_pro+pro);
					randvalue=rand.nextDouble();
					if(randvalue<restart) //restart
					{
						for(int i=0;i<obj_state.state.size();i++)
						{
							flag[obj_state.state.get(i)]=false;
						}
						obj_state.state.clear();
						return;
					}
					else //reduce
					{
						randvalue=rand.nextDouble();
						if(randvalue<0.5) //left
						{
							Integer firstNodeIndex=obj_state.state.get(0);
							new_state=this.removeAhead(obj_state);
							flag[firstNodeIndex]=false;
						}
						else //right
						{
							lastNodeIndex=obj_state.state.get(obj_state.state.size()-1);
							new_state=this.removeBack(obj_state);
							flag[lastNodeIndex]=false;
						}
					}
				}
				else  //add
				{
					int randnext=Math.abs(rand.nextInt())%alternativeList.size();
					new_state=this.pushBack(obj_state, alternativeList.get(randnext));
					flag[alternativeList.get(randnext)]=true;
				}
			}
		}
		
		//MH
		
		int dXt=ComputeDegree(obj_state);
		int dj=ComputeDegree(new_state);
		
		randvalue=rand.nextDouble();
		if(randvalue<Math.min(1, (double)dXt/dj))
		{
			obj_state=new_state;
			if(obj_state.state.size()>1)
			{
				this.memorystateset.add(obj_state);
				this.PrintAState(psPath, obj_state);
				accepttime++;
			}
		}
		else
		{
			//recover flag list
			for(int i=0;i<new_state.state.size();i++)
			{
				flag[new_state.state.get(i)]=false;
			}
			for(int i=0;i<obj_state.state.size();i++)
			{
				flag[obj_state.state.get(i)]=true;
			}
		}
		
		printcount++;
		if(printcount%100000==0)
		{
			System.out.println(this.memorystateset.size());
		}
		
		
	}
	
	
	public void PrintAState(PrintStream ps,StatePOG state)
	{
		for(int i=0;i<state.state.size();i++)
		{
			ps.print(state.state.get(i)+"\t");
		}
		ps.println();
	}
	
	public void StartRandom()
	{
		flag=new boolean[this.numnodes];
		while(true)
		{
			if(obj_state.state.size()==0)
			{
				Integer startNodeIndex=Math.abs(rand.nextInt())%numnodes;
				obj_state.state.add(startNodeIndex);
				flag[startNodeIndex]=true;
			}
			RandOnceMH();
			
			if(this.memorystateset.size()>=this.stopThrethold)
				break;
			randtime++;
			
		}
		
		System.out.println("End rand: "+randtime+"\t"+accepttime+"\t"+this.memorystateset.size());
		
	}
	
	
	
}

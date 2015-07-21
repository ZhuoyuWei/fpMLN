package wzy.infer.KBC;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map;

import wzy.func.MLNProcess;
import wzy.func.OutputData;
import wzy.infer.K_PathEdge;
import wzy.infer.K_PathGraph;
import wzy.infer.K_PathNode;
import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.model.HyperPath;
import wzy.model.StatePOG;
import wzy.model.TrueFalseCount;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;

public class RandomWalkForKBCinfer {

	public Integer threadId;
	public Integer startPoint;
	public List<StatePOG> stateList;
	public Random rand=new Random();
	public PrintStream ps_middle_path;
	
	public int maxround=GlobalStaticData.MaxRound;
	public int queryId=GlobalStaticData.query;
	
	//for ensure the state containing query node
	public StatePOG ensurepog;
	
	public boolean trainortestflag=true;
	
	public Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount;
	

	//For Kgraph
	public K_PathGraph kgraph;
	public K_PathNode nowState;
	public K_PathNode obj_preState;
	
	public boolean InitFirstNode()
	{
		nowState=new K_PathNode();
		HyperNode hnode=GlobalStaticData.entityToNode.get(startPoint);
		List<HyperEdge> edgeList=new ArrayList<HyperEdge>();
		for(int i=0;i<hnode.neighbourList.size();i++)
		{
			List<HyperEdge> edges=hnode.edges.get(hnode.neighbourList.get(i));
			if(edges==null)
				continue;
			edgeList.addAll(edges);
		}
		if(edgeList.size()==0)
			return false;
		int icount=0;
		K_PathNode mapStartKnode=null;
		K_PathNode startKnode=null;
		while(icount<10)
		{
			int index=Math.abs(rand.nextInt())%edgeList.size();
			HyperEdge startEdge=edgeList.get(index);
			HyperPath startPath=new HyperPath();
			startPath.path.add(startEdge);
			startKnode=new K_PathNode();
			startKnode.path=startPath;
			K_PathNode conceptualStartNode=new K_PathNode();
			conceptualStartNode.path=MLNProcess.Conceptualize(startPath);
			
			mapStartKnode=kgraph.nodeMap.get(conceptualStartNode);
			if(mapStartKnode!=null)
			{
				break;
			}
			icount++;
		}
		if(mapStartKnode==null)
		{
			int startindex=Math.abs(rand.nextInt())%edgeList.size();
			for(int i=0;i<edgeList.size();i++)
			{
				HyperEdge startEdge=edgeList.get((i+startindex)%edgeList.size());
				HyperPath startPath=new HyperPath();
				startPath.path.add(startEdge);
				startKnode=new K_PathNode();
				startKnode.path=startPath;
				K_PathNode conceptualStartNode=new K_PathNode();
				conceptualStartNode.path=MLNProcess.Conceptualize(startPath);
			
				mapStartKnode=kgraph.nodeMap.get(conceptualStartNode);
				if(mapStartKnode!=null)
				{
					break;
				}
			}
		}
		if(mapStartKnode==null)
		{
			return false;
		}
		else
		{
			nowState=startKnode;
			return true;
		}
	}
	
	public boolean RandomWalkOnKGraph(K_PathNode nowState,double restartProbability)
	{
		
		
		double randvalue=rand.nextDouble();
		//random sub nodes
		if(nowState.path.path.size()>1&&randvalue<0.5)
		{
			K_PathNode nextState=null;
			//random remove Ahead
			if(randvalue<0.25)
			{
				K_PathNode knode=nowState.RemoveAhead();
				K_PathNode conceptualKnode=new K_PathNode();
				conceptualKnode.path=MLNProcess.Conceptualize(knode.path);
				nextState=kgraph.nodeMap.get(conceptualKnode);
				if(nextState!=null)
				{
					nowState=knode;
					return true;
				}
			}
			//random and force remove back
			K_PathNode knode=nowState.RemoveBack();
			K_PathNode conceptualKnode=new K_PathNode();
			conceptualKnode.path=MLNProcess.Conceptualize(knode.path);
			nextState=kgraph.nodeMap.get(conceptualKnode);
			if(nextState!=null)
			{
				nowState=knode;
				return true;
			}
			
			//force remove ahead
			knode=nowState.RemoveAhead();
			conceptualKnode=new K_PathNode();
			conceptualKnode.path=MLNProcess.Conceptualize(knode.path);
			nextState=kgraph.nodeMap.get(conceptualKnode);
			if(nextState!=null)
			{
				nowState=knode;
				return true;
			}			
		}
		
		//random and force super nodes
		K_PathNode conceptualNow=new K_PathNode();
		conceptualNow.path=MLNProcess.Conceptualize(nowState.path);
		conceptualNow=kgraph.nodeMap.get(conceptualNow);
		if(conceptualNow==null)
		{
			return false;
		}
		//List<K_PathEdge> kedgeList=new ArrayList<K_PathEdge>();
		//kedgeList.addAll(conceptualNow.superEdgeListleft);
		//kedgeList.addAll(conceptualNow.superEdgeListright);
		List<HyperEdge> candidateEdgeList=new ArrayList<HyperEdge>();
		List<K_PathNode> candidateKnode=new ArrayList<K_PathNode>();
		for(int i=0;i<conceptualNow.superEdgeListleft.size();i++)
		{
			K_PathEdge kedge=conceptualNow.superEdgeListleft.get(i);
			Integer entity=kedge.isLeftAdded?nowState.path.path.get(0).nodeList.get(0)
					:nowState.path.path.get(0).nodeList.get(1);
			HyperNode hnode=GlobalStaticData.entityToNode.get(entity);
			List<HyperEdge> entityEdgeList=hnode.relToEdgeList.get(kedge.relOfAddedEdge);
			if(entityEdgeList==null)
				continue;
			for(int j=0;j<entityEdgeList.size();j++)
			{
				HyperEdge newEdge=entityEdgeList.get(j);
				if(newEdge.nodeList.get(kedge.nodeposition).equals(entity))
				{
					candidateEdgeList.add(newEdge);
					K_PathNode ksuperleft=nowState.PushAhead(newEdge);
					candidateKnode.add(ksuperleft);
				}
			}
		}
		for(int i=0;i<conceptualNow.superEdgeListright.size();i++)
		{
			K_PathEdge kedge=conceptualNow.superEdgeListright.get(i);
			int l=conceptualNow.path.path.size();
			Integer entity=kedge.isLeftAdded?conceptualNow.path.path.get(l-1).nodeList.get(0)
					:conceptualNow.path.path.get(l-1).nodeList.get(1);
			HyperNode hnode=GlobalStaticData.entityToNode.get(entity);
			List<HyperEdge> entityEdgeList=hnode.relToEdgeList.get(kedge.relOfAddedEdge);
			if(entityEdgeList==null)
				continue;
			for(int j=0;j<entityEdgeList.size();j++)
			{
				HyperEdge newEdge=entityEdgeList.get(j);
				if(newEdge.nodeList.get(kedge.nodeposition).equals(entity))
				{
					candidateEdgeList.add(newEdge);
					K_PathNode ksuperright=nowState.PushBack(newEdge);
					candidateKnode.add(ksuperright);
				}
			}
		}		
		
		//force remove
		if(nowState.path.path.size()>1&&candidateKnode.size()==0)
		{
			K_PathNode nextState=null;
			//random remove Ahead
			if(randvalue<0.25)
			{
				K_PathNode knode=nowState.RemoveAhead();
				K_PathNode conceptualKnode=new K_PathNode();
				conceptualKnode.path=MLNProcess.Conceptualize(knode.path);
				nextState=kgraph.nodeMap.get(conceptualKnode);
				if(nextState!=null)
				{
					nowState=knode;
					return true;
				}
			}
			//random and force remove back
			K_PathNode knode=nowState.RemoveBack();
			K_PathNode conceptualKnode=new K_PathNode();
			conceptualKnode.path=MLNProcess.Conceptualize(knode.path);
			nextState=kgraph.nodeMap.get(conceptualKnode);
			if(nextState!=null)
			{
				nowState=knode;
				return true;
			}
			
			//force remove ahead
			knode=nowState.RemoveAhead();
			conceptualKnode=new K_PathNode();
			conceptualKnode.path=MLNProcess.Conceptualize(knode.path);
			nextState=kgraph.nodeMap.get(conceptualKnode);
			if(nextState!=null)
			{
				nowState=knode;
				return true;
			}			
		}		
		
		//init knode
		if(candidateKnode.size()==0)
		{
			InitFirstNode();
		}
		else
		{
			int randnum=Math.abs(rand.nextInt())%candidateKnode.size();
			K_PathNode knewState=candidateKnode.get(randnum);
			
			//for check
			K_PathNode conceptualKnew=new K_PathNode();
			conceptualKnew.path=MLNProcess.Conceptualize(knewState.path);
			conceptualKnew=kgraph.nodeMap.get(conceptualKnew);
			if(conceptualKnew==null)
				return true;
			nowState=knewState;
			if(conceptualKnew.finalEdge!=null)
			{
				List<HyperPath> entityFormulaList=GeneFormula(nowState,conceptualKnew.finalEdge);
				if(entityFormulaList!=null&&entityFormulaList.size()>0)
				{
					for(int i=0;i<entityFormulaList.size();i++)
						CountFormulasForQuery(entityFormulaList.get(i));
				}
			}
		}
		return true;
	}
	
	public void CountFormulasForQuery(HyperPath entityFormula)
	{
		HyperPath conceptualFormula=MLNProcess.Conceptualize(entityFormula);
		Integer index=GlobalStaticData.conceptFormulaToIndex.get(conceptualFormula);
		if(index==null)
			return;
		for(int i=0;i<entityFormula.path.size();i++)
		{
			if(entityFormula.path.get(i).rel.equals(queryId))
			{
				Map<HyperPath,TrueFalseCount> hmap=queryToCount.get(entityFormula.path.get(i));
				if(hmap==null)
					continue;
				TrueFalseCount tfCount=hmap.get(conceptualFormula);
				if(tfCount==null)
				{
					tfCount=new TrueFalseCount();
					hmap.put(conceptualFormula, tfCount);
				}
				// count the true value.
				Boolean[] truthQueryList=new Boolean[entityFormula.path.size()];
				for(int j=0;j<entityFormula.path.size();j++)
				{
					truthQueryList[j]=entityFormula.path.get(j).value>0.5;
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
	}
	
	public List<HyperPath> GeneFormula(K_PathNode knode,K_PathEdge kedge)
	{
		int entity0=knode.path.path.get(0).nodeList.get(kedge.freenodeposition[0]);
		int entity1=knode.path.path.get(knode.path.path.size()-1).nodeList.get(kedge.freenodeposition[1]);
		
		HyperNode hn0=GlobalStaticData.entityToNode.get(entity0);
		List<HyperEdge> edgeList=hn0.edges.get(entity1);
		
		if(edgeList!=null)
		{
			List<HyperPath> trueCandidateList=new ArrayList<HyperPath>();
			for(int i=0;i<edgeList.size();i++)
			{
				HyperPath candidatePath=knode.path.copyOnePath();
				candidatePath.path.add(edgeList.get(i));
				HyperPath conceptual_cand=MLNProcess.Conceptualize(candidatePath);
				Integer findex=GlobalStaticData.conceptFormulaToIndex.get(conceptual_cand);
				if(findex==null)
					continue;
				trueCandidateList.add(candidatePath);
			}
			if(trueCandidateList.size()>0)
			{
				return trueCandidateList;
			}
		}
		
		
		if(GlobalStaticData.MaxFalseQuery<0)
			GlobalStaticData.MaxFalseQuery=5;
		
		List<HyperPath> falseCandidateList=new ArrayList<HyperPath>();
		if(kedge.newKnodeList==null)
		{
			kedge.newKnodeList=new ArrayList<K_PathNode>();
			kedge.newKnodeList.addAll(kedge.newKnodeSet);
		}
		for(int i=0;i<kedge.newKnodeList.size();i++)
		{	
			int l=kedge.newKnodeList.get(i).path.path.size();
			HyperEdge hedge1=new HyperEdge();
			hedge1.value=0.;
			hedge1.rel=kedge.newKnodeList.get(i).path.path.get(l-1).rel;
			hedge1.nodeList.add(entity0);
			hedge1.nodeList.add(entity1);
			HyperPath candidatePath=knode.path.copyOnePath();
			candidatePath.path.add(hedge1);
			HyperPath conceptual_cand=MLNProcess.Conceptualize(candidatePath);
			Integer findex=GlobalStaticData.conceptFormulaToIndex.get(conceptual_cand);
			if(findex!=null)
			{
				OutputData.PrintOneEntityHyperEdge(LogPrintStream.getPs_alwaysdebug(),hedge1);
				LogPrintStream.getPs_alwaysdebug().println();
				falseCandidateList.add(candidatePath);
			}
			
			HyperEdge hedge2=new HyperEdge();
			hedge2.value=0.;
			hedge2.rel=kedge.newKnodeList.get(i).path.path.get(l-1).rel;
			hedge2.nodeList.add(entity1);
			hedge2.nodeList.add(entity0);
			candidatePath=knode.path.copyOnePath();
			candidatePath.path.add(hedge2);
			conceptual_cand=MLNProcess.Conceptualize(candidatePath);
			findex=GlobalStaticData.conceptFormulaToIndex.get(conceptual_cand);
			if(findex!=null)
			{
				OutputData.PrintOneEntityHyperEdge(LogPrintStream.getPs_alwaysdebug(),hedge2);
				LogPrintStream.getPs_alwaysdebug().println();
				falseCandidateList.add(candidatePath);
			}
		}
		
		if(falseCandidateList.size()<=GlobalStaticData.MaxFalseQuery)
		{
			return falseCandidateList;
		}
		else
		{
			List<HyperPath> randFalseList=new ArrayList<HyperPath>();
			for(int i=0;i<GlobalStaticData.MaxFalseQuery;i++)
			{
				int index=Math.abs(rand.nextInt())%falseCandidateList.size();
				randFalseList.add(falseCandidateList.get(index));
			}
			return randFalseList;
		}
		
		
	}
	
	public boolean call() throws Exception {
		// TODO Auto-generated method stub
		LogPrintStream.getPs_thread_info().println("Thread "+threadId+" is started!");
		long startTimePoint=System.currentTimeMillis();
		
		//InitStates();
		LogPrintStream.getPs_thread_info().println(("Thread "+threadId+"init"));
		boolean flag=false;
		try{
			flag=InitFirstNode();
			}catch(Exception e)
			{
				e.printStackTrace(LogPrintStream.getPs_thread_info());
			}
		if(!flag)
			return false;
		for(int i=0;i<maxround;i++)
		{
			try{
				flag=RandomWalkOnKGraph(nowState,GlobalStaticData.restartProbability);
				if(!flag) //This thread has error, and stop it;
					break;
			}catch(Exception e)
			{
				e.printStackTrace(LogPrintStream.getPs_thread_info());
			}
			
		}
		//OutputData.PrintStateList(ps_middle_path, stateList);
		//stateList.clear();
		
		long endTimePoint=System.currentTimeMillis();
		LogPrintStream.getPs_thread_info().println("Thread "+threadId+" is end in "+(endTimePoint-startTimePoint)+"ms");
		
		
		return true;
	}	
	
	public static void AddAllQueryToQTC(List<HyperEdge> queryList
			,Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount)
	{
		for(int i=0;i<queryList.size();i++)
		{
			HyperEdge query=queryList.get(i);
			queryToCount.put(query,new HashMap<HyperPath,TrueFalseCount>());
		}
	}
	
}

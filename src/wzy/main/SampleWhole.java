package wzy.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import wzy.func.FileTools;
import wzy.func.InputData;
import wzy.func.MLNProcess;
import wzy.func.OutputData;
import wzy.func.RandomWalkPure;
import wzy.model.HyperNode;
import wzy.model.HyperPath;
import wzy.model.StatePOG;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;
import wzy.stat.ParameterCollection;

public class SampleWhole {

	
	public Map<String,Integer> tokenToIndex=new HashMap<String,Integer>();
	public List<String> indexToToken=new ArrayList<String>();
	
	
	
	//public Map<Integer,Set<HyperPath>> nodeToPathSet=new HashMap<Integer,Set<HyperPath>>();
	
	public Map<Integer,Set<StatePOG>> nodeToPathSet=new HashMap<Integer,Set<StatePOG>>();
	public Set<StatePOG> memoryforwhole=new HashSet<StatePOG>();
	public Set<StatePOG> memoryforSample=new HashSet<StatePOG>();
	
	public void BFSforAllPath(Integer startnode,int maxLength)
	{
		
		Set<StatePOG> set=nodeToPathSet.get(startnode);
		if(set==null)
		{
			set=new HashSet<StatePOG>();
			nodeToPathSet.put(startnode, set);
		}
		
		StatePOG sp=new StatePOG();
		sp.state.add(startnode);
		Queue<StatePOG> queue=new LinkedList<StatePOG>();
		queue.add(sp);
		while(!queue.isEmpty())
		{
			StatePOG statenow=queue.poll();
			Integer lastnodeindex=statenow.state.get(statenow.state.size()-1);
			if(statenow.state.size()<maxLength)
			{
				HyperNode hn=GlobalStaticData.entityToNode.get(lastnodeindex);
				List<Integer> neightbours=hn.neighbourList;
				for(int i=0;i<neightbours.size();i++)
				{
					StatePOG substate=statenow.copyOne();
					substate.state.add(neightbours.get(i));
					queue.add(substate);
					set.add(substate);
					//this.memoryforwhole.add(substate);
					System.out.println(substate.state);
				}
			}
		}
	}
	
	

	public void DFS(StatePOG sp,boolean[] flag,int maxLength)
	{
		if(sp.state.size()>=maxLength)
			return;
		Integer lastNodeIndex=sp.state.get(sp.state.size()-1);
		HyperNode lastNode=GlobalStaticData.entityToNode.get(lastNodeIndex);
		for(int i=0;i<lastNode.neighbourList.size();i++)
		{
			Integer nextIndex=lastNode.neighbourList.get(i);
			if(!flag[nextIndex])
			{
				flag[nextIndex]=true;
				StatePOG new_state=sp.copyOne();
				new_state.state.add(nextIndex);
				this.memoryforwhole.add(new_state);
				DFS(new_state,flag,maxLength);
				flag[nextIndex]=false;
			}
		}
	}
	
	public void DFSforAll(Integer startnode,int maxLength)
	{
		StatePOG sp=new StatePOG();
		sp.state.add(startnode);
		boolean[] flag=new boolean[GlobalStaticData.entityToNode.size()];
		flag[startnode]=true;
		DFS(sp,flag,maxLength);
	}
	
	
	
	public void PrintMap(String dir)
	{
		Iterator it=nodeToPathSet.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			Integer nodeIndex=(Integer)entry.getKey();
			Set<StatePOG> pathset=(Set<StatePOG>)entry.getValue();
			
			String nodeString=GlobalStaticData.entityMapItoS.get(nodeIndex);
			try {
				PrintStream ps=new PrintStream(dir+nodeString);
				
				Iterator it2=pathset.iterator();
				while(it2.hasNext())
				{
					StatePOG spog=(StatePOG)it2.next();
					for(int i=0;i<spog.state.size();i++)
					{
						ps.print(spog.state.get(i)+"\t");
					}
					ps.println();
				}
				
				ps.close();
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	public void NoSeedRandom(PrintStream psPath,double rate)
	{
		RandomWalkPure randomWalkPure=new RandomWalkPure();
		
		//set parameters
		randomWalkPure.MaxLength=5;
		randomWalkPure.numnodes=GlobalStaticData.entityToNode.size();
		randomWalkPure.restart_pro=0.1;
		randomWalkPure.memorystateset=this.memoryforSample;
		randomWalkPure.stopThrethold=(int)(rate*this.memoryforwhole.size());
		randomWalkPure.psPath=psPath;
		
		randomWalkPure.StartRandom();
	}
	
	public int GetOverlapNodes()
	{
		int count=0;
		Iterator it=this.memoryforwhole.iterator();
		while(it.hasNext())
		{
			StatePOG index=(StatePOG)it.next();
			if(this.memoryforSample.contains(index))
			{
				count++;
			}
		}
		return count;
	}
	
	public void PrintSet(PrintStream ps,Set<StatePOG> stateset)
	{
		Iterator it=stateset.iterator();
		while(it.hasNext())
		{
			StatePOG state=(StatePOG)it.next();
			for(int i=0;i<state.state.size();i++)
			{
				ps.print(state.state.get(i)+"\t");
			}
			ps.println();
		}
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println("is not that?");
		long start=System.currentTimeMillis();
		//Initialization
		ParameterCollection parameterCollection=new ParameterCollection();
		parameterCollection.ReadParameter(args[0]);
		System.out.println("SemanticLinkPrediction verison 1 is start.\t"+parameterCollection.getParameter("BaseDir"));

		GlobalStaticData.IntiDataStructure();
		
		//Create the result dir
		GlobalStaticData.setBaseDir(parameterCollection.getParameter("BaseDir"));
		FileTools.CreateNewFolder(GlobalStaticData.baseDir);
		FileTools.CreateNewFolder(GlobalStaticData.baseDir+"/mid");
		FileTools.CreateNewFolder(GlobalStaticData.baseDir+"/res");
		FileTools.CreateNewFolder(GlobalStaticData.baseDir+"/log");
				
		LogPrintStream.InitAllPrintStream();
		long initEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Initialization is over in "+(initEnd-start)+"ms.");
		
		
		//Input Datas
		String[] inputInfoFiles=parameterCollection.splitParaStringBySemicolon(parameterCollection.getParameter("inputInfoFile"));
		for(int i=0;i<inputInfoFiles.length;i++)
			InputData.ReadInfo(inputInfoFiles[i], GlobalStaticData.relMapItoS
					, GlobalStaticData.relMapStoI, GlobalStaticData.typeMapItoS, GlobalStaticData.typeMapStoI
					, GlobalStaticData.conceptRelList);
		String[] inputDbFiles=parameterCollection.splitParaStringBySemicolon(parameterCollection.getParameter("trainDbFile"));
		for(int i=0;i<inputDbFiles.length;i++)
			InputData.ReadDBgetentityInListSameTime(inputDbFiles[i], GlobalStaticData.entityMapStoI
					, GlobalStaticData.entityMapItoS, GlobalStaticData.entityToNode, GlobalStaticData.relMapStoI
					, GlobalStaticData.conceptRelList, GlobalStaticData.seed_train_list
					, parameterCollection.getParameter("Query"));
		
		
		GlobalStaticData.setQuery(parameterCollection.getParameter("Query"));  //set the global query for mln
		//System.out.println("why 1");
		MLNProcess.KeepTypeIndexToEntitySet();
		
		//GeneFalseQuery.GeneFalseQueryForTrain(GlobalStaticData.query);
		//GlobalStaticData.seed_train_list=GeneFalseQuery.GeneTrainQuerySeeds(GlobalStaticData.query);
		//GlobalStaticData.seed_train_list=GlobalStaticData.seed_train_list.subList(0,
				//GlobalStaticData.trainQuerySet.size());
		
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);//Formulize all paths obtained from random walk
		
		
		//Get All Paths for each nodes
		long startsw=System.currentTimeMillis();
		SampleWhole sw=new SampleWhole();
		for(int i=0;i<GlobalStaticData.entityMapItoS.size();i++)
		{
			//sw.BFSforAllPath(i, 5);
			sw.DFSforAll(i, 5);
			//break;
		}
		
		
		
		long endwd=System.currentTimeMillis();
		System.out.println("Sampling is over!\t"+(endwd-startsw)+"ms\t"+sw.memoryforwhole.size());
		
		//sw.PrintMap(GlobalStaticData.baseDir+"/mid/");
		
		
		PrintStream psWhole=new PrintStream(GlobalStaticData.baseDir+"/mid/sp.whole");
		sw.PrintSet(psWhole, sw.memoryforwhole);
		
		
		PrintStream psPath=new PrintStream(GlobalStaticData.baseDir+"/mid/sp.path");
		
		sw.NoSeedRandom(psPath,Double.parseDouble(parameterCollection.getParameter("overlop_rate")));
		
		int overlap=sw.GetOverlapNodes();
		System.out.println("Overlap\t"+overlap+"\t"+sw.memoryforwhole.size()+"\t"+sw.memoryforSample.size());
		
	}
	
	
}

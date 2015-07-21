package wzy.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wzy.func.FileTools;
import wzy.func.InferOneBigQueryThread;
import wzy.func.InputData;
import wzy.func.Learner;
import wzy.func.OneRelTaskThread;
import wzy.func.OutputData;
import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;
import wzy.stat.ParameterCollection;

public class MultiSemanticLinkPrediction {

	public static void SetGlobalLearnParameter(ParameterCollection parameterCollection)
	{
		OneRelTaskThread.maxIterator=Integer.parseInt(parameterCollection.getParameter("MaxIterator"));
		OneRelTaskThread.upsilon=Double.parseDouble(parameterCollection.getParameter("Upsilon"));
		OneRelTaskThread.lamada=Double.parseDouble(parameterCollection.getParameter("Lamda"));
		OneRelTaskThread.C=Double.parseDouble(parameterCollection.getParameter("C"));
		OneRelTaskThread.K=Integer.parseInt(parameterCollection.getParameter("K"));
		OneRelTaskThread.z=Double.parseDouble(parameterCollection.getParameter("z"));
	}
	
	public static void AllocThreads()
	{
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 		
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		for(int i=0;i<GlobalStaticData.relMapItoS.size();i++)
		{
			OneRelTaskThread onethread=new OneRelTaskThread();
			onethread.rel=i;
			alThreads.add(onethread);
		}
		GlobalStaticData.taskThreads=alThreads;
		try {
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();
	}
	


	
	public static void IndexTrainingSeed()
	{
		for(int i=0;i<GlobalStaticData.entityToNode.size();i++)
		{
			HyperNode hn=GlobalStaticData.entityToNode.get(i);
			Iterator it=hn.edges.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry entry=(Map.Entry)it.next();
				Integer sec=(Integer)entry.getKey();
				List<HyperEdge> edgeList=(List<HyperEdge>)entry.getValue();
				for(int j=0;j<edgeList.size();j++)
				{
					int edgerel=edgeList.get(j).rel;
					Set<Integer> entitySet=GlobalStaticData.relTotrainSeedSet.get(edgerel);
					if(entitySet==null)
					{
						entitySet=new HashSet<Integer>();
						GlobalStaticData.relTotrainSeedSet.put(edgerel, entitySet);
					}
					for(int k=0;k<edgeList.get(j).nodeList.size();k++)
					{
						entitySet.add(edgeList.get(j).nodeList.get(k));
					}
				}
			}
		}
		for(int i=0;i<GlobalStaticData.relMapItoS.size();i++)
		{
			Set<Integer> seedSet=GlobalStaticData.relTotrainSeedSet.get(i);
			if(seedSet==null)
				continue;
			List<Integer> seedList=new ArrayList<Integer>();
			seedList.addAll(seedSet);
			GlobalStaticData.relTotrainSeedList.put(i, seedList);
		}
	}
	
	public static void main(String[] args)
	{
		System.out.println("Compile");
		long start=System.currentTimeMillis();
		//Initialization
		ParameterCollection parameterCollection=new ParameterCollection();
		parameterCollection.ReadParameter(args[0]);
		System.out.println("Knowledge Base Completion version 2 is start.\t"+parameterCollection.getParameter("BaseDir"));
		GlobalStaticData.IntiDataStructure();
		GlobalStaticData.SetThreadAndRandom(Integer.parseInt(parameterCollection.getParameter("ThreadNum")),
				Integer.parseInt(parameterCollection.getParameter("RandomWalkNum")));
		GlobalStaticData.SetRandomWalkParameters(Integer.parseInt(parameterCollection.getParameter("MaxRound")),
				Integer.parseInt(parameterCollection.getParameter("MinLength")),
				Integer.parseInt(parameterCollection.getParameter("MaxLength")));
		GlobalStaticData.setRestartProbability(Double.parseDouble(parameterCollection.getParameter("RestartProbability")));
		String maxfalsequery=parameterCollection.getParameter("MaxFalseQuery");
		if(maxfalsequery!=null)
		{
			GlobalStaticData.MaxFalseQuery=Integer.parseInt(maxfalsequery);
		}
		try {
			pslog=new PrintStream("/dev/shm/matlab_temp/fb15k_data/parameter/log.log");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Create the result dir
		GlobalStaticData.setBaseDir(parameterCollection.getParameter("BaseDir"));
		FileTools.CreateNewFolder(GlobalStaticData.baseDir);
		FileTools.CreateNewFolder(GlobalStaticData.baseDir+"/mid");
		FileTools.CreateNewFolder(GlobalStaticData.baseDir+"/res");
		FileTools.CreateNewFolder(GlobalStaticData.baseDir+"/log");
		FileTools.CreateNewFolder(GlobalStaticData.baseDir+"/lw");	
		FileTools.CreateNewFolder(GlobalStaticData.baseDir+"/dp");
		FileTools.CreateNewFolder(GlobalStaticData.baseDir+"/rw");		
		
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
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);//Formulize all paths obtained from random walk
		OutputData.PrintGlobalStaticDataInfo(LogPrintStream.getPs_system_out());			
		long inputEnd=System.currentTimeMillis();
		//read query files
		GlobalStaticData.relToQuery=InputData.ReadAllFile(parameterCollection.getParameter("QueryDir"),
				"test.query");
		LogPrintStream.getPs_system_out().println("Input Data is over in "+(inputEnd-initEnd)+"ms");
		//AfterInputAnalyze(GlobalStaticData.entityToNode);
		//System.exit(-1);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		SetGlobalLearnParameter(parameterCollection);
		IndexTrainingSeed();
		OneRelTaskThread.querydir=parameterCollection.getParameter("QueryDir");
		//Random Walk New	
		System.out.println("Newest Multi-Learn-Predict Process is starting.");
		AllocThreads();
		//OutputData.PrintAllScoresbyfiles(parameterCollection.getParameter("QueryDir"),"results.pro");
	}
	public static PrintStream pslog;
}

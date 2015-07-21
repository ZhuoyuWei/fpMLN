package wzy.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wzy.func.FileTools;
import wzy.func.InferOneBigQueryThread;
import wzy.func.InputData;
import wzy.func.LearnWeightThread;
import wzy.func.Learner;
import wzy.func.MLNProcess;
import wzy.func.OutputData;
import wzy.func.RandomWalKnowledgeThread;
import wzy.func.RandomWalkThread;
import wzy.model.BigQuery;
import wzy.model.DataPoint;
import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.model.HyperPath;
import wzy.model.TrueFalseCount;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;
import wzy.stat.ParameterCollection;

public class KnowledgeBaseCompletion {

	public static Random rand=new Random();
	
	public static void RandomWalk(List<Integer> seedList,boolean isLearning)
	{
		//ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		ExecutorService exec = Executors.newFixedThreadPool(1); 		
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();

		for(int i=0;i<seedList.size();i++)
		{
			RandomWalKnowledgeThread randomThread=new RandomWalKnowledgeThread();
			randomThread.setThreadId(i);
			randomThread.setStartPoint(seedList.get(i));
			randomThread.islearning=isLearning;
			alThreads.add(randomThread);
		}	
		try {
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();
	}
	public static void RandomWalkRandom(List<Integer> seedList,boolean isLearning)
	{
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		//ExecutorService exec = Executors.newFixedThreadPool(1); 		
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();

		for(int i=0;i<GlobalStaticData.RandomWalkNum;i++)
		{
			RandomWalKnowledgeThread randomThread=new RandomWalKnowledgeThread();
			randomThread.setThreadId(i);
			randomThread.setStartPoint(Math.abs(rand.nextInt())%seedList.size());
			randomThread.islearning=isLearning;
			alThreads.add(randomThread);
		}	
		try {
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();
	}	
	
	public static void MergeTotalMap(Map<Integer,Map<HyperEdge,Map<HyperPath,TrueFalseCount>>> totalMap
			,List<Map<Integer,Map<HyperEdge,Map<HyperPath,TrueFalseCount>>>> totalMapList)
	{
		for(int i=0;i<GlobalStaticData.relMapItoS.size();i++)
		{
			GlobalStaticData.relToFormulaIndex.put(i, new HashMap<HyperPath,Integer>());
			GlobalStaticData.relToIndexFormula.put(i, new ArrayList<HyperPath>());
		}
		for(int i=0;i<totalMapList.size();i++)
		{
			Iterator it=totalMapList.get(i).entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry entry=(Map.Entry)it.next();
				Integer rel=(Integer)entry.getKey();
				Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryMap
					=(Map<HyperEdge,Map<HyperPath,TrueFalseCount>>)entry.getValue();
				Map<HyperEdge,Map<HyperPath,TrueFalseCount>> totalQueryMap=totalMap.get(rel);
				if(totalQueryMap==null)
				{
					totalMap.put(rel, queryMap);
				}
				else
				{
					Iterator it2=queryMap.entrySet().iterator();
					while(it2.hasNext())
					{
						Map.Entry entry2=(Map.Entry)it2.next();
						HyperEdge query=(HyperEdge)entry2.getKey();
						Map<HyperPath,TrueFalseCount> formulaMap=(Map<HyperPath,TrueFalseCount>)
								entry2.getValue();
						Map<HyperPath,TrueFalseCount> totalFormulaMap=totalQueryMap.get(query);
						if(totalFormulaMap==null)
						{
							totalQueryMap.put(query, formulaMap);
						}
						else
						{
							Iterator it3=formulaMap.entrySet().iterator();
							while(it3.hasNext())
							{
								Map.Entry entry3=(Map.Entry)it3.next();
								HyperPath formula=(HyperPath)entry3.getKey();
								////////////////////////////
								Integer formulaindex=GlobalStaticData.relToFormulaIndex.get(rel).get(formula);
								if(formulaindex==null)
								{
									GlobalStaticData.relToFormulaIndex.get(rel).put(formula,
											GlobalStaticData.indexToConceptFormula.size());
									GlobalStaticData.relToIndexFormula.get(rel).add(formula);
								}
								////////////////////////////
								TrueFalseCount count=(TrueFalseCount)entry3.getValue();
								TrueFalseCount totalCount=totalFormulaMap.get(formula);
								if(totalCount==null)
								{
									totalFormulaMap.put(formula, count);
								}
								else
								{
									totalCount.trueCount+=count.trueCount;
									totalCount.falseCount+=count.falseCount;
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static void ProduceAllDataPoint(Map<Integer,List<DataPoint>> relToDataPoint
			,Map<Integer,Map<HyperEdge,Map<HyperPath,TrueFalseCount>>> totalMap,
			Map<Integer,Map<HyperPath,Integer>> relToFormulaIndex,
			Map<Integer,List<HyperPath>> relToIndexFormula)
	{
		Iterator it=totalMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			Integer rel=(Integer)entry.getKey();
			Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount
				=(Map<HyperEdge,Map<HyperPath,TrueFalseCount>>)entry.getValue();
			List<DataPoint> dpList=new ArrayList<DataPoint>();
			MLNProcess.IndexQueryAndProduceDataPointTrain(queryToCount, dpList,
					relToIndexFormula.get(rel), relToFormulaIndex.get(rel));
			relToDataPoint.put(rel, dpList);
		}
	}
	
	public static void LearnAllWeight(ParameterCollection parameterCollection)
	{

		long start=System.currentTimeMillis();
		System.out.println("Start Learn weight");
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();

		Iterator it=GlobalStaticData.relToDataPoint.entrySet().iterator();
		int count=0;
		//System.out.println("Start Alloc");
		while(it.hasNext())
		{
			//long infer_start=System.currentTimeMillis();
			Map.Entry entry=(Map.Entry)it.next();
			Learner learner=new Learner();
			learner.IntiLearner(Integer.parseInt(parameterCollection.getParameter("MaxIterator")),
					Double.parseDouble(parameterCollection.getParameter("Lamda")),
					Double.parseDouble(parameterCollection.getParameter("Upsilon")),
					Double.parseDouble(parameterCollection.getParameter("C")),
					Integer.parseInt(parameterCollection.getParameter("K")),
					Double.parseDouble(parameterCollection.getParameter("z")),Learner.rand,
					GlobalStaticData.relToIndexFormula.get((Integer)entry.getKey()).size());
			//LogPrintStream.getPs_alwaysdebug().println("Feature size:"+
				//	GlobalStaticData.relToIndexFormula.get((Integer)entry.getKey()).size());
			//LogPrintStream.getPs_alwaysdebug().println("Learner "+count+" is ready");
			LearnWeightThread learnThread=new LearnWeightThread();
			learnThread.relIndex=count++;
			learnThread.rel=(Integer)entry.getKey();
			learnThread.dpList=(List<DataPoint>)entry.getValue();
			learnThread.learner=learner;
			//LogPrintStream.getPs_alwaysdebug().println("Learner Infer "+count+" is ready");	
			//long add_start=System.currentTimeMillis();
			GlobalStaticData.relToLearner.put(learnThread.rel, learner);
			//long add_end=System.currentTimeMillis();
			//LogPrintStream.getPs_alwaysdebug().println("Global Add "+(add_end-add_start)+"ms");
			//LogPrintStream.getPs_alwaysdebug().println("Map Added");
			alThreads.add(learnThread);

			//LogPrintStream.getPs_alwaysdebug().println("List Added");
			//long infer_end=System.currentTimeMillis();
			//LogPrintStream.getPs_alwaysdebug().println("One learn over. "+(infer_end-infer_start)+"ms");
		}
		long end=System.currentTimeMillis();
		System.out.println("Learn weight alloc is over "+(end-start)+"ms");
		//LogPrintStream.getPs_alwaysdebug().println("Alloc Done! "+alThreads.size()+" "+GlobalStaticData.relToLearner.size());
		try {
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();

	}
	
	public static void InferAll(List<BigQuery> bqList)
	{
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		GlobalStaticData.inferThreads=new ArrayList<InferOneBigQueryThread>();
		long start=System.currentTimeMillis();
		for(int i=0;i<bqList.size();i++)
		{
			InferOneBigQueryThread inferThread=new InferOneBigQueryThread();
			inferThread.bigqueryID=i;
			inferThread.bigquery=bqList.get(i);
			inferThread.learner=GlobalStaticData.relToLearner.get(inferThread.bigquery.rel);
			if(inferThread.learner==null)
			{
				continue;
			}
			inferThread.formulaToIndex=GlobalStaticData.relToFormulaIndex.get(inferThread.bigquery.rel);
			inferThread.indexToFormula=GlobalStaticData.relToIndexFormula.get(inferThread.bigquery.rel);
			/*try {
				inferThread.ps_rw_path=new PrintStream(GlobalStaticData.baseDir+"/rw/"+i+".log");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			inferThread.InitThread();
			alThreads.add(inferThread);
			GlobalStaticData.inferThreads.add(inferThread);
		}
		long end=System.currentTimeMillis();
		System.out.println("Infer allco is over "+(end-start)+"ms");
		try {
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();
	}
	
	public static void AfterInputAnalyze(List<HyperNode> nodeList)
	{
		int npaircount=0;
		int edgecount=0;
		for(int i=0;i<nodeList.size();i++)
		{
			for(int j=0;j<nodeList.get(i).neighbourList.size();j++)
			{
				npaircount++;
				edgecount+=nodeList.get(i).edges.get(nodeList.get(i).neighbourList.get(j)).size();
			}
		}
		System.out.println("pair edges: "+((double)edgecount/(double)npaircount));
	}
	
	public static void main(String[] args)
	{
		System.out.println("Compile");
		long start=System.currentTimeMillis();
		//Initialization
		ParameterCollection parameterCollection=new ParameterCollection();
		parameterCollection.ReadParameter(args[0]);
		System.out.println("Knowledge Base Completion version 1 is start.\t"+parameterCollection.getParameter("BaseDir"));
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
		LogPrintStream.getPs_system_out().println("Input Data is over in "+(inputEnd-initEnd)+"ms");
		//AfterInputAnalyze(GlobalStaticData.entityToNode);
		//System.exit(-1);
////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//Random Walk New
		long new_randomwalk_start=System.currentTimeMillis();
		List<Integer> trainSeedList=new ArrayList<Integer>(GlobalStaticData.entityMapItoS.size());
		for(int i=0;i<GlobalStaticData.entityMapItoS.size();i++)
		{
			trainSeedList.add(i);
		}
		//RandomWalk(trainSeedList,true);
		RandomWalkRandom(trainSeedList,true);
		long new_randomwalk_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Learn RW is over "
				+(new_randomwalk_end-new_randomwalk_start)+"ms");
		long new_mergemap_start=System.currentTimeMillis();
		MergeTotalMap(GlobalStaticData.totalDataPointMap,GlobalStaticData.totalDataPointMapList);
		System.out.println("Formula "+GlobalStaticData.conceptFormulaToIndex.size());
		long new_mergemap_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Merge map is over "+(new_mergemap_end
				-new_mergemap_start)+"ms");
		long new_produce_start=System.currentTimeMillis();
		ProduceAllDataPoint(GlobalStaticData.relToDataPoint,GlobalStaticData.totalDataPointMap,
				GlobalStaticData.relToFormulaIndex,GlobalStaticData.relToIndexFormula);
		long new_produce_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Produce is over "+(new_produce_end
				-new_produce_start)+"ms");
		long new_learnweight_start=System.currentTimeMillis();
		LearnAllWeight(parameterCollection);
		long new_learnweight_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Learn weight is over "+(new_learnweight_end
				-new_learnweight_start)+"ms");
		System.out.println("Train is Over!");
		GlobalStaticData.totalDataPointMap.clear();
		GlobalStaticData.totalDataPointMapList.clear();
		GlobalStaticData.relToDataPoint.clear();
		try{
		OutputData.logprint_FormulaAndWeigh(GlobalStaticData.relMapItoS,
				GlobalStaticData.relToIndexFormula, GlobalStaticData.relToLearner,
				GlobalStaticData.baseDir+"/lw");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		//Infer Start
		GlobalStaticData.bigqueryList=
				InputData.ReadFormatEntRelEntList(parameterCollection.getParameter("queryDbFile")
				, Boolean.parseBoolean(parameterCollection.getParameter("isLeft")));
		long new_infer_start=System.currentTimeMillis();
		GlobalStaticData.MaxRound=Integer.parseInt(parameterCollection.getParameter("inferMaxRound"));
		InferOneBigQueryThread.InitSet();
		InferAll(GlobalStaticData.bigqueryList);
		long new_infer_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Infer is over "+(new_infer_end-new_infer_start)+"ms");
		//PrintStream psbigquery=new PrintStream(GlobalStaticData.baseDir+"/res/bigquery.result");
		OutputData.PrintBigQueryResult(GlobalStaticData.baseDir+"/res/bigquery.result",
				GlobalStaticData.baseDir+"/res/bigquery.result.log"
				, GlobalStaticData.bigqueryList);
		try {
			OutputData.logprint_inferdp(GlobalStaticData.inferThreads,
					GlobalStaticData.baseDir+"/dp/", Boolean.parseBoolean(
							parameterCollection.getParameter("isLeft")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Infer is Over");
		
		
	}
}

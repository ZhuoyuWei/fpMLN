package wzy.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import analy.eval_other;
import auc.AUCCalculator;

import wzy.func.FileTools;
import wzy.func.GeneFalseQuery;
import wzy.func.InferenceTestTool;
import wzy.func.InputData;
import wzy.func.Learner;
import wzy.func.MLNProcess;
import wzy.func.OutputData;
import wzy.func.RandomWalkThread;
import wzy.infer.Infer;
import wzy.model.HyperEdge;
import wzy.model.HyperPath;
import wzy.model.StatePOG;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;
import wzy.stat.ParameterCollection;

public class SemanticLinkPrediction {

	public static Random rand=new Random();
	public static void RandomWalkFindPath(PrintStream ps_middle_path)
	{
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		for(int i=0;i<GlobalStaticData.RandomWalkNum;i++)
		{
			RandomWalkThread randomWalkThread=new RandomWalkThread();
			randomWalkThread.setThreadId(i);
			randomWalkThread.setStartPoint(Math.abs(rand.nextInt())%GlobalStaticData.entityToNode.size());
			randomWalkThread.setPs_middle_path(ps_middle_path);
			alThreads.add(randomWalkThread);
		}
		try {
			//Thread.sleep(10000);
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();
	}
	public static void RandomWalkFindPathforTest(PrintStream ps_middle_path)
	{
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		for(int i=0;i<GlobalStaticData.entitativeTestQueryList.size();i++)
		{
			RandomWalkThread randomWalkThread=new RandomWalkThread();
			randomWalkThread.setThreadId(i);
			HyperEdge query=GlobalStaticData.entitativeTestQueryList.get(i);
			randomWalkThread.setStartPoint(query.nodeList.get(0));
			randomWalkThread.setPs_middle_path(ps_middle_path);
			randomWalkThread.InitStates();
			alThreads.add(randomWalkThread);
		}
		try {
			//Thread.sleep(10000);
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();
	}	
	/**
	 * Change by wzy, at 2014.5.20, Synchronization for Dis and NoDis
	 * version 2.0
	 * @param ps_middle_path
	 */
	public static void RandomWalkFindPathSelectedTrain(PrintStream ps_middle_path)
	{
		System.out.println("RandomWalk: "+GlobalStaticData.RandomWalkNum+"\t"+GlobalStaticData.MaxRound);
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		//if(GlobalStaticData.RandomWalkNum<GlobalStaticData.seed_train_list.size())
		if(true)
		{
			for(int i=0;i<GlobalStaticData.RandomWalkNum;i++)
			{
				RandomWalkThread randomWalkThread=new RandomWalkThread();
				randomWalkThread.setThreadId(i);
				Integer startdp=GlobalStaticData.seed_train_list
						.get(Math.abs(rand.nextInt())%GlobalStaticData.seed_train_list.size());
				randomWalkThread.setStartPoint(startdp);
				LogPrintStream.getPs_MLN_ReadPathsAndProduceTrainningData().println(startdp);
				randomWalkThread.setPs_middle_path(ps_middle_path);
				randomWalkThread.InitStates();
				alThreads.add(randomWalkThread);
			}	
		}
		else
		{
			for(int i=0;i<GlobalStaticData.seed_train_list.size();i++)
			{
				RandomWalkThread randomWalkThread=new RandomWalkThread();
				randomWalkThread.setThreadId(i);
				randomWalkThread.setStartPoint(GlobalStaticData.seed_train_list.get(i));
				randomWalkThread.setPs_middle_path(ps_middle_path);
				randomWalkThread.InitStates();
				alThreads.add(randomWalkThread);
			}
		}

		try {
			//Thread.sleep(10000);
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();
	}	
	
	/**
	 * Change by wzy, at 2014.5.20, Synchronization for Dis and NoDis
	 * version 2.0
	 * @param ps_middle_path
	 */
	public static void RandomWalkFindPathSelectedTrain(PrintStream ps_middle_path,List<Integer> seedList)
	{
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		//if(GlobalStaticData.RandomWalkNum<seedList.size())
		if(true)
		{
			for(int i=0;i<GlobalStaticData.RandomWalkNum;i++)
			{
				RandomWalkThread randomWalkThread=new RandomWalkThread();
				randomWalkThread.setThreadId(i);
				randomWalkThread.setStartPoint(seedList
						.get(Math.abs(rand.nextInt())%seedList.size()));
				randomWalkThread.setPs_middle_path(ps_middle_path);
				randomWalkThread.InitStates();
				alThreads.add(randomWalkThread);
			}	
		}
		else
		{
			for(int i=0;i<seedList.size();i++)
			{
				RandomWalkThread randomWalkThread=new RandomWalkThread();
				randomWalkThread.setThreadId(i);
				randomWalkThread.setStartPoint(seedList.get(i));
				randomWalkThread.setPs_middle_path(ps_middle_path);
				randomWalkThread.InitStates();
				alThreads.add(randomWalkThread);
			}
		}

		try {
			//Thread.sleep(10000);
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();
	}	
	/**
	 * Change by wzy, at 2014.5.21, Synchronization for Dis and NoDis
	 * version 3.0
	 * @param ps_middle_path
	 */
	public static void RandomWalkFindPathSelectedfortest(PrintStream ps_middle_path,List<Integer> seedList)
	{
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		/*try {
			RandomWalkThread.pslog=new PrintStream("F:\\datainpaper\\TransE_Experiment_forRandomWalk\\14954\\mid\\result\\pro.log");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		//if(GlobalStaticData.RandomWalkNum<seedList.size())
		for(int i=0;i<seedList.size();i++)
		{
			RandomWalkThread randomWalkThread=new RandomWalkThread();
			randomWalkThread.setThreadId(i);
			randomWalkThread.setStartPoint(seedList.get(i));
			randomWalkThread.setPs_middle_path(ps_middle_path);
			randomWalkThread.InitStates();
			randomWalkThread.trainortestflag=false;
			randomWalkThread.maxround=10000;
			alThreads.add(randomWalkThread);
		}
	

		try {
			//Thread.sleep(10000);
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		exec.shutdown();
	}	
	
	
	public static void RandomWalkFindPathfortestStartWithTwoPoint(PrintStream ps_middle_path
			,List<HyperEdge> queryList)
	{
			ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
			List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
			for(int i=0;i<queryList.size();i++)
			{
				HyperEdge queryedge=queryList.get(i);
				StatePOG statepog=new StatePOG();
				for(int j=0;j<queryedge.nodeList.size();j++)
				{
					statepog.state.add(queryedge.nodeList.get(j));
				}
				RandomWalkThread randomWalkThread=new RandomWalkThread();
				randomWalkThread.setThreadId(i);
				randomWalkThread.setPs_middle_path(ps_middle_path);
				randomWalkThread.maxround=20000;
				randomWalkThread.InitStates(statepog);
				randomWalkThread.trainortestflag=false;
				//System.out.println(statepog.state);
				alThreads.add(randomWalkThread);
			}
			try {
				//Thread.sleep(10000);
				exec.invokeAll(alThreads);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			exec.shutdown();
			ps_middle_path.close();
	}
	
	/**
	 * This method is used to change infer method from previous method to 
	 * typed-formula-learned-pog graph(the new infer method)
	 */
	public static void UseInferObjectToInfer(ParameterCollection parameterCollection,Learner learner)
	{
		long learnEnd=System.currentTimeMillis();
		GlobalStaticData.clearGlobalVariablesAfterTrain();
		GlobalStaticData.SetRandomWalkParameters(Integer.parseInt(parameterCollection.getParameter("iMaxRound")),
				Integer.parseInt(parameterCollection.getParameter("MinLength")),
				Integer.parseInt(parameterCollection.getParameter("MaxLength")));
		InputData.ReadDB(parameterCollection.getParameter("testDbFile"), GlobalStaticData.entityMapStoI
				, GlobalStaticData.entityMapItoS, GlobalStaticData.entityToNode, GlobalStaticData.relMapStoI
				, GlobalStaticData.conceptRelList);
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);

		InputData.ReadQueryDB(parameterCollection.getParameter("queryDbFile"),
				GlobalStaticData.entityMapStoI, GlobalStaticData.entityMapItoS, GlobalStaticData.entityToNode,
				GlobalStaticData.relMapStoI, GlobalStaticData.conceptRelList,
				GlobalStaticData.entitativeTestQueryList,null);		
		//We have a map from query node set to HyperEdge(query itself).
		GlobalStaticData.entitivesetToQuery=InferenceTestTool
				.MapQueryListToIntSet(GlobalStaticData.entitativeTestQueryList);
		MLNProcess.KeepTypeIndexToEntitySet();	
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);
		
		GlobalStaticData.weights=learner.getWeights();
		Infer infer=new Infer();
		infer.clauseList=GlobalStaticData.indexToConceptFormula;
		infer.formulaMap=GlobalStaticData.conceptFormulaToIndex;
		infer.weightList=new ArrayList<Double>(GlobalStaticData.weights.length);
		for(int i=0;i<GlobalStaticData.weights.length;i++)
		{
			infer.weightList.add(GlobalStaticData.weights[i]);
		}
		
		//remove the zero weight formulas
		infer.FilterZeroWeightFormula2(1e-3);
		
		infer.HyperNodeSelfTyped();
		long time_buildPOG_start=System.currentTimeMillis();
		infer.BuildPOGGraph();
		long time_buildPOG_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Infer:Build POG Graph time is "+(time_buildPOG_end
				-time_buildPOG_start)+"ms");
		
		long time_extentFormula_start=System.currentTimeMillis();
		infer.ExtentFormulas();
		infer.RebuildGlobalFormula3();
		long time_extentFormula_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Infer:Extent formulas time is "+(time_extentFormula_end
				-time_extentFormula_start)+"ms");
		
		GlobalStaticData.seed_train_list=new ArrayList<Integer>();
		//InputData.GetSeedFromQueryList(GlobalStaticData.entitativeTestQueryList, GlobalStaticData.seed_train_list);
		InputData.GetSeedFromQueryListLeftAndRight(GlobalStaticData.entitativeTestQueryList, GlobalStaticData.seed_train_list);
		//System.out.println(GlobalStaticData.seed_train_list.size());
		long time_randomwalk_start=System.currentTimeMillis();
		Infer.RandomWalkFindPathSelectedfortest(LogPrintStream.getPs_middleresult_nodepath_test(),
				GlobalStaticData.seed_train_list);
		long time_randomwalk_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Infer:Random walk time is "+(time_randomwalk_end
				-time_randomwalk_start)+"ms");
		LogPrintStream.getPs_middleresult_nodepath_test().close();
		
		long time_genenous_start=System.currentTimeMillis();
		MLNProcess.ReadPathsAndProduceTrainningDataWithCheck(GlobalStaticData.baseDir+"/mid/nodepath_test.db",
				GlobalStaticData.testqueryToCount,GlobalStaticData.query,true);
		MLNProcess.IndexQueryAndProduceDataPointTest(GlobalStaticData.testqueryToCount,GlobalStaticData.entitativeTestQueryList,
				GlobalStaticData.testDataPointList,
				GlobalStaticData.indexToConceptFormula,GlobalStaticData.conceptFormulaToIndex);
		long time_genenous_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Infer:Gene Data time is "+(time_genenous_end
				-time_genenous_start)+"ms");
		
		MLNProcess.DeleteTypeIndexToEntitySet();
		OutputData.PrintMiddleDataPoint(LogPrintStream.getPs_middleresult_test_datapoint(), GlobalStaticData.testDataPointList);
		//Double[] probability=InferenceTestTool.CalcularProbability(
			//	GlobalStaticData.testDataPointList,GlobalStaticData.weights);
		InferenceTestTool.NormalDataPoints(GlobalStaticData.testDataPointList,
				GlobalStaticData.testDataPointNormalList);
		double time_calcular_start=System.currentTimeMillis();
		Double[] probability=InferenceTestTool.CalcularProbabilityNormal(
				GlobalStaticData.testDataPointNormalList,GlobalStaticData.weights);		
		double time_calcular_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Calculate probability time is "+(time_calcular_end
				-time_calcular_start)+"ms");
		double time_summary=time_buildPOG_end-time_buildPOG_start
				+time_randomwalk_end-time_randomwalk_start
				+time_genenous_end-time_genenous_start
				+time_calcular_end-time_calcular_start;
		LogPrintStream.getPs_system_out().println("New Total inference time is "+time_summary
				+"ms\t"+(time_summary/1000.));
		System.out.println(probability.length);
		OutputData.PrintCLLAndPro(probability, GlobalStaticData.testDataPointList,
				LogPrintStream.getPs_finalresult_cll(), LogPrintStream.getPs_finalresult_probability(),
				LogPrintStream.getPs_finalresult_view(),LogPrintStream.getPs_finalresult_expcll(),
				LogPrintStream.getPs_system_out());
		long inferenceEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Inference is over in "+(inferenceEnd-learnEnd)+"ms");
		System.out.println("Inference is over in "+(inferenceEnd-learnEnd)+"ms");	
		
		//Calculate AUC
				AUCCalculator.CalculateAUC(GlobalStaticData.baseDir+"/res/probability.result",
						"list", LogPrintStream.getPs_system_out());
				//Calculate CLL, In the PrintCLLAndPro()
				
				
				LogPrintStream.CloseAllPrintStream();
				
				long finalEnd=System.currentTimeMillis();
				
				
				PrintStream psFinal;
				try {
					psFinal = new PrintStream(GlobalStaticData.baseDir+"/res/final.log");
					eval_other.SplitResultForWordnet(GlobalStaticData.baseDir+"/res/userview.result"
						,GlobalStaticData.baseDir+"/res/userview.probabily");
					eval_other.Main(GlobalStaticData.baseDir+"/res/userview.probabily",
						GlobalStaticData.baseDir+"/res/userview.result",
						GlobalStaticData.baseDir+"/res/auc.temp_",
						GlobalStaticData.baseDir+"/res/res.log_", psFinal);			
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch blockS
					e.printStackTrace();
				}
		
	}
	
	
	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println("is not that? learner 1");
		long start=System.currentTimeMillis();
		//Initialization
		ParameterCollection parameterCollection=new ParameterCollection();
		parameterCollection.ReadParameter(args[0]);
		System.out.println("SemanticLinkPrediction verison 1 is start.\t"+parameterCollection.getParameter("BaseDir"));
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
		
		
		//List<String> temp_undirect_relList=InputData.ReadUndierectRel(parameterCollection.getParameter("undirectfile"));
		//if(temp_undirect_relList!=null)
			//GlobalStaticData.BuildUndirectResls(temp_undirect_relList);
		
		GlobalStaticData.setQuery(parameterCollection.getParameter("Query"));  //set the global query for mln
		//System.out.println("why 1");
		MLNProcess.KeepTypeIndexToEntitySet();
		
		//GeneFalseQuery.GeneFalseQueryForTrain(GlobalStaticData.query);
		//GlobalStaticData.seed_train_list=GeneFalseQuery.GeneTrainQuerySeeds(GlobalStaticData.query);
		//GlobalStaticData.seed_train_list=GlobalStaticData.seed_train_list.subList(0,
				//GlobalStaticData.trainQuerySet.size());
		
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);//Formulize all paths obtained from random walk
		
		
		long inputEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Input Data is over in "+(inputEnd-initEnd)+"ms");
		
		//Print Read file result
		OutputData.PrintGlobalStaticDataInfo(LogPrintStream.getPs_system_out());
		
		//Random walk to find paths and write them to file.
		RandomWalkFindPathSelectedTrain(LogPrintStream.getPs_middleresult_nodepath_train());
		LogPrintStream.getPs_middleresult_nodepath_train().close();
		//InputData.FilterDuplicatePath(GlobalStaticData.baseDir+"/mid/nodepath_train.db",
				//GlobalStaticData.baseDir+"/mid/nodepath_train.db");
		
		System.out.println("N:"+GlobalStaticData.entityToNode.size()+"\tT:"
				+GlobalStaticData.typeMapItoS.size()+"\tR:"+GlobalStaticData.relMapItoS.size());
		
		
		//RandomWalkFindPathSelectedfortest(LogPrintStream.getPs_middleresult_nodepath_train(),GlobalStaticData.seed_train_list);
		//RandomWalkFindPath(LogPrintStream.getPs_middleresult_nodepath_train());
		long randEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Random walk is over in "+(randEnd-inputEnd)+"ms");
		//System.out.println("NewLOG: RandomWalk for Train is over! "+(randEnd-inputEnd)+"ms");
		
		//System.out.println("why 2");		
		MLNProcess.ReadPathsAndProduceTrainningData(GlobalStaticData.baseDir+"/mid/nodepath_train.db"
				,GlobalStaticData.queryToCount);
		//MLNProcess.ReadPathsAndProduceTrainningDataWithCheck(GlobalStaticData.baseDir+"/mid/nodepath_train.db",
				//GlobalStaticData.queryToCount,GlobalStaticData.query);
		
		
		System.out.println("Train Before:\t"+GlobalStaticData.queryToCount.size());
		
		//added by wzy for query as seeds, at 5.27
		//GlobalStaticData.queryToCount=MLNProcess.FilterQueryCountByOrders(GlobalStaticData.queryToCount);
		
		System.out.println("Train After:\t"+GlobalStaticData.queryToCount.size());		
		//System.out.println("why 3");			
		MLNProcess.DeleteTypeIndexToEntitySet();
		//System.out.println("why 4");		
		long formulizeEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Formulize process is over in "+(formulizeEnd-randEnd)+"ms");
		System.out.println("NewLOG: RandomWalk for Train is over! "+(formulizeEnd-randEnd)+"ms");
		//Produce the training data points.
		MLNProcess.IndexQueryAndProduceDataPointTrain(GlobalStaticData.queryToCount, GlobalStaticData.trainDataPointList,
				GlobalStaticData.indexToConceptFormula,GlobalStaticData.conceptFormulaToIndex);
		//MLNProcess.FilterFormulaByAtLeastOnceTrueForTrain();  //remove the conceptual formulas with no true example in the knowledge base.
		OutputData.PrintMiddleDataPoint(LogPrintStream.getPs_middleresult_train_datapoint(), GlobalStaticData.trainDataPointList);
		long produceDataEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Produce Data is over in "+(produceDataEnd-formulizeEnd)+"ms");
		OutputData.PrintConceptualFormula(LogPrintStream.getPs_foruser_conceptualFormula(),
				GlobalStaticData.indexToConceptFormula);
		
		
		//learn weights for conceptual formulas
		Learner learner=new Learner();
		learner.IntiLearner(Integer.parseInt(parameterCollection.getParameter("MaxIterator")),
				Double.parseDouble(parameterCollection.getParameter("Lamda")),
				Double.parseDouble(parameterCollection.getParameter("Upsilon")),
				Double.parseDouble(parameterCollection.getParameter("C")),
				Integer.parseInt(parameterCollection.getParameter("K")),
				Double.parseDouble(parameterCollection.getParameter("z")),Learner.rand);
		learner.Learning(GlobalStaticData.trainDataPointList);
		OutputData.PrintWeights(LogPrintStream.getPs_middleresult_weights(), learner.getWeights());
		long learnEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Learn is over in "+(learnEnd-produceDataEnd)+"ms");
		
		
		//UseInferObjectToInfer(parameterCollection,learner);
		System.exit(0);
		
		//Inference process. It looks like Learning process.
		long infere_StartTime=System.currentTimeMillis();
		/*GlobalStaticData.clearGlobalVariablesAfterTrain();
		InputData.ReadDB(parameterCollection.getParameter("testDbFile"), GlobalStaticData.entityMapStoI
				, GlobalStaticData.entityMapItoS, GlobalStaticData.entityToNode, GlobalStaticData.relMapStoI
				, GlobalStaticData.conceptRelList);*/

		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);
		
		//InferenceTestTool.ExtractTrueQueryFromNodes();
		InputData.ReadQueryDB(parameterCollection.getParameter("queryDbFile"),
				GlobalStaticData.entityMapStoI, GlobalStaticData.entityMapItoS, GlobalStaticData.entityToNode,
				GlobalStaticData.relMapStoI, GlobalStaticData.conceptRelList,
				GlobalStaticData.entitativeTestQueryList,null);
		
		try {
			GlobalStaticData.candidateToProb=InputData.ReadProbMapForCandidate(parameterCollection.getParameter("scoreFile"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		}
		
		long infere_ReadEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("infer: read is over in "+(infere_ReadEnd-infere_StartTime)+"ms");		
		
		GlobalStaticData.entitivesetToQuery=InferenceTestTool
				.MapQueryListToIntSet(GlobalStaticData.entitativeTestQueryList);
		
		//System.out.println(GlobalStaticData.entitativeTestQueryList.size());
		MLNProcess.KeepTypeIndexToEntitySet();
		
		//InferenceTestTool.ProduceFalseQuery(Double.parseDouble(parameterCollection.getParameter("falseRate")));
		
		
		//System.out.println(GlobalStaticData.entitativeTestQueryList.size());
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);
		long infere_FalseDataEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("infer: producing false queries is over in "+(infere_FalseDataEnd-infere_ReadEnd)+"ms");
		
		//RandomWalkFindPathfortestStartWithTwoPoint(LogPrintStream.getPs_middleresult_nodepath_test()
				//,GlobalStaticData.entitativeTestQueryList);
	
		GlobalStaticData.seed_train_list=new ArrayList<Integer>();
		//InputData.GetSeedFromQueryListLeftAndRight(GlobalStaticData.entitativeTestQueryList, GlobalStaticData.seed_train_list);
		GlobalStaticData.isleft=parameterCollection.getParameter("islorr").equals("left");
		if(GlobalStaticData.isleft)
			InputData.GetSeedFromQueryListLeft(GlobalStaticData.entitativeTestQueryList, GlobalStaticData.seed_train_list);
		else
			InputData.GetSeedFromQueryListRight(GlobalStaticData.entitativeTestQueryList, GlobalStaticData.seed_train_list);			
		//System.out.println("SeedList: "+GlobalStaticData.seed_train_list.size()+"\t"
				//+GlobalStaticData.entityMapItoS.get(GlobalStaticData.seed_train_list.get(0))
				//+GlobalStaticData.entityMapItoS.get(GlobalStaticData.seed_train_list.get(1)));
		RandomWalkFindPathSelectedfortest(LogPrintStream.getPs_middleresult_nodepath_test(),GlobalStaticData.seed_train_list);
		
		
		
		
		
		
		LogPrintStream.getPs_middleresult_nodepath_test().close();
		
		//InputData.FilterDuplicatePath(GlobalStaticData.baseDir+"/mid/nodepath_test.db",
				//GlobalStaticData.baseDir+"/mid/nodepath_test.db");
		
		long infere_randomEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("infer: random walk in "+(infere_randomEnd-infere_FalseDataEnd)+"ms");
		

		//MLNProcess.ReadPathsAndProduceTrainningData(GlobalStaticData.baseDir+"/mid/nodepath_test.db",
				//GlobalStaticData.testqueryToCount);
		//MLNProcess.ReadPathsAndProduceTrainningTest(GlobalStaticData.baseDir+"/mid/nodepath_test.db",
				//GlobalStaticData.testqueryToCount);
		MLNProcess.ReadPathsAndProduceTrainningDataWithCheck(GlobalStaticData.baseDir+"/mid/nodepath_test.db",
				GlobalStaticData.testqueryToCount,GlobalStaticData.query,false);
		
		
		
		
		System.out.println("Before:\t"+GlobalStaticData.testqueryToCount.size());
		
		
		//added by wzy for query as seeds, at 5.27
		//GlobalStaticData.testqueryToCount=MLNProcess.FilterQueryCountByOrders(GlobalStaticData.testqueryToCount);
		
		
		System.out.println("After:\t"+GlobalStaticData.testqueryToCount.size());
		
		long infere_produceTest=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("produce test data in "+(infere_produceTest-infere_FalseDataEnd)+"ms");
		
		
		//5.20 by wzy, debug to look for what difference between train and test datas.
		
		/*MLNProcess.IndexQueryAndProduceDataPointTrain(GlobalStaticData.testqueryToCount, GlobalStaticData.testDataPointList,
				GlobalStaticData.test_indexToConceptFormula,GlobalStaticData.test_conceptFormulaToIndex);
		PrintStream ps_debug_user=new PrintStream(GlobalStaticData.baseDir+"/mid/conceptualFormula.user.test");
		if(ps_debug_user==null)
		{
			System.out.println("Debug file error");
		}
		else
		{
			System.out.println("Successful create file!");
		}
		OutputData.PrintConceptualFormula(ps_debug_user,
				GlobalStaticData.test_indexToConceptFormula);*/
		
		
		MLNProcess.IndexQueryAndProduceDataPointTest(GlobalStaticData.testqueryToCount,GlobalStaticData.entitativeTestQueryList,
			GlobalStaticData.testDataPointList,
			GlobalStaticData.indexToConceptFormula,GlobalStaticData.conceptFormulaToIndex);

		long infere_indexEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("index the test data in "+(infere_indexEnd-infere_produceTest)+"ms");
		
		MLNProcess.DeleteTypeIndexToEntitySet();
		OutputData.PrintMiddleDataPoint(LogPrintStream.getPs_middleresult_test_datapoint(), GlobalStaticData.testDataPointList);
		Double[] probability=InferenceTestTool.CalcularProbability(GlobalStaticData.testDataPointList,learner.getWeights());
		OutputData.PrintCLLAndPro(probability, GlobalStaticData.testDataPointList,
				LogPrintStream.getPs_finalresult_cll(), LogPrintStream.getPs_finalresult_probability(),
				LogPrintStream.getPs_finalresult_view(),LogPrintStream.getPs_finalresult_expcll(),
				LogPrintStream.getPs_system_out());
		long inferenceEnd=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Inference is over in "+(inferenceEnd-learnEnd)+"ms");
		
		//Calculate AUC
		AUCCalculator.CalculateAUC(GlobalStaticData.baseDir+"/res/probability.result", "list", LogPrintStream.getPs_system_out());
		//Calculate CLL, In the PrintCLLAndPro()
		
		
		LogPrintStream.CloseAllPrintStream();
		
		long finalEnd=System.currentTimeMillis();
		System.out.println(GlobalStaticData.baseDir+" is over in "+(finalEnd-start)+"ms");
		
		PrintStream psFinal;
		try {
			psFinal = new PrintStream(GlobalStaticData.baseDir+"/res/final.log");
			eval_other.SplitResultForWordnet(GlobalStaticData.baseDir+"/res/userview.result"
				,GlobalStaticData.baseDir+"/res/userview.probabily");
			eval_other.Main(GlobalStaticData.baseDir+"/res/userview.probabily",
				GlobalStaticData.baseDir+"/res/userview.result",
				GlobalStaticData.baseDir+"/res/auc.temp_",
				GlobalStaticData.baseDir+"/res/res.log_", psFinal);			
			psFinal.println(String.format("%.1f", (formulizeEnd-inputEnd)/1000.)+"s");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch blockS
			e.printStackTrace();
		}
		
		/*MLNProcess.FilterDataPoint(learner.getWeights(), GlobalStaticData.trainDataPointList);
		PrintStream pstrain2=new PrintStream(GlobalStaticData.baseDir+"/mid/train_datapoint.db");
		OutputData.PrintMiddleDataPoint(pstrain2, GlobalStaticData.trainDataPointList);
		
		MLNProcess.FilterDataPoint(learner.getWeights(), GlobalStaticData.testDataPointList);
		PrintStream pstest2=new PrintStream(GlobalStaticData.baseDir+"/mid/test_datapoint.db");
		OutputData.PrintMiddleDataPoint(pstest2, GlobalStaticData.testDataPointList);		
		
		
		PrintStream psformula=new PrintStream(GlobalStaticData.baseDir+"/mid/formula_nozero");
		OutputData.PrintFormulaNoZero(psformula, GlobalStaticData.indexToConceptFormula, learner.getWeights());*/
		

	}
	
}

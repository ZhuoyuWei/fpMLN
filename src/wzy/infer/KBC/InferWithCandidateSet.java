package wzy.infer.KBC;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import analy.eval_other;
import auc.AUCCalculator;

import wzy.func.FileTools;
import wzy.func.InferenceTestTool;
import wzy.func.InputData;
import wzy.func.MLNProcess;
import wzy.func.OutputData;
import wzy.infer.K_PathGraph;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;
import wzy.stat.ParameterCollection;
import wzy.model.HyperEdge;

public class InferWithCandidateSet {

	
	public static void PreProcess(String parametersfile)
	{
		System.out.println("Test KGraph!");
		ParameterCollection parameterCollection=new ParameterCollection();
		parameterCollection.ReadParameter("F:\\datainpaper\\TransE_Experiment_forRandomWalk\\14954\\mid\\parameter");
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
	
		
		//Input Datas
		//read rel file
		String[] inputInfoFiles=parameterCollection.splitParaStringBySemicolon(parameterCollection.getParameter("inputInfoFile"));
		for(int i=0;i<inputInfoFiles.length;i++)
			InputData.ReadInfo(inputInfoFiles[i], GlobalStaticData.relMapItoS
					, GlobalStaticData.relMapStoI, GlobalStaticData.typeMapItoS, GlobalStaticData.typeMapStoI
					, GlobalStaticData.conceptRelList);
		GlobalStaticData.setQuery(parameterCollection.getParameter("Query"));
		System.out.println("Concept Rel:\t"+GlobalStaticData.conceptRelList.size());
		//read db file
		GlobalStaticData.clearGlobalVariablesAfterTrain();
		InputData.ReadDB(parameterCollection.getParameter("testDbFile"), GlobalStaticData.entityMapStoI
				, GlobalStaticData.entityMapItoS, GlobalStaticData.entityToNode, GlobalStaticData.relMapStoI
				, GlobalStaticData.conceptRelList);
		
		
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);

		//InferenceTestTool.ExtractTrueQueryFromNodes();
		//query file
		InputData.ReadQueryDB(parameterCollection.getParameter("queryDbFile"),
				GlobalStaticData.entityMapStoI, GlobalStaticData.entityMapItoS, GlobalStaticData.entityToNode,
				GlobalStaticData.relMapStoI, GlobalStaticData.conceptRelList,
				GlobalStaticData.entitativeTestQueryList,null);	
		
		//We have a map from query node set to HyperEdge(query itself).
/*		Comment by wzy, in 2014.12.18
 * 		GlobalStaticData.entitivesetToQuery=InferenceTestTool
				.MapQueryListToIntSet(GlobalStaticData.entitativeTestQueryList);*/

		//System.out.println(GlobalStaticData.entitativeTestQueryList.size());
		MLNProcess.KeepTypeIndexToEntitySet();	
		//InferenceTestTool.ProduceFalseQuery(Double.parseDouble(parameterCollection.getParameter("falseRate")));
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);
		InputData.IndexAllNodeNeighbourstoType(GlobalStaticData.entityToNode);
	}
	
	public static Integer[] ProduceInferSeedList(List<HyperEdge> queryList)
	{
		Set<Integer> seedSet=new HashSet<Integer>();
		for(int i=0;i<queryList.size();i++)
		{
			//seedList.add(e)
			seedSet.add(queryList.get(i).nodeList.get(0));
			seedSet.add(queryList.get(i).nodeList.get(1));			
		}
		return seedSet.toArray(new Integer[0]);
	}
	
	public static void RandomWalkForKInfer(Integer[] seedList,K_PathGraph kgraph) throws Exception
	{
		for(int i=0;i<seedList.length;i++)
		//for(int i=0;i<1;i++)
		{
			RandomWalkForKBCinfer rw=new RandomWalkForKBCinfer();
			rw.kgraph=kgraph;
			rw.queryToCount=GlobalStaticData.testqueryToCount;
			rw.maxround=20000;//GlobalStaticData.MaxRound;
			rw.startPoint=seedList[i];
			//rw.startPoint=4834;
			rw.threadId=i;
			rw.call();
		}
	}
	
	public static void Debug_PrintAllQuery(String filename,List<HyperEdge> queryList) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<queryList.size();i++)
		{
			OutputData.PrintOneEntityHyperEdge(ps,queryList.get(i));
			ps.println();
		}
		ps.close();
	}
	public static void Debug_PrintAllSeed(String filename,Integer[] entityList) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<entityList.length;i++)
		{
			ps.println(GlobalStaticData.entityMapItoS.get(entityList[i]));
		}
		ps.close();		
	}
	
	
	public static void main(String[] args) throws Exception
	{
		PreProcess(null);
		
		
		TestKGraphBuilding tkgb=new TestKGraphBuilding();
		tkgb.ReadMLNStringfromFile("F:\\datainpaper\\TransE_Experiment_forRandomWalk\\14954\\mid\\conceptualFormula.user",
				"F:\\datainpaper\\TransE_Experiment_forRandomWalk\\14954\\mid\\weights.db");
		tkgb.kgraph.queryId=GlobalStaticData.query;
		tkgb.FilterSmallWeightFormula(1e-5);
		//System.out.println(tkgb.mlnStringList.size());
		tkgb.GetTopWeightFormulas(10);
		tkgb.PrintFormulas("F:\\datainpaper\\TransE_Experiment_forRandomWalk\\14954\\mid\\filtered.mln");
		tkgb.ParsingFormulas();
		tkgb.BuildTwoMapForFormulas(tkgb.mlnList);
		System.out.println(tkgb.mlnList.size());
		tkgb.PrintConceptualEdges("F:\\datainpaper\\TransE_Experiment_forRandomWalk\\14954\\mid\\conceptual.db");
		//tkgb.kgraph.BuildGraph(tkgb.mlnList);
		tkgb.kgraph.BuildAllGraph(tkgb.mlnList);	
		tkgb.kgraph.DebugBFSGraph2("F:\\datainpaper\\TransE_Experiment_forRandomWalk\\14954\\mid\\graph.log");
		
		RandomWalkForKBCinfer.AddAllQueryToQTC(GlobalStaticData.entitativeTestQueryList,
				GlobalStaticData.testqueryToCount);
		Integer[] seedList=ProduceInferSeedList(GlobalStaticData.entitativeTestQueryList);
		Debug_PrintAllQuery("F:\\datainpaper\\TransE_Experiment_forRandomWalk\\14954\\mid\\result\\log\\query.log"
				,GlobalStaticData.entitativeTestQueryList);
		Debug_PrintAllSeed("F:\\datainpaper\\TransE_Experiment_forRandomWalk\\14954\\mid\\result\\log\\seed.log"
				,seedList);
		RandomWalkForKInfer(seedList, tkgb.kgraph);
		MLNProcess.IndexQueryAndProduceDataPointTest(GlobalStaticData.testqueryToCount,GlobalStaticData.entitativeTestQueryList,
				GlobalStaticData.testDataPointList,
				GlobalStaticData.indexToConceptFormula,GlobalStaticData.conceptFormulaToIndex);
		OutputData.PrintMiddleDataPoint(LogPrintStream.getPs_middleresult_test_datapoint(), GlobalStaticData.testDataPointList);
		Double[] probability=InferenceTestTool.CalcularProbability(GlobalStaticData.testDataPointList,tkgb.weightList.toArray(new Double[0]));
		OutputData.PrintCLLAndPro(probability, GlobalStaticData.testDataPointList,
				LogPrintStream.getPs_finalresult_cll(), LogPrintStream.getPs_finalresult_probability(),
				LogPrintStream.getPs_finalresult_view(),LogPrintStream.getPs_finalresult_expcll(),
				LogPrintStream.getPs_system_out());
		long inferenceEnd=System.currentTimeMillis();
		//LogPrintStream.getPs_system_out().println("Inference is over in "+(inferenceEnd-learnEnd)+"ms");
		
		//Calculate AUC
		AUCCalculator.CalculateAUC(GlobalStaticData.baseDir+"/res/probability.result", "list", LogPrintStream.getPs_system_out());
		//Calculate CLL, In the PrintCLLAndPro()
		
		
		LogPrintStream.CloseAllPrintStream();
		
		long finalEnd=System.currentTimeMillis();
		//System.out.println(GlobalStaticData.baseDir+" is over in "+(finalEnd-start)+"ms");
		
		PrintStream psFinal;
		try {
			psFinal = new PrintStream(GlobalStaticData.baseDir+"/res/final.log");
			eval_other.SplitResultForWordnet(GlobalStaticData.baseDir+"/res/userview.result"
				,GlobalStaticData.baseDir+"/res/userview.probabily");
			eval_other.Main(GlobalStaticData.baseDir+"/res/userview.probabily",
				GlobalStaticData.baseDir+"/res/userview.result",
				GlobalStaticData.baseDir+"/res/auc.temp_",
				GlobalStaticData.baseDir+"/res/res.log_", psFinal);			
			//psFinal.println(String.format("%.1f", (formulizeEnd-inputEnd)/1000.)+"s");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch blockS
			e.printStackTrace();
		}
		
	}
	
}

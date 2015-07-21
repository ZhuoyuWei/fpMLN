package wzy.stat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;


import analy.TempCheckCode;

import wzy.func.InferOneBigQueryThread;
import wzy.func.Learner;
import wzy.func.OneRelTaskThread;
import wzy.infer.POG_typePathNode;
import wzy.infer.TypeNode;
import wzy.model.*;

/**
 * This is a static collection of data structures, which are hold
 * @author Zhuoyu Wei
 * @version 1.0
 * Field information:<br>
 * 
 */

public class GlobalStaticData {

	public static Map<String,Integer> relMapStoI;
	public static List<String> relMapItoS;
	public static List<HyperEdge> conceptRelList;
	
	public static Map<String,Integer> typeMapStoI;
	public static List<String> typeMapItoS;
	public static List<List<Integer>> typeToEntityList;
	
	public static Map<String,Integer> entityMapStoI;
	public static List<String> entityMapItoS;
	public static List<HyperNode> entityToNode;
	

	
	public static int ThreadNum=-1;
	public static int RandomWalkNum=-1;
	
	public static int MaxRound=-1;
	public static int MaxLength=-1;
	public static int MinLength=-1;
	
	public static Integer query;
	public static double restartProbability;
	
	public static Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount;
	//for debug , 5.20 by wzy.
	public static Map<HyperEdge,Map<HyperPath,TrueFalseCount>> testqueryToCount;	
	
	
	public static List<HyperPath> indexToConceptFormula;
	public static Map<HyperPath,Integer> conceptFormulaToIndex;
	public static Double[] weights;
	public static Map<HyperPath,Integer> sameFormulaMap;
	
	//for debug, 5.20 by wzy.
	public static List<HyperPath> test_indexToConceptFormula;
	public static Map<HyperPath,Integer> test_conceptFormulaToIndex;	
	
	public static List<DataPoint> trainDataPointList;
	public static List<DataPoint> testDataPointList;
	public static List<DataPointNormal> testDataPointNormalList;	
	
	//for the inference test evaluation.
	public static List<HyperEdge> entitativeTestQueryList; //true and false queries are all added in it.
														   //Add true ones from the test db file.
														   //Add false ones by producing random.
	public static List<HyperEdge> entitativeTestAnswerList;
	public static Set<HyperEdge> trainQuerySet;
	
	//for discriminal train and test
	public static Map<AIntSet,HyperEdge> entitivesetToQuery;
	
	
	public static String baseDir;
	public static String midDir;
	public static String resDir;
	
	
	//Undirect rel such as SameAuthor SameBib SameTitle SameVenue in cora dataset.
	public static Set<Integer> undirectRelSet;
	
	//special for baidu data
	public static List<Integer> seed_train_list;
	
	
	public static Integer MaxFalseQuery=-1;
	
	
	public static TypeNode[] typeNodeList;
	
	
	//For random infer
	public static Map<POG_typePathNode,List<HyperPath>> map_PathToFormulaList;
	public static Map<HyperPath,POG_typePathNode> map_FormulaToPath;
	public static Map<POG_typePathNode,Integer> pogNodeToIndex;
	public static List<POG_typePathNode> indexToPogNode;
	
	//For Knowledge Base Completion,2014.10.30
	public static Map<Integer,Map<HyperEdge,Map<HyperPath,TrueFalseCount>>> totalDataPointMap
		=new HashMap<Integer,Map<HyperEdge,Map<HyperPath,TrueFalseCount>>>();
	public static List<Map<Integer,Map<HyperEdge,Map<HyperPath,TrueFalseCount>>>> totalDataPointMapList
		=Collections.synchronizedList(new ArrayList<Map<Integer,Map<HyperEdge,
		Map<HyperPath,TrueFalseCount>>>>());
	public static Map<Integer,Map<HyperPath,Integer>> relToFormulaIndex=new HashMap<Integer
			,Map<HyperPath,Integer>>();
	public static Map<Integer,List<HyperPath>> relToIndexFormula=new HashMap<Integer,List<HyperPath>>();
	
	public static Map<Integer,List<DataPoint>> relToDataPoint=new HashMap<Integer,List<DataPoint>>();
	public static Map<Integer,Learner> relToLearner=Collections.synchronizedMap(
			new HashMap<Integer,Learner>());
	public static List<BigQuery> bigqueryList=new ArrayList<BigQuery>();
	public static Map<Integer,List<HyperEdge>> relToQuery=new HashMap<Integer,List<HyperEdge>>();
	public static Map<Integer,Set<Integer>> relTotrainSeedSet=new HashMap<Integer,Set<Integer>>();
	public static Map<Integer,List<Integer>> relTotrainSeedList=new HashMap<Integer,List<Integer>>();
	public static List<Callable<Integer>> taskThreads;
	public static Map<Integer,List<String>> relToQueryFile;
	
	//for debug, by wzy,11.3
	public static List<InferOneBigQueryThread> inferThreads;
	
	//for different probabilities of randomwalk, by wzy, 2014.12.18
	public static Map<Integer,Double> candidateToProb;
	public static boolean isleft=true;
	
	
	/**
	 * Initialize the global data structs, such as some List and Map
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public static void IntiDataStructure()
	{
		relMapStoI=new HashMap<String,Integer>();
		relMapItoS=new ArrayList<String>();
		conceptRelList=new ArrayList<HyperEdge>();
		
		typeMapStoI=new HashMap<String,Integer>();
		typeMapItoS=new ArrayList<String>();
		
		entityMapStoI=new HashMap<String,Integer>();
		entityMapItoS=new ArrayList<String>();
		entityToNode=new ArrayList<HyperNode>();
		
		queryToCount=new HashMap<HyperEdge,Map<HyperPath,TrueFalseCount>>();
		testqueryToCount=new HashMap<HyperEdge,Map<HyperPath,TrueFalseCount>>();  //for debug, 5.20, by wzy
		indexToConceptFormula=new ArrayList<HyperPath>();
		conceptFormulaToIndex=new HashMap<HyperPath,Integer>();
		sameFormulaMap=new HashMap<HyperPath,Integer>();
		
		
		test_indexToConceptFormula=new ArrayList<HyperPath>();          //for debug, 5.20, by wzy
		test_conceptFormulaToIndex=new HashMap<HyperPath,Integer>();	//for debug, 5.20, by wzy
		
		
		trainDataPointList=new ArrayList<DataPoint>();
		testDataPointList=new ArrayList<DataPoint>();
		testDataPointNormalList=new ArrayList<DataPointNormal>();
		
		entitativeTestQueryList=new ArrayList<HyperEdge>();
		entitativeTestAnswerList=new ArrayList<HyperEdge>();
		trainQuerySet=new HashSet<HyperEdge>();
		
		entitivesetToQuery=new HashMap<AIntSet,HyperEdge>();
		seed_train_list=new ArrayList<Integer>();
		
		undirectRelSet=new HashSet<Integer>();
		
		candidateToProb=new HashMap<Integer,Double>();
		
	}
	public static void IntiTypeToEntityList(int size)
	{
		typeToEntityList=new ArrayList<List<Integer>>(size);
		for(int i=0;i<size;i++)
		{
			typeToEntityList.add(new ArrayList<Integer>());
		}
	}
	/**
	 * Set the parameters in the RandomWalkThread process.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param maxRound In a random walk thread, the maximum of round to visit new state.
	 * @param minLength the minimum length of the path of hypernodes.
	 * @param maxLength the maximum length of the path of hypernodes.
	 */
	public static void SetRandomWalkParameters(int maxRound,int minLength,int maxLength)
	{
		MaxRound=maxRound;
		MinLength=minLength;
		MaxLength=maxLength;
	}
	/**
	 * Set the number of thread with considering about your running machine, and set the number of independent random walk times.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param threadNum the number of thread for FixedThreadPool.
	 * @param randomWalkNum the number of independent random walk times, it is usually larger than threadNum.
	 */
	public static void SetThreadAndRandom(int threadNum,int randomWalkNum)
	{
		ThreadNum=threadNum;
		RandomWalkNum=randomWalkNum;
	}
	
	/**
	 * Set the global query, it can be set in the parameters.txt file. 
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param queryText the string for the query.
	 */
	public static void setQuery(String queryText)
	{
		Integer rel=relMapStoI.get(queryText);
		if(rel==null)
		{
			System.out.println("There is no such query in the info file or db.");
			System.out.println(queryText);
			TempCheckCode.DebugPrintRels(relMapStoI);
			System.exit(0);
		}
		query=rel;
	}
	
	/**
	 * Set the probability for restart at the any state.
	 * @author Zhuoyu Wei
	 * @verison 1.0
	 * @param restartProbability
	 */
	public static void setRestartProbability(double restartProbability) {
		GlobalStaticData.restartProbability = restartProbability;
	}
	
	/**
	 * Clear the global variables after learning process to make sure it won't effect the inference process.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 */
	public static void clearGlobalVariablesAfterTrain()
	{
		entityMapStoI=new HashMap<String,Integer>();
		entityMapItoS=new ArrayList<String>();
		entityToNode=new ArrayList<HyperNode>();
		trainDataPointList.clear();
		queryToCount.clear();
	}
	
	public static void setBaseDir(String baseDir) {
		GlobalStaticData.baseDir = baseDir;
	}
	

	

	public static HyperPath ProduceOneCoceptQueryPath()
	{
		HyperPath queryPath=new HyperPath();
		HyperEdge queryEdge=GlobalStaticData.conceptRelList.get(query);
		queryPath.path.add(queryEdge);
		return queryPath;
	}
	
	public static void BuildUndirectResls(List<String> relList)
	{
		for(int i=0;i<relList.size();i++)
		{
			Integer index=relMapStoI.get(relList.get(i));
			if(index!=null)
			{
				undirectRelSet.add(index);
			}
		}
		System.out.println("UndirectSet size: "+undirectRelSet.size());
	}
	
}

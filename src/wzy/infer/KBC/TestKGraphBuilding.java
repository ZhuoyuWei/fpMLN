package wzy.infer.KBC;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wzy.func.FileTools;
import wzy.func.InferenceTestTool;
import wzy.func.InputData;
import wzy.func.MLNProcess;
import wzy.func.OutputData;
import wzy.infer.K_PathGraph;
import wzy.model.HyperEdge;
import wzy.model.HyperPath;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;
import wzy.stat.ParameterCollection;

public class TestKGraphBuilding {

	public List<String> mlnStringList=new ArrayList<String>();
	public List<Double> weightList=new ArrayList<Double>();
	public List<HyperPath> mlnList=new ArrayList<HyperPath>();
	public K_PathGraph kgraph=new K_PathGraph();
	
	public void ReadMLNStringfromFile(String mlnfile,String weightfile)throws IOException
	{
		//Read the learning results for structure and weights
		BufferedReader br=new BufferedReader(new FileReader(mlnfile));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("\t");
			mlnStringList.add(ss[1]);
		}
		br.close();
		
		buffer=null;
		br=new BufferedReader(new FileReader(weightfile));
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<1)
				continue;
			weightList.add(Double.parseDouble(buffer));
		}
		br.close();

	}
	
	/**
	 * Not infer for formulas whose weight is zero
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @date 2014.8.25
	 */
	public void FilterSmallWeightFormula(double smallweight)
	{
		List<Double> tempWeight=this.weightList;
		List<String> tempMln=this.mlnStringList;
		
		this.weightList=new ArrayList<Double>();
		this.mlnStringList=new ArrayList<String>();
		
		for(int i=0;i<tempWeight.size();i++)
		{
			if(Math.abs(tempWeight.get(i))>smallweight)
			{
				this.weightList.add(tempWeight.get(i));
				this.mlnStringList.add(tempMln.get(i));
			}
		}
	}
	
	public void GetTopWeightFormulas(int Top)
	{
		class TwoValue implements Comparator
		{
			String clause;
			Double value;
			
			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				
				TwoValue t1=(TwoValue)o1;
				TwoValue t2=(TwoValue)o2;
				
				if((Math.abs(Math.abs(t1.value)-Math.abs(t2.value)))<1e-10)
					return 0;
				else if(Math.abs(t1.value)>Math.abs(t2.value))
					return -1;
				else
					return 1;
			}
		}
		List<TwoValue> tvList=new ArrayList<TwoValue>();
		for(int i=0;i<weightList.size();i++)
		{
			TwoValue tv=new TwoValue();
			tv.clause=mlnStringList.get(i);
			tv.value=weightList.get(i);
			tvList.add(tv);
		}
		Collections.sort(tvList,new TwoValue());
		mlnStringList=new ArrayList<String>();
		weightList=new ArrayList<Double>();
		
		Top=Top<tvList.size()?Top:tvList.size();
		
		for(int i=0;i<Top;i++)
		{
			mlnStringList.add(tvList.get(i).clause);
			weightList.add(tvList.get(i).value);
		}
		
	}
	
	public int SplitATypeAndNum(String buffer)
	{
		int i;
		for(i=buffer.length()-1;i>=0;i--)
		{
			if(buffer.charAt(i)>'9'||buffer.charAt(i)<'0')
				break;
		}
		return i;
	}
	/**
	 * Parsing a formula string to a HyperPath just like ones in learning progress.
	 * @author Zhouyu Wei
	 * @param formula string
	 * @return conceptual formual
	 */
	public HyperPath ParsingToFormula(String formula)
	{
		String pattern="[A-Za-z0-9]+[(]+.*?[)]+";
		Pattern p=Pattern.compile(pattern);
		Matcher matcher=p.matcher(formula);

		Map<String,Integer> typeToIndex=new HashMap<String,Integer>();
		HyperPath entityHp=new HyperPath();
		HyperPath conceptHp=new HyperPath();
		
		System.out.println(formula);
		
		while(matcher.find())
		{
			String buffer=matcher.group();
			//System.out.println(buffer);
			
			String[] ss=buffer.split("[()]+");
			String rel=ss[0];
			String[] token=ss[1].split(",");
		//	System.out.println(rel+"\t"+token[0]+"\t"+token[1]);
			
			HyperEdge hyperEdge=new HyperEdge();
			
			hyperEdge.rel=GlobalStaticData.relMapStoI.get(rel);
			
			//System.out.println("***\t"+rel+"\t"+hyperEdge.rel+"\t"+GlobalStaticData.relMapItoS.get(hyperEdge.rel));
			
			for(int i=0;i<token.length;i++)
			{
				/*Integer index=typeToIndex.get(token[i]);
				if(index==null)
				{
					index=typeToIndex.size();
					typeToIndex.put(token[i], index);
				}
				hyperEdge.nodeList.add(index);*/
				
				int index=this.SplitATypeAndNum(token[i]);
				String temp_type=token[i].substring(0, index+1);
				Integer temp_index=Integer.parseInt(token[i].substring(index+1));
				//String temp_index=token[i].substring(index+1);
				//System.out.println(temp_type+"\t"+temp_index);
				
				int temp_code=GlobalStaticData.typeMapStoI.get(temp_type)
						+GlobalStaticData.typeMapItoS.size()*temp_index;
				
				hyperEdge.nodeList.add(temp_code);
				
				
			}
			//System.out.println(hyperEdge.rel+"\t"+hyperEdge.nodeList.get(0)+"\t"+hyperEdge.nodeList.get(1));			
			entityHp.path.add(hyperEdge);
			
		}
		/*OutputData.PrintOneEntityHyperPath(System.out, entityHp);
		System.out.println();
		conceptHp=MLNProcess.Conceptualize(entityHp);
		OutputData.PrintOneConceptHyperPath(System.out, conceptHp);
		return conceptHp;*/
		return entityHp;
		
	}
	
	
	public void ParsingFormulas()
	{
		for(int i=0;i<mlnStringList.size();i++)
		{
			mlnList.add(ParsingToFormula(mlnStringList.get(i)));
		}
	}
	
	public void PrintFormulas(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		System.out.println(mlnStringList.size());
		for(int i=0;i<mlnStringList.size();i++)
		{
			ps.println(weightList.get(i)+"\t"+mlnStringList.get(i));
		}
	}
	
	//just for test
	public void PrintConceptualEdges(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<mlnList.size();i++)
		{
			for(int j=0;j<mlnList.get(i).path.size();j++)
			{
				OutputData.PrintOneEntityHyperEdge(ps, mlnList.get(i).path.get(j));
			}
		}
		ps.close();
	}
	
	public void BuildTwoMapForFormulas(List<HyperPath> mlnList)
	{
		GlobalStaticData.indexToConceptFormula=mlnList;
		GlobalStaticData.conceptFormulaToIndex=new HashMap<HyperPath,Integer>();
		for(int i=0;i<mlnList.size();i++)
		{
			GlobalStaticData.conceptFormulaToIndex.put(mlnList.get(i), i);
		}
	}
	
	public static void main(String[] args) throws IOException
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
		GlobalStaticData.entitivesetToQuery=InferenceTestTool
				.MapQueryListToIntSet(GlobalStaticData.entitativeTestQueryList);

		//System.out.println(GlobalStaticData.entitativeTestQueryList.size());
		MLNProcess.KeepTypeIndexToEntitySet();	
		//InferenceTestTool.ProduceFalseQuery(Double.parseDouble(parameterCollection.getParameter("falseRate")));
		InputData.IndexAllNodeNeighbours(GlobalStaticData.entityToNode);
		
		
		
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
		
	}
}

package wzy.infer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import analy.eval_other;
import auc.AUCCalculator;

import wzy.func.FileTools;
import wzy.func.InferenceTestTool;
import wzy.func.InputData;
import wzy.func.MLNProcess;
import wzy.func.OutputData;
import wzy.func.RandomWalkThread;
import wzy.func.RandomWalkThreadinfer;
import wzy.model.HyperEdge;
import wzy.model.HyperNode;
import wzy.model.HyperPath;
import wzy.model.StatePOG;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;
import wzy.stat.ParameterCollection;

public class Infer {
	
	
	public List<String> mlnList=new ArrayList<String>();
	public List<Double> weightList=new ArrayList<Double>();
	public List<HyperPath> clauseList=new ArrayList<HyperPath>();
	public Map<HyperPath,Integer> formulaMap=new HashMap<HyperPath,Integer>();	
	
	public TypeNode[] typeNodeList;
	
	public Map<POG_typePathNode,List<HyperPath>> map_PathToFormulaList=new HashMap<POG_typePathNode,List<HyperPath>>();
	public Map<HyperPath,POG_typePathNode> map_FormulaToPath=new HashMap<HyperPath,POG_typePathNode>();
	
	public Map<POG_typePathNode,Integer> pogNodeToIndex=new HashMap<POG_typePathNode,Integer>();
	public List<POG_typePathNode> indexToPogNode=new ArrayList<POG_typePathNode>();


	
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
			mlnList.add(ss[1]);
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
	public void FilterZeroWeightFormula()
	{
		List<Double> tempWeight=this.weightList;
		List<String> tempMln=this.mlnList;
		
		this.weightList=new ArrayList<Double>();
		this.mlnList=new ArrayList<String>();
		
		for(int i=0;i<tempWeight.size();i++)
		{
			if(Math.abs(tempWeight.get(i))>1e-3)
			{
				this.weightList.add(tempWeight.get(i));
				this.mlnList.add(tempMln.get(i));
			}
		}
	}
	public void FilterZeroWeightFormula2(double threhold)
	{
		List<Double> tempWeight=new ArrayList<Double>();
		List<HyperPath> tempitoc=new ArrayList<HyperPath>();
		Map<HyperPath,Integer> tempctoi=new HashMap<HyperPath,Integer>();
		
		for(int i=0;i<weightList.size();i++)
		{
			if(Math.abs(weightList.get(i))>threhold)
			{
				tempWeight.add(weightList.get(i));
				//this.mlnList.add(tempMln.get(i));
				tempctoi.put(clauseList.get(i), tempitoc.size());
				tempitoc.add(clauseList.get(i));
			}			
		}
		
		weightList=tempWeight;
		formulaMap=tempctoi;
		clauseList=tempitoc;
		GlobalStaticData.conceptFormulaToIndex=formulaMap;
		GlobalStaticData.indexToConceptFormula=clauseList;
		GlobalStaticData.weights=weightList.toArray(new Double[0]);
		
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
		String pattern="[A-Za-z]+[(]+.*?[)]+";
		Pattern p=Pattern.compile(pattern);
		Matcher matcher=p.matcher(formula);

		Map<String,Integer> typeToIndex=new HashMap<String,Integer>();
		HyperPath entityHp=new HyperPath();
		HyperPath conceptHp=new HyperPath();
		
		
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
	
	/**
	 * By this step, we can obtain formuals map just like learning process, but remember that each formula's index<\br>
	 * is not the same as ones in learning process. However, it doesn't matter.<\br>
	 * If We run learning and Infer in one Process, this step can be skipped because we have one.
	 * @author Zhuoyu Wei
	 */
	public void BuildFormulaMap()
	{
		//parsing the string to formula and save as map to index.
		for(int i=0;i<mlnList.size();i++)
		{
			String mlnString=mlnList.get(i);
			HyperPath hpFormula=ParsingToFormula(mlnString);

			formulaMap.put(hpFormula, clauseList.size());
			clauseList.add(hpFormula);
		}
		GlobalStaticData.conceptFormulaToIndex=formulaMap;
		GlobalStaticData.indexToConceptFormula=clauseList;
		GlobalStaticData.weights=this.weightList.toArray(new Double[0]);
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
	private POG_typePathNode pushAhead(POG_typePathNode nowState,Integer type)
	{
		POG_typePathNode typeNode=new POG_typePathNode();
		typeNode.nodeList.add(type);
		typeNode.nodeList.addAll(nowState.nodeList);
		return typeNode;
	}
	/**
	 * Build a new State from the nowState by adding an entity at the end of the nowState
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param nowState
	 * @param entity
	 * @return the new state
	 */
	private POG_typePathNode pushBack(POG_typePathNode nowState,Integer type)
	{
		POG_typePathNode typeNode=new POG_typePathNode();
		typeNode.nodeList.addAll(nowState.nodeList);
		typeNode.nodeList.add(type);
		return typeNode;
	}	
	/**
	 * Build a new State from the nowState by removing the entity at the head of the nowState
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param nowState
	 * @param entity
	 * @return the new state
	 */	
	private POG_typePathNode removeAhead(POG_typePathNode nowState)
	{
		POG_typePathNode typeNode=new POG_typePathNode();
		copyList(typeNode.nodeList,nowState.nodeList,1,nowState.nodeList.size()-1);
		return typeNode;
	}
	/**
	 * Build a new State from the nowState by removing the entity at the end of the nowState
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param nowState
	 * @param entity
	 * @return the new state
	 */		
	private POG_typePathNode removeBack(POG_typePathNode nowState)
	{
		POG_typePathNode typeNode=new POG_typePathNode();
		copyList(typeNode.nodeList,nowState.nodeList,0,nowState.nodeList.size()-2);
		return typeNode;
	}
	
	/**
	 * Build a map for each hypernode, map<type, neighbors list>
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @date 2014.8.25
	 */
	public void HyperNodeSelfTyped()
	{
		List<HyperNode> hnList=GlobalStaticData.entityToNode;
		for(int i=0;i<hnList.size();i++)
		{
			hnList.get(i).BuildTypeMapItself();
		}
	}
	
	
	/**
	 * It is the most important process in the whole infer process.<\br>
	 * It builds a POG graph, whose nodes are a typical state(path). Unlike learning,<\br>
	 * this pog graph is built explicitly.<\br>
	 * @author Zhuoyu Wei
	 */
	public void BuildPOGGraph()
	{
		for(int i=0;i<this.clauseList.size();i++)
		{
			//For each clause got from structure learning or reading mln files, we general its all Pog_typePathNode
			//and subpath nodes.
			
			
			HyperPath hyperPath=this.clauseList.get(i);
			
			List<Integer> nodeList=new ArrayList<Integer>();
			//It applies only to the case that all formula are circle and 
			//there no more than two same entity in the ajacent relations
			
			for(int j=0;j<hyperPath.path.size();j++)
			{
				HyperEdge hyperEdge=hyperPath.path.get(j);
				HyperEdge hyperEdge2=hyperPath.path.get((j+hyperPath.path.size()-1)%hyperPath.path.size());
				int temp=-1;
				for(int k=0;k<hyperEdge.nodeList.size();k++)
				{
					for(int h=0;h<hyperEdge2.nodeList.size();h++)
					{
						if(hyperEdge.nodeList.get(k)==hyperEdge2.nodeList.get(h))
						{
							temp=hyperEdge.nodeList.get(k);
							break;
						}
					}
					if(temp>=0)
						break;
				}
				nodeList.add(temp);
			}
			
			POG_typePathNode pog_Node=new POG_typePathNode();
			for(int j=0;j<nodeList.size();j++)
			{
				Integer code=nodeList.get(j);
				Integer type=code%GlobalStaticData.typeMapItoS.size();
				pog_Node.nodeList.add(type);
			}
			
			//Build map relations between HyperPath and pog_typePathNode
			List<HyperPath> hpList=this.map_PathToFormulaList.get(pog_Node);
			if(hpList==null)
			{
				hpList=new ArrayList<HyperPath>();
				this.map_PathToFormulaList.put(pog_Node, hpList);
			}
			hpList.add(hyperPath);
			this.map_FormulaToPath.put(hyperPath, pog_Node);
			
			
			//Generate all subpath of this pog path node, and save them, then link them by pog_typePathEdges
			//Finally, they can constract the Type pratial order graph.
			for(int j=1;j<=pog_Node.nodeList.size();j++)
			{
				for(int k=0;k<=pog_Node.nodeList.size()-j;k++)
				{
					POG_typePathNode pog_temp=new POG_typePathNode();
					for(int h=k;h<k+j;h++)
					{
						pog_temp.nodeList.add(pog_Node.nodeList.get(h));
					}
					
					Integer index=pogNodeToIndex.get(pog_temp);
					if(index==null)
					{
						index=indexToPogNode.size();
						pogNodeToIndex.put(pog_temp, index);
						indexToPogNode.add(pog_temp);
					}
				}
			}
		}
		
		//Add empty pog node
		POG_typePathNode emptyNode=new POG_typePathNode();
		this.pogNodeToIndex.put(emptyNode, this.indexToPogNode.size());
		this.indexToPogNode.add(emptyNode);		
/*		System.out.println("***\tPOG nodes are built\t"+this.pogNodeToIndex.size()+"\t"+this.indexToPogNode.size());
		for(int i=0;i<this.indexToPogNode.size();i++)
		{
			System.out.println(i+"\t"+this.indexToPogNode.get(i).nodeList);
		}
		for(int i=0;i<GlobalStaticData.typeMapItoS.size();i++)
		{
			System.out.print(GlobalStaticData.typeMapItoS.get(i)+"\t");
		}
		System.out.println();*/
		//Link edges for nodes in the pog graph.
		for(int i=0;i<indexToPogNode.size();i++)
		{
			POG_typePathNode pogNode=indexToPogNode.get(i);
			if(pogNode.nodeList.size()>0)
			{
				POG_typePathNode aheadSubNode=this.removeAhead(pogNode);
				Integer aheadSubIndex=this.pogNodeToIndex.get(aheadSubNode);
				if(aheadSubIndex!=null)
				{
					//POG_typePathEdge subAhead=new POG_typePathEdge();
					//subAhead.state=1;
					POG_typePathEdge superAhead=new POG_typePathEdge();
					superAhead.state=3;
					superAhead.type=pogNode.nodeList.get(0);
					
					aheadSubNode=this.indexToPogNode.get(aheadSubIndex);
					aheadSubNode.superEdgeSet.add(superAhead);
				}
				
				POG_typePathNode backSubNode=this.removeBack(pogNode);
				Integer backSubIndex=this.pogNodeToIndex.get(backSubNode);
				if(backSubIndex!=null)
				{
					POG_typePathEdge superBack=new POG_typePathEdge();
					superBack.state=4;
					superBack.type=pogNode.nodeList.get(pogNode.nodeList.size()-1);
					
					backSubNode=this.indexToPogNode.get(backSubIndex);
					backSubNode.superEdgeSet.add(superBack);					
				}
				
			}
		}
		//Index List from set for all POG_typePathNodes'edges
		for(int i=0;i<indexToPogNode.size();i++)
		{
			POG_typePathNode pogNode=indexToPogNode.get(i);
			Iterator it=pogNode.superEdgeSet.iterator();
			while(it.hasNext())
			{
				pogNode.superEdgeList.add((POG_typePathEdge)it.next());
			}
		}
		
		//Notify GlobalStaticData maps and lists
		GlobalStaticData.map_FormulaToPath=this.map_FormulaToPath;
		GlobalStaticData.map_PathToFormulaList=this.map_PathToFormulaList;
		GlobalStaticData.pogNodeToIndex=this.pogNodeToIndex;
		GlobalStaticData.indexToPogNode=this.indexToPogNode;
		
	}
	
	
/*	public void BuildTypeGraph() 
	{
		typeNodeList=new TypeNode[GlobalStaticData.typeMapItoS.size()];
		for(int i=0;i<typeNodeList.length;i++)
		{
			typeNodeList[i]=new TypeNode();
		}
		for(int i=0;i<clauseList.size();i++)
		{
			HyperPath hp=clauseList.get(i);
			Double score=weightList.get(i);
			for(int j=0;j<hp.path.size();j++)
			{
				HyperEdge he=hp.path.get(j);
				
				for(int k=0;k<he.nodeList.size();k++)
				{
					for(int h=0;h<he.nodeList.size();h++)
					{
						if(k==h)
							continue;
						Double value=typeNodeList[k].nextToValue.get(h);
						if(value==null)
						{
							typeNodeList[k].nextToValue.put(h, score);
						}
						else
						{
							value+=score;
							typeNodeList[k].nextToValue.remove(h);
							typeNodeList[k].nextToValue.put(h, value);
						}
					}
				}
			}
		}
		
		
		for(int i=0;i<typeNodeList.length;i++)
		{
			TypeNode tn=typeNodeList[i];
			Iterator it=tn.nextToValue.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry entry=(Map.Entry)it.next();
				Integer type=(Integer)entry.getKey();
				Double value=(Double)entry.getValue();
				tn.nextType.add(type);
				tn.edgeValue.add(value);
			}
		}
		GlobalStaticData.typeNodeList=typeNodeList;
	}*/
	
	
	
	public void ReadFormula(List<String> mln)
	{
		GlobalStaticData.conceptFormulaToIndex=new HashMap<HyperPath,Integer>();
		GlobalStaticData.indexToConceptFormula=new ArrayList<HyperPath>();
	}
	
	
	
	
	/**
	 * Infer in the new way, Random in a conceptual type graph, connected by HyperEdge in the formulas
	 * @author Zhuoyu Wei
	 * @version 4.0,just for infer
	 * @param ps_middle_path
	 * @param seedList, Entities as nodes in queries
	 */
	public static void RandomWalkFindPathSelectedfortest(PrintStream ps_middle_path,List<Integer> seedList)
	{
		ExecutorService exec = Executors.newFixedThreadPool(GlobalStaticData.ThreadNum); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		//if(GlobalStaticData.RandomWalkNum<seedList.size())
		System.out.println("SeedSize:\t"+seedList.size()+"\t"+GlobalStaticData.MaxRound);
		for(int i=0;i<seedList.size();i++)
		{
			RandomWalkThreadinfer randomWalkThread=new RandomWalkThreadinfer();
			randomWalkThread.setThreadId(i);
			randomWalkThread.setStartPoint(seedList.get(i));
			randomWalkThread.setPs_middle_path(ps_middle_path);
			randomWalkThread.InitStates();
			randomWalkThread.maxround=GlobalStaticData.MaxRound;
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
	 * The follow three functions are for the following situation.<\b>
	 * R1(A1,B1)^R2(B1,C1)^R3(C1,A2)=>R4(A2,A1)
	 * R1(A1,B1)^R2(B1,C1)^R3(C1,A1)=>R4(A1,A1)
	 * We Add all possible formula to build a formula same map.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @date 2014.8.26
	 */
	public void ExtentFormulas()
	{
		for(int i=0;i<GlobalStaticData.indexToConceptFormula.size();i++)
		{
			HyperPath hp=GlobalStaticData.indexToConceptFormula.get(i);
			Map<Integer,Set<Integer>> typeMap=new HashMap<Integer,Set<Integer>>();
			for(int j=0;j<hp.path.size();j++)
			{
				HyperEdge he=hp.path.get(j);
				for(int k=0;k<he.nodeList.size();k++)
				{
					int code=he.nodeList.get(k);
					Integer type=code%GlobalStaticData.typeMapItoS.size();
					Set<Integer> nodeSet=typeMap.get(type);
					if(nodeSet==null)
					{
						nodeSet=new HashSet<Integer>();
						typeMap.put(type, nodeSet);
					}
					nodeSet.add(code);
				}
			}

			
			Iterator it=typeMap.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry entry=(Map.Entry)it.next();
				Integer type=(Integer)entry.getKey();
				Set<Integer> nodeSet=(Set<Integer>)entry.getValue();
				Integer[] nodeList=nodeSet.toArray(new Integer[0]);
				List<Integer> indexList=new ArrayList<Integer>();
				DFSWholeList(nodeList,indexList,hp,i);

			}
			
			GlobalStaticData.sameFormulaMap.put(hp, i);
			
		}
	}
	
	public void RebuildGlobalFormula3()
	{
		List<HyperPath> temp_indexToConceptFormula=new ArrayList<HyperPath>();
		Map<HyperPath,Integer> temp_conceptFormulaToIndex=new HashMap<HyperPath,Integer>();
		List<Double> temp_weightList=new ArrayList<Double>();

		Iterator it=GlobalStaticData.sameFormulaMap.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			HyperPath formula=(HyperPath)entry.getKey();
			Integer temp_index=(Integer)entry.getValue();
			Double temp_weight=GlobalStaticData.weights[temp_index];
			
			temp_conceptFormulaToIndex.put(formula, temp_indexToConceptFormula.size());
			temp_indexToConceptFormula.add(formula);
			temp_weightList.add(temp_weight);
		}
		
		GlobalStaticData.conceptFormulaToIndex=temp_conceptFormulaToIndex;
		GlobalStaticData.indexToConceptFormula=temp_indexToConceptFormula;
		GlobalStaticData.weights=temp_weightList.toArray(new Double[0]);
		System.out.println("Same:\t"+GlobalStaticData.conceptFormulaToIndex.size()+"\t"
				+GlobalStaticData.indexToConceptFormula.size()+"\t"+
				GlobalStaticData.weights.length);
	}
	
	public void DFSWholeList(Integer[] nodeList,List<Integer> indexList,HyperPath formula,int formulaIndex)
	{
		int index=-1;
		if(indexList.size()>0)
		{
			index=indexList.get(indexList.size()-1);
		}
		
		for(int i=index+1;i<nodeList.length;i++)
		{
			indexList.add(i);
			if(indexList.size()>=2)
			{
				Set<Integer> nodeSet=new HashSet<Integer>();
				int startnode=nodeList[indexList.get(0)];
				for(int j=0;j<indexList.size();j++)
				{
					nodeSet.add(nodeList[indexList.get(j)]);
				}
				HyperPath hp=new HyperPath();
				for(int j=0;j<formula.path.size();j++)
				{
					HyperEdge formula_he=formula.path.get(j);
					HyperEdge new_he=new HyperEdge();
					new_he.rel=formula_he.rel;
					for(int k=0;k<formula_he.nodeList.size();k++)
					{
						if(nodeSet.contains(formula_he.nodeList.get(k)))
						{
							new_he.nodeList.add(startnode);
						}
						else
						{
							new_he.nodeList.add(formula_he.nodeList.get(k));
						}
					}
					hp.path.add(new_he);
				}
				GlobalStaticData.sameFormulaMap.put(hp, formulaIndex);
			}
			DFSWholeList(nodeList,indexList,formula,formulaIndex);
			indexList.remove(indexList.size()-1);
		}
			
	}
	
	

	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Infer new!");
		ParameterCollection parameterCollection=new ParameterCollection();
		parameterCollection.ReadParameter(args[0]);
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


		
		long learnEnd=System.currentTimeMillis();
		Infer infer=new Infer();
		//infer.ReadFormula(infer.mlnList);
		double time_readMLN_start=System.currentTimeMillis();
		infer.ReadMLNStringfromFile(parameterCollection.getParameter("formulaFile"),
				parameterCollection.getParameter("weightFile"));
		
		infer.FilterZeroWeightFormula();
		
		infer.BuildFormulaMap();

		System.out.println("Global Formula:\t"+GlobalStaticData.conceptFormulaToIndex.size());
		infer.HyperNodeSelfTyped();	
		
		double time_readMLN_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Read and prepare infer's time is "+(time_readMLN_end-time_readMLN_start)+"ms");
		
		long time_buildPOG_start=System.currentTimeMillis();
		infer.BuildPOGGraph();
		long time_buildPOG_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Build POG Graph time is "+(time_buildPOG_end
				-time_buildPOG_start)+"ms");
		
		long time_extentFormula_start=System.currentTimeMillis();
		infer.ExtentFormulas();
		infer.RebuildGlobalFormula3();
		long time_extentFormula_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Extent formulas time is "+(time_extentFormula_end
				-time_extentFormula_start)+"ms");
		
		
		/*PrintStream ps_wzy_temp_test=new PrintStream
				("C:\\Users\\Administrator\\Desktop\\infer experiments\\Test_Avea\\test.log");
		OutputData.PrintConceptualFormula(ps_wzy_temp_test, GlobalStaticData.indexToConceptFormula
				,GlobalStaticData.weights);
		PrintStream ps_log_POG=new PrintStream(
				"C:\\Users\\Administrator\\Desktop\\infer experiments\\Test_Avea\\ps.log.pog");
		infer.PrintPOGGraph(ps_log_POG);
		ps_log_POG.println("=================================================================================");
		infer.PrintMapPOGPathToFormulas(ps_log_POG);
		ps_log_POG.close();*/
		
		GlobalStaticData.seed_train_list=new ArrayList<Integer>();
		InputData.GetSeedFromQueryListLeft(GlobalStaticData.entitativeTestQueryList, GlobalStaticData.seed_train_list);
		//System.out.println(GlobalStaticData.seed_train_list.size());
		
		long time_randomwalk_start=System.currentTimeMillis();
		RandomWalkFindPathSelectedfortest(LogPrintStream.getPs_middleresult_nodepath_test(),
				GlobalStaticData.seed_train_list);
		long time_randomwalk_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Random walk time is "+(time_randomwalk_end
				-time_randomwalk_start)+"ms");
		
		
		
		
		LogPrintStream.getPs_middleresult_nodepath_test().close();
		
		//InputData.FilterDuplicatePath(GlobalStaticData.baseDir+"/mid/nodepath_test.db",
				//GlobalStaticData.baseDir+"/mid/nodepath_test.db");
		
		long infere_randomEnd=System.currentTimeMillis();
		
		long time_genenous_start=System.currentTimeMillis();

		//MLNProcess.ReadPathsAndProduceTrainningData(GlobalStaticData.baseDir+"/mid/nodepath_test.db",
				//GlobalStaticData.testqueryToCount);
		//MLNProcess.ReadPathsAndProduceTrainningTest(GlobalStaticData.baseDir+"/mid/nodepath_test.db",
				//GlobalStaticData.testqueryToCount);
		MLNProcess.ReadPathsAndProduceTrainningDataWithCheck(GlobalStaticData.baseDir+"/mid/nodepath_test.db",
				GlobalStaticData.testqueryToCount,GlobalStaticData.query,true);
		
		
		
		
		System.out.println("Before:\t"+GlobalStaticData.testqueryToCount.size());
		
		
		//added by wzy for query as seeds, at 5.27
		//GlobalStaticData.testqueryToCount=MLNProcess.FilterQueryCountByOrders(GlobalStaticData.testqueryToCount);
		
		
		System.out.println("After:\t"+GlobalStaticData.testqueryToCount.size());
		
		long infere_produceTest=System.currentTimeMillis();
		//LogPrintStream.getPs_system_out().println("produce test data in "+(infere_produceTest-infere_FalseDataEnd)+"ms");
		
		
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
			GlobalStaticData.testDataPointList,GlobalStaticData.indexToConceptFormula,GlobalStaticData.conceptFormulaToIndex);
		long time_genenous_end=System.currentTimeMillis();
		LogPrintStream.getPs_system_out().println("Genenous formulas time is "+(time_genenous_end
				-time_genenous_start)+"ms");

		long infere_indexEnd=System.currentTimeMillis();
		

		
		LogPrintStream.getPs_system_out().println("index the test data in "+(infere_indexEnd-infere_produceTest)+"ms");
		
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
		
		/*infer.CheckDBNodeLinkItseft();
		infer.CheckPathNode("C:\\Users\\Administrator\\Desktop\\infer experiments" +
				"\\infer\\result_fpMLN_infer\\SameVenue\\1\\mid\\nodepath_test.db");*/
	}
	
	
	
	
	//for debug
	/**
	 * 打印POG图的信息，每个节点代表的type的路径，以及节点之间的边是怎么连接的（只有super边，sub边不需要显式地写出来）
	 * @param ps
	 */
	public void PrintPOGGraph(PrintStream ps)
	{
		//print nodes
		Iterator it=GlobalStaticData.pogNodeToIndex.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			POG_typePathNode pogNode=(POG_typePathNode)entry.getKey();
			
			ps.println("POG Node:\t"+pogNode.nodeList);
			
			for(int i=0;i<pogNode.nodeList.size();i++)
			{
				ps.print(GlobalStaticData.typeMapItoS.get(pogNode.nodeList.get(i))+"\t");
			}
			ps.println();
			for(int i=0;i<pogNode.superEdgeList.size();i++)
			{
				ps.println(pogNode.superEdgeList.get(i).state+"\t"+pogNode.superEdgeList.get(i).type);
				ps.print("\t********\t");
				if(pogNode.superEdgeList.get(i).state==3)
				{
					ps.print(pogNode.superEdgeList.get(i).type+"\t");
					for(int j=0;j<pogNode.nodeList.size();j++)
					{
						ps.print(pogNode.nodeList.get(j)+"\t");
					}
					ps.println();
				}
				else
				{
					for(int j=0;j<pogNode.nodeList.size();j++)
					{
						ps.print(pogNode.nodeList.get(j)+"\t");
					}
					ps.print(pogNode.superEdgeList.get(i).type+"\t");
					ps.println();					
				}				
				ps.print("\t********\t");
				if(pogNode.superEdgeList.get(i).state==3)
				{
					ps.print(GlobalStaticData.typeMapItoS.get(pogNode.superEdgeList.get(i).type)+"\t");
					for(int j=0;j<pogNode.nodeList.size();j++)
					{
						ps.print(GlobalStaticData.typeMapItoS.get(pogNode.nodeList.get(j))+"\t");
					}
					ps.println();
				}
				else
				{
					for(int j=0;j<pogNode.nodeList.size();j++)
					{
						ps.print(GlobalStaticData.typeMapItoS.get(pogNode.nodeList.get(j))+"\t");
					}
					ps.print(GlobalStaticData.typeMapItoS.get(pogNode.superEdgeList.get(i).type)+"\t");
					ps.println();					
				}
			}
			ps.println();
			
		}
	}
	
	public void PrintMapPOGPathToFormulas(PrintStream ps)
	{
		Iterator it=GlobalStaticData.map_PathToFormulaList.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			POG_typePathNode pogNode=(POG_typePathNode)entry.getKey();
			List<HyperPath> hpList=(List<HyperPath>)entry.getValue();
			
			ps.println("POG Node:\t"+pogNode.nodeList);
			for(int i=0;i<pogNode.nodeList.size();i++)
			{
				ps.print(GlobalStaticData.typeMapItoS.get(pogNode.nodeList.get(i))+"\t");
			}
			ps.println();
			
			for(int i=0;i<hpList.size();i++)
			{
				HyperPath hp=hpList.get(i);
				OutputData.PrintOneConceptHyperPath(ps, hp);
				ps.println();
			}
			ps.println();
		}
	}
	
	public void CheckDBNodeLinkItseft()
	{
		int count=0;
		for(int i=0;i<GlobalStaticData.entityToNode.size();i++)
		{
			HyperNode hn=GlobalStaticData.entityToNode.get(i);
			boolean flag=false;
			for(int j=0;j<hn.neighbourList.size();j++)
			{
				if(hn.neighbourList.get(j).equals(hn.entity))
				{
					flag=true;
					break;
				}
			}
			if(flag)
				count++;
		}
		System.out.println("Db link itseft\t"+count+"("+GlobalStaticData.entityToNode.size()+")");
	}
	
	public void CheckPathNode(String filename)
	{
		BufferedReader br;
		int count=0;
		try {
			br = new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<1)
					continue;
				String[] ss=buffer.split("\t");
				/*Set<String> set=new HashSet<String>();
				for(int i=0;i<ss.length;i++)
				{
					set.add(ss[i]);
				}
				*/
				boolean flag=false;
				for(int i=0;i<ss.length;i++)
				{
					if(ss[i].equals(ss[(i+1)%ss.length]))
					{
						flag=true;
						break;
					}
				}
				if(flag)
				{
					System.out.println(buffer);
					count++;
				}
				
			}
			System.out.println("Repeat Paths:\t"+count);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}

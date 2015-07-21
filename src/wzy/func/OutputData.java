package wzy.func;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import wzy.model.*;
import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;

public class OutputData {

	/**
	 * At any time in the random walk process, any thread can use this function to print middle results of paths of hypernodes.<br>
	 * It has synchronization mechanism to guarantee that only one thread visit it at one time.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param ps the global Print Stream 
	 * @param stateList paths(states) be printed.
	 */
	public static void PrintStateList(PrintStream ps,List<StatePOG> stateList)
	{
		for(int i=0;i<stateList.size();i++)
		{
			StatePOG statepog=stateList.get(i);
			synchronized(ps)
			{
				for(int j=0;j<statepog.state.size();j++)
				{
					ps.print(statepog.state.get(j)+"\t");
				}
				ps.println();
			}
		}
	}
	
	/**
	 * After Formulize paths and produce data points, the function output the datapoints no matter for train or test.<br>
	 * Only the main thread will use it, so no need add synchronized for it.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param ps the middle data point output filename 
	 * @param datapointList data points List produce in the previous process
	 * @deprecated
	 */
/*	public static void PrintMiddleDataPoint(PrintStream ps,List<DataPoint> datapointList)
	{
		for(int i=0;i<datapointList.size();i++)
		{
			DataPoint dp=datapointList.get(i);
			//PrintOneEntityHyperEdge(ps,dp.query);
			//ps.print("\t"+dp.query.value+"\t"+dp.nodeList.get(0)+":"+dp.countList.get(0));
			ps.print(dp.query.value+"\t"+dp.nodeList.get(0)+":"+dp.countList.get(0));
			for(int j=1;j<dp.nodeList.size();j++)
			{
				ps.print(";"+dp.nodeList.get(j)+":"+dp.countList.get(j));
			}
			ps.println();
		}
	}*/
	/**
	 * After Formulize paths and produce data points, the function output the datapoints no matter for train or test.<br>
	 * Only the main thread will use it, so no need add synchronized for it.
	 * @author Zhuoyu Wei
	 * @version 1.1 the value of datapoint is changed to TrueFalseCount.
	 * @param ps the middle data point output filename 
	 * @param datapointList data points List produce in the previous process
	 */
	public static void PrintMiddleDataPoint(PrintStream ps,List<DataPoint> datapointList)
	{
		for(int i=0;i<datapointList.size();i++)
		{
			DataPoint dp=datapointList.get(i);
			//PrintOneEntityHyperEdge(ps,dp.query);
			//ps.print("\t"+dp.query.value+"\t"+dp.nodeList.get(0)+":"+dp.countList.get(0));
			ps.print(dp.query.value);
			if(dp.nodeList.size()>0)
				ps.print("\t"+dp.nodeList.get(0)+":"+dp.countList.get(0).trueCount+":"+dp.countList.get(0).falseCount);
			for(int j=1;j<dp.nodeList.size();j++)
			{
				ps.print("\t"+dp.nodeList.get(j)+":"+dp.countList.get(j).trueCount+":"+dp.countList.get(j).falseCount);
			}
			ps.println();
		}
	}	
	
	
	/**
	 * Print a entitative hyper edge.
	 * @author Zhuoyu Wei
	 * @version 1.0 
	 * @param ps the PrintStream.
	 * @param he HyperEdge.
	 */
	public static void PrintOneEntityHyperEdge(PrintStream ps,HyperEdge he)
	{
		ps.print(GlobalStaticData.relMapItoS.get(he.rel)+"("+GlobalStaticData.entityMapItoS.get(he.nodeList.get(0)));
		for(int i=1;i<he.nodeList.size();i++)
		{
			ps.print(","+GlobalStaticData.entityMapItoS.get(he.nodeList.get(i)));
		}
		ps.print(")");
	}
	/**
	 * Print a conceptual hyper edge.]
	 * @author Zhuoyu Wei
	 * @verison 1.0
	 * @param ps the PrintStream
	 * @param he HyperEdge.
	 */
	public static void PrintOneConceptHyperEdge(PrintStream ps,HyperEdge he)
	{
		Integer code=he.nodeList.get(0);
		Integer type=code%GlobalStaticData.typeMapItoS.size();
		Integer index=code/GlobalStaticData.typeMapItoS.size();
		ps.print(GlobalStaticData.relMapItoS.get(he.rel)+"("+GlobalStaticData.typeMapItoS.get(type)+index);
		for(int i=1;i<he.nodeList.size();i++)
		{
			code=he.nodeList.get(i);
			type=code%GlobalStaticData.typeMapItoS.size();
			index=code/GlobalStaticData.typeMapItoS.size();
			ps.print(","+GlobalStaticData.typeMapItoS.get(type)+index);
		}
		ps.print(")");
	}	
	/**
	 * Print a entitative hyper path by call PrintOneEntityHyperEdge()
	 * @author Zhuoyu Wei
	 * @verison 1.0
	 * @param ps the PrintStream
	 * @param hp the HyperPath
	 */	
	public static void PrintOneEntityHyperPath(PrintStream ps,HyperPath hp)
	{
		for(int i=0;i<hp.path.size()-1;i++)
		{
			PrintOneEntityHyperEdge(ps,hp.path.get(i));
			if(i<hp.path.size()-2)
			{
				ps.print("^");
			}
		}
		ps.print("=>");
		PrintOneEntityHyperEdge(ps,hp.path.get(hp.path.size()-1));
	}
	/**
	 * Print a conceptual hyper path by call PrintOneConceptHyperEdge()
	 * @author Zhuoyu Wei
	 * @verison 1.0
	 * @param ps the PrintStream
	 * @param hp the HyperPath
	 */		
	public static void PrintOneConceptHyperPath(PrintStream ps,HyperPath hp)
	{
		for(int i=0;i<hp.path.size()-1;i++)
		{
			PrintOneConceptHyperEdge(ps,hp.path.get(i));
			if(i<hp.path.size()-2)
			{
				ps.print("^");
			}
		}
		ps.print("=>");
		PrintOneConceptHyperEdge(ps,hp.path.get(hp.path.size()-1));
	}	
	
	/**
	 * Print all the conceptual formulas with their indexs
	 * @author Zhuoyu Wei
	 * @version 1.0 
	 * @param ps
	 * @param hpList
	 */
	public static void PrintConceptualFormula(PrintStream ps,List<HyperPath> hpList)
	{
		for(int i=0;i<hpList.size();i++)
		{
			ps.print(i+"\t");
			HyperPath hp=hpList.get(i);
			PrintOneConceptHyperPath(ps,hp);
			ps.println();
		}
	}
	public static void PrintConceptualFormula(PrintStream ps,List<HyperPath> hpList,Double[] weights)
	{
		for(int i=0;i<hpList.size();i++)
		{
			ps.print(i+"\t");
			HyperPath hp=hpList.get(i);
			PrintOneConceptHyperPath(ps,hp);
			ps.println("\t"+weights[i]);
		}
	}	
	
	
	/**
	 * Simply print out the weights learned
	 * @param ps
	 * @param weights
	 */
	public static void PrintWeights(PrintStream ps,Double[] weights)
	{
		for(int i=0;i<weights.length;i++)
		{
			ps.println(weights[i]);
		}
	}
	
	/**
	 * Print the final results: the probability of that query is true, and the cll for each query.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param probability the probability list.
	 * @param dpList the data point for test list.
	 * @param ps_cll the print stream of cll
	 * @param ps_pro the print stream of probability
	 * @param ps_view the print stream of cll, probability and the query's truly truth value at user view. 
	 * @param ps_expcll for the users, the exp(cll), it looks directly.
	 */
	public static void PrintCLLAndPro(Double[] probability,List<DataPoint> dpList,
			PrintStream ps_cll,PrintStream ps_pro,PrintStream ps_view,PrintStream ps_expcll,PrintStream ps_log)
	{
		//double CLL;
		//ps_pro.println("test");
		double cllsum=0.;
		for(int i=0;i<dpList.size();i++)
		{
			DataPoint dp=dpList.get(i);
			double cll=dp.query.value>0.5?probability[i]:(1-probability[i]);
			if(ps_expcll!=null)
			{
				ps_expcll.println(cll);
			}
			cll=Math.log(cll);
			cllsum+=cll;
			ps_cll.println(cll);
			int label=dp.query.value>0.5?1:0;
			ps_pro.println(probability[i]+" "+label);
			PrintOneEntityHyperEdge(ps_view,dp.query);
			ps_view.println("\t"+dp.query.value+"\t"+probability[i]+"\t"+cll);
		}
		if(dpList.size()>0)
			cllsum/=dpList.size();
		else
			cllsum=0.;
		ps_log.println("Cll is "+cllsum);
	}
	
	public static void PrintGlobalStaticDataInfo(PrintStream ps)
	{
		ps.println("relation(info) map size: "+GlobalStaticData.relMapStoI.size());
		ps.println("relation(info) list size: "+GlobalStaticData.relMapItoS.size());		
		ps.println("relation(info) hyperedge list size: "+GlobalStaticData.conceptRelList.size());		
		
		ps.println("entity's type(db's type or concept) map size: "+GlobalStaticData.typeMapStoI.size());
		ps.println("entity's type(db's type or concept) map size: "+GlobalStaticData.typeMapStoI.size());		
		
		ps.println("entity(db) map size: "+GlobalStaticData.entityMapStoI.size());
		ps.println("entity(db) list size: "+GlobalStaticData.entityMapItoS.size());
		ps.println("entity(db) node list size: "+GlobalStaticData.entityToNode.size());		
		
	}
	

	public static void PrintFormulaNoZero(PrintStream ps,List<HyperPath> conceptforList,Double[] weights)
	{
		for(int i=0;i<weights.length;i++)
		{
			if(Math.abs(weights[i])>1e-6)
			{
				OutputData.PrintOneConceptHyperPath(ps,conceptforList.get(i));
				ps.println();
			}
		}
	}
	

	public static void PrintBigQueryResult(String filename,String filename2,List<BigQuery> bqList)
	{
		PrintStream ps;
		PrintStream ps2;
		try {
			ps = new PrintStream(filename);
			ps2=new PrintStream(filename2);
		for(int i=0;i<bqList.size();i++)
		{
			ps.print(GlobalStaticData.entityMapItoS.get(bqList.get(i).entity)+"\t");
			ps2.print(GlobalStaticData.entityMapItoS.get(bqList.get(i).entity)+"\t");			
			ps.print(GlobalStaticData.relMapItoS.get(bqList.get(i).rel)+"\t");
			ps2.print(GlobalStaticData.relMapItoS.get(bqList.get(i).rel)+"\t");			
			if(bqList.get(i).entityList.size()>0)
			{
				int j;
				for(j=0;j<bqList.get(i).entityList.size()-1;j++)
				{
					ps.print(GlobalStaticData.entityMapItoS.get(bqList.get(i).entityList.get(j))+";");
					ps2.print(GlobalStaticData.entityMapItoS.get(bqList.get(i).entityList.get(j))+"("+
							bqList.get(i).debugScore.get(j)+")"+";");					
				}
				ps.println(GlobalStaticData.entityMapItoS.get(bqList.get(i).entityList.get(j)));
				ps2.println(GlobalStaticData.entityMapItoS.get(bqList.get(i).entityList.get(j))+"("+
						bqList.get(i).debugScore.get(j)+")");				
			}
			else
			{
				ps.println();
				ps2.println();
			}
		}
		ps.close();
		ps2.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//by wzy, 11.3
	public static void logprint_FormulaAndWeigh(List<String> relList,Map<Integer,List<HyperPath>> relToFormula
			,Map<Integer,Learner> learnmap,String basedir) throws FileNotFoundException
	{
		class StringAndScore implements Comparator
		{
			Integer index;
			Double score;
			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				StringAndScore s1=(StringAndScore)o1;
				StringAndScore s2=(StringAndScore)o2;
				if(Math.abs(s1.score)==Math.abs(s2.score))
					return 0;
				if(Math.abs(s1.score)>Math.abs(s2.score))
					return -1;
				else
					return 1;
			}
		}
		for(int i=0;i<relList.size();i++)
		{
			List<HyperPath> formulas=relToFormula.get(i);
			if(formulas==null)
				continue;
			Learner learner=learnmap.get(i);
			if(learner==null)
				continue;
			List<StringAndScore> ssList=new ArrayList<StringAndScore>();
			if(formulas.size()!=learner.getWeights().length)
			{
				System.out.println("rel "+relList.get(i)+" has different number of weights and formulas: " +
						formulas.size()+" "+learner.getWeights().length);
			}
			for(int j=0;j<formulas.size();j++)
			{
				StringAndScore ss=new StringAndScore();
				ss.index=j;
				ss.score=learner.getWeights()[j];
				ssList.add(ss);
			}
			Collections.sort(ssList,new StringAndScore());
			PrintStream ps=new PrintStream(basedir+"/"+i+"_"+relList.get(i)+".log");
			for(int j=0;j<ssList.size();j++)
			{
				if(Math.abs(ssList.get(j).score)<1e-5)
					break;
				ps.print(ssList.get(j).score+"\t");
				OutputData.PrintOneConceptHyperPath(ps, formulas.get(ssList.get(j).index));
				ps.println();
			}
			ps.close();
		}
	}
	
	public static void logprint_inferdp(List<InferOneBigQueryThread> alThreads,String basedir,boolean isLeft) throws FileNotFoundException
	{
		for(int i=0;i<alThreads.size();i++)
		{
			InferOneBigQueryThread iob=alThreads.get(i);
			PrintStream ps=new PrintStream(basedir+GlobalStaticData.entityMapItoS.get(iob.bigquery.entity)
					+"_"+GlobalStaticData.relMapItoS.get(iob.bigquery.rel)+".log");
/*			for(int j=0;j<iob.bigquery.entityList.size();j++)
			{
				HyperEdge query=new HyperEdge();
				query.rel=iob.bigquery.rel;
				if(isLeft)
				{
					query.nodeList.add(iob.bigquery.entity);
					query.nodeList.add(iob.bigquery.entityList.get(j));
				}
				else
				{
					query.nodeList.add(iob.bigquery.entityList.get(j));		
					query.nodeList.add(iob.bigquery.entity);					
				}
				OutputData.PrintOneEntityHyperEdge(ps, query);
				Map<>
			}*/
			for(int j=0;j<iob.dpList.size();j++)
			{
				OutputData.PrintOneEntityHyperEdge(ps, iob.dpList.get(j).query);
				ps.print("\t");
				for(int k=0;k<iob.dpList.get(j).countList.size();k++)
				{
					ps.print(iob.dpList.get(j).nodeList.get(k)+":"+
							iob.dpList.get(j).countList.get(k).trueCount+":"+
							iob.dpList.get(j).countList.get(k).falseCount+" ");
				}
				ps.println();
			}
			ps.println("***************************************************");
			logprint_queryToCount(ps,iob.queryToCount);
			ps.close();
		}
	}
	
	public static void logprint_queryToCount(PrintStream ps,
			Map<HyperEdge,Map<HyperPath,TrueFalseCount>> queryToCount)
	{
		ps.println("Query size: "+queryToCount.size());
		Iterator it=queryToCount.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			HyperEdge query=(HyperEdge)entry.getKey();
			Map<HyperPath,TrueFalseCount> amap=(Map<HyperPath,TrueFalseCount>)entry.getValue();
			OutputData.PrintOneEntityHyperEdge(ps, query);
			ps.println("\t"+amap.size());
		}
	}
	
	public static void logprint_randomwalkpath(StatePOG state,PrintStream ps)
	{
		for(int i=0;i<state.state.size();i++)
			ps.print(GlobalStaticData.entityMapItoS.get(state.state.get(i))+"\t");
		ps.println();
	}
	
	public static void PrintAllScoresbyfiles(String dir,String filename)
	{
		for(int i=0;i<GlobalStaticData.taskThreads.size();i++)
		{
			OneRelTaskThread or=(OneRelTaskThread)GlobalStaticData.taskThreads.get(i);
			try {
				PrintStream ps=new PrintStream(dir+"/"+GlobalStaticData.relMapItoS.get(or.rel)
						+"/"+filename);
				for(int j=0;j<or.scores.length;j++)
				{
					ps.println(or.scores[j]);
				}
				ps.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

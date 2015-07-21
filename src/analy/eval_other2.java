package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wzy.model.DataPoint;
import auc.AUCCalculator;

public class eval_other2 {
	
	public List<DataPoint> test_dp_list=new ArrayList<DataPoint>();
	
	public Map<String,Double> queryToPro=new HashMap<String,Double>();
	public Map<String,Boolean> queryToTruth=new HashMap<String,Boolean>();
	
	public void ReadTestQuery(String filename)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[\\s]+");
				String query=ss[0];
				Double score=Double.parseDouble(ss[1]);
				//System.out.println(ss[0]+"\n"+score);
				queryToPro.put(query, score);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double ReadAnwserAndCreate(String filename,String aucfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		PrintStream ps=new PrintStream(aucfile);
		double cll=0.;
		int count=0;
		//double auc=0.;
		
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			count++;
			String[] ss=buffer.split("\t");
			String query=ss[0];
			Double score=Double.parseDouble(ss[1]);
			boolean flag=score>0.5;
			
			Double testScore=queryToPro.get(query);
			if(testScore==null)
			{
				testScore=10e-5;
			}
			
				if(flag)
				{
					
					ps.println(testScore+"\t1");
					cll+=Math.log(testScore);
				}
				else
				{
					ps.println(testScore+"\t0");
					cll+=Math.log(1.-testScore);
				}
			
			
		}
		ps.close();
		br.close();
		if(count<=0)
		{
			cll=Math.log(0);
		}
		else
		{
			cll/=count;
			//cll=Math.log(cll);
		}
		
		return cll;
	}
	

	public void ReadandCalculateFscoreStep1(String filename,Double Fthreshold)
	{
		List<Double> valueList=new ArrayList<Double>();
		List<String> formulaList=new ArrayList<String>();
		List<SortQueryString> sqsList=new ArrayList<SortQueryString>();
		double sum=0.;
		double maxValue=-1.;
		double minValue=9999.;
		
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[\\s]+");
				double value=Double.parseDouble(ss[1]);
				SortQueryString sqs=new SortQueryString();
				sqs.str=ss[0];
				sqs.value=value;
				sqsList.add(sqs);
			}
			
			Collections.sort(sqsList,new SortQueryString());
			int total=(int)(sqsList.size()*Fthreshold);
			int i;
			for(i=0;i<total;i++)
			{
				queryToTruth.put(sqsList.get(i).str,true);
			}
			for(;i<sqsList.size();i++)
			{
				queryToTruth.put(sqsList.get(i).str,false);
			}			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double ReadandCalculateFscoreStep2(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		double cll=0.;
		int count=0;
		int tp=0;
		int fp=0;
		int fn=0;
		//double auc=0.;
		
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			count++;
			String[] ss=buffer.split("\t");
			String query=ss[0];
			Double score=Double.parseDouble(ss[1]);
			boolean flag=score>0.5;
			
			Boolean value=queryToTruth.get(ss[0]);
			if(value)
			{
				if(flag)
					tp++;
				else
					fp++;
			}
			else
			{
				if(flag)
					fn++;
			}
		}
		
		double p=(double)tp/(double)(tp+fp);
		double r=(double)tp/(double)(tp+fn);
		
		double f1=2*p*r/(p+r);
		
		return f1;
	}
	
	public static double[] ReadLogAndPrintResult(String filename)
	{
		double[] auc=new double[2];
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String cll=br.readLine();
			String prauc=br.readLine();
			String rocauc=br.readLine();
			
			/*String pattern="[0-9.-]+";
			Pattern p=Pattern.compile(pattern);
			Matcher matcher1=p.matcher(prauc);
			Matcher matcher2=p.matcher(rocauc);
			matcher1.find();
			matcher2.find();
			auc[0]=Double.parseDouble(matcher1.group());
			auc[1]=Double.parseDouble(matcher2.group());
			*/
			
			String[] pr=prauc.split("[\\s]+");
			auc[0]=Double.parseDouble(pr[pr.length-1]);
			String[] roc=rocauc.split("[\\s]+");
			auc[1]=Double.parseDouble(roc[roc.length-1]);
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return auc;
		
	}
	
	
	public static void SplitResultForWordnet(String inputfile,String resultfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			PrintStream ps=new PrintStream(resultfile);
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				ps.println(ss[0]+"\t"+ss[2]);
			}
			ps.close();
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void Main(String args0,String args1,String args2,String args3,PrintStream psres) throws IOException
	{
		eval_other eo=new eval_other();
		eo.ReadTestQuery(args0);
		double cll=eo.ReadAnwserAndCreate(args1, args2);
		PrintStream pslog=new PrintStream(args3);
		//PrintStream psres=new PrintStream(args[4]);
		pslog.println("cll: "+cll);
		
		/*eo.ReadandCalculateFscoreStep1(args0,0.3);
		double f1_3=eo.ReadandCalculateFscoreStep2(args1);		
		eo.queryToTruth.clear();		
		eo.ReadandCalculateFscoreStep1(args0,0.5);
		double f1_5=eo.ReadandCalculateFscoreStep2(args1);
		eo.queryToTruth.clear();
		eo.ReadandCalculateFscoreStep1(args0,0.7);
		double f1_7=eo.ReadandCalculateFscoreStep2(args1);	*/
		
		AUCCalculator.CalculateAUC(args2, "list", pslog);
		
		
		
		 double[] aucres=ReadLogAndPrintResult(args3);
		 psres.println(String.format("%.6f", cll)+"\t"+String.format("%.6f",aucres[0])+"\t"+
				 String.format("%.6f", aucres[1])+"\t0"+"\t0"+"\t0");
		 //psres.close();
	}
	
	public static String OneFileProcess(String resultfile,String queryfile,String aucfile) throws IOException
	{
		eval_other eo=new eval_other();
		eo.ReadTestQuery(resultfile);
		double cll=eo.ReadAnwserAndCreate(queryfile, aucfile);
		return String.format("%.6f", cll);
	}
	
	
	Set<String> trueSet=new HashSet<String>();
	public void ReadAnswer(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			Double score=Double.parseDouble(ss[1]);
			if(score>0.5)
				trueSet.add(ss[0]);
			
 		}
		br.close();
	}
	
	public double ReadTestResultAndPrint(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		PrintStream ps=new PrintStream(outputfile);
		String buffer=null;
		int count=0;
		double cll=0.;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			count++;
			String[] ss=buffer.split("[\\s]+");
			String query=ss[0];
			Double testScore=Double.parseDouble(ss[1]);
			boolean flag=this.trueSet.contains(query);


			
			if(flag)
			{
				ps.println(testScore+"\t1");
				cll+=Math.log(testScore);
			}
			else
			{
				ps.println(testScore+"\t0");
				cll+=Math.log(1.-testScore);
			}
			
			
		}
		ps.close();
		br.close();
		if(count<=0)
		{
			cll=Math.log(0);
		}
		else
		{
			cll/=count;
			//cll=Math.log(cll);
		}
		
		return cll;
		
	}
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("EVAL is staring!");
		eval_other2 eo=new eval_other2();
		eo.ReadAnswer(args[1]);
		double cll=eo.ReadTestResultAndPrint(args[0], args[2]);
		PrintStream pslog=new PrintStream(args[3]);
		//PrintStream psres=new PrintStream(args[4]);
		pslog.println("cll: "+cll);
		
/*		eo.ReadandCalculateFscoreStep1(args[0],0.3);
		double f1_3=eo.ReadandCalculateFscoreStep2(args[1]);		
		eo.queryToTruth.clear();		
		eo.ReadandCalculateFscoreStep1(args[0],0.5);
		double f1_5=eo.ReadandCalculateFscoreStep2(args[1]);
		eo.queryToTruth.clear();
		eo.ReadandCalculateFscoreStep1(args[0],0.7);
		double f1_7=eo.ReadandCalculateFscoreStep2(args[1]);*/		
		
		AUCCalculator.CalculateAUC(args[2], "list", pslog);
		
		
		
		 double[] aucres=ReadLogAndPrintResult(args[3]);
		 PrintStream psres=new PrintStream(args[4]);
		 psres.println(String.format("%.6f", cll)+"\t"+String.format("%.6f",aucres[0])+"\t"+
				 String.format("%.6f", aucres[1])+"\t0"+"\t0"+"\t0");
		 psres.close();
		
		 
		 
		 
	}
	
	
	public static List<String> Readfilenames(String filefile)
	{	
		List<String> result=new ArrayList<String>();
		try {
			BufferedReader br=new BufferedReader(new FileReader(filefile));
			String buffer=null;
		
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				result.add(buffer);
			}
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
}

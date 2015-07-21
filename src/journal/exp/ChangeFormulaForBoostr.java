package journal.exp;

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

import wzy.func.FileTools;

import analy.ChangeClauseToMLN_learned;

public class ChangeFormulaForBoostr {

	

	
	public List<String> clauseList=new ArrayList<String>();
	public List<String> changedClauseList=new ArrayList<String>();
	public List<Double> weights=new ArrayList<Double>();
	

	
	public String[] ChangeHyperEdge(String s,Map<String,Integer> tokenMap)
	{
		//System.out.println(s);
		String[] result=new String[2];
		String[] ss=s.split("[()]+");
		String[] tokens=ss[1].split(",");
		StringBuilder sb=new StringBuilder();
		StringBuilder sb2=new StringBuilder();	
		
		Integer index=tokenMap.get(tokens[0]);
		if(index==null)
		{
			tokenMap.put(tokens[0], tokenMap.size()+1);
			index=tokenMap.size();
		}
		sb.append(ss[0]+"("+tokens[0].charAt(0)+tokens[0].charAt(tokens[0].length()-1));
		sb2.append(ss[0]+"(a"+index);		
		//List<String> tokenList=new ArrayList<String>();
		for(int i=1;i<tokens.length;i++)
		{
			String t1=tokens[i].charAt(0)+""+tokens[i].charAt(tokens[i].length()-1);
			index=tokenMap.get(tokens[i]);
			if(index==null)
			{
				tokenMap.put(tokens[i], tokenMap.size()+1);
				index=tokenMap.size();
			}
			String t2="a"+index;
			
			//tokenList.add(t);
			sb.append(","+t1);
			sb2.append(","+t2);
		}
		sb.append(")");
		sb2.append(")");
		result[0]=sb.toString();
		result[1]=sb2.toString();
		return result;
	}
	

	
	public void ReadClauseAndChange(String clausefile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(clausefile));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				String formular=ss[1];
				//System.out.println(formular);
				String[] sss=formular.split("=>");
				//System.out.println(sss[0]+"\t"+sss[1]);
				String body=sss[0];
				String head=sss[1];
				
				
				String[] edges=body.split("\\^");
				//System.out.println(edges.length);
				StringBuilder sb=new StringBuilder();
				StringBuilder sb2=new StringBuilder();
				Map<String,Integer> tokenMap=new HashMap<String,Integer>();
				for(int i=0;i<edges.length;i++)
				{
					if(edges[i].length()<2)
						continue;
					String[] t=ChangeHyperEdge(edges[i],tokenMap);
					sb.append("!"+t[0]+" v ");
					sb2.append("!"+t[1]+" v ");
				}
				String[] t=ChangeHyperEdge(head,tokenMap);
				sb.append(t[0]);
				sb2.append(t[1]);
				
				clauseList.add(sb.toString());
				changedClauseList.add(sb2.toString());
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void ReadWeights(String weightfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(weightfile));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				Double score=Double.parseDouble(ss[0]);
				weights.add(score);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void PrintHead(String mlnfile,PrintStream ps)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(mlnfile));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				ps.println(buffer);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ReadClauseAndWeightsToPrint(String clausefile,String weightfile,String mlnfile,String outputfile)
	{
		
		ChangeFormulaForBoostr cffb=new ChangeFormulaForBoostr();
		cffb.ReadClauseAndChange(clausefile);
		cffb.ReadWeights(weightfile);
		if(cffb.clauseList.size()!=cffb.changedClauseList.size()||cffb.changedClauseList.size()!=cffb.weights.size())
		{
			System.out.println("weights and clause have different numbers. "
					+cffb.clauseList.size()+" "+cffb.changedClauseList.size()+" "+cffb.weights.size());
			return;
		}
		try {
			//DecimalFormat df=new DecimalFormat("#.000000");
			PrintStream ps=new PrintStream(outputfile);
			PrintHead(mlnfile,ps);
			for(int i=0;i<cffb.clauseList.size();i++)
			{
				if(Math.abs(cffb.weights.get(i))<1e-10)
					continue;
				ps.println("//\t"+cffb.weights.get(i)+"\t"+cffb.clauseList.get(i));
				ps.println(String.format("%.6f", cffb.weights.get(i))+"\t"+cffb.changedClauseList.get(i));
				ps.println();
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void ReadFileNamse(String file,List<String> names)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(file));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				names.add(buffer);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	

	
	public static void CopyFile(String input,String output)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(input));
			PrintStream ps=new PrintStream(output);
			
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				//if(buffer.length()<2)
					//continue;
				ps.println(buffer);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public static void ReadAndPrint(String inputfile,String outputfile,String query)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			PrintStream ps=new PrintStream(outputfile);
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[()]+");
				if(ss[0].equals(query))
					continue;
				ps.println(buffer);
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
	
	
	public static void tempGetQuery(String inputname,String outputname)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputname));
			PrintStream ps=new PrintStream(outputname);
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				ps.println(ss[0]);
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
	
	
	public static void CreateQuery(String filename,PrintStream ps) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			ps.println(ss[0]);
		}
		ps.close();
		br.close();
	}
	
	public static void RemoveRelation_(String inputfile,String outputfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			String buffer=null;
			PrintStream ps=new PrintStream(outputfile);
			while((buffer=br.readLine())!=null)
			{
				String temp=buffer.replace('_', 'r');
				ps.println(temp);
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
	
	
	public static void FilterForBoostr(String inputfile,String outputfile)
	{
		//Set<String> relSet=new HashSet<String>();
		try {
			BufferedReader br=new BufferedReader(new FileReader(inputfile));
			PrintStream ps=new PrintStream(outputfile);
			String buffer=null;
			//int line=0;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				/*if(line<25||line%2==0)
				{
					line++;
					continue;
				}
				else
					line++;*/
				
				
				//System.out.println(buffer);
				buffer=buffer.replaceAll("sameperson", "samePerson");
				//System.out.println(buffer);
				buffer=buffer.replaceAll("courseta", "courseTA");
				//System.out.println(buffer);
				buffer=buffer.replaceAll("courseprof", "courseProf");
				
				buffer=buffer.replaceAll("hasposition", "hasPosition");
				buffer=buffer.replaceAll("samephase", "samePhase");
				buffer=buffer.replaceAll("advisedby", "advisedBy");
				buffer=buffer.replaceAll("sameproject", "sameProject");				
				buffer=buffer.replaceAll("samecourse", "sameCourse");						
				buffer=buffer.replaceAll("yearsinprogram", "yearsInProgram");						
				buffer=buffer.replaceAll("inphase", "inPhase");						
				buffer=buffer.replaceAll("sameposition", "samePosition");	
				buffer=buffer.replaceAll("sameperson", "samePerson");	
				buffer=buffer.replaceAll("samequarter", "sameQuarter");					
				buffer=buffer.replaceAll("samelevel", "sameLevel");					
				buffer=buffer.replaceAll("taughtby", "taughtBy");	
				buffer=buffer.replaceAll("tempadvisedby", "tempAdvisedBy");
				buffer=buffer.replaceAll("tempadvisedBy", "tempAdvisedBy");	
				buffer=buffer.replaceAll("tempAdvisedby", "tempAdvisedBy");					
				buffer=buffer.replaceAll("introcourse", "introCourse");					
				buffer=buffer.replaceAll("projectmember", "projectMember");	
				buffer=buffer.replaceAll("courselevel", "courseLevel");	
				buffer=buffer.replaceAll("sameinteger", "sameInteger");		
				buffer=buffer.replaceAll("sametitle", "sameTitle");						
	
			
				/*//System.out.println(buffer);
				String[] ss=buffer.split("\t");
				String[] sss=ss[1].split(" v ");
				for(int i=0;i<sss.length;i++)
				{
					String[] ssss=sss[i].split("[()]+");
					relSet.add(ssss[0]);
				}
				*/
				
				//System.out.println(buffer);
				ps.println(buffer);
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		/*Iterator it=relSet.iterator();
		while(it.hasNext())
		{
			System.out.println((String)it.next());
		}*/
		
	}
	
	public static void main(String[] args)
	{
		//ChangeFormulaForBoostr cffb=new ChangeFormulaForBoostr();
		/*ChangeFormulaForBoostr.ReadClauseAndWeightsToPrint("C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\webkb_boostr_results\\webkb_ta_result\\train4\\models\\bRDNs\\formulas.mln",
				"C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\webkb_boostr_results\\webkb_ta_result\\train4\\models\\bRDNs\\formulas.mln",
				"C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\forinfer.mln",
				"C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\webkb_boostr_mcsat\\weights.t.mln");
		ChangeFormulaForBoostr.FilterForBoostr("C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\webkb_boostr_mcsat\\weights.t.mln",
				"C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\webkb_boostr_mcsat\\weights.mln");*/
		
		
		ChangeFormulaForBoostr.ReadClauseAndWeightsToPrint("C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\uw_boostr\\results\\6.10 results\\uw-cse_rdn_temp\\train4_advisedby\\models\\bRDNs\\formulas.mln",
				"C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\uw_boostr\\results\\6.10 results\\uw-cse_rdn_temp\\train4_advisedby\\models\\bRDNs\\formulas.mln",
				"C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\forinfer.mln",
				"C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\uw_boostr_mcsat\\weights.t.mln");
		ChangeFormulaForBoostr.FilterForBoostr("C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\uw_boostr_mcsat\\weights.t.mln",
				"C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\uw_boostr_mcsat\\weights.mln");	
		
		
	}
	

	
	
}

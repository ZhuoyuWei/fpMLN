package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wzy.func.FileTools;
import wzy.stat.GlobalStaticData;

public class ChangeClauseToMLN_learned {

	
	public List<String> clauseList=new ArrayList<String>();
	public List<String> changedClauseList=new ArrayList<String>();
	public List<Double> weights=new ArrayList<Double>();
	
	public static String[] onequery=new String[4];
	
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
	
	public String CheckAndChangeOneVariable(String s)
	{
		//System.out.println(s);
		String[] ss=s.split("[()]+");
		String[] tokens=ss[1].split(",");
		boolean flag=false;
		for(int i=0;i<4;i++)
		{
			//System.out.println();
			if(onequery[i].equals(ss[0]))
			{
				flag=true;
				break;
			}
			
		}
		if(flag)
			return ss[0]+"("+tokens[0]+")";
		else
			return s;
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
				String head=CheckAndChangeOneVariable(sss[1]);
				
				
				String[] edges=body.split("\\^");
				//System.out.println(edges.length);
				StringBuilder sb=new StringBuilder();
				StringBuilder sb2=new StringBuilder();
				Map<String,Integer> tokenMap=new HashMap<String,Integer>();
				for(int i=0;i<edges.length;i++)
				{
					if(edges[i].length()<2)
						continue;
					edges[i]=CheckAndChangeOneVariable(edges[i]);
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
		
		ChangeClauseToMLN_learned ccml=new ChangeClauseToMLN_learned();
		ccml.ReadClauseAndChange(clausefile);
		ccml.ReadWeights(weightfile);
		System.out.println(ccml.clauseList.size()+"formulas");
		if(ccml.clauseList.size()!=ccml.changedClauseList.size()||ccml.changedClauseList.size()!=ccml.weights.size())
		{
			System.out.println("weights and clause have different numbers. "
					+ccml.clauseList.size()+" "+ccml.changedClauseList.size()+" "+ccml.weights.size());
			return;
		}
		try {
			
			List<ValueAndTwoString> vtsList=new ArrayList<ValueAndTwoString>();
			for(int i=0;i<ccml.clauseList.size();i++)
			{
				ValueAndTwoString vats=new ValueAndTwoString();
				vats.value=ccml.weights.get(i);
				vats.s1=ccml.clauseList.get(i);
				vats.s2=ccml.changedClauseList.get(i);
				
				vtsList.add(vats);
			}
			Collections.sort(vtsList,new ValueAndTwoString());
			ccml.weights=new ArrayList<Double>();
			ccml.clauseList=new ArrayList<String>();
			ccml.changedClauseList=new ArrayList<String>();
			int MaxFormulaCount=10;
			for(int i=0;i<vtsList.size();i++)
			{
				ValueAndTwoString vts=vtsList.get(i);
				ccml.weights.add(vts.value);
				ccml.clauseList.add(vts.s1);
				ccml.changedClauseList.add(vts.s2);
				if(ccml.weights.size()>=MaxFormulaCount)
				{
					//break;
				}
			}
			
			
			
			//DecimalFormat df=new DecimalFormat("#.000000");
			PrintStream ps=new PrintStream(outputfile);
			PrintHead(mlnfile,ps);
			
			int reallyCount=0;
			for(int i=0;i<ccml.clauseList.size();i++)
			{
				if(Math.abs(ccml.weights.get(i))<1e-3)
					continue;
				reallyCount++;
				ps.println("//\t"+ccml.weights.get(i)+"\t"+ccml.clauseList.get(i));
				ps.println(String.format("%.6f", ccml.weights.
						get(i))+"\t"+ccml.changedClauseList.get(i));
				ps.println();
			}
			System.out.println(reallyCount+"formulas after filter");
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
	
	public static void main(String[] args) throws IOException
	{
		/*ReadClauseAndWeightsToPrint("C:\\Users\\Administrator\\Documents\\query\\advisedBy\\test_ai\\mid\\conceptualFormula.user",
				"C:\\Users\\Administrator\\Documents\\query\\advisedBy\\test_ai\\mid\\weights.db",
				"C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\forinfer.mln",
				"C:\\Users\\Administrator\\Documents\\query\\advisedBy\\test_ai\\mid\\weights.db.mln");*/
		
		onequery[0]="student";
		onequery[1]="professor";
		onequery[2]="introCourse";
		onequery[3]="faculty";
		
		
		List<String>[] filenameList=new List[4];
		
		for(int i=0;i<4;i++)
		{
			filenameList[i]=new ArrayList<String>();
			ReadFileNamse("C:\\Users\\Administrator\\Desktop\\mln_data\\queryquery1\\"+(i+1)+".txt",filenameList[i]);
		}
		for(int i=0;i<filenameList[0].size();i++)
		{
			FileTools.CreateNewFolder(filenameList[3].get(i));
			ReadClauseAndWeightsToPrint(filenameList[0].get(i),
					filenameList[1].get(i),
					filenameList[2].get(i),
					filenameList[3].get(i)+"\\weights.mln");
			RemoveRelation_(filenameList[3].get(i)+"\\weights.mln",filenameList[3].get(i)+"\\weights.mln.1");
		}
		
		/*List<String>[] filenameList=new List[2];
		for(int i=0;i<2;i++)
		{
			filenameList[i]=new ArrayList<String>();
			ReadFileNamse("C:\\Users\\Administrator\\Desktop\\mln_data\\queryquery1\\"+(i+5)+".txt",filenameList[i]);
		}*/		
		/*List<String> filenameList=new ArrayList<String>();
		ReadFileNamse("C:\\Users\\Administrator\\Desktop\\mln_data\\queryquery1\\6.txt",filenameList);
		for(int i=0;i<filenameList.size();i++)
		{
			//FileTools.CreateNewFolder(filenameList[1].get(i));
			CopyFile("C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet_no\\test.query.1",filenameList.get(i)+"\\test.query");
			CopyFile("C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet_no\\test.query_noresult.1",filenameList.get(i)+"\\test.query_noresult");
			CopyFile("C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet_no\\test.db.1",filenameList.get(i)+"\\test.db");
			
		}*/
		
/*		List<String>[] filenameList=new List[2];
		for(int i=0;i<2;i++)z
		{
			filenameList[i]=new ArrayList<String>();
			ReadFileNamse("C:\\Users\\Administrator\\Desktop\\mln_data\\queryquery\\"+(i+7)+".txt",filenameList[i]);
		}		
		
		for(int i=0;i<filenameList[0].size();i++)
		{
			FileTools.CreateNewFolder(filenameList[1].get(i));

			CopyFile(filenameList[0].get(i),filenameList[1].get(i)+"\\test.query");
		}*/
		
		/*List<String>[] filenameList=new List[3];
		for(int i=0;i<3;i++)
		{
			filenameList[i]=new ArrayList<String>();
			ReadFileNamse("C:\\Users\\Administrator\\Desktop\\mln_data\\queryquery\\"+(i+9)+".txt",filenameList[i]);
		}		
		
		for(int i=0;i<filenameList[0].size();i++)
		{
			FileTools.CreateNewFolder(filenameList[1].get(i));
			ReadAndPrint(filenameList[0].get(i),
					filenameList[1].get(i)+"\\test.db",
					filenameList[2].get(i));
			
			
		}*/
		
		
	
		
		
		/*List<String> filenames=new ArrayList<String>();
		ReadFileNamse("C:\\Users\\Administrator\\Desktop\\mln_data\\queryquery\\10.txt",filenames);
		for(int i=0;i<filenames.size();i++)
		{
			tempGetQuery(filenames.get(i)+"\\test.query",filenames.get(i)+"\\test.query_noresult");
		}*/
		
		//PrintStream ps=new PrintStream("C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet\\test.query_noresult");
		//CreateQuery("C:\\Users\\Administrator\\Desktop\\mln_data\\Wordnet\\test.query",ps);
		
	}
	
}


class ValueAndTwoString implements Comparator
{
	public double value;
	public String s1;
	public String s2;
	
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		
		ValueAndTwoString v1=(ValueAndTwoString)o1;
		ValueAndTwoString v2=(ValueAndTwoString)o2;
		
		if(Math.abs(v1.value)>Math.abs(v2.value))
			return -1;
		else if(Math.abs(v1.value)<Math.abs(v2.value))
			return 1;
		else
			return 0;
		
	}
}

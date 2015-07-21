package exper.cora;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wzy.func.FileTools;

public class SubFormulas {

	
	List<String> formulaList=new ArrayList<String>();
	List<Double> weightList=new ArrayList<Double>();
	
	List<String> mln1List=new ArrayList<String>();
	List<String> mln2List=new ArrayList<String>();
	
	
	public static String[] onequery=new String[4];
	
	
	public void ReadFormulas(String inputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("\t");
			formulaList.add(ss[1]);
		}
	}
	public void ReadWeights(String inputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<1)
				continue;
			weightList.add(Double.parseDouble(buffer));
		}
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
	
	public void ChangeToSAT(String formular,Double weight)
	{
		
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
		
		mln1List.add(sb.toString());
		mln2List.add(sb2.toString());
		
		//return "//\t"+weight+"\t"+sb.toString()+"\n"+weight+"\t"+sb2.toString();
	}
	
	public void ChangeAll()
	{
		for(int i=0;i<this.formulaList.size();i++)
		{
			this.ChangeToSAT(this.formulaList.get(i), this.weightList.get(i));
		}
	}
	
	public void PrintHead(String mlnfile,PrintStream ps)
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
	
	public void PrintFormulas(String output1,String output2,String output3,int num,String mlnfile) throws FileNotFoundException
	{
		PrintStream ps1=new PrintStream(output1);
		PrintStream ps2=new PrintStream(output3);
		PrintStream ps3=new PrintStream(output2);
		
		PrintHead(mlnfile,ps2);
		
		if(num>this.weightList.size())
		{
			num=this.weightList.size();
		}
		
		for(int i=0;i<num;i++)
		{
			ps1.println((i+1)+"\t"+this.formulaList.get(i));
			ps3.println(String.format("%.6f", this.weightList.get(i)));
			ps2.println("//\t"+String.format("%.6f", this.weightList.get(i))+"\t"+this.mln1List.get(i));
			ps2.println(String.format("%.6f", this.weightList.get(i))+"\t"+this.mln2List.get(i));
			ps2.println();
		}
		
		
		
	}
	
	public static void main(String[] args) throws IOException
	{
		onequery[0]="student";
		onequery[1]="professor";
		onequery[2]="introCourse";
		onequery[3]="faculty";
		
		SubFormulas sf=new SubFormulas();
		
		sf.ReadFormulas("C:\\Users\\Administrator\\Desktop\\infer experiments\\result_formula_10\\SameVenue\\1\\mid\\conceptualFormula.user");
		sf.ReadWeights("C:\\Users\\Administrator\\Desktop\\infer experiments\\result_formula_10\\SameVenue\\1\\mid\\weights.db");
		sf.ChangeAll();
		
		
		int[] formulaSize={1,2,4,8,16,32,64,128,256,512};
		
		for(int i=0;i<formulaSize.length;i++)
		{
			String temp_dir="C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_formula_sub\\"+formulaSize[i]+"\\SameVenue\\";
			FileTools.CreateNewFolder(temp_dir);
			sf.PrintFormulas(temp_dir+"conceptualFormula.user", temp_dir+"weights.db",
					temp_dir+"weights.gibmc",formulaSize[i], 
					"C:\\Users\\Administrator\\Desktop\\infer experiments\\cora_fpMLN_mcsat\\forinfer.mln");
		}
		
		
		
		
		
	}
}

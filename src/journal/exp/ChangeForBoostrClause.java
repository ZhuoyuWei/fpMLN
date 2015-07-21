package journal.exp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChangeForBoostrClause {

	List<String> formulaList=new ArrayList<String>();
	
	public void ReadMLNAndChange(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			Double score=0.;
			try{
				score=Double.parseDouble(ss[0]);
			}catch(Exception e)
			{
				continue;
			}
			
			StringBuilder sb=new StringBuilder();
			for(int i=1;i<ss.length-2;i++)
			{
				sb.append(" "+ss[i]);
			}

			String formula=sb.toString();
			
			//System.out.println(formula);
			String[] token=formula.split(" v ");
			///System.out.println(token.length);
			//System.exit(0);
			Map<String,Integer> tokenToIndex=new HashMap<String,Integer>();
			List<String> tokenList=new ArrayList<String>();
			int count_=10;
			for(int i=0;i<token.length;i++)
			{
				//System.out.println(token[i]);
				token[i]=token[i].replaceAll("[\\s]+", "");
				String[] sss=token[i].split("[()]+");
				String rel=this.tokenToCase.get(sss[0]);
				//System.out.println(sss[0]);
				String[] entity=sss[1].split(",");
				for(int j=0;j<entity.length;j++)
				{
					String temp=entity[j].replaceAll("[\\s]+", "");
					Integer index=tokenToIndex.get(temp);
					if(index==null)
					{
						index=tokenToIndex.size();
						tokenToIndex.put(temp, index);
					}
				}
				StringBuilder sb_token=new StringBuilder();
				sb_token.append(rel+"(");
				for(int j=0;j<entity.length-1;j++)
				{
					String temp=entity[j].replaceAll("[\\s]+", "");
					Integer inttemp=tokenToIndex.get(temp);
			
					if(temp.indexOf("_")==0)
					{
						inttemp=tokenToIndex.size()+(count_++);
					}
					sb_token.append("a"+inttemp+", ");
				}
				String temp=entity[entity.length-1].replaceAll("[\\s]+", "");
				Integer inttemp=tokenToIndex.get(temp);
				if(temp.indexOf("_")==0)
				{
					inttemp=tokenToIndex.size()+(count_++);
				}
				sb_token.append("a"+inttemp+")");
				tokenList.add(sb_token.toString());
			}
			//System.out.println(tokenToIndex);
			String new_buffer=ss[0]+" ";
			for(int i=0;i<tokenList.size()-1;i++)
			{
				new_buffer+=tokenList.get(i)+" v ";
			}
			new_buffer+=tokenList.get(tokenList.size()-1);
			this.formulaList.add(new_buffer);
		}
	}
	
	public void ReadAndPrint(String inputfile,String outputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		PrintStream ps=new PrintStream(outputfile);
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			ps.println(buffer);
		}
		br.close();
		for(int i=0;i<this.formulaList.size();i++)
		{
			ps.println(formulaList.get(i));
		}
		ps.close();
		
	}
	
	public void Filter()
	{
		Map<String,Double> formulaToScore=new HashMap<String,Double>();
		for(int i=0;i<this.formulaList.size();i++)
		{
			String[] ss=this.formulaList.get(i).split("[\\s]+");
			Double scorevalue=Double.parseDouble(ss[0]);
			String formula="";
			for(int j=1;j<ss.length;j++)
			{
				formula+=" "+ss[j];
			}
			Double score=formulaToScore.get(formula);
			if(score==null)
			{
				formulaToScore.put(formula, scorevalue);
			}
			else
			{
				score+=scorevalue;
				formulaToScore.remove(formula);
				formulaToScore.put(formula, score);
			}
		}
		
		this.formulaList=new ArrayList<String>();
		Iterator it=formulaToScore.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			String formula=(String)entry.getKey();
			Double score=(Double)entry.getValue();
			formulaList.add(score+"\t"+formula);
		}
		
	}
	Map<String,String> tokenToCase=new HashMap<String,String>();
 	public void ReadRel(String inputfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[()]+");
			String little=ss[0].toLowerCase();
			tokenToCase.put(little, ss[0]);
			tokenToCase.put("!"+little, "!"+ss[0]);			
		}
		
	}
	
	public static void main(String[] args) throws IOException
	{
		ChangeForBoostrClause cfbc=new ChangeForBoostrClause();
		cfbc.ReadRel("C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\forinfer.mln");
		cfbc.ReadMLNAndChange("C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\webkb_boostr_results\\6.27result\\webkb_prof_clause\\train4\\.mln");
		cfbc.Filter();
		cfbc.ReadAndPrint("C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\forinfer.mln", "C:\\Users\\Administrator\\Desktop\\Experiments_6\\learn\\webkb_boostr_mcsat\\weights.mln");

	}
	
}

package journal.cora;

import java.util.*;
import java.io.*;

//用不太着了，oh year！

public class ChangeMyBoostrTofpMLN {

	public List<String> posList=new ArrayList<String>();
	public List<String> negList=new ArrayList<String>();
	
	public void ReadStringList(String filename,List<String> lineList) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			lineList.add(ChangeOneLine(buffer));
		}
		br.close();
	}
	
	public void PrintChangedLines(PrintStream ps,List<String> lineList, String value)
	{
		for(int i=0;i<lineList.size();i++)
		{
			ps.println(lineList.get(i)+"\t"+value);
		}
	}
	
	public String ChangeOneLine(String line)
	{
		StringBuilder sb=new StringBuilder();
		
		String[] ss=line.split("[()]+");
		String rel=ChangeToken(ss[0]);
		sb.append(rel);
		sb.append("(");
		String[] token=ss[1].split(",");
		for(int i=0;i<token.length-1;i++)
		{
			sb.append(ChangeToken(token[i]));
			sb.append(",");
		}
		sb.append(ChangeToken(token[token.length-1]));
		sb.append(")");
		return sb.toString();
	}
	public String ChangeToken(String token)
	{
		String sub1=token.substring(0, 1);
		String sub2=token.substring(1,token.length());
		return sub1.toUpperCase()+sub2;
	}
	
	public static void OneFileProcess(String boostr_pos_file,String boostr_neg_file,String queryfile) throws IOException
	{
		ChangeMyBoostrTofpMLN cm=new ChangeMyBoostrTofpMLN();
		cm.ReadStringList(boostr_pos_file, cm.posList);
		cm.ReadStringList(boostr_neg_file, cm.negList);
		PrintStream ps=new PrintStream(queryfile);
		cm.PrintChangedLines(ps, cm.posList, "1.0");
		cm.PrintChangedLines(ps, cm.negList, "0.0");
	}
	
	public static void main(String[] args) throws IOException
	{
		String[] poss={};
		String[] negs={};
		String[] querys={};
		for(int i=0;i<poss.length;i++)
		{
			OneFileProcess(poss[i],negs[i],querys[i]);
		}
	}
}

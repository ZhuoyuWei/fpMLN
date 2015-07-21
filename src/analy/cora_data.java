package analy;

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



public class cora_data {

	
	public Map<String,List<Integer>> relToConceptNodes=new HashMap<String,List<Integer>>();
	public Map<String,Set<Integer>> entityToConcepts=new HashMap<String,Set<Integer>>();
	public List<String> indexToRel=new ArrayList<String>();
	
	
	public int[] parents;
	
	public void ReadDBFiles(String[] filenames) throws IOException
	{
		int conceptCount=0;
		for(int i=0;i<filenames.length;i++)
		{
			BufferedReader br=new BufferedReader(new FileReader(filenames[i]));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[()]+");
				String rel=ss[0];
				String[] tokens=ss[1].split(",");
				
				List<Integer> conceptNodeList=relToConceptNodes.get(rel);
				if(conceptNodeList==null)
				{
					conceptNodeList=new ArrayList<Integer>();
					for(int j=0;j<tokens.length;j++)
					{
						conceptNodeList.add(conceptCount++);
					}
					relToConceptNodes.put(rel, conceptNodeList);
					indexToRel.add(rel);
				}
				
				for(int j=0;j<tokens.length;j++)
				{
					Set<Integer> conceptSet=entityToConcepts.get(tokens[j]);
					if(conceptSet==null)
					{
						conceptSet=new HashSet<Integer>();
						entityToConcepts.put(tokens[j], conceptSet);
					}
					conceptSet.add(conceptNodeList.get(j));
				}
			}
			System.out.println("Read is over");
		}
	}
	
	
	public void initUnionFindSet()
	{
		parents=new int[indexToRel.size()*2];
		int l=parents.length;
		for(int i=0;i<l;i++)
		{
			parents[i]=i;
		}
	}
	
	public int Find(int x)
	{
		if(parents[x]==x)
			return x;
		return parents[x]=Find(parents[x]);
	}
	public void Union(int a,int b)
	{
		int pa=Find(a);
		int pb=Find(b);
		if(pa!=pb)
		{
			parents[pa]=pb;
		}
	}
	
	public void BuildTheSet()
	{
		Iterator it=entityToConcepts.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			String entity=(String)entry.getKey();
			Set<Integer> indexSet=(Set<Integer>)entry.getValue();
			Integer[] indexList=indexSet.toArray(new Integer[0]);
			if(indexList.length<2)
				continue;
			int a=indexList[0];
			for(int i=1;i<indexList.length;i++)
			{
				int b=indexList[i];
				Union(a,b);
			}
		}
	}
	
	
	public void ChangeRelConceptAndPrintInfo(PrintStream ps)
	{
		Set<Integer> conceptSet=new HashSet<Integer>();
		for(int i=0;i<indexToRel.size();i++)
		{
			String rel=indexToRel.get(i);
			
			List<Integer> nodes=relToConceptNodes.get(rel);
			
			ps.print(rel+"(c"+parents[nodes.get(0)]);
			for(int j=1;j<nodes.size();j++)
			{
				ps.print(",c"+parents[nodes.get(j)]);
			}
			ps.println(")");
		}
		ps.close();
		System.out.println(conceptSet.size());
	}
	
	public void printLog(PrintStream ps)
	{
		Iterator it=entityToConcepts.entrySet().iterator();
		while(it.hasNext())
		{
			Map.Entry entry=(Map.Entry)it.next();
			String entity=(String)entry.getKey();
			Set<Integer> conceptSet=(Set<Integer>)entry.getValue();
			ps.println(entity+"\t"+conceptSet.size());
		}
	}
	
	public static void main(String[] args) throws IOException
	{
		String[] filenames=new String[5];
		
		filenames[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.1.db";
		filenames[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.2.db";		
		filenames[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.3.db";
		filenames[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.4.db";			
		filenames[4]="C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.5.db";			
		
		cora_data cd=new cora_data();
		cd.ReadDBFiles(filenames);
		
		PrintStream pslog=new PrintStream("C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.log");
		cd.printLog(pslog);
		
		cd.initUnionFindSet();
		cd.BuildTheSet();
		PrintStream ps=new PrintStream("C:\\Users\\Administrator\\Desktop\\mln_data\\cora.er\\coraSepFixedhwng.info");
		cd.ChangeRelConceptAndPrintInfo(ps);
		
	}
	
}

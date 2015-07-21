package kokcll;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalcularAUCAndCLL {

	List<Double> trueList=new ArrayList<Double>();
	List<Double> falseList=new ArrayList<Double>();
	
	public void FilterAUCFile(String inputfile,int truequerysize) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(inputfile));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			try{
				double value0=Double.parseDouble(ss[0]);
				double value1=Double.parseDouble(ss[1]);
				if(value1>0.5)
				{
					trueList.add(value0);
				}
				else
				{
					falseList.add(value0);
				}
			}catch(Exception e)
			{
				continue;
			}
		}
		
		for(int i=trueList.size()-1;i<truequerysize;i++)
		{
			trueList.add(0.);
		}
		
		Collections.sort(trueList);
		Collections.sort(falseList);
		
	}
	
	public void ComputeAUC(List<Double> thresholdList)
	{
		List<Double> precList=new ArrayList<Double>();
		for(int i=0;i<thresholdList.size();i++)
		{
			int trueCount=0;
			for(int j=trueList.size()-1;j>=0;j--)
			{
				if(trueList.get(j)>thresholdList.get(i))
				{
					trueCount++;
				}
				else
					break;
			}
			
			int falseCount=0;
			for(int j=falseList.size()-1;j>=0;j--)
			{
				if(falseList.get(j)>thresholdList.get(i))
				{
					falseCount++;
				}
				else
					break;
			}
			
			precList.add((double)trueCount/(double)(trueCount+falseCount));
		}
		
		List<Double> wList=new ArrayList<Double>();
		wList.add(0.);
		//for()
		
		for(int i=0;i<trueList.size();i++)
		{
			
		}
	}
	
	
	
	
	

	
	public static void main(String[] args)
	{
		//FilterAUCFile faf=new FileterAUCFile();
		
	}
	
}


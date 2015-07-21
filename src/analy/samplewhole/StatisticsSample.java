package analy.samplewhole;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import analy.samplewhole.model.StringAndCount;
import analy.samplewhole.model.WindowResult;

public class StatisticsSample {

	
	public void CountRepetition(String filename,PrintStream ps)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			Map<String,Integer> lineToCount=new HashMap<String,Integer>();
			int[] countList=new int[7];
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				Integer count=lineToCount.get(buffer);
				if(count==null)
				{
					lineToCount.put(buffer, count);
				}
				else
				{
					count++;
					lineToCount.remove(buffer);
					lineToCount.put(buffer, count);
				}
				String[] ss=buffer.split("\t");
				countList[ss.length]++;
			}
			System.out.println(lineToCount.size());
			for(int i=0;i<7;i++)
			{
				System.out.print(countList[i]);
			}
			System.out.println();
			
			
			//print
			Iterator it=lineToCount.entrySet().iterator();
			List<StringAndCount> sacList=new ArrayList<StringAndCount>();
			while((it.hasNext()))
			{
				Map.Entry entry=(Map.Entry)it.next();
				String path=(String)entry.getKey();
				Integer count=(Integer)entry.getValue();
				
				StringAndCount sac=new StringAndCount();
				sac.path=path;
				sac.count=count;
				
				sacList.add(sac);
				
			}
			
			Collections.sort(sacList,new StringAndCount());
			
			for(int i=0;i<sacList.size();i++)
			{
				ps.println(sacList.get(i).path+"\t"+sacList.get(i).count);
			}
			ps.close();
			
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void WindowCounting(String filename,int windowSize)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			//Map<String,Integer> lineToCount=new HashMap<String,Integer>();
			List<String> bufferList=new ArrayList<String>();

			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				bufferList.add(buffer);
			}
			br.close();
			
			int win=bufferList.size()/windowSize;
			List<WindowResult> winresList=new ArrayList<WindowResult>();
			
			for(int i=0;i<win;i++)
			{
				Map<String,Integer> lineToCount=new HashMap<String,Integer>();
				int[] countList=new int[7];
				
				for(int j=0;j<windowSize;j++)
				{
					buffer=bufferList.get(i*windowSize+j);
					Integer count=lineToCount.get(buffer);
					if(count==null)
					{
						lineToCount.put(buffer, count);
					}
					else
					{
						count++;
						lineToCount.remove(buffer);
						lineToCount.put(buffer, count);
					}
					String[] ss=buffer.split("\t");
					countList[ss.length]++;
				}

				WindowResult wr=new WindowResult();
				wr.mapsize=lineToCount.size();
				wr.countList=countList;
				winresList.add(wr);
			}
			
			for(int i=0;i<winresList.size();i++)
			{
				WindowResult wr=winresList.get(i);
				System.out.println("Window "+i+":");
				System.out.println(wr.mapsize);
				for(int j=0;j<7;j++)
				{
					System.out.print(wr.countList[j]+"\t");
				}
				System.out.println();
			}
			
			
			
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println("Pro is starting.");
		System.out.println(args[0]);
		StatisticsSample ss=new StatisticsSample();
		PrintStream ps=new PrintStream(args[1]);
		ss.CountRepetition(args[0],ps);
		ss.WindowCounting(args[0], 100000);
	}
	
}

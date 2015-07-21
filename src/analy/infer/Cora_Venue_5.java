package analy.infer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Cora_Venue_5 {

	List<Cora_DataPoint> dpList=new ArrayList<Cora_DataPoint>();
	List<Double> resultList=new ArrayList<Double>();
	
	List<Cora_DataPoint> goodList=new ArrayList<Cora_DataPoint>();
	List<Cora_DataPoint> badList=new ArrayList<Cora_DataPoint>();
	
	int maxIndex=-1;
	
	public void ReadFiles(String dbfile,String resultfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(dbfile));
			String buffer=null;
			int line=0;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<1)
					continue;
				String[] ss=buffer.split("[\\s]+");
				Double value=Double.parseDouble(ss[0]);
				if(value<0.5)
					break;
				Cora_DataPoint cdp=new Cora_DataPoint();
				cdp.index=line++;
				for(int i=1;i<ss.length;i++)
				{
					String[] sss=ss[i].split(":");
					int temp=Integer.parseInt(sss[0]);
					cdp.findex.add(temp);
					cdp.ftrue.add(Integer.parseInt(sss[1]));
					cdp.ffalse.add(Integer.parseInt(sss[2]));
					if(temp>this.maxIndex)
					{
						maxIndex=temp;
					}
				}
				dpList.add(cdp);
			}
			br.close();
			br=new BufferedReader(new FileReader(resultfile));
			buffer=null;
			line=0;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<1)
					continue;
				String[] ss=buffer.split("[\\s]+");		
				Double value=Double.parseDouble(ss[0]);
				Integer label=Integer.parseInt(ss[1]);
				
				if(label==0)
					break;
				
				resultList.add(value);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
public void ReadFilesFalse(String dbfile,String resultfile)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(dbfile));
			String buffer=null;
			int line=0;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<1)
					continue;
				String[] ss=buffer.split("[\\s]+");
				Double value=Double.parseDouble(ss[0]);
				if(value>0.5)
					continue;
				Cora_DataPoint cdp=new Cora_DataPoint();
				cdp.index=line++;
				for(int i=1;i<ss.length;i++)
				{
					String[] sss=ss[i].split(":");
					int temp=Integer.parseInt(sss[0]);
					cdp.findex.add(temp);
					cdp.ftrue.add(Integer.parseInt(sss[1]));
					cdp.ffalse.add(Integer.parseInt(sss[2]));
					if(temp>this.maxIndex)
					{
						maxIndex=temp;
					}
				}
				dpList.add(cdp);
			}
			br.close();
			br=new BufferedReader(new FileReader(resultfile));
			buffer=null;
			line=0;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<1)
					continue;
				String[] ss=buffer.split("[\\s]+");		
				Double value=Double.parseDouble(ss[0]);
				Integer label=Integer.parseInt(ss[1]);
				
				if(label==1)
					continue;
				
				resultList.add(value);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
	public void Mapping()
	{
		if(this.dpList.size()!=this.resultList.size())
		{
			System.out.println("error!");
		}
		for(int i=0;i<this.dpList.size();i++)
		{
			this.dpList.get(i).value=this.resultList.get(i);
		}
	}
	
	
	public void SplitDpList(double threthold)
	{
		for(int i=0;i<dpList.size();i++)
		{
			Cora_DataPoint cdp=dpList.get(i);
			if(cdp.value>=threthold)
			{
				goodList.add(cdp);
			}
			else
			{
				badList.add(cdp);
			}
		}
	}
	
	public void SplitDpList(double upthrethold,double downthrethold)
	{
		for(int i=0;i<dpList.size();i++)
		{
			Cora_DataPoint cdp=dpList.get(i);
			if(cdp.value>=upthrethold)
			{
				goodList.add(cdp);
			}
			else if(cdp.value<=downthrethold)
			{
				badList.add(cdp);
			}
		}
	}	
	
	
	public int[] StatisticFeature(List<Cora_DataPoint> cdpList)
	{
		int[] count=new int[this.maxIndex+1];
		for(int i=0;i<cdpList.size();i++)
		{
			Cora_DataPoint cdp=cdpList.get(i);
			for(int j=0;j<cdp.findex.size();j++)
			{
				count[cdp.findex.get(j)]++;
			}
		}
		return count;
	}
	
	public void PrintCounts(PrintStream ps,int[] count)
	{
		class TempPair implements Comparator
		{
			int index;
			int count;
			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				TempPair t1=(TempPair)o1;
				TempPair t2=(TempPair)o2;
				
				if(t1.count>t2.count)
					return -1;
				else if(t1.count<t2.count)
					return 1;
				return 0;
			}
		}
		List<TempPair> tpList=new ArrayList<TempPair>(count.length);
		for(int i=0;i<count.length;i++)
		{
			TempPair tp=new TempPair();
			tp.index=i;
			tp.count=count[i];
			tpList.add(tp);
		}
		Collections.sort(tpList, new TempPair());
		
		for(int i=0;i<tpList.size();i++)
		{
			ps.println(tpList.get(i).index+"\t"+tpList.get(i).count);
		}
		ps.close();
	}
	
	
	
	public static void main(String[] args) throws FileNotFoundException
	{
		Cora_Venue_5 cv5=new Cora_Venue_5();
		cv5.ReadFilesFalse("C:\\Users\\Administrator\\Desktop\\infer experiments\\" +
				"Test_Avea\\result\\SameVenue\\1\\mid\\test_datapoint.db",
				"C:\\Users\\Administrator\\Desktop\\infer experiments\\"+
				"Test_Avea\\result\\SameVenue\\1\\res\\probability.result");
		cv5.Mapping();
		Collections.sort(cv5.dpList,new Cora_DataPoint());
		cv5.SplitDpList(0.9,0.7);
		int[] goodCounts=cv5.StatisticFeature(cv5.goodList);
		int[] badCounts=cv5.StatisticFeature(cv5.badList);
		System.out.println(goodCounts.length+"\t"+badCounts.length);
		PrintStream psgood=new PrintStream("C:\\Users\\Administrator\\Desktop\\infer experiments\\"+
				"Test_Avea\\result\\SameVenue\\1\\mid\\false_good");
		cv5.PrintCounts(psgood, goodCounts);
		
		PrintStream psbad=new PrintStream("C:\\Users\\Administrator\\Desktop\\infer experiments\\"+
				"Test_Avea\\result\\SameVenue\\1\\mid\\false_bad");
		cv5.PrintCounts(psbad, badCounts);		
	}
	
}


class Cora_DataPoint implements Comparator
{
	int index;
	double value;
	List<Integer> findex=new ArrayList<Integer>();
	List<Integer> ftrue=new ArrayList<Integer>();
	List<Integer> ffalse=new ArrayList<Integer>();
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		Cora_DataPoint cdp1=(Cora_DataPoint)o1;
		Cora_DataPoint cdp2=(Cora_DataPoint)o2;
		return 0;
	}
}

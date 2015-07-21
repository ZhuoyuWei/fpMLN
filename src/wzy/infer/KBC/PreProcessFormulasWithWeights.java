package wzy.infer.KBC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PreProcessFormulasWithWeights {

	public List<String> mlnStringList=new ArrayList<String>();
	public List<Double> weightList=new ArrayList<Double>();
	
	public void ReadMLNStringfromFile(String mlnfile,String weightfile)throws IOException
	{
		//Read the learning results for structure and weights
		BufferedReader br=new BufferedReader(new FileReader(mlnfile));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("\t");
			mlnStringList.add(ss[1]);
		}
		br.close();
		
		buffer=null;
		br=new BufferedReader(new FileReader(weightfile));
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<1)
				continue;
			weightList.add(Double.parseDouble(buffer));
		}
		br.close();

	}
	
	/**
	 * Not infer for formulas whose weight is zero
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @date 2014.8.25
	 */
	public void FilterSmallWeightFormula(double smallweight)
	{
		List<Double> tempWeight=this.weightList;
		List<String> tempMln=this.mlnStringList;
		
		this.weightList=new ArrayList<Double>();
		this.mlnStringList=new ArrayList<String>();
		
		for(int i=0;i<tempWeight.size();i++)
		{
			if(Math.abs(tempWeight.get(i))>smallweight)
			{
				this.weightList.add(tempWeight.get(i));
				this.mlnStringList.add(tempMln.get(i));
			}
		}
	}
	
	public void GetTopWeightFormulas(int Top)
	{
		class TwoValue implements Comparator
		{
			String clause;
			Double value;
			
			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				
				TwoValue t1=(TwoValue)o1;
				TwoValue t2=(TwoValue)o2;
				
				if((Math.abs(t1.value-t2.value))<1e-10)
					return 0;
				else if(t1.value>t2.value)
					return -1;
				else
					return 1;
			}
		}
		List<TwoValue> tvList=new ArrayList<TwoValue>();
		for(int i=0;i<weightList.size();i++)
		{
			TwoValue tv=new TwoValue();
			tv.clause=mlnStringList.get(i);
			tv.value=weightList.get(i);
		}
		Collections.sort(tvList,new TwoValue());
		mlnStringList=new ArrayList<String>();
		weightList=new ArrayList<Double>();
		
		Top=Top<tvList.size()?Top:tvList.size();
		
		for(int i=0;i<Top;i++)
		{
			mlnStringList.add(tvList.get(i).clause);
			weightList.add(tvList.get(i).value);
		}
		
	}
	
	
	public static void main(String[] args)
	{
		
	}
	
}

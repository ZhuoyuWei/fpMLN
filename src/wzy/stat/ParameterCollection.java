package wzy.stat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ParameterCollection {
	private Map<String,String> parameters=new TreeMap<String,String>();
	
	public String getParameter(String key)
	{
		return parameters.get(key);
	}
	
	public void ReadParameter(String paraFile)
	{
		try {
			//BufferedReader br=new BufferedReader(new FileReader(paraFile));
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(paraFile),"utf-8"));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				if(buffer.charAt(0)=='/'&&buffer.charAt(1)=='/')
					continue;
				String[] ss=buffer.split("=");
				String key=ss[0];
				String value=ss[1];
				for(int i=2;i<ss.length;i++)
				{
					value+=ss[i];
				}
				parameters.put(key, value);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String[] splitParaStringBySemicolon(String s)
	{
		String[] splitS=s.split(",");
		List<String> listS=new ArrayList<String>();
		//check to keep each string is no empty
		for(int i=0;i<splitS.length;i++)
		{
			if(splitS[i].length()<1)
				continue;
			listS.add(splitS[i]);
		}
		return listS.toArray(new String[0]);
	}
}

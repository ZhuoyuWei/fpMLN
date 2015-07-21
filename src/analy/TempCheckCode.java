package analy;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.Map;

public class TempCheckCode {

	public static void DebugPrintRels(Map<String,Integer> stoi)
	{
		Iterator it=stoi.entrySet().iterator();
		try {
			PrintStream ps=new PrintStream("rel.log");	
			while(it.hasNext())
			{
				Map.Entry entry=(Map.Entry)it.next();
				String key=(String)entry.getKey();
				ps.println(key);
				System.out.println(key);
			}
			ps.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
}

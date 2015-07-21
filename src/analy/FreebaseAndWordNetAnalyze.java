package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FreebaseAndWordNetAnalyze {

	
	public Set<String> entitySet=new HashSet<String>();
	
	
	public void ReadAndCountEntity(String filename)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			int count=0;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[()]+");
				String[] tokens=ss[1].split(",");
				for(int i=0;i<tokens.length;i++)
				{
					entitySet.add(tokens[i]);
				}
				count++;
			}
			System.out.println(filename+"\t"+count+"\t"+entitySet.size());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args)
	{
		String[] filenames=new String[6];
		
		filenames[0]="E:\\Workspace_sun\\WordnetAndFreebaseDataProcess\\Freebase\\train.db";
		filenames[1]="E:\\Workspace_sun\\WordnetAndFreebaseDataProcess\\Freebase\\dev.db";
		filenames[2]="E:\\Workspace_sun\\WordnetAndFreebaseDataProcess\\Freebase\\test.db";
		filenames[3]="E:\\Workspace_sun\\WordnetAndFreebaseDataProcess\\Wordnet\\train.db";		
		filenames[4]="E:\\Workspace_sun\\WordnetAndFreebaseDataProcess\\Wordnet\\dev.db";
		filenames[5]="E:\\Workspace_sun\\WordnetAndFreebaseDataProcess\\Wordnet\\test.db";			
		
		for(int i=0;i<6;i++)
		{
			FreebaseAndWordNetAnalyze fan=new FreebaseAndWordNetAnalyze();
			fan.ReadAndCountEntity(filenames[i]);
		}
		
	}
	
}

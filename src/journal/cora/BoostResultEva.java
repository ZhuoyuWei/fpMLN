package journal.cora;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import wzy.stat.GlobalStaticData;
import wzy.stat.LogPrintStream;
import auc.AUCCalculator;

public class BoostResultEva {

	public Map<String,Integer> queryToScore=new HashMap<String,Integer>();
	
	
	public void ReadAnwserFiles(String posfile,String negfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(posfile));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[.]+");
			queryToScore.put(ss[0], 1);
			//System.out.println(ss[0]);
			//System.exit(-1);
		}
		br.close();
		br=new BufferedReader(new FileReader(negfile));
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[.]+");
			queryToScore.put(ss[0], 0);
		}
		br.close();
	}
	
	public void ReadResultFile(String filename,String outfile) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		PrintStream ps=new PrintStream(outfile);
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			String[] ss=buffer.split("[\\s]+");
			Integer score=queryToScore.get(ss[0]+ss[1]);
			if(score==null)
			{
				System.out.println("error "+ss[0]+ss[1]);
				
			}
			else
			{
				ps.println(ss[2]+"\t"+score);
			}
		}
	}
	
	
	public static void main(String[] args) throws IOException
	{
		for(int i=1;i<=5;i++)
		{
			try{
			BoostResultEva bre=new BoostResultEva();
			bre.ReadAnwserFiles("D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_samevenue\\test"+i+"_same\\test"+i+"_same_pos.txt"
					, "D:\\data\\Experiments_6\\learn\\cora_boostr\\cora-er_samevenue\\test"+i+"_same\\test"+i+"_same_neg.txt");
			bre.ReadResultFile("D:\\data\\temp_infer\\effect\\boostr_cora_result\\cora-er4\\test"+i+"_same\\results_samevenue.db"
					, "D:\\data\\temp_infer\\effect\\boostr_cora_result\\cora-er4\\test"+i+"_same\\probability.res");
			PrintStream ps=new PrintStream("D:\\data\\temp_infer\\effect\\boostr_cora_result\\cora-er4\\test"
					+i+"_same\\final.log");
			AUCCalculator.CalculateAUC("D:\\data\\temp_infer\\effect\\boostr_cora_result\\cora-er4\\test"
					+i+"_same\\probability.res", "list", ps);
			}catch(Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}
	}
}

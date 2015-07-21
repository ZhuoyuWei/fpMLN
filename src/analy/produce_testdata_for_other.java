package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class produce_testdata_for_other {
	
	
	public static void ReadUserDataAndPrintOut(String input_file,String output_file,String result_file)
	{
		try {
			BufferedReader br=new BufferedReader(new FileReader(input_file));
			PrintStream ps=new PrintStream(output_file);
			PrintStream ps2=new PrintStream(result_file);
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("\t");
				ps.println(ss[0]);
				ps2.println(ss[0]+"\t"+ss[1]);
			}
			ps2.close();
			ps.close();
			br.close();
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
		ReadUserDataAndPrintOut(args[0],args[1],args[2]);
	}
}

package analy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class webkb_data {

	
	public static void Read4Writer1(String[] inputfile,String outputfile)
	{
		try {
			PrintStream ps=new PrintStream(outputfile);
			for(int i=0;i<3;i++)
			{
				BufferedReader br=new BufferedReader(new FileReader(inputfile[i]));
				String buffer=null;
				while((buffer=br.readLine())!=null)
				{
					if(buffer.length()<2)
						continue;
					ps.println(buffer);
				}
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
	
	
	public static void main(String[] args)
	{
		
		
		//String info_filename="C:\\Users\\Administrator\\Desktop\\mln_data\\uw-cse\\uw.simple2.mln";
		String[] db_filename=new String[4];
		db_filename[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.1.db";
		db_filename[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.2.db";		
		db_filename[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.3.db";
		db_filename[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\webkb.4.db";
		
		String[] db_filename_test=new String[4];
		db_filename_test[0]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\miss-1.db";
		db_filename_test[1]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\miss-2.db";
		db_filename_test[2]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\miss-3.db";
		db_filename_test[3]="C:\\Users\\Administrator\\Desktop\\mln_data\\webkb-data\\webkb-data\\miss-4.db";				
		
		
		//InputData.ReadInfo(info_filename, ud.relList, ud.relMap, ud.typeList, ud.typeMap, ud.conceptRelList);
		
		
		for(int i=0;i<4;i++)
		{
			//readOneFile(info_filename,db_filename[i]);
			List<String> inputfile=new ArrayList<String>();
			for(int j=0;j<4;j++)
			{
				if(i==j)
					continue;
				inputfile.add(db_filename[j]);
			}
			Read4Writer1(inputfile.toArray(new String[0]),db_filename_test[i]);
		}
	}
	
}

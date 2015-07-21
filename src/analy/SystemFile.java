package analy;

import java.io.File;

public class SystemFile {

	
	public static void Traverse(File dir)
	{
		File[] fs=dir.listFiles();
		if(fs.length<13)
		{
			System.out.println(dir.getPath());
		}
		for(int i=0;i<fs.length;i++)
		{
			if(fs[i].isDirectory())
			{
				Traverse(fs[i]);
			}
		}
	}
	
	
	
	public static void main(String[] args)
	{
		Traverse(new File("C:\\Users\\Administrator\\Documents\\mlnWeights"));	
	}
	
}

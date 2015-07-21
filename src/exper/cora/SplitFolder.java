package exper.cora;

import java.io.File;

import wzy.func.FileTools;

public class SplitFolder {

	public void OneFolder(String dirfile)
	{
		File dir=new File(dirfile);
		if(dir.isDirectory())
		{
			File[] files=dir.listFiles();
			for(int i=0;i<files.length;i++)
			{
				OneFolder(files[i].getPath());
			}
		}
		else
		{
			OneFile(dir);
		}
	}
	
	public void OneFile(File file)
	{
		String filename=file.getName();
		System.out.println(filename);
		String[] ss=filename.split("[.]+");
		//String cur=ss[0];
		String dir=file.getParent();
		FileTools.CreateNewFolder(dir+"\\"+ss[0]);
		
		FileTools.CopyFile(file.getPath(), dir+"\\"+ss[0]+"\\"+filename);
		
	}
	
	public static void main(String[] args)
	{
		SplitFolder sf=new SplitFolder();
		sf.OneFolder("C:\\Users\\Administrator\\Desktop\\infer experiments\\infer_sub");
	}
	
}

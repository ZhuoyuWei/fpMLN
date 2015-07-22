package test;

import java.io.*;

public class ChangeParameterFileDir {

	public void OneFileProcess(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		StringBuilder sb=new StringBuilder();
		
		while((buffer=br.readLine())!=null)
		{
			sb.append(buffer);
			sb.append("\n");
		}
		
		br.close();
		
		String content=sb.toString();
		content=content.replaceAll("/dev/shm", "../examples/kbcompletion");
		content=content.replaceAll("/state/partition1", "../examples/kbcompletion");		
		
		
		PrintStream ps=new PrintStream(filename);
		ps.print(content);
		ps.flush();
		ps.close();
	}
	
	
	public void ChangeDir(String dirfile) throws IOException
	{
		File dir=new File(dirfile);
		File[] files=dir.listFiles();
		for(int i=0;i<files.length;i++)
		{
			try {
				OneFileProcess(files[i].getPath()+"/parameter");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public static void main(String[] args) throws IOException
	{
		ChangeParameterFileDir cpfd=new ChangeParameterFileDir();
		cpfd.ChangeDir("F:\\Workspace\\fpMLN\\fpMLN\\examples\\kbcompletion\\mln_exp_can10\\left");
	}
	
}

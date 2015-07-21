package wzy.func;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import wzy.stat.GlobalStaticData;
import wzy.model.DataPoint;
import wzy.model.TrueFalseCount;


public class Learner {
	
	public static Random rand=new Random();

	private Double[] weights;
	private Integer MaxIterator;
	private double lamda0;
	private double upsilon;
	private double punishmentC;
	private Integer K;
	private static double z;
	
	//by wzy,2014.10.31
	private int formulaSize;
	
	
	
	
	public Double[] getWeights() {
		return weights;
	}




	/**
	 * Initialize a learner for weight learning.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param MaxIterator the maximum round for the iterator.
	 * @param lamda the initialized step size for each iterator of optimizing.
	 * @param upsilon the termination precision.
	 * @param C the regularization parameter.
	 * @param K the maximun number of data points in one iterator train.
	 * @param rand The random tool.
	 */
	public void IntiLearner(Integer MaxIterator, double lamda, double upsilon, double C, Integer K,double z, Random rand)
	{
		this.MaxIterator=MaxIterator;
		this.lamda0=lamda;
		this.upsilon=upsilon;
		this.punishmentC=C;
		this.K=K;
		
		weights=new Double[GlobalStaticData.indexToConceptFormula.size()];
		if(rand!=null)
		{
			for(int i=0;i<weights.length;i++)
			{
				double randTempDouble=rand.nextDouble();
				weights[i]=rand.nextInt()>0?randTempDouble:(-randTempDouble);
			}
		}
		else
		{
			for(int i=0;i<weights.length;i++)
			{
				weights[i]=0.;
			}
		}
	}
	public void IntiLearner(Integer MaxIterator, double lamda, double upsilon
			, double C, Integer K,double z, Random rand,int formulaSize)
	{
		this.MaxIterator=MaxIterator;
		this.lamda0=lamda;
		this.upsilon=upsilon;
		this.punishmentC=C;
		this.K=K;
		
		weights=new Double[formulaSize];
		if(rand!=null)
		{
			for(int i=0;i<weights.length;i++)
			{
				double randTempDouble=rand.nextDouble();
				weights[i]=rand.nextInt()>0?randTempDouble:(-randTempDouble);
			}
		}
		else
		{
			for(int i=0;i<weights.length;i++)
			{
				weights[i]=0.;
			}
		}
	}
	/**
	 * Calculate gradients for one learn process for MLN, it is called by CalculateGradient() which is the framework for<br>
	 * calculating gradients.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param datapointList the list of trainning data points. The size is smaller than K in the learner.
	 * @param gradient a vector of gradient, it will be filled in this function.
	 */
	private void CalculateMLNGradient(List<DataPoint> datapointList,Double[] gradient)
	{
		for(int i=0;i<datapointList.size();i++)
		{
			DataPoint dp=datapointList.get(i);
			double conditionValue=0.;
			for(int j=0;j<dp.nodeList.size();j++)
			{
				Integer formulaIndex=dp.nodeList.get(j);
				TrueFalseCount tfc=dp.countList.get(j);
				conditionValue+=weights[formulaIndex]*(tfc.trueCount-tfc.falseCount);
			}
			
			for(int j=0;j<dp.nodeList.size();j++)
			{
				Integer formulaIndex=dp.nodeList.get(j);
				TrueFalseCount tfc=dp.countList.get(j);
				double tempValue=dp.query.value>0?-tfc.trueCount:-tfc.falseCount;
				if(conditionValue>0)
				{
					tempValue+=tfc.trueCount;
					double expValue=Math.exp(-conditionValue);
					tempValue+=expValue*(tfc.falseCount-tfc.trueCount)/(1.+expValue);
				}
				else
				{
					tempValue+=tfc.falseCount;
					double expValue=Math.exp(conditionValue);
					tempValue+=expValue*(tfc.trueCount-tfc.falseCount)/(1.+expValue);					
				}
				gradient[formulaIndex]+=tempValue;
			}
		}
		for(int i=0;i<gradient.length;i++)
		{
			gradient[i]/=datapointList.size();
		}
	}
	/**
	 * Calculate gradients for one learn process for MLN, it is called by CalculateGradient() which is the framework for<br>
	 * calculating gradients.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param datapointList the list of trainning data points. The size is smaller than K in the learner.
	 * @param gradient a vector of gradient, it will be filled in this function.
	 */
	private void CalculateMLNGradientWithL2(List<DataPoint> datapointList,Double[] gradient)
	{
		for(int i=0;i<datapointList.size();i++)
		{
			DataPoint dp=datapointList.get(i);
			double conditionValue=0.;
			for(int j=0;j<dp.nodeList.size();j++)
			{
				Integer formulaIndex=dp.nodeList.get(j);
				TrueFalseCount tfc=dp.countList.get(j);
				conditionValue+=weights[formulaIndex]*(tfc.trueCount-tfc.falseCount);
			}
			
			for(int j=0;j<dp.nodeList.size();j++)
			{
				Integer formulaIndex=dp.nodeList.get(j);
				TrueFalseCount tfc=dp.countList.get(j);
				double tempValue=dp.query.value>0?-tfc.trueCount:-tfc.falseCount;
				if(conditionValue>0)
				{
					tempValue+=tfc.trueCount;
					double expValue=Math.exp(-conditionValue);
					tempValue+=expValue*(tfc.falseCount-tfc.trueCount)/(1.+expValue);
				}
				else
				{
					tempValue+=tfc.falseCount;
					double expValue=Math.exp(conditionValue);
					tempValue+=expValue*(tfc.trueCount-tfc.falseCount)/(1.+expValue);					
				}
				gradient[formulaIndex]+=tempValue;
			}
		}
		double c=1.;
		for(int i=0;i<gradient.length;i++)
		{
			gradient[i]/=datapointList.size();
			gradient[i]+=2*weights[i]*c;
		}
	}
	/**
	 * It is a framework of calculating gradients, you need implement a function for your objection updating.<br>
	 * And it calculate and return the norm-2 value of the gradient vector for deciding whether stop iterator early.
	 * @author Zhuoyu Wei
	 * @version 1.0
	 * @param datapointList the list of trainning data points. The size is smaller than K in the learner.
	 * @param gradient a vector of gradient, it will be filled in this function.
	 * @return the norm-2 of the gradient vector.
	 */
	public double CalculateGradient(List<DataPoint> datapointList,Double[] gradient)  //max compute the gradient when minimize the objective function.
	{
		//init
		for(int i=0;i<gradient.length;i++)
		{
			gradient[i]=0.;
		}
		
		//Which function used for calculating gradients determines which objective function your choose.
		//CalculateMLNGradientWithL2(datapointList,gradient);
		CalculateMLNGradient(datapointList,gradient);
		
		//Calculate gradient
		double sum=0.;
		for(int i=0;i<gradient.length;i++)
		{
			sum+=gradient[i]*gradient[i];
		}
		
		return Math.sqrt(sum);
	}
	
	/**
	 * Learn once for at most K data points. In this function, it call CalculateGradient() to calculate the gradient vector<br>
	 * update gradients by the gradient vector, and use the capenter's method to update weights by norm-1 item.
	 * @author Zhuoyu Wei
	 * @verison 1.0
	 * @param datapointList the list of trainning data points. The size is smaller than K in the learner.
	 * @param lamda the update parameter for this round.
	 * @return the norm-2 of the gradient vector.
	 */
	public double LearnOnceProcess(List<DataPoint> datapointList,double lamda)
	{
		Double[] gradients=new Double[weights.length];
		double value=CalculateGradient(datapointList,gradients);
		//update weights only by gradient but no norm-1 item. 
		for(int i=0;i<weights.length;i++)
		{
			weights[i]=weights[i]-lamda*gradients[i];
		}
		//update weights by norm-1 item. It used the Capenter's method.
		double theta=CalcularTheta(weights);
		//System.out.println(theta);
		for(int i=0;i<weights.length;i++)
		{
			if(Math.abs(weights[i])<1e-10)
			{
				weights[i]=0.;
			}
			else if(weights[i]>0)
			{
				//weights[i]=Math.max(0., weights[i]-punishmentC*lamda);
				weights[i]=Math.max(weights[i]-theta, 0);
			}
			else if(weights[i]<0)
			{
				//weights[i]=Math.min(0., weights[i]+punishmentC*lamda);
				weights[i]=Math.min(weights[i]+theta,0);
			}
		}
		return value;
	}
	
	/**
	 * Calcular the lamda for each round weights updating.
	 * @author Zhuoyu Wei
	 * @verison 1.0 lamda=lamda0/(1+round)
	 * @param round the round index
	 * @return the lamda calculared.
	 */
	public double CalcularLamda(int round)
	{
		return lamda0/(1.+round);
	}
	
	/**
	 * The main process for learning weights.
	 * @author Zhuoyu Wei
	 * @verison 1.0
	 * @param datapointList all the data points.
	 */
	public void Learning(List<DataPoint> datapointList)
	{
		for(int r=0;r<MaxIterator;r++)
		{
			List<DataPoint> subTrainData=new ArrayList<DataPoint>();
			double lamda=CalcularLamda(r);
			for(int i=0;i<datapointList.size();i++)
			{
				subTrainData.add(datapointList.get(i));
				if(subTrainData.size()>=K)
				{
					double value=LearnOnceProcess(subTrainData,lamda);
					if(value<upsilon)   //if the gradient is small enough, the process is stopped early.
					{
						System.out.println("Converge early in Round "+r);
						return;
					}
					subTrainData.clear();
				}
			}
			if(subTrainData.size()>0)
			{
				double value=LearnOnceProcess(subTrainData,lamda);
				if(value<upsilon)//if the gradient is small enough, the process is stopped early.
				{
					System.out.println("Converge early in Round "+r);
					return;
				}
			}
		}
	}
	
	
	private static int Partition(Double[] v,int p,int q,int r)
	{
		double t=v[r];
		v[r]=v[q];
		v[q]=t;
		int j=p;
		for(int i=p;i<q;i++)
		{
			if(v[i]>v[q])
			{
				t=v[i];
				v[i]=v[j];
				v[j]=t;
				j++;
			}
		}
		
		t=v[j];
		v[j]=v[p];
		v[p]=t;
		
		return j;
	}
	
	
	
	public static double CalcularTheta(Double[] weights)
	{
		List<Double> vList=new ArrayList<Double>();
		for(int i=0;i<weights.length;i++)
		{
			double w=Math.abs(weights[i]);
			if(w<1E-10)
				continue;
			vList.add(w);
		}
		Double[] v=vList.toArray(new Double[0]);
		
		double s=0.;
		int ro=0;
		int p=0,q=v.length-1;
		while(true)
		{
			if(q<p)
				break;
			int r=Math.abs(rand.nextInt())%(q-p+1)+p;
			int m=Partition(v,p,q,r);
			ro=m;
			if(m<=p)
				break;
			double deltaS=0.;
			for(int i=p;i<m;i++)
			{
				deltaS+=v[i];
			}
			if(deltaS+s-ro*v[m]<z)
			{
				s+=deltaS;
				p=m+1;
			}
			else
			{
				q=m-1;
			}
		}
		
		return ro>0?(s-z)/ro:0.;
		
	}
	
}

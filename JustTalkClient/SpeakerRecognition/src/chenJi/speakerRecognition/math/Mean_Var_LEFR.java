package chenJi.speakerRecognition.math;

public class Mean_Var_LEFR {
	
	public Mean_Var_LEFR()
	{
		
	}
	
	public double getMean(double[] data)
	{
		double sum = 0;
		
		for(int i=0; i<data.length; i++)
		{
			sum += data[i];
		}
		
		return (sum==0)?sum:(sum/data.length);
	}
	
	public double getVariance(double[] data)
	{
		double var = 0;
		double mean = getMean(data);
		
		for(int i=0; i<data.length; i++)
		{
			var += Math.pow(data[i]-mean, 2);
		}
		
		return (var==0)?var:(var/data.length);
	}
	
	public double getVariance(double[] data, double mean)
	{
		double var = 0;
		
		for(int i=0; i<data.length; i++)
		{
			var += Math.pow(data[i]-mean, 2);
		}
		
		return (var==0)?var:(var/data.length);
	}
	
	public double getLEFR(double[] data, double factor)
	{
		double count = 0;
		double mean = getMean(data);
		double threshold = factor*mean;
		
		for(int i=0; i<data.length; i++)
		{
			if(data[i] < threshold) count++;
		}
		
		return (count==0)?count:(count/data.length);
	}

	public double getLEFR(double[] data, double mean, double factor)
	{
		double count = 0;
		double threshold = factor*mean;
		
		for(int i=0; i<data.length; i++)
		{
			if(data[i] < threshold) count++;
		}
		
		return (count==0)?count:(count/data.length);
	}
}

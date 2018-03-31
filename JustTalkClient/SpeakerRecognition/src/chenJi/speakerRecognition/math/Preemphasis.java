package chenJi.speakerRecognition.math;

public class Preemphasis 
{
	public double factor;
	public double initValue;

	public Preemphasis(double fValue, double initV)
	{
		factor = fValue;
		initValue = initV;
	}
	
	public void applyPreemphasis(double[] buffer)
	{
		
		//double tmpValue = buffer[buffer.length-1];
		for(int i=buffer.length-1; i>0; i--)
		{
			buffer[i] = buffer[i] - (buffer[i-1]*factor);
		}
		buffer[0] = buffer[0] - (initValue*factor);
		//initValue = tmpValue;
	}

}

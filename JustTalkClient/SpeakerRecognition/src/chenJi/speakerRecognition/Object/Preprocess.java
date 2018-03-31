package chenJi.speakerRecognition.Object;

public class Preprocess {
	
	public  Preprocess()
	{
		
	}
	
	public double[][] doFraming(double[] originalSignal, int frameSize, int slidingSize, boolean isNormalized)
	{
		if(isNormalized)
		{
			for(int i=0; i<originalSignal.length; i++) 
				if(originalSignal[i]!=0.0) originalSignal[i] /= 32768;
		}
			
		int noOfFrames = ((originalSignal.length - frameSize)/slidingSize) + 1;
		double[][] framedSignal = new double[noOfFrames][frameSize];
		
		for(int i=0; i<noOfFrames; i++)
		{
			for (int j = 0; j < frameSize; j++)
			{
				framedSignal[i][j] = originalSignal[i*slidingSize+j];
			}
		}
		
		return framedSignal;
	}

}

package chenJi.speakerRecognition.math;

public class VAD_features {
	
	public VAD_features()
	{

	}
	
	public double[] getZCR(double[][] framedSignal)
	{
		double[] framedZCR = new double[framedSignal.length];
		
		for(int i=0; i<framedSignal.length; i++)
		{
			double ZCR = 0;
			for(int j=1; j<framedSignal[i].length; j++)
			{
				ZCR += Math.abs(framedSignal[i][j] - framedSignal[i][j-1]);		
			}
			framedZCR[i] = ZCR/2;
		}
		
		return framedZCR;
	}
	
	public double[] getRMS(double[][] framedSignal)
	{
		double[] framedRMS = new double[framedSignal.length];
		
		for(int i=0; i<framedSignal.length; i++)
		{
			double RMS = 0;
			for(int j=0; j<framedSignal[i].length; j++)
			{
				RMS += Math.pow(framedSignal[i][j], 2);
			}
			framedRMS[i] = Math.sqrt(RMS/framedSignal[i].length);
		}
		
		return framedRMS;
	}

}

package chenJi.speakerRecognition.Object;

import java.util.Arrays;

import chenJi.speakerRecognition.math.*;



public class MFCC_Frame {

	private int RECORDER_SAMPLERATE = 16000;
	private int FFT_SIZE = 512;
	private int WindowSize = 512;
	private double Preemphasis_factor = 0.95;
	private int MFCCS_VALUE = 13;
	private int MEL_BANDS = 20;
	
	private FFT featureFFT = null;
    private MFCC featureMFCC = null;
    private Window featureWin = null;
    private Preemphasis featurePre = null;

	public MFCC_Frame(int sampleRate, int frameSize, int mfccValue) 
	{
		RECORDER_SAMPLERATE = sampleRate;
		FFT_SIZE = frameSize;
		WindowSize = frameSize;
		MFCCS_VALUE = mfccValue;
		featureFFT = new FFT(FFT_SIZE);
	    featureWin = new Window(WindowSize);
	    featurePre = new Preemphasis(Preemphasis_factor, 0.0);
	    featureMFCC = new MFCC(FFT_SIZE, MFCCS_VALUE, MEL_BANDS, RECORDER_SAMPLERATE);

	}
	public double[] getMFCC(short[] data16bit)
	{
		double fftBufferR[] = new double[FFT_SIZE];
    	double fftBufferI[] = new double[FFT_SIZE];
    	double featureCepstrum[] = new double[MFCCS_VALUE];
    
    	
    	// Frequency analysis
		Arrays.fill(fftBufferR, 0);
		Arrays.fill(fftBufferI, 0);
		
		// Convert audio buffer to doubles
		for (int i = 0; i < FFT_SIZE; i++)
		{
			fftBufferR[i] = data16bit[i];
		}
		
		// In-place Preemphasis
		featurePre.applyPreemphasis(fftBufferR);
		
		// In-place windowing
		featureWin.applyWindow(fftBufferR);

		// In-place FFT
		featureFFT.fft(fftBufferR, fftBufferI);

		// Get MFCCs
		featureCepstrum = featureMFCC.cepstrum(fftBufferR, fftBufferI);
		
    	return featureCepstrum;
	}
	
	public double[] getMFCC(double[] data16bit)
	{
		double fftBufferR[] = new double[FFT_SIZE];
    	double fftBufferI[] = new double[FFT_SIZE];
    	double featureCepstrum[] = new double[MFCCS_VALUE];
    
    	
    	// Frequency analysis
		Arrays.fill(fftBufferR, 0);
		Arrays.fill(fftBufferI, 0);
		
		// Convert audio buffer to doubles
		for (int i = 0; i < FFT_SIZE; i++)
		{
			fftBufferR[i] = data16bit[i];
		}
		
		// In-place Preemphasis
		featurePre.applyPreemphasis(fftBufferR);
		
		// In-place windowing
		featureWin.applyWindow(fftBufferR);

		// In-place FFT
		featureFFT.fft(fftBufferR, fftBufferI);

		// Get MFCCs
		featureCepstrum = featureMFCC.cepstrum(fftBufferR, fftBufferI);
		
    	return featureCepstrum;
	}
	public double calculateEnergy(double[] Signals)
	{
		double energyValue;
		int sum = 0;
		
		for(int i=0; i<Signals.length; i++)
		{
			sum += Math.pow(Signals[i], 2);
		}
		energyValue = Math.log(sum);
		
		return energyValue;
	}

}

package chenJi.speakerRecognition.Object;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import chenJi.speakerRecognition.math.Mean_Var_LEFR;
import chenJi.speakerRecognition.math.VAD_features;

public class HumanVoiceDetection {

	private J48 J48_VAD_object = null;
	private FastVector attributesVAD;
	private int VAD_dimension = 5;
	private double soundThreshold_test = 0.003;
	private double soundThreshold_train = 0.006;
	private static int frameSize = 512;
	private static int slidingSize = 256;
	private static String trainingDataPath = "C:\\Users\\TingYuan\\workspace\\SpeakerRecognition\\SpeakerRec_Data\\training_data";
	private static String trainingModelPath = "C:\\Users\\TingYuan\\workspace\\SpeakerRecognition\\SpeakerRec_Data\\training_model";
		
	private VAD_features vadFeatures;
	private Mean_Var_LEFR meanVarLEFR;
	
	public HumanVoiceDetection()
	{
		initVAD_FV();
		this.vadFeatures = new VAD_features();
		this.meanVarLEFR = new Mean_Var_LEFR();
		
		try {
			this.J48_VAD_object = (J48) weka.core.SerializationHelper.read(trainingModelPath+"\\J48_VAD.model");
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public boolean isHumanVoice(double[][] framedSignal)
	{
		
		boolean isHuman;
		double classType = 0;
		double[] arrayZCR = vadFeatures.getZCR(framedSignal);
		double[] arrayRMS = vadFeatures.getRMS(framedSignal);
		double meanOfZCR = meanVarLEFR.getMean(arrayZCR);
		double varOfZCR = meanVarLEFR.getVariance(arrayZCR, meanOfZCR);
		double meanOfRMS = meanVarLEFR.getMean(arrayRMS);
		double varOfRMS = meanVarLEFR.getVariance(arrayRMS, meanOfRMS);
		double lowEnergyFrameRate = meanVarLEFR.getLEFR(arrayRMS, meanOfRMS, 0.5);
		double result = 0;
		
		try {
			result = this.J48_VAD_object.classifyInstance(getInstanceOfVAD(meanOfZCR,varOfZCR, varOfRMS, lowEnergyFrameRate, classType));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//System.out.println(result+" "+meanOfRMS);
		
		if(meanOfRMS>this.soundThreshold_test && result==1) isHuman = true;
		else isHuman = false;
		
		return isHuman;
	}
	
	public double getMeanOfRMS(double[][] framedSignal)
	{
		double[] arrayRMS = vadFeatures.getRMS(framedSignal);
		double meanOfRMS = meanVarLEFR.getMean(arrayRMS);
		
		return meanOfRMS;
	}
	
	public void trainVAD()
	{
		Instances trainingSet;
		Preprocess preProcess = new Preprocess();
		
		FileInputStream fis = null;
		File fileDirNow;
		
		int readSizeOnce = 16128;
		int readCount = 0;
		int readSize = 0;
		byte[] data8bit;
		short[] data16bit;
		double[] originalSignal;
		double[][] framedSignal;
		
		
		//initialize Instances
		trainingSet = new Instances("Rel", this.attributesVAD, 0);
		// Make the last attribute be the class
		trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		
		//human voice
		fileDirNow = new File(trainingDataPath+"\\300s\\");
		for(int fileNo=0; fileNo<fileDirNow.list().length; fileNo++)
		{
			File file = new File(fileDirNow, fileDirNow.list()[fileNo]);
			if (file.exists()) 
			{
				data8bit = new byte[readSizeOnce*2];
				data16bit = new short[readSizeOnce];
				originalSignal = new double[readSizeOnce];
				
				try {              
	                fis = new FileInputStream(file);
	            } catch (Exception e) {  
	                e.printStackTrace();  
	            }
				
				readCount = 0;
				while(true)
	    		{
	    			try {
	    				readSize = fis.read(data8bit, 0, readSizeOnce*2);
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    			}
	    			if(readSize==readSizeOnce*2)
	    			{
	    				for (int i=0; i <readSizeOnce; i++)
	    	    		{
	    					short tmp1, tmp2;
	    					tmp1 = (short)data8bit[i*2];
	    					tmp1 &= (short)255;
	    					tmp2 = (short)data8bit[i*2+1];
	    					tmp2 &= (short)255;
	    					data16bit[i] = (short) (tmp1 | (tmp2 << 8));
	    					originalSignal[i] = data16bit[i];
	    	    		}
	    				
	    				//framing
		    			framedSignal = preProcess.doFraming(originalSignal, frameSize, slidingSize, true);
		    			
		    			VAD_features vadFeatures = new VAD_features();
		    			Mean_Var_LEFR meanVarLEFR = new Mean_Var_LEFR();
		    			
		    			double classType = 1;
		    			double[] arrayZCR = vadFeatures.getZCR(framedSignal);
		    			double[] arrayRMS = vadFeatures.getRMS(framedSignal);
		    			double meanOfZCR = meanVarLEFR.getMean(arrayZCR);
		    			double varOfZCR = meanVarLEFR.getVariance(arrayZCR, meanOfZCR);
		    			double meanOfRMS = meanVarLEFR.getMean(arrayRMS);
		    			double varOfRMS = meanVarLEFR.getVariance(arrayRMS, meanOfRMS);
		    			double lowEnergyFrameRate = meanVarLEFR.getLEFR(arrayRMS, meanOfRMS, 0.5);
		    			
		    			if(meanOfRMS > soundThreshold_train)
		    			{
		    				trainingSet.add(getInstanceOfVAD(meanOfZCR,varOfZCR, varOfRMS, lowEnergyFrameRate, classType));
		    				readCount++;
		    			}
		    			
	    			}
	    			else 
	    			{
	    				System.out.println("readCount: "+(readCount));
	    				break;
	    			}
	    		}
				
			}
			else
			{
				System.out.println("Open training data is not exist");
			}
		}
		
		
		//non-human voice
		File fileDir = new File(trainingDataPath+"\\non-human\\");
		for(int fileNo=0; fileNo<fileDir.list().length; fileNo++)
		{
			File file = new File(fileDir, fileDir.list()[fileNo]);
			if (file.exists()) 
			{
				data8bit = new byte[readSizeOnce*2];
				data16bit = new short[readSizeOnce];
				originalSignal = new double[readSizeOnce];
				
				try {              
	                fis = new FileInputStream(file);
	            } catch (Exception e) {  
	                e.printStackTrace();  
	            }
				
				readCount = 0;
				while(true)
	    		{
	    			try {
	    				readSize = fis.read(data8bit, 0, readSizeOnce*2);
	    				readCount++;
	    			} catch (IOException e) {
	    				e.printStackTrace();
	    			}
	    			if(readSize==readSizeOnce*2)
	    			{
	    				for (int i=0; i <readSizeOnce; i++)
	    	    		{
	    					short tmp1, tmp2;
	    					tmp1 = (short)data8bit[i*2];
	    					tmp1 &= (short)255;
	    					tmp2 = (short)data8bit[i*2+1];
	    					tmp2 &= (short)255;
	    					data16bit[i] = (short) (tmp1 | (tmp2 << 8));
	    					originalSignal[i] = data16bit[i];
	    	    		}
	    				
	    				//framing
		    			framedSignal = preProcess.doFraming(originalSignal, frameSize, slidingSize, true);
		    			
		    			VAD_features vadFeatures = new VAD_features();
		    			Mean_Var_LEFR meanVarLEFR = new Mean_Var_LEFR();
		    			
		    			double classType = 0;
		    			double[] arrayZCR = vadFeatures.getZCR(framedSignal);
		    			double[] arrayRMS = vadFeatures.getRMS(framedSignal);
		    			double meanOfZCR = meanVarLEFR.getMean(arrayZCR);
		    			double varOfZCR = meanVarLEFR.getVariance(arrayZCR, meanOfZCR);
		    			double meanOfRMS = meanVarLEFR.getMean(arrayRMS);
		    			double varOfRMS = meanVarLEFR.getVariance(arrayRMS, meanOfRMS);
		    			double lowEnergyFrameRate = meanVarLEFR.getLEFR(arrayRMS, meanOfRMS, 0.5);
		    			
		    			trainingSet.add(getInstanceOfVAD(meanOfZCR,varOfZCR, varOfRMS, lowEnergyFrameRate, classType));
	    				
	    			}
	    			else 
	    			{
	    				System.out.println("readCount: "+readCount);
	    				break;
	    			}
	    		}
				
			}
			else
			{
				System.out.println("Open training data is not exist");
			}
		}
			
		
		this.J48_VAD_object = new J48();
		//J48_VAD_object.setUseLaplace(true);
		try {
			double s = (double)(System.currentTimeMillis())/1000.0d;
			this.J48_VAD_object.buildClassifier(trainingSet);
			double f = (double)(System.currentTimeMillis())/1000.0d;
			final double trainingTime = f-s;
			
			System.out.println("trainingTime: "+trainingTime+" s");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			weka.core.SerializationHelper.write(trainingModelPath+"\\J48_VAD.model", this.J48_VAD_object);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public FastVector getFastVectorOfVAD()
	{
		return this.attributesVAD;
	}
	
	public Instance getInstanceOfVAD(double meanOfZCR, double varOfZCR, double varOfRMS, double lowEnergyFrameRate, double classType)
	{
		//initialize Instances
		Instances trainingSet = new Instances("Rel", this.attributesVAD, 0);
		// Make the last attribute be the class
		trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
		
		Instance instanceVAD = new Instance(VAD_dimension);
		instanceVAD.setDataset(trainingSet);
		
		instanceVAD.setValue(0, meanOfZCR);
		instanceVAD.setValue(1, varOfZCR);
		instanceVAD.setValue(2, varOfRMS);
		instanceVAD.setValue(3, lowEnergyFrameRate);
		instanceVAD.setValue(4, classType);
		
		return instanceVAD;
	}
	
	private void initVAD_FV()
	{
		FastVector fvClassVal = new FastVector(2);
		fvClassVal.addElement("no");
		fvClassVal.addElement("yes");
		Attribute ClassAttribute = new Attribute("humanVoice", fvClassVal);
		 
		this.attributesVAD = new FastVector(VAD_dimension);
		this.attributesVAD.addElement(new Attribute("meanOfZCR"));
		this.attributesVAD.addElement(new Attribute("varOfZCR"));
		this.attributesVAD.addElement(new Attribute("varOfRMS"));
		this.attributesVAD.addElement(new Attribute("lowEnergyFrameRate"));
		this.attributesVAD.addElement(ClassAttribute);
	}
}

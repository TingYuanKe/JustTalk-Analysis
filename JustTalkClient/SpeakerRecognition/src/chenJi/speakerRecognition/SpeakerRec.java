package chenJi.speakerRecognition;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;

import weka.classifiers.trees.J48;
import weka.clusterers.EM;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import chenJi.speakerRecognition.Object.*;
import chenJi.speakerRecognition.math.*;

public class SpeakerRec {

	private static int TrainingSeconds = 300;
	
	private static int sampleRate = 16000;
	private static int frameSize = 512;
	private static int slidingSize = 256;
	private static int mfcc_dimension = 13;
	private static int VAD_dimension = 5;
	
    private static FastVector attributesMFCC;
    private static FastVector attributesVAD;
    
   /* private static String trainingDataPath = "D:\\MyCode\\java\\workspace\\Speaker_Recognition\\SpeakerRec_Data\\training_data";
    private static String trainingModelPath = "D:\\MyCode\\java\\workspace\\Speaker_Recognition\\SpeakerRec_Data\\training_model";
    private static String trainingTestPath = "D:\\MyCode\\java\\workspace\\Speaker_Recognition\\SpeakerRec_Data\\testing_data";
    
    private static String trainingDataPath_emotion = "D:\\MyCode\\java\\workspace\\Emotion_Recognition\\EmotionRec_Data\\training_data\\CASIA";
	private static String trainingModelPath_emotion = "D:\\MyCode\\java\\workspace\\Emotion_Recognition\\EmotionRec_Data\\training_model";*/
    
	 private static String trainingDataPath = "C:\\Users\\TingYuan\\Desktop\\JustTalk from Bajo\\SpeakerRecognition\\SpeakerRec_Data\\training_data";
	 private static String trainingModelPath = "C:\\Users\\TingYuan\\Desktop\\JustTalk from Bajo\\SpeakerRecognition\\SpeakerRec_Data\\training_model";
	 private static String trainingTestPath = "C:\\Users\\TingYuan\\workspace\\SpeakerRecognition\\SpeakerRec_Data\\testing_data";
	    
	  private static String trainingDataPath_emotion = "D:\\An_eclipse\\Emotion_Recognition\\EmotionRec_Data\\training_data\\CASIA";
      private static String trainingModelPath_emotion = "D:\\An_eclipse\\Emotion_Recognition\\EmotionRec_Data\\training_model";
	
	
	private static EM model;
	private static DecimalFormat dcF_3 = new DecimalFormat("0.000");
	private static DecimalFormat dcF_2 = new DecimalFormat("0.00");
	private static HumanVoiceDetection VAD = new HumanVoiceDetection();
	
	private static String[] emotionType = {"angry", "sad", "happy", "neutral"};
	private static String[] trainingPersonOur = {"chenJi", "hsiangChih", "cya", "littleBall", "chung"};
	
	public static void main(String[] args) {
		
		init();
		
		//VAD.trainVAD();
//		testVAD();
//		precision_recall();		
		
		
		//for(int i=0; i<trainingPersonOur.length; i++) 
			//trainSpeakerModel_usingEmotion(i);
		trainEM(trainingDataPath+"\\300s\\", "hans", ".pcm");
		train_Evaluate_EM();
	}
	
	//groundTruth and testing pcm file precision_recall 
	private static void precision_recall()
	{
		double gap = 1.5;
		double runner = 60;
		double ava_precision = 0.0, ava_recall = 0.0;
		for(int dataNo=1; dataNo<=1; dataNo++) // number of test file
		{
			String groundTruth_fileName = "groundTruth_"+String.valueOf(dataNo)+".txt";
			String test_fileName = "testEX_"+String.valueOf(dataNo)+"(0.5s).txt";
			FileReader fr = null;
			BufferedReader br = null;
			File filedir = new File(trainingTestPath+"\\m_indoor_andy_wearable_h_chest_1\\");
			File fileIn;
			
			Turn_Info_List truthList = new Turn_Info_List();
			Turn_Info_List testList = new Turn_Info_List();
			Turn_Info_List turnsQueue = new Turn_Info_List();
			Turn_Info_List tmpList = new Turn_Info_List();
			double truthDuration = 0, testDuration = 0, overlapDuration = 0;
			
			//read groundTruth data
			fileIn = new File(filedir, groundTruth_fileName);
			if (fileIn.exists()) 
			{
				try {	
					fr = new FileReader(fileIn);
					br = new BufferedReader(fr);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					while(br.ready())
					{
						String[] tmpTime = br.readLine().split(":");
						//truthList.addElementToList(new Turn_Info(Double.parseDouble(tmpTime[0]), Double.parseDouble(tmpTime[1]), 0));
						if (Double.parseDouble(tmpTime[0]) < runner) 
						{
							truthList.addElementToList(new Turn_Info(Double.parseDouble(tmpTime[0]), Double.parseDouble(tmpTime[1]), 0));
						}
						else if ((Double.parseDouble(tmpTime[0]) < runner) && (Double.parseDouble(tmpTime[1]) > runner) ) 
						{
							truthList.addElementToList(new Turn_Info(Double.parseDouble(tmpTime[0]), runner, 0));
						}	
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				try {  
		            br.close();
		            fr.close();
		        } catch (IOException e) {  
		            e.printStackTrace();  
		        }
			}
			else
			{
				System.out.println(groundTruth_fileName+" is not exist");
			}
			
			//read testing data
			fileIn = new File(filedir, test_fileName);
			if (fileIn.exists()) 
			{
				try {	
					fr = new FileReader(fileIn);
					br = new BufferedReader(fr);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				try {
					while(br.ready())
					{
						String[] tmpTime = br.readLine().split(":");
						//tmpList.addElementToList(new Turn_Info(Double.parseDouble(tmpTime[0]), Double.parseDouble(tmpTime[1]), 1));
						if (Double.parseDouble(tmpTime[0]) < runner) 
						{
							tmpList.addElementToList(new Turn_Info(Double.parseDouble(tmpTime[0]), Double.parseDouble(tmpTime[1]), 0));
						}
						else if ((Double.parseDouble(tmpTime[0]) < runner) && (Double.parseDouble(tmpTime[1]) > runner) ) 
						{
							tmpList.addElementToList(new Turn_Info(Double.parseDouble(tmpTime[0]), runner, 0));
						}	
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				try {  
		            br.close();
		            fr.close();
		        } catch (IOException e) {  
		            e.printStackTrace();  
		        }
			}
			else
			{
				System.out.println(test_fileName+" is not exist");
			}
			
			
			double startTime = 0, endTime = 0;
			for(int i=0, flag=0; i<tmpList.getList().size(); i++)
			{
				if(flag==0)
				{
					startTime = tmpList.getList().get(i).getStartTime();
					endTime = tmpList.getList().get(i).getEndTime();
					flag = 1;
				}
				else
				{
					double tmpGap = tmpList.getList().get(i).getStartTime() - tmpList.getList().get(i-1).getEndTime();

					if(tmpGap <= gap) 
					{
						endTime = tmpList.getList().get(i).getEndTime();
					}
					else
					{
						testList.addElementToList(new Turn_Info(startTime, endTime, 1));
						flag = 0;
						i--;
					}
				}
			}
			testList.addElementToList(new Turn_Info(startTime, endTime, 1));
			
			
			truthDuration = 0.0;
			for(int i=0; i<truthList.getList().size(); i++)
				truthDuration += truthList.getList().get(i).getEndTime() - truthList.getList().get(i).getStartTime();
			
			testDuration = 0.0;
			for(int i=0; i<testList.getList().size(); i++)
				testDuration += testList.getList().get(i).getEndTime() - testList.getList().get(i).getStartTime();
			
			
			
			int truthIndex, testIndex;
			truthIndex = 0;
			testIndex = 0;
			
			while(truthIndex<truthList.getList().size() || testIndex<testList.getList().size())
			{
				if(truthIndex<truthList.getList().size() && testIndex>=testList.getList().size())
				{
					turnsQueue.addElementToList(truthList.getList().get(truthIndex));
					truthIndex++;
				}
				else if(testIndex<testList.getList().size() && truthIndex>=truthList.getList().size())
				{
					turnsQueue.addElementToList(testList.getList().get(testIndex));
					testIndex++;
				}
				else
				{
					if(testList.getList().get(testIndex).getStartTime() < truthList.getList().get(truthIndex).getStartTime())
					{
						turnsQueue.addElementToList(testList.getList().get(testIndex));
						testIndex++;
					}
					else
					{
						turnsQueue.addElementToList(truthList.getList().get(truthIndex));
						truthIndex++;
					}
				}
			}

			for(int i=0; i<turnsQueue.getList().size(); i++)
				System.out.println(i+" "+turnsQueue.getList().get(i).getStartTime()+" "+turnsQueue.getList().get(i).getEndTime()+" "+turnsQueue.getList().get(i).getOwner());
			
			overlapDuration = 0.0;
			int ptr_p, ptr_q;
			ptr_p = 0;
			ptr_q = ptr_p +1;
			
			while(ptr_q < turnsQueue.getList().size())
			{
				if(turnsQueue.getList().get(ptr_p).getOwner() != turnsQueue.getList().get(ptr_q).getOwner())
				{
					if(turnsQueue.getList().get(ptr_q).getStartTime() < turnsQueue.getList().get(ptr_p).getEndTime())
					{
						if(turnsQueue.getList().get(ptr_q).getEndTime() <= turnsQueue.getList().get(ptr_p).getEndTime())
							overlapDuration += turnsQueue.getList().get(ptr_q).getEndTime() - turnsQueue.getList().get(ptr_q).getStartTime();
						else 
							overlapDuration += turnsQueue.getList().get(ptr_p).getEndTime() - turnsQueue.getList().get(ptr_q).getStartTime();
					}
				}
				
				if(turnsQueue.getList().get(ptr_p).getEndTime() <= turnsQueue.getList().get(ptr_q).getEndTime()) ptr_p = ptr_q;
				ptr_q++;
			}
			
			double precisioin = (overlapDuration/testDuration)*100.0;
			double recall = (overlapDuration/truthDuration)*100.0;
			double fp = testDuration-overlapDuration;
			double fn = truthDuration-overlapDuration;
			double tn = runner - fp - fn - overlapDuration;
			double accuracy = ((overlapDuration+tn)/(overlapDuration+tn+fp+fn))*100.0;
			//double measure = (2*(precisioin*recall)/(precisioin+recall))*100.0;
			double measure = ((2*overlapDuration)/(2*overlapDuration+fp+fn))*100.0;
			ava_precision += precisioin;
			ava_recall += recall;
			System.out.println("data: "+test_fileName);
			//System.out.println(overlapDuration+" "+testDuration+" "+truthDuration);
			System.out.println("overlapDuration_tp: "+dcF_2.format(overlapDuration));
			System.out.println("tn: "+dcF_2.format(tn));
			System.out.println("fp: "+dcF_2.format(fp));
			System.out.println("fn: "+dcF_2.format(fn));
			System.out.println("testDuration:"+dcF_2.format(testDuration));
			System.out.println("truthDuration: "+dcF_2.format(truthDuration));
			System.out.println("Precision: "+dcF_2.format(precisioin));
			System.out.println("Recall: "+dcF_2.format(recall));
			System.out.println("Accuracy: "+dcF_2.format(accuracy));
			System.out.println("F-measure: "+dcF_2.format(measure));
			System.out.println();
			for(int i=0; i<testList.getList().size(); i++)
				System.out.println(testList.getList().get(i).getStartTime()+":"+testList.getList().get(i).getEndTime());
		}
		System.out.println("ava_precision: "+dcF_2.format(ava_precision/5));
		System.out.println("ava_recall: "+dcF_2.format(ava_recall/5));
	}
	
	private static void testVAD()
	{
		EM[] model = new EM[trainingPersonOur.length];
		Preprocess preProcess = new Preprocess();
		MFCC_Frame mfcc_frame = new MFCC_Frame(sampleRate, frameSize, mfcc_dimension);
		Mean_Var_LEFR meanVarLEFR = new Mean_Var_LEFR();
		
		FileInputStream fis = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		
		boolean useRMS = true;
		int readSizeOnce = 7936; //16128 7936
		double timeWin = 0.496;   //1.008 0.496
		double totalTime = 380;
		int sizeTime = (int) (totalTime/timeWin);

		int fileLen;
		int readCount = 0;
		int readSize = 0;
		byte[] data8bit;
		short[] data16bit;
		double[] originalSignal;
		double[][] framedSignal;
		double[][] framedMFCC;
	
		int[] slot = new int[sizeTime];
		int[] isHuman = new int[sizeTime];
		double[] meanOfRMS = new double[sizeTime];
		
		double[] likelihood_62frame = new double[trainingPersonOur.length];	
		for(int i=0; i<trainingPersonOur.length; i++)
		{
			try {
				//model[i] = (EM) weka.core.SerializationHelper.read(trainingModelPath+"\\training_300s_"+String.valueOf(i+1)+".model");
				model[i] = (EM) weka.core.SerializationHelper.read(trainingModelPath+"\\emotion_"+String.valueOf(i+1)+".model");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//human voice
		for(int dataNum=0; dataNum<=4; dataNum++)
		{
			File file = new File(trainingTestPath+"\\outdoor_cafe2\\test_"+String.valueOf(dataNum+1)+".pcm");
			String fileOutputName = "testEX_"+String.valueOf(dataNum+1)+"(0.5s).txt";
			if (file.exists()) 
			{
				data8bit = new byte[readSizeOnce*2];
				data16bit = new short[readSizeOnce];
				originalSignal = new double[readSizeOnce];
				int humanVoiceCnt = 0;
				int nonHumanVoiceCnt = 0;
				
				try {              
	                fis = new FileInputStream(file);
	                
	                File dir = new File(trainingTestPath+"\\outdoor_cafe2\\");
	                if(!dir.exists())
	                {
	                	boolean makeTrue = dir.mkdirs();
	                }     
	                File fileOut = new File(dir, fileOutputName);
	                if (fileOut.exists()) {
	                	fileOut.delete();  
	                }  
	                fw = new FileWriter(fileOut, false);
	                bw = new BufferedWriter(fw);
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
	    				readCount++;
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
		    			
		    			framedMFCC = new double[framedSignal.length][];
		    			for(int noOfFrame=0; noOfFrame<framedSignal.length; noOfFrame++)
		    			{
		    				framedMFCC[noOfFrame] = mfcc_frame.getMFCC(framedSignal[noOfFrame]);
		    			}
		    			
		    			boolean isHumanVoice = VAD.isHumanVoice(framedSignal);	
		    			
		    			if(isHumanVoice==true) 
		    			{
		    				//System.out.println(meanOfRMS+"  "+dcF_3.format((readCount-1)*1.008)+"~"+dcF_3.format((readCount)*1.008)+"s  "+humanVoiceCnt);
		    				//System.out.println(dcF_3.format((readCount-1)*1.008)+"~"+dcF_3.format((readCount)*1.008)+"s");
		    				
		    				for(int i=0; i<trainingPersonOur.length; i++)
			    			{
			    				likelihood_62frame[i] = 0.0;
			    				
			    				for(int noOfFrame=0; noOfFrame<framedSignal.length; noOfFrame++)
			    				{
			    					try {
			    						likelihood_62frame[i] += model[i].logDensityForInstance(getInstanceMFCC(framedMFCC[noOfFrame]));
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
			    				}
			    			}
			    			
			    			//decide which likelihood is max
			    			int maxIndex = 0;
			    			for(int i=1; i<trainingPersonOur.length; i++)
			    			{
			    				if(likelihood_62frame[maxIndex] < likelihood_62frame[i]) maxIndex = i;
			    			}
			    			
			    			if(maxIndex==dataNum) 
			    			{
			    				slot[readCount-1] = 1;
			    			}
			    			else 
			    			{
			    				slot[readCount-1] = 0;
			    			}
			    			
			    			humanVoiceCnt++;
			    			isHuman[readCount-1] = 1;
			    			meanOfRMS[readCount-1] = VAD.getMeanOfRMS(framedSignal);
		    			}
		    			else
	    				{
		    				slot[readCount-1] = 0;
		    				
		    				nonHumanVoiceCnt++;
		    				isHuman[readCount-1] = 0;
		    				meanOfRMS[readCount-1] = 0;
	    				}

		    			
		    			
		    			//System.out.println(dcF_3.format((readCount-1)*1.008)+"~"+dcF_3.format((readCount)*1.008)+"s : "+ isHumanVoice);
   				
	    			}
	    			else 
	    			{
	    				System.out.println("readCount: "+(readCount));
	    				break;
	    			}
	    		}
				
				System.out.println(humanVoiceCnt+" "+nonHumanVoiceCnt);
				
				if(useRMS)
				{
					int cnt = 0;
					double meanOfRMS_threshold = 0;;
					for(int i=0; i<sizeTime; i++)
					{
						if(isHuman[i]==1) 
						{
							cnt++;
							meanOfRMS_threshold += meanOfRMS[i];
						}
					}
					meanOfRMS_threshold /= cnt;
					
					System.out.println(cnt+" "+(sizeTime-cnt));
					
					for(int i=0; i<sizeTime; i++)
					{
						if(slot[i]==1)
						{
							if(meanOfRMS[i] > meanOfRMS_threshold) slot[i] = 1;
							else slot[i] = 0;
						}
					}
					
					/*int interval = 30;
					int round = (int) (sizeTime/interval);
					int reminder = sizeTime%interval;
					
					double[] meanArray;
					for(int i=0; i<round; i++)
					{
						meanArray = new double[interval];
						for(int j=0; j<interval; j++)
						{
							meanArray[j] = meanOfRMS[i*interval+j];
						}
						meanOfRMS_threshold = meanVarLEFR.getMean(meanArray);
						for(int j=0; j<interval; j++)
						{
							if(slot[i*interval+j]==1)
							{
								if(meanOfRMS[i*interval+j] > meanOfRMS_threshold) slot[i*interval+j] = 1;
								else slot[i*interval+j] = 0;
							}
						}
						
					}
					
					if(reminder!=0)
					{
						meanArray = new double[reminder];
						for(int j=0; j<reminder; j++)
						{
							meanArray[j] = meanOfRMS[round*interval+j];
						}
						meanOfRMS_threshold = meanVarLEFR.getMean(meanArray);
						for(int j=0; j<reminder; j++)
						{
							if(slot[round*interval+j]==1)
							{
								if(meanOfRMS[round*interval+j] > meanOfRMS_threshold) slot[round*interval+j] = 1;
								else slot[round*interval+j] = 0;
							}
						}
					}*/
				}
				
				
				int startIndex=0;
				int endIndex=0;
				int flag = 0;
				for(int i=0; i<sizeTime; i++)
				{
					if(flag==0)
					{
						if(slot[i]==1) 
						{
							startIndex = i;
							flag = 1;
						}
					}
					else
					{
						if(slot[i]==1) continue;
						else
						{
							endIndex = i-1;
							flag = 0;
							
							try {
								bw.write(dcF_3.format(startIndex*timeWin)+":"+dcF_3.format((endIndex+1)*timeWin));
								bw.newLine();
			    				bw.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    				
						}
					}
					
				}
				
				try {  
	    			fis.close();
	    			bw.close();
	    			fw.close();
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }
				
			}
			else
			{
				System.out.println("Open training data is not exist");
			}
		}
	}

	private static void trainSpeakerModel_usingEmotion(int personIndex)
	{
		int emotionClassNum = emotionType.length;
		
		EM EM_object = null;
		Instances trainingSet;
		MFCC_Frame mfcc_frame = new MFCC_Frame(sampleRate, frameSize, mfcc_dimension);
		Preprocess preProcess = new Preprocess();
		
		File fileIn = null;
		FileInputStream fis = null;
		int fileLen;
		byte[] data8bit;
		short[] data16bit;
		double[] originalSignal;
		double[][] framedSignal;
		double[] MFCC;

		int readSize = 0;
		int successFileCount = 0;
		int totalFrame = 0;
		
		//initialize Instances
		trainingSet = new Instances("Rel", attributesMFCC, 0);
		
		for(int emotionIndex=0; emotionIndex < emotionClassNum; emotionIndex++)
		{	
			
			for(int dataIndex=1; dataIndex<=20; dataIndex++)
			{
				fileIn = new File(trainingDataPath_emotion+"\\"+trainingPersonOur[personIndex]+"\\"+emotionType[emotionIndex]+"\\"+String.valueOf(dataIndex)+".snd");
				if (fileIn.exists()) 
				{
					fileLen = (int)fileIn.length();
					data8bit = new byte[fileLen];
					data16bit = new short[fileLen/2];
					originalSignal = new double[fileLen/2];
					
					try {              
		                fis = new FileInputStream(fileIn);
		            } catch (Exception e) {  
		                e.printStackTrace();  
		            }
					try {
						readSize = fis.read(data8bit, 0, fileLen);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					if(readSize==fileLen)
		    		{
		    			//byteÂàshort
		    			for (int i = 0; i < fileLen/2; i++)
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
		    					    			
		    			//System.out.println("frameSize: "+framedSignal.length);
		    			
		    			for(int i=0; i<framedSignal.length; i++)
		    			{
		    				//get MFCC of each frame
		    				MFCC = mfcc_frame.getMFCC(framedSignal[i]);
		    				//add trainingData(Instance) to trainingSet(Instances)
		    				trainingSet.add(getInstanceMFCC(MFCC));
		    			}
		    			
		    			totalFrame += framedSignal.length;
		    			successFileCount++;
		    		}
					
					try {  
		                fis.close();
		            } catch (IOException e) {  
		                e.printStackTrace();  
		            }
					
				}
				else
				{
					System.out.println("GG");
				}
			}
		}
		
		
		System.out.println("successFileCount: "+successFileCount);
		System.out.println("audioTotalTime: "+dcF_3.format((totalFrame-1)*0.016+0.032)+" s");
		
		
		
		
		//initialize EM
		EM_object = new EM();
		
		// set further options for EM  
	    String[] options = new String[4];  
	    // max. iterations   
	    options[0] = "-I";   
	    options[1] = "100";  
	    //set cluseter numbers  
	    options[2]="-N";  
	    options[3]="32"; 
	    
	    try {
			EM_object.setOptions(options);
			//EM_object.setMinStdDev(0.1);
			double s = (double)(System.currentTimeMillis())/1000.0d;
			EM_object.buildClusterer(trainingSet);
			double f = (double)(System.currentTimeMillis())/1000.0d;
			final double trainingTime = f-s;
			
			System.out.println("trainingTime: "+dcF_3.format(trainingTime)+" s");

			weka.core.SerializationHelper.write(trainingDataPath_emotion+"\\"+trainingPersonOur[personIndex]+"\\"+"emotionEX_"+String.valueOf(personIndex+1)+".model", EM_object);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private static void train_Evaluate_EM()
	{
		/*
		for(int i=1; i<=4; i++)
		{
			trainEM(trainingDataPath+"\\300s\\", "training_300s_"+String.valueOf(i), ".pcm");
		}
		
		for(int i=1; i<=4; i++)
		{
			try {
				model = (EM) weka.core.SerializationHelper.read(trainingModelPath+"\\training_300s_"+String.valueOf(i)+".model");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(int j=1; j<=4; j++)
			{
				evaluateEM(model, trainingDataPath+"\\300s\\", "training_300s_"+String.valueOf(j)+".pcm", trainingModelPath+"\\", "test_"+String.valueOf(i)+"_"+String.valueOf(j)+".txt");
			}
		}
		*/
		try {
			model = (EM) weka.core.SerializationHelper.read(trainingModelPath+"\\zn2.model");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		evaluateEM(model, trainingDataPath+"\\300s\\", "training_300s_"+String.valueOf(2)+".pcm", trainingModelPath+"\\", "test_EX.txt");
	}
	
	private static EM trainEM(String Path, String fileName, String fileType)
	{
		EM EM_object = null;
		Instances trainingSet;
		MFCC_Frame mfcc_frame = new MFCC_Frame(sampleRate, frameSize, mfcc_dimension);
		Preprocess preProcess = new Preprocess();
		
		FileInputStream fis = null;
		int fileLen;
		byte[] data8bit;
		short[] data16bit;
		double[] originalSignal;
		double[][] framedSignal;
		double[] MFCC;
		
		int readSize = 0;
		boolean trainingSuccess = false;
		
		File fileIn = new File(Path+fileName+fileType);
		if (fileIn.exists()) 
		{
			fileLen = (int)fileIn.length();
			data8bit = new byte[fileLen];
			data16bit = new short[fileLen/2];
			originalSignal = new double[fileLen/2];
			
			//initialize Instances
			trainingSet = new Instances("Rel", attributesMFCC, TrainingSeconds*sampleRate/frameSize);

        	try {              
                fis = new FileInputStream(fileIn);
            } catch (Exception e) {  
                e.printStackTrace();  
            }
    		
        	try {
				readSize = fis.read(data8bit, 0, fileLen);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        	
        	if(readSize==fileLen)
    		{
        		System.out.println("readSize: "+readSize);
    			//byteÂàshort
    			for (int i = 0; i < fileLen/2; i++)
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
    			
    			System.out.println("frameSize: "+framedSignal.length);
    			
    			for(int i=0; i<framedSignal.length; i++)
    			{
    				System.out.println("iterator: "+i);
    				//get MFCC of each frame
    				MFCC = mfcc_frame.getMFCC(framedSignal[i]);
    				//add trainingData(Instance) to trainingSet(Instances)
    				trainingSet.add(getInstanceMFCC(MFCC));
    			}
    		}
    		
    		try {  
                fis.close();
            } catch (IOException e) {  
                e.printStackTrace();  
            }
    		
    		//initialize EM
			EM_object = new EM();
			System.out.println("start training");
    		// set further options for EM  
    	    String[] options = new String[4];  
    	    // max. iterations   
    	    options[0] = "-I";   
    	    options[1] = "100";  
    	    //set cluseter numbers  
    	    options[2]="-N";  
    	    options[3]="32"; 
    	    
    	    try {
				EM_object.setOptions(options);
				//EM_object.setMinStdDev(0.1);
				double s = (double)(System.currentTimeMillis())/1000.0d;
				EM_object.buildClusterer(trainingSet);
				double f = (double)(System.currentTimeMillis())/1000.0d;
				final double trainingTime = f-s;
				
				System.out.println("trainingTime: "+dcF_3.format(trainingTime)+" s");

				weka.core.SerializationHelper.write(trainingModelPath+"\\"+fileName+".model", EM_object);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	    finally{
    	    	trainingSuccess = true;	
    	    }
        }
		else
		{
			trainingSuccess = false;
		}
		
		System.out.println("training comeplete");
		return trainingSuccess?EM_object:null;
	}
	
	private static void evaluateEM(EM EM_object, String readPath, String readName, String writePath, String writeName)
	{
		MFCC_Frame mfcc_frame = new MFCC_Frame(sampleRate, frameSize, mfcc_dimension);
		Preprocess preProcess = new Preprocess();
		
		FileInputStream fis = null;
		int fileLen;
		byte[] data8bit;
		short[] data16bit;
		double[] originalSignal;
		double[][] framedSignal;
		double[] MFCC;

		int readSize = 0;
		
		FileWriter fw = null;
		BufferedWriter bw = null;
		double likelihood = 0.0;
		double likelihood_total = 0.0;
		
		
		File fileIn = new File(readPath+readName);
		if (fileIn.exists()) 
		{
			fileLen = (int)fileIn.length();
			data8bit = new byte[fileLen];
			data16bit = new short[fileLen/2];
			originalSignal = new double[fileLen/2];

        	try {              
                fis = new FileInputStream(fileIn);
                
                
                File dir = new File(writePath);
                if(!dir.exists())
                {
                	boolean makeTrue = dir.mkdirs();
                }     
                File fileOut = new File(dir, writeName);
                if (fileOut.exists()) {
                	fileOut.delete();  
                }  
                fw = new FileWriter(fileOut, false);
                bw = new BufferedWriter(fw);
                
            } catch (Exception e) {  
                e.printStackTrace();  
            }
    		
        	try {
				readSize = fis.read(data8bit, 0, fileLen);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	
        	if(readSize==fileLen)
    		{
        		System.out.println("readSize: "+readSize);
    			//byteÂàshort
    			for (int i = 0; i < fileLen/2; i++)
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
    			
    			System.out.println("frameSize: "+framedSignal.length);
    			
    			for(int i=0; i<framedSignal.length; i++)
    			{
    				//get MFCC of each frame
    				MFCC = mfcc_frame.getMFCC(framedSignal[i]);
    				//get likelihood
    				try {
    					likelihood = EM_object.logDensityForInstance(getInstanceMFCC(MFCC));
    					likelihood_total += likelihood;
    				} catch (Exception e) {
    					likelihood = 100.0;
    					e.printStackTrace();
    				}
    				
    				try {
    					bw.write(dcF_3.format(likelihood));
    					bw.newLine();
    					bw.flush();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    			}
    			
    			try {
    				bw.write("likelihood_total: "+dcF_3.format(likelihood_total));
    				bw.newLine();
    				bw.write("likelihood_avarage: "+dcF_3.format(likelihood_total/framedSignal.length));
    				bw.newLine();
    				bw.flush();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    		}

        	try {  
    			fis.close();
    			bw.close();
    			fw.close();
            } catch (IOException e) {  
                e.printStackTrace();  
            }
		}
		else
		{
			System.out.println("File is not exist!");
		}
		
	}
	
	private static void init()
	{
		initWekaFV();
	}
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	private static void initWekaFV()
	{
		initMFCC_FV();
	}
	private static void initMFCC_FV()
	{
		attributesMFCC = new FastVector(mfcc_dimension-1);
		attributesMFCC.addElement(new Attribute("MFCC1"));
		attributesMFCC.addElement(new Attribute("MFCC2"));
		attributesMFCC.addElement(new Attribute("MFCC3"));
		attributesMFCC.addElement(new Attribute("MFCC4"));
		attributesMFCC.addElement(new Attribute("MFCC5"));
		attributesMFCC.addElement(new Attribute("MFCC6"));
		attributesMFCC.addElement(new Attribute("MFCC7"));
		attributesMFCC.addElement(new Attribute("MFCC8"));
		attributesMFCC.addElement(new Attribute("MFCC9"));
		attributesMFCC.addElement(new Attribute("MFCC10"));
		attributesMFCC.addElement(new Attribute("MFCC11"));
		attributesMFCC.addElement(new Attribute("MFCC12"));
	}
	
	private static Instance getInstanceMFCC(double[] dataMFCC)
	{
		Instance instanceMFCC = new Instance(mfcc_dimension-1);
		instanceMFCC.setValue(0, dataMFCC[1]);
		instanceMFCC.setValue(1, dataMFCC[2]);
		instanceMFCC.setValue(2, dataMFCC[3]);
		instanceMFCC.setValue(3, dataMFCC[4]);
		instanceMFCC.setValue(4, dataMFCC[5]);
		instanceMFCC.setValue(5, dataMFCC[6]);
		instanceMFCC.setValue(6, dataMFCC[7]);
		instanceMFCC.setValue(7, dataMFCC[8]);
		instanceMFCC.setValue(8, dataMFCC[9]);
		instanceMFCC.setValue(9, dataMFCC[10]);
		instanceMFCC.setValue(10, dataMFCC[11]);
		instanceMFCC.setValue(11, dataMFCC[12]);
		
		return instanceMFCC;
	}

}

package chenJi.speakerRecognition.Object;

public class Turn_Info {

	private double startTime;
	private double endTime;
	private int owner;
	private String emotion;
	
	public Turn_Info() {
	}
	public Turn_Info(double start, double end, int own) {
		this.startTime = start;
		this.endTime = end;
		this.owner = own;
		this.emotion = null;
	}
	
	public Turn_Info(double start, double end, int own, String emo) {
		this.startTime = start;
		this.endTime = end;
		this.owner = own;
		this.emotion = emo;
	}
	
	public double getStartTime()
	{
		return this.startTime;
	}
	public double getEndTime()
	{
		return this.endTime;
	}
	public int getOwner()
	{
		return this.owner;
	}
	
	public String getEmotion()
	{
		return this.emotion;
	}
}

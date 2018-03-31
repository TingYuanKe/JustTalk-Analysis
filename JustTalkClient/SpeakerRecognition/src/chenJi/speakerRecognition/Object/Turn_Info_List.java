package chenJi.speakerRecognition.Object;

import java.util.ArrayList;
import java.util.List;

public class Turn_Info_List {

	private List<Turn_Info> TurnInfoList = new ArrayList<Turn_Info>();
	
	public Turn_Info_List() {
	}
	public Turn_Info_List(List<Turn_Info> List) {
		this.TurnInfoList = List;
	}
	
	public void addElementToList(Turn_Info newElement)
	{
		this.TurnInfoList.add(newElement);
	}
	public List<Turn_Info> getList()
	{
		return this.TurnInfoList;
	}
}

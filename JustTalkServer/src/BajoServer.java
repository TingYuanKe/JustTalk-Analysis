import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;


public class BajoServer {
	public static ServerSock servsock = null;
	public static Socket sock = null;
	
	public final static int SOCKET_PORT = 7777; //
	// public final static String FILE_TO_SEND =
	// "C:/Users/TingYuanKeke/Desktop/test/test01.txt"; // you may change this
	public final static int FILE_SIZE = 1024 * 1024;
	// FIle recieve folder
	
	public static void main(String[] args) throws IOException{
		try{
			//showing IP
			servsock = new ServerSock(SOCKET_PORT);
			InetAddress ip= InetAddress.getLocalHost();
			System.out.println("Current IP Address : " + ip.getHostAddress());
			
			if(true){
				servsock.run();
				servsock.run();
			}
		} finally{
			if (servsock != null)
				servsock.close();
		}
	}
}


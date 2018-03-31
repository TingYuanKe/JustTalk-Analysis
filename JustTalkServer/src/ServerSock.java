import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerSock {
	private int port;
	private ServerSocket servsock;
    static String ipaddress = "localhost";
    
	public static boolean isPairing = false;
	public int collectInterval = 5000;
	public int pairingInterval =1000;
	
	public ServerSock(int port) throws IOException {
		this.port = port;
		this.servsock = new ServerSocket(port);
		
		System.out.println("JustTalkServer starts!");
	}

	public void run() {
		ExecutorService executor = Executors.newFixedThreadPool(12);
		while (true)// ¥Ã»·°õ¦æ
		{
			Socket sockName = null;
			Socket sockFile = null;
			try {
				System.out.println("waiting client...");
//				sockName = servsock.accept();
//				Runnable worker1 =new AudioRecieveThread(sockName);
//				executor.execute(worker1);
				sockFile = servsock.accept();
				 Runnable worker2 =new AudioRecieveThread(sockFile);
				 executor.execute(worker2);
				System.out.println("Accepted connection : " + sockFile + ","+sockName);
			} catch (IOException e) {
				e.printStackTrace();
			}
	
		
//			 Runnable worker = new FileRecieveThread(sock);
//			 executor.execute(worker);
		}
	}
	
	public void close() throws IOException{
		this.servsock.close();
	}
}


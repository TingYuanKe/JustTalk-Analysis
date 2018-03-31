import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;



public class NameRecieveThread  extends Thread{
	public final static int FILE_SIZE =  1024*1024*512 ;
	
	protected Socket socket;
	protected FileOutputStream fos = null;
	protected BufferedOutputStream bos = null;
	protected InputStream is = null;
	protected DataInputStream dis = null;
	
	public NameRecieveThread(Socket clientSocket) {
		this.socket = clientSocket;
		try {
			OutputStream outstream = socket .getOutputStream(); 
			PrintWriter out = new PrintWriter(outstream);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		
	}

	
}


import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;



public class AudioRecieveThread  extends Thread{
	public final static int FILE_SIZE =  1024*1024*512 ;
	
	protected Socket socket;
	protected FileOutputStream fos = null;
	protected BufferedOutputStream bos = null;
	protected InputStream is = null;
	protected DataInputStream dis = null;
	
	public AudioRecieveThread(Socket clientSocket) {
		this.socket = clientSocket;
		try {
			is = this.socket.getInputStream();
			dis = new DataInputStream(is);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
	
		receiveFile();
	}
	public void receiveName() {
		
	}
	public void receiveFile() {
		try {
			int bytesRead;
			int current = 0;
			
		
			System.out.println("Ready to recieve file");
			byte[] mybytearray = new byte[FILE_SIZE];
			InputStream is = socket.getInputStream();
			DataInputStream dis =  new DataInputStream(is); 
			
			//receive name
			String name  = dis.readUTF();
			System.out.println(" name = "+name);
			String WAV_TO_RECEIVED = "C:/Users/Public/Data/AudioData/"+name+".wav";
			String PCM_TO_RECEIVED = "C:/Users/Public/Data/AudioData/"+name+".wav";
			
			//receive Wav file
			is = socket.getInputStream();
			bytesRead = dis.read(mybytearray, 0, mybytearray.length);
			current = bytesRead;
			System.out.println("start downloading WAV");
			if (bytesRead > 1) {
				do {
					bytesRead = dis.read(mybytearray, current, (mybytearray.length - current));
					if (bytesRead >= 0)
						current += bytesRead;
					System.out.println("   "+bytesRead);
				} while (bytesRead > -1);
				fos = new FileOutputStream(WAV_TO_RECEIVED);
				bos = new BufferedOutputStream(fos);
				bos.write(mybytearray, 0, current);
	
				System.out.println("\n**Wav from *" + name + "* has been downloaded (" + current + " bytes read)\n");
			}
			
			//receive PCM file
			is = socket.getInputStream();
			bytesRead = dis.read(mybytearray, 0, mybytearray.length);
			current = bytesRead;
//			System.out.println("start downloading PCM bytesRead = "+bytesRead);
//			if (bytesRead > 1) {
//				do {
//					bytesRead = dis.read(mybytearray, current, (mybytearray.length - current));
//					if (bytesRead >= 0)
//						current += bytesRead;
//					System.out.println("   "+bytesRead);
//				} while (bytesRead > -1);
//				fos = new FileOutputStream(PCM_TO_RECEIVED);
//				bos = new BufferedOutputStream(fos);
//				bos.write(mybytearray, 0, current);
//	
//				
//				System.out.println("\n**PCM from *" + name + "* has been downloaded (" + current + " bytes read)\n");
//	
//			}
			bos.close();
			socket.shutdownInput();
			
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {


	}
}


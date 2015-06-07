
//author Keerti
import java.io.*;
import java.net.*;
public class Server 
{

	public static void serverFunction(int serverPort) throws IOException
	{
		int serverSequenceNumber = 0;
		int totalFileSize = 0;
		int packetLength = 10240;
		int offset = 0;
		DatagramSocket socket = new DatagramSocket(serverPort);
		byte[] fileArray = new byte[1024];
		DatagramPacket filePacket = new DatagramPacket(fileArray, fileArray.length);
		
		socket.receive(filePacket);
		fileArray = filePacket.getData();
		String requestedFile = new String(fileArray);
		System.out.println("requestedFile"+requestedFile);
		InetAddress IPaddress = filePacket.getAddress();
		int clientPort = filePacket.getPort();
		File file = new File(requestedFile.trim());
		
		
		if(!file.exists())
		{
			
			System.out.println("Requested file does not eist.");
			return;
		}
		
		InputStream iStreamFile = new FileInputStream(file);
		byte[] fileDataArray = new byte[(int) file.length()];
		iStreamFile.read(fileDataArray);
		System.out.println("file size is "+fileDataArray.length);
		
		int noOfLoop = (fileDataArray.length)/10240 +1;
		
		for(int i=0;i<noOfLoop;i++)
		{
			serverSequenceNumber ++;
			byte[] sendByte = new byte[10250];
			sendByte[0] = (byte) (serverSequenceNumber >> 8);
			sendByte[1] = (byte) (serverSequenceNumber);
			if((fileDataArray.length - totalFileSize) > 10240)
			{
				char ch = 'N';
				sendByte[2]=(byte)(ch & 0xff);
				sendByte[3]=(byte)(ch >> 8 & 0xff);
				sendByte[4] = (byte) (packetLength >> 8);
				sendByte[5] = (byte) (packetLength);
				sendByte[6] = (byte) (fileDataArray.length >> 24);
				sendByte[7] = (byte) (fileDataArray.length >> 16);
				sendByte[8] = (byte) (fileDataArray.length >> 8);
				sendByte[9] = (byte) (fileDataArray.length);
				System.arraycopy(fileDataArray, offset, sendByte, 10, packetLength-1);
				
			}
			else
			{
				System.out.println("Preparing last packet...");
				char ch = 'Y';
				sendByte[2]=(byte)(ch & 0xff);
				sendByte[3]=(byte)(ch >> 8 & 0xff);
				packetLength = fileDataArray.length - totalFileSize;
				sendByte[4] = (byte) (packetLength >> 8);
				sendByte[5] = (byte) (packetLength);
				sendByte[6] = (byte) (fileDataArray.length >> 24);
				sendByte[7] = (byte) (fileDataArray.length >>16);
				sendByte[8] = (byte) (fileDataArray.length >> 8);
				sendByte[9] = (byte) (fileDataArray.length);
				System.out.println("and the last packet size is :"+packetLength);
				System.arraycopy(fileDataArray, offset, sendByte, 10, packetLength-1);
			
			}
			
			DatagramPacket sendBytePacket = new DatagramPacket(sendByte,
					sendByte.length, IPaddress, clientPort);
			socket.send(sendBytePacket);
			System.out.println("+++++++++++++++Sent packet with sequence number = " + serverSequenceNumber);
					

			byte[] ackData = new byte[10];
			DatagramPacket ackDataPacket = new DatagramPacket(ackData, ackData.length);
			socket.setSoTimeout(500);
			
			while (true) 
			{

				try 
				{
					System.out.println("Receiving acknowledgement from client..");
					socket.receive(ackDataPacket);
					byte[] data = ackDataPacket.getData();
					String str = new String(data);
					if (!str.trim().equals("" + serverSequenceNumber)) 
					{
						System.out.println("Resending the packet with sequence number : "
								+ serverSequenceNumber);
						socket.send(sendBytePacket);
					} 
					else 
					{
						System.out.println("Packet with sequence number"
								+ serverSequenceNumber
								+ "is ackonwledged by the client..");
						break;
					}
					
				} 
				catch (SocketTimeoutException e) 
				{
					System.out.println("socket times out..and resending");
					socket.send(sendBytePacket);
				}
				
			}
			totalFileSize += packetLength;
			System.out.println("<=========totalFileSize is :"+totalFileSize+"==========>");
			offset +=10240;
		}//end of for loop
	}
	
	public static void main(String[] args) throws IOException
	{
		int serverPort = Integer.parseInt(args[0]);
		serverFunction(serverPort);
	}
}
//author Keerti
import java.io.*;
import java.net.*;

public class Client 
{
	public static void clientFunction(int serverPort,String fileName, InetAddress serverAddress) throws IOException
	{
		boolean lastPacket = false;
		int serverSeqNo = 0;
		int clientSeqNo = 1;
		int requestedFileSize=0;
		DatagramSocket socket = new DatagramSocket();
		byte[] fileData = new byte[1024];
		fileData = fileName.getBytes();
		DatagramPacket filePacket = new DatagramPacket(fileData, fileData.length,serverAddress,serverPort);
		System.out.println("Sending file request to server..");
		socket.send(filePacket);
		
		File file = new File(fileName);
		FileOutputStream outToFile = new FileOutputStream(file);
		
		while (!lastPacket) 
		{
			
			byte[] receivedPacketData = new byte[10250];
			byte[] dataPacket = new byte[10240];

			DatagramPacket receivedPacket = new DatagramPacket(receivedPacketData,
					receivedPacketData.length);
			socket.receive(receivedPacket);
			receivedPacketData = receivedPacket.getData();

			// Retrieve the sequence number from server's packet
			serverSeqNo = ((receivedPacketData[0] & 0xff) << 8) + (receivedPacketData[1] & 0xff);
			requestedFileSize = ((receivedPacketData[6] & 0xff) << 24) + ((receivedPacketData[7] & 0xff) << 16) + ((receivedPacketData[8] & 0xff) << 8) + (receivedPacketData[9] & 0xff);
			// Sequence numbers are being checked to verify the relevant packet transfer.
			if (serverSeqNo == clientSeqNo) 
			{
				char c = (char)((receivedPacketData[2] & 0xFF) + (receivedPacketData[3] << 8 & 0xff));
				if (c == 'Y') 
				{
					System.out.println("This is the last packet");
					lastPacket = true;
				} 
				else 
				{
					lastPacket = false;
				}
				clientSeqNo += 1;

				String ackToServer = Integer.toString(serverSeqNo);
				byte[] ackData = ackToServer.getBytes();
				DatagramPacket sendAckPacket = new DatagramPacket(ackData,
						ackData.length, serverAddress, serverPort);
				socket.send(sendAckPacket);
				//retrieve data from byte array
				if(lastPacket)
				{
					
					int length = ((receivedPacketData[4] & 0xff) << 8) + (receivedPacketData[5] & 0xff);
					System.out.println("and file size to write is"+length);
					dataPacket = new byte[length];
					System.out.println("the array is of the last packet is "+dataPacket.length);
					System.arraycopy(receivedPacketData, 10, dataPacket, 0, length);
				}
				else
				{
				System.arraycopy(receivedPacketData, 10, dataPacket, 0, 10240);
				}
				outToFile.write(dataPacket);
				System.out.println(">>>>>>>>>>>>>Total received file size is :"+file.length());
				System.out.println("===============data received is"+file.length());
				//System.out.println("===========requestedFileSize is "+requestedFileSize);
				double value1 = (double)file.length();
				double value2 = (double)requestedFileSize;
				double percent = (value1/value2) *(100);
				System.out.println("/////////////////////////////////////////////////////////////////////////");
			
				System.out.println("		Percentage of file transfer completed is... <<<<< "+percent+" % "+" >>>>>");
				
				System.out.println("////////////////////////////////////////////////////////////////////////");
				System.out.println("Received: Sequence number = "+ serverSeqNo);
				if (lastPacket)
				{	
					outToFile.close();
					socket.close();
					break;
				}
			} 
			else 
			{
				String ack = Integer.toString(serverSeqNo);
				byte[] ackData = ack.getBytes();
				DatagramPacket ackPacket = new DatagramPacket(ackData, ackData.length, serverAddress, serverPort);
				socket.send(ackPacket);
			}
		}
		socket.close();
		
	}
	
	
	public static void main(String[] args) throws IOException
	{
		String address = args[0];
		int serverPort = Integer.parseInt(args[1]);
		String fileName = args[2];
		InetAddress serverAddress = InetAddress.getByName(address);
		clientFunction(serverPort, fileName,serverAddress);
	}
}
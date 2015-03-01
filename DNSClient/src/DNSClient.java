import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class DNSClient
{
	static int counter =0;
	static boolean pointer =false;
	public static void sendQueryToServer(byte[] ip,String domainName) throws IOException
	{
		
		DatagramSocket socket = null;
		DatagramPacket sendPacket;
		int index=0;
		byte[] sendByte = new byte[512];
		
		sendByte[0] = 0x23;//1st id:9071
		sendByte[1] = 0x6F;
		//2nd
		sendByte[2] = 0x01;
		sendByte[3] = 0x00;
		//QDCOUNT
		sendByte[4] = 0x00;
		sendByte[5] = 0x01;
		//ANCOUNT
		sendByte[6] = 0x00;
		sendByte[7] = 0x00;
		//NSCOUNT
		sendByte[8] = 0x00;
		sendByte[9] = 0x00;
		//ARCOUNT
		sendByte[10] = 0x00;
		sendByte[11] = 0x00;
		//QNAME
		int currentIndex = 12;
		int count = 0;
		for(int i=0;i<domainName.length();i++)
		{
			if(domainName.charAt(i) == '.')
			{
				sendByte[currentIndex] = (byte)(int)(count);
				currentIndex += count +1;
				count=0; 
				i++;
			}
			char ch = domainName.charAt(i);
			String hex = String.format("0x%02X", (int) ch);
			byte value = (byte)ch;
			sendByte[currentIndex+count+1] = value;
			count++;
		}
		sendByte[currentIndex] = (byte)(count);
		sendByte[13+domainName.length()]=0x00;
		//QTYPE
		sendByte[14+domainName.length()] = 0x00;
		sendByte[15+domainName.length()] = 0x01;
		//QCLASS
		sendByte[16+domainName.length()] = 0x00;
		sendByte[17+domainName.length()] = 0x01;
		long packetTimer = System.nanoTime();
		socket = new DatagramSocket();
		InetAddress address = null;
		address = InetAddress.getByAddress(ip);
		sendPacket = new DatagramPacket(sendByte,sendByte.length,address,53);
		socket.send(sendPacket);
		/*receive packet from server */
		byte[] buf = new byte[512];
		byte[] received = new byte[512];
		DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
		
		
		try{
		socket.setSoTimeout(1500);
		socket.receive(receivePacket);
		received = receivePacket.getData();
		}
		catch(SocketTimeoutException e)
		{
			
		counter ++;
		socket.send(sendPacket);
		socket.close();
		}
		System.out.println("counter "+counter);
			long packetTimer2 = System.nanoTime();
			long diff = packetTimer2 - packetTimer;

		System.out.println("ANSWER SECTION :");
		parseResponse(received);
	}
	
	public static void parseResponse(byte[] received)
	{
		int n =29;
		while(true)
		{
			if(n > received.length-1)
			{
				break;
				//continue;
			}
			n = readResponse(received, n);
		}
	}
		
	
	public static int readResponse(byte[] received,int n)
	{
		
		if(n >received.length-1)
			return n;
		int ptr = received[n];
		if(ptr == -64)
		{
			n = n+1;
			int ind;	
			ind = received[n];
			n = extractSite(received, ind, n);
			n =n+1;
		}
		if(received[n] !=-64)
		{
			n = getExtras(received, n);
			n=n+1;
			int rdLength;
			rdLength = received[n] + received[++n];
			n =n+1;
			if(rdLength ==4)
			{
				n = extractIP(received, n,n);
				System.out.print("\n");
			}
			else
			{
				n = getResponse(received, n, n, rdLength);
				System.out.print("\n");
			}
			if(pointer)
			{
				
			}
			else
			{
				n = n+1;
			}
		}
		
		
		return n;
		
	}
	
	public static int getResponse(byte[] received,int n,int m,int rdLength)
	{
		pointer = false;
		//exitAfterPointer = false;
		int lastIndex = n + rdLength; 
		while(true)
		{
			if(n > received.length-1)
			{
				break;
			}
			int p = received[n];
			int q = n+1;
			if(p ==-64)
			{
				n = n+1;
				int ind = received[n];
				n = extractSite(received, ind, n);
				n = n+1;
			}
			else if(p >0)
			{
				n = n+1;
				String str = getDomain(received, n, p);
				System.out.print(str);
				System.out.print(".");
				n = n+p;
				
			}
			else if(p==0)
			{
				break;
			}
			if(p ==-64 && lastIndex==n)
			{
				pointer =true;
				break;
				
			}
		}
		if(m==n)
		{
			return m;
		}
		if(m<n)
		{
			return n;
		}
		else 
		{
			return m;
		}
		
	}

	public static int extractSite(byte[] received,int n,int m)
    {
    	pointer = false;
    	while(true)
    	{
    		if(n > received.length-1 )
    				break;
    		
    		int p =received[n];
    		int q= n+1;
    		if(p == -64 && q==m)
    			return m;
    		if(p == -64)
    		{
    			++n;
    			int index = received[n];
    			n = extractSite(received, index, n);
    			++n;
    		}
    		if(p==0)
    		{
    			break;
    		}
    		else if(p>0)
    		{
    			n++;
    			String str = getDomain(received, n, p);//++n
    			System.out.print(str);
    			System.out.print(".");
    			n = n+p;
    		}
    	}
    	System.out.print("  ");
    	if(m==n)
    	{
    		return m;
    	}
    	if(m<n)
    	{
    		return n;
    	}
    	else
    	{
    		return m;
    	}
    	    	
		
    }
    
    public static String getDomain(byte[] received,int index,int length)
    {
    	length = index +length;
    	String ans="";
    	while(true)
    	{
    		ans += (char)received[index];
    		index++;
    		if(index == length)
    		{
    			break;
    		}
    	}
    	return ans;
    }

    public static int getExtras(byte[] received, int n)
    {
    	int value = received[n] + received[++n];
		if(value == 5)
		{
			System.out.print("    CNAME");
		}
		else if(value ==1)
		{
			System.out.print("A");
		}
		else if(value == 15)
		{
			System.out.print("MX");
		}
		value = received[++n]+received[++n];// value is QCLASS value
		if(value == 1)
		{
			System.out.print("     IN     ");
		}
		
		String bin1 = decimalToBinary(received[++n]) + decimalToBinary(received[++n]) +
				decimalToBinary(received[++n]) + decimalToBinary(received[++n]);
		int decimalValue = Integer.parseInt(bin1, 2);
		System.out.print(decimalValue+"     ");
		return n;
    	
    }
    
    public static int extractIP(byte[] received, int n, int m)
    {
    	int last = n+3;
    	while(true)
    	{
    		int p = received[n];
    		String dec =decimalToBinary(p);
    		int decimalValue = Integer.parseInt(dec, 2);
    		System.out.print(decimalValue);
    		System.out.print(".");
    		if(last == n)
    		{
    			break;
    		}
    		n = n+1;
    		
    	}
    	return n;
    	    	
    }
    
    
    public static String decimalToBinary(int decimal)
    {
    	String bin =null;
    	String str = Integer.toString(decimal);
    	if(str.startsWith("-"))
    	{
    		
    		bin = Integer.toBinaryString(decimal).substring(24, 32); 
    		
    	}
    	else
    	{
    	
    		bin = Integer.toBinaryString(decimal);
    	}
    	
    	return bin;
    }
	public static void main(String[] args)
	{	
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(args[0]);
		} catch (UnknownHostException e) {
					e.printStackTrace();
		}
		byte[] ip = addr.getAddress();
		String domainName = args[1];
		for(int i=0;i<10;i++)
		{
		try {
			sendQueryToServer(ip,domainName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		}
		
		
	}
}
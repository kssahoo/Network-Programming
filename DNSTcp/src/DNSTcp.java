import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.CharBuffer;

public class DNSTcp {
	static boolean pointer = false;
	static long minTime = 999999;
	static long maxTime = 0;
	static long avgTime = 0;
	static long totalTime = 0;

	public static int sendQueryToServer(byte[] ip, String type, String ip1,
			String domainName, String QTYPE, String mode) throws IOException {
		byte[] received = new byte[1024];
		if (type.equals("tcp") || type.equals("TCP")) {
			DataOutputStream outToServer = null;
			DataInputStream inFromServer = null;
			Socket clientSocket = null;

			System.out.println("Inside TCP");
			clientSocket = new Socket(ip1, 53);
			long packetTimer1 = System.currentTimeMillis();
			byte[] sendByte = new byte[1024];
			sendByte[0] = 0x23;// 1st id:9071
			sendByte[1] = 0x6F;

			// 2nd
			sendByte[2] = 0x01;
			sendByte[3] = 0x00;
			// QDCOUNT
			sendByte[4] = 0x00;
			sendByte[5] = 0x01;
			// ANCOUNT
			sendByte[6] = 0x00;
			sendByte[7] = 0x00;
			// NSCOUNT
			sendByte[8] = 0x00;
			sendByte[9] = 0x00;
			// ARCOUNT
			sendByte[10] = 0x00;
			sendByte[11] = 0x00;
			// QNAME
			int currentIndex = 12;
			int count = 0;

			for (int i = 0; i < domainName.length(); i++) {
				if (domainName.charAt(i) == '.') {
					sendByte[currentIndex] = (byte) (int) (count);
					currentIndex += count + 1;
					count = 0;
					i++;
				}
				char ch = domainName.charAt(i);
				String hex = String.format("0x%02X", (int) ch);
				byte value = (byte) ch;
				sendByte[currentIndex + count + 1] = value;
				count++;
			}
			sendByte[currentIndex] = (byte) (count);
			sendByte[13 + domainName.length()] = 0x00;
			// QTYPE

			switch (QTYPE) {
			case "A":
				sendByte[14 + domainName.length()] = 0x00;
				sendByte[15 + domainName.length()] = 0x01;
			case "NS":
				sendByte[14 + domainName.length()] = 0x00;
				sendByte[15 + domainName.length()] = 0x02;
			case "MX":
				sendByte[14 + domainName.length()] = 0x01;
				sendByte[15 + domainName.length()] = 0x00;
			}

			sendByte[16 + domainName.length()] = 0x00;
			sendByte[17 + domainName.length()] = 0x01;
			int size = 18 + domainName.length();
			byte[] sendByte1 = { 0, 0 };
			sendByte1[0] = 0x00;
			sendByte1[1] = (byte) (size & 0xff);

			try {
				outToServer = new DataOutputStream(
						clientSocket.getOutputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				outToServer.write(sendByte1);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {

				outToServer.write(sendByte, 0, size);

			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// DataInputStream inFromServer;
			try {
				inFromServer = new DataInputStream(
						clientSocket.getInputStream());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// byte[] received = new byte[1024];
			try {
				inFromServer.read(received);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (mode.equals("experiment") || mode.equals("EXPERIMENT")) {
				long packetTimer2 = System.currentTimeMillis();
				long packetTimer = packetTimer2 - packetTimer1;
				System.out.println("Response time is :" + packetTimer);
				if (minTime > packetTimer) {
					minTime = packetTimer;
				}
				if (maxTime < packetTimer) {
					maxTime = packetTimer;
				}
				totalTime += packetTimer;
			}
			if (mode.equals("answer") || mode.equals("ANSWER")) {
				parseResponse(received, type, size);
				clientSocket.close();
			}
		}

		if (type.equals("udp") || type.equals("UDP")) {
			System.out.println("Inside UDP");
			byte[] sendByte = new byte[1024];
			DatagramSocket socket = null;
			DatagramPacket sendPacket;
			sendByte[0] = 0x23;// 1st id:9071
			sendByte[1] = 0x6F;

			// 2nd
			sendByte[2] = 0x01;
			sendByte[3] = 0x00;
			// QDCOUNT
			sendByte[4] = 0x00;
			sendByte[5] = 0x01;
			// ANCOUNT
			sendByte[6] = 0x00;
			sendByte[7] = 0x00;
			// NSCOUNT
			sendByte[8] = 0x00;
			sendByte[9] = 0x00;
			// ARCOUNT
			sendByte[10] = 0x00;
			sendByte[11] = 0x00;
			// QNAME
			int currentIndex = 12;
			int count = 0;
			// System.out.println(domainName.length());
			for (int i = 0; i < domainName.length(); i++) {
				if (domainName.charAt(i) == '.') {
					sendByte[currentIndex] = (byte) (int) (count);
					currentIndex += count + 1;
					count = 0;
					i++;
				}
				char ch = domainName.charAt(i);
				String hex = String.format("0x%02X", (int) ch);
				byte value = (byte) ch;
				sendByte[currentIndex + count + 1] = value;
				count++;
			}
			sendByte[currentIndex] = (byte) (count);
			sendByte[13 + domainName.length()] = 0x00;
			// QTYPE
			switch (QTYPE) {
			case "A":
				sendByte[14 + domainName.length()] = 0x00;
				sendByte[15 + domainName.length()] = 0x01;
			case "NS":
				sendByte[14 + domainName.length()] = 0x00;
				sendByte[15 + domainName.length()] = 0x02;
			case "MX":
				sendByte[14 + domainName.length()] = 0x01;
				sendByte[15 + domainName.length()] = 0x00;
			}

			// QCLASS
			sendByte[16 + domainName.length()] = 0x00;
			sendByte[17 + domainName.length()] = 0x01;
			int size = 18 + domainName.length();

			long packetTimer = System.currentTimeMillis();
			socket = new DatagramSocket();
			socket.setSoTimeout(1500);

			InetAddress address = null;
			address = InetAddress.getByAddress(ip);
			sendPacket = new DatagramPacket(sendByte, sendByte.length, address,
					53);
			try {
				socket.send(sendPacket);
				byte[] buf = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(buf,
						buf.length);
				socket.receive(receivePacket);
				received = receivePacket.getData();
			} catch (SocketTimeoutException e) {
				socket.close();
				return 0;

			}

			if (mode.equals("experiment") || mode.equals("EXPERIMENT")) {
				long packetTimer2 = System.currentTimeMillis();
				long responseTime = packetTimer2 - packetTimer;
				System.out.println("Response time is :" + responseTime);
				if (minTime > responseTime) {
					minTime = responseTime;
				}
				if (maxTime < responseTime) {
					maxTime = responseTime;
				}
				totalTime += responseTime;
			}

			if (mode.equals("answer") || mode.equals("ANSWER")) {
				System.out.println("ANSWER SECTION :");
				parseResponse(received, type, size);
				socket.close();
			}
		}
		return 1;
	}

	public static void getTimeDetails(int noOfLoops) {
		System.out.println("Minimum response time is :" + minTime);
		System.out.println("Maximum response time is :" + maxTime);
		avgTime = (totalTime) / noOfLoops;
		System.out.println("Average response time is :" + avgTime);
	}

	public static void parseResponse(byte[] received, String type, int size) {
		int n;
		if (type.equals("tcp") || type.equals("TCP")) {
			n = size + 2;
		} else {
			n = size;
		}
		// System.out.println("Received size : " + received.length);
		int ans = 0;
		int totAns = 0;
		if (type.equals("tcp") || type.equals("TCP")) {
			totAns = received[8] + received[9];
		} else {

			totAns = received[6] + received[7];
		}

		while (true) {
			ans = ans + 1;
			if (n > received.length - 1) {
				break;
			}
			n = getAnswer(received, n, type);

			if (ans == totAns) {
				break;
			}
		}
	}

	public static int getAnswer(byte[] received, int n, String type) {

		if (n > received.length - 1)
			return n;
		int ptr = received[n];
		if (ptr == -64) {
			n = n + 1;
			int ind;
			ind = received[n];
			if (type.equals("tcp") || type.equals("TCP")) {
				ind = ind + 2;
			}

			n = getSiteContent(received, ind, n, type);
			n = n + 1;
		}
		if (received[n] != -64) {
			n = getExtras(received, n);
			n = n + 1;
			int rdLength;
			rdLength = received[n] + received[++n];
			n = n + 1;
			if (rdLength == 4) {
				n = getIPDetails(received, n, n);
				System.out.print("\n");
			} else {
				n = extractQans(received, n, n, rdLength, type);
				System.out.print("\n");
			}
			if (pointer) {
			} else {
				n = n + 1;
			}
		}
		return n;
	}

	public static int extractQans(byte[] received, int n, int k, int rdLength,
			String type) {
		pointer = false;
		int lastIndex = n + rdLength;
		while (true) {
			if (n > received.length - 1) {
				break;
			}
			int p = received[n];
			int m = n + 1;
			if (p == -64) {
				n = n + 1;
				int ind = received[n];
				if (type.equals("tcp") || type.equals("TCP")) {
					ind = ind + 2;
				}
				n = getSiteContent(received, ind, n, type);
				n = n + 1;
			} else if (p > 0) {
				n = n + 1;
				String str = getDomain(received, n, p);
				System.out.print(str);
				System.out.print(".");
				n = n + p;

			} else if (p == 0) {
				break;
			}
			if (p == -64 && lastIndex == n) {
				pointer = true;
				break;
			}
		}
		if (k == n) {
			return k;
		}
		if (k < n) {
			return n;
		} else {
			return k;
		}
	}

	public static int getSiteContent(byte[] received, int n, int k, String type) {
		pointer = false;
		while (true) {
			if (n > received.length - 1)
				break;

			int p = received[n];
			int m = n + 1;
			if (p == -64 && m == k)
				return k;
			if (p == -64) {
				++n;
				int index = received[n];
				if (type.equals("tcp") || type.equals("TCP")) {
					index = index + 2;
				}
				n = getSiteContent(received, index, n, type);
				++n;
			}
			if (p == 0) {
				break;
			} else if (p > 0) {
				n++;
				String str = getDomain(received, n, p);// ++n
				System.out.print(str);
				System.out.print(".");
				n = n + p;
			}
		}
		System.out.print("  ");
		if (k == n) {
			return k;
		}
		if (k < n) {
			return n;
		} else {
			return k;
		}
	}

	public static String getDomain(byte[] received, int index, int length) {
		length = index + length;
		String ans = "";
		while (true) {
			ans += (char) received[index];
			index++;
			if (index == length) {
				break;
			}
		}
		return ans;
	}

	public static int getExtras(byte[] received, int n) {
		int value = received[n] + received[++n];
		if (value == 5) {
			System.out.print("    CNAME");
		} else if (value == 1) {
			System.out.print("A");
		} else if (value == 15) {
			System.out.print("MX");
		}
		value = received[++n] + received[++n];// value is QCLASS value
		if (value == 1) {
			System.out.print("     IN     ");
		}

		String bin1 = decimalToBinary(received[++n])
				+ decimalToBinary(received[++n])
				+ decimalToBinary(received[++n])
				+ decimalToBinary(received[++n]);
		int decimalValue = Integer.parseInt(bin1, 2);
		System.out.print(decimalValue + "     ");
		return n;

	}

	public static int getIPDetails(byte[] received, int n, int k) {
		int last = n + 3;
		while (true) {
			int p = received[n];
			String dec = decimalToBinary(p);
			int decimalValue = Integer.parseInt(dec, 2);
			System.out.print(decimalValue);
			if (n < last) {
				System.out.print(".");
			}
			if (last == n) {
				break;
			}
			n = n + 1;

		}
		return n;

	}

	public static String decimalToBinary(int decimal) {
		String bin = null;
		String str = Integer.toString(decimal);
		if (str.startsWith("-")) {
			bin = Integer.toBinaryString(decimal).substring(24, 32);

		} else {
			// Integer intgr = new Integer(decimal);
			bin = Integer.toBinaryString(decimal);
		}
		// System.out.println("bin"+bin);
		return bin;
	}

	/**
	 * Print input arguments
	 * 
	 * @param args
	 */
	private static void printArg(String[] args) {
		System.out.println("Argument size : " + args.length);
		System.out.println("Arguments:");
		System.out.println("**********");
		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i]);
		}
	}

	public static void main(String[] args) {
		int pass = 0;
		int fail = 0;
		InetAddress addr = null;
		try {
			addr = InetAddress.getByName(args[1]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		String type = args[0];
		String ip1 = args[1];
		byte[] ad = addr.getAddress();
		String domainName = args[2];
		String qtype = args[3];
		int noOfLoop = Integer.parseInt(args[4]);
		String mode = args[5];
		int result;
		if (type.equals("tcp") || type.equals("TCP")) {
			for (int i = 0; i < noOfLoop; i++) {
				try {
					result = sendQueryToServer(ad, type, ip1, domainName,
							qtype, mode);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (mode.equals("experiment") || mode.equals("EXPERIMENT")) {
				getTimeDetails(noOfLoop);
			}
		} else if (type.equals("udp") || type.equals("UDP")) {
			for (int i = 0; i < noOfLoop; i++) {
				try {
					result = sendQueryToServer(ad, type, ip1, domainName,
							qtype, mode);
					if (result == 1) {
						pass++;
						System.out.println("Successful");
					} else {
						fail++;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("No of Successes :" + pass);
			System.out.println("No of Failures  :" + fail);
			if (mode.equals("experiment") || mode.equals("EXPERIMENT")) {
				getTimeDetails(noOfLoop);
			}
		}

	}
}
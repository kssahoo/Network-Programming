import java.io.*;
import java.net.*;
import java.util.HashMap;

public class SocketReceive {
	static HashMap<String, Integer> map = new HashMap<String, Integer>();

	public void receiveResponse() {
		MulticastSocket socket = null;
		DatagramPacket inPacket = null;
		byte[] inBuf = new byte[256];
		byte[] received = new byte[256];

		try {
			// Prepare to join multicast group
			socket = new MulticastSocket(5353);
			InetAddress address = InetAddress.getByName("224.0.0.251");
			socket.joinGroup(address);

			while (true) {
				inPacket = new DatagramPacket(inBuf, inBuf.length);
				socket.receive(inPacket);
				received = inPacket.getData();

				int index = 12;
				StringBuilder sb = new StringBuilder();

				char ch = (char) received[12];
				sb.append(ch); // c
				ch = (char) received[13];
				sb.append(ch); // s

				int p = (char) received[14];
				ch = (char) p;
				sb.append(ch);// 6
				// System.out.println("Read 15");
				p = (char) received[15];
				ch = (char) p;
				sb.append(ch);// 2

				p = (char) received[16];
				ch = (char) p;
				sb.append(ch);// 1
				ch = (char) received[17];
				sb.append(ch);// -
				ch = (char) received[18];
				sb.append(ch);// c
				ch = (char) received[19];
				sb.append(ch); // a
				ch = (char) received[20];
				sb.append(ch);// c
				ch = (char) received[21];
				sb.append(ch);// c
				ch = (char) received[22];
				sb.append(ch);// h
				ch = (char) received[23];
				sb.append(ch);// e
				String str = new String(sb);

				if (str.trim().equals("cs621-cache")) {
					String addr = inPacket.getAddress().toString();
					int port1 = inPacket.getPort();
					map.put(addr, port1);
				} else {
					System.out.println("Service types are different");
				}
			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}

	public HashMap<String, Integer> getHashMap() {
		return map;
	}

	public static void main(String[] args) {
	}
}

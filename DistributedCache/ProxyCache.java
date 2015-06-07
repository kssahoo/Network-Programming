/**
 * ProxyCache.java - Simple caching proxy
 *
 * $Id: ProxyCache.java,v 1.3 2004/02/16 15:22:00 kangasha Exp $
 *
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ProxyCache {
	private static String CRLF = "\r\n";;
	/** Port for the proxy */
	private static int port;
	private static int port1;
	static int myPort = 0;
	/** Socket for client connections */
	private static ServerSocket socket;

	public static long start;

	/** Create the ProxyCache object and the socket */
	private static Map<String, String> cache = new HashMap<String, String>();
	static HashMap<String, Integer> hashMap = new HashMap<String, Integer>();

	// HashMap<InetAddress,Integer> map = new HashMap<InetAddress,Integer>();
	public static void init(int p) {
		port1 = p;
		try {
			socket = new ServerSocket(port1);
		} catch (IOException e) {
			System.out.println("Error creating socket: " + e);
			System.exit(-1);
		}
	}

	public static HttpResponse handle(Socket client) {
		System.out.println("handle method");
		Socket server = null;
		Socket peer = null;
		HttpRequest request = null;
		HttpResponse response = null;
		boolean local = false;
		/*
		 * Process request. If there are any exceptions, then simply return and
		 * end this request. This unfortunately means the client will hang for a
		 * while, until it timeouts.
		 */
		request = readRequest(client);
		SocketReceive sr = new SocketReceive();
		hashMap = sr.getHashMap();

		if (request != null)
			if (!request.getError()) {
				if (cache.containsKey(request.getURI())) {

					// System.out.println("$======Serving from local cache=======$");
					// Get from cache and send it to client
					String fCache = cache.get(request.getURI());
					File cacheFile = new File(fCache);
					int totalBytes = (int) cacheFile.length();
					byte[] cacheData = new byte[totalBytes];
					try {
						System.out.println("serving from local cache");
						FileInputStream inFile = new FileInputStream(cacheFile);
						inFile.read(cacheData);

						DataOutputStream toClient = new DataOutputStream(
								client.getOutputStream());
						System.out.println(client.getInetAddress());
						System.out.println(client.getLocalAddress());
						if (client.getInetAddress().equals(
								client.getLocalAddress())) {
							System.out
									.println("Serving from the local machine");
							toClient.write(cacheData);
						} else {
							System.out.println("Serving from remote machine");
							toClient.write(cacheData);
						}

						long end = System.currentTimeMillis();
						System.out.println("From cache " + (end - start));
						client.close();
						inFile.close();// added

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					local = true;
				}

				if (!client.getInetAddress().equals(client.getLocalAddress())
						&& !local) {
					boolean proxy = false;
					System.out.println("Request is from a remote machine");
					DataOutputStream toClient = null;
					try {
						toClient = new DataOutputStream(
								client.getOutputStream());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String reply = "NOTFOUND";
					try {
						toClient.writeBytes(reply);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						toClient.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Response sent to remote machine");
				}
				if (client.getInetAddress().equals(client.getLocalAddress())
						&& !local) {
					boolean proxyValue = false;
					Integer portValue = 6772;
					System.out.println("Request is from local; mahcine");
					for (String addre : hashMap.keySet()) {
						String addrs[] = addre.split("/");
						String address = addrs[1];

						String[] str = client.getLocalAddress().toString()
								.split("/");
						String s = str[1];
						// System.out.println("client.getLocalAddress()"+s);
						if (address.equals(s)) {
							System.out
									.println("My local adddres.Hence continue the loop");
							continue;
						} else {

							peer = sendRequestToPeer(request, address,
									portValue);
							response = getResponseFromPeer(peer);
							if (!response.toString().trim().equals("NOTFOUND")) {
								System.out
										.println("==============Serving from peer===============");
								addResponseToCache(request, response);
								sendResponseToClient(response, client, server);
								long end = System.currentTimeMillis();
								System.out
										.println("From peer " + (end - start));
								proxyValue = true;
								break;
							} else {
								proxyValue = false;
								// break;
							}

						}

					}
					if (!proxyValue) {
						System.out.println("Serving from server");
						server = sendRequestToServer(request);
						response = getResponseFromServer(server);
						addResponseToCache(request, response);

						addResponseToCache(request, response);
						/* Get response from server and send it to client */
						sendResponseToClient(response, client, server);

						long end = System.currentTimeMillis();
						System.out.println("From server " + (end - start));
						try {
							client.close();
							server.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else {
				System.out.println("Ignored request");
			}

		return response;
	}

	private static Socket sendRequestToServer(HttpRequest request) {
		try {
			/* Open socket and write request to socket */
			Socket server = new Socket(request.getHost(), request.getPort());
			DataOutputStream toServer = new DataOutputStream(
					server.getOutputStream());
			toServer.writeBytes(request.toString());//
			return server;
		} catch (UnknownHostException e) {
			System.out.println("Unknown host: " + request.getHost());
			System.out.println(e);
			return null;
		} catch (IOException e) {
			System.out.println("Error writing to server: " + e);
			return null;
		}
	}

	/* Read request */
	private static HttpRequest readRequest(Socket client) {
		try {
			BufferedReader fromClient = new BufferedReader(
					new InputStreamReader(client.getInputStream()));
			HttpRequest request = new HttpRequest(fromClient);

			if (request.getError()) {
				return null;
			}

			return request;

		} catch (IOException e) {
			System.out.println("Error reading from client: " + e);
			return null;
		}
	}

	// get response from server.
	public static HttpResponse getResponseFromServer(Socket server) {
		try {
			// System.out.println("Get response from serveer");
			DataInputStream fromServer = new DataInputStream(
					server.getInputStream());
			HttpResponse response = new HttpResponse(fromServer);
			return response;
		} catch (IOException e) {
			System.out.println("Error writing response to client: " + e);
			return null;

		}
	}

	// send response from to client
	private static void sendResponseToClient(HttpResponse response,
			Socket client, Socket server) {
		try {

			DataOutputStream toClient = new DataOutputStream(
					client.getOutputStream());
			toClient.writeBytes(response.toString());//

			toClient.write(response.body);
			toClient.close();
			client.close();
		} catch (IOException e) {
			System.out.println("Error writing response to client: " + e);
		}
	}

	// caching start point
	private static HttpResponse checkCache(HttpRequest request) {
		HttpResponse response = null;
		if (cache.get(request.getURI()) != null) {
			// response = cache.get(request.getURI());
		}
		return response;
	}

	private static void addResponseToCache(HttpRequest request,
			HttpResponse response) {
		System.out.println("Add response to cache");
		String fileName = "Cache/" + "file_" + System.currentTimeMillis();
		File fCache = new File(fileName);
		DataOutputStream fStream;
		try {
			fStream = new DataOutputStream(new FileOutputStream(fCache));
			fStream.writeBytes(response.toString());
			fStream.write(response.body);
			cache.put(request.getURI(), fileName);
			System.out.println("Add response to cache done!!");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public class SendingThreadHandler implements Runnable {

		@Override
		public void run() {
			SocketSend ss = new SocketSend();
			try {
				ss.sendResponse();
			} catch (IOException e) {

				e.printStackTrace();
			}

		}

	}

	public class ReceivingThreadHandler implements Runnable {
		SocketReceive sr = new SocketReceive();

		@Override
		public void run() {
			sr.receiveResponse();
		}
	}

	public class ProxyCacheThreadHandler implements Runnable {

		@Override
		public void run() {
			ProxyCache pc = new ProxyCache();
			pc.proxyCacheMethod();

		}

	}

	public void proxyCacheMethod() {
		System.out.println("inside proxy cache method");
		System.out.println("thread is " + Thread.currentThread());
		/**
		 * Main loop. Listen for incoming connections and spawn a new thread for
		 * handling them
		 */

		Socket client = null;

		while (true) {
			try {
				System.out.println("Inside proxyCacheMethod");
				client = socket.accept();
				start = System.currentTimeMillis();
				handle(client);

			} catch (IOException e) {
				System.out.println("Error reading request from client(main): "
						+ e);
				/*
				 * Definitely cannot continue processing this request, so skip
				 * to next iteration of while loop.
				 */
				continue;
			}
		}

	}

	public void threadHandler() {
		SendingThreadHandler sendThreadObj = new SendingThreadHandler();
		ReceivingThreadHandler receiveThreadObj = new ReceivingThreadHandler();
		ProxyCacheThreadHandler proxyCacheThreadObj = new ProxyCacheThreadHandler();
		Thread t1 = new Thread(sendThreadObj);
		Thread t2 = new Thread(receiveThreadObj);
		Thread t3 = new Thread(proxyCacheThreadObj);
		t1.start();
		t2.start();
		t3.start();
	}

	/**
	 * test code
	 * 
	 */

	public static Socket sendRequestToPeer(HttpRequest request, String address,
			Integer ip) {
		try {
			/* Open socket and write request to socket */
			Socket peer = new Socket(address, ip);
			DataOutputStream toPeer = new DataOutputStream(
					peer.getOutputStream());
			toPeer.writeBytes(request.toString());// we need only URL
			return peer;
		} catch (UnknownHostException e) {
			System.out.println("Unknown host: " + request.getHost());
			System.out.println(e);
			return null;
		} catch (IOException e) {
			System.out.println("Error writing to peer: " + e);
			return null;
		}

	}

	private static HttpResponse getResponseFromPeer(Socket peer) {
		System.out.println("  Connected to peer for fecthing response");
		try {

			DataInputStream fromPeer = new DataInputStream(
					peer.getInputStream());

			HttpResponse response = new HttpResponse(fromPeer);
			return response;
		} catch (IOException e) {
			System.out.println("Error writing response to client: " + e);
			return null;

		}
	}

	/**
	 * test code ends here
	 * 
	 * 
	 * 
	 * /** Read command line arguments and start proxy
	 */
	public static void main(String args[]) {
		File folder = new File("Cache/");
		if (!folder.exists()) {
			folder.mkdir();
		}
		try {
			myPort = Integer.parseInt(args[0]);
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Need port number");
			System.exit(-1);
		} catch (NumberFormatException e) {
			System.out.println("Please give port number as integer.");
			System.exit(-1);
		}
		System.out.println("myport" + myPort);
		init(myPort);
		ProxyCache proxObj = new ProxyCache();
		proxObj.threadHandler();
	}
}

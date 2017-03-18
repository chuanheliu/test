
package ChatServer;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * the server to create threads
 * @author Chuanhe Liu
 *
 */
public class Server {

	public static HashMap<String, String> user = new HashMap<String, String>();
	public static ArrayList<String[]> messageList = new ArrayList<String[]>();

	public Server(int port, String add){

		try {
			InetAddress addr = InetAddress.getByName(add);
			ServerSocket ss = new ServerSocket(port, 50, addr);
			ExecutorService pool = Executors.newFixedThreadPool(5);

			while(true){
				Socket s = ss.accept();
				pool.execute(new ConnectionThread(s));			
			}
		} 
		catch (IOException e){
			e.printStackTrace();
		}
		
		
	}

	public static void main(String[] args){

		new Server(Integer.parseInt(args[1]), args[0]);
	}
}

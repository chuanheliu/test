package ChatServer;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import Protocol.SimpleProtocol;


/**
 * the thread that the run method will run after a thread start
 * @author Chuanhe Liu
 *
 */
public class ConnectionThread implements Runnable{

	private Socket s;
	private String[] message = new String[3] ;
	private String user;


	ConnectionThread(Socket s){
		this.s = s;
	}

	/**
	 * when you sing up judge whether the username has exit.
	 * @param username
	 * @return true or fasle
	 */
	private boolean hasUsername(String s){
		Set<Map.Entry<String, String>> user = Server.user.entrySet();
		Iterator<Map.Entry<String, String>> it = user.iterator();
		while(it.hasNext()){
			Map.Entry<String, String> check = it.next();
			if(check.getKey().equals(message[1]))
				return true;
		}
		return false;
	}

	/**
	 * a method can get the current time.
	 * @return
	 */
	private String getTime(){
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		return dateFormat.format(new Date());
	}

	@Override
	public void run(){
		try{
			DataOutputStream out = new DataOutputStream(s.getOutputStream());
			SimpleProtocol sp = new SimpleProtocol();

			out.writeBytes(sp.createMessage("send-message", "Welcome to my server")+"\n");
			System.out.println("ip: "+s.getInetAddress().getHostAddress()+" has connected");

			BufferedReader bufr = new BufferedReader(new InputStreamReader(s.getInputStream()));

			String line = null;
			while((line = bufr.readLine()) != null){

				this.message = sp.decodeMessage(line);

				//when click the sign up button
				if(message[0].equals("sign-up")){
					if(message[1].length() <= 20 && message[1].length() >= 5 
							&& message[2].length() >= 8 && message[2].length() <= 32){
						if(hasUsername(message[1])){
							out.writeBytes(sp.createMessage("sign-up", "false", "username has exit!!")+"\n");
							s.close();
						}
						else{
							Server.user.put(message[1], message[2]);
							out.writeBytes(sp.createMessage("sign-up", "true", "Sign Up Successfully!!")+"\n");
						}
					}
					else{
						out.writeBytes(sp.createMessage("sign-up", "false", "The username should between 5 and 20 characters long,"
								+ "and the password should be at least 8 characters long with a miximum of 32")+"\n");
						s.close();
					}
				}

				//when click the sign in button
				if(message[0].equals("sign-in")){
					Set<Map.Entry<String, String>> user = Server.user.entrySet();
					Iterator<Map.Entry<String, String>> it = user.iterator();
					//define a flag that control the loop and judge whether sign up successfully.
					boolean flag = true;
					while(it.hasNext()){
						Map.Entry<String, String> check = it.next();
						if(check.getKey().equals(message[1]) && check.getValue().equals(message[2])){
							this.user = check.getKey();
							out.writeBytes(sp.createMessage("sign-in", "true", " welcome back "+message[1])+"\n");
							flag = false;
						}
					}
					if(flag)
						out.writeBytes(sp.createMessage("sign-in", "false", "password/username does not match")+"\n");
					if(!flag)
						break;
				}
			}

			//when sign in successfully, keep the operate in the get-message and send-message function
			while((line = bufr.readLine()) != null){
				this.message = sp.decodeMessage(line);

				//when want get message
				if(message[0].equals("get-message")){
					if(message[1].equals(-1+"")){
						if(!Server.messageList.isEmpty()){
							for(int i = 0; i < Server.messageList.size(); i++){
								out.writeBytes(sp.createMessage("get-message", i+"", Server.messageList.get(i)[1], Server.messageList.get(i)[2], Server.messageList.get(i)[3])+"\n");
							}
						}	
					}
					else if(message[0].equals("100")){
						for(int i = 100; i < Server.messageList.size(); i++){
							out.writeBytes(sp.createMessage("get-message", i+"", Server.messageList.get(i)[1], Server.messageList.get(i)[2], Server.messageList.get(i)[3])+"\n");
						}
					}
					else{
						for(int i = Integer.parseInt(message[1]); i < Server.messageList.size(); i++){
							out.writeBytes(sp.createMessage("get-message", i+"", Server.messageList.get(i)[1], Server.messageList.get(i)[2], Server.messageList.get(i)[3])+"\n");
						}
					}

				}

				//when want to send message
				else if(message[0].equals("send-message")){

					String[] sendMessage = {Server.messageList.size()+"", user, getTime(), message[1]};
					Server.messageList.add(sendMessage);
					if(Server.messageList.contains(sendMessage))
						out.writeBytes(sp.createMessage("send-message", "true", Server.messageList.size()+"")+"\n");
					else
						out.writeBytes(sp.createMessage("send-message", "false", "send message failer")+"\n");

				}

			}
		}
		catch(IOException e){
			System.out.println(e.getStackTrace()+e.getMessage());
		}

	}

}

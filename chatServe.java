import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

public class chatServe{
	private static ServerSocket server = null;
	private static Socket socket = null;
	private static DataInputStream in = null;
	private static DataOutputStream out = null;
	private static String configPath = "";
	private static String errorLogPath = "";
	private static String currentRoomPath = "";
	private static String roomLogPath = "";

	public static void main(String[] args){
		try{
			if(!(new File("/etc/chat.conf")).exists()){
				System.err.println("\t> error: chat server configuration file not found at path \'/etc/chat.conf\'");
				System.exit(1);
			}
			FileReader reader = new FileReader("/etc/chat.conf");
			String line = "";
			for(int i = 0; i != -1 || (char)i != '\n'; i = reader.read()){
				line += (char)i;
			}
			Scanner lineI = new Scanner(line);
			if(lineI.next().equals("errors")){
				errorLogPath = line;
			}else{
				configPath = line;
			}
			lineI = null;
			for(int i = 0; i != -1 || (char)i != '\n'; i = reader.read()){
				line += (char)i;
			}
			lineI = new Scanner(line);
			if(lineI.next().equals("errors")){
				errorLogPath = line;
			}else{
				configPath = line;
			}
			reader.close();
		}catch(IOException e){
			System.err.println("\t> error: error caught while getting server config files from \'/etc/chat.conf\'");
			System.exit(1);
		}
		try{
			server = new ServerSocket(Integer.parseInt(args[0]));
			socket = server.accept();
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			promptRooms();
			readLoop();
		}catch(IOException e){
			System.err.println("\t> error: error caught while initializing chat server");
			errorLog("error: could not initialize server");
		}
	}

	public static void errorLog(String message){
		try{
			FileWriter scrib = new FileWriter(new File(errorLogPath));
			scrib.write(message);
			scrib.close();
		}catch(IOException e){
			System.err.println("\t> error: error caught while attempting to log message:");
			System.err.println("\t> " + message);
			System.err.println("\t> to error log at path " + errorLogPath);
		}
	}

	public static void promptRooms(){
		try{
			out.writeUTF("if you have a room to join in mind, please write the name of the room now.\notherwise, write LIST to recieve a list of rooms");
			while(true){
				String res = in.readUTF();
				if(res.equals("LIST")){
					out.writeUTF(getRoomNames());
				}else{
					currentRoomPath =  getRoomPath(res);
				}
			}
		}catch(IOException e){
			errorLog("error: error caught while prompting rooms");
		}
	}

	public static String getRoomNames(){
		String ret = "";
		String line = "";
		Scanner configI = new Scanner(configPath);
		while(configI.hasNextLine()){
			line = configI.nextLine();
			Scanner lineI = new Scanner(line);
			ret += lineI.next() + "\n";
		}
		ret = ret.substring(0, ret.length() - 1);
		return ret;
	}

	public static String getRoomPath(String roomName){
		String ret = "";
		Scanner configI = new Scanner(configPath);
		while(configI.hasNextLine()){
			String line = configI.nextLine();
			ret += line.indexOf(configI.next() + 1) + "\n";
		}
		ret = ret.substring(0, ret.length() - 1);
		return ret;
	}

	public static void readLoop(){
		try{
			FileWriter scrib = new FileWriter(roomLogPath, true);
			String line = "";
			while(true){
				line = in.readUTF();
				scrib.write(line);
				out.writeUTF("ack");
			}
		}catch(IOException e){
			errorLog("error: could not commit message to log " + roomLogPath + " while in read loop");
		}
	}
}

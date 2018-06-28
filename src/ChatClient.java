import java.io.*;
import java.net.Socket;

public class ChatClient {

	private Socket socket;
	private String serverIP = "localhost";
	private BufferedReader inputFromKBD;
	private String name = "";

	public ChatClient() {
	}

	public static void main(String[] args) {
		new ChatClient().start();
	}

	private void start() {
		try {
			socket = new Socket(serverIP, 9100);
			System.out.println("서버와 연결되었습니다. 대화명을 입력하십시오.");
			inputFromKBD = new BufferedReader(new InputStreamReader(System.in));
			name = inputFromKBD.readLine();
			System.out.println("명령어 보기는 help를 입력하세요.");
			
			new ClientReceiver(socket).start();
			clientSender();
		} catch (IOException e) {
		}
	}

	private void clientSender() {
		PrintWriter output;
		String outputLine = "";

		try {
			output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			output.println(name);
			output.flush();

			while ((outputLine = inputFromKBD.readLine()) != null) {
				output.println(outputLine);
				output.flush();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ClientReceiver extends Thread {
		Socket socket;
		BufferedReader inputFromServer = null;

		public ClientReceiver(Socket socket) {
			this.socket = socket;

			try {
				inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				// inputLine = inputFromServer.readLine();
				// System.out.println(inputLine);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
			}
		}

		public void run() {
			String inputLine;

			try {
				while ((inputLine = inputFromServer.readLine()) != null) {
					System.out.println(inputLine);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (inputFromServer != null)
						inputFromServer.close();
					if (socket != null)
						socket.close();
				} catch (IOException e) {
				}
			}
		}// run
	}
}
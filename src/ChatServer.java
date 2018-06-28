import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

	private ConcurrentHashMap<String, PrintWriter> client_list; // 클라이언트 관리
	private ConcurrentHashMap<String, ArrayList<String>> group_list; // 그룹 관리
	private ArrayList<String[]> user_list;
	private ArrayList<String> logon_list;
	private ServerSocket serverSocket;

	public ChatServer() {
		client_list = new ConcurrentHashMap<String, PrintWriter>();
		group_list = new ConcurrentHashMap<String, ArrayList<String>>();
		user_list = new ArrayList<String[]>();
		logon_list = new ArrayList<String>();
	} // default constructor

	public static void main(String[] args) {
		new ChatServer().start();
	} // main

	private void start() {
		Socket socket = null;

		try {
			serverSocket = new ServerSocket(9100);
			System.out.println("서버가 시작되었습니다.");
			System.out.println("Server IP Address: " + InetAddress.getLocalHost().getHostAddress() + "port: "
					+ serverSocket.getLocalPort());
			while (true) {
				socket = serverSocket.accept();
				// serverSocket의 연결요청이 온 클라이언트의 소켓을 생성
				// 클라이언트와의 소켓은 1:1 연결
				System.out.println("\n=====클라이언트가 연결되었습니다.======\n");
				System.out.println("Client IP Address: " + socket.getInetAddress().getHostAddress() + " prot: "
						+ socket.getPort());

				new ChatThread(socket).start();

			} // while
		} catch (IOException e) {
			e.printStackTrace();
		}
	} // start

	private class ChatThread extends Thread {
		private Socket threadSocket;
		private BufferedReader input;
		private PrintWriter output;
		private String name = "";

		public ChatThread(Socket socket) {
			this.threadSocket = socket;

			try {
				input = new BufferedReader(new InputStreamReader(threadSocket.getInputStream(), "UTF-8"));
				output = new PrintWriter(new OutputStreamWriter(threadSocket.getOutputStream(), "UTF-8"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			String inputLine = null;

			try {
				threadSocket.getPort();
				name = input.readLine();
				client_list.put(name, output);

				System.out.println("\n[" + name + "]" + threadSocket.getInetAddress() + " : " + threadSocket.getPort()
						+ "님이 대화방에 접속하셨습니다.");

				sendToAll("[" + name + "] 님이 대화방에 접속하였습니다.");

				System.out.println("\n 현재 " + client_list.size() + "명이 연결 중 입니다...\n");

				while ((inputLine = input.readLine()) != null) {
					String fromToArr[] = inputLine.split(" ", 10);

					System.out.println("[" + name + "] :: " + inputLine);

					if (fromToArr[0].equalsIgnoreCase("create")) { // 계정생성
						if (fromToArr.length == 3)
							Create(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("delete")) { // 계정삭제
						if (fromToArr.length == 3)
							Delete(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("login")) { // 로그인
						if (fromToArr.length == 3)
							Login(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("logout")) { // 로그아웃
						if (fromToArr.length == 2)
							Logout(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("a")) { // 전체채팅전송
						if (fromToArr.length == 2)
							talkToAll(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("g")) { // 그룹채팅전송
						if (fromToArr.length == 3)
							talkToGroup(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("w")) { // 귓속말전송
						if (fromToArr.length == 3)
							sendTo(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("g_create")) { // 그룹생성
						if (fromToArr.length == 2)
							Group_create(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("g_delete")) { // 그룹삭제
						if (fromToArr.length == 2)
							Group_delete(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("g_join")) { // 그룹참여
						if (fromToArr.length == 2)
							Group_join(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("g_quit")) { // 그룹나가기
						if (fromToArr.length == 2)
							Group_quit(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("user")) { // 접속자조회
						if (fromToArr.length == 1)
							showUser(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("group")) { // 그룹조회
						if (fromToArr.length == 1)
							showGroup(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("member")) { // 그룹멤버조회
						if (fromToArr.length == 2)
							showMember(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("help")) { // 명령어조회
						if (fromToArr.length == 1)
							showHelp(inputLine);
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("clear")) { // 채팅지우기
						if (fromToArr.length == 1)
							Clear();
						else
							sendToBack("잘못된 명령어입니다.");
					} else if (fromToArr[0].equalsIgnoreCase("quit")) { // 채팅나가기
						if (fromToArr.length == 1) {
							client_list.remove(name, output);
							break;
						} else
							sendToBack("잘못된 명령어입니다.");
					} else { // 메시지 형식 오류
						sendToBack("잘못된 명령어입니다.");
					}
				}

			} catch (Exception e) {
			} finally {

				client_list.remove(name);

				sendToAll("[" + name + "]  님이 대화방에 나가셨습니다.");

				System.out.println("[" + name + "]" + threadSocket.getInetAddress() + " : " + threadSocket.getPort()
						+ "님이 대화방에 나갔습니다.");

				System.out.println("현재 " + client_list.size() + "명이 접속 중 입니다.");

				try {
					if (threadSocket != null)
						threadSocket.close();
				} catch (Exception e) {
				}
			}
		}

		private void sendToAll(String inputLine) { // 전채알림 프로토콜
			Iterator<String> iterator = client_list.keySet().iterator();

			while (iterator.hasNext()) {
				try {
					PrintWriter pw = client_list.get(iterator.next());
					pw.println(inputLine);
					pw.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void Create(String inputLine) { // 계정 생성 프로토콜
			String fromToArr[] = inputLine.split(" ", 3);
			String idpw[] = new String[2];

			idpw[0] = fromToArr[1];
			idpw[1] = fromToArr[2];

			Iterator<String[]> iterator = user_list.iterator();

			while (iterator.hasNext()) {
				String[] temp = iterator.next();
				if (temp[0].equals(idpw[0])) {
					sendToBack("아이디 중복");
					sendToBack("계정 생성 실패");
					return;
				}
			}
			user_list.add(idpw);
			sendToBack("계정 생성 성공");
		}

		private void Delete(String inputLine) { // 계정 삭제 프로토콜
			String fromToArr[] = inputLine.split(" ", 3);
			String idpw[] = new String[2];
			int count = 0;

			idpw[0] = fromToArr[1];
			idpw[1] = fromToArr[2];

			Iterator<String[]> iterator = user_list.iterator();

			while (iterator.hasNext()) {
				String[] temp = iterator.next();
				if (temp[0].equals(idpw[0]) && temp[1].equals(idpw[1])) {
					user_list.remove(count);
					sendToBack("계정 삭제 성공");
					return;
				} else if (temp[0].equals(idpw[0]) && !temp[1].equals(idpw[1])) {
					sendToBack("비밀번호가 틀립니다");
					sendToBack("계정 삭제 실패");
					return;
				}
				count++;
			}
			sendToBack("아이디가 없습니다");
			sendToBack("계정 삭제 실패");
		}

		private void Login(String inputLine) { // 로그인 프로토콜
			String fromToArr[] = inputLine.split(" ", 3);
			String idpw[] = new String[2];

			idpw[0] = fromToArr[1];
			idpw[1] = fromToArr[2];

			Iterator<String[]> iterator = user_list.iterator();

			while (iterator.hasNext()) {
				String[] temp = iterator.next();
				if (temp[0].equals(idpw[0]) && temp[1].equals(idpw[1])) {
					logon_list.add(idpw[0]);
					sendToBack("로그인 성공");
					return;
				} else if (temp[0].equals(idpw[0]) && !temp[1].equals(idpw[1])) {
					sendToBack("비밀번호가 틀립니다");
					sendToBack("로그인 실패");
					return;
				}
			}
			sendToBack("아이디가 없습니다");
			sendToBack("로그인 실패");
		}

		private void Logout(String inputLine) { // 로그아웃 프로토콜
			String fromToArr[] = inputLine.split(" ", 2);
			int count = 0;

			Iterator<String> iterator = logon_list.iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					logon_list.remove(count);
					sendToBack("로그아웃 성공");
					return;
				}
				count++;
			}
			sendToBack("로그온 되어 있지 않습니다.");
		}

		private void talkToAll(String inputLine) { // 자신을 제외한 전채채팅 프로토콜
			String fromToArr[] = inputLine.split(" ", 2);
			Iterator<String> iterator = client_list.keySet().iterator();

			while (iterator.hasNext()) {
				try {
					String temp = iterator.next();
					if (!temp.equals(name)) {
						PrintWriter pw = client_list.get(temp);
						pw.println(fromToArr[1]);
						pw.flush();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void talkToGroup(String inputLine) { // 자신이 속한 그룹채팅 프로토콜
			String fromToArr[] = inputLine.split(" ", 3);
			Iterator<String> iterator = group_list.keySet().iterator();
			ArrayList<String> list = new ArrayList<String>();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					list = group_list.get(temp);
				}
			}
			
			Iterator<String> iterator2 = list.iterator();
			
			while (iterator2.hasNext()) {
				try {
					String temp = iterator2.next();
					if (!temp.equals(name)) {
						PrintWriter pw = client_list.get(temp);
						pw.println(fromToArr[2]);
						pw.flush();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void sendTo(String inputLine) { // 귓속말 프로토콜
			String fromToArr[] = inputLine.split(" ", 3);
			if (fromToArr[2].length() > 0) {
				Object obj = client_list.get(fromToArr[1]);

				if (obj != null) {
					PrintWriter pw = (PrintWriter) obj;
					pw.println("[from " + name + "] " + fromToArr[2]);
					pw.flush();
				} else {
					sendToBack("아이디가 틀립니다.");
				}
			}

		}

		private void Group_create(String inputLine) { // 그룹 생성 프로토콜
			String fromToArr[] = inputLine.split(" ", 2);
			ArrayList<String> list = new ArrayList<String>();
			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					sendToBack("그룹이름 중복");
					sendToBack("그룹 생성 실패");
					return;
				}
			}
			group_list.put(fromToArr[1], list);
			sendToBack("그룹 생성 성공");
		}

		private void Group_delete(String inputLine) { // 그룹 삭제 프로토콜
			String fromToArr[] = inputLine.split(" ", 2);

			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					group_list.remove(fromToArr[1]);
					sendToBack("그룹 삭제 성공");
					return;
				}
			}
			sendToBack("일치하는 그룹이 없습니다.");
			sendToBack("그룹 삭제 실패");
		}

		private void Group_join(String inputLine) { // 그룹 참여 프로토콜
			String fromToArr[] = inputLine.split(" ", 2);
			ArrayList<String> list = new ArrayList<String>();
			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					list = group_list.get(fromToArr[1]);
					list.add(name);
					group_list.put(fromToArr[1], list);
					sendToBack("그룹 참여 성공");
					return;
				}
			}
			sendToBack("일치하는 그룹이 없습니다.");
			sendToBack("그룹 참여 실패");
		}

		private void Group_quit(String inputLine) { // 그룹 나가기 프로토콜
			String fromToArr[] = inputLine.split(" ", 2);
			ArrayList<String> list = new ArrayList<String>();
			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					list = group_list.get(fromToArr[1]);
					list.remove(name);
					group_list.put(fromToArr[1], list);
					sendToBack("그룹 탈퇴 성공");
					return;
				}
			}
			sendToBack("가입된 그룹이 없습니다.");
		}

		private void showUser(String inputLine) { // 접속자 조회 프로토콜
			Iterator<String> iterator = client_list.keySet().iterator();

			while (iterator.hasNext()) {
				sendToBack(iterator.next());
			}
		}

		private void showGroup(String inputLine) { // 그룹 조회 프로토콜
			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				sendToBack(iterator.next());
			}
		}

		private void showMember(String inputLine) { // 그룹원 조회 프로토콜
			String fromToArr[] = inputLine.split(" ", 2);
			Iterator<String> iterator = group_list.keySet().iterator();
			ArrayList<String> list = new ArrayList<String>();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					list = group_list.get(temp);
				}
			}

			Iterator<String> iterator2 = list.iterator();

			while (iterator2.hasNext()) {
				sendToBack(iterator2.next());
				return;
			}
			sendToBack("일치하는 그룹이 없거나 그룹에 멤버가 없습니다.");
		}

		private void showHelp(String inputLine) { // 명령어 조회 프로토콜
			String help[] = new String[17];
			help[0] = "create 아이디 비밀번호 (계정생성)";
			help[1] = "delete 아이디 비밀번호 (계정삭제)";
			help[2] = "login 아이디 비밀번호 (로그인)";
			help[3] = "logout 아이디 (로그아웃)";
			help[4] = "a 내용 (전채채팅)";
			help[5] = "g 그룹이름 내용 (그룹채팅)";
			help[6] = "w 아이디 내용 (귓속말)";
			help[7] = "g_create 그룹이름 (그룹생성)";
			help[8] = "g_delete 그룹이름 (그룹삭제)";
			help[9] = "g_join 그룹이름 (그룹참여)";
			help[10] = "g_quit 그룹이름 (그룹나가기)";
			help[11] = "user (접속자 조회)";
			help[12] = "group (그룹 조회)";
			help[13] = "member 그룹이름 (그룹 조회)";
			help[14] = "help (명령어 조회)";
			help[15] = "clear (화면새로고침)";
			help[16] = "quit (채팅방 나가기)";

			for (int i = 0; i < help.length; i++)
				sendToBack(help[i]);
		}

		private void Clear() { // 화면 지우기 프로토콜
			for (int i = 0; i < 50; i++)
				sendToBack("\n");
		}

		private void sendToBack(String inputline) { // 리콜메시지 프로토콜
			Object obj = client_list.get(name);

			if (obj != null) {
				PrintWriter pw = (PrintWriter) obj;
				pw.println(inputline);
				pw.flush();
			}
		}

	}
}
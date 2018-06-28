import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {

	private ConcurrentHashMap<String, PrintWriter> client_list; // Ŭ���̾�Ʈ ����
	private ConcurrentHashMap<String, ArrayList<String>> group_list; // �׷� ����
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
			System.out.println("������ ���۵Ǿ����ϴ�.");
			System.out.println("Server IP Address: " + InetAddress.getLocalHost().getHostAddress() + "port: "
					+ serverSocket.getLocalPort());
			while (true) {
				socket = serverSocket.accept();
				// serverSocket�� �����û�� �� Ŭ���̾�Ʈ�� ������ ����
				// Ŭ���̾�Ʈ���� ������ 1:1 ����
				System.out.println("\n=====Ŭ���̾�Ʈ�� ����Ǿ����ϴ�.======\n");
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
						+ "���� ��ȭ�濡 �����ϼ̽��ϴ�.");

				sendToAll("[" + name + "] ���� ��ȭ�濡 �����Ͽ����ϴ�.");

				System.out.println("\n ���� " + client_list.size() + "���� ���� �� �Դϴ�...\n");

				while ((inputLine = input.readLine()) != null) {
					String fromToArr[] = inputLine.split(" ", 10);

					System.out.println("[" + name + "] :: " + inputLine);

					if (fromToArr[0].equalsIgnoreCase("create")) { // ��������
						if (fromToArr.length == 3)
							Create(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("delete")) { // ��������
						if (fromToArr.length == 3)
							Delete(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("login")) { // �α���
						if (fromToArr.length == 3)
							Login(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("logout")) { // �α׾ƿ�
						if (fromToArr.length == 2)
							Logout(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("a")) { // ��üä������
						if (fromToArr.length == 2)
							talkToAll(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("g")) { // �׷�ä������
						if (fromToArr.length == 3)
							talkToGroup(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("w")) { // �ӼӸ�����
						if (fromToArr.length == 3)
							sendTo(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("g_create")) { // �׷����
						if (fromToArr.length == 2)
							Group_create(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("g_delete")) { // �׷����
						if (fromToArr.length == 2)
							Group_delete(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("g_join")) { // �׷�����
						if (fromToArr.length == 2)
							Group_join(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("g_quit")) { // �׷쳪����
						if (fromToArr.length == 2)
							Group_quit(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("user")) { // ��������ȸ
						if (fromToArr.length == 1)
							showUser(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("group")) { // �׷���ȸ
						if (fromToArr.length == 1)
							showGroup(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("member")) { // �׷�����ȸ
						if (fromToArr.length == 2)
							showMember(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("help")) { // ��ɾ���ȸ
						if (fromToArr.length == 1)
							showHelp(inputLine);
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("clear")) { // ä�������
						if (fromToArr.length == 1)
							Clear();
						else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else if (fromToArr[0].equalsIgnoreCase("quit")) { // ä�ó�����
						if (fromToArr.length == 1) {
							client_list.remove(name, output);
							break;
						} else
							sendToBack("�߸��� ��ɾ��Դϴ�.");
					} else { // �޽��� ���� ����
						sendToBack("�߸��� ��ɾ��Դϴ�.");
					}
				}

			} catch (Exception e) {
			} finally {

				client_list.remove(name);

				sendToAll("[" + name + "]  ���� ��ȭ�濡 �����̽��ϴ�.");

				System.out.println("[" + name + "]" + threadSocket.getInetAddress() + " : " + threadSocket.getPort()
						+ "���� ��ȭ�濡 �������ϴ�.");

				System.out.println("���� " + client_list.size() + "���� ���� �� �Դϴ�.");

				try {
					if (threadSocket != null)
						threadSocket.close();
				} catch (Exception e) {
				}
			}
		}

		private void sendToAll(String inputLine) { // ��ä�˸� ��������
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

		private void Create(String inputLine) { // ���� ���� ��������
			String fromToArr[] = inputLine.split(" ", 3);
			String idpw[] = new String[2];

			idpw[0] = fromToArr[1];
			idpw[1] = fromToArr[2];

			Iterator<String[]> iterator = user_list.iterator();

			while (iterator.hasNext()) {
				String[] temp = iterator.next();
				if (temp[0].equals(idpw[0])) {
					sendToBack("���̵� �ߺ�");
					sendToBack("���� ���� ����");
					return;
				}
			}
			user_list.add(idpw);
			sendToBack("���� ���� ����");
		}

		private void Delete(String inputLine) { // ���� ���� ��������
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
					sendToBack("���� ���� ����");
					return;
				} else if (temp[0].equals(idpw[0]) && !temp[1].equals(idpw[1])) {
					sendToBack("��й�ȣ�� Ʋ���ϴ�");
					sendToBack("���� ���� ����");
					return;
				}
				count++;
			}
			sendToBack("���̵� �����ϴ�");
			sendToBack("���� ���� ����");
		}

		private void Login(String inputLine) { // �α��� ��������
			String fromToArr[] = inputLine.split(" ", 3);
			String idpw[] = new String[2];

			idpw[0] = fromToArr[1];
			idpw[1] = fromToArr[2];

			Iterator<String[]> iterator = user_list.iterator();

			while (iterator.hasNext()) {
				String[] temp = iterator.next();
				if (temp[0].equals(idpw[0]) && temp[1].equals(idpw[1])) {
					logon_list.add(idpw[0]);
					sendToBack("�α��� ����");
					return;
				} else if (temp[0].equals(idpw[0]) && !temp[1].equals(idpw[1])) {
					sendToBack("��й�ȣ�� Ʋ���ϴ�");
					sendToBack("�α��� ����");
					return;
				}
			}
			sendToBack("���̵� �����ϴ�");
			sendToBack("�α��� ����");
		}

		private void Logout(String inputLine) { // �α׾ƿ� ��������
			String fromToArr[] = inputLine.split(" ", 2);
			int count = 0;

			Iterator<String> iterator = logon_list.iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					logon_list.remove(count);
					sendToBack("�α׾ƿ� ����");
					return;
				}
				count++;
			}
			sendToBack("�α׿� �Ǿ� ���� �ʽ��ϴ�.");
		}

		private void talkToAll(String inputLine) { // �ڽ��� ������ ��ää�� ��������
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

		private void talkToGroup(String inputLine) { // �ڽ��� ���� �׷�ä�� ��������
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

		private void sendTo(String inputLine) { // �ӼӸ� ��������
			String fromToArr[] = inputLine.split(" ", 3);
			if (fromToArr[2].length() > 0) {
				Object obj = client_list.get(fromToArr[1]);

				if (obj != null) {
					PrintWriter pw = (PrintWriter) obj;
					pw.println("[from " + name + "] " + fromToArr[2]);
					pw.flush();
				} else {
					sendToBack("���̵� Ʋ���ϴ�.");
				}
			}

		}

		private void Group_create(String inputLine) { // �׷� ���� ��������
			String fromToArr[] = inputLine.split(" ", 2);
			ArrayList<String> list = new ArrayList<String>();
			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					sendToBack("�׷��̸� �ߺ�");
					sendToBack("�׷� ���� ����");
					return;
				}
			}
			group_list.put(fromToArr[1], list);
			sendToBack("�׷� ���� ����");
		}

		private void Group_delete(String inputLine) { // �׷� ���� ��������
			String fromToArr[] = inputLine.split(" ", 2);

			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					group_list.remove(fromToArr[1]);
					sendToBack("�׷� ���� ����");
					return;
				}
			}
			sendToBack("��ġ�ϴ� �׷��� �����ϴ�.");
			sendToBack("�׷� ���� ����");
		}

		private void Group_join(String inputLine) { // �׷� ���� ��������
			String fromToArr[] = inputLine.split(" ", 2);
			ArrayList<String> list = new ArrayList<String>();
			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					list = group_list.get(fromToArr[1]);
					list.add(name);
					group_list.put(fromToArr[1], list);
					sendToBack("�׷� ���� ����");
					return;
				}
			}
			sendToBack("��ġ�ϴ� �׷��� �����ϴ�.");
			sendToBack("�׷� ���� ����");
		}

		private void Group_quit(String inputLine) { // �׷� ������ ��������
			String fromToArr[] = inputLine.split(" ", 2);
			ArrayList<String> list = new ArrayList<String>();
			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				String temp = iterator.next();
				if (temp.equals(fromToArr[1])) {
					list = group_list.get(fromToArr[1]);
					list.remove(name);
					group_list.put(fromToArr[1], list);
					sendToBack("�׷� Ż�� ����");
					return;
				}
			}
			sendToBack("���Ե� �׷��� �����ϴ�.");
		}

		private void showUser(String inputLine) { // ������ ��ȸ ��������
			Iterator<String> iterator = client_list.keySet().iterator();

			while (iterator.hasNext()) {
				sendToBack(iterator.next());
			}
		}

		private void showGroup(String inputLine) { // �׷� ��ȸ ��������
			Iterator<String> iterator = group_list.keySet().iterator();

			while (iterator.hasNext()) {
				sendToBack(iterator.next());
			}
		}

		private void showMember(String inputLine) { // �׷�� ��ȸ ��������
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
			sendToBack("��ġ�ϴ� �׷��� ���ų� �׷쿡 ����� �����ϴ�.");
		}

		private void showHelp(String inputLine) { // ��ɾ� ��ȸ ��������
			String help[] = new String[17];
			help[0] = "create ���̵� ��й�ȣ (��������)";
			help[1] = "delete ���̵� ��й�ȣ (��������)";
			help[2] = "login ���̵� ��й�ȣ (�α���)";
			help[3] = "logout ���̵� (�α׾ƿ�)";
			help[4] = "a ���� (��ää��)";
			help[5] = "g �׷��̸� ���� (�׷�ä��)";
			help[6] = "w ���̵� ���� (�ӼӸ�)";
			help[7] = "g_create �׷��̸� (�׷����)";
			help[8] = "g_delete �׷��̸� (�׷����)";
			help[9] = "g_join �׷��̸� (�׷�����)";
			help[10] = "g_quit �׷��̸� (�׷쳪����)";
			help[11] = "user (������ ��ȸ)";
			help[12] = "group (�׷� ��ȸ)";
			help[13] = "member �׷��̸� (�׷� ��ȸ)";
			help[14] = "help (��ɾ� ��ȸ)";
			help[15] = "clear (ȭ����ΰ�ħ)";
			help[16] = "quit (ä�ù� ������)";

			for (int i = 0; i < help.length; i++)
				sendToBack(help[i]);
		}

		private void Clear() { // ȭ�� ����� ��������
			for (int i = 0; i < 50; i++)
				sendToBack("\n");
		}

		private void sendToBack(String inputline) { // ���ݸ޽��� ��������
			Object obj = client_list.get(name);

			if (obj != null) {
				PrintWriter pw = (PrintWriter) obj;
				pw.println(inputline);
				pw.flush();
			}
		}

	}
}
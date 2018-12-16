package pl.mgr.hs.chat;

import com.blogspot.debukkitsblog.net.Client;
import com.blogspot.debukkitsblog.net.Datapackage;
import com.google.gson.Gson;

import java.net.Socket;
import java.util.Scanner;

import static pl.mgr.hs.chat.Constants.messageId.*;

/** Created by dominik on 23.11.18. */
public class ChatClient extends Client {
  private static final int PAYLOAD = 1;
  private static final int IP_ADDRESS = 0;
  private static final int PORT = 1;
  private static final String EXIT_COMMNAND = ":exit";

  private boolean initialMessageIsSent;
  private String nick;
  private Gson gson = new Gson();
  private Scanner scanner = new Scanner(System.in, "UTF-8");

  public static ChatClient createClient(String ipPort, String nick) {
    String[] splittedIpPort = ipPort.split(":");

    if (splittedIpPort.length != 2) {
      throw new IllegalArgumentException("The address should have ip:port format!");
    }
    return new ChatClient(splittedIpPort[IP_ADDRESS], Integer.parseInt(splittedIpPort[PORT]), nick);
  }

  @Override
  public void onConnectionGood() {
    if (!initialMessageIsSent) {
      sendInitialMessage();
      initialMessageIsSent = true;
    }
  }

  @Override
  public void onConnectionProblem() {
    System.out.println("Cannot communicate with server...");
    initialMessageIsSent = false;
  }

  private ChatClient(String hostname, int port, String nick) {
    super(hostname, port);
    setMuted(true);
    this.nick = nick;

    registerMethod(SERVER_MESSAGE, this::handleMessage);
    registerMethod(CLIENT_MESSAGE, this::handleMessage);

    start();
    chat();
  }

  private void handleMessage(Datapackage pack, Socket socket) {
    Message message = gson.fromJson((String) pack.get(PAYLOAD), Message.class);
    System.out.println(String.format("%s: %s", message.getId(), message.getMessage()));
  }

  private void sendInitialMessage() {
    sendMessage(CLIENT_INITIAL_MESSAGE, gson.toJson(new Message(nick, null)));
  }

  private void chat() {
    while (!stopped) {
      String text = scanner.nextLine();
      if (EXIT_COMMNAND.equals(text)) {
        stop();
        break;
      }
      sendMessage(CLIENT_MESSAGE, gson.toJson(new Message(nick, text)));
    }
  }
}

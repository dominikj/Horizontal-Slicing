package pl.mgr.hs.chat;

/** Created by dominik on 16.11.18. */
public class ChatApp {

  private static final int MODE = 0;
  private static final String SERVER_MODE = "server";
  private static final int PORT = 1;
  private static final int DESC = 2;
  private static final String CLIENT_MODE = "client";
  private static final int IP_PORT = 1;
  private static final int NICK = 2;

  public static void main(String[] args) {

    if (args.length < 3) {
      showHelp();
      return;
    }

    switch (args[MODE]) {
      case SERVER_MODE:
        {
          ChatServer.createServer(args[PORT], args[DESC]);
          break;
        }
      case CLIENT_MODE:
        {
          ChatClient.createClient(args[IP_PORT], args[NICK]);
          break;
        }
      default:
        {
          showHelp();
        }
    }
  }

  private static void showHelp() {
    System.out.println(
        "chatApp [" + SERVER_MODE + "|" + CLIENT_MODE + "] [port|ip:port] [description|nick]");
  }
}

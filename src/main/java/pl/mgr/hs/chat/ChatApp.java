package pl.mgr.hs.chat;

import java.util.Scanner;

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

    if (args.length < 2) {
      showHelp();
      return;
    }

    switch (args[MODE]) {
      case SERVER_MODE:
        {
          if (args.length < 3) {
            showHelp();
            break;
          }
          ChatServer.createServer(args[PORT], args[DESC]);
          break;
        }
      case CLIENT_MODE:
        {
          String nick;
          if (args.length == 2) {
            nick = getNick();
          } else {
            nick = args[NICK];
          }
          ChatClient.createClient(args[IP_PORT], nick);
          break;
        }
      default:
        {
          showHelp();
        }
    }

    // For faster closing application send SIGINT signal - ugly
    System.exit(0);
  }

  private static void showHelp() {
    System.out.println(
        "chatApp [" + SERVER_MODE + "|" + CLIENT_MODE + "] [port|ip:port] [description|nick] ");
  }

  private static String getNick() {
    System.out.println("Nick:");
    Scanner scanner = new Scanner(System.in);
    return scanner.nextLine();
  }
}

package pl.mgr.hs.client.cli;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;

/** Created by dominik on 16.11.18. */
public class ClientCli {

  private static final String LIST_OPTION = "list";
  private static final String HOST_ADDRESS_FORMAT = "^\\d+.\\d+.\\d+.\\d+:\\d+$";
  private static final int COMMAND_INDEX = 0;
  private static final String CONNECT_OPTION = "connect";
  private static final String DISCONNECT_OPTION = "disconnect";
  private static final String SERVICE_OPTION = "service";
  private static final String WHITESPACES = "\\s+";
  private static final String EXIT_OPTION = "exit";
  private static final int ADDRESS_ARG = 0;
  private static final int SLICE_NAME_PARAM = 1;
  private static final String COMMAND_PROMPT = ">";
  private static Scanner scanner = new Scanner(System.in);

  public static void main(String[] args) {

    MenuService menu = new MenuService();

    if (args.length != 1 || !addressSyntaxIsCorrect(args[ADDRESS_ARG])) {
      menu.showCommand();
      return;
    }
    // FIXME
    BasicConfigurator.configure(new NullAppender());

    SliceService sliceService = new SliceService();
    String[] input;
    String managerAddress = args[ADDRESS_ARG];
    String connectedSlice = "";

    menu.showUsage();

    while (true) {
      input = getInput();

      if (input.length > 2) {
        menu.showUsage();
        continue;
      }

      switch (input[COMMAND_INDEX]) {
        case LIST_OPTION:
          {
            menu.showList(sliceService.getAvailableSlicesForHost(getHostName(), managerAddress));
            break;
          }
        case CONNECT_OPTION:
          {
            if (input.length != 2) {
              menu.showUsage();
              break;
            }
            sliceService.joinToSlice(input[SLICE_NAME_PARAM], getHostName(), managerAddress);
            connectedSlice = input[SLICE_NAME_PARAM];
            break;
          }
        case DISCONNECT_OPTION:
          {
            sliceService.disconnectFromSlice();
            connectedSlice = "";
            break;
          }
        case SERVICE_OPTION:
          {
            sliceService.attachToSliceApp(connectedSlice, getHostName(), managerAddress);
            break;
          }
        case EXIT_OPTION:
          {
            if (!"".equals(connectedSlice)) {
              sliceService.disconnectFromSlice();
            }
            return;
          }
        default:
          {
            menu.showUsage();
          }
      }
    }
  }

  private static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static boolean addressSyntaxIsCorrect(String address) {

    String hostUrl = address.replace("localhost", "127.0.0.1");
    return hostUrl.matches(HOST_ADDRESS_FORMAT);
  }

  private static String[] getInput() {
    System.out.print(COMMAND_PROMPT);
    return scanner.nextLine().split(WHITESPACES);
  }
}

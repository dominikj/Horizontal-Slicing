package pl.mgr.hs.client.cli;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import pl.mgr.hs.client.cli.rest.MenuService;
import pl.mgr.hs.client.cli.rest.SliceService;

import java.net.InetAddress;
import java.net.UnknownHostException;

/** Created by dominik on 16.11.18. */
public class ClientCli {

  private static final String LIST_OPTION = "--list";
  private static final String HOST_PARAM = "-h";
  private static final int HOST_ADDRESS_PARAM_INDEX = 0;
  private static final String HOST_ADDRESS_FORMAT = "^\\d+.\\d+.\\d+.\\d+:\\d+$";
  private static final int HOST_ADDRESS_VALUE_INDEX = 1;
  private static final int COMMAND_INDEX = 2;
  private static final String CONNECT_OPTION = "--connect";
  private static final int COMMAND_PARAM_INDEX = 3;
  private static final String DISCONNECT_OPTION = "--disconnect";

  public static void main(String[] args) {

    // FIXME
    BasicConfigurator.configure(new NullAppender());

    MenuService menu = new MenuService();
    SliceService sliceService = new SliceService();

    if (!syntaxIsCorrect(args)) {
      menu.showUsage();
      return;
    }

    switch (args[COMMAND_INDEX]) {
      case LIST_OPTION:
        {
          menu.showList(
              sliceService.getAvailableSlicesForHost(
                  getHostName(), args[HOST_ADDRESS_VALUE_INDEX]));
          break;
        }
      case CONNECT_OPTION:
        {
          if (args.length < 4) {
            menu.showUsage();
          }
          sliceService.joinToSlice(
              args[COMMAND_PARAM_INDEX], getHostName(), args[HOST_ADDRESS_VALUE_INDEX]);
          break;
        }
      case DISCONNECT_OPTION:
        {
          sliceService.disconnectFromSlice();
          break;
        }
      default:
        {
          menu.showUsage();
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

  private static boolean syntaxIsCorrect(String[] args) {
    if (args.length < 3 || !HOST_PARAM.equals(args[HOST_ADDRESS_PARAM_INDEX])) {
      return false;
    }
    String hostUrl = args[HOST_ADDRESS_VALUE_INDEX];
    hostUrl = hostUrl.replace("localhost", "127.0.0.1");

    return hostUrl.matches(HOST_ADDRESS_FORMAT);
  }
}

package pl.mgr.hs.client.cli;

import org.apache.commons.lang.StringUtils;
import pl.mgr.hs.client.cli.rest.data.slice.SliceListResponse;

/** Created by dominik on 18.11.18. */
public class MenuService {

  private static final String LIST_FORMAT = "%-30.30s %-60.60s %n";

  public void showUsage() {
    System.out.println(
        " Commands: \n"
            + " list - list of available slices \n"
            + " connect <slice name> - connect to slice \n"
            + " disconnect - disconnect from slice \n"
            + " service - attach to service application served by slice \n"
            + " exit - exit");
  }

  public void showList(SliceListResponse response, String currentSliceName) {
    System.out.printf(LIST_FORMAT, "NAME", "DESCRIPTION");

    response
        .getSlices()
        .forEach(
            sliceData ->
                System.out.printf(
                    LIST_FORMAT,
                    markCurrentSelectedSlice(currentSliceName, sliceData.getName()),
                    sliceData.getDescription()));
  }

  private String markCurrentSelectedSlice(String runningSliceName, String sliceName) {
    return StringUtils.isNotBlank(runningSliceName) && runningSliceName.equals(sliceName)
        ? "* " + sliceName
        : sliceName;
  }

  public void showCommand() {
    System.out.println("Usage: \n" + " sliceclient <manager ip:port> \n");
  }
}

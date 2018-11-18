package pl.mgr.hs.client.cli.rest;

import pl.mgr.hs.client.cli.rest.data.slice.SliceListResponse;

/** Created by dominik on 18.11.18. */
public class MenuService {
  public void showUsage() {
    System.out.println(
        "Usage: \n"
            + " sliceclient -h <manager ip:port> command \n"
            + " Commands: \n"
            + " --list - list of available slices \n"
            + " --connect <slice name> - connect to slice"
            + " --disconnect - disconnect from slice");
  }

  public void showList(SliceListResponse response) {
    System.out.println("NAME \t DESCRIPTION");
    response
        .getSlices()
        .stream()
        .map(sliceData -> sliceData.getName() + "\t" + sliceData.getDescription() + "\n")
        .forEachOrdered(System.out::print);
  }
}

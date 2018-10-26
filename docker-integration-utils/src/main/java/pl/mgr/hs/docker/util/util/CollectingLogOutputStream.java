package pl.mgr.hs.docker.util.util;

import org.apache.commons.exec.LogOutputStream;

import java.util.LinkedList;
import java.util.List;

/** Created by dominik on 20.10.18. */
public class CollectingLogOutputStream extends LogOutputStream {
  private final List<String> lines = new LinkedList<String>();

  @Override
  protected void processLine(String line, int level) {
    lines.add(line);
  }

  public List<String> getLines() {
    return lines;
  }
}

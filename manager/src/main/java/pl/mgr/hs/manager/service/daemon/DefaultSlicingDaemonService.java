package pl.mgr.hs.manager.service.daemon;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.mgr.hs.manager.dto.internal.NodeDto;
import pl.mgr.hs.manager.dto.web.SliceListDto;
import pl.mgr.hs.manager.service.SliceService;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static pl.mgr.hs.docker.util.constant.Constants.DOWN_NODE_STATE;

/** Created by dominik on 07.11.18. */
@Service
public class DefaultSlicingDaemonService implements SlicingDaemonService {
  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSlicingDaemonService.class);

  private final SliceService sliceService;
  private final TaskScheduler taskScheduler;
  private Map<Integer, List<NodeDto>> nodesScheduledToRemove = new ConcurrentHashMap<>();

  @Value("${daemon.downNodes.retentionPeriod}")
  private long downNodesRetentionPeriod;

  @Autowired
  public DefaultSlicingDaemonService(SliceService sliceService, TaskScheduler taskScheduler) {
    this.sliceService = sliceService;
    this.taskScheduler = taskScheduler;
  }

  @Override
  public void registerSlice(Integer id) {
    nodesScheduledToRemove.put(id, new ArrayList<>());
  }

  @Override
  public void removeSlice(Integer id) {
    nodesScheduledToRemove.remove(id);
  }

  @PostConstruct
  public void init() {
    sliceService
        .getAllSlices()
        .stream()
        .map(SliceListDto::getId)
        .forEach(id -> nodesScheduledToRemove.put(id, new ArrayList<>()));
  }

  @Scheduled(fixedRateString = "${daemon.checkingNodesState.rate}")
  public void checkNodesInSlices() {

    for (Integer sliceName : getAllRegisteredSlices()) {
      List<NodeDto> nodesToRemove =
          sliceService
              .getAllNodesForSlice(sliceName)
              .stream()
              // TODO: DOWN_NODE_STATE
              .filter(node -> DOWN_NODE_STATE.equals(node.getState()))
              .filter(node -> !nodesScheduledToRemove.get(sliceName).contains(node))
              .collect(Collectors.toList());

      nodesToRemove.forEach(nodeDto -> scheduleRemoving(nodeDto, sliceName));
    }
  }

  @Scheduled(
    fixedRateString = "${daemon.rotateJoinTokens.rate}",
    initialDelayString = "${daemon.rotateJoinTokens.rate}"
  )
  public void rotateJoinTokens() {
    LOGGER.debug("Rotate tokens in slices...");

    getAllRegisteredSlices().forEach(sliceService::rotateJoinToken);
  }

  private void scheduleRemoving(NodeDto node, Integer sliceId) {
    LOGGER.info("Scheduling node {} from slice {} to remove", node.getId(), sliceId);

    taskScheduler.schedule(
        new Task(node, sliceId),
        ZonedDateTime.now()
            .plusMinutes(Duration.ofMillis(downNodesRetentionPeriod).toMinutes())
            .toInstant());

    nodesScheduledToRemove.get(sliceId).add(node);
  }

  private Set<Integer> getAllRegisteredSlices() {
    return nodesScheduledToRemove.keySet();
  }

  @AllArgsConstructor
  private class Task implements Runnable {
    private NodeDto node;
    private Integer sliceId;

    @Override
    public void run() {
      LOGGER.info("Removing node {} from slice {}", node.getId(), sliceId);

      // TODO: DOWN_NODE_STATE
      if (DOWN_NODE_STATE.equals(sliceService.getNodeState(sliceId, node.getId()))) {
        sliceService.removeNodeFromSlice(sliceId, node.getId());
      }

      Optional.ofNullable(nodesScheduledToRemove.get(sliceId))
          .ifPresent(nodeDtos -> nodeDtos.remove(node));
    }
  }
}

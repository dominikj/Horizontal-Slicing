package pl.mgr.hs.manager.controller;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerCertificates;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.swarm.Node;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import pl.mgr.hs.manager.entity.Application;
import pl.mgr.hs.manager.entity.Slice;
import pl.mgr.hs.manager.repository.SliceRepository;

import java.net.URI;
import java.nio.file.Paths;
import java.util.List;

/** Created by dominik on 19.10.18. */
@Controller
@RequestMapping("/test")
@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
public class ListController {

  private static final String CERT_PATH_TEST = "/root/.docker/machine/machines/swarm-master";
  private final SliceRepository sliceRepository;

  @Autowired
  public ListController(SliceRepository sliceRepository) {
    this.sliceRepository = sliceRepository;
  }

  @GetMapping("/create")
  @ResponseBody
  public String create(@RequestParam String name) {

    Slice slice = new Slice();
    slice.setName(name);
    slice.setManagerHostName("swarm-master");
    Application servApp = new Application();
    servApp.setImage("nginx");
    servApp.setPublishedPort(80);

    Application cliApp = new Application();
    cliApp.setImage("nginx");
    cliApp.setPublishedPort(8080);
    slice.setServerApplication(servApp);
    slice.setClientApplication(cliApp);
    sliceRepository.save(slice);

    slice = new Slice();
    slice.setName(name + " - nie działający");
    slice.setManagerHostName("ala_2_" + name);
    slice.setServerApplication(servApp);
    slice.setClientApplication(cliApp);
    sliceRepository.save(slice);

    return "created";
  }

  @GetMapping("/get-all")
  @ResponseBody
  public Iterable<Slice> getSlices() {
    return sliceRepository.findAll();
  }

  @GetMapping("/test-docker-api")
  @ResponseBody
  public List<Node> test()
      throws DockerCertificateException, DockerException, InterruptedException {
    final DockerClient docker =
        DefaultDockerClient.builder()
            .uri(URI.create("https://192.168.99.100:2376"))
            .dockerCertificates(new DockerCertificates(Paths.get(CERT_PATH_TEST)))
            .build();
    List<Node> worker = docker.listNodes(Node.Criteria.builder().nodeRole("worker").build());
    return worker;
  }
}

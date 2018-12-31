package pl.mgr.hs.manager.constant;

/** Created by dominik on 03.11.18. */
public interface Constants {
  interface Pages {
    String PAGES = "pages";
    String NEW = PAGES + "/new";
    String LIST = PAGES + "/list";
    String DETAILS = PAGES + "/details";
    String DASHBOARD = PAGES + "/index";
  }

  interface ServiceIds {
    String CLIENT_APP_SERVICE_ID = "clientApp";
    String SERVER_APP_SERVICE_ID = "serverApp";
  }

  interface overlayNetwork {
    String OVERLAY_NETWORK_MASK = "/24";
    String SUBNET = "172.20.0.0" + OVERLAY_NETWORK_MASK;
  }
}

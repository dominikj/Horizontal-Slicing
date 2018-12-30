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
}

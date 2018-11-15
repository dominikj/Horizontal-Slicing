package pl.mgr.hs.manager.interceptor;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Created by dominik on 06.11.18. */
@Component
public class VersionInterceptor extends HandlerInterceptorAdapter {

  private final String appName;
  private final String appVersion;

  public VersionInterceptor(String appName, String appVersion) {
    this.appName = appName;
    this.appVersion = appVersion;
  }

  @Override
  public void postHandle(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      ModelAndView modelAndView)
      throws Exception {

    if (modelAndView == null) {
      return;
    }

    ModelMap model = modelAndView.getModelMap();
    model.addAllAttributes(ImmutableMap.of("appName", appName, "appVersion", appVersion));
  }
}

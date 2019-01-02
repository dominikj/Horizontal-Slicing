package pl.mgr.hs.manager.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import pl.mgr.hs.docker.util.service.machine.DefaultDockerMachineService;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.remote.DefaultDockerIntegrationService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;
import pl.mgr.hs.docker.util.service.ssh.DefaultSSHService;
import pl.mgr.hs.docker.util.service.ssh.SSHService;
import pl.mgr.hs.docker.util.service.virtualbox.DefaultVirtualboxService;
import pl.mgr.hs.docker.util.service.virtualbox.VirtualboxService;
import pl.mgr.hs.manager.interceptor.VersionInterceptor;

@Configuration
@EnableScheduling
@EnableWebSecurity
@PropertySource("classpath:pass.properties")
public class ManagerConfiguration extends WebSecurityConfigurerAdapter
    implements WebMvcConfigurer, SchedulingConfigurer {

  @Value("${app.name}")
  private String appName;

  @Value("${app.version}")
  private String appVersion;

  @Value(("${scheduler.pool.size}"))
  private int schedulerPoolSize;

  @Bean
  public InternalResourceViewResolver viewResolver() {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setPrefix("/");
    resolver.setSuffix(".jsp");
    return resolver;
  }

  @Bean
  public DockerMachineService dockerMachineService(
      @Value("${local.sudo.password}") String sudoPassword,
      @Value("${slice.machine.image.boot2Docker.url}") String boot2DockerUrl) {
    return new DefaultDockerMachineService(sudoPassword, boot2DockerUrl);
  }

  @Bean
  public DockerIntegrationService dockerIntegrationService() {
    return new DefaultDockerIntegrationService();
  }

  @Bean
  public VirtualboxService virtualboxService() {
    return new DefaultVirtualboxService();
  }

  @Bean
  public VersionInterceptor versionInterceptor() {
    return new VersionInterceptor(appName, appVersion);
  }

  @Bean
  public SSHService sshService() {
    return new DefaultSSHService();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(versionInterceptor()).excludePathPatterns("/rest/*");
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    taskRegistrar.setTaskScheduler(threadPoolTaskScheduler());
  }

  @Bean
  public TaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(schedulerPoolSize);
    return scheduler;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        // FIXME
        .antMatchers("/rest/slice/*")
        .permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .httpBasic();
  }
}

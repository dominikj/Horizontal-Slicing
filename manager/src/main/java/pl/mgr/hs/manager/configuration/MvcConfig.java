package pl.mgr.hs.manager.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import pl.mgr.hs.docker.util.service.machine.DefaultDockerMachineService;
import pl.mgr.hs.docker.util.service.machine.DockerMachineService;
import pl.mgr.hs.docker.util.service.remote.DefaultDockerIntegrationService;
import pl.mgr.hs.docker.util.service.remote.DockerIntegrationService;

@Configuration
@PropertySource("classpath:pass.properties")
public class MvcConfig implements WebMvcConfigurer {

    @Bean
    public InternalResourceViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/");
        resolver.setSuffix(".jsp");
        return resolver;
    }

    @Bean
    public DockerMachineService dockerMachineService(@Value("${local.sudo.password}") String sudoPassword) {
        return new DefaultDockerMachineService(sudoPassword);
    }

    @Bean
    public DockerIntegrationService dockerIntegrationService() {
        return new DefaultDockerIntegrationService();
    }
}
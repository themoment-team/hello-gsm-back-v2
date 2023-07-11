package team.themoment.hellogsm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import team.themoment.hellogsm.web.global.security.auth.AuthEnvironment;

@EnableAspectJAutoProxy
@SpringBootApplication
@EnableConfigurationProperties({AuthEnvironment.class})
public class HellogsmWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(HellogsmWebApplication.class, args);
    }

}

package at.aau.serg.scotlandyard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);


    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
        logger.info("Server started");
    }

}

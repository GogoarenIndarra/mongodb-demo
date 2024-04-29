package pl.jablonski.mongodbdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.time.Clock;

@SpringBootApplication
@EnableMongoRepositories
public class MongodbDemoApplication {

    public static void main(final String[] args) {
        SpringApplication.run(MongodbDemoApplication.class, args);
    }

    @Bean
    public Clock getClock() {
        return Clock.systemDefaultZone();
    }
}

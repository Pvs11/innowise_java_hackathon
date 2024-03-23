package cryptocurrencyBotApplication;

import cryptocurrencyBotApplication.model.SendNotification;
import cryptocurrencyBotApplication.service.CryptocurrencyBot;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class cryptocurrencyBotApplication {
    public static void main(String[] args) {
        SpringApplication.run(cryptocurrencyBotApplication.class, args);


    }



}

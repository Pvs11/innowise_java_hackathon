package cryptocurrencyBotApplication.model;

import cryptocurrencyBotApplication.service.CryptocurrencyBot;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerContext;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

@Component
public class SendNotification implements Job {
    private SchedulerContext schedulerContext;
    public void execute(JobExecutionContext jobExecutionContext) {
        try {
            schedulerContext = jobExecutionContext.getScheduler().getContext();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        CryptocurrencyBot bot = (CryptocurrencyBot) schedulerContext.get("cryptocurrencyBot");
        try {
            bot.sendNotificationConstantly();
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}

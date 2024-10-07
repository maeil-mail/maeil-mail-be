package maeilmail;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean("mailExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(5);
        threadPoolTaskExecutor.setThreadNamePrefix("async mail executor-");
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}

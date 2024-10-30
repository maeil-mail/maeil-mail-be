package maeilmail;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
class AsyncConfig implements AsyncConfigurer {

    /**
     * corePoolSize보다 적은 스레드가 있거나 maxPoolSize보다 적은 스레드가 실행중이고,
     * queueCapacity에 의해 정의된 큐 사이즈가 가득찰 경우 스레드가 생성된다.
     * CallerRunsPolicy shutdown 상태가 아닌 경우, ThreadPoolExecutor에 요청한 스레드에서 직접 처리한다.
     */
    @Override
    @Bean("mailExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(3);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        threadPoolTaskExecutor.setThreadNamePrefix("async mail executor-");
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}

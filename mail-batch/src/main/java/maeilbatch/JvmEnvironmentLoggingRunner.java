package maeilbatch;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
class JvmEnvironmentLoggingRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMxBean.getHeapMemoryUsage();
        long allocatedHeapMb = toMB(runtime.totalMemory());
        long usedHeapMb = toMB(heapUsage.getUsed());
        long heapMaxMb = toMB(heapUsage.getMax());

        log.info("===== JVM STARTUP INFO =====");
        log.info("availableProcessors={}", runtime.availableProcessors());
        log.info("heap: used={}MB, allocated={}MB, max={}MB", usedHeapMb, allocatedHeapMb, heapMaxMb);
        log.info("jvmInputArguments={}", runtimeMxBean.getInputArguments());

        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            log.info("gcName={}", gcBean.getName());
        }

        log.info("============================");
    }

    private long toMB(long data) {
        return data / (1024 * 1024);
    }
}

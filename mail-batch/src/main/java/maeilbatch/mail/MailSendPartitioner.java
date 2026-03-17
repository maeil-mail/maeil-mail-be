package maeilbatch.mail;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import maeilbatch.forward.ForwardDao;
import maeilbatch.forward.ForwardIdRange;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class MailSendPartitioner implements Partitioner {

    private final ForwardDao forwardDao;

    @Value("#{jobParameters['datetime']}")
    private LocalDateTime dateTime;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        LocalDateTime startDateTime = dateTime;
        LocalDateTime endDateTime = dateTime.plusDays(1);
        ForwardIdRange idRange = forwardDao.queryIdRange(startDateTime, endDateTime);

        if (idRange.isEmpty()) {
            return createDummyPartition();
        }

        return createPartitions(gridSize, idRange);
    }

    private Map<String, ExecutionContext> createPartitions(long gridSize, ForwardIdRange idRange) {
        Map<String, ExecutionContext> partitions = new LinkedHashMap<>();
        long minId = idRange.minId();
        long maxId = idRange.maxId();
        long partitionSize = calculatePartitionSize(gridSize, idRange);

        for (int partitionIndex = 0; partitionIndex < gridSize; partitionIndex++) {
            long endId = Math.min(minId + partitionSize - 1, maxId);
            partitions.put(
                    "partition" + partitionIndex,
                    createExecutionContext(minId, endId)
            );
            minId = endId + 1;
        }

        return partitions;
    }

    private Map<String, ExecutionContext> createDummyPartition() {
        return Map.of("partition0", createExecutionContext(1L, 0));
    }

    private long calculatePartitionSize(long gridSize, ForwardIdRange idRange) {
        long totalCount = idRange.maxId() - idRange.minId() + 1;

        return Math.max(1L, (long) Math.ceil((double) totalCount / gridSize));
    }

    private ExecutionContext createExecutionContext(long startId, long endId) {
        ExecutionContext context = new ExecutionContext();
        context.putLong("startId", startId);
        context.putLong("endId", endId);

        return context;
    }
}

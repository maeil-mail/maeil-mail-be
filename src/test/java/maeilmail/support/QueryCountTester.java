package maeilmail.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.opentest4j.AssertionFailedError;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class QueryCountTester {

    private final Map<String, Integer> counter = new ConcurrentHashMap<>();

    void tryIncrease(String sql) {
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        if (!sql.startsWith("select")) {
            return;
        }

        counter.compute(currentTransactionName, (key, count) -> count == null ? 1 : count + 1);
    }

    public void assertQueryCount(int expected) {
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();
        Integer count = counter.get(currentTransactionName);
        if (count == null) {
            return;
        }

        if (count != expected) {
            throw new AssertionFailedError("기대되는 select 쿼리의 수와 실제 쿼리 수가 일치하지 않습니다.", expected, count);
        }
    }
}

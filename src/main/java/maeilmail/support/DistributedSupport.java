package maeilmail.support;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DistributedSupport {

    @Value("${distribute.server.count}")
    private int totalServerCount;

    @Value("${distribute.server.index}")
    private int nowServerIndex;

    public boolean isMine(Long id) {
        return nowServerIndex == ((id % totalServerCount) + 1);
    }
}

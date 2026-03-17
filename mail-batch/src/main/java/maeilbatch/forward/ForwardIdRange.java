package maeilbatch.forward;

public record ForwardIdRange(long minId, long maxId) {

    public boolean isEmpty() {
        return minId == 0L || maxId == 0L;
    }
}

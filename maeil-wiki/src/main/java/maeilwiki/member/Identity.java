package maeilwiki.member;

public class Identity {

    private final Long id;

    public Identity(Long id) {
        this.id = id;
    }

    public boolean canAccessToResource(Long resourceOwnerId) {
        return false;
    }

    public Long getId() {
        return id;
    }
}

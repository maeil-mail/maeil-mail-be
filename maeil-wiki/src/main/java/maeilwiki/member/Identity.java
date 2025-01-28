package maeilwiki.member;

import java.util.Objects;

public record Identity(Long id) {

    public boolean canAccessToResource(Long resourceOwnerId) {
        return Objects.equals(id, resourceOwnerId);
    }
}

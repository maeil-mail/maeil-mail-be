package maeilwiki.member.application;

import java.util.Objects;

public record MemberIdentity(Long id, String name, String profileImage) {

    public boolean canAccessToResource(Long resourceOwnerId) {
        return Objects.equals(id, resourceOwnerId);
    }
}

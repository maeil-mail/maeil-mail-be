package maeilmail.subscribe;

import java.util.Map;

public interface EmailView {

    String render(Map<Object, Object> attribute);

    String getType();
}

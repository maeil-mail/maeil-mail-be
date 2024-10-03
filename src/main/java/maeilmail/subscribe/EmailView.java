package maeilmail.subscribe;

import java.util.Map;

interface EmailView {

    String render(Map<Object, Object> attribute);
}

package maeilwiki.sample;

import static org.assertj.core.api.Assertions.assertThat;

import maeilwiki.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SampleWikiServiceTest extends IntegrationTestSupport {

    @Autowired
    private SampleWikiService sampleWikiService;

    @Test
    @DisplayName("test")
    void test() {
        // context 생성 테스팅
        assertThat(sampleWikiService).isNotNull();
    }
}

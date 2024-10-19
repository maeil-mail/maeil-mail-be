package maeilmail.subscribe.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class TemporalSubscribeTest {

    @Test
    @DisplayName("인증 코드가 유효하지 않으면 구독이 불가능하다.")
    void invalidCode() {
        CodeGenerator codeGenerator = Mockito.mock(CodeGenerator.class);
        when(codeGenerator.generateCode())
                .thenReturn("1234");
        TemporalSubscribe temporalSubscribe = new TemporalSubscribe("leehaneul@gmail.com", codeGenerator);

        String invalidCode = "4321";
        assertThatThrownBy(() -> temporalSubscribe.verify(invalidCode))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("인증되지 않은 이메일입니다.");
    }

    @Test
    @DisplayName("인증 코드가 유효하면 구독이 가능하다.")
    void verify() {
        CodeGenerator codeGenerator = Mockito.mock(CodeGenerator.class);
        when(codeGenerator.generateCode())
                .thenReturn("1234");
        TemporalSubscribe temporalSubscribe = new TemporalSubscribe("leehaneul@gmail.com", codeGenerator);

        temporalSubscribe.verify("1234");

        boolean verified = temporalSubscribe.isVerified();
        assertThat(verified).isTrue();
    }
}

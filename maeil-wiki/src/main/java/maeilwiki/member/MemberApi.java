package maeilwiki.member;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class MemberApi {

    private final MemberService memberService;

    @PostMapping("/member")
    public ResponseEntity<MemberTokenResponse> createMember(@RequestBody MemberRequest request) {
        MemberTokenResponse response = memberService.apply(request);

        return ResponseEntity.ok(response);
    }
}

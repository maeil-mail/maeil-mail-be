package maeilwiki.member.application;

import java.util.NoSuchElementException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.member.MemberTokenGenerator;
import maeilwiki.member.github.GithubMemberFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberTokenGenerator memberTokenGenerator;
    private final MemberRepository memberRepository;
    private final GithubMemberFactory memberFactory;

    @Value("${client.secret}")
    private String clientSecret;

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public MemberTokenResponse apply(MemberRequest request) {
        validateClientSecret(request.clientSecret());
        Member candidateMember = memberFactory.create(request.accessToken());
        Member actualMember = trySignInOrSignUp(candidateMember);

        return generateTokenResponse(actualMember);
    }

    private void validateClientSecret(String clientSecret) {
        if (!this.clientSecret.equals(clientSecret)) {
            throw new IllegalArgumentException("올바른 클라이언트 암호를 입력해주세요.");
        }
    }

    private Member trySignInOrSignUp(Member candidateMember) {
        return memberRepository
                .findByProviderId(candidateMember.getProviderId())
                .map(this::changeRefreshToken)
                .orElseGet(signUp(candidateMember));
    }

    private Member changeRefreshToken(Member existingMember) {
        existingMember.setRefreshToken(memberTokenGenerator.generateRefreshToken());

        return existingMember;
    }

    private Supplier<Member> signUp(Member candidateMember) {
        return () -> {
            candidateMember.setRefreshToken(memberTokenGenerator.generateRefreshToken());
            memberRepository.save(candidateMember);

            return candidateMember;
        };
    }

    private MemberTokenResponse generateTokenResponse(Member actualMember) {
        String accessToken = memberTokenGenerator.generateAccessToken(actualMember);
        String refreshToken = actualMember.getRefreshToken();

        return new MemberTokenResponse(accessToken, refreshToken);
    }
}

package maeilwiki.member.application;

import java.util.NoSuchElementException;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import maeilwiki.member.domain.Member;
import maeilwiki.member.domain.MemberRepository;
import maeilwiki.member.infra.MemberRefreshTokenValidator;
import maeilwiki.member.infra.MemberTokenGenerator;
import maeilwiki.member.infra.github.GithubMemberFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final GithubMemberFactory memberFactory;
    private final MemberTokenGenerator memberTokenGenerator;
    private final MemberRefreshTokenValidator memberRefreshTokenValidator;

    public Member findById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public MemberTokenResponse apply(MemberRequest request) {
        Member candidateMember = memberFactory.create(request.oauthAccessToken());
        Member actualMember = trySignInOrSignUp(candidateMember);

        return generateTokenResponse(actualMember);
    }

    private Member trySignInOrSignUp(Member candidateMember) {
        return memberRepository
                .findByProviderId(candidateMember.getProviderId())
                .map(this::rotateRefreshToken)
                .orElseGet(signUp(candidateMember));
    }

    private Supplier<Member> signUp(Member candidateMember) {
        return () -> {
            // TODO: refresh가 만료된 경우 갱신한다. 그렇지 않으면 동기화 한다.
            candidateMember.setRefreshToken(memberTokenGenerator.generateRefreshToken());
            memberRepository.save(candidateMember);

            return candidateMember;
        };
    }

    @Transactional
    public MemberTokenResponse refresh(MemberRefreshRequest request) {
        String refreshToken = request.refreshToken();
        memberRefreshTokenValidator.validateRefreshToken(refreshToken);

        Member member = memberRepository.findByRefreshToken(refreshToken)
                .orElseThrow(MemberIdentityException::new);
        rotateRefreshToken(member);

        return generateTokenResponse(member);
    }

    // TODO: refresh 발급을 제거한다.
    private Member rotateRefreshToken(Member member) {
        String refreshToken = memberTokenGenerator.generateRefreshToken();
        member.setRefreshToken(refreshToken);

        return member;
    }

    private MemberTokenResponse generateTokenResponse(Member member) {
        String accessToken = memberTokenGenerator.generateAccessToken(member);
        String refreshToken = member.getRefreshToken();

        return new MemberTokenResponse(accessToken, refreshToken);
    }
}

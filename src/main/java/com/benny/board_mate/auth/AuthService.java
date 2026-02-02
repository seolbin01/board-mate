package com.benny.board_mate.auth;

import com.benny.board_mate.auth.dto.LoginRequest;
import com.benny.board_mate.auth.dto.SignupRequest;
import com.benny.board_mate.auth.dto.TokenResponse;
import com.benny.board_mate.common.exception.BusinessException;
import com.benny.board_mate.common.exception.ErrorCode;
import com.benny.board_mate.trust.TrustScore;
import com.benny.board_mate.trust.TrustScoreRepository;
import com.benny.board_mate.user.User;
import com.benny.board_mate.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final TrustScoreRepository trustScoreRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role("USER")
                .build();

        userRepository.save(user);

        // 신뢰도 초기화 (100점)
        trustScoreRepository.save(new TrustScore(user));

        return createToken(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return createToken(user);
    }

    private TokenResponse createToken(User user) {
        return TokenResponse.builder()
                .accessToken(jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole()))
                .refreshToken(jwtProvider.createRefreshToken(user.getId(), user.getEmail(), user.getRole()))
                .build();
    }
}
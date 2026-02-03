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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrustScoreRepository trustScoreRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        private SignupRequest request;

        @BeforeEach
        void setUp() {
            request = new SignupRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");
            request.setNickname("테스터");
        }

        @Test
        @DisplayName("성공 - 새로운 사용자가 회원가입하면 토큰을 반환한다")
        void signup_Success() {
            // given
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.existsByNickname(request.getNickname())).willReturn(false);
            given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User user = invocation.getArgument(0);
                ReflectionTestUtils.setField(user, "id", 1L);
                return user;
            });
            given(jwtProvider.createAccessToken(eq(1L), eq(request.getEmail()), eq("USER")))
                    .willReturn("accessToken");
            given(jwtProvider.createRefreshToken(eq(1L), eq(request.getEmail()), eq("USER")))
                    .willReturn("refreshToken");

            // when
            TokenResponse response = authService.signup(request);

            // then
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getRefreshToken()).isEqualTo("refreshToken");

            verify(trustScoreRepository).save(any(TrustScore.class));
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 이메일로 가입 시 예외 발생")
        void signup_DuplicateEmail() {
            // given
            given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_EMAIL);

            verify(userRepository, never()).save(any());
            verify(trustScoreRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패 - 이미 존재하는 닉네임으로 가입 시 예외 발생")
        void signup_DuplicateNickname() {
            // given
            given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
            given(userRepository.existsByNickname(request.getNickname())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("로그인")
    class Login {

        private LoginRequest request;
        private User user;

        @BeforeEach
        void setUp() {
            request = new LoginRequest();
            request.setEmail("test@example.com");
            request.setPassword("password123");

            user = User.builder()
                    .email("test@example.com")
                    .password("encodedPassword")
                    .nickname("테스터")
                    .role("USER")
                    .build();
            ReflectionTestUtils.setField(user, "id", 1L);
        }

        @Test
        @DisplayName("성공 - 올바른 자격증명으로 로그인하면 토큰을 반환한다")
        void login_Success() {
            // given
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
            given(jwtProvider.createAccessToken(eq(1L), eq("test@example.com"), eq("USER")))
                    .willReturn("accessToken");
            given(jwtProvider.createRefreshToken(eq(1L), eq("test@example.com"), eq("USER")))
                    .willReturn("refreshToken");

            // when
            TokenResponse response = authService.login(request);

            // then
            assertThat(response.getAccessToken()).isEqualTo("accessToken");
            assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 이메일로 로그인 시 예외 발생")
        void login_UserNotFound() {
            // given
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
        }

        @Test
        @DisplayName("실패 - 잘못된 비밀번호로 로그인 시 예외 발생")
        void login_WrongPassword() {
            // given
            given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_CREDENTIALS);
        }
    }
}

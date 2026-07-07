지금까지 대화 핵심 내용입니다.

**OAuth 처리 방향**
- `api-gateway`가 OAuth 인증 자체를 처리하기보다, 기존 구조에 맞게 `auth-service`가 OAuth2 로그인/콜백/계정 연결/JWT 발급을 담당하는 방향이 적절하다고 판단했습니다.
- `api-gateway`는 `/oauth2/**`, `/login/oauth2/**`를 인증 예외로 열고 `auth-service`로 라우팅하는 역할만 맡는 구조로 정리했습니다.

**원래 의도한 OAuth 흐름**
- 프론트에서 구글/네이버/깃허브 로그인 버튼 클릭
- OAuth provider에서 인증
- provider가 `auth-service`의 redirect URI로 callback
- 서버가 provider 응답을 확인
- `oauth_account`, `appuser` 조회
- 등록된 사용자면 기존 로그인과 동일하게 내부 JWT/Refresh Token 발급 후 대시보드 이동
- 신규 사용자면 가입/회사 생성 페이지 이동

**현재 구현한 Spring Security 방식**
- 프론트가 Google URL을 직접 만들지 않고, auth-service 시작 URL로 이동해야 함:

```text
http://localhost:8081/oauth2/authorization/google
```

- Google 콘솔에 등록할 redirect URI:

```text
http://localhost:8081/login/oauth2/code/google
```

- `/login/oauth2/code/google`은 직접 만든 Controller가 아니라 Spring Security OAuth2 필터가 처리합니다.
- 인증 성공 후에는 `OAuth2AuthenticationSuccessHandler`가 호출되고, 여기서 `OAuthLoginService`를 통해 계정 연결/가입 분기를 처리합니다.

**프론트 코드 문제**
- 기존 프론트는 Google OAuth URL을 직접 구성하고 있었습니다.

```text
https://accounts.google.com/o/oauth2/v2/auth
redirect_uri=http://localhost:8081/login/oauth2/code/google
```

- 이 방식은 현재 Spring Security OAuth2 Login 방식과 맞지 않습니다.
- 이유는 auth-service가 OAuth 시작 단계에 참여하지 않아 Spring Security가 `state`를 세션에 저장하지 못하기 때문입니다.
- 그래서 callback 시 `state` 불일치 또는 authorization request 없음으로 실패하고 `/login?error`로 떨어질 수 있습니다.

**state 처리 결론**
- Spring Security OAuth2 Login을 유지한다면 `state`를 직접 구현할 필요는 없습니다.
- 대신 반드시 `/oauth2/authorization/{provider}`로 시작해야 합니다.
- 프론트에서 Google URL을 직접 만들고 싶다면, Spring Security 기본 OAuth2 Login 대신 직접 callback controller를 구현해야 합니다.

**직접 구현 방식과 Spring Security 방식 차이**
- 프론트 직접 방식:
  - 프론트가 Google 인증 URL 생성
  - 서버는 callback부터 직접 처리
  - 서버가 `state`, `code -> token`, `user-info` 조회를 직접 구현해야 함
- Spring Security 방식:
  - 프론트는 auth-service 시작 URL만 호출
  - auth-service/Spring Security가 provider 인증 URL 생성, state 저장, callback, token 교환, user-info 조회 처리
  - 우리는 success handler에서 서비스 계정 처리만 구현

**구현된 주요 파일**
- `AuthType`
- `OAuthProvider`
- `OAuthAccount`
- `OAuthAccountRepository`
- provider별 profile extractor:
  - `GoogleOAuthUserProfileExtractor`
  - `NaverOAuthUserProfileExtractor`
  - `GithubOAuthUserProfileExtractor`
- `OAuthUserProfileExtractorRegistry`
- `OAuthLoginService`
- `OAuth2AuthenticationSuccessHandler`
- `OAuth2AuthenticationFailureHandler`
- `PasswordConfig`

**순환 참조 이슈**
- `auth-service` 실행 시 순환 참조가 발생했습니다.

```text
AuthSecurityConfig
 -> OAuth2AuthenticationSuccessHandler
 -> OAuthLoginService
 -> UserService
 -> PasswordEncoder
 -> AuthSecurityConfig
```

- 원인은 `PasswordEncoder` 빈이 `AuthSecurityConfig` 안에 있었기 때문입니다.
- 해결:
  - `PasswordEncoder` 빈을 `PasswordConfig`로 분리
  - `DaoAuthenticationProvider`는 `PasswordEncoder`를 파라미터로 주입받도록 변경
  - `OAuth2AuthenticationSuccessHandler`, `ClientRegistrationRepository`는 `ObjectProvider`로 지연 조회하도록 변경

**ObjectProvider 설명**
- `ObjectProvider`는 빈을 즉시 주입받지 않고 필요한 시점에 조회하게 합니다.
- `ClientRegistrationRepository`는 OAuth 설정이 없을 수도 있으므로 `ObjectProvider`가 적절합니다.
- `OAuth2AuthenticationSuccessHandler`는 순환 참조 완화를 위해 사용했지만, `PasswordEncoder` 분리 후에는 직접 주입으로 바꿔도 됩니다.

**현재 401 / `/login?error` 원인**
- `/login/oauth2/code/google` 자체가 문제라기보다, OAuth callback 이전에 Spring Security가 state를 저장하지 못한 흐름일 가능성이 큽니다.
- 프론트가 Google URL을 직접 호출하면 이 문제가 발생합니다.
- 해결은 프론트 로그인 시작 URL을 다음으로 바꾸는 것입니다.

```text
http://localhost:8081/oauth2/authorization/google
```

또는 gateway 사용 시:

```text
http://localhost:8070/oauth2/authorization/google
```

Google redirect URI도 같은 origin으로 맞춰야 합니다.

```text
http://localhost:8070/login/oauth2/code/google
```
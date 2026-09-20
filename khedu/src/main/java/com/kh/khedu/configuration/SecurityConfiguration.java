package com.kh.khedu.configuration;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kh.khedu.enums.RoleType;

import jakarta.servlet.http.Cookie;

@Configuration
public class SecurityConfiguration {
	// 단방향 암호화를 위한 BCryptPasswordEncoder 등록
	@Bean
	public PasswordEncoder passwordEncoder() {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		return encoder;
	}
	
	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http//Spring Security가 제공하는 http 설정 객체
		,BearerTokenResolver bearerTokenResolver // 내가 만든 토큰해석기
		,JwtAuthenticationConverter jwtAuthenticationConverter // 내가 jwt에서 만든 권한을 securityfilterChain에 맞게 변환
	) throws Exception {
		//http에 홈페이지 운영 규칙을 모두 설정하고 Build해서 반환
		http
			//csrf 비활성화
			.csrf(csrf -> csrf.disable())
			//cors 설정 : 별도로 등록한 CorsconfigurationSource의 설정을 따르겟다(없으면 기본값)
			.cors(Customizer.withDefaults())
			//session 설정 : 무상태(StateLess)로 설정
			.sessionManagement(
				session-> session.sessionCreationPolicy(
					SessionCreationPolicy.STATELESS //HTTP 세션을 인증 상태 유지 목적으로 사용하지 않겠다HTTP 세션을 인증 상태 유지 목적으로 사용하지 않겠다
				)
			)
			//security의 기본 제공되는 로그인 화면과 인증시스템을 비활성화
			.formLogin(form->form.disable())
			.httpBasic(basic->basic.disable())
			.logout(logout->logout.disable())
//			.logout(AbstractHttpConfigurer::disable) //Java Method Reference
			
			//HTTP 요청에 대한 처리 계획
			//.requestMatchers("적용시킬 주소or패턴")
			// .permitAll() - 모두 수락(접속 허용)
			// .denyAll() - 모두 거절(접속차단)
			// .authenticated() - 인증 필요 (인증 방식에 대해서는 따로 정의)
			// .hasRole() - Spring security의 기본 역할 (`ROLE_`로 시작)
			// .hasAuthority() - 사용자가 임의로 지정한 역할
		
			.authorizeHttpRequests(
					auth -> auth	
					 // =========================================================
			        // [0] React SPA 화면 / 정적 리소스
			        // =========================================================
			        .requestMatchers(
			            "/",
			            "/index.html",
			            "/assets/**",
			            "/favicon.ico",
			            "/*.svg",
			            "/*.png",
			            "/*.ico",
			            "/error",

			            // React 화면 진입 자체는 공개
			            "/employee/**",
			            "/academy/**"
			            ,"/api/sse/connect"
			        ).permitAll()


			        // =========================================================
			        // [1] 비회원 공개 API
			        // =========================================================
			        .requestMatchers(
			            "/active",

			            // 학원 공개 영역
			            "/api/academy/",
			            "/api/academy/reservation/**"
			            ,"/api/attach/**",

			            // 공개 강사 정보
			            "/api/academy/tutor/**"
			        ).permitAll()


			        // =========================================================
			        // [2] 인증 / 회원가입 관련 공개 API
			        // =========================================================
			        .requestMatchers(
			        		 // 로그인 / 로그아웃 / 토큰 갱신
			        	    "/service/auth/login",
			        	    "/service/auth/logout",
			        	    "/service/auth/refresh",

			        	    // 인증
			        	    "/service/cert/**",

			        	    // 계정 찾기
			        	    "/api/account/find-id",
			        	    "/api/account/find-password",
			        	    "/api/account/check-id/**"
			        ).permitAll()
			        
			        // 회원가입만 공개
			        .requestMatchers(
			            HttpMethod.POST,
			            "/api/academy/parent/",
			            "/api/academy/student/"
			        ).permitAll()

			        .requestMatchers(
		        	    HttpMethod.PUT,
		        	    "/api/academy/parent/"
		        	).hasAuthority(
		        	    RoleType.PARENT.getCode()
		        	)
			        
			        // =========================================================
			        // [3] 학부모
			        // =========================================================
			        .requestMatchers(
				            "/api/academy/parent/**"
			        ).hasAnyAuthority(
				        RoleType.PARENT.getCode()
			        )
			        // =========================================================
			        // [4] 학생
			        // =========================================================
			        .requestMatchers(
				        "/api/academy/student/",
			            "/api/academy/assignment/**",
			            "/api/academy/assignment-submit/**",
			            "/api/academy/attempt-answer/**",
			            "/api/academy/attempt/**",
			            "/api/academy/exam/student/**"
			        ).hasAnyAuthority(
			            RoleType.STUDENT.getCode()
			        )
			        
			        // [5-1] 학생 학부모
			        .requestMatchers(
				            "/api/academy/question/**"
			        ).hasAnyAuthority(
				        RoleType.PARENT.getCode(),
				        RoleType.STUDENT.getCode()
			        )
			        // =========================================================
			        // [5] 회원 전체
			        // 학생 / 학부모 / 강사 / 데스크 / 원장
			        // =========================================================
			        .requestMatchers(
			            "/api/account/**",
			            "/api/attach/**",
			            "/api/academy/**"
			        ).hasAnyAuthority(
			            RoleType.STUDENT.getCode(),
			            RoleType.PARENT.getCode(),
			            RoleType.TUTOR.getCode(),
			            RoleType.DESK.getCode(),
			            RoleType.ADMIN.getCode()
			        )


			        // =========================================================
			        // [6] 직원
			        // 강사 / 데스크 / 원장
			        // =========================================================
			        .requestMatchers(
			            "/api/employee/**",
			            "/api/attendance/**"
			        ).hasAnyAuthority(
			            RoleType.TUTOR.getCode(),
			            RoleType.DESK.getCode(),
			            RoleType.ADMIN.getCode()
			        )


			        // =========================================================
			        // [7] 데스크 / 원장
			        // =========================================================
			        .requestMatchers(
			            "/api/admin/employee/**"
			        ).hasAnyAuthority(
			            RoleType.DESK.getCode(),
			            RoleType.ADMIN.getCode()
			        )
			        
			        // view 허용
			        //뷰 허용
					.requestMatchers(HttpMethod.GET, "/views/**").permitAll()
			        
			        // =========================================================
			        // [8] 나머지
			        // =========================================================
			        .anyRequest().permitAll()
				)
			//JWT를 어떻게 검증할 것인지 설정 (JwtDecoder가 반드시 필요)
			//→ BearerTokenResolver :AccessToken을 꺼내서 Jwt를 뽑아내는 도구
			//→ JwtAuthenticationConverter : Jwt의 authority를 Spring Security용으로 변환
			.oauth2ResourceServer(
					oauth2 -> 	oauth2
						//하단에 @Bean으로 만든 해석도구를 oauth2의 표준 해석기로 설정
						.bearerTokenResolver(bearerTokenResolver)
						//하단에 @Bean으로 만든 JWT 권한 해석 및 변환기를 설정
						.jwt(
							jwt -> jwt.jwtAuthenticationConverter(
									jwtAuthenticationConverter //내가 만든 도구
							)
						)
						
				)
			//예외 상황 처리 설정
			//→ 인증되지 않은 경우는 401 , 권한이 부족한 경우는 403으로 반환하도록 설정
			.exceptionHandling(
				exception -> exception
					//인증되지 않은 경우
					.authenticationEntryPoint(
						(req, res, exp) -> res.setStatus(401)
					)
					//접근을 거부당한 경우
					.accessDeniedHandler(
						(req, res, exp) -> res.setStatus(403)
					)
			)
		;
		
		return http.build();
	}
	
	//CorsConfigurationSource 생성 (Security의 기본값으로 자동 설정)
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		//설정 객체를 생성 //data가 클 경우에만 스트리밍방식을 사용(스트리밍 방식일 때, .reactive를 import)
		CorsConfiguration config = new CorsConfiguration();
		
		//CORS 설정 코드 작성
		//[1] 허용되는 접근 대상을 지정 (allow origins or pattern)
		config.setAllowedOrigins(List.of(
			// 여기에 운영주소 넣어주면됨
			"http://localhost:5173",
			"http://52.79.242.143:8080"
		));
		//[2] 허용할 HTTP 메소드 설정
		config.setAllowedMethods(List.of(
				"GET", "POST", "PUT", "PATCH", "DELETE",
				//OPTIONS는 불확실한 상황일 때 보내는 사전 답사용 요청
				//불확실한 상황 : origin이 다른데 GET/HEAD가 아닌 요청을 보내면 불확실하다고 판단
				"OPTIONS",
				//HEAD는 GET과 같은데 응답 본문을 가져오지 않는 요청방식
				"HEAD"
		));
		
		
		//[3] 허용할 HTTP헤더 설정
		//→ 특정 헤더를 반드시 포함해야 하는 경우가 존재
		//→ 보안이 강화되면 CSRF 헤더만 허용하는 경우가 있음 (CSRF: 사이트간 요청 위조 방지 헤더)
		config.setAllowedHeaders(List.of("*"));
		//[4] 인증 쿠기 설정
		config.setAllowCredentials(true);
		//[5] preflight 시간 설정(캐싱 지속시간)
		config.setMaxAge(Duration.ofHours(1L)); //1시간 (=3600초, 기본값)
		
		//적용시킬 주소까지 포함한 설정 객체로 확장
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();	
		source.registerCorsConfiguration(
			"/**", //적용할 주소
			config //적용할 설정
		);
		//완성된 객체 반환
		return source;
	}
	
	//BearerTokenResolver
		// - Bearer는 토큰의 한 종류 (인증을 통해 무언가를 얻어내겠다는 의미의 토큰)
		// - 토큰은 표준이 없어서 JWT앞에 어떤 접두사를 붙여도 무방
		// - 헤더 방식인 경우 "Authorization: Bearer [토큰값]" 과 같은 형태로 전달
		// - 카카오는 KAKAOAK 라는 자체 이름을 만들어서 토큰에 적용하여 사용하고 있음 (즉, 자율적)
		// - 인증용 토큰을 해석하는 도구(accessToken 쿠키)
	@Bean
	public BearerTokenResolver bearerTokenResolver() {
	    return request -> {
	        String path = request.getServletPath();
	        String method = request.getMethod();

	        // =========================================================
	        // 1. API 요청(/api/**, /service/**, /ws)이 아니면
	        //    토큰을 추출하지 않음
	        // =========================================================
	        if (!path.startsWith("/api/")
	                && !path.startsWith("/service/")
	                && !path.startsWith("/ws")) {
	            return null;
	        }

	        // =========================================================
	        // 2. 비로그인/공개 API
	        //    브라우저에 만료된 쿠키가 남아있어도 토큰 검사를 하지 않음
	        // =========================================================
	        if (
	            // 인증 관련
	            path.startsWith("/service/auth/") ||

	            // 이메일 인증
	            path.startsWith("/service/cert/") ||

	            // React 화면
	            path.equals("/academy") ||
	            path.startsWith("/academy/") ||

	            // 학생/학부모 회원가입
	            // ★ POST일 때만 토큰 추출 제외
	            (
	                "POST".equals(method) &&
	                (
	                    path.equals("/api/academy/student") ||
	                    path.equals("/api/academy/student/") ||
	                    path.equals("/api/academy/parent") ||
	                    path.equals("/api/academy/parent/")
	                )
	            ) ||

	            // 기존 공개 API
	            path.startsWith("/api/student") ||
	            path.startsWith("/api/parent") ||

	            // 계정 찾기 및 중복체크
	            path.startsWith("/api/account/check-id/") ||
	            path.equals("/api/account/check-id") ||
	            path.equals("/api/account/find-id") ||
	            path.equals("/api/account/find-password")
	        ) {
	            return null;
	        }

	        // =========================================================
	        // 3. 여기까지 왔다면 accessToken이 필요한 요청
	        // =========================================================
	        Cookie[] cookies = request.getCookies();

	        if (cookies == null) {
	            return null;
	        }

	        // accessToken 쿠키 찾기
	        return Arrays.stream(cookies)
	                .filter(cookie -> cookie.getName().equals("accessToken"))
	                .map(cookie -> cookie.getValue())
	                .filter(value -> value != null && !value.isBlank())
	                .findFirst()
	                .orElse(null);
	    };
	}
		
		//JwtAuthenticationConverter
		// - JWT의 authorities 항목을 Spring Security Authority로 변환하는 역할
		@Bean
		public JwtAuthenticationConverter jwtAuthenticationConverter() {
			
			//권한 정보 변환 도구 생성
			JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
			
			//jwt에서 authorities와 관련된 claim 이름을 설정
			converter.setAuthoritiesClaimName("authorities");
			
			//기본 접두사 (ROLE_, SCOPE_)를 모두 제거
			converter.setAuthorityPrefix(""); //접두사 없음
			
			//최종 JWT 변환 도구를 생성	
			JwtAuthenticationConverter result = new JwtAuthenticationConverter();
			
			result.setJwtGrantedAuthoritiesConverter(jwt -> {

		        Collection<GrantedAuthority> authorities =
		                converter.convert(jwt);

		        return authorities;
		    });
			
			//앞서 만든 도구를 장착
			result.setJwtGrantedAuthoritiesConverter(converter);
			
			//반환
			return  result;
		}
}

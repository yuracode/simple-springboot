package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// レッスン19：Spring Security の設定
@Configuration
public class SecurityConfig {

    // どの URL にログインが必要かを決める
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/books/new").authenticated()                 // 登録フォーム
                .requestMatchers(HttpMethod.POST, "/books").authenticated()    // 登録処理
                .anyRequest().permitAll())                                     // それ以外は誰でも
            .formLogin(form -> form
                .loginPage("/login")             // 自作のログイン画面
                .defaultSuccessUrl("/books")     // ログイン後の移動先（元のページがあればそちら）
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout"));
        return http.build();
    }

    // パスワードはハッシュ化して保存する
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // 学習用：メモリ上にユーザーを1人用意する（本番では DB から読み込む）
    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        UserDetails user = User.withUsername("user")
                .password(encoder.encode("password"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }
}

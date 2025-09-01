package com.ecommerce.userservice.infrastructure.config;

import com.ecommerce.userservice.infrastructure.security.jwt.JwtFilter;
import com.ecommerce.userservice.infrastructure.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter, CustomUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/login", "/api/user/register","/api/user/health-check","/actuator/**","/api/user/get-all-user").permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) //Stateless session
                .userDetailsService(userDetailsService)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.
                userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
/*
package com.ecommerce.userservice.infrastructure.config;


import com.ecommerce.userservice.infrastructure.security.jwt.JwtFilter;
import com.ecommerce.userservice.infrastructure.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtFilter jwtFilter, CustomUserDetailsService userDetailsService) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF'yi devre dışı bırakmak için 'csrf().disable()' yerine 'csrf().disable().and()' ekliyoruz.
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        //bu equestmachers'in içinde permitAll dediğimiz endpointlere herkesin erişebileceği anlamına geliyor, ancak anyRequest().authenticated() dediğimizde ise public olmayan tüm
                        //endpointlere bir authentication işlemi uygulayacağımız anlamına geliyor.
                        .requestMatchers("api/users/login","api/users/register").permitAll()//herkesin erişebileceği bir endpoint
                        .anyRequest().authenticated()  //geri kalan tüm isteklerin kimlik doğrulaması gerektirmesini sağlıyoruz.
                )
                //session kullanmıcaz yani token sürekli olacak ve her requestte kontrol edeceğiz
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //userDetailsService'yi atayarak tokeni doğrulamak için kullanıyoruz
                .userDetailsService(userDetailsService)
                //buraya jwtFilter'ı ekliyoruz,bu filter her requestte çalışacak ve tokeni kontrol edecek, biz b urada username ve password ile giriş yapmadığımız için bu filteri ekliyoruz
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                //burada build ediyoruz
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        //önce AuthBuilder'i oluşturuyoruz
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        //sonra da userDetailsService ve passwordEncoder'ı ekliyoruz
        authBuilder.userDetailsService(userDetailsService)
                //.passwordEncoder(NoOpPasswordEncoder.getInstance());
                // pawwsordleri iifreleme için BCrypt kullanıyoruz bunu ayrı bir bean ile tanımladık zaten
                .passwordEncoder(passwordEncoder());
        return authBuilder.build();
    }
    //passwordEncoder beani tanımlıyoruz,bu beani kullanarak şifreleri bCrypt ile şifreleyeceğiz
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
*/
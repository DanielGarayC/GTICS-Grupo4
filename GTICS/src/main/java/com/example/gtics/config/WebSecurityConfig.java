package com.example.gtics.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    final DataSource dataSource;

    public WebSecurityConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin().loginPage("/ExpressDealsLogin").loginProcessingUrl("/submitLoginForm").successHandler(new CustomAuthenticationSuccessHandler());

        http.logout();
        http.authorizeHttpRequests()
                .requestMatchers("/SuperAdmin", "/SuperAdmin/**").hasAnyAuthority("Super Admin")
                .requestMatchers("/UsuarioFinal", "/UsuarioFinal/**").hasAnyAuthority("Usuario Final")
                .requestMatchers("/AdminZonal", "/AdminZonal/**").hasAnyAuthority("Administrador Zonal")
                .requestMatchers("/Agente", "/Agente/**").hasAnyAuthority("Agente")
                .anyRequest().permitAll();
        return http.build();
    }

    @Bean
    public UserDetailsManager user(DataSource dataSource) {
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        String sql1 = "select email, contrasena, activo from usuario where email=?";
        String sql2 = "SELECT u.email, r.nombreRol FROM usuario u INNER JOIN rol r ON u.idRol = r.idRol WHERE u.email = ? AND u.activo = 1 AND u.baneado = 0";


        users.setUsersByUsernameQuery(sql1);
        users.setAuthoritiesByUsernameQuery(sql2);
        return users;
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

}

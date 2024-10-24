package com.example.gtics.config;

import com.example.gtics.repository.RolRepository;
import com.example.gtics.repository.UsuarioSessionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.web.filter.HiddenHttpMethodFilter;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    final DataSource dataSource;
    final UsuarioSessionRepository usuarioSessionRepository;
    public WebSecurityConfig(DataSource dataSource,
                             UsuarioSessionRepository usuarioSessionRepository) {

        this.dataSource = dataSource;
        this.usuarioSessionRepository = usuarioSessionRepository;

    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.formLogin()
                .loginPage("/ExpressDealsLogin")
                .loginProcessingUrl("/submitLoginForm")
                .successHandler((request, response, authentication) -> {

                    DefaultSavedRequest defaultSavedRequest =
                            (DefaultSavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");

                    HttpSession session = request.getSession();
                    session.setAttribute("usuario", usuarioSessionRepository.findByEmail(authentication.getName()));
                    //si vengo por url -> defaultSR existe
                    if (defaultSavedRequest != null) {
                        String targetURl = defaultSavedRequest.getRequestURL();
                        new DefaultRedirectStrategy().sendRedirect(request, response, targetURl);
                    } else { //estoy viniendo del botón de login
                        String rol = "";
                        for (GrantedAuthority role : authentication.getAuthorities()) {
                            rol = role.getAuthority();
                            break;
                        }

                        if (rol.equals("Super Admin")) {
                            response.sendRedirect("/SuperAdmin");
                        } else if (rol.equals("Usuario Final")) {
                            response.sendRedirect("/UsuarioFinal");
                        }else if (rol.equals("Administrador Zonal")) {
                            response.sendRedirect("/AdminZonal");
                        }else {
                            response.sendRedirect("/Agente");
                        }
                    }
                });

        http.logout()
                .logoutSuccessUrl("/ExpressDealsLogin")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true);

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

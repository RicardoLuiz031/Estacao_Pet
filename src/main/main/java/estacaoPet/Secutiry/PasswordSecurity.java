package estacaoPet.Secutiry;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

//Configuration vem de configuração, logo começando a configurar um método de segurança para o spring
@Configuration
public class PasswordSecurity {
    //Bean é um atributo do spring que recebe um atributo e manipula ele e retorna o resultado esperado
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Com o spring-boot-starter-security no projeto, o Spring bloqueia TUDO por padrão
     * (tela de login própria, CSRF...). Aqui desligamos esse comportamento padrão, porque
     * a autenticação é feita pelo nosso /api/login e a proteção das rotas /api/** (login,
     * expiração de 30 s e perfil ADMIN) é feita pelo SessaoConfig, validando CADA requisição.
     *
     * CSRF desligado: a API só aceita JSON e o cookie de sessão é SameSite=Strict
     * (configurado no application.properties), então outro site não consegue enviar requisições
     * em nome do usuário.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}

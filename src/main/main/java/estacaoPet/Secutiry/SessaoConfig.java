package estacaoPet.Secutiry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

/**
 * Controle de sessão no SERVIDOR (o cronômetro do navegador pode ser pausado ou manipulado):
 *  - toda rota /api/** (menos login/logout) exige usuário logado;
 *  - se passou mais do que o limite sem nenhuma requisição, a sessão é destruída (401);
 *  - /api/funcionarios só para ADMIN (403).
 */
@Configuration
public class SessaoConfig implements WebMvcConfigurer {

    public static final String ATTR_ID = "usuarioId";
    public static final String ATTR_NOME = "usuarioNome";
    public static final String ATTR_PERFIL = "usuarioPerfil";
    public static final String ATTR_ULTIMA = "ultimaAtividade";

    @Value("${app.sessao.limite-segundos:30}")
    private long limiteSegundos;

    @Value("${app.sessao.tolerancia-segundos:5}")
    private long toleranciaSegundos;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws IOException {
                if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                    return true;
                }
                HttpSession sessao = request.getSession(false);
                if (sessao == null || sessao.getAttribute(ATTR_ID) == null) {
                    return negar(response, 401, "NAO_AUTENTICADO", "Faça login para continuar.");
                }

                long agora = System.currentTimeMillis();
                Long ultima = (Long) sessao.getAttribute(ATTR_ULTIMA);
                long limiteMs = (limiteSegundos + toleranciaSegundos) * 1000;
                if (ultima == null || agora - ultima > limiteMs) {
                    sessao.invalidate();
                    return negar(response, 401, "SESSAO_EXPIRADA",
                            "Sua sessão expirou por inatividade. Faça login novamente.");
                }
                sessao.setAttribute(ATTR_ULTIMA, agora); // atividade: reinicia a contagem

                boolean rotaDeAdmin = request.getRequestURI().startsWith("/api/funcionarios");
                if (rotaDeAdmin && !"ADMIN".equals(sessao.getAttribute(ATTR_PERFIL))) {
                    return negar(response, 403, "ACESSO_NEGADO", "Apenas administradores podem acessar esta área.");
                }
                return true;
            }
        }).addPathPatterns("/api/**").excludePathPatterns("/api/login", "/api/logout");
    }

    // Faz o caminho "../static/css/x.css" das páginas funcionar: /static/** -> pasta resources/static
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    private boolean negar(HttpServletResponse response, int status, String codigo, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"codigo\":\"" + codigo + "\",\"erro\":\"" + mensagem + "\"}");
        return false;
    }
}

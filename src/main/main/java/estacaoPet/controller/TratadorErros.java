package estacaoPet.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Transforma qualquer erro da API em JSON no formato:
 *   { "erro": "mensagem geral", "campos": { "cpf": "CPF inválido." } }
 * O frontend mostra "erro" no #mensagem e cada item de "campos" perto do campo correspondente.
 */
@RestControllerAdvice
public class TratadorErros {

    private static final Logger log = LoggerFactory.getLogger(TratadorErros.class);

    /** Erro de regra de negócio ligado a um (ou mais) campos do formulário. */
    public static class ErroValidacao extends RuntimeException {
        private final Map<String, String> campos;

        public ErroValidacao(String campo, String mensagem) {
            super(mensagem);
            this.campos = Map.of(campo, mensagem);
        }

        public ErroValidacao(Map<String, String> campos) {
            super("Verifique os campos informados.");
            this.campos = campos;
        }

        public Map<String, String> getCampos() {
            return campos;
        }
    }

    @ExceptionHandler(ErroValidacao.class)
    public ResponseEntity<Map<String, Object>> regraDeNegocio(ErroValidacao e) {
        return ResponseEntity.badRequest().body(corpo("Verifique os campos informados.", e.getCampos()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> beanValidation(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> campos.putIfAbsent(erro.getField(), erro.getDefaultMessage()));
        return ResponseEntity.badRequest().body(corpo("Verifique os campos informados.", campos));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> comStatus(ResponseStatusException e) {
        String mensagem = e.getReason() == null ? "Não foi possível concluir a operação." : e.getReason();
        return ResponseEntity.status(e.getStatusCode()).body(corpo(mensagem, null));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integridade(DataIntegrityViolationException e) {
        log.warn("Violação de integridade no banco: {}", e.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(corpo("Operação não permitida: registro duplicado ou em uso por outro cadastro.", null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(corpo("Dados enviados em formato inválido.", null));
    }

    private Map<String, Object> corpo(String erro, Map<String, String> campos) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", erro);
        if (campos != null && !campos.isEmpty()) {
            corpo.put("campos", campos);
        }
        return corpo;
    }
}

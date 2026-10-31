package estacaoPet.model;

import jakarta.persistence.*;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

@Entity
@Table(name = "tutor")
public class Tutor {
    //Atributos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tutor")
    private Integer id;
    @Column(name = "nomeTutor")
    private String nome;

    // Campos sensíveis: gravados CRIPTOGRAFADOS (AES-GCM) no banco
    @Convert(converter = Cripto.class)
    @Column(name = "cpfCifTutor", nullable = false)
    private String cpf;
    // Hash (HMAC) do CPF: permite buscar/evitar duplicidade sem descriptografar o banco todo
    @Column(name = "cpfHashTutor", nullable = false, unique = true, length = 64)
    private String cpfHash;
    @Convert(converter = Cripto.class)
    @Column(name = "telefoneCifTutor")
    private String telefone;
    @Convert(converter = Cripto.class)
    @Column(name = "emailCifTutor")
    private String email;
    @Convert(converter = Cripto.class)
    @Column(name = "enderecoCifTutor")
    private String endereco;

    /**
     * Converte String (Java) <-> bytes criptografados (coluna varbinary).
     * A chave vem da variável de ambiente ESTACAO_CHAVE (nunca fica no código).
     * Formato gravado: [IV 12 bytes][texto cifrado + etiqueta GCM].
     */
    @Converter
    public static class Cripto implements AttributeConverter<String, byte[]> {
        private static final SecureRandom ALEATORIO = new SecureRandom();
        private static final int TAM_IV = 12;
        private static volatile SecretKeySpec chaveAes;
        private static volatile SecretKeySpec chaveHmac;

        /** Chamado na subida da aplicação: se a variável não existir, o sistema nem inicia. */
        public static void validarChave() {
            carregar();
        }

        private static void carregar() {
            if (chaveAes != null && chaveHmac != null) {
                return;
            }
            String segredo = System.getenv("ESTACAO_CHAVE");
            if (segredo == null || segredo.isBlank()) {
                throw new IllegalStateException(
                        "Variável de ambiente ESTACAO_CHAVE não definida (chave de criptografia).");
            }
            chaveAes = new SecretKeySpec(derivar(segredo, "aes"), "AES");
            chaveHmac = new SecretKeySpec(derivar(segredo, "hmac"), "HmacSHA256");
        }

        private static byte[] derivar(String segredo, String uso) {
            try {
                return MessageDigest.getInstance("SHA-256")
                        .digest((segredo + "|" + uso).getBytes(StandardCharsets.UTF_8));
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Falha ao preparar a chave.", e);
            }
        }

        /** Hash determinístico (para buscar CPF sem expor o valor). */
        public static String hashBusca(String valor) {
            try {
                carregar();
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(chaveHmac);
                return HexFormat.of().formatHex(mac.doFinal(valor.getBytes(StandardCharsets.UTF_8)));
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Falha ao gerar o hash de busca.", e);
            }
        }

        @Override
        public byte[] convertToDatabaseColumn(String texto) {
            if (texto == null) {
                return null;
            }
            try {
                carregar();
                byte[] iv = new byte[TAM_IV];
                ALEATORIO.nextBytes(iv);
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.ENCRYPT_MODE, chaveAes, new GCMParameterSpec(128, iv));
                byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
                byte[] saida = new byte[iv.length + cifrado.length];
                System.arraycopy(iv, 0, saida, 0, iv.length);
                System.arraycopy(cifrado, 0, saida, iv.length, cifrado.length);
                return saida;
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Falha ao criptografar o dado.", e);
            }
        }

        @Override
        public String convertToEntityAttribute(byte[] dados) {
            if (dados == null) {
                return null;
            }
            try {
                carregar();
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, chaveAes, new GCMParameterSpec(128, dados, 0, TAM_IV));
                byte[] claro = cipher.doFinal(dados, TAM_IV, dados.length - TAM_IV);
                return new String(claro, StandardCharsets.UTF_8);
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException(
                        "Falha ao descriptografar o dado (a chave ESTACAO_CHAVE é a mesma usada ao gravar?).", e);
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCpfHash() {
        return cpfHash;
    }

    public void setCpfHash(String cpfHash) {
        this.cpfHash = cpfHash;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}

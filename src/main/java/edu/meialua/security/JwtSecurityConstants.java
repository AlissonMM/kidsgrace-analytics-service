package edu.meialua.security;

/**
 * Precisa ficar EXATAMENTE igual a
 * edu.meialua.morkstore.security.SecurityConstants (loja-brinquedos-api) —
 * este serviço não emite token, só valida o que a API principal já emitiu.
 * Se o segredo mudar lá, muda aqui também.
 */
public final class JwtSecurityConstants {

    public static final String JWT_SECRET = "kgbNAOentreAquipOrFaVOrNAoEAcIAneMACHinarenatodeusdaFATEC";

    private JwtSecurityConstants() {
    }
}

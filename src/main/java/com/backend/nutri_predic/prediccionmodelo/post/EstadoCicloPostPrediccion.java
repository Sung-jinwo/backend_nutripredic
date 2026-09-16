package com.backend.nutri_predic.prediccionmodelo.post;

public record EstadoCicloPostPrediccion(
        String pccIa,
        String pcs,
        boolean completo,
        String moduloFallo,
        String motivoFallo) {
    public static EstadoCicloPostPrediccion completo(String pccIa, String pcs) {
        return new EstadoCicloPostPrediccion(pccIa, pcs, true, null, null);
    }

    public static EstadoCicloPostPrediccion fallido(
            String pccIa, String pcs, String modulo, String motivo) {
        return new EstadoCicloPostPrediccion(pccIa, pcs, false, modulo, motivo);
    }
}

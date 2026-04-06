package com.enterprise.ong_pet2.model.dto.financeiro;

import com.enterprise.ong_pet2.enums.TipoDoacao;

import java.math.BigDecimal;
import java.util.Map;

public record ResumoFinanceiroDTO(
        BigDecimal totalDoacoes,
        BigDecimal totalDespesas,
        BigDecimal saldo,
        Map<TipoDoacao, BigDecimal> doacoesPorCategoria,
        long quantidadeDoacoes,
        long quantidadeDespesas
) {}
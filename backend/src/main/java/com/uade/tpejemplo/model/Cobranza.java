package com.uade.tpejemplo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cobranzas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cobranza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "id_credito", referencedColumnName = "id_credito"),
        @JoinColumn(name = "id_cuota",   referencedColumnName = "id_cuota")
    })
    private Cuota cuota;

    @NotNull
    @Column(name = "importe", nullable = false, precision = 12, scale = 2)
    private BigDecimal importe;

    // CAMBIO: metadatos para controlar anulación solo de cobranzas del día y mantener historial.
    @Column(name = "fecha_cobranza", nullable = false)
    private LocalDate fechaCobranza;

    @Column(nullable = false)
    private boolean anulada = false;

    public Cobranza(Long id, Cuota cuota, BigDecimal importe) {
        this(id, cuota, importe, LocalDate.now(), false);
    }
}

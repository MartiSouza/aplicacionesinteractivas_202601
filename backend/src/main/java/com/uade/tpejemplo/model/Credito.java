package com.uade.tpejemplo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "creditos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Credito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dni_cliente", nullable = false)
    private Cliente cliente;

    @NotNull
    @Column(name = "deuda_original", nullable = false, precision = 12, scale = 2)
    private BigDecimal deudaOriginal;

    @NotNull
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull
    @Column(name = "importe_cuota", nullable = false, precision = 12, scale = 2)
    private BigDecimal importeCuota;

    @Min(1)
    @Column(name = "cantidad_cuotas", nullable = false)
    private Integer cantidadCuotas;

    // CAMBIO: baja lógica para anulación de créditos sin eliminar el registro.
    @Column(nullable = false)
    private boolean anulado = false;

    @OneToMany(mappedBy = "credito", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cuota> cuotas;

    public Credito(Long id,
                   Cliente cliente,
                   BigDecimal deudaOriginal,
                   LocalDate fecha,
                   BigDecimal importeCuota,
                   Integer cantidadCuotas,
                   List<Cuota> cuotas) {
        this(id, cliente, deudaOriginal, fecha, importeCuota, cantidadCuotas, false, cuotas);
    }
}

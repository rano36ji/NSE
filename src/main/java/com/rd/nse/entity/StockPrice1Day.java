package com.rd.nse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_prices_1day")
public class StockPrice1Day  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;

    @Column(nullable = false)
    private LocalDateTime timestamp;  // The partitioning column

    private Double open;
    private Double high;
    private Double low;
    private Double close;
    private Long volume;

    // Getters and setters
}
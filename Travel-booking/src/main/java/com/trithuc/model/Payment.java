package com.trithuc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "payment")
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    private LocalDate date;

    private Long total;

    @OneToOne
    @JoinColumn(name = "yourBooking_id")
    private YourBooking yourBooking;
}

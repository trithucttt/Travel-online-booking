package com.trithuc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class PostTour implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @OneToMany(mappedBy = "postTour", cascade = CascadeType.ALL)
    private List<Comment> comments ;

    @OneToMany(mappedBy = "postTour", cascade = CascadeType.ALL)
    private List<tourbooking_item> tourbooking_items;

    private Integer quantity;
    private Double discount;
    private  LocalDateTime startTimeTour;
    private LocalDateTime endTimeTour;
}

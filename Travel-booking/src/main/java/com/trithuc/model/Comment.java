package com.trithuc.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comment")
public class Comment implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    // user comment

    private Long id;
    private String content;
    private LocalDateTime start_time;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // Người dùng tạo comment

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Comment parent; // Comment gốc mà reply này thuộc về

    @OneToMany(mappedBy = "parent" ,fetch = FetchType.LAZY)
    private Set<Comment> replies = new HashSet<>(); // Các reply cho comment này

    @ManyToOne
    @JoinColumn(name = "post_tour_id")
    private PostTour postTour;


}





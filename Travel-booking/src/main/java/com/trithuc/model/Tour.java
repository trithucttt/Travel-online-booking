package com.trithuc.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "tour")
@AllArgsConstructor
//@NoArgsConstructor(access=AccessLevel.PRIVATE, force=true)
@NoArgsConstructor
public class Tour implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String title;
    @Lob
    @Column(length = 1000)
    private String description;
    private Double price;
    @ManyToOne
    @JoinColumn(name = "manager_id")
    private User manager;

    @ManyToMany
    @JoinTable(
            name = "destination_in_tour",
            joinColumns = @JoinColumn(name = "tour_id"),
            inverseJoinColumns = @JoinColumn(name = "destination_id")
    )
    private Set<Destination> destination = new HashSet<>();


    @OneToMany(mappedBy = "tour")
    private Set<PostTour> posts = new HashSet<>();
    private String image_tour;


//    @ManyToMany(mappedBy = "tours")
//    private Set<Post> posts = new HashSet<>();

//    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL)
//    private List<Image> images;


//    private Long quantity;
    //	@ManyToMany(mappedBy = "tours", cascade = CascadeType.ALL)
//	private Set<Event> events = new HashSet<>();
//    @OneToMany(mappedBy = "tour",cascade = CascadeType.ALL)
//    private Set<Comment> comments = new HashSet<>();

}

package com.trithuc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "user")
@AllArgsConstructor
@NoArgsConstructor(access=AccessLevel.PUBLIC, force=true)
public class User implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	private String username;
	private String firstname;
	private String lastname;
	private String email;
	private String password;
	private String profileImage;
	private String address;
	
	@Enumerated(EnumType.STRING)
	private Role role;
	
	@OneToMany(mappedBy = "users")
	@JsonManagedReference
	private List<Post> posts;
	
	@OneToMany(mappedBy = "user")
	private List<YourBooking> yourBookings;

	@OneToMany(mappedBy = "manager")
	private Set<Tour> managedTour;

	@OneToMany(mappedBy = "manager")
	private Set<Destination> managedDestination;

	@OneToMany(mappedBy = "user")
	private List<Comment> comments = new ArrayList<>(); // Các comment của người dùng

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
	@JsonIgnore
	private List<CartItems> cartItems = new ArrayList<>();
}

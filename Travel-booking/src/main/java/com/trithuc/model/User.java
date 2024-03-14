package com.trithuc.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.hibernate.bytecode.internal.bytebuddy.PrivateAccessorException;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;

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
	private Set<Comment> comments; // Các comment của người dùng
	
	
}

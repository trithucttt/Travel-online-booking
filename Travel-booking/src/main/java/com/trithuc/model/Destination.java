package com.trithuc.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "destination")
public class Destination implements Serializable{
// điểm đến của các tour
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)

	private Long id;
	private String name;
	// địa chỉ cụ thể của điểm đến
	private String address;
	
//	 quận chứa địa chỉ của điểm đến
	@ManyToOne
	@JoinColumn(name = "ward_id")
	private Ward ward;

	@ManyToOne
	@JoinColumn(name = "manager_id")
	private User manager;

	
	// 1 tour có nhiều điểm đến và 1 điểm đến có thể thuộc nhiều tour
	@ManyToMany(mappedBy = "destination")
	private Set<Tour> tours = new HashSet<>();

//	@ManyToMany
	private String image_destination;
}

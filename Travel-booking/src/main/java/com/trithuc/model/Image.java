package com.trithuc.model;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "image")
public class Image implements Serializable{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 private Long id;
	private String entityType;// loại ảnh
	private Long entityId;//id của loại

	
	private String imageData;
 	
//	@ManyToOne
//	@JoinColumn(name = "tour_id")
//	private Tour tour;
	
//	@ManyToOne
//	@JoinColumn(name = "post_id")
//	private Post post;
	
	
}

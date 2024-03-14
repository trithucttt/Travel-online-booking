package com.trithuc.service;

import com.trithuc.dto.DestinationDto;
import com.trithuc.dto.PostDto;
import com.trithuc.dto.TourDto;
import com.trithuc.model.Destination;
import com.trithuc.model.Post;
import com.trithuc.model.Tour;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface TravelContentService {
    List<PostDto> getAllPost();
    PostDto getDetailPost(Long postId);

    Post getPostById(Long postId);

    TourDto getDetailTour(Long tourId);

    TourDto DetailTourFolloPost(Long tourId, Long postID);

    List<PostDto> convertListPost(List<Post> posts);

    List<TourDto> convertTour(List<Tour> tours);

    List<DestinationDto> convertDestination(List<Destination> destinations);

    List<TourDto> getTourByUser(Long userId);

    List<TourDto> getTourByTokenUser(String token);

    List<DestinationDto> getDestinationByUser(Long userId);

    Resource getImageTour(Long tourId);

    Resource getImageDestination(Long desId);

    Resource loadImagePost(String imageName);

    public List<PostDto> searchByName(String name, LocalDateTime startTime, LocalDateTime endTime);

    List<PostDto> sortByPrice();

    List<PostDto> sortByTitle();

    List<PostDto> getPostByUser(Long useId);
}

package com.nit.review;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingSummary {

	private double averageRating;
	private int totalReview;
	private int fivestar;
	private int fourStar;
	private int threeStar;
	private int twoStar;
	private int oneStar;
	
}

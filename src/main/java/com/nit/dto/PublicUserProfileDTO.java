
package com.nit.dto;

public class PublicUserProfileDTO {

    private Long id;
    private String name;
    private String bio;
    private String country;
    private String avatarUrl;

    public PublicUserProfileDTO() {
    }

    public PublicUserProfileDTO(
            Long id,
            String name,
            String bio,
            String country,
            String avatarUrl) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.country = country;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBio() {
        return bio;
    }

    public String getCountry() {
        return country;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
}
package com.nit.dto;

public class UserProfileDTO {

    private Long id;
    private String name;
    private String email;
    private String bio;
    private String country;
    private Boolean profilePublic;
    private String avatarUrl;

    public UserProfileDTO() {
    }

    public UserProfileDTO(
            Long id,
            String name,
            String email,
            String bio,
            String country,
            Boolean profilePublic,
            String avatarUrl) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.bio = bio;
        this.country = country;
        this.profilePublic = profilePublic;
        this.avatarUrl = avatarUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Boolean getProfilePublic() {
        return profilePublic;
    }

    public void setProfilePublic(Boolean profilePublic) {
        this.profilePublic = profilePublic;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}



package com.nit.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private String name;
	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email address")
	private String email;
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank(message = "Password is required")
	@Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
	private String password;
	private String role = "USER";
	private String status = "ACTIVE";
	@Column(length = 500)
	private String bio;
	@Column(length = 100)
	private String country;
	@Column(name = "PROFILE_PUBLIC", nullable = false)
	private Boolean profilePublic = true;
	@Column(name = "AVATAR_URL", length = 500)
	private String avatarUrl;
	public User() {
	}
	public User(String name, String email, String password) {
		this.name = name;
		this.email = email;
		this.password = password;
		this.role = "USER";
		this.status = "ACTIVE";
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
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
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
	@Override
	public String toString() {
		return "User{" + "id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + ", role='" + role + '\''
				+ ", status='" + status + '\'' + ", bio='" + bio + '\'' + ", country='" + country + '\''
				+ ", profilePublic=" + profilePublic + ", avatarUrl='" + avatarUrl + '\'' + '}';
	}
}

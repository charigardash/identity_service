package com.learning.dbentity.identity;

import com.learning.enums.OAuth2ProviderEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users", uniqueConstraints = {
@UniqueConstraint(columnNames = "userName"), @UniqueConstraint(columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String userName;

    @NotBlank
    @Size(max = 100)
    @Email
    private String email;

    @NotBlank
    @Size(max = 255)
    private String password;

    private Boolean enabled = true;

    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private OAuth2ProviderEnum provider = OAuth2ProviderEnum.LOCAL;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "email_verified")
    private Boolean emailVerified = Boolean.FALSE;


    // Constructor for local users
    public User(String userName, String email, String password){
        this.userName = userName;
        this.email = email;
        this.password = password;
        createdAt = LocalDateTime.now();
    }

    // Constructor for OAuth2 users
    public User(String username, String email, OAuth2ProviderEnum provider, String providerId) {
        this.userName = username;
        this.email = email;
        this.provider = provider;
        this.providerId = providerId;
        this.password = null; // OAuth2 users don't have passwords
    }
}

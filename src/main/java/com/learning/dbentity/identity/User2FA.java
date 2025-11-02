package com.learning.dbentity.identity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name =  "user_2fa")
@Data
public class User2FA {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;
    @Column(length = 500)
    private String secretKey;

    @Column(nullable = false)
    private Boolean enabled = false;

    @Column(columnDefinition = "TEXT")
    private String backupCodes; //json array of backup codes

    private Instant createdAt;

    private Instant updatedAt;

    @PrePersist
    protected void onCreate(){
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = Instant.now();
    }

    public User2FA() {}

    public User2FA(User user) {
        this.user = user;
    }
}

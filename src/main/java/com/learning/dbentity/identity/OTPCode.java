package com.learning.dbentity.identity;

import com.learning.enums.OtpTypeEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "otp_codes")
@Data
public class OTPCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OtpTypeEnum type;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Boolean used = false;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = Instant.now();
    }

    public OTPCode() {}

    public OTPCode(User user, String code, OtpTypeEnum type, Instant expiresAt) {
        this.user = user;
        this.code = code;
        this.type = type;
        this.expiresAt = expiresAt;
    }
}

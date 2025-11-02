package com.learning.dbentity.identity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "trusted_devices")
@Data
public class TrustedDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false, length = 100)
    private String deviceId;

    @Column(nullable = false, length = 100)
    private String deviceName;

    @Column
    private Instant lastUsed;

    @Column
    private Instant createdAt;

    @PrePersist
    protected void onCreate(){
        createdAt = Instant.now();
        lastUsed = Instant.now();
    }
    @PreUpdate
    protected void onUpdate(){
        lastUsed = Instant.now();
    }

    public TrustedDevice() {}

    public TrustedDevice(User user, String deviceId, String deviceName) {
        this.user = user;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }
}

package com.infotact.fleet.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "fleet_users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username")
})
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 100)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(name = "contact_number", length = 30)
    private String contactNumber;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AppUser() {
        // Required by JPA
    }

    public AppUser(String username, String password, Role role, String name, String email, String contactNumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void updateProfile(String name, String email, String contactNumber, Role role) {
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
        this.role = role;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getContactNumber() { return contactNumber; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}

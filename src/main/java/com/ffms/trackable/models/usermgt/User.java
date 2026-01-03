package com.ffms.trackable.models.usermgt;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.trackable.common.model.AuditEntity;
import com.ffms.trackable.common.model.Status;
import com.ffms.trackable.enums.usermgt.UserType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "username"),
                @UniqueConstraint(columnNames = "email")
        })
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditEntity {

    @Column(name = "userName", columnDefinition = "VARCHAR(50)", unique = true, updatable = false)
    @JsonProperty("user_name")
    private String userName;

    @Column(name = "firstName", columnDefinition = "VARCHAR(50)")
    @JsonProperty("first_name")
    private String firstName;

    @Column(name = "lastName", columnDefinition = "VARCHAR(50)")
    @JsonProperty("last_name")
    private String lastName;

    @Column(name = "address", columnDefinition = "VARCHAR(300)")
    @JsonProperty("address")
    private String address;

    @Column(name = "phone", columnDefinition = "VARCHAR(11)")
    @JsonProperty("phone")
    private String phone;

    @Column(name = "fax", columnDefinition = "VARCHAR(11)")
    @JsonProperty("fax")
    private String fax;

    @Column(name = "status", columnDefinition = "VARCHAR(10)")
    @JsonProperty("status")
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @NotBlank
    @Column(name = "email", columnDefinition = "VARCHAR(50)", unique = true)
    @JsonProperty("email")
    private String email;

    @Size(max = 120)
    @Column(name = "password")
    private String password;

    @Column(name = "oauth2_provider", columnDefinition = "VARCHAR(20)")
    @JsonProperty("oauth2_provider")
    private String oauth2Provider;

    @Column(name = "oauth2_provider_id", columnDefinition = "VARCHAR(100)")
    @JsonProperty("oauth2_provider_id")
    private String oauth2ProviderId;

    @Column(name = "type", columnDefinition = "VARCHAR(20)")
    @JsonProperty("type")
    @Enumerated(EnumType.STRING)
    private UserType type = UserType.USER;

    @ManyToOne
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "role_id")
    @JsonBackReference
    private Role role;
}

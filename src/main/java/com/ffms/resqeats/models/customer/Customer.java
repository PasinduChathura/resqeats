package com.ffms.resqeats.models.customer;

import com.ffms.resqeats.common.model.AuditEntity;
import com.ffms.resqeats.common.model.Status;
import com.ffms.resqeats.enums.customer.CustomerType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

@Table(name = "customer",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name", "address"}),
                @UniqueConstraint(columnNames = "ref_id")
        }
)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends AuditEntity {
    @Column(name = "ref_id", columnDefinition = "VARCHAR(50)", nullable = false, unique = true)
    private String refId;

    @Column(name = "name", columnDefinition = "VARCHAR(50)", nullable = false)
    private String name;

    @Column(name = "address", columnDefinition = "VARCHAR(500)", nullable = false)
    private String address;

    @Column(name = "district", columnDefinition = "VARCHAR(65)", nullable = false)
    private String district;

    @Column(name = "province", columnDefinition = "VARCHAR(65)", nullable = false)
    private String province;

    @Column(name = "join_date", columnDefinition = "DATETIME")
    private Date joinDate;

    @Column(name = "phone", columnDefinition = "VARCHAR(11)", nullable = false)
    private String phone;

    @Column(name = "fax", columnDefinition = "VARCHAR(11)")
    private String fax;

    @Column(name = "email", columnDefinition = "VARCHAR(50)", nullable = false)
    private String email;

    @Column(name = "person", columnDefinition = "VARCHAR(100)", nullable = false)
    private String person;

    @Column(name = "person_designation", columnDefinition = "VARCHAR(100)")
    private String personDesignation;

    @Column(name = "latitude", columnDefinition = "DOUBLE", nullable = false)
    private double latitude;

    @Column(name = "longitude", columnDefinition = "DOUBLE", nullable = false)
    private double longitude;

    @Column(name = "status", columnDefinition = "VARCHAR(10)", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    @Column(name = "type", columnDefinition = "VARCHAR(20)", nullable = false)
    @Enumerated(EnumType.STRING)
    private CustomerType type = CustomerType.PARENT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Customer parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Customer> children;
}

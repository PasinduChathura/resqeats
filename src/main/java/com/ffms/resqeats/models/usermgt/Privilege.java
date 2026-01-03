package com.ffms.resqeats.models.usermgt;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ffms.resqeats.common.model.BaseEntity;
import com.ffms.resqeats.enums.usermgt.PrivilegeType;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Privilege extends BaseEntity {

    @ManyToMany(mappedBy = "privileges", fetch = FetchType.LAZY)
    @JsonBackReference
    private Set<Role> roles;

    @Column(columnDefinition = "VARCHAR(150)", unique = true)
    private String name;

    @Column(name = "type", columnDefinition = "VARCHAR(20)", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PrivilegeType type = PrivilegeType.USER;

    public Privilege(Long id) {
        this.setId(id);
    }
}

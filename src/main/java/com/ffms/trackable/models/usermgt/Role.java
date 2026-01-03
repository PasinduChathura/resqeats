package com.ffms.trackable.models.usermgt;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ffms.trackable.common.model.BaseEntity;
import com.ffms.trackable.enums.usermgt.RoleType;
import com.ffms.trackable.models.master.WorkflowNode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "roles")
public class Role extends BaseEntity {
    @Column(name = "type", columnDefinition = "VARCHAR(20)")
    @JsonProperty("type")
    @Enumerated(EnumType.STRING)
    private RoleType type = RoleType.USER;

    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<User> users;

    @ManyToMany
    @JoinTable(name = "roles_privileges", joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "privilege_id", referencedColumnName = "id"))
    private Set<Privilege> privileges;

    @ManyToMany(mappedBy = "roles")
    private Set<WorkflowNode> workflowStages;
}
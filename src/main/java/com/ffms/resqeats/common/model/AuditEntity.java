package com.ffms.resqeats.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Audited
public class AuditEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", columnDefinition = "DATETIME  DEFAULT CURRENT_TIMESTAMP", updatable = false)
    @CreationTimestamp
    @JsonProperty("created_at")
    private Date createdAt;

    @Column(name = "created_by", columnDefinition = "VARCHAR(30) DEFAULT 'system'", updatable = false)
    @JsonProperty("created_by")
    private String createdBy = "system";

    @Column(name = "updated_at", columnDefinition = "DATETIME  DEFAULT CURRENT_TIMESTAMP")
    @UpdateTimestamp
    @JsonProperty("updated_at")
    private Date updatedAt;

    @Column(name = "updated_by", columnDefinition = "VARCHAR(30) DEFAULT 'system'")
    @JsonProperty("updated_by")
    private String updatedBy = "system";

    public AuditEntity() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}

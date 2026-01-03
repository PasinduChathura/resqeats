package com.ffms.trackable.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.annotations.Parameter;

import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
@Audited
public class AuditEntity implements Serializable {

    @Id
   // @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @GeneratedValue(generator = "sequenceIdGenerator")
    @GenericGenerator(
            name = "sequenceIdGenerator", 
            strategy = "sequence",
            parameters = @Parameter (
                    name = SequenceStyleGenerator.CONFIG_SEQUENCE_PER_ENTITY_SUFFIX,
                    value = "_seq"))
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

package com.ffms.trackable.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;


@MappedSuperclass
@Audited
public class BaseEntity extends AuditEntity {

    @Column(columnDefinition = "VARCHAR(150)")
    private String name;

    @Column(columnDefinition = "VARCHAR(150)")
    @NotAudited
    private String label;

    @Column(columnDefinition = "VARCHAR(500)")
    private String description;

    @Column(columnDefinition = "VARCHAR(1000)")
    @NotAudited
    private String metadata;

    @Column(name = "is_deprecated", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @JsonProperty("is_deprecated")
    private Boolean isDeprecated = false;

    public BaseEntity() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Boolean getIsDeprecated() {
        return isDeprecated;
    }

    public void setIsDeprecated(Boolean isDeprecated) {
        this.isDeprecated = isDeprecated;
    }
}

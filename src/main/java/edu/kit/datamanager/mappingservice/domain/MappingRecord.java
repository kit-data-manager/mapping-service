/*
 * Copyright 2019 Karlsruhe Institute of Technology.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.kit.datamanager.mappingservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.kit.datamanager.entities.EtagSupport;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.springframework.http.MediaType;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author jejkal
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class MappingRecord implements EtagSupport, Serializable {
    public final static MediaType MAPPING_RECORD_MEDIA_TYPE = MediaType.valueOf("application/vnd.datamanager.mapping-record+json");

    @Id
    @NotNull(message = "The unique identify of the record.")
    private String mappingId;

    @NotNull(message = "Type of the mapping, e.g. GEMMA, XSLT, handlebars, ....")
    private String mappingType;

    @NotNull(message = "Title of the mapping.")
    private String title;

    @NotNull(message = "Description of the mapping.")
    private String description;

    @NotNull(message = "A list of access control entries for restricting access.")
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private final Set<AclEntry> acl = new HashSet<>();

    @NotNull(message = "The metadata document uri, e.g. pointing to a local file.")
    private String mappingDocumentUri;

    @NotNull(message = "The SHA-256 hash of the associated metadata file. The hash is used for comparison while updating.")
    private String documentHash;

    /**
     * Set new access control list.
     *
     * @param newAclList new list with acl.
     */
    public void setAcl(Set<AclEntry> newAclList) {
        acl.clear();
        acl.addAll(newAclList);
    }

    @Override
    @JsonIgnore
    public String getEtag() {
        return Integer.toString(hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        MappingRecord that = (MappingRecord) o;
        return mappingId != null && Objects.equals(mappingId, that.mappingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mappingId);
    }
}

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
package edu.kit.datamanager.indexer.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.kit.datamanager.entities.EtagSupport;
import edu.kit.datamanager.indexer.domain.acl.AclEntry;
import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.http.MediaType;

/**
 *
 * @author jejkal
 */
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@IdClass(CompositeKey.class)
@Data
public class MappingRecord implements EtagSupport, Serializable {
  public final static MediaType MAPPING_RECORD_MEDIA_TYPE = MediaType.valueOf("application/vnd.datamanager.mapping-record+json");

  @Id
  @NotBlank(message = "The unique identify of the record.")
  private String mappingId;
  @Id
  @NotBlank(message = "Type of the mapping, e.g. GEMMA, XSLT, handlebars, ....")
  private String mappingType;
  @NotNull(message = "A list of access control entries for resticting access.")
  @OneToMany(cascade = javax.persistence.CascadeType.ALL, orphanRemoval = true)
   private final Set<AclEntry> acl = new HashSet<>();
  @NotBlank(message = "The metadata document uri, e.g. pointing to a local file.")
  private String mappingDocumentUri;
  @NotBlank(message = "The SHA-1 hash of the associated metadata file. The hash is used for comparison while updating.")
  private String documentHash;

  /**
   * Set new access control list.
   * @param newAclList new list with acls.
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
}

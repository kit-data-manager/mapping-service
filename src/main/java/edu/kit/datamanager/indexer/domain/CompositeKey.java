/*
 * Copyright 2020 hartmann-v.
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

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key for MappingRecord
 */
public class CompositeKey implements Serializable {
  String mappingId;
  String mappingType; 

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 59 * hash + Objects.hashCode(this.mappingId);
    hash = 59 * hash + Objects.hashCode(this.mappingType);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CompositeKey other = (CompositeKey) obj;
    if (!Objects.equals(this.mappingId, other.mappingId)) {
      return false;
    }
    if (!Objects.equals(this.mappingType, other.mappingType)) {
      return false;
    }
    return true;
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.indexer.dao;

import edu.kit.datamanager.indexer.domain.MappingRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 */
public interface IMappingRecordDao extends JpaRepository<MappingRecord, String>, JpaSpecificationExecutor<MappingRecord>{
  Optional<MappingRecord> findByMappingIdAndMappingType(String mappingId, String mappingType);
  Iterable<MappingRecord> findByMappingIdInOrMappingTypeIn(List<String> mappingId, List<String> mappingType);
  Page<MappingRecord> findByMappingIdInOrMappingTypeIn(List<String> mappingId, List<String> mappingType, Pageable pgbl);
  
}

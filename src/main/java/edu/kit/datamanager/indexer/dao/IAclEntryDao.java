/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.kit.datamanager.indexer.dao;

import edu.kit.datamanager.indexer.domain.acl.AclEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 */
public interface IAclEntryDao extends JpaRepository<AclEntry, String>, JpaSpecificationExecutor<AclEntry> {
}

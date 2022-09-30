/*
 * Copyright 2022 Karlsruhe Institute of Technology.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.kit.datamanager.mappingservice.domain;

import edu.kit.datamanager.entities.PERMISSION;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MappingRecordTest {
    
    private MappingRecord record;
    
    @BeforeEach
    void setUp() {
        record = new MappingRecord();
        record.setMappingId("testID");
        record.setMappingType("testType");
        record.setTitle("testTitle");
        record.setDescription("testDescription");
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(test);
        record.setAcl(aclEntries);
        record.setMappingDocumentUri("testUri");
        record.setDocumentHash("testHash");
    }

    @Test
    void setAcl() {
        MappingRecord testRecord = new MappingRecord();
        assertNotEquals(record, testRecord);
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(test);
        testRecord.setAcl(aclEntries);
        assertEquals(testRecord.getAcl(), record.getAcl());
    }

    @Test
    void getEtag() {
        assertEquals(Integer.toString(Objects.hash(record.getMappingId())), record.getEtag());
    }

    @Test
    void testEquals() {
        assertEquals(record, record);
        assertNotEquals(record, null);
        assertNotEquals(record, new Object());
        MappingRecord testRecord = new MappingRecord();
        assertNotEquals(record, testRecord);
        testRecord.setMappingId("testID");
        testRecord.setMappingType("testType");
        testRecord.setTitle("testTitle");
        testRecord.setDescription("testDescription");
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(test);
        testRecord.setAcl(aclEntries);
        assertEquals(testRecord, record);
    }

    @Test
    void testHashCode() {
        MappingRecord testRecord = new MappingRecord();
        assertNotEquals(record.hashCode(), testRecord.hashCode());
        testRecord.setMappingId("testID");
        testRecord.setMappingType("testType");
        testRecord.setTitle("testTitle");
        testRecord.setDescription("testDescription");
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(test);
        testRecord.setAcl(aclEntries);
        assertEquals(testRecord.hashCode(), record.hashCode());
    }

    @Test
    void getMappingId() {
        assertNotNull(record.getMappingId());
        assertEquals("testID", record.getMappingId());
    }

    @Test
    void getMappingType() {
        assertNotNull(record.getMappingType());
        assertEquals("testType", record.getMappingType());
    }

    @Test
    void getTitle() {
        assertNotNull(record.getTitle());
        assertEquals("testTitle", record.getTitle());
    }

    @Test
    void getDescription() {
        assertNotNull(record.getDescription());
        assertEquals("testDescription", record.getDescription());
    }

    @Test
    void getAcl() {
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(test);
        assertEquals(aclEntries, record.getAcl());
    }

    @Test
    void getMappingDocumentUri() {
        assertNotNull(record.getMappingDocumentUri());
        assertEquals("testUri", record.getMappingDocumentUri());
    }

    @Test
    void getDocumentHash() {
        assertNotNull(record.getDocumentHash());
        assertEquals("testHash", record.getDocumentHash());
    }

    @Test
    void setMappingId() {
        MappingRecord testRecord = new MappingRecord();
        System.out.println(record);
        testRecord.setMappingId("testID");
        assertEquals(testRecord.getMappingId(), record.getMappingId());
        assertEquals(record, testRecord); // Special case, because the ID can only be set once
    }

    @Test
    void setMappingType() {
        MappingRecord testRecord = new MappingRecord();
        assertNotEquals(record, testRecord);
        testRecord.setMappingType("testType");
        assertEquals(testRecord.getMappingType(), record.getMappingType());
        assertNotEquals(testRecord, record);
    }

    @Test
    void setTitle() {
        MappingRecord testRecord = new MappingRecord();
        assertNotEquals(record, testRecord);
        testRecord.setTitle("testTitle");
        assertEquals(testRecord.getTitle(), record.getTitle());
        assertNotEquals(testRecord, record);
    }

    @Test
    void setDescription() {
        MappingRecord testRecord = new MappingRecord();
        assertNotEquals(record, testRecord);
        testRecord.setDescription("testDescription");
        assertEquals(testRecord.getDescription(), record.getDescription());
        assertNotEquals(testRecord, record);
    }

    @Test
    void setMappingDocumentUri() {
        MappingRecord testRecord = new MappingRecord();
        assertNotEquals(record, testRecord);
        testRecord.setMappingDocumentUri("testUri");
        assertEquals(testRecord.getMappingDocumentUri(), record.getMappingDocumentUri());
        assertNotEquals(testRecord, record);
    }

    @Test
    void setDocumentHash() {
        MappingRecord testRecord = new MappingRecord();
        assertNotEquals(record, testRecord);
        testRecord.setDocumentHash("testHash");
        assertEquals(testRecord.getDocumentHash(), record.getDocumentHash());
        assertNotEquals(testRecord, record);
    }

    @Test
    void testToString() {
        assertNotNull(record.toString());
        MappingRecord testRecord = new MappingRecord();
        testRecord.setMappingId("testID");
        testRecord.setMappingType("testType");
        testRecord.setTitle("testTitle");
        testRecord.setDescription("testDescription");
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        Set<AclEntry> aclEntries = new HashSet<>();
        aclEntries.add(test);
        testRecord.setAcl(aclEntries);
        testRecord.setMappingDocumentUri("testUri");
        testRecord.setDocumentHash("testHash");
        assertEquals(testRecord.toString(), record.toString());
    }
}
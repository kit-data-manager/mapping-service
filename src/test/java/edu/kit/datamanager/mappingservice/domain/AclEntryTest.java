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

import static org.junit.jupiter.api.Assertions.*;

class AclEntryTest {

    private AclEntry aclEntry;
    private AclEntry aclEntry2;

    @BeforeEach
    public void setUp(){
        aclEntry = new AclEntry();
        aclEntry2 = new AclEntry();
        aclEntry2.setId(1L);
        aclEntry2.setSid("User1");
        aclEntry2.setPermission(PERMISSION.ADMINISTRATE);

    }

    @Test
    void testHashCode() {
        assertEquals(aclEntry.hashCode(), new AclEntry().hashCode());
        assertNotEquals(aclEntry.hashCode(), aclEntry2.hashCode());
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        assertEquals(aclEntry2.hashCode(), test.hashCode());
    }

    @Test
    void testEquals() {
        assertNotEquals(aclEntry, aclEntry2);
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        assertEquals(aclEntry2, test);
        test.setSid("User2");
        assertNotEquals(aclEntry2, test);
    }

    @Test
    void getId() {
        assertEquals(aclEntry.getId(), new AclEntry().getId());
        assertNotEquals(aclEntry.getId(), aclEntry2.getId());
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        assertEquals(aclEntry2.getId(), test.getId());
    }

    @Test
    void getSid() {
        assertEquals(aclEntry.getSid(), new AclEntry().getSid());
        assertNotEquals(aclEntry.getSid(), aclEntry2.getSid());
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        assertEquals(aclEntry2.getSid(), test.getSid());
    }

    @Test
    void getPermission() {
        assertEquals(aclEntry.getPermission(), new AclEntry().getPermission());
        assertNotEquals(aclEntry.getPermission(), aclEntry2.getPermission());
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        assertEquals(aclEntry2.getPermission(), test.getPermission());
    }

    @Test
    void setId() {
        assertEquals(aclEntry.getId(), new AclEntry().getId());
        aclEntry.setId(1L);
        assertNotEquals(aclEntry.getId(), new AclEntry().getId());
        assertEquals(aclEntry2.getId(), aclEntry.getId());
    }

    @Test
    void setSid() {
        assertEquals(aclEntry.getSid(), new AclEntry().getSid());
        aclEntry.setSid("User1");
        assertNotEquals(aclEntry.getSid(), new AclEntry().getSid());
        assertEquals(aclEntry2.getSid(), aclEntry.getSid());
    }

    @Test
    void setPermission() {
        assertEquals(aclEntry.getPermission(), new AclEntry().getPermission());
        aclEntry.setPermission(PERMISSION.ADMINISTRATE);
        assertNotEquals(aclEntry.getPermission(), new AclEntry().getPermission());
        assertEquals(aclEntry2.getPermission(), aclEntry.getPermission());
    }

    @Test
    void testToString() {
        assertEquals(aclEntry.toString(), new AclEntry().toString());
        assertNotEquals(aclEntry.toString(), aclEntry2.toString());
        AclEntry test = new AclEntry();
        test.setId(1L);
        test.setSid("User1");
        test.setPermission(PERMISSION.ADMINISTRATE);
        assertEquals(aclEntry2.toString(), test.toString());
    }
}
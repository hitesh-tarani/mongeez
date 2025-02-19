/*
 * Copyright 2011 SecondMarket Labs, LLC.
 * Copyright 2023 Hitesh Tarani
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.mongeez;

import static org.testng.Assert.assertEquals;

import com.mongodb.client.MongoClient;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.mongeez.validation.ValidationException;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class MongeezTest {
    private String dbName = "test_mongeez";
    private MongoClient mongoClient;
    private String mongoUri = "mongodb://localhost/"+dbName;
    private MongoDatabase db;

    @BeforeMethod
    protected void setUp() throws Exception {
        mongoClient = MongoClients.create();
        db = mongoClient.getDatabase(dbName);

        db.drop();
    }

    private Mongeez create(String path) {
        Mongeez mongeez = new Mongeez();
        mongeez.setFile(new ClassPathResource(path));
        mongeez.setMongoClient(mongoClient);
        mongeez.setMongoUri(mongoUri);
        mongeez.setMongeezCollectionDB(dbName);
        return mongeez;
    }

    @Test(groups = "dao")
    public void testMongeez() throws Exception {
        Mongeez mongeez = create("mongeez.xml");

        mongeez.process();

        assertEquals(db.getCollection("mongeez").countDocuments(), 5);

        assertEquals(db.getCollection("organization").countDocuments(), 2);
        assertEquals(db.getCollection("user").countDocuments(), 2);
    }

    @Test(groups = "dao")
    public void testRunTwice() throws Exception {
        testMongeez();
        testMongeez();
    }

    @Test(groups = "dao")
    public void testFailOnError_False() throws Exception {
        assertEquals(db.getCollection("mongeez").countDocuments(), 0);

        Mongeez mongeez = create("mongeez_fail.xml");
        mongeez.process();

        assertEquals(db.getCollection("mongeez").countDocuments(), 2);
    }

    @Test(groups = "dao", expectedExceptions = com.mongodb.MongoException.class)
    public void testFailOnError_True() throws Exception {
        Mongeez mongeez = create("mongeez_fail_fail.xml");
        mongeez.process();
    }

    @Test(groups = "dao")
    public void testNoFiles() throws Exception {
        Mongeez mongeez = create("mongeez_empty.xml");
        mongeez.process();

        assertEquals(db.getCollection("mongeez").countDocuments(), 1);
    }

    @Test(groups = "dao")
    public void testNoFailureOnEmptyChangeLog() throws Exception {
        assertEquals(db.getCollection("mongeez").countDocuments(), 0);

        Mongeez mongeez = create("mongeez_empty_changelog.xml");
        mongeez.process();

        assertEquals(db.getCollection("mongeez").countDocuments(), 1);
    }

    @Test(groups = "dao", expectedExceptions = ValidationException.class)
    public void testNoFailureOnNoChangeFilesBlock() throws Exception {
        assertEquals(db.getCollection("mongeez").countDocuments(), 0);

        Mongeez mongeez = create("mongeez_no_changefiles_declared.xml");
        mongeez.process();
    }

    @Test(groups = "dao")
    public void testChangesWContextContextNotSet() throws Exception {
        assertEquals(db.getCollection("mongeez").countDocuments(), 0);

        Mongeez mongeez = create("mongeez_contexts.xml");
        mongeez.process();
        assertEquals(db.getCollection("mongeez").countDocuments(), 2);
        assertEquals(db.getCollection("car").countDocuments(), 2);
        assertEquals(db.getCollection("user").countDocuments(), 0);
        assertEquals(db.getCollection("organization").countDocuments(), 0);
        assertEquals(db.getCollection("house").countDocuments(), 0);
    }

    @Test(groups = "dao")
    public void testChangesWContextContextSetToUsers() throws Exception {
        assertEquals(db.getCollection("mongeez").countDocuments(), 0);

        Mongeez mongeez = create("mongeez_contexts.xml");
        mongeez.setContext("users");
        mongeez.process();
        assertEquals(db.getCollection("mongeez").countDocuments(), 4);
        assertEquals(db.getCollection("car").countDocuments(), 2);
        assertEquals(db.getCollection("user").countDocuments(), 2);
        assertEquals(db.getCollection("organization").countDocuments(), 0);
        assertEquals(db.getCollection("house").countDocuments(), 2);
    }

    @Test(groups = "dao")
    public void testChangesWContextContextSetToOrganizations() throws Exception {
        assertEquals(db.getCollection("mongeez").countDocuments(), 0);

        Mongeez mongeez = create("mongeez_contexts.xml");
        mongeez.setContext("organizations");
        mongeez.process();
        assertEquals(db.getCollection("mongeez").countDocuments(), 4);
        assertEquals(db.getCollection("car").countDocuments(), 2);
        assertEquals(db.getCollection("user").countDocuments(), 0);
        assertEquals(db.getCollection("organization").countDocuments(), 2);
        assertEquals(db.getCollection("house").countDocuments(), 2);
    }

    @Test(groups = "dao", expectedExceptions = ValidationException.class)
    public void testFailDuplicateIds() throws Exception {
        Mongeez mongeez = create("mongeez_fail_on_duplicate_changeset_ids.xml");
        mongeez.process();
    }
}

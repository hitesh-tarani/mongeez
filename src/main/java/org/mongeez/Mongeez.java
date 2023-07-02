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

import com.mongodb.client.MongoClients;
import org.mongeez.commands.ChangeSet;
import org.mongeez.commands.Script;
import org.mongeez.reader.ChangeSetFileProvider;
import org.mongeez.reader.ChangeSetReaderFactory;
import org.mongeez.reader.FilesetXMLChangeSetFileProvider;
import org.mongeez.validation.ChangeSetsValidator;
import org.mongeez.validation.DefaultChangeSetsValidator;

import com.mongodb.client.MongoClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;


public class Mongeez {
    private final static Logger logger = LoggerFactory.getLogger(Mongeez.class);

    private String dbName;
    private ChangeSetFileProvider changeSetFileProvider = null;
    private ChangeSetsValidator changeSetsValidator = new DefaultChangeSetsValidator();
    private String context = null;
    private MongoClient mongoClient;
    private String mongoClientUri;

  public void executeAllChanges() {
        List<ChangeSet> changeSets = getChangeSets();
        new ChangeSetExecutor(mongoClient, mongoClientUri, context, dbName).execute(changeSets);
    }

    public void process() {
        List<ChangeSet> changeSets = getChangeSets();
        new ChangeSetExecutor(mongoClient, mongoClientUri, context, dbName).execute(changeSets);
    }

    public void verifyAllChanges() {
        getChangeSets();
    }

    private List<ChangeSet> getChangeSets() {
        List<Resource> files = changeSetFileProvider.getChangeSetFiles();
        List<ChangeSet> changeSets = new ArrayList<ChangeSet>();

        ChangeSetReaderFactory readerFactory = ChangeSetReaderFactory.getInstance();
        for (Resource file : files) {
            changeSets.addAll(readerFactory.getChangeSetReader(file).getChangeSets(file));
        }
        logChangeSets(changeSets);
        changeSetsValidator.validate(changeSets);
        return changeSets;
    }

    private void logChangeSets(List<ChangeSet> changeSets) {
        if (logger.isTraceEnabled()) {
            for (ChangeSet changeSet : changeSets) {
                logger.trace("Changeset");
                logger.trace("id: " + changeSet.getChangeId());
                logger.trace("author: " + changeSet.getAuthor());
                if (! "".equals(changeSet.getContexts())) {
                    logger.trace("contexts: {}", changeSet.getContexts());
                }
                for (Script command : changeSet.getCommands()) {
                    logger.trace("script");
                    logger.trace(command.getBody());
                }
            }
        }
    }

    public void setMongoClient(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }
  
    public void setMongoUri(String mongoUri) {
        this.mongoClient = MongoClients.create(mongoUri);
        this.mongoClientUri = mongoUri;
    }

    public void setMongeezCollectionDB(String dbName) {
        this.dbName = dbName;
    }

    public void setChangeSetsValidator(ChangeSetsValidator changeSetsValidator) {
        this.changeSetsValidator = changeSetsValidator;
    }

    /**
     * Convenience method to set the ChangeSetFileProvider to an XML fileset based on the specified file
     */
    public void setFile(Resource file) {
        setChangeSetFileProvider(new FilesetXMLChangeSetFileProvider(file));
    }

    public void setChangeSetFileProvider(ChangeSetFileProvider changeSetFileProvider) {
        this.changeSetFileProvider = changeSetFileProvider;
    }

    public void setContext(String context) {
        this.context = context;
    }

}

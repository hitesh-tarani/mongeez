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

import org.mongeez.commands.ChangeSet;
import org.mongeez.commands.Script;
import org.mongeez.dao.MongeezDao;

import com.mongodb.MongoClient;
import org.mongeez.dao.MongoShellScriptExecutor;
import org.mongeez.dao.ShellScriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class ChangeSetExecutor {
    private final Logger logger = LoggerFactory.getLogger(ChangeSetExecutor.class);

    private MongeezDao dao = null;
    private String context = null;

    /**
     * This executes scripts with a {@link org.mongeez.dao.MongoShellScriptExecutor} which needs
     * <a href="https://www.mongodb.com/docs/v4.4/mongo/">mongo shell client</a> executable available on the system path.
     * To use the alternative mongo shells, use {@link ChangeSetExecutor#ChangeSetExecutor(com.mongodb.MongoClient, java.lang.String, java.lang.String, org.mongeez.dao.ShellScriptExecutor)}
     */
    public ChangeSetExecutor(MongoClient mongo, String mongoClientUri, String context, String dbName) {
        dao = new MongeezDao(mongo, dbName, new MongoShellScriptExecutor(mongoClientUri));
        this.context = context;
    }

    /**
     * This executes scripts with the provided shell client executor
     * @see ShellScriptExecutor
     */
    public ChangeSetExecutor(MongoClient mongo, String context, String dbName, ShellScriptExecutor executor) {
        dao = new MongeezDao(mongo, dbName, executor);
        this.context = context;
    }

    public void execute(List<ChangeSet> changeSets) {
        for (ChangeSet changeSet : changeSets) {
            if (changeSet.canBeAppliedInContext(context)) {
                if (changeSet.isRunAlways() || !dao.wasExecuted(changeSet)) {
                    execute(changeSet);
                    logger.info("ChangeSet " + changeSet.getChangeId() + " has been executed");
                } else {
                    logger.info("ChangeSet already executed: " + changeSet.getChangeId());
                }
            }
            else {
                logger.info("Not executing Changeset {} it cannot run in the context {}", changeSet.getChangeId(), context);
            }
        }
    }

    private void execute(ChangeSet changeSet) {
        File changesetTempFile = null;
        try {
            for (Script command : changeSet.getCommands()) {
              changesetTempFile = File.createTempFile(getTempFilePath(changeSet), ".js");
              writeChangeSetBodyToFile(command.getBody(), changesetTempFile.getPath());
              dao.runScript(changesetTempFile.getAbsolutePath());
              deleteFile(changesetTempFile);
            }
        } catch (RuntimeException e) {
            deleteFile(changesetTempFile);
            if (changeSet.isFailOnError()) {
                throw e;
            } else {
                logger.warn("ChangeSet " + changeSet.getChangeId() + " has failed, but failOnError is set to false", e.getMessage());
            }
        } catch (IOException e) {
            deleteFile(changesetTempFile);
            if (changeSet.isFailOnError()) {
                throw new RuntimeException(e);
            } else {
                logger.warn("ChangeSet " + changeSet.getChangeId() + " has failed, but failOnError is set to false", e.getMessage());
            }
        }
        dao.logChangeSet(changeSet);
    }

    private String getTempFilePath(ChangeSet changeSet) {
      String resourcePath = changeSet.getResourcePath();
      String changeFilePrefix = resourcePath.substring(0, resourcePath.lastIndexOf("."));
      return changeFilePrefix + "." + changeSet.getChangeId();
    }

    private void writeChangeSetBodyToFile(String body, String filePath) {
        PrintWriter out = null;
  
        try {
          FileWriter fw = new FileWriter(filePath);
          out = new PrintWriter(fw);
          out.write(body);
        } catch (IOException e) {
            throw new RuntimeException("Unable to write to temporary changeset file" + filePath);
        } finally {
          if (out != null) {
            out.close();
          }
        }
      }

    private void deleteFile(File file) {
        try {
            file.delete();
        } catch (Exception e) {
            logger.error("Unable to delete temporary file" + file.getPath());
        }
    }
}

package org.mongeez.dao;

/**
 * This executes scripts with a <a href="https://www.mongodb.com/docs/v4.4/mongo">mongo shell client</a>
 */
public class MongoShellScriptExecutor extends AbstractMongoShellScriptExecutor implements ShellScriptExecutor {

  public MongoShellScriptExecutor(String mongoClientUri) {
    super(mongoClientUri, "mongo", "MongoDB server version");
  }

  public void shutdown() throws InterruptedException {
    sendCommand(mongoProcess, "exit");
    mongoProcess.waitFor();
  }
}

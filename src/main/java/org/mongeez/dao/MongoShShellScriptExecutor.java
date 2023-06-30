package org.mongeez.dao;

/**
 * This executes scripts with a <a href="https://www.mongodb.com/docs/mongodb-shell">mongosh shell client</a>
 */
public class MongoShShellScriptExecutor extends AbstractMongoShellScriptExecutor implements ShellScriptExecutor {
  public MongoShShellScriptExecutor(String mongoClientUri) {
    super(mongoClientUri, "mongosh", "You may want to copy or rename");
  }
}

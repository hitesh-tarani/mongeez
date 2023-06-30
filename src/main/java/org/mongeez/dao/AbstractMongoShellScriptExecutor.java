package org.mongeez.dao;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMongoShellScriptExecutor implements ShellScriptExecutor {
  private final Logger logger = LoggerFactory.getLogger(MongoShellScriptExecutor.class);

  protected Process mongoProcess;

   AbstractMongoShellScriptExecutor(String mongoClientUri, String mongoCommand, String terminalOutput) {
    ProcessBuilder pb = new ProcessBuilder();
    pb.redirectErrorStream(true);

    List<String> commandArgs = new ArrayList<>();
    commandArgs.add(mongoCommand);
    commandArgs.add(mongoClientUri);

    pb.command(commandArgs);
    try {
      this.mongoProcess = pb.start();
      InputStream is = mongoProcess.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));

      boolean exited = false;
      // wait for shell to connect 
      while (is.available() <= 0 && !exited) {
        exited = mongoProcess.waitFor(10, TimeUnit.MILLISECONDS);

      }

      boolean isStarted = false;
      while (!isStarted && mongoProcess.isAlive()) {
        while (is.available() > 0) {
          String line = reader.readLine();
          if (line.startsWith(terminalOutput)) {
            isStarted = true;
          }
          logger.info("Mongo shell: {}", line);
        }
      }

      if (exited || !mongoProcess.isAlive()) {
        throw new RuntimeException("Mongo shell startup failed, see above for any errors");
      }

    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Exception in initialising mongo shell client", e);
    }
  }

  public void runScript(String filePath) {

    boolean isSuccess = false;

    try {
      sendCommand(mongoProcess, String.format("load(\"%s\")", filePath));

      boolean outputFetched = false;
      do {
        mongoProcess.waitFor(50, TimeUnit.MILLISECONDS);
        InputStream is = mongoProcess.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        if (is.available() > 0) {
          String line = reader.readLine();
          if (!line.equals("true")) {
            logger.error(line);
            mongoProcess.waitFor(100, TimeUnit.MILLISECONDS);
            while (is.available() > 0) {
              line = reader.readLine();
              logger.error(line);
            }
          } else {
            isSuccess = true;
          }
          outputFetched = true;
        }
      } while (!outputFetched);
    } catch (InterruptedException | IOException e) {
      throw new RuntimeException("Exception in executing script", e);
    }

    if (!isSuccess) {
      throw new MongoException("Error in running script for file:" + filePath);
    }
  }

  protected void sendCommand(Process process, String command) {
    OutputStream os = process.getOutputStream();
    // BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
    PrintWriter writer = new PrintWriter(os);
    logger.debug("Sending command " + command);
    try {
      writer.write(command + "\n");
      writer.flush();
    } catch (Exception e) {
      logger.error("Error in sending command", e);
    }
  }

}

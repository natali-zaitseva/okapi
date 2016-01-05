/*
 * Copyright (c) 1995-2015, Index Data
 * All rights reserved.
 * See the file LICENSE for details.
 */
package com.indexdata.sling.conduit;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetClientOptions;
import io.vertx.core.net.NetSocket;
import java.io.IOException;

public class ProcessModuleHandle implements ModuleHandle {

  private final Vertx vertx;
  private final ProcessDeploymentDescriptor desc;
  private Process p;
  private final int port;

  public ProcessModuleHandle(Vertx vertx, ProcessDeploymentDescriptor desc,
          int port) {
    this.vertx = vertx;
    this.desc = desc;
    this.port = port;
  }

  private void tryConnect(Handler<AsyncResult<Void>> startFuture, int count) {
    NetClientOptions options = new NetClientOptions().setConnectTimeout(200);
    NetClient c = vertx.createNetClient(options);
    c.connect(port, "localhost", res -> {
      if (res.succeeded()) {
        System.out.println("Connected to service at port " + port + " count " + count);
        NetSocket socket = res.result();
        socket.close();
        startFuture.handle(Future.succeededFuture());
      } else {
        if (count < 8) {
          vertx.setTimer((count + 1) * 200, id -> {
            tryConnect(startFuture, count + 1);
          });
        } else {
          System.out.println("Failed to connect to service at port " + port + " : " + res.cause().getMessage());
          startFuture.handle(Future.failedFuture(res.cause()));
        }
      }
    });
  }

  @Override
  public void start(Handler<AsyncResult<Void>> startFuture) {
    final String cmdline = desc.getCmdlineStart().replace("%p", Integer.toString(port));
    vertx.executeBlocking(future -> {
      if (p == null) {
        try {
          // TODO: handle quoted strings / backslashes
          ProcessBuilder pb = new ProcessBuilder(cmdline.split(" "));
          pb.inheritIO();
          p = pb.start();
        } catch (IOException ex) {
          future.fail(ex);
          return;
        }
      }
      future.complete();
    }, false, result -> {
      if (result.failed()) {
        startFuture.handle(Future.failedFuture(result.cause()));
      } else {
        if (port > 0) {
          tryConnect(startFuture, 0);
        } else {
          startFuture.handle(Future.succeededFuture());
        }
      }
    });
  }

  @Override
  public void stop(Handler<AsyncResult<Void>> stopFuture) {
    if (p != null) {
      p.destroy();
    }
    stopFuture.handle(Future.succeededFuture());
  }
  public int getPort() {
    return port;
  }
}

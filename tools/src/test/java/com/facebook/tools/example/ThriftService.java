/*
 * Copyright (C) 2014 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.tools.example;

import com.facebook.nifty.client.FramedClientConnector;
import com.facebook.nifty.client.NiftyClientChannel;
import com.facebook.nifty.client.NiftyClientConnector;
import com.facebook.nifty.client.UnframedClientConnector;
import com.facebook.swift.service.ThriftClient;
import com.facebook.swift.service.ThriftClientConfig;
import com.facebook.swift.service.ThriftClientManager;
import com.facebook.tools.ErrorMessage;
import com.facebook.tools.parser.CliCommand;
import com.facebook.tools.parser.CliParser;
import com.facebook.tools.parser.OneOfConverter;
import com.google.common.base.Throwables;
import com.google.common.net.HostAndPort;
import org.apache.thrift.transport.TTransportException;

import java.util.concurrent.ExecutionException;

import static org.apache.thrift.transport.TTransportException.TIMED_OUT;
import static org.apache.thrift.transport.TTransportException.UNKNOWN;

public class ThriftService<T> {
  private final Class<T> service;
  private final Transport transport;
  private final ThriftClientConfig config;
  private final ThriftClientManager clientManager = new ThriftClientManager();

  public ThriftService(Class<T> service, Transport transport, ThriftClientConfig config) {
    this.service = service;
    this.transport = transport;
    this.config = config;
  }

  public ThriftService(Class<T> service, CliParser parser) {
    this(
      service,
      Transport.valueOf(
        parser.get("--thrift-transport", OneOfConverter.oneOf("framed", "buffered")).toUpperCase()
      ),
      new ThriftClientConfig()
        .setSocksProxy(parser.get("--socks", Converters.HOST_PORT))
    );
  }

  public ThriftService(Class<T> service) {
    this(service, Transport.FRAMED, new ThriftClientConfig());
  }

  public ThriftClient<T> createClient() {
    return new ThriftClient<>(clientManager, service, config, service.getSimpleName());
  }

  public T openService(HostAndPort host) {
    ThriftClient<T> client = createClient();

    try {
      return openService(host, client, transport);
    } catch (TTransportException e) {
      throw new ErrorMessage(e, "Failed to connect to %s", host);
    }
  }

  public static void mixin(CliCommand.Builder builder) {
    builder.addOption("--socks")
      .withMetavar("proxy")
      .withDescription("SOCKS proxy address")
      .withExample("localhost:1080")
      .withDefault(null);
    builder.addOption("--thrift-transport")
      .withMetavar("type")
      .withDescription("Transport type, one of: framed, buffered")
      .withDefault("framed");
  }

  public static enum Transport {
    FRAMED,
    BUFFERED,
  }

  private T openService(HostAndPort host, ThriftClient<T> client, Transport transport)
    throws TTransportException {
    try {
      NiftyClientConnector<? extends NiftyClientChannel> connector;

      if (Transport.FRAMED.equals(transport)) {
        connector = new FramedClientConnector(host);
      } else if (Transport.BUFFERED.equals(transport)) {
        connector = new UnframedClientConnector(host);
      } else {
        throw new ErrorMessage("Unexpected thrift transport type: %s", transport);
      }

      return client.open(connector).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();

      throw new TTransportException(TIMED_OUT, "Interrupted opening connection to " + host, e);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();

      Throwables.propagateIfInstanceOf(cause, TTransportException.class);

      throw new TTransportException(UNKNOWN, "Exception opening connection to " + host, cause);
    }
  }
}


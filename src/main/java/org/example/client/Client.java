package org.example.client;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;

import javax.net.ssl.SSLException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

public class Client {
  public static void main(String[] args) throws ExecutionException, InterruptedException, SSLException {
    final SslContext sslContext = SslContextBuilder.forClient()
        .keyManager(Path.of("server.crt").toFile(), Path.of("server.key.converted").toFile())
        .trustManager(Path.of("ca.crt").toFile())
        .build();

    final DefaultAsyncHttpClientConfig config = new DefaultAsyncHttpClientConfig.Builder()
        .setSslContext(sslContext)
        .build();

    Dsl.asyncHttpClient(config)
        .prepareGet("wss://server.local:8080/ws")
        .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(new WebSocketListener() {
          @Override
          public void onOpen(WebSocket webSocket) {

          }

          @Override
          public void onTextFrame(String payload, boolean finalFragment, int rsv) {
            System.out.println(payload);
          }

          @Override
          public void onClose(WebSocket webSocket, int i, String s) {

          }

          @Override
          public void onError(Throwable throwable) {

          }
        }).build())
        .get();
  }
}

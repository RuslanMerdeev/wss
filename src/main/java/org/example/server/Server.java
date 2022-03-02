package org.example.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import reactor.core.publisher.Flux;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.SslProvider;

import javax.net.ssl.SSLException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Caution: в браузере стоит ограничение на кол-во подключений к одному хосту
// Если захотите больше 5-6 подключений, то вам нужно несколько браузеров
public class Server {
  private static final Faker faker = Faker.instance();
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final Flux<String> flux = Flux.interval(Duration.ofSeconds(1))
      .map(o -> Map.of(
              "id", UUID.randomUUID(),
              "sender", faker.finance().creditCard(),
              "recipient", faker.finance().creditCard(),
              "amount", faker.random().nextInt(10_000_00, 100_000_00)
          )
      )
      .log()
      .share()
      .map(o -> {
        try {
          return mapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      });

  public static void main(String[] args) throws SSLException {
    final int port = Optional.ofNullable(System.getenv("app.port")).map(Integer::parseInt).orElse(8080);
    final SslContext sslContext = SslContextBuilder.forServer(Path.of("server.crt").toFile(), Path.of("server.key.converted").toFile())
        .trustManager(Path.of("ca.crt").toFile())
        .build();
    final DisposableServer server = HttpServer.create()
        .secure(SslProvider.builder().sslContext(sslContext).build())
        .route(
            routes -> routes
                .get("/ws.html", (request, response) -> response.sendFile(Path.of("ws.html")))
                .get("/ws.js", (request, response) -> {
                  response.header("Content-Type", "text/javascript");
                  return response.sendFile(Path.of("ws.js"));
                })
                .ws("/ws", (inbound, outbound) -> outbound.sendString(flux))
        )
        .port(port)
        .bindNow();

    server.onDispose().block();
  }
}

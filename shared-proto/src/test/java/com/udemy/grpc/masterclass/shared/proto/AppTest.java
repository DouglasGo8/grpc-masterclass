package com.udemy.grpc.masterclass.shared.proto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udemy.grpc.masterclass.shared.proto.models.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@Slf4j
public class AppTest {


  @Test
  @Disabled
  public void personProtoRepresentation() {
    var john = Person.newBuilder()
            .setAge(45)
            .setName("John Doe")
            .build();

    var otherJohn = Person.newBuilder()
            .setAge(45)
            .setName("John Doe")
            .build();

    var otherJohnTwo = Person.newBuilder()
            .setAge(45)
            .setName("John doe")
            .build();
    //
    log.info("Hello {}, your age is {}", john.getName(), john.getAge());
    assertEquals(john, otherJohn); // true
    assertNotEquals(john, otherJohnTwo);
  }

  @Test
  @Disabled
  @SneakyThrows
  public void personProtoSerialization() {
    var john = Person.newBuilder()
            .setAge(45)
            .setName("John Doe")
            .build();
    //
    var path = Paths.get("sam.ser");
    Files.write(path, john.toByteArray());
  }

  @Test
  @Disabled
  @SneakyThrows
  public void personProtoDeserialization() {
    var path = Paths.get("sam.ser");
    var bytes = Files.readAllBytes(path);
    var john = Person.parseFrom(bytes);
    log.info("Hello {}, your age is {}", john.getName(), john.getAge());
  }

  @Test
  @Disabled
  @SneakyThrows
  public void personJsonVsProtobufPerformance() {

    BiConsumer<String, Runnable> func = (kind, task) -> {
      log.info("Using {} to compare", kind);
      long start = System.currentTimeMillis();
      for (int i = 0; i < 5_000_000; i++) {
        task.run();
      }
      long end = System.currentTimeMillis();
      log.info("result is: {} ms", end - start);
    };
    //
    var jperson = new JPerson();
    jperson.setName("John Doe");
    jperson.setAge(45);
    //
    final var mapper = new ObjectMapper();

    for (int i = 0; i < 5; i++) {

      // json
      func.accept("JSON", () -> {
        try {
          var bs = mapper.writeValueAsBytes(jperson);
          mapper.readValue(bs, JPerson.class);
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      // protobuf
      func.accept("PROTOBUF", () -> {
        try {
          var john = Person.newBuilder()
                  .setAge(45)
                  .setName("John Doe")
                  .build();
          var bs = john.toByteArray();
          Person.parseFrom(bs);
        } catch (Exception e) {
          e.printStackTrace();
        }

      });
    }


    //TimeUnit.SECONDS.sleep(5);
  }

  @Test
  @Disabled
  public void compositionRepresentation() {
    var address = Address.newBuilder()
            .setPostbox(123)
            .setStreet("main street")
            .setCity("Atlanta")
            .build();

    var crv = Car.newBuilder()
            .setMake("Honda")
            .setModel("Cr-v")
            .setYear(2024)
            .build();
    var volvo = Car.newBuilder()
            .setMake("Volvo")
            .setModel("XC-60")
            .setYear(2024)
            .build();

    var cars = List.of(crv, volvo);

    //
    var person = Person.newBuilder()
            .setName("John Doe")
            .setAge(45)
            //.addCar(crv)
            //.addCar(volvo)
            .addAllCar(cars)
            .setAddress(address)
            .build();
    //
    log.info("{}", person);
  }

  @Test
  @Disabled
  public void mapProtoRepresentation() {
    var crv = Car.newBuilder()
            .setMake("Honda")
            .setModel("Cr-v")
            .setYear(2023)
            .build();
    var volvo = Car.newBuilder()
            .setMake("Volvo")
            .setModel("XC-60")
            .setYear(2024)
            .setBodyStyle(BodyStyle.SUV)
            .build();
    //
    var dealer = Dealer.newBuilder()
            .putModel(2023, crv)
            .putModel(2024, volvo)
            .build();
    log.info("{}", dealer);
    log.info("{}", dealer.getModelOrThrow(2024));
    // getModelMap => HashMap
    // dealer.getModelOrDefault(key, volvo)
    //log.info("{}", dealer.getModelOrThrow(2024).getBodyStyle());

  }

  @Test
  @Disabled
  public void defaultValueRepresentation() {
    var person = Person.newBuilder()
            .setName("John Doe")
            .setAge(45)
            //.addCar(crv)
            //.addCar(volvo)
            .build();
    log.info(person.getAddress().getCity());
    log.info("{}", person.hasAddress()); // protobuf don't have nulls

  }

  @Test
  @Disabled
  public void oneOfRepresentation() {

    Consumer<Credentials> login = (c) -> {
      switch (c.getModeCase()) {
        case EMAILMODE:
          log.info("{}", c.getEmailMode());
          break;
        case PHONEMODE:
          log.info("{}", c.getPhoneMode());
          break;
        default:
          throw new IllegalArgumentException();
      }
    };


    var email = EmailOTP.newBuilder()
            .setEmail("unknown@mail.com")
            .setPassword("123xx123")
            .build();

    var phone = PhoneOTP.newBuilder()
            .setCode(55)
            .setNumber(1234567)
            .build();

    var credentials = Credentials.newBuilder()
            .setEmailMode(email)
            //.setPhoneMode(phone)
            .build();

    login.accept(credentials);

  }

  @Test
  @SneakyThrows
  public void versionCompatibility() {
    /*var tvShow_v1 = Television.newBuilder()
            .setBrand("sony")
            .setYear(1999)
            .build();*/

    //
    // Files.write(Paths.get("tv-v1.ser"), tvShow_v1.toByteArray());
    //

    var bs = Files.readAllBytes(Paths.get("tv-v1.ser"));

    var tvShow_v1_final = Television.parseFrom(bs);
    log.info("{}", tvShow_v1_final.getType()); // not in v1

  }


}

package com.example.cherrydan.memory;

import org.junit.jupiter.api.Test;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MemorySizeTest {

    @Test
    public void testPrimitiveSizes() {
        System.out.println("=== JVM 정보 ===");
        System.out.println(VM.current().details());
        System.out.println();


        System.out.println("=== Primitive 타입 크기 ===");
        System.out.println("long 크기: " + ClassLayout.parseClass(long.class).instanceSize() + " bytes");
        System.out.println();

        System.out.println("=== Wrapper 클래스 크기 ===");
        Long longObject = 123L;
        System.out.println("Long 객체 레이아웃:");
        System.out.println(ClassLayout.parseInstance(longObject).toPrintable());
        System.out.println("Long 객체 총 크기: " + GraphLayout.parseInstance(longObject).totalSize() + " bytes");
        System.out.println();

        Integer intObject = 123;
        System.out.println("Integer 객체 레이아웃:");
        System.out.println(ClassLayout.parseInstance(intObject).toPrintable());
        System.out.println("Integer 객체 총 크기: " + GraphLayout.parseInstance(intObject).totalSize() + " bytes");
        System.out.println();

        Boolean boolObject = true;
        System.out.println("Boolean 객체 레이아웃:");
        System.out.println(ClassLayout.parseInstance(boolObject).toPrintable());
        System.out.println("Boolean 객체 총 크기: " + GraphLayout.parseInstance(boolObject).totalSize() + " bytes");
        System.out.println();

        System.out.println("=== Date/Time 클래스 크기 ===");
        LocalDate localDate = LocalDate.now();
        System.out.println("LocalDate 레이아웃:");
        System.out.println(ClassLayout.parseInstance(localDate).toPrintable());
        System.out.println("LocalDate 총 크기: " + GraphLayout.parseInstance(localDate).totalSize() + " bytes");
        System.out.println();

        LocalDateTime localDateTime = LocalDateTime.now();
        System.out.println("LocalDateTime 레이아웃:");
        System.out.println(ClassLayout.parseInstance(localDateTime).toPrintable());
        System.out.println("LocalDateTime 총 크기: " + GraphLayout.parseInstance(localDateTime).totalSize() + " bytes");
        System.out.println();

        System.out.println("=== String 크기 예시 ===");
        String shortString = "Hello";
        System.out.println("짧은 String (\"Hello\") 크기: " + GraphLayout.parseInstance(shortString).totalSize() + " bytes");
        
        String mediumString = "This is a medium length string for testing";
        System.out.println("중간 String 크기: " + GraphLayout.parseInstance(mediumString).totalSize() + " bytes");
        
        String longString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                           "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
        System.out.println("긴 String 크기: " + GraphLayout.parseInstance(longString).totalSize() + " bytes");
        System.out.println();

        System.out.println("=== 샘플 엔티티 크기 측정 ===");
        SampleEntity entity = new SampleEntity();
        entity.id = 1L;
        entity.name = "Test User";
        entity.email = "test@example.com";
        entity.active = true;
        entity.createdAt = LocalDateTime.now();
        
        System.out.println("SampleEntity 레이아웃:");
        System.out.println(ClassLayout.parseInstance(entity).toPrintable());
        System.out.println("SampleEntity 총 크기 (shallow): " + ClassLayout.parseInstance(entity).instanceSize() + " bytes");
        System.out.println("SampleEntity 총 크기 (deep): " + GraphLayout.parseInstance(entity).totalSize() + " bytes");
    }

    static class SampleEntity {
        Long id;
        String name;
        String email;
        Boolean active;
        LocalDateTime createdAt;
    }
}
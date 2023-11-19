package io.github.sekassel.person.backend;

public record Person(
        String image,
        String firstName,
        String lastName,
        int age
) {
}

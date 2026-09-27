package benchmark;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record Person(
        long id,
        String firstName,
        String lastName,
        String email,
        LocalDate birthDate,
        int age,
        BigDecimal balance,
        String city,
        boolean active,
        LocalDateTime createdAt) {
}

package com.example.hobbiesmanagment;

import com.example.hobbiesmanagment.Entities.Credential;
import com.example.hobbiesmanagment.Entities.Gender;
import com.example.hobbiesmanagment.Entities.User;
import com.example.hobbiesmanagment.Repositories.UserRepository; // ודאי שהנתיב נכון
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HobbiesManagmentApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Test
	void testCreateFullUser() {

		Credential userCredential = new Credential("tahel", "securePassword123");

		User user = User.builder()
				.id(312345678L)
				.email("example@domain.com")
				.credential(userCredential)
				.city("Jerusalem")
				.phone("054-0000000")
				.birthday(LocalDate.of(1995, 5, 15))
				.description("מתכנתת ב-Spring Boot")
				.gender(Gender.MALE)
				.build();
		User savedUser = userRepository.save(user);

		assertThat(savedUser.getId()).isEqualTo(312345678L);
		assertThat(savedUser.getCreatedAt()).isNotNull();
		System.out.println("User created successfully at: " + savedUser.getCreatedAt());
	}
}
package com.example.hobbiesmanagment;

import com.example.hobbiesmanagment.DTO.UserRegistrationDto;
import com.example.hobbiesmanagment.Entities.Role;
import com.example.hobbiesmanagment.Entities.RoleName;
import com.example.hobbiesmanagment.Entities.User;
import com.example.hobbiesmanagment.Repositories.RoleRepository;
import com.example.hobbiesmanagment.Repositories.UserRepository;
import com.example.hobbiesmanagment.Service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;
@EnableScheduling
@SpringBootApplication
public class HobbiesManagmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(HobbiesManagmentApplication.class, args);
	}
	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
	@Bean
	CommandLineRunner initData(UserService userService, RoleRepository roleRepository) {
		return args -> {
			if (roleRepository.count() == 0) {
				roleRepository.save(new Role(null, RoleName.LEARNER));
				roleRepository.save(new Role(null, RoleName.MENTOR));
			}

			UserRegistrationDto adminRequest = new UserRegistrationDto();
			adminRequest.setIdNumber("1");
			adminRequest.setName("מנהל מערכת");
			adminRequest.setEmail("admin@test.com");
			adminRequest.setPassword("123456");
			adminRequest.setSelectedRole("MENTOR");
			try {
				userService.register(adminRequest);
				System.out.println(">> Admin user created via AuthService!");
			} catch (Exception e) {
				System.out.println(">> Admin already exists or error occurred: " + e.getMessage());
			}
		};
	}
}


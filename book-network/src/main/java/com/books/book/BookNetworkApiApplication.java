package com.books.book;

import com.books.book.role.Role;
import com.books.book.role.RoleRepository;
import com.books.book.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditAware")
@EnableAsync
public class BookNetworkApiApplication {

	@Autowired
	private RoleRepository roleRepository;

	public static void main(String[] args) {
		 SpringApplication.run(BookNetworkApiApplication.class, args);

	}

	@Bean
	public CommandLineRunner runner(){
		return args -> {
			System.out.println("args: " + Arrays.stream(args).peek(System.out::println
			));
			if(roleRepository.findByName("USER").isEmpty()){
				Role userRole = Role.builder().name("USER").build();
				roleRepository.save(userRole);
			}
		};
	}

}

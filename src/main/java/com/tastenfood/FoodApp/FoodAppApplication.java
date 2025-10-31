package com.tastenfood.FoodApp;

import com.tastenfood.FoodApp.email_notification.dtos.NotificationDTO;
import com.tastenfood.FoodApp.email_notification.services.NotificationService;
import com.tastenfood.FoodApp.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
public class FoodAppApplication {

	private final NotificationService notificationService;

	public static void main(String[] args) {
        SpringApplication.run(FoodAppApplication.class, args);
	}

	@Bean
	CommandLineRunner runner(){
		return args -> {
			NotificationDTO notificationDTO = NotificationDTO.builder()
					.recipient("skbnsl20@gmail.com")
					.subject("Test")
					.body("Test")
					.type(NotificationType.EMAIL)
					.build();

			notificationService.sendmail(notificationDTO);

		};
	}

}
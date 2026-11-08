package com.redehoteleira.api_hotel;

import com.redehoteleira.api_hotel.views.MainFrame;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication
public class ApiHotelApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = new SpringApplicationBuilder(ApiHotelApplication.class)
				.headless(false)
				.run(args);

		SwingUtilities.invokeLater(() -> context.getBean(MainFrame.class).setVisible(true));
	}
}
package com.example.demo.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
//		SpringApplication.run(DemoApplication.class, args);
		System.out.println(3 >> 1);
		System.out.println(3 << 1);
	}

	@RestController
	public static class TempC {
		@GetMapping("/")
		public String go(HttpServletRequest httpServletRequest) {
			new ArrayList<Integer>().add(1);
			return "helloWOrld";
		}
	}


}


class Parent {}
class Child extends Parent{}

class Mom<Parant> {}

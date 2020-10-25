package com.example.demo.java;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.groovy.util.Maps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@RestController
	public static class TempC {
		@GetMapping("/mapString")
		public Dto go(HttpServletRequest httpServletRequest) {
			new ArrayList<Integer>().add(1);
			Map<String, Item> itemMap = Maps.of(
					"1", new Item("item1", 1000),
					"2", new Item("item2", 1000),
					"3", new Item("item3", 1000),
					"4", new Item("item4", 1000)
			);

			Map<Item, Item> itemItemMap = itemMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getValue));
			return new Dto(itemMap, itemItemMap, itemMap.get("1"));
		}
	}

	@Getter
	@AllArgsConstructor
	static class Dto {
		private Map<String, Item> itemMap;
		private Map<Item, Item> itemItemMap;
		private Item item;
	}

	@Getter
	@AllArgsConstructor
	static class Item {
		String name;
		Integer price;
	}
}



class Parent {}
class Child extends Parent{}

class Mom<Parant> {}

package com.kh.khedu.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/views")
public class ReactForwardController {
	@GetMapping("/**")
	public String forward() {
		return "forward:/index.html";
	}
}

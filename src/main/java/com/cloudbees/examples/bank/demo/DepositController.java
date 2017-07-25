package com.cloudbees.examples.bank.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DepositController {

	@Value("${api.proto}")
	private String apiProto;

	@Value("${api.host}")
	private String apiHost;

	@Value("${api.port}")
	private String apiPort;

	@Value("${version}")
	private String version;

	@RequestMapping("/deposit")
	public String deposit(Model model) {
		model.addAttribute("apiProto", apiProto);
		model.addAttribute("apiHost", apiHost);
		model.addAttribute("apiPort", apiPort);
		model.addAttribute("version", version);

		return "deposit";
	}

}

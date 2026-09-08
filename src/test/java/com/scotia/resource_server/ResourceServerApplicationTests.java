package com.scotia.resource_server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// start the spring app for this test
@SpringBootTest

// set up the fake http caller
@AutoConfigureMockMvc
class ResourceServerApplicationTests {

	@Autowired
	MockMvc mockMvc;

	// test: protected endpoint with valid token + correct scope: 200 (passes both gates)
	@Test
	void statusEndpoint_returnsActive_forCodeA() throws Exception {
		mockMvc.perform(get("/users/salem/status/A")
						.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_read:status"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value("salem"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	// test: protected endpoint with valid token but wrong scope: 403 (blocked at authorization gate)
	@Test
	void statusEndpoint_returnsForbidden_withInsufficientScope() throws Exception {
		mockMvc.perform(get("/users/salem/status/A")
						.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_write:status"))))
				.andExpect(status().isForbidden());
	}

	// test: protected endpoint with no token: 401 (blocked at authentication gate)
	@Test
	void statusEndpoint_returnsUnauthorized_withoutToken() throws Exception {
		mockMvc.perform(get("/users/simon/status/L"))
				.andExpect(status().isUnauthorized());
	}

	// test: public endpoint: 200 (no auth required, permitAll)
	@Test
	void publicHealthEndpoint_returnsUp() throws Exception {
		mockMvc.perform(get("/public/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

}
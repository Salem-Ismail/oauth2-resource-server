package com.salem.resourceserver;

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

@SpringBootTest                 // start the spring app for this test
@AutoConfigureMockMvc           // set up the fake http caller
class ResourceServerApplicationTests {

	@Autowired
	MockMvc mockMvc;

	// ============================================================
	// Status endpoint: status-code mapping (the switch branches)
	// Each test covers one branch of getUserStatus's switch.
	// ============================================================

	// A: ACTIVE
	@Test
	void statusEndpoint_returnsActive_forCodeA() throws Exception {
		mockMvc.perform(get("/users/salem/status/A")
						.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_read:status"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value("salem"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	// L: LOCKED
	@Test
	void statusEndpoint_returnsLocked_forCodeL() throws Exception {
		mockMvc.perform(get("/users/salem/status/L")
						.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_read:status"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("LOCKED"));
	}

	// S: SUSPENDED
	@Test
	void statusEndpoint_returnsSuspended_forCodeS() throws Exception {
		mockMvc.perform(get("/users/salem/status/S")
						.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_read:status"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("SUSPENDED"));
	}

	// unknown code: UNKNOWN (default branch)
	@Test
	void statusEndpoint_returnsUnknown_forInvalidCode() throws Exception {
		mockMvc.perform(get("/users/salem/status/Z")
						.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_read:status"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UNKNOWN"));
	}

	// ============================================================
	// Status endpoint: access control (the two security gates)
	// Same endpoint, varying the token to prove authn + authz.
	// ============================================================

	// valid token but wrong scope: 403 (blocked at authorization gate)
	@Test
	void statusEndpoint_returnsForbidden_withInsufficientScope() throws Exception {
		mockMvc.perform(get("/users/salem/status/A")
						.with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_write:status"))))
				.andExpect(status().isForbidden());
	}

	// no token: 401 (blocked at authentication gate)
	@Test
	void statusEndpoint_returnsUnauthorized_withoutToken() throws Exception {
		mockMvc.perform(get("/users/simon/status/L"))
				.andExpect(status().isUnauthorized());
	}

	// ============================================================
	// Other endpoints
	// ============================================================

	// public health check: 200 (no auth required, permitAll)
	@Test
	void publicHealthEndpoint_returnsUp() throws Exception {
		mockMvc.perform(get("/public/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

}
package com.scotia.resource_server;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

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

	// test for active status
	@Test
	void statusEndpoint_returnsActive_forCodeA() throws Exception {
		mockMvc.perform(get("/users/salem/status/A"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value("salem"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	// new test for locked status
	@Test
	void statusEndpoint_returnsLocked_forCodeL() throws Exception {
		mockMvc.perform(get("/users/simon/status/L"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userId").value("simon"))
				.andExpect(jsonPath("$.status").value("LOCKED"));
	}

}
/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mohaseeb.mgmt.tracking;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.shell.jline.PromptProvider;

/**
 * Main entry point for the application.
 *
 * <p>Creates the application context and start the REPL.</p>
 *
 * @author Eric Bottard
 */
@SpringBootApplication
@EnableJpaRepositories
public class TrackShell {

	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(TrackShell.class, args);
	}

	@Bean
	public PromptProvider trackPromptProvider() {
		return () -> new AttributedString("track:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
	}
}
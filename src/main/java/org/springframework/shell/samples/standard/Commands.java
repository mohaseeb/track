/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.shell.samples.standard;

import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.MethodParameter;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.ValueProviderSupport;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Size;

/**
 * Example commands for the Shell 2 Standard resolver.
 *
 * @author Eric Bottard
 */
@ShellComponent()
public class Commands {
	private final static String NOTSET = "not set";

	@ShellMethod(value = "Starts a an episode")
	public void start(@ShellOption(defaultValue=NOTSET) String when) {
		System.out.println("start episode");
	}


	@ShellMethod(value = "Ends the last episode")
	public void end(@ShellOption(defaultValue=NOTSET) String when) {
		System.out.println("close the last open episode");
	}

	@ShellMethod("Test completion of special values.")
	public void quote(@ShellOption(valueProvider = FunnyValuesProvider.class) String text) {
		System.out.println("You said " + text);
	}


	@ShellMethod(value = "show current day")
	public void day() {
		System.out.println("close the last open episode");
	}

	@ShellMethod(value = "show current week days")
	public void week(@ShellOption(defaultValue=NOTSET) String weekNo) {
		System.out.println("close the last open episode");
	}


	@ShellMethod(value = "show current month weeks")
	public void month(@ShellOption(defaultValue=NOTSET) String monthNo) {
		System.out.println("close the last open episode");
	}

	@ShellMethod(value = "show current months")
	public void year(@ShellOption(defaultValue=NOTSET) String yearNo) {
		System.out.println("close the last open episode");
	}


}

/**
 * A {@link org.springframework.shell.standard.ValueProvider} that emits values with special characters
 * (quotes, escapes, <em>etc.</em>)
 *
 * @author Eric Bottard
 */
@Component
class FunnyValuesProvider extends ValueProviderSupport {

	private final static String[] VALUES = new String[] {
		"hello world",
		"I'm quoting \"The Daily Mail\"",
		"10 \\ 3 = 3"
	};

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext completionContext, String[] hints) {
		return Arrays.stream(VALUES).map(CompletionProposal::new).collect(Collectors.toList());
	}
}

/*
 * Copyright 2023 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.dansiviter.cdi.repos.processor;

import static java.lang.String.format;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

import com.squareup.javapoet.JavaFile;

/**
 * Defines a processing context.
 */
record Context(
	RepositoryProcessor parent,
	List<Consumer<JavaFile.Builder>> fileDecorators)
{

	Context(RepositoryProcessor parent) {
		this(parent, new ArrayList<>());
	}

	ProcessingEnvironment env() {
		return parent.processingEnv();
	}

	void error(Element e, String format, Object... args) {
		env().getMessager().printMessage(ERROR, format(format, args), e);
	}

	public void note(Element e, String format, Object... args) {
		env().getMessager().printMessage(NOTE, format(format, args), e);
	}
}

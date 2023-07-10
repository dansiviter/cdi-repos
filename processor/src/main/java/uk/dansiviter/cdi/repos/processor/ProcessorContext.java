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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/**
 * Defines a processing context.
 */
class ProcessorContext {
	final RepositoryProcessor parent;
	final TypeElement rootType;
	final TypeSpec.Builder builder;
	final List<Consumer<JavaFile.Builder>> fileDecorators = new ArrayList<>();

	ProcessorContext(RepositoryProcessor parent, TypeElement rootType, TypeSpec.Builder builder) {
		this.parent = parent;
		this.rootType = rootType;
		this.builder = builder;
	}

	ProcessingEnvironment env() {
		return parent.processingEnv();
	}
}

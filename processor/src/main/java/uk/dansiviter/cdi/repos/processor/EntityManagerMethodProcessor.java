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

import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.tools.Diagnostic.Kind.ERROR;
import static uk.dansiviter.cdi.repos.processor.ProcessorUtil.isClass;

import javax.lang.model.element.ExecutableElement;

import jakarta.persistence.EntityManager;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

import uk.dansiviter.cdi.repos.processor.RepositoryProcessor.SubProcessor;

/**
 * Processes {@link EntityManager} methods.
 */
class EntityManagerMethodProcessor implements SubProcessor<ExecutableElement> {
	@Override
	public void process(ProcessorContext ctx, Builder builder, ExecutableElement e) {
		if (e.getReturnType().getKind() != DECLARED
				|| !isClass(ctx.env(), e.getReturnType(), EntityManager.class))
		{
			return;
		}

		if (!e.getParameters().isEmpty()) {
			ctx.env().getMessager().printMessage(ERROR, "Methods returning EntityManager cannot have parameters", e);
			return;
		}

		var method = MethodSpec.methodBuilder(e.getSimpleName().toString())
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC)
				.returns(TypeName.get(e.getReturnType()))
				.addStatement("return this.em");

		builder.addMethod(method.build());
	}
}

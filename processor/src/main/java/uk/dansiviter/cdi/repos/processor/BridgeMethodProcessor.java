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
import static javax.tools.Diagnostic.Kind.ERROR;

import java.util.Optional;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;

import jakarta.persistence.EntityManager;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

import uk.dansiviter.cdi.repos.Util;

/**
 * Handles methods that bridge between {@link EntityManager} methods plus some special cases (i.e. {@code #save} and
 * {@code #get}).
 */
public class BridgeMethodProcessor implements SubProcessor<ExecutableElement> {
	private static Set<String> BRIDGE_METHODS = Set.of("find", "get", "persist", "merge", "delete", "remove", "save", "flush");

	@Override
	public void process(ProcessorContext ctx, Builder builder, ExecutableElement e) {
		if (!BRIDGE_METHODS.contains(e.getSimpleName().toString())) {
			return;
		}

		var method = MethodSpec.methodBuilder(e.getSimpleName().toString())
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC)
				.returns(TypeName.get(e.getReturnType()));

		e.getParameters().forEach(p -> method.addParameter(TypeName.get(p.asType()), p.getSimpleName().toString()));

		switch (e.getSimpleName().toString()) {
			case "find":
			case "get":
				processFindMethod(ctx, method, e);
				break;
			case "flush":
				method.addStatement("this.em.flush()");
				break;
			case "save":
				verifyParamCount(ctx, 1, e);
				method.addStatement("$T.save($L, this.em)", Util.class, e.getParameters().get(0));
				break;
			case "persist":
				verifyParamCount(ctx, 1, e);
				verifyVoidReturn(ctx, e);
				method.addStatement("this.em.persist($L)", e.getParameters().get(0));
				break;
			case "merge":
				verifyParamCount(ctx, 1, e);
				method.addStatement("this.em.merge($L)", e.getParameters().get(0));
				break;
			case "remove":
			case "delete":
				verifyParamCount(ctx, 1, e);
				method.addStatement("this.em.remove($L)", e.getParameters().get(0));
				break;
		}

		builder.addMethod(method.build());
	}

	private static void processFindMethod(ProcessorContext ctx, MethodSpec.Builder method, ExecutableElement e) {
		verifyParamCount(ctx, 1, e);

		var declared = (DeclaredType) e.getReturnType();
		if (declared.asElement().getSimpleName().toString().equals("Optional")) {
			method.addStatement("return $T.ofNullable(this.em.find($T.class, $L))", Optional.class,
					declared.getTypeArguments().get(0), e.getParameters().get(0));
		} else {
			method.addStatement("return this.em.find($T, $L)", declared, e.getParameters().get(0));
		}
	}

	private static void verifyParamCount(ProcessorContext ctx, int params, ExecutableElement e) {
		if (e.getParameters().size() != params) {
			ctx.env().getMessager().printMessage(ERROR, String.format("Only %d params supported", params), e);
		}
	}

	private static void verifyVoidReturn(ProcessorContext ctx, ExecutableElement e) {
		if (e.getReturnType().getKind() != TypeKind.VOID) {
			ctx.env().getMessager().printMessage(ERROR, "Only void return type supported", e);
		}
	}
}

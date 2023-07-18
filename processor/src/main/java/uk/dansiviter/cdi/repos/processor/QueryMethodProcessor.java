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

import static javax.lang.model.type.TypeKind.INT;
import static javax.lang.model.type.TypeKind.LONG;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.tools.Diagnostic.Kind.ERROR;
import static uk.dansiviter.cdi.repos.processor.ProcessorUtil.addTransactional;
import static uk.dansiviter.cdi.repos.processor.ProcessorUtil.isClass;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

import uk.dansiviter.cdi.repos.Util;
import uk.dansiviter.cdi.repos.annotations.Query;
import uk.dansiviter.cdi.repos.annotations.Temporal;
import uk.dansiviter.cdi.repos.processor.RepositoryProcessor.SubProcessor;

/**
 * This processor handles {@link Query} annotation methods.
 */
class QueryMethodProcessor implements SubProcessor<ExecutableElement> {
	@Override
	public void process(Context ctx, Builder builder, ExecutableElement e) {
		var query = e.getAnnotation(Query.class);
		if (query == null) {
			return;
		}

		if (query.value().isEmpty()) {
			ctx.env().getMessager().printMessage(ERROR, "Query name cannot be empty", e);
			return;
		}

		var method = MethodSpec.methodBuilder(e.getSimpleName().toString())
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(TypeName.get(e.getReturnType()));
		addTransactional(method, e);

		var returnType = e.getReturnType();

		if (isClass(ctx.env(), returnType, Stream.class) ||
				isClass(ctx.env(), returnType, List.class) ||
				isClass(ctx.env(), returnType, Optional.class))
		{
			var dt = (DeclaredType) returnType;
			method.addStatement("var q = this.em.createNamedQuery($S, $T.class)", query.value(), dt.getTypeArguments().get(0));
		} else {
			method.addStatement("var q = this.em.createNamedQuery($S)", query.value());
		}

		parameters(ctx, e, method, query);

		if (isClass(ctx.env(), returnType, Stream.class)) {
			method.addStatement("return q.getResultStream()");
		} else if (isClass(ctx.env(), returnType, List.class)) {
			method.addStatement("return q.getResultList()");
		} else if (isClass(ctx.env(), returnType, Optional.class)) {
			method.addStatement("return $T.singleResult(q.getResultStream())", Util.class);
		} else if (returnType.getKind() == INT || returnType.getKind() == LONG) {
			method.addStatement("return q.executeUpdate()");
		} else if (returnType.getKind() == VOID) {
			method.addStatement("q.executeUpdate()");
		} else {
			ctx.env().getMessager().printMessage(ERROR, "Unknown return type '" + e.getReturnType() + "'!", e);
			return;
		}

		builder.addMethod(method.build());
	}

	private static void parameters(Context ctx, ExecutableElement e, MethodSpec.Builder method, Query query) {
		var paramCount = new AtomicInteger();
		for (var p : e.getParameters()) {
			method.addParameter(TypeName.get(p.asType()), p.getSimpleName().toString());

			var codeBlock = CodeBlock.builder().add("q.setParameter(");

			if (query.namedParameters()) {
				codeBlock.add("$S, ", p.getSimpleName());
			} else {
				codeBlock.add("$L, ", paramCount.incrementAndGet());
			}

			if (isClass(ctx.env(), p.asType(), Optional.class)) {
				codeBlock.add("$L.orElse(null)", p);
			} else if (isClass(ctx.env(), p.asType(), OptionalInt.class) ||
				isClass(ctx.env(), p.asType(), OptionalLong.class) ||
				isClass(ctx.env(), p.asType(), OptionalDouble.class)) {
				codeBlock.add("$T.orElseNull($L)", Util.class, p);
			} else {
				codeBlock.add("$L", p);
			}

			var temporal = p.getAnnotation(Temporal.class);
			if (temporal != null) {
				codeBlock.add(", $L)", temporal.value());
				ctx.fileDecorators().add(f -> f.addStaticImport(temporal.value()));
			} else {
				codeBlock.add(")");
			}
			method.addStatement(codeBlock.build());
		}
	}
}

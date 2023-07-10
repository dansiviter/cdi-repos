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

import static javax.tools.Diagnostic.Kind.ERROR;
import static uk.dansiviter.cdi.repos.processor.ProcessorUtil.isClass;

import java.util.List;
import java.util.stream.Stream;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

import uk.dansiviter.cdi.repos.Util;
import uk.dansiviter.cdi.repos.annotations.Query;
import uk.dansiviter.cdi.repos.annotations.Temporal;

/**
 * This processor handles {@link Query} annotation methods.
 */
public class QueryMethodProcessor implements SubProcessor<ExecutableElement> {
	@Override
	public void process(ProcessorContext ctx, Builder builder, ExecutableElement e) {
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

		if (query.storedProcedure()) {
			ctx.env().getMessager().printMessage(ERROR, "Stored procedure queries not supported yet", e);
			return;
		} else {
			method.addStatement("var q = this.em.createNamedQuery($S)", query.value());
		}

		parameters(ctx, builder, e, method, query);

		if (isClass(ctx.env(), e.getReturnType(), Stream.class)) {
			method.addStatement("return q.getResultStream()");
		} else if (isClass(ctx.env(), e.getReturnType(), List.class)) {
			method.addStatement("return q.getResultList()");
		} else if (e.getReturnType().getKind() == TypeKind.INT || e.getReturnType().getKind() == TypeKind.LONG) {
			method.addStatement("return q.executeUpdate()");
		} else if (e.getReturnType().getKind() == TypeKind.VOID) {
			method.addStatement("q.executeUpdate()");
		} else {
			ctx.env().getMessager().printMessage(ERROR, "Unknown return type '" + e.getReturnType() + "'!", e);
			return;
		}

		builder.addMethod(method.build());
	}

	private static void parameters(ProcessorContext ctx, Builder builder, ExecutableElement e, MethodSpec.Builder method, Query query) {
		for (int i = 0; i < e.getParameters().size(); i++) {
			var p = e.getParameters().get(i);
			method.addParameter(TypeName.get(p.asType()), p.getSimpleName().toString());
			var temporal = p.getAnnotation(Temporal.class);
			if (query.namedParameters()) {
				if (temporal != null) {
					method.addStatement("q.setParameter($S, $T.unwrap($L), $L)", p.getSimpleName(), Util.class, p, temporal.value());
					ctx.fileDecorators.add(f -> f.addStaticImport(temporal.value()));
				} else {
					method.addStatement("q.setParameter($S, $T.unwrap($L))", p.getSimpleName(), Util.class, p);
				}
			} else {
				if (temporal != null) {
					method.addStatement("q.setParameter($L, $T.unwrap($L), $L)", i + 1, Util.class, p, temporal.value());
					ctx.fileDecorators.add(f -> f.addStaticImport(temporal.value()));
				} else {
					method.addStatement("q.setParameter($L, $T.unwrap($L))", i + 1, Util.class, p);
				}
			}
		}
	}
}

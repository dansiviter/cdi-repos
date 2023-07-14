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
import static javax.lang.model.type.TypeKind.VOID;
import static javax.tools.Diagnostic.Kind.ERROR;
import static uk.dansiviter.cdi.repos.processor.ProcessorUtil.addTransactional;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import jakarta.persistence.EntityManager;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec.Builder;

import uk.dansiviter.cdi.repos.Util;
import uk.dansiviter.cdi.repos.processor.RepositoryProcessor.SubProcessor;

/**
 * Handles methods that bridge between {@link EntityManager} methods plus some special cases (i.e. {@code #save} and
 * {@code #get}).
 */
class BridgeMethodProcessor implements SubProcessor<ExecutableElement> {
	private static final Pattern FIND_METHOD = Pattern.compile("(find|get)");
	private static final Pattern PERSIST_METHOD = Pattern.compile("persist(AndFlush)?");
	private static final Pattern MERGE_METHOD = Pattern.compile("merge(AndFlush)?");
	private static final Pattern REMOVE_METHOD = Pattern.compile("(?:delete|remove)(AndFlush)?");
	private static final Pattern FLUSH_METHOD = Pattern.compile("flush");
	private static final Pattern SAVE_METHOD = Pattern.compile("save(AndFlush)?");

	private static final Map<Pattern, MethodProcessor> PROCESSORS = Map.of(
		FIND_METHOD, BridgeMethodProcessor::processFind,
		PERSIST_METHOD, BridgeMethodProcessor::processPersist,
		MERGE_METHOD, BridgeMethodProcessor::processMerge,
		REMOVE_METHOD, BridgeMethodProcessor::processRemove,
		FLUSH_METHOD, BridgeMethodProcessor::processFlush,
		SAVE_METHOD, BridgeMethodProcessor::processSave);

	@Override
	public void process(Context ctx, Builder builder, ExecutableElement e) {
		var method = MethodSpec.methodBuilder(e.getSimpleName().toString())
				.addAnnotation(Override.class)
				.addModifiers(PUBLIC)
				.returns(TypeName.get(e.getReturnType()));
		addTransactional(method, e);

		e.getParameters().forEach(p -> method.addParameter(TypeName.get(p.asType()), p.getSimpleName().toString()));

		for (var entry : PROCESSORS.entrySet()) {
			var matcher = entry.getKey().matcher(e.getSimpleName());
			if (matcher.matches()) {
				entry.getValue().process(matcher, ctx, method, e);
				builder.addMethod(method.build());
				return;
			}
		}
	}

	private static void processFind(Matcher matcher, Context ctx, MethodSpec.Builder method, ExecutableElement e) {
		verifyParamCount(ctx, 1, e);

		var declared = (DeclaredType) e.getReturnType();
		if (declared.asElement().getSimpleName().toString().equals("Optional")) {
			method.addStatement("return $T.ofNullable(this.em.find($T.class, $L))", Optional.class,
					declared.getTypeArguments().get(0), e.getParameters().get(0));
		} else {
			method.addStatement("return this.em.find($T, $L)", declared, e.getParameters().get(0));
		}
	}

	private static void processPersist(Matcher matcher, Context ctx, MethodSpec.Builder method, ExecutableElement e) {
		verifyParamCount(ctx, 1, e);

		wrapFlush(matcher.group(1) != null, method, () -> {
			var param = e.getParameters().get(0);
			method.addStatement("this.em.persist($L)", param);

			if (e.getReturnType().getKind() != VOID) {
				method.addStatement("return $L", param);
			}
		});
	}

	private static void processMerge(Matcher matcher, Context ctx, MethodSpec.Builder method, ExecutableElement e) {
		verifyParamCount(ctx, 1, e);

		wrapFlush(matcher.group(1) != null, method, () -> {
			var statement = e.getReturnType().getKind() == VOID ? "this.em.merge($L)" : "return this.em.merge($L)";
			method.addStatement(statement, e.getParameters().get(0));
		});
	}

	private static void processRemove(Matcher matcher, Context ctx, MethodSpec.Builder method, ExecutableElement e) {
		verifyParamCount(ctx, 1, e);

		wrapFlush(matcher.group(1) != null, method,
			() -> method.addStatement("this.em.remove($L)", e.getParameters().get(0)));
	}

	private static void processFlush(Matcher matcher, Context ctx, MethodSpec.Builder method, ExecutableElement e) {
		method.addStatement("this.em.flush()");
	}

	private static void processSave(Matcher matcher, Context ctx, MethodSpec.Builder method, ExecutableElement e) {
		verifyParamCount(ctx, 1, e);

		wrapFlush(matcher.group(1) != null, method, () -> {
			var statement = e.getReturnType().getKind() == VOID ? "$T.save($L, this.em)" : "return $T.save($L, this.em)";
			method.addStatement(statement, Util.class, e.getParameters().get(0));
		});
	}

	private static void verifyParamCount(Context ctx, int params, ExecutableElement e) {
		if (e.getParameters().size() != params) {
			ctx.env().getMessager().printMessage(ERROR, String.format("Only %d params supported", params), e);
		}
	}

	private static void wrapFlush(boolean flush, MethodSpec.Builder method, Runnable command) {
		if (flush) {
			method.beginControlFlow("try");
		}
		command.run();
		if (flush) {
			method.nextControlFlow("finally")
				.addStatement("this.em.flush()")
				.endControlFlow();
		}
	}

	@FunctionalInterface
	private interface MethodProcessor {
		void process(Matcher matcher, Context ctx, MethodSpec.Builder method, ExecutableElement e);
	}
}

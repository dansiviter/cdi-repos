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

import static javax.lang.model.SourceVersion.RELEASE_17;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static uk.dansiviter.cdi.repos.processor.ProcessorUtil.className;

import java.io.IOException;
import java.time.Instant;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import uk.dansiviter.cdi.repos.annotations.Repository;

/**
 * Processes {@link Repository} annotations.
 */
@SupportedAnnotationTypes("uk.dansiviter.cdi.repos.annotations.Repository")
@SupportedSourceVersion(RELEASE_17)
public class RepositoryProcessor extends AbstractProcessor {
	private static final Set<SubProcessor<ExecutableElement>> METHOD_PROCESSORS = Set.of(
			new EntityManagerMethodProcessor(),
			new QueryMethodProcessor(),
			new BridgeMethodProcessor());

	private final Supplier<Instant> nowSupplier;

	/**
	 * Creates a new processor.
	 */
	public RepositoryProcessor() {
		this(Instant::now);
	}

	RepositoryProcessor(Supplier<Instant> nowSupplier) {
		this.nowSupplier = nowSupplier;
	}

	ProcessingEnvironment processingEnv() {
		return this.processingEnv;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		annotations.forEach(a -> roundEnv.getElementsAnnotatedWith(a).forEach(e -> process((TypeElement) e)));
		return true;
	}

	private void process(TypeElement element) {
		var ctx = new Context(this);

		var className = className(processingEnv, element);
		if (element.getKind() != ElementKind.INTERFACE) {
			ctx.error(element, "Not interface type: %s", className);
			return;
		}
		ctx.note(element, "Generating class for: %s", className);

		var pkg = this.processingEnv.getElementUtils().getPackageOf(element);
		var concreteName = className.substring(className.lastIndexOf('.') + 1).concat("$impl");
		createConcrete(ctx, className, element, concreteName, pkg);
	}

	private void createConcrete(
		Context ctx,
		String className,
		TypeElement type,
		String concreteName,
		PackageElement pkg)
	{
		var typeBuilder = TypeSpec.classBuilder(concreteName)
				.addModifiers(PUBLIC)
				.addAnnotation(AnnotationSpec
						.builder(Generated.class)
						.addMember("value", "$S", getClass().getName())
						.addMember("comments", "$S", "https://cdi-repos.dansiviter.uk")
						.addMember("date", "$S", this.nowSupplier.get().toString())
						.build())
				.addAnnotation(AnnotationSpec
						.builder(ApplicationScoped.class)
						.build())
				.addSuperinterface(type.asType());

		processPersistenceContext(typeBuilder, type);

		methods(ctx, type).forEach(m -> METHOD_PROCESSORS.forEach(p -> p.process(ctx, typeBuilder, m)));

		var javaFile = JavaFile.builder(pkg.getQualifiedName().toString(), typeBuilder.build());

		ctx.fileDecorators().forEach(d -> d.accept(javaFile));

		try {
			javaFile.build().writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			ctx.error(type, e.getMessage());
		}
	}

	private void processPersistenceContext(TypeSpec.Builder builder, TypeElement type) {
		var persistenceCtx = type.getAnnotation(PersistenceContext.class);
		var annotation = persistenceCtx != null ?
			AnnotationSpec.get(persistenceCtx) : AnnotationSpec.builder(PersistenceContext.class).build();

		builder.addField(FieldSpec
				.builder(EntityManager.class, "em", PRIVATE)
				.addAnnotation(annotation).build());
	}

	private static Stream<? extends ExecutableElement> methods(Context ctx, TypeElement type) {
		var methods = type.getEnclosedElements().stream()
				.filter(e -> e.getKind() == ElementKind.METHOD)
				.map(ExecutableElement.class::cast)
				.filter(e -> !e.isDefault());

		var interfaceMethods = type.getInterfaces().stream()
				.map(ctx.env().getTypeUtils()::asElement)
				.map(TypeElement.class::cast)
				.flatMap(t -> methods(ctx, t));

		return Stream.concat(methods, interfaceMethods);
	}

	interface SubProcessor<E extends Element> {
		void process(Context ctx, TypeSpec.Builder builder, E element);
	}
}

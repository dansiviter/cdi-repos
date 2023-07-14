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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import jakarta.transaction.Transactional;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;

enum ProcessorUtil { ;

	static String className(ProcessingEnvironment env, Element e) {
		if (e instanceof TypeElement te) {
			return env.getElementUtils().getBinaryName(te).toString();
		}
		throw new IllegalStateException("Unknown type! " + e) ;
	}

	static String classSimpleName(ProcessingEnvironment env, Element e) {
		var name = className(env, e);
		if (name != null) {
			return name.substring(name.lastIndexOf('.') + 1);
		}
		return null;
	}

	static boolean isClass(ProcessingEnvironment env, Element e, Class<?> cls) {
		return cls.getName().equals(className(env, e));
	}

	static boolean isClass(ProcessingEnvironment env, TypeMirror type, Class<?> cls) {
		if (type instanceof DeclaredType dt) {
			return isClass(env, dt.asElement(), cls);
		}
		return false;
	}

	static void addTransactional(MethodSpec.Builder method, ExecutableElement e) {
		var transactional = e.getAnnotation(Transactional.class);
		if (transactional != null) {
			method.addAnnotation(get(transactional));
		}
	}

	static AnnotationSpec get(Annotation annotation) {
		return get(annotation, false);
	}

	/**
	 *@see com.squareup.javapoet.AnnotationSpec#get(Annotation, boolean)
	 */
	static AnnotationSpec get(Annotation annotation, boolean includeDefaultValues) {
    var builder = AnnotationSpec.builder(annotation.annotationType());
		try {
			var methods = annotation.annotationType().getDeclaredMethods();
      Arrays.sort(methods, Comparator.comparing(Method::getName));
      for (var method : methods) {
				Object value;
				try {
            value = method.invoke(annotation);
        } catch (InvocationTargetException ite) {
					if (ite.getTargetException() instanceof MirroredTypesException mte) {
            value = mte.getTypeMirrors().toArray();
					} else if (ite.getTargetException() instanceof MirroredTypeException mte) {
            value = mte.getTypeMirror();
					} else {
						throw new IllegalStateException(ite);
					}
        }

        if (!includeDefaultValues) {
          if (Objects.deepEquals(value, method.getDefaultValue())) {
            continue;
          }
        }
        if (value.getClass().isArray()) {
          for (int i = 0; i < Array.getLength(value); i++) {
            addMemberForValue(builder, method.getName(), Array.get(value, i));
          }
          continue;
        }
        if (value instanceof Annotation a) {
          builder.addMember(method.getName(), "$L", get(a, includeDefaultValues));
          continue;
        }
        addMemberForValue(builder, method.getName(), value);
      }
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Reflecting " + annotation + " failed!", e);
		}
    return builder.build();
  }

	/**
	 * @see com.squareup.javapoet.AnnotationSpec#addMemberForValue(String, Object)
	 */
	static void addMemberForValue(AnnotationSpec.Builder builder, String memberName, Object value) {
		if (value instanceof Class<?> || value instanceof TypeMirror) {
			builder.addMember(memberName, "$T.class", value);
		} else if (value instanceof Enum e) {
			builder.addMember(memberName, "$T.$L", value.getClass(), e.name());
		} else if (value instanceof String s) {
			builder.addMember(memberName, "$S", s);
		} else if (value instanceof Float f) {
			builder.addMember(memberName, "$Lf", f);
		} else if (value instanceof Character c) {
			builder.addMember(memberName, "'$L'", characterLiteralWithoutSingleQuotes(c));
		} else {
			builder.addMember(memberName, "$L", value);
		}
	}

	static String characterLiteralWithoutSingleQuotes(char c) {
		// see https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.10.6
		switch (c) {
			case '\b': return "\\b"; /* \u0008: backspace (BS) */
			case '\t': return "\\t"; /* \u0009: horizontal tab (HT) */
			case '\n': return "\\n"; /* \u000a: linefeed (LF) */
			case '\f': return "\\f"; /* \u000c: form feed (FF) */
			case '\r': return "\\r"; /* \u000d: carriage return (CR) */
			case '\"': return "\"";  /* \u0022: double quote (") */
			case '\'': return "\\'"; /* \u0027: single quote (') */
			case '\\': return "\\\\";  /* \u005c: backslash (\) */
			default:
				return Character.isISOControl(c) ? String.format("\\u%04x", (int) c) : Character.toString(c);
		}
	}
}

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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public enum ProcessorUtil { ;

	static String className(ProcessingEnvironment env, Element e) {
		if (e instanceof TypeElement) {
			return env.getElementUtils().getBinaryName((TypeElement) e).toString();
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
		if (type instanceof DeclaredType) {
			return isClass(env, ((DeclaredType) type).asElement(), cls);
		}
		return false;
	}

}

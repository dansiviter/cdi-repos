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
package uk.dansiviter.cdi.repos;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import jakarta.persistence.EntityManager;

/**
 * Utilities for use with generated repositories.
 */
public enum Util { ;
	/**
	 * Analyses the entity and returns if it is new or not. This is based on the key being either {@code null} or if
	 * it is a primitive number is zero.
	 *
	 * @param entity the entity to analyze.
	 * @param em the entity manager associated with the entity.
	 * @return {@code true} if it is a new entity.
	 */
	public static boolean isNew(Object entity, EntityManager em) {
		var id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
		if (id == null) {
			return true;
		}
		if (id instanceof Number n) {
			return n.intValue() == 0;
		}
		return false;
	}

	/**
	 * Either uses {@link EntityManager#persist(Object) #persist(Object)} or
	 * {@link EntityManager#merge(Object) #merge(Object)} depending on the result of
	 * {@link #isNew(Object, EntityManager)}.
	 *
	 * @param <T> the entity type.
	 * @param entity the entity to save.
	 * @param em the entity manager associated with the entity.
	 * @return the saved value.
	 * @see #isNew(Object, EntityManager)
	 */
	public static <T> T save(T entity, EntityManager em) {
		if (isNew(entity, em)) {
			em.persist(entity);
			return entity;
		}
		return em.merge(entity);
	}

	/**
	 * Horrible convenience method to unwrap the value.
	 *
	 * @param optional
	 * @return
	 */
	public static Integer orElseNull(OptionalInt opt) {
		return opt.isPresent() ? Integer.valueOf(opt.getAsInt()) : null;
	}

	/**
	 * Horrible convenience method to unwrap the value.
	 *
	 * @param optional
	 * @return
	 */
	public static Long orElseNull(OptionalLong opt) {
		return opt.isPresent() ? Long.valueOf(opt.getAsLong()) : null;
	}

	/**
	 * Horrible convenience method to unwrap the value.
	 *
	 * @param optional
	 * @return
	 */
	public static Double orElseNull(OptionalDouble opt) {
		return opt.isPresent() ? Double.valueOf(opt.getAsDouble()) : null;
	}
}

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

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.Supplier;

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
	 * Horrible method that helps unwraps the underlying value.
	 *
	 * @param <T> the type to return.
	 * @param value the value to unwrap.
	 * @return the unwrapped value.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unwrap(T value) {
		if (value instanceof Optional) {
			return ((Optional<T>) value).orElse(null);
		}
		if (value instanceof OptionalInt oi) {
			return (T) (oi.isPresent() ? oi.getAsInt() : null);
		}
		if (value instanceof OptionalLong ol) {
			return (T) (ol.isPresent() ? ol.getAsLong() : null);
		}
		if (value instanceof OptionalDouble od) {
			return (T) (od.isPresent() ? od.getAsDouble() : null);
		}
		if (value instanceof Supplier s) {
			return (T) s.get();
		}
		return (T) value;
	}
}

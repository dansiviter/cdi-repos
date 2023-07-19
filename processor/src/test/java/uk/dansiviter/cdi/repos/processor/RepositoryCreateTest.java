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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.dansiviter.cdi.repos.annotations.Query;
import uk.dansiviter.cdi.repos.annotations.Repository;
import uk.dansiviter.cdi.repos.annotations.Temporal;

/**
 *
 */
@ExtendWith(MockitoExtension.class)
class RepositoryCreateTest {
	@Mock
	EntityManager em;

	Class<?> repoCls;
	MyRepo repo;

	@BeforeEach
	void before() throws ReflectiveOperationException {
		repoCls = Class.forName(MyRepo.class.getName() + "$impl");
		repo = (MyRepo) repoCls.getDeclaredConstructor().newInstance();

		var emField = repoCls.getDeclaredField("em");
		emField.trySetAccessible();
		emField.set(repo, em);
	}

	@Test
	void classFixtures() throws ReflectiveOperationException {
		assertTrue(repoCls.isAnnotationPresent(ApplicationScoped.class));
		assertTrue(repoCls.getDeclaredField("em").isAnnotationPresent(PersistenceContext.class));
	}

	@Test
	void find() {
		var result = repo.find(123);

		assertFalse(result.isPresent());

		verify(this.em).find(MyEntity.class, 123);
	}

	@Test
	void namedParametersQuery(@Mock TypedQuery<MyEntity> query) {
		when(em.createNamedQuery(any(), eq(MyEntity.class))).thenReturn(query);
		when(query.getResultList()).thenReturn(List.of());

		var result = repo.namedParametersQuery(123);
		assertTrue(result.isEmpty());

		verify(this.em).createNamedQuery("namedParametersQuery", MyEntity.class);
    verify(query).setParameter("integer", 123);
    verify(query).getResultList();
	}


	static class MyEntity { }

	interface MyCrudRepo {
		Optional<MyEntity> find(int key);

		void persist(MyEntity key);

		MyEntity persistAndFlush(MyEntity key);

		void merge(MyEntity key);

		MyEntity save(MyEntity entity);

		void saveAndFlush(MyEntity entity);

		void delete(MyEntity entity);

		void flush();
	}

	@Repository
	interface MyRepo extends MyCrudRepo {
		@Query("voidQuery")
		void voidQuery();

		@Query("updateQuery")
		int updateQuery();

		@Query("query")
		List<MyEntity> query(OptionalInt arg);

		@Query(value = "namedParametersQuery", namedParameters = true)
		List<MyEntity> namedParametersQuery(int integer);

		@Query(value = "temporalQuery")
		List<MyEntity> temporalQuery(@Temporal(TemporalType.DATE) Calendar date);

		@Query(value = "optionalQuery")
		Optional<MyEntity> optionalQuery();

		@Query(value = "optionalIntQuery")
		OptionalInt optionalIntQuery();

		@Query("streamQuery")
		Stream<MyEntity> streamQuery();

		EntityManager em();

		default void anotherMethods(String foo) {
			em().clear();
		}
	}
}

package uk.dansiviter.cdi.repos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link Util}.
 */
@ExtendWith(MockitoExtension.class)
class UtilTest {
	@Test
	void isNew_null(@Mock EntityManager em, @Mock EntityManagerFactory emf, @Mock PersistenceUnitUtil util) {
		var entity = new MyEntity(null);
		when(em.getEntityManagerFactory()).thenReturn(emf);
		when(emf.getPersistenceUnitUtil()).thenReturn(util);
		when(util.getIdentifier(entity)).thenReturn(entity.key());

		var isNew = Util.isNew(entity, em);

		assertTrue(isNew);
	}

	@Test
	void isNew_notNull(@Mock EntityManager em, @Mock EntityManagerFactory emf, @Mock PersistenceUnitUtil util) {
		var entity = new MyEntity("foo");
		when(em.getEntityManagerFactory()).thenReturn(emf);
		when(emf.getPersistenceUnitUtil()).thenReturn(util);
		when(util.getIdentifier(entity)).thenReturn(entity.key());

		var isNew = Util.isNew(entity, em);

		assertFalse(isNew);
	}

	@Test
	void isNew_number(@Mock EntityManager em, @Mock EntityManagerFactory emf, @Mock PersistenceUnitUtil util) {
		var entity = new MyEntity(0L);

		when(em.getEntityManagerFactory()).thenReturn(emf);
		when(emf.getPersistenceUnitUtil()).thenReturn(util);
		when(util.getIdentifier(entity)).thenReturn(entity.key());

		var isNew = Util.isNew(entity, em);

		assertTrue(isNew);
	}

	@Test
	void isNew_numberNotZero(@Mock EntityManager em, @Mock EntityManagerFactory emf, @Mock PersistenceUnitUtil util) {
		var entity = new MyEntity(123L);

		when(em.getEntityManagerFactory()).thenReturn(emf);
		when(emf.getPersistenceUnitUtil()).thenReturn(util);
		when(util.getIdentifier(entity)).thenReturn(entity.key());

		var isNew = Util.isNew(entity, em);

		assertFalse(isNew);
	}

	@Test
	void save_persist(@Mock EntityManager em, @Mock EntityManagerFactory emf, @Mock PersistenceUnitUtil util) {
		var entity = new MyEntity(null);

		when(em.getEntityManagerFactory()).thenReturn(emf);
		when(emf.getPersistenceUnitUtil()).thenReturn(util);
		when(util.getIdentifier(entity)).thenReturn(entity.key());

		var result = Util.save(entity, em);

		assertSame(entity, result);
		verify(em).persist(entity);
	}

	@Test
	void save_merge(@Mock EntityManager em, @Mock EntityManagerFactory emf, @Mock PersistenceUnitUtil util) {
		var entity = new MyEntity(123L);
		var updatedEntity = new MyEntity(123L);

		when(em.getEntityManagerFactory()).thenReturn(emf);
		when(emf.getPersistenceUnitUtil()).thenReturn(util);
		when(util.getIdentifier(entity)).thenReturn(entity.key());
		when(em.merge(any())).thenReturn(updatedEntity);

		var result = Util.save(entity, em);

		assertNotSame(entity, result);
		verify(em).merge(entity);
	}

	@Test
	void orElseNull_optionalInt() {
		var optional = OptionalInt.of(123);

		var optionalResult = Util.orElseNull(optional);
		assertEquals(123, optionalResult);

		var optionalEmpty = OptionalInt.empty();

		var optionalEmptyResult = Util.orElseNull(optionalEmpty);
		assertNull(optionalEmptyResult);
	}

	@Test
	void orElseNull_optionalLong() {
		var optional = OptionalLong.of(123L);

		var optionalResult = Util.orElseNull(optional);
		assertEquals(123L, optionalResult);

		var optionalEmpty = OptionalLong.empty();

		var optionalEmptyResult = Util.orElseNull(optionalEmpty);
		assertNull(optionalEmptyResult);
	}

	@Test
	void orElseNull_optionalDouble() {
		var optional = OptionalDouble.of(123D);

		var optionalResult = Util.orElseNull(optional);
		assertEquals(123D, optionalResult);

		var optionalEmpty = OptionalDouble.empty();

		var optionalEmptyResult = Util.orElseNull(optionalEmpty);
		assertNull(optionalEmptyResult);
	}

	record MyEntity(Object key) { }
}

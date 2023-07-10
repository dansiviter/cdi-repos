package uk.dansiviter.cdi.repos;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

import jakarta.persistence.EntityManager;

public enum Util { ;

	public static boolean isNew(Object entity, EntityManager em) {
		var id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entity);
		if (id == null) {
			return true;
		}
		if (id instanceof Number) {
			return ((Number) id).intValue() == 0;
		}
		return false;
	}

	public static void save(Object entity, EntityManager em) {
		if (isNew(entity, em)) {
			em.persist(entity);
		} else {
			em.merge(entity);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T unwrap(T value) {
		if (value instanceof Optional) {
			return ((Optional<T>) value).orElse(null);
		}
		if (value instanceof OptionalInt) {
			var optional = (OptionalInt) value;
			return (T) (optional.isPresent() ? optional.getAsInt() : null);
		}
		if (value instanceof OptionalLong) {
			var optional = (OptionalLong) value;
			return (T) (optional.isPresent() ? optional.getAsLong() : null);
		}
		if (value instanceof OptionalDouble) {
			var optional = (OptionalDouble) value;
			return (T) (optional.isPresent() ? optional.getAsDouble() : null);
		}
		return value;
	}
}

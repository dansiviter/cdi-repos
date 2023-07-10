package uk.dansiviter.cdi.repos.processor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.dansiviter.cdi.repos.annotations.Query;
import uk.dansiviter.cdi.repos.annotations.Repository;

@Repository
@PersistenceContext(type = PersistenceContextType.EXTENDED)
interface Good {
	Optional<MyEntity> find(int key);

	void persist(MyEntity key);

	void merge(MyEntity key);

	void save(MyEntity entity);

	void delete(int key);

	void flush();

	@Query("query")
	void query();

	@Query("query")
	void query(OptionalInt arg);

	// @Query(value = "storedProcedureQuery", storedProcedure = true)
	// List<MyEntity> storedProcedureQuery(int arg);

	@Query(value = "query", namedParameters = true)
	int namedParametersQuery(int arg);

	@Query(value = "query")
	Stream<MyEntity> streamQuery();

	EntityManager em();

	default void anotherMethods(String foo) {
		em().clear();
	}
}

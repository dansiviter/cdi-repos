package uk.dansiviter.cdi.repos.processor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.dansiviter.cdi.repos.annotations.Query;
import uk.dansiviter.cdi.repos.annotations.Repository;

@Repository
@PersistenceContext(unitName = "foo", type = PersistenceContextType.EXTENDED)
interface Good {
	Optional<MyEntity> find(int key);

	MyEntity persist(MyEntity entity);

	void persistAndFlush(MyEntity entity);

	void merge(MyEntity entity);

	void mergeAndFlush(MyEntity entity);

	void save(MyEntity entity);

	@Transactional
	MyEntity saveAndFlush(MyEntity entity);

	void delete(int key);

	void flush();

	@Query("query")
	void query();

	@Query("query")
	void query(OptionalInt arg);

	@Query(value = "query", namedParameters = true)
	int namedParametersQuery(int arg);

	@Query(value = "query")
	@Transactional(value = TxType.MANDATORY, rollbackOn = ExecutionException.class)
	Stream<MyEntity> streamQuery();

	EntityManager em();

	default void anotherMethods(String foo) {
		em().clear();
	}
}

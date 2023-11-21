package uk.dansiviter.cdi.repos.processor;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
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
	long query(OptionalInt arg);

	@Query(value = "query", namedParameters = true)
	int namedParametersQuery(int arg);

	@Query("singleResultQuery")
  BigDecimal singleResultQuery();

	@Query("listQuery")
	List<MyEntity> listQuery();

	@Query("streamQuery")
	@Transactional(value = TxType.MANDATORY, rollbackOn = ExecutionException.class)
	Stream<MyEntity> streamQuery();

	@Query("singleQuery")
	Optional<MyEntity> singleQuery();

	@Query("singleIntQuery")
	OptionalInt singleIntQuery();

	@Query("singleLongQuery")
	OptionalLong singleLongQuery();

	@Query("singleDoubleQuery")
	OptionalDouble singleDoubleQuery();

	EntityManager em();

	default void anotherMethods(String foo) {
		em().clear();
	}
}

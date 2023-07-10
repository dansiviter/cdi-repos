package uk.dansiviter.cdi.repos.processor;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;

import uk.dansiviter.cdi.repos.annotations.Query;
import uk.dansiviter.cdi.repos.annotations.Repository;

@Repository
@PersistenceContext(type = PersistenceContextType.EXTENDED)
interface Foo {
	@Query("")
  void empty();

  void merge(Object arg0, Object arg1);

  @Query(value = "storedProcedure", storedProcedure = true)
  void storedProcedure(Object arg0, Object arg1);
}

package uk.dansiviter.cdi.repos.processor;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.TemporalType;

import uk.dansiviter.cdi.repos.annotations.Query;
import uk.dansiviter.cdi.repos.annotations.Repository;
import uk.dansiviter.cdi.repos.annotations.Temporal;

@Repository
@PersistenceContext(type = PersistenceContextType.EXTENDED)
interface Bad {
	@Query("")
  void emptyQuery();

  void merge(Object arg0, Object arg1);

  @Query("query")
  MyEntity responseQuery(Object arg0, Object arg1);

  @Query("query")
  void temporalQuery(@Temporal(TemporalType.DATE) Object arg0);

  EntityManager emParam(Object arg0);

  @Query("query")
  EntityManager emQuery();
}

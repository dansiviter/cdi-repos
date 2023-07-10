package uk.dansiviter.cdi.repos.processor;

import static jakarta.persistence.PersistenceContextType.EXTENDED;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.lang.Override;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;
import javax.annotation.processing.Generated;
import uk.dansiviter.cdi.repos.Util;

@Generated(
    value = "uk.dansiviter.cdi.repos.processor.RepositoryProcessor",
    comments = "https://cdi-repos.dansiviter.uk",
    date = "2023-02-01T01:02:03.000004Z"
)
@ApplicationScoped
public class Good$impl implements Good {
  @PersistenceContext(
      type = EXTENDED
  )
  private EntityManager em;

  @Override
  public Optional<MyEntity> find(int key) {
    return Optional.ofNullable(this.em.find(MyEntity.class, key));
  }

  @Override
  public void persist(MyEntity key) {
    this.em.persist(key);
  }

  @Override
  public void merge(MyEntity key) {
    this.em.merge(key);
  }

  @Override
  public void save(MyEntity entity) {
    Util.save(entity, this.em);
  }

  @Override
  public void delete(int key) {
    this.em.remove(key);
  }

  @Override
  public void flush() {
    this.em.flush();
  }

  @Override
  public void query() {
    var q = this.em.createNamedQuery("query");
    q.executeUpdate();
  }

  @Override
  public void query(OptionalInt arg) {
    var q = this.em.createNamedQuery("query");
    q.setParameter(1, Util.unwrap(arg));
    q.executeUpdate();
  }

  @Override
  public int namedParametersQuery(int arg) {
    var q = this.em.createNamedQuery("query");
    q.setParameter("arg", Util.unwrap(arg));
    return q.executeUpdate();
  }

  @Override
  public Stream<MyEntity> streamQuery() {
    var q = this.em.createNamedQuery("query");
    return q.getResultStream();
  }

  @Override
  public EntityManager em() {
    return this.em;
  }
}

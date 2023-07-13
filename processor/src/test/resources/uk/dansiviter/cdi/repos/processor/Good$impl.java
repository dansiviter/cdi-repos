package uk.dansiviter.cdi.repos.processor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.transaction.Transactional;
import java.lang.Override;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ExecutionException;
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
      type = PersistenceContextType.EXTENDED
  )
  private EntityManager em;

  @Override
  public Optional<MyEntity> find(int key) {
    return Optional.ofNullable(this.em.find(MyEntity.class, key));
  }

  @Override
  public MyEntity persist(MyEntity entity) {
    this.em.persist(entity);
    return entity;
  }

  @Override
  public void persistAndFlush(MyEntity entity) {
    try {
      this.em.persist(entity);
    } finally {
      this.em.flush();
    }
  }

  @Override
  public void merge(MyEntity entity) {
    this.em.merge(entity);
  }

  @Override
  public void mergeAndFlush(MyEntity entity) {
    try {
      this.em.merge(entity);
    } finally {
      this.em.flush();
    }
  }

  @Override
  public void save(MyEntity entity) {
    Util.save(entity, this.em);
  }

  @Override
  @Transactional
  public MyEntity saveAndFlush(MyEntity entity) {
    try {
      return Util.save(entity, this.em);
    } finally {
      this.em.flush();
    }
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
  @Transactional(
      rollbackOn = ExecutionException.class
  )
  public Stream<MyEntity> streamQuery() {
    var q = this.em.createNamedQuery("query");
    return q.getResultStream();
  }

  @Override
  public EntityManager em() {
    return this.em;
  }
}

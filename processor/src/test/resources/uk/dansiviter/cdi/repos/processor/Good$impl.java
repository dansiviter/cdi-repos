package uk.dansiviter.cdi.repos.processor;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.transaction.Transactional;
import java.lang.Double;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Override;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
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
      type = PersistenceContextType.EXTENDED,
      unitName = "foo"
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
  public long query(OptionalInt arg) {
    var q = this.em.createNamedQuery("query");
    q.setParameter(1, Util.orElseNull(arg));
    return long.class.cast(q.getSingleResult());
  }

  @Override
  public int namedParametersQuery(int arg) {
    var q = this.em.createNamedQuery("query");
    q.setParameter("arg", arg);
    return q.executeUpdate();
  }

  @Override
  public BigDecimal singleResultQuery() {
    var q = this.em.createNamedQuery("singleResultQuery");
    return BigDecimal.class.cast(q.getSingleResult());
  }

  @Override
  public List<MyEntity> listQuery() {
    var q = this.em.createNamedQuery("listQuery", MyEntity.class);
    return q.getResultList();
  }

  @Override
  @Transactional(
      rollbackOn = ExecutionException.class,
      value = Transactional.TxType.MANDATORY
  )
  public Stream<MyEntity> streamQuery() {
    var q = this.em.createNamedQuery("streamQuery", MyEntity.class);
    return q.getResultStream();
  }

  @Override
  public Optional<MyEntity> singleQuery() {
    var q = this.em.createNamedQuery("singleQuery", MyEntity.class);
    return Util.toOptional(q.getResultStream());
  }

  @Override
  public OptionalInt singleIntQuery() {
    var q = this.em.createNamedQuery("singleIntQuery", Integer.class);
    return Util.toOptionalInt(q.getResultStream());
  }

  @Override
  public OptionalLong singleLongQuery() {
    var q = this.em.createNamedQuery("singleLongQuery", Long.class);
    return Util.toOptionalLong(q.getResultStream());
  }

  @Override
  public OptionalDouble singleDoubleQuery() {
    var q = this.em.createNamedQuery("singleDoubleQuery", Double.class);
    return Util.toOptionalDouble(q.getResultStream());
  }

  @Override
  public EntityManager em() {
    return this.em;
  }
}

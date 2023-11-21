[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/dansiviter/cdi-repos/deploy.yaml?style=flat-square)](https://github.com/dansiviter/cdi-repos/actions/workflows/deploy.yaml) [![Known Vulnerabilities](https://snyk.io/test/github/dansiviter/cdi-repos/badge.svg?style=flat-square)](https://snyk.io/test/github/dansiviter/cdi-repos) [![Sonar Coverage](https://img.shields.io/sonar/coverage/dansiviter_cdi-repos?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)](https://sonarcloud.io/dashboard?id=dansiviter_cdi-repos) [![Maven Central](https://img.shields.io/maven-central/v/uk.dansiviter.cdi-repos/cdi-repos-project?style=flat-square)](https://search.maven.org/artifact/uk.dansiviter.cdi-repos/cdi-repos-project) ![Java 17+](https://img.shields.io/badge/-Java%2017%2B-informational?style=flat-square)


# CDI Repositories #

This library is for auto-generating repositories somewhat similar to [Jakarta Data](/jakartaee/data), Spring and Micronaut Data projects. However, instead of performing compilation magic (i.e. no source code) or generating queries from method names (i.e. brittle) it takes a transparent approach which is both minimal and easy to debug.


## Usage ##

This uses [Annotation Processing](https://docs.oracle.com/en/java/javase/11/docs/api/java.compiler/javax/annotation/processing/package-summary.html) to generate an implementation that creates many useful commonly used methods.


Lets get started... first import dependencies...

Maven:
```xml
<dependency>
  <groupId>uk.dansiviter.cdi-repos</groupId>
  <artifactId>cdi-repos</artifactId>
  <version>${cdi-repos.version}</version>
</dependency>
<dependency>
  <groupId>uk.dansiviter.cdi-repos</groupId>
  <artifactId>cdi-repos-processor</artifactId>
  <version>${cdi-repos.version}</version>
  <scope>provided</scope> <!-- only needed during compilation -->
  <optional>true</optional>
</dependency>
```

Gradle:
```
annotationProcessor('uk.dansiviter.cdi-repos:cdi-repos-processor:${cdi-repos.version}')
implementation('uk.dansiviter.cdi-repos:cdi-repos:${cdi-repos.version}')
```

Define a repository interface:
```java
/**
 * Trigger annotation processing by using @Repository. If no PersistenceContext is defined it will use a default value.
 */
@Repository
@PersistenceContext(unitName = "myUnit")
public interface MyRepo {
  // methods will go here
}
```

This will then generate a `@ApplicationScoped` concrete implementation called `MyRepo$impl.java` which can then be inspected and debugged.

> :information_source: This generator does not verify the types are correct for the queries therefore, be careful to correctly specify the correct types or there will be potentially compile and runtime exceptions. It's recommended you perform integration tests to verify these are setup correctly.

There are several method types that can be used; first there are bridge methods. These typically 'bridge' between `EntityManager` methods but there are a few special cases:
```java
  /**
   * This delegates to EntityManager#find(...) and has alternative #get(...), Optionals on return are supported.
   */
  @Transactional
  Optional<MyEntity> find(int id);

  /**
   * Special case: if this has a key this will delegate to EntityManager#merge(...), if not
   * EntityManager#persist(...).
   */
  void save(MyEntity myEntity);

  /**
   * Same as above but will wrapped with a EntityManager#flush(). Returning the entity is supported as is
   * @Transactional on all bridge methods.
   */
  @Transactional
  MyEntity saveAndFlush(MyEntity myEntity);
```

> :information_source: Methods like `#findAll` and `#deleteAll` are purposefully not automatically generated for memory and data loss reasons. If you wish to do this, use the `default` method mechanism.

Then there are query methods:
```java
  /**
   * This uses the 'myQuery' named or named native query. It defaults to positional parameters. Optionals
   * will use null if empty.
   */
  @Query("myQuery")
  List<MyEntity> myQuery(String param0, OptionalInt param1, @Temporal(TemporalType.TIMESTAMP) Calendar param2);

  /**
   * This will use the parameter name for the named parameters. Stream results are supported.
   */
  @Query(value = "myQueryStream", namedParameters = true)
  Stream<MyEntity> myQueryStream(String queryParam);

  /**
   * If you expect a single result using Query#getSingleResult.
   */
  @Query("mySingleResult")
  MyEntity myOptionalSingleResultQuery(String param);

  /**
   * You can expect zero or one result. If more than one result is returned NonUniqueResultException will be thrown.
   */
  @Query("myOptionalSingleResult")
  Optional<MyEntity> myOptionalSingleResultQuery(String param);

  /**
   * If a void, int or long is used then it will use Query#executeUpdate and return the result if possible.
   * @Transactional is supported on all query methods.
   */
  @Query("myUpdateQuery")
  @Transactional
  int myUpdateQuery(String param);
```

Finally, there is custom usage of `EntityManager`:

```java
  /**
   * Expose the EntityManager to be used in the default method. This could be named anything as long as it returns
   * EntityManager.
   */
  EntityManager entityManager();

  /**
   * Then use it...
   */
  @Transactional
  default int customQuery() {
    var em = entityManager();
    var q = em.getCriteriaQueryBuilder().createCriteriaDelete(MyEntity.class);
    return em.createQuery(q).executeUpdate();
  }
```

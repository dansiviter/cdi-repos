[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/dansiviter/cdi-repos/Build?style=flat-square)](https://github.com/dansiviter/cdi-repos/actions/workflows/build.yaml) [![Known Vulnerabilities](https://snyk.io/test/github/dansiviter/cdi-repos/badge.svg?style=flat-square)](https://snyk.io/test/github/dansiviter/cdi-repos) [![Sonar Coverage](https://img.shields.io/sonar/coverage/dansiviter_cdi-repos?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)](https://sonarcloud.io/dashboard?id=dansiviter_cdi-repos) [![Maven Central](https://img.shields.io/maven-central/v/uk.dansiviter.cdi-repos/cdi-repos-project?style=flat-square)](https://search.maven.org/artifact/uk.dansiviter.cdi-repos/cdi-repos-project) ![Java 11+](https://img.shields.io/badge/-Java%2011%2B-informational?style=flat-square)


# CDI Repositories #

This library is for auto-generating repositories somewhat similar to Spring and Micronaut Data projects but more cross platform.


## Usage ##

This uses [Annotation Processing](https://docs.oracle.com/en/java/javase/11/docs/api/java.compiler/javax/annotation/processing/package-summary.html) to generate an implementation that creates many useful commonly used methods.


Lets get started... first import dependencies:

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

Define a logger interface:
```java
@Repository  // used to trigger annotation processing
@PersistenceContext(unitName = "myUnit")  // defaults to 'default' for persistence context if not defined
public interface MyRepo {
  Optional<MyEntity> find(int id);  // delegates to EntityManager methods such as #find(...) optionals supported

  void save(MyEntity myEntity);  // will attempt to check if entity exits and either persist(...) or merge(...)

  void flush();  // flush the persistence context

  @Query(name = "myQuery")  // uses the 'myQuery' named query
  List<MyEntity> myQuery(String param);  // defaults to positional parameters, unless specified in @Query

  @Query(name = "myQuery")
  Stream<MyEntity> myQueryStream(String param);  // Streams are supported

  @Query(name = "myNativeQuery", nativeQuery = true)
  List<MyEntity> myNativeQuery(String param);  // Native queries supported

  @Query(name = "myQuery")
  List<MyEntity> myQuery(OptionalInt param);  // Optionals will resolve to null if empty

  @Query(name = "myUpdateQuery")
  int myUpdateQuery(String param);  // if update or delete return number of entities updated or deleted

  EntityManager entityManager();  // expose underlying EntityManager

  default int customQuery() {  // using above EntityManager for custom usage
    var em = entityManager();
    var q = em.getCriteriaQueryBuilder().createCriteriaDelete(MyEntity.class);
    return em.createQuery(q).executeUpdate();
  }
}
```

This will then generate a `@ApplicationScoped` concrete implementation called `MyRepo$impl.java` which can then be inspected.

> **Note:** This generator does not verify the types are correct for the queries therefore, be careful to correctly specify the correct types or there will be runtime exceptions. It's recommended you perform integration tests to verify these are setup correctly.

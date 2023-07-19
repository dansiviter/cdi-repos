package uk.dansiviter.cdi.repos.processor;

import jakarta.persistence.PersistenceContext;

import uk.dansiviter.cdi.repos.annotations.Repository;

@Repository
@PersistenceContext
public class BadClass { }

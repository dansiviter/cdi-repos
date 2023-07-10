package uk.dansiviter.cdi.repos.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.persistence.TemporalType;

/**
 *
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Temporal {
	TemporalType value();
}

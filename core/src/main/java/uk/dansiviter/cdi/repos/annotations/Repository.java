/*
 * Copyright 2023 Daniel Siviter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.dansiviter.cdi.repos.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Designates the class as a repository.
 * <p>
 * Usage:
 * <pre>
 * &#064;Repository
 * &#064;PersistenceContext(unitName = "myUnit")
 * public interface MyRepo {
 *   Optional&lt;MyObject&gt; find(int key);
 *
 *   &#064;Query("myQuery")
 *   Optional&lt;MyObject&gt; doQuery(int myValue);
 * }
 * </pre>
 *
 * @see Message
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Repository { }

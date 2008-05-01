package org.jboss.seam.annotations.security.permission;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.jboss.seam.security.permission.IdentifierStrategy;

/**
 * Configures the Identifier strategy to use for instance-based permissions.  The specified class
 * should implement the IdentifierStrategy interface.
 *  
 * @author Shane Bryzak
 */
@Target({METHOD,FIELD})
@Documented
@Retention(RUNTIME)
@Inherited
public @interface Identifier
{
   Class<? extends IdentifierStrategy> value();
   String name() default "";
}

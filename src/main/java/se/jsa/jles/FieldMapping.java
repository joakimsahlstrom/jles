package se.jsa.jles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.jsa.jles.FieldValueMapper.IdentityMapper;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface FieldMapping {
	public String value();
	public Class<? extends FieldValueMapper> mapper() default IdentityMapper.class;
}

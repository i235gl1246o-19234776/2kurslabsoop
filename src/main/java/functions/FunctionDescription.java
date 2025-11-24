// src/main/java/functions/annotations/FunctionDescription.java
package functions;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FunctionDescription {
    String displayName();    // Локализованное название
    int displayPriority();   // Приоритет отображения
}
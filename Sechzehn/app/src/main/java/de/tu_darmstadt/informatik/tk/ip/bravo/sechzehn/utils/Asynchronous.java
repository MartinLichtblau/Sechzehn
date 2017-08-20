package de.tu_darmstadt.informatik.tk.ip.bravo.sechzehn.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks method as asynchronous.
 *
 * @author Alexander Geiß on 20.08.2017.
 */
@Documented
@Target(ElementType.METHOD)
public @interface Asynchronous {
}

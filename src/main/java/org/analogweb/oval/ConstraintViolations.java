package org.analogweb.oval;

import java.util.Collection;

/**
 * @author snowgoose
 */
public interface ConstraintViolations<V> {

    Collection<V> all();

}

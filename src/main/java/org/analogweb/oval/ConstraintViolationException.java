package org.analogweb.oval;

import net.sf.oval.ConstraintViolation;

import org.analogweb.core.ApplicationRuntimeException;

/**
 * エントリポイントを構成する値の検証に失敗した場合に投げられる例外です。
 * @author snowgoose
 */
public class ConstraintViolationException extends ApplicationRuntimeException {

    private static final long serialVersionUID = -4408827931821944004L;
    private ConstraintViolations<ConstraintViolation> violations;

    public ConstraintViolationException(ConstraintViolations<ConstraintViolation> violations) {
        this.violations = violations;
    }

    public ConstraintViolations<ConstraintViolation> violations() {
        return this.violations;
    }

}

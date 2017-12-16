package Shamir;

import java.io.Serializable;
import java.math.BigInteger;

public class SecretShare implements Serializable {
    private static final long serialVersionUID = 1L;
    private int x;
    private BigInteger shadow;

    public SecretShare(){

    }

    public SecretShare(final int x, final BigInteger shadow) {
        this.x = x;
        this.shadow = shadow;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public void setShadow(BigInteger shadow) {
        this.shadow = shadow;
    }

    public BigInteger getShadow() {
        return shadow;
    }
}

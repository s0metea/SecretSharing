package Shamir;

import java.math.BigInteger;
import java.util.Random;

public final class Shamir {

    private BigInteger prime;

    private final int t;
    private final int n;
    private final Random random;

    private static final int CERTAINTY = 50;

    public Shamir(final int t, final int n) {
        this.t = t;
        this.n = n;

        random = new Random();
    }

    public SecretShare[] split(final BigInteger secret) {
        final BigInteger[] coefficients = new BigInteger[t - 1];
        final int modLength = secret.bitLength() + 1;

        prime = new BigInteger(modLength, CERTAINTY, random);

        for (int i = 0; i < t - 1; i++) {
            coefficients[i] = generateRandomCoefficient(prime);
        }

        final SecretShare[] shadows = new SecretShare[n];
        for (int i = 1; i <= n; i++) {
            BigInteger shadow = secret;
            for (int j = 1; j < t; j++) {
                final BigInteger t1 = BigInteger.valueOf(i).modPow(BigInteger.valueOf(j), prime);
                final BigInteger t2 = coefficients[j - 1].multiply(t1).mod(prime);
                shadow = shadow.add(t2).mod(prime);
            }
            shadows[i - 1] = new SecretShare(i, shadow);
        }
        return shadows;
    }

    public BigInteger combine(final SecretShare[] choseShadows, final BigInteger primeNumber) {
        BigInteger sum = BigInteger.ZERO;
        for (int i = 0; i < t; i++) {

            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < t; j++) {
                if (i != j) {
                    numerator = numerator.multiply(BigInteger.valueOf(-choseShadows[j].getNumber())).mod
                            (primeNumber);
                    denominator = denominator.multiply(BigInteger.valueOf(choseShadows[i].getNumber() -
                            choseShadows[j].getNumber())).mod(primeNumber);
                }
            }

            final BigInteger value = choseShadows[i].getShadow();

            final BigInteger tmp = value.multiply(numerator).multiply(denominator.modInverse(primeNumber)).mod(primeNumber);
            sum = sum.add(tmp).mod(primeNumber);
        }
        return sum;
    }

    private BigInteger generateRandomCoefficient(final BigInteger p) {
        while (true) {
            final BigInteger r = new BigInteger(p.bitLength(), random);
            if (r.compareTo(BigInteger.ZERO) > 0 && r.compareTo(p) < 0) {
                return r;
            }
        }
    }

    public final class SecretShare {

        private final int number;
        private final BigInteger shadow;

        public SecretShare(final int number, final BigInteger shadow) {
            this.number = number;
            this.shadow = shadow;
        }

        public int getNumber() {
            return number;
        }
        public BigInteger getShadow() {
            return shadow;
        }
    }

    public BigInteger getPrime() {
        return prime;
    }
}
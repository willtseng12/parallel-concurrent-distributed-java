package edu.coursera.concurrent;

import edu.rice.pcdp.Actor;

import static edu.rice.pcdp.PCDP.finish;

/**
 * An actor-based implementation of the Sieve of Eratosthenes.
 * <p>
 * TODO Fill in the empty SieveActorActor actor class below and use it from
 * countPrimes to determin the number of primes <= limit.
 */
public final class SieveActor extends Sieve {
    /**
     * {@inheritDoc}
     * <p>
     * TODO Use the SieveActorActor class to calculate the number of primes <=
     * limit in parallel. You might consider how you can model the Sieve of
     * Eratosthenes as a pipeline of actors, each corresponding to a single
     * prime number.
     */
    @Override
    public int countPrimes(final int limit) {
        final SieveActorActor sieveActor = new SieveActorActor(2);
        finish(() -> {
            for (int i = 3; i <= limit; i += 2) {
                sieveActor.send(i);
            }
            sieveActor.send(0);
        });
        int primeCount = 0;
        SieveActorActor currentActor = sieveActor;
        while (currentActor != null) {
            primeCount += currentActor.numLocalPrime;
            currentActor = currentActor.nextActor;
        }
        return primeCount;
    }

    /**
     * An actor class that helps implement the Sieve of Eratosthenes in
     * parallel.
     */
    public static final class SieveActorActor extends Actor {
        final int MAXLOCALPRIME = 1000;
        final private int[] localPrimes = new int[MAXLOCALPRIME];
        private int numLocalPrime = 0;
        private SieveActorActor nextActor;

        public SieveActorActor(int localPrime) {
            this.localPrimes[0] = localPrime;
            this.numLocalPrime++;
        }

        /**
         * Process a single message sent to this actor.
         * <p>
         * TODO complete this method.
         *
         * @param msg Received message
         */
        @Override
        public void process(final Object msg) {
            int candidate = (int) msg;
            if (candidate <= 0) {
                if (nextActor != null) {
                    nextActor.send(msg);
                }
                return;
            }

            if (isLocalPrime(candidate)) {
                if (numLocalPrime == MAXLOCALPRIME) {
                    if (nextActor == null) {
                        nextActor = new SieveActorActor(candidate);
                    } else {
                        nextActor.send(msg);
                    }
                } else {
                    localPrimes[numLocalPrime++] = candidate;
                }
            }
        }

        private boolean isLocalPrime(int num) {
            for (int i = 0; i < numLocalPrime; i++) {
                if (num % localPrimes[i] == 0) return false;
            }
            return true;
        }
    }
}

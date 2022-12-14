package ca.lerta.fly.inmemory;

/**
 * A primary key generator that generates a sequence of long integers.
 *
 * @author RealLifeDeveloper
 *
 */
public class LongPrimaryKeyGenerator implements PrimaryKeyGenerator<Long> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Long nextPrimaryKey(Long previousMax) {
        if (previousMax == null) {
            return 1L;
        } else {
            return previousMax + 1;
        }
    }
}
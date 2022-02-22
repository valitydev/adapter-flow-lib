package dev.vality.adapter.flow.lib.utils.backoff;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExponentialBackOff implements BackOff {

    public static final Integer DEFAULT_MUTIPLIER = 2;
    public static final Integer DEFAULT_INITIAL_INTERVAL_SEC = 2;
    public static final Integer DEFAULT_MAX_INTERVAL_SEC = 300;

    private Integer multiplier = DEFAULT_MUTIPLIER;
    private Integer initialInterval = DEFAULT_INITIAL_INTERVAL_SEC;
    private Integer maxInterval = DEFAULT_MAX_INTERVAL_SEC;

    private Long startTime;
    private Long currentTime;

    public ExponentialBackOff(
            Long startTime,
            Long currentTime,
            Integer multiplier,
            Integer initialInterval,
            Integer maxInterval) {
        this.startTime = startTime;
        this.currentTime = currentTime;
        this.multiplier = multiplier;
        this.initialInterval = initialInterval;
        this.maxInterval = maxInterval;
    }

    @Override
    public BackOffExecution start() {
        return new ExponentialBackOffExecution();
    }

    private class ExponentialBackOffExecution implements BackOffExecution {
        @Override
        public Long nextBackOff() {
            if (ExponentialBackOff.this.currentTime.equals(ExponentialBackOff.this.startTime)) {
                return Long.valueOf(ExponentialBackOff.this.initialInterval);
            }

            long nextBackOff = computeNextInterval(
                    ExponentialBackOff.this.multiplier,
                    ExponentialBackOff.this.startTime,
                    ExponentialBackOff.this.currentTime);

            if (nextBackOff > ExponentialBackOff.this.maxInterval) {
                nextBackOff = (long) ExponentialBackOff.this.maxInterval;
            }

            return nextBackOff;
        }

        private long computeNextInterval(int multiplier, Long startTime, Long currentTime) {
            long diff = (currentTime - startTime) / 1000;
            if (diff < 1 || multiplier == 1) {
                return initialInterval;
            }
            long result = initialInterval;
            int step = 0;
            while (diff >= result) {
                long pow = (long) Math.pow(multiplier, step++);
                result = initialInterval * pow;
            }
            return result;
        }
    }
}

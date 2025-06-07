package github.hjkim27.util.system;

public abstract class RetryIntervalUtils {

    protected static long cacheTime = System.currentTimeMillis();

    /**
     * 5 minute
     */
    protected static final long cacheInterval = 1000 * 60;

    /**
     * <PRE>
     * if last call 5 minutes ago?
     * return true
     * </PRE>
     *
     * @return
     */
    protected static final boolean isOldMinute(int minute) {
        if (cacheTime - System.currentTimeMillis() > cacheInterval * minute) {
            return true;
        }
        return false;
    }
}

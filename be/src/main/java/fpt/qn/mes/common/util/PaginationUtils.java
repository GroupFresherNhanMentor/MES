package fpt.qn.mes.common.util;

public final class PaginationUtils {

    private PaginationUtils() {
        // Prevent instantiation
    }

    /**
     * Calculates the total number of pages based on total elements and page size.
     *
     * @param totalElements Total count of elements
     * @param pageSize      Size of each page
     * @return Total pages count (0 if totalElements == 0 or pageSize <= 0)
     */
    public static int calculateTotalPages(long totalElements, int pageSize) {
        if (pageSize <= 0 || totalElements <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
    }
}

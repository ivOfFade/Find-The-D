package com.ivofade.findthed.model;

import java.util.Locale;

public class DormFilter {

    private final String keyword;

    public DormFilter(String keyword) {
        if (keyword == null) {
            this.keyword = "";
        } else {
            this.keyword = keyword.trim();
        }
    }

    public boolean matches(Dorm dorm) {
        if (dorm == null) {
            return false;
        }

        if (keyword.isEmpty()) {
            return true;
        }

        String searchText = keyword.toLowerCase(Locale.ROOT);

        // Check the dorm name.
        String dormName = dorm.getDormName().toLowerCase(Locale.ROOT);

        if (dormName.contains(searchText)) {
            return true;
        }

        // Check each amenity.
        for (String amenity : dorm.getAmenities()) {
            String amenityName = amenity.toLowerCase(Locale.ROOT);

            if (amenityName.contains(searchText)) {
                return true;
            }
        }

        return false;
    }

    public String getKeyword() {
        return keyword;
    }
}
package com.mechsync.modules.customerportal.application.usecase;

public final class VinMasker {

    private static final int VISIBLE_SUFFIX_LENGTH = 4;

    private VinMasker() {
    }

    public static String mask(String vin) {
        if (vin == null || vin.isBlank()) {
            return null;
        }
        if (vin.length() <= VISIBLE_SUFFIX_LENGTH) {
            return "*".repeat(vin.length());
        }
        return "*".repeat(vin.length() - VISIBLE_SUFFIX_LENGTH)
                + vin.substring(vin.length() - VISIBLE_SUFFIX_LENGTH);
    }
}

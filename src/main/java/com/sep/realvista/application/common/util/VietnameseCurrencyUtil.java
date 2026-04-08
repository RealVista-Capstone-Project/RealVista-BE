package com.sep.realvista.application.common.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.DayOfWeek;

/**
 * Utility class for formatting currency amounts and dates in Vietnamese style.
 * <p>
 * Provides:
 * <ul>
 *   <li>{@link #formatAmount(BigDecimal)} — formats as "1.500.000" (dot as thousands separator)</li>
 *   <li>{@link #amountToWords(BigDecimal)} — converts amount to Vietnamese words</li>
 *   <li>{@link #getDayOfWeekVietnamese(DayOfWeek)} — returns Vietnamese day name</li>
 * </ul>
 */
public final class VietnameseCurrencyUtil {

    private VietnameseCurrencyUtil() {
        // utility class
    }

    // ── Currency Formatting ───────────────────────────────────────────────────

    /**
     * Formats a BigDecimal amount with dots as thousands separators.
     * <p>
     * Examples: 1500000 → "1.500.000", 250000 → "250.000"
     */
    public static String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(amount.longValue());
    }

    // ── Amount To Vietnamese Words ────────────────────────────────────────────

    private static final String[] UNITS = {
            "", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín"
    };

    private static final String[] TENS = {
            "", "mười", "hai mươi", "ba mươi", "bốn mươi",
            "năm mươi", "sáu mươi", "bảy mươi", "tám mươi", "chín mươi"
    };

    /**
     * Converts a BigDecimal amount to Vietnamese words.
     * <p>
     * Examples:
     * <ul>
     *   <li>1_500_000 → "một triệu năm trăm nghìn đồng"</li>
     *   <li>250_000   → "hai trăm năm mươi nghìn đồng"</li>
     *   <li>5_000_000 → "năm triệu đồng"</li>
     * </ul>
     */
    public static String amountToWords(BigDecimal amount) {
        if (amount == null) {
            return "";
        }
        long value = amount.longValue();
        if (value == 0) {
            return "không đồng";
        }
        return convertToWords(value).trim() + " đồng";
    }

    private static String convertToWords(long number) {
        if (number == 0) {
            return "";
        }

        if (number < 0) {
            return "âm " + convertToWords(-number);
        }

        if (number < 10) {
            return UNITS[(int) number];
        }

        if (number < 100) {
            int ten = (int) (number / 10);
            int unit = (int) (number % 10);
            String result = TENS[ten];
            if (unit != 0) {
                if (ten == 1 && unit == 5) {
                    result += " lăm";
                } else if (ten > 1 && unit == 1) {
                    result += " mốt";
                } else if (ten > 1 && unit == 5) {
                    result += " lăm";
                } else {
                    result += " " + UNITS[unit];
                }
            }
            return result;
        }

        if (number < 1_000) {
            int hundred = (int) (number / 100);
            long remainder = number % 100;
            String result = UNITS[hundred] + " trăm";
            if (remainder > 0) {
                if (remainder < 10) {
                    result += " lẻ " + UNITS[(int) remainder];
                } else {
                    result += " " + convertToWords(remainder);
                }
            }
            return result;
        }

        if (number < 1_000_000) {
            long thousands = number / 1_000;
            long remainder = number % 1_000;
            String result = convertToWords(thousands) + " nghìn";
            if (remainder > 0) {
                if (remainder < 100) {
                    result += " không trăm " + convertToWords(remainder);
                } else {
                    result += " " + convertToWords(remainder);
                }
            }
            return result;
        }

        if (number < 1_000_000_000L) {
            long millions = number / 1_000_000;
            long remainder = number % 1_000_000;
            String result = convertToWords(millions) + " triệu";
            if (remainder > 0) {
                if (remainder < 1_000) {
                    result += " không nghìn " + convertToWords(remainder);
                } else {
                    result += " " + convertToWords(remainder);
                }
            }
            return result;
        }

        if (number < 1_000_000_000_000L) {
            long billions = number / 1_000_000_000L;
            long remainder = number % 1_000_000_000L;
            String result = convertToWords(billions) + " tỷ";
            if (remainder > 0) {
                result += " " + convertToWords(remainder);
            }
            return result;
        }

        // Fallback for very large numbers — just return numeric string
        return String.valueOf(number);
    }

    // ── Day of Week (Vietnamese) ──────────────────────────────────────────────

    /**
     * Returns the Vietnamese name for the given day of week.
     * <p>
     * Examples: MONDAY → "Thứ Hai", SUNDAY → "Chủ Nhật"
     */
    public static String getDayOfWeekVietnamese(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            case SUNDAY -> "Chủ Nhật";
        };
    }
}

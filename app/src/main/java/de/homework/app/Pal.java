package de.homework.app;

/** Farben für dunkles und helles Design. */
final class Pal {
    static boolean light;
    static int bg, card, field, line, text, dim, accent, red;

    static void set(boolean isLight) {
        light = isLight;
        if (isLight) {
            bg = 0xFFF3F3F1; card = 0xFFFFFFFF; field = 0xFFF0F0EE; line = 0xFFDDDEE1;
            text = 0xFF17181B; dim = 0xFF6A6F79; accent = 0xFF3A6FD8; red = 0xFFC4453A;
        } else {
            bg = 0xFF111214; card = 0xFF1B1D21; field = 0xFF24272C; line = 0xFF2E3138;
            text = 0xFFECEDF0; dim = 0xFF8C919B; accent = 0xFF5B8DEF; red = 0xFFE36B5F;
        }
    }
}

package de.homework.app;

import java.util.Locale;

/** Texte: Englisch, Deutsch, Spanisch, Russisch, Kroatisch. */
final class T {
    static final String[] CODES = {"en", "de", "es", "ru", "hr"};
    static final String[] NAMES = {"English", "Deutsch", "Español", "Русский", "Hrvatski"};
    static int lang = 0;

    static String s(String... v) { return v[lang]; }

    static Locale locale() { return Locale.forLanguageTag(CODES[lang]); }

    static String datePattern() {
        return lang == 1 || lang == 4 ? "EEE, d. MMM" : "EEE, d MMM";
    }

    static String title() { return s("Homework", "Hausaufgaben", "Deberes", "Домашка", "Zadaće"); }
    static String empty() { return s("No homework yet.\nTap + to add one.", "Noch keine Hausaufgaben.\nTippe auf +, um eine hinzuzufügen.", "Todavía no hay deberes.\nToca + para añadir uno.", "Пока нет заданий.\nНажми +, чтобы добавить.", "Još nema zadaća.\nDodirni + za dodavanje."); }
    static String allDone() { return s("All done.", "Alles erledigt.", "Todo hecho.", "Всё сделано.", "Sve je gotovo."); }
    static String today() { return s("Today", "Heute", "Hoy", "Сегодня", "Danas"); }
    static String tomorrow() { return s("Tomorrow", "Morgen", "Mañana", "Завтра", "Sutra"); }
    static String yesterday() { return s("Yesterday", "Gestern", "Ayer", "Вчера", "Jučer"); }
    static String inAWeek() { return s("In a week", "In einer Woche", "En una semana", "Через неделю", "Za tjedan dana"); }
    static String overdue() { return s("Overdue", "Überfällig", "Atrasados", "Просрочено", "Kasni"); }
    static String earlier() { return s("Finished earlier", "Früher erledigt", "Terminados antes", "Выполнено раньше", "Ranije završeno"); }
    static String newTask() { return s("New homework", "Neue Hausaufgabe", "Nuevo deber", "Новое задание", "Nova zadaća"); }
    static String editTask() { return s("Edit homework", "Hausaufgabe bearbeiten", "Editar deber", "Изменить задание", "Uredi zadaću"); }
    static String subject() { return s("Subject", "Fach", "Asignatura", "Предмет", "Predmet"); }
    static String subjectHint() { return s("e.g. Maths", "z. B. Mathe", "p. ej. Matemáticas", "напр. Математика", "npr. Matematika"); }
    static String task() { return s("Task", "Aufgabe", "Tarea", "Задание", "Zadatak"); }
    static String taskHint() { return s("e.g. Page 42, no. 3–5", "z. B. S. 42, Nr. 3–5", "p. ej. Pág. 42, ej. 3–5", "напр. стр. 42, № 3–5", "npr. str. 42, zad. 3–5"); }
    static String due() { return s("Due", "Fällig am", "Para el", "Сдать к", "Rok"); }
    static String save() { return s("Save", "Speichern", "Guardar", "Сохранить", "Spremi"); }
    static String cancel() { return s("Cancel", "Abbrechen", "Cancelar", "Отмена", "Odustani"); }
    static String delete() { return s("Delete", "Löschen", "Eliminar", "Удалить", "Obriši"); }
    static String deleteQ() { return s("Delete this homework?", "Diese Hausaufgabe löschen?", "¿Eliminar este deber?", "Удалить это задание?", "Obrisati ovu zadaću?"); }
    static String settings() { return s("Settings", "Einstellungen", "Ajustes", "Настройки", "Postavke"); }
    static String language() { return s("Language", "Sprache", "Idioma", "Язык", "Jezik"); }
    static String theme() { return s("Theme", "Design", "Tema", "Тема", "Tema"); }
    static String dark() { return s("Dark", "Dunkel", "Oscuro", "Тёмная", "Tamna"); }
    static String light() { return s("Light", "Hell", "Claro", "Светлая", "Svijetla"); }
    static String hideDone() { return s("Hide finished homework", "Erledigte ausblenden", "Ocultar los terminados", "Скрыть выполненные", "Sakrij gotove zadaće"); }
    static String clearDone() { return s("Delete finished homework", "Erledigte löschen", "Eliminar los terminados", "Удалить выполненные", "Obriši gotove zadaće"); }
    static String clearQ() { return s("Delete all finished homework?", "Alle erledigten Hausaufgaben löschen?", "¿Eliminar todos los deberes terminados?", "Удалить все выполненные задания?", "Obrisati sve gotove zadaće?"); }
    static String needText() { return s("Enter a subject or a task", "Gib ein Fach oder eine Aufgabe ein", "Escribe una asignatura o una tarea", "Введи предмет или задание", "Upiši predmet ili zadatak"); }
    static String close() { return s("Close", "Schließen", "Cerrar", "Закрыть", "Zatvori"); }
    static String open(int n) {
        switch (lang) {
            case 1: return n + " offen";
            case 2: return n + (n == 1 ? " pendiente" : " pendientes");
            case 3: return "Открыто: " + n;
            case 4: return "Otvoreno: " + n;
            default: return n + " open";
        }
    }
}

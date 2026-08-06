package com.eyebuy.bookcatalog.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BookGenre Enum Tests")
class BookGenreTest {

    @Test
    @DisplayName("BookGenre should have correct display names")
    void displayNames() {
        assertThat(BookGenre.FICTION.getDisplayName()).isEqualTo("小说");
        assertThat(BookGenre.TECHNOLOGY.getDisplayName()).isEqualTo("计算机");
        assertThat(BookGenre.FANTASY.getDisplayName()).isEqualTo("奇幻");
    }

    @Test
    @DisplayName("BookGenre should resolve by name using valueOf")
    void valueOfByName() {
        assertThat(BookGenre.valueOf("FICTION")).isEqualTo(BookGenre.FICTION);
        assertThat(BookGenre.valueOf("TECHNOLOGY")).isEqualTo(BookGenre.TECHNOLOGY);
    }

    @Test
    @DisplayName("BookGenre should have all expected values")
    void allValues() {
        BookGenre[] genres = BookGenre.values();
        assertThat(genres.length).isGreaterThan(10);
        assertThat(genres).contains(
                BookGenre.FICTION,
                BookGenre.NON_FICTION,
                BookGenre.TECHNOLOGY,
                BookGenre.FANTASY,
                BookGenre.HISTORY,
                BookGenre.SCIENCE,
                BookGenre.SELF_HELP,
                BookGenre.BIOGRAPHY,
                BookGenre.BUSINESS,
                BookGenre.CHILDREN,
                BookGenre.RELIGION,
                BookGenre.SPORTS,
                BookGenre.TRAVEL,
                BookGenre.OTHER
        );
    }

    @Test
    @DisplayName("BookGenre should return correct ordinal")
    void ordinals() {
        assertThat(BookGenre.FICTION.ordinal()).isEqualTo(0);
        assertThat(BookGenre.OTHER.ordinal()).isGreaterThan(0);
    }

    @Test
    @DisplayName("All BookGenre values should have non-null display names")
    void allDisplayNamesNonNull() {
        for (BookGenre genre : BookGenre.values()) {
            assertThat(genre.getDisplayName()).isNotNull();
            assertThat(genre.getDisplayName()).isNotEmpty();
        }
    }
}
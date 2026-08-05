package com.eyebuy.bookcatalog.enums;

import lombok.Getter;

@Getter
public enum BookGenre {
    FICTION("小说"),
    NON_FICTION("非虚构"),
    TECHNOLOGY("计算机"),
    FANTASY("奇幻"),
    HISTORY("历史"),
    SCIENCE("科学"),
    SELF_HELP("自助"),
    BIOGRAPHY("传记"),
    BUSINESS("商业"),
    CHILDREN("儿童"),
    RELIGION("宗教"),
    SPORTS("运动"),
    TRAVEL("旅行"),
    OTHER("其他");

    private final String displayName;

    BookGenre(String displayName) {
        this.displayName = displayName;
    }
}

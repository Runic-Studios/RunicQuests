package com.runicrealms.runicquests.event;

public class QuestMenuWrapper {

    private int currentPage;
    private boolean showingRepeatableQuests;

    public QuestMenuWrapper(int currentPage, boolean showingRepeatableQuests) {
        this.currentPage = currentPage;
        this.showingRepeatableQuests = showingRepeatableQuests;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public boolean isShowingRepeatableQuests() {
        return showingRepeatableQuests;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void setShowingRepeatableQuests(boolean showingRepeatableQuests) {
        this.showingRepeatableQuests = showingRepeatableQuests;
    }
}

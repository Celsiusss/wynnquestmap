package celsiuss.wynnquestmap;

import java.util.ArrayList;

public class Quests {
    private ArrayList<Quest> quests;

    public Quests(ArrayList<Quest> quests) {
        this.quests = quests;
    }

    public Quests() {
        this.quests = new ArrayList<Quest>();
    }

    public void add(Quest quest) {
        this.quests.add(quest);
    }

    public void clear() {
        this.quests.clear();
    }

    public String toString() {
        return this.quests.toString();
    }
}

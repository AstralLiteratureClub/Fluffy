package bet.astral.chat.menu;

import bet.astral.messenger.v2.Messenger;

import java.util.ArrayList;

public class ChatMenuBuilder {
    private ArrayList<MenuComponent> components;
    private int valuesPerPage;
    private Messenger messenger;
    private boolean disablePrefixFromMessage = false;

    public ChatMenuBuilder setComponents(ArrayList<MenuComponent> components) {
        this.components = components;
        return this;
    }
    public ChatMenuBuilder setValuesPerPage(int valuesPerPage) {
        this.valuesPerPage = valuesPerPage;
        return this;
    }

    public ChatMenuBuilder setMessenger(Messenger messenger) {
        this.messenger = messenger;
        return this;
    }

    public ChatMenuBuilder setDisablePrefixFromMessage(boolean disablePrefixFromMessage) {
        this.disablePrefixFromMessage = disablePrefixFromMessage;
        return this;
    }

    public ChatMenu build() {
        return new ChatMenu(components, valuesPerPage, messenger, disablePrefixFromMessage);
    }
}
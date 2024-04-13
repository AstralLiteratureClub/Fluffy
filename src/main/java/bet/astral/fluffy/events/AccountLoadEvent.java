package bet.astral.fluffy.events;

import bet.astral.fluffy.statistic.Account;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AccountLoadEvent extends Event {
	private static final HandlerList HANDLER_LIST = new HandlerList();
	@NotNull
	@Getter
	private final Account account;

	public AccountLoadEvent(boolean async, @NotNull Account account) {
		super(async);
		this.account = account;
	}

	@NotNull
	public static final HandlerList getHandlerList(){
		return HANDLER_LIST;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLER_LIST;
	}
}

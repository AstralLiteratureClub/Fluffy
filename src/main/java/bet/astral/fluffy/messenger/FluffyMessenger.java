package bet.astral.fluffy.messenger;

import bet.astral.messenger.v2.paper.PaperMessenger;
import org.slf4j.Logger;

import java.util.Locale;

public class FluffyMessenger extends PaperMessenger {
	private Logger logger;

	public FluffyMessenger() {
		super(null);
		setASync(true);
		setLocale(Locale.US);
		setUseReceiverLocale(false);
	}


	@Override
	public Logger getLogger() {
		return logger;
	}

	public FluffyMessenger setLogger(Logger logger) {
		this.logger = logger;
		return this;
	}
}
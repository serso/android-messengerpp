package org.solovyev.android.messenger.messages;

public enum MessageState {

	created(false, false),
	removed(false, false),

	sending(true, false),
	sent(true, false),

	delivered(true, false),

	received(false, true);

	private final boolean outgoing;
	private final boolean incoming;

	MessageState(boolean outgoing, boolean incoming) {
		this.outgoing = outgoing;
		this.incoming = incoming;
	}

	public boolean isOutgoing() {
		return outgoing;
	}

	public boolean isIncoming() {
		return incoming;
	}
}

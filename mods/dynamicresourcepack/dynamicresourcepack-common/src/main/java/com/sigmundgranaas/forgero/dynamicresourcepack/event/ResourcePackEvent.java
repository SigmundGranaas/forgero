package com.sigmundgranaas.forgero.dynamicresourcepack.event;

import io.reactivex.rxjava3.core.Observable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ResourcePackEvent {
	@NotNull Observable<@Nullable Void> BEFORE_VANILLA = Observable.just(null);

	public class BeforeVanilla() {

	}
}

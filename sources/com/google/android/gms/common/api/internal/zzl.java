package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.Preconditions;

final class zzl {
    private final int zzet;
    private final ConnectionResult zzeu;

    zzl(ConnectionResult connectionResult, int i) {
        Preconditions.checkNotNull(connectionResult);
        this.zzeu = connectionResult;
        this.zzet = i;
    }

    /* access modifiers changed from: 0000 */
    public final ConnectionResult getConnectionResult() {
        return this.zzeu;
    }

    /* access modifiers changed from: 0000 */
    public final int zzu() {
        return this.zzet;
    }
}

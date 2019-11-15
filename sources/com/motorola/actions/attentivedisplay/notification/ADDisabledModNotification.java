package com.motorola.actions.attentivedisplay.notification;

import com.motorola.actions.ActionsApplication;
import com.motorola.actions.C0504R;
import com.motorola.actions.utils.NotificationActionId;
import com.motorola.actions.utils.NotificationId;

public class ADDisabledModNotification extends ADDisabledFeatureBaseNotification {
    /* access modifiers changed from: protected */
    public String getNotificationDontShowAgainAction() {
        return ADModNotificationReceiver.ACTION_AD_MOD_NOTIFICATION_DONT_SHOW_AGAIN;
    }

    /* access modifiers changed from: protected */
    public String getNotificationGotItAction() {
        return ADModNotificationReceiver.ACTION_AD_MOD_NOTIFICATION_GOT_IT;
    }

    /* access modifiers changed from: protected */
    public int getNotificationId() {
        return NotificationId.ACTIONS_AD_MOD_INCOMPATIBILITY.ordinal();
    }

    /* access modifiers changed from: protected */
    public int getGotItRequestCode() {
        return NotificationActionId.ACTIONS_AD_MOD_GOT_IT_ACTION.ordinal();
    }

    /* access modifiers changed from: protected */
    public int getDontShowAgainRequestCode() {
        return NotificationActionId.ACTIONS_AD_MOD_DONT_SHOW_AGAIN_ACTION.ordinal();
    }

    /* access modifiers changed from: protected */
    public String getContentText() {
        return ActionsApplication.getAppContext().getString(C0504R.string.ad_mod_notification_content_text);
    }
}

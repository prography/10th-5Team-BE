package com.example.cherrydan.activity.domain.vo;

import com.example.cherrydan.activity.domain.ActivityAlert;
import com.example.cherrydan.notification.domain.AlertMessage;

import java.util.Map;

public record ActivityAlertMessage(String title,
                                   String body,
                                   String imageUrl,
                                   Map<String, String> data)
    implements AlertMessage {
    private static final String TYPE = "activity_alert";
    private static final String ACTION = "open_activity_page";


    public static ActivityAlertMessage create(ActivityAlert activityAlert){
        Map<String, String> data = Map.of(
                "type", TYPE,
                "alert_type", activityAlert.getAlertType().name(),
                "campaign_id", String.valueOf(activityAlert.getCampaign().getId()),
                "campaign_title", activityAlert.getCampaign().getTitle(),
                "action", ACTION
        );

        return new ActivityAlertMessage(
                activityAlert.getNotificationTitle(),
                activityAlert.getNotificationBody(),
                activityAlert.getCampaign().getImageUrl(),
                data);
    }
}

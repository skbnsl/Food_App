package com.tastenfood.FoodApp.email_notification.services;

import com.tastenfood.FoodApp.email_notification.dtos.NotificationDTO;

public interface NotificationService {

    void sendmail(NotificationDTO notificationDTO);

}

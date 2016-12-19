/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXNotification.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_NOTIFICATIONS_API__H__)
#define __TOBII_TX_NOTIFICATIONS_API__H__

/*********************************************************************************************************************/

/**
  txGetNotificationType

  Gets the TX_NOTIFICATIONTYPE of a notification.
 
  @param hNotification [in]: 
    A TX_CONSTHANDLE to the notification.
    Must not be TX_EMPTY_HANDLE.
 
  @param pNotificationType [out]: 
    A pointer to a TX_NOTIFICATIONTYPE which will be set to the type of the notification.
    Must not be NULL.
 
  @return 
    TX_RESULT_OK: The type of the notification was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetNotificationType(
    TX_CONSTHANDLE hNotification,
    TX_NOTIFICATIONTYPE* pNotificationType
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetNotificationTypeHook)(
    TX_CONSTHANDLE hNotification,
    TX_NOTIFICATIONTYPE* pNotificationType
    );


/*********************************************************************************************************************/

/**
  txGetNotificationData

  Gets the data of a notification.
 
  @param hNotification [in]: 
    A TX_CONSTHANDLE to the notification.
    Must not be TX_EMPTY_HANDLE.
 
  @param phObject [out]: 
    A pointer to a TX_HANDLE to which the handle of the object used as data will be copied.
    This handle must be released using txReleaseObject to avoid leaks.
    Must not be NULL.
    The value of the pointer must be set to TX_EMPTY_HANDLE.
 
  @return 
    TX_RESULT_OK: The data of the notification was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_NOTFOUND: The notification does not have any data.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetNotificationData(
    TX_CONSTHANDLE hNotification,
    TX_HANDLE* phObject
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetNotificationDataHook)(
    TX_CONSTHANDLE hNotification,
    TX_HANDLE* phObject
    );


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_NOTIFICATIONS_API__H__) */

/*********************************************************************************************************************/
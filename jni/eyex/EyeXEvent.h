/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXEvent.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_EVENT_API__H__)
#define __TOBII_TX_EVENT_API__H__

/*********************************************************************************************************************/

/**
  txGetEventInteractorId

  Gets the id of the interactor for which the event should apply.
 
  @param hEvent [in]: 
    A TX_CONSTHANDLE to the event from which the interactor id should be retrieved.
    Must not be TX_EMPTY_HANDLE.
 
  @param pInteractorId [out]: 
    A TX_STRING to which the interactor id will be copied.
    Must be at least the size of the interactor id.
    Can be NULL to only get the size of the interactor.
 
  @param pInteractorIdSize [in,out]: 
    A pointer to a TX_SIZE which will be set to the size of the interactor id.
    Must not be NULL.
    The value must be 0 if pInteractorId is NULL.
 
  @return 
    TX_RESULT_OK: The interactor id or the required size of the string was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_INVALIDBUFFERSIZE: The size of pInteractorId is invalid (*pInteractorIdSize will be set to the required size).
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetEventInteractorId(
    TX_CONSTHANDLE hEvent, 
    TX_STRING pInteractorId,
    TX_SIZE* pInteractorIdSize
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetEventInteractorIdHook)(
    TX_CONSTHANDLE hEvent, 
    TX_STRING pInteractorId,
    TX_SIZE* pInteractorIdSize
    );


/*********************************************************************************************************************/

/**
  txGetEventBehavior

  Gets a behavior with a specified TX_BEHAVIORTYPE from an event.
  If the event does not have a behavior of the specified type this call will fail.
 
  @param hEvent [in]: 
    A TX_CONSTHANDLE to the event from which the behavior should be retrieved.
    Must not be TX_EMPTY_HANDLE.
 
  @param phBehavior [out]: 
    A pointer to a TX_HANDLE which will be set to the behavior.
    This handle must be released using txReleaseObject to avoid leaks.
    Must not be NULL.
    The value of the pointer must be set to TX_EMPTY_HANDLE.
 
  @param behaviorType [in]: 
    The TX_BEHAVIORTYPE which specifies what type of behavior to get.
 
  @return 
    TX_RESULT_OK: The behavior was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_NOTFOUND: This event does not have a behavior of the specified type.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetEventBehavior(
    TX_CONSTHANDLE hEvent,
    TX_HANDLE* phBehavior,
    TX_BEHAVIORTYPE behaviorType
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetEventBehaviorHook)(
    TX_CONSTHANDLE hEvent,
    TX_HANDLE* phBehavior,
    TX_BEHAVIORTYPE behaviorType
    );

  
/*********************************************************************************************************************/

/**
  txGetEventBehaviors

  Gets the TX_HANDLEs to all the behaviors on an event.
 
  @param hEvent [in]: 
    A TX_CONSTHANDLE to the event from which to get the behaviors.
    Must not be TX_EMPTY_HANDLE.
 
  @param phBehaviors [out]: 
    A pointer to an array of TX_HANDLEs to which the behavior handles will be copied.
    These handles must be released using txReleaseObject to avoid leaks.
    Can be NULL to only get the required size.
 
  @param pBehaviorsSize [in,out]: 
    A pointer to a TX_SIZE which will be set to the number of behaviors.
    Must not be NULL.
    The value must be 0 if phBehaviors is NULL.
 
  @return 
    TX_RESULT_OK: The handles or the required size of the buffer was retrieved successfully.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_INVALIDBUFFERSIZE: The size of the array is invalid. (*pBehaviorsSize will be set to the number of behaviors).
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetEventBehaviors(
    TX_CONSTHANDLE hEvent,
    TX_HANDLE* phBehaviors,
    TX_SIZE* pBehaviorsSize
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetEventBehaviorsHook)(
    TX_CONSTHANDLE hEvent,
    TX_HANDLE* phBehaviors,
    TX_SIZE* pBehaviorsSize
    );


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_EVENT_API__H__) */

/*********************************************************************************************************************/

/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXContext.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_CONTEXT_API__H__)
#define __TOBII_TX_CONTEXT_API__H__

/*********************************************************************************************************************/

/**
  txCreateContext

  Creates a new context. 
  A context represents an environment in which the interaction objects live and a single connection to the client.
  Interaction objects may not be shared between contexts.
  A context must be cleaned up using txReleaseContext to avoid leaks.
   
  @param phContext [out]: 
    A pointer to a TX_CONTEXTHANDLE which will be set to the context. 
    Must not be NULL.

  @param trackObjects [in]: 
    Specifies if objects owned by this context should be tracked. 
    Specifying TX_TRUE will give more information on leaking objects when shutting down but comes with
    a performance hit and is not recommended for production builds.
    
  @return 
    TX_RESULT_OK: The context was successfully created.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txCreateContext(
    TX_CONTEXTHANDLE* phContext,
    TX_BOOL trackObjects
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *CreateContextHook)(
    TX_CONTEXTHANDLE* phContext,
    TX_BOOL trackObjects
    );


/*********************************************************************************************************************/

/**
  txReleaseContext

  Releases an existing context.
  If not all objects has been released prior to this call then TX_RESULT_OBJECTLEAKAGE will be returned and the context
  will not be released. The leaking objects may be retreived using txGetTrackedObjects.
    
  @param phContext [in,out]: 
    A pointer to a TX_CONTEXTHANDLE pointing to the context.
    If the context is successfully released the value of this pointer will be set to NULL.
    Must not be NULL.

  @return 
    TX_RESULT_OK: The context was successfully released.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.    
    TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txReleaseContext(
    TX_CONTEXTHANDLE* phContext
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *ReleaseContextHook)(
    TX_CONTEXTHANDLE* phContext
    );


/*********************************************************************************************************************/

/**
  txShutdownContext

  Shuts down a context.
  This call will block until the context has been properly shut down or until the specified timeout has passed.
  If any interaction objects are still alive when a context is being shut down this call will block for the specified
  timeout waiting for other threads to release the objects. If any objects are still not released after this amount 
  of time then TX_RESULT_OBJECTLEAKAGE will be returned. The leaking objects may be retreived using txGetTrackedObjects.

  @param hContext [in]: 
    A TX_CONTEXTHANDLE pointing to the context.
    Must not be NULL.

  @param cleanupTimeout [in]: 
    The amount of time in millseconds to wait for objects to be released.
    Use the constant TX_CLEANUPTIMEOUT_DEFAULT for a default timeout of 500 ms.
    Use the special value TX_CLEANUPTIMEOUT_FORCEIMMEDIATE to shut down the context immediately, without checking for 
    leaking objects.

  @param logLeakingObjectsInfo [in]: 
    Specifies if information about the leaking objects should be logged. To get full details on leaking objects the 
    context must be set up to track objects when created.

  @return 
    TX_RESULT_OK: The context was successfully shut down.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_OBJECTLEAKAGE: All interaction objects have not been released properly. The context was not deleted.
    TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txShutdownContext(
    TX_CONTEXTHANDLE hContext,
    TX_INTEGER cleanupTimeout,
    TX_BOOL logLeakingObjectsInfo
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *ShutdownContextHook)(
    TX_CONTEXTHANDLE hContext,
    TX_INTEGER cleanupTimeout,
    TX_BOOL logLeakingObjectsInfo
    );


/*********************************************************************************************************************/

/**
  txSetContextName

  Sets the name of a context. This name will only be used when logging messages. This method is typically in debugging scenarios
  with multiple contexts.

  @param hContext [in]: 
    A TX_CONTEXTHANDLE for the context on which to set the name.
    Must not be NULL.

  @param name [in]: 
    The name of the context.

  @return 
    TX_RESULT_OK: The name of the context was successfully set.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/

TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txSetContextName(
	TX_CONTEXTHANDLE hContext,
	TX_CONSTSTRING name
	);
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *SetContextNameHook)(
	TX_CONTEXTHANDLE hContext,
	TX_CONSTSTRING name
	);


/*********************************************************************************************************************/

/**
  txSetContextParams

  Sets custom params which gives the context different behaviors. For internal use only.

  @param hContext [in]: 
    A TX_CONTEXTHANDLE for the context on which to set the parameters.
    Must not be NULL.

  @param hParams [in]: 
   A TX_CONSTHANDLE to an object containing the params.

  @return 
    TX_RESULT_OK: The params was successfully set.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/

TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txSetContextParams(
	TX_CONTEXTHANDLE hContext,
	TX_CONSTHANDLE hParams
	);
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *SetContextParamsHook)(
	TX_CONTEXTHANDLE hContext,
	TX_CONSTHANDLE hParams
	);


/*********************************************************************************************************************/

/**
  txGetContextName

  Gets the name of a context.
 
 @param hContext [in]: 
    A TX_CONTEXTHANDLE pointing to the context.
    Must not be NULL.

  @param pName [out]: 
    A TX_STRING to which the context name will be copied.
    Must be at least the size of the context name.
    Can be NULL to only get the size of the context name.
 
  @param pNameSize [in,out]: 
    A pointer to a TX_SIZE which will be set the size of the context name.
    Must not be NULL. 
    The value must be 0 if pNameSize is NULL.
 
  @return 
    TX_RESULT_OK: The context name or the required size of the string was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_INVALIDBUFFERSIZE: The size of pName is invalid (*pNameSize will be set to the required size).
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetContextName(
    TX_CONTEXTHANDLE hContext, 
    TX_STRING pName,
    TX_SIZE* pNameSize
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetContextNameHook)(
    TX_CONTEXTHANDLE hContext, 
    TX_STRING pName,
    TX_SIZE* pNameSize
    );


/*********************************************************************************************************************/

/**
  txGetTrackedObjects

  Gets all the objects that currently tracked by a context. This requires the context to be set up to track objects.
  
  @param hContext [in]:
    A TX_CONSTCONTEXTHANDLE to the context from which to get the tracked objects.
    Must not be TX_EMPTY_HANDLE.
  
  @param phObjects [in]: 
    A pointer to an array of TX_HANDLEs to which the property handles will be copied.
    These handles must be released using txReleaseObject to avoid leaks.
    Can be NULL to only retreive the size.
  
  @param pObjectsSize [in,out]:
    A pointer to a TX_SIZE which will be set to the number of objects.
    Must not be NULL.
    The value must be 0 if phObjects is NULL.
  
  @return 
     TX_RESULT_OK: The objects or required sie of the buffer were successfully retrieved.
     TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
     TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
     TX_RESULT_INVALIDBUFFERSIZE: The size of the buffer was to small. (*pObjectSize will be set to the required size.)
     TX_RESULT_OBJECTTRACKINGNOTENABLED: The specified context has not been set up to track objects.
 */ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetTrackedObjects(
    TX_CONSTCONTEXTHANDLE hContext,
    TX_HANDLE* phObjects,
    TX_SIZE* pObjectsSize
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetTrackedObjectsHook)(
    TX_CONSTCONTEXTHANDLE hContext,
    TX_HANDLE* phObjects,
    TX_SIZE* pObjectsSize
    );


/*********************************************************************************************************************/

/**
  txEnableConnection

  Enables the connection to the client.
  This method must be called to start the communication between the client and server.
  Once the connection has been enabled the client will attempt to always keep it alive. If the connection
  to the client for some reason goes down, the client will immediately attemt to reconnect.
  See txRegisterConnectionStateChangedHandler to get notified of the current connection state.
 
  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context which should get its connection enabled.
    Must not be TX_EMPTY_HANDLE.
  
  @return 
    TX_RESULT_OK: The connection was successfully enabled (does not mean that a connection to the client has been established).
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txEnableConnection(
    TX_CONTEXTHANDLE hContext 
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *EnableConnectionHook)(
    TX_CONTEXTHANDLE hContext 
    );


/*********************************************************************************************************************/

/**
  txDisableConnection

  Disables the connection to the client.
 
  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context which should get its connection disabled.
    Must not be TX_EMPTY_HANDLE.
  
  @return 
    TX_RESULT_OK: The connection was successfully disabled.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txDisableConnection(
    TX_CONTEXTHANDLE hContext 
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *DisableConnectionHook)(
    TX_CONTEXTHANDLE hContext 
    );


/*********************************************************************************************************************/

/**
  txGetConnectionState

  Gets the current connection state of the context.
 
  @param hContext [in]: 
    A TX_CONSTCONTEXTHANDLE to the context from which to retrieve the connection state.
    Must not be TX_EMPTY_HANDLE.
 
  @param pConnectionState [out]:
	A pointer to a TX_CONNECTIONSTATE which will get the current connection state.
	Must not be NULL.
 
  @return 
    TX_RESULT_OK: The current connection state was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetConnectionState(
    TX_CONSTCONTEXTHANDLE hContext,
	TX_CONNECTIONSTATE* pConnectionState
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetConnectionStateHook)(
    TX_CONSTCONTEXTHANDLE hContext,
	TX_CONNECTIONSTATE* pConnectionState
    );


/*********************************************************************************************************************/

/**
  txRegisterConnectionStateChangedHandler
  
  Registers a callback which will be invoked when the connection state changes. Use txEnableConnection
  to initiate a connection to the engine.
  
  @param hContext [in]:
    A TX_CONTEXTHANDLE to the context on which to register the callback.
    Must not be TX_EMPTY_HANDLE.
  
  @param pTicket [out]:
    A pointer to a TX_TICKET which will represent this registration.
    This ticket should be used for unregistration.
    Must not be NULL.
  
  @param handler [in]:
    A TX_CONNECTIONSTATECHANGEDCALLBACK which will be called when the connection state changes.
    Must not be NULL.
  
  @param userParam [in]:
    A TX_USERPARAM which will be provided as a parameter to the callback.
    Can be NULL and will in this case be ignored.
  
  @return
    TX_RESULT_OK: The callback was successfully registered.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
	TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txRegisterConnectionStateChangedHandler(
	TX_CONTEXTHANDLE hContext,
	TX_TICKET* pTicket,
	TX_CONNECTIONSTATECHANGEDCALLBACK handler,
	TX_USERPARAM userParam
	);
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *RegisterConnectionStateChangedHandlerHook)(
	TX_CONTEXTHANDLE hContext,
	TX_TICKET* pTicket,
	TX_CONNECTIONSTATECHANGEDCALLBACK handler,
	TX_USERPARAM userParam
	);


/*********************************************************************************************************************/

#if defined(__cplusplus)
#ifndef TOBII_TX_INTEROP
#include <functional>

    TX_API_FUNCTION_CPP(RegisterConnectionStateChangedHandler,(
        TX_CONTEXTHANDLE hContext,
        TX_TICKET* pTicket,
        const Tx::ConnectionStateChangedCallback& handler));
#endif
#endif

/*********************************************************************************************************************/

/**
  txUnregisterConnectionStateChangedHandler

  Unregisters a callback previously registered for connection state changes.
  This function may not be called on a callback thread.
 
  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to unregister the callback.
    Must not be TX_EMPTY_HANDLE.
 
  @param ticket [in]: 
    A TX_TICKET which represents the registration. 
    Must not be TX_INVALID_TICKET.
   
  @return 
    TX_RESULT_OK: The callback was successfully unregistered.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_NOTFOUND: A registration for the specified ticket could not be found.   
	TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txUnregisterConnectionStateChangedHandler(
    TX_CONTEXTHANDLE hContext, 
    TX_TICKET ticket    
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *UnregisterConnectionStateChangedHandlerHook)(
    TX_CONTEXTHANDLE hContext, 
    TX_TICKET ticket    
    );


/*********************************************************************************************************************/

/**
  txRegisterMessageHandler

  Registers a callback to be invoked when a message of a specific type arrives.
  This function may not be called on a callback thread.
 
  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to register the callback.
    Must not be TX_EMPTY_HANDLE.
 
  @param pTicket [out]: 
    A pointer to a TX_TICKET which will represent this registration. 
    This ticket should be used for unregistration.
    Must not be NULL.
  
  @param messsageType [in]: 
    The TX_MESSAGETYPE for which to register the callback.
 
  @param hOptions [in]: 
    A TX_HANDLE to an interaction object containing the options for this registration.
    The following options should be provided for the following message types:
 
    TX_MESSAGETYPE_QUERY:
      - TX_LITERAL_PROCESSID: The id of the process for which to get queries.
 
  @param handler [in]: 
    A TX_ASYNCDATACALLBACK which will be called when a message of the specified type arrives.
    When this callback is invoked it is passed a parameter hAsyncData which contains the message as its content.
	The content of the async data can be retrieved using txGetAsyncDataContent.
	That handle to the async data must NOT be released as it is automatically released by the API when the callback returns.
    Must not be NULL.
 
  @param userParam [in]: 
    A TX_USERPARAM which will be provided as a parameter to the callback.
    Can be NULL and will in this case be ignored.
 
  @return 
    TX_RESULT_OK: The callback was successfully registered.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.   	
    TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txRegisterMessageHandler(
    TX_CONTEXTHANDLE hContext, 
    TX_TICKET* pTicket,
    TX_MESSAGETYPE messageType,
    TX_HANDLE hOptions,
    TX_ASYNCDATACALLBACK handler, 
    TX_USERPARAM userParam
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *RegisterMessageHandlerHook)(
    TX_CONTEXTHANDLE hContext, 
    TX_TICKET* pTicket,
    TX_MESSAGETYPE messageType,
    TX_HANDLE hOptions,
    TX_ASYNCDATACALLBACK handler, 
    TX_USERPARAM userParam
    );


/*********************************************************************************************************************/

/**
  txUnregisterMessageHandler

  Unregisters a callback previously registered as a message handler.
 
  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to unregister the callback.
    Must not be TX_EMPTY_HANDLE.
 
  @param ticket [in]: 
    A TX_TICKET which represents the registration. 
    Must not be TX_INVALID_TICKET
   
  @return 
    TX_RESULT_OK: The callback was successfully unregistered.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_NOTFOUND: A registration for the specified ticket could not be found.
	TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txUnregisterMessageHandler(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET ticket
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *UnregisterMessageHandlerHook)(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET ticket
    );


/*********************************************************************************************************************/

/**
  txRegisterQueryHandler

  Registers a query handler.

  This will setup a subscription to receive interactor queries from the engine.

  This is a specialization of txRegisterMessageHandler.
 
   @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to register the callback.
    Must not be TX_EMPTY_HANDLE.

  @param pTicket [out]: 
    A pointer to a TX_TICKET which will represent this registration. 
    This ticket should be used for unregistration.
    Must not be NULL.

  @param handler [in]: 
    A TX_ASYNCDATACALLBACK which will be called when a query arrives.
    When this callback is invoked it is passed a parameter hAsyncData which contains the query as its content.
	The content of the async data can be retrieved using txGetAsyncDataContent().
    That handle to the async data must NOT be released as it is automatically released by the API when the callback returns.
    Must not be NULL.

  @param userParam [in]: 
    A TX_USERPARAM which will be provided as a parameter to the callback.

  @return 
    TX_RESULT_OK: The Query Handler was successfully registered.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
	TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txRegisterQueryHandler(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET* pTicket,
    TX_ASYNCDATACALLBACK handler,
    TX_USERPARAM userParam
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *RegisterQueryHandlerHook)(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET* pTicket,
    TX_ASYNCDATACALLBACK handler,
    TX_USERPARAM userParam
    );


/*********************************************************************************************************************/

/**
  txUnregisterQueryHandler

  Unregisters a previously registered query handler callback 
  This is a specialization of txUnregisterMessageHandler.

  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to unregister the callback.
    Must not be TX_EMPTY_HANDLE.
 
  @param ticket [in]: 
    A TX_TICKET which represents the registration. 
    Must not be TX_INVALID_TICKET
 
  @return 
    TX_RESULT_OK: The callback was successfully unregistered.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_NOTFOUND: A registration for the specified ticket could not be found.
	TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txUnregisterQueryHandler(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET ticket 
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *UnregisterQueryHandlerHook)(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET ticket 
    );


/*********************************************************************************************************************/

/**
  txRegisterEventHandler

  Registers an event handler. This will setup a subscription to receive interaction events
  from the engine.

  This is a specialization of txRegisterMessageHandler.
 
   @param hContext [in]:   
    A TX_CONTEXTHANDLE to the context on which to register the callback.
    Must not be TX_EMPTY_HANDLE.

  @param pTicket [out]: 
    A pointer to a TX_TICKET which will represent this registration. 
    This ticket should be used for unregistration.
    Must not be NULL.

  @param handler [in]: 
    A TX_ASYNCDATACALLBACK which will be called when an event arrives.
    When this callback is invoked it is passed a parameter hAsyncData which contains the event as its content.
	The content of the async data can be retrieved using txGetAsyncDataContent().
    That handle to the async data must NOT be released as it is automatically released by the API when the callback returns.
    Must not be NULL.

  @param userParam [in]: 
    A TX_USERPARAM which will be provided as a parameter to the callback.

  @return 
    TX_RESULT_OK: The event handler was successfully registered.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
	TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txRegisterEventHandler(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET* pTicket,
    TX_ASYNCDATACALLBACK handler,
    TX_USERPARAM userParam
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *RegisterEventHandlerHook)(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET* pTicket,
    TX_ASYNCDATACALLBACK handler,
    TX_USERPARAM userParam
    );


/*********************************************************************************************************************/

/**
  txUnregisterEventHandler

  Unregisters a previously registered event handler callback 
  This is a specialization of txUnregisterMessageHandler.

  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to unregister the callback.
    Must not be TX_EMPTY_HANDLE.
 
  @param ticket [in]: 
    A TX_TICKET which represents the registration. 
    Must not be TX_INVALID_TICKET
 
  @return 
    TX_RESULT_OK: The callback was successfully unregistered.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_NOTFOUND: A registration for the specified ticket could not be found.
	TX_RESULT_INVALIDTHREAD: Attempted to call the function from a callback thread.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txUnregisterEventHandler(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET ticket  
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *UnregisterEventHandlerHook)(
    TX_CONTEXTHANDLE hContext,
    TX_TICKET ticket  
    );


/*********************************************************************************************************************/

/**
  txPerformScheduledJobs

  Performs all jobs scheduled by the API on the thread that calls this function.
  This call will block until all jobs have been performed. For internal use only.

  @param hContext [in]: 
	A TX_CONTEXTHANDLE to the context which jobs should be performed.
	
  @return 
    TX_RESULT_OK: The callback was successfully unregistered.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
	TX_RESULT_INVALIDSCHEDULINGMODE: The scheduling mode must be set TX_SCHEDULINGMODE_USERFRAME for this call to be valid.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txPerformScheduledJobs(
	TX_CONTEXTHANDLE hContext
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *PerformScheduledJobsHook)(
	TX_CONTEXTHANDLE hContext
    );


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_CONTEXT_API__H__) */

/*********************************************************************************************************************/

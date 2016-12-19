/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXEnv.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_ENV_API__H__)
#define __TOBII_TX_ENV_API__H__

/*********************************************************************************************************************/

/**
  txInitializeEyeX

  Initializes the Tobii EyeX client environment.
  This function must be called prior to any other in the API, except txGetEyeXAvailability and txEnableMonoCallbacks.
  A client can choose to override the default memory model, threading model and logging model by supplying custom models
  to this function.

  @param flags [in]:
    Specifies which components to override.
    
  @param pLoggingModel [in]:
    A pointer to a TX_LOGGINGMODEL which will override the default model.
    This argument can be NULL to use the default logging model.
        
  @param pThreadingModel [in]:
    A pointer to a TX_THREADINGMODEL which will override the default model.
    This argument can be NULL to use the default threading model. Any 
    non-NULL value is for internal use only.

  @param pSchedulingModel [in]:
	A pointer to a TX_SCHEDULINGMODEL which will override the default model.
	This argument can be NULL to use the default scheduling model. Any 
    non-NULL value is for internal use only.

  @param pMemoryModel [in]:
	Reserved for future use.

  @return 
    TX_RESULT_OK: The client environment was successfully initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_EYEXALREADYINITIALIZED: The EyeX client environment is already initialized.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txInitializeEyeX(
    TX_EYEXCOMPONENTOVERRIDEFLAGS flags,
    const TX_LOGGINGMODEL* pLoggingModel,
    const TX_THREADINGMODEL* pThreadingModel,
	const TX_SCHEDULINGMODEL* pSchedulingModel,
    void* pMemoryModel
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *InitializeEyeXHook)(
    TX_EYEXCOMPONENTOVERRIDEFLAGS flags,
    const TX_LOGGINGMODEL* pLoggingModel,
    const TX_THREADINGMODEL* pThreadingModel,
	const TX_SCHEDULINGMODEL* pSchedulingModel,
    void* pMemoryModel
    );


/*********************************************************************************************************************/

/**
  txUninitializeEyeX

  Uninitializes the EyeX client environment.
  If any context is still active this call will fail.
  
  @return 
    TX_RESULT_OK: The client environment was successfully uninitialized.
    TX_RESULT_EYEXNOTINITIALIZED: The client environment is not initialized.
    TX_RESULT_EYEXSTILLINUSE: The EyeX client environment is still in use.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txUninitializeEyeX();
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *UninitializeEyeXHook)();


/*********************************************************************************************************************/

/**
  txIsEyeXInitialized

  Checks if the EyeX client environment has been initialized.
  
  @param pInitialized [out]: 
    A pointer to a TX_BOOL which will be set to true if the environment is initialized and false otherwise.
    Must not be NULL.

  @return 
    TX_RESULT_OK: The operation was successful.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txIsEyeXInitialized(
    TX_BOOL* pInitialized
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *IsEyeXInitializedHook)(
    TX_BOOL* pInitialized
    );


/*********************************************************************************************************************/

/**
  txWriteLogMessage

  Writes a message using the internal logging model. 
  This method is typically not intended for end users but rather for the different language bindings to have a common
  way of utilizing the logging model.

  @param level [in]: 
    The log level for this message.

  @param scope [in]: 
    The scope for this message.

  @param message [in]:
    The log message it self.

  @return
    TX_RESULT_OK: The message was successfully written to the log.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
  */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txWriteLogMessage(
    TX_LOGLEVEL level,
    TX_CONSTSTRING scope,
    TX_CONSTSTRING message
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *WriteLogMessageHook)(
    TX_LOGLEVEL level,
    TX_CONSTSTRING scope,
    TX_CONSTSTRING message
    );


/*********************************************************************************************************************/
/**
  txSetInvalidArgumentHandler

  Sets a hook that notifies when an invalid argument has been passed to any of the API function.
  This function should typically only be used for testing purposes.

  @param handler [in]: 
    The callback to be invoked when an invalid argument is detected.
	
  @param userParam [in]:
    A TX_USERPARAM which will be provided as a parameter to the callback.
    Can be NULL and will in this case be ignored.

  @return 
    TX_RESULT_OK: The invalid argument handler was successful set.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txSetInvalidArgumentHandler(
    TX_INVALIDARGUMENTCALLBACK handler,
    TX_USERPARAM userParam
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *SetInvalidArgumentHandlerHook)(
    TX_INVALIDARGUMENTCALLBACK handler,
    TX_USERPARAM userParam
    );


/*********************************************************************************************************************/
/**
  txEnableMonoCallbacks

  Prepares the EyeX client library for use with the Mono .NET runtime: before a callback function is invoked, the 
  thread on which the callback will be made is attached to a mono domain, and the thread is detached again when the 
  callback function returns. Mono requires that any threads calling managed code be attached for garbage collection 
  and soft debugging to work properly.
  
  This function must be called prior to any other in the API, and from a managed thread. The subsequent callback 
  invocations will be attached to the same mono domain as the caller thread.

  Note that Mono callbacks cannot be used in combination with a custom threading model.

  @param monoModuleName [in]: 
    The name of the Mono runtime module (dll). Typically "mono".
 
  @return 
    TX_RESULT_OK: The mono callbacks were successfully enabled.
    TX_RESULT_INVALIDARGUMENT: The Mono module name could not be used to resolve the necessary Mono functions.
  */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txEnableMonoCallbacks(
	TX_CONSTSTRING monoModuleName
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *EnableMonoCallbacksHook)(
	TX_CONSTSTRING monoModuleName
    );


/*********************************************************************************************************************/
/**
  txGetEyeXAvailability

  Gets the availability of the EyeX Engine.

  @param pEyeXAvailability [out]:
    The availability of EyeX Engine.
  
  @return
    TX_RESULT_OK: The status was fetched successfully.
	TX_RESULT_INVALIDARGUMENT: An invalid argument was supplied.
  */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetEyeXAvailability(
	TX_EYEXAVAILABILITY* pEyeXAvailability
	);
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetEyeXAvailabilityHook)(
	TX_EYEXAVAILABILITY* pEyeXAvailability
	);


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_ENV_API__H__) */

/*********************************************************************************************************************/

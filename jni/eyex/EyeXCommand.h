/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXCommand.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_COMMAND_API__H__)
#define __TOBII_TX_COMMAND_API__H__

/*********************************************************************************************************************/

/**
  txCreateCommand. For internal use only.

  Creates a command. Internal: In first hand, prefer a higher abstraction before
  sending raw commands to the engine.
 
  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to create the command.
    Must not be TX_EMPTY_HANDLE.
  
  @param phCommand [out]: 
    A pointer to a TX_HANDLE which will be set to the newly created command.
    This handle must be released using txReleaseObject to avoid leaks.
    Must not be NULL.
    The value of the pointer must be set to TX_EMPTY_HANDLE.

  @param commandType [in]
    The type of the command.
 
  @return 
    TX_RESULT_OK: The command was successfully created.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_INVALIDCONTEXT: The handle to the context was invalid.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txCreateCommand(
    TX_CONTEXTHANDLE hContext,
    TX_HANDLE* phCommand,
    TX_COMMANDTYPE commandType
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *CreateCommandHook)(
    TX_CONTEXTHANDLE hContext,
    TX_HANDLE* phCommand,
    TX_COMMANDTYPE commandType
    );


/*********************************************************************************************************************/

/**
  txGetCommandType. For internal use only.

  Gets the TX_COMMANDTYPE of a command.
 
  @param hCommand [in]: 
    A TX_CONSTHANDLE to the command.
    Must not be TX_EMPTY_HANDLE.
 
  @param pCommandType [out]: 
    A pointer to a TX_COMMANDTYPE which will be set to the type of the command.
    Must not be NULL.
 
  @return 
    TX_RESULT_OK: The type of the command was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetCommandType(
    TX_CONSTHANDLE hCommand,
    TX_COMMANDTYPE* pCommandType
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetCommandTypeHook)(
    TX_CONSTHANDLE hCommand,
    TX_COMMANDTYPE* pCommandType
    );


/*********************************************************************************************************************/

/**
  txSetCommandData. For internal use only.

  Sets the data of a command.
  If the command already has some object set as data it will be replaced and released.
 
  @param hCommand [in]: 
    A TX_HANDLE to the command.
    Must not be TX_EMPTY_HANDLE.
 
  @param hObject [in]: 
    A TX_HANDLE to the object that should represent the data of the command.
    Must not be NULL.
 
  @return 
    TX_RESULT_OK: The data of the command was successfully set.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txSetCommandData(
    TX_HANDLE hCommand,
    TX_HANDLE hObject
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *SetCommandDataHook)(
    TX_HANDLE hCommand,
    TX_HANDLE hObject
    );


/*********************************************************************************************************************/

/**
  txGetCommandData. For internal use only.

  Gets the data of a command.
 
  @param hCommand [in]: 
    A TX_CONSTHANDLE to the command.
    Must not be TX_EMPTY_HANDLE.
 
  @param phObject [out]: 
    A pointer to a TX_HANDLE to which the handle of the object used as data will be copied.
    This handle must be released using txReleaseObject to avoid leaks.
    Must not be NULL.
    The value of the pointer must be set to TX_EMPTY_HANDLE.
 
  @return 
    TX_RESULT_OK: The data of the command was successfully set.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_NOTFOUND: The command does not have any data.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetCommandData(
    TX_CONSTHANDLE hCommand,
    TX_HANDLE* phObject
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetCommandDataHook)(
    TX_CONSTHANDLE hCommand,
    TX_HANDLE* phObject
    );


/*********************************************************************************************************************/

/**
  txExecuteCommandAsync. For internal use only.

  Executes a command asynchronously.
 
  @param hCommand [in]: 
    A TX_HANDLE to the command.
    Must not be TX_EMPTY_HANDLE.

  @param completionHandler [in]:
    The TX_ASYNCDATACALLBACK that will handle the command result.
    Can be NULL.

	The data provided by the TX_ASYNCDATACALLBACK will contain a result code which can be retrieved using 
	txGetAsyncDataResult(). The result code will be one of the follwing:

		TX_RESULT_OK: 
			The command was succesfully executed on the client.
						
		TX_RESULT_INVALIDCOMMAND: 
			The command was rejected by the client.
			
		TX_RESULT_CANCELLED:
			The asynchronous operation was cancelled.

	That handle to the async data must NOT be released.

  @param userParam [in]:
    A TX_USERPARAM which will be provided as a parameter to the completion callback. 
    Can be NULL.
 
  @return 
    TX_RESULT_OK: The command was successfully executed. The actual result of the command will be provided to the callback.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txExecuteCommandAsync(
    TX_HANDLE hCommand,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *ExecuteCommandAsyncHook)(
    TX_HANDLE hCommand,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam
    );


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_COMMAND_API__H__) */

/*********************************************************************************************************************/

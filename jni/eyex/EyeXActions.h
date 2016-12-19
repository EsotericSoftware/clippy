/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXAction.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_ACTION_API__H__)
#define __TOBII_TX_ACTION_API__H__

/*********************************************************************************************************************/

/**
  txCreateActionCommand

  Creates an Action command.
 
  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to create the command.
    Must not be TX_EMPTY_HANDLE.
  
  @param phCommand [out]: 
    A pointer to a TX_HANDLE which will be set to the newly created command.
    This handle must be released using txReleaseObject to avoid leaks.
    Must not be TX_EMPTY_HANDLE.

  @param action [in]:
    The type of action.  
 
  @return 
    TX_RESULT_OK: The command was successfully created.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txCreateActionCommand(
    TX_CONTEXTHANDLE hContext,
    TX_HANDLE* phCommand,
    TX_ACTIONTYPE actionType
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *CreateActionCommandHook)(
    TX_CONTEXTHANDLE hContext,
    TX_HANDLE* phCommand,
    TX_ACTIONTYPE actionType
    );


/*********************************************************************************************************************/

/**
  txDisableBuiltinKeysAsync

  Disables EyeX builtin keys for a top-level window. When the gaze is over the
  specified window, all interaction must be done through action commands.
 
  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to disable keys.
    Must not be TX_EMPTY_HANDLE.
  
  @param windowId [in]: 
    The window id for which to disable keys (window id corresponds to the windows handle on Windows).

  @param completionHandler [in]:
    The TX_ASYNCDATACALLBACK that will handle the request result.
    Can be NULL.

    The data provided by the TX_ASYNCDATACALLBACK will contain a result code which can be retrieved using 

        TX_RESULT_OK: 
            The request was succesfully executed on the client.
                                    
        TX_RESULT_CANCELLED:
            The asynchronous operation was cancelled.

    That handle to the async data must NOT be released.

  @param userParam [in]:
    A TX_USERPARAM which will be provided as a parameter to the completion callback. 
    Can be NULL.
 
  @return 
    TX_RESULT_OK: The request was successfully sent.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txDisableBuiltinKeys(
    TX_CONTEXTHANDLE hContext,
    TX_CONSTSTRING windowId,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *DisableBuiltinKeysHook)(
    TX_CONTEXTHANDLE hContext,
    TX_CONSTSTRING windowId,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam
    );


/*********************************************************************************************************************/

/**
  txEnableBuiltinKeysAsync

  Enables EyeX builtin keys for a top-level window where the keys was previously disabled.   
 
  @param hContext [in]: 
    A TX_CONTEXTHANDLE to the context on which to enable keys.
    Must not be TX_EMPTY_HANDLE.
  
  @param windowId [in]: 
    The window id for which to re-enable keys (window id corresponds to the windows handle on Windows).

  @param completionHandler [in]:
    The TX_ASYNCDATACALLBACK that will handle the request result.
    Can be NULL.

    The data provided by the TX_ASYNCDATACALLBACK will contain a result code which can be retrieved using 
    txGetAsyncDataResult(). The result code will be one of the following:

        TX_RESULT_OK: 
            The request was succesfully executed on the client.
            
        TX_RESULT_CANCELLED:
            The asynchronous operation was cancelled.

    That handle to the async data must NOT be released.

  @param userParam [in]:
    A TX_USERPARAM which will be provided as a parameter to the completion callback. 
    Can be NULL.
 
  @return 
    TX_RESULT_OK: The command was successfully created.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txEnableBuiltinKeys(
    TX_CONTEXTHANDLE hContext,
    TX_CONSTSTRING windowId,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *EnableBuiltinKeysHook)(
    TX_CONTEXTHANDLE hContext,
    TX_CONSTSTRING windowId,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam
    );


/*********************************************************************************************************************/

/**
  txLaunchConfigurationTool

  \since Version 1.1.0

  Launch a configuration tool. The supported tools are:  
    - EyeX Settings
    - Test eye tracking
    - Recalibrate current user profile
    - Create new user profile 
    - Guest calibration 
    - Diagnostics

  @param hContext [in]:
    A TX_CONTEXTHANDLE to the context.
    Must not be TX_EMPTY_HANDLE.

  @param configurationTool[in]:
    A TX_CONFIGURATIONTOOL that determines which tool to launch.

  @param completionHandler [in]:
    The TX_ASYNCDATACALLBACK that will handle the request result.
    Can be NULL.

    The data provided by the TX_ASYNCDATACALLBACK will contain a result code which can be retrieved using
    txGetAsyncDataResult(). The result code will be one of the following:

    TX_RESULT_OK:
      The tool was successfully launched.

    TX_RESULT_INVALIDEYETRACKERSTATE:
      The tool can not be launched in the current eye tracker state. 
      This could be that another configuration tool is active or that 
      the eye tracker is in an invalid state to start the configuration tool,
      see TX_CONFIGURATIONTOOL for details. 

    TX_RESULT_NOTFOUND:
      The tool was not found or failed to launch. 

	TX_RESULT_CANCELLED:
	  The client is not connected.

    The handle to the async data must NOT be released.

  @param userParam [in]:
    A TX_USERPARAM which will be provided as a parameter to the completion callback.
    Can be NULL.

  @return
    TX_RESULT_OK: The command was successfully created.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txLaunchConfigurationTool(
    TX_CONTEXTHANDLE hContext,    
    TX_CONFIGURATIONTOOL configurationTool,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam    
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *LaunchConfigurationToolHook)(
    TX_CONTEXTHANDLE hContext,    
    TX_CONFIGURATIONTOOL configurationTool,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam    
    );



/*********************************************************************************************************************/

/**
   Set the current calibration profile. 
   
   \since Version 1.3.0

   @param hContext [in]:
    A TX_CONTEXTHANDLE to the context.
    Must not be TX_EMPTY_HANDLE.

  @param profileName[in]:
    The name of the profile to activate. Must be one of the available profiles, see state TX_STATEPATH_EYETRACKINGPROFILES.

  @param completionHandler [in]:
    The TX_ASYNCDATACALLBACK that will handle the request result.
    Can be NULL.

    The data provided by the TX_ASYNCDATACALLBACK will contain a result code which can be retrieved using
    txGetAsyncDataResult(). The result code will be one of the following:

    TX_RESULT_OK:
    The profile was successfully set.

    TX_RESULT_NOTFOUND:
    The profile was not found among the available profiles.

    The handle to the async data must NOT be released.

    @param userParam [in]:
    A TX_USERPARAM which will be provided as a parameter to the completion callback.
    Can be NULL.

    @return
    TX_RESULT_OK: The command was successfully created.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.

 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txSetCurrentProfile(
    TX_CONTEXTHANDLE hContext,
    TX_CONSTSTRING profileName,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam);
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *SetCurrentProfileHook)(
    TX_CONTEXTHANDLE hContext,
    TX_CONSTSTRING profileName,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam);



/*********************************************************************************************************************/

/**
  Delete a calibration profile.

  \since Version 1.3.0

  @param hContext [in]:
    A TX_CONTEXTHANDLE to the context.
    Must not be TX_EMPTY_HANDLE.

  @param profileName[in]:
    The name of the profile to delete. Must be one of the available profiles, see state TX_STATEPATH_EYETRACKINGPROFILES.

  @param completionHandler [in]:
    The TX_ASYNCDATACALLBACK that will handle the request result.
    Can be NULL.

    The data provided by the TX_ASYNCDATACALLBACK will contain a result code which can be retrieved using
    txGetAsyncDataResult(). The result code will be one of the following:

  TX_RESULT_OK:
    The profile was successfully set.

  TX_RESULT_NOTFOUND:
    The profile was not found among the available profiles.

    The handle to the async data must NOT be released.

  @param userParam [in]:
    A TX_USERPARAM which will be provided as a parameter to the completion callback.
    Can be NULL.

  @return
    TX_RESULT_OK: The command was successfully created.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.

*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txDeleteProfile(
    TX_CONTEXTHANDLE hContext,
    TX_CONSTSTRING profileName,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam);
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *DeleteProfileHook)(
    TX_CONTEXTHANDLE hContext,
    TX_CONSTSTRING profileName,
    TX_ASYNCDATACALLBACK completionHandler,
    TX_USERPARAM userParam);


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_ACTION_API__H__) */

/*********************************************************************************************************************/

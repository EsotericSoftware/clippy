/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXAsyncData.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_ASYNCDATA_API__H__)
#define __TOBII_TX_ASYNCDATA_API__H__

/*********************************************************************************************************************/

/**
  txGetAsyncDataResultCode

  Gets the result code contained by an async data.
  Not all async data objects have a result code. See the specific asynchronous call for 
  details.
 
  @param hAsyncData [in]: 
    A TX_CONSTHANDLE to the async data object.
    Must not be TX_EMPTY_HANDLE.
 
  @param pResult [out]: 
    A pointer to a TX_RESULT which will be set to the result code.
    Must not be NULL.
 
  @return 
    TX_RESULT_OK: The result code of the async data was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.    
	TX_RESULT_NOTFOUND: The async data does not have a result code.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetAsyncDataResultCode(
    TX_CONSTHANDLE hAsyncData,
    TX_RESULT* pResult
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetAsyncDataResultCodeHook)(
    TX_CONSTHANDLE hAsyncData,
    TX_RESULT* pResult
    );


/*********************************************************************************************************************/

/**
  txGetAsyncDataContent

  Gets the content of an asynchronous data object. 
  The content may be any interaction object or nothing depending on the operation.
 
  @param hCommand [in]: 
    A TX_CONSTHANDLE to the async data object.
    Must not be TX_EMPTY_HANDLE.
 
  @param phObject [out]: 
    A pointer to a TX_HANDLE will be set to the content of the async data.
    This handle must be released using txReleaseObject to avoid leaks.
    Must not be NULL.
    The value of the pointer must be set to TX_EMPTY_HANDLE.
 
  @return 
    TX_RESULT_OK: The content of the async data was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.    
    TX_RESULT_NOTFOUND: The async data does not have any content.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetAsyncDataContent(
    TX_CONSTHANDLE hAsyncData,
    TX_HANDLE* phObject
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetAsyncDataContentHook)(
    TX_CONSTHANDLE hAsyncData,
    TX_HANDLE* phObject
    );


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_ASYNCDATA_API__H__) */

/*********************************************************************************************************************/

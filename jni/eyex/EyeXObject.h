/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXObject.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_OBJECT_API__H__)
#define __TOBII_TX_OBJECT_API__H__

/*********************************************************************************************************************/

/**
  txGetContext

  Gets the context to which a specified interaction object belongs.
  Unlike interaction objects, this handle does not need to be released.
   
  @param hObject [in]: 
    A TX_CONSTHANDLE to the interaction object for which the context should be retrieved. 
    Must not be TX_EMPTY_HANDLE.
   
  @param phContext [out]: 
    A pointer to a TX_CONSTCONTEXTHANDLE which will be set to the context.
    Must not be NULL.
   
  @return 
    TX_RESULT_OK: The context was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetContext(
    TX_CONSTHANDLE hObject,
    TX_CONTEXTHANDLE* phContext 
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetContextHook)(
    TX_CONSTHANDLE hObject,
    TX_CONTEXTHANDLE* phContext 
    );


/*********************************************************************************************************************/

/**
  txGetObjectType

  Gets the TX_INTERACTIONOBJECTTYPE of an interaction object.
 
  @param hObject [in]: 
    A TX_CONSTHANDLE to the interaction object for which to get the TX_INTERACTIONOBJECTTYPE.
    Must not be TX_EMPTY_HANDLE.
 
  @param phObjectType [out]: 
    A pointer to a TX_INTERACTIONOBJECTTYPE which will be set to the type of the object.
    Must not be NULL.
 
  @return 
    TX_RESULT_OK: The TX_INTERACTIONOBJECTTYPE of the object was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetObjectType(
    TX_CONSTHANDLE hObject,
    TX_INTERACTIONOBJECTTYPE* phObjectType
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetObjectTypeHook)(
    TX_CONSTHANDLE hObject,
    TX_INTERACTIONOBJECTTYPE* phObjectType
    );


/*********************************************************************************************************************/

/**
  txGetObjectTypeName

  Gets the type name of an interaction object.
 
  @param hObject [in]: 
    A TX_CONSTHANDLE to the interaction object for which to get the type name.
    Must not be TX_EMPTY_HANDLE.
 
  @param pObjectTypeName [out]: 
    A TX_STRING to which the type name will be copied.
    The string will be null terminated.
	May be NULL to only retreive the size.
 
  @param pObjectTypeNameSize [in,out]: 
    A pointer to a TX_SIZE which should contain the size of the pObjectTypeName string.
    The size will be set to the current number of characters in the type name + 1. 
	Must not be NULL.
 
  @return 
    TX_RESULT_OK: The type name of the object or size of the string was successfully retreived.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_INVALIDBUFFERSIZE: The size of the pObjectTypeName was to small. (*pObjectTypeNameSize will be set to the required size.)
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetObjectTypeName(
    TX_CONSTHANDLE hObject,
    TX_STRING pObjectTypeName,
    TX_SIZE* pObjectTypeNameSize
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetObjectTypeNameHook)(
    TX_CONSTHANDLE hObject,
    TX_STRING pObjectTypeName,
    TX_SIZE* pObjectTypeNameSize
    );


/*********************************************************************************************************************/

/**
  txReleaseObject

  Releases an interaction object. The object reference count will be decreased by one.
  If the reference count hits 0 the object will be destroyed. 
  The handle will be set to TX_EMPTY_HANDLE.
 
  @param phObject [in,out]: 
    A pointer to a TX_HANDLE to the interaction object that should be released.
    Must not be NULL.
    The value of the pointer must not be TX_EMPTY_HANDLE.
 
  @return 
    TX_RESULT_OK: The object was successfully released.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txReleaseObject(
    TX_HANDLE* phObject
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *ReleaseObjectHook)(
    TX_HANDLE* phObject
    );


/*********************************************************************************************************************/

/**
  txFormatObjectAsText

  Formats an interaction object as text. (primarily for debugging purposes)
 
  @param hObject [in]: 
    A TX_HANDLE to an object.
    Must not be TX_EMPTY_HANDLE.
 
  @param pText [out]: 
    A TX_STRING to which the formatted text will be copied.
    Must be at least the size of the formatted text.
    Can be NULL to only get the size of the formatted text.
 
  @param pTextSize [in,out]: 
    A pointer to a TX_SIZE which will be set the size of the formatted text.
    Must not be NULL. 
    The value must be 0 if pText is NULL.
 
  @return 
    TX_RESULT_OK: The formatted text or required size of the string was successfully retreived.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_INVALIDBUFFERSIZE: The size of the pText was to small. (*pTextSize will be set to the required size.)
*/ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txFormatObjectAsText(
    TX_CONSTHANDLE hObject,
    TX_STRING pText,
    TX_SIZE* pTextSize
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *FormatObjectAsTextHook)(
    TX_CONSTHANDLE hObject,
    TX_STRING pText,
    TX_SIZE* pTextSize
    );


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_OBJECT_API__H__) */

/*********************************************************************************************************************/

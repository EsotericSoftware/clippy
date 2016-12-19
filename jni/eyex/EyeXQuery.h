/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXQuery.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_QUERY_API__H__)
#define __TOBII_TX_QUERY_API__H__

/*********************************************************************************************************************/

/**
  txGetQueryBounds

  Gets the bounds of a query.
 
  @param hQuery [in]: 
    A TX_CONSTHANDLE to the query from which the bounds should be retrieved.
    Must not be TX_EMPTY_HANDLE.
 
  @param phBounds [out]: 
    A pointer to a TX_HANDLE which will be set to the bounds of the interactor.
    This handle must be released using txReleaseObject to avoid leaks.
    Must not be NULL.
    The value of the pointer must be set to TX_EMPTY_HANDLE.
 
  @return 
    TX_RESULT_OK: The bounds was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_NOTFOUND: This query does not have any bounds.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetQueryBounds(
    TX_CONSTHANDLE hQuery, 
    TX_HANDLE* phBounds
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetQueryBoundsHook)(
    TX_CONSTHANDLE hQuery, 
    TX_HANDLE* phBounds
    );


/*********************************************************************************************************************/

/**
  txGetQueryWindowIdCount

  Gets the number of window ids held by a query. 
  The client is expected to add interactors to a snapshot for the windows specified in the query, and
  also report these window id's in the snapshot, regardless of if any interactors are found for that window.
 
  @param hQuery [in]: 
    A TX_CONSTHANDLE to the query for which the number of window ids should be retrieved.
    Must not be TX_EMPTY_HANDLE.
 
  @param pWindowIdsCount [out]: 
    A pointer to a TX_SIZE which will be set the number of window ids.
    Must not be NULL.
 
  @return 
    TX_RESULT_OK: The number of window ids was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetQueryWindowIdCount(
    TX_CONSTHANDLE hQuery,
    TX_SIZE* pWindowIdsCount
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetQueryWindowIdCountHook)(
    TX_CONSTHANDLE hQuery,
    TX_SIZE* pWindowIdsCount
    );


/*********************************************************************************************************************/

/**
  txGetQueryWindowId

  Gets one of the window ids held by a query. Which one is specified by an index.
  The client is expected to add interactors to a snapshot for the windows specified in the query, and
  also report these window id's in the snapshot, regardless of if any interactors are found for that window.
 
  @param hQuery [in]: 
    A TX_CONSTHANDLE to the query for which the window id should be retrieved.
    Must not be TX_EMPTY_HANDLE.
 
  @param windowIdIndex [in]: 
    The index of the window id to get.
    Must be a positive integer.
 
  @param pWindowId [out]: 
    A TX_STRING to which the window id will be copied.
    Must be at least the size of the window id.
    Can be NULL to only get the size of the window id.
 
  @param pWindowIdSize [in,out]: 
    A pointer to a TX_SIZE which tells the size of pWindowId.
    Will be set the size of the window id.
    Must not be NULL.
    The value must be 0 if pWindowId is NULL.
 
  @return 
    TX_RESULT_OK: The window id or the required size of the string was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
    TX_RESULT_INVALIDBUFFERSIZE: The size of windowId is invalid (pWindowIdSize will be set to the required size).
    TX_RESULT_NOTFOUND: The specified index was out of range.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetQueryWindowId(
    TX_CONSTHANDLE hQuery,
    TX_INTEGER windowIdIndex,
    TX_STRING pWindowId,
    TX_SIZE* pWindowIdSize
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetQueryWindowIdHook)(
    TX_CONSTHANDLE hQuery,
    TX_INTEGER windowIdIndex,
    TX_STRING pWindowId,
    TX_SIZE* pWindowIdSize
    );


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_QUERY_API__H__) */

/*********************************************************************************************************************/

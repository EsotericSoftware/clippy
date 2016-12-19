/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXBounds.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_BOUNDS_API__H__)
#define __TOBII_TX_BOUNDS_API__H__

/*********************************************************************************************************************/

/**
  txGetBoundsType

  Gets the TX_BOUNDSTYPE of an interaction bounds object.

  @param hBounds [in]: 
    A TX_CONSTHANDLE to the bounds.
    Must not be TX_EMPTY_HANDLE.
 
  @param pBoundsType [out]: 
    A pointer to a TX_BOUNDSTYPE which will be set to the type of the bounds.
    Must not be NULL.
 
  @return 
    TX_RESULT_OK: The type of the bounds was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.
 */
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetBoundsType(
    TX_CONSTHANDLE hBounds,
    TX_BOUNDSTYPE* pBoundsType
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetBoundsTypeHook)(
    TX_CONSTHANDLE hBounds,
    TX_BOUNDSTYPE* pBoundsType
    );


/*********************************************************************************************************************/

/**
  txSetRectangularBoundsData

  Sets rectangular bounds data for a bounds object.
 
  @param hBounds [in]: 
    A TX_HANDLE to the bounds on which to set the rectangle.
    Must not be TX_EMPTY_HANDLE.
 
  @param x [in]: 
    Position of left edge of the rectangle.
 
  @param y [in]: 
    Position of top edge of the rectangle.
 
  @param width [in]: 
    Width of the rectangle. Must not be negative.
 
  @param height [in]: 
    Height of the rectangle. Must not be negative.
 
  @return 
    TX_RESULT_OK: The rectangular data was successfully set.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.    
    TX_RESULT_INVALIDBOUNDSTYPE: The bounds type was invalid, must be TX_BOUNDSTYPE_RECTANGULAR.
 */ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txSetRectangularBoundsData(
    TX_HANDLE hBounds,
    TX_REAL x,
    TX_REAL y,
    TX_REAL width,
    TX_REAL height
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *SetRectangularBoundsDataHook)(
    TX_HANDLE hBounds,
    TX_REAL x,
    TX_REAL y,
    TX_REAL width,
    TX_REAL height
    );


/*********************************************************************************************************************/

/**
  txSetRectangularBoundsDataRect

  Sets rectangular bounds data for a bounds object.
 
  @param hBounds [in]: 
    A TX_HANDLE to the bounds object on which to set the rectangle.
    Must not be TX_EMPTY_HANDLE.
 
  @param pRect [in]: 
    A pointer to a TX_RECT which holds the rectangular data.
    Must not be NULL.
 
  @return 
    TX_RESULT_OK: The rectangular data was successfully set.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.    
    TX_RESULT_INVALIDBOUNDSTYPE: The bounds type was invalid, must be TX_BOUNDSTYPE_RECTANGULAR.
 */ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txSetRectangularBoundsDataRect(
    TX_HANDLE hBounds,
    const TX_RECT* pRect
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *SetRectangularBoundsDataRectHook)(
    TX_HANDLE hBounds,
    const TX_RECT* pRect
    );


/*********************************************************************************************************************/

/**
  txGetRectangularBoundsData

  Gets rectangular bounds data from a bounds object.
 
  @param hBounds [in]: 
    A TX_CONSTHANDLE to the bounds object from which to get the rectangular data.
    Must not be TX_EMPTY_HANDLE.
 
  @param pX [out]: 
    A pointer to a TX_REAL which will be set to the position of the left edge of the rectangle.
	Must not be NULL.
 
  @param pY [out]: 
    A pointer to a TX_REAL which will be set to the position of the top edge of the rectangle.
	Must not be NULL.
 
  @param pWidth [out]: 
    A pointer to a TX_REAL which will be set to the width of the rectangle.
	Must not be NULL.
 
  @param height [out]: 
    A pointer to a TX_REAL which will be set to the height of the rectangle.
	Must not be NULL.
 
  @return 
    TX_RESULT_OK: The rectangular data was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.    
    TX_RESULT_INVALIDBOUNDSTYPE: The bounds type is invalid, must be TX_BOUNDSTYPE_RECTANGULAR.
 */ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetRectangularBoundsData(
    TX_CONSTHANDLE hBounds,
    TX_REAL* pX,
    TX_REAL* pY,
    TX_REAL* pWidth,
    TX_REAL* pHeight
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetRectangularBoundsDataHook)(
    TX_CONSTHANDLE hBounds,
    TX_REAL* pX,
    TX_REAL* pY,
    TX_REAL* pWidth,
    TX_REAL* pHeight
    );


/*********************************************************************************************************************/

/**
  txGetRectangularBoundsDataRect

  Gets rectangular bounds data from a bounds object.
 
  @param hBounds [in]: 
    A TX_CONSTHANDLE to the Bounds on which to get the rectangle data.
    Must not be TX_EMPTY_HANDLE.
 
  @param pRect [out]: 
    A pointer to a TX_RECT which will hold the rectangle data.
	Must not be NULL.
   
  @return 
    TX_RESULT_OK: The rectangular data was successfully retrieved.
    TX_RESULT_EYEXNOTINITIALIZED: The EyeX client environment is not initialized.
    TX_RESULT_INVALIDARGUMENT: An invalid argument was passed to the function.    
    TX_RESULT_INVALIDBOUNDSTYPE: The bounds type is invalid, must be TX_BOUNDSTYPE_RECTANGULAR.
 */ 
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txGetRectangularBoundsDataRect(
    TX_CONSTHANDLE hBounds,
    TX_RECT* pRect
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *GetRectangularBoundsDataRectHook)(
    TX_CONSTHANDLE hBounds,
    TX_RECT* pRect
    );


/*********************************************************************************************************************/

/**
  txBoundsIntersect

  Checks if a bound intersects with a rectangle.
  
  @param hBounds [in]:
    The bounds to check intersection with.

  @param x2 [in]: 
    The upper left x coordinate of the rectangle

  @param y2 [in]: 
    The upper left y coordinate of the rectangle

  @param width2 [in]: 
    The width of the rectangle

  @param height2 [in]: 
    The height of the rectangle

  @param pIntersects [out]
    The intersection test result. Will be non-zero if rectangles intersects.
	Must not be NULL.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txBoundsIntersect(
    TX_CONSTHANDLE hBounds,
    TX_REAL x2,
    TX_REAL y2,
    TX_REAL width2,
    TX_REAL height2,
    TX_BOOL* pIntersects
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *BoundsIntersectHook)(
    TX_CONSTHANDLE hBounds,
    TX_REAL x2,
    TX_REAL y2,
    TX_REAL width2,
    TX_REAL height2,
    TX_BOOL* pIntersects
    );


/*********************************************************************************************************************/

/**  
  txBoundsIntersectRect

  Checks if a bound intersects with a rectangle.

  @param hBounds [in]:
    The bounds to check intersection with.
  
  @param pRect2 [in]: 
    The rectangle to check intersection with.

  @param pIntersects [out]
    The intersection test result. Will be non-zero if rectangles intersects.
*/
TX_C_BEGIN
TX_API TX_RESULT TX_CALLCONVENTION txBoundsIntersectRect(
    TX_CONSTHANDLE hBounds,
    const TX_RECT* pRect2,
    TX_BOOL* pIntersects
    );
TX_C_END

typedef TX_RESULT (TX_CALLCONVENTION *BoundsIntersectRectHook)(
    TX_CONSTHANDLE hBounds,
    const TX_RECT* pRect2,
    TX_BOOL* pIntersects
    );


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_BOUNDS_API__H__) */

/*********************************************************************************************************************/

/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXInternalTypes.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_INTERNAL_TYPES__H__)
#define __TOBII_TX_INTERNAL_TYPES__H__

/*********************************************************************************************************************/

/**
  Callback for an Invalid argument.
 
  @param functionName [in]: 
    A TX_CONSTSTRING
 
  @param parameterName [in]: 
    A TX_CONSTSTRING 
 
  @param userParam [in]: 
    Supplied when registering the callback, normally used to respond to the event outside of the callback.
  
  @return 
    void
 */
typedef void (TX_CALLCONVENTION *TX_INVALIDARGUMENTCALLBACK)(
    TX_CONSTSTRING functionName,
    TX_CONSTSTRING parameterName,
    TX_USERPARAM userParam
    );

/*********************************************************************************************************************/

typedef enum {
    TX_PROPERTYFLAG_NONE = TX_FLAGS_NONE_VALUE,
    TX_PROPERTYFLAG_NONREMOVABLE = 1 << 0,
    TX_PROPERTYFLAG_MANUALCLONING = 1 << 1
} TX_PROPERTYFLAGS;

/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_INTERNAL_TYPES__H__) */

/*********************************************************************************************************************/

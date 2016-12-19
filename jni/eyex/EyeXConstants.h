/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXConstants.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_CONSTANTS__H__)
#define __TOBII_TX_CONSTANTS__H__

/*********************************************************************************************************************/

/**
 * Constants for mask weights.
 *
 * @field TX_MASKWEIGHT_NONE:
 *	Use this mask weight to indicate that a region of an interactor has no weight (not interactable).
 *
 * @field TX_MASKWEIGHT_DEFAULT:
 *  Use this mask weight to indicate that a region of an interactor has a default weight.
 
 * @field TX_MASKWEIGHT_HIGH:
 *  Use this mask weight to indicate that a region of an interactor has a high weight (more likely to be interacted with).
 */

	static const unsigned char None = 0;
	static const unsigned char Default = 1;
	static const unsigned char High = 255;

        
/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_CONSTANTS__H__) */

/*********************************************************************************************************************/

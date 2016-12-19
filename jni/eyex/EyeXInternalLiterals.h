/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXInternalLiterals.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_INTERNALLITERALS__H__)
#define __TOBII_TX_INTERNALLITERALS__H__

/*********************************************************************************************************************
 * Literals
 *********************************************************************************************************************/


    
    /**
    *   Snapshot internal literals
    */
    static const char* TX_INTERNALLITERAL_NONAUTHORITATIVEWINDOWIDS = "NonAuthoritativeWindowIds";       
    static const char* TX_INTERNALLITERAL_FOCUSEDTOPLEVELWINDOW = "FocusedTopLevelWindow";
    static const char* TX_INTERNALLITERAL_ISFOCUSEDTOPLEVELWINDOW = "IsFocusedTopLevelWindow";

    /**
    *   Callback Response Literals
    */    
    static const char* TX_INTERNALLITERAL_ISCANCELLED = "IsCancelled";

/*********************************************************************************************************************/    

    /**
    *  Termination Literals
    */
    static const char* TX_INTERNALLITERAL_TERMINATEGXWINDOWS = "TerminateGxWindows";
    static const char* TX_INTERNALLITERAL_GLOBALTERMINATEGXSERVER = "Global\\TerminateGxServer";    

   /**
    *   Internal Raw Gaze Data LiteralsConfiguredEyesWithGaze
    **/
    static const char* TX_INTERNALLITERAL_EYEPOSITIONFROMSCREENCENTERMM = "EyePositionFromScreenCenterMM";
    static const char* TX_INTERNALLITERAL_EYEPOSITIONINTRACKBOXNORMALIZED = "EyePositionInTrackBoxNormalized";
    static const char* TX_INTERNALLITERAL_GAZEPOINTFROMSCREENCENTERMM = "GazePointFromScreenCenterMM";
    static const char* TX_INTERNALLITERAL_GAZEPOINTONDISPLAYNORMALIZED = "GazePointOnDisplayNormalized";
    static const char* TX_INTERNALLITERAL_BOTTOMLEFT = "BottomLeft";
    static const char* TX_INTERNALLITERAL_TOPLEFT = "TopLeft";
    static const char* TX_INTERNALLITERAL_TOPRIGHT = "TopRight";
    static const char* TX_INTERNALLITERAL_SCREENBOUNDSMM = "ScreenBoundsMm";
    static const char* TX_INTERNALLITERAL_SCREENBOUNDSPIXELS = "ScreenBoundsPixels";
    static const char* TX_INTERNALLITERAL_CONFIGUREDEYESWITHGAZE = "ConfiguredEyesWithGaze"; 
    //these below could be in an enum, but wont be, since its an internal stream
	static const char* TX_INTERNALLITERAL_CONFIGUREDEYESWITHGAZENONE = "ConfiguredEyesWithGazeNone";
	static const char* TX_INTERNALLITERAL_CONFIGUREDEYESWITHGAZEBOTH = "ConfiguredEyesWithGazeBoth";
	static const char* TX_INTERNALLITERAL_CONFIGUREDEYESWITHGAZELEFT = "ConfiguredEyesWithGazeLeft";
	static const char* TX_INTERNALLITERAL_CONFIGUREDEYESWITHGAZERIGHT = "ConfiguredEyesWithGazeRight";

    static const char* TX_INTERNALLITERAL_DETECTEDEYES = "DetectedEyes";
    static const char* TX_INTERNALLITERAL_DETECTEDEYESNONE = "DetectedEyesNone";
    static const char* TX_INTERNALLITERAL_DETECTEDEYESBOTH = "DetectedEyesBoth";
    static const char* TX_INTERNALLITERAL_DETECTEDEYESLEFT = "DetectedEyesLeft";
    static const char* TX_INTERNALLITERAL_DETECTEDEYESRIGHT = "DetectedEyesRight";
   
    /**
    *   Internal Zoom Literals
    **/
    static const char* TX_INTERNALLITERAL_ZOOMDIRECTION = "ZoomDirection";
    static const char* TX_INTERNALLITERAL_ZOOMGAZEPOINTX = "ZoomGazePointX";
    static const char* TX_INTERNALLITERAL_ZOOMGAZEPOINTY = "ZoomGazePointY";
		
    /**
    *   Internal Eye Position Behavior Literals
    **/
    static const char* TX_INTERNALLITERAL_LEFTEYETRACKED = "LeftEyeTracked";
    static const char* TX_INTERNALLITERAL_RIGHTEYETRACKED = "RightEyeTracked";

    /**
    *   Internal Process Client Ids
    **/
    static const char* TX_INTERNALLITERAL_CLIENTPROCESSIDS = "ClientProcessIds";



/*********************************************************************************************************************/
    
#endif /* !defined(__TOBII_TX_INTERNALLITERALS__H__) */

/*********************************************************************************************************************/

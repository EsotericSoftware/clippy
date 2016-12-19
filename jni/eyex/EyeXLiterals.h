/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXLiterals.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_LITERALS__H__)
#define __TOBII_TX_LITERALS__H__

/*********************************************************************************************************************
 * Literals
 *********************************************************************************************************************/



    /**
    *   Message Literals
    */
    static const char* TX_LITERAL_HEADER = "Header";
    static const char* TX_LITERAL_BODY = "Body";
    static const char* TX_LITERAL_ID = "Id";
    static const char* TX_LITERAL_PROCESSID = "ProcessId";
    
    /**
    *    Client Literals
    */
    static const char* TX_LITERAL_AGENTID = "AgentId";
    static const char* TX_LITERAL_TARGETPROCESSID = "TargetProcessId";
    static const char* TX_LITERAL_CLIENTMODE = "ClientMode";
    
    /**
    *   Miscellaneous Literals
    */
    static const char* TX_LITERAL_TYPE = "Type";
    static const char* TX_LITERAL_TIMESTAMP = "Timestamp";
    static const char* TX_LITERAL_DATA = "Data";
    static const char* TX_LITERAL_PARAMETERS = "Parameters";
    static const char* TX_LITERAL_X = "X";
    static const char* TX_LITERAL_Y = "Y";
    static const char* TX_LITERAL_Z = "Z";
    
    /**
    *   Bounds Literals
    */
    static const char* TX_LITERAL_BOUNDS = "Bounds";
    static const char* TX_LITERAL_BOUNDSTYPE = "BoundsType";
    static const char* TX_LITERAL_NONE = "None";    
    static const char* TX_LITERAL_RECTANGULAR = "Rectangular";
    static const char* TX_LITERAL_TOP = "Top";
    static const char* TX_LITERAL_LEFT = "Left";
    static const char* TX_LITERAL_RIGHT = "Right";
    static const char* TX_LITERAL_BOTTOM = "Bottom";
    static const char* TX_LITERAL_WIDTH = "Width";
    static const char* TX_LITERAL_HEIGHT = "Height";
    
    /**
    *   Interactor Literals
    */
    static const char* TX_LITERAL_ROOTID = "_RootId";
    static const char* TX_LITERAL_GLOBALINTERACTORWINDOWID = "GlobalInteractorWindowId";
    static const char* TX_LITERAL_MASK = "Mask";
    static const char* TX_LITERAL_MASKID = "MaskId";
    static const char* TX_LITERAL_MASKBOUNDS = "MaskBounds";
        
    /**
    *   Mask Literals
    */
    static const char* TX_LITERAL_MASKTYPE = "MaskType";
    static const char* TX_LITERAL_ROWCOUNT = "RowCount";
    static const char* TX_LITERAL_COLUMNCOUNT = "ColumnCount";
    
    /**
    * Gaze Point Data Behavior Literals
    */
    static const char* TX_LITERAL_GAZEPOINTDATAMODE = "GazePointDataMode";
    static const char* TX_LITERAL_GAZEPOINTDATAEVENTTYPE = "GazePointDataEventType";
    
    /**
    *    Activation Behavior Literals
    */
    static const char* TX_LITERAL_ACTIVATABLEEVENTTYPE = "ActivatableEventType";
    static const char* TX_LITERAL_HASACTIVATIONFOCUS = "HasActivationFocus";    
    static const char* TX_LITERAL_HASTENTATIVEACTIVATIONFOCUS = "HasTentativeActivationFocus";
    static const char* TX_LITERAL_ISACTIVATED = "IsActivated";
    static const char* TX_LITERAL_ISTENTATIVEFOCUSENABLED = "IsTentativeFocusEnabled";
    static const char* TX_LITERAL_ISSMALLITEMDETECTIONENABLED = "IsSmallItemDetectionEnabled";

    /**
    * Fixation Data Behavior Literals
    */
    static const char* TX_LITERAL_FIXATIONDATAMODE = "FixationDataMode";
    static const char* TX_LITERAL_FIXATIONDATAEVENTTYPE = "FixationDataEventType";

    /**
    * Action data Behavior Literals 
    */
    static const char* TX_LITERAL_ACTIONDATAEVENTTYPE = "ActionDataEventType";
    static const char* TX_LITERAL_ACTIVATIONMISSED = "ActivationMissed";
    
    /**
    * Gaze-Aware Behavior Literals
    */
    static const char* TX_LITERAL_HASGAZE = "HasGaze";
    static const char* TX_LITERAL_GAZEAWAREMODE = "GazeAwareMode";
    static const char* TX_LITERAL_DELAYTIME = "DelayTime";
    
    /**
    * Gaze Data Diagnostics Behavior Literals
    */
    static const char* TX_LITERAL_QUALITY = "Quality";    
    static const char* TX_LITERAL_NOISE = "Noise";
    static const char* TX_LITERAL_INSACCADE = "InSaccade";
    static const char* TX_LITERAL_INFIXATION = "InFixation";
    
    /**
    * Eye Position Behavior Literals
    */
    static const char* TX_LITERAL_LEFTEYEPOSITION = "LeftEyePosition";    
    static const char* TX_LITERAL_RIGHTEYEPOSITION = "RightEyePosition";
    static const char* TX_LITERAL_LEFTEYEPOSITIONNORMALIZED = "LeftEyePositionNormalized";    
    static const char* TX_LITERAL_RIGHTEYEPOSITIONNORMALIZED = "RightEyePositionNormalized";
    static const char* TX_LITERAL_HASLEFTEYEPOSITION = "HasLeftEyePosition";
    static const char* TX_LITERAL_HASRIGHTEYEPOSITION = "HasRightEyePosition";

    /**
    * Presence Behavior Literals
    */
    static const char* TX_LITERAL_PRESENCEDATA = "Presence";


    /**
    * Pannable Behavior Literals
    */
    static const char* TX_LITERAL_PANVELOCITYX = "PanVelocityX";
    static const char* TX_LITERAL_PANVELOCITYY = "PanVelocityY";
    static const char* TX_LITERAL_PANSTEPX = "PanStepX";
    static const char* TX_LITERAL_PANSTEPY = "PanStepY";
    static const char* TX_LITERAL_PANSTEPDURATION = "PanStepDuration";
    static const char* TX_LITERAL_PANHANDSFREE = "PanHandsFree";
    static const char* TX_LITERAL_PANPROFILE = "Profile";
    static const char* TX_LITERAL_PANDIRECTIONSAVAILABLE = "PanDirectionsAvailable";
    static const char* TX_LITERAL_PANPEAKVELOCITY = "PeakVelocity";
    static const char* TX_LITERAL_PANADAPTVELOCITYTOVIEWPORT = "AdaptVelocityToViewport";
    static const char* TX_LITERAL_PANMAXZONERELATIVESIZE = "MaxPanZoneRelativeSize";
    static const char* TX_LITERAL_PANMAXZONESIZE = "MaxPanZoneSize";
    static const char* TX_LITERAL_PANZONESIZE = "PanZoneSize";
    static const char* TX_LITERAL_PANNABLEEVENTTYPE = "PannableEventType";

    /**
    *   Callback Response Literals
    */
    static const char* TX_LITERAL_REQUESTTYPE = "RequestType";
    static const char* TX_LITERAL_REQUESTID = "RequestId";
    static const char* TX_LITERAL_ERRORMESSAGE = "ErrorMessage";
    static const char* TX_LITERAL_RESULT = "Result";

    /**
    *   Interaction Mode Literals
    */
    static const char* TX_LITERAL_ACTIONTYPE = "ActionType";    

    /**
    *   State literals
    */
    static const char* TX_LITERAL_STATEPATH = "StatePath";
    static const char* TX_LITERAL_STATEPATHDELIMITER = ".";    

    /*
    *  Configuration Tool Literals
    */
    static const char* TX_LITERAL_CONFIGURATIONTOOL = "ConfigurationTool";

    /*
    *  Current Profile Literals 
    */
    static const char* TX_LITERAL_PROFILENAME = "ProfileName";



/*********************************************************************************************************************/

/**
 * Literals for state paths.
 * 
 * @field TX_STATEPATH_EYETRACKING:
 *   The root node for all eyetracking information.
 *   GETTABLE.
 *
 * @field TX_STATEPATH_EYETRACKINGSCREENBOUNDS:
 *   Holds the virtual screen bounds in pixels. 
 *   The value can be retrieved from the state bag as a TX_RECT structure with GetStateValueAsRectangle.
 *   If the screen bounds can not be determined screen bounds (0, 0, 0, 0) will be returned.
 *   Replaces deprecated state path TX_STATEPATH_SCREENBOUNDS from version 1.3.0.
 *   GETTABLE.
 *  \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGDISPLAYSIZE:
 *   Holds the display size in millimeters as width and height. 
 *   The value can be retrieved from the state bag as a TX_SIZE2 structure with GetStateValueAsSize2.
 *   If the display size can not be determined Width and Height (0, 0) will be returned.
 *   Replaces deprecated state path TX_STATEPATH_DISPLAYSIZE from version 1.3.0.
 *   GETTABLE.
 *  \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGSTATE: 
 *   Holds the eye tracking state. The value is of type TX_EYETRACKINGDEVICESTATUS.
 *   GETTABLE.
 *
 * @field TX_STATEPATH_EYETRACKINGCURRENTPROFILE:
 *   Holds the following data: 
 *   "name" - See TX_STATEPATH_EYETRACKINGCURRENTPROFILENAME.
 *   "trackedeyes" - See TX_STATEPATH_EYETRACKINGCURRENTPROFILETRACKEDEYES.
  *   GETTABLE.
 *  \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGCURRENTPROFILENAME: 
 *   Holds the name of the current eye tracking profile. The value is of type TX_STRING.
 *   Replaces deprecated state path TX_STATEPATH_PROFILENAME from version 1.3.0.
 *   GETTABLE.
 *  \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGCURRENTPROFILETRACKEDEYES:
 *   Holds the tracked eyes of the current eye tracking profile. The value is of type TX_TRACKEDEYES.
 *   GETTABLE and SETTABLE.
 *  \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGPROFILES:
 *   Holds the list of available eye tracking profiles. The value is an array of TX_STRING. It can be accessed with 
 *   txGetStateValueAsString as a string containing all profiles separated with a null termination character. 
 *   There is also a utility function available to access profiles as a std::vector of std::strings, see Tx::GetStateValueAsArrayOfStrings.
 *   GETTABLE. 
 *  \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGCONFIGURATIONSTATUS:
 *   Holds the configuration status of the eye tracker. The value is of type TX_EYETRACKINGCONFIGURATIONSTATUS.
 *   GETTABLE.
 *   \since Version 1.1.0
 *
 * @field TX_STATEPATH_EYETRACKINGINFO:
 *   Holds information about the eye tracker. 
 *   GETTABLE.
 *   \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGINFOMODELNAME:
 *   Eye tracker model name. The value is of type TX_STRING.
 *   GETTABLE.
 *   \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGINFOSERIALNUMBER:
 *   Eye tracker serial number. The value is of type TX_STRING.
 *   GETTABLE.
 *   \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGINFOGENERATION:
 *   Eye tracker generation name. The value is of type TX_STRING.
 *   GETTABLE.
 *   \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGINFOFIRMWAREVERSION:
 *   Eye tracker firmware version. The value is of type TX_STRING.
 *   GETTABLE.
 *   \since Version 1.3.0
 *
 * @field TX_STATEPATH_EYETRACKINGINFOEYEXCONTROLLERCOREVERSION:
 *   EyeX Controller Core Version. The value is of type TX_STRING.
 *   GETTABLE.
 *   \since Version 1.3.0
 *
 * @field TX_STATEPATH_ENGINEINFOVERSION:
 *   Reports the engine version. The value is of type TX_STRING.
 *   Replaces deprecated state path TX_STATEPATH_ENGINEVERSION since version 1.3.0.
 *   GETTABLE.
 *   \since Version 1.3.0
 *
 * @field TX_STATEPATH_ENGINEINFOINTERNAL:
 *   Holds the following data:
 *   "tobiiserviceversion" - The installed version of Tobii Service as TX_STRING or empty string if not installed.
 *   "tobiieyexcontrollerdriverversion" - The installed version of Tobii EyeX Controller Driver as TX_STRING or empty string if not installed.
 *   "tobiiusbserviceversion" - The installed version of Tobii USB Service version as TX_STRING or empty string if not installed.                         
 *   "tobiieyexinteractionversion" - The installed version of Tobii EyeX Interaction as TX_STRING or empty string if not installed.
 *   "tobiieyexconfigversion" - The installed version of Tobii EyeX Config as TX_STRING or empty string if not installed.
 *   GETTABLE.
 *   \since Version 1.3.0
 *
 * @field TX_STATEPATH_USERPRESENCE:
 *   Holds data about user presence. The value is of type TX_USERPRESENCE.
 *   The value of TX_USERPRESENCE will be TX_USERPRESENCE_UNKNOWN if there isn't any observer registered for this state.
 *   GETTABLE.
 *
 * @field TX_STATEPATH_FAILEDACTION:
 *   Notifies when interactions fail. The value is of type TX_FAILEDACTIONTYPE.
 *   SUBSCRIBABLE.
 * 
 * @field TX_STATEPATH_INTERACTIONMODES:
 *   Holds the current engine interaction mode. The value is of type TX_INTERACTIONMODES.
 *   GETTABLE.
 *   \since Version 1.1.0
 *
 * @field TX_STATEPATH_PROFILENAME:
 *   Holds the name of the eye tracking profile used. The value is of type TX_STRING.
 *   Deprecated, use TX_STATEPATH_EYETRACKINGCURRENTPROFILENAME for engine version 1.3.0 and greater.
 *   GETTABLE.
 *
 * @field TX_STATEPATH_ENGINEVERSION:
 *   Reports the engine version. The value is of type TX_STRING.
 *   Deprecated, use TX_STATEPATH_ENGINEINFOVERSION for engine version 1.3.0 and greater.
 *   GETTABLE.
 *
 * @field TX_STATEPATH_SCREENBOUNDS:
 *   Holds the virtual screen bounds in pixels.
 *   The value can be retrieved from the state bag as a TX_RECT structure with GetStateValueAsRectangle.
 *   If the screen bounds can not be determined screen bounds (0, 0, 0, 0) will be returned.
 *   Deprecated, use TX_STATEPATH_EYETRACKINGSCREENBOUNDS for engine version 1.3.0 and greater.
 *   GETTABLE.
 *
 * @field TX_STATEPATH_DISPLAYSIZE:
 *   Holds the display size in millimeters as width and height.
 *   The value can be retrieved from the state bag as a TX_SIZE2 structure with GetStateValueAsSize2.
 *   If the display size can not be determined Width and Height (0, 0) will be returned.
 *   Deprecated, use TX_STATEPATH_EYETRACKINGDISPLAYSIZE for engine version 1.3.0 and greater. 
 *   GETTABLE.
 *
 * @field TX_STATEPATH_CONFIGURATIONSTATUS:
 *   Holds the configuration status of the eye tracker. The value is of type TX_EYETRACKINGCONFIGURATIONSTATUS.
 *   \since Version 1.1.0
 *   Deprecated, use TX_STATEPATH_EYETRACKINGCONFIGURATIONSTATUS for engine version 1.3.0 and greater. 
 *   GETTABLE.
 * 
 */
    
    static const char* TX_STATEPATH_EYETRACKING = "eyeTracking";
    static const char* TX_STATEPATH_EYETRACKINGSCREENBOUNDS = "eyeTracking.screenBounds";
    static const char* TX_STATEPATH_EYETRACKINGDISPLAYSIZE = "eyeTracking.displaySize";
    static const char* TX_STATEPATH_EYETRACKINGSTATE = "eyeTracking.state";
            
    static const char* TX_STATEPATH_EYETRACKINGPROFILES = "eyeTracking.profiles";
    static const char* TX_STATEPATH_EYETRACKINGCURRENTPROFILE = "eyeTracking.currentprofile";
    static const char* TX_STATEPATH_EYETRACKINGCURRENTPROFILENAME = "eyeTracking.currentprofile.name";
    static const char* TX_STATEPATH_EYETRACKINGCURRENTPROFILETRACKEDEYES = "eyeTracking.currentprofile.trackedeyes";

    static const char* TX_STATEPATH_EYETRACKINGCONFIGURATIONSTATUS = "eyeTracking.configurationStatus";

    static const char* TX_STATEPATH_EYETRACKINGINFO = "eyeTracking.info";
    static const char* TX_STATEPATH_EYETRACKINGINFOMODELNAME = "eyeTracking.info.modelname";
    static const char* TX_STATEPATH_EYETRACKINGINFOSERIALNUMBER = "eyeTracking.info.serialnumber";
    static const char* TX_STATEPATH_EYETRACKINGINFOGENERATION = "eyeTracking.info.generation";
    static const char* TX_STATEPATH_EYETRACKINGINFOFIRMWAREVERSION = "eyeTracking.info.firmwareversion";
    static const char* TX_STATEPATH_EYETRACKINGINFOHASFIXEDDISPLAYAREA = "eyeTracking.info.hasfixeddisplayarea";
    static const char* TX_STATEPATH_EYETRACKINGINFOTOBIIEYEXCONTROLLERCOREVERSION = "eyeTracking.info.tobiieyexcontrollercoreversion";  

    static const char* TX_STATEPATH_ENGINEINFOVERSION = "engine.info.version";    
    static const char* TX_STATEPATH_ENGINEINFOINTERNAL = "engine.info.internal";
    
    static const char* TX_STATEPATH_GAZETRACKING = "status.gazeTracking";

    static const char* TX_STATEPATH_USERPRESENCE = "userPresence";

    static const char* TX_STATEPATH_FAILEDACTION = "failedAction";

    static const char* TX_STATEPATH_INTERACTIONMODES = "status.interaction.interactionModes";

    /* Deprecated since version 1.3.0. For compatibility between client libs and engine version 1.2.1 or lesser */
    static const char* TX_STATEPATH_PROFILENAME = "eyeTracking.profileName"; 
    static const char* TX_STATEPATH_ENGINEVERSION = "engineVersion"; 
    static const char* TX_STATEPATH_CONFIGURATIONSTATUS = "eyeTracking.configurationStatus"; 
    static const char* TX_STATEPATH_SCREENBOUNDS = "eyeTracking.screenBounds"; 
    static const char* TX_STATEPATH_DISPLAYSIZE = "eyeTracking.displaySize";                 


/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_LITERALS__H__) */

/*********************************************************************************************************************/

/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXFrameworkTypes.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_FRAMEWORK_TYPES__H__)
#define __TOBII_TX_FRAMEWORK_TYPES__H__

/*********************************************************************************************************************/

#include "EyeXMacros.h"

/*********************************************************************************************************************/

/**
  TX_RESULT

  Enumeration for all result codes returned by the API functions.

  @field TX_RESULT_UNKNOWN:
    Unknown error, typically returned if something unexpected occurs in the API. Is most likely a bug in the API.

  @field TX_RESULT_OK:
    Everything went well.

  @field TX_RESULT_EYEXNOTINITIALIZED:
    The EyeX client environment is not initalized. All API functions except txInitializeEyeX requires the EyeX client environment to be
    initialized prior to being called.

  @field TX_RESULT_EYEXALREADYINITIALIZED:
    The EyeX client environment has already been initialized. This is returned by txInitializeEyeX if called twice without being
    uninitialized in between.

  @field TX_RESULT_EYEXSTILLINUSE:
    The EyeX client environment is still in use. This is returned by txUninitializeEyeX if at least one context is still being used.

  @field TX_RESULT_INVALIDARGUMENT:
    An invalid argument was passed to an API function. All arguments are checked before an API function actually
    does something. There are many reasons why an argument can be considered invalid. 
    Check the log for more details if this code is returned.
    
  @field TX_RESULT_INVALIDHANDLE:
    The handle for an interaction object is not valid.
    
  @field TX_RESULT_NOTFOUND:
    Generic result code when something could not be found.
    
  @field TX_RESULT_INVALIDBUFFERSIZE:
    Some buffer; string, array, etc had an invalid size. Typically, API functions that return this result code also
    provides the required size.
    
  @field TX_RESULT_DUPLICATEPROPERTY:
    An attempt has been made to create a property that does already exist.
    
  @field TX_RESULT_DUPLICATEBOUNDS:
    An attempt has been made to create bounds that already exists.
    
  @field TX_RESULT_DUPLICATEBEHAVIOR:
    An attempt has been made to create a behavior that already exists.
    
  @field TX_RESULT_DUPLICATEINTERACTOR:
    An attempt has been made to create an interactor with the same id as another in the same snapshot.
    
  @field TX_RESULT_DUPLICATESTATEOBSERVER:
    An attempt has been made to register the same state observer twice.
    
  @field TX_RESULT_DUPLICATEMASK:
    An attempt has been made to create more than one mask on an interactor.
    
  @field TX_RESULT_INVALIDPROPERTYTYPE:
    A type specific operation has been made on a property of a different type. For example a property containing a 
    TX_INTEGER has been requested for its value as a TX_STRING.
    
  @field TX_RESULT_INVALIDPROPERTYNAME:
    The specified property name is invalid.
    
  @field TX_RESULT_PROPERTYNOTREMOVABLE:
    An attempt has been made to remove a property that is not removable. Typically such properties are the ones
    backing up data that is required on different interaction objects.
    
  @field TX_RESULT_NOTCONNECTED:
    An attempt was made to perform an operation that requires a valid connection to the client.

  @field TX_RESULT_INVALIDOBJECTCAST:
    A handle for a different type of interaction object than expected was provided.
    
  @field TX_RESULT_INVALIDTHREAD:
    An attempt was made to perform an operation on a thread that is not allowed to perform such an operation.
    For example a context can not be deleted on a callback from the API.
        
  @field TX_RESULT_INVALIDBOUNDSTYPE:
    An attempt was made to perform an operation that does not apply to the current bounds type.
    
  @field TX_RESULT_INVALIDBEHAVIORTYPE:
    An attempt was made to perform an operation that does not apply to the current behavior type.
    
  @field TX_RESULT_OBJECTLEAKAGE:
    A leakage of an interaction object has been detected. May be returned by a 
    successful txReleaseContext call where some object were not released properly.
    
  @field TX_RESULT_OBJECTTRACKINGNOTENABLED:
    An attempt to retrieve tracked object has been made without tracking of objects being enabled.
    
  @field TX_RESULT_INVALIDSNAPSHOT:
    The snapshot committed to the client contained some invalid data.

  @field TX_RESULT_INVALIDCOMMAND:
    The submitted command was malformed or not recognized by the client.

  @field TX_RESULT_CANCELLED:
    An attempt has been made to perform an operation that is not supported during shutdown. 
    
  @field TX_RESULT_INVALIDSCHEDULINGMODE:
    The scheduling mode is invalid.

  @field TX_RESULT_MASKTOOLARGE:
    The supplied mask is too large, width*height must be less than 65536

   @field TX_RESULT_INVALIDEYETRACKERSTATE
    \since Version 1.1.0
    The submitted command can not be executed in the current state of the eye tracker
 */
typedef enum {
    TX_RESULT_UNKNOWN = TX_ENUM_STARTVALUE,
    TX_RESULT_OK,
    TX_RESULT_EYEXNOTINITIALIZED,
    TX_RESULT_EYEXALREADYINITIALIZED,
    TX_RESULT_EYEXSTILLINUSE,
    TX_RESULT_INVALIDARGUMENT,   
    TX_RESULT_INVALIDHANDLE,
    TX_RESULT_NOTFOUND,
    TX_RESULT_INVALIDBUFFERSIZE,
    TX_RESULT_DUPLICATEPROPERTY,
    TX_RESULT_DUPLICATEBOUNDS,
    TX_RESULT_DUPLICATEBEHAVIOR,
    TX_RESULT_DUPLICATEINTERACTOR,
    TX_RESULT_DUPLICATESTATEOBSERVER,
    TX_RESULT_DUPLICATEMASK,
    TX_RESULT_INVALIDPROPERTYTYPE,
    TX_RESULT_INVALIDPROPERTYNAME,
    TX_RESULT_PROPERTYNOTREMOVABLE,
    TX_RESULT_NOTCONNECTED,
    TX_RESULT_INVALIDOBJECTCAST,
    TX_RESULT_INVALIDTHREAD,
    TX_RESULT_INVALIDBOUNDSTYPE,
    TX_RESULT_INVALIDBEHAVIORTYPE,
    TX_RESULT_OBJECTLEAKAGE,
    TX_RESULT_OBJECTTRACKINGNOTENABLED,
    TX_RESULT_INVALIDSNAPSHOT,
    TX_RESULT_INVALIDCOMMAND,
    TX_RESULT_CANCELLED,
    TX_RESULT_INVALIDSCHEDULINGMODE,
    TX_RESULT_MASKTOOLARGE,
    TX_RESULT_INVALIDEYETRACKERSTATE
} TX_RESULT;

/*********************************************************************************************************************
 * Interaction Object Types
 *********************************************************************************************************************/

/**
  Enumeration for all the types of interaction objects that can be exposed through the API.

  @field TX_INTERACTIONOBJECTTYPE_SNAPSHOT:
    The object is a snapshot.

  @field TX_INTERACTIONOBJECTTYPE_INTERACTOR:
    The object is an interactor.
    
  @field TX_INTERACTIONOBJECTTYPE_QUERY:
    The object is a query.

  @field TX_INTERACTIONOBJECTTYPE_EVENT:
    The object is an event.

  @field TX_INTERACTIONOBJECTTYPE_BEHAVIOR:
    The object is a behavior.
        
  @field TX_INTERACTIONOBJECTTYPE_BOUNDS:
    The object is a bounds structure.

  @field TX_INTERACTIONOBJECTTYPE_PROPERTYBAG:
    The object is a property bag.

  @field TX_INTERACTIONOBJECTTYPE_PROPERTY:
    The object is a property.
    
  @field TX_INTERACTIONOBJECTTYPE_COMMAND:
    The object is a command.
        
  @field TX_INTERACTIONOBJECTTYPE_STATEBAG:
    The object is a state bag.
    
  @field TX_INTERACTIONOBJECTTYPE_NOTIFICATION:
    The object is a notification.
        
  @field TX_INTERACTIONOBJECTTYPE_MASK:
    The object is a mask.
 */
typedef enum {
    TX_INTERACTIONOBJECTTYPE_SNAPSHOT = TX_ENUM_STARTVALUE, 
    TX_INTERACTIONOBJECTTYPE_INTERACTOR,
    TX_INTERACTIONOBJECTTYPE_QUERY,
    TX_INTERACTIONOBJECTTYPE_EVENT,
    TX_INTERACTIONOBJECTTYPE_BEHAVIOR,
    TX_INTERACTIONOBJECTTYPE_BOUNDS,
    TX_INTERACTIONOBJECTTYPE_PROPERTYBAG,
    TX_INTERACTIONOBJECTTYPE_COMMAND,    
    TX_INTERACTIONOBJECTTYPE_STATEBAG,
    TX_INTERACTIONOBJECTTYPE_NOTIFICATION,
    TX_INTERACTIONOBJECTTYPE_MASK,
    TX_INTERACTIONOBJECTTYPE_ASYNCDATA,

    /* for internal use only */
    TX_INTERACTIONOBJECTTYPE_INTERNAL_MESSAGE = TX_INTERNAL_ENUM_STARTVALUE,
    TX_INTERACTIONOBJECTTYPE_INTERNAL_MESSAGEHEADER  
} TX_INTERACTIONOBJECTTYPE;

/*********************************************************************************************************************/

/**
  TX_MESSAGETYPE

  Enumeration for all message types.
  The messages type is metadata contained by all packets sent between the client and server. 
  Some messages should be handled by the application to do proper interaction, others are internal and should be
  ignored.
    
  @field TX_MESSAGETYPE_QUERY:
    Message contains a query.
    
  @field TX_MESSAGETYPE_EVENT:
    Message contains an event.
    
  @field TX_MESSAGETYPE_NOTIFICATION:
    Message contains a notification. This is an internal message type.
    
  @field TX_MESSAGETYPE_REQUEST:
    Message contains a request. This is an internal message type.
    
  @field TX_MESSAGETYPE_RESPONSE:
    Message contains a response. This is an internal message type.
    
  @field TX_MESSAGETYPE_CUSTOM:
    Base value for custom message defined by other protocols.
 */
typedef enum {
    TX_MESSAGETYPE_QUERY = TX_ENUM_STARTVALUE,
    TX_MESSAGETYPE_EVENT,
    TX_MESSAGETYPE_NOTIFICATION = TX_INTERNAL_ENUM_STARTVALUE,
    TX_MESSAGETYPE_REQUEST,
    TX_MESSAGETYPE_RESPONSE,
    TX_MESSAGETYPE_CUSTOM
} TX_MESSAGETYPE;

/*********************************************************************************************************************/

/**
  TX_NOTIFICATIONTYPE

  Enumeration for all notification types.
  The notification type is metadata contained by all notifications to specify what kind of notification it is.

  @field TX_NOTIFICATIONTYPE_STATECHANGED:
    Notifies that some states have changed.
 */
typedef enum {    
    TX_NOTIFICATIONTYPE_STATECHANGED = TX_ENUM_STARTVALUE,
    TX_NOTIFICATIONTYPE_DIAGNOSTICSDATA
} TX_NOTIFICATIONTYPE;

/*********************************************************************************************************************/

/**
  TX_BEHAVIORTYPE
 
  Enumeration for all behavior types.
  The behavior type is metadata contained by all behaviors to specify what kind of behavior it is.

  @field TX_BEHAVIORTYPE_GAZEPOINTDATA:
    Behavior used on interactors to receive gaze point data.    

  @field TX_BEHAVIORTYPE_EYEPOSITIONDATA:
    Behavior used on interactors to receive eye position data.
    
  @field TX_BEHAVIORTYPE_GAZEAWARE:
    Behavior used on interactors to perform gaze-aware interaction.

  @field TX_BEHAVIORTYPE_ACTIVATABLE:
    Behavior used on interactors to perform activation interaction.

  @field TX_BEHAVIORTYPE_PANNABLE:
    Behavior used on interactors to perform panning interaction.  

  @field TX_BEHAVIORTYPE_FIXATIONDATA:
    Behavior used on interactors to receive fixation data.
 */
typedef enum {
    TX_BEHAVIORTYPE_GAZEPOINTDATA = TX_ENUM_STARTVALUE,    
    TX_BEHAVIORTYPE_EYEPOSITIONDATA,
    TX_BEHAVIORTYPE_GAZEAWARE,
    TX_BEHAVIORTYPE_ACTIVATABLE,
    TX_BEHAVIORTYPE_PANNABLE,    
    TX_BEHAVIORTYPE_FIXATIONDATA,
 /* For Internal use */
    TX_INTERNAL_BEHAVIORTYPE_RAWGAZEDATA = TX_INTERNAL_ENUM_STARTVALUE,
    TX_INTERNAL_BEHAVIORTYPE_ZOOMABLE,
    TX_BEHAVIORTYPE_GAZEDATADIAGNOSTICS    
} TX_BEHAVIORTYPE;

/*********************************************************************************************************************/

/**
  TX_BOUNDSTYPE

  Enumeration for all bounds types.

  @field TX_BOUNDSTYPE_NONE:
    No bounds.

  @field TX_BOUNDSTYPE_RECTANGULAR:    
    Rectangular bounds.
 */
typedef enum {
    TX_BOUNDSTYPE_NONE = TX_ENUM_STARTVALUE,
    TX_BOUNDSTYPE_RECTANGULAR
} TX_BOUNDSTYPE;

/*********************************************************************************************************************/

/**
  TX_ACTIVATABLEEVENTTYPE

  Enumeration for all activation event types.
  Activatable event type are metadata contained by all behaviors of type TX_BEHAVIORTYPE_ACTIVATABLE sent
  from the client. This event type specifies what kind of activation event actually happened.
  
  @field TX_ACTIVATABLEEVENTTYPE_ACTIVATED:
    The interactor has been activated.

  @field TX_ACTIVATABLEEVENTTYPE_ACTIVATIONFOCUSCHANGED:
    The activation focus and/or tentative activation focus has changed.
 */
typedef enum {    
    TX_ACTIVATABLEEVENTTYPE_ACTIVATED = TX_ENUM_STARTVALUE,
    TX_ACTIVATABLEEVENTTYPE_ACTIVATIONFOCUSCHANGED
} TX_ACTIVATABLEEVENTTYPE;

/*********************************************************************************************************************/

/**
  TX_FAILEDACTIONTYPE

  Enumeration for all action data types.

  @field TX_FAILEDACTIONTYPE_ACTIVATIONNOHIT
    An activation action did not hit any valid interactor
  @field TX_FAILEDACTIONTYPE_PANNOHIT
    A pan action did not hit any valid interactor
  @field TX_FAILEDACTIONTYPE_ZOOMNOHIT
    A zoom action did not hit any valid interactor
  @field TX_FAILEDACTIONTYPE_ACTIVATIONDURINGNOTRACKING
    An activation action occured when there was no tracking
  @field TX_FAILEDACTIONTYPE_PANDURINGNOTRACKING
    A pan action occured when there was no tracking
  @field TX_FAILEDACTIONTYPE_ZOOMDURINGNOTRACKING
    A zoom action occured when there was no tracking
  @field TX_FAILEDACTIONTYPE_ACTIVATIONSMALLITEMS
    For internal use only.
    An activation occured near multiple small interactors and was therefore undecided
 */
typedef enum {    
    TX_FAILEDACTIONTYPE_ACTIVATIONNOHIT = TX_ENUM_STARTVALUE,
    TX_FAILEDACTIONTYPE_PANNOHIT,
    TX_FAILEDACTIONTYPE_ZOOMNOHIT,
    TX_FAILEDACTIONTYPE_ACTIVATIONDURINGNOTRACKING,
    TX_FAILEDACTIONTYPE_PANDURINGNOTRACKING,
    TX_FAILEDACTIONTYPE_ZOOMDURINGNOTRACKING,
    TX_FAILEDACTIONTYPE_ACTIVATIONSMALLITEMS
} TX_FAILEDACTIONTYPE;

/*********************************************************************************************************************/

/**
  TX_FIXATIONDATAEVENTTYPE

  Enumeration for all fixation data event types.
  Fixation event type is metadata contained by all behaviors of type TX_BEHAVIORTYPE_FIXATION sent
  from the client. This event type specifies what kind of fixation event actually happened.
  
  @field TX_FIXATIONDATAEVENTTYPE_BEGIN:
    The fixation has begun. The gaze point data provided is a combination of the gaze points used to detect the 
    fixation. The timestamp will reflect when the fixation actually began.

  @field TX_FIXATIONDATAEVENTTYPE_END:
    The fixation has ended. The last valid gaze point is provided. The timestamp will reflect when the 
    fixation actually ended.
    
  @field TX_FIXATIONDATAEVENTTYPE_DATA:
    The fixation is still occuring. A new, filtered gaze point within the fixation is provided.
 */
typedef enum {    
    TX_FIXATIONDATAEVENTTYPE_BEGIN = TX_ENUM_STARTVALUE,
    TX_FIXATIONDATAEVENTTYPE_END,
    TX_FIXATIONDATAEVENTTYPE_DATA
} TX_FIXATIONDATAEVENTTYPE;

/*********************************************************************************************************************/

/**
  TX_GAZEPOINTDATAMODE

   Enumeration for all gaze point data modes.
   The gaze point data mode is metadata contained by all behaviors of type TX_BEHAVIORTYPE_GAZEPOINTDATA.
   When put on an interactor it specifies what kind of filter to use by the engine when calculating the gaze points.
   When put on an event it specifies what kind of filter that was used by the engine.

   @field TX_GAZEPOINTDATAMODE_UNFILTERED:    
    No filter will be applied to the gaze points. (note though that invalid gaze points are discarded)

   @field: TX_GAZEPOINTDATAMODE_LIGHTLYFILTERED:
    A light filter was/will be applied to the gaze point data. It is not a simple smoothing filter, it aims to be 
    smooth but responsive. This should be you default choice for gaze point data.
 */
typedef enum {
    TX_GAZEPOINTDATAMODE_UNFILTERED = TX_ENUM_STARTVALUE,
    TX_GAZEPOINTDATAMODE_LIGHTLYFILTERED
} TX_GAZEPOINTDATAMODE;


/*********************************************************************************************************************/

/**
  TX_GAZEAWAREMODE

   Enumeration for all gaze aware modes.
   The gaze aware mode is metadata contained by all behaviors of type TX_BEHAVIORTYPE_GAZEAWARE.
   When put on an interactor it specifies how the user must gaze on the interactor to make it gaze aware.
   When put on an event it specifies what kind of mode that was used by the engine.
   
   @field: TX_GAZEAWAREMODE_NORMAL:
    The interactor will get a gaze aware event when the engine considers the user to intentionally look at it.
       
   @field: TX_GAZEAWAREMODE_DELAYED:
    The interactor will get a gaze aware event when the engine considers the user to have looked at it for a 
    specified amount of time. When using this mode TX_GAZEAWAREPARAMS needs have the field DelayTime set.
 */
typedef enum {
    TX_GAZEAWAREMODE_NORMAL = TX_ENUM_STARTVALUE,
    TX_GAZEAWAREMODE_DELAYED    
} TX_GAZEAWAREMODE;

/*********************************************************************************************************************/

/**
  TX_FIXATIONDATAMODE

   Enumeration for all fixation data modes.
   The fixation data mode is metadata contained by all behaviors of type TX_BEHAVIORTYPE_FIXATION.
   When put on an interactor it specifies what kind of filter to use by the engine when finding fixations.
   When put on an event it specifies what kind of filter that was used by the engine.

   @field TX_FIXATIONDATAMODE_SENSITIVE:    
    Very sensitive fixation filter, will result in many fixations, sometimes very close and in quick succession.

   @field TX_FIXATIONDATAMODE_SLOW:
    Fairly sensitive to enter fixation but can be slow to exit, as it tries merge fixations close to each other. 
    Will result in fairly stable fixations but fixation end events may be coming rather late in certain circumstances.
 */
typedef enum {
    TX_FIXATIONDATAMODE_SENSITIVE = TX_ENUM_STARTVALUE,
    TX_FIXATIONDATAMODE_SLOW
} TX_FIXATIONDATAMODE;

/*********************************************************************************************************************/

/**
  TX_EYETRACKINGDEVICESTATUS

  Enumeration for all eye tracking device statuses.

  @field TX_EYETRACKINGDEVICESTATUS_INITIALIZING:
    The eye tracking device is initializing.

  @field TX_EYETRACKINGDEVICESTATUS_NOTAVAILABLE:
    There is no eye tracking device available.

  @field TX_EYETRACKINGDEVICESTATUS_INVALIDCONFIGURATION:
    The eye tracking device has an invalid configuration.

  @field TX_EYETRACKINGDEVICESTATUS_DEVICENOTCONNECTED:
    The eye tracking device is not connected.

  @field TX_EYETRACKINGDEVICESTATUS_TRACKING:
    The eye tracking device is currently tracking.

  @field TX_EYETRACKINGDEVICESTATUS_TRACKINGPAUSED:
    The eye tracking device is paused.

  @field TX_EYETRACKINGDEVICESTATUS_CONFIGURING:
    The eye tracking device is being configured.

  @field TX_EYETRACKINGDEVICESTATUS_UNKNOWNERROR:
    Unknown error.
    
  @field TX_EYETRACKINGDEVICESTATUS_CONNECTIONERROR:
    The eye tracking device is connected to USB port but EyeX Engine can not connect to it.

  @field TX_EYETRACKINGDEVICESTATUS_TRACKINGUNAVAILABLE:
    The eye tracking device is functioning as intended, but no gaze data is sent.
 */
typedef enum {
    TX_EYETRACKINGDEVICESTATUS_INITIALIZING = TX_ENUM_STARTVALUE,
    TX_EYETRACKINGDEVICESTATUS_NOTAVAILABLE,
    TX_EYETRACKINGDEVICESTATUS_INVALIDCONFIGURATION,
    TX_EYETRACKINGDEVICESTATUS_DEVICENOTCONNECTED,    
    TX_EYETRACKINGDEVICESTATUS_TRACKING,
    TX_EYETRACKINGDEVICESTATUS_TRACKINGPAUSED,
    TX_EYETRACKINGDEVICESTATUS_CONFIGURING,
    TX_EYETRACKINGDEVICESTATUS_UNKNOWNERROR,
    TX_EYETRACKINGDEVICESTATUS_CONNECTIONERROR,
    TX_EYETRACKINGDEVICESTATUS_TRACKINGUNAVAILABLE
} TX_EYETRACKINGDEVICESTATUS;

/*********************************************************************************************************************/

/**
  Enumeration for all command types. For internal use only.
 */
typedef enum {
    TX_COMMANDTYPE_EXECUTEACTION = TX_ENUM_STARTVALUE,
    TX_COMMANDTYPE_SETSTATE,
    TX_COMMANDTYPE_GETSTATE,
    TX_COMMANDTYPE_REGISTERSTATEOBSERVER,
    TX_COMMANDTYPE_UNREGISTERSTATEOBSERVER,    
    TX_COMMANDTYPE_COMMITSNAPSHOT,    
    TX_COMMANDTYPE_ENABLEBUILTINKEYS,
    TX_COMMANDTYPE_DISABLEBUILTINKEYS,
    TX_COMMANDTYPE_CLIENTCONNECTION,
    TX_COMMANDTYPE_LAUNCHEYETRACKINGCONTROLPANEL, /** Deprecated */
    TX_COMMANDTYPE_REGISTERQUERYHANDLER,
    TX_COMMANDTYPE_UNREGISTERQUERYHANDLER,
    TX_COMMANDTYPE_DIAGNOSTICSREQUEST,
    TX_COMMANDTYPE_LAUNCHCONFIGURATIONTOOL,
    TX_COMMANDTYPE_SETCURRENTPROFILE,
    TX_COMMANDTYPE_DELETEPROFILE,
    TX_COMMANDTYPE_CLIENTPROCESSIDLIST


} TX_COMMANDTYPE;

/*********************************************************************************************************************/

/**
  Enumeration for all action types.
  An action is a way to interact with the EyeX Engine in addition or instead of the default keybindings that normally
  executes gaze actions.

  @field TX_ACTIONTYPE_ACTIVATE:
    Activates an interactor. This corresponds to a click on the activation button

  @field TX_ACTIONTYPE_ACTIVATIONMODEON:
    Turns on activation mode. This corresponds to pressing the activation button.

  @field TX_ACTIONTYPE_ACTIVATIONMODEOFF:
    Turns off activation mode. This corresponds to releasing the activation button.

  @field TX_ACTIONTYPE_PANNINGBEGIN:
    Begins a panning. This corresponds to pressing the panning button.

  @field TX_ACTIONTYPE_PANNINGEND:
    Ends a panning. This corresponds to releasing the panning button.

  @field TX_ACTIONTYPE_PANNINGSTEP:
    Performs a panning step action. This corresponds to a click on the panning button.

  @field TX_ACTIONTYPE_ZOOMIN:
    Not yet supported. 

  @field TX_ACTIONTYPE_ZOOMOUT:
    Not yet supported. 

  @field TX_ACTIONTYPE_PANNINGTOGGLEHANDSFREE:
    Not yet supported. 
 */
typedef enum {
    TX_ACTIONTYPE_ACTIVATE = TX_ENUM_STARTVALUE,
    TX_ACTIONTYPE_ACTIVATIONMODEON,
    TX_ACTIONTYPE_ACTIVATIONMODEOFF,
    TX_ACTIONTYPE_PANNINGBEGIN,
    TX_ACTIONTYPE_PANNINGEND,    
    TX_ACTIONTYPE_PANNINGSTEP,
    TX_ACTIONTYPE_ZOOMIN,
    TX_ACTIONTYPE_ZOOMOUT,
    TX_ACTIONTYPE_PANNINGTOGGLEHANDSFREE
} TX_ACTIONTYPE;

/*********************************************************************************************************************/

/**
  TX_PANNABLEEVENTTYPE

  Enumeration for all pannable event types.
  Pannable event type are metadata contained by all behaviors of type TX_BEHAVIORTYPE_PANNABLE sent
  from the client. This event type specifies what kind of pannable event actually happened.
  
  @field TX_PANNABLEEVENTTYPE_PAN:
    The interactor has been panned.

  @field TX_PANNABLEEVENTTYPE_STEP:
    The interactor has been stepped.

  @field TX_PANNABLEEVENTTYPE_HANDSFREE:
    Not yet supported.
 */
typedef enum {    
    TX_PANNABLEEVENTTYPE_PAN = TX_ENUM_STARTVALUE,
    TX_PANNABLEEVENTTYPE_STEP,
    TX_PANNABLEEVENTTYPE_HANDSFREE
} TX_PANNABLEEVENTTYPE;


/*********************************************************************************************************************/

/**
  TX_PANDIRECTION

  Enumeration flags for all pannable directions.
  Governs available directions to pan for a pannable interactor. 
  The directions are bitwise combinable.

  @field TX_PANDIRECTION_NONE:
    No pandirection available.

  @field TX_PANDIRECTION_LEFT:
    Panning to the left available.

  @field TX_PANDIRECTION_RIGHT:
    Panning to the right available.

  @field TX_PANDIRECTION_UP:
    Panning up available.

  @field TX_PANDIRECTION_DOWN:
    Panning down available.

  @field TX_PANDIRECTION_ALL:
    All pan directions available.
 */
typedef enum {
    TX_PANDIRECTION_NONE = TX_FLAGS_NONE_VALUE,
    TX_PANDIRECTION_LEFT = 1,
    TX_PANDIRECTION_RIGHT = 1 << 1,
    TX_PANDIRECTION_UP = 1 << 2,
    TX_PANDIRECTION_DOWN = 1 << 3,
    TX_PANDIRECTION_ALL = (1 << 4)-1
} TX_PANDIRECTION;

/*********************************************************************************************************************/

/**
  TX_PANNINGPROFILE
  
  Enumeration for all panning profiles.

  @field TX_PANNINGPROFILE_NONE:
    No panning profile.

  @field TX_PANNINGPROFILE_READING:
    Panning profile for reading, currently same as TX_PANNINGPROFILE_VERTICAL. Will be available in subsequent versions.

  @field TX_PANNINGPROFILE_HORIZONTAL:
    Left and right only panning profile.

  @field TX_PANNINGPROFILE_VERTICAL:
    Up and down only panning profile.

  @field TX_PANNINGPROFILE_VERTICALFIRSTTHENHORIZONTAL:
    Up, down, left and right, with emphasis on vertical panning.

  @field TX_PANNINGPROFILE_RADIAL:
    Panning in any direction.

  @field TX_PANNINGPROFILE_HORIZONTALFIRSTTHENVERTICAL:
    Up, down, left and right, with emphasis on horizontal panning.
 */
typedef enum {
    TX_PANNINGPROFILE_NONE = TX_ENUM_STARTVALUE,
    TX_PANNINGPROFILE_READING,
    TX_PANNINGPROFILE_HORIZONTAL,
    TX_PANNINGPROFILE_VERTICAL,
    TX_PANNINGPROFILE_VERTICALFIRSTTHENHORIZONTAL,
    TX_PANNINGPROFILE_RADIAL,    
    TX_PANNINGPROFILE_HORIZONTALFIRSTTHENVERTICAL
} TX_PANNINGPROFILE;

/*********************************************************************************************************************/

/**
  TX_GAZETRACKING

  \since Version 1.4.0

  Enumeration for conveying gaze tracking status. 

  @field TX_GAZETRACKING_GAZETRACKED:
    Gaze is currently being tracked.

  @field TX_GAZETRACKING_GAZENOTTRACKED:
    Gaze is currently not being tracked. This state can be set when for example the user is gazing outside of the
    tracked display, no user is present in front of the eye tracker, or that no connection is established with the
    eye tracker etc.
 */

 typedef enum {
    TX_GAZETRACKING_GAZETRACKED = TX_ENUM_STARTVALUE,
    TX_GAZETRACKING_GAZENOTTRACKED
} TX_GAZETRACKING;

/*********************************************************************************************************************/

/**
  TX_USERPRESENCE
 
  Enumeration for conveying presence status.

  @field TX_USERPRESENCE_PRESENT:
    A user is present in front of the eye tracker.
  
  @field TX_USERPRESENCE_NOTPRESENT:
    A user is not present in front of the eye tracker.

  @field TX_USERPRESENCE_UNKNOWN:
    It is unknown whether or not a user is present in front of the eye tracker.
    This value will be returned if there is no observer registered for TX_STATEPATH_USERPRESENCE.
 */
typedef enum {
    TX_USERPRESENCE_PRESENT = TX_ENUM_STARTVALUE,
    TX_USERPRESENCE_NOTPRESENT,
    TX_USERPRESENCE_UNKNOWN
} TX_USERPRESENCE;

/*********************************************************************************************************************/

/**
  TX_REQUESTTYPE

  Enumeration for all the types of requests that can be exposed through the API.
    
  @field TX_REQUESTTYPE_COMMAND:
    The request handles a command.
    
  @field TX_REQUESTTYPE_CUSTOM:
    Base value for custom requests defined by other protocols.
 */
typedef enum {
    TX_REQUESTTYPE_COMMAND = TX_ENUM_STARTVALUE,    
    TX_REQUESTTYPE_CUSTOM = TX_INTERNAL_ENUM_STARTVALUE
} TX_REQUESTTYPE;

/*********************************************************************************************************************/

/**
  TX_MASKTYPE

  Enumeration for mask types.

  @field TX_MASKTYPE_DEFAULT:
    Default mask type.
 */
typedef enum {
    TX_MASKTYPE_DEFAULT = TX_ENUM_STARTVALUE
} TX_MASKTYPE;

/*********************************************************************************************************************/

/**
  TX_INTERACTIONMODES 

  \since Version 1.1.0

  Flags for describing engine interaction modes. These influence what behaviors are being 
  treated and what interaction behavior events are being generated.

  @field TX_INTERACTIONMODES_NONE:
    Engine is not in any specific interacion mode, gaze aware behaviors and data stream 
    behaviors are being treated only.

  @field TX_INTERACTIONMODES_ACTIVATIONMODE:
    Engine is in activation mode, meaning activatable interactors are prioritized and 
    activation events are being generated.

  @field TX_INTERACTIONMODES_PANNINGMODE:
    Engine is in panning mode, meaning pannable interactors are being prioritzed, and 
    appropriate events being generated.

 */
typedef enum {
    TX_INTERACTIONMODES_NONE           = TX_FLAGS_NONE_VALUE,
    TX_INTERACTIONMODES_ACTIVATIONMODE = 1,
    TX_INTERACTIONMODES_PANNINGMODE    = 1 << 2
} TX_INTERACTIONMODES;

/*********************************************************************************************************************/


typedef enum {
    TX_CLIENTMODE_AGENT = TX_ENUM_STARTVALUE,
    TX_CLIENTMODE_DIAGNOSTICS
} TX_CLIENTMODE;


/*********************************************************************************************************************/

/**
  TX_CONFIGURATIONTOOL

  \since Version 1.1.0
  
  Enumeration for configuration tools.

  @field TX_CONFIGURATIONTOOL_EYEXSETTINGS:
    EyeX Settings. Always available. 

  @field TX_CONFIGURATIONTOOL_RECALIBRATE: 
    Re-Calibrate the current profile. 
    Available when the following is fulfilled:
    - Eye Tracking Device Status is "Tracking", "TrackingPaused" or "InvalidConfiguration".
    - Eye Tracking Configuration Status is "Valid" or "InvalidCalibration".
    - State TX_STATEPATH_EYETRACKINGCURRENTPROFILENAME has a value other than empty string.

    When the Recalibrate tool is active the Eye Tracking Device Status will be "Configuring", i.e. tracking is off.
  
  @field TX_CONFIGURATIONTOOL_GUESTCALIBRATION:
    Create and calibrate a guest profile and set it as active profile.
    Available when the following is fulfilled:
    - Eye Tracking Device Status is "Tracking", "TrackingPaused" or "InvalidConfiguration".
    - Eye Tracking Configuration Status is "Valid" or "InvalidCalibration".

    When the Guest Calibration tool is active the Eye Tracking Device Status will be "Configuring", i.e. tracking is off.
      
  @field TX_CONFIGURATIONTOOL_CREATENEWPROFILE:
    Create and calibrate a new profile and set it as active profile.
    Available when the following is fulfilled:
    - Eye Tracking Device Status is "Tracking", "TrackingPaused" or "InvalidConfiguration".
    - Eye Tracking Configuration Status is "Valid" or "InvalidCalibration".

    When the Create New Profile tool is active the Eye Tracking Device Status will be "Configuring", i.e. tracking is off.

  @field TX_CONFIGURATIONTOOL_TESTEYETRACKING:
    Test your eye tracking.
    Available when the following is fulfilled:
    - Eye Tracking Device Status is "Tracking".    
    
  @field TX_CONFIGURATIONTOOL_DIAGNOSTICS:
    Diagnose your eye tracking.
    Always available.
*/

typedef enum {
    TX_CONFIGURATIONTOOL_EYEXSETTINGS = TX_ENUM_STARTVALUE,
    TX_CONFIGURATIONTOOL_RECALIBRATE,
    TX_CONFIGURATIONTOOL_GUESTCALIBRATION,
    TX_CONFIGURATIONTOOL_CREATENEWPROFILE,
    TX_CONFIGURATIONTOOL_TESTEYETRACKING,
    TX_CONFIGURATIONTOOL_DIAGNOSTICS,

    /* for internal use only */
    TX_CONFIGURATIONTOOL_SETUPDISPLAY = TX_INTERNAL_ENUM_STARTVALUE,
    TX_CONFIGURATIONTOOL_EYEPOSITION,
    TX_CONFIGURATIONTOOL_EYECAPTURE,
    TX_CONFIGURATIONTOOL_FIRMWARE_UPGRADE,
    TX_CONFIGURATIONTOOL_CHECKFORUPDATES,
    TX_CONFIGURATIONTOOL_RETAILCALIBRATION
} TX_CONFIGURATIONTOOL;

/*********************************************************************************************************************/
/**
  TX_EYETRACKINGCONFIGURATIONSTATUS

  \since Version 1.1.0

  Enumeration for configuration status. Gives information about the configuration status of the eye tracker,
  for example if it needs to be calibrated or if we need to setup display. Can be used as input to determine
  when to enable launching of configuration tools, see txLaunchConfigurationTool
  and TX_CONFIGURATIONTOOL.

  @field TX_EYETRACKINGCONFIGURATIONSTATUS_VALID:
  The configuration status of the eye tracker is valid.

  @field TX_EYETRACKINGCONFIGURATIONSTATUS_INVALIDMONITORCONFIGURATION:
  The monitor where the eye tracker is mounted need to be configured.

  @field TX_EYETRACKINGCONFIGURATIONSTATUS_INVALIDCALIBRATION:
  The eye tracker need to be calibrated. If no user profile exists (see state TX_STATEPATH_EYETRACKINGCURRENTPROFILENAME) a new profile should be created.

  @field TX_EYETRACKINGCONFIGURATIONSTATUS_UNKNOWNERROR:
  The configuration is in an unknown error state.
*/

typedef enum {
    TX_EYETRACKINGCONFIGURATIONSTATUS_VALID = TX_ENUM_STARTVALUE,
    TX_EYETRACKINGCONFIGURATIONSTATUS_INVALIDMONITORCONFIGURATION,
    TX_EYETRACKINGCONFIGURATIONSTATUS_INVALIDCALIBRATION,
    TX_EYETRACKINGCONFIGURATIONSTATUS_UNKNOWNERROR
} TX_EYETRACKINGCONFIGURATIONSTATUS;


/*********************************************************************************************************************/

/**
  TX_TRACKEDEYE

  \since Version 1.3.0

  Enumeration for tracked eye of current profile.

  @field TX_TRACKEDEYES_BOTH:
  Track both eyes.

  @field TX_TRACKEDEYES_ONLY_LEFT_EYE:
  Track left eye only.

  @field TX_TRACKEDEYES_ONLY_RIGHT_EYE:
  Track right eye only.

  @field TX_EYETRACKINGCONFIGURATIONSTATUS_INVALIDMONITORCONFIGURATION:
  The monitor where the eye tracker is mounted need to be configured.
*/
typedef enum {
    TX_TRACKEDEYES_BOTH = TX_ENUM_STARTVALUE,
    TX_TRACKEDEYES_ONLY_LEFT_EYE,
    TX_TRACKEDEYES_ONLY_RIGHT_EYE
} TX_TRACKEDEYES;


/*********************************************************************************************************************/

/**
  TX_HANDSFREEPANNINGMODE
  
  This is For internal use only!

  Enumeration for the different hands free modes.
  Used in TX_PANNABLE_PARAMS for a specific behavior/interactor

  @field TX_HANDSFREEPANNINGMODE_DEFAULT:
    Handsfree panning follows the current engine panning handsfree state, which may be set by the user.

    @field TX_HANDSFREEPANNINGMODE_ALWAYSENABLED:
    Handsfree panning is always enabled, regardless of engine panning handsfree state. 

  @field TX_HANDSFREEPANNINGMODE_ALWAYSDISABLED:
    Handsfree panning is always disabled, regardless of engine panning handsfree state. 

 */
typedef enum {
	TX_HANDSFREEPANNINGMODE_DEFAULT = 0,
	TX_HANDSFREEPANNINGMODE_ALWAYSENABLED,
	TX_HANDSFREEPANNINGMODE_ALWAYSDISABLED
} TX_HANDSFREEPANNINGMODE;

/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_FRAMEWORK_TYPES__H__) */

/*********************************************************************************************************************/

/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXClientTypes.h
 *********************************************************************************************************************/

#if !defined(__TOBII_TX_CLIENT_TYPES__H__)
#define __TOBII_TX_CLIENT_TYPES__H__

/*********************************************************************************************************************
 * Common types
 *********************************************************************************************************************/

typedef void* TX_USERPARAM;
typedef struct txInteractionObject* TX_HANDLE;
typedef const struct txInteractionObject* TX_CONSTHANDLE;
typedef struct txProperty* TX_PROPERTYHANDLE;
typedef const struct txProperty* TX_CONSTPROPERTYHANDLE;
typedef struct txContext* TX_CONTEXTHANDLE;
typedef const struct txContext* TX_CONSTCONTEXTHANDLE;
typedef int TX_TICKET;
typedef int TX_BOOL;
typedef unsigned char TX_BYTE;
typedef int TX_SIZE;
typedef int TX_INTEGER;
typedef double TX_REAL;
typedef char TX_CHAR;
typedef char* TX_STRING;
typedef const char* TX_CONSTSTRING;
typedef void* TX_RAWPTR;
typedef int TX_THREADID;

/*********************************************************************************************************************/

#include "EyeXInternalTypes.h"

/*********************************************************************************************************************/

#define TX_EMPTY_HANDLE 0
#define TX_INVALID_TICKET 0

#define TX_TRUE 1
#define TX_FALSE 0

#define TX_CLEANUPTIMEOUT_DEFAULT 500
#define TX_CLEANUPTIMEOUT_FORCEIMMEDIATE -1

/*********************************************************************************************************************/

/**
  TX_EYEXCOMPONENTOVERRIDEFLAGS    

  Enumeration for all client environment component override flags.
  When calling txInitializeEyeX these flags must be combined to specify which components should be overridden.

  @field TX_EYEXCOMPONENTOVERRIDEFLAG_NONE:
    No client environment component should be overridden.

  @field TX_EYEXCOMPONENTOVERRIDEFLAG_LOGGINGMODEL:
    The logging model should be overridden.
    The logging model can be overridden by just specifying some of the standard log targets (see TX_LOGTARGET) or by
    a custom user implemented log writer.
    
  @field TX_EYEXCOMPONENTOVERRIDEFLAG_INTERNAL_MEMORYMODEL:
    The memory model should be overridden. For internal use only.
    
  @field TX_EYEXCOMPONENTOVERRIDEFLAG_INTERNAL_THREADINGMODEL:
    The threading model should be overridden. For internal use only.
    
  @field TX_EYEXCOMPONENTOVERRIDEFLAG_INTERNAL_SCHEDULINGMODEL:
    The scheduling model should be overridden. For internal use only.
 */ 
typedef enum {
    TX_EYEXCOMPONENTOVERRIDEFLAG_NONE = TX_FLAGS_NONE_VALUE,  
    TX_EYEXCOMPONENTOVERRIDEFLAG_LOGGINGMODEL = 1 << 0,
    TX_EYEXCOMPONENTOVERRIDEFLAG_INTERNAL_MEMORYMODEL = 1 << 1,
    TX_EYEXCOMPONENTOVERRIDEFLAG_INTERNAL_THREADINGMODEL = 1 << 2,    
    TX_EYEXCOMPONENTOVERRIDEFLAG_INTERNAL_SCHEDULINGMODEL = 1 << 3
} TX_EYEXCOMPONENTOVERRIDEFLAGS;

/*********************************************************************************************************************/

/**
  TX_CONNECTIONSTATE

  Enumeration for all connection states.
  These values are used to notify the application of the current connection state. 
  To receive these notifications the client needs to subscribe using txRegisterConnectionStateChangedHandler and then 
  call txEnableConnection. 

  @field TX_CONNECTIONSTATE_CONNECTED:
    The client is now connected to the server.

  @field TX_CONNECTIONSTATE_DISCONNECTED:
    The client is now disconnected from the server. Unless this is due to txDisableConnection being called the client
    will shortly attempt to connect again.
    
  @field TX_CONNECTIONSTATE_TRYINGTOCONNECT:
    The client is now trying to connect to the server. This is the first state being sent to the application after 
    txEnableConnection has been called.
    
  @field TX_CONNECTIONSTATE_SERVERVERSIONTOOLOW:
    the server version is too low. The client is not connected and will not try to reconnect.
    
  @field TX_CONNECTIONSTATE_SERVERVERSIONTOOHIGH:
    the server version is too high. The client is not connected and will not try to reconnect.
 */ 
typedef enum {
    TX_CONNECTIONSTATE_CONNECTED = TX_ENUM_STARTVALUE,  
    TX_CONNECTIONSTATE_DISCONNECTED,
    TX_CONNECTIONSTATE_TRYINGTOCONNECT,
    TX_CONNECTIONSTATE_SERVERVERSIONTOOLOW,
    TX_CONNECTIONSTATE_SERVERVERSIONTOOHIGH
} TX_CONNECTIONSTATE;

/*********************************************************************************************************************/

/**
  TX_LOGTARGET

  Enumeration for all log targets.
  When overriding the logging model these flags specify which log targets to use. The flags can be combined.

  @field TX_LOGTARGET_NONE:
    No logging should occur at all.

  @field TX_LOGTARGET_CONSOLE:
    The log message should be written to the console.
    
  @field TX_LOGTARGET_TRACE:
    The log messages should be traced. (output window i Visual Studio)
    
  @field TX_LOGTARGET_CUSTOM:
    The specified TX_LOGCALLBACK should be invoked for custom logging.
 */
typedef enum {
    TX_LOGTARGET_NONE = TX_FLAGS_NONE_VALUE,
    TX_LOGTARGET_CONSOLE = 1 << 0,
    TX_LOGTARGET_TRACE = 1 << 1,    
    TX_LOGTARGET_CUSTOM = 1 << 2
} TX_LOGTARGET;

/*********************************************************************************************************************/

/**
  TX_LOGLEVEL

  Enumeration for all log levels.
  The log levels are used to indicate the severity of the message.

  @field TX_LOGLEVEL_DEBUG:
    The message is just a debug print out typically used during development.

  @field TX_LOGLEVEL_INFO:
    The message is plain info and does not indciate that something is wrong.
    
  @field TX_LOGLEVEL_WARNING:
    The message is a warning that indicates that something is not the way it should, not yet critical.
    
  @field TX_LOGLEVEL_ERROR:
    The message indicates that there is some kind of error.
 */
typedef enum {
    TX_LOGLEVEL_DEBUG  = TX_ENUM_STARTVALUE,
    TX_LOGLEVEL_INFO,
    TX_LOGLEVEL_WARNING,
    TX_LOGLEVEL_ERROR    
} TX_LOGLEVEL;

/*********************************************************************************************************************/

/**
  TX_SCHEDULINGMODE

  Enumeration for all schedulng modes.
  When overriding the scheduling model the mode specifies which of the available scheduling modes to use.

  @field TX_SCHEDULINGMODE_DIRECT:
    All jobs are performed immediately on the thread that calls them.

  @field TX_SCHEDULINGMODE_USERFRAME:
    All jobs are performed when the txPerformScheduledJobs are called.
    
  @field TX_SCHEDULINGMODE_CUSTOM:
    Whenever a job is to be performed a callback function is invoked giving the client application full control.
 */
typedef enum {
    TX_SCHEDULINGMODE_DIRECT = TX_ENUM_STARTVALUE,
    TX_SCHEDULINGMODE_USERFRAME,
    TX_SCHEDULINGMODE_CUSTOM    
} TX_SCHEDULINGMODE;

/*********************************************************************************************************************/

/**
  TX_PROPERTYVALUETYPE   

  Enumeration for all property value types.

  @field TX_PROPERTYVALUETYPE_EMPTY:
    The property does not have a value.

  @field TX_PROPERTYVALUETYPE_OBJECT:
    The property currently holds an interaction object.
    
  @field TX_PROPERTYVALUETYPE_INTEGER:
    The property currently holds an integer.

  @field TX_PROPERTYVALUETYPE_REAL:
    The property currently holds a real.

  @field TX_PROPERTYVALUETYPE_STRING:
    The property currently holds a string.
        
  @field TX_PROPERTYVALUETYPE_BLOB:
    The property currently holds a blob.
 */ 
typedef enum {
    TX_PROPERTYVALUETYPE_EMPTY = TX_ENUM_STARTVALUE,
    TX_PROPERTYVALUETYPE_OBJECT,
    TX_PROPERTYVALUETYPE_INTEGER,
    TX_PROPERTYVALUETYPE_REAL,
    TX_PROPERTYVALUETYPE_STRING,
    TX_PROPERTYVALUETYPE_BLOB
} TX_PROPERTYVALUETYPE;

/*********************************************************************************************************************/

/**
  TX_PROPERTYBAGTYPE

  Enumeration for the all Property Bag types.
    
  @field TX_PROPERTYBAGTYPE_OBJECT:
    The property is a normal object with named properties.

  @field TX_PROPERTYBAGTYPE_ARRAY:
    The property bag is an array with sequentially named properties appearing in the order they where inserted.
 */
typedef enum {
    TX_PROPERTYBAGTYPE_OBJECT = TX_ENUM_STARTVALUE,
    TX_PROPERTYBAGTYPE_ARRAY
} TX_PROPERTYBAGTYPE;

/*********************************************************************************************************************/

/**
  TX_EYEXAVAILABILITY

  Enumeration for the availability status of the EyeX Engine.

  @field TX_EYEXAVAILABILITY_NOTAVAILABLE:
    EyeX Engine is not installed on the system or otherwise not available.

  @field TX_EYEXAVAILABILITY_NOTRUNNING:
	EyeX Engine is not running.
  
  @field TX_EYEXAVAILABILITY_RUNNING:
	EyeX Engine is running.
 */
typedef enum {
	TX_EYEXAVAILABILITY_NOTAVAILABLE = TX_ENUM_STARTVALUE,
	TX_EYEXAVAILABILITY_NOTRUNNING,
	TX_EYEXAVAILABILITY_RUNNING
} TX_EYEXAVAILABILITY;


/*********************************************************************************************************************
 * Callbacks
 *********************************************************************************************************************/

/**
  Callback for when the connection state is changed.
    See txRegisterConnectionStateChangedHandler 
  
  @param state [in]: 
    Specifies the current state of the connection.
  
  @param userParam [in]: 
    The user parameter provided to the txRegisterConnectionStateChangedHandler function.
 
  @return 
    void
 */ 
typedef void (TX_CALLCONVENTION *TX_CONNECTIONSTATECHANGEDCALLBACK)(
    TX_CONNECTIONSTATE state,
    TX_USERPARAM userParam
    );

#if defined(__cplusplus)
#ifndef TOBII_TX_INTEROP
#include <functional>

    namespace Tx {
        typedef std::function<void (TX_CONNECTIONSTATE)> ConnectionStateChangedCallback;
    }

#endif
#endif
    
/*********************************************************************************************************************/

/**
  Callback for an asynchronous operations.
   
  @param hAsyncData [in]: 
    A TX_CONSTHANDLE to the async data. 
 
  @param userParam [in]: 
    The user parameter provided to the asynchronous operation.
        
  @return 
    void
 */
typedef void (TX_CALLCONVENTION *TX_ASYNCDATACALLBACK)(    
    TX_CALLBACK_PARAM(TX_CONSTHANDLE) hAsyncData,
    TX_USERPARAM userParam
    );

#if defined(__cplusplus)
#ifndef TOBII_TX_INTEROP
#include <functional>

    namespace Tx {
        typedef std::function<void (TX_CONSTHANDLE)> AsyncDataCallback;
    }

#endif
#endif

/*********************************************************************************************************************/

/**
  Function run by a thread.
    See txInitializeEyeX, TX_THREADINGMODEL
 
  @param threadWorkerParam [in]: 
    The user parameter provided to the CreateThreadCallback.  
 
  @return 
    void
 */
typedef void (TX_CALLCONVENTION *TX_THREADWORKERFUNCTION)(
    TX_USERPARAM threadWorkerParam
    );

/*********************************************************************************************************************/

/**
  Callback used to create a thread.
    See txInitializeEyeX, TX_THREADINGMODEL
 
  @param worker [in]: 
   Worker function that will be run by the thread.
 
  @param threadWorkerParam [in]:
    A user parameter passed to worker function.
 
  @param userParam [in]: 
    The user parameter provided by the TX_THREADINGMODEL structure.
 
  @return 
    TX_THREADID, the id of the created thread.
 */
typedef TX_THREADID (TX_CALLCONVENTION *TX_CREATETHREADCALLBACK)(
    TX_THREADWORKERFUNCTION worker,
    TX_USERPARAM threadWorkerParam,
    TX_USERPARAM userParam
    );

/*********************************************************************************************************************/

/**
  Callback used to get the current thread id.
    See txInitializeEyeX, TX_THREADINGMODEL
 
  @param userParam [in]: 
    The user parameter provided by the TX_THREADINGMODEL structure.
 
  @return 
    TX_THREADID, the id of the current thread
 */
typedef TX_THREADID (TX_CALLCONVENTION *TX_GETCURRENTTHREADIDCALLBACK)(
    TX_USERPARAM userParam
    );

/*********************************************************************************************************************/

/**
  Callback used to join a thread.
    See txInitializeEyeX, TX_THREADINGMODEL
 
  @param threadId [in]: 
    The id of the thread to join.
 
  @param userParam [in]: 
    The user parameter provided by the TX_THREADINGMODEL structure.
 
  @return 
    TX_TRUE if the thread was successfully joined. TX_FALSE on non existing thread.
 */
typedef TX_BOOL (TX_CALLCONVENTION *TX_JOINTHREADCALLBACK)(
    TX_THREADID threadId,
    TX_USERPARAM userParam
    );

/*********************************************************************************************************************/

/**
  Callback used to delete a thread.
    See txInitializeEyeX, TX_THREADINGMODEL
 
  @param threadId [in]: 
    The id of the thread to be deleted.
 
  @param userParam [in]: 
    The user parameter provided by the TX_THREADINGMODEL structure.
 
  @return 
    TX_TRUE if the thread was successfully deleted, otherwise TX_FALSE. 
 */
typedef TX_BOOL (TX_CALLCONVENTION *TX_DELETETHREADCALLBACK)(
    TX_THREADID threadId,
    TX_USERPARAM userParam
    );

/*********************************************************************************************************************/

/**
  Callback used to release a threading model or a logging model.
    See SetThreadingModel, SetLoggingModel
 
  @param userParam [in]: 
    Normally used for capture outside the scope of the callback.
 */
typedef void (TX_CALLCONVENTION *TX_DELETEMODELCALLBACK)(
    TX_USERPARAM userParam
    );

/*********************************************************************************************************************/

/**
  Allocator function, used to override allocation of memory
    See SetCustomAllocator
 
  @param length [in]: 
    Size in bytes of the requested memory block
 
  @return 
    void
 */
typedef void (TX_CALLCONVENTION *TX_ALLOCATORFUNCTION)(
    TX_INTEGER length
    );

/*********************************************************************************************************************/

/**
  Callback for logging.
    If a custom logging model is set, see TX_LOGGINGMODEL, this callback will be invoked when a log message is 
    written by the API.
 
  @param level [in]: 
    The level of log message, see TX_LOGLEVEL for levels.
 
  @param scope [in]: 
    A string token representing from which part the log message was originated.
 
  @param message [in]: 
    The message to be logged.
 
  @param userParam [in]: 
    The user parameter provided by the TX_LOGGINGMODEL structure.
  
  @return 
    void
 */
typedef void (TX_CALLCONVENTION *TX_LOGCALLBACK)(
    TX_LOGLEVEL level,
    TX_CONSTSTRING scope,
    TX_CONSTSTRING message,
    TX_USERPARAM userParam
    );

/*********************************************************************************************************************/

/**
  Function provided by the API when a job is scheduled.
    See TX_SCHEDULEJOBCALLBACK.

  @param jobParam [in]: 
    The user parameter provided by the API when a job is scheduled.  
 
  @return 
    void
 */
typedef void (TX_CALLCONVENTION *TX_PERFORMJOBFUNCTION)(
    TX_USERPARAM jobParam
    );

/*********************************************************************************************************************/

/**
  Callback for scheduling a job.
    If a custom scheduling model is set, see TX_SCHEDULINGMODEL, this callback will be invoked when a job is to be
    scheduled.
 
  @param performJob [in]: 
    The function to invoke when the job is to be performed.
 
  @param jobParam [in]: 
    A parameter used to provide a context to the job.
 
  @param userParam [in]: 
    The user parameter provided to the TX_SCHEDULINGMODEL.
   
  @return 
    void
 */
typedef void (TX_CALLCONVENTION *TX_SCHEDULEJOBCALLBACK)(
    TX_PERFORMJOBFUNCTION performJob,
    TX_USERPARAM jobParam,
    TX_USERPARAM userParam
    );

/*********************************************************************************************************************
 * Structs
 *********************************************************************************************************************/

/**
  Struct for a rectangle.
      
  @field X:
    The X coordinate for the upper left corner of the rectangle.
  
  @field Y:
    The Y coordinate for the upper left corner of the rectangle.
  
  @field Width:
    The width of the rectangle.

  @field Height:
    The height of the rectangle.
 */
typedef struct {
    TX_REAL             X;                             
    TX_REAL             Y;                             
    TX_REAL             Width;                         
    TX_REAL             Height;                        
} TX_RECT;

/*********************************************************************************************************************/

/**
  Struct for 2D vector.
   
  @field X:
    The X coordinate of the vector.
  
  @field Y:
    The Y coordinate of the vector.
 */
typedef struct {
    TX_REAL             X;                              
    TX_REAL             Y;                              
} TX_VECTOR2;

 /*********************************************************************************************************************/

/**
  Struct for 2D size.
   
  @field Width:
    The width of the size.
  
  @field Height:
    The height of the size.
 */
typedef struct {
    TX_REAL             Width;                              
    TX_REAL             Height;                             
} TX_SIZE2;

/*********************************************************************************************************************/

/**
  Struct for pannable behavior parameters.
   
  @field IsHandsFreeEnabled:
    Set to TX_FALSE - hands free panning is not yet implemented.
  
  @field Profile:
    The panning profile. See TX_PANNINGPROFILE.
    
  @field PeakVelocity:
    Currently not used.
  
  @field PanDirectionsAvailable:
    Flags specifying which pan directions are currently possible. See TX_PANDIRECTION.
    Correct pan direction flags are needed for panning to work properly. 
 */
typedef struct {
    TX_BOOL            IsHandsFreeEnabled;
    TX_PANNINGPROFILE  Profile;                        
    TX_REAL            PeakVelocity;                   
    TX_PANDIRECTION    PanDirectionsAvailable;         
} TX_PANNABLEPARAMS;

/*********************************************************************************************************************/

/**
  Struct for pannable pan event parameters.
   
  @field PanVelocityX:
    The X velocity for the pan. In pixels per second.
  
  @field PanVelocityY:
    The Y velocity for the pan. In pixels per second.
 */
typedef struct {
    TX_REAL PanVelocityX;
    TX_REAL PanVelocityY;
} TX_PANNABLEPANEVENTPARAMS;

/*********************************************************************************************************************/

/**
  Struct for pannable step event parameters.
   
  @field PanStepX:
    The step length on the X axis in pixels.
  
  @field PanStepY:    
    The step length on the Y axis in pixels.
    
  @field PanStepDuration:    
    The amount of time in seconds during which the step should be performed.
 */
typedef struct {
    TX_REAL PanStepX;
    TX_REAL PanStepY;
    TX_REAL PanStepDuration;
} TX_PANNABLESTEPEVENTPARAMS;

/*********************************************************************************************************************/

/**
  Struct for pannable hands free event parameters.
   
  @field HandsFreeEnabled:
    Specifies if hands free panning is enabled or not.
 */
typedef struct {
    TX_BOOL HandsFreeEnabled;
} TX_PANNABLEHANDSFREEEVENTPARAMS;

/*********************************************************************************************************************/

/**
  Struct for activatable behavior parameters.
   
  @field EnableTentativeFocus:
    Specifies if tentative focus should be enabled.
  @field EnableSmallItemActivation:
    Specifies if small item detection should be enabled.
	For internal use only.
 */
typedef struct {
    TX_BOOL EnableTentativeFocus;    
    TX_BOOL EnableSmallItemDetection;
} TX_ACTIVATABLEPARAMS;

/*********************************************************************************************************************/

/**
  Struct for gaze aware parameters.
   
  @field GazeAwareMode:
    Specifies the gaze aware mode. See TX_GAZEAWAREMODE.

  @field DelayTime:
    Specifies the amount of time in milliseconds that the user has to look at an interactor before a gaze aware event
    is sent. This value only has an effect if the mode is set to TX_GAZEAWAREMODE_DELAYED.
 */
typedef struct {
    TX_GAZEAWAREMODE GazeAwareMode;
    TX_REAL DelayTime;                  
} TX_GAZEAWAREPARAMS;

/*********************************************************************************************************************/

/**
  Struct for gaze aware event parameters.
   
  @field HasGaze:
    Specifies if the interactor currently has gaze on it.
 */
typedef struct {
    TX_BOOL HasGaze;  
} TX_GAZEAWAREEVENTPARAMS;

/*********************************************************************************************************************/

/**
  Struct for activation focus changed Params.
   
  @field HasTentativeActivationFocus:
    Specifies if the interactor currently has tentative activation focus.
  
  @field HasActivationFocus:    
    Specifies if the interactor currently has activation focus.
 */
typedef struct {
    TX_BOOL HasTentativeActivationFocus;              
    TX_BOOL HasActivationFocus;                       
} TX_ACTIVATIONFOCUSCHANGEDEVENTPARAMS;

/*********************************************************************************************************************/

/**
  Struct for gaze point data behavior parameters.
   
  @field GazePointDataMode:
    Specifies the gaze point data mode. See TX_GAZEPOINTDATAMODE.
 */
typedef struct {
    TX_GAZEPOINTDATAMODE GazePointDataMode;          
} TX_GAZEPOINTDATAPARAMS;

/*********************************************************************************************************************/
    
/**
  Struct for fixation behavior parameters.
   
  @field FixationDataMode:
    Specifies the fixation data mode. See TX_FIXATIONDATAMODE.
 */
typedef struct {
    TX_FIXATIONDATAMODE FixationDataMode;       
} TX_FIXATIONDATAPARAMS;

/*********************************************************************************************************************/

/**
  Struct for fixation behavior event parameters.
   
  @field FixationDataMode:
    The fixation data mode. See TX_FIXATIONDATAMODE.
    
  @field EventType:
    The type of fixation event. See TX_FIXATIONDATAEVENTTYPE.
    
  @field Timestamp:
    For TX_FIXATIONDATAEVENTTYPE_BEGIN, this is the time when the fixation started, in milliseconds.
    For TX_FIXATIONDATAEVENTTYPE_END, this is the time when the fixation ended, in milliseconds.
    For TX_FIXATIONDATAEVENTTYPE_DATA, the timestamp for the filtered gaze point provided within 
    the current fixation, when the filter was applied, in milliseconds.
    
  @field X:
    The current X coordinate of the fixation in pixels. For begin and end events will reflect where the fixation 
    began or ended.
    
  @field Y:
    The current Y coordinate of the fixation in pixels. For begin and end events will reflect where the fixation 
    began or ended.
 */
typedef struct {
    TX_FIXATIONDATAMODE FixationDataMode;
    TX_FIXATIONDATAEVENTTYPE EventType;
    TX_REAL Timestamp;
    TX_REAL X;       
    TX_REAL Y;
} TX_FIXATIONDATAEVENTPARAMS;

/*********************************************************************************************************************/

/**
  Struct for gaze point data behavior event parameters. 
   
  @field GazePointDataMode:
    The gaze point data mode. See TX_GAZEPOINTDATAMODE.
    
  @field Timestamp:
    For TX_GAZEPOINTDATAMODE_LIGHTLYFILTERED this is the point in time when the filter was applied, in milliseconds.
    For TX_GAZEPOINTDATAMODE_UNFILTERED this is the point in time time when gazepoint was captured, in milliseconds.
    
  @field X:
    The X coordinate of the gaze point in pixels.
    
  @field Y:
    The Y coordinate of the gaze point in pixels.
 */
typedef struct {
    TX_GAZEPOINTDATAMODE GazePointDataMode;
    TX_REAL Timestamp;
    TX_REAL X;       
    TX_REAL Y;
} TX_GAZEPOINTDATAEVENTPARAMS;

/*********************************************************************************************************************/

/**
  Struct for eye position data behavior event parameters.

  The components of the eye vectors are the relative position of the eyes from the center of the screen in
  millimeters on each axis.
   
  @field Timestamp:
    The point in time when the eye position was captured, in milliseconds.
    
  @field HasLeftEyePosition:
    Specifies if the data for the left eye is valid.
    
  @field HasRightEyePosition:
    Specifies if the data for the right eye is valid.
    
  @field LeftEyeX:
    The X coordinate of the left eye in millimeters.
    
  @field LeftEyeY:
    The Y coordinate of the left eye in millimeters.
    
  @field LeftEyeZ:
    The Z coordinate of the left eye in millimeters.

  @field LeftEyeXNormalized:
    The X coordinate of the left eye normalized in the track box.
    
  @field LeftEyeYNormalized:
    The Y coordinate of the left eye normalized in the track box.
    
  @field LeftEyeZNormalized:
    The Z coordinate of the left eye normalized in the track box.
    
  @field RightEyeX:
    The X coordinate of the right eye in millimeters.
    
  @field RightEyeY:
    The Y coordinate of the right eye in millimeters.
    
  @field RightEyeZ:
    The Z coordinate of the right eye in millimeters.

  @field RightEyeXNormalized:
    The X coordinate of the right eye normalized in the track box.
    
  @field RightEyeYNormalized:
    The Y coordinate of the right eye normalized in the track box.
    
  @field RightEyeZNormalized:
    The Z coordinate of the right eye normalized in the track box.
 */
typedef struct {
    TX_REAL Timestamp;
    TX_BOOL HasLeftEyePosition;
    TX_BOOL HasRightEyePosition;
    TX_REAL LeftEyeX;
    TX_REAL LeftEyeY;
    TX_REAL LeftEyeZ;
    TX_REAL LeftEyeXNormalized;
    TX_REAL LeftEyeYNormalized;
    TX_REAL LeftEyeZNormalized;
    TX_REAL RightEyeX;
    TX_REAL RightEyeY;
    TX_REAL RightEyeZ;
    TX_REAL RightEyeXNormalized;
    TX_REAL RightEyeYNormalized;
    TX_REAL RightEyeZNormalized;
} TX_EYEPOSITIONDATAEVENTPARAMS;

/*********************************************************************************************************************/

/**
  Struct for the threading model.
   
  @field CreateThread:
    Callback function used to create a thread. See TX_CREATETHREADCALLBACK.
    
  @field GetCurrentThreadId:
     Callback function used to get the id of the current (calling) thread. See TX_GETCURRENTTHREADIDCALLBACK.
        
  @field JoinThread:
     Callback function used to join a thread. See TX_JOINTHREADCALLBACK.
    
  @field DeleteThread:
     Callback function used to delete a thread. See TX_DELETETHREADCALLBACK.
    
  @field DeleteModel:
     Callback function used to release the threading model.
    
  @field UserParam:
    User parameter which will be passed to the functions.
 */
typedef struct {
    TX_CREATETHREADCALLBACK       CreateThread;         
    TX_GETCURRENTTHREADIDCALLBACK GetCurrentThreadId;   
    TX_JOINTHREADCALLBACK         JoinThread;           
    TX_DELETETHREADCALLBACK       DeleteThread;         
    TX_DELETEMODELCALLBACK        DeleteModel;
    TX_USERPARAM                  UserParam;            
} TX_THREADINGMODEL;

/*********************************************************************************************************************/

/**
  Struct for the logging model.
   
  @field Targets:
     Specifies which log targets to use. See TX_LOGTARGET.
    
  @field Log:
     Callback function used to write a custom log message. See TX_LOGCALLBACK.
    
  @field DeleteModel:
     Callback function used to release the logging model.
    
  @field UserParam:
    User parameter which will be passed to the custom log function.
 */
typedef struct {
    TX_LOGTARGET Targets;
    TX_LOGCALLBACK Log;        
    TX_DELETEMODELCALLBACK DeleteModel;
    TX_USERPARAM UserParam;    
} TX_LOGGINGMODEL;
    
/*********************************************************************************************************************/

/**
  Struct for the scheduling model.
   
  @field Mode:
     Specifies which scheduling mode to use. See TX_SCHEDULINGMODE.
    
  @field Schedule:
     Callback function schedule a work item. See TX_SCHEDULEJOBCALLBACK.
    
  @field DeleteModel:
     Callback function used to release the logging model.
    
  @field UserParam:
    User parameter which will be passed to the custom schedule function.
 */
typedef struct {
    TX_SCHEDULINGMODE Mode;
    TX_SCHEDULEJOBCALLBACK ScheduleJob;        
    TX_DELETEMODELCALLBACK DeleteModel;
    TX_USERPARAM UserParam;    
} TX_SCHEDULINGMODEL;
    
/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_CLIENT_TYPES__H__) */

/*********************************************************************************************************************/

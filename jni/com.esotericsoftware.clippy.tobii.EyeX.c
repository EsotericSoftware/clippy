
#include <com.esotericsoftware.clippy.tobii.EyeX.h>

#define WIN32_LEAN_AND_MEAN
#include <windows.h>

#include <stdlib.h>
#include <stdio.h>
#include <conio.h>
#include <assert.h>
#include "eyex/EyeX.h"

static JavaVM* jvm = NULL;
static __declspec(thread) JNIEnv* threadEnv = NULL;

typedef struct EyeX {
	jobject object;
	jmethodID eventMethod;
	jmethodID gazeEventMethod;
	jmethodID eyeEventMethod;
	TX_CONTEXTHANDLE context;
	TX_HANDLE interactorSnapshot;
} EyeX;

typedef enum {
	connectTrying, connected, connectFailed, serverTooHigh, serverTooLow, commitSnapshotFailed, disconnected, eventError
} Event;

/* Initializes the snapshot with an interactor that has the Gaze Point behavior. */
BOOL initializeInteractorSnapshot (EyeX* eyex, const char* interactorID) {
	TX_HANDLE interactor = TX_EMPTY_HANDLE;
	TX_GAZEPOINTDATAPARAMS params = { TX_GAZEPOINTDATAMODE_LIGHTLYFILTERED };
	TX_HANDLE behavior = TX_EMPTY_HANDLE;
	BOOL success;

	success = txCreateGlobalInteractorSnapshot(
		eyex->context,
		interactorID,
		&eyex->interactorSnapshot,
		&interactor
	) == TX_RESULT_OK;
	success &= txCreateGazePointDataBehavior(interactor, &params) == TX_RESULT_OK;

	success &= txCreateInteractorBehavior(interactor, &behavior, TX_BEHAVIORTYPE_EYEPOSITIONDATA) == TX_RESULT_OK;
	txReleaseObject(&behavior);

	txReleaseObject(&interactor);

	return success;
}

void notify (EyeX* eyex, Event event) {
	if (!threadEnv) (*jvm)->AttachCurrentThread(jvm, (void**)&threadEnv, NULL);
	(*threadEnv)->CallVoidMethod(threadEnv, eyex->object, eyex->eventMethod, event);
}

void notifyGaze (EyeX* eyex, TX_GAZEPOINTDATAEVENTPARAMS params) {
	if (!threadEnv) (*jvm)->AttachCurrentThread(jvm, (void**)&threadEnv, NULL);
	(*threadEnv)->CallVoidMethod(threadEnv, eyex->object, eyex->gazeEventMethod,
		params.Timestamp,
		params.X,
		params.Y
	);
}

void notifyEye (EyeX* eyex, TX_EYEPOSITIONDATAEVENTPARAMS params) {
	if (!threadEnv) (*jvm)->AttachCurrentThread(jvm, (void**)&threadEnv, NULL);
	(*threadEnv)->CallVoidMethod(threadEnv, eyex->object, eyex->eyeEventMethod,
		params.Timestamp,
		params.HasLeftEyePosition,
		params.HasRightEyePosition,
		params.LeftEyeX,
		params.LeftEyeY,
		params.LeftEyeZ,
		params.LeftEyeXNormalized,
		params.LeftEyeYNormalized,
		params.LeftEyeZNormalized,
		params.RightEyeX,
		params.RightEyeY,
		params.RightEyeZ,
		params.RightEyeXNormalized,
		params.RightEyeYNormalized,
		params.RightEyeZNormalized
	);
}

/* Callback invoked when a snapshot has been committed. */
void TX_CALLCONVENTION OnSnapshotCommitted (TX_CONSTHANDLE asyncData, TX_USERPARAM userParam) {
	// Check the result code to catch validation errors and runtime errors in debug builds.
	TX_RESULT result = TX_RESULT_UNKNOWN;
	txGetAsyncDataResultCode(asyncData, &result);
	assert(result == TX_RESULT_OK || result == TX_RESULT_CANCELLED);
}

/* Callback invoked when the status of the connection to the EyeX Engine has changed. */
void TX_CALLCONVENTION OnEngineConnectionStateChanged (TX_CONNECTIONSTATE connectionState, TX_USERPARAM userParam) {
	EyeX* eyex = (EyeX*)userParam;

	switch (connectionState) {
	case TX_CONNECTIONSTATE_CONNECTED: {
		notify(eyex, connected);
		// Commit the interactor snapshot as soon as the connection to the engine is established.
		// This cannot be done earlier because committing means "send to the engine".
		if (txCommitSnapshotAsync(eyex->interactorSnapshot, OnSnapshotCommitted, NULL) != TX_RESULT_OK)
			notify(eyex, commitSnapshotFailed);
		break;
	}
	case TX_CONNECTIONSTATE_DISCONNECTED:
		notify(eyex, disconnected);
		break;
	case TX_CONNECTIONSTATE_TRYINGTOCONNECT:
		notify(eyex, connectTrying);
		break;
	case TX_CONNECTIONSTATE_SERVERVERSIONTOOLOW:
		notify(eyex, serverTooLow);
		break;
	case TX_CONNECTIONSTATE_SERVERVERSIONTOOHIGH:
		notify(eyex, serverTooHigh);
		break;
	}
}

/* Callback invoked when an event has been received from the EyeX Engine. */
void TX_CALLCONVENTION HandleEvent (TX_CONSTHANDLE hAsyncData, TX_USERPARAM userParam) {
	EyeX* eyex = (EyeX*)userParam;

	TX_HANDLE event = TX_EMPTY_HANDLE;
	txGetAsyncDataContent(hAsyncData, &event);

	TX_HANDLE behavior = TX_EMPTY_HANDLE;
	if (txGetEventBehavior(event, &behavior, TX_BEHAVIORTYPE_GAZEPOINTDATA) == TX_RESULT_OK) {
		TX_GAZEPOINTDATAEVENTPARAMS params;
		if (txGetGazePointDataEventParams(behavior, &params) == TX_RESULT_OK)
			notifyGaze(eyex, params);
		else
			notify(eyex, eventError);
		txReleaseObject(&behavior);
	}

	behavior = TX_EMPTY_HANDLE;
	if (txGetEventBehavior(event, &behavior, TX_BEHAVIORTYPE_EYEPOSITIONDATA) == TX_RESULT_OK) {
		TX_EYEPOSITIONDATAEVENTPARAMS params;
		if (txGetEyePositionDataEventParams(behavior, &params) == TX_RESULT_OK)
			notifyEye(eyex, params);
		else
			notify(eyex, eventError);
		txReleaseObject(&behavior);
	}
	
	txReleaseObject(&event);
}

JNIEXPORT jlong JNICALL Java_com_esotericsoftware_clippy_tobii_EyeX__1connect (JNIEnv* env, jobject object, jstring j_interactorID) {
	if (!jvm) (*env)->GetJavaVM(env, &jvm);
	threadEnv = env;

	const char* interactorID = (*env)->GetStringUTFChars(env, j_interactorID, 0);

	EyeX* eyex = malloc(sizeof(EyeX));
	eyex->object = (*env)->NewGlobalRef(env, object);
	eyex->eventMethod = (*env)->GetMethodID(env, (*env)->GetObjectClass(env, eyex->object), "event", "(I)V");
	eyex->gazeEventMethod = (*env)->GetMethodID(env, (*env)->GetObjectClass(env, eyex->object), "gazeEvent", "(DDD)V");
	eyex->eyeEventMethod = (*env)->GetMethodID(env, (*env)->GetObjectClass(env, eyex->object), "eyeEvent", "(DZZDDDDDDDDDDDD)V");
	eyex->context = TX_EMPTY_HANDLE;
	eyex->interactorSnapshot = TX_EMPTY_HANDLE;

	BOOL success = txInitializeEyeX(TX_EYEXCOMPONENTOVERRIDEFLAG_NONE, NULL, NULL, NULL, NULL) == TX_RESULT_OK;
	success &= txCreateContext(&eyex->context, TX_FALSE) == TX_RESULT_OK;
	success &= initializeInteractorSnapshot(eyex, interactorID);

	TX_TICKET connectionStateChangedTicket = TX_INVALID_TICKET;
	success &= txRegisterConnectionStateChangedHandler(eyex->context, &connectionStateChangedTicket, OnEngineConnectionStateChanged, eyex) == TX_RESULT_OK;
	
	TX_TICKET eventHandlerTicket = TX_INVALID_TICKET;
	success &= txRegisterEventHandler(eyex->context, &eventHandlerTicket, HandleEvent, eyex) == TX_RESULT_OK;
	
	success &= txEnableConnection(eyex->context) == TX_RESULT_OK;

	(*env)->ReleaseStringUTFChars(env, j_interactorID, interactorID);

	return success ? (jlong)eyex : 0;
}

JNIEXPORT jboolean JNICALL Java_com_esotericsoftware_clippy_tobii_EyeX__1disconnect (JNIEnv* env, jobject object, jlong address) {
	EyeX* eyex = (EyeX*)address;
	
	txDisableConnection(eyex->context);
	txReleaseObject(&eyex->interactorSnapshot);
	BOOL success = txShutdownContext(eyex->context, TX_CLEANUPTIMEOUT_DEFAULT, TX_FALSE) == TX_RESULT_OK;
	success &= txReleaseContext(&eyex->context) == TX_RESULT_OK;
	success &= txUninitializeEyeX() == TX_RESULT_OK;

	(*env)->DeleteGlobalRef(env, object);

	return success;
}

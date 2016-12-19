/*********************************************************************************************************************
 * Copyright 2013-2014 Tobii Technology AB. All rights reserved.
 * EyeXMacros.h
 *********************************************************************************************************************/
 
#if !defined(__TOBII_TX_MACROS__H__)
#define __TOBII_TX_MACROS__H__

/*********************************************************************************************************************/

#ifdef WIN32
	#define TX_CALLCONVENTION __cdecl
	#ifdef TX_STATIC_LIB
		#define TX_API
	#else
		#ifdef TX_EXPORTING
			#define TX_API __declspec(dllexport)
		#else
			#define TX_API __declspec(dllimport)
		#endif /* TX_EXPORTING */
	#endif /* TX_STATIC_LIB */
#else
	#define TX_CALLCONVENTION
	#define TX_API
#endif /* WIN32 */

/*********************************************************************************************************************/

#define TX_PREFIX(prefix, fn) prefix##fn
#define TX_PREFIXSTR2(fn) #fn
#define TX_PREFIXSTR(fn) TX_PREFIXSTR2(tx##fn)

/*********************************************************************************************************************/

/**
 * Common macro used for functions that should be public.
 */
#define TX_API_FUNCTION_BASE(impexp, fn, prefix) \
    impexp TX_RESULT TX_CALLCONVENTION prefix##fn

/**
 * Base macro which generates code common for C and  CLI/C++
 */
#if defined(__cplusplus)
	#define TX_C_BEGIN extern "C" {
	#define TX_C_END }
#else
    #define TX_C_BEGIN
	#define TX_C_END 
#endif

#define _TX_API_FUNCTION(fnName, paramList) \
	TX_C_BEGIN \
		TX_API_FUNCTION_BASE(TX_API,  fnName paramList, tx); \
		typedef TX_RESULT (TX_CALLCONVENTION *fnName##Hook)paramList; \
		TX_API fnName##Hook TX_CALLCONVENTION TX_PREFIX(tx, Set##fnName##Hook)(fnName##Hook); \
	TX_C_END

#define _TX_API_FUNCTION_CPP(fnName, paramList) \
		TX_API_FUNCTION_BASE(TX_API,  fnName paramList, ); \
		typedef TX_RESULT (TX_CALLCONVENTION *fnName##Hook)paramList; \
		TX_API fnName##Hook TX_CALLCONVENTION TX_PREFIX(, Set##fnName##Hook)(fnName##Hook); \
	

/**
 * The final API function generator macro.
 * In C this generates the following:
 *
 * __declspec([dllexport/dllimport]) TX_RESULT __cdecl SomeFunction(parameters);
 * __declspec([dllexport/dllimport]) TX_RESULT __cdecl _SomeFunction(parameters);
 * typedef TX_RESULT (__cdecl *SomeFunctionHook)(parameters);
 * __declspec([dllexport/dllimport]) SetSomeFunctionHook(SomeFunctionHook);
 */
#if !defined(TX_API_FUNCTION)
#define TX_API_FUNCTION(fnName, paramList) \
	_TX_API_FUNCTION(fnName, paramList)
#endif /* !defined(TX_API_FUNCTION) */

#if !defined(TX_API_FUNCTION_CPP)
#define TX_API_FUNCTION_CPP(fnName, paramList) \
    namespace Tx {_TX_API_FUNCTION_CPP(fnName, paramList) }
#endif /* !defined(TX_API_FUNCTION_CPP) */

/**
 * This macro generates the beginning of an enum definition.
 */
#if !defined(TX_BEGIN_ENUM)
#define TX_BEGIN_ENUM(clrName) typedef enum {
#endif /* !defined(TX_BEGIN_ENUM) */

/**
 * This macro generates the end of an enum definition.
 */ 
#if !defined(TX_END_ENUM)
#define TX_END_ENUM(name, clrName) } name;
#endif /* !defined(TX_END_ENUM) */

/**
 * This macro generates the beginning of an enum flags definition.
 */
#if !defined(TX_BEGIN_FLAGS)
#define TX_BEGIN_FLAGS(clrName) TX_BEGIN_ENUM(clrName)
#endif /* !defined(TX_BEGIN_FLAGS) */

/**
 * This macro generates the end of an enum flags definition.
 */ 
#if !defined(TX_END_FLAGS)
#define TX_END_FLAGS(name, clrName) TX_END_ENUM(name, clrName)
#endif /* !defined(TX_END_FLAGS) */


#define TX_ENUM_STARTVALUE 1
#define TX_INTERNAL_ENUM_STARTVALUE 10000000
#define TX_FLAGS_NONE_VALUE 0

/**
 * This macro generates a single value in an enum definition.
 */
#if !defined(TX_ENUM_VALUE)
#define TX_ENUM_VALUE(name, clrName) name
#endif /* !defined(TX_ENUM_VALUE) */

#if !defined(TX_BEGIN_SCOPE)
#define TX_BEGIN_SCOPE(name)
#endif /* !defined(TX_BEGIN_SCOPE) */

#if !defined(TX_END_SCOPE)
#define TX_END_SCOPE
#endif /* !defined(TX_END_SCOPE) */

/**
 * This macro generates the beginning of message tokens definition.
 */
#if !defined(TX_CONSTANTS_BEGIN)
#define TX_CONSTANTS_BEGIN(name)
#endif /* !defined(TX_CONSTANTS_BEGIN) */

/**
 * This macro generates the end of message tokens definition.
 */
#if !defined(TX_CONSTANTS_END)
#define TX_CONSTANTS_END
#endif /* !defined(TX_CONSTANTS_END) */

/**
 * This macro generates a single message token.
 */
#if !defined(TX_LITERALS_VALUE)
#define TX_LITERALS_VALUE(name, clrName, str) static const char* name = str; 
#endif /* !defined(TX_LITERALS_VALUE) */

#if !defined(TX_CONSTANT_INTEGER_VALUE)
#define TX_CONSTANT_INTEGER_VALUE(name, clrName, value) static const int name = value;
#endif /* !defined(TX_CONSTANT_INTEGER_VALUE) */

#if !defined(TX_CONSTANT_REAL_VALUE)
#define TX_CONSTANT_REAL_VALUE(name, clrName, value) static const double name = value;
#endif /* !defined(TX_CONSTANT_REAL_VALUE) */

#if !defined(TX_CONSTANT_BYTE_VALUE)
#define TX_CONSTANT_BYTE_VALUE(name, clrName, value) static const unsigned char name = value;
#endif /* !defined(TX_CONSTANT_BYTE_VALUE) */
 
/**
 * This macro generates code which marks the beginning of a set of public api functions.
 */
#if !defined(TX_API_FUNCTIONS_BEGIN)
#define TX_API_FUNCTIONS_BEGIN 
#endif /* !defined(TX_API_FUNCTIONS_BEGIN) */

/**
 * This macro generates code which marks the end of a set of public api functions.
 */
#if !defined(TX_API_FUNCTIONS_END)
#define TX_API_FUNCTIONS_END
#endif /* !defined(TX_API_FUNCTIONS_END) */

#if !defined(TX_OUT_PARAM)
#define TX_OUT_PARAM(param) param*
#endif /* !defined(TX_OUT_PARAM) */

#if !defined(TX_REF_PARAM)
#define TX_REF_PARAM(param) param*
#endif /* !defined(TX_REF_PARAM) */

#if !defined(TX_IN_PARAM)
#define TX_IN_PARAM(param) const param*
#endif /* !defined(TX_IN_PARAM) */

#if !defined(TX_PTR_PARAM)
#define TX_PTR_PARAM(param) param*
#endif /* !defined(TX_PTR_PARAM) */
 
#if !defined(TX_CONSTPTR_PARAM)
#define TX_CONSTPTR_PARAM(param) const param*
#endif /* !defined(TX_CONSTPTR_PARAM) */

#if !defined(TX_CALLBACK_PARAM) 
#define TX_CALLBACK_PARAM(param) param
#endif /* !defined(TX_CALLBACK_PARAM) */

/*********************************************************************************************************************/

#if !defined(TX_DEFINE_CALLABLE)
#define TX_DEFINE_CALLABLE(name, clrName, returnType, paramList) \
	typedef returnType (TX_CALLCONVENTION *name)paramList;
#endif /* !defined(TX_DEFINE_CALLABLE) */

#if !defined(TX_STRUCT_BEGIN)
#define TX_STRUCT_BEGIN(name, clrName) \
	typedef struct {
#endif /* !defined(TX_STRUCT_BEGIN) */

#if !defined(TX_STRUCT_END)
#define TX_STRUCT_END(name, clrName) \
	} name;
#endif /* !defined(TX_STRUCT_END) */


/*********************************************************************************************************************/

/**
  Macro that generates a type of the specified type.
 */
#if !defined(TX_DEFINE_TYPE)
#define TX_DEFINE_TYPE(type, name) typedef type name;
#endif /* !defined(TX_TRANSLATE_TYPE) */

/*********************************************************************************************************************/

#endif /* !defined(__TOBII_TX_MACROS__H__) */

/*********************************************************************************************************************/

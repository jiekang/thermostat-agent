/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

#include "com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper.h"

#if !defined(TRUE)
# define TRUE 1
#endif

#define STUB_RETURN 0

// the error values aren't important; they just have to be unique for the unit tests
#define STUB_ERROR_CODE 100
#define STUB_ERROR_CODE_BROKEN_PIPE 101
#define STUB_ERROR_CODE_EOF 102
#define STUB_ERROR_CODE_INCOMPLETE 103
#define STUB_ERROR_CODE_MORE_DATA 104
#define STUB_ERROR_CODE_PENDING 105
#define STUB_ERROR_CODE_PIPE_BUSY 106
#define STUB_ERROR_CODE_PIPE_CONNECTED 107

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    createNamedPipe0
 * Signature: (Ljava/lang/String;II)J
 */
JNIEXPORT jlong JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_createNamedPipe0
  (JNIEnv *env, jobject obj, jstring pipeName, jint instances, jint bufsize)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    openNamedPipe0
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_openExistingNamedPipe0
  (JNIEnv *env, jobject obj, jstring pipeName)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    createEvent0
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_createEvent0
  (JNIEnv *env, jobject obj, jboolean manual, jboolean initial)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    resetEvent0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_resetEvent0
  (JNIEnv *env, jobject obj, jlong eventHandle)
{
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    setEvent0
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_setEvent0
  (JNIEnv *env, jobject obj, jlong eventHandle)
{
}

JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getLastError0
  (JNIEnv *env, jobject obj)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    connectNamedPipe0
 * Signature: (JJ[D)Ljava/lang/String;
 *
 * return 0 if sucessfull, ERROR_IO_PENDING if still pending, or GetLastError() if failure
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_connectNamedPipe0
  (JNIEnv *env, jobject obj, jlong pipeHandle, jobject ooverlapped)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    disconnectnamedPipe0
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_disconnectNamedPipe0
  (JNIEnv *env, jobject obj, jlong pipeHandle)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getNamedPipeClientProcessId0
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getNamedPipeClientProcessId0
  (JNIEnv *env, jobject obj, jlong pipeHandle)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    closeHandle0
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_closeHandle0
  (JNIEnv *env, jobject obj, jlong handle)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    waitForMultipleObjects0
 * Signature: (I[JZI)I
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_waitForMultipleObjects0
  (JNIEnv *env, jobject obj, jint numObjects, jlongArray handles, jboolean waitForAll, jint millis)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getOverlappedResult0
 * Signature: (JJ[DZ)J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getOverlappedResult0
  (JNIEnv *env, jobject obj, jlong pipeHandle, jobject ooverlapped, jboolean wait)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    readFileOverlapped0
 * Signature: (JJ[D[BJ)J
 */
JNIEXPORT jboolean JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_readFileOverlapped0
  (JNIEnv *env, jobject obj, jlong handle, jobject ooverlapped, jobject buffer, jint offset, jint bufsize)
{
    return STUB_RETURN;
}


/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    readFile0
 * Signature: (J[BJ)J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_readFile0
  (JNIEnv *env, jobject obj, jlong handle, jbyteArray array, jint offset, jint bufsize)
{
    return STUB_RETURN;
}


/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    writeFileOverlapped0
 * Signature: (JJ[D[BJ)J
 */
JNIEXPORT jboolean JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_writeFileOverlapped0
  (JNIEnv *env, jobject obj, jlong handle, jobject ooverlapped, jobject buffer, jint offset, jint bufsize)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    writeFile0
 * Signature: (J[BJ)J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_writeFile0
  (JNIEnv *env, jobject obj, jlong handle, jobject array, jint offset, jint bufsize)
{
    return STUB_RETURN;
}

JNIEXPORT jlong JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantWaitObject0
  (JNIEnv *env, jobject obj)
{
    return STUB_RETURN;
}

JNIEXPORT jlong JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantInfinite0
  (JNIEnv *env, jobject obj)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getConstantErrorIOPending0
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantErrorIOPending0
  (JNIEnv *env, jobject obj)
{
    return STUB_ERROR_CODE_PENDING;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getConstantErrorIOIncomplete0
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantErrorIOIncomplete0
  (JNIEnv *env, jobject obj)
{
    return STUB_ERROR_CODE_INCOMPLETE;
}
/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getConstantErrorHandleEOF0
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantErrorHandleEOF0
  (JNIEnv *env, jobject obj)
{
    return STUB_ERROR_CODE_EOF;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getConstantErrorMoreData0
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantErrorMoreData0
  (JNIEnv *env, jobject obj)
{
    return STUB_ERROR_CODE_MORE_DATA;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getConstantErrorPipeBusy0
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantErrorPipeBusy0
  (JNIEnv *env, jobject obj)
{
    return STUB_ERROR_CODE_PIPE_BUSY;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getConstantErrorPipeConnected0
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantErrorPipeConnected0
  (JNIEnv *env, jobject obj)
{
    return STUB_ERROR_CODE_PIPE_CONNECTED;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getConstantInvalidHandle0
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantInvalidHandle0
  (JNIEnv *env, jobject obj)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    getConstantErrorBrokenPipe0
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_getConstantErrorBrokenPipe0
  (JNIEnv *env, jobject obj)
{
    return STUB_ERROR_CODE_BROKEN_PIPE;
}


/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    createDirectBuffer0
 * Signature: (I)Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_createDirectBuffer0
  (JNIEnv *env, jobject obj, jint bufsize)
{
    return NULL;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    createDirectOverlapStruct0
 * Signature: ()Ljava/nio/ByteBuffer;
 */
JNIEXPORT jobject JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_createDirectOverlapStruct0
  (JNIEnv *env, jobject obj, jlong eHandle)
{
    return NULL;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    freeDirectBuffer0
 * Signature: (Ljava/nio/ByteBuffer;)V
 */
JNIEXPORT void JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_freeDirectBuffer0
  (JNIEnv *env, jobject obj, jobject bytebuffer)
{
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    cancelIo0
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_cancelIo0
  (JNIEnv *env, jobject obj, jlong pipeHandle)
{
    return STUB_RETURN;
}

/*
 * Class:     com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper
 * Method:    cancelIoEx0
 * Signature: (JLjava/nio/ByteBuffer;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_redhat_thermostat_agent_ipc_winpipes_common_internal_WinPipesNativeHelper_cancelIoEx0
  (JNIEnv *env, jobject obj, jlong pipeHandle, jobject ooverlapped)
{
    return STUB_RETURN;
}

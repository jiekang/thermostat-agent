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

package com.redhat.thermostat.common.portability.internal.windows;

import com.redhat.thermostat.common.portability.internal.PortableNativeLibraryLoader;
import com.redhat.thermostat.common.portability.PortableHost;
import com.redhat.thermostat.shared.config.OS;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to access Windows native code
 */
public class WindowsHelperImpl extends PortableNativeLibraryLoader {

    public static WindowsHelperImpl INSTANCE;

    /*
     // from MemoryStatusEx (8 values)
        DWORD     dwMemoryLoad;
        DWORDLONG ullTotalPhys;
        DWORDLONG ullAvailPhys;
        DWORDLONG ullTotalPageFile;
        DWORDLONG ullAvailPageFile;
        DWORDLONG ullTotalVirtual;
        DWORDLONG ullAvailVirtual;
        DWORDLONG ullAvailExtendedVirtual;

     // from PERFORMANCE_INFORMATION (13 values)
        SIZE_T CommitTotal;
        SIZE_T CommitLimit;
        SIZE_T CommitPeak;
        SIZE_T PhysicalTotal;
        SIZE_T PhysicalAvailable;
        SIZE_T SystemCache;
        SIZE_T KernelTotal;
        SIZE_T KernelPaged;
        SIZE_T KernelNonpaged;
        SIZE_T PageSize;
        DWORD  HandleCount;
        DWORD  ProcessCount;
        DWORD  ThreadCount;
     */

    static {
        INSTANCE = OS.IS_WINDOWS && isNativeLibraryLoaded() ? new WindowsHelperImpl() : null;
    }

    private WindowsHelperImpl() {
    }
    // local host-wide information

    public String getHostName() {
        return getHostName0(true);
    }

    public String getOSName() {
        return System.getProperty("os.name");
    }

    public String getOSVersion() {
        final long info[] = new long[3];  // major, minor, build
        getOSVersion0(info);
        // the build number is not available on newer windows versions (8.1 and up)
        return (info[2] != 0) ? "" + info[0] + "" + info[1] + " (Build " + info[2] + ")" : "" + info[0] + "." + info[1];
    }

    public String getCPUModel() {
        return getCPUString0();
    }

    public int getCPUCount() {
        return getCPUCount0();
    }

    public long getTotalMemory() {
        final long info[] = getMemoryInfo();
        return info[1]; // totalPhysical
    }

    long[] getMemoryInfo() {
        /**
         *
         data[0] = statex.dwMemoryLoad;
         data[1] = statex.ullTotalPhys;
         data[2] = statex.ullAvailPhys;
         data[3] = statex.ullTotalPageFile;
         data[4] = statex.ullAvailPageFile;
         data[5] = statex.ullTotalVirtual;
         data[6] = statex.ullAvailVirtual;
         data[7] = statex.ullAvailExtendedVirtual;
         */
        final long[] mi = new long[8];
        getGlobalMemoryStatus0(mi);
        return mi;
    }

    public long getClockTicksPerSecond() {
        // we "know" this is 10E7 (units from  getProcessStat)
        // but calculate it anyways from current process
        final long[] info = INSTANCE.getProcessCPUInfo(0);
        return info[4];
    }

    // local process-specific information

    public int getCurrentProcessPid() {
        return getCurrentProcessID0();
    }

    public boolean exists(int pid) {
        return exists0(pid) != 0;
    }

    public String getUserName(int pid) {
        return getUserName0(pid,true);
    }

    public int getUid(int pid) {
        final String sid = getProcessSID0(pid);
        if (sid == null) {
            return -1;
        }
        final int idx = sid.lastIndexOf('-');
        final String uidStr = sid.substring(idx+1);
        return Integer.parseInt(uidStr);
    }

    private String extractString( ByteBuffer buff, char terminator) {
        StringBuilder sb = new StringBuilder();
        while (buff.hasRemaining()) {
            char c = buff.getChar();
            if (c == terminator || c == 0) {
                break;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public Map<String, String> getEnvironment(int pid) {
        final long hProcess = pid != 0 ? getProcessHandle0(pid) : getCurrentProcessHandle0();
        final Object obj = getEnvironment0(hProcess, 0);
        closeHandle0(hProcess);
        if (obj == null) {
            return Collections.emptyMap();
        }
        else if (obj instanceof String) {
            System.err.println("env 0 returns " + obj);
        }
        else /* if (obj instanceof ByteBuffer) */ {
            final ByteBuffer buff = (ByteBuffer)(obj);
            final Map<String, String> env = new HashMap<>();
            buff.get();
            while (buff.hasRemaining()) {
                String k = extractString(buff, '=');
                if (k.isEmpty()) {
                    break;
                }
                String v = extractString(buff, '\0');
                env.put(k, v);
            }
            freeDirectBuffer0(buff);
            return env;
        }
        return null;
    }

    public String getCWD(int pid) {
        final long hProcess = pid != 0 ? getProcessHandle0(pid) : getCurrentProcessHandle0();
        final Object obj = getEnvironment0(hProcess, 1);
        closeHandle0(hProcess);
        if (obj == null) {
            return null;
        }
        return (String)(obj);
    }

    public String getExecutable(int pid) {
        final long hProcess = pid != 0 ? getProcessHandle0(pid) : getCurrentProcessHandle0();
        final Object obj = getEnvironment0(hProcess, 2);
        closeHandle0(hProcess);
        if (obj == null) {
            return null;
        }
        return (String)(obj);
    }

    public String getCommandLine(int pid) {
        final long hProcess = pid != 0 ? getProcessHandle0(pid) : getCurrentProcessHandle0();
        final Object obj = getEnvironment0(hProcess, 3);
        closeHandle0(hProcess);
        if (obj == null) {
            return null;
        }
        return (String)(obj);
    }

    /**
     * fetch process counters
     * @param pid process id
     * @return long[] working set size, user time, kernel time, (todo: elapsed time), ticks per second
     */
    long[] getProcessCPUInfo(int pid) {
        final long[] info = new long[5];
        getProcessInfo0(pid, info);
        return info;
    }

    long[] getProcessMemInfo(int pid) {
        final long[] info = new long[5];
        getProcessInfo0(pid, info);
        return info;
    }

    long[] getProcessIOInfo(int pid) {
        final long info[] = new long[6];
        getProcessIOInfo0(pid,info);
        return info;
    }

    /**
     * getSystemTimes()
     * This returns consolidated ticks over all CPUs
     * @return long[3] array of idle, system and user times (in ticks)
     */
    long[] getSystemTimes() {
        final long times[] = new long[PortableHost.CPU_TIMES_SIZE];
        getSystemTimes0(times);
        return times;
    }

    /**
     * getCPUUsagePercent()
     * @return array (each row is a logical processor) of array (idle/system/uyser percents)
     */
    int[][] getCPUUsagePercent() {
        final int numProcessors = getCPUCount();
        final int[][] procs = new int[numProcessors][];
        for (int i=0; i<numProcessors; i++) {
            final int times[] = new int[PortableHost.CPU_TIMES_SIZE];
            procs[i] = times;
        }
        getCPUUsagePercent0(procs);
        return procs;
    }

    public int getLastError() {
        return getLastError0();
    }

    public long getProcessHandle(int pid) {
        return getProcessHandle0(pid);
    }

    public void closeHandle(long handle) {
        closeHandle0(handle);
    }

    public boolean terminateProcess(int pid) {
        return terminateProcess0(pid,0, -1);
    }

    public boolean terminateProcess(int pid, boolean wait) {
        return terminateProcess0(pid,0, 0);
    }

    public boolean terminateProcess(int pid, int exitcode, int waitMillis) {
        return terminateProcess0(pid, exitcode, waitMillis);
    }

    /**
     * returns a list ofprocess modules.
     * @param pid process id (input) if 0, use current process
     * @return array of process moudles (DLLs and executable), module 0 is executable binary.  NULL if there was an error.
     */
    public String[] getProcessModules(int pid) {
        return getModules0(pid);
    }

    private static native String getHostName0(boolean prependDomain);
    private static native void getOSVersion0(long[] versionAndBuild);
    private static native boolean getGlobalMemoryStatus0(long[] info);
    private static native String getCPUString0();
    private static native int getCPUCount0();
    private static native int getCPUUsagePercent0(int[][] percents);

    private static native long queryPerformanceFrequency0();
    private static native void getSystemTimes0(long[] times); // array is idle, kernel, user times

    private static native int getLastError0();

    private static native String getProcessSID0(int pid);
    private static native String getUserName0(int pid, boolean prependDomain);
    private static native boolean getProcessInfo0(int pid, long[] info);
    private static native boolean getProcessIOInfo0(int pid, long[] info);
    private static native int exists0(int pid);

    // mode = 0 returns DirectByteBuffer, 1 = String cwd, 2 = String executable, 3 = String command line
    private static native Object getEnvironment0(long hProcess, int mode);

    private static native String[] getModules0(int pid);

    private static native int getCurrentProcessID0();
    private static native long getCurrentProcessHandle0();
    private static native long getProcessHandle0(int pid);
    private static native long getLimitedProcessHandle0(int pid);
    private static native void closeHandle0(long handle);
    private static native void freeDirectBuffer0(final ByteBuffer byteBuffer);
    private static native boolean terminateProcess0(int pid, int exitCode, int waitMillis);
}

package structure.utils;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public interface WinInet extends StdCallLibrary {
    public WinInet INSTANCE = (WinInet) Native.loadLibrary("WinInet", WinInet.class);
    public boolean InternetGetCookieExW(WString url, WString cookie, byte [] data, IntByReference size, int flags, int reserved);
}
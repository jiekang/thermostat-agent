CC         = gcc
JAVAH      = javah
MYCFLAGS   = -c -Wall $(EXTRA_CFLAGS)
MYLDFLAGS  = -shared $(EXTRA_CFLAGS)
COPY       = cp -a

CLASSPATH  = target/classes/
TARGET_DIR = target
EXECUTABLE = $(SO_PREFIX)thermostat-common-portability$(SO_SUFFIX)

ifeq ($(JNI_PLATFORM),win32)
    JNI_PLATFORM = win32
    SO_PREFIX  =
    SO_SUFFIX  = .dll
else
ifeq ($(JNI_PLATFORM),darwin)
    JNI_PLATFORM = darwin
    SO_PREFIX  = lib
    SO_SUFFIX  = .dylib
    MYCFLAGS   +=
    MYLDFLAGS  +=
else
    JNI_PLATFORM = linux
    SO_PREFIX  = lib
    SO_SUFFIX  = .so
    MYCFLAGS   += -fPIC
    MYLDFLAGS  += -fPIC
endif
endif

INCLUDE    = -I $(TARGET_DIR) -I "$(JAVA_HOME)/include/" -I "$(JAVA_HOME)/include/$(JNI_PLATFORM)"

POSIX_HELPER_SOURCES    = src/main/native/PosixHelperImpl.c
POSIX_HELPER_TARGET     = $(TARGET_DIR)/PosixHelperImpl.c
POSIX_HELPER_OBJECTS    = $(POSIX_HELPER_TARGET:.c=.o)

USERNAME_SOURCES    = src/main/native/UserNameUtilImpl.c
USERNAME_TARGET     = $(TARGET_DIR)/UserNameUtilImpl.c
USERNAME_OBJECTS    = $(USERNAME_TARGET:.c=.o)

ifeq ($(JNI_PLATFORM),win32)
HELPER_SOURCES    = src/main/native/WindowsHelperImpl.c
HELPER_TARGET     = $(TARGET_DIR)/WindowsHelperImpl.c
HELPER_OBJECTS    = $(HELPER_TARGET:.c=.o)
endif

ifeq ($(JNI_PLATFORM),darwin)
HELPER_SOURCES    = src/main/native/MacOSHelperImpl.c
HELPER_TARGET     = $(TARGET_DIR)/MacOSHelperImpl.c
HELPER_OBJECTS    = $(HELPER_TARGET:.c=.o)
endif

EXECUTABLES          = $(EXECUTABLE)

.PHONY:UserNameUtilImpl
JNI_LIST = com.redhat.thermostat.common.portability.internal.PosixHelperImpl com.redhat.thermostat.common.portability.internal.linux.UserNameUtilImpl

ifeq ($(JNI_PLATFORM),win32)
    JNI_LIST     +=  com.redhat.thermostat.common.portability.internal.windows.WindowsHelperImpl
    HELPER_LIBS  += -l psapi -l Netapi32 -l ole32 -l oleaut32 -l wbemuuid -o wmi
endif

ifeq ($(JNI_PLATFORM),darwin)
    JNI_LIST     +=  com.redhat.thermostat.common.portability.internal.macos.MacOSHelperImpl
endif

$(JNI_LIST):
	$(JAVAH) -force -classpath $(CLASSPATH) -d $(TARGET_DIR) $(JNI_LIST)

all: $(JNI_LIST) init $(POSIX_HELPER_SOURCES) $(USERNAME_SOURCES) $(HELPER_SOURCES) $(EXECUTABLES)

.PHONY:
init:
	$(COPY) $(POSIX_HELPER_SOURCES) $(POSIX_HELPER_TARGET)
	$(COPY) $(USERNAME_SOURCES) $(USERNAME_TARGET)
ifneq ($(strip $(HELPER_SOURCES)),)
	$(COPY) $(HELPER_SOURCES) $(HELPER_TARGET)
endif

$(EXECUTABLE): $(POSIX_HELPER_OBJECTS) $(USERNAME_OBJECTS) $(HELPER_OBJECTS)
	$(CC) $(MYLDFLAGS) $(LDFLAGS) $(POSIX_HELPER_OBJECTS) $(USERNAME_OBJECTS) $(HELPER_OBJECTS) $(PLATFORM_LIBS) $(HELPER_LIBS) -o $(TARGET_DIR)/$@

.c.o:
	$(CC) $(MYCFLAGS) $(CFLAGS) $(INCLUDE) $< -o $@

.cpp.o:
	$(CC) $(MYCFLAGS) $(CFLAGS) $(INCLUDE) $< -o $@

clean-lib:
	rm -f $(TARGET_DIR)/$(EXECUTABLE)

clean-obj:
	rm -f $(POSIX_HELPER_OBJECTS)
	rm -f $(USERNAME_OBJECTS)
	rm -f $(HELPER_OBJECTS)

clean: clean-obj clean-lib


#==============================================================================
#            Copyright (c) 2010-2011 QUALCOMM Incorporated.
#            All Rights Reserved.
#            Qualcomm Confidential and Proprietary
#==============================================================================


# An Android.mk file must begin with the definition of the LOCAL_PATH
# variable. It is used to locate source files in the development tree. Here
# the macro function 'my-dir', provided by the build system, is used to return
# the path of the current directory.

QCAR_SDK_ROOT := ../../../../lib/qcar-android
LOCAL_PATH := $(call my-dir)

# The following section is used for copying the libQCAR.so prebuilt library
# into the appropriate folder (libs/armeabi and libs/armeabi-v7a respectively)
# and setting the include path for library-specific header files

include $(CLEAR_VARS)
LOCAL_MODULE := QCAR-prebuilt
LOCAL_SRC_FILES = $(QCAR_SDK_ROOT)/build/lib/$(TARGET_ARCH_ABI)/libQCAR.so
LOCAL_EXPORT_C_INCLUDES := $(QCAR_SDK_ROOT)/build/include
include $(PREBUILT_SHARED_LIBRARY)

#-----------------------------------------------------------------------------

# The CLEAR_VARS variable is provided by the build system and points to a
# special GNU Makefile that will clear many LOCAL_XXX variables for you
# (e.g. LOCAL_MODULE, LOCAL_SRC_FILES, LOCAL_STATIC_LIBRARIES, etc...),
# with the exception of LOCAL_PATH. This is needed because all build
# control files are parsed in a single GNU Make execution context where
# all variables are global. 

include $(CLEAR_VARS)

# The LOCAL_MODULE variable must be defined to identify each module you
# describe in your Android.mk. The name must be *unique* and not contain
# any spaces. Note that the build system will automatically add proper
# prefix and suffix to the corresponding generated file. In other words,
# a shared library module named 'foo' will generate 'libfoo.so'.

LOCAL_MODULE := PositionDetector

# The TARGET_PLATFORM defines the targetted Android Platform API level

TARGET_PLATFORM := android-5
	
# This sample always uses OpenGL ES 2.0.
OPENGLES_LIB  := -lGLESv2
OPENGLES_DEF  := -DUSE_OPENGL_ES_2_0

# An optional set of compiler flags that will be passed when building
# C ***AND*** C++ source files.
#
# NOTE: flag "-Wno-write-strings" removes warning about deprecated conversion
#       from string constant to �char*�

LOCAL_CFLAGS := -Wno-write-strings $(OPENGLES_DEF)

# The list of additional linker flags to be used when building your
# module. This is useful to pass the name of specific system libraries
# with the "-l" prefix.

LOCAL_LDLIBS := \
    -llog $(OPENGLES_LIB)
    
# The list of shared libraries *modules* this module depends on at runtime.
# This is necessary at link time and to embed the corresponding information
# in the generated file. We reference the prebuilt library defined earlier 
# in this makefile.
     
LOCAL_SHARED_LIBRARIES := QCAR-prebuilt

# The LOCAL_SRC_FILES variables must contain a list of C and/or C++ source
# files that will be built and assembled into a module. Note that you should
# not list header and included files here, because the build system will
# compute dependencies automatically for you; just list the source files
# that will be passed directly to a compiler, and you should be good. 	
	
LOCAL_SRC_FILES := PositionDetector.cpp SampleUtils.cpp Texture.cpp

# By default, ARM target binaries will be generated in 'thumb' mode, where
# each instruction are 16-bit wide. You can define this variable to 'arm'
# if you want to force the generation of the module's object files in
# 'arm' (32-bit instructions) mode	

LOCAL_ARM_MODE := arm

# The BUILD_SHARED_LIBRARY is a variable provided by the build system that
# points to a GNU Makefile script that is in charge of collecting all the
# information you defined in LOCAL_XXX variables since the latest
# 'include $(CLEAR_VARS)' and determine what to build, and how to do it
# exactly. There is also BUILD_STATIC_LIBRARY to generate a static library. 

include $(BUILD_SHARED_LIBRARY)

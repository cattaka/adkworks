/*==============================================================================
            Copyright (c) 2010-2011 QUALCOMM Incorporated.
            All Rights Reserved.
            Qualcomm Confidential and Proprietary
            
@file 
    Q_object.h

@brief
    Geometry for the Q object used in the samples.

==============================================================================*/
#ifndef _Q_OBJECT_H_
#define _Q_OBJECT_H_


#define NUM_Q_OBJECT_VERTEX 8
#define NUM_Q_OBJECT_INDEX 8 * 3


static const float QobjectVertices[NUM_Q_OBJECT_VERTEX * 3] =
{
     0, 0, 0,
     2, 0, 0,
     2, 2, 0,
     0, 2, 0,

     0.2, 0.2, 0,
     1.8, 0.2, 0,
     1.8, 1.8, 0,
     0.2, 1.8, 0,
};

static const float QobjectTexCoords[NUM_Q_OBJECT_VERTEX * 2] =
{
	 1, 1,
	-1, 1,
	-1,-1,
	 1,-1,
     0.1, 0.1,
     1.8, 0.0,
     1.8, 1.8,
     0.1, 1.8,
};

static const float QobjectNormals[NUM_Q_OBJECT_VERTEX * 3] =
{
	 0, 0, 1,
	 0, 0, 1,
	 0, 0, 1,
	 0, 0, 1,
	 0, 0, 1,
	 0, 0, 1,
	 0, 0, 1,
	 0, 0, 1,
};

static const unsigned short QobjectIndices[NUM_Q_OBJECT_INDEX] =
{
    0,1,5, 5,4,0,
    1,2,6, 6,5,1,
    2,3,7, 7,6,2,
    3,0,4, 4,7,3.

};


#endif // _Q_OBJECT_H_

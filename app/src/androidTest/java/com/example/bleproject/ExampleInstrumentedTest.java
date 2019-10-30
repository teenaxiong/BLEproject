/*
 * *
 *  * Created by Teena Xiong on 10/30/19 12:03 AM
 *  * Copyright (c) 2019 . All rights reserved.
 *  * Last modified 10/23/19 8:33 PM
 *
 */

package com.example.bleproject;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.example.bleproject", appContext.getPackageName());
    }
}

/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.constraintlayout.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.constraintlayout.core.state.ConstraintSetParser
import androidx.constraintlayout.core.state.CoreMotionScene


/**
 * Information for MotionLayout to animate between multiple [ConstraintSet]s.
 */
@Immutable
interface MotionScene : CoreMotionScene

/**
 * Parses the given JSON5 into a [MotionScene].
 *
 * See the official [Github Wiki](https://github.com/androidx/constraintlayout/wiki/Compose-MotionLayout-JSON-Syntax) to learn the syntax.
 */
@Composable
fun MotionScene(content: String): MotionScene {
    return remember(content) {
        JSONMotionScene(content)
    }
}


internal class JSONMotionScene(content: String) : EditableJSONLayout(content),
    MotionScene {

    private val constraintSetsContent = HashMap<String, String>()
    private val transitionsContent = HashMap<String, String>()
    private var forcedProgress: Float = Float.NaN

    init {
        // call parent init here so that hashmaps are created
        initialization()
    }

    // region Accessors
    override fun setConstraintSetContent(name: String, content: String) {
        constraintSetsContent[name] = content
    }

    override fun setTransitionContent(name: String, content: String) {
        transitionsContent[name] = content
    }

    override fun getConstraintSet(name: String): String? {
        return constraintSetsContent[name]
    }

    override fun getConstraintSet(index: Int): String? {
        return constraintSetsContent.values.elementAtOrNull(index)
    }

    override fun getTransition(name: String?): String? {
        return transitionsContent[name]
    }

    override fun getForcedProgress(): Float {
        return forcedProgress
    }

    override fun resetForcedProgress() {
        forcedProgress = Float.NaN
    }
    // endregion

    // region On Update Methods
    override fun onNewContent(content: String) {
        super.onNewContent(content)
        try {
            ConstraintSetParser.parseMotionSceneJSON(this, content)
        } catch (e: Exception) {
            // nothing (content might be invalid, sent by live edit)
        }
    }

    override fun onNewProgress(progress: Float) {
        forcedProgress = progress
        signalUpdate()
    }
    // endregion
}
